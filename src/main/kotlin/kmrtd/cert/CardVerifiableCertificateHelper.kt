package kmrtd.cert
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
import org.ejbca.cvc.CAReferenceField
import org.ejbca.cvc.CVCertificate
import org.ejbca.cvc.CVCertificateBody
import org.ejbca.cvc.HolderReferenceField
import org.ejbca.cvc.exception.ConstructionException
import java.security.PublicKey
import java.util.*

/*
    * TODO: perhaps move this to factory class (CertificateFactory, CertificateBuilder, whatever).
    * NOTE: algorithm should be one of"SHA224withECDSA", "SHA256withECDSA", "SHA384withECDSA", "SHA512withECDSA",
    * or similar with RSA.
    */
    /**
     * Constructs a certificate.
     *
     * @param authorityReference authority reference
     * @param holderReference holder reference
     * @param publicKey public key
     * @param algorithm algorithm
     * @param notBefore valid from date
     * @param notAfter valid to date
     * @param role role
     * @param permission permission
     * @param signatureData signed date
     */
    @Throws(IllegalArgumentException::class)
    fun CardVerifiableCertificate.Companion.from(
        authorityReference: CVCPrincipal,
        holderReference: CVCPrincipal,
        publicKey: PublicKey,
        algorithm: String,
        notBefore: Date,
        notAfter: Date,
        role: CVCAuthorizationTemplate.Role,
        permission: CVCAuthorizationTemplate.Permission,
        signatureData: ByteArray
    ): CardVerifiableCertificate {
        try {
            val authorityRef = CAReferenceField(
                authorityReference.getCountry().toAlpha2Code(),
                authorityReference.mnemonic,
                authorityReference.seqNumber
            )
            val holderRef = HolderReferenceField(
                holderReference.getCountry().toAlpha2Code(),
                holderReference.mnemonic,
                holderReference.seqNumber
            )
            val authRole = CVCAuthorizationTemplate.fromRole(role)
            val accessRight = CVCAuthorizationTemplate.fromPermission(permission)
            val body = CVCertificateBody(
                authorityRef,
                org.ejbca.cvc.KeyFactory.createInstance(publicKey, algorithm, authRole),
                holderRef,
                authRole,
                accessRight,
                notBefore,
                notAfter
            )
            val cvCertificate = CVCertificate(body)
            cvCertificate.signature = signatureData
            cvCertificate.tbs
            return CardVerifiableCertificate(cvCertificate)
        } catch (ce: ConstructionException) {
            throw IllegalArgumentException(ce)
        }
    }