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
 * $Id: PACESecretKeySpec.java 1786 2018-07-08 21:06:32Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd

import kmrtd.AccessKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * A secret key for PACE.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1786 $
 * 
 * (Contributions by g.giorkhelidze.)
 */
class PACESecretKeySpec : SecretKeySpec, AccessKeySpec {
    /**
     * Returns reference specifying the type of key from BSI TR-03110 (Appendix B).
     * 
     * @return a key reference
     */
    val keyReference: Byte

    /**
     * Constructs a secret key from the given byte array, using the first `len`
     * bytes of `key`, starting at `offset` inclusive.
     * 
     * @param key the key bytes
     * @param offset the offset with `key`
     * @param len the length of the key within `key`
     * @param algorithm the name of the secret-key algorithm to be associated with the given key material
     * @param paceKeyReference a reference specifying the type of key from BSI TR-03110 (Appendix B)
     */
    constructor(key: ByteArray, offset: Int, len: Int, algorithm: String, paceKeyReference: Byte) : super(
        key,
        offset,
        len,
        algorithm
    ) {
        this.keyReference = paceKeyReference
    }

    /**
     * Constructs a secret key from the given byte array.
     * 
     * @param key the key bytes
     * @param algorithm the name of the secret-key algorithm to be associated with the given key material
     * @param paceKeyReference a reference specifying the type of key from BSI TR-03110 (Appendix B)
     */
    constructor(key: ByteArray, algorithm: String?, paceKeyReference: Byte) : super(key, algorithm) {
        this.keyReference = paceKeyReference
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = super.hashCode()
        result = prime * result + keyReference
        return result
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (!super.equals(obj)) {
            return false
        }
        if (javaClass != obj!!.javaClass) {
            return false
        }

        val other = obj as PACESecretKeySpec
        return keyReference == other.keyReference
    }

    /**
     * Returns the encoded key (key seed) used in key derivation.
     * 
     * @return the encoded key
     */
    override fun getKey(): ByteArray? {
        return super.getEncoded()
    }

    companion object {
        private val serialVersionUID = -5181060361947453857L
    }
}
