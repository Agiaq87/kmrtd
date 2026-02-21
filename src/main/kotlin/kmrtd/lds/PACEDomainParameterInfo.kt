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
 * $Id: PACEDomainParameterInfo.java 1829 2019-11-27 09:26:16Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.lds

import kmrtd.Util
import org.bouncycastle.asn1.*
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.asn1.x9.X962NamedCurves
import org.bouncycastle.asn1.x9.X962Parameters
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec
import java.math.BigInteger
import java.security.spec.AlgorithmParameterSpec
import java.security.spec.ECFieldFp
import java.security.spec.ECParameterSpec
import java.security.spec.EllipticCurve
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.text.StringBuilder

/**
 * PACE Domain Parameter Info object as per SAC TR 1.01, November 11, 2010.
 * 
 * The object identifier dhpublicnumber or ecPublicKey for DH or ECDH, respectively, SHALL be used to reference
 * explicit domain parameters in an AlgorithmIdentifier (cf. Section 9.1):
 * 
 * <pre>
 * dhpublicnumber OBJECT IDENTIFIER ::= {
 * iso(1) member-body(2) us(840) ansi-x942(10046) number-type(2) 1
 * }
</pre> * 
 * <pre>
 * ecPublicKey OBJECT IDENTIFIER ::= {
 * iso(1) member-body(2) us(840) ansi-x962(10045) keyType(2) 1
 * }
</pre> * 
 * 
 * In the case of elliptic curves, domain parameters MUST be described explicitly in the ECParameters structure,
 * contained as parameters in the AlgorithmIdentifier, i.e. named curves and implicit domain parameters MUST NOT
 * be used.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1829 $
 * 
 * @since 0.5.0
 */
class PACEDomainParameterInfo @JvmOverloads constructor(
    protocolOID: String,
    domainParameter: AlgorithmIdentifier,
    parameterId: BigInteger? = null
) : SecurityInfo() {
    override val objectIdentifier: String

    /*
   * FIXME: This field is now transient, but should not be.
   *
   * We should serialize the contents of concrete instantiations explicitly.
   * Possibly by first defining PACEECDomainParameters and PACEDHDomainParameters subclasses
   * first (yet, ECParameterSpec and DHParameterSpec are also not Serializable).
   */
    @Transient
    private val domainParameter: AlgorithmIdentifier

    /**
     * Returns the parameter id, or `null` if this is the only domain parameter info.
     * 
     * @return the parameter id or `null`
     */
    val parameterId: BigInteger?

    /**
     * Constructs a PACE domain parameter info structure.
     * 
     * @param protocolOID must be [SecurityInfo.ID_PACE_DH_GM], [SecurityInfo.ID_PACE_ECDH_GM],
     * [SecurityInfo.ID_PACE_DH_IM], [SecurityInfo.ID_PACE_ECDH_IM]
     * @param domainParameter parameters in the form of algorithm identifier with algorithm
     * 1.2.840.10046.2.1 (DH public number) or 1.2.840.10045.2.1 (EC public key)
     * @param parameterId an identifier to identify this info
     */
    /**
     * Constructs a PACE domain parameter info structure.
     * 
     * @param protocolOID must be [SecurityInfo.ID_PACE_DH_GM], [SecurityInfo.ID_PACE_ECDH_GM],
     * [SecurityInfo.ID_PACE_DH_IM], [SecurityInfo.ID_PACE_ECDH_IM]
     * @param domainParameter parameters in the form of algorithm identifier with algorithm
     * 1.2.840.10046.2.1 (DH public number) or 1.2.840.10045.2.1 (EC public key)
     */
    init {
        require(checkRequiredIdentifier(protocolOID)) { "Invalid protocol id: " + protocolOID }

        this.objectIdentifier = protocolOID
        this.domainParameter = domainParameter
        this.parameterId = parameterId
    }

    override val protocolOIDString: String?
        /**
         * Returns the protocol object identifier as a human readable string.
         * 
         * @return a string
         */
        get() = toProtocolOIDString(this.objectIdentifier)

    val parameters: AlgorithmParameterSpec
        /**
         * Returns the parameters in the form of algorithm identifier
         * with algorithm 1.2.840.10046.2.1 (DH public number)
         * or 1.2.840.10045.2.1 (EC public key).
         * 
         * @return the parameters
         */
        get() {
            check(ID_DH_PUBLIC_NUMBER != this.objectIdentifier) { "DH PACEDomainParameterInfo not yet implemented" }
            if (ID_EC_PUBLIC_KEY == this.objectIdentifier) {
                return toECParameterSpec(domainParameter)
            } else {
                throw IllegalStateException("Unsupported PACEDomainParameterInfo type " + this.objectIdentifier)
            }
        }

    @get:Deprecated("Remove this method from visible interface (because of dependency on BC API)")
    override val dERObject: ASN1Primitive
        /**
         * Returns a DER object with this `SecurityInfo` data (DER sequence).
         * 
         * @return a DER object with this `SecurityInfo` data
         * 
         */
        get() {
            val vector = ASN1EncodableVector()

            /* Protocol */
            vector.add(ASN1ObjectIdentifier(this.objectIdentifier))

            /* Required data */
            vector.add(domainParameter)

            /* Optional data */
            if (parameterId != null) {
                vector.add(ASN1Integer(parameterId))
            }
            return DLSequence(vector)
        }

    override fun toString(): String {
        return StringBuilder()
            .append("PACEDomainParameterInfo")
            .append("[")
            .append("protocol: ").append(toProtocolOIDString(this.objectIdentifier))
            .append(", ")
            .append("domainParameter: [")
            .append("algorithm: ").append(domainParameter.getAlgorithm().getId()) // e.g. ID_EC_PUBLIC_KEY
            .append(", ")
            .append("parameters: ").append(domainParameter.getParameters()) // e.g. ASN1 sequence of length 6
            .append(if (parameterId == null) "" else ", parameterId: $parameterId")
            .append("]")
            .toString()
    }

    override fun hashCode(): Int {
        return (111111111
                + 7 * objectIdentifier.hashCode() + 5 * domainParameter.hashCode() + 3 * (if (parameterId == null) 333 else parameterId.hashCode()))
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        if (other === this) {
            return true
        }
        if (PACEDomainParameterInfo::class.java != other.javaClass) {
            return false
        }

        val otherPACEDomainParameterInfo = other as PACEDomainParameterInfo
        return this.dERObject.equals(otherPACEDomainParameterInfo.dERObject)
    }

    companion object {
        private val LOGGER: Logger = Logger.getLogger("org.jmrtd")

        private val serialVersionUID = -5851251908152594728L

        /**
         * Value for parameter algorithm OID (part of parameters AlgorithmIdentifier).
         */
        const val ID_PRIME_FIELD: String = "1.2.840.10045.1.1"

        /**
         * Value for parameter algorithm OID (part of parameters AlgorithmIdentifier).
         * `ecPublicKey OBJECT IDENTIFIER ::= { iso(1) member-body(2) us(840) ansi-x962(10045) keyType(2) 1 }`.
         */
        const val ID_EC_PUBLIC_KEY: String = "1.2.840.10045.2.1"

        /**
         * Value for parameter algorithm OID (part of parameters AlgorithmIdentifier).
         * `dhpublicnumber OBJECT IDENTIFIER ::= { iso(1) member-body(2) us(840) ansi-x942(10046) number-type(2) 1 }`.
         */
        const val ID_DH_PUBLIC_NUMBER: String = "1.2.840.10046.2.1"

        /**
         * Checks whether the object identifier is an allowed PACE related object identifier.
         * 
         * @param oid a string representing an object identifier
         * 
         * @return a boolean indicating whether the object identifier is allowed
         */
        fun checkRequiredIdentifier(oid: String?): Boolean {
            return ID_PACE_DH_GM == oid
                    || ID_PACE_ECDH_GM == oid
                    || ID_PACE_DH_IM == oid
                    || ID_PACE_ECDH_IM == oid
                    || ID_PACE_ECDH_CAM == oid
        }

        /* TODO: toAlgorithmIdentifier for DH case. */
        /**
         * Returns a BC algorithm identifier object from an EC parameter spec.
         * 
         * @param ecParameterSpec the EC parameter spec
         * 
         * @return the BC algorithm identifier object
         * 
         */
        @Deprecated("Visibility will be restricted")
        fun toAlgorithmIdentifier(ecParameterSpec: ECParameterSpec): AlgorithmIdentifier {
            val paramSequenceList: MutableList<ASN1Encodable?> = ArrayList<ASN1Encodable?>()

            val versionObject = ASN1Integer(BigInteger.ONE)
            paramSequenceList.add(versionObject)

            val fieldIdOID = ASN1ObjectIdentifier(ID_PRIME_FIELD)
            val curve = ecParameterSpec.getCurve()
            val field = curve.getField() as ECFieldFp
            val p = ASN1Integer(field.getP())
            val fieldIdObject: ASN1Sequence = DLSequence(arrayOf<ASN1Encodable>(fieldIdOID, p))
            paramSequenceList.add(fieldIdObject)

            val aObject: ASN1OctetString = DEROctetString(Util.i2os(curve.a))
            val bObject: ASN1OctetString = DEROctetString(Util.i2os(curve.b))
            val curveObject: ASN1Sequence = DLSequence(arrayOf<ASN1Encodable>(aObject, bObject))
            paramSequenceList.add(curveObject)

            val basePointObject: ASN1OctetString = DEROctetString(
                Util.ecPoint2OS(
                    ecParameterSpec.generator,
                    ecParameterSpec.curve.field.fieldSize
                )
            )
            paramSequenceList.add(basePointObject)

            val orderObject = ASN1Integer(ecParameterSpec.order)
            paramSequenceList.add(orderObject)

            val coFactorObject = ASN1Integer(ecParameterSpec.cofactor.toLong())
            paramSequenceList.add(coFactorObject)

            val paramSequenceArray = arrayOfNulls<ASN1Encodable>(paramSequenceList.size)
            paramSequenceList.toArray<ASN1Encodable?>(paramSequenceArray)
            val paramSequence: ASN1Sequence = DLSequence(paramSequenceArray)
            return AlgorithmIdentifier(ASN1ObjectIdentifier(ID_EC_PUBLIC_KEY), paramSequence)
        }

        /* TODO: toDHParameterSpec for DH case. */
        /**
         * Returns the EC parameter spec form the BC algorithm identifier object.
         * 
         * @param domainParameter the BC algorithm identifier object
         * 
         * @return an EC parameter spec
         * 
         */
        @Deprecated("Visibility will be restricted")
        fun toECParameterSpec(domainParameter: AlgorithmIdentifier): ECParameterSpec {
            val parameters = domainParameter.parameters

            require(parameters is ASN1Sequence) { "Was expecting an ASN.1 sequence" }

            /* We support named EC curves, even though they are actually not allowed here. */
            try {
                val x962params = X962Parameters.getInstance(parameters)
                if (x962params.isNamedCurve) {
                    val x96ParamsOID = x962params.parameters as ASN1ObjectIdentifier?
                    val x9ECParams = X962NamedCurves.getByOID(x96ParamsOID)
                    val bcECNamedCurveParams =
                        ECNamedCurveParameterSpec(
                            X962NamedCurves.getName(x96ParamsOID), x9ECParams.curve, x9ECParams.g,
                            x9ECParams.n, x9ECParams.h, x9ECParams.seed
                        )
                    return Util.toECNamedCurveSpec(bcECNamedCurveParams)
                }
            } catch (e: Exception) {
                LOGGER.log(Level.WARNING, "Exception", e)
            }

            /* Explicit EC parameters. */

            /*
     * ECParameters ::= SEQUENCE {
     *     version INTEGER { ecpVer1(1) } (ecpVer1),
     *     fieldID FieldID {{FieldTypes}},
     *     curve Curve,
     *     base ECPoint,
     *     order INTEGER,
     *     cofactor INTEGER OPTIONAL,
     *     ...
     * }
     */
            val paramSequence = parameters

            require(paramSequence.size() >= 5) { "Was expecting an ASN.1 sequence of length 5 or longer" }

            try {
                val versionObject = paramSequence.getObjectAt(0) as ASN1Integer
                /* BigInteger version = */
                (versionObject).getValue()

                //        assert BigInteger.ONE.equals(version);
                val fieldIdObject = paramSequence.getObjectAt(1) as ASN1Sequence
                //        assert 2 == fieldIdObject.size();
                /* String fieldIdOID = */
                (fieldIdObject.getObjectAt(0) as ASN1ObjectIdentifier).getId()
                //        assert ID_PRIME_FIELD.equals(fieldIdOID);
                val p = (fieldIdObject.getObjectAt(1) as ASN1Integer).positiveValue

                val curveObject = paramSequence.getObjectAt(2) as ASN1Sequence
                //        assert 2 == curveObject.size();
                val aObject = curveObject.getObjectAt(0) as ASN1OctetString
                val bObject = curveObject.getObjectAt(1) as ASN1OctetString
                val a = Util.os2i(aObject.octets)
                val b = Util.os2i(bObject.octets)

                val basePointObject = paramSequence.getObjectAt(3) as ASN1OctetString
                val g = Util.os2ECPoint(basePointObject.octets)
                val x = g.affineX
                val y = g.affineY
                // assert G is on the curve
                val lhs = y.pow(2).mod(p)
                val xPow3 = x.pow(3)
                val rhs = xPow3.add(a.multiply(x)).add(b).mod(p)

                val curve = EllipticCurve(ECFieldFp(p), a, b)

                val orderObject = paramSequence.getObjectAt(4) as ASN1Integer
                val n = orderObject.positiveValue

                if (paramSequence.size() <= 5) {
                    return ECParameterSpec(curve, g, n, 1)
                } else {
                    val coFactorObject = paramSequence.getObjectAt(5) as ASN1Integer
                    val coFactor = coFactorObject.value
                    return ECParameterSpec(curve, g, n, coFactor.toInt())
                }
            } catch (e: Exception) {
                LOGGER.log(Level.WARNING, "Exception", e)
                throw IllegalArgumentException("Could not get EC parameters from explicit parameters")
            }
        }

        /* ONLY PRIVATE METHODS BELOW */
        /**
         * Returns an BC algorithm identifier for the given protocol object identifier.
         * 
         * @param protocolOID the protocol object identifier
         * @param parameters the parameters as a BC ASN1 encodable
         * 
         * @return an algorithm identifier
         */
        private fun toAlgorithmIdentifier(protocolOID: String?, parameters: ASN1Encodable?): AlgorithmIdentifier {
            if (ID_PACE_DH_GM == protocolOID
                || ID_PACE_DH_IM == protocolOID
            ) {
                return AlgorithmIdentifier(ASN1ObjectIdentifier(ID_DH_PUBLIC_NUMBER), parameters)
            } else if (ID_PACE_ECDH_GM == protocolOID
                || ID_PACE_ECDH_IM == protocolOID
                || ID_PACE_ECDH_CAM == protocolOID
            ) {
                return AlgorithmIdentifier(ASN1ObjectIdentifier(ID_EC_PUBLIC_KEY), parameters)
            }
            throw IllegalArgumentException("Cannot infer algorithm OID from protocol OID: $protocolOID")
        }

        /**
         * Returns an ASN1 name for the give object identifier.
         * 
         * @param oid an object identifier
         * 
         * @return an ASN1 name
         */
        private fun toProtocolOIDString(oid: String?): String? {
            if (ID_PACE_DH_GM == oid) {
                return "id-PACE-DH-GM"
            }
            if (ID_PACE_ECDH_GM == oid) {
                return "id-PACE-ECDH-GM"
            }
            if (ID_PACE_DH_IM == oid) {
                return "id-PACE-DH-IM"
            }
            if (ID_PACE_ECDH_IM == oid) {
                return "id-PACE-ECDH-IM"
            }
            if (ID_PACE_ECDH_CAM == oid) {
                return "id-PACE-ECDH-CAM"
            }

            return oid
        }
    }
}
