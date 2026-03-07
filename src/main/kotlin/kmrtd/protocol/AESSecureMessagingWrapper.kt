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
 * $Id: AESSecureMessagingWrapper.java 1805 2018-11-26 21:39:46Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.protocol

import kmrtd.Util
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.IOException
import java.io.Serializable
import java.security.GeneralSecurityException
import java.util.logging.Level
import java.util.logging.Logger
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

/**
 * An AES secure messaging wrapper for APDUs. Based on TR-SAC.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * @version $Revision: 1805 $
 */
class AESSecureMessagingWrapper(
    ksEnc: SecretKey?,
    ksMac: SecretKey?,
    maxTranceiveLength: Int,
    shouldCheckMAC: Boolean,
    ssc: Long
) : SecureMessagingWrapper(
    ksEnc,
    ksMac,
    "AES/CBC/NoPadding",
    "AESCMAC",
    maxTranceiveLength,
    shouldCheckMAC,
    ssc
), Serializable {
    @Transient
    private val sscIVCipher: Cipher =
        Util.getCipher("AES/ECB/NoPadding", Cipher.ENCRYPT_MODE, ksEnc)

    /**
     * Constructs a secure messaging wrapper based on the secure messaging
     * session keys and the initial value of the send sequence counter.
     * Used in BAC and EAC 1.
     * 
     * @param ksEnc the session key for encryption
     * @param ksMac the session key for macs
     * @param ssc   the initial value of the send sequence counter
     * @throws GeneralSecurityException when the available JCE providers cannot provide the necessary cryptographic primitives
     */
    constructor(ksEnc: SecretKey?, ksMac: SecretKey?, ssc: Long) : this(
        ksEnc,
        ksMac,
        256,
        true,
        ssc
    )

    /**
     * Constructs a secure messaging wrapper based on the given existing secure messaging wrapper.
     * This is a convenience copy constructor.
     * 
     * @param wrapper an existing wrapper
     * @throws GeneralSecurityException when the available JCE providers cannot provide the necessary cryptographic primitives
     */
    constructor(wrapper: AESSecureMessagingWrapper) : this(
        wrapper.encryptionKey,
        wrapper.mACKey,
        wrapper.maxTranceiveLength,
        wrapper.shouldCheckMAC(),
        wrapper.sendSequenceCounter
    )

    /**
     * Returns the type of secure messaging wrapper (in this case `"AES"`).
     * 
     * @return the type of secure messaging wrapper
     */
    override fun getType(): String {
        return "AES"
    }

    /**
     * Returns the length (in bytes) to use for padding.
     * For AES this is 16.
     *
     * @return the length to use for padding
     */
    override val padLength: Int
        get() = 16

    /*fun getPadLength(): Int {
        return 16
    }*/

    /**
     * Returns the send sequence counter as bytes, making sure
     * the 128 bit (16 byte) block-size is used.
     *
     * @return the send sequence counter as a 16 byte array
     */
    override val encodedSendSequenceCounter: ByteArray?
        get() {
            val byteArrayOutputStream = ByteArrayOutputStream(16)
            try {
                byteArrayOutputStream.write(0x00)
                byteArrayOutputStream.write(0x00)
                byteArrayOutputStream.write(0x00)
                byteArrayOutputStream.write(0x00)
                byteArrayOutputStream.write(0x00)
                byteArrayOutputStream.write(0x00)
                byteArrayOutputStream.write(0x00)
                byteArrayOutputStream.write(0x00)

                /* A long will take 8 bytes. */
                val dataOutputStream = DataOutputStream(byteArrayOutputStream)
                dataOutputStream.writeLong(sendSequenceCounter)
                dataOutputStream.close()
                return byteArrayOutputStream.toByteArray()
            } catch (ioe: IOException) {
                /* Never happens. */
                LOGGER.log(Level.FINE, "Error writing to stream", ioe)
            } finally {
                try {
                    byteArrayOutputStream.close()
                } catch (ioe: IOException) {
                    LOGGER.log(Level.FINE, "Error closing stream", ioe)
                }
            }
            return null
        }

    /*fun getEncodedSendSequenceCounter(): ByteArray? {
        val byteArrayOutputStream = ByteArrayOutputStream(16)
        try {
            byteArrayOutputStream.write(0x00)
            byteArrayOutputStream.write(0x00)
            byteArrayOutputStream.write(0x00)
            byteArrayOutputStream.write(0x00)
            byteArrayOutputStream.write(0x00)
            byteArrayOutputStream.write(0x00)
            byteArrayOutputStream.write(0x00)
            byteArrayOutputStream.write(0x00)

            *//* A long will take 8 bytes. *//*
            val dataOutputStream = DataOutputStream(byteArrayOutputStream)
            dataOutputStream.writeLong(sendSequenceCounter)
            dataOutputStream.close()
            return byteArrayOutputStream.toByteArray()
        } catch (ioe: IOException) {
            *//* Never happens. *//*
            LOGGER.log(Level.FINE, "Error writing to stream", ioe)
        } finally {
            try {
                byteArrayOutputStream.close()
            } catch (ioe: IOException) {
                LOGGER.log(Level.FINE, "Error closing stream", ioe)
            }
        }
        return null
    }*/

    override fun toString(): String {
        return StringBuilder()
            .append("AESSecureMessagingWrapper [")
            .append("ssc: ").append(sendSequenceCounter)
            .append(", kEnc: ").append(encryptionKey)
            .append(", kMac: ").append(mACKey)
            .append(", shouldCheckMAC: ").append(shouldCheckMAC())
            .append(", maxTranceiveLength: ").append(maxTranceiveLength)
            .append("]")
            .toString()
    }

    override fun hashCode(): Int {
        return 71 * super.hashCode() + 17
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

        return super.equals(obj)
    }

    /**
     * Returns the IV by encrypting the send sequence counter.
     *
     *
     * AES uses IV = E K_Enc , SSC), see ICAO SAC TR Section 4.6.3.
     *
     * @return the initialization vector specification
     * @throws GeneralSecurityException on error
     */
    override val iV: IvParameterSpec
        get() {
            val encryptedSSC = sscIVCipher.doFinal(encodedSendSequenceCounter)
            return IvParameterSpec(encryptedSSC)
        }

    /*@Throws(GeneralSecurityException::class)
    fun getIV(): IvParameterSpec {
        val encryptedSSC = sscIVCipher.doFinal(getEncodedSendSequenceCounter())
        return IvParameterSpec(encryptedSSC)
    }*/

    companion object {
        private val LOGGER: Logger = Logger.getLogger("org.jmrtd")
    }
}
