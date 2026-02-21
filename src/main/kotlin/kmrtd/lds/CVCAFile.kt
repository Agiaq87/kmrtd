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
 * $Id: CVCAFile.java 1824 2019-11-06 08:25:39Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.lds

import org.jmrtd.PassportService
import org.jmrtd.cert.CVCPrincipal
import java.io.DataInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.StringBuilder

/* TODO: Use CVCPrincipal instead of String for references? */ /**
 * File structure for CVCA file (on EAC protected documents).
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1824 $
 */
class CVCAFile : AbstractLDSFile {
    /**
     * Returns the file identifier of this CVCA file.
     * 
     * @return the file identifier
     */
    val fID: Short

    private var caReference: String? = null

    private var altCAReference: String? = null

    /**
     * Constructs a CVCA file by reading from a stream.
     * 
     * @param inputStream the stream to read from
     * 
     * @throws IOException on error reading from the stream
     */
    constructor(inputStream: InputStream) : this(PassportService.EF_CVCA, inputStream)

    /**
     * Constructs a new CVCA file from the data contained in an input stream.
     * 
     * @param fid file identifier
     * @param inputStream stream with the data to be parsed
     * 
     * @throws IOException on error reading from input stream
     */
    constructor(fid: Short, inputStream: InputStream) {
        this.fID = fid
        readObject(inputStream)
    }

    /**
     * Constructs a new CVCA file with default file identifier.
     * 
     * @param caReference CA reference
     * @param altCAReference alternative CA reference
     */
    constructor(caReference: String, altCAReference: String?) : this(
        PassportService.EF_CVCA,
        caReference,
        altCAReference
    )

    /**
     * Constructs a new CVCA file with the given certificate references.
     * 
     * @param this.fID file identifier
     * @param caReference main CA certificate reference
     * @param altCAReference second (alternative) CA certificate reference
     */
    /**
     * Constructs a new CVCA file with the given certificate reference.
     * 
     * @param fid file identifier
     * @param caReference main CA certificate reference
     */
    @JvmOverloads
    constructor(fid: Short, caReference: String, altCAReference: String? = null) {
        require(!(caReference == null || caReference.length > 16 || (altCAReference != null && altCAReference.length > 16)))
        this.fID = fid
        this.caReference = caReference
        this.altCAReference = altCAReference
    }

    @Throws(IOException::class)
    override fun readObject(inputStream: InputStream) {
        val dataIn = DataInputStream(inputStream)
        var tag = dataIn.read()
        require(tag == CAR_TAG.toInt()) {
            "Wrong tag, expected " + Integer.toHexString(CAR_TAG.toInt()) + ", found " + Integer.toHexString(
                tag
            )
        }
        var length = dataIn.read()
        require(length <= 16) { "Wrong length" }
        var data = ByteArray(length)
        dataIn.readFully(data)
        caReference = String(data)
        tag = dataIn.read()
        if (tag != 0 && tag != -1) {
            require(tag == CAR_TAG.toInt()) { "Wrong tag" }
            length = dataIn.read()
            require(length <= 16) { "Wrong length" }
            data = ByteArray(length)
            dataIn.readFully(data)
            altCAReference = String(data)
            tag = dataIn.read()
        }
        while (tag != -1) {
            require(tag == 0) { "Bad file padding" }
            tag = dataIn.read()
        }
    }

    @Throws(IOException::class)
    override fun writeObject(outputStream: OutputStream) {
        val result = ByteArray(Companion.length)
        result[0] = CAR_TAG
        result[1] = caReference!!.length.toByte()
        System.arraycopy(caReference!!.toByteArray(), 0, result, 2, result[1].toInt())
        if (altCAReference != null) {
            val index = result[1] + 2
            result[index] = CAR_TAG
            result[index + 1] = altCAReference!!.length.toByte()
            System.arraycopy(
                altCAReference!!.toByteArray(), 0, result, index + 2,
                result[index + 1].toInt()
            )
        }
        outputStream.write(result)
    }

    val cAReference: CVCPrincipal?
        /**
         * Returns the CA Certificate identifier.
         * 
         * @return the CA Certificate identifier
         */
        get() = if (caReference == null) null else CVCPrincipal(caReference)

    /**
     * Returns the second (alternative) CA Certificate identifier, null if none
     * exists.
     * 
     * @return the second (alternative) CA Certificate identifier
     */
    fun getAltCAReference(): CVCPrincipal? {
        return if (altCAReference == null) null else CVCPrincipal(altCAReference)
    }

    /**
     * Returns a textual representation of this CVCAFile.
     * 
     * @return a textual representation of this CVCAFile
     */
    override fun toString(): String {
        return StringBuilder()
            .append("CA reference: \"").append(caReference).append("\"")
            .append((if (altCAReference != null) ", Alternative CA reference: " + altCAReference else ""))
            .toString()
    }

    /**
     * Tests whether this CVCAFile is equal to the provided object.
     * 
     * @param other some other object
     * 
     * @return whether this CVCAFile equals the other object
     */
    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        if (this.javaClass != other.javaClass) {
            return false
        }

        val otherCVCAFile = other as CVCAFile
        return caReference == otherCVCAFile.caReference
                && ((altCAReference == null && otherCVCAFile.altCAReference == null)
                || (altCAReference != null && altCAReference == otherCVCAFile.altCAReference))
    }

    /**
     * Computes a hash code of this CVCAFile.
     * 
     * @return a hash code
     */
    override fun hashCode(): Int {
        return (11 * caReference.hashCode() + (if (altCAReference != null) 13 * altCAReference.hashCode() else 0)
                + 5)
    }

    companion object {
        private val serialVersionUID = -1100904058684365703L

        const val CAR_TAG: Byte = 0x42
        val length: Int = 36
            /**
             * Returns the length of the content of this CVCA file. This always returns {@value #LENGTH}.
             * 
             * @return {@value #LENGTH}
             */
            get() = Companion.field
    }
}
