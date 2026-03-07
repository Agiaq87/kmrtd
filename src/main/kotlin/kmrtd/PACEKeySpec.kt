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
 * $Id: PACEKeySpec.java 1816 2019-07-15 13:02:26Z martijno $
 */
package kmrtd

import kmrtd.protocol.PACEProtocol
import java.security.GeneralSecurityException

/**
 * A key for PACE, can be CAN, MRZ, PIN, or PUK.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * @version $Revision: 1816 $
 * 
 * 
 * (Contributions by g.giorkhelidze.)
 */
data class PACEKeySpec
/**
 * Constructs a key.
 * 
 * @param key          CAN, MRZ, PIN, PUK password bytes
 * @param keyReference indicates the type of key, valid values are
 * `MRZ_PACE_KEY_REFERENCE`, `CAN_PACE_KEY_REFERENCE`,
 * `PIN_PACE_KEY_REFERENCE`, `PUK_PACE_KEY_REFERENCE`
 */(
    /**
     * Returns the key bytes.
     * 
     * @return the key bytes
     */
    override val key: ByteArray,
    /**
     * Returns the type of key, valid values are
     * `MRZ_PACE_KEY_REFERENCE`, `CAN_PACE_KEY_REFERENCE`,
     * `PIN_PACE_KEY_REFERENCE`, `PUK_PACE_KEY_REFERENCE`.
     * 
     * @return the type of key
     */
    val keyReference: Byte
) : AccessKeySpec {
    /**
     * Constructs a PACE key from a string value.
     * 
     * @param key          the string value containing CAN, PIN or PUK
     * @param keyReference indicates the type of key, valid values are
     * `MRZ_PACE_KEY_REFERENCE`, `CAN_PACE_KEY_REFERENCE`,
     * `PIN_PACE_KEY_REFERENCE`, `PUK_PACE_KEY_REFERENCE`
     */
    constructor(key: String, keyReference: Byte) : this(Util.getBytes(key), keyReference)

    override val algorithm: String
        /**
         * Returns the algorithm.
         * 
         * @return the algorithm
         */
        get() = "PACE"

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + key.contentHashCode()
        result = prime * result + keyReference
        return result
    }

    /*override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (obj == null) {
            return false
        }
        if (javaClass != obj.javaClass) {
            return false
        }
        val other = obj as PACEKeySpec
        if (!key.contentEquals(other.key)) {
            return false
        }
        if (keyReference != other.keyReference) {
            return false
        }
        return true
    }

    override fun toString(): String {
        return StringBuilder()
            .append("PACEKeySpec [")
            .append("key: ").append(Hex.bytesToHexString(key)).append(", ")
            .append("keyReference: ").append(keyReferenceToString(keyReference))
            .append("]")
            .toString()
    }*/

    companion object {

        /**
         * Creates a PACE key from relevant details from a Machine Readable Zone.
         * 
         * @param mrz the details from the Machine Readable Zone
         * @return the PACE key
         * @throws GeneralSecurityException on error
         */
        @Throws(GeneralSecurityException::class)
        fun createMRZKey(mrz: BACKeySpec): PACEKeySpec =
            PACEKeySpec(
                PACEProtocol.computeKeySeedForPACE(mrz),
                PassportService.MRZ_PACE_KEY_REFERENCE
            )

        /**
         * Creates a PACE key from a Card Access Number.
         * 
         * @param can the Card Access Number
         * @return the PACE key
         */
        fun createCANKey(can: String): PACEKeySpec =
            PACEKeySpec(can, PassportService.CAN_PACE_KEY_REFERENCE)

        /**
         * Creates a PACE key from a PIN.
         * 
         * @param pin the PIN
         * @return the PACE key
         */
        fun createPINKey(pin: String): PACEKeySpec =
            PACEKeySpec(pin, PassportService.PIN_PACE_KEY_REFERENCE)

        /**
         * Creates a PACE key from a PUK.
         * 
         * @param puk the PUK
         * @return the PACE key
         */
        fun createPUKKey(puk: String): PACEKeySpec =
            PACEKeySpec(puk, PassportService.PUK_PACE_KEY_REFERENCE)

        /**
         * Returns a textual representation of the given key reference parameter.
         * 
         * @param keyReference a key reference parameter
         * @return a textual representation of the key reference
         */
        private fun keyReferenceToString(keyReference: Byte): String =
            when (keyReference) {
                PassportService.MRZ_PACE_KEY_REFERENCE -> "MRZ"
                PassportService.CAN_PACE_KEY_REFERENCE -> "CAN"
                PassportService.PIN_PACE_KEY_REFERENCE -> "PIN"
                PassportService.PUK_PACE_KEY_REFERENCE -> "PUK"
                PassportService.NO_PACE_KEY_REFERENCE -> "NO"
                else -> keyReference.toInt().toString()
            }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PACEKeySpec

        if (keyReference != other.keyReference) return false
        if (!key.contentEquals(other.key)) return false
        if (algorithm != other.algorithm) return false

        return true
    }
}

