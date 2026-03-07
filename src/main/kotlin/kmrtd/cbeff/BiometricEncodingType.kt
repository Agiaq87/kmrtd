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
 * $Id: BiometricEncodingType.java 1897 2025-05-27 12:34:36Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.cbeff

/**
 * Specifies what encoding (ISO/IEC-19794 or ISO/IEC-39794)
 * was used for biometric data blocks.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * @version $Revision: 1897 $
 */
enum class BiometricEncodingType {
    /**
     * Unknown encoding.
     */
    UNKNOWN,

    /**
     * Uses ISO-19794 records BDB.
     */
    ISO_19794,

    /**
     * Used ISO-39794 records BDB.
     */
    ISO_39794;

    companion object {
        /**
         * Maps tag to encoding type.
         * 
         * @param bioDataBlockTag either `0x5F2E` or `0x7F2E`
         * @return the corresponding type
         */
        fun fromBDBTag(bioDataBlockTag: Int): BiometricEncodingType =
            when (bioDataBlockTag) {
                ISO781611.BIOMETRIC_DATA_BLOCK_TAG ->  /* 5F2E */
                    ISO_19794

                ISO781611.BIOMETRIC_DATA_BLOCK_CONSTRUCTED_TAG ->  /* 7F2E */
                    ISO_39794

                else -> UNKNOWN
            }

        /**
         * Maps encoding type to tag.
         * Defaults to ISO-19794 (`0x5F2E`).
         * 
         * @param encodingType one of the enum values
         * @return either `0x5F2E` or `0x7F2E`
         */
        fun toBDBTag(encodingType: BiometricEncodingType): Int =
            when (encodingType) {
                ISO_39794 ->  /* 7F2E */
                    ISO781611.BIOMETRIC_DATA_BLOCK_CONSTRUCTED_TAG

                else ->  /* 5F2E */
                    ISO781611.BIOMETRIC_DATA_BLOCK_TAG
            }
    }
}