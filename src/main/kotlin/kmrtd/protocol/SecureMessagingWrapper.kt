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
 * $Id: SecureMessagingWrapper.java 1807 2019-03-06 23:01:37Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.protocol

import kmrtd.Util
import net.sf.scuba.smartcards.APDUWrapper
import net.sf.scuba.smartcards.CommandAPDU
import net.sf.scuba.smartcards.ISO7816
import net.sf.scuba.smartcards.ResponseAPDU
import net.sf.scuba.tlv.TLVUtil
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.io.Serializable
import java.security.GeneralSecurityException
import java.util.logging.Level
import java.util.logging.Logger
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

/**
 * Secure messaging wrapper base class.
 * 
 * @author The JMRTD team
 * @version $Revision: 1807 $
 */
abstract class SecureMessagingWrapper protected constructor(
    ksEnc: SecretKey?,
    ksMac: SecretKey?,
    cipherAlg: String?,
    macAlg: String?,
    /**
     * Returns the maximum tranceive length of wrapped command and response APDUs,
     * typical values are 256 and 65536.
     *
     * @return the maximum tranceive length of wrapped command and response APDUs
     */
    val maxTranceiveLength: Int,
    private val shouldCheckMAC: Boolean,
    ssc: Long
) : Serializable, APDUWrapper {

    @Transient
    private val cipher: Cipher

    @Transient
    private val mac: Mac

    /**
     * Returns the shared key for encrypting APDU payloads.
     * 
     * @return the encryption key
     */
    val encryptionKey: SecretKey? = ksEnc

    /**
     * Returns the shared key for computing message authentication codes over APDU payloads.
     * 
     * @return the MAC key
     */
    val mACKey: SecretKey? = ksMac

    /**
     * Returns the current value of the send sequence counter.
     * 
     * @return the current value of the send sequence counter.
     */
    var sendSequenceCounter: Long
        private set

    /**
     * Constructs a secure messaging wrapper based on the secure messaging
     * session keys and the initial value of the send sequence counter.
     * 
     * @param ksEnc              the session key for encryption
     * @param ksMac              the session key for message authenticity
     * @param cipherAlg          the mnemonic Java string describing the cipher algorithm
     * @param macAlg             the mnemonic Java string describing the message authenticity checking algorithm
     * @param maxTranceiveLength the maximum tranceive length, typical values are 256 or 65536
     * @param shouldCheckMAC     a boolean indicating whether this wrapper will check the MAC in wrapped response APDUs
     * @param ssc                the initial value of the send sequence counter
     * @throws GeneralSecurityException when the available JCE providers cannot provide the necessary cryptographic primitives
     */
    init {
        this.sendSequenceCounter = ssc

        this.cipher = Util.getCipher(cipherAlg)
        this.mac = Util.getMac(macAlg)
    }

    /**
     * Returns a boolean indicating whether this wrapper will check the MAC in wrapped response APDUs.
     * 
     * @return a boolean indicating whether this wrapper will check the MAC in wrapped response APDUs
     */
    fun shouldCheckMAC(): Boolean {
        return shouldCheckMAC
    }

    /**
     * Wraps the APDU buffer of a command APDU.
     * As a side effect, this method increments the internal send
     * sequence counter maintained by this wrapper.
     * 
     * @param commandAPDU buffer containing the command APDU
     * @return length of the command APDU after wrapping
     */
    override fun wrap(commandAPDU: CommandAPDU): CommandAPDU {
        this.sendSequenceCounter++
        try {
            return wrapCommandAPDU(commandAPDU)
        } catch (gse: GeneralSecurityException) {
            throw IllegalStateException("Unexpected exception", gse)
        } catch (ioe: IOException) {
            throw IllegalStateException("Unexpected exception", ioe)
        }
    }

    /**
     * Unwraps the APDU buffer of a response APDU.
     * 
     * @param responseAPDU the response APDU
     * @return a new byte array containing the unwrapped buffer
     */
    override fun unwrap(responseAPDU: ResponseAPDU): ResponseAPDU {
        this.sendSequenceCounter++
        try {
            val data = responseAPDU.getData()
            check(!(data == null || data.isEmpty())) {
                "Card indicates SM error, SW = " + Integer.toHexString(
                    responseAPDU.sw and 0xFFFF
                )
            }
            return unwrapResponseAPDU(responseAPDU)
        } catch (gse: GeneralSecurityException) {
            throw IllegalStateException("Unexpected exception", gse)
        } catch (ioe: IOException) {
            throw IllegalStateException("Unexpected exception", ioe)
        }
    }

    /**
     * Checks the MAC.
     * 
     * @param rapdu the bytes of the response APDU, including the `0x8E` tag, the length of the MAC, the MAC itself, and the status word
     * @param cc    the MAC sent by the other party
     * @return whether the computed MAC is identical
     * @throws GeneralSecurityException on security related error
     */
    @Throws(GeneralSecurityException::class)
    protected fun checkMac(rapdu: ByteArray, cc: ByteArray): Boolean {
        try {
            val byteArrayOutputStream = ByteArrayOutputStream()
            val dataOutputStream = DataOutputStream(byteArrayOutputStream)
            dataOutputStream.write(this.encodedSendSequenceCounter)
            val paddedData = Util.pad(
                rapdu, 0, rapdu.size - 2 - 8 - 2,
                this.padLength
            )
            dataOutputStream.write(paddedData, 0, paddedData.size)
            dataOutputStream.flush()
            dataOutputStream.close()
            mac.init(this.mACKey)
            var cc2 = mac.doFinal(byteArrayOutputStream.toByteArray())

            if (cc2.size > 8 && cc.size == 8) {
                val newCC2 = ByteArray(8)
                System.arraycopy(cc2, 0, newCC2, 0, newCC2.size)
                cc2 = newCC2
            }

            return cc.contentEquals(cc2)
        } catch (ioe: IOException) {
            LOGGER.log(Level.WARNING, "Exception checking MAC", ioe)
            return false
        }
    }

    /**
     * Returns the length (in bytes) to use for padding.
     * 
     * @return the length to use for padding
     */
    protected abstract val padLength: Int

    @get:Throws(GeneralSecurityException::class)
    protected abstract val iV: IvParameterSpec?

    /**
     * Returns the send sequence counter encoded as a byte array for inclusion in wrapped APDUs.
     * 
     * @return the send sequence counter encoded as byte array
     */
    protected abstract val encodedSendSequenceCounter: ByteArray?

    /* PRIVATE BELOW. */ /*
     * The SM Data Objects (see [ISO/IEC 7816-4]) MUST be used in the following order:
     *   - Command APDU: [DO‘85’ or DO‘87’] [DO‘97’] DO‘8E’.
     *   - Response APDU: [DO‘85’ or DO‘87’] [DO‘99’] DO‘8E’.
     */
    /**
     * Performs the actual encoding of a command APDU.
     * Based on Section E.3 of ICAO-TR-PKI, especially the examples.
     * 
     * @param commandAPDU the command APDU
     * @return a byte array containing the wrapped APDU buffer
     * @throws GeneralSecurityException on error wrapping the APDU
     * @throws IOException              on error writing the result to memory
     */
    @Throws(GeneralSecurityException::class, IOException::class)
    private fun wrapCommandAPDU(commandAPDU: CommandAPDU): CommandAPDU {
        val cla = commandAPDU.cla
        val ins = commandAPDU.ins
        val p1 = commandAPDU.p1
        val p2 = commandAPDU.p2
        val lc = commandAPDU.nc
        val le = commandAPDU.ne

        val maskedHeader = byteArrayOf(
            (cla or 0x0C.toByte().toInt()).toByte(),
            ins.toByte(),
            p1.toByte(),
            p2.toByte()
        )
        val paddedMaskedHeader = Util.pad(maskedHeader, this.padLength)

        val hasDO85 = (commandAPDU.ins.toByte() == ISO7816.INS_READ_BINARY2)

        var do8587 = ByteArray(0)
        var do97 = ByteArray(0)

        val byteArrayOutputStream = ByteArrayOutputStream()
        try {
            /* Include the expected length, if present. */

            if (le > 0) {
                do97 = TLVUtil.wrapDO(0x97, encodeLe(le))
            }

            /* Encrypt command data, if present. */
            if (lc > 0) {
                val data = Util.pad(commandAPDU.getData(), this.padLength)

                /* Re-initialize cipher, this time with IV based on SSC. */
                cipher.init(Cipher.ENCRYPT_MODE, this.encryptionKey, this.iV)
                val ciphertext = cipher.doFinal(data)

                byteArrayOutputStream.reset()
                byteArrayOutputStream.write((if (hasDO85) 0x85.toByte() else 0x87.toByte()).toInt())
                byteArrayOutputStream.write(TLVUtil.getLengthAsBytes(ciphertext.size + (if (hasDO85) 0 else 1)))
                if (!hasDO85) {
                    byteArrayOutputStream.write(0x01)
                }
                byteArrayOutputStream.write(ciphertext, 0, ciphertext.size)
                do8587 = byteArrayOutputStream.toByteArray()
            }

            byteArrayOutputStream.reset()
            byteArrayOutputStream.write(this.encodedSendSequenceCounter)
            byteArrayOutputStream.write(paddedMaskedHeader)
            byteArrayOutputStream.write(do8587)
            byteArrayOutputStream.write(do97)
            val n = Util.pad(
                byteArrayOutputStream.toByteArray(),
                this.padLength
            )

            /* Compute cryptographic checksum... */
            mac.init(this.mACKey)
            val cc = mac.doFinal(n)
            var ccLength = cc.size
            if (ccLength != 8) {
                ccLength = 8
            }

            byteArrayOutputStream.reset()
            byteArrayOutputStream.write(0x8E.toByte().toInt())
            byteArrayOutputStream.write(ccLength)
            byteArrayOutputStream.write(cc, 0, ccLength)
            val do8E = byteArrayOutputStream.toByteArray()

            /* Construct protected APDU... */
            byteArrayOutputStream.reset()
            byteArrayOutputStream.write(do8587)
            byteArrayOutputStream.write(do97)
            byteArrayOutputStream.write(do8E)
        } finally {
            try {
                byteArrayOutputStream.close()
            } catch (ioe: IOException) {
                /* Never happens. */
                LOGGER.log(Level.FINE, "Error closing stream", ioe)
            }
        }

        val data = byteArrayOutputStream.toByteArray()

        /*
         * The requested response is 0x00 or 0x0000, depending on whether extended length is needed.
         */
        if (le <= 256 && data.size <= 255) {
            return CommandAPDU(
                maskedHeader[0].toInt(),
                maskedHeader[1].toInt(),
                maskedHeader[2].toInt(),
                maskedHeader[3].toInt(),
                data,
                256
            )
        } else if (le > 256 || data.size > 255) {
            return CommandAPDU(
                maskedHeader[0].toInt(),
                maskedHeader[1].toInt(),
                maskedHeader[2].toInt(),
                maskedHeader[3].toInt(),
                data,
                65536
            )
        } else {
            /* Not sure if this case ever occurs, but this is consistent with previous behavior. */
            return CommandAPDU(
                maskedHeader[0].toInt(),
                maskedHeader[1].toInt(),
                maskedHeader[2].toInt(),
                maskedHeader[3].toInt(),
                data,
                this.maxTranceiveLength
            )
        }
    }

    /**
     * Unwraps a response APDU sent by the ICC.
     * Based on Section E.3 of TR-PKI, especially the examples.
     * 
     * @param responseAPDU the response APDU
     * @return a byte array containing the unwrapped APDU buffer
     * @throws GeneralSecurityException on error unwrapping the APDU
     * @throws IOException              on error writing the result to memory
     */
    @Throws(GeneralSecurityException::class, IOException::class)
    private fun unwrapResponseAPDU(responseAPDU: ResponseAPDU): ResponseAPDU {
        val rapdu = responseAPDU.bytes
        require(!(rapdu == null || rapdu.size < 2)) { "Invalid response APDU" }
        cipher.init(Cipher.DECRYPT_MODE, this.encryptionKey, this.iV)

        var data = ByteArray(0)
        var cc: ByteArray? = null
        var sw: Short = 0
        val inputStream = DataInputStream(ByteArrayInputStream(rapdu))
        inputStream.use { inputStream ->
            var isFinished = false
            while (!isFinished) {
                when (val tag = inputStream.readByte().toInt() and 0xFF) {
                    0x87/*.toByte()*/ -> data = readDO87(inputStream, false)
                    0x85/*.toByte()*/ -> data = readDO87(inputStream, true)
                    0x99/*.toByte()*/ -> sw = readDO99(inputStream)
                    0x8E/*.toByte()*/ -> {
                        cc = readDO8E(inputStream)
                        isFinished = true
                    }

                    else -> LOGGER.warning("Unexpected tag " + Integer.toHexString(tag))
                }
            }
        }
        check(!(shouldCheckMAC() && !checkMac(rapdu, cc!!))) { "Invalid MAC" }
        val bOut = ByteArrayOutputStream()
        bOut.write(data, 0, data.size)
        bOut.write((sw.toInt() and 0xFF00) shr 8)
        bOut.write(sw.toInt() and 0x00FF)
        return ResponseAPDU(bOut.toByteArray())
    }

    /**
     * Encodes the expected length value to a byte array for inclusion in wrapped APDUs.
     * The result is a byte array of length 1 or 2.
     * 
     * @param le a non-negative expected length
     * @return a byte array with the encoded expected length
     */
    private fun encodeLe(le: Int): ByteArray {
        if (le in 0..256) {
            /* NOTE: Both 0x00 and 0x100 are mapped to 0x00. */
            return byteArrayOf(le.toByte())
        } else {
            return byteArrayOf(((le and 0xFF00) shr 8).toByte(), (le and 0xFF).toByte())
        }
    }

    /**
     * Reads a data object.
     * The `0x87` tag has already been read.
     * 
     * @param inputStream the stream to read from
     * @param do85        whether to expect a `0x85` (including an extra 1 length) data object.
     * @return the bytes that were read
     * @throws IOException              on error reading from the stream
     * @throws GeneralSecurityException on error decrypting the data
     */
    @Throws(IOException::class, GeneralSecurityException::class)
    private fun readDO87(inputStream: DataInputStream, do85: Boolean): ByteArray {
        /* Read length... */
        var length = 0
        var buf = inputStream.readUnsignedByte()
        if ((buf and 0x00000080) != 0x00000080) {
            /* Short form */
            length = buf
            if (!do85) {
                buf = inputStream.readUnsignedByte() /* should be 0x01... */
                check(buf == 0x01) { "DO'87 expected 0x01 marker, found " + Integer.toHexString(buf and 0xFF) }
            }
        } else {
            /* Long form */
            val lengthBytesCount = buf and 0x0000007F
            for (i in 0..<lengthBytesCount) {
                length = (length shl 8) or inputStream.readUnsignedByte()
            }
            if (!do85) {
                buf = inputStream.readUnsignedByte() /* should be 0x01... */
                check(buf == 0x01) { "DO'87 expected 0x01 marker" }
            }
        }
        if (!do85) {
            length-- /* takes care of the extra 0x01 marker... */
        }
        /* Read, decrypt, unpad the data... */
        val ciphertext = ByteArray(length)
        inputStream.readFully(ciphertext)
        val paddedData = cipher.doFinal(ciphertext)
        return Util.unpad(paddedData)
    }

    /**
     * Reads a data object.
     * The `0x99` tag has already been read.
     * 
     * @param inputStream the stream to read from
     * @return the status word
     * @throws IOException on error reading from the stream
     */
    @Throws(IOException::class)
    private fun readDO99(inputStream: DataInputStream): Short {
        val length = inputStream.readUnsignedByte()
        check(length == 2) { "DO'99 wrong length" }
        val sw1 = inputStream.readByte()
        val sw2 = inputStream.readByte()
        return (((sw1.toInt() and 0x000000FF) shl 8) or (sw2.toInt() and 0x000000FF)).toShort()
    }

    /**
     * Reads a data object.
     * This assumes that the `0x8E` tag has already been read.
     * 
     * @param inputStream the stream to read from
     * @return the bytes that were read
     * @throws IOException on error
     */
    @Throws(IOException::class)
    private fun readDO8E(inputStream: DataInputStream): ByteArray {
        val length = inputStream.readUnsignedByte()
        check(!(length != 8 && length != 16)) { "DO'8E wrong length for MAC: $length" }
        val cc = ByteArray(length)
        inputStream.readFully(cc)
        return cc
    }

    override fun toString(): String {
        return StringBuilder()
            .append("SecureMessagingWrapper [")
            .append("ssc: ").append(this.sendSequenceCounter)
            .append(", ksEnc: ").append(this.encryptionKey)
            .append(", ksMac: ").append(this.mACKey)
            .append(", maxTranceiveLength: ").append(maxTranceiveLength)
            .append(", shouldCheckMAC: ").append(shouldCheckMAC)
            .append("]")
            .toString()
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + (if (this.encryptionKey == null) 0 else encryptionKey.hashCode())
        result = prime * result + (if (this.mACKey == null) 0 else mACKey.hashCode())
        result = prime * result + maxTranceiveLength
        result = prime * result + (if (shouldCheckMAC) 1231 else 1237)
        result =
            prime * result + (this.sendSequenceCounter xor (this.sendSequenceCounter ushr 32)).toInt()
        return result
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (obj == null) {
            return false
        }
        if (javaClass != obj.javaClass) {
            return false
        }

        val other = obj as SecureMessagingWrapper
        if (this.encryptionKey == null) {
            if (other.encryptionKey != null) {
                return false
            }
        } else if (this.encryptionKey != other.encryptionKey) {
            return false
        }
        if (this.mACKey == null) {
            if (other.mACKey != null) {
                return false
            }
        } else if (this.mACKey != other.mACKey) {
            return false
        }
        if (maxTranceiveLength != other.maxTranceiveLength) {
            return false
        }
        if (shouldCheckMAC != other.shouldCheckMAC) {
            return false
        }

        return this.sendSequenceCounter == other.sendSequenceCounter
    }

    companion object {
        private val LOGGER: Logger = Logger.getLogger("kmrtd.protocol")

        /**
         * Returns a copy of the given wrapper, with an identical (but perhaps independent)
         * state for known secure messaging wrapper types. If the wrapper type is not recognized
         * the original wrapper is returned.
         * 
         * @param wrapper the original wrapper
         * @return a copy of that wrapper
         */
        @JvmStatic
        fun getInstance(wrapper: SecureMessagingWrapper?): SecureMessagingWrapper? {
            try {
                if (wrapper is DESedeSecureMessagingWrapper) {
                    return DESedeSecureMessagingWrapper(wrapper)
                } else if (wrapper is AESSecureMessagingWrapper) {
                    return AESSecureMessagingWrapper(wrapper)
                }
            } catch (gse: GeneralSecurityException) {
                LOGGER.log(Level.WARNING, "Could not copy wrapper", gse)
            }

            LOGGER.warning("Not copying wrapper")
            return wrapper
        }
    }
}
