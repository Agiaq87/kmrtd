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
 * $Id: APDULevelEACCACapable.java 1802 2018-11-06 16:29:28Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD 
 *
 * Licensed under LGPL 3.0
 */
package kmrtd

import net.sf.scuba.smartcards.APDUWrapper
import net.sf.scuba.smartcards.CardServiceException
import java.math.BigInteger

/**
 * The low-level capability of sending APDUs for the (EAC) Chip Authentication protocol (version 1).
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1802 $
 */
interface APDULevelEACCACapable {
    /**
     * The MSE KAT APDU, see EAC 1.11 spec, Section B.1.
     * 
     * @param wrapper secure messaging wrapper
     * @param keyData key data object (tag 0x91)
     * @param idData key id data object (tag 0x84), can be null
     * 
     * @throws CardServiceException on error
     */
    @Throws(CardServiceException::class)
    fun sendMSEKAT(wrapper: APDUWrapper?, keyData: ByteArray?, idData: ByteArray?)

    /**
     * The  MSE Set AT for Chip Authentication.
     * 
     * @param wrapper secure messaging wrapper
     * @param oid the OID
     * @param keyId the keyId or `null`
     * 
     * @throws CardServiceException on error
     */
    @Throws(CardServiceException::class)
    fun sendMSESetATIntAuth(wrapper: APDUWrapper?, oid: String?, keyId: BigInteger?)

    /**
     * Sends a General Authenticate command.
     * 
     * @param wrapper secure messaging wrapper
     * @param data data to be sent, without the `0x7C` prefix (this method will add it)
     * @param isLast indicates whether this is the last command in the chain
     * 
     * @return dynamic authentication data without the `0x7C` prefix (this method will remove it)
     * 
     * @throws CardServiceException on error
     */
    @Throws(CardServiceException::class)
    fun sendGeneralAuthenticate(wrapper: APDUWrapper?, data: ByteArray?, isLast: Boolean): ByteArray?
}
