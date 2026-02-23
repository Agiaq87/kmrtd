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
 * $Id: ActiveAuthenticationInfo.java 1858 2021-07-19 07:10:20Z martijno $
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
import java.security.NoSuchAlgorithmException
import java.util.logging.Logger

/*
 * <pre>
 * ActiveAuthenticationInfo ::= SEQUENCE {
 *    protocol id-icao-mrtd-security-aaProtocolObject,
 *    version INTEGER -- MUST be 1
 *    signatureAlgorithm OBJECT IDENTIFIER
 * }
 *
 * -- Object Identifiers
 * id-icao OBJECT IDENTIFIER ::= {2 23 136}
 * id-icao-mrtd OBJECT IDENTIFIER ::= {id-icao 1}
 * id-icao-mrtd-security OBJECT IDENTIFIER ::= {id-icao-mrtd 1}
 *
 * id-icao-mrtd-security-aaProtocolObject OBJECT IDENTIFIER ::=
 *    {id-icao-mrtd-security 5}
 * </pre>
 */
/**
 * A concrete SecurityInfo structure that stores active authentication
 * info, see TR-LDS-PKI Maintenance V1.0.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1858 $
 */
class ActiveAuthenticationInfo internal constructor(
    /**
     * Returns the protocol object identifier of this AA security info.
     * 
     * @return an object identifier
     */
    override val objectIdentifier: String?,
    /**
     * Returns the version of the Active Authentication protocol (should be 1).
     * 
     * @return the version
     */
    val version: Int,
    /**
     * Returns the signature algorithm object identifier.
     * 
     * @return signature algorithm OID
     */
    val signatureAlgorithmOID: String?
) : SecurityInfo() {
    /**
     * Constructs a new object.
     * 
     * @param objectIdentifier the id_AA identifier
     * @param version has to be 1
     * @param signatureAlgorithmOID the signature algorithm OID
     */
    init {
        checkFields()
    }

    /**
     * Constructs a new object.
     * 
     * @param signatureAlgorithmOID the signature algorithm OID
     */
    constructor(signatureAlgorithmOID: String?) : this(ID_AA, VERSION_1, signatureAlgorithmOID)

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
            if (signatureAlgorithmOID != null) {
                v.add(ASN1ObjectIdentifier(signatureAlgorithmOID))
            }
            return DLSequence(v)
        }

    override val protocolOIDString: String?
        /**
         * Returns the protocol object identifier as a human readable string.
         * 
         * @return a string representing the protocol object identifier
         */
        get() = toProtocolOIDString(this.objectIdentifier)

    /**
     * Returns a textual representation of this object.
     * 
     * @return a textual representation of this object
     */
    override fun toString(): String {
        val result = StringBuilder()
        result.append("ActiveAuthenticationInfo")
        result.append(" [")
        result.append("protocol: " + toProtocolOIDString(this.objectIdentifier))
        result.append(", ")
        result.append("version: $version")
        result.append(", ")
        result.append(
            "signatureAlgorithmOID: " + toSignatureAlgorithmOIDString(
                this.signatureAlgorithmOID
            )
        )
        result.append("]")
        return result.toString()
    }

    /**
     * Tests equality with respect to another object.
     * 
     * @param other another object
     * 
     * @return whether this object equals the other object
     */
    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        if (other === this) {
            return true
        }
        if (ActiveAuthenticationInfo::class.java != other.javaClass) {
            return false
        }
        val otherActiveAuthenticationInfo = other as ActiveAuthenticationInfo
        return this.dERObject.equals(otherActiveAuthenticationInfo.dERObject)
    }

    /**
     * Returns the hash code of this object.
     * 
     * @return the hash code
     */
    override fun hashCode(): Int {
        return (12345
                + 3 * (if (this.objectIdentifier == null) 0 else objectIdentifier.hashCode()) + 5 * version + 11 * (signatureAlgorithmOID?.hashCode()
            ?: 1))
    }

    /**
     * Checks the correctness of the data for this instance of `SecurityInfo`.
     */
    private fun checkFields() {
        try {
            require(checkRequiredIdentifier(this.objectIdentifier)) { "Wrong identifier: " + this.objectIdentifier }
            if (version != VERSION_1) {
                LOGGER.warning("Wrong version: $version")
            }

            /* FIXME check to see if signatureAlgorithmOID is valid. */
            require(!((ECDSA_PLAIN_SHA1_OID != signatureAlgorithmOID) && (ECDSA_PLAIN_SHA224_OID != signatureAlgorithmOID) && (ECDSA_PLAIN_SHA256_OID != signatureAlgorithmOID) && (ECDSA_PLAIN_SHA384_OID != signatureAlgorithmOID) && (ECDSA_PLAIN_SHA512_OID != signatureAlgorithmOID) && (ECDSA_PLAIN_RIPEMD160_OID != signatureAlgorithmOID))) { "Wrong signature algorithm OID: " + signatureAlgorithmOID }
        } catch (e: Exception) {
            throw IllegalArgumentException("Malformed ActiveAuthenticationInfo", e)
        }
    }

    /**
     * Returns a human readable rendering of the given object identifier string.
     * 
     * @param oid the object identifier (dotted notation)
     * 
     * @return a human readable string representing the given object identifier
     */
    private fun toProtocolOIDString(oid: String?): String? {
        if (ID_AA == oid) {
            return "id-AA"
        }

        return oid
    }

    companion object {
        private const val serialVersionUID = 6830847342039845308L

        private val LOGGER: Logger = Logger.getLogger("kmrtd.lds")

        const val VERSION_1: Int = 1

        /** Specified in BSI TR 03111 Section 5.2.1.  */
        const val ECDSA_PLAIN_SIGNATURES: String = "0.4.0.127.0.7.1.1.4.1"
        const val ECDSA_PLAIN_SHA1_OID: String = "$ECDSA_PLAIN_SIGNATURES.1" /* 0.4.0.127.0.7.1.1.4.1.1, ecdsa-plain-SHA1 */
        const val ECDSA_PLAIN_SHA224_OID: String =
            "$ECDSA_PLAIN_SIGNATURES.2" /* 0.4.0.127.0.7.1.1.4.1.2, ecdsa-plain-SHA224 */
        const val ECDSA_PLAIN_SHA256_OID: String =
            "$ECDSA_PLAIN_SIGNATURES.3" /* 0.4.0.127.0.7.1.1.4.1.3, ecdsa-plain-SHA256 */
        const val ECDSA_PLAIN_SHA384_OID: String =
            "$ECDSA_PLAIN_SIGNATURES.4" /* 0.4.0.127.0.7.1.1.4.1.4, ecdsa-plain-SHA384 */
        const val ECDSA_PLAIN_SHA512_OID: String =
            "$ECDSA_PLAIN_SIGNATURES.5" /* 0.4.0.127.0.7.1.1.4.1.5, ecdsa-plain-SHA512 */
        const val ECDSA_PLAIN_RIPEMD160_OID: String =
            "$ECDSA_PLAIN_SIGNATURES.6" /* 0.4.0.127.0.7.1.1.4.1.6, ecdsa-plain-RIPEMD160 */

        /**
         * Translates an OID string to a Java mnemonic algorithm string.
         * 
         * @param oid the OID string
         * 
         * @return a mnemonic algorithm string
         * 
         * @throws NoSuchAlgorithmException if the OID was not recognized
         */
        @Throws(NoSuchAlgorithmException::class)
        fun lookupMnemonicByOID(oid: String?): String {
            if (ECDSA_PLAIN_SHA1_OID == oid) {
                return "SHA1withECDSA"
            }
            if (ECDSA_PLAIN_SHA224_OID == oid) {
                return "SHA224withECDSA"
            }
            if (ECDSA_PLAIN_SHA256_OID == oid) {
                return "SHA256withECDSA"
            }
            if (ECDSA_PLAIN_SHA384_OID == oid) {
                return "SHA384withECDSA"
            }
            if (ECDSA_PLAIN_SHA512_OID == oid) {
                return "SHA512withECDSA"
            }
            if (ECDSA_PLAIN_RIPEMD160_OID == oid) {
                return "RIPEMD160withECDSA"
            }

            throw NoSuchAlgorithmException("Unknown OID $oid")
        }

        /* ONLY NON-PUBLIC METHODS BELOW */
        /**
         * Checks whether the given object identifier identifies a
         * ActiveAuthenticationInfo structure.
         * 
         * @param id
         * object identifier
         * @return true if the match is positive
         */
        fun checkRequiredIdentifier(id: String?): Boolean {
            return ID_AA == id
        }

        /**
         * Returns a human readable rendering of the given object identifier string.
         * 
         * @param oid the object identifier (dotted notation)
         * 
         * @return a human readable string representing the given object identifier
         */
        fun toSignatureAlgorithmOIDString(oid: String?): String? {
            if (ECDSA_PLAIN_SHA1_OID == oid) {
                return "ecdsa-plain-SHA1"
            }
            if (ECDSA_PLAIN_SHA224_OID == oid) {
                return "ecdsa-plain-SHA224"
            }
            if (ECDSA_PLAIN_SHA256_OID == oid) {
                return "ecdsa-plain-SHA256"
            }
            if (ECDSA_PLAIN_SHA384_OID == oid) {
                return "ecdsa-plain-SHA384"
            }
            if (ECDSA_PLAIN_SHA512_OID == oid) {
                return "ecdsa-plain-SHA512"
            }
            if (ECDSA_PLAIN_RIPEMD160_OID == oid) {
                return "ecdsa-plain-RIPEMD160"
            }

            return oid
        }
    }
}
