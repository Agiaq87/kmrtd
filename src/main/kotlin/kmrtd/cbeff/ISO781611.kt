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
 * $Id: ISO781611.java 1901 2025-07-15 12:31:11Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.cbeff

/**
 * Constants interface representing ISO7816-11.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * @version $Revision: 1901 $
 * @since 0.4.7
 */
object ISO781611 {
    const val BIOMETRIC_INFORMATION_GROUP_TEMPLATE_TAG: Int = 0x7F61
    const val BIOMETRIC_INFORMATION_TEMPLATE_TAG: Int = 0x7F60

    const val BIOMETRIC_INFO_COUNT_TAG: Int = 0x02
    const val BIOMETRIC_HEADER_TEMPLATE_BASE_TAG: Int = 0xA1
    const val BIOMETRIC_DATA_BLOCK_TAG: Int = 0x5F2E
    const val BIOMETRIC_DATA_BLOCK_CONSTRUCTED_TAG: Int = 0x7F2E

    const val DISCRETIONARY_DATA_FOR_PAYLOAD_TAG: Int = 0x53
    const val DISCRETIONARY_DATA_FOR_PAYLOAD_CONSTRUCTED_TAG: Int = 0x73

    /*
 * FIXME: For 7F2E check ISO7816-11, Table 3: a 7F2E structure appears to include a 5F2E structure?
 * Difference between primitive/constructed.
 */
    /**
     * From ISO7816-11: Secure Messaging Template tag.
     */
    const val SMT_TAG: Int = 0x7D

    /**
     * From ISO7816-11: Secure Messaging Template tag.
     */
    const val SMT_DO_PV: Int = 0x81

    /**
     * From ISO7816-11: Secure Messaging Template tag.
     */
    const val SMT_DO_CG: Int = 0x85

    /**
     * From ISO7816-11: Secure Messaging Template tag.
     */
    const val SMT_DO_CC: Int = 0x8E

    /**
     * From ISO7816-11: Secure Messaging Template tag.
     */
    const val SMT_DO_DS: Int = 0x9E

    /**
     * ISO 7816-11, table C.1., used inside the BHT.
     * Length 2.
     */
    const val PATRON_HEADER_VERSION_TAG: Int = 0x80

    /**
     * Biometric Type tag, ISO7816-11.
     */
    const val BIOMETRIC_TYPE_TAG: Int = 0x81

    /**
     * Biometric Subtype tag, ISO7816-11.
     */
    const val BIOMETRIC_SUBTYPE_TAG: Int = 0x82

    /**
     * ISO7816-11 table C.1, (7), creation date and time of biometric data (CCYYMMDDhhmmss), used inside the BHT.
     * Length 2.
     */
    const val CREATION_DATE_AND_TIME_TAG: Int = 0x83

    /**
     * ISO7816-11 table C.1, (8), validity period (from CCYYMMDD, to CCYYMMDD), used inside the BHT.
     * Length 2.
     */
    const val VALIDITY_PERIOD_TAG: Int = 0x85

    /**
     * ISO7816-11 table C.1, (2), Identifier of product (PID) that created the biometric reference data,
     * value assigned by IBIA.
     * Length 2.
     */
    const val CREATOR_OF_BIOMETRIC_REFERENCE_DATA: Int = 0x86

    /**
     * ID of the Group or Vendor which defined the BDB, specified in NISTIR-6529A and/or ISO7817-11.
     * Length 2.
     */
    const val FORMAT_OWNER_TAG: Int = 0x87

    /**
     * BDB Format Type as specified by the Format Owner, specified in NISTIR-6529A and/or ISO7816-11.
     * Length 2.
     */
    const val FORMAT_TYPE_TAG: Int = 0x88
}
