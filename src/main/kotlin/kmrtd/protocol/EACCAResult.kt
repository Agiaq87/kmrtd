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
 * $Id: EACCAResult.java 1852 2021-06-10 10:56:17Z martijno $
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
import java.io.Serializable
import java.math.BigInteger
import java.security.PrivateKey
import java.security.PublicKey

/**
 * Result of EAC Chip Authentication protocol.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1852 $
 */
class EACCAResult
/**
 * Creates a result.
 * 
 * @param keyId the key identifier of the ICC's public key or `null`
 * @param publicKey the ICC's public key
 * @param keyHash the hash of the PCD's public key
 * @param pCDPublicKey the public key of the terminal
 * @param pCDPrivateKey the private key of the terminal
 * @param wrapper secure messaging wrapper
 */(
    /**
     * Returns the ICC's public key identifier.
     * 
     * @return the key id or `null`
     */
    val keyId: BigInteger?,
    /**
     * Returns the PICC's public key that was used as input to chip authentication protocol.
     * 
     * @return the public key
     */
    val publicKey: PublicKey?,
    /**
     * Returns the hash of the ephemeral public key of the terminal.
     * 
     * @return the hash of the ephemeral public key of the terminal
     */
    val keyHash: ByteArray?,
    /**
     * Returns the ephemeral public key of the terminal that was used in the key exchange.
     * 
     * @return the public key
     */
    val pCDPublicKey: PublicKey?,
    /**
     * The ephemeral private key of the terminal that was used in the key exchange.
     * 
     * @return the private key
     */
    val pCDPrivateKey: PrivateKey?,
    /**
     * Returns the resulting secure messaging wrapper.
     * 
     * @return the secure messaging wrapper
     */
    val wrapper: SecureMessagingWrapper?
) : Serializable {
    override fun toString(): String {
        return StringBuilder()
            .append("EACCAResult [keyId: ").append(keyId)
            .append(", PICC public key: ").append(this.publicKey)
            .append(", wrapper: ").append(wrapper)
            .append(", key hash: ").append(Hex.bytesToHexString(keyHash))
            .append(", PCD public key: ").append(Util.getDetailedPublicKeyAlgorithm(this.pCDPublicKey))
            .append(", PCD private key: ").append(Util.getDetailedPrivateKeyAlgorithm(this.pCDPrivateKey))
            .append("]").toString()
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + keyHash.contentHashCode()
        result = prime * result + (if (keyId == null) 0 else keyId.hashCode())
        result = prime * result + (if (this.publicKey == null) 0 else publicKey.hashCode())
        result = prime * result + (if (this.pCDPublicKey == null) 0 else pCDPublicKey.hashCode())
        result = prime * result + (if (this.pCDPrivateKey == null) 0 else pCDPrivateKey.hashCode())
        result = prime * result + (if (wrapper == null) 0 else wrapper.hashCode())
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
        val other = obj as EACCAResult
        if (!keyHash.contentEquals(other.keyHash)) {
            return false
        }
        if (keyId == null) {
            if (other.keyId != null) {
                return false
            }
        } else if (keyId != other.keyId) {
            return false
        }
        if (this.pCDPrivateKey == null) {
            if (other.pCDPrivateKey != null) {
                return false
            }
        } else if (this.pCDPrivateKey != other.pCDPrivateKey) {
            return false
        }
        if (this.pCDPublicKey == null) {
            if (other.pCDPublicKey != null) {
                return false
            }
        } else if (this.pCDPublicKey != other.pCDPublicKey) {
            return false
        }
        if (this.publicKey == null) {
            if (other.publicKey != null) {
                return false
            }
        } else if (this.publicKey != other.publicKey) {
            return false
        }
        if (wrapper == null) {
            if (other.wrapper != null) {
                return false
            }
        } else if (wrapper != other.wrapper) {
            return false
        }

        return true
    }

    companion object {
        private const val serialVersionUID = 4431711176589761513L
    }
}
