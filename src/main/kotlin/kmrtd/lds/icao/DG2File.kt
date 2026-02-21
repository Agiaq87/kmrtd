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
 * $Id: DG2File.java 1905 2025-09-25 08:49:09Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.lds.icao

import kmrtd.lds.CBEFFDataGroup
import kmrtd.lds.LDSFile
import kmrtd.lds.iso19794.FaceInfo
import kmrtd.lds.iso39794.FaceImageDataBlock
import net.sf.scuba.tlv.TLVInputStream
import net.sf.scuba.tlv.TLVOutputStream
import org.jmrtd.cbeff.*
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * File structure for the EF_DG2 file.
 * Datagroup 2 contains the facial features of the document holder.
 * Based on ISO/IEC 19794-5 and ISO/IEC 39794-5.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1905 $
 */
class DG2File : CBEFFDataGroup {
    /**
     * Creates a new file with the specified records.
     * 
     * @param faceInfos records
     * 
     */
    @Deprecated("Use the corresponding factory method for ISO19794 instead")
    constructor(faceInfos: MutableList<FaceInfo?>) : this(BiometricEncodingType.ISO_19794, faceInfos)

    /**
     * Creates a new file based on an input stream.
     * 
     * @param inputStream an input stream
     * 
     * @throws IOException on error reading from input stream
     */
    constructor(inputStream: InputStream?) : super(LDSFile.Companion.EF_DG2_TAG, inputStream, false)

    private constructor(
        encodingType: BiometricEncodingType?,
        biometricDataBlocks: MutableList<out BiometricDataBlock?>
    ) : super(
        LDSFile.Companion.EF_DG2_TAG, encodingType, biometricDataBlocks, false
    )

    val encoder: ISO781611Encoder<BiometricDataBlock?>
        get() {
            if (encodingType == null) {
                return ISO_19794_ENCODER
            }
            when (encodingType) {
                BiometricEncodingType.ISO_19794 -> return ISO_19794_ENCODER
                BiometricEncodingType.ISO_39794 -> return ISO_39794_ENCODER
                else -> return ISO_19794_ENCODER
            }
        }

    /**
     * Returns a textual representation of this file.
     * 
     * @return a textual representation of this file
     */
    public override fun toString(): String {
        return "DG2File [" + super.toString() + "]"
    }

    @get:Deprecated("Use {@link #getSubRecords()} and check with {@code instanceof} instead")
    val faceInfos: MutableList<FaceInfo?>?
        /**
         * Returns the face infos embedded in this file.
         * 
         * @return face infos
         * 
         */
        get() = toFaceInfos(getSubRecords())

    companion object {
        private const val serialVersionUID = 414300652684010416L

        val decoder: ISO781611Decoder<BiometricDataBlock?> = ISO781611Decoder<BiometricDataBlock?>(
            decoderMap
        )
            get() = Companion.field

        private val decoderMap: MutableMap<Int?, BiometricDataBlockDecoder<BiometricDataBlock?>?>
            get() {
                val decoders: MutableMap<Int?, BiometricDataBlockDecoder<BiometricDataBlock?>?> =
                    HashMap<Int?, BiometricDataBlockDecoder<BiometricDataBlock?>?>()

                /* 5F2E */
                decoders.put(
                    ISO781611.BIOMETRIC_DATA_BLOCK_TAG,
                    object : BiometricDataBlockDecoder<BiometricDataBlock?> {
                        @Throws(IOException::class)
                        override fun decode(
                            inputStream: InputStream?,
                            sbh: StandardBiometricHeader?,
                            index: Int,
                            length: Int
                        ): BiometricDataBlock {
                            return FaceInfo(sbh, inputStream)
                        }
                    })

                /* 7F2E */
                decoders.put(
                    ISO781611.BIOMETRIC_DATA_BLOCK_CONSTRUCTED_TAG,
                    object : BiometricDataBlockDecoder<BiometricDataBlock?> {
                        @Throws(IOException::class)
                        override fun decode(
                            inputStream: InputStream?,
                            sbh: StandardBiometricHeader?,
                            index: Int,
                            length: Int
                        ): BiometricDataBlock {
                            if (sbh != null && sbh.hasFormatType(StandardBiometricHeader.ISO_19794_FACE_IMAGE_FORMAT_TYPE_VALUE)) {
                                return FaceInfo(sbh, inputStream)
                            }
                            if (sbh != null && !sbh.hasFormatType(StandardBiometricHeader.ISO_39794_FACE_IMAGE_FORMAT_TYPE_VALUE)) {
                                LOGGER.warning("Unexpected format type in standard biometric header " + sbh + ", assuming ISO-39794 encoding")
                            }
                            val tlvInputStream =
                                if (inputStream is TLVInputStream) inputStream else TLVInputStream(
                                    inputStream
                                )
                            val tag = tlvInputStream.readTag() // 0xA1
                            if (tag != ISO781611.BIOMETRIC_HEADER_TEMPLATE_BASE_TAG) {
                                /* ISO/IEC 39794-5 Application Profile for eMRTDs Version – 1.00: Table 2: Data Structure under DO7F2E. */
                                LOGGER.warning(
                                    "Expected tag A1, found " + Integer.toHexString(
                                        tag
                                    )
                                )
                            }
                            tlvInputStream.readLength()
                            return FaceImageDataBlock(sbh, inputStream)
                        }
                    })

                return decoders
            }

        private val ISO_19794_ENCODER =
            ISO781611Encoder<BiometricDataBlock?>(object : BiometricDataBlockEncoder<BiometricDataBlock?> {
                @Throws(IOException::class)
                override fun encode(info: BiometricDataBlock?, outputStream: OutputStream?) {
                    if (info is FaceInfo) {
                        info.writeObject(outputStream)
                    }
                }

                override fun getEncodingType(): BiometricEncodingType {
                    return BiometricEncodingType.ISO_19794
                }
            })

        private val ISO_39794_ENCODER =
            ISO781611Encoder<BiometricDataBlock?>(object : BiometricDataBlockEncoder<BiometricDataBlock?> {
                @Throws(IOException::class)
                override fun encode(info: BiometricDataBlock?, outputStream: OutputStream?) {
                    if (info is FaceImageDataBlock) {
                        val tlvOutputStream =
                            if (outputStream is TLVOutputStream) outputStream else TLVOutputStream(outputStream)
                        val encodedFaceImageDataBlock = info.encoded
                        tlvOutputStream.writeTag(0xA1)
                        tlvOutputStream.writeValue(encodedFaceImageDataBlock)
                    }
                }

                override fun getEncodingType(): BiometricEncodingType {
                    return BiometricEncodingType.ISO_39794
                }
            })

        fun createISO19794DG2File(faceInfos: MutableList<FaceInfo?>): DG2File {
            return DG2File(BiometricEncodingType.ISO_19794, faceInfos)
        }

        fun createISO39794DG2File(faceImageDataBlocks: MutableList<FaceImageDataBlock?>): DG2File {
            return DG2File(BiometricEncodingType.ISO_39794, faceImageDataBlocks)
        }

        private fun toFaceInfos(records: MutableList<BiometricDataBlock?>?): MutableList<FaceInfo?>? {
            if (records == null) {
                return null
            }

            val faceInfos: MutableList<FaceInfo?> = ArrayList<FaceInfo?>(records.size)
            for (record in records) {
                if (record is FaceInfo) {
                    faceInfos.add(record)
                }
            }
            return faceInfos
        }
    }
}
