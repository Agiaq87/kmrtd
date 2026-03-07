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
 * $Id: PACEGMWithDHMappingResult.java 1763 2018-02-18 07:41:30Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.protocol

import java.security.KeyPair
import java.security.PublicKey
import java.security.spec.AlgorithmParameterSpec

/**
 * The result of the PACE nonce mapping step in Generic Mapping with Diffie-Hellman setting.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * @version $Revision: $
 */
class PACEGMWithDHMappingResult(
    staticParameters: AlgorithmParameterSpec?, piccNonce: ByteArray?,
    piccMappingPublicKey: PublicKey, pcdMappingKeyPair: KeyPair,
    sharedSecret: ByteArray?, ephemeralParameters: AlgorithmParameterSpec
) : PACEGMMappingResult(
    staticParameters,
    piccNonce,
    piccMappingPublicKey,
    pcdMappingKeyPair,
    ephemeralParameters
) {
    private val sharedSecret: ByteArray? = sharedSecret?.copyOf(sharedSecret.size)

    /**
     * Returns the shared secret that was derived during this protocol step.
     * 
     * @return the shared secret
     */
    fun getSharedSecret(): ByteArray? {
        return sharedSecret?.copyOf(sharedSecret.size)
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = super.hashCode()
        result = prime * result + sharedSecret.contentHashCode()
        return result
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (!super.equals(obj)) {
            return false
        }
        if (javaClass != obj?.javaClass) {
            return false
        }

        val other = obj as PACEGMWithDHMappingResult
        return sharedSecret.contentEquals(other.sharedSecret)
    }
}
