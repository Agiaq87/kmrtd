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
 * $Id: TerminalAuthenticationInfo.java 1850 2021-05-21 06:25:03Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.lds

import org.bouncycastle.asn1.*
import java.util.logging.Logger

/**
 * A concrete SecurityInfo structure that stores terminal authentication
 * info, see EAC 1.11 specification.
 * 
 * This data structure provides detailed information on an implementation of Terminal Authentication.
 * 
 *  * The object identifier `protocol` SHALL identify the Terminal
 * Authentication Protocol as the specific protocol may change over time.
 *  * The integer `version` SHALL identify the version of the protocol.
 * Currently, versions 1 and 2 are supported.
 *  * The sequence `efCVCA` MAY be used to indicate a (short) file
 * identifier of the file EF.CVCA. It MUST be used, if the default (short) file
 * identifier is not used.
 * 
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1850 $
 */
class TerminalAuthenticationInfo internal constructor(
    private val oid: String?,
    /**
     * Returns the version. This will be 1 or 2.
     * 
     * @return the version
     */
    val version: Int, /*
   * FIXME: This shouldn't be transient, as we want this part of the state to be (de)serialized.
   *
   * This contains just a file identifier, and possibly a short file identifier?
   * Why not short instead of ASN1Sequence? (In which case we can remove the transient.)
   *
   * Alternatively we could explicitly (de)serialize this in readObject/writeObject
   * (using BC's getEncoded()).
   */@field:Transient private val efCVCA: ASN1Sequence?
) : SecurityInfo() {
    /**
     * Constructs a new object.
     * 
     * @param oid the id_TA identifier
     * @param version has to be 1 or 2
     * @param efCVCA the file ID information of the efCVCA file
     */
    init {
        checkFields()
    }

    /**
     * Constructs a new object.
     * 
     * @param identifier the id_TA identifier
     * @param version has to be 1 or 2
     */
    internal constructor(identifier: String?, version: Int) : this(identifier, version, null)

    /**
     * Constructs a terminal authentication info using id_TA identifier [.ID_TA]
     * and version {@value #VERSION_1}.
     */
    constructor() : this(ID_TA, VERSION_1)

    /**
     * Constructs a new Terminal Authentication info with the required
     * object identifier and version number 1, and file identifier and
     * short file identifier (possibly -1).
     * 
     * @param fileId a file identifier reference to the efCVCA file
     * @param shortFileId short file id for the above file, -1 if none
     */
    constructor(fileId: Short, shortFileId: Byte) : this(ID_TA, VERSION_1, constructEFCVCA(fileId, shortFileId))

    /**
     * Returns a DER object with this SecurityInfo data (DER sequence).
     * 
     * @return a DER object with this SecurityInfo data
     * 
     */
    @Deprecated("this method will be removed from visible interface (because of dependency on BC API)")
    override fun getDERObject(): ASN1Primitive {
        val v = ASN1EncodableVector()
        v.add(ASN1ObjectIdentifier(oid))
        v.add(ASN1Integer(version.toLong()))
        if (efCVCA != null) {
            v.add(efCVCA)
        }
        return DLSequence(v)
    }

    /**
     * Returns the object identifier of this Terminal Authentication info.
     * 
     * @return an object identifier
     */
    override fun getObjectIdentifier(): String? {
        return oid
    }

    /**
     * Returns the protocol object identifier as a human readable string.
     * 
     * @return a string
     */
    override fun getProtocolOIDString(): String? {
        return toProtocolOIDString(oid)
    }

    val fileId: Int
        /**
         * Returns the efCVCA file identifier stored in this file, -1 if none.
         * 
         * @return the efCVCA file identifier stored in this file
         */
        get() = getFID(efCVCA).toInt()

    val shortFileId: Byte
        /**
         * Returns the efCVCA short file identifier stored in this file, -1 if none
         * or not present.
         * 
         * @return the efCVCA short file identifier stored in this file
         */
        get() = getSFI(efCVCA)

    override fun toString(): String {
        return StringBuilder()
            .append("TerminalAuthenticationInfo [")
            .append("protocol: ").append(toProtocolOIDString(oid)).append(", ")
            .append("version: ").append(version).append(", ")
            .append("fileID: ").append(this.fileId).append(", ")
            .append("shortFileID: ").append(this.shortFileId.toInt())
            .append("]")
            .toString()
    }

    override fun hashCode(): Int {
        return (123
                + 7 * (if (oid == null) 0 else oid.hashCode()) + 5 * version + 3 * (if (efCVCA == null) 1 else efCVCA.hashCode()))
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        if (other === this) {
            return true
        }
        if (TerminalAuthenticationInfo::class.java != other.javaClass) {
            return false
        }
        val otherTerminalAuthenticationInfo = other as TerminalAuthenticationInfo
        if (efCVCA == null && otherTerminalAuthenticationInfo.efCVCA != null) {
            return false
        }
        if (efCVCA != null && otherTerminalAuthenticationInfo.efCVCA == null) {
            return false
        }

        return getDERObject().equals(otherTerminalAuthenticationInfo.getDERObject())
    }

    /**
     * Checks the correctness of the data for this instance of `SecurityInfo`.
     */
    private fun checkFields() {
        try {
            require(checkRequiredIdentifier(oid)) { "Wrong identifier: " + oid }
            if (version != VERSION_1 && version != VERSION_2) {
                LOGGER.warning(
                    ("Wrong version. Was expecting " + VERSION_1 + " or " + VERSION_2
                            + ", found " + version)
                )
            }
            if (efCVCA != null) {
                val fid = efCVCA.getObjectAt(0) as ASN1OctetString
                require(fid.getOctets().size == 2) { "Malformed FID." }
                if (efCVCA.size() == 2) {
                    val sfi = efCVCA.getObjectAt(1) as ASN1OctetString
                    require(sfi.getOctets().size == 1) { "Malformed SFI." }
                }
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("Malformed TerminalAuthenticationInfo", e)
        }
    }

    /**
     * Returns the ASN1 name for the given protocol object identifier.
     * 
     * @param oid the protocol object identifier
     * 
     * @return the ASN1 name if known, or the original object identifier if not
     */
    private fun toProtocolOIDString(oid: String?): String? {
        if (ID_TA == oid) {
            return "id-TA"
        }
        if (ID_TA_RSA == oid) {
            return "id-TA-RSA"
        }
        if (ID_TA_RSA_V1_5_SHA_1 == oid) {
            return "id-TA-RSA-v1-5-SHA-1"
        }
        if (ID_TA_RSA_V1_5_SHA_256 == oid) {
            return "id-TA-RSA-v1-5-SHA-256"
        }
        if (ID_TA_RSA_PSS_SHA_1 == oid) {
            return "id-TA-RSA-PSS-SHA-1"
        }
        if (ID_TA_RSA_PSS_SHA_256 == oid) {
            return "id-TA-RSA-PSS-SHA-256"
        }
        if (ID_TA_ECDSA == oid) {
            return "id-TA-ECDSA"
        }
        if (ID_TA_ECDSA_SHA_1 == oid) {
            return "id-TA-ECDSA-SHA-1"
        }
        if (ID_TA_ECDSA_SHA_224 == oid) {
            return "id-TA-ECDSA-SHA-224"
        }
        if (ID_TA_ECDSA_SHA_256 == oid) {
            return "id-TA-ECDSA-SHA-256"
        }

        return oid
    }

    companion object {
        private const val serialVersionUID = 6220506985707094044L

        private val LOGGER: Logger = Logger.getLogger("kmrtd.lds")

        const val VERSION_1: Int = 1
        private const val VERSION_2 = 2

        /* ONLY NON-PUBLIC METHODS BELOW */
        /**
         * Checks whether the given object identifier identifies a
         * TerminalAuthenticationInfo structure.
         * 
         * @param id
         * object identifier
         * @return true if the match is positive
         */
        @JvmStatic
        fun checkRequiredIdentifier(id: String?): Boolean {
            return ID_TA == id
        }

        /**
         * Encodes the BC object representing a reference to a CVCA file.
         * 
         * @param fid the file identifier
         * @param sfi the short file identifier
         * 
         * @return an BC ASN1 sequence
         */
        private fun constructEFCVCA(fid: Short, sfi: Byte): ASN1Sequence {
            if (sfi.toInt() != -1) {
                return DLSequence(
                    arrayOf<ASN1Encodable>(
                        DEROctetString(
                            byteArrayOf(
                                ((fid.toInt() and 0xFF00) shr 8).toByte(),
                                (fid.toInt() and 0xFF).toByte()
                            )
                        ),
                        DEROctetString(byteArrayOf((sfi.toInt() and 0xFF).toByte()))
                    )
                )
            } else {
                return DLSequence(
                    arrayOf<ASN1Encodable>(
                        DEROctetString(
                            byteArrayOf(
                                ((fid.toInt() and 0xFF00) shr 8).toByte(),
                                (fid.toInt() and 0xFF).toByte()
                            )
                        )
                    )
                )
            }
        }

        /**
         * Returns the file identifier encoded in an BC ASN1 sequence.
         * 
         * @param efCVCA the BC ASN1 sequence
         * 
         * @return the file identifier
         */
        private fun getFID(efCVCA: ASN1Sequence?): Short {
            if (efCVCA == null) {
                return -1
            }
            val s: ASN1Sequence? = efCVCA
            val fid = s!!.getObjectAt(0) as ASN1OctetString
            val bytes = fid.octets
            return (((bytes[0].toInt() and 0xFF) shl 8) or (bytes[1].toInt() and 0xFF)).toShort()
        }

        /**
         * Returns the short file identifier encoded in an BC ASN1 sequence.
         * 
         * @param efCVCA the BC ASN1 sequence
         * 
         * @return the short file identifier
         */
        private fun getSFI(efCVCA: ASN1Sequence?): Byte {
            if (efCVCA == null) {
                return -1
            }
            if (efCVCA.size() != 2) {
                return -1
            }
            return (efCVCA.getObjectAt(1) as ASN1OctetString).octets[0]
        }
    }
}
