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
 * $Id: AbstractTaggedLDSFile.java 1811 2019-05-27 14:08:20Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.lds

import kotlinx.serialization.Serializable
import net.sf.scuba.tlv.TLVInputStream
import net.sf.scuba.tlv.TLVOutputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Base class for TLV based LDS files.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1811 $
 */
@Serializable
abstract class AbstractTaggedLDSFile protected constructor(dataGroupTag: Int): AbstractLDSFile() {
    /**
     * Returns the tag that identifies this LDS file.
     * 
     * @return the tag of this LDS file
     */
    open var tag: Int = dataGroupTag
    /**
     * The length of the value of the data group.
     *
     * @return the length of the value of the data group
     */
    override var length = 0
        get()  {
            if (field <= 0) {
                field = this.content.size
            }
            return field
        }

    /**
     * Constructs a data group. This constructor
     * is only visible to the other classes in this package.
     * 
     * @param dataGroupTag data group tag
     */
    /*protected constructor(dataGroupTag: Int) {
        this.tag = dataGroupTag
    }*/

    /**
     * Constructs a data group from the DER encoded data in the
     * given input stream.
     * 
     * @param tag the tag
     * @param inputStream an input stream
     * 
     * @throws IOException on error reading input stream
     */
    protected constructor(tag: Int, inputStream: InputStream) : this(tag) {
        //this.tag = tag
        readObject(inputStream)
    }

    /**
     * Reads the contents of this LDS file, including tag and length from an input stream.
     * 
     * @param inputStream the stream to read from
     * 
     * @throws IOException if reading from the stream fails
     */
    @Throws(IOException::class)
    override fun readObject(inputStream: InputStream) {
        val tlvIn = inputStream as? TLVInputStream ?: TLVInputStream(inputStream)
        val inputTag = tlvIn.readTag()
        require(inputTag == tag) {
            "Was expecting tag " + Integer.toHexString(tag) + ", found " + Integer.toHexString(
                inputTag
            )
        }
        length = tlvIn.readLength()
        readContent(tlvIn)
        //    readContent(new SplittableInputStream(inputStream, length)); // was using this in <= 0.7.9 -- MO
    }

    @Throws(IOException::class)
    override fun writeObject(outputStream: OutputStream) {
        val tlvOut = outputStream as? TLVOutputStream ?: TLVOutputStream(outputStream)
        val ourTag = this.tag
        if (tag != ourTag) {
            tag = ourTag
        }
        tlvOut.writeTag(ourTag)
        val value = this.content
        val ourLength = if (value == null) 0 else value.size
        if (length != ourLength) {
            length = ourLength
        }
        tlvOut.writeValue(value)
    }

    /**
     * Reads the contents of the data group from an input stream.
     * Client code implementing this method should only read the contents
     * from the input stream, not the tag or length of the data group.
     * 
     * @param inputStream the input stream to read from
     * 
     * @throws IOException on error reading from input stream
     */
    @Throws(IOException::class)
    protected abstract fun readContent(inputStream: InputStream)

    /**
     * Writes the contents of the data group to an output stream.
     * Client code implementing this method should only write the contents
     * to the output stream, not the tag or length of the data group.
     * 
     * @param outputStream the output stream to write to
     * 
     * @throws IOException on error writing to output stream
     */
    @Throws(IOException::class)
    protected abstract fun writeContent(outputStream: OutputStream)

    /**
     * Returns a textual representation of this file.
     * 
     * @return a textual representation of this file
     */
    override fun toString(): String {
        return "TaggedLDSFile [" + Integer.toHexString(this.tag) + " (" + length + ")]"
    }

    /**
     * The length of the value of the data group.
     * 
     * @return the length of the value of the data group
     */
    /*override fun getLength(): Int {
        if (length <= 0) {
            length = this.content.size
        }
        return length
    }*/

    private val content: ByteArray
        /**
         * Returns the value part of this LDS file.
         * 
         * @return the value as byte array
         */
        get() {
            val outputStream = ByteArrayOutputStream()
            try {
                writeContent(outputStream)
                outputStream.flush()
                return outputStream.toByteArray()
            } catch (ioe: IOException) {
                throw IllegalStateException("Could not get DG content", ioe)
            } finally {
                try {
                    outputStream.close()
                } catch (ioe: IOException) {
                    LOGGER.log(
                        Level.FINE,
                        "Error closing stream",
                        ioe
                    )
                }
            }
        }

    companion object {
        private val serialVersionUID = -4761360877353069639L

        private val LOGGER: Logger = Logger.getLogger("kmrtd")
    }
}
