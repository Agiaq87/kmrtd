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
 * $Id: AAResult.java 1763 2018-02-18 07:41:30Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.protocol

import kmrtd.Util
import net.sf.scuba.util.Hex
import java.io.Serializable
import java.security.PublicKey

/**
 * Result from Active Authentication protocol.
 * 
 * @author The JMRTD team
 * @version $Revision: 1763 $
 */
data class AAResult
/**
 * The result of an Active Authentication protocol run.
 * 
 * @param publicKey          the AA public key that was used
 * @param digestAlgorithm    the digest algorithm that was used
 * @param signatureAlgorithm the signature algorithm that was used
 * @param challenge          the challenge that was used
 * @param response           the response that resulted
 */(
    /**
     * Returns the public key for verifying the result.
     * 
     * @return the public key for verofying the result
     */
    val publicKey: PublicKey?,
    /**
     * Returns the digest algorithm used by the ICC.
     * 
     * @return the digest algorithm used by the ICC
     */
    val digestAlgorithm: String?,
    /**
     * Returns the signature algorithm used by the ICC.
     * 
     * @return the signature algorithm used by the ICC
     */
    val signatureAlgorithm: String?,
    /**
     * Returns the challenge that was initially used.
     * 
     * @return the challenge
     */
    val challenge: ByteArray?,
    /**
     * Returns the response that was sent back by the ICC.
     * 
     * @return the response that was sent back by the ICC
     */
    val response: ByteArray?
) : Serializable {
    override fun toString(): String {
        return StringBuilder()
            .append("AAResult [")
            .append("publicKey: ").append(Util.getDetailedPublicKeyAlgorithm(publicKey))
            .append(", digestAlgorithm: ").append(digestAlgorithm)
            .append(", signatureAlgorithm: ").append(signatureAlgorithm)
            .append(", challenge: ").append(Hex.bytesToHexString(challenge))
            .append(", response: ").append(Hex.bytesToHexString(response))
            .toString()
    }

    override fun hashCode(): Int {
        val prime = 1991
        var result = 1234567891
        result = prime * result + challenge.contentHashCode()
        result = prime * result + (digestAlgorithm?.hashCode() ?: 0)
        result = prime * result + (publicKey?.hashCode() ?: 0)
        result = prime * result + response.contentHashCode()
        result =
            prime * result + (if (signatureAlgorithm == null) 0 else signatureAlgorithm.hashCode())
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

        val other = obj as AAResult
        if (!challenge.contentEquals(other.challenge)) {
            return false
        }
        if (digestAlgorithm == null) {
            if (other.digestAlgorithm != null) {
                return false
            }
        } else if (digestAlgorithm != other.digestAlgorithm) {
            return false
        }
        if (publicKey == null) {
            if (other.publicKey != null) {
                return false
            }
        } else if (publicKey != other.publicKey) {
            return false
        }
        if (!response.contentEquals(other.response)) {
            return false
        }
        if (signatureAlgorithm == null) {
            if (other.signatureAlgorithm != null) {
                return false
            }
        } else if (signatureAlgorithm != other.signatureAlgorithm) {
            return false
        }

        return true
    }
}
