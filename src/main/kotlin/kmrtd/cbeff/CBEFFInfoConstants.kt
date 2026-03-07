/*
 * Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.cbeff

object CBEFFInfoConstants {
    /*
 * Biometric type value, based on
 * Section 5.2.1.5 and Table 4 in NISTIR-6529A,
 * Table C.2 in ISO/IEC 7816-11,
 * Section 6.5.6 of ISO/IEC 19785-1.
 */
    /**
     * Biometric type value.
     */
    const val BIOMETRIC_TYPE_NO_INFORMATION_GIVEN: Int = 0x000000

    /**
     * Biometric type value.
     */
    const val BIOMETRIC_TYPE_MULTIPLE_BIOMETRICS_USED: Int = 0x000001

    /**
     * Biometric type value.
     */
    const val BIOMETRIC_TYPE_FACIAL_FEATURES: Int = 0x000002

    /**
     * Biometric type value.
     */
    const val BIOMETRIC_TYPE_VOICE: Int = 0x000004

    /**
     * Biometric type value.
     */
    const val BIOMETRIC_TYPE_FINGERPRINT: Int = 0x000008

    /**
     * Biometric type value.
     */
    const val BIOMETRIC_TYPE_IRIS: Int = 0x000010

    /**
     * Biometric type value.
     */
    const val BIOMETRIC_TYPE_RETINA: Int = 0x000020

    /**
     * Biometric type value.
     */
    const val BIOMETRIC_TYPE_HAND_GEOMETRY: Int = 0x000040

    /**
     * Biometric type value.
     */
    const val BIOMETRIC_TYPE_SIGNATURE_DYNAMICS: Int = 0x000080

    /**
     * Biometric type value.
     */
    const val BIOMETRIC_TYPE_KEYSTROKE_DYNAMICS: Int = 0x000100

    /**
     * Biometric type value.
     */
    const val BIOMETRIC_TYPE_LIP_MOVEMENT: Int = 0x000200

    /**
     * Biometric type value.
     */
    const val BIOMETRIC_TYPE_THERMAL_FACE_IMAGE: Int = 0x000400

    /**
     * Biometric type value.
     */
    const val BIOMETRIC_TYPE_THERMAL_HAND_IMAGE: Int = 0x000800

    /**
     * Biometric type value.
     */
    const val BIOMETRIC_TYPE_GAIT: Int = 0x001000

    /**
     * Biometric type value.
     */
    const val BIOMETRIC_TYPE_BODY_ODOR: Int = 0x002000

    /**
     * Biometric type value.
     */
    const val BIOMETRIC_TYPE_DNA: Int = 0x004000

    /**
     * Biometric type value.
     */
    const val BIOMETRIC_TYPE_EAR_SHAPE: Int = 0x008000

    /**
     * Biometric type value.
     */
    const val BIOMETRIC_TYPE_FINGER_GEOMETRY: Int = 0x010000

    /**
     * Biometric type value.
     */
    const val BIOMETRIC_TYPE_PALM_PRINT: Int = 0x020000

    /**
     * Biometric type value.
     */
    const val BIOMETRIC_TYPE_VEIN_PATTERN: Int = 0x040000

    /**
     * Biometric type value.
     */
    const val BIOMETRIC_TYPE_FOOT_PRINT: Int = 0x080000

    /*
 * Biometric subtype, based on
 * Section 5.1.2.6 and Table 6 in NISTIR-6529A,
 * Table C.3 in ISO/IEC 7816-11,
 * Section 6.5.7 of ISO/IEC 19785-1.
 */
    /**
     * Biometric subtype.
     */
    const val BIOMETRIC_SUBTYPE_NONE: Int = 0x00 /* 00000000 */

    /**
     * Biometric subtype.
     */
    const val BIOMETRIC_SUBTYPE_MASK_RIGHT: Int = 0x01 /* xxxxxx01 */

    /**
     * Biometric subtype.
     */
    const val BIOMETRIC_SUBTYPE_MASK_LEFT: Int = 0x02 /* xxxxxx10 */

    /**
     * Biometric subtype.
     */
    const val BIOMETRIC_SUBTYPE_MASK_THUMB: Int = 0x04 /* xxx001xx */

    /**
     * Biometric subtype.
     */
    const val BIOMETRIC_SUBTYPE_MASK_POINTER_FINGER: Int = 0x08 /* xxx010xx */

    /**
     * Biometric subtype.
     */
    const val BIOMETRIC_SUBTYPE_MASK_MIDDLE_FINGER: Int = 0x0C /* xxx011xx */

    /**
     * Biometric subtype.
     */
    const val BIOMETRIC_SUBTYPE_MASK_RING_FINGER: Int = 0x10 /* xxx100xx */

    /**
     * Biometric subtype.
     */
    const val BIOMETRIC_SUBTYPE_MASK_LITTLE_FINGER: Int = 0x14 /* xxx101xx */
}