/*
 * JMRTD - A Java API for accessing machine readable travel documents.
 *
 * Copyright (C) 2006 - 2021  The JMRTD team
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
 * $Id: CardServiceProtocolException.java 1892 2025-03-18 15:15:52Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package org.jmrtd

import net.sf.scuba.smartcards.CardServiceException

/**
 * An exception to signal errors during execution of a protocol.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1892 $
 *
 * @since 0.7.27
 */
open class CardServiceProtocolException : CardServiceException {
    /**
     * Identifies the protocol step that failed.
     * 
     * @return the protocol step that failed
     */
    /** Identifies the protocol step that failed.  */
    val step: Int

    /**
     * Creates a `CardServiceProtocolException`.
     * 
     * @param msg a message
     * @param step the protocol step that failed
     */
    constructor(msg: String, step: Int) : super(msg) {
        this.step = step
    }

    /**
     * Creates a `CardServiceProtocolException`.
     * 
     * @param msg a message
     * @param step the protocol step that failed
     * @param cause the exception causing this exception
     */
    constructor(msg: String, step: Int, cause: Throwable?) : super(msg, cause) {
        this.step = step
    }

    /**
     * Creates a `CardServiceProtocolException` with a specific status word.
     * 
     * @param msg a message
     * @param step the protocol step that failed
     * @param sw the status word that caused this CardServiceException
     */
    constructor(msg: String, step: Int, sw: Int) : super(msg, sw) {
        this.step = step
    }

    /**
     * Creates a `CardServiceProtocolException` with a specific status word.
     * 
     * @param msg a message
     * @param step the protocol step that failed
     * @param cause the exception causing this exception
     * @param sw the status word that caused this CardServiceException
     */
    constructor(msg: String, step: Int, cause: Throwable?, sw: Int) : super(msg, cause, sw) {
        this.step = step
    }

    override val message: String
        get() = "${super.message} (" + "step: $step)"


    companion object {
        private const val serialVersionUID = 8527846223511524125L
    }
}
