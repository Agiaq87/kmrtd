/*
 * JMRTD - A Java API for accessing machine readable travel documents.
 *
 * Copyright (C) 2006 - 2017  The JMRTD team
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
 * $Id: EACTAProtocol.java 1853 2021-06-26 18:13:26Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.protocol

import kmrtd.APDULevelEACTACapable
import kmrtd.CardServiceProtocolException
import kmrtd.Util
import kmrtd.cert.CVCPrincipal
import kmrtd.cert.CardVerifiableCertificate
import kmrtd.cert.support.Role
import kmrtd.lds.icao.MRZInfo
import net.sf.scuba.smartcards.CardServiceException
import net.sf.scuba.tlv.TLVOutputStream
import net.sf.scuba.tlv.TLVUtil
import org.bouncycastle.jce.interfaces.ECPrivateKey
import java.io.ByteArrayOutputStream
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.Provider
import java.security.PublicKey
import java.security.Signature
import java.security.interfaces.ECPublicKey
import java.util.Locale
import java.util.logging.Level
import java.util.logging.Logger
import javax.crypto.interfaces.DHPublicKey
import kotlin.math.ceil

/**
 * The EAC Terminal Authentication protocol.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * @version $Revision: 1853 $
 * @since 0.5.6
 */
data class EACTAProtocol
/**
 * Creates a protocol instance.
 * 
 * @param service the card service for APDU communication
 * @param wrapper the secure messaging wrapper
 */(private val service: APDULevelEACTACapable, private val wrapper: SecureMessagingWrapper?) {
    /**
     * Perform the EAC-TA (Terminal Authentication) part of EAC (version 1).
     * For details see TR-03110 ver. 1.11. In short, we feed the sequence of
     * terminal certificates to the card for verification, get a challenge
     * from the card, sign it with terminal private key, and send back to
     * the card for verification.
     * 
     * @param caReference              a reference to the issuer
     * @param terminalCertificates     the terminal certificate chain
     * @param terminalKey              the terminal private key
     * @param taAlg                    the algorithm
     * @param chipAuthenticationResult the chip authentication result
     * @param documentNumber           the document number from which the chip key hash will be derived
     * @return the Terminal Authentication result
     * @throws CardServiceException on error
     */
    @Synchronized
    @Throws(CardServiceException::class)
    fun doEACTA(
        caReference: CVCPrincipal?,
        terminalCertificates: MutableList<CardVerifiableCertificate>,
        terminalKey: PrivateKey,
        taAlg: String?,
        chipAuthenticationResult: EACCAResult,
        documentNumber: String?
    ): EACTAResult {
        val idPICC: ByteArray? = deriveIdentifier(documentNumber)
        return doTA(
            caReference,
            terminalCertificates,
            terminalKey,
            taAlg,
            chipAuthenticationResult,
            idPICC
        )
    }

    /**
     * Perform TA (Terminal Authentication) part of EAC (version 1). For details see
     * TR-03110 ver. 1.11. In short, we feed the sequence of terminal certificates
     * to the card for verification, get a challenge from the card, sign it with
     * terminal private key, and send back to the card for verification.
     * 
     * @param caReference              reference issuer
     * @param terminalCertificates     terminal certificate chain
     * @param terminalKey              terminal private key
     * @param taAlg                    the algorithm
     * @param chipAuthenticationResult the chip authentication result
     * @param paceResult               the PACE result from which the chip key hash will be derived
     * @return the Terminal Authentication result
     * @throws CardServiceException on error
     */
    @Synchronized
    @Throws(CardServiceException::class)
    fun doTA(
        caReference: CVCPrincipal?,
        terminalCertificates: MutableList<CardVerifiableCertificate>,
        terminalKey: PrivateKey,
        taAlg: String?,
        chipAuthenticationResult: EACCAResult,
        paceResult: PACEResult
    ): EACTAResult {
        try {
            val idPICC: ByteArray? = deriveIdentifier(paceResult.pICCPublicKey)
            return doTA(
                caReference,
                terminalCertificates,
                terminalKey,
                taAlg,
                chipAuthenticationResult,
                idPICC
            )
        } catch (e: NoSuchAlgorithmException) {
            throw CardServiceException("No such algorithm", e)
        }
    }

    /**
     * Executes the Terminal Authentication protocol.
     * 
     * @param caReference              the certificate authority
     * @param terminalCertificates     the chain of certificates to send
     * @param terminalKey              the inspection system's private key
     * @param taAlg                    the algorithm
     * @param chipAuthenticationResult the result of the Chip Authentication protocol execution
     * @param idPICC                   the chip identifier
     * @return the result of Terminal Authentication
     * @throws CardServiceException on error
     */
    @Synchronized
    @Throws(CardServiceException::class)
    fun doTA(
        caReference: CVCPrincipal?,
        terminalCertificates: MutableList<CardVerifiableCertificate>,
        terminalKey: PrivateKey,
        taAlg: String?,
        chipAuthenticationResult: EACCAResult,
        idPICC: ByteArray?
    ): EACTAResult {
        var caReference = caReference
        try {
            require(!(terminalCertificates.isEmpty())) { "Need at least 1 certificate to perform TA, found: $terminalCertificates" }
            //requireNotNull(chipAuthenticationResult) { "Could not get EAC-CA key hash" }
            val caKeyHash = chipAuthenticationResult.keyHash
            /* The key hash that resulted from CA. */
            requireNotNull(caKeyHash) { "Could nnot get EAC-CA key hash" }

            /*
             * FIXME: check that terminalCertificates holds a (inverted, i.e. issuer before
             * subject) chain.
             */

            /*
             * Check if first cert is/has the expected CVCA, and remove it from chain if it
             * is the CVCA.
             */
            val firstCert = terminalCertificates.get(0)
            val firstCertRole = firstCert.authorizationTemplate.role
            if (Role.CVCA == firstCertRole) {
                val firstCertHolderReference = firstCert.holderReference
                if (caReference != null && caReference != firstCertHolderReference) {
                    throw CardServiceException(
                        ("First certificate holds wrong authority, found \""
                                + firstCertHolderReference.getName() + "\", expected \"" + caReference.getName() + "\"")
                    )
                }
                if (caReference == null) {
                    caReference = firstCertHolderReference
                }
                terminalCertificates.removeAt(0)
            }
            val firstCertAuthorityReference = firstCert.authorityReference
            if (caReference != null && caReference != firstCertAuthorityReference) {
                throw CardServiceException(
                    ("First certificate not signed by expected CA, found "
                            + firstCertAuthorityReference.getName() + ", expected " + caReference.getName())
                )
            }
            if (caReference == null) {
                caReference = firstCertAuthorityReference
            }

            /* Check if the last cert is an IS cert. */
            val lastCert = terminalCertificates[terminalCertificates.size - 1]
            val lastCertRole = lastCert.authorizationTemplate.role
            if (Role.IS != lastCertRole) {
                throw CardServiceException(
                    ("Last certificate in chain (" + lastCert.holderReference.getName()
                            + ") does not have role IS, but has role " + lastCertRole)
                )
            }

            /* Have the MRTD check our chain. */
            for (cert in terminalCertificates) {
                try {
                    val authorityReference = cert.authorityReference

                    /* Step 1: MSE:SetDST */
                    /*
                     * Manage Security Environment: Set for verification: Digital Signature
                     * Template, indicate authority of cert to check.
                     */
                    val authorityRefBytes = TLVUtil.wrapDO(
                        0x83, authorityReference.getName().toByteArray(
                            charset("ISO-8859-1")
                        )
                    )
                    service.sendMSESetDST(wrapper, authorityRefBytes)
                } catch (e: Exception) {
                    throw CardServiceProtocolException("Exception in MSE:SetDST", 1, e)
                }

                try {
                    /* Cert body is already in TLV format. */
                    val body = cert.certBodyData

                    /* Signature not yet in TLV format, prefix it with tag and length. */
                    var signature = cert.signature
                    val sigOut = ByteArrayOutputStream()
                    val tlvSigOut = TLVOutputStream(sigOut)
                    tlvSigOut.writeTag(TAG_CVCERTIFICATE_SIGNATURE)
                    tlvSigOut.writeValue(signature)
                    tlvSigOut.close()
                    signature = sigOut.toByteArray()

                    /* Step 2: PSO:Verify Certificate */
                    service.sendPSOExtendedLengthMode(wrapper, body, signature)
                } catch (e: Exception) {
                    /* FIXME: Does this mean we failed to authenticate? -- MO */
                    throw CardServiceProtocolException("Exception", 2, e)
                }
            }

            if (terminalKey == null) {
                throw CardServiceException("No terminal key")
            }

            /* Step 3: MSE Set AT */
            try {
                val holderRef = lastCert.holderReference
                val holderRefBytes =
                    TLVUtil.wrapDO(0x83, holderRef.getName().toByteArray(charset("ISO-8859-1")))
                /*
                 * Manage Security Environment: Set for external authentication: Authentication
                 * Template
                 */
                service.sendMSESetATExtAuth(wrapper, holderRefBytes)
            } catch (e: Exception) {
                throw CardServiceProtocolException("Exception in MSE Set AT", 3, e)
            }

            /* Step 4: send get challenge */
            var rPICC: ByteArray? = null
            try {
                rPICC = service.sendGetChallenge(wrapper)
            } catch (e: Exception) {
                throw CardServiceProtocolException("Exception in Get Challenge", 4, e)
            }

            /* Step 5: external authenticate. */
            try {
                val dtbs = ByteArrayOutputStream()
                dtbs.write(idPICC)
                dtbs.write(rPICC)
                dtbs.write(caKeyHash)
                dtbs.close()
                val dtbsBytes = dtbs.toByteArray()

                val sigAlg = lastCert.sigAlgName
                checkNotNull(sigAlg) { "Could not determine signature algorithm for terminal certificate " + lastCert.holderReference.getName() }
                val sig: Signature = Signature.getInstance(sigAlg, BC_PROVIDER)
                sig.initSign(terminalKey)
                sig.update(dtbsBytes)
                var signedData = sig.sign()
                if (sigAlg.uppercase(Locale.getDefault()).endsWith("ECDSA")) {
                    val keySize = ceil(
                        (terminalKey as ECPrivateKey).getParameters().getCurve()
                            .getFieldSize() / 8.0
                    ).toInt() //TODO: Interop Ispra 20170925
                    signedData = Util.getRawECDSASignature(signedData, keySize)
                }

                service.sendMutualAuthenticate(wrapper, signedData)
                return EACTAResult(
                    chipAuthenticationResult,
                    caReference,
                    terminalCertificates,
                    terminalKey,
                    null,
                    rPICC
                )
            } catch (e: Exception) {
                LOGGER.log(Level.WARNING, "Exception", e)
                throw CardServiceProtocolException("Exception in External Authenticate", 5, e)
            }
        } catch (cse: CardServiceException) {
            throw cse
        } catch (e: Exception) {
            throw CardServiceException("Unexpected exception", e)
        }
    }

    companion object {
        private val LOGGER: Logger = Logger.getLogger("org.jmrtd.protocol")

        private const val TAG_CVCERTIFICATE_SIGNATURE = 0x5F37

        private val BC_PROVIDER: Provider = Util.getBouncyCastleProvider()

        /*
     * From BSI-03110 v1.1, B.2:
     *
     * <pre>
     * The following sequence of commands SHALL be used to implement Terminal
     * Authentication:
     *    1. MSE:Set DST
     *    2. PSO:Verify Certificate
     *    3. MSE:Set AT
     *    4. Get Challenge
     *    5. External Authenticate
     *
     * Steps 1 and 2 are repeated for every CV certificate to be verified
     * (CVCA Link Certificates, DV Certificate, IS Certificate).
     * </pre>
     */
        /**
         * Derives a chip identifier from the document number (BAC MRZ based case).
         * 
         * @param documentNumber the document number that was used for primary access control (typically BAC)
         * @return the chip identifier
         */
        fun deriveIdentifier(documentNumber: String?): ByteArray? {
            if (documentNumber == null) {
                return null
            }
            val documentNumberLength = documentNumber.length
            val idPICC = ByteArray(documentNumberLength + 1)
            try {
                System.arraycopy(
                    documentNumber.toByteArray(charset("ISO-8859-1")),
                    0,
                    idPICC,
                    0,
                    documentNumberLength
                )
                idPICC[documentNumberLength] = MRZInfo.checkDigit(documentNumber).code.toByte()
                return idPICC
            } catch (e: UnsupportedEncodingException) {
                /* NOTE: Never happens, ISO-8859-1 is always supported. */
                throw IllegalStateException("Unsupported encoding", e)
            }
        }

        /**
         * Derives a chip identifier from a PACE result (PACE case).
         * 
         * @param publicKey the PACE result
         * @return the chip identifier
         * @throws NoSuchAlgorithmException on error
         */
        @Throws(NoSuchAlgorithmException::class)
        fun deriveIdentifier(publicKey: PublicKey?): ByteArray? {
            if (publicKey == null) {
                return null
            }
            val publicKeyAlg = publicKey.algorithm
            if ("DH" == publicKeyAlg || publicKey is DHPublicKey) {
                /* TODO: this is probably wrong, what should be hashed? */
                val md = MessageDigest.getInstance("SHA-1")
                val dhPublicKey = publicKey as DHPublicKey
                return md.digest(Util.i2os(dhPublicKey.y))
            } else if ("ECDH" == publicKeyAlg || publicKey is ECPublicKey) {
                val piccECPublicKey = publicKey as org.bouncycastle.jce.interfaces.ECPublicKey
                val t = Util.i2os(piccECPublicKey.getQ().getAffineXCoord().toBigInteger())
                return Util.alignKeyDataToSize(
                    t,
                    ceil(piccECPublicKey.parameters.curve.fieldSize / 8.0).toInt()
                ) // TODO: Interop Ispra for SecP521r1 20170925.
            }

            throw NoSuchAlgorithmException("Unsupported agreement algorithm $publicKeyAlg")
        }
    }
}

