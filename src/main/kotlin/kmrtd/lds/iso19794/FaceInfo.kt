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
 * $Id: FaceInfo.java 1896 2025-04-18 21:39:56Z martijno $
 */
package kmrtd.lds.iso19794

import kmrtd.cbeff.BiometricDataBlock
import kmrtd.cbeff.CBEFFInfoConstants
import kmrtd.cbeff.ISO781611
import kmrtd.cbeff.StandardBiometricHeader
import kmrtd.lds.AbstractListInfo
import net.sf.scuba.data.Gender
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.SortedMap
import java.util.TreeMap
import java.util.logging.Level
import java.util.logging.Logger

/**
 * A facial record consists of a facial record header and one or more facial record datas.
 * See 5.1 of ISO 19794-5.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * @version $Revision: 1896 $
 */
class FaceInfo : AbstractListInfo<FaceImageInfo?>, BiometricDataBlock {
    private var sbh: StandardBiometricHeader?

    /**
     * Constructs a face info from a list of face image infos.
     * 
     * @param faceImageInfos face image infos
     */
    constructor(faceImageInfos: MutableList<FaceImageInfo?>?) : this(null, faceImageInfos)

    /**
     * Constructs a face info from a list of face image infos.
     * 
     * @param sbh            the standard biometric header to use
     * @param faceImageInfos face image infos
     */
    constructor(sbh: StandardBiometricHeader?, faceImageInfos: MutableList<FaceImageInfo?>?) {
        this.sbh = sbh
        addAll(faceImageInfos)
    }

    /**
     * Constructs a face info from binary encoding.
     * 
     * @param inputStream an input stream
     * @throws IOException when decoding fails
     */
    constructor(inputStream: InputStream) : this(null, inputStream)

    /**
     * Constructs a face info from binary encoding.
     * 
     * @param sbh         the standard biometric header to use
     * @param inputStream an input stream
     * @throws IOException when decoding fails
     */
    constructor(sbh: StandardBiometricHeader?, inputStream: InputStream) {
        this.sbh = sbh
        readObject(inputStream)
    }

    /**
     * Reads the facial record from an input stream. Note that the standard biometric header
     * has already been read.
     * 
     * @param inputStream the input stream
     */
    @Throws(IOException::class)
    override fun readObject(inputStream: InputStream) {
        val dataInputStream =
            if (inputStream is DataInputStream) inputStream else DataInputStream(inputStream)

        /* Facial Record Header (14) */
        val fac0 = dataInputStream.readInt() // header (e.g. "FAC", 0x00)						/* 4 */
        if (fac0 != FORMAT_IDENTIFIER) {
            LOGGER.log(Level.WARNING, "'FAC' marker expected! Found " + Integer.toHexString(fac0))

            if (fac0 == 0x0000000C) {
                /* Magic JP2 header. Best effort, assume this is a single image. */

                val bOut = ByteArrayOutputStream()
                val dataOutputStream = DataOutputStream(bOut)
                dataOutputStream.writeInt(fac0)

                val imageLength = dataInputStream.readShort().toInt()

                dataOutputStream.writeShort(imageLength)

                var totalBytesRead = 0
                while (totalBytesRead < imageLength) {
                    val buffer = ByteArray(2048)
                    val chunkSize = dataInputStream.read(buffer)
                    if (chunkSize < 0) {
                        break
                    }
                    bOut.write(buffer)
                    totalBytesRead += chunkSize
                }

                /* Construct header with default values. */
                val imageInfo = FaceImageInfo(
                    Gender.UNKNOWN,
                    FaceImageInfo.EyeColor.UNSPECIFIED,
                    0x00,
                    FaceImageInfo.HAIR_COLOR_UNSPECIFIED,
                    FaceImageInfo.EXPRESSION_UNSPECIFIED.toInt(),
                    intArrayOf(0, 0, 0),
                    intArrayOf(0, 0, 0),
                    FaceImageInfo.IMAGE_DATA_TYPE_JPEG2000,
                    FaceImageInfo.IMAGE_COLOR_SPACE_UNSPECIFIED,
                    FaceImageInfo.SOURCE_TYPE_UNSPECIFIED,
                    0x00,
                    0,
                    arrayOf<FaceImageInfo.FeaturePoint?>(),
                    0,
                    0,
                    ByteArrayInputStream(bOut.toByteArray()),
                    imageLength,
                    FaceImageInfo.IMAGE_DATA_TYPE_JPEG2000
                )
                add(imageInfo)
                return
            }
        }

        val version =
            dataInputStream.readInt() // version in ASCII (e.g. "010" 0x00)			/* + 4 = 8 */
        require(version == VERSION_NUMBER) {
            "'010' version number expected! Found " + Integer.toHexString(
                version
            )
        }

        val recordLength = dataInputStream.readInt().toLong() and 0xFFFFFFFFL /* + 4 = 12 */
        val headerLength: Long = 14 /* 4 + 4 + 4 + 2 */
        val dataLength = recordLength - headerLength

        var constructedDataLength = 0L

        val count = dataInputStream.readUnsignedShort() /* + 2 = 14 */

        for (i in 0..<count) {
            val imageInfo = FaceImageInfo(inputStream)
            constructedDataLength += imageInfo.getRecordLength()
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
     * Writes the facial record to an output stream. Note that the standard biometric header
     * (part of CBEFF structure) is not written here.
     * 
     * @param outputStream an output stream
     */
    @Throws(IOException::class)
    override fun writeObject(outputStream: OutputStream?) {
        val headerLength = 14 /* 4 + 4 + 4 + 2 (Section 5.4 of ISO/IEC 19794-5) */

        var dataLength: Long = 0
        val faceImageInfos = getSubRecords()
        for (faceImageInfo in faceImageInfos) {
            dataLength += faceImageInfo.getRecordLength()
        }

        val recordLength = headerLength + dataLength

        val dataOut =
            if (outputStream is DataOutputStream) outputStream else DataOutputStream(outputStream)

        dataOut.writeInt(FORMAT_IDENTIFIER) /* 4 */
        dataOut.writeInt(VERSION_NUMBER) /* + 4 = 8 */

        /*
         * The (4 byte) Length of Record field shall
         * be the combined length in bytes for the record.
         * This is the entire length of the record including
         * the Facial Record Header and Facial Record Data.
         */
        dataOut.writeInt((recordLength and 0x00000000FFFFFFFFL).toInt()) /* + 4 = 12 */

        /* Number of facial record data blocks. */
        dataOut.writeShort(faceImageInfos.size) /* + 2 = 14 */

        for (faceImageInfo in faceImageInfos) {
            faceImageInfo.writeObject(dataOut)
        }
    }

    val standardBiometricHeader: StandardBiometricHeader
        /**
         * Returns the standard biometric header of this biometric data block.
         * 
         * @return the standard biometric header
         */
        get() {
            if (sbh == null) {
                val biometricType =
                    byteArrayOf(CBEFFInfoConstants.BIOMETRIC_TYPE_FACIAL_FEATURES.toByte())
                val biometricSubtype =
                    byteArrayOf(CBEFFInfoConstants.BIOMETRIC_SUBTYPE_NONE.toByte())
                val formatOwner = byteArrayOf(
                    ((StandardBiometricHeader.JTC1_SC37_FORMAT_OWNER_VALUE and 0xFF00) shr 8).toByte(),
                    (StandardBiometricHeader.JTC1_SC37_FORMAT_OWNER_VALUE and 0xFF).toByte()
                )
                val formatType = byteArrayOf(
                    ((StandardBiometricHeader.ISO_19794_FACE_IMAGE_FORMAT_TYPE_VALUE and 0xFF00) shr 8).toByte(),
                    (StandardBiometricHeader.ISO_19794_FACE_IMAGE_FORMAT_TYPE_VALUE and 0xFF).toByte()
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

    val faceImageInfos: MutableList<FaceImageInfo?>
        /**
         * Returns the face image infos embedded in this face info.
         * 
         * @return the embedded face image infos
         */
        get() = getSubRecords()

    /**
     * Adds a face image info to this face info.
     * 
     * @param faceImageInfo the face image info to add
     */
    fun addFaceImageInfo(faceImageInfo: FaceImageInfo?) {
        add(faceImageInfo)
    }

    /**
     * Removes a face image info from this face info.
     * 
     * @param index the index of the face image info to remove
     */
    fun removeFaceImageInfo(index: Int) {
        remove(index)
    }

    override fun toString(): String {
        val result = StringBuilder()
        result.append("FaceInfo [")
        val records = getSubRecords()
        for (record in records) {
            result.append(record.toString())
        }
        result.append("]")
        return result.toString()
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = super.hashCode()
        result = prime * result + (if (sbh == null) 0 else sbh.hashCode())
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

        val other = obj as FaceInfo
        if (sbh == null) {
            return other.sbh == null
        }

        return sbh === other.sbh || sbh!!.equals(other.sbh)
    }

    companion object {
        private val serialVersionUID = -6053206262773400725L

        private val LOGGER: Logger = Logger.getLogger("org.jmrtd")

        /**
         * Facial Record Header 'F', 'A', 'C', 0x00. Section 5.4, Table 2 of ISO/IEC 19794-5.
         */
        private const val FORMAT_IDENTIFIER = 0x46414300

        /**
         * Version number '0', '1', '0', 0x00. Section 5.4, Table 2 of ISO/IEC 19794-5.
         */
        private const val VERSION_NUMBER = 0x30313000
    }
}
