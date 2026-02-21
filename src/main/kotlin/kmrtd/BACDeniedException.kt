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
 * $Id: BACDeniedException.java 1851 2021-05-27 20:56:53Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd

import net.sf.scuba.smartcards.CardServiceException
import kmrtd.BACKeySpec

/**
 * Exception for signaling failed BAC.
 * 
 * @author The JMRTD team
 * 
 * @version $Revision: 1851 $
 * 
 * @since 0.4.8
 * 
 */
@Deprecated("Use {@link CardServiceProtocolException} instead.")
data class BACDeniedException
/**
 * Creates an exception.
 * 
 * @param msg the message
 * @param bACKey the BAC entry that was tried
 * @param sw status word or `-1`
 */(
    val msg: String,
    /**
     * Returns the BAC key that was tried before BAC failed.
     * 
     * @return a BAC key
     */
    val bACKey: BACKeySpec?,
    val sw: Int
) : CardServiceException(msg, sw) {
    companion object {
        private val serialVersionUID = -7094953658210693249L
    }
}
