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
 * $Id: PositionInputStream.java 1817 2019-08-02 12:09:17Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.io

import java.io.IOException
import java.io.InputStream
import java.util.logging.Logger

/**
 * A stream that decorates an existing stream and keeps track of the current position.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1817 $
 */
class PositionInputStream(private val carrier: InputStream) : InputStream() {
    /**
     * Returns the position within the input stream.
     * 
     * @return the position within the input stream
     */
    var position: Long = 0L
        private set
    private var markedPosition: Long

    /**
     * Constructs a position input stream by decorating an existing input stream.
     * 
     * @param carrier the existing input stream
     */
    init {
        markedPosition = MARK_NOT_SET
    }

    @Throws(IOException::class)
    override fun read(): Int {
        val b = carrier.read()
        if (b >= 0) {
            position++
        }
        return b
    }

    @Throws(IOException::class)
    override fun read(dest: ByteArray): Int {
        return read(dest, 0, dest.size)
    }

    @Throws(IOException::class)
    override fun read(dest: ByteArray, offset: Int, length: Int): Int {
        val bytesRead = carrier.read(dest, offset, length)
        position += bytesRead.toLong()
        return bytesRead
    }

    @Throws(IOException::class)
    override fun skip(n: Long): Long {
        val skippedBytes = carrier.skip(n)
        if (skippedBytes <= 0) {
            LOGGER.warning("Carrier (" + carrier.javaClass.getCanonicalName() + ")'s skip(" + n + ") only skipped " + skippedBytes + ", position = " + position)
        }

        position += skippedBytes
        return skippedBytes
    }

    @Synchronized
    override fun mark(readLimit: Int) {
        carrier.mark(readLimit)
        markedPosition = position
    }

    @Synchronized
    @Throws(IOException::class)
    override fun reset() {
        carrier.reset()
        position = markedPosition
    }

    override fun markSupported(): Boolean {
        return carrier.markSupported()
    }

    companion object {
        private val LOGGER: Logger = Logger.getLogger("org.jmrtd")

        private val MARK_NOT_SET = -1L
    }
}
