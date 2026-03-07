/*
 * JMRTD - A Java API for accessing machine readable travel documents.
 *
 * Copyright (C) 2006 - 2025  The JMRTD team
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
 * $Id: IrisInfo.java 1896 2025-04-18 21:39:56Z martijno $
 */
package kmrtd.lds.iso19794

import kmrtd.cbeff.BiometricDataBlock
import kmrtd.cbeff.CBEFFInfoConstants
import kmrtd.cbeff.ISO781611
import kmrtd.cbeff.StandardBiometricHeader
import kmrtd.lds.AbstractListInfo
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.SortedMap
import java.util.TreeMap
import java.util.logging.Logger

/**
 * Iris record header and biometric subtype blocks
 * based on Section 6.5.3 and Table 2 of
 * ISO/IEC 19794-6 2005.
 * 
 * 
 * TODO: proper enums for fields.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * @version $Revision: 1896 $
 */
class IrisInfo : AbstractListInfo<IrisBiometricSubtypeInfo?>, BiometricDataBlock {
    private var recordLength: Long = 0

    /**
     * Returns the capture device identifier.
     * 
     * @return the capture device identifier
     */
    /* 16 bit */
    var captureDeviceId: Int = 0
        private set

    /**
     * Returns the horizontal orientation.
     * 
     * @return the horizontalOrientation, one of [.ORIENTATION_UNDEF], [.ORIENTATION_BASE], or [.ORIENTATION_FLIPPED]
     */
    var horizontalOrientation: Int = 0
        private set

    /**
     * Returns the vertical orientation.
     * 
     * @return the verticalOrientation, one of [.ORIENTATION_UNDEF], [.ORIENTATION_BASE], or [.ORIENTATION_FLIPPED]
     */
    var verticalOrientation: Int = 0
        private set

    /**
     * Returns the scan type.
     * One of [.SCAN_TYPE_UNDEF], [.SCAN_TYPE_PROGRESSIVE],
     * [.SCAN_TYPE_INTERLACE_FRAME], [.SCAN_TYPE_INTERLACE_FIELD],
     * or [.SCAN_TYPE_CORRECTED].
     * 
     * @return the scanType
     */
    var scanType: Int = 0
        private set

    /**
     * Returns the iris occlusion.
     * 
     * @return the irisOcclusion
     */
    var irisOcclusion: Int = 0
        private set

    /**
     * Returns the iris occlusing filling.
     * 
     * @return the occlusionFilling
     */
    var occlusionFilling: Int = 0
        private set

    /**
     * Returns the boundary extraction.
     * 
     * @return the boundaryExtraction
     */
    var boundaryExtraction: Int = 0
        private set

    /**
     * Returns the iris diameter.
     * 
     * @return the irisDiameter
     */
    var irisDiameter: Int = 0
        private set

    /**
     * Returns the image format.
     * 
     * @return the imageFormat
     */
    var imageFormat: Int = 0
        private set

    /**
     * Returns the raw image width.
     * 
     * @return the rawImageWidth
     */
    var rawImageWidth: Int = 0
        private set

    /**
     * Returns the raw image height.
     * 
     * @return the rawImageHeight
     */
    var rawImageHeight: Int = 0
        private set

    /**
     * Returns the intensity depth.
     * 
     * @return the intensityDepth
     */
    var intensityDepth: Int = 0
        private set

    /**
     * Returns the image transformation.
     * 
     * @return the imageTransformation
     */
    var imageTransformation: Int = 0
        private set

    /**
     * Returns the device unique id.
     * 
     * @return the deviceUniqueId
     */
    /*
          * Length 16, starts with 'D' (serial), 'M' (MAC address) or 'P' (processor Id),
          * or all zeroes (indicating no serial number).
          */
    var deviceUniqueId: ByteArray
        private set

    private var sbh: StandardBiometricHeader?

    /**
     * Constructs a new iris info object.
     * 
     * @param captureDeviceId           capture device identifier assigned by vendor
     * @param horizontalOrientation     horizontal orientation: [.ORIENTATION_UNDEF], [.ORIENTATION_BASE], or [.ORIENTATION_FLIPPED]
     * @param verticalOrientation       vertical orientation: [.ORIENTATION_UNDEF], [.ORIENTATION_BASE], or [.ORIENTATION_FLIPPED]
     * @param scanType                  scan type
     * @param irisOcclusion             iris occlusion (polar only)
     * @param occlusionFilling          occlusion filling (polar only)
     * @param boundaryExtraction        boundary extraction (polar only)
     * @param irisDiameter              expected iris diameter in pixels (rectilinear only)
     * @param imageFormat               image format of data blob (JPEG, raw, etc.)
     * @param rawImageWidth             raw image width, pixels
     * @param rawImageHeight            raw image height, pixels
     * @param intensityDepth            intensity depth, bits, per color
     * @param imageTransformation       transformation to polar image (polar only)
     * @param deviceUniqueId            a 16 character string uniquely identifying the device or source of that data
     * @param irisBiometricSubtypeInfos the iris biometric subtype records
     */
    constructor(
        captureDeviceId: Int, horizontalOrientation: Int, verticalOrientation: Int,
        scanType: Int, irisOcclusion: Int, occlusionFilling: Int,
        boundaryExtraction: Int, irisDiameter: Int, imageFormat: Int,
        rawImageWidth: Int, rawImageHeight: Int, intensityDepth: Int, imageTransformation: Int,
        deviceUniqueId: ByteArray,
        irisBiometricSubtypeInfos: MutableList<IrisBiometricSubtypeInfo>
    ) : this(
        null, captureDeviceId, horizontalOrientation, verticalOrientation,
        scanType, irisOcclusion, occlusionFilling,
        boundaryExtraction, irisDiameter, imageFormat,
        rawImageWidth, rawImageHeight, intensityDepth, imageTransformation,
        deviceUniqueId, irisBiometricSubtypeInfos
    )

    /**
     * Constructs a new iris info object.
     * 
     * @param sbh                       standard biometric header to use
     * @param captureDeviceId           capture device identifier assigned by vendor
     * @param horizontalOrientation     horizontal orientation: [.ORIENTATION_UNDEF], [.ORIENTATION_BASE], or [.ORIENTATION_FLIPPED]
     * @param verticalOrientation       vertical orientation: [.ORIENTATION_UNDEF], [.ORIENTATION_BASE], or [.ORIENTATION_FLIPPED]
     * @param scanType                  scan type
     * @param irisOcclusion             iris occlusion (polar only)
     * @param occlusionFilling          occlusion filling (polar only)
     * @param boundaryExtraction        boundary extraction (polar only)
     * @param irisDiameter              expected iris diameter in pixels (rectilinear only)
     * @param imageFormat               image format of data blob (JPEG, raw, etc.)
     * @param rawImageWidth             raw image width, pixels
     * @param rawImageHeight            raw image height, pixels
     * @param intensityDepth            intensity depth, bits, per color
     * @param imageTransformation       transformation to polar image (polar only)
     * @param deviceUniqueId            a 16 character string uniquely identifying the device or source of that data
     * @param irisBiometricSubtypeInfos the iris biometric subtype records
     */
    constructor(
        sbh: StandardBiometricHeader?,
        captureDeviceId: Int, horizontalOrientation: Int, verticalOrientation: Int,
        scanType: Int, irisOcclusion: Int, occlusionFilling: Int,
        boundaryExtraction: Int, irisDiameter: Int, imageFormat: Int,
        rawImageWidth: Int, rawImageHeight: Int, intensityDepth: Int, imageTransformation: Int,
        deviceUniqueId: ByteArray,
        irisBiometricSubtypeInfos: MutableList<IrisBiometricSubtypeInfo>
    ) {
        this.sbh = sbh
        requireNotNull(irisBiometricSubtypeInfos) { "Null irisBiometricSubtypeInfos" }
        this.captureDeviceId = captureDeviceId
        this.horizontalOrientation = horizontalOrientation
        this.verticalOrientation = verticalOrientation
        this.scanType = scanType
        this.irisOcclusion = irisOcclusion
        this.occlusionFilling = occlusionFilling
        this.boundaryExtraction = boundaryExtraction
        this.irisDiameter = irisDiameter
        this.imageFormat = imageFormat
        this.rawImageWidth = rawImageWidth
        this.rawImageHeight = rawImageHeight
        this.intensityDepth = intensityDepth
        this.imageTransformation = imageTransformation
        val headerLength: Long = 45
        var dataLength: Long = 0
        for (irisBiometricSubtypeInfo in irisBiometricSubtypeInfos) {
            dataLength += irisBiometricSubtypeInfo.getRecordLength()
            add(irisBiometricSubtypeInfo)
        }
        require(!(deviceUniqueId == null || deviceUniqueId.size != 16)) { "deviceUniqueId invalid" }
        this.deviceUniqueId = ByteArray(16)
        System.arraycopy(deviceUniqueId, 0, this.deviceUniqueId, 0, deviceUniqueId.size)
        this.recordLength = headerLength + dataLength
    }

    /**
     * Constructs an iris info from binary encoding.
     * 
     * @param inputStream an input stream
     * @throws IOException if reading fails
     */
    constructor(inputStream: InputStream) : this(null, inputStream)

    /**
     * Constructs an iris info from binary encoding.
     * 
     * @param sbh         standard biometric header to use
     * @param inputStream an input stream
     * @throws IOException if reading fails
     */
    constructor(sbh: StandardBiometricHeader?, inputStream: InputStream) {
        this.sbh = sbh
        readObject(inputStream)
    }

    /**
     * Reads this iris info from input stream.
     * 
     * @param inputStream an input stream
     * @throws IOException if reading fails
     */
    @Throws(IOException::class)
    override fun readObject(inputStream: InputStream) {
        /* Iris Record Header (45) */

        val dataIn =
            if (inputStream is DataInputStream) inputStream else DataInputStream(inputStream)

        val iir0 = dataIn.readInt() /* format id (e.g. "IIR" 0x00) */ /* 4 */
        require(iir0 == FORMAT_IDENTIFIER) {
            "'IIR' marker expected! Found " + Integer.toHexString(
                iir0
            )
        }

        val version = dataIn.readInt() /* version (e.g. "010" 0x00) */ /* + 4 = 8 */
        require(version == VERSION_NUMBER) {
            "'010' version number expected! Found " + Integer.toHexString(
                version
            )
        }

        this.recordLength = dataIn.readInt().toLong() /* & 0x00000000FFFFFFFFL */ /* + 4 = 12 */
        val headerLength: Long = 45
        val dataLength = this.recordLength - headerLength

        captureDeviceId = dataIn.readUnsignedShort() /* + 2 = 14 */
        val count = dataIn.readUnsignedByte() /* + 1 = 15 */

        val recordHeaderLength = dataIn.readUnsignedShort() /* Should be 45. */ /* + 2 = 17 */
        require(recordHeaderLength.toLong() == headerLength) { "Expected header length " + headerLength + ", found " + recordHeaderLength }

        /*
         *  16 15 14 13 12 11 10  9  8  7  6  5  4  3  2  1
         * [  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  ]
         *                                             1  1  = 0x0003 horizontalOrientation (>> 0)
         *                                       1  1  0  0  = 0x000C verticalOrientation (>> 2)
         *                              1  1  1  0  0  0  0  = 0x0070 scanType (>> 4)
         *                           1  0  0  0  0  0  0  0  = 0x0080 irisOcclusion (>> 7)
         *                        1  0  0  0  0  0  0  0  0  = 0x0100 occlusionFilling (>> 8)
         *                     1  0  0  0  0  0  0  0  0  0  = 0x0200 boundaryExtraction (>> 9)
         */
        val imagePropertiesBits = dataIn.readUnsignedShort() /* + 2 = 19 */
        horizontalOrientation = imagePropertiesBits and 0x0003
        verticalOrientation = (imagePropertiesBits and 0x00C) shr 2
        scanType = (imagePropertiesBits and 0x0070) shr 4
        irisOcclusion = (imagePropertiesBits and 0x0080) shr 7
        occlusionFilling = (imagePropertiesBits and 0x0100) shr 8
        boundaryExtraction = (imagePropertiesBits and 0x0200) shr 9

        irisDiameter = dataIn.readUnsignedShort() /* + 2 = 21 */
        imageFormat = dataIn.readUnsignedShort() /* + 2 = 23 */
        rawImageWidth = dataIn.readUnsignedShort() /* + 2 = 25 */
        rawImageHeight = dataIn.readUnsignedShort() /* + 2 = 27 */
        intensityDepth = dataIn.readUnsignedByte() /* + 1 = 28*/
        imageTransformation = dataIn.readUnsignedByte() /* + 1 = 29 */

        /*
         * A 16 character string uniquely identifying the
         * device or source of the data. This data can be
         * one of:
         * Device Serial number, identified by the first character "D"
         * Host PC Mac address, identified by the first character "M"
         * Host PC processor ID, identified by the first character "P"
         * No serial number, identified by all zeros
         */
        deviceUniqueId = ByteArray(16) /* + 16 = 45 */
        dataIn.readFully(deviceUniqueId)

        var constructedDataLength = 0L

        /* A record contains biometric subtype (or: 'feature') blocks (which contain image data blocks)... */
        for (i in 0..<count) {
            val biometricSubtypeInfo = IrisBiometricSubtypeInfo(inputStream, imageFormat)
            constructedDataLength += biometricSubtypeInfo.getRecordLength()
            add(biometricSubtypeInfo)
        }
        if (dataLength != constructedDataLength) {
            LOGGER.warning(
                ("ConstructedDataLength and dataLength differ: "
                        + "dataLength = " + dataLength
                        + ", constructedDataLength = " + constructedDataLength)
            )
        }
    }

    /**
     * Writes this iris info to an output stream.
     * 
     * @param outputStream an output stream
     * @throws IOException if writing fails
     */
    @Throws(IOException::class)
    override fun writeObject(outputStream: OutputStream?) {
        val headerLength = 45

        var dataLength = 0
        val biometricSubtypeInfos = getSubRecords()
        for (biometricSubtypeInfo in biometricSubtypeInfos) {
            dataLength += biometricSubtypeInfo.getRecordLength().toInt()
        }

        val recordLength = headerLength + dataLength

        /* Iris Record Header (45) */
        val dataOut =
            if (outputStream is DataOutputStream) outputStream else DataOutputStream(outputStream)

        dataOut.writeInt(FORMAT_IDENTIFIER) /* header (e.g. "IIR", 0x00) */ /* 4 */
        dataOut.writeInt(VERSION_NUMBER) /* version in ASCII (e.g. "010" 0x00) */ /* +4 = 8 */

        dataOut.writeInt(recordLength) /* NOTE: bytes 9-12, i.e. 4 bytes, despite "unsigned long" in ISO/IEC FCD 19749-6. */ /* +4 = 12 */

        dataOut.writeShort(captureDeviceId) /* +2 = 14 */

        dataOut.writeByte(biometricSubtypeInfos.size) /* +1 = 15 */
        dataOut.writeShort(headerLength) /* +2 = 17 */

        var imagePropertiesBits = 0
        imagePropertiesBits = imagePropertiesBits or (horizontalOrientation and 0x0003)
        imagePropertiesBits = imagePropertiesBits or ((verticalOrientation shl 2) and 0x00C)
        imagePropertiesBits = imagePropertiesBits or ((scanType shl 4) and 0x0070)
        imagePropertiesBits = imagePropertiesBits or ((irisOcclusion shl 7) and 0x0080)
        imagePropertiesBits = imagePropertiesBits or ((occlusionFilling shl 8) and 0x0100)
        imagePropertiesBits = imagePropertiesBits or ((boundaryExtraction shl 9) and 0x0200)
        dataOut.writeShort(imagePropertiesBits) /* +2 = 19 */

        dataOut.writeShort(irisDiameter) /* +2 = 21 */
        dataOut.writeShort(imageFormat) /* +2 = 23 */
        dataOut.writeShort(rawImageWidth) /* +2 = 25 */
        dataOut.writeShort(rawImageHeight) /* +2 = 27 */
        dataOut.writeByte(intensityDepth) /* +1 = 28 */
        dataOut.writeByte(imageTransformation) /* +1 = 29 */
        dataOut.write(deviceUniqueId) /* array of length 16 */ /* + 16 = 45 */

        for (biometricSubtypeInfo in biometricSubtypeInfos) {
            biometricSubtypeInfo.writeObject(outputStream)
        }
    }

    val standardBiometricHeader: StandardBiometricHeader
        /**
         * Returns the standard biometric header of this iris info.
         * 
         * @return the standard biometric header
         */
        get() {
            if (sbh == null) {
                val biometricType =
                    byteArrayOf(CBEFFInfoConstants.BIOMETRIC_TYPE_FINGERPRINT.toByte())
                val biometricSubtype =
                    byteArrayOf(this.biometricSubtype.toByte())
                val formatOwner = byteArrayOf(
                    ((StandardBiometricHeader.JTC1_SC37_FORMAT_OWNER_VALUE and 0xFF00) shr 8).toByte(),
                    (StandardBiometricHeader.JTC1_SC37_FORMAT_OWNER_VALUE and 0xFF).toByte()
                )
                val formatType = byteArrayOf(
                    ((StandardBiometricHeader.ISO_19794_IRIS_IMAGE_FORMAT_TYPE_VALUE and 0xFF00) shr 8).toByte(),
                    (StandardBiometricHeader.ISO_19794_IRIS_IMAGE_FORMAT_TYPE_VALUE and 0xFF).toByte()
                )

                val elements: SortedMap<Int?, ByteArray?> =
                    TreeMap<Int?, ByteArray?>()
                elements.put(ISO781611.BIOMETRIC_TYPE_TAG, biometricType)
                elements.put(ISO781611.BIOMETRIC_SUBTYPE_TAG, biometricSubtype)
                elements.put(ISO781611.FORMAT_OWNER_TAG, formatOwner)
                elements.put(ISO781611.FORMAT_TYPE_TAG, formatType)

                sbh = StandardBiometricHeader(elements)
            }
            return sbh
        }

    override fun hashCode(): Int {
        val prime = 31
        var result = super.hashCode()
        result = prime * result + boundaryExtraction
        result = prime * result + captureDeviceId
        result = prime * result + deviceUniqueId.contentHashCode()
        result = prime * result + horizontalOrientation
        result = prime * result + imageFormat
        result = prime * result + imageTransformation
        result = prime * result + intensityDepth
        result = prime * result + irisDiameter
        result = prime * result + irisOcclusion
        result = prime * result + occlusionFilling
        result = prime * result + rawImageHeight
        result = prime * result + rawImageWidth
        result = prime * result + (recordLength xor (recordLength ushr 32)).toInt()
        result = prime * result + (if (sbh == null) 0 else sbh.hashCode())
        result = prime * result + scanType
        result = prime * result + verticalOrientation
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

        val other = obj as IrisInfo
        if (sbh == null) {
            if (other.sbh != null) {
                return false
            }
        } else if (!sbh!!.equals(other.sbh)) {
            return false
        }
        if (boundaryExtraction != other.boundaryExtraction) {
            return false
        }
        if (captureDeviceId != other.captureDeviceId) {
            return false
        }
        if (!deviceUniqueId.contentEquals(other.deviceUniqueId)) {
            return false
        }
        if (horizontalOrientation != other.horizontalOrientation) {
            return false
        }
        if (imageFormat != other.imageFormat) {
            return false
        }
        if (imageTransformation != other.imageTransformation) {
            return false
        }
        if (intensityDepth != other.intensityDepth) {
            return false
        }
        if (irisDiameter != other.irisDiameter) {
            return false
        }
        if (irisOcclusion != other.irisOcclusion) {
            return false
        }
        if (occlusionFilling != other.occlusionFilling) {
            return false
        }
        if (rawImageHeight != other.rawImageHeight) {
            return false
        }
        if (rawImageWidth != other.rawImageWidth) {
            return false
        }
        if (recordLength != other.recordLength) {
            return false
        }
        if (scanType != other.scanType) {
            return false
        }
        if (verticalOrientation != other.verticalOrientation) {
            return false
        }
        return true
    }

    /**
     * Generates a textual representation of this object.
     * 
     * @return a textual representation of this object
     * @see Object.toString
     */
    override fun toString(): String {
        val result = StringBuilder()
        result.append("IrisInfo [")
        // TODO: contents
        result.append("]")
        return result.toString()
    }

    val irisBiometricSubtypeInfos: MutableList<IrisBiometricSubtypeInfo?>
        /**
         * Returns the iris biometric subtype infos embedded in this iris info.
         * 
         * @return iris biometric subtype infos
         */
        get() = getSubRecords()

    /**
     * Adds an iris biometric subtype info to this iris info.
     * 
     * @param irisBiometricSubtypeInfo an iris biometric subtype info
     */
    fun addIrisBiometricSubtypeInfo(irisBiometricSubtypeInfo: IrisBiometricSubtypeInfo?) {
        add(irisBiometricSubtypeInfo)
    }

    /**
     * Removes an iris biometric subtype info from this iris info.
     * 
     * @param index the index of the biometric subtype info to remove
     */
    fun removeIrisBiometricSubtypeInfo(index: Int) {
        remove(index)
    }

    /* ONLY PRIVATE METHODS BELOW */
    private val biometricSubtype: Int
        /**
         * Returns a bit-mask for the biometric sub-types found in this iris info.
         * 
         * @return a bit-mask for the biometric sub-types found in this iris info
         */
        get() {
            var result = CBEFFInfoConstants.BIOMETRIC_SUBTYPE_NONE
            val irisBiometricSubtypeInfos =
                getSubRecords()
            for (irisBiometricSubtypeInfo in irisBiometricSubtypeInfos) {
                result = result and irisBiometricSubtypeInfo.getBiometricSubtype()
            }
            return result
        }

    companion object {
        /**
         * Image format.
         */
        const val IMAGEFORMAT_MONO_RAW: Int = 2 /* (0x0002) */

        /**
         * Image format.
         */
        const val IMAGEFORMAT_RGB_RAW: Int = 4 /* (0x0004) */

        /**
         * Image format.
         */
        const val IMAGEFORMAT_MONO_JPEG: Int = 6 /* (0x0006) */

        /**
         * Image format.
         */
        const val IMAGEFORMAT_RGB_JPEG: Int = 8 /* (0x0008) */

        /**
         * Image format.
         */
        const val IMAGEFORMAT_MONO_JPEG_LS: Int = 10 /* (0x000A) */

        /**
         * Image format.
         */
        const val IMAGEFORMAT_RGB_JPEG_LS: Int = 12 /* (0x000C) */

        /**
         * Image format.
         */
        const val IMAGEFORMAT_MONO_JPEG2000: Int = 14 /* (0x000E) */

        /**
         * Image format.
         */
        const val IMAGEFORMAT_RGB_JPEG2000: Int = 16 /* (0x0010) */

        /**
         * Constant for capture device Id, based on Table 2 in Section 5.5 in ISO 19794-6.
         */
        const val CAPTURE_DEVICE_UNDEF: Int = 0

        /**
         * Constant for horizontal and veritical orientation, based on Table 2 in Section 5.5 in ISO 19794-6.
         */
        const val ORIENTATION_UNDEF: Int = 0

        /**
         * Constant for horizontal and veritical orientation, based on Table 2 in Section 5.5 in ISO 19794-6.
         */
        const val ORIENTATION_BASE: Int = 1

        /**
         * Constant for horizontal and veritical orientation, based on Table 2 in Section 5.5 in ISO 19794-6.
         */
        const val ORIENTATION_FLIPPED: Int = 2

        /**
         * Scan type (rectilinear only), based on Table 2 in Section 5.5 in ISO 19794-6.
         */
        const val SCAN_TYPE_UNDEF: Int = 0

        /**
         * Scan type (rectilinear only), based on Table 2 in Section 5.5 in ISO 19794-6.
         */
        const val SCAN_TYPE_PROGRESSIVE: Int = 1

        /**
         * Scan type (rectilinear only), based on Table 2 in Section 5.5 in ISO 19794-6.
         */
        const val SCAN_TYPE_INTERLACE_FRAME: Int = 2

        /**
         * Scan type (rectilinear only), based on Table 2 in Section 5.5 in ISO 19794-6.
         */
        const val SCAN_TYPE_INTERLACE_FIELD: Int = 3

        /**
         * Scan type (rectilinear only), based on Table 2 in Section 5.5 in ISO 19794-6.
         */
        const val SCAN_TYPE_CORRECTED: Int = 4

        /**
         * Iris occlusion (polar only), based on Table 2 in Section 5.5 in ISO 19794-6.
         */
        const val IROCC_UNDEF: Int = 0

        /**
         * Iris occlusion (polar only), based on Table 2 in Section 5.5 in ISO 19794-6.
         */
        const val IROCC_PROCESSED: Int = 1

        /**
         * Iris occlusion filling (polar only), based on Table 2 in Section 5.5 in ISO 19794-6.
         */
        const val IROCC_ZEROFILL: Int = 0

        /**
         * Iris occlusion filling (polar only), based on Table 2 in Section 5.5 in ISO 19794-6.
         */
        const val IROC_UNITFILL: Int = 1

        /* TODO: reference to specification. */
        const val INTENSITY_DEPTH_UNDEF: Int = 0

        /* TODO: reference to specification. */
        const val TRANS_UNDEF: Int = 0
        const val TRANS_STD: Int = 1

        /* TODO: reference to specification. */
        const val IRBNDY_UNDEF: Int = 0
        const val IRBNDY_PROCESSED: Int = 1
        private val serialVersionUID = -3415309711643815511L
        private val LOGGER: Logger = Logger.getLogger("org.jmrtd")

        /**
         * Format identifier 'I', 'I', 'R', 0x00.
         */
        private const val FORMAT_IDENTIFIER = 0x49495200

        /**
         * Version number.
         */
        private const val VERSION_NUMBER = 0x30313000
    }
}
