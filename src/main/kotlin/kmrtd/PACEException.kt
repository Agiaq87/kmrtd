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
 * $Id: PACEException.java 1851 2021-05-27 20:56:53Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd

/**
 * An exception to signal errors during execution of the PACE protocol.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1851 $
 * 
 */
@Deprecated("Use {@link CardServiceProtocolException} instead.")
class PACEException : CardServiceProtocolException {
    /**
     * Creates a `PACEException`.
     * 
     * @param msg a message
     * @param step the protocol step that failed
     */
    constructor(msg: String?, step: Int) : super(msg, step)

    /**
     * Creates a `PACEException`.
     * 
     * @param msg a message
     * @param step the protocol step that failed
     * @param cause the exception causing this exception
     */
    constructor(msg: String?, step: Int, cause: Throwable?) : super(msg, step, cause)

    /**
     * Creates a PACEException with a specific status word.
     * 
     * @param msg a message
     * @param step the protocol step that failed
     * @param sw the status word that caused this CardServiceException
     */
    constructor(msg: String?, step: Int, sw: Int) : super(msg, step, sw)

    /**
     * Creates a PACEException with a specific status word.
     * 
     * @param msg a message
     * @param step the protocol step that failed
     * @param cause the exception causing this exception
     * @param sw the status word that caused this CardServiceException
     */
    constructor(msg: String?, step: Int, cause: Throwable?, sw: Int) : super(msg, step, cause, sw)

    companion object {
        private const val serialVersionUID = 8383980807753919040L
    }
}
