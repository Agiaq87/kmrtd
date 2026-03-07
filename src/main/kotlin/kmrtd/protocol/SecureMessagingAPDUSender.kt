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
 * $Id: SecureMessagingAPDUSender.java 1841 2020-09-18 19:11:27Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.protocol

import kmrtd.Util
import kmrtd.WrappedAPDUEvent
import net.sf.scuba.smartcards.APDUEvent
import net.sf.scuba.smartcards.APDUListener
import net.sf.scuba.smartcards.APDUWrapper
import net.sf.scuba.smartcards.CardService
import net.sf.scuba.smartcards.CardServiceException
import net.sf.scuba.smartcards.CommandAPDU
import net.sf.scuba.smartcards.ISO7816
import net.sf.scuba.smartcards.ResponseAPDU
import net.sf.scuba.util.Hex
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.logging.Level
import java.util.logging.Logger

/**
 * An APDU sender for tranceiving wrapped APDUs.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * @version $Revision: 1841 $
 * @since 0.7.0
 */
class SecureMessagingAPDUSender(private val service: CardService) {

    private var apduCount: Int = 0

    /**
     * Transmits an APDU.
     * 
     * @param wrapper     the secure messaging wrapper
     * @param commandAPDU the APDU to send
     * @return the APDU received from the PICC
     * @throws CardServiceException if tranceiving failed
     */
    @Throws(CardServiceException::class)
    fun transmit(wrapper: APDUWrapper?, commandAPDU: CommandAPDU): ResponseAPDU {
        var commandAPDU = commandAPDU
        val plainCapdu = commandAPDU
        if (wrapper != null) {
            commandAPDU = wrapper.wrap(commandAPDU)
        }
        var responseAPDU = service.transmit(commandAPDU)
        val rawRapdu: ResponseAPDU? = responseAPDU
        val sw = responseAPDU.sw.toShort()
        if (wrapper == null) {
            notifyExchangedAPDU(APDUEvent(this, "PLAIN", ++apduCount, commandAPDU, responseAPDU))
        } else {
            try {
                if ((sw.toInt() and ISO7816.SW_WRONG_LENGTH.toInt()) == ISO7816.SW_WRONG_LENGTH.toInt()) {
                    return responseAPDU
                }
                if (responseAPDU.bytes.size <= 2) {
                    throw CardServiceException(
                        ("Exception during transmission of wrapped APDU"
                                + ", C=" + Hex.bytesToHexString(plainCapdu.bytes)), sw.toInt()
                    )
                }

                responseAPDU = wrapper.unwrap(responseAPDU)
            } catch (cse: CardServiceException) {
                throw cse
            } catch (e: Exception) {
                throw CardServiceException(
                    ("Exception during transmission of wrapped APDU"
                            + ", C=" + Hex.bytesToHexString(plainCapdu.bytes)), e, sw.toInt()
                )
            } finally {
                notifyExchangedAPDU(
                    WrappedAPDUEvent(
                        this,
                        wrapper.type,
                        ++apduCount,
                        plainCapdu,
                        responseAPDU,
                        commandAPDU,
                        rawRapdu
                    )
                )
            }
        }

        return responseAPDU
    }

    val isExtendedAPDULengthSupported: Boolean
        /**
         * Returns a boolean indicating whether extended length APDUs are supported.
         * 
         * @return a boolean indicating whether extended length APDUs are supported
         */
        get() = service.isExtendedAPDULengthSupported

    /**
     * Adds a listener.
     * 
     * @param l the listener to add
     */
    fun addAPDUListener(l: APDUListener?) {
        service.addAPDUListener(l)
    }

    /**
     * Removes a listener.
     * If the specified listener is not present, this method has no effect.
     * 
     * @param l the listener to remove
     */
    fun removeAPDUListener(l: APDUListener?) {
        service.removeAPDUListener(l)
    }

    /**
     * Notifies listeners about APDU event.
     * 
     * @param event the APDU event
     */
    protected fun notifyExchangedAPDU(event: APDUEvent?) {
        val apduListeners = service.apduListeners
        if (apduListeners == null || apduListeners.isEmpty()) {
            return
        }

        for (listener in apduListeners) {
            listener.exchangedAPDU(event)
        }
    }

    /* EXPERIMENTAL CODE BELOW */
    /**
     * Sends a (lengthy) command APDU using command chaining as described in ISO 7816-4 5.3.3.
     * 
     * @param commandAPDU the command APDU to send
     * @param chunkSize   the maximum size of data within each APDU
     * @return the resulting response APDUs that were received
     * @throws CardServiceException on error while sending
     */
    @Throws(CardServiceException::class)
    private fun sendUsingCommandChaining(
        commandAPDU: CommandAPDU,
        chunkSize: Int
    ): MutableList<ResponseAPDU?> {
        val data = commandAPDU.getData()
        val segments = Util.partition(chunkSize, data)
        val responseAPDUs: MutableList<ResponseAPDU?> = ArrayList(segments.size)
        var index = 0
        for (segment in segments) {
            val isLast = ++index >= segments.size
            var cla = commandAPDU.cla
            if (!isLast) {
                cla = cla or ISO7816.CLA_COMMAND_CHAINING.toInt()
            }
            val partialCommandAPDU = CommandAPDU(
                cla,
                commandAPDU.ins,
                commandAPDU.p1,
                commandAPDU.p2,
                segment,
                commandAPDU.ne
            )
            val responseAPDU = service.transmit(partialCommandAPDU)
            responseAPDUs.add(responseAPDU)
        }

        return responseAPDUs
    }

    /**
     * Response chaining as described in ISO 7816-4 Section 5.3.4.
     * This will send additional `GET RESPONSE` APDUs.
     * 
     * @param wrapper a secure messaging wrapper
     * @param sw      the status word of the first APDU, of which the first byte is `0x61`
     * @param data    the data of the first response APDU
     * @return the total amount of data
     * @throws CardServiceException on error while sending
     */
    @Throws(CardServiceException::class)
    private fun continueSendingUsingResponseChaining(
        wrapper: APDUWrapper?,
        sw: Short,
        data: ByteArray
    ): ByteArray {
        var sw = sw
        var data = data
        val byteArrayOutputStream = ByteArrayOutputStream()
        try {
            while ((sw.toInt() and 0xFF00) == 0x6100) {
                /* More bytes remaining. */
                byteArrayOutputStream.write(data)

                val remainingLength = sw.toInt() and 0xFF
                if (remainingLength <= 0) {
                    break
                }
                val capdu = CommandAPDU(
                    ISO7816.CLA_ISO7816.toInt(),
                    ISO7816.INS_GET_RESPONSE.toInt(),
                    0x00,
                    0x00,
                    remainingLength
                )
                val rapdu = transmit(wrapper, capdu)
                data = rapdu.getData()
                sw = rapdu.sw.toShort()
            }

            return byteArrayOutputStream.toByteArray()
        } catch (ioe: IOException) {
            /* NOTE: Unlikely, we can always write to in-memory stream. */
            throw CardServiceException("Could not write to stream", ioe, sw.toInt())
        } finally {
            try {
                byteArrayOutputStream.close()
            } catch (ioe: IOException) {
                LOGGER.log(Level.FINE, "Error closing stream", ioe)
            }
        }
    }

    companion object {
        private val LOGGER: Logger = Logger.getLogger("org.jmrtd.protocol")
    }
}
