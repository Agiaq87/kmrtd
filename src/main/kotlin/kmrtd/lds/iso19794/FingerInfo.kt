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
 * $Id: FingerInfo.java 1896 2025-04-18 21:39:56Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
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
 * Fingerprint general record header and finger image data blocks
 * based on Section 7 and Table 2 of ISO/IEC FCD 19794-4 aka Annex F.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * @version $Revision: 1896 $
 */
class FingerInfo : AbstractListInfo<FingerImageInfo?>, BiometricDataBlock {
    /**
     * Returns the capture device identifier as specified in Section 7.1.4 of ISO 19794-4.
     * Only the low-order 12 bits are significant.
     * 
     * @return the capture device identifier
     */
    var captureDeviceId: Int = 0
        private set

    /**
     * Returns the image acquisition level as specified in Section 7.1.5 and Table 1 of ISO 19794-4.
     * Valid settings are: 10 (125 ppi), 20 (250 ppi), 30 (500 ppi), 31 (500 ppi), 40 (1000 ppi), 41 (1000 ppi).
     * 
     * @return image acquisition level
     */
    var acquisitionLevel: Int = 0
        private set

    /**
     * Returns the units used to describe the scanning and resolution of the image.
     * Either `PPI` or `PPCM`. As specified in Section 7.1.7 of ISO 19794-4.
     * 
     * @return scale units type
     */
    var scaleUnits: Int = 0
        private set

    /**
     * Returns the rounded scanning resolution used in the horizontal direction.
     * As specified in Section 7.1.8 of ISO 19794-4.
     * Depending on [.getScaleUnits] the result is either in PPI or PPCM.
     * 
     * @return the horizontal scanning resolution
     */
    var horizontalScanningResolution: Int = 0
        private set

    /**
     * Returns the rounded scanning resolution used in the vertical direction.
     * As specified in Section 7.1.9 of ISO 19794-4.
     * Depending on [.getScaleUnits] the result is either in PPI or PPCM.
     * 
     * @return the vertical scanning resolution
     */
    var verticalScanningResolution: Int = 0
        private set

    /**
     * Returns the rounded image resolution used in the horizontal direction
     * as specified in Section 7.1.10 of ISO 19794-4.
     * Depending on [.getScaleUnits] the result is either in PPI or PPCM.
     * 
     * @return the horizontal image resolution
     */
    var horizontalImageResolution: Int = 0
        private set

    /**
     * Returns the rounded image resolution used in the vertical direction
     * as specified in Section 7.1.11 of ISO 19794-4.
     * Depending on [.getScaleUnits] the result is either in PPI or PPCM.
     * 
     * @return the vertical image resolution
     */
    var verticalImageResolution: Int = 0
        private set

    /**
     * Returns the pixel depth. As specified in Section 7.1.12 of ISO 19794-4.
     * Valid values are between `0x01` to `0x10`.
     * 
     * @return the pixel depth
     */
    var depth: Int = 0
        private set

    /**
     * Returns the compression algorithm
     * as specified in Section 7.1.13 of ISO 19794-4.
     * One of
     * [.COMPRESSION_UNCOMPRESSED_BIT_PACKED],
     * [.COMPRESSION_UNCOMPRESSED_NO_BIT_PACKING],
     * [.COMPRESSION_JPEG],
     * [.COMPRESSION_JPEG2000],
     * [.COMPRESSION_PNG],
     * [.COMPRESSION_WSQ].
     * 
     * @return a constant representing the used image compression algorithm
     */
    var compressionAlgorithm: Int = 0
        private set

    private var sbh: StandardBiometricHeader? = null

    /**
     * Constructs a finger info record.
     * 
     * @param sbh                       standard biometric header to use
     * @param captureDeviceId           capture device identifier
     * @param acquisitionLevel          acquisition level
     * @param scaleUnits                scale units, one of [.SCALE_UNITS_PPI], [.SCALE_UNITS_PPCM]
     * @param scanResolutionHorizontal  horizontal scan resolution
     * @param scanResolutionVertical    vertical scan resolution
     * @param imageResolutionHorizontal horizontal image resolution
     * @param imageResolutionVertical   vertical image resolution
     * @param depth                     image depth
     * @param compressionAlgorithm      compression algorithm, see [.getCompressionAlgorithm] for valid values
     * @param fingerImageInfos          the image records
     */
    constructor(
        sbh: StandardBiometricHeader?,
        captureDeviceId: Int,
        acquisitionLevel: Int,
        scaleUnits: Int,
        scanResolutionHorizontal: Int,
        scanResolutionVertical: Int,
        imageResolutionHorizontal: Int,
        imageResolutionVertical: Int,
        depth: Int,
        compressionAlgorithm: Int,
        fingerImageInfos: List<FingerImageInfo>
    ) {
        this.sbh = sbh
        this.captureDeviceId = captureDeviceId
        this.acquisitionLevel = acquisitionLevel
        this.scaleUnits = scaleUnits
        this.horizontalScanningResolution = scanResolutionHorizontal
        this.verticalScanningResolution = scanResolutionVertical
        this.horizontalImageResolution = imageResolutionHorizontal
        this.verticalImageResolution = imageResolutionVertical
        this.depth = depth
        this.compressionAlgorithm = compressionAlgorithm
        addAll(fingerImageInfos)
    }

    /**
     * Constructs a finger info record.
     * 
     * @param inputStream input stream
     * @throws IOException on I/O error
     */
    constructor(inputStream: InputStream) : this(null, inputStream)

    /**
     * Constructs a finger info record.
     * 
     * @param sbh         standard biometric header to use
     * @param inputStream input stream
     * @throws IOException on I/O error
     */
    constructor(sbh: StandardBiometricHeader?, inputStream: InputStream) {
        this.sbh = sbh
        readObject(inputStream)
    }

    /**
     * Reads a finger info from an input stream.
     * 
     * @param inputStream an input stream
     * @throws IOException if reading fails
     */
    @Throws(IOException::class)
    override fun readObject(inputStream: InputStream) {
        /* General record header (32) according to Table 2 in Section 7.1 of ISO/IEC 19794-4. */

        val dataIn =
            inputStream as? DataInputStream ?: DataInputStream(inputStream)

        val fir0 = dataIn.readInt() /* header (e.g. "FIR", 0x00) (4) */
        require(fir0 == FORMAT_IDENTIFIER) {
            "'FIR' marker expected! Found " + Integer.toHexString(
                fir0
            )
        }

        val version = dataIn.readInt() /* version in ASCII (e.g. "010" 0x00) (4) */
        require(version == VERSION_NUMBER) {
            "'010' version number expected! Found " + Integer.toHexString(
                version
            )
        }

        val recordLength: Long = readUnsignedLong(dataIn, 6) // & 0xFFFFFFFFFFFFL;
        captureDeviceId =
            dataIn.readUnsignedShort() /* all zeros means 'unreported', only lower 12-bits used, see 7.1.4 ISO/IEC 19794-4. */
        acquisitionLevel = dataIn.readUnsignedShort()
        val count = dataIn.readUnsignedByte()
        scaleUnits = dataIn.readUnsignedByte() /* 1 -> PPI, 2 -> PPCM */
        this.horizontalScanningResolution = dataIn.readUnsignedShort()
        this.verticalScanningResolution = dataIn.readUnsignedShort()
        this.horizontalImageResolution = dataIn.readUnsignedShort() /* should be <= scanResH */
        this.verticalImageResolution = dataIn.readUnsignedShort() /* should be <= scanResV */
        depth = dataIn.readUnsignedByte() /* 1 - 16 bits, i.e. 2 - 65546 gray levels */
        compressionAlgorithm = dataIn.readUnsignedByte() /* 0 Uncompressed, no bit packing
         * 1 Uncompressed, bit packed
         * 2 Compressed, WSQ
         * 3 Compressed, JPEG
         * 4 Compressed, JPEG2000
         * 5 PNG
         */
        /* int RFU = */
        dataIn.readUnsignedShort() /* Should be 0x0000 */

        val headerLength = 4L + 4L + 6L + 2L + 2L + 1L + 1L + 2L + 2L + 2L + 2L + 1L + 1L + 2L
        val dataLength = recordLength - headerLength

        var constructedDataLength = 0L

        for (i in 0..<count) {
            val imageInfo = FingerImageInfo(inputStream, compressionAlgorithm)
            constructedDataLength += imageInfo.recordLength
            add(imageInfo)
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
     * Writes this finger info to an output stream.
     * 
     * @param outputStream an output stream
     * @throws IOException if writing fails
     */
    @Throws(IOException::class)
    override fun writeObject(outputStream: OutputStream?) {
        val headerLength: Long = 32 /* 4 + 4 + 6 + 2 + 2 + 1 + 1 + 2 + 2 + 2 + 2 + 1 + 1 + 2 */

        var dataLength: Long = 0
        val fingerImageInfos = getSubRecords()
        for (fingerImageInfo in fingerImageInfos) {
            fingerImageInfo?.let {
                dataLength += it.recordLength
            }
            //dataLength += fingerImageInfo?.recordLength
        }

        val recordLength = headerLength + dataLength

        /* General record header, should be 32... */
        val dataOut =
            outputStream as? DataOutputStream ?: DataOutputStream(outputStream)

        dataOut.writeInt(FORMAT_IDENTIFIER) /* 4 */
        dataOut.writeInt(VERSION_NUMBER) /* + 4 = 8 */

        writeLong(recordLength, dataOut, 6) /* + 6 = 14 */

        dataOut.writeShort(captureDeviceId) /* + 2 = 16 */
        dataOut.writeShort(acquisitionLevel) /* + 2 = 18 */
        dataOut.writeByte(fingerImageInfos.size) /* + 1 = 19 */
        dataOut.writeByte(scaleUnits) /* + 1 = 20 */
        dataOut.writeShort(this.horizontalScanningResolution) /* + 2 = 22 */
        dataOut.writeShort(this.verticalScanningResolution) /* + 2 = 24 */
        dataOut.writeShort(this.horizontalImageResolution) /* + 2 = 26 */
        dataOut.writeShort(this.verticalImageResolution) /* + 2 = 28 */
        dataOut.writeByte(depth) /* + 1 = 29 */

        dataOut.writeByte(compressionAlgorithm) /* + 1 = 30 */
        dataOut.writeShort(0x0000) /* RFU */ /* + 2 = 32 */

        for (fingerImageInfo in fingerImageInfos) {
            fingerImageInfo?.writeObject(dataOut)
        }
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = super.hashCode()
        result = prime * result + acquisitionLevel
        result = prime * result + captureDeviceId
        result = prime * result + compressionAlgorithm
        result = prime * result + depth
        result = prime * result + this.horizontalImageResolution
        result = prime * result + this.verticalImageResolution
        result = prime * result + (if (sbh == null) 0 else sbh.hashCode())
        result = prime * result + scaleUnits
        result = prime * result + this.horizontalScanningResolution
        result = prime * result + this.verticalScanningResolution
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

        val other = obj as FingerInfo
        return acquisitionLevel == other.acquisitionLevel &&
                captureDeviceId == other.captureDeviceId &&
                compressionAlgorithm == other.compressionAlgorithm &&
                depth == other.depth &&
                this.horizontalImageResolution == other.horizontalImageResolution &&
                this.verticalImageResolution == other.verticalImageResolution &&
                scaleUnits == other.scaleUnits &&
                this.horizontalScanningResolution == other.horizontalScanningResolution &&
                this.verticalScanningResolution == other.verticalScanningResolution
    }

    override fun toString(): String {
        val result = StringBuilder()
        result.append("FingerInfo [")
        val records = getSubRecords()
        for (record in records) {
            result.append(record.toString())
        }
        result.append("]")
        return result.toString()
    }

    /* ONLY PRIVATE BELOW */
    override val standardBiometricHeader: StandardBiometricHeader
        /**
         * Returns the standard biometric header of this biometric data block.
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
                    ((StandardBiometricHeader.ISO_19794_FINGER_IMAGE_FORMAT_TYPE_VALUE and 0xFF00) shr 8).toByte(),
                    (StandardBiometricHeader.ISO_19794_FINGER_IMAGE_FORMAT_TYPE_VALUE and 0xFF).toByte()
                )

                val elements: SortedMap<Int, ByteArray> = TreeMap()
                elements[ISO781611.BIOMETRIC_TYPE_TAG] = biometricType
                elements[ISO781611.BIOMETRIC_SUBTYPE_TAG] = biometricSubtype
                elements[ISO781611.FORMAT_OWNER_TAG] = formatOwner
                elements[ISO781611.FORMAT_TYPE_TAG] = formatType

                sbh = StandardBiometricHeader(elements)
            }
            return sbh!!
        }

    val fingerImageInfos: MutableList<FingerImageInfo?>
        /**
         * Returns the finger image infos embedded in this finger info.
         * 
         * @return the embedded finger image infos
         */
        get() = getSubRecords()

    /**
     * Adds a finger image info to this finger info.
     * 
     * @param fingerImageInfo the finger image info to add
     */
    fun addFingerImageInfo(fingerImageInfo: FingerImageInfo?) {
        add(fingerImageInfo)
    }

    /**
     * Removes a finger image info from this finger info.
     * 
     * @param index the index of the finger image info to remove
     */
    fun removeFingerImageInfo(index: Int) {
        remove(index)
    }

    private val biometricSubtype: Int
        /**
         * Returns the biometric sub-type bit mask for the fingers in this finger info.
         * 
         * @return a biometric sub-type bit mask
         */
        get() {
            var result = CBEFFInfoConstants.BIOMETRIC_SUBTYPE_NONE
            var isFirst = true
            val fingerImageInfos =
                getSubRecords()
            for (fingerImageInfo in fingerImageInfos) {
                fingerImageInfo?.biometricSubtype?.let {
                    if (isFirst) {
                        result = it
                        isFirst = false
                    } else {
                        result = result and it
                    }
                }
            }
            return result
        }

    companion object {
        /**
         * Scale units points per inch.
         */
        const val SCALE_UNITS_PPI: Int = 1

        /**
         * Scale units points per centimeter.
         */
        const val SCALE_UNITS_PPCM: Int = 2

        /**
         * Image compression algorithm type as specified in Section 7.1.13 and Table 3 of ISO 19794-4.
         */
        const val COMPRESSION_UNCOMPRESSED_NO_BIT_PACKING: Int = 0

        /**
         * Image compression algorithm type as specified in Section 7.1.13 and Table 3 of ISO 19794-4.
         */
        const val COMPRESSION_UNCOMPRESSED_BIT_PACKED: Int = 1

        /**
         * Image compression algorithm type as specified in Section 7.1.13 and Table 3 of ISO 19794-4.
         */
        const val COMPRESSION_WSQ: Int = 2

        /**
         * Image compression algorithm type as specified in Section 7.1.13 and Table 3 of ISO 19794-4.
         */
        const val COMPRESSION_JPEG: Int = 3

        /**
         * Image compression algorithm type as specified in Section 7.1.13 and Table 3 of ISO 19794-4.
         */
        const val COMPRESSION_JPEG2000: Int = 4

        /**
         * Image compression algorithm type as specified in Section 7.1.13 and Table 3 of ISO 19794-4.
         */
        const val COMPRESSION_PNG: Int = 5
        private const val serialVersionUID = 5808625058034008176L
        private val LOGGER: Logger = Logger.getLogger("org.jmrtd")

        /**
         * Format identifier 'F', 'I', 'R', 0x00. Specified in ISO/IEC 19794-4 Section 7.1, Table 2.
         */
        private const val FORMAT_IDENTIFIER = 0x46495200

        /**
         * Version number '0', '1', '0', 0x00. Specified in ISO/IEC 19794-4 Section 7.1, Table 2.
         */
        private const val VERSION_NUMBER = 0x30313000

        /**
         * Reads a long from a stream.
         * 
         * @param inputStream the stream to read from
         * @param byteCount   the number of bytes to read
         * @return the resulting long
         * @throws IOException on error reading from the stream
         */
        @Throws(IOException::class)
        private fun readUnsignedLong(inputStream: InputStream, byteCount: Int): Long {
            val dataIn =
                inputStream as? DataInputStream ?: DataInputStream(inputStream)
            val buf = ByteArray(byteCount)
            dataIn.readFully(buf)
            var result = 0L
            for (i in 0..<byteCount) {
                result = result shl 8
                result += (buf[i].toInt() and 0xFF).toLong()
            }
            return result
        }

        /**
         * Writes a long to a stream.
         * 
         * @param value        the long value to write
         * @param outputStream the stream to write to
         * @param byteCount    the number of bytes to use
         * @throws IOException on error writing to the stream
         */
        @Throws(IOException::class)
        private fun writeLong(value: Long, outputStream: OutputStream, byteCount: Int) {
            var byteCount = byteCount
            if (byteCount <= 0) {
                return
            }

            for (i in 0..<(byteCount - 8)) {
                outputStream.write(0)
            }
            if (byteCount > 8) {
                byteCount = 8
            }
            for (i in (byteCount - 1) downTo 0) {
                val mask = 0xFFL shl (i * 8)
                val b = ((value and mask) shr (i * 8)).toByte()
                outputStream.write(b.toInt())
            }
        }

        /**
         * Converts an image data type code to a mime-type.
         * Compression algorithm codes based on Table 3 in Section 7.1.13 of 19794-4.
         * 
         * 
         * 0 Uncompressed, no bit packing
         * 1 Uncompressed, bit packed
         * 2 Compressed, WSQ
         * 3 Compressed, JPEG
         * 4 Compressed, JPEG2000
         * 5 PNG
         * 
         * @param imageDataType an image data type constant, one of
         * `COMPRESSION_UNCOMPRESSED_NO_BIT_PACKING`,
         * `COMPRESSION_UNCOMPRESSED_BIT_PACKED`,
         * `COMPRESSION_WSQ`, `COMPRESSION_JPEG`,
         * `COMPRESSION_JPEG2000`, or `COMPRESSION_PNG`
         * @return a mime-type string
         */
        fun toMimeType(imageDataType: Int): String? =
            when (imageDataType) {
                COMPRESSION_UNCOMPRESSED_NO_BIT_PACKING -> "image/raw"
                COMPRESSION_UNCOMPRESSED_BIT_PACKED -> "image/raw"
                COMPRESSION_WSQ -> "image/x-wsq"
                COMPRESSION_JPEG -> "image/jpeg"
                COMPRESSION_JPEG2000 -> "image/jpeg2000"
                COMPRESSION_PNG -> "image/png"
                else -> null
            }

        /**
         * Converts a mime-type to an image data (compression) type.
         * 
         * @param mimeType the mime-type to convert
         * @return the image data (compression) type
         */
        fun fromMimeType(mimeType: String?): Int {
            if ("image/x-wsq" == mimeType) {
                return COMPRESSION_WSQ
            }
            if ("image/jpeg" == mimeType) {
                return COMPRESSION_JPEG
            }
            if ("image/jpeg2000" == mimeType) {
                return COMPRESSION_JPEG2000
            }
            if ("images/png" == mimeType) {
                return COMPRESSION_PNG
            }

            throw IllegalArgumentException("Did not recognize mimeType")
        }

        /**
         * Factory method
         *
         * Constructs a finger info record.
         * @param sbh         standard biometric header to use
         * @param inputStream input stream
         * @throws IOException on I/O error
         */
    }
}
