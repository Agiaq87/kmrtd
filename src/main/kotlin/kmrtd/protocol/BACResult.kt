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
 * $Id: BACResult.java 1781 2018-05-25 11:41:48Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.protocol

import kmrtd.AccessKeySpec
import java.io.Serializable

/**
 * Result of a Basic Access Control protocol run.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * @version $Revision: 1781 $
 */
data class BACResult
/**
 * Creates a BAC result.
 * 
 * @param bACKey  the initial access key
 * @param wrapper the secure messaging wrapper that resulted from the BAC protocol run
 */(
    /**
     * Returns the initial access key or `null`.
     * 
     * @return the initial access key or `null`
     */
    val bACKey: AccessKeySpec?,
    /**
     * Returns the secure messaging wrapper.
     * 
     * @return the secure messaging wrapper
     */
    val wrapper: SecureMessagingWrapper?
) : Serializable {
    /**
     * Creates a BAC result without specifying the initial access key.
     * 
     * @param wrapper the secure messaging wrapper that resulted from the BAC protocol run
     */
    constructor(wrapper: SecureMessagingWrapper?) : this(null, wrapper)

}
