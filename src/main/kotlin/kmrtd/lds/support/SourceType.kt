/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.lds.support

/**
 * Source type based on Section 5.7.6 of ISO 19794-5.
 */
enum class SourceType {
    UNSPECIFIED,
    STATIC_PHOTO_UNKNOWN_SOURCE,
    STATIC_PHOTO_DIGITAL_CAM,
    STATIC_PHOTO_SCANNER,
    VIDEO_FRAME_UNKNOWN_SOURCE,
    VIDEO_FRAME_ANALOG_CAM,
    VIDEO_FRAME_DIGITAL_CAM,
    UNKNOWN
}