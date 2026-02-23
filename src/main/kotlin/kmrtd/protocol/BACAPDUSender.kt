package kmrtd.protocol

import net.sf.scuba.smartcards.*
import kmrtd.APDULevelBACCapable
import kmrtd.CardServiceProtocolException
import kmrtd.Util
import java.security.GeneralSecurityException
import java.security.Provider
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

/*
* Modified work Copyright (C) 2026 Alessandro Giaquinto
* Kotlin port of JMRTD
*
* Licensed under LGPL 3.0
*/
/**
 * A low-level APDU sender to support the BAC protocol.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1851 $
 * 
 * @since 0.7.0
 */
class BACAPDUSender(private val service: CardService) : APDULevelBACCapable {
    /** DESede encryption/decryption cipher.  */
    private var cipher: Cipher? = null

    /** ISO9797Alg3Mac.  */
    private var mac: Mac? = null

    /**
     * Creates an APDU sender for tranceiving BAC protocol APDUs.
     * 
     * @param service the card service for tranceiving APDUs
     */
    init {
        try {
            this.mac = Mac.getInstance("ISO9797Alg3Mac", BC_PROVIDER)
            this.cipher = Util.getCipher("DESede/CBC/NoPadding")
        } catch (gse: GeneralSecurityException) {
            throw IllegalStateException("Unexpected security exception during initialization", gse)
        }
    }

    /**
     * Sends a `GET CHALLENGE` command to the passport.
     * 
     * @return a byte array of length 8 containing the challenge
     * 
     * @throws CardServiceException on tranceive error
     */
    @Synchronized
    @Throws(CardServiceException::class)
    override fun sendGetChallenge(): ByteArray {
        return sendGetChallenge(null)
    }

    /**
     * Sends a `GET CHALLENGE` command to the passport.
     * 
     * @param wrapper secure messaging wrapper
     * 
     * @return a byte array of length 8 containing the challenge
     * 
     * @throws CardServiceException on tranceive error
     */
    @Synchronized
    @Throws(CardServiceException::class)
    fun sendGetChallenge(wrapper: APDUWrapper?): ByteArray {
        val commandAPDU = CommandAPDU(ISO7816.CLA_ISO7816.toInt(), ISO7816.INS_GET_CHALLENGE.toInt(), 0x00, 0x00, 8)
        val responseAPDU = service.transmit(commandAPDU)
        val challenge = responseAPDU.getData()
        if (challenge == null || challenge.size != 8) {
            throw CardServiceException("Get challenge failed", responseAPDU.getSW())
        }
        return challenge
    }

    /**
     * Sends an `EXTERNAL AUTHENTICATE` command to the passport.
     * This is part of BAC.
     * The resulting byte array has length 32 and contains `rndICC`
     * (first 8 bytes), `rndIFD` (next 8 bytes), their key material
     * `kICC` (last 16 bytes).
     * 
     * @param rndIFD our challenge
     * @param rndICC their challenge
     * @param kIFD our key material
     * @param kEnc the static encryption key
     * @param kMac the static mac key
     * 
     * @return a byte array of length 32 containing the response that was sent
     * by the passport, decrypted (using `kEnc`) and verified
     * (using `kMac`)
     * 
     * @throws CardServiceException on tranceive error
     */
    @Synchronized
    @Throws(CardServiceException::class)
    override fun sendMutualAuth(
        rndIFD: ByteArray,
        rndICC: ByteArray,
        kIFD: ByteArray,
        kEnc: SecretKey,
        kMac: SecretKey
    ): ByteArray {
        var rndICC = rndICC
        try {
            require(!(rndIFD == null || rndIFD.size != 8)) { "rndIFD wrong length" }
            if (rndICC == null || rndICC.size != 8) {
                rndICC = ByteArray(8)
            }
            require(!(kIFD == null || kIFD.size != 16)) { "kIFD wrong length" }
            requireNotNull(kEnc) { "kEnc == null" }
            requireNotNull(kMac) { "kMac == null" }

            cipher?.init(Cipher.ENCRYPT_MODE, kEnc, ZERO_IV_PARAM_SPEC)
            val plaintext = ByteArray(32)
            System.arraycopy(rndIFD, 0, plaintext, 0, 8)
            System.arraycopy(rndICC, 0, plaintext, 8, 8)
            System.arraycopy(kIFD, 0, plaintext, 16, 16)
            val ciphertext = cipher?.doFinal(plaintext)
            check(ciphertext?.size == 32) { "Cryptogram wrong length " + ciphertext?.size }

            mac!!.init(kMac)
            val mactext = mac?.doFinal(Util.pad(ciphertext, 8))
            check(mactext?.size == 8) { "MAC wrong length" }

            val p1 = 0x00.toByte()
            val p2 = 0x00.toByte()

            val data = ByteArray(32 + 8)
            System.arraycopy(ciphertext, 0, data, 0, 32)
            System.arraycopy(mactext, 0, data, 32, 8)
            var le = 40 /* 40 means max ne is 40 (0x28). */
            var commandAPDU = CommandAPDU(
                ISO7816.CLA_ISO7816.toInt(),
                ISO7816.INS_EXTERNAL_AUTHENTICATE.toInt(),
                p1.toInt(),
                p2.toInt(),
                data,
                le
            )
            var responseAPDU = service.transmit(commandAPDU)

            if (responseAPDU == null) {
                throw CardServiceException("Mutual authentication failed, received null response APDU")
            }

            var responseAPDUBytes = responseAPDU.getBytes()
            var sw = responseAPDU.getSW().toShort()
            if (responseAPDUBytes == null) {
                throw CardServiceException(
                    "Mutual authentication failed, received empty data in response APDU",
                    sw.toInt()
                )
            }

            /* Some MRTDs apparently don't support 40 here, try again with 0. See R2-p1_v2_sIII_0035 (and other issues). */
            if (sw != ISO7816.SW_NO_ERROR) {
                le = 0 /* 0 means ne is max 256 (0xFF). */
                commandAPDU = CommandAPDU(
                    ISO7816.CLA_ISO7816.toInt(),
                    ISO7816.INS_EXTERNAL_AUTHENTICATE.toInt(),
                    p1.toInt(),
                    p2.toInt(),
                    data,
                    le
                )
                responseAPDU = service.transmit(commandAPDU)
                responseAPDUBytes = responseAPDU.getBytes()
                sw = responseAPDU.getSW().toShort()
            }

            if (responseAPDUBytes.size != 42) {
                throw CardServiceProtocolException(
                    "Mutual authentication failed: expected length: 40 + 2, actual length: " + responseAPDUBytes.size,
                    0,
                    sw.toInt()
                )
            }

            /* Decrypt the response. */
            cipher?.init(Cipher.DECRYPT_MODE, kEnc, ZERO_IV_PARAM_SPEC)
            val result = cipher?.doFinal(responseAPDUBytes, 0, responseAPDUBytes.size - 8 - 2)
            if (result?.size != 32) {
                /* The PICC allowed access, but probably the resulting secure channel will be wrong. */
                throw CardServiceException(
                    "Cryptogram wrong length, was expecting 32, found " + result?.size,
                    sw.toInt()
                )
            }

            return result
        } catch (gse: GeneralSecurityException) {
            /* Lower level security exception, probably the resulting secure channel will be wrong. */
            throw CardServiceException("Security exception during mutual auth", gse)
        }
    }

    companion object {
        private val BC_PROVIDER: Provider = Util.bouncyCastleProvider

        /** Initialization vector used by the cipher below.  */
        private val ZERO_IV_PARAM_SPEC = IvParameterSpec(byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00))
    }
}
