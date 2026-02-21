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
 * $Id: ChipAuthenticationPublicKeyInfo.java 1819 2019-09-26 12:40:53Z martijno $
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
import org.jmrtd.Util
import java.math.BigInteger
import java.security.PublicKey
import java.util.logging.Level
import java.util.logging.Logger

/**
 * A concrete SecurityInfo structure that stores chip authentication public
 * key info, see EAC TR 03110 1.11 specification.
 * 
 * This data structure provides a Chip Authentication Public Key of the MRTD chip.
 * 
 *  * The object identifier `protocol` SHALL identify the type of the public key
 * (i.e. DH or ECDH).
 *  * The sequence `chipAuthenticationPublicKey` SHALL contain the public key
 * in encoded form.
 *  * The integer `keyId` MAY be used to indicate the local key identifier.
 * It MUST be used if the MRTD chip provides multiple public keys for Chip
 * Authentication.
 * 
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1819 $
 */
class ChipAuthenticationPublicKeyInfo @JvmOverloads constructor(
    override val objectIdentifier: String, publicKey: PublicKey?,
    /**
     * Returns a key identifier stored in this ChipAuthenticationPublicKeyInfo
     * structure, `null` if not present.
     * 
     * @return key identifier stored in this ChipAuthenticationPublicKeyInfo structure
     */
    /* Optional, use null if implicit. */val keyId: BigInteger? = null
) : SecurityInfo() {
    /**
     * Returns a SubjectPublicKeyInfo contained in this
     * ChipAuthenticationPublicKeyInfo structure.
     * 
     * @return SubjectPublicKeyInfo contained in this
     * ChipAuthenticationPublicKeyInfo structure
     */
    val subjectPublicKey: PublicKey? = Util.reconstructPublicKey(publicKey)

    /**
     * Creates a public key info structure.
     * 
     * @param publicKey Either a DH public key or an EC public key
     * @param keyId key identifier
     */
    /**
     * Creates a public key info structure with implicit key identifier.
     * 
     * @param publicKey Either a DH public key or an EC public key
     */
    @JvmOverloads
    constructor(publicKey: PublicKey, keyId: BigInteger? = null) : this(
        Util.inferProtocolIdentifier(publicKey),
        publicKey,
        keyId
    )

    /**
     * Creates a public key info structure.
     * 
     * @param objectIdentifier a proper public key identifier
     * @param publicKey appropriate public key
     * @param keyId the key identifier or `null` if not present
     */
    /**
     * Creates a public key info structure with implicit key identifier.
     * 
     * @param objectIdentifier a proper public key identifier
     * @param publicKey appropriate public key
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
            val vector = ASN1EncodableVector()
            val subjectPublicKeyInfo =
                Util.toSubjectPublicKeyInfo(this.subjectPublicKey)
            if (subjectPublicKeyInfo == null) {
                LOGGER.log(
                    Level.WARNING,
                    "Could not convert public key to subject-public-key-info structure"
                )
            } else {
                vector.add(ASN1ObjectIdentifier(this.objectIdentifier))
                vector.add((subjectPublicKeyInfo.toASN1Primitive()))
                if (keyId != null) {
                    vector.add(ASN1Integer(keyId))
                }
            }
            return DLSequence(vector)
        }

    override val protocolOIDString: String?
        /**
         * Returns the protocol object identifier as a human readable string.
         * 
         * @return a string
         */
        get() = toProtocolOIDString(this.objectIdentifier)

    /**
     * Checks the correctness of the data for this instance of `SecurityInfo`.
     */
    // FIXME: also check type of public key
    protected fun checkFields() {
        try {
            require(checkRequiredIdentifier(this.objectIdentifier)) { "Wrong identifier: " + this.objectIdentifier }
        } catch (e: Exception) {
            throw IllegalArgumentException("Malformed ChipAuthenticationInfo", e)
        }
    }

    override fun toString(): String {
        return ("ChipAuthenticationPublicKeyInfo ["
                + "protocol: " + toProtocolOIDString(this.objectIdentifier) + ", "
                + "chipAuthenticationPublicKey: " + Util.getDetailedPublicKeyAlgorithm(this.subjectPublicKey) + ", "
                + "keyId: " + (keyId?.toString() ?: "-")
                + "]")
    }

    override fun hashCode(): Int {
        return 123 + 1337 * (objectIdentifier.hashCode() + (if (keyId == null) 111 else keyId.hashCode()) + (if (this.subjectPublicKey == null) 111 else subjectPublicKey.hashCode()))
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        if (other === this) {
            return true
        }
        if (ChipAuthenticationPublicKeyInfo::class.java != other.javaClass) {
            return false
        }

        val otherInfo = other as ChipAuthenticationPublicKeyInfo
        return this.objectIdentifier == otherInfo.objectIdentifier
                && (keyId == null && otherInfo.keyId == null || keyId != null && keyId == otherInfo.keyId)
                && this.subjectPublicKey == otherInfo.subjectPublicKey
    }

    companion object {
        private const val serialVersionUID = 5687291829854501771L

        private val LOGGER: Logger = Logger.getLogger("org.jmrtd")

        /**
         * Checks whether the given object identifier identifies a
         * ChipAuthenticationPublicKeyInfo structure.
         * 
         * @param oid object identifier
         * 
         * @return true if the match is positive
         */
        fun checkRequiredIdentifier(oid: String?): Boolean {
            return ID_PK_DH == oid || ID_PK_ECDH == oid
        }

        /**
         * Returns the key agreement algorithm (`"DH"` or `"ECDH"`
         * for the given Chip Authentication Public Key info object identifier.
         * This may throw an unchecked exception if the given object identifier not
         * a known Chip Authentication Public Key info object identifier.
         * 
         * @param oid a EAC-CA public key info object identifier
         * 
         * @return the key agreement algorithm
         */
        fun toKeyAgreementAlgorithm(oid: String): String {
            if (oid == null) {
                throw NumberFormatException("Unknown OID: null")
            }

            if (ID_PK_DH == oid) {
                return "DH"
            }
            if (ID_PK_ECDH == oid) {
                return "ECDH"
            }

            throw NumberFormatException("Unknown OID: \"" + oid + "\"")
        }

        /**
         * Returns an ASN1 name for the protocol object identifier.
         * 
         * @param oid the protocol object identifier
         * 
         * @return an ASN1 name if known, or the object identifier itself if not
         */
        private fun toProtocolOIDString(oid: String?): String? {
            if (ID_PK_DH == oid) {
                return "id-PK-DH"
            }
            if (ID_PK_ECDH == oid) {
                return "id-PK-ECDH"
            }

            return oid
        }
    }
}
