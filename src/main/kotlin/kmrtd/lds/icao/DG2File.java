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

package kmrtd.lds.icao;

import net.sf.scuba.tlv.TLVInputStream;
import net.sf.scuba.tlv.TLVOutputStream;

import kmrtd.cbeff.BiometricDataBlock;
import kmrtd.cbeff.BiometricDataBlockDecoder;
import kmrtd.cbeff.BiometricDataBlockEncoder;
import kmrtd.cbeff.BiometricEncodingType;
import kmrtd.cbeff.ISO781611;
import kmrtd.cbeff.ISO781611Decoder;
import kmrtd.cbeff.ISO781611Encoder;
import kmrtd.cbeff.StandardBiometricHeader;
import kmrtd.lds.CBEFFDataGroup;
import kmrtd.lds.iso19794.FaceInfo;
import kmrtd.lds.iso39794.FaceImageDataBlock;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * File structure for the EF_DG2 file.
 * Datagroup 2 contains the facial features of the document holder.
 * Based on ISO/IEC 19794-5 and ISO/IEC 39794-5.
 *
 * @author The JMRTD team (info@jmrtd.org)
 * @version $Revision: 1905 $
 */
public class DG2File extends CBEFFDataGroup {

    private static final long serialVersionUID = 414300652684010416L;

    private static final ISO781611Decoder<BiometricDataBlock> DECODER = new ISO781611Decoder<BiometricDataBlock>(getDecoderMap());
    private static final ISO781611Encoder<BiometricDataBlock> ISO_19794_ENCODER = new ISO781611Encoder<BiometricDataBlock>(new BiometricDataBlockEncoder<BiometricDataBlock>() {

        @Override
        public void encode(BiometricDataBlock info, OutputStream outputStream) throws IOException {
            if (info instanceof FaceInfo) {
                ((FaceInfo) info).writeObject(outputStream);
            }
        }

        @Override
        public BiometricEncodingType getEncodingType() {
            return BiometricEncodingType.ISO_19794;
        }
    });
    private static final ISO781611Encoder<BiometricDataBlock> ISO_39794_ENCODER = new ISO781611Encoder<BiometricDataBlock>(new BiometricDataBlockEncoder<BiometricDataBlock>() {

        @Override
        public void encode(BiometricDataBlock info, OutputStream outputStream) throws IOException {
            if (info instanceof FaceImageDataBlock) {
                TLVOutputStream tlvOutputStream = outputStream instanceof TLVOutputStream ? (TLVOutputStream) outputStream : new TLVOutputStream(outputStream);
                byte[] encodedFaceImageDataBlock = ((FaceImageDataBlock) info).getEncoded();
                tlvOutputStream.writeTag(0xA1);
                tlvOutputStream.writeValue(encodedFaceImageDataBlock);
            }
        }

        @Override
        public BiometricEncodingType getEncodingType() {
            return BiometricEncodingType.ISO_39794;
        }
    });

    /**
     * Creates a new file with the specified records.
     *
     * @param faceInfos records
     * @deprecated Use the corresponding factory method for ISO19794 instead
     */
    @Deprecated
    public DG2File(List<FaceInfo> faceInfos) {
        this(BiometricEncodingType.ISO_19794, faceInfos);
    }

    /**
     * Creates a new file based on an input stream.
     *
     * @param inputStream an input stream
     * @throws IOException on error reading from input stream
     */
    public DG2File(InputStream inputStream) throws IOException {
        super(EF_DG2_TAG, inputStream, false);
    }

    private DG2File(BiometricEncodingType encodingType, List<? extends BiometricDataBlock> biometricDataBlocks) {
        super(EF_DG2_TAG, encodingType, biometricDataBlocks, false);
    }

    private static Map<Integer, BiometricDataBlockDecoder<BiometricDataBlock>> getDecoderMap() {
        Map<Integer, BiometricDataBlockDecoder<BiometricDataBlock>> decoders = new HashMap<Integer, BiometricDataBlockDecoder<BiometricDataBlock>>();

        /* 5F2E */
        decoders.put(ISO781611.BIOMETRIC_DATA_BLOCK_TAG, new BiometricDataBlockDecoder<BiometricDataBlock>() {
            public BiometricDataBlock decode(InputStream inputStream, StandardBiometricHeader sbh, int index, int length) throws IOException {
                return new FaceInfo(sbh, inputStream);
            }
        });

        /* 7F2E */
        decoders.put(ISO781611.BIOMETRIC_DATA_BLOCK_CONSTRUCTED_TAG, new BiometricDataBlockDecoder<BiometricDataBlock>() {
            public BiometricDataBlock decode(InputStream inputStream, StandardBiometricHeader sbh, int index, int length) throws IOException {
                if (sbh != null && sbh.hasFormatType(StandardBiometricHeader.ISO_19794_FACE_IMAGE_FORMAT_TYPE_VALUE)) {
                    return new FaceInfo(sbh, inputStream);
                }
                if (sbh != null && !sbh.hasFormatType(StandardBiometricHeader.ISO_39794_FACE_IMAGE_FORMAT_TYPE_VALUE)) {
                    LOGGER.warning("Unexpected format type in standard biometric header " + sbh + ", assuming ISO-39794 encoding");
                }
                TLVInputStream tlvInputStream = inputStream instanceof TLVInputStream ? (TLVInputStream) inputStream : new TLVInputStream(inputStream);
                int tag = tlvInputStream.readTag(); // 0xA1
                if (tag != ISO781611.BIOMETRIC_HEADER_TEMPLATE_BASE_TAG) {
                    /* ISO/IEC 39794-5 Application Profile for eMRTDs Version – 1.00: Table 2: Data Structure under DO7F2E. */
                    LOGGER.warning("Expected tag A1, found " + Integer.toHexString(tag));
                }
                tlvInputStream.readLength();
                return new FaceImageDataBlock(sbh, inputStream);
            }
        });

        return decoders;
    }

    public static DG2File createISO19794DG2File(List<FaceInfo> faceInfos) {
        return new DG2File(BiometricEncodingType.ISO_19794, faceInfos);
    }

    public static DG2File createISO39794DG2File(List<FaceImageDataBlock> faceImageDataBlocks) {
        return new DG2File(BiometricEncodingType.ISO_39794, faceImageDataBlocks);
    }

    private static List<FaceInfo> toFaceInfos(List<BiometricDataBlock> records) {
        if (records == null) {
            return null;
        }

        List<FaceInfo> faceInfos = new ArrayList<FaceInfo>(records.size());
        for (BiometricDataBlock record : records) {
            if (record instanceof FaceInfo) {
                faceInfos.add((FaceInfo) record);
            }
        }
        return faceInfos;
    }

    @Override
    public ISO781611Decoder<BiometricDataBlock> getDecoder() {
        return DECODER;
    }

    @Override
    public ISO781611Encoder<BiometricDataBlock> getEncoder() {
        if (encodingType == null) {
            return ISO_19794_ENCODER;
        }
        switch (encodingType) {
            case ISO_19794:
                return ISO_19794_ENCODER;
            case ISO_39794:
                return ISO_39794_ENCODER;
            default:
                return ISO_19794_ENCODER;
        }
    }

    /**
     * Returns a textual representation of this file.
     *
     * @return a textual representation of this file
     */
    @Override
    public String toString() {
        return "DG2File [" + super.toString() + "]";
    }

    /**
     * Returns the face infos embedded in this file.
     *
     * @return face infos
     * @deprecated Use {@link #getSubRecords()} and check with {@code instanceof} instead
     */
    @Deprecated
    public List<FaceInfo> getFaceInfos() {
        return toFaceInfos(getSubRecords());
    }
}
