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
 * $Id: SplittableInputStream.java 1808 2019-03-07 21:32:19Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package org.jmrtd.io

import java.io.IOException
import java.io.InputStream

/**
 * An input stream which will wrap another input stream (and yield the same bytes) and which can
 * spawn new fresh input stream copies (using [.getInputStream])
 * (that also yield the same bytes).
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1808 $
 */
class SplittableInputStream(inputStream: InputStream, length: Int) : InputStream() {
    private val inputStreamBuffer: InputStreamBuffer = InputStreamBuffer(inputStream, length)
    private val carrier: InputStreamBuffer.SubInputStream = inputStreamBuffer.inputStream

    /**
     * Updates this stream's buffer based on some other stream's buffer.
     * 
     * @param other the other stream
     */
    fun updateFrom(other: SplittableInputStream) {
        inputStreamBuffer.updateFrom(other.inputStreamBuffer)
    }

    /**
     * Returns a copy of the inputstream positioned at `position`.
     * 
     * @param position a position between `0` and [.getPosition]
     * 
     * @return a fresh input stream
     */
    fun getInputStream(position: Int): InputStream {
        try {
            val inputStream: InputStream = inputStreamBuffer.inputStream
            var skippedBytes = 0L
            while (skippedBytes < position) {
                skippedBytes += inputStream.skip(position - skippedBytes)
            }
            return inputStream
        } catch (ioe: IOException) {
            throw IllegalStateException(ioe)
        }
    }

    val position: Int
        /**
         * Returns the position within the input stream (the number of bytes read since this input stream was constructed).
         * 
         * @return the position within this input stream
         */
        get() = carrier.position

    /**
     * Reads the next byte of data from the input stream. The value byte is
     * returned as an `int` in the range `0` to
     * `255`. If no byte is available because the end of the stream
     * has been reached, the value `-1` is returned. This method
     * blocks until input data is available, the end of the stream is detected,
     * or an exception is thrown.
     * 
     * @return     the next byte of data, or `-1` if the end of the
     * stream is reached
     * 
     * @throws IOException if an I/O error occurs
     */
    @Throws(IOException::class)
    override fun read(): Int =
        carrier.read()

    /**
     * Skips over and discards `n` bytes of data from this input
     * stream. The `skip` method may, for a variety of reasons, end
     * up skipping over some smaller number of bytes, possibly `0`.
     * This may result from any of a number of conditions; reaching end of file
     * before `n` bytes have been skipped is only one possibility.
     * The actual number of bytes skipped is returned.  If `n` is
     * negative, no bytes are skipped.
     * 
     * @param n the number of bytes to be skipped
     * 
     * @return the actual number of bytes skipped
     * 
     * @throws IOException if the stream does not support seek, or if some other I/O error occurs
     */
    @Throws(IOException::class)
    override fun skip(n: Long): Long =
        carrier.skip(n)

    /**
     * Returns an estimate of the number of bytes that can be read (or
     * skipped over) from this input stream without blocking by the next
     * invocation of a method for this input stream. The next invocation
     * might be the same thread or another thread.  A single read or skip of this
     * many bytes will not block, but may read or skip fewer bytes.
     * 
     * @return an estimate of the number of bytes that can be read (or skipped
     * over) from this input stream without blocking or `0` when
     * it reaches the end of the input stream
     * 
     * @throws IOException on error
     */
    @Throws(IOException::class)
    override fun available(): Int =
        carrier.available()

    /**
     * Closes this input stream and releases any system resources associated
     * with the stream.
     * 
     * @throws IOException on error
     */
    @Throws(IOException::class)
    override fun close() =
        carrier.close()

    /**
     * Marks the current position in this input stream. A subsequent call to
     * the `reset` method repositions this stream at the last marked
     * position so that subsequent reads re-read the same bytes.
     * 
     * 
     * The `readlimit` arguments tells this input stream to
     * allow that many bytes to be read before the mark position gets
     * invalidated.
     * 
     * 
     * The general contract of `mark` is that, if the method
     * `markSupported` returns `true`, the stream somehow
     * remembers all the bytes read after the call to `mark` and
     * stands ready to supply those same bytes again if and whenever the method
     * `reset` is called.  However, the stream is not required to
     * remember any data at all if more than `readlimit` bytes are
     * read from the stream before `reset` is called.
     * 
     * @param readlimit the maximum limit of bytes that can be read before the mark position becomes invalid
     * 
     * @see InputStream.reset
     */
    @Synchronized
    override fun mark(readlimit: Int) =
        carrier.mark(readlimit)

    /**
     * Repositions this stream to the position at the time the
     * `mark` method was last called on this input stream.
     * 
     * The general contract of `reset` is:
     * 
     * 
     * 
     *  *  If the method `markSupported` returns
     * `true`, then:
     * 
     *  *  If the method `mark` has not been called since
     * the stream was created, or the number of bytes read from the stream
     * since `mark` was last called is larger than the argument
     * to `mark` at that last call, then an
     * `IOException` might be thrown.
     * 
     *  *  If such an `IOException` is not thrown, then the
     * stream is reset to a state such that all the bytes read since the
     * most recent call to `mark` (or since the start of the
     * file, if `mark` has not been called) will be resupplied
     * to subsequent callers of the `read` method, followed by
     * any bytes that otherwise would have been the next input data as of
     * the time of the call to `reset`. 
     * 
     *  *  If the method `markSupported` returns
     * `false`, then:
     * 
     *  *  The call to `reset` may throw an
     * `IOException`.
     * 
     *  *  If an `IOException` is not thrown, then the stream
     * is reset to a fixed state that depends on the particular type of the
     * input stream and how it was created. The bytes that will be supplied
     * to subsequent callers of the `read` method depend on the
     * particular type of the input stream. 
     * 
     * @throws IOException if this stream has not been marked or if the mark has been invalidated
     * 
     * @see InputStream.mark
     * @see IOException
     * 
     * 
     * @throws IOException on error
     */
    @Synchronized
    @Throws(IOException::class)
    override fun reset() =
        carrier.reset()

    /**
     * Tests if this input stream supports the `mark` and
     * `reset` methods. Whether or not `mark` and
     * `reset` are supported is an invariant property of a
     * particular input stream instance. The `markSupported` method
     * of `InputStream` returns `false`.
     * 
     * @return `true` if this stream instance supports the mark
     * and reset methods and `false` otherwise
     * 
     * @see InputStream.mark
     * @see InputStream.reset
     */
    override fun markSupported(): Boolean =
        carrier.markSupported()

    val length: Int
        /**
         * Returns the length of the underlying buffer.
         * 
         * @return the length of the underlying buffer
         */
        get() = inputStreamBuffer.length

    val bytesBuffered: Int
        /**
         * Returns the number of buffered bytes in the underlying buffer.
         * 
         * @return the number of buffered bytes in the underlying buffer
         */
        get() = inputStreamBuffer.bytesBuffered
}
