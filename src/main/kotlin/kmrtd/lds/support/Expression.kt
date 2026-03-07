/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.lds.support

/**
 * Expression code based on Section 5.5.7 of ISO 19794-5.
 */
enum class Expression {
    UNSPECIFIED,
    NEUTRAL,
    SMILE_CLOSED,
    SMILE_OPEN,
    RAISED_EYEBROWS,
    EYES_LOOKING_AWAY,
    SQUINTING,
    FROWNING
}