/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.cert.support

/**
 * The authorization permission.
 *
 * @author The JMRTD team (info@jmrtd.org)
 * @version $Revision: 1853 $
 */
enum class Permission(private val _value: Int) {
    /**
     * No read access.
     */
    READ_ACCESS_NONE(0x00),

    /**
     * Read access to DG3.
     */
    READ_ACCESS_DG3(0x01),

    /**
     * Read access to DG4.
     */
    READ_ACCESS_DG4(0x02),

    /**
     * Read access to DG3 and DG4.
     */
    READ_ACCESS_DG3_AND_DG4(0x03);

    /**
     * Returns the tag as a bitmap.
     *
     * @return a bitmap
     */
    val value: Byte = _value.toByte()

    /**
     * Whether this permission implies the other permission.
     *
     * @param other some other permission
     * @return a boolean
     */
    fun implies(other: Permission?): Boolean =
        when (this) {
            READ_ACCESS_NONE -> other == READ_ACCESS_NONE
            READ_ACCESS_DG3 -> other == READ_ACCESS_DG3
            READ_ACCESS_DG4 -> other == READ_ACCESS_DG4
            READ_ACCESS_DG3_AND_DG4 -> other == READ_ACCESS_DG3 || other == READ_ACCESS_DG4 || other == READ_ACCESS_DG3_AND_DG4
            else -> false
        }
}