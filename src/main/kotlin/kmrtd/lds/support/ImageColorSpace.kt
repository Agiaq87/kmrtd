/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.lds.support

/**
 * Color space code based on Section 5.7.4 of ISO 19794-5.
 */
enum class ImageColorSpace {
    UNSPECIFIED,
    RGB24,
    YUV422,
    GRAY8,
    OTHER
}