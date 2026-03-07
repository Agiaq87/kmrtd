/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.lds.support

import kmrtd.lds.iso19794.FaceImageInfo.Companion.EYE_COLOR_BLACK
import kmrtd.lds.iso19794.FaceImageInfo.Companion.EYE_COLOR_BLUE
import kmrtd.lds.iso19794.FaceImageInfo.Companion.EYE_COLOR_BROWN
import kmrtd.lds.iso19794.FaceImageInfo.Companion.EYE_COLOR_GRAY
import kmrtd.lds.iso19794.FaceImageInfo.Companion.EYE_COLOR_GREEN
import kmrtd.lds.iso19794.FaceImageInfo.Companion.EYE_COLOR_MULTI_COLORED
import kmrtd.lds.iso19794.FaceImageInfo.Companion.EYE_COLOR_PINK
import kmrtd.lds.iso19794.FaceImageInfo.Companion.EYE_COLOR_UNKNOWN
import kmrtd.lds.iso19794.FaceImageInfo.Companion.EYE_COLOR_UNSPECIFIED

/**
 * Eye color code based on Section 5.5.4 of ISO 19794-5.
 */
enum class EyeColor
/**
 * Creates an eye color.
 *
 * @param code the ISO19794-5 integer code for the color
 */(private val code: Int) {
    UNSPECIFIED(EYE_COLOR_UNSPECIFIED),
    BLACK(EYE_COLOR_BLACK),
    BLUE(EYE_COLOR_BLUE),
    BROWN(EYE_COLOR_BROWN),
    GRAY(EYE_COLOR_GRAY),
    GREEN(EYE_COLOR_GREEN),
    MULTI_COLORED(EYE_COLOR_MULTI_COLORED),
    PINK(EYE_COLOR_PINK),
    UNKNOWN(EYE_COLOR_UNKNOWN);

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