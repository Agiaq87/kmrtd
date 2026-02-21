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
 * $Id: EACTAResult.java 1799 2018-10-30 16:25:48Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.protocol

import net.sf.scuba.util.Hex
import org.jmrtd.Util
import org.jmrtd.cert.CVCPrincipal
import org.jmrtd.cert.CardVerifiableCertificate
import java.io.Serializable
import java.security.PrivateKey
import java.security.cert.CertificateException
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Result of EAC Terminal Authentication protocol.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1799 $
 */
class EACTAResult(
    /**
     * Returns the chip authentication result.
     * 
     * @return the chip authenticaiton result
     */
    val chipAuthenticationResult: EACCAResult?,
    /**
     * Returns CA certificate's reference used during EAC-TA.
     * 
     * @return CA certificate's reference
     */
    val cAReference: CVCPrincipal?,
    terminalCertificates: MutableList<CardVerifiableCertificate?>, terminalKey: PrivateKey?,
    documentNumber: String?, cardChallenge: ByteArray?
) : Serializable {
    /**
     * Returns the chain of card verifiable certificates that is to be used
     * for authenticating the PCD to the ICC.
     * 
     * @return the chain of CVCertificates used to authenticate the terminal to
     * the card
     */
    val cVCertificates: MutableList<CardVerifiableCertificate>? = ArrayList<CardVerifiableCertificate>()

    /**
     * Returns the PCD's private key used during EAC.
     * 
     * @return the PCD's private key
     */
    val terminalKey: PrivateKey?

    /**
     * Returns the identifier of the card used during EAC.
     * 
     * @return the id of the card
     */
    val documentNumber: String?

    /**
     * Returns the card's challenge generated during EAC.
     * 
     * @return the card's challenge
     */
    val cardChallenge: ByteArray?

    /**
     * Constructs a new terminal authentication result.
     * 
     * @param chipAuthenticationResult the chip authentication result
     * @param cAReference the certificate authority
     * @param terminalCertificates terminal certificates
     * @param terminalKey the terminal's private key
     * @param documentNumber the documentNumber
     * @param cardChallenge the challenge
     */
    init {
        for (terinalCertificate in terminalCertificates) {
            this.cVCertificates!!.add(terinalCertificate!!)
        }
        this.terminalKey = terminalKey
        this.documentNumber = documentNumber
        this.cardChallenge = cardChallenge
    }

    /**
     * Returns a textual representation of this terminal authentication result.
     * 
     * @return a textual representation of this terminal authentication result
     */
    override fun toString(): String {
        val result = StringBuilder()
        result.append("TAResult [chipAuthenticationResult: " + chipAuthenticationResult).append(", ")
        result.append("caReference: " + this.cAReference).append(", ")
        result.append("terminalCertificates: [")
        var isFirst = true
        for (cert in this.cVCertificates!!) {
            if (isFirst) {
                isFirst = false
            } else {
                result.append(", ")
            }
            result.append(toString(cert))
        }
        result.append("terminalKey = ").append(Util.getDetailedPrivateKeyAlgorithm(terminalKey)).append(", ")
        result.append("documentNumber = ").append(documentNumber).append(", ")
        result.append("cardChallenge = ").append(Hex.bytesToHexString(cardChallenge)).append(", ")
        result.append("]")
        return result.toString()
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + (if (this.cAReference == null) 0 else cAReference.hashCode())
        result = prime * result + cardChallenge.contentHashCode()
        result = prime * result + (if (chipAuthenticationResult == null) 0 else chipAuthenticationResult.hashCode())
        result = prime * result + (if (documentNumber == null) 0 else documentNumber.hashCode())
        result = prime * result + (if (this.cVCertificates == null) 0 else cVCertificates.hashCode())
        result = prime * result + (if (terminalKey == null) 0 else terminalKey.hashCode())
        return result
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (obj == null) {
            return false
        }
        if (javaClass != obj.javaClass) {
            return false
        }
        val other = obj as EACTAResult
        if (this.cAReference == null) {
            if (other.cAReference != null) {
                return false
            }
        } else if (this.cAReference != other.cAReference) {
            return false
        }
        if (!cardChallenge.contentEquals(other.cardChallenge)) {
            return false
        }
        if (chipAuthenticationResult == null) {
            if (other.chipAuthenticationResult != null) {
                return false
            }
        } else if (!chipAuthenticationResult.equals(other.chipAuthenticationResult)) {
            return false
        }
        if (documentNumber == null) {
            if (other.documentNumber != null) {
                return false
            }
        } else if (documentNumber != other.documentNumber) {
            return false
        }
        if (this.cVCertificates == null) {
            if (other.cVCertificates != null) {
                return false
            }
        } else if (this.cVCertificates != other.cVCertificates) {
            return false
        }
        if (terminalKey == null) {
            return other.terminalKey == null
        }

        return terminalKey == other.terminalKey
    }

    /**
     * Returns a textual representation of the certificate.
     * 
     * @param certificate the certificate
     * 
     * @return a textual representation of the certificate
     */
    private fun toString(certificate: CardVerifiableCertificate): Any {
        val result = StringBuilder()
        result.append("CardVerifiableCertificate [")
        try {
            val reference = certificate.getHolderReference()
            if (this.cAReference != reference) {
                result.append("holderReference: " + reference)
            }
        } catch (ce: CertificateException) {
            result.append("holderReference = ???")
            LOGGER.log(Level.WARNING, "Exception", ce)
        }

        result.append("]")

        return result.toString()
    }

    companion object {
        private val serialVersionUID = -2926063872890928748L

        private val LOGGER: Logger = Logger.getLogger("org.jmrtd")
    }
}
