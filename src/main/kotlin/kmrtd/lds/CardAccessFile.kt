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
 * $Id: CardAccessFile.java 1850 2021-05-21 06:25:03Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.lds

import kmrtd.lds.SecurityInfo.Companion.getInstance
import org.bouncycastle.asn1.*
import java.io.*
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Card access file stores a set of SecurityInfos for PACE.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1850 $
 * 
 * @since 0.5.1
 */
class CardAccessFile : Serializable {
    /** The security infos that make up this file.  */
    private var securityInfos: MutableSet<SecurityInfo>? = null

    /**
     * Constructs a new file from the provided data.
     * 
     * @param securityInfos a non-empty list of security infos
     */
    constructor(securityInfos: MutableCollection<SecurityInfo?>) {
        requireNotNull(securityInfos) { "Null securityInfos" }
        this.securityInfos = HashSet<SecurityInfo>(securityInfos)
    }

    /**
     * Constructs a new file from the data in an input stream.
     * 
     * @param inputStream the input stream to parse the data from
     * 
     * @throws IOException on error reading input stream
     */
    constructor(inputStream: InputStream?) {
        readContent(inputStream)
    }

    /**
     * Reads the contents as a card access file from a stream.
     * 
     * @param inputStream the stream to read from
     * 
     * @throws IOException on error reading from the stream
     */
    @Throws(IOException::class)
    protected fun readContent(inputStream: InputStream?) {
        securityInfos = HashSet<SecurityInfo>()
        val asn1In = ASN1InputStream(inputStream)
        val set = asn1In.readObject() as ASN1Set
        for (i in 0..<set.size()) {
            val `object` = set.getObjectAt(i).toASN1Primitive()
            try {
                val securityInfo = getInstance(`object`)
                if (securityInfo == null) {
                    /* NOTE: skipping this unsupported SecurityInfo */
                    continue
                }
                securityInfos!!.add(securityInfo)
            } catch (e: Exception) {
                /* NOTE: skipping this unsupported SecurityInfo. */
                continue
            }
        }
    }

    /* FIXME: rewrite (using writeObject instead of getDERObject) to remove interface dependency on BC. */
    /**
     * Writes the contents of this file to a stream.
     * 
     * @param outputStream the stream to write to
     * 
     * @throws IOException on error writing to the stream
     */
    @Throws(IOException::class)
    protected fun writeContent(outputStream: OutputStream) {
        val vector = ASN1EncodableVector()
        for (securityInfo in securityInfos!!) {
            vector.add(securityInfo.dERObject)
        }
        val derSet: ASN1Set = DLSet(vector)
        outputStream.write(derSet.getEncoded(ASN1Encoding.DER))
    }

    val encoded: ByteArray?
        /**
         * Returns a DER encoded of this file.
         * 
         * @return the encoded file
         */
        get() {
            val byteArrayOutputStream = ByteArrayOutputStream()
            try {
                writeContent(byteArrayOutputStream)
                byteArrayOutputStream.flush()
                return byteArrayOutputStream.toByteArray()
            } catch (ioe: IOException) {
                LOGGER.log(
                    Level.WARNING,
                    "Exception while encoding",
                    ioe
                )
                return null
            } finally {
                try {
                    byteArrayOutputStream.close()
                } catch (ioe: IOException) {
                    LOGGER.log(
                        Level.FINE,
                        "Error closing stream",
                        ioe
                    )
                }
            }
        }

    /**
     * Returns the security infos as an unordered collection.
     * 
     * @return security infos
     */
    fun getSecurityInfos(): MutableCollection<SecurityInfo?> {
        return Collections.unmodifiableCollection<SecurityInfo?>(securityInfos)
    }

    /**
     * Returns the signature algorithm object identifier.
     * 
     * @return signature algorithm OID
     */
    override fun toString(): String {
        return StringBuilder()
            .append("CardAccessFile [")
            .append(securityInfos.toString())
            .append("]").toString()
    }

    /**
     * Tests equality with respect to another object.
     * 
     * @param otherObj another object
     * 
     * @return whether this object equals the other object
     */
    override fun equals(otherObj: Any?): Boolean {
        if (otherObj == null) {
            return false
        }

        if (!(otherObj.javaClass == this.javaClass)) {
            return false
        }

        val other = otherObj as CardAccessFile
        if (securityInfos == null) {
            return other.securityInfos == null
        }
        if (other.securityInfos == null) {
            return securityInfos == null
        }

        return securityInfos == other.securityInfos
    }

    /**
     * Returns a hash code of this object.
     * 
     * @return the hash code
     */
    override fun hashCode(): Int {
        return 7 * securityInfos.hashCode() + 61
    }

    companion object {
        private val LOGGER: Logger = Logger.getLogger("kmrtd.lds")

        private const val serialVersionUID = -3536507558193769951L
    }
}
