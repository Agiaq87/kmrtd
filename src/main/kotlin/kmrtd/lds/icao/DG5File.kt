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
 * $Id: DG5File.java 1751 2018-01-15 15:35:45Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.lds.icao

import kmrtd.lds.DisplayedImageDataGroup
import kmrtd.lds.DisplayedImageInfo
import kmrtd.lds.LDSFile
import java.io.InputStream

/**
 * File structure for the EF_DG5 file.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1751 $
 */
class DG5File : DisplayedImageDataGroup {
    /**
     * Constructs a new file from a list of displayed images.
     * 
     * @param images the displayed images, all of which should be of type *Portrait*
     */
    constructor(images: MutableList<DisplayedImageInfo?>) : super(
        LDSFile.EF_DG5_TAG,
        images,
        DisplayedImageInfo.DISPLAYED_PORTRAIT_TAG
    )

    /**
     * Constructs a new file from binary representation.
     * 
     * @param inputStream an input stream
     * 
     * @throws IOException on error reading input stream
     */
    constructor(inputStream: InputStream?) : super(LDSFile.EF_DG5_TAG, inputStream)

    companion object {
        private const val serialVersionUID = 923840683207218244L
    }
}
