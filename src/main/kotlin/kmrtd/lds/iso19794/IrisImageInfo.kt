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
 * $Id: IrisImageInfo.java 1799 2018-10-30 16:25:48Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.lds.iso19794

import kmrtd.lds.AbstractImageInfo
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * Iris image header and image data
 * based on Section 6.5.3 and Table 4 of
 * ISO/IEC 19794-6 2005.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * @version $Revision: 1799 $
 */
class IrisImageInfo : AbstractImageInfo {
    /**
     * Returns the image format.
     * 
     * @return the image format
     */
    /**
     * The imageFormat (is more precise than mimeType). Constants are in [IrisInfo].
     */
    var imageFormat: Int = 0
        private set

    /**
     * Returns the image number.
     * 
     * @return the image number
     */
    var imageNumber: Int = 0
        private set

    /**
     * Returns the quality.
     * 
     * @return the image quality
     */
    var quality: Int = 0
        private set

    /**
     * Returns the rotation angle.
     * 
     * @return the rotationAngle
     */
    // TODO: rotation angle of eye and rotation uncertainty as angles, instead of encoded.
    var rotationAngle: Int = 0
        private set

    /**
     * Returns the rotation angle uncertainty.
     * 
     * @return the rotationAngleUncertainty
     */
    var rotationAngleUncertainty: Int = 0
        private set

    /**
     * Constructs an iris image info.
     * 
     * @param imageNumber              the image number
     * @param quality                  quality
     * @param rotationAngle            rotation angle
     * @param rotationAngleUncertainty rotation angle uncertainty
     * @param width                    with
     * @param height                   height
     * @param imageBytes               the encoded image
     * @param imageLength              the length of the encoded image
     * @param imageFormat              the image format used for encoding
     * @throws IOException on error reading the image input stream
     */
    constructor(
        imageNumber: Int,
        quality: Int,
        rotationAngle: Int,
        rotationAngleUncertainty: Int,
        width: Int,
        height: Int,
        imageBytes: InputStream,
        imageLength: Int,
        imageFormat: Int
    ) : super(
        TYPE_IRIS,
        width,
        height,
        imageBytes,
        imageLength.toLong(),
        ISO19794.ImageQuality.getMimeTypeFromImageFormat(imageFormat)
    ) {
        requireNotNull(imageBytes) { "Null image bytes" }
        this.imageNumber = imageNumber
        this.quality = quality
        this.rotationAngle = rotationAngle
        this.rotationAngleUncertainty = rotationAngleUncertainty
    }

    /**
     * Constructs an iris image info.
     * 
     * @param imageNumber the image number
     * @param width       width
     * @param height      height
     * @param imageBytes  the encoded image
     * @param imageLength the length of the encoded image
     * @param imageFormat the image format used for encoding
     * @throws IOException on error reading the image stream
     */
    constructor(
        imageNumber: Int,
        width: Int,
        height: Int,
        imageBytes: InputStream,
        imageLength: Int,
        imageFormat: Int
    ) : this(
        imageNumber,
        ISO19794.ImageQuality.IMAGE_QUAL_UNDEF,
        ISO19794.ImageQuality.ROT_ANGLE_UNDEF,
        ISO19794.ImageQuality.ROT_UNCERTAIN_UNDEF,
        width, height, imageBytes, imageLength, imageFormat
    )

    /**
     * Constructs a new iris image record.
     * 
     * @param inputStream input stream
     * @param imageFormat the image format used for encoding
     * @throws IOException if input cannot be read
     */
    internal constructor(inputStream: InputStream, imageFormat: Int) : super(TYPE_IRIS) {
        this.imageFormat = imageFormat
        mimeType = ISO19794.ImageQuality.getMimeTypeFromImageFormat(imageFormat)
        readObject(inputStream)
    }

    /**
     * Returns the record length.
     * 
     * @return the record length
     */
    override fun getRecordLength(): Long {
        return 11L + getImageLength()
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = super.hashCode()
        result = prime * result + imageFormat
        result = prime * result + imageNumber
        result = prime * result + quality
        result = prime * result + rotationAngle
        result = prime * result + rotationAngleUncertainty
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

        val other = obj as IrisImageInfo
        return imageFormat == other.imageFormat && imageNumber == other.imageNumber && quality == other.quality && rotationAngle == other.rotationAngle && rotationAngleUncertainty == other.rotationAngleUncertainty
    }

    /**
     * Generates a textual representation of this object.
     * 
     * @return a textual representation of this object
     * @see Object.toString
     */
    override fun toString(): String {
        return "IrisImageInfo [" +
                "image number: " + imageNumber + ", " +
                "quality: " + quality + ", " +
                "image: " +
                width + " x " + height +
                "mime-type: " + ISO19794.ImageQuality.getMimeTypeFromImageFormat(imageFormat) +
                "]"
    }

    @Throws(IOException::class)
    override fun readObject(inputStream: InputStream) {
        val dataIn =
            inputStream as? DataInputStream ?: DataInputStream(inputStream)

        this.imageNumber = dataIn.readUnsignedShort() /* 2 */
        this.quality = dataIn.readUnsignedByte() /* + 1 = 3 */

        /*
         * (65536*angle/360) modulo 65536
         * ROT_ANGLE_UNDEF = 0xFFFF
         * Where angle is measured in degrees from
         * horizontal
         * Used only for rectilinear images. For polar images
         * entry shall be ROT_ANGLE_UNDEF
         */
        rotationAngle = dataIn.readShort().toInt() /* + 2 + 5 */

        /*
         * Rotation uncertainty = (unsigned short) round
         * (65536 * uncertainty/180)
         * Where 0 <= uncertainty < 180
         * ROT_UNCERTAIN_UNDEF = 0xFFFF
         * Where uncertainty is measured in degrees and is
         * the absolute value of maximum error
         */
        rotationAngleUncertainty = dataIn.readUnsignedShort() /* + 2 = 7 */

        /*
         * Size of image data, bytes, 0 - 4294967295.
         */
        val imageLength = dataIn.readInt().toLong() and 0x00000000FFFFFFFFL /* + 4 = 11 */

        readImage(inputStream, imageLength)
    }

    @Throws(IOException::class)
    public override fun writeObject(out: OutputStream?) {
        val dataOut = out as? DataOutputStream ?: DataOutputStream(out)

        dataOut.writeShort(this.imageNumber) /* 2 */
        dataOut.writeByte(this.quality) /* + 1 = 3 */

        /*
         * (65536*angle/360) modulo 65536
         * ROT_ANGLE_UNDEF = 0xFFFF
         * Where angle is measured in degrees from
         * horizontal
         * Used only for rectilinear images. For polar images
         * entry shall be ROT_ANGLE_UNDEF
         */
        dataOut.writeShort(rotationAngle) /* + 2 = 5 */

        /*
         * Rotation uncertainty = (unsigned short) round
         * (65536 * uncertainty/180)
         * Where 0 <= uncertainty < 180
         * ROT_UNCERTAIN_UNDEF = 0xFFFF
         * Where uncertainty is measured in degrees and is
         * the absolute value of maximum error
         */
        dataOut.writeShort(rotationAngleUncertainty) /* + 2 = 7 */

        dataOut.writeInt(getImageLength()) /* + 4 = 11 */
        writeImage(dataOut)
    }
}
