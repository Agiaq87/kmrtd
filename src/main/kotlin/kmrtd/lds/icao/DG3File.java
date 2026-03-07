/*
 * JMRTD - A Java API for accessing machine readable travel documents.
 *
 * Copyright (C) 2006 - 2017  The JMRTD team
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
 * $Id: DG3File.java 1905 2025-09-25 08:49:09Z martijno $
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
import kmrtd.lds.iso19794.FingerInfo;
import kmrtd.lds.iso39794.FingerImageDataBlock;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * File structure for the EF_DG3 file.
 * based on ISO/IEC 19794-4 and ISO/IEC 39794-4.
 *
 * @author The JMRTD team (info@jmrtd.org)
 * @version $Revision: 1905 $
 */
public class DG3File extends CBEFFDataGroup {

    private static final long serialVersionUID = -1037522331623814528L;

    private static final ISO781611Decoder<BiometricDataBlock> DECODER = new ISO781611Decoder<BiometricDataBlock>(getDecoderMap());
    private static final ISO781611Encoder<BiometricDataBlock> ISO_19794_ENCODER = new ISO781611Encoder<BiometricDataBlock>(new BiometricDataBlockEncoder<BiometricDataBlock>() {

        @Override
        public void encode(BiometricDataBlock info, OutputStream outputStream) throws IOException {
            if (info instanceof FingerInfo) {
                ((FingerInfo) info).writeObject(outputStream);
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
            if (info instanceof FingerImageDataBlock) {
                TLVOutputStream tlvOutputStream = outputStream instanceof TLVOutputStream ? (TLVOutputStream) outputStream : new TLVOutputStream(outputStream);
                tlvOutputStream.writeTag(0xA1);
                tlvOutputStream.writeValue(((FingerImageDataBlock) info).getEncoded());
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
     * @param fingerInfos records
     * @deprecated Use the corresponding factory method for ISO19794 instead
     */
    @Deprecated
    public DG3File(List<FingerInfo> fingerInfos) {
        this(fingerInfos, true);
    }

    /**
     * Creates a new file with the specified records.
     *
     * @param fingerInfos                records
     * @param shouldAddRandomDataIfEmpty whether to add random data when there are no records to encode
     * @deprecated Use the corresponding factory method for ISO19794 instead
     */
    @Deprecated
    public DG3File(List<FingerInfo> fingerInfos, boolean shouldAddRandomDataIfEmpty) {
        this(BiometricEncodingType.ISO_19794, fingerInfos, shouldAddRandomDataIfEmpty);
    }

    private DG3File(BiometricEncodingType encodingType, List<? extends BiometricDataBlock> dataBlocks, boolean shouldAddRandomDataIfEmpty) {
        super(EF_DG3_TAG, encodingType, dataBlocks, shouldAddRandomDataIfEmpty);
    }

    /**
     * Creates a new file based on an input stream.
     *
     * @param inputStream an input stream
     * @throws IOException on error reading from input stream
     */
    public DG3File(InputStream inputStream) throws IOException {
        super(EF_DG3_TAG, inputStream, false);
    }

    private static Map<Integer, BiometricDataBlockDecoder<BiometricDataBlock>> getDecoderMap() {
        Map<Integer, BiometricDataBlockDecoder<BiometricDataBlock>> decoders = new HashMap<Integer, BiometricDataBlockDecoder<BiometricDataBlock>>();

        /* 5F2E */
        decoders.put(ISO781611.BIOMETRIC_DATA_BLOCK_TAG, new BiometricDataBlockDecoder<BiometricDataBlock>() {
            public BiometricDataBlock decode(InputStream inputStream, StandardBiometricHeader sbh, int index, int length) throws IOException {
                return new FingerInfo(sbh, inputStream);
            }
        });

        /* 7F2E */
        decoders.put(ISO781611.BIOMETRIC_DATA_BLOCK_CONSTRUCTED_TAG, new BiometricDataBlockDecoder<BiometricDataBlock>() {
            public BiometricDataBlock decode(InputStream inputStream, StandardBiometricHeader sbh, int index, int length) throws IOException {
                if (sbh != null && sbh.hasFormatType(StandardBiometricHeader.ISO_19794_FINGER_IMAGE_FORMAT_TYPE_VALUE)) {
                    return new FingerInfo(sbh, inputStream);
                }
                if (sbh != null && !sbh.hasFormatType(StandardBiometricHeader.ISO_39794_FINGER_IMAGE_FORMAT_TYPE_VALUE)) {
                    LOGGER.warning("Unexpected format type in standard biometric header " + sbh + ", assuming ISO-39794 encoding");
                }
                TLVInputStream tlvInputStream = inputStream instanceof TLVInputStream ? (TLVInputStream) inputStream : new TLVInputStream(inputStream);
                int tag = tlvInputStream.readTag(); // 0xA1
                if (tag != ISO781611.BIOMETRIC_HEADER_TEMPLATE_BASE_TAG) {
                    /* ISO/IEC 39794-5 Application Profile for eMRTDs Version – 1.00: Table 2: Data Structure under DO7F2E. */
                    LOGGER.warning("Expected tag A1, found " + Integer.toHexString(tag));
                }
                tlvInputStream.readLength();
                return new FingerImageDataBlock(sbh, inputStream);
            }
        });

        return decoders;
    }

    public static DG3File createISO19794DG3File(List<FingerInfo> fingerInfos) {
        return new DG3File(BiometricEncodingType.ISO_19794, fingerInfos, false);
    }

    public static DG3File createISO39794DG3File(List<FingerImageDataBlock> fingerImageDataBlocks) {
        return new DG3File(BiometricEncodingType.ISO_39794, fingerImageDataBlocks, false);
    }

    private static List<FingerInfo> toFingerInfos(List<BiometricDataBlock> records) {
        if (records == null) {
            return null;
        }

        List<FingerInfo> FingerInfos = new ArrayList<FingerInfo>(records.size());
        for (BiometricDataBlock record : records) {
            if (record instanceof FingerInfo) {
                FingerInfos.add((FingerInfo) record);
            }
        }
        return FingerInfos;
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
        return "DG3File [" + super.toString() + "]";
    }

    /**
     * Returns the finger infos embedded in this file.
     *
     * @return finger infos
     * @deprecated Use {@link #getSubRecords()} and check with {@code instanceof} instead
     */
    @Deprecated
    public List<FingerInfo> getFingerInfos() {
        return toFingerInfos(getSubRecords());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (shouldAddRandomDataIfEmpty ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        DG3File other = (DG3File) obj;
        return shouldAddRandomDataIfEmpty == other.shouldAddRandomDataIfEmpty;
    }
}
