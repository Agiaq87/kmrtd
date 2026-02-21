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
 * $Id: DG4File.java 1905 2025-09-25 08:49:09Z martijno $
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
import kmrtd.lds.iso19794.IrisInfo
import kmrtd.lds.iso39794.IrisImageDataBlock
import net.sf.scuba.tlv.TLVInputStream
import net.sf.scuba.tlv.TLVOutputStream
import org.jmrtd.cbeff.*
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * File structure for the EF_DG4 file.
 * Based on ISO/IEC 19794-6 and ISO/IEC 39794-6.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1905 $
 */
class DG4File : CBEFFDataGroup {
    /**
     * Creates a new file with the specified records.
     * 
     * @param irisInfos records
     */
    constructor(irisInfos: MutableList<IrisInfo?>) : this(irisInfos, true)

    /**
     * Creates a new file with the specified records.
     * 
     * @param irisInfos records
     * @param shouldAddRandomDataIfEmpty indicates whether the encoder should add random data when no templates are present
     * 
     */
    @Deprecated("Use the corresponding factory method for ISO19794 instead")
    constructor(
        irisInfos: MutableList<IrisInfo?>,
        shouldAddRandomDataIfEmpty: Boolean
    ) : super(LDSFile.Companion.EF_DG4_TAG, BiometricEncodingType.ISO_19794, irisInfos, shouldAddRandomDataIfEmpty) {
        this.shouldAddRandomDataIfEmpty = shouldAddRandomDataIfEmpty
    }

    private constructor(
        encodingType: BiometricEncodingType?,
        dataBlocks: MutableList<out BiometricDataBlock?>,
        shouldAddRandomDataIfEmpty: Boolean
    ) : super(
        LDSFile.Companion.EF_DG4_TAG, encodingType, dataBlocks, shouldAddRandomDataIfEmpty
    )

    /**
     * Constructs a new file based on an input stream.
     * 
     * @param inputStream an input stream
     * 
     * @throws IOException on error reading from input stream
     */
    constructor(inputStream: InputStream?) : super(LDSFile.Companion.EF_DG4_TAG, inputStream, false)

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
        return "DG4File [" + super.toString() + "]"
    }

    @get:Deprecated("Use {@link #getSubRecords()} and check with {@code instanceof} instead")
    val irisInfos: MutableList<IrisInfo?>?
        /**
         * Returns the embedded iris infos in this file.
         * 
         * @return iris infos
         * 
         */
        get() = toIrisInfos(getSubRecords())

    public override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (if (shouldAddRandomDataIfEmpty) 1231 else 1237)
        return result
    }

    public override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (!super.equals(obj)) {
            return false
        }
        if (javaClass != obj!!.javaClass) {
            return false
        }

        val other = obj as DG4File
        return shouldAddRandomDataIfEmpty == other.shouldAddRandomDataIfEmpty
    }

    companion object {
        private val serialVersionUID = -1290365855823447586L

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
                            return IrisInfo(sbh, inputStream)
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
                            if (sbh != null && sbh.hasFormatType(StandardBiometricHeader.ISO_19794_IRIS_IMAGE_FORMAT_TYPE_VALUE)) {
                                return IrisInfo(sbh, inputStream)
                            }
                            if (sbh != null && !sbh.hasFormatType(StandardBiometricHeader.ISO_39794_IRIS_IMAGE_FORMAT_TYPE_VALUE)) {
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
                            return IrisImageDataBlock(sbh, inputStream)
                        }
                    })

                return decoders
            }

        private val ISO_19794_ENCODER =
            ISO781611Encoder<BiometricDataBlock?>(object : BiometricDataBlockEncoder<BiometricDataBlock?> {
                @Throws(IOException::class)
                override fun encode(info: BiometricDataBlock?, outputStream: OutputStream?) {
                    if (info is IrisInfo) {
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
                    if (info is IrisImageDataBlock) {
                        val tlvOutputStream =
                            if (outputStream is TLVOutputStream) outputStream else TLVOutputStream(outputStream)
                        tlvOutputStream.writeTag(0xA1)
                        tlvOutputStream.writeValue(info.encoded)
                    }
                }

                override fun getEncodingType(): BiometricEncodingType {
                    return BiometricEncodingType.ISO_39794
                }
            })

        fun createISO19794DG4File(irisInfos: MutableList<IrisInfo?>): DG4File {
            return DG4File(BiometricEncodingType.ISO_19794, irisInfos, false)
        }

        fun createISO39794DG4File(irisImageDataBlocks: MutableList<IrisImageDataBlock?>): DG4File {
            return DG4File(BiometricEncodingType.ISO_39794, irisImageDataBlocks, false)
        }

        private fun toIrisInfos(records: MutableList<BiometricDataBlock?>?): MutableList<IrisInfo?>? {
            if (records == null) {
                return null
            }

            val irisInfos: MutableList<IrisInfo?> = ArrayList<IrisInfo?>(records.size)
            for (record in records) {
                if (record is IrisInfo) {
                    irisInfos.add(record)
                }
            }
            return irisInfos
        }
    }
}
