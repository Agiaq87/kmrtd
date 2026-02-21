/*
 * JMRTD - A Java API for accessing machine readable travel documents.
 *
 * Copyright (C) 2006 - 2018  The JMRTD team
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *
 * $Id: Util.java 1902 2025-07-31 07:40:22Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd

import kmrtd.lds.PACEInfo
import kmrtd.lds.SecurityInfo
import kmrtd.lds.icao.MRZInfo
import net.sf.scuba.tlv.TLVInputStream
import net.sf.scuba.tlv.TLVUtil
import org.bouncycastle.asn1.*
import org.bouncycastle.asn1.pkcs.DHParameter
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.asn1.x9.X962NamedCurves
import org.bouncycastle.asn1.x9.X9ECParameters
import org.bouncycastle.asn1.x9.X9ECPoint
import org.bouncycastle.crypto.params.DHParameters
import org.bouncycastle.crypto.params.ECDomainParameters
import org.bouncycastle.crypto.params.ECPrivateKeyParameters
import org.bouncycastle.crypto.params.ECPublicKeyParameters
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec
import org.bouncycastle.jce.spec.ECNamedCurveSpec
import org.bouncycastle.math.ec.ECCurve
import org.bouncycastle.util.encoders.Hex
import java.io.*
import java.math.BigInteger
import java.security.*
import java.security.cert.CertificateFactory
import java.security.interfaces.*
import java.security.spec.*
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import javax.crypto.*
import javax.crypto.interfaces.DHPrivateKey
import javax.crypto.interfaces.DHPublicKey
import javax.crypto.spec.DHParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.math.ceil
import kotlin.math.min

/**
 * Some static helper functions. Mostly dealing with low-level crypto.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1902 $
 */
object Util {
    private val LOGGER: Logger = Logger.getLogger("kmrtd")

    /** Mode for KDF.  */
    const val ENC_MODE: Int = 1
    const val MAC_MODE: Int = 2
    const val PACE_MODE: Int = 3

    /**
     * Returns the BC provider, if present.
     * 
     * @return the BC provider, the SC provider, or `null`
     */
    val bouncyCastleProvider: Provider = BouncyCastleProvider()

    /**
     * Derives the ENC or MAC key for BAC from the keySeed.
     * 
     * @param keySeed the key seed.
     * @param mode either `ENC_MODE` or `MAC_MODE`
     * 
     * @return the key
     * 
     * @throws GeneralSecurityException on security error
     */
    @Throws(GeneralSecurityException::class)
    fun deriveKey(keySeed: ByteArray?, mode: Int): SecretKey {
        return deriveKey(keySeed, "DESede", 128, mode)
    }

    /**
     * Derives the ENC or MAC key for BAC or PACE.
     * 
     * @param keySeed the key seed.
     * @param cipherAlgName either AES or DESede
     * @param keyLength key length in bits
     * @param mode either `ENC_MODE`, `MAC_MODE`, or `PACE_MODE`
     * 
     * @return the key.
     * 
     * @throws GeneralSecurityException on security error
     */
    @Throws(GeneralSecurityException::class)
    fun deriveKey(keySeed: ByteArray?, cipherAlgName: String, keyLength: Int, mode: Int): SecretKey {
        return deriveKey(keySeed, cipherAlgName, keyLength, null, mode)
    }

    /**
     * Derives a shared key.
     * 
     * @param keySeed the shared secret, as octets
     * @param cipherAlg in Java mnemonic notation (for example "DESede", "AES")
     * @param keyLength length in bits
     * @param nonce optional nonce or `null`
     * @param mode the mode either `ENC`, `MAC`, or `PACE` mode
     * 
     * @return the derived key
     * 
     * @throws GeneralSecurityException if something went wrong
     */
    @Throws(GeneralSecurityException::class)
    fun deriveKey(keySeed: ByteArray?, cipherAlg: String, keyLength: Int, nonce: ByteArray?, mode: Int): SecretKey {
        return deriveKey(keySeed, cipherAlg, keyLength, nonce, mode, PassportService.NO_PACE_KEY_REFERENCE)
    }

    /**
     * Derives a shared key.
     * 
     * @param keySeed the shared secret, as octets
     * @param cipherAlg in Java mnemonic notation (for example "DESede", "AES")
     * @param keyLength length in bits
     * @param nonce optional nonce or `null`
     * @param mode the mode either `ENC`, `MAC`, or `PACE` mode
     * @param paceKeyReference Key Reference For Pace Protocol
     * 
     * @return the derived key
     * 
     * @throws GeneralSecurityException if something went wrong
     */
    @Throws(GeneralSecurityException::class)
    fun deriveKey(
        keySeed: ByteArray?,
        cipherAlg: String,
        keyLength: Int,
        nonce: ByteArray?,
        mode: Int,
        paceKeyReference: Byte
    ): SecretKey {
        val digestAlg = inferDigestAlgorithmFromCipherAlgorithmForKeyDerivation(cipherAlg, keyLength)
        val digest = getMessageDigest(digestAlg)
        digest.reset()
        digest.update(keySeed)
        if (nonce != null) {
            digest.update(nonce)
        }
        digest.update(byteArrayOf(0x00, 0x00, 0x00, mode.toByte()))
        val hashResult = digest.digest()
        var keyBytes: ByteArray? = null
        if ("DESede".equals(cipherAlg, ignoreCase = true) || "3DES".equals(cipherAlg, ignoreCase = true)) {
            /* TR-SAC 1.01, 4.2.1. */
            when (keyLength) {
                112, 128 -> {
                    keyBytes = ByteArray(24)
                    System.arraycopy(hashResult, 0, keyBytes, 0, 8) /* E  (octets 1 to 8) */
                    System.arraycopy(hashResult, 8, keyBytes, 8, 8) /* D  (octets 9 to 16) */
                    System.arraycopy(
                        hashResult,
                        0,
                        keyBytes,
                        16,
                        8
                    ) /* E (again octets 1 to 8, i.e. 112-bit 3DES key) */
                }

                else -> throw IllegalArgumentException("KDF can only use DESede with 128-bit key length")
            }
        } else if ("AES".equals(cipherAlg, ignoreCase = true) || cipherAlg.startsWith("AES")) {
            /* TR-SAC 1.01, 4.2.2. */
            when (keyLength) {
                128 -> {
                    keyBytes = ByteArray(16) /* NOTE: 128 = 16 * 8 */
                    System.arraycopy(hashResult, 0, keyBytes, 0, 16)
                }

                192 -> {
                    keyBytes = ByteArray(24) /* NOTE: 192 = 24 * 8 */
                    System.arraycopy(hashResult, 0, keyBytes, 0, 24)
                }

                256 -> {
                    keyBytes = ByteArray(32) /* NOTE: 256 = 32 * 8 */
                    System.arraycopy(hashResult, 0, keyBytes, 0, 32)
                }

                else -> throw IllegalArgumentException("KDF can only use AES with 128-bit, 192-bit key or 256-bit length, found: $keyLength-bit key length")
            }
        }

        if (paceKeyReference == PassportService.NO_PACE_KEY_REFERENCE) {
            return SecretKeySpec(keyBytes, cipherAlg)
        } else {
            return PACESecretKeySpec(keyBytes!!, cipherAlg, paceKeyReference)
        }
    }

    /**
     * Computes the static key seed, based on information from the MRZ.
     * 
     * @param documentNumber a string containing the document number
     * @param dateOfBirth a string containing the date of birth (YYMMDD)
     * @param dateOfExpiry a string containing the date of expiry (YYMMDD)
     * @param digestAlg a Java mnemonic algorithm string to indicate the digest algorithm (typically SHA-1)
     * @param doTruncate whether to truncate the resulting output to 16 bytes
     * 
     * @return a byte array of length 16 containing the key seed
     * 
     * @throws GeneralSecurityException on security error
     */
    @Throws(GeneralSecurityException::class)
    fun computeKeySeed(
        documentNumber: String?,
        dateOfBirth: String?,
        dateOfExpiry: String?,
        digestAlg: String,
        doTruncate: Boolean
    ): ByteArray {
        val text = StringBuilder()
            .append(documentNumber)
            .append(MRZInfo.checkDigit(documentNumber))
            .append(dateOfBirth)
            .append(MRZInfo.checkDigit(dateOfBirth))
            .append(dateOfExpiry)
            .append(MRZInfo.checkDigit(dateOfExpiry))
            .toString()

        return computeKeySeed(text, digestAlg, doTruncate)
    }

    /**
     * Computes the key seed from a card access number (CAN) to derive
     * secure messaging keys from.
     * 
     * @param cardAccessNumber the card access number
     * @param digestAlg the digest algorithm to use
     * @param doTruncate whether to truncate to 16 bytes or not
     * 
     * @return the resulting key seed
     * 
     * @throws GeneralSecurityException on error
     */
    @Throws(GeneralSecurityException::class)
    fun computeKeySeed(cardAccessNumber: String, digestAlg: String, doTruncate: Boolean): ByteArray {
        val shaDigest = MessageDigest.getInstance(digestAlg)

        shaDigest.update(getBytes(cardAccessNumber))

        val hash = shaDigest.digest()

        if (doTruncate) {
            val keySeed = ByteArray(16)
            System.arraycopy(hash, 0, keySeed, 0, 16)
            return keySeed
        } else {
            return hash
        }
    }

    /**
     * Pads the input `in` according to ISO9797-1 padding method 2,
     * using the given block size.
     * 
     * @param in input
     * @param blockSize the block size
     * 
     * @return padded bytes
     */
    fun pad(`in`: ByteArray, blockSize: Int): ByteArray {
        return pad(`in`, 0, `in`.size, blockSize)
    }

    /**
     * Pads the input `bytes` indicated by `offset` and `length`
     * according to ISO9797-1 padding method 2, using the given block size in `blockSize`.
     * 
     * @param bytes input
     * @param offset the offset
     * @param length the length
     * @param blockSize the block size
     * 
     * @return padded bytes
     */
    fun pad(bytes: ByteArray, offset: Int, length: Int, blockSize: Int): ByteArray {
        val outputStream = ByteArrayOutputStream()
        outputStream.write(bytes, offset, length)
        outputStream.write(0x80.toByte().toInt())
        while (outputStream.size() % blockSize != 0) {
            outputStream.write(0x00.toByte().toInt())
        }
        return outputStream.toByteArray()
    }

    /**
     * Unpads the input `bytes` according to ISO9797-1 padding method 2.
     * 
     * @param bytes the input
     * 
     * @return the unpadded bytes
     * 
     * @throws BadPaddingException on padding exception
     */
    @Throws(BadPaddingException::class)
    fun unpad(bytes: ByteArray): ByteArray {
        var i = bytes.size - 1
        while (i >= 0 && bytes[i].toInt() == 0x00) {
            i--
        }
        if ((bytes[i].toInt() and 0xFF) != 0x80) {
            throw BadPaddingException("Expected constant 0x80, found 0x" + Integer.toHexString((bytes[i].toInt() and 0x000000FF)))
        }
        val out = ByteArray(i)
        System.arraycopy(bytes, 0, out, 0, i)
        return out
    }

    /**
     * Recovers the M1 part of the message sent back by the AA protocol
     * (INTERNAL AUTHENTICATE command). The algorithm is described in
     * ISO 9796-2:2002 9.3.
     * 
     * @param digestLength should be 20
     * @param decryptedResponse response from card, already 'decrypted' (using the AA public key)
     * 
     * @return the m1 part of the message
     */
    fun recoverMessage(digestLength: Int, decryptedResponse: ByteArray): ByteArray {
        require(!(decryptedResponse == null || decryptedResponse.size < 1)) { "Plaintext is too short to recover message" }

        /* Trailer. */
        if (((decryptedResponse[decryptedResponse.size - 1].toInt() and 0xF) xor 0xC) != 0) {
            /*
       * Trailer.
       * NOTE: 0xF = 0000 1111, 0xC = 0000 1100.
       */
            throw NumberFormatException("Could not get M1, malformed trailer")
        }

        var trailerLength = 1
        /* Trailer. Find out whether this is t=1 or t=2. */
        if (((decryptedResponse[decryptedResponse.size - 1].toInt() and 0xFF) xor 0xBC) == 0) {
            /* Option 1 (t = 1): the trailer shall consist of a single octet; this octet shall be equal to hexadecimal 'BC'. */
            trailerLength = 1
        } else if (((decryptedResponse[decryptedResponse.size - 1].toInt() and 0xFF) xor 0xCC) == 0) {
            /*
       * Option 2 (t = 2): the trailer shall consist of two consecutive octets;
       * the rightmost octet shall be equal hexadecimal 'CC' and the leftmost octet shall be the hash-function identifier.
       * The hash-function identifier indicates the hash-function in use.
       */
            trailerLength = 2
        } else {
            throw NumberFormatException("Not an ISO 9796-2 scheme 2 signature trailer")
        }

        /* Header. */
        if (((decryptedResponse[0].toInt() and 0xC0) xor 0x40) != 0) {
            /*
       * First two bits (working from left to right) should be '01'.
       * NOTE: 0xC0 = 1100 0000, 0x40 = 0100 0000.
       */
            throw NumberFormatException("Could not get M1")
        }
        if ((decryptedResponse[0].toInt() and 0x20) == 0) {
            /* Third bit (working from left to right) should be '1' for partial recovery. */
            throw NumberFormatException(
                "Could not get M1, first byte indicates partial recovery not enabled: " + Integer.toHexString(
                    decryptedResponse[0].toInt()
                )
            )
        }

        /* Padding to the left of M1, find out how long. */
        var paddingLength = 0
        while (paddingLength < decryptedResponse.size) {
            // 0x0A = 0000 1010
            if (((decryptedResponse[paddingLength].toInt() and 0x0F) xor 0x0A) == 0) {
                break
            }
            paddingLength++
        }
        val messageOffset = paddingLength + 1

        val paddedMessageLength = decryptedResponse.size - trailerLength - digestLength
        val messageLength = paddedMessageLength - messageOffset

        /* There must be at least one byte of message string. */
        if (messageLength <= 0) {
            throw NumberFormatException("Could not get M1")
        }

        /* TODO: If we contain the whole message as well, check the hash of that. */
        val recoveredMessage = ByteArray(messageLength)
        System.arraycopy(decryptedResponse, messageOffset, recoveredMessage, 0, messageLength)

        return recoveredMessage
    }

    /**
     * For ECDSA the EAC 1.11 specification requires the signature to be stripped down from any ASN.1 wrappers, as so.
     * 
     * @param signedData signed data
     * @param keySize key size
     * 
     * @return signature without wrappers
     * 
     * @throws IOException on error
     */
    @Throws(IOException::class)
    fun getRawECDSASignature(signedData: ByteArray, keySize: Int): ByteArray {
        val asn1In = ASN1InputStream(signedData, true)
        val out = ByteArrayOutputStream()
        try {
            val obj = asn1In.readObject() as ASN1Sequence
            val e: Enumeration<ASN1Primitive?> = obj.getObjects()
            while (e.hasMoreElements()) {
                val i = e.nextElement() as ASN1Integer
                var t = i.getValue().toByteArray()
                t = alignKeyDataToSize(t, keySize)
                out.write(t)
            }
            out.flush()
            return out.toByteArray()
        } finally {
            asn1In.close()
            out.close()
        }
    }

    /* FIXME: improve documentation. Is used in PACE, EAC-CA, EAC-TA. -- MO */
    /**
     * Align the given key data.
     * 
     * @param keyData the key data
     * @param size the new size
     * 
     * @return a byte array with key data
     */
    fun alignKeyDataToSize(keyData: ByteArray, size: Int): ByteArray {
        var size = size
        val result = ByteArray(size)
        if (keyData.size < size) {
            size = keyData.size
        }
        System.arraycopy(keyData, keyData.size - size, result, result.size - size, size)
        return result
    }

    /**
     * Converts an integer to an octet string.
     * Based on BSI TR 03111 Section 3.1.2.
     * 
     * @param val a non-negative integer
     * @param length the desired length of the octet string
     * 
     * @return octet string
     */
    fun i2os(`val`: BigInteger, length: Int): ByteArray {
        var `val` = `val`
        val base = BigInteger.valueOf(256)
        val result = ByteArray(length)
        for (i in 0..<length) {
            val remainder = `val`.mod(base)
            `val` = `val`.divide(base)
            result[length - 1 - i] = remainder.toInt().toByte()
        }
        return result
    }

    /**
     * Converts a non-negative integer to an octet string.
     * 
     * @param val non-negative integer
     * 
     * @return the octet string
     */
    fun i2os(`val`: BigInteger): ByteArray {
        var sizeInNibbles = `val`.toString(16).length
        if (sizeInNibbles % 2 != 0) {
            sizeInNibbles++
        }
        val length = sizeInNibbles / 2
        return i2os(`val`, length)
    }

    /**
     * Converts an octet string to an integer.
     * Based on BSI TR 03111 Section 3.1.2.
     * 
     * @param bytes octet string
     * 
     * @return a non-negative integer
     */
    fun os2i(bytes: ByteArray): BigInteger {
        requireNotNull(bytes)
        return os2i(bytes, 0, bytes.size)
    }

    /**
     * Converts an octet string to an integer.
     * Based on BSI TR 03111 Section 3.1.2.
     * 
     * @param bytes a byte array containing the octet string
     * @param offset the offset of the octet string within the given byte array
     * @param length the length of the octet string
     * 
     * @return a non-negative integer
     */
    fun os2i(bytes: ByteArray, offset: Int, length: Int): BigInteger {
        requireNotNull(bytes)

        var result = BigInteger.ZERO
        val base = BigInteger.valueOf(256)
        for (i in offset..<offset + length) {
            result = result.multiply(base)
            result = result.add(BigInteger.valueOf((bytes[i].toInt() and 0xFF).toLong()))
        }

        return result
    }

    /**
     * Converts an octet string to a field element via OS2FE as specified in BSI TR-03111.
     * 
     * @param bytes octet string
     * @param p the modulus
     * 
     * @return a non-negative integer modulo p
     */
    fun os2fe(bytes: ByteArray, p: BigInteger?): BigInteger {
        return os2i(bytes).mod(p)
    }

    /**
     * Infers a digest algorithm mnemonic from a signature algorithm mnemonic.
     * 
     * @param signatureAlgorithm a signature algorithm
     * 
     * @return a digest algorithm, or `null` if inference failed
     */
    fun inferDigestAlgorithmFromSignatureAlgorithm(signatureAlgorithm: String): String? {
        requireNotNull(signatureAlgorithm)

        var digestAlgorithm: String? = null
        val signatureAlgorithmToUppercase = signatureAlgorithm.uppercase(Locale.getDefault())
        if (signatureAlgorithmToUppercase.contains("WITH")) {
            val components: Array<String?> =
                signatureAlgorithmToUppercase.split("WITH".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            digestAlgorithm = components[0]
        }

        if ("SHA1".equals(digestAlgorithm, ignoreCase = true)) {
            return "SHA-1"
        }
        if ("SHA224".equals(digestAlgorithm, ignoreCase = true)) {
            return "SHA-224"
        }
        if ("SHA256".equals(digestAlgorithm, ignoreCase = true)) {
            return "SHA-256"
        }
        if ("SHA384".equals(digestAlgorithm, ignoreCase = true)) {
            return "SHA-384"
        }
        if ("SHA512".equals(digestAlgorithm, ignoreCase = true)) {
            return "SHA-512"
        }

        return digestAlgorithm
    }

    /**
     * Infers a digest algorithm mnemonic from a signature algorithm mnemonic for
     * use in key derivation.
     * 
     * @param cipherAlg a cipher algorithm
     * @param keyLength the key length
     * 
     * @return a (best effort approximation) digest algorithm that is typically used in conjunction
     * with the given cipher algorithm and key length, or `null` if inference failed
     */
    fun inferDigestAlgorithmFromCipherAlgorithmForKeyDerivation(cipherAlg: String, keyLength: Int): String {
        requireNotNull(cipherAlg)
        if ("DESede" == cipherAlg || "AES-128" == cipherAlg) {
            return "SHA-1"
        }
        if ("AES" == cipherAlg && keyLength == 128) {
            return "SHA-1"
        }
        if ("AES-256" == cipherAlg || "AES-192" == cipherAlg) {
            return "SHA-256"
        }
        if ("AES" == cipherAlg && (keyLength == 192 || keyLength == 256)) {
            return "SHA-256"
        }

        throw IllegalArgumentException("Unsupported cipher algorithm or key length \"" + cipherAlg + "\", " + keyLength)
    }

    /**
     * Returns a Difie-Hellman parameter specification which includes
     * the prime order of the subgroup generated by the generator if this
     * information is available in the given (Bouncy Castle) parameters.
     * 
     * @param params parameters for Diffie-Hellman as a Bouncy Castle specific object.
     * 
     * @return a JCE Diffie-Hellman parameter specification
     */
    fun toExplicitDHParameterSpec(params: DHParameters): DHParameterSpec {
        val p = params.getP()
        val generator = params.getG()
        val q = params.getQ()
        val order = params.getL()
        if (q == null) {
            return DHParameterSpec(p, generator, order)
        } else {
            return PACEInfo.DHCParameterSpec(p, generator, q)
        }
    }

    /**
     * Returns detailed information about the given public key (like RSA or) with some extra
     * information (like 1024 bits).
     * 
     * @param publicKey a public key
     * 
     * @return the algorithm
     */
    fun getDetailedPublicKeyAlgorithm(publicKey: PublicKey?): String? {
        if (publicKey == null) {
            return "null"
        }

        var algorithm = publicKey.algorithm
        if (publicKey is RSAPublicKey) {
            val rsaPublicKey = publicKey
            val bitLength = rsaPublicKey.modulus.bitLength()
            algorithm += " [$bitLength bit]"
        } else if (publicKey is ECPublicKey) {
            val ecPublicKey = publicKey
            val ecParams = ecPublicKey.params
            val name = getCurveName(ecParams)
            if (name != null) {
                algorithm += " [$name]"
            }
        } else if (publicKey is DHPublicKey) {
            val dhPublicKey = publicKey
            dhPublicKey.y
            val dhParamSpec = dhPublicKey.params
            val g = dhParamSpec.g
            val l = dhParamSpec.l
            val p = dhParamSpec.p
            algorithm += " [p.length = " + p.bitLength() + ", g.length = " + g.bitLength() + ", l = " + l + "]"
        }

        return algorithm
    }

    /**
     * Returns detailed algorithm information (including key length) about the given private key.
     * 
     * @param privateKey a private key
     * 
     * @return detailed information about the given private key
     */
    fun getDetailedPrivateKeyAlgorithm(privateKey: PrivateKey?): String? {
        if (privateKey == null) {
            return "null"
        }

        var algorithm = privateKey.getAlgorithm()
        if (privateKey is RSAPrivateKey) {
            val rsaPrivateKey = privateKey
            val bitLength = rsaPrivateKey.getModulus().bitLength()
            algorithm += " [$bitLength bit]"
        } else if (privateKey is ECPrivateKey) {
            val ecPrivateKey = privateKey
            val ecParams = ecPrivateKey.getParams()
            val name = getCurveName(ecParams)
            if (name != null) {
                algorithm += " [$name]"
            }
        }
        return algorithm
    }

    /**
     * Returns a JCE parameter specification for the given named curve, or `null`
     * 
     * @param curveName a curve name
     * 
     * @return the JCE parameter specification
     */
    fun getECParameterSpec(curveName: String?): ECParameterSpec {
        val bcParamSpec = ECNamedCurveTable.getParameterSpec(curveName)
        return ECNamedCurveSpec(
            bcParamSpec.name,
            bcParamSpec.curve,
            bcParamSpec.g,
            bcParamSpec.n,
            bcParamSpec.h,
            bcParamSpec.seed
        )
    }

    /**
     * Returns the approximate size of signatures made or verifiable using that key in bits.
     * The key should be a public or private key suitable for signature creation or verfication.
     * For RSA this is just `N`.
     * For ECDSA this is `2 * N` (but the actual size of signatures will be slightly larger).
     * Silently returns `0` for unsupported key types (such as DSA).
     * 
     * @param key the key
     * 
     * @return the size in bits
     */
    fun getApproximateSignatureSize(key: Key?): Int {
        if (key is RSAPublicKey) {
            return key.modulus.bitLength()
        } else if (key is RSAPrivateKey) {
            return key.modulus.bitLength()
        } else if (key is ECPublicKey) {
            val keySize = ceil(key.params.curve.field.fieldSize.toDouble()).toInt()
            return 2 * keySize
        } else if (key is ECPrivateKey) {
            val keySize = ceil(key.params.curve.field.fieldSize.toDouble()).toInt()
            return 2 * keySize
        }

        LOGGER.warning("Unknown key type, returning 0")
        return 0
    }

    /**
     * Returns the curve name, if known, or `null`.
     * 
     * @param params an specification of the curve
     * 
     * @return the curve name
     */
    fun getCurveName(params: ECParameterSpec?): String? {
        val namedECParams = toNamedCurveSpec(params)
        if (namedECParams == null) {
            return null
        }

        return namedECParams.getName()
    }

    /**
     * Translates (named) curve specification to JCA compliant explicit parameter specification.
     * 
     * @param parameterSpec a BC named curve parameter specification
     * 
     * @return a JCA compliant explicit parameter specification
     */
    fun toExplicitECParameterSpec(parameterSpec: ECNamedCurveParameterSpec): ECParameterSpec {
        return toExplicitECParameterSpec(toECNamedCurveSpec(parameterSpec))
    }

    /**
     * Translates (named) curve specification to JCA compliant explicit param specification.
     * 
     * @param params an EC parameter specification, possibly named
     * 
     * @return another specification not name based
     */
    fun toExplicitECParameterSpec(params: ECParameterSpec?): ECParameterSpec {
        if (params == null) {
            return null
        }
        try {
            val g = params.generator
            val n = params.order // Order, order
            val h = params.cofactor // co-factor
            val curve = params.curve
            val a = curve.a
            val b = curve.b
            val field = curve.field
            if (field is ECFieldFp) {
                val p = field.p
                val resultField: ECField = ECFieldFp(p)
                val resultCurve = EllipticCurve(resultField, a, b)
                return ECParameterSpec(resultCurve, g, n, h)
            } else if (field is ECFieldF2m) {
                val m = field.m
                val resultField: ECField = ECFieldF2m(m)
                val resultCurve = EllipticCurve(resultField, a, b)
                return ECParameterSpec(resultCurve, g, n, h)
            } else {
                LOGGER.warning("Could not make named EC param spec explicit")
                return params
            }
        } catch (e: Exception) {
            LOGGER.log(Level.WARNING, "Could not make named EC param spec explicit", e)
            return params
        }
    }

    /**
     * Converts the given EC parameter specification to a BC named curve specification if known.
     * 
     * @param ecParamSpec the JCA EC parameter specification, possibly explicit
     * 
     * @return a BC named curve specification if recognized, or `null` if not
     */
    private fun toNamedCurveSpec(ecParamSpec: ECParameterSpec?): ECNamedCurveSpec? {
        if (ecParamSpec == null) {
            return null
        }
        if (ecParamSpec is ECNamedCurveSpec) {
            return ecParamSpec
        }

        val names: MutableList<String?> = Collections.list<Any?>(ECNamedCurveTable.getNames())
        val namedSpecs: MutableList<ECNamedCurveSpec?> = ArrayList<ECNamedCurveSpec?>()
        for (name in names) {
            val namedSpec = toECNamedCurveSpec(ECNamedCurveTable.getParameterSpec(name))
            if (namedSpec.curve == ecParamSpec.curve
                && namedSpec.generator == ecParamSpec.generator
                && namedSpec.order == ecParamSpec.order
                && namedSpec.cofactor == ecParamSpec.cofactor
            ) {
                namedSpecs.add(namedSpec)
            }
        }
        if (namedSpecs.isEmpty()) {
            return null
        } else if (namedSpecs.size == 1) {
            return namedSpecs[0]
        } else {
            return namedSpecs[0]
        }
    }

    /**
     * Translates internal BC named curve spec to BC provided JCA compliant named curve spec.
     * 
     * @param namedParamSpec a named EC parameter spec
     * 
     * @return a JCA compliant named EC parameter spec
     */
    fun toECNamedCurveSpec(namedParamSpec: ECNamedCurveParameterSpec): ECNamedCurveSpec {
        val name = namedParamSpec.name
        val curve = namedParamSpec.curve
        val generator = namedParamSpec.g
        val order = namedParamSpec.n
        val coFactor = namedParamSpec.h
        val seed = namedParamSpec.seed
        return ECNamedCurveSpec(name, curve, generator, order, coFactor, seed)
    }

    /*
   * NOTE: Woj, I moved this here from DG14File, seemed more appropriate here. -- MO
   * FIXME: Do we still need this now that we have reconstructPublicKey? -- MO
   *
   * Woj says: Here we need to some hocus-pokus, the EAC specification require for
   * all the key information to include the domain parameters explicitly. This is
   * not what Bouncy Castle does by default. But we first have to check if this is
   * the case.
   */
    /**
     * Convert the given JCA compliant public key to a BC subject public key info structure.
     * 
     * @param publicKey a public key
     * 
     * @return a BC subject public key info structure
     */
    fun toSubjectPublicKeyInfo(publicKey: PublicKey): SubjectPublicKeyInfo? {
        try {
            val algorithm = publicKey.algorithm
            if ("EC" == algorithm || "ECDH" == algorithm || (publicKey is ECPublicKey)) {
                val asn1In = ASN1InputStream(publicKey.encoded, true)
                try {
                    var subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(asn1In.readObject())
                    val algorithmIdentifier = subjectPublicKeyInfo!!.algorithm
                    val algOID = algorithmIdentifier.algorithm.getId()
                    check(SecurityInfo.ID_EC_PUBLIC_KEY == algOID) { "Was expecting id-ecPublicKey (" + SecurityInfo.ID_EC_PUBLIC_KEY_TYPE + "), found " + algOID }
                    val derEncodedParams = algorithmIdentifier.parameters.toASN1Primitive()
                    var params: X9ECParameters? = null
                    if (derEncodedParams is ASN1ObjectIdentifier) {
                        val paramsOID = derEncodedParams

                        /* It's a named curve from X9.62. */
                        params = X962NamedCurves.getByOID(paramsOID)
                        checkNotNull(params) { "Could not find X9.62 named curve for OID " + paramsOID.getId() }

                        /* Reconstruct the parameters. */
                        var generator = params.g
                        val curve = generator.getCurve()
                        generator = curve.createPoint(
                            generator.getAffineXCoord().toBigInteger(),
                            generator.getAffineYCoord().toBigInteger()
                        )
                        params = X9ECParameters(
                            params.curve,
                            X9ECPoint(generator, false),
                            params.n,
                            params.h,
                            params.seed
                        )
                    } else {
                        /* It's not a named curve, we can just return the decoded public key info. */
                        return subjectPublicKeyInfo
                    }

                    if (publicKey is org.bouncycastle.jce.interfaces.ECPublicKey) {
                        val ecPublicKey = publicKey
                        val id = AlgorithmIdentifier(
                            subjectPublicKeyInfo.algorithm.algorithm,
                            params.toASN1Primitive()
                        )
                        val q = ecPublicKey.q
                        subjectPublicKeyInfo = SubjectPublicKeyInfo(id, q.getEncoded(false))
                        return subjectPublicKeyInfo
                    } else {
                        return subjectPublicKeyInfo
                    }
                } finally {
                    asn1In.close()
                }
            } else if ("DH" == algorithm || (publicKey is DHPublicKey)) {
                val asn1In = ASN1InputStream(publicKey.encoded, true)
                try {
                    val subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance((asn1In.readObject()))
                    val algorithmIdentifier = subjectPublicKeyInfo!!.algorithm

                    val dhPublicKey = publicKey as DHPublicKey
                    val dhSpec = dhPublicKey.params
                    return SubjectPublicKeyInfo(
                        AlgorithmIdentifier(
                            algorithmIdentifier.algorithm,
                            DHParameter(dhSpec.p, dhSpec.g, dhSpec.l).toASN1Primitive()
                        ),
                        ASN1Integer(dhPublicKey.y)
                    )
                } finally {
                    asn1In.close()
                }
            } else {
                throw IllegalArgumentException("Unrecognized key type, found " + publicKey.algorithm + ", should be DH or ECDH")
            }
        } catch (e: Exception) {
            LOGGER.log(Level.WARNING, "Exception", e)
            return null
        }
    }

    /**
     * Extracts a public key from a BC subject public key info structure.
     * 
     * @param subjectPublicKeyInfo the BC subject public key info structure
     * 
     * @return a public key or `null`
     */
    fun toPublicKey(subjectPublicKeyInfo: SubjectPublicKeyInfo): PublicKey? {
        try {
            val encodedPublicKeyInfoBytes = subjectPublicKeyInfo.getEncoded(ASN1Encoding.DER)
            val keySpec: KeySpec = X509EncodedKeySpec(encodedPublicKeyInfoBytes)
            try {
                val factory = KeyFactory.getInstance("DH")
                return factory.generatePublic(keySpec)
            } catch (gse: GeneralSecurityException) {
                LOGGER.log(Level.FINE, "Not DH public key? Fine, try EC public key", gse)
                val factory: KeyFactory = KeyFactory.getInstance("EC", bouncyCastleProvider)
                return factory.generatePublic(keySpec)
            }
        } catch (gse2: GeneralSecurityException) {
            LOGGER.log(Level.WARNING, "Exception", gse2)
            return null
        } catch (e: Exception) {
            LOGGER.log(Level.WARNING, "Exception", e)
            return null
        }
    }

    /**
     * Reconstructs the public key to use explicit domain params for EC public keys.
     * 
     * @param publicKey the public key
     * 
     * @return the same public key (if not EC or error), or a reconstructed one (if EC)
     */
    fun reconstructPublicKey(publicKey: PublicKey?): PublicKey? {
        if (publicKey !is ECPublicKey) {
            return publicKey
        }

        try {
            val ecPublicKey = publicKey
            val w = ecPublicKey.w
            var params = ecPublicKey.params
            params = toExplicitECParameterSpec(params)
            val explicitPublicKeySpec = ECPublicKeySpec(w, params)

            return KeyFactory.getInstance("EC", bouncyCastleProvider).generatePublic(explicitPublicKeySpec)
        } catch (e: Exception) {
            LOGGER.log(Level.WARNING, "Could not make public key param spec explicit", e)
            return publicKey
        }
    }

    /**
     * Attempts to add missing parameters to a public key.
     * If the public key already has appropriate parameters, then this does nothing.
     * 
     * @param params the parameter spec
     * @param publicKey the public key
     * 
     * @return a public key with possibly added parameters
     */
    fun addMissingParametersToPublicKey(params: AlgorithmParameterSpec?, publicKey: PublicKey?): PublicKey? {
        if (publicKey == null) {
            return null
        }
        try {
            val algorithm = publicKey.algorithm
            if ("EC" == algorithm || "ECDSA" == algorithm || "ECDH" == algorithm) {
                if (params !is ECParameterSpec) {
                    return publicKey
                }

                val ecPublicKey = publicKey as ECPublicKey
                val w = ecPublicKey.w
                val explicitPublicKeySpec = ECPublicKeySpec(w, params)

                return KeyFactory.getInstance("EC", bouncyCastleProvider).generatePublic(explicitPublicKeySpec)
            }
        } catch (e: Exception) {
            LOGGER.log(Level.WARNING, "Could not make public key param spec explicit", e)
            return publicKey
        }

        return publicKey
    }

    /**
     * Decodes an EC point from a BSI encoded octet string.
     * 
     * @param encodedECPoint the encoded EC point
     * 
     * @return the EC point
     */
    fun os2ECPoint(encodedECPoint: ByteArray): ECPoint {
        val dataIn = DataInputStream(ByteArrayInputStream(encodedECPoint))
        try {
            val b = dataIn.read()
            require(b == 0x04) { "Expected encoded ECPoint to start with 0x04" }
            val length = (encodedECPoint.size - 1) / 2
            val xCoordBytes = ByteArray(length)
            val yCoordBytes = ByteArray(length)
            dataIn.readFully(xCoordBytes)
            dataIn.readFully(yCoordBytes)
            dataIn.close()
            val x = os2i(xCoordBytes)
            val y = os2i(yCoordBytes)
            return ECPoint(x, y)
        } catch (ioe: IOException) {
            throw IllegalArgumentException("Exception", ioe)
        } finally {
            try {
                dataIn.close()
            } catch (ioe: IOException) {
                LOGGER.log(Level.FINE, "Error closing stream", ioe)
            }
        }
    }

    /**
     * Encodes (using BSI encoding) an EC point (for use as public key value).
     * Prefixes a `0x04` tag (without a length).
     * 
     * @param point an EC Point
     * @param bitLength the length in bits to use for each coordinate (the field size)
     * 
     * @return an octet string
     */
    fun ecPoint2OS(point: ECPoint, bitLength: Int): ByteArray {
        val bOut = ByteArrayOutputStream()
        val x = point.affineX
        val y = point.affineY
        try {
            bOut.write(0x04) // FIXME: Constant for 0x04.
            bOut.write(i2os(x, ceil(bitLength / 8.0).toInt()))
            bOut.write(i2os(y, ceil(bitLength / 8.0).toInt()))
            bOut.close()
        } catch (ioe: IOException) {
            throw IllegalStateException("Exception", ioe)
        }
        return bOut.toByteArray()
    }

    /**
     * Infers an EAC object identifier for an EC or DH public key.
     * 
     * @param publicKey a public key
     * 
     * @return either ID_PK_ECDH or ID_PK_DH
     */
    fun inferProtocolIdentifier(publicKey: PublicKey): String? {
        val algorithm = publicKey.getAlgorithm()
        if ("EC" == algorithm || "ECDH" == algorithm) {
            return SecurityInfo.ID_PK_ECDH
        } else if ("DH" == algorithm) {
            return SecurityInfo.ID_PK_DH
        } else {
            throw IllegalArgumentException("Wrong key type. Was expecting ECDH or DH public key.")
        }
    }

    /**
     * Adds two EC points.
     * 
     * @param x an EC point
     * @param y another EC point
     * @param params the domain parameters
     * 
     * @return the resulting EC point
     */
    fun add(x: ECPoint, y: ECPoint, params: ECParameterSpec): ECPoint {
        val bcX = toBouncyCastleECPoint(x, params)
        val bcY = toBouncyCastleECPoint(y, params)
        val bcSum = bcX.add(bcY)
        return fromBouncyCastleECPoint(bcSum)
    }

    /**
     * Multiplies a scalar and an EC point.
     * 
     * @param s the scalar
     * @param point the EC point
     * @param params the domain parameters
     * 
     * @return the resulting EC point
     */
    fun multiply(s: BigInteger?, point: ECPoint, params: ECParameterSpec): ECPoint {
        val bcPoint = toBouncyCastleECPoint(point, params)
        val bcProd = bcPoint.multiply(s)
        return fromBouncyCastleECPoint(bcProd)
    }

    /**
     * Checks whether the given point is on the given curve.
     * This just checks the Weierstrass equation.
     * 
     * @param xy a point
     * @param ecParams parameters specifying the curve
     * 
     * @return a boolean indicating whether the point is on the curve
     */
    fun isPointOnCurve(xy: ECPoint, ecParams: ECParameterSpec): Boolean {
        val x = xy.affineX
        val y = xy.affineY
        val p = getPrime(ecParams)

        val curve = ecParams.curve
        val a = curve.a
        val b = curve.b
        val lhs = y.multiply(y).mod(p)
        val rhs = x.multiply(x).multiply(x).add(a.multiply(x)).add(b).mod(p)

        return lhs == rhs
    }

    /**
     * Converts a string to bytes using UTF-8.
     * 
     * @param str a string
     * 
     * @return the bytes
     */
    fun getBytes(str: String): ByteArray {
        var bytes = str.toByteArray()
        try {
            bytes = str.toByteArray(charset("UTF-8"))
        } catch (use: UnsupportedEncodingException) {
            /* NOTE: unlikely. */
            LOGGER.log(Level.WARNING, "Exception", use)
        }

        return bytes
    }

    /**
     * Extracts the prime from the given DH or ECDH parameter specification
     * which (hopefully) specifies a curve over a prime field.
     * (This will throw an `IllegalArgumentException` for non-prime fields.)
     * 
     * @param params a parameter specification
     * 
     * @return the prime
     */
    fun getPrime(params: AlgorithmParameterSpec): BigInteger {
        requireNotNull(params) { "Parameters null" }

        if (params is DHParameterSpec) {
            return params.p
        } else if (params is ECParameterSpec) {
            val curve = params.curve
            val field = curve.field
            check(field is ECFieldFp) { "Was expecting prime field of type ECFieldFp, found " + field.javaClass.canonicalName }
            return field.p
        } else {
            throw IllegalArgumentException("Unsupported agreement algorithm, was expecting DHParameterSpec or ECParameterSpec, found " + params.javaClass.canonicalName)
        }
    }

    /**
     * Attempts to infer a relevant key agreement algorithm
     * (either `"DH"` or `"ECDH"`) given a public key.
     * 
     * @param publicKey the public key
     * 
     * @return either `"DH"` or `"ECDH"`
     */
    fun inferKeyAgreementAlgorithm(publicKey: PublicKey?): String {
        if (publicKey is ECPublicKey) {
            return "ECDH"
        } else if (publicKey is DHPublicKey) {
            return "DH"
        } else {
            throw IllegalArgumentException("Unsupported public key: $publicKey")
        }
    }

    /**
     * This just solves the curve equation for y.
     * 
     * @param affineX the x coord of a point on the curve
     * @param params EC parameters for curve over Fp
     * 
     * @return the corresponding y coord
     */
    fun computeAffineY(affineX: BigInteger?, params: ECParameterSpec): BigInteger? {
        val bcCurve = toBouncyCastleECCurve(params)
        val a = bcCurve.getA()
        val b = bcCurve.getB()
        val x = bcCurve.fromBigInteger(affineX)
        val y = x.multiply(x).add(a).multiply(x).add(b).sqrt()

        return y.toBigInteger()
    }

    /**
     * Converts a JCA EC point to a BC EC point.
     * 
     * @param point the JCA EC point
     * @param params the parameters to interpret the point
     * 
     * @return the corresponding BC EC point
     */
    fun toBouncyCastleECPoint(point: ECPoint, params: ECParameterSpec): org.bouncycastle.math.ec.ECPoint {
        val bcCurve = toBouncyCastleECCurve(params)
        return bcCurve.createPoint(point.affineX, point.affineY)
    }

    /**
     * Convert a BC EC point to a JCA EC point.
     * 
     * @param point the BC EC point
     * 
     * @return the corresponding JCA EC point
     */
    fun fromBouncyCastleECPoint(point: org.bouncycastle.math.ec.ECPoint): ECPoint {
        var point = point
        point = point.normalize()
        if (!point.isValid) {
            LOGGER.warning("point not valid")
        }
        return ECPoint(point.getAffineXCoord().toBigInteger(), point.getAffineYCoord().toBigInteger())
    }

    /**
     * Determines whether an EC point is valid with respect to the given EC parameters.
     * 
     * @param ecPoint an EC point
     * @param params the EC parameter specification
     * 
     * @return a boolean indicating whether the EC point is valid with respect tot the given EC parameters
     */
    fun isValid(ecPoint: ECPoint, params: ECParameterSpec): Boolean {
        val bcPoint = toBouncyCastleECPoint(ecPoint, params)
        return bcPoint.isValid
    }

    /**
     * Normalizes an EC point given the EC parameters.
     * 
     * @param ecPoint the EC point
     * @param params the EC parameter specification
     * 
     * @return the normalized EC point
     */
    fun normalize(ecPoint: ECPoint, params: ECParameterSpec): ECPoint {
        var bcPoint = toBouncyCastleECPoint(ecPoint, params)
        bcPoint = bcPoint.normalize()
        return fromBouncyCastleECPoint(bcPoint)
    }

    /**
     * Converts the EC parameter specification (including a curve) to a BC typed EC curve.
     * Currently supports curves over prime fields only.
     * 
     * @param params the EC parameter specification
     * 
     * @return the corresponding EC curve
     */
    private fun toBouncyCastleECCurve(params: ECParameterSpec): ECCurve {
        val curve = params.curve
        val field = curve.field
        require(field is ECFieldFp) { "Only prime field supported (for now), found " + field.javaClass.canonicalName }
        val coFactor = params.cofactor
        val order = params.order
        val a = curve.a
        val b = curve.b
        val p = getPrime(params)
        return ECCurve.Fp(p, a, b, order, BigInteger.valueOf(coFactor.toLong()))
    }

    /**
     * Converts the EC public key to a BC public key parameter specification.
     * 
     * @param publicKey the EC public key
     * 
     * @return a BC typed public key parameter specification
     */
    fun toBouncyECPublicKeyParameters(publicKey: ECPublicKey): ECPublicKeyParameters {
        val ecParams = publicKey.params
        val q = toBouncyCastleECPoint(publicKey.w, ecParams)
        return ECPublicKeyParameters(q, toBouncyECDomainParameters(ecParams))
    }

    /**
     * Converts the EC private key to a BC private key parameter specification.
     * 
     * @param privateKey the EC private key
     * 
     * @return a BC typed private key parameter specification
     */
    fun toBouncyECPrivateKeyParameters(privateKey: ECPrivateKey): ECPrivateKeyParameters {
        val d = privateKey.getS()
        val ecParams = toBouncyECDomainParameters(privateKey.params)
        return ECPrivateKeyParameters(d, ecParams)
    }

    /**
     * Converts a JCA compliant EC parameter (domain) specification to a BC
     * EC domain specification.
     * 
     * @param params the EC parameter specification
     * 
     * @return the corresponding BC typed EC domain parameter specification.
     */
    fun toBouncyECDomainParameters(params: ECParameterSpec): ECDomainParameters {
        val curve = toBouncyCastleECCurve(params)
        val g = toBouncyCastleECPoint(params.generator, params)
        val n = params.order
        val h = BigInteger.valueOf(params.cofactor.toLong())
        val seed = params.curve.seed
        return ECDomainParameters(curve, g, n, h, seed)
    }

    /* Get standard crypto primitives from default provider or (if that fails) from BC. */
    /**
     * Returns a cipher for the given encryption algorithm,
     * possibly using the BC provider explicitly if the
     * configured JCA providers cannot provide a cipher for the
     * algorithm.
     * 
     * @param algorithm the encryption algorithm
     * 
     * @return a cipher
     * 
     * @throws GeneralSecurityException on error
     */
    @Throws(GeneralSecurityException::class)
    fun getCipher(algorithm: String): Cipher {
        try {
            return Cipher.getInstance(algorithm)
        } catch (e: Exception) {
            LOGGER.log(Level.FINE, "Default provider could not provide this cipher, falling back to explicit BC", e)
            return Cipher.getInstance(algorithm, bouncyCastleProvider)
        }
    }

    /**
     * Returns a cipher for the given encryption algorithm and key,
     * possibly using the BC provider explicitly if the
     * configured JCA providers cannot provide a cipher for the
     * algorithm and key.
     * 
     * @param algorithm the encryption algorithm
     * @param mode the mode of operation (encrypt or decrypt)
     * @param key the key
     * 
     * @return a cipher
     * 
     * @throws GeneralSecurityException on error
     */
    @Throws(GeneralSecurityException::class)
    fun getCipher(algorithm: String, mode: Int, key: Key?): Cipher {
        try {
            val cipher = Cipher.getInstance(algorithm)
            cipher.init(mode, key)
            return cipher
        } catch (e: Exception) {
            LOGGER.log(Level.FINE, "Default provider could not provide this Cipher, falling back to explicit BC", e)
            val cipher: Cipher = Cipher.getInstance(algorithm, bouncyCastleProvider)
            cipher.init(mode, key)
            return cipher
        }
    }

    /**
     * Returns a key agreement object for the given algorithm, possibly using
     * the BC provider explicitly if the configured JCA providers cannot provide
     * a key agreement for the algorithm.
     * 
     * @param algorithm the key agreement algorithm
     * 
     * @return a key agreement object
     * 
     * @throws GeneralSecurityException on error
     */
    @Throws(GeneralSecurityException::class)
    fun getKeyAgreement(algorithm: String): KeyAgreement {
        try {
            return KeyAgreement.getInstance(algorithm)
        } catch (e: Exception) {
            LOGGER.log(
                Level.FINE,
                "Default provider could not provide this Key Agreement, falling back to explicit BC",
                e
            )
            return KeyAgreement.getInstance(algorithm, bouncyCastleProvider)
        }
    }

    /**
     * Returns a key pair generator for the given algorithm, possibly using
     * the BC provider explicitly when the configured JCA providers cannot
     * provide a generator for the algorithm.
     * 
     * @param algorithm the algorithm
     * 
     * @return a key pair generator
     * 
     * @throws GeneralSecurityException on error
     */
    @Throws(GeneralSecurityException::class)
    fun getKeyPairGenerator(algorithm: String): KeyPairGenerator? {
        try {
            return KeyPairGenerator.getInstance(algorithm)
        } catch (e: Exception) {
            LOGGER.log(
                Level.FINE,
                "Default provider could not provide this Key Pair Generator, falling back to explicit BC",
                e
            )
            return KeyPairGenerator.getInstance(algorithm, bouncyCastleProvider)
        }
    }

    /**
     * Returns a MAC for the given algorithm, possibly using the
     * BC provider explicitly if the configured JCA providers cannot
     * provide a MAC for the algorithm.
     * 
     * @param algorithm the MAC algorithm
     * 
     * @return a MAC object
     * 
     * @throws GeneralSecurityException on error
     */
    @Throws(GeneralSecurityException::class)
    fun getMac(algorithm: String): Mac {
        try {
            return Mac.getInstance(algorithm)
        } catch (e: Exception) {
            LOGGER.log(Level.FINE, "Default provider could not provide this Mac, falling back to explicit BC", e)
            return Mac.getInstance(algorithm, bouncyCastleProvider)
        }
    }

    /**
     * Returns a MAC for the given algorithm and key, possibly using
     * the BC provider explicitly when the configured JCA providers
     * cannot provide a MAC for the algorithm and key.
     * 
     * @param algorithm the MAC algorithm
     * @param key the key
     * 
     * @return a MAC object
     * 
     * @throws GeneralSecurityException on error
     */
    @Throws(GeneralSecurityException::class)
    fun getMac(algorithm: String, key: Key?): Mac {
        try {
            val mac = Mac.getInstance(algorithm)
            mac.init(key)
            return mac
        } catch (e: Exception) {
            LOGGER.log(Level.FINE, "Default provider could not provide this Mac, falling back to explicit BC", e)
            val mac: Mac = Mac.getInstance(algorithm, bouncyCastleProvider)
            mac.init(key)
            return mac
        }
    }

    /**
     * Returns a message digest for the given algorithm, possibly
     * using the BC provider explicitly if the configured JCA providers
     * cannot provide a message digest for the algorithm.
     * 
     * @param algorithm the message digest algorithm
     * 
     * @return a message digest object
     * 
     * @throws GeneralSecurityException on error
     */
    @Throws(GeneralSecurityException::class)
    fun getMessageDigest(algorithm: String): MessageDigest {
        try {
            return MessageDigest.getInstance(algorithm)
        } catch (e: Exception) {
            LOGGER.log(
                Level.FINE,
                "Default provider could not provide this Message Digest, falling back to explicit BC",
                e
            )
            return MessageDigest.getInstance(algorithm, bouncyCastleProvider)
        }
    }

    /**
     * Returns a public key for the given algorithm and key specification,
     * possibly using the BC provider explicitly when the configured JCA
     * providers cannot provide a public key for the algorithm and key
     * specification.
     * 
     * @param algorithm the public key algorithm
     * @param keySpec the key specification
     * 
     * @return a public key object
     * 
     * @throws GeneralSecurityException on error
     */
    @Throws(GeneralSecurityException::class)
    fun getPublicKey(algorithm: String, keySpec: KeySpec?): PublicKey? {
        try {
            val kf = KeyFactory.getInstance(algorithm)
            return kf.generatePublic(keySpec)
        } catch (e: Exception) {
            LOGGER.log(
                Level.FINE,
                "Default provider could not provide this Key Factory or Public Key, falling back to explicit BC",
                e
            )
            val kf: KeyFactory = KeyFactory.getInstance(algorithm, bouncyCastleProvider)
            return kf.generatePublic(keySpec)
        }
    }

    /**
     * Returns a signature for the given signature algorithm, possibly using the BC
     * provider if the configured JCA providers cannot provide a signature.
     * 
     * @param algorithm the signature algorithm
     * 
     * @return a signature object
     * 
     * @throws GeneralSecurityException on error
     */
    @Throws(GeneralSecurityException::class)
    fun getSignature(algorithm: String): Signature? {
        try {
            return Signature.getInstance(algorithm)
        } catch (e: Exception) {
            LOGGER.log(Level.FINE, "Default provider could not provide this Signature, falling back to explicit BC", e)
            return Signature.getInstance(algorithm, bouncyCastleProvider)
        }
    }

    /**
     * Returns a certificate factory object for the given certificate algorithm,
     * possibly using the BC provider explicitly if the configured JCA providers
     * cannot provide a certificate factory for the algorithm.
     * 
     * @param algorithm the certificate algorithm
     * 
     * @return a certificate factory
     * 
     * @throws GeneralSecurityException on error
     */
    @Throws(GeneralSecurityException::class)
    fun getCertificateFactory(algorithm: String): CertificateFactory {
        try {
            return CertificateFactory.getInstance(algorithm)
        } catch (e: Exception) {
            LOGGER.log(
                Level.FINE,
                "Default provider could not provide this Certificate Factory, falling back ot explicit BC",
                e
            )
            return CertificateFactory.getInstance(algorithm, bouncyCastleProvider)
        }
    }

    /**
     * Encodes an object identifier.
     * 0x80 Cryptographic mechanism reference.
     * Object Identifier of the protocol to select (value only, tag 0x06 is omitted).
     * 
     * @param oid the object identifier
     * 
     * @return the encoding
     */
    fun toOIDBytes(oid: String): ByteArray? {
        var oidBytes: ByteArray? = null
        try {
            val oidTLVIn = TLVInputStream(ByteArrayInputStream(ASN1ObjectIdentifier(oid).getEncoded()))
            try {
                oidTLVIn.readTag() /* Should be 0x06 */
                oidTLVIn.readLength()
                oidBytes = oidTLVIn.readValue()
            } finally {
                oidTLVIn.close()
            }
            return TLVUtil.wrapDO(0x80, oidBytes) /* FIXME: define constant for 0x80. */
        } catch (ioe: IOException) {
            throw IllegalArgumentException("Illegal OID: \"$oid", ioe)
        }
    }

    /**
     * Partitions a byte array into a number of segments of the given size,
     * and a final segment if there is a remainder.
     * 
     * @param segmentSize the number of bytes per segment
     * @param data the data to be partitioned
     * 
     * @return a list with the segments
     */
    fun partition(segmentSize: Int, data: ByteArray): MutableList<ByteArray?> {
        var segmentSize = segmentSize
        val segments = ArrayList<ByteArray?>()
        require(!(data == null || segmentSize <= 0)) { "Cannot partition" }

        /* Check if all data fits in one segment. */
        segmentSize = min(data.size, segmentSize)

        val segmentCount = data.size / segmentSize // Excluding the remainder.
        val lastSegmentSize = data.size % segmentSize

        var offset = 0
        for (i in 0..<segmentCount) {
            val segment = ByteArray(segmentSize)
            System.arraycopy(data, offset, segment, 0, segmentSize)
            segments.add(segment)
            offset += segmentSize
        }

        if (lastSegmentSize != 0) {
            val segment = ByteArray(lastSegmentSize)
            System.arraycopy(data, offset, segment, 0, lastSegmentSize)
            segments.add(segment)
        }

        return segments
    }

    /**
     * Returns the algorithm parameter specification from the given key.
     * 
     * @param key the key
     * 
     * @return an algorithm parameter specification, or `null`
     * 
     * @throws GeneralSecurityException on error
     */
    @Throws(GeneralSecurityException::class)
    fun getAlgorithmParams(key: Key): AlgorithmParameterSpec? {
        requireNotNull(key) { "Key is null" }
        when (key) {
            is DHPublicKey -> {
                return key.params
            }

            is ECPublicKey -> {
                return key.params
            }

            is RSAPublicKey -> {
                return key.params
            }

            is DSAPublicKey -> {
                val dsaParams = key.params
                return DSAParameterSpec(dsaParams.p, dsaParams.q, dsaParams.g)
            }

            is DHPrivateKey -> {
                return key.params
            }

            is ECPrivateKey -> {
                return key.params
            }

            is RSAPrivateKey -> {
                return key.params
            }

            is DSAPrivateKey -> {
                val dsaPrivateKey = key
                val dsaParams = dsaPrivateKey.params
                return DSAParameterSpec(dsaParams.p, dsaParams.q, dsaParams.g)
            }

            else -> {
                throw NoSuchAlgorithmException("Unsupported key type")
            }
        }
    }

    /**
     * Strips any leading zeroes from a byte-array and
     * returns the resulting byte-array.
     * 
     * @param bytes the input byte-array (which is not modified in the process)
     * 
     * @return a copy of the input byte-array, without the leading zeroes
     */
    fun stripLeadingZeroes(bytes: ByteArray?): ByteArray? {
        var bytes = bytes
        if (bytes == null || bytes.size <= 1) {
            return bytes
        }

        while (bytes!!.size > 0 && bytes[0].toInt() == 0x00) {
            val result = ByteArray(bytes.size - 1)
            System.arraycopy(bytes, 1, result, 0, result.size)
            bytes = result
        }

        return bytes
    }

    /* NOTE: Copied from BC. Deprecated at or before 1.65, removed at 1.66. */ /*
   * RFC 5114
   */
    private val RFC5114_1024_160_P = ("B10B8F96A080E01DDE92DE5EAE5D54EC52C99FBCFB06A3C6"
            + "9A6A9DCA52D23B616073E28675A23D189838EF1E2EE652C0" + "13ECB4AEA906112324975C3CD49B83BFACCBDD7D90C4BD70"
            + "98488E9C219A73724EFFD6FAE5644738FAA31A4FF55BCCC0" + "A151AF5F0DC8B4BD45BF37DF365C1A65E68CFDA76D4DA708"
            + "DF1FB2BC2E4A4371")
    private val RFC5114_1024_160_G = ("A4D1CBD5C3FD34126765A442EFB99905F8104DD258AC507F"
            + "D6406CFF14266D31266FEA1E5C41564B777E690F5504F213" + "160217B4B01B886A5E91547F9E2749F4D7FBD7D3B9A92EE1"
            + "909D0D2263F80A76A6A24C087A091F531DBF0A0169B6A28A" + "D662A4D18E73AFA32D779D5918D08BC8858F4DCEF97C2A24"
            + "855E6EEB22B3B2E5")
    private const val RFC5114_1024_160_Q = "F518AA8781A8DF278ABA4E7D64B7CB9D49462353"

    private val RFC5114_2048_224_P = ("AD107E1E9123A9D0D660FAA79559C51FA20D64E5683B9FD1"
            + "B54B1597B61D0A75E6FA141DF95A56DBAF9A3C407BA1DF15" + "EB3D688A309C180E1DE6B85A1274A0A66D3F8152AD6AC212"
            + "9037C9EDEFDA4DF8D91E8FEF55B7394B7AD5B7D0B6C12207" + "C9F98D11ED34DBF6C6BA0B2C8BBC27BE6A00E0A0B9C49708"
            + "B3BF8A317091883681286130BC8985DB1602E714415D9330" + "278273C7DE31EFDC7310F7121FD5A07415987D9ADC0A486D"
            + "CDF93ACC44328387315D75E198C641A480CD86A1B9E587E8" + "BE60E69CC928B2B9C52172E413042E9B23F10B0E16E79763"
            + "C9B53DCF4BA80A29E3FB73C16B8E75B97EF363E2FFA31F71" + "CF9DE5384E71B81C0AC4DFFE0C10E64F")
    private val RFC5114_2048_224_G = ("AC4032EF4F2D9AE39DF30B5C8FFDAC506CDEBE7B89998CAF"
            + "74866A08CFE4FFE3A6824A4E10B9A6F0DD921F01A70C4AFA" + "AB739D7700C29F52C57DB17C620A8652BE5E9001A8D66AD7"
            + "C17669101999024AF4D027275AC1348BB8A762D0521BC98A" + "E247150422EA1ED409939D54DA7460CDB5F6C6B250717CBE"
            + "F180EB34118E98D119529A45D6F834566E3025E316A330EF" + "BB77A86F0C1AB15B051AE3D428C8F8ACB70A8137150B8EEB"
            + "10E183EDD19963DDD9E263E4770589EF6AA21E7F5F2FF381" + "B539CCE3409D13CD566AFBB48D6C019181E1BCFE94B30269"
            + "EDFE72FE9B6AA4BD7B5A0F1C71CFFF4C19C418E1F6EC0179" + "81BC087F2A7065B384B890D3191F2BFA")
    private const val RFC5114_2048_224_Q = "801C0D34C58D93FE997177101F80535A4738CEBCBF389A99B36371EB"

    private val RFC5114_2048_256_P = ("87A8E61DB4B6663CFFBBD19C651959998CEEF608660DD0F2"
            + "5D2CEED4435E3B00E00DF8F1D61957D4FAF7DF4561B2AA30" + "16C3D91134096FAA3BF4296D830E9A7C209E0C6497517ABD"
            + "5A8A9D306BCF67ED91F9E6725B4758C022E0B1EF4275BF7B" + "6C5BFC11D45F9088B941F54EB1E59BB8BC39A0BF12307F5C"
            + "4FDB70C581B23F76B63ACAE1CAA6B7902D52526735488A0E" + "F13C6D9A51BFA4AB3AD8347796524D8EF6A167B5A41825D9"
            + "67E144E5140564251CCACB83E6B486F6B3CA3F7971506026" + "C0B857F689962856DED4010ABD0BE621C3A3960A54E710C3"
            + "75F26375D7014103A4B54330C198AF126116D2276E11715F" + "693877FAD7EF09CADB094AE91E1A1597")
    private val RFC5114_2048_256_G = ("3FB32C9B73134D0B2E77506660EDBD484CA7B18F21EF2054"
            + "07F4793A1A0BA12510DBC15077BE463FFF4FED4AAC0BB555" + "BE3A6C1B0C6B47B1BC3773BF7E8C6F62901228F8C28CBB18"
            + "A55AE31341000A650196F931C77A57F2DDF463E5E9EC144B" + "777DE62AAAB8A8628AC376D282D6ED3864E67982428EBC83"
            + "1D14348F6F2F9193B5045AF2767164E1DFC967C1FB3F2E55" + "A4BD1BFFE83B9C80D052B985D182EA0ADB2A3B7313D3FE14"
            + "C8484B1E052588B9B7D2BBD2DF016199ECD06E1557CD0915" + "B3353BBB64E0EC377FD028370DF92B52C7891428CDC67EB6"
            + "184B523D1DB246C32F63078490F00EF8D647D148D4795451" + "5E2327CFEF98C582664B4C0F6CC41659")
    private val RFC5114_2048_256_Q = ("8CF83642A709A097B447997640129DA299B1A47D1EB3750B"
            + "A308B0FE64F5FBD3")

    @Deprecated("Existence of a hidden SNFS backdoor cannot be ruled out. see https://eprint.iacr.org/2016/961.pdf")
    val RFC5114_1024_160: DHParameters = fromPGQ(RFC5114_1024_160_P, RFC5114_1024_160_G, RFC5114_1024_160_Q)

    @Deprecated("Existence of a hidden SNFS backdoor cannot be ruled out. see https://eprint.iacr.org/2016/961.pdf")
    val RFC5114_2048_224: DHParameters = fromPGQ(RFC5114_2048_224_P, RFC5114_2048_224_G, RFC5114_2048_224_Q)

    @Deprecated("Existence of a hidden SNFS backdoor cannot be ruled out. see https://eprint.iacr.org/2016/961.pdf")
    val RFC5114_2048_256: DHParameters = fromPGQ(RFC5114_2048_256_P, RFC5114_2048_256_G, RFC5114_2048_256_Q)

    private fun fromPGQ(hexP: String, hexG: String, hexQ: String): DHParameters {
        return DHParameters(fromHex(hexP), fromHex(hexG), fromHex(hexQ))
    }

    private fun fromHex(hex: String): BigInteger {
        return BigInteger(1, Hex.decodeStrict(hex))
    }
}
