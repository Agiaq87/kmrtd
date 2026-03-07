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
 * $Id: PACEGMWithECDHMappingResult.java 1763 2018-02-18 07:41:30Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.protocol

import java.math.BigInteger
import java.security.KeyPair
import java.security.PublicKey
import java.security.spec.AlgorithmParameterSpec
import java.security.spec.ECPoint

/**
 * The result of the PACE nonce mapping step in Generic Mapping with
 * Elliptic Curve Diffie-Hellman setting.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * @version $Revision: $
 */
class PACEGMWithECDHMappingResult(
    staticParameters: AlgorithmParameterSpec?,
    piccNonce: ByteArray?,
    piccMappingPublicKey: PublicKey,
    pcdMappingKeyPair: KeyPair,
    sharedSecretPoint: ECPoint,
    ephemeralParameters: AlgorithmParameterSpec
) : PACEGMMappingResult(
    staticParameters,
    piccNonce,
    piccMappingPublicKey,
    pcdMappingKeyPair,
    ephemeralParameters
) {
    private val sharedSecretPointX: BigInteger = sharedSecretPoint.affineX
    private val sharedSecretPointY: BigInteger = sharedSecretPoint.affineY

    val sharedSecretPoint: ECPoint
        /**
         * Returns the shared secret point that was derived during this step.
         * 
         * @return the shared secret point that was derived
         */
        get() = ECPoint(sharedSecretPointX, sharedSecretPointY)

    override fun hashCode(): Int {
        val prime = 31
        var result = super.hashCode()
        result =
            (prime * result + (sharedSecretPointX.hashCode())
                    + (sharedSecretPointY.hashCode()))

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

        val other = obj as PACEGMWithECDHMappingResult
        if (sharedSecretPointX != other.sharedSecretPointX) {
            return false
        }

        if (sharedSecretPointY != other.sharedSecretPointY) {
            return false
        }

        return true
    }
}
