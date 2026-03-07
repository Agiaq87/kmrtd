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

import java.io.Serializable
import java.math.BigInteger
import java.security.PrivateKey
import java.security.PublicKey

/**
 * Result of EAC Chip Authentication protocol.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * @version $Revision: 1852 $
 */
data class EACCAResult
/**
 * Creates a result.
 * 
 * @param keyId         the key identifier of the ICC's public key or `null`
 * @param publicKey the ICC's public key
 * @param keyHash       the hash of the PCD's public key
 * @param pCDPublicKey  the public key of the terminal
 * @param pCDPrivateKey the private key of the terminal
 * @param wrapper       secure messaging wrapper
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
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EACCAResult

        if (keyId != other.keyId) return false
        if (publicKey != other.publicKey) return false
        if (!keyHash.contentEquals(other.keyHash)) return false
        if (pCDPublicKey != other.pCDPublicKey) return false
        if (pCDPrivateKey != other.pCDPrivateKey) return false
        if (wrapper != other.wrapper) return false

        return true
    }

    override fun hashCode(): Int {
        var result = keyId?.hashCode() ?: 0
        result = 31 * result + (publicKey?.hashCode() ?: 0)
        result = 31 * result + (keyHash?.contentHashCode() ?: 0)
        result = 31 * result + (pCDPublicKey?.hashCode() ?: 0)
        result = 31 * result + (pCDPrivateKey?.hashCode() ?: 0)
        result = 31 * result + (wrapper?.hashCode() ?: 0)
        return result
    }
}
