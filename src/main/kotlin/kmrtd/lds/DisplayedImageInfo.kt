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
 * $Id: DisplayedImageInfo.java 1766 2018-02-20 11:33:20Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.lds

import net.sf.scuba.tlv.TLVInputStream
import net.sf.scuba.tlv.TLVOutputStream
import net.sf.scuba.tlv.TLVUtil
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * Data structure for storing either a *Portrait* (as used in DG5) or
 * a *Signature or mark* (as used in DG7).
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1766 $
 */
class DisplayedImageInfo : AbstractImageInfo {
    /**
     * Returns the displayed image tag.
     * Either [.DISPLAYED_PORTRAIT_TAG] or [.DISPLAYED_SIGNATURE_OR_MARK_TAG],
     * depending on the type of image.
     * 
     * @return the displayed image tag
     */
    var displayedImageTag: Int = 0
        private set

    /**
     * Constructs a displayed image info from the image bytes.
     * 
     * @param type one of [ImageInfo.TYPE_PORTRAIT] or [ImageInfo.TYPE_SIGNATURE_OR_MARK]
     * @param imageBytes encoded image, for *Portrait* and *Signature or mark* use JPEG encoding
     */
    constructor(type: Int, imageBytes: ByteArray?) : super(type, getMimeTypeFromType(type)) {
        displayedImageTag = getDisplayedImageTagFromType(type)
        setImageBytes(imageBytes)
    }

    /**
     * Constructs a displayed image info from binary encoding.
     * 
     * @param in an input stream
     * 
     * @throws IOException if decoding fails
     */
    constructor(`in`: InputStream?) {
        readObject(`in`)
    }

    /**
     * Reads the displayed image. This method should be implemented by concrete
     * subclasses. The 5F2E or 7F2E tag and the length are already read.
     * 
     * @param inputStream the input stream positioned so that biometric data block tag and length are already read
     * 
     * @throws IOException if reading fails
     */
    @Throws(IOException::class)
    override fun readObject(inputStream: InputStream?) {
        val tlvIn = inputStream as? TLVInputStream ?: TLVInputStream(inputStream)

        displayedImageTag = tlvIn.readTag()
        require(
            !(displayedImageTag != DISPLAYED_PORTRAIT_TAG /* 5F40 */
                    && displayedImageTag != DISPLAYED_SIGNATURE_OR_MARK_TAG /* 5F43 */)
        ) { "Expected tag 0x5F40 or 0x5F43, found " + Integer.toHexString(displayedImageTag) }

        val type: Int = getTypeFromDisplayedImageTag(displayedImageTag)
        type = type
        mimeType = getMimeTypeFromType(type)

        val imageLength = tlvIn.readLength().toLong()

        readImage(tlvIn, imageLength)
    }

    @Throws(IOException::class)
    public override fun writeObject(outputStream: OutputStream?) {
        val tlvOut = outputStream as? TLVOutputStream ?: TLVOutputStream(outputStream)
        tlvOut.writeTag(getDisplayedImageTagFromType(type))
        writeImage(tlvOut)
        tlvOut.writeValueEnd()
    }

    override val recordLength: Long
        /**
         * Returns the record length of the encoded image info.
         * 
         * @return the record length of the encoded image info
         */
        get() {
            var length: Long = 0
            val imageLength = imageLength
            length += TLVUtil.getTagLength(getDisplayedImageTagFromType(type))
                .toLong()
            length += TLVUtil.getLengthLength(imageLength).toLong()
            length += imageLength.toLong()
            return length
        }

    override fun hashCode(): Int {
        val prime = 31
        var result = super.hashCode()
        result = prime * result + displayedImageTag
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

        val other = obj as DisplayedImageInfo
        return displayedImageTag == other.displayedImageTag
    }

    companion object {
        private const val serialVersionUID = 3801320585294302721L

        const val DISPLAYED_PORTRAIT_TAG: Int = 0x5F40

        const val DISPLAYED_SIGNATURE_OR_MARK_TAG: Int = 0x5F43

        /* ONLY PRIVATE METHODS BELOW */
        /**
         * As per A1.11.4 in Doc 9303 Part 3 Vol 2:
         * 
         * 
         *  * Displayed Facial Image: ISO 10918, JFIF option.
         *  * Displayed Finger: ANSI/NIST-ITL 1-2000.
         *  * Displayed Signature/ usual mark: ISO 10918, JFIF option.
         * 
         * 
         * @param type the type
         * 
         * @return the mime-type
         */
        private fun getMimeTypeFromType(type: Int): String {
            when (type) {
                ImageInfo.Companion.TYPE_PORTRAIT -> return "image/jpeg"
                ImageInfo.Companion.TYPE_FINGER -> return "image/x-wsq"
                ImageInfo.Companion.TYPE_SIGNATURE_OR_MARK -> return "image/jpeg"
                else -> throw NumberFormatException("Unknown type: " + Integer.toHexString(type))
            }
        }

        /**
         * Derives the displayed image info tag from the image type.
         * 
         * @param type the image type, either [.TYPE_PORTRAIT] or [.TYPE_SIGNATURE_OR_MARK]
         * 
         * @return the corresponding image info tag
         */
        private fun getDisplayedImageTagFromType(type: Int): Int {
            when (type) {
                ImageInfo.Companion.TYPE_PORTRAIT -> return DISPLAYED_PORTRAIT_TAG
                ImageInfo.Companion.TYPE_SIGNATURE_OR_MARK -> return DISPLAYED_SIGNATURE_OR_MARK_TAG
                else -> throw NumberFormatException("Unknown type: " + Integer.toHexString(type))
            }
        }

        /**
         * Derives the image info type from the given tag.
         * 
         * @param tag a tag, either [.DISPLAYED_PORTRAIT_TAG] or [.DISPLAYED_SIGNATURE_OR_MARK_TAG]
         * 
         * @return the corresponding image info type
         */
        private fun getTypeFromDisplayedImageTag(tag: Int): Int {
            return when (tag) {
                DISPLAYED_PORTRAIT_TAG -> ImageInfo.TYPE_PORTRAIT
                DISPLAYED_SIGNATURE_OR_MARK_TAG -> ImageInfo.TYPE_SIGNATURE_OR_MARK
                else -> throw NumberFormatException("Unknown tag: " + Integer.toHexString(tag))
            }
        }
    }
}
