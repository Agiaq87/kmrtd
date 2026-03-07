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
 * $Id: ISO781611Encoder.java 1897 2025-05-27 12:34:36Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.cbeff

import net.sf.scuba.tlv.TLVOutputStream
import java.io.IOException
import java.io.OutputStream

/**
 * ISO 7816-11 encoder for BIR.
 * 
 * @param <B> the biometric data block type to use
 * @author The JMRTD team (info@jmrtd.org)
 * @version $Revision: 1897 $
</B> */
class ISO781611Encoder<B : BiometricDataBlock>(private val bdbEncoder: BiometricDataBlockEncoder<B>) {

    private val encodingType: BiometricEncodingType = bdbEncoder.encodingType

    /**
     * Writes a BIT group to an output stream.
     * 
     * @param cbeffInfo    a CBEFF info containing the BIT group
     * @param outputStream the output stream to write to
     * @throws IOException if something goes wrong
     */
    @Throws(IOException::class)
    fun encode(cbeffInfo: CBEFFInfo<*>?, outputStream: OutputStream?) {
        if (cbeffInfo is SimpleCBEFFInfo<*>) {
            writeBITGroup(
                mutableListOf(cbeffInfo),
                outputStream
            )
        } else if (cbeffInfo is ComplexCBEFFInfo<*>) {
            writeBITGroup(cbeffInfo.getSubRecords().toMutableList(), outputStream)
        }
    }

    /**
     * Writes a BIT group to a stream.
     * 
     * @param records      the records of the BIT group
     * @param outputStream the stream to write to
     * @throws IOException on error writing to the stream
     */
    @Throws(IOException::class)
    private fun writeBITGroup(records: MutableList<CBEFFInfo<*>>, outputStream: OutputStream?) {
        val tlvOut =
            outputStream as? TLVOutputStream ?: TLVOutputStream(outputStream)
        tlvOut.writeTag(ISO781611.BIOMETRIC_INFORMATION_GROUP_TEMPLATE_TAG) /* 7F61 */
        tlvOut.writeTag(ISO781611.BIOMETRIC_INFO_COUNT_TAG) /* 0x02 */
        val count = records.size
        tlvOut.writeValue(byteArrayOf(count.toByte()))

        for (index in 0..<count) {
            val simpleCBEFFInfo = records[index] as SimpleCBEFFInfo<B>
            writeBIT(tlvOut, index, simpleCBEFFInfo)
        }
        tlvOut.writeValueEnd() /* BIOMETRIC_INFORMATION_GROUP_TEMPLATE_TAG, i.e. 7F61 */
    }

    /**
     * Writes a single BIT to a stream.
     * 
     * @param tlvOutputStream the stream to write to
     * @param index           the index of the BIT within the BIT group
     * @param cbeffInfo       the BIT
     * @throws IOException on error writing to the stream
     */
    @Throws(IOException::class)
    private fun writeBIT(
        tlvOutputStream: TLVOutputStream,
        index: Int,
        cbeffInfo: SimpleCBEFFInfo<B>
    ) {
        tlvOutputStream.writeTag(ISO781611.BIOMETRIC_INFORMATION_TEMPLATE_TAG) /* 7F60 */
        writeBHT(tlvOutputStream, index, cbeffInfo)
        writeBiometricDataBlock(tlvOutputStream, cbeffInfo.biometricDataBlock)
        tlvOutputStream.writeValueEnd() /* BIOMETRIC_INFORMATION_TEMPLATE_TAG, i.e. 7F60 */
    }

    /**
     * Writes a a header for a single BIT to a stream.
     * 
     * @param tlvOutputStream the stream to write to
     * @param index           the index of the BIT within the BIT group
     * @param cbeffInfo       the BIT to write
     * @throws IOException on error writing to the stream
     */
    @Throws(IOException::class)
    private fun writeBHT(
        tlvOutputStream: TLVOutputStream,
        index: Int,
        cbeffInfo: SimpleCBEFFInfo<B>
    ) {
        tlvOutputStream.writeTag((ISO781611.BIOMETRIC_HEADER_TEMPLATE_BASE_TAG /* + index */) and 0xFF) /* A1 */

        val bdb = cbeffInfo.biometricDataBlock

        /* SBH */
        val sbh = bdb.standardBiometricHeader
        val elements = sbh.sortedElements
        for (entry in elements.entries) {
            tlvOutputStream.writeTag(entry.key)
            tlvOutputStream.writeValue(entry.value)
        }
        tlvOutputStream.writeValueEnd() /* BIOMETRIC_HEADER_TEMPLATE_BASE_TAG, i.e. A1 */
    }

    /**
     * Writes the contents of a single BIT to a stream.
     * 
     * @param tlvOutputStream the stream to write to
     * @param bdb             the contents to write
     * @throws IOException on error writing to the stream
     */
    @Throws(IOException::class)
    private fun writeBiometricDataBlock(tlvOutputStream: TLVOutputStream, bdb: B) {
        tlvOutputStream.writeTag(BiometricEncodingType.toBDBTag(encodingType)) /* 5F2E or 7F2E */

        bdbEncoder.encode(bdb, tlvOutputStream)
        tlvOutputStream.writeValueEnd() /* BIOMETRIC_DATA_BLOCK_TAG, i.e. 5F2E or 7F2E */
    }
}
