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
 * $Id: DisplayedImageDataGroup.java 1808 2019-03-07 21:32:19Z martijno $
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
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.logging.Logger

/**
 * File structure image template files that can be displayed.
 * Abstract super class for ICAO LDS EF_DG5 - EF_DG7.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1808 $
 */
abstract class DisplayedImageDataGroup : DataGroup {
    private var displayedImageTagToUse = 0
    private var imageInfos: MutableList<DisplayedImageInfo>? = null

    /**
     * Constructs a displayed image data group from a list of displayed images.
     * The list should not be `null` or contain `null` valued displayed images.
     * 
     * @param dataGroupTag a tag indicating DG5, DG6, or DG7
     * @param imageInfos a list of displayed images
     * @param displayedImageTagToUse a tag indicating *Portrait* or *Signature or mark*
     */
    constructor(dataGroupTag: Int, imageInfos: MutableList<DisplayedImageInfo>, displayedImageTagToUse: Int) : super(
        dataGroupTag
    ) {
        requireNotNull(imageInfos) { "imageInfos cannot be null" }
        this.displayedImageTagToUse = displayedImageTagToUse
        this.imageInfos = ArrayList<DisplayedImageInfo>(imageInfos)
        checkTypesConsistentWithTag()
    }

    /**
     * Constructs a displayed image data group from binary representation.
     * 
     * @param dataGroupTag a tag indicating DG5, DG6, or DG7
     * @param inputStream an input stream
     * 
     * @throws IOException on error reading the input stream
     */
    constructor(dataGroupTag: Int, inputStream: InputStream?) : super(dataGroupTag, inputStream) {
        if (this.imageInfos == null) {
            this.imageInfos = ArrayList<DisplayedImageInfo>()
        }
        checkTypesConsistentWithTag()
    }

    @Throws(IOException::class)
    override fun readContent(inputStream: InputStream?) {
        val tlvIn = if (inputStream is TLVInputStream) inputStream else TLVInputStream(inputStream)
        val countTag = tlvIn.readTag()
        require(countTag == DISPLAYED_IMAGE_COUNT_TAG) {
            "Expected tag 0x02 in displayed image structure, found " + Integer.toHexString(
                countTag
            )
        }
        val countLength = tlvIn.readLength()
        require(countLength == 1) { "DISPLAYED_IMAGE_COUNT should have length 1" }
        val count = (tlvIn.readValue()[0].toInt() and 0xFF)
        for (i in 0..<count) {
            val imageInfo = DisplayedImageInfo(tlvIn)
            if (i == 0) {
                displayedImageTagToUse = imageInfo.displayedImageTag
            } else if (imageInfo.displayedImageTag != displayedImageTagToUse) {
                throw IOException("Found images with different displayed image tags inside displayed image datagroup")
            }
            add(imageInfo)
        }
    }

    /**
     * Writes the contents of this structure to a stream.
     * 
     * @param outputStream the stream to write to
     * 
     * @throws IOException on error writing to the stream
     */
    @Throws(IOException::class)
    override fun writeContent(outputStream: OutputStream?) {
        val tlvOut = if (outputStream is TLVOutputStream) outputStream else TLVOutputStream(outputStream)
        tlvOut.writeTag(DISPLAYED_IMAGE_COUNT_TAG)
        tlvOut.writeValue(byteArrayOf(imageInfos!!.size.toByte()))
        for (imageInfo in imageInfos!!) {
            imageInfo.writeObject(tlvOut)
        }
    }

    public override fun toString(): String {
        val result = StringBuilder()
        result.append(javaClass.getSimpleName())
        result.append(" [")
        var isFirst = true
        checkNotNull(imageInfos) { "imageInfos cannot be null" }
        for (info in imageInfos) {
            if (isFirst) {
                isFirst = false
            } else {
                result.append(", ")
            }
            result.append(info.toString())
        }
        result.append("]")
        return result.toString()
    }

    override fun hashCode(): Int {
        return 1337 + (if (imageInfos == null) 1 else imageInfos.hashCode()) + 31337
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        if (other === this) {
            return true
        }
        if (javaClass != other.javaClass) {
            return false
        }

        val otherDG = other as DisplayedImageDataGroup
        return this.imageInfos === otherDG.imageInfos || this.imageInfos != null && this.imageInfos == otherDG.imageInfos
    }

    val images: MutableList<DisplayedImageInfo?>
        /**
         * Returns the image infos.
         * 
         * @return images
         */
        get() = ArrayList<DisplayedImageInfo?>(imageInfos)

    /**
     * Adds an image info to this data group.
     * 
     * @param image the image to add
     */
    private fun add(image: DisplayedImageInfo?) {
        if (imageInfos == null) {
            imageInfos = ArrayList<DisplayedImageInfo>()
        }
        imageInfos!!.add(image!!)
    }

    /**
     * Checks whether the type of image infos is consistent with the type
     * and throws an `IllegalArgumentException` if not.
     */
    private fun checkTypesConsistentWithTag() {
        for (imageInfo in imageInfos!!) {
            requireNotNull(imageInfo) { "Found a null image info" }
            when (imageInfo.type) {
                ImageInfo.TYPE_SIGNATURE_OR_MARK -> require(displayedImageTagToUse == DisplayedImageInfo.DISPLAYED_SIGNATURE_OR_MARK_TAG) { "\'Portrait\' image cannot be part of a \'Signature or usual mark\' displayed image datagroup" }
                ImageInfo.TYPE_PORTRAIT -> require(displayedImageTagToUse == DisplayedImageInfo.DISPLAYED_PORTRAIT_TAG) { "\'Signature or usual mark\' image cannot be part of a \'Portrait\' displayed image datagroup" }
                else -> LOGGER.warning("Unsupported image type")
            }
        }
    }

    companion object {
        private const val serialVersionUID = 5994136177872308962L

        private val LOGGER: Logger = Logger.getLogger("org.jmrtd")

        private const val DISPLAYED_IMAGE_COUNT_TAG = 0x02
    }
}
