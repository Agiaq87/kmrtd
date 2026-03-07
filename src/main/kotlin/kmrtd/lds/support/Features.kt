/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.lds.support

/**
 * Feature flags meaning based on Section 5.5.6 of ISO 19794-5.
 */
enum class Features {
    FEATURES_ARE_SPECIFIED,
    GLASSES,
    MOUSTACHE,
    BEARD,
    TEETH_VISIBLE,
    BLINK,
    MOUTH_OPEN,
    LEFT_EYE_PATCH,
    RIGHT_EYE_PATCH,
    DARK_GLASSES,
    DISTORTING_MEDICAL_CONDITION
}