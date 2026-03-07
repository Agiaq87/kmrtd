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
 * $Id: FaceImageInfo.java 1808 2019-03-07 21:32:19Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.lds.iso19794

import kmrtd.lds.AbstractImageInfo
import kmrtd.lds.support.EyeColor
import kmrtd.lds.support.FeaturePoint
import net.sf.scuba.data.Gender
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.logging.Logger
import kotlin.IntArray

/**
 * Data structure for storing facial image data. This represents
 * a facial record data block as specified in Section 5.5, 5.6,
 * and 5.7 of ISO/IEC FCD 19794-5 (2004-03-22, AKA Annex D).
 * 
 * 
 * A facial record data block contains a single facial image.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * @version $Revision: 1808 $
 */
class FaceImageInfo
/**
 * Constructs a new face information data structure instance.
 *
 * @param gender               gender
 * @param eyeColor             eye color
 * @param featureMask          feature mask (least significant 3 bytes)
 * @param hairColor            hair color
 * @param expression           expression
 * @param poseAngle            (encoded) pose angle
 * @param poseAngleUncertainty pose angle uncertainty
 * @param faceImageType        face image type
 * @param colorSpace           color space
 * @param sourceType           source type
 * @param deviceType           capture device type (unspecified is `0x00`)
 * @param quality              quality
 * @param featurePoints        feature points
 * @param width                width
 * @param height               height
 * @param imageInputStream     encoded image bytes
 * @param imageLength          length of encoded image
 * @param imageDataType        either IMAGE_DATA_TYPE_JPEG or IMAGE_DATA_TYPE_JPEG2000
 * @throws IOException on error reading input
 */(
    /**
     * Returns the gender
     * (male, female, etc).
     *
     * @return gender
     */
    val gender: Gender,
    /**
     * Returns the eye color
     * (black, blue, brown, etc).
     *
     * @return eye color
     */
    val eyeColor: EyeColor,
    /**
     * Returns the feature mask.
     *
     * @return feature mask
     */
    val featureMask: Int,
    /**
     * Returns the hair color
     * (bald, black, blonde, etc).
     *
     * @return hair color
     */
    val hairColor: Int,
    /**
     * Returns the expression
     * (neutral, smiling, eyebrow raised, etc).
     *
     * @return expression
     */
    val expression: Int,
    /**
     * Returns the face image type
     * (full frontal, token frontal, etc).
     *
     * @return face image type
     */
    val faceImageType: Int,
    /**
     * Returns the image color space
     * (rgb, grayscale, etc).
     *
     * @return image color space
     */
    val colorSpace: Int,
    /**
     * Returns the source type
     * (camera, scanner, etc).
     *
     * @return source type
     */
    val sourceType: Int,
    /**
     * Returns the device type.
     *
     * @return device type
     */
    val deviceType: Int,
    /**
     * Returns the quality as unsigned integer.
     *
     * @return quality
     */
    val quality: Int,
    /**
     * Returns the available feature points of this face.
     *
     * @return feature points
     */
    var featurePoints: MutableList<FeaturePoint>,
    val width: Int,
    val height: Int,
    val imageInputStream: InputStream,
    val imageLength: Int,
    /**
     * Returns the image data type.
     *
     * @return image data type
     */
    val imageDataType: Int
) : AbstractImageInfo(
    TYPE_PORTRAIT,
    width,
    height,
    imageInputStream,
    imageLength.toLong(),
    toMimeType(imageDataType)
) {
    val poseAngle: IntArray = IntArray(3)
    val poseAngleUncertainty: IntArray = IntArray(3)

    val recordLength: Long = 20L + 8 * featurePoints.size + 12L + imageLength

    init {
        //requireNotNull(imageInputStream) { "Null image" }
        this.featurePoints = mutableListOf()
        if (featurePoints.isNotEmpty()) {
            System.arraycopy(featurePoints, 0, this.featurePoints, 0, featurePoints.size)
        }

        System.arraycopy(poseAngle, 0, this.poseAngle, 0, 3)
        System.arraycopy(poseAngleUncertainty, 0, this.poseAngleUncertainty, 0, 3)
    }

    /**
     * Constructs a new face information structure from binary encoding.
     * 
     * @param inputStream an input stream
     * @throws IOException if input cannot be read
     */
    constructor(inputStream: InputStream) : super(TYPE_PORTRAIT) {
        readObject(inputStream)
    }

    @Throws(IOException::class)
    override fun readObject(inputStream: InputStream) {
        val dataIn =
            inputStream as? DataInputStream ?: DataInputStream(inputStream)

        /* Facial Information Block (20), see ISO 19794-5 5.5 */
        recordLength = dataIn.readInt().toLong() and 0xFFFFFFFFL /* 4 */
        val featurePointCount = dataIn.readUnsignedShort() /* +2 = 6 */
        gender = Gender.getInstance(dataIn.readUnsignedByte()) /* +1 = 7 */
        eyeColor = EyeColor.toEyeColor(dataIn.readUnsignedByte()) /* +1 = 8 */
        hairColor = dataIn.readUnsignedByte() /* +1 = 9 */
        featureMask = dataIn.readUnsignedByte() /* +1 = 10 */
        featureMask = (featureMask shl 16) or dataIn.readUnsignedShort() /* +2 = 12 */
        expression = dataIn.readShort().toInt() /* +2 = 14 */
        poseAngle = IntArray(3)
        val by = dataIn.readUnsignedByte() /* +1 = 15 */
        poseAngle[ISO19794.YAW] = by
        val bp = dataIn.readUnsignedByte() /* +1 = 16 */
        poseAngle[ISO19794.PITCH] = bp
        val br = dataIn.readUnsignedByte() /* +1 = 17 */
        poseAngle[ISO19794.ROLL] = br
        poseAngleUncertainty = IntArray(3)
        poseAngleUncertainty[ISO19794.YAW] = dataIn.readUnsignedByte() /* +1 = 18 */
        poseAngleUncertainty[ISO19794.PITCH] = dataIn.readUnsignedByte() /* +1 = 19 */
        poseAngleUncertainty[ISO19794.ROLL] = dataIn.readUnsignedByte() /* +1 = 20 */

        /* Feature Point(s) (optional) (8 * featurePointCount), see ISO 19794-5 5.8 */
        featurePoints = arrayOfNulls<FeaturePoint>(featurePointCount)
        for (i in 0..<featurePointCount) {
            val featureType = dataIn.readUnsignedByte() /* 1 */
            val featurePoint = dataIn.readByte() /* +1 = 2 */
            val x = dataIn.readUnsignedShort() /* +2 = 4 */
            val y = dataIn.readUnsignedShort() /* +2 = 6 */
            var skippedBytes: Long = 0
            while (skippedBytes < 2) {
                skippedBytes += dataIn.skip(2)
            } /* +2 = 8, NOTE: 2 bytes reserved */
            featurePoints[i] = FeaturePoint.from(featureType, featurePoint, x, y)
        }

        /* Image Information */
        faceImageType = dataIn.readUnsignedByte() /* 1 */
        imageDataType = dataIn.readUnsignedByte() /* +1 = 2 */

        colorSpace = dataIn.readUnsignedByte() /* +1 = 7 */
        sourceType = dataIn.readUnsignedByte() /* +1 = 8 */
        deviceType = dataIn.readUnsignedShort() /* +2 = 10 */
        quality = dataIn.readUnsignedShort() /* +2 = 12 */

        /* Temporarily fix width and height if 0. */
        if (width <= 0) {
            width = 800
        }
        if (height <= 0) {
            height = 600
        }

        /*
         * Read image data, image data type code based on Section 5.8.1
         * ISO 19794-5.
         */
        mimeType = toMimeType(imageDataType)
        val imageLength = recordLength - 20 - 8 * featurePointCount - 12

        readImage(inputStream, imageLength)
    }

    /**
     * Writes this face image info to output stream.
     * 
     * @param outputStream an output stream
     * @throws IOException if writing fails
     */
    @Throws(IOException::class)
    public override fun writeObject(outputStream: OutputStream?) {
        val recordOut = ByteArrayOutputStream()
        writeFacialRecordData(recordOut)
        val facialRecordData = recordOut.toByteArray()
        val faceImageBlockLength = facialRecordData.size + 4L
        val dataOut = DataOutputStream(outputStream)
        dataOut.writeInt(faceImageBlockLength.toInt())
        dataOut.write(facialRecordData)
        dataOut.flush()
    }

    /**
     * Returns the record length.
     * 
     * @return the record length
     */
    override fun getRecordLength(): Long {
        /* Should be equal to (20 + 8 * featurePoints.length + 12 + getImageLength()). */
        return recordLength
    }

    /**
     * Returns the pose angle as an integer array of length 3,
     * containing yaw, pitch, and roll angle in encoded form.
     * 
     * @return an integer array of length 3
     */
    fun getPoseAngle(): IntArray {
        val result = IntArray(3)
        System.arraycopy(poseAngle, 0, result, 0, result.size)
        return result
    }

    /**
     * Returns the pose angle uncertainty as an integer array of length 3,
     * containing yaw, pitch, and roll angle uncertainty.
     * 
     * @return an integer array of length 3
     */
    fun getPoseAngleUncertainty(): IntArray {
        val result = IntArray(3)
        System.arraycopy(poseAngleUncertainty, 0, result, 0, result.size)
        return result
    }

    /**
     * Generates a textual representation of this object.
     * 
     * @return a textual representation of this object
     * @see Object.toString
     */
    override fun toString(): String {
        val out = StringBuilder()
        out.append("FaceImageInfo [")
        out.append("Image size: ").append(width).append(" x ").append(height).append(", ")
        out.append("Gender: ").append(if (gender == null) Gender.UNSPECIFIED else gender)
            .append(", ")
        out.append("Eye color: ").append(if (eyeColor == null) EyeColor.UNSPECIFIED else eyeColor)
            .append(", ")
        out.append("Hair color: ").append(hairColorToString()).append(", ")
        out.append("Feature mask: ").append(featureMaskToString()).append(", ")
        out.append("Expression: ").append(expressionToString()).append(", ")
        out.append("Pose angle: ").append(poseAngleToString()).append(", ")
        out.append("Face image type: ").append(faceImageTypeToString()).append(", ")
        out.append("Source type: ").append(sourceTypeToString()).append(", ")
        out.append("FeaturePoints [")
        if (featurePoints.isNotEmpty()) {
            var isFirstFeaturePoint = true
            for (featurePoint in featurePoints) {
                if (isFirstFeaturePoint) {
                    isFirstFeaturePoint = false
                } else {
                    out.append(", ")
                }
                out.append(featurePoint.toString())
            }
        }
        out.append("]") /* FeaturePoints. */
        out.append("]") /* FaceImageInfo. */
        return out.toString()
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = super.hashCode()
        result = prime * result + colorSpace
        result = prime * result + deviceType
        result = prime * result + expression
        result = prime * result + (if (eyeColor == null) 0 else eyeColor.hashCode())
        result = prime * result + faceImageType
        result = prime * result + featureMask
        result = prime * result + featurePoints.contentHashCode()
        result = prime * result + (if (gender == null) 0 else gender.hashCode())
        result = prime * result + hairColor
        result = prime * result + imageDataType
        result = prime * result + poseAngle.contentHashCode()
        result = prime * result + poseAngleUncertainty.contentHashCode()
        result = prime * result + quality
        result = prime * result + (recordLength xor (recordLength ushr 32)).toInt()
        result = prime * result + sourceType
        return result
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (!super.equals(obj)) {
            return false
        }
        if (javaClass != obj?.javaClass) {
            return false
        }

        val other = obj as FaceImageInfo
        return colorSpace == other.colorSpace && deviceType == other.deviceType && expression == other.expression && eyeColor == other.eyeColor && faceImageType == other.faceImageType && featureMask == other.featureMask && featurePoints.contentEquals(
            other.featurePoints
        ) && gender === other.gender && hairColor == other.hairColor && imageDataType == other.imageDataType && poseAngle.contentEquals(
            other.poseAngle
        ) && poseAngleUncertainty.contentEquals(other.poseAngleUncertainty) && quality == other.quality && recordLength == other.recordLength && sourceType == other.sourceType
    }

    /**
     * Writes the record data to a stream.
     * 
     * @param outputStream the stream to write to
     * @throws IOException on error
     */
    @Throws(IOException::class)
    private fun writeFacialRecordData(outputStream: OutputStream?) {
        val dataOut = DataOutputStream(outputStream)

        /* Facial Information (16) */
        dataOut.writeShort(featurePoints!!.size) /* 2 */
        dataOut.writeByte(if (gender == null) Gender.UNSPECIFIED.toInt() else gender!!.toInt()) /* 1 */
        dataOut.writeByte(if (eyeColor == null) EyeColor.UNSPECIFIED.toInt() else eyeColor!!.toInt()) /* 1 */
        dataOut.writeByte(hairColor) /* 1 */
        dataOut.writeByte(((featureMask.toLong() and 0xFF0000L) shr 16).toByte().toInt()) /* 1 */
        dataOut.writeByte(((featureMask.toLong() and 0x00FF00L) shr 8).toByte().toInt()) /* 1 */
        dataOut.writeByte((featureMask.toLong() and 0x0000FFL).toByte().toInt()) /* 1 */
        dataOut.writeShort(expression) /* 2 */
        for (i in 0..2) {                                                          /* 3 */
            val b = poseAngle[i]
            dataOut.writeByte(b)
        }
        for (i in 0..2) {                                                          /* 3 */
            dataOut.writeByte(poseAngleUncertainty[i])
        }

        /* Feature Point(s) (optional) (8 * featurePointCount) */
        for (fp in featurePoints) {
            dataOut.writeByte(fp.type)
            dataOut.writeByte((fp.majorCode shl 4) or fp.minorCode)
            dataOut.writeShort(fp.x)
            dataOut.writeShort(fp.y)
            dataOut.writeShort(0x00) /* 2 bytes RFU */
        }

        /* Image Information (12) */
        dataOut.writeByte(faceImageType) /* 1 */
        dataOut.writeByte(imageDataType) /* 1 */
        dataOut.writeShort(width) /* 2 */
        dataOut.writeShort(height) /* 2 */
        dataOut.writeByte(colorSpace) /* 1 */
        dataOut.writeByte(sourceType) /* 1 */
        dataOut.writeShort(deviceType) /* 2 */
        dataOut.writeShort(quality) /* 2 */

        /*
         * Image data type code based on Section 5.8.1
         * ISO 19794-5
         */
        writeImage(dataOut)
        dataOut.flush()
        dataOut.close()
    }

    /**
     * Converts a hair color value to a human readable string.
     * 
     * @return a human readable string for the current hair color value
     */
    private fun hairColorToString(): String =
        when (hairColor) {
            ISO19794.HAIR_COLOR_UNSPECIFIED -> "unspecified"
            ISO19794.HAIR_COLOR_BALD -> "bald"
            ISO19794.HAIR_COLOR_BLACK -> "black"
            ISO19794.HAIR_COLOR_BLONDE -> "blonde"
            ISO19794.HAIR_COLOR_BROWN -> "brown"
            ISO19794.HAIR_COLOR_GRAY -> "gray"
            ISO19794.HAIR_COLOR_WHITE -> "white"
            ISO19794.HAIR_COLOR_RED -> "red"
            ISO19794.HAIR_COLOR_GREEN -> "green"
            ISO19794.HAIR_COLOR_BLUE -> "blue"
            else -> "unknown"
        }

    /**
     * Returns a human readable string for the current feature mask.
     * 
     * @return a human readable string
     */
    private fun featureMaskToString(): String {
        if ((featureMask and ISO19794.FEATURE_FEATURES_ARE_SPECIFIED_FLAG) == 0) {
            return ""
        }
        val features: MutableCollection<String?> = ArrayList()
        if ((featureMask and ISO19794.FEATURE_GLASSES_FLAG) != 0) {
            features.add("glasses")
        }
        if ((featureMask and ISO19794.FEATURE_MOUSTACHE_FLAG) != 0) {
            features.add("moustache")
        }
        if ((featureMask and ISO19794.FEATURE_BEARD_FLAG) != 0) {
            features.add("beard")
        }
        if ((featureMask and ISO19794.FEATURE_TEETH_VISIBLE_FLAG) != 0) {
            features.add("teeth visible")
        }
        if ((featureMask and ISO19794.FEATURE_BLINK_FLAG) != 0) {
            features.add("blink")
        }
        if ((featureMask and ISO19794.FEATURE_MOUTH_OPEN_FLAG) != 0) {
            features.add("mouth open")
        }
        if ((featureMask and ISO19794.FEATURE_LEFT_EYE_PATCH_FLAG) != 0) {
            features.add("left eye patch")
        }
        if ((featureMask and ISO19794.FEATURE_RIGHT_EYE_PATCH) != 0) {
            features.add("right eye patch")
        }
        if ((featureMask and ISO19794.FEATURE_DARK_GLASSES) != 0) {
            features.add("dark glasses")
        }
        if ((featureMask and ISO19794.FEATURE_DISTORTING_MEDICAL_CONDITION) != 0) {
            features.add("distorting medical condition (which could impact feature point detection)")
        }
        val out = StringBuilder()
        val it = features.iterator()
        while (it.hasNext()) {
            out.append(it.next())
            if (it.hasNext()) {
                out.append(", ")
            }
        }

        return out.toString()
    }

    /**
     * Converts the current expression to a human readable string.
     * 
     * @return a human readable string
     */
    private fun expressionToString(): String =
        when (expression) {
            ISO19794.EXPRESSION_UNSPECIFIED.toInt() -> "unspecified"
            ISO19794.EXPRESSION_NEUTRAL.toInt() -> "neutral (non-smiling) with both eyes open and mouth closed"
            ISO19794.EXPRESSION_SMILE_CLOSED.toInt() -> "a smile where the inside of the mouth and/or teeth is not exposed (closed jaw)"
            ISO19794.EXPRESSION_SMILE_OPEN.toInt() -> "a smile where the inside of the mouth and/or teeth is exposed"
            ISO19794.EXPRESSION_RAISED_EYEBROWS.toInt() -> "raised eyebrows"
            ISO19794.EXPRESSION_EYES_LOOKING_AWAY.toInt() -> "eyes looking away from the camera"
            ISO19794.EXPRESSION_SQUINTING.toInt() -> "squinting"
            ISO19794.EXPRESSION_FROWNING.toInt() -> "frowning"
            else -> "unknown"
        }

    /**
     * Converts the current pose angle to a human readable string.
     * 
     * @return a human readable string
     */
    private fun poseAngleToString(): String {
        val out = StringBuilder()
        out.append("(")
        out.append("y: ").append(poseAngle[ISO19794.YAW])
        if (poseAngleUncertainty[ISO19794.YAW] != 0) {
            out.append(" (").append(poseAngleUncertainty[ISO19794.YAW]).append(")")
        }
        out.append(", ")
        out.append("p:").append(poseAngle[ISO19794.PITCH])
        if (poseAngleUncertainty[ISO19794.PITCH] != 0) {
            out.append(" (").append(poseAngleUncertainty[ISO19794.PITCH]).append(")")
        }
        out.append(", ")
        out.append("r: ").append(poseAngle[ISO19794.ROLL])
        if (poseAngleUncertainty[ISO19794.ROLL] != 0) {
            out.append(" (").append(poseAngleUncertainty[ISO19794.ROLL]).append(")")
        }
        out.append(")")
        return out.toString()
    }

    /**
     * Returns a textual representation of the face image type
     * (`"basic"`, `"full frontal"`, `"token frontal"`,
     * or `"unknown"`).
     * 
     * @return a textual representation of the face image type
     */
    private fun faceImageTypeToString(): String =
        when (faceImageType) {
            ISO19794.FACE_IMAGE_TYPE_BASIC -> "basic"
            ISO19794.FACE_IMAGE_TYPE_FULL_FRONTAL -> "full frontal"
            ISO19794.FACE_IMAGE_TYPE_TOKEN_FRONTAL -> "token frontal"
            else -> "unknown"
        }

    /**
     * Returns a textual representation of the source type.
     * 
     * @return a textual representation of the source type
     */
    private fun sourceTypeToString(): String =
        when (sourceType) {
            ISO19794.SOURCE_TYPE_UNSPECIFIED -> "unspecified"
            ISO19794.SOURCE_TYPE_STATIC_PHOTO_UNKNOWN_SOURCE -> "static photograph from an unknown source"
            ISO19794.SOURCE_TYPE_STATIC_PHOTO_DIGITAL_CAM -> "static photograph from a digital still-image camera"
            ISO19794.SOURCE_TYPE_STATIC_PHOTO_SCANNER -> "static photograph from a scanner"
            ISO19794.SOURCE_TYPE_VIDEO_FRAME_UNKNOWN_SOURCE -> "single video frame from an unknown source"
            ISO19794.SOURCE_TYPE_VIDEO_FRAME_ANALOG_CAM -> "single video frame from an analogue camera"
            ISO19794.SOURCE_TYPE_VIDEO_FRAME_DIGITAL_CAM -> "single video frame from a digital camera"
            else -> "unknown"
        }

    companion object {
        private val LOGGER: Logger = Logger.getLogger("kmrtd")

        /**
         * Returns a mime-type string for the compression algorithm code.
         * 
         * @param compressionAlg the compression algorithm code as it occurs in the header
         * @return a mime-type string,
         * typically `JPEG_MIME_TYPE` or `JPEG2000_MIME_TYPE`
         */
        private fun toMimeType(compressionAlg: Int): String? {
            when (compressionAlg) {
                ISO19794.IMAGE_DATA_TYPE_JPEG -> return JPEG_MIME_TYPE
                ISO19794.IMAGE_DATA_TYPE_JPEG2000 -> return JPEG2000_MIME_TYPE
                else -> {
                    LOGGER.warning("Unknown image type: $compressionAlg")
                    return null
                }
            }
        }

        /**
         * Factory method
         *
         * Construct FaceImageInfo from InputStream.
         *
         * @param inputStream an input stream
         * @return a new FaceImageInfo instance
         * @throws IOException if input cannot be read
         */
        @JvmStatic
        @Throws(IOException::class)
        fun from(inputStream: InputStream): FaceImageInfo {
            val dataIn =
                inputStream as? DataInputStream ?: DataInputStream(inputStream)

            val featurePointCount = dataIn.readUnsignedShort() /* +2 = 6 */
            val featurePoints = mutableListOf<FeaturePoint>()
            for (i in 0..<featurePointCount) {
                val featureType = dataIn.readUnsignedByte() /* 1 */
                val featurePoint = dataIn.readByte() /* +1 = 2 */
                val x = dataIn.readUnsignedShort() /* +2 = 4 */
                val y = dataIn.readUnsignedShort() /* +2 = 6 */
                var skippedBytes: Long = 0
                while (skippedBytes < 2) {
                    skippedBytes += dataIn.skip(2)
                } /* +2 = 8, NOTE: 2 bytes reserved */
                featurePoints.add(FeaturePoint.from(featureType, featurePoint, x, y))
            }

            val featureMask = dataIn.readUnsignedByte() /* +1 = 10 */
            val poseAngle = IntArray(3)
            poseAngle[ISO19794.YAW] = dataIn.readUnsignedByte() /* +1 = 15 */
            poseAngle[ISO19794.PITCH] = dataIn.readUnsignedByte() /* +1 = 16 */
            poseAngle[ISO19794.ROLL] = dataIn.readUnsignedByte() /* +1 = 17 */

            val poseAngleUncertainty = IntArray(3)
            poseAngleUncertainty[ISO19794.YAW] = dataIn.readUnsignedByte() /* +1 = 18 */
            poseAngleUncertainty[ISO19794.PITCH] = dataIn.readUnsignedByte() /* +1 = 19 */
            poseAngleUncertainty[ISO19794.ROLL] = dataIn.readUnsignedByte() /* +1 = 20 */

            var width = dataIn.readUnsignedShort() /* +2 = 4 */
            var height = dataIn.readUnsignedShort() /* +2 = 6 */

            if (width <= 0) {
                width = 800
            }
            if (height <= 0) {
                height = 600
            }

            val returnedObject = FeaturePoint(
                recordLength = dataIn.readInt().toLong() and 0xFFFFFFFFL /* 4 */,
                gender = Gender.getInstance(dataIn.readUnsignedByte()) /* +1 = 7 */,
                eyeColor = EyeColor.toEyeColor(dataIn.readUnsignedByte()) /* +1 = 8 */,
                hairColor = dataIn.readUnsignedByte() /* +1 = 9 */,
                featureMask = featureMask,
                featureMask = (featureMask shl 16) or dataIn.readUnsignedShort() /* +2 = 12 */,
                expression = dataIn.readShort().toInt() /* +2 = 14 */,
                poseAngle = poseAngle,
                poseAngleUncertainty = poseAngleUncertainty,
                featurePoints = featurePoints,
                faceImageType = dataIn.readUnsignedByte() /* 1 */,
                imageDataType = dataIn.readUnsignedByte() /* +1 = 2 */,
                width = width,
                height = height,
                colorSpace = dataIn.readUnsignedByte() /* +1 = 7 */,
                sourceType = dataIn.readUnsignedByte() /* +1 = 8 */,
                deviceType = dataIn.readUnsignedShort() /* +2 = 10 */,
                quality = dataIn.readUnsignedShort() /* +2 = 12 */
            )
        }
    }
}
