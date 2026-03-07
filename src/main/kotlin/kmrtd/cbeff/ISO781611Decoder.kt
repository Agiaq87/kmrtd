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
 * $Id: ISO781611Decoder.java 1892 2025-03-18 15:15:52Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.cbeff

import kmrtd.support.asTLV
import net.sf.scuba.tlv.TLVInputStream
import net.sf.scuba.tlv.TLVUtil
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.util.logging.Logger

/**
 * ISO 7816-11 decoder for BIR.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * @version $Revision: 1892 $
 * @since 0.4.7
 */
class ISO781611Decoder<B : BiometricDataBlock>(private val bdbDecoders: Map<Int, BiometricDataBlockDecoder<B>>) {
    var encodingType: BiometricEncodingType? = null
        private set

    /**
     * Constructs an ISO7816-11 decoder that uses the given BDB decoder.
     * 
     * @param bdbDecoder the BDB decoder to use
     */
    constructor(bdbDecoder: BiometricDataBlockDecoder<B>) : this(toMap<B>(bdbDecoder))


    /**
     * Reads a BIT group from an input stream.
     * 
     * @param inputStream the input stream to read from
     * @return a complex CBEFF info representing the BIT group
     * @throws IOException if reading fails
     */
    @Throws(IOException::class)
    fun decode(inputStream: InputStream): ComplexCBEFFInfo<B> =
        readBITGroup(inputStream)

    /**
     * Reads a BIT group from an input stream.
     * 
     * @param inputStream the input stream to read from
     * @return a complex CBEFF info representing the BIT group
     * @throws IOException if reading fails
     */
    @Throws(IOException::class)
    private fun readBITGroup(inputStream: InputStream): ComplexCBEFFInfo<B> {
        val tlvIn = inputStream.asTLV()
        val tag = tlvIn.readTag()
        require(tag == ISO781611.BIOMETRIC_INFORMATION_GROUP_TEMPLATE_TAG) {
            "Expected tag " + Integer.toHexString(
                ISO781611.BIOMETRIC_INFORMATION_GROUP_TEMPLATE_TAG
            ) + ", found " + Integer.toHexString(tag)
        }

        val length = tlvIn.readLength()
        return readBITGroup(tag, length, inputStream)
    }

    /**
     * Reads a BIT group value from an input stream.
     * 
     * @param tag         the tag that was already read, which should be a BIT group tag (`7F61`)
     * @param length      the length that was already read
     * @param inputStream the input stream from which to read the value
     * @return CBEFF info representing the BIT group that was read
     * @throws IOException on error reading from the stream
     */
    @Throws(IOException::class)
    private fun readBITGroup(
        tag: Int,
        length: Int,
        inputStream: InputStream
    ): ComplexCBEFFInfo<B> {
        val tlvIn = inputStream.asTLV()
        val result = ComplexCBEFFInfo<B>()
        require(tag == ISO781611.BIOMETRIC_INFORMATION_GROUP_TEMPLATE_TAG) {
            "Expected tag " + Integer.toHexString(
                ISO781611.BIOMETRIC_INFORMATION_GROUP_TEMPLATE_TAG
            ) + ", found " + Integer.toHexString(tag)
        }
        val bitCountTag = tlvIn.readTag()
        require(bitCountTag == ISO781611.BIOMETRIC_INFO_COUNT_TAG) {
            "Expected tag BIOMETRIC_INFO_COUNT_TAG (" + Integer.toHexString(
                ISO781611.BIOMETRIC_INFO_COUNT_TAG
            ) + ") in CBEFF structure, found " + Integer.toHexString(bitCountTag)
        }
        val bitCountLength = tlvIn.readLength()
        require(bitCountLength == 1) { "BIOMETRIC_INFO_COUNT should have length 1, found length $bitCountLength" }
        val bitCount = (tlvIn.readValue()[0].toInt() and 0xFF)
        for (i in 0..<bitCount) {
            result.add(readBIT(inputStream, i)!!)
        }

        /* TODO: possibly more content, e.g. 0x53 tag with random as per ICAO 9303 Supplement R7-p1_v2_sIII_0057 */
        return result
    }

    /**
     * Reads a single BIT from the input stream.
     * 
     * @param inputStream the input stream to read from
     * @param index       index of this BIT within the BIT group
     * @return a CBEFF info representing the BIT
     * @throws IOException if reading fails
     */
    @Throws(IOException::class)
    private fun readBIT(inputStream: InputStream, index: Int): CBEFFInfo<B>? {
        val tlvIn = inputStream.asTLV()
        val tag = tlvIn.readTag()
        val length = tlvIn.readLength()
        return readBIT(tag, length, inputStream, index)
    }

    /**
     * Reads a single BIT from the input stream.
     * 
     * @param tag         the tag that was already read
     * @param length      the length that was already read
     * @param inputStream the stream to read the BIT value from
     * @param index       the index of the BIT withing the BIT group
     * @return a CBEFF info representing the BIT
     * @throws IOException on error reading from the stream
     */
    @Throws(IOException::class)
    private fun readBIT(
        tag: Int,
        length: Int,
        inputStream: InputStream,
        index: Int
    ): CBEFFInfo<B>? {
        val tlvIn = inputStream.asTLV()
        require(tag == ISO781611.BIOMETRIC_INFORMATION_TEMPLATE_TAG /* 7F60 */) {
            "Expected tag BIOMETRIC_INFORMATION_TEMPLATE_TAG (" + Integer.toHexString(
                ISO781611.BIOMETRIC_INFORMATION_TEMPLATE_TAG
            ) + "), found " + Integer.toHexString(tag) + ", index is " + index
        }

        val bhtTag = tlvIn.readTag()
        val bhtLength = tlvIn.readLength()

        if ((bhtTag == ISO781611.SMT_TAG)) {
            /* The BIT is protected... */
            readStaticallyProtectedBIT(inputStream, bhtTag, bhtLength, index)
        } else if ((bhtTag and 0xA0) == 0xA0) {
            val sbh = readBHT(inputStream, bhtTag, bhtLength, index)
            val bdb = readBiometricDataBlock(inputStream, sbh, index)
            return SimpleCBEFFInfo(bdb)
        } else {
            throw IllegalArgumentException("Unsupported template tag: " + Integer.toHexString(bhtTag))
        }

        return null // FIXME
    }

    /**
     * Reads the biometric header template from an input stream.
     * A1, A2, ...
     * Will contain DOs as described in ISO 7816-11 Annex C.
     * 
     * @param inputStream the stream to read from
     * @param bhtTag      the tag of the biometric header
     * @param bhtLength   the length of the header
     * @param index       the index
     * @return the standard biometric header
     * @throws IOException on error reading from the stream
     */
    @Throws(IOException::class)
    private fun readBHT(
        inputStream: InputStream,
        bhtTag: Int,
        bhtLength: Int,
        index: Int
    ): StandardBiometricHeader {
        val tlvIn = inputStream.asTLV()
        val expectedBHTTag =
            (ISO781611.BIOMETRIC_HEADER_TEMPLATE_BASE_TAG /* + index */) and 0xFF
        if (bhtTag != expectedBHTTag) {
            LOGGER.warning(
                "Expected tag ${Integer.toHexString(expectedBHTTag)}, found ${
                    Integer.toHexString(
                    bhtTag
                    )
                }"
            )
        }
        val elements: MutableMap<Int, ByteArray?> = mutableMapOf()
        var bytesRead = 0
        while (bytesRead < bhtLength) {
            val tag = tlvIn.readTag()
            bytesRead += TLVUtil.getTagLength(tag)
            val length = tlvIn.readLength()
            bytesRead += TLVUtil.getLengthLength(length)
            val value = tlvIn.readValue()
            bytesRead += value.size
            elements[tag] = value
        }
        return StandardBiometricHeader(elements)
    }

    /**
     * Reads a biometric information template protected with secure messaging.
     * Described in ISO7816-11 Annex D.
     * 
     * @param inputStream source to read from
     * @param tag         should be `0x7D`
     * @param length      the length of the BIT
     * @param index       index of the template
     * @throws IOException on failure
     */
    @Throws(IOException::class)
    private fun readStaticallyProtectedBIT(
        inputStream: InputStream,
        tag: Int,
        length: Int,
        index: Int
    ) {
        val tlvBHTIn = TLVInputStream(ByteArrayInputStream(decodeSMTValue(inputStream)))
        try {
            val headerTemplateTag = tlvBHTIn.readTag()
            val headerTemplateLength = tlvBHTIn.readLength()
            val sbh = readBHT(tlvBHTIn, headerTemplateTag, headerTemplateLength, index)
            val biometricDataBlockIn: InputStream =
                ByteArrayInputStream(decodeSMTValue(inputStream))
            readBiometricDataBlock(biometricDataBlockIn, sbh, index)
        } finally {
            tlvBHTIn.close()
        }
    } /* FIXME: return type??? */

    /**
     * Decodes a (protected) data object.
     * Encrypted payloads are not currently supported.
     * 
     * @param inputStream the stream to read from
     * @return the decoded value
     * @throws IOException on error reading from the stream
     */
    @Throws(IOException::class)
    private fun decodeSMTValue(inputStream: InputStream): ByteArray? {
        val tlvIn = inputStream.asTLV()
        val doTag = tlvIn.readTag()
        val doLength = tlvIn.readLength()
        var skippedBytes = 0L
        when (doTag) {
            ISO781611.SMT_DO_PV ->                 /* NOTE: Plain value, just return whatever is in the payload */
                return tlvIn.readValue()

            ISO781611.SMT_DO_CG ->                 /* NOTE: content of payload is encrypted */
                throw IllegalStateException("Access denied. Biometric Information Template is statically protected.")

            ISO781611.SMT_DO_CC -> {
                /* NOTE: payload contains a MAC */
                while (skippedBytes < doLength) {
                    skippedBytes += tlvIn.skip(doLength.toLong())
                }
                return null
            }

            ISO781611.SMT_DO_DS -> {
                /* NOTE: payload contains a signature */

                while (skippedBytes < doLength) {
                    skippedBytes += tlvIn.skip(doLength.toLong())
                }
                return null
            }

            else -> {
                LOGGER.info("Unsupported data object tag " + Integer.toHexString(doTag))
                return null
            }
        }
    }

    /**
     * Reads a biometric data block from an input stream.
     * 
     * @param inputStream the stream to read from
     * @param sbh         the biometric header that was already read
     * @param index       the index of the biometric data block within the BIT group
     * @return the biometric data block
     * @throws IOException on error reading from the stream
     */
    @Throws(IOException::class)
    private fun readBiometricDataBlock(
        inputStream: InputStream,
        sbh: StandardBiometricHeader?,
        index: Int
    ): B {
        val tlvIn = inputStream.asTLV()
        val bioDataBlockTag = tlvIn.readTag()
        require(
            !(bioDataBlockTag != ISO781611.BIOMETRIC_DATA_BLOCK_TAG /* 5F2E */ &&
                    bioDataBlockTag != ISO781611.BIOMETRIC_DATA_BLOCK_CONSTRUCTED_TAG /* 7F2E */)
        ) {
            ("Expected tag BIOMETRIC_DATA_BLOCK_TAG (" + Integer.toHexString(
                ISO781611.BIOMETRIC_DATA_BLOCK_TAG
            )
                    + ") or BIOMETRIC_DATA_BLOCK_CONSTRUCTED_ALT (" + Integer.toHexString(ISO781611.BIOMETRIC_DATA_BLOCK_CONSTRUCTED_TAG)
                    + "), found " + Integer.toHexString(bioDataBlockTag))
        }
        encodingType = BiometricEncodingType.fromBDBTag(bioDataBlockTag)
        val length = tlvIn.readLength()
        requireNotNull(bdbDecoders[bioDataBlockTag]) {
            "No decoder for biometric data block tag " + Integer.toHexString(
                bioDataBlockTag
            )
        }
        val bdbDecoder: BiometricDataBlockDecoder<B> = bdbDecoders[bioDataBlockTag]!!
        return bdbDecoder.decode(inputStream, sbh, index, length)
    }

    companion object {
        private val LOGGER: Logger = Logger.getLogger("kmrtd.cbeff")

        private fun <R : BiometricDataBlock> toMap(bdbDecoder: BiometricDataBlockDecoder<R>): Map<Int, BiometricDataBlockDecoder<R>> =
            buildMap {
                //val bdbDecoders: MutableMap<Int, BiometricDataBlockDecoder<R>> = mutableMapOf()
                put(ISO781611.BIOMETRIC_DATA_BLOCK_TAG, bdbDecoder) /* 5F2E */
                put(ISO781611.BIOMETRIC_DATA_BLOCK_CONSTRUCTED_TAG, bdbDecoder) /* 7F2E */
                //return bdbDecoders
            }
    }
}
