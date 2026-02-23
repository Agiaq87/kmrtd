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
 * $Id: SignedDataUtil.java 1889 2025-03-15 21:09:22Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.lds

import org.bouncycastle.asn1.*
import org.bouncycastle.asn1.cms.*
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers
import org.bouncycastle.asn1.pkcs.RSASSAPSSparams
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.asn1.x509.Certificate
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.*
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.AlgorithmParameterSpec
import java.security.spec.MGF1ParameterSpec
import java.security.spec.PSSParameterSpec
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Utility class for helping with CMS SignedData in security object document and
 * card security file.
 * 
 * This hopefully abstracts some of the BC dependencies away.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1889 $
 */
object SignedDataUtil {
    private val LOGGER: Logger = Logger.getLogger("kmrtd")

    /** SignedData related object identifier.  */
    const val RFC_3369_SIGNED_DATA_OID: String =
        "1.2.840.113549.1.7.2" /* id-signedData OBJECT IDENTIFIER ::= { iso(1) member-body(2) us(840) rsadsi(113549) pkcs(1) pkcs7(7) 2 } */

    /** SignedData related object identifier.  */
    const val RFC_3369_CONTENT_TYPE_OID: String = "1.2.840.113549.1.9.3"

    /** SignedData related object identifier.  */
    const val RFC_3369_MESSAGE_DIGEST_OID: String = "1.2.840.113549.1.9.4"

    /** SignedData related object identifier.  */
    const val PKCS1_RSA_OID: String = "1.2.840.113549.1.1.1"

    /** SignedData related object identifier.  */
    const val PKCS1_MD2_WITH_RSA_OID: String = "1.2.840.113549.1.1.2"

    /** SignedData related object identifier.  */
    const val PKCS1_MD4_WITH_RSA_OID: String = "1.2.840.113549.1.1.3"

    /** SignedData related object identifier.  */
    const val PKCS1_MD5_WITH_RSA_OID: String = "1.2.840.113549.1.1.4"

    /** SignedData related object identifier.  */
    const val PKCS1_SHA1_WITH_RSA_OID: String = "1.2.840.113549.1.1.5"

    /** SignedData related object identifier.  */
    const val PKCS1_MGF1: String = "1.2.840.113549.1.1.8"

    /** SignedData related object identifier.  */
    const val PKCS1_RSASSA_PSS_OID: String = "1.2.840.113549.1.1.10"

    /** SignedData related object identifier.  */
    const val PKCS1_SHA256_WITH_RSA_OID: String = "1.2.840.113549.1.1.11"

    /** SignedData related object identifier.  */
    const val PKCS1_SHA384_WITH_RSA_OID: String = "1.2.840.113549.1.1.12"

    /** SignedData related object identifier.  */
    const val PKCS1_SHA512_WITH_RSA_OID: String = "1.2.840.113549.1.1.13"

    /** SignedData related object identifier.  */
    const val PKCS1_SHA224_WITH_RSA_OID: String = "1.2.840.113549.1.1.14"

    /** SignedData related object identifier.  */
    const val X9_SHA1_WITH_ECDSA_OID: String = "1.2.840.10045.4.1"

    /** SignedData related object identifier.  */
    const val X9_SHA224_WITH_ECDSA_OID: String = "1.2.840.10045.4.3.1"

    /** SignedData related object identifier.  */
    const val X9_SHA256_WITH_ECDSA_OID: String = "1.2.840.10045.4.3.2"

    /** SignedData related object identifier.  */
    const val X9_SHA384_WITH_ECDSA_OID: String = "1.2.840.10045.4.3.3"

    /** SignedData related object identifier.  */
    const val X9_SHA512_WITH_ECDSA_OID: String = "1.2.840.10045.4.3.4"

    /** SignedData related object identifier.  */
    const val IEEE_P1363_SHA1_OID: String = "1.3.14.3.2.26"

    /**
     * Reads a signed data structure from a stream.
     * 
     * @param inputStream the stream to read from
     * 
     * @return the signed data structure
     * 
     * @throws IOException on error reading from the stream
     */
    @JvmStatic
    @Throws(IOException::class)
    fun readSignedData(inputStream: InputStream?): SignedData {
        val asn1InputStream = ASN1InputStream(inputStream, true)
        val sequence = ASN1Sequence.getInstance(asn1InputStream.readObject())

        if (sequence.size() != 2) {
            throw IOException("Was expecting a DER sequence of length 2, found a DER sequence of length " + sequence.size())
        }

        val contentTypeOID = (sequence.getObjectAt(0) as ASN1ObjectIdentifier).getId()
        if (RFC_3369_SIGNED_DATA_OID != contentTypeOID) {
            throw IOException("Was expecting signed-data content type OID ($RFC_3369_SIGNED_DATA_OID), found $contentTypeOID")
        }

        val asn1SequenceWithSignedData = getObjectFromTaggedObject(sequence.getObjectAt(1))

        if (asn1SequenceWithSignedData !is ASN1Sequence) {
            throw IOException("Was expecting an ASN.1 sequence as content")
        }

        return SignedData.getInstance(asn1SequenceWithSignedData)
    }

    /**
     * Writes a signed data structure to a stream.
     * 
     * @param signedData the signed data to write
     * @param outputStream the stream to write to
     * 
     * @throws IOException on error writing to the stream
     */
    @JvmStatic
    @Throws(IOException::class)
    fun writeData(signedData: SignedData?, outputStream: OutputStream) {
        val v = ASN1EncodableVector()
        v.add(ASN1ObjectIdentifier(RFC_3369_SIGNED_DATA_OID))
        v.add(DERTaggedObject(0, signedData))
        val fileContentsObject: ASN1Sequence = DLSequence(v)
        val fileContentsBytes = fileContentsObject.getEncoded(ASN1Encoding.DER)
        outputStream.write(fileContentsBytes)
    }

    /**
     * Extracts the content from a signed data structure.
     * 
     * @param signedData the signed data
     * 
     * @return the contents of the e-content in the signed data structure
     */
    @JvmStatic
    fun getContent(signedData: SignedData): ASN1Primitive? {
        val encapContentInfo = signedData.getEncapContentInfo()

        val eContent = encapContentInfo.getContent() as ASN1OctetString

        var inputStream: ASN1InputStream? = null
        try {
            inputStream = ASN1InputStream(ByteArrayInputStream(eContent.getOctets()))
            return inputStream.readObject()
        } catch (ioe: IOException) {
            LOGGER.log(Level.WARNING, "Unexpected exception", ioe)
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close()
                } catch (ioe: IOException) {
                    LOGGER.log(Level.FINE, "Exception closing input stream", ioe)
                    /* At least we tried... */
                }
            }
        }

        return null
    }

    /**
     * Removes the tag from a tagged object.
     * 
     * @param asn1Encodable the encoded tagged object
     * 
     * @return the object
     * 
     * @throws IOException if the input is not a tagged object or the tagNo is not 0
     */
    @Throws(IOException::class)
    fun getObjectFromTaggedObject(asn1Encodable: ASN1Encodable): ASN1Object? {
        if (asn1Encodable !is ASN1TaggedObject) {
            throw IOException("Was expecting an ASN1TaggedObject, found " + asn1Encodable.javaClass.canonicalName)
        }

        val asn1TaggedObject = asn1Encodable

        val tagClass = asn1TaggedObject.getTagClass()
        if (tagClass != BERTags.CONTEXT_SPECIFIC) {
            throw IOException(
                "Was expecting CONTEXT_SPECIFIC tag class in ASN1 tagged object, found " + Integer.toHexString(
                    tagClass
                )
            )
        }

        val tagNo = asn1TaggedObject.getTagNo()
        if (tagNo != 0) {
            throw IOException("Was expecting tag 0, found " + Integer.toHexString(tagNo))
        }

        return asn1TaggedObject.getExplicitBaseObject()
    }

    /**
     * Returns the digest algirithm used in the signer info in a signed data structure.
     * 
     * @param signedData the signed data structure
     * 
     * @return the digest algorithm
     */
    @JvmStatic
    fun getSignerInfoDigestAlgorithm(signedData: SignedData): String? {
        try {
            val signerInfo = getSignerInfo(signedData)
            val digestAlgOID = signerInfo.digestAlgorithm.algorithm.getId()
            return lookupMnemonicByOID(digestAlgOID)
        } catch (nsae: NoSuchAlgorithmException) {
            LOGGER.log(Level.WARNING, "No such algorithm $nsae")
            return null
        }
    }

    /**
     * Returns the parameters of the digest encryption (signature) algorithm
     * used in the given signed data object.
     * For instance for `"RSASSA/PSS"` this includes the hash algorithm
     * and the salt length.
     * 
     * @param signedData the signed data object
     * 
     * @return the algorithm parameters, or `PSSParameterSpec.DEFAULT` for RSASSA/PSS, or `null` on unrecognized algorithms
     */
    @JvmStatic
    fun getDigestEncryptionAlgorithmParams(signedData: SignedData): AlgorithmParameterSpec? {
        try {
            val signerInfo = getSignerInfo(signedData)
            val digestEncryptionAlgorithm = signerInfo.digestEncryptionAlgorithm
            val digestEncryptionAlgorithmOID = digestEncryptionAlgorithm.algorithm.getId()
            if (PKCS1_RSASSA_PSS_OID != digestEncryptionAlgorithmOID) {
                /* We only support additional parameters for RSASSA/PSS signature algorithm. */
                return null
            }

            val params = digestEncryptionAlgorithm.parameters
            if (params == null) {
                LOGGER.warning("Cannot get RSASSA/PSS parameters")
                return null
            }

            val rsaSSAParams = RSASSAPSSparams.getInstance(params)
            if (rsaSSAParams == null) {
                LOGGER.warning("Cannot get RSASSA/PSS parameters")
                return null
            }

            return toAlgorithmParameterSpec(rsaSSAParams)
        } catch (e: Exception) {
            LOGGER.log(Level.WARNING, "Cannot get RSASSA/PSS parameters", e)
        }

        return null
    }

    /**
     * Returns the signature algorithm used in the given signed data structure.
     * 
     * @param signedData the signed data structure
     * 
     * @return a JCE mnemonic algorithm string
     */
    @JvmStatic
    fun getDigestEncryptionAlgorithm(signedData: SignedData): String? {
        try {
            val signerInfo = getSignerInfo(signedData)
            val digestEncryptionAlgorithmOID = signerInfo.digestEncryptionAlgorithm.algorithm.getId()
            if (digestEncryptionAlgorithmOID == null) {
                LOGGER.warning("Could not determine digest encryption algorithm OID")
                return null
            }
            return lookupMnemonicByOID(digestEncryptionAlgorithmOID)
        } catch (nsae: NoSuchAlgorithmException) {
            LOGGER.log(Level.WARNING, "No such algorithm", nsae)
            return null
        }
    }

    /**
     * Returns the contents of the signed data over which the
     * signature is to be computed.
     * 
     * See RFC 3369, Cryptographic Message Syntax, August 2002,
     * Section 5.4 for details.
     * 
     * FIXME: Maybe throw an exception instead of issuing warnings
     * on logger if signed attributes do not check out.
     * 
     * @param signedData the signed data
     * 
     * @return the contents of the security object over which the
     * signature is to be computed
     * 
     * @throws SignatureException if the contents do not check out
     */
    @JvmStatic
    @Throws(SignatureException::class)
    fun getEContent(signedData: SignedData): ByteArray? {
        val signerInfo = getSignerInfo(signedData)
        val signedAttributesSet = signerInfo.authenticatedAttributes

        val contentInfo = signedData.encapContentInfo
        val contentBytes = (contentInfo.content as ASN1OctetString).octets

        if (signedAttributesSet.size() == 0) {
            /* Signed attributes absent, return content to be signed... */
            return contentBytes
        }

        /* Signed attributes present (i.e. a structure containing a hash of the content), return that structure to be signed... */
        /* This option is taken by ICAO passports. */
        var attributesBytes: ByteArray? = null
        val digAlg = signerInfo.digestAlgorithm.algorithm.getId()

        try {
            attributesBytes = signedAttributesSet.getEncoded(ASN1Encoding.DER)
            checkEContent(getAttributes(signedAttributesSet), digAlg, contentBytes)
            return attributesBytes
        } catch (e: Exception) {
            throw SignatureException(e)
        }
    }

    /**
     * Returns the stored signature of the security object.
     * 
     * @param signedData the signed data
     * 
     * @return the signature
     */
    @JvmStatic
    fun getEncryptedDigest(signedData: SignedData): ByteArray {
        val signerInfo = getSignerInfo(signedData)
        return signerInfo.encryptedDigest.octets
    }

    /**
     * Returns the issuer and serial number stored in the given signed data structure.
     * 
     * @param signedData the signed data structure
     * 
     * @return the issuer and serial number
     */
    @JvmStatic
    fun getIssuerAndSerialNumber(signedData: SignedData): IssuerAndSerialNumber? {
        val signerInfo = getSignerInfo(signedData)
        val signerIdentifier = signerInfo.sid
        val signerIdentifierId = signerIdentifier.getId()
        if (!(signerIdentifierId is ASN1Sequence || signerIdentifierId is IssuerAndSerialNumber)) {
            /* NOTE: DE MasterList appears to use DER octet string here. */
            return null
        }

        val issuerAndSerialNumber = IssuerAndSerialNumber.getInstance(signerIdentifierId)
        val issuer = issuerAndSerialNumber.name
        val serialNumber = issuerAndSerialNumber.serialNumber.value
        return IssuerAndSerialNumber(issuer, serialNumber)
    }

    /**
     * Returns the subject-key-identifier in the given signed-data structure
     * if the signer is identified through a subject-key-identifier.
     * This will return `null` if the signer is identified through
     * issuer name and serial number.
     * 
     * @param signedData the signed-data-structure
     * 
     * @return the subject-key-identifier
     */
    @JvmStatic
    fun getSubjectKeyIdentifier(signedData: SignedData): ByteArray {
        val signerInfo = getSignerInfo(signedData)
        val signerIdentifier = signerInfo.getSID()
        if (signerIdentifier == null) {
            return null
        }

        val signerIdentifierId = signerIdentifier.getId()
        if (signerIdentifierId == null || signerIdentifierId !is ASN1OctetString) {
            return null
        }

        return signerIdentifierId.getOctets()
    }

    /**
     * Reads any objects in the given ASN1 octet string (as an ASN1 input stream).
     * 
     * @param octetString the octet string
     * 
     * @return a list of objects read
     */
    fun getObjectsFromOctetString(octetString: ASN1OctetString): MutableList<ASN1Primitive?> {
        val result: MutableList<ASN1Primitive?> = ArrayList<ASN1Primitive?>()
        val octets = octetString.getOctets()
        val derInputStream = ASN1InputStream(ByteArrayInputStream(octets))
        try {
            while (true) {
                val derObject = derInputStream.readObject()
                if (derObject == null) {
                    break
                }
                result.add(derObject)
            }
            derInputStream.close()
        } catch (ioe: IOException) {
            LOGGER.log(Level.WARNING, "Exception", ioe)
        }

        return result
    }

    /**
     * Extracts the list of embedded certificates from a signed data object.
     * 
     * @param signedData the signed data object
     * 
     * @return the list of certificates
     */
    @JvmStatic
    fun getCertificates(signedData: SignedData): MutableList<X509Certificate?> {
        val encodedCertificates = signedData.certificates
        val certificateCount = if (encodedCertificates == null) 0 else encodedCertificates.size()

        val result: MutableList<X509Certificate?> = ArrayList<X509Certificate?>(certificateCount)
        if (certificateCount <= 0) {
            return result
        }

        for (i in 0..<certificateCount) {
            try {
                val certAsASN1Object = Certificate.getInstance(encodedCertificates!!.getObjectAt(i))
                result.add(SignedDataUtil.decodeCertificate(certAsASN1Object!!))
            } catch (e: Exception) {
                LOGGER.log(Level.WARNING, "Exception in decoding certificate", e)
            }
        }

        return result
    }

    /**
     * Decodes an ASN1 encoded BC certificate object to a JCA certificate object.
     * 
     * @param certAsASN1Object the ASN1 object
     * 
     * @return an X509 certificate
     * 
     * @throws IOException on error decoding the DER structure, never happens
     * @throws GeneralSecurityException on error decoding
     */
    @Throws(IOException::class, GeneralSecurityException::class)
    fun decodeCertificate(certAsASN1Object: Certificate): X509Certificate? {
        val certSpec = certAsASN1Object.getEncoded(ASN1Encoding.DER)
        /*
     * NOTE: We explicitly prefer Bouncy Castle here. The default Sun provider claims to support all X509 encoded
     * certificates but cannot handle named EC curves
     * for EC public keys.
     */
        val factory = CertificateFactory.getInstance("X.509", Util.getBouncyCastleProvider())
        return factory.generateCertificate(ByteArrayInputStream(certSpec)) as X509Certificate?
    }

    /**
     * Creates a signed data structure, for inclusion in a security object.
     * 
     * @param digestAlgorithm the digest algorithm
     * @param digestEncryptionAlgorithm the signature algorithm
     * @param contentTypeOID the object identifier
     * @param contentInfo the content info
     * @param encryptedDigest the signature bytes
     * @param docSigningCertificate the document signer certificate
     * 
     * @return the signed data structure
     * 
     * @throws GeneralSecurityException on error
     */
    @JvmStatic
    @Throws(GeneralSecurityException::class)
    fun createSignedData(
        digestAlgorithm: String?, digestEncryptionAlgorithm: String,
        contentTypeOID: String, contentInfo: ContentInfo, encryptedDigest: ByteArray,
        docSigningCertificate: X509Certificate
    ): SignedData {
        return createSignedData(
            digestAlgorithm,
            digestEncryptionAlgorithm,
            null,
            contentTypeOID,
            contentInfo,
            encryptedDigest,
            docSigningCertificate
        )
    }

    /**
     * Creates a signed data structure, for inclusion in a security object.
     * 
     * @param digestAlgorithm the digest algorithm
     * @param digestEncryptionAlgorithm the signature algorithm
     * @param digestEncryptionParameters the digest encryption algorithm parameters
     * @param contentTypeOID the object identifier
     * @param contentInfo the content info
     * @param encryptedDigest the signature bytes
     * @param docSigningCertificate the document signer certificate
     * 
     * @return the signed data structure
     * 
     * @throws GeneralSecurityException on error
     */
    @JvmStatic
    @Throws(GeneralSecurityException::class)
    fun createSignedData(
        digestAlgorithm: String?,
        digestEncryptionAlgorithm: String,
        digestEncryptionParameters: AlgorithmParameterSpec?,
        contentTypeOID: String,
        contentInfo: ContentInfo,
        encryptedDigest: ByteArray,
        docSigningCertificate: X509Certificate
    ): SignedData {
        val digestAlgorithmsSet = createSingletonSet(createDigestAlgorithms(digestAlgorithm))
        val certificates = createSingletonSet(createCertificate(docSigningCertificate))
        val crls: ASN1Set? = null
        val signerInfos = createSingletonSet(
            createSignerInfo(
                digestAlgorithm,
                digestEncryptionAlgorithm,
                digestEncryptionParameters,
                contentTypeOID,
                contentInfo,
                encryptedDigest,
                docSigningCertificate
            ).toASN1Primitive()
        )
        return SignedData(digestAlgorithmsSet, contentInfo, certificates, crls, signerInfos)
    }

    /**
     * Creates a signer info structures.
     * 
     * @param digestAlgorithm the digest algorithm
     * @param digestEncryptionAlgorithm the signature algorithm
     * @param contentTypeOID the object identifier
     * @param contentInfo the content info
     * @param encryptedDigest the signature bytes
     * @param docSigningCertificate the document signer certificate
     * 
     * @return the signer info structure
     * 
     * @throws GeneralSecurityException on error
     */
    @Throws(GeneralSecurityException::class)
    fun createSignerInfo(
        digestAlgorithm: String?,
        digestEncryptionAlgorithm: String, contentTypeOID: String, contentInfo: ContentInfo,
        encryptedDigest: ByteArray, docSigningCertificate: X509Certificate
    ): SignerInfo {
        return createSignerInfo(
            digestAlgorithm,
            digestEncryptionAlgorithm,
            null,
            contentTypeOID,
            contentInfo,
            encryptedDigest,
            docSigningCertificate
        )
    }

    /**
     * Creates a signer info structures.
     * 
     * @param digestAlgorithm the digest algorithm
     * @param digestEncryptionAlgorithm the signature algorithm
     * @param digestEncryptionParameters the digest encryption algorithm parameters, or `null`
     * @param contentTypeOID the object identifier
     * @param contentInfo the content info
     * @param encryptedDigest the signature bytes
     * @param docSigningCertificate the document signer certificate
     * 
     * @return the signer info structure
     * 
     * @throws GeneralSecurityException on error
     */
    @Throws(GeneralSecurityException::class)
    fun createSignerInfo(
        digestAlgorithm: String?,
        digestEncryptionAlgorithm: String,
        digestEncryptionParameters: AlgorithmParameterSpec?,
        contentTypeOID: String,
        contentInfo: ContentInfo,
        encryptedDigest: ByteArray,
        docSigningCertificate: X509Certificate
    ): SignerInfo {
        requireNotNull(encryptedDigest) { "Encrypted digest cannot be null" }

        /* Get the issuer name (CN, O, OU, C) from the cert and put it in a SignerIdentifier struct. */
        val docSignerName = X500Name.getInstance(docSigningCertificate.getIssuerX500Principal().getEncoded())
        val serial = docSigningCertificate.getSerialNumber()
        val sid = SignerIdentifier(IssuerAndSerialNumber(docSignerName, serial))

        val digestAlgorithmObject = AlgorithmIdentifier(ASN1ObjectIdentifier(lookupOIDByMnemonic(digestAlgorithm)))
        val digestEncryptionAlgorithmObject =
            getDigestEncryptionAlgorithmObject(digestEncryptionAlgorithm, digestEncryptionParameters)

        val authenticatedAttributes = createAuthenticatedAttributes(
            digestAlgorithm,
            contentTypeOID,
            contentInfo
        ) // struct containing the hash of content
        val encryptedDigestObject: ASN1OctetString = DEROctetString(encryptedDigest) // this is the signature
        val unAuthenticatedAttributes: ASN1Set? = null // should be empty set?

        return SignerInfo(
            sid,
            digestAlgorithmObject,
            authenticatedAttributes,
            digestEncryptionAlgorithmObject,
            encryptedDigestObject,
            unAuthenticatedAttributes
        )
    }

    /**
     * Creates the authenticated attributes to be signed.
     * 
     * @param digestAlgorithm the digest algorithm
     * @param contentTypeOID the object identifier
     * @param contentInfo the content info to digest
     * 
     * @return authenticated attributes to be signed
     * 
     * @throws GeneralSecurityException on error
     */
    @Throws(GeneralSecurityException::class)
    fun createAuthenticatedAttributes(
        digestAlgorithm: String?,
        contentTypeOID: String,
        contentInfo: ContentInfo
    ): ASN1Set {
        /* Check bug found by Paulo Assumpco. */
        var digestAlgorithm = digestAlgorithm
        if ("SHA256" == digestAlgorithm) {
            digestAlgorithm = "SHA-256"
        }
        val dig = Util.getMessageDigest(digestAlgorithm)
        val contentBytes = (contentInfo.getContent() as ASN1OctetString).getOctets()
        val digestedContentBytes = dig.digest(contentBytes)
        val digestedContent: ASN1OctetString = DEROctetString(digestedContentBytes)
        val contentTypeAttribute = Attribute(
            ASN1ObjectIdentifier(RFC_3369_CONTENT_TYPE_OID), createSingletonSet(
                ASN1ObjectIdentifier(contentTypeOID)
            )
        )
        val messageDigestAttribute =
            Attribute(ASN1ObjectIdentifier(RFC_3369_MESSAGE_DIGEST_OID), createSingletonSet(digestedContent))
        val result =
            arrayOf<ASN1Object?>(contentTypeAttribute.toASN1Primitive(), messageDigestAttribute.toASN1Primitive())
        return DLSet(result)
    }

    /**
     * Encodes the given JCE mnemonic digest algorithm as an BC ASN1 sequence.
     * 
     * @param digestAlgorithm the JCE mnemonic digest algorithm
     * 
     * @return the encoded digest algorithm
     * 
     * @throws NoSuchAlgorithmException when the digest algorithm is not known
     */
    @Throws(NoSuchAlgorithmException::class)
    fun createDigestAlgorithms(digestAlgorithm: String?): ASN1Sequence {
        val algorithmIdentifier = ASN1ObjectIdentifier(lookupOIDByMnemonic(digestAlgorithm))
        val v = ASN1EncodableVector()
        v.add(algorithmIdentifier)
        return DLSequence(v)
    }

    /**
     * Encodes an X509 certificate as a BC ASN1 sequence.
     * 
     * @param certificate a certificate
     * 
     * @return a BC ASN1 sequence with the encoded certificate
     * 
     * @throws CertificateException on error
     */
    @Throws(CertificateException::class)
    fun createCertificate(certificate: X509Certificate): ASN1Sequence? {
        requireNotNull(certificate) { "Cannot encode null certificate" }

        try {
            val certSpec = certificate.getEncoded()
            val asn1In = ASN1InputStream(certSpec)
            try {
                return asn1In.readObject() as ASN1Sequence?
            } finally {
                try {
                    asn1In.close()
                } catch (ioe: IOException) {
                    LOGGER.log(Level.FINE, "Error closing stream", ioe)
                }
            }
        } catch (ioe: IOException) {
            throw CertificateException("Could not construct certificate byte stream", ioe)
        }
    }

    /**
     * Signs the (authenticated attributes derived from the given) data.
     * 
     * @param digestAlgorithm the digest algorithm
     * @param digestEncryptionAlgorithm the signature algorithm
     * @param contentTypeOID the object identifier
     * @param contentInfo the content info
     * @param privateKey the private key to use for signing
     * @param provider the preferred provider to use
     * 
     * @return the signed data
     */
    @JvmStatic
    fun signData(
        digestAlgorithm: String?,
        digestEncryptionAlgorithm: String,
        contentTypeOID: String,
        contentInfo: ContentInfo,
        privateKey: PrivateKey?,
        provider: String?
    ): ByteArray? {
        return signData(
            digestAlgorithm,
            digestEncryptionAlgorithm,
            null,
            contentTypeOID,
            contentInfo,
            privateKey,
            provider
        )
    }

    /**
     * Signs the (authenticated attributes derived from the given) data.
     * 
     * @param digestAlgorithm the digest algorithm
     * @param digestEncryptionAlgorithm the signature algorithm
     * @param digestEncryptionParameters the parameters, or `null`
     * @param contentTypeOID the object identifier
     * @param contentInfo the content info
     * @param privateKey the private key to use for signing
     * @param provider the preferred provider to use
     * 
     * @return the signed data
     */
    @JvmStatic
    fun signData(
        digestAlgorithm: String?,
        digestEncryptionAlgorithm: String,
        digestEncryptionParameters: AlgorithmParameterSpec?,
        contentTypeOID: String,
        contentInfo: ContentInfo,
        privateKey: PrivateKey?,
        provider: String?
    ): ByteArray? {
        var encryptedDigest: ByteArray? = null
        try {
            val dataToBeSigned = createAuthenticatedAttributes(digestAlgorithm, contentTypeOID, contentInfo).getEncoded(
                ASN1Encoding.DER
            )
            var s: Signature? = null
            if (provider != null) {
                s = Signature.getInstance(digestEncryptionAlgorithm, provider)
            } else {
                s = Signature.getInstance(digestEncryptionAlgorithm)
            }
            if (digestEncryptionParameters != null) {
                s.setParameter(digestEncryptionParameters)
            }
            s.initSign(privateKey)
            s.update(dataToBeSigned)
            encryptedDigest = s.sign()
        } catch (e: Exception) {
            LOGGER.log(Level.WARNING, "Exception", e)
            return null
        }
        return encryptedDigest
    }

    /**
     * Extracts the signer info structure from a signed data structure.
     * 
     * @param signedData the signed data structure
     * 
     * @return the signer info structure
     */
    @JvmStatic
    fun getSignerInfo(signedData: SignedData): SignerInfo {
        val signerInfos = signedData.getSignerInfos()
        require(!(signerInfos == null || signerInfos.size() <= 0)) { "No signer info in signed data" }

        if (signerInfos.size() > 1) {
            LOGGER.warning("Found " + signerInfos.size() + " signerInfos")
        }

        return SignerInfo.getInstance(signerInfos.getObjectAt(0))
    }

    /**
     * Returns the common mnemonic string (such as "SHA1", "SHA256withRSA") given an OID.
     * 
     * @param oid an object identifier
     * 
     * @return a mnemonic string
     * 
     * @throws NoSuchAlgorithmException if the provided OID is not yet supported
     */
    @JvmStatic
    @Throws(NoSuchAlgorithmException::class)
    fun lookupMnemonicByOID(oid: String?): String {
        if (oid == null) {
            return null
        }
        if (oid == X509ObjectIdentifiers.organization.getId()) {
            return "O"
        }
        if (oid == X509ObjectIdentifiers.organizationalUnitName.getId()) {
            return "OU"
        }
        if (oid == X509ObjectIdentifiers.commonName.getId()) {
            return "CN"
        }
        if (oid == X509ObjectIdentifiers.countryName.getId()) {
            return "C"
        }
        if (oid == X509ObjectIdentifiers.stateOrProvinceName.getId()) {
            return "ST"
        }
        if (oid == X509ObjectIdentifiers.localityName.getId()) {
            return "L"
        }
        if (oid == X509ObjectIdentifiers.id_SHA1.getId()) {
            return "SHA-1"
        }
        if (oid == NISTObjectIdentifiers.id_sha224.getId()) {
            return "SHA-224"
        }
        if (oid == NISTObjectIdentifiers.id_sha256.getId()) {
            return "SHA-256"
        }
        if (oid == NISTObjectIdentifiers.id_sha384.getId()) {
            return "SHA-384"
        }
        if (oid == NISTObjectIdentifiers.id_sha512.getId()) {
            return "SHA-512"
        }
        if (oid == X9_SHA1_WITH_ECDSA_OID) {
            return "SHA1withECDSA"
        }
        if (oid == X9_SHA224_WITH_ECDSA_OID) {
            return "SHA224withECDSA"
        }
        if (oid == X9_SHA256_WITH_ECDSA_OID) {
            return "SHA256withECDSA"
        }
        if (oid == X9_SHA384_WITH_ECDSA_OID) {
            return "SHA384withECDSA"
        }
        if (oid == X9_SHA512_WITH_ECDSA_OID) {
            return "SHA512withECDSA"
        }
        if (oid == PKCS1_RSA_OID) {
            return "RSA"
        }
        if (oid == PKCS1_MD2_WITH_RSA_OID) {
            return "MD2withRSA"
        }
        if (oid == PKCS1_MD4_WITH_RSA_OID) {
            return "MD4withRSA"
        }
        if (oid == PKCS1_MD5_WITH_RSA_OID) {
            return "MD5withRSA"
        }
        if (oid == PKCS1_SHA1_WITH_RSA_OID) {
            return "SHA1withRSA"
        }
        if (oid == PKCS1_SHA256_WITH_RSA_OID) {
            return "SHA256withRSA"
        }
        if (oid == PKCS1_SHA384_WITH_RSA_OID) {
            return "SHA384withRSA"
        }
        if (oid == PKCS1_SHA512_WITH_RSA_OID) {
            return "SHA512withRSA"
        }
        if (oid == PKCS1_SHA224_WITH_RSA_OID) {
            return "SHA224withRSA"
        }
        if (oid == IEEE_P1363_SHA1_OID) {
            return "SHA-1"
        }
        if (oid == PKCS1_RSASSA_PSS_OID) {
            return "SSAwithRSA/PSS"
        }
        if (oid == PKCS1_MGF1) {
            return "MGF1"
        }

        throw NoSuchAlgorithmException("Unknown OID " + oid)
    }

    /**
     * Looks up an object identifier for the given JCE mnemonic.
     * 
     * @param name a JCE mnemonic string
     * 
     * @return an object identifier if known
     * 
     * @throws NoSuchAlgorithmException if the mnemonic does not correspond to a known object identifier
     */
    @JvmStatic
    @Throws(NoSuchAlgorithmException::class)
    fun lookupOIDByMnemonic(name: String?): String {
        if ("O" == name) {
            return X509ObjectIdentifiers.organization.getId()
        }
        if ("OU" == name) {
            return X509ObjectIdentifiers.organizationalUnitName.getId()
        }
        if ("CN" == name) {
            return X509ObjectIdentifiers.commonName.getId()
        }
        if ("C" == name) {
            return X509ObjectIdentifiers.countryName.getId()
        }
        if ("ST" == name) {
            return X509ObjectIdentifiers.stateOrProvinceName.getId()
        }
        if ("L" == name) {
            return X509ObjectIdentifiers.localityName.getId()
        }
        if ("SHA-1".equals(name, ignoreCase = true) || "SHA1".equals(name, ignoreCase = true)) {
            return X509ObjectIdentifiers.id_SHA1.getId()
        }
        if ("SHA-224".equals(name, ignoreCase = true) || "SHA224".equals(name, ignoreCase = true)) {
            return NISTObjectIdentifiers.id_sha224.getId()
        }
        if ("SHA-256".equals(name, ignoreCase = true) || "SHA256".equals(name, ignoreCase = true)) {
            return NISTObjectIdentifiers.id_sha256.getId()
        }
        if ("SHA-384".equals(name, ignoreCase = true) || "SHA384".equals(name, ignoreCase = true)) {
            return NISTObjectIdentifiers.id_sha384.getId()
        }
        if ("SHA-512".equals(name, ignoreCase = true) || "SHA512".equals(name, ignoreCase = true)) {
            return NISTObjectIdentifiers.id_sha512.getId()
        }
        if ("RSA".equals(name, ignoreCase = true)) {
            return PKCS1_RSA_OID
        }
        if ("MD2withRSA".equals(name, ignoreCase = true)) {
            return PKCS1_MD2_WITH_RSA_OID
        }
        if ("MD4withRSA".equals(name, ignoreCase = true)) {
            return PKCS1_MD4_WITH_RSA_OID
        }
        if ("MD5withRSA".equals(name, ignoreCase = true)) {
            return PKCS1_MD5_WITH_RSA_OID
        }
        if ("SHA1withRSA".equals(name, ignoreCase = true)) {
            return PKCS1_SHA1_WITH_RSA_OID
        }
        if ("SHA256withRSA".equals(name, ignoreCase = true)) {
            return PKCS1_SHA256_WITH_RSA_OID
        }
        if ("SHA384withRSA".equals(name, ignoreCase = true)) {
            return PKCS1_SHA384_WITH_RSA_OID
        }
        if ("SHA512withRSA".equals(name, ignoreCase = true)) {
            return PKCS1_SHA512_WITH_RSA_OID
        }
        if ("SHA224withRSA".equals(name, ignoreCase = true)) {
            return PKCS1_SHA224_WITH_RSA_OID
        }
        if ("SHA1withECDSA".equals(name, ignoreCase = true)) {
            return X9_SHA1_WITH_ECDSA_OID
        }
        if ("SHA224withECDSA".equals(name, ignoreCase = true)) {
            return X9_SHA224_WITH_ECDSA_OID
        }
        if ("SHA256withECDSA".equals(name, ignoreCase = true)) {
            return X9_SHA256_WITH_ECDSA_OID
        }
        if ("SHA384withECDSA".equals(name, ignoreCase = true)) {
            return X9_SHA384_WITH_ECDSA_OID
        }
        if ("SHA512withECDSA".equals(name, ignoreCase = true)) {
            return X9_SHA512_WITH_ECDSA_OID
        }
        if ("SAwithRSA/PSS".equals(name, ignoreCase = true)) {
            return PKCS1_RSASSA_PSS_OID
        }
        if ("SSAwithRSA/PSS".equals(name, ignoreCase = true)) {
            return PKCS1_RSASSA_PSS_OID
        }
        if ("RSASSA-PSS".equals(name, ignoreCase = true)) {
            return PKCS1_RSASSA_PSS_OID
        }
        if ("MGF1".equals(name, ignoreCase = true)) {
            return PKCS1_MGF1
        }
        if ("SHA256withRSAandMGF1".equals(name, ignoreCase = true)) {
            return PKCS1_MGF1
        }
        if ("SHA512withRSAandMGF1".equals(name, ignoreCase = true)) {
            return PKCS1_MGF1
        }

        throw NoSuchAlgorithmException("Unknown name $name")
    }

    /* PRIVATE BELOW */
    /**
     * Checks that the content actually digests to the hash value contained in the message digest attribute.
     * 
     * @param attributes the attributes, this should contain an attribute of type [.RFC_3369_MESSAGE_DIGEST_OID]
     * @param digAlg the digest algorithm
     * @param contentBytes the contents
     * 
     * @throws NoSuchAlgorithmException if the digest algorithm is unsupported
     * @throws SignatureException if the reported digest does not correspond to the computed digest
     */
    @Throws(NoSuchAlgorithmException::class, SignatureException::class)
    private fun checkEContent(attributes: MutableCollection<Attribute>, digAlg: String, contentBytes: ByteArray?) {
        for (attribute in attributes) {
            if (RFC_3369_MESSAGE_DIGEST_OID != attribute.attrType.getId()) {
                continue
            }

            val attrValuesSet = attribute.attrValues
            if (attrValuesSet.size() != 1) {
                LOGGER.warning("Expected only one attribute value in signedAttribute message digest in eContent!")
            }
            val storedDigestedContent = (attrValuesSet.getObjectAt(0) as ASN1OctetString).getOctets()

            if (storedDigestedContent == null) {
                LOGGER.warning("Error extracting signedAttribute message digest in eContent!")
            }

            val dig = MessageDigest.getInstance(digAlg)
            val computedDigestedContent = dig.digest(contentBytes)
            if (!storedDigestedContent.contentEquals(computedDigestedContent)) {
                throw SignatureException("Error checking signedAttribute message digest in eContent!")
            }
        }
    }

    /**
     * Extracts the attributes as a Java list from the BC ASN1 encoded signed attributes set structure.
     * 
     * @param signedAttributesSet the BC ASN1 encoded signed attributes set structure
     * 
     * @return the attributes as a Java list
     */
    private fun getAttributes(signedAttributesSet: ASN1Set): MutableList<Attribute> {
        val attributeObjects: MutableList<ASN1Sequence?> = Collections.list<Any?>(signedAttributesSet.getObjects())
        val attributes: MutableList<Attribute> = ArrayList<Attribute>(attributeObjects.size)
        for (attributeObject in attributeObjects) {
            val attribute = Attribute.getInstance(attributeObject)
            attributes.add(attribute!!)
        }
        return attributes
    }

    /**
     * Converts the BC RSA-SSA parameter specification to a JCE parameter specification.
     * 
     * @param rsaSSAParams the RSA-SSA parameter specification to convert
     * 
     * @return a corresponding JCE parameter specification, or `null` if the BC specification specifies an illegal JCE specification
     * 
     * @throws NoSuchAlgorithmException on error
     */
    @Throws(NoSuchAlgorithmException::class)
    private fun toAlgorithmParameterSpec(rsaSSAParams: RSASSAPSSparams?): AlgorithmParameterSpec? {
        if (rsaSSAParams == null) {
            return null
        }

        val hashAlgorithmOID = rsaSSAParams.hashAlgorithm.algorithm.getId()
        val maskGenAlgorithm = rsaSSAParams.maskGenAlgorithm
        val maskGenAlgorithmOID = maskGenAlgorithm.algorithm.getId()

        val hashAlgorithmName = lookupMnemonicByOID(hashAlgorithmOID)
        val maskGenAlgorithmName = lookupMnemonicByOID(maskGenAlgorithmOID)

        val saltLength = rsaSSAParams.saltLength.toInt()
        val trailerField = rsaSSAParams.trailerField.toInt()

        if (hashAlgorithmName == null || maskGenAlgorithmName == null || saltLength < 0 || trailerField < 0) {
            LOGGER.warning("Cannot get RSASSA/PSS parameters")
            return null
        }

        return PSSParameterSpec(
            hashAlgorithmName,
            maskGenAlgorithmName,
            toMaskGenAlgorithmParameterSpec(maskGenAlgorithm),
            saltLength,
            trailerField
        )
    }

    /**
     * Converts the BC algorithm identifier (used in RSA-SSA) to a JCE algorithm parameter specification.
     * 
     * @param maskGenAlgorithm the algorithm identifier to convert
     * 
     * @return the corresponding JCE algorithm parameter specification
     */
    private fun toMaskGenAlgorithmParameterSpec(maskGenAlgorithm: AlgorithmIdentifier): AlgorithmParameterSpec {
        try {
            val maskGenParams = maskGenAlgorithm.parameters
            if (maskGenParams != null) {
                val hashIdentifier = AlgorithmIdentifier.getInstance(maskGenParams)
                val hashOID = hashIdentifier.algorithm.getId()
                val hashName = lookupMnemonicByOID(hashOID)
                return MGF1ParameterSpec(hashName)
            }
        } catch (e: Exception) {
            LOGGER.log(Level.WARNING, "Exception", e)
        }
        /* Default to SHA-1. */
        return MGF1ParameterSpec("SHA-1")
    }

    /**
     * Constructs the BC algorithm identifier for the given signature algorithm, taking into account
     * the parameters if they are non-null.
     * 
     * @param digestEncryptionAlgorithm a Java mnemonic algorithm string
     * @param digestEncryptionParameters the parameters, or `null`
     * 
     * @return the algorithm identifier
     * 
     * @throws GeneralSecurityException when the algorithm is not recognized
     */
    @Throws(GeneralSecurityException::class)
    private fun getDigestEncryptionAlgorithmObject(
        digestEncryptionAlgorithm: String,
        digestEncryptionParameters: AlgorithmParameterSpec?
    ): AlgorithmIdentifier {
        val digestEncryptionAlgOID = ASN1ObjectIdentifier(lookupOIDByMnemonic(digestEncryptionAlgorithm))
        if (digestEncryptionParameters == null) {
            return AlgorithmIdentifier(digestEncryptionAlgOID)
        }

        try {
            val params = AlgorithmParameters.getInstance(digestEncryptionAlgorithm)
            params.init(digestEncryptionParameters)
            return AlgorithmIdentifier(digestEncryptionAlgOID, ASN1Primitive.fromByteArray(params.encoded))
        } catch (ioe: IOException) {
            throw InvalidAlgorithmParameterException("Unable to encode parameters object", ioe)
        }
    }

    /**
     * Creates a singleton ASN1 set containing the given BC ASN1 object.
     * 
     * @param e the ASN1 object
     * 
     * @return an ASN1 set containing the given object as single element
     */
    private fun createSingletonSet(e: ASN1Object?): ASN1Set {
        return DLSet(arrayOf<ASN1Encodable?>(e))
    }
}
