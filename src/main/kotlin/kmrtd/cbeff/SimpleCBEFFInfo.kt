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
 * $Id: SimpleCBEFFInfo.java 1885 2024-11-07 09:17:29Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.cbeff

/**
 * Simple CBEFF BIR.
 * Specified in ISO 19785-1 (version 2.0) and NISTIR 6529-A (version 1.1).
 * 
 * @param <B> the embedded BDB
 * @author The JMRTD team (info@jmrtd.org)
 * @version $Revision: 1885 $
 * @since 0.4.7
</B> */
class SimpleCBEFFInfo<B : BiometricDataBlock>
/**
 * Constructs a simple CBEFF info from the given BDB.
 * 
 * @param biometricDataBlock a biometric data block
 */(
    /**
     * Returns the biometric data block from this simple CBEFF info.
     * 
     * @return a biometric data block
     */
    val biometricDataBlock: B
) : CBEFFInfo<B>
