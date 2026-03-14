/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.lds.support

import kmrtd.lds.iso19794.ISO19794

/**
 * Eye color code based on Section 5.5.4 of ISO 19794-5.
 */
enum class EyeColor
/**
 * Creates an eye color.
 *
 * @param code the ISO19794-5 integer code for the color
 */(private val code: Int) {
    UNSPECIFIED(ISO19794.EYE_COLOR_UNSPECIFIED),
    BLACK(ISO19794.EYE_COLOR_BLACK),
    BLUE(ISO19794.EYE_COLOR_BLUE),
    BROWN(ISO19794.EYE_COLOR_BROWN),
    GRAY(ISO19794.EYE_COLOR_GRAY),
    GREEN(ISO19794.EYE_COLOR_GREEN),
    MULTI_COLORED(ISO19794.EYE_COLOR_MULTI_COLORED),
    PINK(ISO19794.EYE_COLOR_PINK),
    UNKNOWN(ISO19794.EYE_COLOR_UNKNOWN);

    /**
     * Returns the integer code to use in ISO19794-5 encoding for this color.
     *
     * @return the integer code
     */
    fun toInt(): Int {
        return code
    }

    companion object {
        /**
         * Returns an eye color value for the given code.
         *
         * @param i the integer code for a color
         * @return the color value
         */
        fun toEyeColor(i: Int): EyeColor {
            for (c in entries) {
                if (c.toInt() == i) {
                    return c
                }
            }
            return UNKNOWN
        }
    }
}