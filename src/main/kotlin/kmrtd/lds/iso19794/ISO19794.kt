/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.lds.iso19794

import kmrtd.lds.ImageInfo.JPEG2000_MIME_TYPE
import kmrtd.lds.ImageInfo.JPEG_MIME_TYPE
import kmrtd.lds.ImageInfo.WSQ_MIME_TYPE

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

    /**
     * Consumed in IrisInfo
     */
    object ImageFormat {
        /**
         * Image format.
         */
        const val IMAGEFORMAT_MONO_RAW: Int = 2 /* (0x0002) */

        /**
         * Image format.
         */
        const val IMAGEFORMAT_RGB_RAW: Int = 4 /* (0x0004) */

        /**
         * Image format.
         */
        const val IMAGEFORMAT_MONO_JPEG: Int = 6 /* (0x0006) */

        /**
         * Image format.
         */
        const val IMAGEFORMAT_RGB_JPEG: Int = 8 /* (0x0008) */

        /**
         * Image format.
         */
        const val IMAGEFORMAT_MONO_JPEG_LS: Int = 10 /* (0x000A) */

        /**
         * Image format.
         */
        const val IMAGEFORMAT_RGB_JPEG_LS: Int = 12 /* (0x000C) */

        /**
         * Image format.
         */
        const val IMAGEFORMAT_MONO_JPEG2000: Int = 14 /* (0x000E) */

        /**
         * Image format.
         */
        const val IMAGEFORMAT_RGB_JPEG2000: Int = 16 /* (0x0010) */

        /**
         * Constant for capture device Id, based on Table 2 in Section 5.5 in ISO 19794-6.
         */
        const val CAPTURE_DEVICE_UNDEF: Int = 0

        /**
         * Constant for horizontal and veritical orientation, based on Table 2 in Section 5.5 in ISO 19794-6.
         */
        const val ORIENTATION_UNDEF: Int = 0

        /**
         * Constant for horizontal and veritical orientation, based on Table 2 in Section 5.5 in ISO 19794-6.
         */
        const val ORIENTATION_BASE: Int = 1

        /**
         * Constant for horizontal and veritical orientation, based on Table 2 in Section 5.5 in ISO 19794-6.
         */
        const val ORIENTATION_FLIPPED: Int = 2

        /**
         * Scan type (rectilinear only), based on Table 2 in Section 5.5 in ISO 19794-6.
         */
        const val SCAN_TYPE_UNDEF: Int = 0

        /**
         * Scan type (rectilinear only), based on Table 2 in Section 5.5 in ISO 19794-6.
         */
        const val SCAN_TYPE_PROGRESSIVE: Int = 1

        /**
         * Scan type (rectilinear only), based on Table 2 in Section 5.5 in ISO 19794-6.
         */
        const val SCAN_TYPE_INTERLACE_FRAME: Int = 2

        /**
         * Scan type (rectilinear only), based on Table 2 in Section 5.5 in ISO 19794-6.
         */
        const val SCAN_TYPE_INTERLACE_FIELD: Int = 3

        /**
         * Scan type (rectilinear only), based on Table 2 in Section 5.5 in ISO 19794-6.
         */
        const val SCAN_TYPE_CORRECTED: Int = 4

        /**
         * Iris occlusion (polar only), based on Table 2 in Section 5.5 in ISO 19794-6.
         */
        const val IROCC_UNDEF: Int = 0

        /**
         * Iris occlusion (polar only), based on Table 2 in Section 5.5 in ISO 19794-6.
         */
        const val IROCC_PROCESSED: Int = 1

        /**
         * Iris occlusion filling (polar only), based on Table 2 in Section 5.5 in ISO 19794-6.
         */
        const val IROCC_ZEROFILL: Int = 0

        /**
         * Iris occlusion filling (polar only), based on Table 2 in Section 5.5 in ISO 19794-6.
         */
        const val IROC_UNITFILL: Int = 1

        /* TODO: reference to specification. */
        const val INTENSITY_DEPTH_UNDEF: Int = 0

        /* TODO: reference to specification. */
        const val TRANS_UNDEF: Int = 0
        const val TRANS_STD: Int = 1

        /* TODO: reference to specification. */
        const val IRBNDY_UNDEF: Int = 0
        const val IRBNDY_PROCESSED: Int = 1

        /**
         * Format identifier 'I', 'I', 'R', 0x00.
         */
        const val FORMAT_IDENTIFIER = 0x49495200

        /**
         * Version number.
         */
        const val VERSION_NUMBER = 0x30313000
    }

    /**
     * Consumed in IrisImageInfo
     */
    object ImageQuality {
        /**
         * Image quality, based on Table 3 in Section 5.5 of ISO 19794-6.
         */
        const val IMAGE_QUAL_UNDEF: Int = 0xFE /* (decimal 254) */

        /* TODO: proper enums for data types */
        /**
         * Image quality, based on Table 3 in Section 5.5 of ISO 19794-6.
         */
        const val IMAGE_QUAL_LOW_LO: Int = 0x1A

        /**
         * Image quality, based on Table 3 in Section 5.5 of ISO 19794-6.
         */
        const val IMAGE_QUAL_LOW_HI: Int = 0x32 /* (decimal 26-50) */

        /**
         * Image quality, based on Table 3 in Section 5.5 of ISO 19794-6.
         */
        const val IMAGE_QUAL_MED_LO: Int = 0x33

        /**
         * Image quality, based on Table 3 in Section 5.5 of ISO 19794-6.
         */
        const val IMAGE_QUAL_MED_HI: Int = 0x4B /* (decimal 51-75) */

        /**
         * Image quality, based on Table 3 in Section 5.5 of ISO 19794-6.
         */
        const val IMAGE_QUAL_HIGH_LO: Int = 0x4C

        /**
         * Image quality, based on Table 3 in Section 5.5 of ISO 19794-6.
         */
        const val IMAGE_QUAL_HIGH_HI: Int = 0x64 /* (decimal 76-100) */
        const val ROT_ANGLE_UNDEF = 0xFFFF
        const val ROT_UNCERTAIN_UNDEF = 0xFFFF

        /**
         * Returns a mime-type for the given image format code.
         *
         * @param imageFormat the image format code
         * @return a mime-type
         */
        fun getMimeTypeFromImageFormat(imageFormat: Int): String? {
            return when (imageFormat) {
                ImageFormat.IMAGEFORMAT_MONO_RAW,
                ImageFormat.IMAGEFORMAT_RGB_RAW -> WSQ_MIME_TYPE

                ImageFormat.IMAGEFORMAT_MONO_JPEG,
                ImageFormat.IMAGEFORMAT_RGB_JPEG,
                ImageFormat.IMAGEFORMAT_MONO_JPEG_LS,
                ImageFormat.IMAGEFORMAT_RGB_JPEG_LS -> JPEG_MIME_TYPE

                ImageFormat.IMAGEFORMAT_MONO_JPEG2000,
                ImageFormat.IMAGEFORMAT_RGB_JPEG2000 -> JPEG2000_MIME_TYPE

                else -> null
            }
        }
    }
}