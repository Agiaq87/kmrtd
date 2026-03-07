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
 * $Id: BiometricDataBlockDecoder.java 1802 2018-11-06 16:29:28Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.cbeff

import java.io.IOException
import java.io.InputStream

/**
 * Interface to be implemented by client code to decode BDB implementations.
 * 
 * @param <B> the type of BDB implementation that is decoded by this decoder
 * @author The JMRTD team (info@jmrtd.org)
 * @version $Revision: 1802 $
 * @since 0.4.7
</B> */
interface BiometricDataBlockDecoder<B : BiometricDataBlock> {
    /**
     * Reads a biometric data block from an input stream.
     * 
     * @param inputStream the input stream to read from
     * @param sbh         the standard biometric header that preceded this BDB
     * @param index       the index of this BDB
     * @param length      the length of this BDB
     * @return a biometric data block
     * @throws IOException if reading failed
     */
    @Throws(IOException::class)
    fun decode(
        inputStream: InputStream?,
        sbh: StandardBiometricHeader?,
        index: Int,
        length: Int
    ): B
}
