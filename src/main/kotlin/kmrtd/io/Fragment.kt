/*
 * Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package org.jmrtd.io

import java.io.Serializable

/**
 * Fragments encapsulate pairs of offset and length.
 */
@ConsistentCopyVisibility
data class Fragment
/**
 * Constructs a fragment.
 * 
 * @param offset the offset within the buffer
 * @param length the length of the fragment
 */ private constructor(
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
    override fun toString(): String =
        "[$offset .. ${(offset + length - 1)} ($length)]"

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
    }

    override fun hashCode(): Int =
        2 * offset + 3 * length + 5

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
        @JvmStatic
        fun getInstance(offset: Int, length: Int): Fragment =
            Fragment(offset, length)
    }
}
