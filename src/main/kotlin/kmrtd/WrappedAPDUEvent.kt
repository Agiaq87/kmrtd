/*
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
 * $Id: WrappedAPDUEvent.java 1763 2018-02-18 07:41:30Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd

import net.sf.scuba.smartcards.APDUEvent
import net.sf.scuba.smartcards.CommandAPDU
import net.sf.scuba.smartcards.ResponseAPDU
import java.io.Serializable

/**
 * An event signifying an exchange of wrapped (protected) command and response APDUs.
 * This makes the underlying unprotected APDUs available.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1763 $
 * 
 * @since 0.6.4
 */
class WrappedAPDUEvent
/**
 * Creates an APDU exchange event.
 * 
 * @param source the source of the event, typically a card service
 * @param type the type of event, typically this identifies the APDU wrapper somehow
 * @param sequenceNumber the sequence number of the APDU exchange within a session
 * @param plainTextCommandAPDU the unprotected command APDU
 * @param plainTextResponseAPDU the unprotected response APDU
 * @param wrappedCommandAPDU the protected command APDU
 * @param wrappedResponseAPDU the protected command APDU
 */(
    source: Any, type: Serializable?, sequenceNumber: Int,
    /**
     * Returns the unprotected, plain-text Command APDU.
     * 
     * @return the unprotected, plain-text Command APDU
     */
    val plainTextCommandAPDU: CommandAPDU?,
    /**
     * Returns the unprotected, plain-text Response APDU.
     * 
     * @return the unprotected, plain-text Response APDU
     */
    val plainTextResponseAPDU: ResponseAPDU?,
    wrappedCommandAPDU: CommandAPDU?, wrappedResponseAPDU: ResponseAPDU?
) : APDUEvent(source, type, sequenceNumber, wrappedCommandAPDU, wrappedResponseAPDU) {
    companion object {
        private const val serialVersionUID = 5958662425525890224L
    }
}

