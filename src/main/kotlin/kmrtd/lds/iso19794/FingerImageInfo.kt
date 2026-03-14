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
 * $Id: FingerImageInfo.java 1808 2019-03-07 21:32:19Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.lds.iso19794

import kmrtd.cbeff.CBEFFInfoConstants
import kmrtd.lds.AbstractImageInfo
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * Data structure for storing view of a single finger
 * image, multi-finger image, or palm. This represents a
 * finger image record header as specified in Section 7.2
 * of ISO/IEC FCD 19794-4 aka Annex F.
 * 
 * 
 * TODO: proper enums for data types
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * @version $Revision: 1808 $
 */
class FingerImageInfo : AbstractImageInfo {
    private var recordLength: Long = 0

    /**
     * Returns the finger/palm position. As specified in Section 7.2.2 of ISO 19794-4.
     * 
     * @return a constant representing the position (see constant definitions starting with `POSITION_`)
     */
    var position: Int = 0
        private set

    /**
     * Returns the total number of specific views available for this finger.
     * As specified in Section 7.2.3 of ISO 19794-4.
     * 
     * @return the total number of specific views available for this finger
     */
    var viewCount: Int = 0
        private set

    /**
     * Returns the specific image view number associated with the finger.
     * As specified in Section 7.2.4 of ISO 19794-4.
     * 
     * @return the specific image view number associated with the finger
     */
    var viewNumber: Int = 0
        private set

    /**
     * Returns the quality of the overall scanned finger/palm image as a number
     * between 0 and 100. As specified in 7.2.5 of ISO 19794-4.
     * 
     * @return the quality of the overall scanned finger/palm image as a number between 0 and 100
     */
    var quality: Int = 0
        private set

    /**
     * Returns the impression type. As specified in Section 7.2.6 of ISO 19794-4.
     * 
     * @return a constant indicating the impression type (see constant definitions starting with `IMPRESSION_TYPE_`)
     */
    var impressionType: Int = 0
        private set

    /**
     * Returns the compression algorithm. One of
     * [FingerInfo.COMPRESSION_UNCOMPRESSED_BIT_PACKED],
     * [FingerInfo.COMPRESSION_UNCOMPRESSED_NO_BIT_PACKING],
     * [FingerInfo.COMPRESSION_JPEG],
     * [FingerInfo.COMPRESSION_JPEG2000],
     * [FingerInfo.COMPRESSION_PNG],
     * [FingerInfo.COMPRESSION_WSQ].
     * As specified in Section 7.1.13 of ISO 19794-4.
     * 
     * @return a constant representing the used image compression algorithm
     */
    var compressionAlgorithm: Int
        private set

    /**
     * Constructs a finger image info.
     * 
     * @param position             finger position according to ISO 19794-4
     * @param viewCount            number of views
     * @param viewNumber           the view number
     * @param quality              quality
     * @param impressionType       impression type accordign to ISO 19794-4
     * @param width                width
     * @param height               height
     * @param imageBytes           encoded image bytes
     * @param imageLength          length of encoded image
     * @param compressionAlgorithm image encoding type according to ISO 19794-4
     * @throws IOException if input cannot be read
     */
    constructor(
        position: Int,
        viewCount: Int,
        viewNumber: Int,
        quality: Int,
        impressionType: Int,
        width: Int,
        height: Int,
        imageBytes: InputStream,
        imageLength: Int,
        compressionAlgorithm: Int
    ) : super(
        TYPE_FINGER,
        width,
        height,
        imageBytes,
        imageLength.toLong(),
        FingerInfo.Companion.toMimeType(compressionAlgorithm)
    ) {
        require(!(0 > quality || quality > 100)) { "Quality needs to be a number between 0 and 100" }
        requireNotNull(imageBytes) { "Null image" }
        this.position = position
        this.viewCount = viewCount
        this.viewNumber = viewNumber
        this.quality = quality
        this.impressionType = impressionType
        this.compressionAlgorithm = compressionAlgorithm
        this.recordLength = imageLength + 14L
    }

    /**
     * Constructs a new finger information record.
     * 
     * @param inputStream          input stream
     * @param compressionAlgorithm image format type (which is given in the general record header, not for each individual image)
     * @throws IOException if input cannot be read
     */
    constructor(inputStream: InputStream, compressionAlgorithm: Int) : super(
        TYPE_FINGER,
        FingerInfo.Companion.toMimeType(compressionAlgorithm)
    ) {
        this.compressionAlgorithm = compressionAlgorithm
        this.compressionAlgorithm = compressionAlgorithm
        readObject(inputStream)
    }

    @Throws(IOException::class)
    override fun readObject(inputStream: InputStream) {
        val dataIn =
            inputStream as? DataInputStream ?: DataInputStream(inputStream)

        /* Finger image header (14), see Table 4, 7.2 in Annex F. */
        /* NOTE: sometimes called "finger header", "finger record header" */
        this.recordLength = dataIn.readInt().toLong() and 0xFFFFFFFFL
        this.position = dataIn.readUnsignedByte()
        this.viewCount = dataIn.readUnsignedByte()
        this.viewNumber = dataIn.readUnsignedByte()
        this.quality = dataIn.readUnsignedByte()
        this.impressionType = dataIn.readUnsignedByte()
        width = dataIn.readUnsignedShort()
        height = dataIn.readUnsignedShort()
        /* int RFU = */
        dataIn.readUnsignedByte() /* Should be 0x0000 */

        val imageLength = recordLength - 14

        readImage(inputStream, imageLength)
    }

    /**
     * Writes the biometric data to `out`.
     * 
     * 
     * Based on Table 4 in Section 8.3 of ISO/IEC FCD 19794-4.
     * 
     * @param out stream to write to
     * @throws IOException if writing to out fails
     */
    @Throws(IOException::class)
    public override fun writeObject(out: OutputStream?) {
        val imageOut = ByteArrayOutputStream()
        writeImage(imageOut)
        imageOut.flush()
        val imageBytes = imageOut.toByteArray()
        imageOut.close()

        val fingerDataBlockLength = imageBytes.size + 14L

        val dataOut = if (out is DataOutputStream) out else DataOutputStream(out)

        /* Finger Information (14) */
        dataOut.writeInt((fingerDataBlockLength and 0xFFFFFFFFL).toInt())
        dataOut.writeByte(position)
        dataOut.writeByte(viewCount)
        dataOut.writeByte(viewNumber)
        dataOut.writeByte(quality)
        dataOut.writeByte(impressionType)
        dataOut.writeShort(width)
        dataOut.writeShort(height)
        dataOut.writeByte(0x00) /* RFU */

        dataOut.write(imageBytes)
        dataOut.flush()
    }

    /**
     * Returns the record length.
     * 
     * @return the record length
     */
    override fun getRecordLength(): Long {
        /* Should be equal to (getImageLength() + 14) */
        return recordLength
    }

    val biometricSubtype: Int
        /**
         * Returns the biometric sub-type.
         * 
         * @return the ICAO/CBEFF (BHT) biometric sub-type
         */
        get() = toBiometricSubtype(position)

    override fun hashCode(): Int {
        val prime = 31
        var result = super.hashCode()
        result = prime * result + compressionAlgorithm
        result = prime * result + impressionType
        result = prime * result + position
        result = prime * result + quality
        result = prime * result + (recordLength xor (recordLength ushr 32)).toInt()
        result = prime * result + viewCount
        result = prime * result + viewNumber
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

        val other = obj as FingerImageInfo
        return compressionAlgorithm == other.compressionAlgorithm &&
                impressionType == other.impressionType &&
                position == other.position &&
                quality == other.quality &&
                recordLength == other.recordLength &&
                viewCount == other.viewCount &&
                viewNumber == other.viewNumber
    }

    /**
     * Generates a textual representation of this object.
     * 
     * @return a textual representation of this object
     * @see Object.toString
     */
    override fun toString(): String {
        return StringBuilder()
            .append("FingerImageInfo [")
            .append("quality: ").append(quality).append(", ")
            .append("position: ").append(positionToString(position)).append(", ")
            .append("impression type: ").append(impressionTypeToString(impressionType)).append(", ")
            .append("horizontal line length: ").append(width).append(", ")
            .append("vertical line length: ").append(height).append(", ")
            .append("image: ").append(width).append(" x ").append(height)
            .append(" \"").append(FingerInfo.Companion.toMimeType(compressionAlgorithm))
            .append("\"")
            .append("]")
            .toString()
    }

    companion object {
        /**
         * Finger code, according to Table 5, 7.2.2, ISO 19794-4.
         */
        const val POSITION_UNKNOWN_FINGER: Int = 0 /* 1745 40.6 1.6 38.1 1.5 */

        /* NOTE: in comment: max image area in sq mm, sq in, with in mm, in, length in mm, in */
        /**
         * Finger code, according to Table 5, 7.2.2, ISO 19794-4.
         */
        const val POSITION_RIGHT_THUMB: Int = 1 /* 1745 40.6 1.6 38.1 1.5 */

        /**
         * Finger code, according to Table 5, 7.2.2, ISO 19794-4.
         */
        const val POSITION_RIGHT_INDEX_FINGER: Int = 2 /* 1640 40.6 1.6 38.1 1.5 */

        /**
         * Finger code, according to Table 5, 7.2.2, ISO 19794-4.
         */
        const val POSITION_RIGHT_MIDDLE_FINGER: Int = 3 /* 1640 40.6 1.6 38.1 1.5 */

        /**
         * Finger code, according to Table 5, 7.2.2, ISO 19794-4.
         */
        const val POSITION_RIGHT_RING_FINGER: Int = 4 /* 1640 40.6 1.6 38.1 1.5 */

        /**
         * Finger code, according to Table 5, 7.2.2, ISO 19794-4.
         */
        const val POSITION_RIGHT_LITTLE_FINGER: Int = 5 /* 1640 40.6 1.6 38.1 1.5 */

        /**
         * Finger code, according to Table 5, 7.2.2, ISO 19794-4.
         */
        const val POSITION_LEFT_THUMB: Int = 6 /* 1745 40.6 1.6 38.1 1.5 */

        /**
         * Finger code, according to Table 5, 7.2.2, ISO 19794-4.
         */
        const val POSITION_LEFT_INDEX_FINGER: Int = 7 /* 1640 40.6 1.6 38.1 1.5 */

        /**
         * Finger code, according to Table 5, 7.2.2, ISO 19794-4.
         */
        const val POSITION_LEFT_MIDDLE_FINGER: Int = 8 /* 1640 40.6 1.6 38.1 1.5 */

        /**
         * Finger code, according to Table 5, 7.2.2, ISO 19794-4.
         */
        const val POSITION_LEFT_RING_FINGER: Int = 9 /* 1640 40.6 1.6 38.1 1.5 */

        /**
         * Finger code, according to Table 5, 7.2.2, ISO 19794-4.
         */
        const val POSITION_LEFT_LITTLE_FINGER: Int = 10 /* 1640 40.6 1.6 38.1 1.5 */

        /**
         * Finger code, according to Table 5, 7.2.2, ISO 19794-4.
         */
        const val POSITION_PLAIN_RIGHT_FOUR_FINGERS: Int = 13 /* 6800 83.8 3.3 76.2 3.0 */

        /**
         * Finger code, according to Table 5, 7.2.2, ISO 19794-4.
         */
        const val POSITION_PLAIN_LEFT_FOUR_FINGERS: Int = 14 /* 6800 83.8 3.3 76.2 3.0 */

        /**
         * Finger code, according to Table 5, 7.2.2, ISO 19794-4.
         */
        const val POSITION_PLAIN_THUMBS: Int = 15 /* 4800 50.8 2.0 76.2 3.0 */

        /**
         * Palm code, according to Table 6, 7.2.2, ISO 19794-4.
         */
        /* NOTE: in comment: max image area in sq mm, sq in, with in mm, in, length in mm, in */
        const val POSITION_UNKNOWN_PALM: Int = 20 /* 283.87 13.97 5.5 20.32 8.0 */

        /**
         * Palm code, according to Table 6, 7.2.2, ISO 19794-4.
         */
        const val POSITION_RIGHT_FULL_PALM: Int = 21 /* 283.87 13.97 5.5 20.32 8.0 */

        /**
         * Palm code, according to Table 6, 7.2.2, ISO 19794-4.
         */
        const val POSITION_RIGHT_WRITER_S_PALM: Int = 22 /* 58.06 4.57 1.8 12.70 5.0 */

        /**
         * Palm code, according to Table 6, 7.2.2, ISO 19794-4.
         */
        const val POSITION_LEFT_FULL_PALM: Int = 23 /* 283.87 13.97 5.5 20.32 8.0 */

        /**
         * Palm code, according to Table 6, 7.2.2, ISO 19794-4.
         */
        const val POSITION_LEFT_WRITER_S_PALM: Int = 24 /* 58.06 4.57 1.8 12.70 5.0 */

        /**
         * Palm code, according to Table 6, 7.2.2, ISO 19794-4.
         */
        const val POSITION_RIGHT_LOWER_PALM: Int = 25 /* 195.16 13.97 5.5 13.97 5.5 */

        /**
         * Palm code, according to Table 6, 7.2.2, ISO 19794-4.
         */
        const val POSITION_RIGHT_UPPER_PALM: Int = 26 /* 195.16 13.97 5.5 13.97 5.5 */

        /**
         * Palm code, according to Table 6, 7.2.2, ISO 19794-4.
         */
        const val POSITION_LEFT_LOWER_PALM: Int = 27 /* 195.16 13.97 5.5 13.97 5.5 */

        /**
         * Palm code, according to Table 6, 7.2.2, ISO 19794-4.
         */
        const val POSITION_LEFT_UPPER_PALM: Int = 28 /* 195.16 13.97 5.5 13.97 5.5 */

        /**
         * Palm code, according to Table 6, 7.2.2, ISO 19794-4.
         */
        const val POSITION_RIGHT_OTHER: Int = 29 /* 283.87 13.97 5.5 20.32 8.0 */

        /**
         * Palm code, according to Table 6, 7.2.2, ISO 19794-4.
         */
        const val POSITION_LEFT_OTHER: Int = 30 /* 283.87 13.97 5.5 20.32 8.0 */

        /**
         * Palm code, according to Table 6, 7.2.2, ISO 19794-4.
         */
        const val POSITION_RIGHT_INTERDIGITAL: Int = 31 /* 106.45 13.97 5.5 7.62 3.0 */

        /**
         * Palm code, according to Table 6, 7.2.2, ISO 19794-4.
         */
        const val POSITION_RIGHT_THENAR: Int = 32 /* 77.42 7.62 3.0 10.16 4.0 */

        /**
         * Palm code, according to Table 6, 7.2.2, ISO 19794-4.
         */
        const val POSITION_RIGHT_HYPOTHENAR: Int = 33 /* 106.45 7.62 3.0 13.97 5.5 */

        /**
         * Palm code, according to Table 6, 7.2.2, ISO 19794-4.
         */
        const val POSITION_LEFT_INTERDIGITAL: Int = 34 /* 106.45 13.97 5.5 7.62 3.0 */

        /**
         * Palm code, according to Table 6, 7.2.2, ISO 19794-4.
         */
        const val POSITION_LEFT_THENAR: Int = 35 /* 77.42 7.62 3.0 10.16 4.0 */

        /**
         * Palm code, according to Table 6, 7.2.2, ISO 19794-4.
         */
        const val POSITION_LEFT_HYPOTHENAR: Int = 36 /* 106.45 7.62 3.0 13.97 5.5 */

        /**
         * Finger or palm impression type, according to Table 7 in ISO 19794-4.
         */
        const val IMPRESSION_TYPE_LIVE_SCAN_PLAIN: Int = 0

        /**
         * Finger or palm impression type, according to Table 7 in ISO 19794-4.
         */
        const val IMPRESSION_TYPE_LIVE_SCAN_ROLLED: Int = 1

        /**
         * Finger or palm impression type, according to Table 7 in ISO 19794-4.
         */
        const val IMPRESSION_TYPE_NON_LIVE_SCAN_PLAIN: Int = 2

        /**
         * Finger or palm impression type, according to Table 7 in ISO 19794-4.
         */
        const val IMPRESSION_TYPE_NON_LIVE_SCAN_ROLLED: Int = 3

        /**
         * Finger or palm impression type, according to Table 7 in ISO 19794-4.
         */
        const val IMPRESSION_TYPE_LATENT: Int = 7

        /**
         * Finger or palm impression type, according to Table 7 in ISO 19794-4.
         */
        const val IMPRESSION_TYPE_SWIPE: Int = 8

        /**
         * Finger or palm impression type, according to Table 7 in ISO 19794-4.
         */
        const val IMPRESSION_TYPE_LIVE_SCAN_CONTACTLESS: Int = 9

        val formatType: ByteArray = byteArrayOf(0x00, 0x09)
            /**
             * Returns the format type.
             * 
             * @return a byte array of length 2
             */


        /**
         * Returns a human readable string for the given position code.
         * 
         * @param position an ISO finger position code
         * @return a human readable string
         */
        private fun positionToString(position: Int): String? =
            when (position) {
                POSITION_UNKNOWN_FINGER -> "Unknown finger"
                POSITION_RIGHT_THUMB -> "Right thumb"
                POSITION_RIGHT_INDEX_FINGER -> "Right index finger"
                POSITION_RIGHT_MIDDLE_FINGER -> "Right middle finger"
                POSITION_RIGHT_RING_FINGER -> "Right ring finger"
                POSITION_RIGHT_LITTLE_FINGER -> "Right little finger"
                POSITION_LEFT_THUMB -> "Left thumb"
                POSITION_LEFT_INDEX_FINGER -> "Left index finger"
                POSITION_LEFT_MIDDLE_FINGER -> "Left middle finger"
                POSITION_LEFT_RING_FINGER -> "Left ring finger"
                POSITION_LEFT_LITTLE_FINGER -> "Left little finger"
                POSITION_PLAIN_RIGHT_FOUR_FINGERS -> "Right four fingers"
                POSITION_PLAIN_LEFT_FOUR_FINGERS -> "Left four fingers"
                POSITION_PLAIN_THUMBS -> "Plain thumbs"
                POSITION_UNKNOWN_PALM -> "Unknown palm"
                POSITION_RIGHT_FULL_PALM -> "Right full palm"
                POSITION_RIGHT_WRITER_S_PALM -> "Right writer's palm"
                POSITION_LEFT_FULL_PALM -> "Left full palm"
                POSITION_LEFT_WRITER_S_PALM -> "Left writer's palm"
                POSITION_RIGHT_LOWER_PALM -> "Right lower palm"
                POSITION_RIGHT_UPPER_PALM -> "Right upper palm"
                POSITION_LEFT_LOWER_PALM -> "Left lower palm"
                POSITION_LEFT_UPPER_PALM -> "Left upper palm"
                POSITION_RIGHT_OTHER -> "Right other"
                POSITION_LEFT_OTHER -> "Left other"
                POSITION_RIGHT_INTERDIGITAL -> "Right interdigital"
                POSITION_RIGHT_THENAR -> "Right thenar"
                POSITION_RIGHT_HYPOTHENAR -> "Right hypothenar"
                POSITION_LEFT_INTERDIGITAL -> "Left interdigital"
                POSITION_LEFT_THENAR -> "Left thenar"
                POSITION_LEFT_HYPOTHENAR -> "Left hypothenar"
                else -> null
            }

        /**
         * Returns a human readable string for the given impression type code.
         * 
         * @param impressionType the impression type code
         * @return a human readable string for the given impression type code
         */
        private fun impressionTypeToString(impressionType: Int): String? =
            when (impressionType) {
                IMPRESSION_TYPE_LIVE_SCAN_PLAIN -> "Live scan plain"
                IMPRESSION_TYPE_LIVE_SCAN_ROLLED -> "Live scan rolled"
                IMPRESSION_TYPE_NON_LIVE_SCAN_PLAIN -> "Non-live scan plain"
                IMPRESSION_TYPE_NON_LIVE_SCAN_ROLLED -> "Non-live scan rolled"
                IMPRESSION_TYPE_LATENT -> "Latent"
                IMPRESSION_TYPE_SWIPE -> "Swipe"
                IMPRESSION_TYPE_LIVE_SCAN_CONTACTLESS -> "Live scan contactless"
                else -> null
            }

        /**
         * Converts from ISO (FRH) coding to ICAO/CBEFF (BHT) coding.
         * 
         * <table>
         * <tr> <td>Finger</td>       <td>BHT coding</td> <td>FRH coding</td> </tr>
         * <tr> <td>Right thumb</td>  <td> 5</td>         <td> 1</td> </tr>
         * <tr> <td>Right index</td>  <td> 9</td>         <td> 2</td> </tr>
         * <tr> <td>Right middle</td> <td>13</td>         <td> 3</td> </tr>
         * <tr> <td>Right ring</td>   <td>17</td>         <td> 4</td> </tr>
         * <tr> <td>Right little</td> <td>21</td>         <td> 5</td> </tr>
         * <tr> <td>Left thumb</td>   <td> 6</td>         <td> 6</td> </tr>
         * <tr> <td>Left index</td>   <td>10</td>         <td> 7</td> </tr>
         * <tr> <td>Left middle</td>  <td>14</td>         <td> 8</td> </tr>
         * <tr> <td>Left ring</td>    <td>18</td>         <td> 9</td> </tr>
         * <tr> <td>Left little</td>  <td>22</td>         <td>10</td> </tr>
        </table> * 
         * 
         * @param position an ISO finger position code
         * @return an ICAO biometric subtype
         */
        private fun toBiometricSubtype(position: Int): Int =
            when (position) {
                POSITION_UNKNOWN_FINGER -> CBEFFInfoConstants.BIOMETRIC_SUBTYPE_NONE
                POSITION_RIGHT_THUMB -> CBEFFInfoConstants.BIOMETRIC_SUBTYPE_NONE or CBEFFInfoConstants.BIOMETRIC_SUBTYPE_MASK_RIGHT or CBEFFInfoConstants.BIOMETRIC_SUBTYPE_MASK_THUMB
                POSITION_RIGHT_INDEX_FINGER -> CBEFFInfoConstants.BIOMETRIC_SUBTYPE_NONE or CBEFFInfoConstants.BIOMETRIC_SUBTYPE_MASK_RIGHT or CBEFFInfoConstants.BIOMETRIC_SUBTYPE_MASK_POINTER_FINGER
                POSITION_RIGHT_MIDDLE_FINGER -> CBEFFInfoConstants.BIOMETRIC_SUBTYPE_NONE or CBEFFInfoConstants.BIOMETRIC_SUBTYPE_MASK_RIGHT or CBEFFInfoConstants.BIOMETRIC_SUBTYPE_MASK_MIDDLE_FINGER
                POSITION_RIGHT_RING_FINGER -> CBEFFInfoConstants.BIOMETRIC_SUBTYPE_NONE or CBEFFInfoConstants.BIOMETRIC_SUBTYPE_MASK_RIGHT or CBEFFInfoConstants.BIOMETRIC_SUBTYPE_MASK_RING_FINGER
                POSITION_RIGHT_LITTLE_FINGER -> CBEFFInfoConstants.BIOMETRIC_SUBTYPE_NONE or CBEFFInfoConstants.BIOMETRIC_SUBTYPE_MASK_RIGHT or CBEFFInfoConstants.BIOMETRIC_SUBTYPE_MASK_LITTLE_FINGER
                POSITION_LEFT_THUMB -> CBEFFInfoConstants.BIOMETRIC_SUBTYPE_NONE or CBEFFInfoConstants.BIOMETRIC_SUBTYPE_MASK_LEFT or CBEFFInfoConstants.BIOMETRIC_SUBTYPE_MASK_THUMB
                POSITION_LEFT_INDEX_FINGER -> CBEFFInfoConstants.BIOMETRIC_SUBTYPE_NONE or CBEFFInfoConstants.BIOMETRIC_SUBTYPE_MASK_LEFT or CBEFFInfoConstants.BIOMETRIC_SUBTYPE_MASK_POINTER_FINGER
                POSITION_LEFT_MIDDLE_FINGER -> CBEFFInfoConstants.BIOMETRIC_SUBTYPE_NONE or CBEFFInfoConstants.BIOMETRIC_SUBTYPE_MASK_LEFT or CBEFFInfoConstants.BIOMETRIC_SUBTYPE_MASK_MIDDLE_FINGER
                POSITION_LEFT_RING_FINGER -> CBEFFInfoConstants.BIOMETRIC_SUBTYPE_NONE or CBEFFInfoConstants.BIOMETRIC_SUBTYPE_MASK_LEFT or CBEFFInfoConstants.BIOMETRIC_SUBTYPE_MASK_RING_FINGER
                POSITION_LEFT_LITTLE_FINGER -> CBEFFInfoConstants.BIOMETRIC_SUBTYPE_NONE or CBEFFInfoConstants.BIOMETRIC_SUBTYPE_MASK_LEFT or CBEFFInfoConstants.BIOMETRIC_SUBTYPE_MASK_LITTLE_FINGER
                POSITION_PLAIN_RIGHT_FOUR_FINGERS -> CBEFFInfoConstants.BIOMETRIC_SUBTYPE_NONE or CBEFFInfoConstants.BIOMETRIC_SUBTYPE_MASK_RIGHT
                POSITION_PLAIN_LEFT_FOUR_FINGERS -> CBEFFInfoConstants.BIOMETRIC_SUBTYPE_NONE or CBEFFInfoConstants.BIOMETRIC_SUBTYPE_MASK_LEFT
                POSITION_PLAIN_THUMBS -> CBEFFInfoConstants.BIOMETRIC_SUBTYPE_NONE or CBEFFInfoConstants.BIOMETRIC_SUBTYPE_MASK_THUMB
                POSITION_UNKNOWN_PALM -> CBEFFInfoConstants.BIOMETRIC_SUBTYPE_NONE
                POSITION_RIGHT_FULL_PALM -> CBEFFInfoConstants.BIOMETRIC_SUBTYPE_NONE or CBEFFInfoConstants.BIOMETRIC_SUBTYPE_MASK_RIGHT
                POSITION_RIGHT_WRITER_S_PALM -> CBEFFInfoConstants.BIOMETRIC_SUBTYPE_NONE
                POSITION_LEFT_FULL_PALM -> CBEFFInfoConstants.BIOMETRIC_SUBTYPE_NONE or CBEFFInfoConstants.BIOMETRIC_SUBTYPE_MASK_LEFT
                POSITION_LEFT_WRITER_S_PALM -> CBEFFInfoConstants.BIOMETRIC_SUBTYPE_NONE or CBEFFInfoConstants.BIOMETRIC_SUBTYPE_MASK_LEFT
                POSITION_RIGHT_LOWER_PALM -> CBEFFInfoConstants.BIOMETRIC_SUBTYPE_NONE or CBEFFInfoConstants.BIOMETRIC_SUBTYPE_MASK_RIGHT
                POSITION_RIGHT_UPPER_PALM -> CBEFFInfoConstants.BIOMETRIC_SUBTYPE_NONE or CBEFFInfoConstants.BIOMETRIC_SUBTYPE_MASK_RIGHT
                POSITION_LEFT_LOWER_PALM -> CBEFFInfoConstants.BIOMETRIC_SUBTYPE_NONE or CBEFFInfoConstants.BIOMETRIC_SUBTYPE_MASK_LEFT
                POSITION_LEFT_UPPER_PALM -> CBEFFInfoConstants.BIOMETRIC_SUBTYPE_NONE or CBEFFInfoConstants.BIOMETRIC_SUBTYPE_MASK_LEFT
                POSITION_RIGHT_OTHER -> CBEFFInfoConstants.BIOMETRIC_SUBTYPE_NONE or CBEFFInfoConstants.BIOMETRIC_SUBTYPE_MASK_RIGHT
                POSITION_LEFT_OTHER -> CBEFFInfoConstants.BIOMETRIC_SUBTYPE_NONE or CBEFFInfoConstants.BIOMETRIC_SUBTYPE_MASK_LEFT
                POSITION_RIGHT_INTERDIGITAL -> CBEFFInfoConstants.BIOMETRIC_SUBTYPE_NONE or CBEFFInfoConstants.BIOMETRIC_SUBTYPE_MASK_RIGHT
                POSITION_RIGHT_THENAR -> CBEFFInfoConstants.BIOMETRIC_SUBTYPE_NONE or CBEFFInfoConstants.BIOMETRIC_SUBTYPE_MASK_RIGHT
                POSITION_RIGHT_HYPOTHENAR -> CBEFFInfoConstants.BIOMETRIC_SUBTYPE_NONE or CBEFFInfoConstants.BIOMETRIC_SUBTYPE_MASK_RIGHT
                POSITION_LEFT_INTERDIGITAL -> CBEFFInfoConstants.BIOMETRIC_SUBTYPE_NONE or CBEFFInfoConstants.BIOMETRIC_SUBTYPE_MASK_LEFT
                POSITION_LEFT_THENAR -> CBEFFInfoConstants.BIOMETRIC_SUBTYPE_NONE or CBEFFInfoConstants.BIOMETRIC_SUBTYPE_MASK_LEFT
                POSITION_LEFT_HYPOTHENAR -> CBEFFInfoConstants.BIOMETRIC_SUBTYPE_NONE or CBEFFInfoConstants.BIOMETRIC_SUBTYPE_MASK_LEFT
                else -> CBEFFInfoConstants.BIOMETRIC_SUBTYPE_NONE
            }
    }
}
