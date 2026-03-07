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
 * $Id: BiometricDataBlockEncoder.java 1897 2025-05-27 12:34:36Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.cbeff

import java.io.IOException
import java.io.OutputStream

/**
 * Interface to be implemented by client code to encode BDB implementations.
 * 
 * @param <B> the type of BDB implementation that is encoded by this encoder
 * @author The JMRTD team (info@jmrtd.org)
 * @version $Revision: 1897 $
 * @since 0.4.7
</B> */
interface BiometricDataBlockEncoder<B : BiometricDataBlock> {
    /**
     * Writes the biometric data block in `bdb` to the output stream.
     * 
     * @param bdb the biometric data block to write
     * @param out the output stream to write to
     * @throws IOException if writing fails
     */
    @Throws(IOException::class)
    fun encode(bdb: B, out: OutputStream)

    /**
     * Returns the biometric encoding type to be used when encoding BITs.
     * 
     * @return the biometric encoding type, either ISO-19794 or ISO-39794
     */
    val encodingType: BiometricEncodingType
}
