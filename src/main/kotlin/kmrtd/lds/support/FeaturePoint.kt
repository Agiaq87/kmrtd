/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.lds.support

/**
 * Feature points as described in Section 5.6.3 of ISO/IEC FCD 19794-5.
 *
 * @author The JMRTD team (info@jmrtd.org)
 * @version $Revision: 1808 $
 */
data class FeaturePoint
/**
 * Constructs a new feature point.
 *
 * @param type      feature point type
 * @param majorCode major code
 * @param minorCode minor code
 * @param x         X-coordinate
 * @param y         Y-coordinate
 */(
    /**
     * Returns the type of this point.
     *
     * @return type
     */
    val type: Int,
    /**
     * Returns the major code of this point.
     *
     * @return major code
     */
    val majorCode: Int,
    /**
     * Returns the minor code of this point.
     *
     * @return minor code
     */
    val minorCode: Int,
    /**
     * Returns the X-coordinate of this point.
     *
     * @return X-coordinate
     */
    val x: Int,
    /**
     * Returns the Y-coordinate of this point.
     *
     * @return Y-coordinate
     */
    val y: Int
) {

    companion object {
        @JvmStatic
        fun from(type: Int, code: Byte, x: Int, y: Int): FeaturePoint {
            val majorCode = code.toInt() shr 4
            val minorCode = code.toInt() and 0x0F

            return FeaturePoint(type, majorCode, minorCode, x, y)
        }
    }
}
