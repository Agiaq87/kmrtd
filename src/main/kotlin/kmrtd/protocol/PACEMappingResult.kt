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
 * $Id: PACEMappingResult.java 1757 2018-02-05 12:01:00Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.protocol

import java.io.Serializable
import java.security.spec.AlgorithmParameterSpec

/**
 * The result of a the nonce mapping step.
 * This is the abstract super type, specific implementations
 * will contain more relevant details.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * @version $Revision: $
 */
abstract class PACEMappingResult(
    /**
     * Returns the static agreement parameters.
     * 
     * @return the original parameters
     */
    // FIXME: Should be serializable instead of transient.
    @field:Transient val staticParameters: AlgorithmParameterSpec?,
    piccNonce: ByteArray?,
    /**
     * Returns the ephemeral (derived) agreement parameters.
     * 
     * @return the resulting parameters
     */
    // FIXME: Should be serializable instead of transient.
    @field:Transient var ephemeralParameters: AlgorithmParameterSpec
) : Serializable {
    /**
     * Returns the nonce that was sent by the PICC.
     * 
     * @return the nonce
     */
    var pICCNonce: ByteArray?
        private set

    /**
     * Constructs a mapping result.
     * 
     * @param staticParameters    the static agreement parameters
     * @param piccNonce           the nonce that was sent by the PICC
     * @param ephemeralParameters the resulting ephemeral parameters
     */
    init {
        this.ephemeralParameters = ephemeralParameters

        this.pICCNonce = null
        if (piccNonce != null) {
            this.pICCNonce = ByteArray(piccNonce.size)
            System.arraycopy(piccNonce, 0, this.pICCNonce, 0, piccNonce.size)
        }
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result =
            prime * result + (ephemeralParameters.hashCode())
        result = prime * result + pICCNonce.contentHashCode()
        result = prime * result + (staticParameters?.hashCode() ?: 0)
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

        val other = obj as PACEMappingResult
        if (ephemeralParameters != other.ephemeralParameters) {
            return false
        }
        if (!pICCNonce.contentEquals(other.pICCNonce)) {
            return false
        }
        if (staticParameters == null) {
            if (other.staticParameters != null) {
                return false
            }
        } else if (staticParameters != other.staticParameters) {
            return false
        }

        return true
    }
}
