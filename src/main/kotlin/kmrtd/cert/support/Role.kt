/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.cert.support

/**
 * The authorization role.
 *
 * @author The JMRTD team (info@jmrtd.org)
 * @version $Revision: 1853 $
 */
enum class Role(private val _value: Int) {
    /**
     * Certificate authority.
     */
    CVCA(0xC0),

    /**
     * Document verifier domestic.
     */
    DV_D(0x80),

    /**
     * Document verifier foreign.
     */
    DV_F(0x40),

    /**
     * Inspection system.
     */
    IS(0x00);

    /**
     * Returns the value as a bitmap.
     *
     * @return a bitmap
     */
    val value: Byte = _value.toByte()
}