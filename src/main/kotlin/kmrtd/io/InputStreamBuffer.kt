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
 * $Id: InputStreamBuffer.java 1799 2018-10-30 16:25:48Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.io

import org.jmrtd.io.FragmentBuffer
import java.io.IOException
import java.io.InputStream
import kotlin.math.min

/**
 * Buffers an inputstream (whose length is known in advance) and can supply clients with fresh
 * &quot;copies&quot; of that inputstream served from the buffer.
 * 
 * NOTE: the original inputstream should no longer be read from, clients should only read bytes
 * from the sub-inputstreams.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 */
class InputStreamBuffer(inputStream: InputStream?, length: Int) {
    private val carrier: PositionInputStream

    private val buffer: FragmentBuffer

    /**
     * Creates an input stream buffer.
     * 
     * @param inputStream the input stream
     * @param length the length of the input stream
     */
    init {
        this.carrier = PositionInputStream(inputStream)
        this.carrier.mark(length)
        this.buffer = FragmentBuffer(length)
    }

    /**
     * Updates this buffer based on some other buffer.
     * 
     * @param other the other buffer
     */
    fun updateFrom(other: InputStreamBuffer) {
        buffer.updateFrom(other.buffer)
    }

    val inputStream: SubInputStream
        /**
         * Returns a copy of the input stream positioned at `0`.
         * 
         * @return a copy of the input stream
         */
        get() {
            synchronized(carrier) {
                return InputStreamBuffer.SubInputStream(carrier)
            }
        }

    @get:Synchronized
    val position: Int
        /**
         * Returns the current position in the buffer.
         * 
         * @return the position in the buffer
         */
        get() = buffer.position

    @get:Synchronized
    val bytesBuffered: Int
        /**
         * Returns the number of bytes buffered so far.
         * 
         * @return the number of bytes buffered so far
         */
        get() = buffer.bytesBuffered

    val length: Int
        /**
         * Returns the size of the buffer.
         * 
         * @return the size of the buffer
         */
        get() = buffer.length

    override fun toString(): String {
        return "InputStreamBuffer [" + buffer + "]"
    }

    /**
     * The sub-input stream to serve to clients.
     */
    inner class SubInputStream(private val syncObject: Any) : InputStream() {
        // FIXME set class visibility to package
        /**
         * The position within this stream.
         * 
         * @return the position
         */
        /** The position within this stream.  */
        var position: Int = 0
            private set

        private var markedPosition: Int

        /**
         * Creates a sub-stream.
         * 
         * @param syncObject an object for locking
         */
        init {
            markedPosition = -1
        }

        /**
         * Returns the underlying fragment buffer.
         * 
         * @return the buffer
         */
        fun getBuffer(): FragmentBuffer {
            return buffer
        }

        @Throws(IOException::class)
        override fun read(): Int {
            synchronized(syncObject) {
                if (position >= buffer.length) {
                    /* FIXME: Is this correct? Isn't buffer capable of growing dynamically? -- MO */
                    return -1
                } else if (buffer.isCoveredByFragment(position)) {
                    /* Serve the byte from the buffer */
                    return buffer.buffer!![position++].toInt() and 0xFF
                } else {
                    /* Get it from the carrier */
                    if (carrier.markSupported()) {
                        syncCarrierPosition(position)
                    }
                    try {
                        val result = carrier.read()
                        if (result < 0) {
                            return -1
                        }

                        buffer.addFragment(position++, result.toByte())
                        return result
                    } catch (ioe: IOException) {
                        /*
             * Carrier failed to read. Now what?
             * - We don't update the buffer or position.
             * - Obviously we also fail to read, with the same exception.
             */
                        throw ioe
                    }
                }
            }
        }

        @Throws(IOException::class)
        override fun read(b: ByteArray): Int {
            synchronized(syncObject) {
                return read(b, 0, b.size)
            }
        }

        @Throws(IOException::class)
        override fun read(b: ByteArray, off: Int, len: Int): Int {
            var len = len
            synchronized(syncObject) {
                if (b == null) {
                    throw NullPointerException()
                } else if (off < 0 || len < 0 || len > b.size - off) {
                    throw IndexOutOfBoundsException()
                } else if (len == 0) {
                    return 0
                }
                if (len > buffer.length - position) {
                    len = buffer.length - position
                }

                if (position >= buffer.length) {
                    /* FIXME: is this correct? See FIXME in read(). */
                    return -1
                }

                if (carrier.markSupported()) {
                    syncCarrierPosition(position)
                }

                val fragment: FragmentBuffer.Fragment = buffer.getSmallestUnbufferedFragment(position, len)
                if (fragment.getLength() > 0) {
                    /* Copy buffered prefix to b. */
                    val alreadyBufferedPrefixLength = fragment.getOffset() - position
                    val unbufferedPostfixLength = fragment.getLength()
                    System.arraycopy(buffer.buffer, position, b, off, alreadyBufferedPrefixLength)
                    position += alreadyBufferedPrefixLength

                    if (carrier.markSupported()) {
                        syncCarrierPosition(position)
                    }

                    /* Read unbuffered postfix from carrier, directly to b and buffer it. */
                    val bytesReadFromCarrier =
                        carrier.read(b, off + alreadyBufferedPrefixLength, unbufferedPostfixLength)
                    buffer.addFragment(fragment.getOffset(), b, off + alreadyBufferedPrefixLength, bytesReadFromCarrier)
                    position += bytesReadFromCarrier

                    return alreadyBufferedPrefixLength + bytesReadFromCarrier
                } else {
                    /* No unbuffered fragment. */
                    val length = min(len, buffer.length - position)
                    System.arraycopy(buffer.buffer, position, b, off, length)
                    position += length
                    return length
                }
            }
        }

        @Throws(IOException::class)
        override fun skip(n: Long): Long {
            synchronized(syncObject) {
                val leftInBuffer = buffer.getBufferedLength(position)
                if (n <= leftInBuffer) {
                    /* If we can skip within the buffer, we do */
                    position += n.toInt()
                    return n
                } else {
                    assert(leftInBuffer < n)
                    /* Otherwise, skip what's left in buffer, then skip within carrier... */
                    position += leftInBuffer
                    var skippedBytes: Long = 0
                    if (carrier.markSupported()) {
                        syncCarrierPosition(position)
                        skippedBytes = carrier.skip(n - leftInBuffer)
                        position += skippedBytes.toInt()
                    } else {
                        skippedBytes = super.skip(n - leftInBuffer)
                        /* As super.skip will call read, position will be adjusted automatically. */
                    }
                    return leftInBuffer + skippedBytes
                }
            }
        }

        @Throws(IOException::class)
        override fun available(): Int {
            return buffer.getBufferedLength(position)
        }

        @Throws(IOException::class)
        override fun close() {
        }

        @Synchronized
        override fun mark(readLimit: Int) {
            markedPosition = position
        }

        @Synchronized
        @Throws(IOException::class)
        override fun reset() {
            if (markedPosition < 0) {
                throw IOException("Invalid reset, was mark() called?")
            }
            position = markedPosition
        }

        override fun markSupported(): Boolean {
            return true
        }

        /**
         * If necessary, resets the carrier (which must support mark) and
         * skips to the current position in the buffer.
         * 
         * @param position the position to skip to
         * 
         * @throws IOException on error
         */
        @Throws(IOException::class)
        private fun syncCarrierPosition(position: Int) {
            if (position.toLong() == carrier.getPosition()) {
                return
            }
            carrier.reset()
            var bytesSkipped = 0
            while (bytesSkipped < position) {
                bytesSkipped += carrier.skip(position.toLong() - bytesSkipped).toInt()
            }
        }
    }
}
