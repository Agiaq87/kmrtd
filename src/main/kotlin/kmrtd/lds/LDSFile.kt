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
 * $Id: LDSFile.java 1751 2018-01-15 15:35:45Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.lds

/**
 * LDS element at file level.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1751 $
 */
interface LDSFile : LDSElement {
    /*
       * FIXME: Note that this is not necessarily the total length of the file:
       * For TLV files this gives the length of the value. -- MO
       */
    /**
     * Returns the length of this file.
     * 
     * @return the length of this file
     */
    val length: Int

    companion object {
        /* NOTE: In EAC 1.11 documents there is also the CVCA file that has no tag. */
        /** ICAO tag for document index (COM).  */
        const val EF_COM_TAG: Int = 0x60

        /** ICAO data group tag for DG1.  */
        const val EF_DG1_TAG: Int = 0x61

        /** ICAO data group tag for DG2.  */
        const val EF_DG2_TAG: Int = 0x75

        /** ICAO data group tag for DG3.  */
        const val EF_DG3_TAG: Int = 0x63

        /** ICAO data group tag for DG4.  */
        const val EF_DG4_TAG: Int = 0x76

        /** ICAO data group tag for DG5.  */
        const val EF_DG5_TAG: Int = 0x65

        /** ICAO data group tag for DG6.  */
        const val EF_DG6_TAG: Int = 0x66

        /** ICAO data group tag for DG7.  */
        const val EF_DG7_TAG: Int = 0x67

        /** ICAO data group tag for DG8.  */
        const val EF_DG8_TAG: Int = 0x68

        /** ICAO data group tag for DG9.  */
        const val EF_DG9_TAG: Int = 0x69

        /** ICAO data group tag for DG10.  */
        const val EF_DG10_TAG: Int = 0x6A

        /** ICAO data group tag for DG11.  */
        const val EF_DG11_TAG: Int = 0x6B

        /** ICAO data group tag for DG12.  */
        const val EF_DG12_TAG: Int = 0x6C

        /** ICAO data group tag for DG13.  */
        const val EF_DG13_TAG: Int = 0x6D

        /** ICAO data group tag for DG14.  */
        const val EF_DG14_TAG: Int = 0x6E

        /** ICAO data group tag for DG15.  */
        const val EF_DG15_TAG: Int = 0x6F

        /** ICAO data group tag for DG16.  */
        const val EF_DG16_TAG: Int = 0x70

        /** ICAO tag for document security index (SOd).  */
        const val EF_SOD_TAG: Int = 0x77
    }
}
