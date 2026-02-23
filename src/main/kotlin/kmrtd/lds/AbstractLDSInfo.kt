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
 * $Id: AbstractLDSInfo.java 1751 2018-01-15 15:35:45Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.lds

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Base class for data structures that are contained in files in the LDS.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1751 $
 */
abstract class AbstractLDSInfo : LDSElement {
    override val encoded: ByteArray?
        /**
         * Returns an encoding of this LDS info.
         * 
         * @return the LDS info encoded as byte array
         */
        get() {
            try {
                val outputStream = ByteArrayOutputStream()
                writeObject(outputStream)
                outputStream.flush()
                return outputStream.toByteArray()
            } catch (ioe: IOException) {
                LOGGER.log(Level.WARNING, "Exception: ", ioe)
                return null
            }
        }

    /**
     * Writes this LDS info to a stream.
     * 
     * @param outputStream the stream to write to
     * 
     * @throws IOException on error writing to the stream
     */
    @Throws(IOException::class)
    abstract fun writeObject(outputStream: OutputStream?)

    companion object {
        private val LOGGER: Logger = Logger.getLogger("kmrtd")

        private val serialVersionUID = -2340098256249194537L
    }
}
