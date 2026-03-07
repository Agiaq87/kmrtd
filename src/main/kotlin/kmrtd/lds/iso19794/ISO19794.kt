/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.lds.iso19794

object ISO19794 {
    /* These correspond to values in Table 4 in 5.5.4 in ISO/IEC 19794-5:2005(E). */
    const val EYE_COLOR_UNSPECIFIED: Int = 0x00
    const val EYE_COLOR_BLACK: Int = 0x01
    const val EYE_COLOR_BLUE: Int = 0x02
    const val EYE_COLOR_BROWN: Int = 0x03
    const val EYE_COLOR_GRAY: Int = 0x04
    const val EYE_COLOR_GREEN: Int = 0x05
    const val EYE_COLOR_MULTI_COLORED: Int = 0x06
    const val EYE_COLOR_PINK: Int = 0x07
    const val EYE_COLOR_UNKNOWN: Int = 0xFF
    const val HAIR_COLOR_UNSPECIFIED: Int = 0x00
    const val HAIR_COLOR_BALD: Int = 0x01
    const val HAIR_COLOR_BLACK: Int = 0x02
    const val HAIR_COLOR_BLONDE: Int = 0x03
    const val HAIR_COLOR_BROWN: Int = 0x04
    const val HAIR_COLOR_GRAY: Int = 0x05
    const val HAIR_COLOR_WHITE: Int = 0x06
    const val HAIR_COLOR_RED: Int = 0x07
    const val HAIR_COLOR_GREEN: Int = 0x08
    const val HAIR_COLOR_BLUE: Int = 0x09
    const val HAIR_COLOR_UNKNOWN: Int = 0xFF
    const val EXPRESSION_UNSPECIFIED: Short = 0x0000
    const val EXPRESSION_NEUTRAL: Short = 0x0001
    const val EXPRESSION_SMILE_CLOSED: Short = 0x0002
    const val EXPRESSION_SMILE_OPEN: Short = 0x0003
    const val EXPRESSION_RAISED_EYEBROWS: Short = 0x0004
    const val EXPRESSION_EYES_LOOKING_AWAY: Short = 0x0005
    const val EXPRESSION_SQUINTING: Short = 0x0006
    const val EXPRESSION_FROWNING: Short = 0x0007
    const val FACE_IMAGE_TYPE_BASIC: Int = 0x00
    const val FACE_IMAGE_TYPE_FULL_FRONTAL: Int = 0x01
    const val FACE_IMAGE_TYPE_TOKEN_FRONTAL: Int = 0x02
    const val IMAGE_DATA_TYPE_JPEG: Int = 0x00
    const val IMAGE_DATA_TYPE_JPEG2000: Int = 0x01
    const val IMAGE_COLOR_SPACE_UNSPECIFIED: Int = 0x00
    const val IMAGE_COLOR_SPACE_RGB24: Int = 0x01
    const val IMAGE_COLOR_SPACE_YUV422: Int = 0x02
    const val IMAGE_COLOR_SPACE_GRAY8: Int = 0x03
    const val IMAGE_COLOR_SPACE_OTHER: Int = 0x04
    const val SOURCE_TYPE_UNSPECIFIED: Int = 0x00
    const val SOURCE_TYPE_STATIC_PHOTO_UNKNOWN_SOURCE: Int = 0x01
    const val SOURCE_TYPE_STATIC_PHOTO_DIGITAL_CAM: Int = 0x02
    const val SOURCE_TYPE_STATIC_PHOTO_SCANNER: Int = 0x03
    const val SOURCE_TYPE_VIDEO_FRAME_UNKNOWN_SOURCE: Int = 0x04
    const val SOURCE_TYPE_VIDEO_FRAME_ANALOG_CAM: Int = 0x05
    const val SOURCE_TYPE_VIDEO_FRAME_DIGITAL_CAM: Int = 0x06
    const val SOURCE_TYPE_UNKNOWN: Int = 0x07

    const val FEATURE_FEATURES_ARE_SPECIFIED_FLAG = 0x000001
    const val FEATURE_GLASSES_FLAG = 0x000002
    const val FEATURE_MOUSTACHE_FLAG = 0x000004
    const val FEATURE_BEARD_FLAG = 0x000008
    const val FEATURE_TEETH_VISIBLE_FLAG = 0x000010
    const val FEATURE_BLINK_FLAG = 0x000020
    const val FEATURE_MOUTH_OPEN_FLAG = 0x000040
    const val FEATURE_LEFT_EYE_PATCH_FLAG = 0x000080
    const val FEATURE_RIGHT_EYE_PATCH = 0x000100
    const val FEATURE_DARK_GLASSES = 0x000200
    const val FEATURE_DISTORTING_MEDICAL_CONDITION = 0x000400

    /**
     * Indexes into poseAngle array.
     */
    const val YAW = 0

    /**
     * Indexes into poseAngle array.
     */
    const val PITCH = 1

    /**
     * Indexes into poseAngle array.
     */
    const val ROLL = 2
}