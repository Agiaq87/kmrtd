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
 * $Id: ChipAuthenticationInfo.java 1850 2021-05-21 06:25:03Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.lds

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.DLSequence
import java.math.BigInteger
import java.util.logging.Level
import java.util.logging.Logger

/**
 * A concrete SecurityInfo structure that stores chip authentication info,
 * see EAC 1.11 specification.
 * 
 * This data structure provides detailed information on an implementation of
 * Chip Authentication.
 * 
 *  * The object identifier `protocol` SHALL identify the
 * algorithms to be used (i.e. key agreement, symmetric cipher and MAC).
 *  * The integer `version` SHALL identify the version of the protocol.
 * Currently, versions 1 and 2 are supported.
 *  * The integer `keyId` MAY be used to indicate the local key identifier.
 * It MUST be used if the MRTD chip provides multiple public keys for Chip
 * Authentication.
 * 
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1850 $
 */
class ChipAuthenticationInfo @JvmOverloads constructor(
    /**
     * Returns the protocol object identifier.
     * 
     * @return the `ID_CA_` object identifier indicating the Chip Authentication protocol
     */
    override val objectIdentifier: String?,
    /**
     * Returns the Chip Authentication version (either 1 or 2).
     * 
     * @return the Chip Authentication version
     */
    val version: Int,
    /**
     * Returns a key identifier stored in this ChipAuthenticationInfo structure,
     * `null` if not present.
     * 
     * @return key identifier stored in this ChipAuthenticationInfo structure
     */
    @JvmField val keyId: BigInteger? = null
) : SecurityInfo() {
    /**
     * Constructs a new object.
     * 
     * @param objectIdentifier a proper EAC identifier
     * @param version has to be 1 or 2
     * @param keyId the key identifier
     */
    /**
     * Constructs a new object.
     * 
     * @param objectIdentifier a proper EAC identifier
     * @param version has to be 1 or 2
     */
    init {
        checkFields()
    }

    @get:Deprecated("Remove this method from visible interface (because of dependency on BC API)")
    override val dERObject: ASN1Primitive
        /**
         * Returns a DER object with this SecurityInfo data (DER sequence).
         * 
         * @return a DER object with this SecurityInfo data
         * 
         */
        get() {
            val v = ASN1EncodableVector()
            v.add(ASN1ObjectIdentifier(this.objectIdentifier))
            v.add(ASN1Integer(version.toLong()))
            if (keyId != null) {
                v.add(ASN1Integer(keyId))
            }
            return DLSequence(v)
        }

    override val protocolOIDString: String?
        /**
         * Returns the protocol object identifier as a human readable string.
         * 
         * @return a string
         */
        get() = toProtocolOIDString(this.objectIdentifier)

    /**
     * Checks the correctness of the data for this instance of SecurityInfo.
     * Throws an `IllegalArgumentException` when not correct.
     */
    protected fun checkFields() {
        try {
            require(checkRequiredIdentifier(this.objectIdentifier)) { "Wrong identifier: " + this.objectIdentifier }
            if (version != VERSION_1 && version != VERSION_2) {
                LOGGER.warning("Wrong version. Was expecting " + VERSION_1 + " or " + VERSION_2 + ", found " + version)
            }
        } catch (e: Exception) {
            LOGGER.log(Level.WARNING, "Unexpected exception", e)
            throw IllegalArgumentException("Malformed ChipAuthenticationInfo.")
        }
    }

    override fun toString(): String {
        return ("ChipAuthenticationInfo ["
                + "protocol: " + toProtocolOIDString(this.objectIdentifier)
                + ", version: " + version
                + ", keyId: " + (if (keyId == null) "-" else keyId) + "]")
    }

    override fun hashCode(): Int {
        return 3 + 11 * (if (this.objectIdentifier == null) 0 else objectIdentifier.hashCode()) + 61 * version + 1991 * (if (keyId == null) 111 else keyId.hashCode())
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        if (other === this) {
            return true
        }
        if (ChipAuthenticationInfo::class.java != other.javaClass) {
            return false
        }

        val otherChipAuthenticationInfo = other as ChipAuthenticationInfo
        return this.objectIdentifier == otherChipAuthenticationInfo.objectIdentifier
                && version == otherChipAuthenticationInfo.version && (keyId == null && otherChipAuthenticationInfo.keyId == null || keyId != null && keyId == otherChipAuthenticationInfo.keyId)
    }

    companion object {
        private const val serialVersionUID = 5591988305059068535L

        private val LOGGER: Logger = Logger.getLogger("kmrtd")

        /** Chip Authentication version 1.  */
        const val VERSION_1: Int = 1

        /** Chip Authentication version 2.  */
        const val VERSION_2: Int = 2

        /**
         * Checks whether the given object identifier identifies a
         * ChipAuthenticationInfo structure.
         * 
         * FIXME: for EAC 1.11 only the 3DES OIDs are allowed.
         * 
         * @param oid
         * object identifier
         * @return true if the match is positive
         */
        fun checkRequiredIdentifier(oid: String?): Boolean {
            return ID_CA_DH_3DES_CBC_CBC == oid
                    || ID_CA_ECDH_3DES_CBC_CBC == oid
                    || ID_CA_DH_AES_CBC_CMAC_128 == oid
                    || ID_CA_DH_AES_CBC_CMAC_192 == oid
                    || ID_CA_DH_AES_CBC_CMAC_256 == oid
                    || ID_CA_ECDH_AES_CBC_CMAC_128 == oid
                    || ID_CA_ECDH_AES_CBC_CMAC_192 == oid
                    || ID_CA_ECDH_AES_CBC_CMAC_256 == oid
        }

        /**
         * Returns the key agreement algorithm (`"DH"` or `"ECDH"`
         * for the given Chip Authentication info object identifier.
         * 
         * @param oid a EAC-CA protocol object identifier
         * 
         * @return the key agreement algorithm
         */
        fun toKeyAgreementAlgorithm(oid: String): String {
            if (oid == null) {
                throw NumberFormatException("Unknown OID: null")
            }

            if (ID_CA_DH_3DES_CBC_CBC == oid
                || ID_CA_DH_AES_CBC_CMAC_128 == oid
                || ID_CA_DH_AES_CBC_CMAC_192 == oid
                || ID_CA_DH_AES_CBC_CMAC_256 == oid
            ) {
                return "DH"
            } else if (ID_CA_ECDH_3DES_CBC_CBC == oid
                || ID_CA_ECDH_AES_CBC_CMAC_128 == oid
                || ID_CA_ECDH_AES_CBC_CMAC_192 == oid
                || ID_CA_ECDH_AES_CBC_CMAC_256 == oid
            ) {
                return "ECDH"
            }

            throw NumberFormatException("Unknown OID: \"" + oid + "\"")
        }

        /**
         * Returns the encryption algorithm (`"DESede"` or `"AES"`)
         * for the given EAC-CA info object identifier.
         * 
         * @param oid a EAC-CA protocol object identifier
         * 
         * @return a JCE mnemonic cipher algorithm string
         */
        fun toCipherAlgorithm(oid: String?): String {
            if (ID_CA_DH_3DES_CBC_CBC == oid
                || ID_CA_ECDH_3DES_CBC_CBC == oid
            ) {
                return "DESede"
            } else if (ID_CA_DH_AES_CBC_CMAC_128 == oid
                || ID_CA_DH_AES_CBC_CMAC_192 == oid
                || ID_CA_DH_AES_CBC_CMAC_256 == oid
                || ID_CA_ECDH_AES_CBC_CMAC_128 == oid
                || ID_CA_ECDH_AES_CBC_CMAC_192 == oid
                || ID_CA_ECDH_AES_CBC_CMAC_256 == oid
            ) {
                return "AES"
            }

            throw NumberFormatException("Unknown OID: \"" + oid + "\"")
        }

        /**
         * Returns the digest algorithm (`"SHA-1"` or `"SHA-256"`)
         * for the given EAC-CA protocol object identifier.
         * 
         * @param oid a EAC-CA protocol object identifier
         * 
         * @return a JCE mnemonic digest algorithm string
         */
        fun toDigestAlgorithm(oid: String?): String {
            if (ID_CA_DH_3DES_CBC_CBC == oid
                || ID_CA_ECDH_3DES_CBC_CBC == oid
                || ID_CA_DH_AES_CBC_CMAC_128 == oid
                || ID_CA_ECDH_AES_CBC_CMAC_128 == oid
            ) {
                return "SHA-1"
            } else if (ID_CA_DH_AES_CBC_CMAC_192 == oid
                || ID_CA_ECDH_AES_CBC_CMAC_192 == oid
                || ID_CA_DH_AES_CBC_CMAC_256 == oid
                || ID_CA_ECDH_AES_CBC_CMAC_256 == oid
            ) {
                return "SHA-256"
            }

            throw NumberFormatException("Unknown OID: \"" + oid + "\"")
        }

        /**
         * Returns the key length in bits (128, 192, or 256)
         * for the given EAC-CA protocol object identifier.
         * 
         * @param oid a EAC-CA protocol object identifier
         * 
         * @return a key length in bits
         */
        fun toKeyLength(oid: String?): Int {
            if (ID_CA_DH_3DES_CBC_CBC == oid
                || ID_CA_ECDH_3DES_CBC_CBC == oid
                || ID_CA_DH_AES_CBC_CMAC_128 == oid
                || ID_CA_ECDH_AES_CBC_CMAC_128 == oid
            ) {
                return 128
            } else if (ID_CA_DH_AES_CBC_CMAC_192 == oid
                || ID_CA_ECDH_AES_CBC_CMAC_192 == oid
            ) {
                return 192
            } else if (ID_CA_DH_AES_CBC_CMAC_256 == oid
                || ID_CA_ECDH_AES_CBC_CMAC_256 == oid
            ) {
                return 256
            }

            throw NumberFormatException("Unknown OID: \"" + oid + "\"")
        }

        /**
         * Returns an ASN1 name for the given EAC-CA protocol identifier.
         * 
         * @param oid a EAC-CA protocol identifier
         * 
         * @return an ASN1 name
         */
        private fun toProtocolOIDString(oid: String?): String? {
            if (ID_CA_DH_3DES_CBC_CBC == oid) {
                return "id-CA-DH-3DES-CBC-CBC"
            }
            if (ID_CA_DH_AES_CBC_CMAC_128 == oid) {
                return "id-CA-DH-AES-CBC-CMAC-128"
            }
            if (ID_CA_DH_AES_CBC_CMAC_192 == oid) {
                return "id-CA-DH-AES-CBC-CMAC-192"
            }
            if (ID_CA_DH_AES_CBC_CMAC_256 == oid) {
                return "id-CA-DH-AES-CBC-CMAC-256"
            }
            if (ID_CA_ECDH_3DES_CBC_CBC == oid) {
                return "id-CA-ECDH-3DES-CBC-CBC"
            }
            if (ID_CA_ECDH_AES_CBC_CMAC_128 == oid) {
                return "id-CA-ECDH-AES-CBC-CMAC-128"
            }
            if (ID_CA_ECDH_AES_CBC_CMAC_192 == oid) {
                return "id-CA-ECDH-AES-CBC-CMAC-192"
            }
            if (ID_CA_ECDH_AES_CBC_CMAC_256 == oid) {
                return "id-CA-ECDH-AES-CBC-CMAC-256"
            }

            return oid
        }
    }
}
