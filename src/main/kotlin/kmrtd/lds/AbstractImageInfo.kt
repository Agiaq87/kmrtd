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
 * $Id: AbstractImageInfo.java 1808 2019-03-07 21:32:19Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.lds

import kmrtd.io.SplittableInputStream
import java.io.*
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Base class for image infos.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1808 $
 */
abstract class AbstractImageInfo
/**
 * Constructs an abstract image info with full parameters.
 * 
 * @param type the type of image
 * @param width the width
 * @param height the height
 * @param mimeType the mime-type string
 */ private constructor(
    /**
     * Sets the type.
     * 
     * @param type the new type
     */
    /**
     * Sets the type.
     * 
     * @param type the new type
     */
  override var type: Int,
    /**
     * Sets the width of this image.
     * 
     * @param width the new width
     */
    /**
     * Sets the width of this image.
     * 
     * @param width the new width
     */
  override var width: Int,
    /**
     * Sets the height of this image.
     * 
     * @param height the new height
     */
    /**
     * Sets the height of this image.
     * 
     * @param height the new height
     */
  override var height: Int,
    /**
     * Sets the mime-type.
     * 
     * @param mimeType the new mime-type
     */
    /**
     * Sets the mime-type.
     * 
     * @param mimeType the new mime-type
     */
  override var mimeType: String?
) : ImageInfo {
    /**
     * Returns the content-type,
     * where content-type is one of
     * [ImageInfo.TYPE_PORTRAIT],
     * [ImageInfo.TYPE_FINGER],
     * [ImageInfo.TYPE_IRIS],
     * [ImageInfo.TYPE_SIGNATURE_OR_MARK].
     * 
     * @return content type
     */
    /**
     * Returns the mime-type of the encoded image.
     * 
     * @return the mime-type of the encoded image
     */
    private var imageBytes: ByteArray?

    // FIXME: It's not clear how serialization should work if not fully read. (Clients should only serialize if imageBytes != null.)
    @Transient
    private var splittableInputStream: SplittableInputStream? = null
    private var imagePositionInInputStream = 0
    override var imageLength = 0

    /**
     * Returns the width of the image.
     * 
     * @return the width of the image
     */
    /**
     * Returns the height of the image.
     * 
     * @return the height of the image
     */

    /* PACKAGE ONLY VISIBLE CONSTRUCTORS BELOW */
    /**
     * Constructs a default abstract image info.
     */
    internal constructor() : this(ImageInfo.TYPE_UNKNOWN, 0, 0, null)

    /**
     * Constructs an abstract image info with a type.
     * 
     * @param type the type of image
     */
    protected constructor(type: Int) : this(type, 0, 0, null)

    /**
     * Constructs an abstract image info with a type and a mime-type.
     * 
     * @param type the type
     * @param mimeType the mime-type string
     */
    protected constructor(type: Int, mimeType: String?) : this(type, 0, 0, mimeType)

    /* PUBLIC CONSRTUCTOR BELOW */
    /**
     * Constructs an abstract image info.
     * 
     * @param type type of image info
     * @param width width of image
     * @param height height of image
     * @param inputStream encoded image
     * @param imageLength length of encoded image
     * @param mimeType mime-type of encoded image
     * 
     * @throws IOException if reading fails
     */
    constructor(
        type: Int,
        width: Int,
        height: Int,
        inputStream: InputStream,
        imageLength: Long,
        mimeType: String?
    ) : this(type, width, height, mimeType) {
        readImage(inputStream, imageLength)
    }

    /* PUBLIC METHODS BELOW */

    /**
     * Returns the length of the encoded image.
     * 
     * @return the length of the encoded image
     */
    override fun getImageLength(): Int {
        /* DEBUG: START */
        if (splittableInputStream != null) {
            return imageLength
        }

        /* DEBUG: END */
        checkNotNull(imageBytes) { "Cannot get length of null" }

        return imageBytes!!.size
    }

    /**
     * Returns a textual representation of this image info.
     * 
     * @return a textual representation of this image info
     */
    override fun toString(): String {
        return StringBuilder()
            .append(this.javaClass.simpleName)
            .append(" [")
            .append("type: ").append(typeToString(type) + ", ")
            .append("size: ").append(imageLength)
            .append("]")
            .toString()
    }

    override fun hashCode(): Int {
        var result = 1234567891
        result = 3 * result + 5 * type
        result += 5 * (mimeType?.hashCode() ?: 1337) + 7
        result += 7 * imageLength + 11
        return result
    }

    override fun equals(other: Any?): Boolean {
        try {
            if (other == null) {
                return false
            }
            if (other === this) {
                return true
            }
            if (other.javaClass != this.javaClass) {
                return false
            }

            val otherImageInfo = other as AbstractImageInfo
            return (getImageBytes().contentEquals(otherImageInfo.getImageBytes())) // && getImageLength() == otherImageInfo.getImageLength()
                    && (mimeType == null && otherImageInfo.mimeType == null || mimeType != null && mimeType == otherImageInfo.mimeType)
                    && type == otherImageInfo.type
        } catch (e: Exception) {
            LOGGER.log(Level.WARNING, "Exception $e")
            return false
        }
    }

    override val encoded: ByteArray?
        /**
         * Encodes this image info.
         * 
         * @return a byte array containing the encoded image info
         */
        get() {
            val out = ByteArrayOutputStream()
            try {
                writeObject(out)
            } catch (ioe: IOException) {
                LOGGER.log(Level.WARNING, "Exception", ioe)
                return null
            }
            return out.toByteArray()
        }

    override val imageInputStream: InputStream
        /**
         * Returns the encoded image as an input stream.
         * 
         * @return an input stream containing the encoded image
         */
        get() {
            /* DEBUG: START */
            return if (splittableInputStream != null) {
                splittableInputStream!!.getInputStream(imagePositionInInputStream)
                /* DEBUG: END */
            } else if (imageBytes != null) {
                ByteArrayInputStream(imageBytes)
            } else {
                throw IllegalStateException("Both the byte buffer and the stream are null")
            }
        }

    /**
     * Clients should call this method after positioning the input stream to the
     * image bytes.
     * 
     * @param inputStream input stream
     * @param imageLength image length
     * 
     * @throws IOException on error reading the input stream, for example at EOF
     */
    @Throws(IOException::class)
    protected fun readImage(inputStream: InputStream, imageLength: Long) {
        /* DEBUG: START */
        //    if (inputStream instanceof SplittableInputStream) {
        //      this.imageBytes = null;
        //      this.splittableInputStream = (SplittableInputStream)inputStream;
        //      this.imagePositionInInputStream = splittableInputStream.getPosition();
        //
        //      this.imageLength = (int)imageLength;
        //      long totalSkippedBytes = 0;
        //      while (totalSkippedBytes < imageLength) {
        //        long currentlySkippedBytes = splittableInputStream.skip(imageLength - totalSkippedBytes);
        //        totalSkippedBytes += currentlySkippedBytes;
        //      }
        //    } else {
        /* DEBUG: END */
        this.splittableInputStream = null
        this.imageBytes = ByteArray(imageLength.toInt())
        val dataIn = DataInputStream(inputStream)
        dataIn.readFully(this.imageBytes)
        //    }
    }

    /**
     * Writes this image to a stream.
     * 
     * @param outputStream the stream to write to
     * 
     * @throws IOException on error writing to the stream
     */
    @Throws(IOException::class)
    protected fun writeImage(outputStream: OutputStream) {
        outputStream.write(getImageBytes())
    }

    /**
     * Sets the encoded image bytes of this image.
     * 
     * @param imageBytes the image bytes
     */
    protected fun setImageBytes(imageBytes: ByteArray) {
        requireNotNull(imageBytes) { "Cannot set null image bytes" }

        try {
            readImage(ByteArrayInputStream(imageBytes), imageBytes.size.toLong())
        } catch (e: IOException) {
            LOGGER.log(Level.WARNING, "Exception", e)
        }
    }

    /**
     * Reads this object from a stream.
     * 
     * @param inputStream the stream to read from
     * 
     * @throws IOException on error reading from the stream
     */
    @Throws(IOException::class)
    protected abstract fun readObject(inputStream: InputStream?)

    /**
     * Writes this object to a stream.
     * 
     * @param outputStream the stream to write to
     * 
     * @throws IOException on error writing to the stream
     */
    @Throws(IOException::class)
    protected abstract fun writeObject(outputStream: OutputStream?)

    /* ONLY PRIVATE METHODS BELOW */
    /**
     * Reads the image bytes from the stream.
     * 
     * @return the image bytes
     * 
     * @throws IOException on error reading from the stream
     */
    @Throws(IOException::class)
    private fun getImageBytes(): ByteArray {
        var inputStream: InputStream? = null
        val length = imageLength
        val imageBytes = ByteArray(length)
        inputStream = imageInputStream
        val imageInputStream = DataInputStream(inputStream)
        imageInputStream.readFully(imageBytes)
        return imageBytes
    }

    companion object {
        private const val serialVersionUID = 2870092217269116309L

        private val LOGGER: Logger = Logger.getLogger("kmrtd")

        /**
         * Returns a human readable string from the image type.
         * 
         * @param type the image type
         * 
         * @return a human readable string
         */
        private fun typeToString(type: Int): String {
            when (type) {
                ImageInfo.TYPE_PORTRAIT -> return "Portrait"
                ImageInfo.TYPE_SIGNATURE_OR_MARK -> return "Signature or usual mark"
                ImageInfo.TYPE_FINGER -> return "Finger"
                ImageInfo.TYPE_IRIS -> return "Iris"
                ImageInfo.TYPE_UNKNOWN -> return "Unknown"
                else -> throw NumberFormatException("Unknown type: " + Integer.toHexString(type))
            }
        }
    }
}
