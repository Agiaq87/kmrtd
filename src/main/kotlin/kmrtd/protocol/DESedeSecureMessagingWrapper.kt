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
 * $Id: DESedeSecureMessagingWrapper.java 1805 2018-11-26 21:39:46Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.protocol

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.IOException
import java.io.Serializable
import java.util.logging.Level
import java.util.logging.Logger
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

/**
 * Secure messaging wrapper for APDUs.
 * Initially based on Section E.3 of ICAO-TR-PKI.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * @version $Revision: 1805 $
 */
class DESedeSecureMessagingWrapper
/**
 * Constructs a secure messaging wrapper based on the secure messaging
 * session keys and the initial value of the send sequence counter.
 * Used in BAC and EAC 1.
 * 
 * @param ksEnc              the session key for encryption
 * @param ksMac              the session key for macs
 * @param maxTranceiveLength the maximum tranceive length, typical values are 256 or 65536
 * @param shouldCheckMAC     a boolean indicating whether this wrapper will check the MAC in wrapped response APDUs
 * @param ssc                the initial value of the send sequence counter
 * @throws GeneralSecurityException when the available JCE providers cannot provide the necessary cryptographic primitives
 */
    (
    ksEnc: SecretKey?,
    ksMac: SecretKey?,
    maxTranceiveLength: Int,
    shouldCheckMAC: Boolean,
    ssc: Long
) : SecureMessagingWrapper(
    ksEnc,
    ksMac,
    "DESede/CBC/NoPadding",
    "ISO9797Alg3Mac",
    maxTranceiveLength,
    shouldCheckMAC,
    ssc
), Serializable {
    /**
     * Constructs a secure messaging wrapper based on the secure messaging
     * session keys. The initial value of the send sequence counter is set to
     * `0L`.
     * 
     * @param ksEnc          the session key for encryption
     * @param ksMac          the session key for macs
     * @param shouldCheckMAC a boolean indicating whether this wrapper will check the MAC in wrapped response APDUs
     * @throws GeneralSecurityException when the available JCE providers cannot provide the necessary
     * cryptographic primitives
     * (`"DESede/CBC/Nopadding"` Cipher, `"ISO9797Alg3Mac"` Mac).
     */
    /**
     * Constructs a secure messaging wrapper based on the secure messaging
     * session keys. The initial value of the send sequence counter is set to
     * `0L`.
     * 
     * @param ksEnc the session key for encryption
     * @param ksMac the session key for macs
     * @throws GeneralSecurityException when the available JCE providers cannot provide the necessary
     * cryptographic primitives
     * (`"DESede/CBC/Nopadding"` Cipher, `"ISO9797Alg3Mac"` Mac).
     */
    @JvmOverloads
    constructor(ksEnc: SecretKey?, ksMac: SecretKey?, shouldCheckMAC: Boolean = true) : this(
        ksEnc,
        ksMac,
        256,
        shouldCheckMAC,
        0L
    )

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
    constructor(wrapper: DESedeSecureMessagingWrapper) : this(
        wrapper.encryptionKey,
        wrapper.mACKey,
        wrapper.maxTranceiveLength,
        wrapper.shouldCheckMAC(),
        wrapper.sendSequenceCounter
    )

    /**
     * Returns the type of secure messaging wrapper.
     * In this case `"DESede"` will be returned.
     * 
     * @return the type of secure messaging wrapper
     */
    override fun getType(): String {
        return "DESede"
    }

    /**
     * Returns the length (in bytes) to use for padding.
     * For 3DES this is 8.
     *
     * @return the length to use for padding
     */
    override val padLength: Int
        get() = 8

    /*fun getPadLength(): Int =
        8*/

    override val encodedSendSequenceCounter: ByteArray?
        get() {
            val byteArrayOutputStream = ByteArrayOutputStream()
            try {
                val dataOutputStream = DataOutputStream(byteArrayOutputStream)
                dataOutputStream.writeLong(sendSequenceCounter)
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

            return byteArrayOutputStream.toByteArray()
        }

    /*fun getEncodedSendSequenceCounter(): ByteArray? {
        val byteArrayOutputStream = ByteArrayOutputStream()
        try {
            val dataOutputStream = DataOutputStream(byteArrayOutputStream)
            dataOutputStream.writeLong(sendSequenceCounter)
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

        return byteArrayOutputStream.toByteArray()
    }*/

    override fun toString(): String {
        return StringBuilder()
            .append("DESedeSecureMessagingWrapper [")
            .append("ssc: ").append(sendSequenceCounter)
            .append(", kEnc: ").append(encryptionKey)
            .append(", kMac: ").append(mACKey)
            .append(", shouldCheckMAC: ").append(shouldCheckMAC())
            .append(", maxTranceiveLength: ").append(maxTranceiveLength)
            .append("]")
            .toString()
    }

    override fun hashCode(): Int {
        return 31 * super.hashCode() + 13
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

    override val iV: IvParameterSpec
        get() = ZERO_IV_PARAM_SPEC

    /*fun getIV(): IvParameterSpec =
        ZERO_IV_PARAM_SPEC*/

    companion object {
        /**
         * Initialization vector consisting of 8 zero bytes.
         */
        val ZERO_IV_PARAM_SPEC: IvParameterSpec =
            IvParameterSpec(byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0))
        private val LOGGER: Logger = Logger.getLogger("org.jmrtd")
    }
}
