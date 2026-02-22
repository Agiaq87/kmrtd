package kmrtd.io
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
import java.io.Serializable

/**
 * Fragments encapsulate pairs of offset and length.
 */
data class Fragment
/**
 * Constructs a fragment.
 *
 * @param offset the offset within the buffer
 * @param length the length of the fragment
 */ constructor(
    /**
     * Returns this fragment's offset within the buffer.
     *
     * @return the offset of the fragment
     */
    val offset: Int,
    /**
     * Returns the length of the fragment.
     *
     * @return the length of the fragment
     */
    val length: Int
) : Serializable {
    /*override fun toString(): String {
        return "[ $offset .. ${offset + length - 1} ($length)]"
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        if (other === this) {
            return true
        }
        if (other.javaClass != Fragment::class.java) {
            return false
        }

        val otherFragment = other as Fragment
        return otherFragment.offset == offset && otherFragment.length == length
    }*/

    override fun hashCode(): Int {
        return 2 * offset + 3 * length + 5
    }

    companion object {
        private val serialVersionUID = -3795931618553980328L

        /**
         * Returns a fragment instance.
         *
         * @param offset the offset within the buffer
         * @param length the length of the fragment
         *
         * @return the new fragment
         */
        /*fun getInstance(offset: Int, length: Int): Fragment {
            return Fragment(offset, length)
        }*/
    }
}