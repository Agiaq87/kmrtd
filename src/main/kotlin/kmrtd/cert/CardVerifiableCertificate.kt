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
 * $Id: CardVerifiableCertificate.java 1808 2019-03-07 21:32:19Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.cert

import net.sf.scuba.data.Country
import org.ejbca.cvc.*
import org.ejbca.cvc.exception.ConstructionException
import java.io.IOException
import java.security.*
import java.security.KeyFactory
import java.security.cert.Certificate
import java.security.cert.CertificateEncodingException
import java.security.cert.CertificateException
import java.security.interfaces.RSAPublicKey
import java.security.spec.RSAPublicKeySpec
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Card verifiable certificates as specified in TR 03110.
 * 
 * Just a wrapper around `org.ejbca.cvc.CVCertificate` by Keijo Kurkinen of EJBCA.org,
 * so that we can subclass `java.security.cert.Certificate`.
 * 
 * We also hide some of the internal structure (no more calls to get the "body" just to get some
 * attributes).
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1808 $
 */
class CardVerifiableCertificate constructor(private val cvCertificate: CVCertificate) : Certificate("CVC") {

    /**
     * Granted from JVM not need to catch exception...
     */
    @Transient
    private var rsaKeyFactory: KeyFactory = KeyFactory.getInstance("RSA")

    val sigAlgName: String?
        /**
         * Returns the signature algorithm.
         * 
         * @return an algorithm name
         */
        get() {
            try {
                val oid = cvCertificate.certificateBody.publicKey.objectIdentifier
                return AlgorithmUtil.getAlgorithmName(oid)
            } catch (nsfe: NoSuchFieldException) {
                LOGGER.log(
                    Level.WARNING,
                    "No such field",
                    nsfe
                )
                return null
            }
        }

    val sigAlgOID: String?
        /**
         * Returns the signature algorithm object identifier.
         * 
         * @return an object identifier
         */
        get() {
            try {
                val oid = cvCertificate.certificateBody.publicKey.objectIdentifier
                return oid.asText
            } catch (nsfe: NoSuchFieldException) {
                LOGGER.log(
                    Level.WARNING,
                    "No such field",
                    nsfe
                )
                return null
            }
        }

    /**
     * Returns the encoded form of this certificate. It is
     * assumed that each certificate type would have only a single
     * form of encoding; for example, X.509 certificates would
     * be encoded as ASN.1 DER.
     * 
     * @return the encoded form of this certificate
     * 
     * @exception CertificateEncodingException if an encoding error occurs.
     */
    @Throws(CertificateEncodingException::class)
    override fun getEncoded(): ByteArray? {
        try {
            return cvCertificate.derEncoded
        } catch (ioe: IOException) {
            throw CertificateEncodingException(ioe)
        }
    }

    /**
     * Returns the public key from this certificate.
     * 
     * @return the public key.
     */
    override fun getPublicKey(): PublicKey? {
        try {
            val publicKey = cvCertificate.certificateBody.publicKey
            if ("RSA" == publicKey.algorithm) { // TODO: something similar for EC / ECDSA?
                val rsaPublicKey = publicKey as RSAPublicKey
                try {
                    return rsaKeyFactory?.generatePublic(
                        RSAPublicKeySpec(
                            rsaPublicKey.modulus,
                            rsaPublicKey.publicExponent
                        )
                    )
                } catch (gse: GeneralSecurityException) {
                    LOGGER.log(Level.WARNING, "Exception", gse)
                    return publicKey
                }
            }

            /* It's ECDSA... */
            return publicKey
        } catch (nsfe: NoSuchFieldException) {
            LOGGER.log(Level.WARNING, "No such field", nsfe)
            return null
        }
    }

    /**
     * Returns a string representation of this certificate.
     * 
     * @return a string representation of this certificate.
     */
    override fun toString(): String {
        return cvCertificate.toString()
    }

    /**
     * Verifies that this certificate was signed using the
     * private key that corresponds to the specified public key.
     * 
     * @param key the PublicKey used to carry out the verification.
     * 
     * @exception NoSuchAlgorithmException on unsupported signature
     * algorithms.
     * @exception InvalidKeyException on incorrect key.
     * @exception NoSuchProviderException if there's no default provider.
     * @exception SignatureException on signature errors.
     * @exception CertificateException on encoding errors.
     */
    @Throws(
        CertificateException::class,
        NoSuchAlgorithmException::class,
        InvalidKeyException::class,
        NoSuchProviderException::class,
        SignatureException::class
    )
    override fun verify(key: PublicKey?) {
        val providers = Security.getProviders()
        var foundProvider = false
        for (provider in providers) {
            try {
                cvCertificate.verify(key, provider.name)
                foundProvider = true
                break
            } catch (nse: NoSuchAlgorithmException) {
                LOGGER.log(Level.FINE, "Trying next provider", nse)
                continue
            }
        }
        if (!foundProvider) {
            throw NoSuchAlgorithmException("Tried all security providers: None was able to provide this signature algorithm.")
        }
    }

    /**
     * Verifies that this certificate was signed using the
     * private key that corresponds to the specified public key.
     * This method uses the signature verification engine
     * supplied by the specified provider.
     * 
     * @param key the PublicKey used to carry out the verification.
     * @param provider the name of the signature provider.
     * 
     * @throws NoSuchAlgorithmException on unsupported signature algorithms.
     * @throws InvalidKeyException on incorrect key.
     * @throws NoSuchProviderException on incorrect provider.
     * @throws SignatureException on signature errors.
     * @throws CertificateException on encoding errors.
     */
    @Throws(
        CertificateException::class,
        NoSuchAlgorithmException::class,
        InvalidKeyException::class,
        NoSuchProviderException::class,
        SignatureException::class
    )
    override fun verify(key: PublicKey?, provider: String?) {
        cvCertificate.verify(key, provider)
    }

    @get:Throws(CertificateException::class, IOException::class)
    val certBodyData: ByteArray?
        /**
         * The DER encoded certificate body.
         * 
         * @return DER encoded certificate body
         * 
         * @throws CertificateException on error
         * @throws IOException on error
         */
        get()  = withCertificateBody { derEncoded }/*{
            try {
                return cvCertificate.certificateBody.derEncoded
            } catch (nsfe: NoSuchFieldException) {
                throw CertificateException("No such field", nsfe)
            }
        }*/

    @get:Throws(CertificateException::class)
    val notBefore: Date?
        /**
         * Returns 'Effective Date'.
         * 
         * @return the effective date
         * 
         * @throws CertificateException on error
         */
        get() = withCertificateBody { validFrom } /*{
            try {
                return cvCertificate.certificateBody.validFrom
            } catch (nsfe: NoSuchFieldException) {
                throw CertificateException("No such field", nsfe)
            }
        }*/

    @get:Throws(CertificateException::class)
    val notAfter: Date?
        /**
         * Returns 'Expiration Date'.
         * 
         * @return the expiration date
         * 
         * @throws CertificateException on error
         */
        get() = withCertificateBody {validTo} /*{
            try {
                return cvCertificate.certificateBody.validTo
            } catch (nsfe: NoSuchFieldException) {
                throw CertificateException("No such field", nsfe)
            }
        }*/

    @get:Throws(CertificateException::class)
    val authorityReference: CVCPrincipal
        /**
         * Returns the authority reference.
         * 
         * @return the authority reference
         * 
         * @throws CertificateException if the authority reference field is not present
         */
        get() {
            try {
                val rf: ReferenceField = cvCertificate.certificateBody.authorityReference
                val countryCode = rf.country.uppercase(Locale.getDefault())
                val country = Country.getInstance(countryCode)
                return CVCPrincipal(country, rf.mnemonic, rf.sequence)
            } catch (nsfe: NoSuchFieldException) {
                throw CertificateException("No such field", nsfe)
            }
        } /*{
        get() {
            try {
                val rf: ReferenceField = cvCertificate.certificateBody.authorityReference
                val countryCode = rf.country.uppercase(Locale.getDefault())
                val country = Country.getInstance(countryCode)
                return CVCPrincipal(country, rf.mnemonic, rf.sequence)
            } catch (nsfe: NoSuchFieldException) {
                throw CertificateException("No such field", nsfe)
            }
        }*/

    @get:Throws(CertificateException::class)
    val holderReference: CVCPrincipal
        /**
         * Returns the holder reference.
         * 
         * @return the holder reference
         * 
         * @throws CertificateException if the authority reference field is not present
         */
        get() {
            try {
                val rf: ReferenceField = cvCertificate.certificateBody.holderReference
                return CVCPrincipal(
                    Country.getInstance(
                        rf.country.uppercase(Locale.getDefault())
                    ), rf.mnemonic, rf.sequence
                )
            } catch (nsfe: NoSuchFieldException) {
                throw CertificateException("No such field", nsfe)
            }
        }
        /*get() {
            try {
                val rf: ReferenceField = cvCertificate.certificateBody.holderReference
                return CVCPrincipal(
                    Country.getInstance(
                        rf.country.uppercase(Locale.getDefault())
                    ), rf.mnemonic, rf.sequence
                )
            } catch (nsfe: NoSuchFieldException) {
                throw CertificateException("No such field", nsfe)
            }
        }*/

    @get:Throws(CertificateException::class)
    val authorizationTemplate: CVCAuthorizationTemplate
        /**
         * Returns the holder authorization template.
         * 
         * @return the holder authorization template
         * 
         * @throws CertificateException on error constructing the template
         */
        get() {
            try {
                val template =
                    cvCertificate.certificateBody.authorizationTemplate
                return CVCAuthorizationTemplate(template)
            } catch (nsfe: NoSuchFieldException) {
                throw CertificateException("No such field", nsfe)
            }
        }

    @get:Throws(CertificateException::class)
    val signature: ByteArray?
        /**
         * Returns the signature (just the value, without the `0x5F37` tag).
         * 
         * @return the signature bytes
         * 
         * @throws CertificateException if certificate doesn't contain a signature
         */
        get() = withBody { signature }
        /*get() {
            try {
                return cvCertificate.signature
            } catch (nsfe: NoSuchFieldException) {
                throw CertificateException("No such field", nsfe)
            }
        }*/

    /**
     * Tests for equality with respect to another object.
     * 
     * @param otherObj the other object
     * 
     * @return whether this certificate equals the other object
     */
    override fun equals(otherObj: Any?): Boolean {
        if (otherObj == null) {
            return false
        }
        if (this === otherObj) {
            return true
        }
        if (this.javaClass != otherObj.javaClass) {
            return false
        }

        return this.cvCertificate == (otherObj as CardVerifiableCertificate).cvCertificate
    }

    private inline fun <T> withCertificateBody(block: CVCertificateBody.() -> T): T? =
        try {
            cvCertificate.certificateBody.block()
        } catch (nsfe: NoSuchFieldException) {
            throw CertificateException("No such field", nsfe)
        }

    private inline fun <T> withBody(block: CVCertificate.() -> T): T? =
        try {
            cvCertificate.block()
        } catch (nsfe: NoSuchFieldException) {
            throw CertificateException("No such field", nsfe)
        }
    /**
     * Returns a hash code for this object.
     * 
     * @return a hash code for this object
     */
    override fun hashCode(): Int {
        return cvCertificate.hashCode() * 2 - 1030507011
    }

    companion object {
        private val serialVersionUID = -3585440601605666288L

        private val LOGGER: Logger = Logger.getLogger("org.jmrtd")
    }
}
