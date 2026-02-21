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
 * $Id: DG15File.java 1808 2019-03-07 21:32:19Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.lds.icao

import kmrtd.lds.DataGroup
import kmrtd.lds.LDSFile
import org.jmrtd.Util
import java.io.DataInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.GeneralSecurityException
import java.security.InvalidAlgorithmParameterException
import java.security.PublicKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec
import java.util.logging.Level
import java.util.logging.Logger

/**
 * File structure for the EF_DG15 file.
 * Datagroup 15 contains the public key used in Active Authentication.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1808 $
 */
class DG15File : DataGroup {
    private var publicKey: PublicKey? = null

    /**
     * Constructs a new file.
     * 
     * @param publicKey the key to store in this file
     */
    constructor(publicKey: PublicKey) : super(LDSFile.Companion.EF_DG15_TAG) {
        this.publicKey = publicKey
    }

    /**
     * Constructs a new file from binary representation.
     * 
     * @param inputStream an input stream
     * 
     * @throws IOException on error reading from input stream
     */
    constructor(inputStream: InputStream?) : super(LDSFile.Companion.EF_DG15_TAG, inputStream)

    @Throws(IOException::class)
    override fun readContent(inputStream: InputStream) {
        val dataInputStream = if (inputStream is DataInputStream) inputStream else DataInputStream(inputStream)
        try {
            val value = ByteArray(getLength())
            dataInputStream.readFully(value)

            publicKey = getPublicKey(value)
        } catch (e: GeneralSecurityException) {
            LOGGER.log(Level.WARNING, "Unexpected exception while reading DG15 content", e)
        }
    }

    @Throws(IOException::class)
    override fun writeContent(out: OutputStream) {
        out.write(publicKey!!.getEncoded())
    }

    /**
     * Returns the public key stored in this file.
     * 
     * @return the public key
     */
    fun getPublicKey(): PublicKey {
        return publicKey!!
    }

    override fun equals(obj: Any?): Boolean {
        if (obj == null) {
            return false
        }
        if (obj.javaClass != this.javaClass) {
            return false
        }

        val other = obj as DG15File
        return publicKey == other.publicKey
    }

    override fun hashCode(): Int {
        return 5 * publicKey.hashCode() + 61
    }

    public override fun toString(): String {
        return "DG15File [" + Util.getDetailedPublicKeyAlgorithm(publicKey) + "]"
    }

    companion object {
        private const val serialVersionUID = 3834304239673755744L

        private val LOGGER: Logger = Logger.getLogger("org.jmrtd")

        private val PUBLIC_KEY_ALGORITHMS = arrayOf<String?>("RSA", "EC")

        /**
         * Constructs a public key from the given key bytes.
         * Public keys of type `"RSA"` and `"EC"`
         * in X509 encoding are supported.
         * 
         * @param keyBytes an X509 encoded public key
         * 
         * @return a public object
         * 
         * @throws GeneralSecurityException when the bytes cannot be interpreted as a public key
         */
        @Throws(GeneralSecurityException::class)
        private fun getPublicKey(keyBytes: ByteArray): PublicKey {
            val pubKeySpec = X509EncodedKeySpec(keyBytes)

            for (algorithm in PUBLIC_KEY_ALGORITHMS) {
                try {
                    return Util.getPublicKey(algorithm, pubKeySpec)
                } catch (ikse: InvalidKeySpecException) {
                    LOGGER.log(Level.FINE, "Ignore, try next algorithm", ikse)
                }
            }

            throw InvalidAlgorithmParameterException()
        }
    }
}
