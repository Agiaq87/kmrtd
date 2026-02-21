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
 * $Id: CVCertificateBuilder.java 1767 2018-02-20 12:54:49Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.cert

import org.ejbca.cvc.*
import org.ejbca.cvc.exception.ConstructionException
import java.io.IOException
import java.security.*
import java.util.*

/**
 * Card verifiable certificate builder.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1767 $
 */
object CVCertificateBuilder {
    /**
     * Produces card verifiable certificates.
     * 
     * @param publicKey the public key
     * @param signerKey private key
     * @param algorithmName algorithm name
     * @param caRef CA principal
     * @param holderRef holder principal
     * @param authZTemplate authorization template
     * @param validFrom valid from date
     * @param validTo valid to date
     * @param provider provider name
     * 
     * @return a card verifiable certificate
     * 
     * @throws IOException on error
     * @throws NoSuchAlgorithmException on unknown algorithm
     * @throws NoSuchProviderException  on unknown provider
     * @throws InvalidKeyException on invalid key
     * @throws SignatureException on error creating signature
     * @throws ConstructionException on error constructing the certificate
     */
    @Throws(
        IOException::class,
        NoSuchAlgorithmException::class,
        NoSuchProviderException::class,
        InvalidKeyException::class,
        SignatureException::class,
        ConstructionException::class
    )
    fun createCertificate(
        publicKey: PublicKey,
        signerKey: PrivateKey?, algorithmName: String, caRef: CVCPrincipal,
        holderRef: CVCPrincipal, authZTemplate: CVCAuthorizationTemplate, validFrom: Date, validTo: Date,
        provider: String
    ): CardVerifiableCertificate {
        return CardVerifiableCertificate(
            CertificateGenerator
                .createCertificate(
                    publicKey,
                    signerKey,
                    algorithmName,
                    CAReferenceField(
                        caRef.getCountry().toAlpha2Code(),
                        caRef.mnemonic, caRef.seqNumber
                    ),
                    HolderReferenceField(
                        holderRef.getCountry()
                            .toAlpha2Code(), holderRef.mnemonic,
                        holderRef.seqNumber
                    ),
                    CVCertificateBuilder.getRole(authZTemplate.role),
                    CVCertificateBuilder.getAccessRight(authZTemplate.accessRight),
                    validFrom,
                    validTo,
                    provider
                )
        )
    }

    /**
     * Translates the role to an EJBCA type.
     * 
     * @param role a role
     * 
     * @return the role as an EJBCA typed object
     */
    private fun getRole(role: CVCAuthorizationTemplate.Role): AuthorizationRoleEnum =
        when (role) {
            CVCAuthorizationTemplate.Role.CVCA -> return AuthorizationRoleEnum.CVCA
            CVCAuthorizationTemplate.Role.DV_D -> return AuthorizationRoleEnum.DV_D
            CVCAuthorizationTemplate.Role.DV_F -> return AuthorizationRoleEnum.DV_F
            CVCAuthorizationTemplate.Role.IS -> return AuthorizationRoleEnum.IS
        }

    /**
     * Translates an access right to an EJBCA type.
     * 
     * @param accessRight the access right
     * 
     * @return the access right as an EJBCA typed object
     */
    private fun getAccessRight(accessRight: CVCAuthorizationTemplate.Permission): AccessRightEnum =
        when (accessRight) {
            CVCAuthorizationTemplate.Permission.READ_ACCESS_NONE -> AccessRightEnum.READ_ACCESS_NONE
            CVCAuthorizationTemplate.Permission.READ_ACCESS_DG3 -> AccessRightEnum.READ_ACCESS_DG3
            CVCAuthorizationTemplate.Permission.READ_ACCESS_DG4 -> AccessRightEnum.READ_ACCESS_DG4
            CVCAuthorizationTemplate.Permission.READ_ACCESS_DG3_AND_DG4 -> AccessRightEnum.READ_ACCESS_DG3_AND_DG4
        }
}
