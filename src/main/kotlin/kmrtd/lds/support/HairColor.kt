/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.lds.support

import kmrtd.lds.iso19794.FaceImageInfo.Companion.HAIR_COLOR_BALD
import kmrtd.lds.iso19794.FaceImageInfo.Companion.HAIR_COLOR_BLACK
import kmrtd.lds.iso19794.FaceImageInfo.Companion.HAIR_COLOR_BLONDE
import kmrtd.lds.iso19794.FaceImageInfo.Companion.HAIR_COLOR_BLUE
import kmrtd.lds.iso19794.FaceImageInfo.Companion.HAIR_COLOR_BROWN
import kmrtd.lds.iso19794.FaceImageInfo.Companion.HAIR_COLOR_GRAY
import kmrtd.lds.iso19794.FaceImageInfo.Companion.HAIR_COLOR_GREEN
import kmrtd.lds.iso19794.FaceImageInfo.Companion.HAIR_COLOR_RED
import kmrtd.lds.iso19794.FaceImageInfo.Companion.HAIR_COLOR_UNKNOWN
import kmrtd.lds.iso19794.FaceImageInfo.Companion.HAIR_COLOR_UNSPECIFIED
import kmrtd.lds.iso19794.FaceImageInfo.Companion.HAIR_COLOR_WHITE

/**
 * Hair color code based on Section 5.5.5 of ISO 19794-5.
 */
enum class HairColor
/**
 * Creates a hair color.
 *
 * @param code the integer code for a color
 */(private val code: Int) {
    UNSPECIFIED(HAIR_COLOR_UNSPECIFIED),
    BALD(HAIR_COLOR_BALD),
    BLACK(HAIR_COLOR_BLACK),
    BLONDE(HAIR_COLOR_BLONDE),
    BROWN(HAIR_COLOR_BROWN),
    GRAY(HAIR_COLOR_GRAY),
    WHITE(HAIR_COLOR_WHITE),
    RED(HAIR_COLOR_RED),
    GREEN(HAIR_COLOR_GREEN),
    BLUE(HAIR_COLOR_BLUE),
    UNKNOWN(HAIR_COLOR_UNKNOWN);

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