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
 * $Id: SecurityInfo.java 1894 2025-03-19 20:00:46Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.lds

import kmrtd.lds.TerminalAuthenticationInfo.Companion.checkRequiredIdentifier
import org.bouncycastle.asn1.*
import org.bouncycastle.asn1.eac.EACObjectIdentifiers
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers
import kmrtd.ASN1Util
import kmrtd.Util
import java.io.IOException
import java.io.OutputStream
import java.util.logging.Level
import java.util.logging.Logger

/* FIXME: dependency on BC in interface? */ /**
 * Abstract base class for security info structure.
 * See the BSI EAC 1.11 specification.
 * See the ICAO TR - SAC v1.1 specification.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1894 $
 */
abstract class SecurityInfo : AbstractLDSInfo() {
    @JvmField
    @get:Deprecated("this method will be removed from visible interface (because of dependency on BC API)")
    abstract val dERObject: ASN1Primitive

    /**
     * Writes this SecurityInfo to output stream.
     * 
     * @param outputStream an ouput stream
     * 
     * @throws IOException if writing fails
     */
    @Throws(IOException::class)
    override fun writeObject(outputStream: OutputStream) {
        val derEncoded: ASN1Primitive = this.dERObject
        if (derEncoded == null) {
            throw IOException("Could not decode from DER.")
        }

        val derEncodedBytes = derEncoded.getEncoded(ASN1Encoding.DER)
        if (derEncodedBytes == null) {
            throw IOException("Could not decode from DER.")
        }

        outputStream.write(derEncodedBytes)
    }

    /**
     * Returns the protocol object identifier of this SecurityInfo.
     * 
     * @return this protocol object identifier
     */
    abstract val objectIdentifier: String?

    /**
     * Returns the protocol object identifier as a human readable string.
     * 
     * @return a human readable string representing the protocol object identifier
     */
    abstract val protocolOIDString: String?

    companion object {
        private const val serialVersionUID = -7919854443619069808L

        private val LOGGER: Logger = Logger.getLogger("kmrtd")

        /**
         * Used in ECDSA based Active Authentication.
         * `{joint-iso-itu-t(2) international-organizations(23) 136 mrtd(1) security(1) aaProtocolObject(5)}`.
         */
        const val ID_AA: String = "2.23.136.1.1.5"

        @JvmField
        val ID_PK_DH: String = EACObjectIdentifiers.id_PK_DH.getId()
        @JvmField
        val ID_PK_ECDH: String = EACObjectIdentifiers.id_PK_ECDH.getId()

        /** Used in Chip Authentication 1 and 2.  */
        @JvmField
        val ID_CA_DH_3DES_CBC_CBC: String? = EACObjectIdentifiers.id_CA_DH_3DES_CBC_CBC.getId()

        /** Used in Chip Authentication 1 and 2.  */
        @JvmField
        val ID_CA_ECDH_3DES_CBC_CBC: String? = EACObjectIdentifiers.id_CA_ECDH_3DES_CBC_CBC.getId()

        /** Used in Chip Authentication 1 and 2.  */
        const val ID_CA_DH_AES_CBC_CMAC_128: String = "0.4.0.127.0.7.2.2.3.1.2"

        /** Used in Chip Authentication 1 and 2.  */
        const val ID_CA_DH_AES_CBC_CMAC_192: String = "0.4.0.127.0.7.2.2.3.1.3"

        /** Used in Chip Authentication 1 and 2.  */
        const val ID_CA_DH_AES_CBC_CMAC_256: String = "0.4.0.127.0.7.2.2.3.1.4"

        /** Used in Chip Authentication 1 and 2.  */
        const val ID_CA_ECDH_AES_CBC_CMAC_128: String = "0.4.0.127.0.7.2.2.3.2.2"

        /** Used in Chip Authentication 1 and 2.  */
        const val ID_CA_ECDH_AES_CBC_CMAC_192: String = "0.4.0.127.0.7.2.2.3.2.3"

        /** Used in Chip Authentication 1 and 2.  */
        const val ID_CA_ECDH_AES_CBC_CMAC_256: String = "0.4.0.127.0.7.2.2.3.2.4"

        /** Used in Terminal Authentication 1 and 2.  */
        val ID_TA: String? = EACObjectIdentifiers.id_TA.getId()

        /** Used in Terminal Authentication 1 and 2.  */
        val ID_TA_RSA: String? = EACObjectIdentifiers.id_TA_RSA.getId()

        /** Used in Terminal Authentication 1 and 2.  */
        val ID_TA_RSA_V1_5_SHA_1: String? = EACObjectIdentifiers.id_TA_RSA_v1_5_SHA_1.getId()

        /** Used in Terminal Authentication 1 and 2.  */
        val ID_TA_RSA_V1_5_SHA_256: String? = EACObjectIdentifiers.id_TA_RSA_v1_5_SHA_256.getId()

        /** Used in Terminal Authentication 1 and 2.  */
        val ID_TA_RSA_PSS_SHA_1: String? = EACObjectIdentifiers.id_TA_RSA_PSS_SHA_1.getId()

        /** Used in Terminal Authentication 1 and 2.  */
        val ID_TA_RSA_PSS_SHA_256: String? = EACObjectIdentifiers.id_TA_RSA_PSS_SHA_256.getId()

        /** Used in Terminal Authentication 1 and 2.  */
        val ID_TA_ECDSA: String? = EACObjectIdentifiers.id_TA_ECDSA.getId()

        /** Used in Terminal Authentication 1 and 2.  */
        val ID_TA_ECDSA_SHA_1: String? = EACObjectIdentifiers.id_TA_ECDSA_SHA_1.getId()

        /** Used in Terminal Authentication 1 and 2.  */
        val ID_TA_ECDSA_SHA_224: String? = EACObjectIdentifiers.id_TA_ECDSA_SHA_224.getId()

        /** Used in Terminal Authentication 1 and 2.  */
        val ID_TA_ECDSA_SHA_256: String? =
            EACObjectIdentifiers.id_TA_ECDSA_SHA_256.getId() // NOTE: "id-TA-ECDSA-SHA-256" is 0.4.0.127.0.7.2.2.2.2.3

        val ID_EC_PUBLIC_KEY_TYPE: String? = X9ObjectIdentifiers.id_publicKeyType.getId()

        val ID_EC_PUBLIC_KEY: String? = X9ObjectIdentifiers.id_ecPublicKey.getId()

        private const val ID_BSI = "0.4.0.127.0.7"

        /* protocols (2), smartcard (2), PACE (4) */
        val ID_PACE: String = "$ID_BSI.2.2.4"

        @JvmField
        val ID_PACE_DH_GM: String = "$ID_PACE.1"
        @JvmField
        val ID_PACE_DH_GM_3DES_CBC_CBC: String =
            "$ID_PACE_DH_GM.1" /* 0.4.0.127.0.7.2.2.4.1.1, id-PACE-DH-GM-3DES-CBC-CBC */
        @JvmField
        val ID_PACE_DH_GM_AES_CBC_CMAC_128: String =
            "$ID_PACE_DH_GM.2" /* 0.4.0.127.0.7.2.2.4.1.2, id-PACE-DH-GM-AES-CBC-CMAC-128 */
        @JvmField
        val ID_PACE_DH_GM_AES_CBC_CMAC_192: String =
            "$ID_PACE_DH_GM.3" /* 0.4.0.127.0.7.2.2.4.1.3, id-PACE-DH-GM-AES-CBC-CMAC-192 */
        @JvmField
        val ID_PACE_DH_GM_AES_CBC_CMAC_256: String =
            "$ID_PACE_DH_GM.4" /* 0.4.0.127.0.7.2.2.4.1.4, id-PACE-DH-GM-AES-CBC-CMAC-256 */

        @JvmField
        val ID_PACE_ECDH_GM: String = ID_PACE + ".2"
        @JvmField
        val ID_PACE_ECDH_GM_3DES_CBC_CBC: String =
            "$ID_PACE_ECDH_GM.1" /* 0.4.0.127.0.7.2.2.4.2.1, id-PACE-ECDH-GM-3DES-CBC-CBC */
        @JvmField
        val ID_PACE_ECDH_GM_AES_CBC_CMAC_128: String =
            "$ID_PACE_ECDH_GM.2" /* 0.4.0.127.0.7.2.2.4.2.2, id-PACE-ECDH-GM-AES-CBC-CMAC-128 */
        @JvmField
        val ID_PACE_ECDH_GM_AES_CBC_CMAC_192: String =
            "$ID_PACE_ECDH_GM.3" /* 0.4.0.127.0.7.2.2.4.2.3, id-PACE-ECDH-GM-AES-CBC-CMAC-192 */
        @JvmField
        val ID_PACE_ECDH_GM_AES_CBC_CMAC_256: String =
            "$ID_PACE_ECDH_GM.4" /* 0.4.0.127.0.7.2.2.4.2.4, id-PACE-ECDH-GM-AES-CBC-CMAC-256 */

        @JvmField
        val ID_PACE_DH_IM: String = "$ID_PACE.3"
        @JvmField
        val ID_PACE_DH_IM_3DES_CBC_CBC: String =
            "$ID_PACE_DH_IM.1" /* 0.4.0.127.0.7.2.2.4.3.1, id-PACE-DH-IM-3DES-CBC-CBC */
        @JvmField
        val ID_PACE_DH_IM_AES_CBC_CMAC_128: String =
            "$ID_PACE_DH_IM.2" /* 0.4.0.127.0.7.2.2.4.3.2, id-PACE-DH-IM-AES-CBC-CMAC-128 */
        @JvmField
        val ID_PACE_DH_IM_AES_CBC_CMAC_192: String =
            "$ID_PACE_DH_IM.3" /* 0.4.0.127.0.7.2.2.4.3.3, id-PACE-DH-IM-AES-CBC-CMAC-192 */
        @JvmField
        val ID_PACE_DH_IM_AES_CBC_CMAC_256: String =
            "$ID_PACE_DH_IM.4" /* 0.4.0.127.0.7.2.2.4.3.4, id-PACE-DH-IM-AES-CBC-CMAC-256 */

        @JvmField
        val ID_PACE_ECDH_IM: String = "$ID_PACE.4"
        @JvmField
        val ID_PACE_ECDH_IM_3DES_CBC_CBC: String =
            "$ID_PACE_ECDH_IM.1" /* 0.4.0.127.0.7.2.2.4.4.1, id-PACE-ECDH-IM-3DES-CBC-CBC */
        @JvmField
        val ID_PACE_ECDH_IM_AES_CBC_CMAC_128: String =
            "$ID_PACE_ECDH_IM.2" /* 0.4.0.127.0.7.2.2.4.4.2, id-PACE-ECDH-IM-AES-CBC-CMAC-128 */
        @JvmField
        val ID_PACE_ECDH_IM_AES_CBC_CMAC_192: String =
            "$ID_PACE_ECDH_IM.3" /* 0.4.0.127.0.7.2.2.4.4.3, id-PACE-ECDH-IM-AES-CBC-CMAC-192 */
        @JvmField
        val ID_PACE_ECDH_IM_AES_CBC_CMAC_256: String =
            "$ID_PACE_ECDH_IM.4" /* 0.4.0.127.0.7.2.2.4.4.4, id-PACE-ECDH-IM-AES-CBC-CMAC-256 */

        @JvmField
        val ID_PACE_ECDH_CAM: String = "$ID_PACE.6"
        @JvmField
        val ID_PACE_ECDH_CAM_AES_CBC_CMAC_128: String =
            "$ID_PACE_ECDH_CAM.2" /* 0.4.0.127.0.7.2.2.4.6.2, id-PACE-ECDH-CAM-AES-CBC-CMAC-128 */
        @JvmField
        val ID_PACE_ECDH_CAM_AES_CBC_CMAC_192: String =
            "$ID_PACE_ECDH_CAM.3" /* 0.4.0.127.0.7.2.2.4.6.3, id-PACE-ECDH-CAM-AES-CBC-CMAC-192 */
        @JvmField
        val ID_PACE_ECDH_CAM_AES_CBC_CMAC_256: String =
            "$ID_PACE_ECDH_CAM.4" /* 0.4.0.127.0.7.2.2.4.6.4, id-PACE-ECDH-CAM-AES-CBC-CMAC-256 */

        /**
         * Factory method for creating security info objects given an input.
         * 
         * @param obj the input
         * 
         * @return a concrete security info object
         */
        @JvmStatic
        fun getInstance(obj: ASN1Encodable?): SecurityInfo? {
            try {
                val sequence = ASN1Util.list(obj)
                val oid = ASN1ObjectIdentifier.getInstance(sequence.get(0)).getId()
                val requiredData = sequence.get(1)
                val optionalData = if (sequence.size <= 2) null else sequence.get(2)

                if (ActiveAuthenticationInfo.checkRequiredIdentifier(oid)) {
                    val version = ASN1Integer.getInstance(requiredData).getValue().toInt()
                    if (optionalData == null) {
                        return ActiveAuthenticationInfo(oid, version, null)
                    } else {
                        val signatureAlgorithmOID = ASN1ObjectIdentifier.getInstance(optionalData).getId()
                        return ActiveAuthenticationInfo(oid, version, signatureAlgorithmOID)
                    }
                } else if (ChipAuthenticationPublicKeyInfo.checkRequiredIdentifier(oid)) {
                    val subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(requiredData)
                    if (optionalData == null) {
                        return ChipAuthenticationPublicKeyInfo(oid, Util.toPublicKey(subjectPublicKeyInfo))
                    } else {
                        val optionalDataAsASN1Integer = ASN1Integer.getInstance(optionalData)
                        val keyId = optionalDataAsASN1Integer.getValue()
                        return ChipAuthenticationPublicKeyInfo(oid, Util.toPublicKey(subjectPublicKeyInfo), keyId)
                    }
                } else if (ChipAuthenticationInfo.checkRequiredIdentifier(oid)) {
                    val version = ASN1Integer.getInstance(requiredData).getValue().toInt()
                    if (optionalData == null) {
                        return ChipAuthenticationInfo(oid, version)
                    } else {
                        val optionalDataAsASN1Integer = ASN1Integer.getInstance(optionalData)
                        val keyId = optionalDataAsASN1Integer.getValue()
                        return ChipAuthenticationInfo(oid, version, keyId)
                    }
                } else if (checkRequiredIdentifier(oid)) {
                    val version = ASN1Integer.getInstance(requiredData).getValue().toInt()
                    if (optionalData == null) {
                        return TerminalAuthenticationInfo(oid, version)
                    } else {
                        val efCVCA = ASN1Sequence.getInstance(optionalData)
                        return TerminalAuthenticationInfo(oid, version, efCVCA)
                    }
                } else if (PACEInfo.checkRequiredIdentifier(oid)) {
                    val version = ASN1Integer.getInstance(requiredData).getValue().toInt()
                    var parameterId = -1
                    if (optionalData != null) {
                        parameterId = ASN1Integer.getInstance(optionalData).getValue().toInt()
                    }
                    return PACEInfo(oid, version, parameterId)
                } else if (PACEDomainParameterInfo.checkRequiredIdentifier(oid)) {
                    val domainParameters = AlgorithmIdentifier.getInstance(requiredData)
                    if (optionalData != null) {
                        val parameterId = ASN1Integer.getInstance(optionalData).getValue()
                        return PACEDomainParameterInfo(oid, domainParameters, parameterId)
                    }
                    return PACEDomainParameterInfo(oid, domainParameters)
                }
                LOGGER.warning("Unsupported SecurityInfo, oid = $oid")
                return null
            } catch (e: Exception) {
                LOGGER.log(Level.WARNING, "Unexpected exception", e)
                throw IllegalArgumentException("Malformed input stream.")
            }
        }
    }
}
