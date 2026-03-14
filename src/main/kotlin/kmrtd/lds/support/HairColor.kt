/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.lds.support

import kmrtd.lds.iso19794.ISO19794

/**
 * Hair color code based on Section 5.5.5 of ISO 19794-5.
 */
enum class HairColor
/**
 * Creates a hair color.
 *
 * @param code the integer code for a color
 */(private val code: Int) {
    UNSPECIFIED(ISO19794.HAIR_COLOR_UNSPECIFIED),
    BALD(ISO19794.HAIR_COLOR_BALD),
    BLACK(ISO19794.HAIR_COLOR_BLACK),
    BLONDE(ISO19794.HAIR_COLOR_BLONDE),
    BROWN(ISO19794.HAIR_COLOR_BROWN),
    GRAY(ISO19794.HAIR_COLOR_GRAY),
    WHITE(ISO19794.HAIR_COLOR_WHITE),
    RED(ISO19794.HAIR_COLOR_RED),
    GREEN(ISO19794.HAIR_COLOR_GREEN),
    BLUE(ISO19794.HAIR_COLOR_BLUE),
    UNKNOWN(ISO19794.HAIR_COLOR_UNKNOWN);

    /**
     * Returns the code for this hair color.
     *
     * @return the code
     */
    fun toInt(): Int {
        return code
    }

    companion object {
        /**
         * Returns a hair color value for the given code.
         *
         * @param i the integer code for a color
         * @return the color value
         */
        fun toHairColor(i: Int): HairColor {
            for (c in entries) {
                if (c.toInt() == i) {
                    return c
                }
            }

            return UNKNOWN
        }
    }
}