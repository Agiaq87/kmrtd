/*
 * JMRTD - A Java API for accessing machine readable travel documents.
 *
 * Copyright (C) 2006 - 2020  The JMRTD team
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
 * $Id: FragmentBuffer.java 1839 2020-08-27 06:28:31Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.io

import java.io.Serializable
import kotlin.math.max

/**
 * A buffer that can be partially filled.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * @version $Revision: 1839 $
 */
class FragmentBuffer @JvmOverloads constructor(length: Int = DEFAULT_SIZE) : Serializable {
    /**
     * Returns the fragments of this buffer.
     * 
     * @return the fragments
     */
    /**
     * Administration of which parts of buffer are filled.
     */
    val fragments: MutableCollection<Fragment> = sortedSetOf(compareBy { it.offset })
    /**
     * Returns the current buffer.
     * 
     * @return the buffer
     */
    /**
     * Buffer with the actual bytes.
     */
    var buffer: ByteArray = ByteArray(length)

    /**
     * Updates this buffer based on the given buffer.
     * 
     * @param other some other fragment buffer
     */
    @Synchronized
    fun updateFrom(other: FragmentBuffer) =
        other.fragments.forEach {
            addFragment(it.offset, other.buffer, it.offset, it.length)
        }

    /**
     * Adds a fragment containing the given byte.
     * 
     * @param offset the offset
     * @param b      the byte to insert
     */
    @Synchronized
    fun addFragment(offset: Int, b: Byte) {
        /*
         * NOTE: This can be done more efficiently for common case resulting from InputStreamBuffer read,
         * scan all fragments and extend neighboring one.
         */
        addFragment(offset, byteArrayOf(b))
    }

    /**
     * Adds a fragment of bytes at a specific offset to this file.
     * 
     * @param offset the fragment offset
     * @param bytes  the bytes from which fragment content will be copied
     */
    @Synchronized
    fun addFragment(offset: Int, bytes: ByteArray) =
        addFragment(offset, bytes, 0, bytes.size)

    /**
     * Adds a fragment of bytes at a specific offset to this file.
     * 
     * @param offset    the fragment offset
     * @param bytes     the bytes from which fragment contents will be copied
     * @param srcOffset the offset within bytes where the contents of the fragment start
     * @param srcLength the length of the fragment
     */
    @Synchronized
    fun addFragment(offset: Int, bytes: ByteArray, srcOffset: Int, srcLength: Int) {
        if (offset + srcLength > buffer.size) {
            this.length = 2 * max(offset + srcLength, buffer.size)
        }
        //bytes.copyInto(bytes, offset, srcOffset, srcOffset + srcLength) TODO
        System.arraycopy(bytes, srcOffset, buffer, offset, srcLength)
        var thisOffset = offset
        var thisLength = srcLength
        val otherFragments: MutableCollection<Fragment> = ArrayList<Fragment>(fragments)
        for (other in otherFragments) {
            /* On partial overlap we change this fragment, possibly remove the other overlapping fragments we encounter. */
            if (other.offset <= thisOffset && thisOffset + thisLength <= other.offset + other.length) {
                /*
                 * [...other fragment.........]
                 *    [...this fragment...]
                 *
                 * This fragment is already contained in other. Don't add and return immediately.
                 */
                return
            } else if (other.offset <= thisOffset && thisOffset <= other.offset + other.length) {
                /*
                 * [...other fragment...]
                 *         [...this fragment...]
                 *
                 * This fragment is partially contained in other. Extend this fragment to size of other, remove other.
                 */
                thisLength = thisOffset + thisLength - other.offset
                thisOffset = other.offset
                fragments.remove(other)
            } else if (thisOffset <= other.offset && other.offset + other.length <= thisOffset + thisLength) {
                /*
                 *    [...other fragment...]
                 * [...this fragment...........]
                 *
                 * The other fragment is contained in this fragment. Remove other.
                 */
                fragments.remove(other)
            } else if (thisOffset <= other.offset && other.offset <= thisOffset + thisLength) {
                /*
                 *        [...other fragment...]
                 * [...this fragment...]
                 *
                 * This fragment is partially contained in other. Extend this fragment to size of other, remove other.
                 */
                thisLength = other.offset + other.length - thisOffset
                fragments.remove(other)
            }
        }
        fragments.add(Fragment(thisOffset, thisLength))
    }

    @get:Synchronized
    val position: Int
        /**
         * Returns the position within the buffer.
         * This is the upper limit of the farthest fragment read so far.
         * 
         * @return the position within the buffer
         */
        get() {
            var result = 0
            for (i in buffer.indices) {
                if (isCoveredByFragment(i)) {
                    result = i + 1
                }
            }
            return result
        }

    @get:Synchronized
    val bytesBuffered: Int
        /**
         * Returns the number of bytes currently buffered.
         * 
         * @return the number of bytes currently buffered
         */
        get() {
            var result = 0
            for (i in buffer.indices) {
                if (isCoveredByFragment(i)) {
                    result++
                }
            }
            return result
        }

    /**
     * Checks whether the byte at the given offset is covered
     * by a fragment.
     * 
     * @param offset the offset
     * @return a boolean indicating whether the byte at the given offset is covered
     */
    @Synchronized
    fun isCoveredByFragment(offset: Int): Boolean =
        isCoveredByFragment(offset, 1)

    /**
     * Checks whether the segment specified by the given offset and length
     * is completely covered by fragments.
     * 
     * @param offset the given offset
     * @param length the given length
     * @return a boolean indicating whether the specified segment is fully covered
     */
    @Synchronized
    fun isCoveredByFragment(offset: Int, length: Int): Boolean {
        for (fragment in fragments) {
            val left = fragment.offset
            val right = fragment.offset + fragment.length
            if (left <= offset && offset + length <= right) {
                return true
            }
        }
        return false
    }

    /**
     * Calculates the number of bytes left in the buffer starting from index `index`.
     * 
     * @param index the index
     * @return the number of bytes left in the buffer
     */
    @Synchronized
    fun getBufferedLength(index: Int): Int {
        var result = 0
        if (index >= buffer.size) {
            return 0
        }

        for (fragment in fragments) {
            val left = fragment.offset
            val right = fragment.offset + fragment.length
            if (index in left..<right) {
                val newResult = right - index
                if (newResult > result) {
                    result = newResult
                }
            }
        }
        return result
    }

    var length: Int
        /**
         * Returns the buffer (the size of the underlying byte array).
         * 
         * @return the size of the buffer
         */
        get() {
            synchronized(this) {
                return buffer.size
            }
        }
        /**
         * Sets the capacity of the buffer.
         * This has no effect for lengths smaller than the current buffer capacity.
         * 
         * @param length the proposed new capacity of the buffer
         */
        private set(length) {
            synchronized(this) {
                if (length <= buffer.size) {
                    return
                }
                val newBuffer = ByteArray(length)
                System.arraycopy(this.buffer, 0, newBuffer, 0, this.buffer.size)
                this.buffer = newBuffer
            }
        }

    /**
     * Returns the smallest fragment which, when added, makes the fragment buffer contains
     * `offset` to `offset + length` that has **not** been buffered in this buffer.
     * 
     * @param offset the offset into the file
     * @param length the length
     * @return the fragment that has not yet been buffered
     */
    @Synchronized
    fun getSmallestUnbufferedFragment(offset: Int, length: Int): Fragment {
        var thisOffset = offset
        var thisLength = length
        for (other in fragments) {
            /* On partial overlap we change this fragment, removing sections already buffered. */
            if (other.offset <= thisOffset && thisOffset + thisLength <= other.offset + other.length) {
                /*
                 * [...other fragment.........]
                 *    [...this fragment...]
                 *
                 * This fragment is already contained in other. Don't add and return immediately.
                 */
                thisLength = 0 /* NOTE: we don't care about offset */
                break
            } else if (other.offset <= thisOffset && thisOffset < other.offset + other.length) {
                /*
                 * [...other fragment...]
                 *         [...this fragment...]
                 *
                 * This fragment is partially contained in other. Only fetch the trailing part of this fragment.
                 */
                val newOffset = other.offset + other.length
                val newLength = thisOffset + thisLength - newOffset
                thisOffset = newOffset
                thisLength = newLength
            } else if (thisOffset <= other.offset && other.offset + other.length <= thisOffset + thisLength) {
                /*
                 *    [...other fragment...]
                 * [...this fragment...........]
                 *
                 * The other fragment is contained in this fragment. We send this fragment as is.
                 */
                continue
            } else if (offset <= other.offset && other.offset < thisOffset + thisLength) {
                /*
                 *        [...other fragment...]
                 * [...this fragment...]
                 *
                 * This fragment is partially contained in other. Only send the leading part of this fragment.
                 */
                thisLength = other.offset - thisOffset
            }
        }
        return Fragment(thisOffset, thisLength)
    }

    @Synchronized
    override fun toString(): String {
        return "FragmentBuffer [" + buffer.size + ", " + fragments + "]"
    }

    @Synchronized
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other === this) return true
        if (other !is FragmentBuffer) return false

        return other.buffer.contentEquals(this.buffer) && other.fragments == this.fragments
    }

    override fun hashCode(): Int {
        return 3 * buffer.contentHashCode() + 2 * fragments.hashCode() + 7
    }

    companion object {
        private const val DEFAULT_SIZE = 2000
    }
}
