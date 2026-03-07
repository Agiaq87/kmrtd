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
 * $Id: PACEGMWithECDHAgreement.java 1827 2019-11-21 11:31:13Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.protocol

import kmrtd.Util
import java.security.InvalidKeyException
import java.security.PrivateKey
import java.security.PublicKey
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.spec.ECPoint

/**
 * An ECDH key agreement implementation, used by PACE protocol for the Generic Mapping
 * case, which is able to return the shared secret in the form of an EC point.
 * 
 * 
 * Specifically this implementation is able to keep both X and Y coordinate instead of
 * only returning the X coordinate.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * @version $Revision: 1827 $
 */
data class PACEGMWithECDHAgreement(
    private val privateKey: ECPrivateKey? = null
) {
    //private var privateKey: ECPrivateKey? = null

    /**
     * Initializes the key agreement implementation.
     * 
     * @param privateKey this party's private key
     * @throws InvalidKeyException if the private key is not an instance of [ECPrivateKey]
     */
    @Throws(InvalidKeyException::class)
    fun init(privateKey: PrivateKey?) {
        if (privateKey !is ECPrivateKey) {
            throw InvalidKeyException("Not an ECPrivateKey")
        }
        //this.privateKey = privateKey
    }

    /**
     * Performs a key agreement protocol.
     * 
     * @param publicKey the other party's public key
     * @return the resulting shared secretin the form of an EC point
     * @throws InvalidKeyException   if the provided key is not an instance of [ECPublicKey]
     * @throws IllegalStateException if this key agreement has not been initialized
     */
    @Throws(InvalidKeyException::class, IllegalStateException::class)
    fun doPhase(publicKey: PublicKey?): ECPoint {
        //checkNotNull(privateKey) { "Not initialized!" }

        if (publicKey !is ECPublicKey) {
            throw InvalidKeyException("Not an ECPublicKey")
        }

        val pub = Util.toBouncyECPublicKeyParameters(publicKey)

        val p =
            pub.q.multiply(Util.toBouncyECPrivateKeyParameters(privateKey).d).normalize()
        check(!p.isInfinity) { "Infinity" }
        return Util.fromBouncyCastleECPoint(p)
    }
}
