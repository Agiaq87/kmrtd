/*
 * JMRTD - A Java API for accessing machine readable travel documents.
 *
 * Copyright (C) 2006 - 2023  The JMRTD team
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
 * $Id: AAProtocol.java 1878 2023-07-31 13:19:51Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.protocol

import kmrtd.APDULevelAACapable
import kmrtd.CardServiceProtocolException
import kmrtd.Util
import net.sf.scuba.smartcards.CardServiceException
import java.security.PublicKey

/**
 * The Active Authentication protocol.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * @version $Revision: 1878 $
 * @since 0.5.6
 */
data class AAProtocol
/**
 * Creates a protocol instance.
 * 
 * @param service the service for APDU communication
 * @param wrapper the secure messaging wrapper
 */(private val service: APDULevelAACapable, private val wrapper: SecureMessagingWrapper?) {
    /**
     * Performs the Active Authentication protocol.
     * 
     * @param publicKey          the public key to use (usually read from the card)
     * @param digestAlgorithm    the digest algorithm to use, or null
     * @param signatureAlgorithm signature algorithm
     * @param challenge          challenge
     * @return a boolean indicating whether the card was authenticated
     * @throws CardServiceException on error
     */
    @Throws(CardServiceException::class)
    fun doAA(
        publicKey: PublicKey?,
        digestAlgorithm: String?,
        signatureAlgorithm: String?,
        challenge: ByteArray
    ): AAResult {
        try {
            require(challenge.size == 8) { "AA failed: bad challenge" }
            val response = service.sendInternalAuthenticate(
                wrapper,
                Util.getApproximateSignatureSize(publicKey),
                challenge
            )
            return AAResult(publicKey, digestAlgorithm, signatureAlgorithm, challenge, response)
        } catch (e: Exception) {
            throw CardServiceProtocolException("Exception", 1, e)
        }
    }
}
