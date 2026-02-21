/*
 * JMRTD - A Java API for accessing machine readable travel documents.
 *
 * Copyright (C) 2006 - 2018  The JMRTD team
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *
 * $Id: ImageInfo.java 1808 2019-03-07 21:32:19Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.lds

import java.io.InputStream

/**
 * Common interface type for records containing an encoded image.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1808 $
 */
interface ImageInfo : LDSElement {
    /**
     * Returns the (biometric) type of the image.
     * One of
     * [.TYPE_PORTRAIT],
     * [.TYPE_SIGNATURE_OR_MARK],
     * [.TYPE_FINGER],
     * [.TYPE_IRIS].
     * 
     * @return type of image
     */
    val type: Int

    /**
     * Returns the mime-type of the encoded image as a `String`.
     * 
     * @return mime-type string
     */
    val mimeType: String

    /**
     * Returns the width of the image in pixels.
     * 
     * @return image width
     */
    val width: Int

    /**
     * Returns the height of the image in pixels.
     * 
     * @return image height
     */
    val height: Int

    /**
     * Returns the length of the total record (header and data) in bytes.
     * 
     * @return the length of the record
     */
    val recordLength: Long

    /**
     * Returns the length of the encoded image in bytes.
     * 
     * @return the length of the image bytes
     */
    val imageLength: Int

    /**
     * Returns an input stream from which the image bytes can be read.
     * 
     * @return image input stream
     */
    val imageInputStream: InputStream

    companion object {
        /** Mime-type.  */
        const val JPEG_MIME_TYPE: String = "image/jpeg"

        /** Mime-type.  */
        const val JPEG2000_MIME_TYPE: String = "image/jp2"

        /** Mime-type.  */
        const val WSQ_MIME_TYPE: String = "image/x-wsq"

        /** Type of image.  */
        val TYPE_UNKNOWN: Int = -1

        /** Type of image.  */
        const val TYPE_PORTRAIT: Int = 0

        /** Type of image.  */
        const val TYPE_SIGNATURE_OR_MARK: Int = 1

        /** Type of image.  */
        const val TYPE_FINGER: Int = 2

        /** Type of image.  */
        const val TYPE_IRIS: Int = 3
    }
}
