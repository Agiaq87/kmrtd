/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.io

/**
 * Fragments encapsulate pairs of offset and length.
 */
data class Fragment
/**
 * Constructs a fragment.
 *
 * @param offset the offset within the buffer
 * @param length the length of the fragment
 */(
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
)
