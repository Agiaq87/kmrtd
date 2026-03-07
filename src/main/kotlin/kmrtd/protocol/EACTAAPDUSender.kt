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
 * $Id: EACTAAPDUSender.java 1799 2018-10-30 16:25:48Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.protocol

import kmrtd.APDULevelEACTACapable
import net.sf.scuba.smartcards.APDUWrapper
import net.sf.scuba.smartcards.CardService
import net.sf.scuba.smartcards.CardServiceException
import net.sf.scuba.smartcards.CommandAPDU
import net.sf.scuba.smartcards.ISO7816

/**
 * A low-level APDU sender to support the (EAC) Terminal Authentication protocol.
 * 
 * @author The JMRTD team
 * @version $Revision: 1799 $
 * @since 0.7.0
 */
class EACTAAPDUSender(service: CardService) : APDULevelEACTACapable {
    private val secureMessagingSender: SecureMessagingAPDUSender =
        SecureMessagingAPDUSender(service)

    /**
     * The MSE DST APDU, see EAC 1.11 spec, Section B.2.
     * This means that a case 3 APDU is sent, to which no response is expected.
     * 
     * @param wrapper secure messaging wrapper
     * @param data    public key reference data object (tag 0x83)
     * @throws CardServiceException on error
     */
    @Synchronized
    @Throws(CardServiceException::class)
    override fun sendMSESetDST(wrapper: APDUWrapper?, data: ByteArray?) {
        val capdu =
            CommandAPDU(ISO7816.CLA_ISO7816.toInt(), ISO7816.INS_MSE.toInt(), 0x81, 0xB6, data)
        val rapdu = secureMessagingSender.transmit(wrapper, capdu)
        val sw = rapdu.sw.toShort()
        if (sw != ISO7816.SW_NO_ERROR) {
            throw CardServiceException("Sending MSE Set DST failed", sw.toInt())
        }
    }

    /**
     * Sends a perform security operation command in extended length mode.
     * 
     * @param wrapper           secure messaging wrapper
     * @param certBodyData      the certificate body
     * @param certSignatureData signature data
     * @throws CardServiceException on error communicating over the service
     */
    @Synchronized
    @Throws(CardServiceException::class)
    override fun sendPSOExtendedLengthMode(
        wrapper: APDUWrapper?,
        certBodyData: ByteArray,
        certSignatureData: ByteArray
    ) {
        val certData = ByteArray(certBodyData.size + certSignatureData.size)
        System.arraycopy(certBodyData, 0, certData, 0, certBodyData.size)
        System.arraycopy(certSignatureData, 0, certData, certBodyData.size, certSignatureData.size)

        val capdu =
            CommandAPDU(ISO7816.CLA_ISO7816.toInt(), ISO7816.INS_PSO.toInt(), 0, 0xBE, certData)
        val rapdu = secureMessagingSender.transmit(wrapper, capdu)
        val sw = rapdu.sw.toShort()
        if (sw != ISO7816.SW_NO_ERROR) {
            throw CardServiceException("Sending PSO failed", sw.toInt())
        }
    }

    /**
     * The MSE Set AT APDU for TA, see EAC 1.11 spec, Section B.2.
     * MANAGE SECURITY ENVIRONMENT command with SET Authentication Template function.
     * 
     * 
     * Note that caller is responsible for prefixing the byte[] params with specified tags.
     * 
     * @param wrapper secure messaging wrapper
     * @param data    public key reference data object (should already be prefixed with tag 0x83)
     * @throws CardServiceException on error
     */
    @Synchronized
    @Throws(CardServiceException::class)
    override fun sendMSESetATExtAuth(wrapper: APDUWrapper?, data: ByteArray?) {
        val capdu =
            CommandAPDU(ISO7816.CLA_ISO7816.toInt(), ISO7816.INS_MSE.toInt(), 0x81, 0xA4, data)
        val rapdu = secureMessagingSender.transmit(wrapper, capdu)
        val sw = rapdu.sw.toShort()
        if (sw != ISO7816.SW_NO_ERROR) {
            throw CardServiceException("Sending MSE AT failed", sw.toInt())
        }
    }

    /**
     * Sends a `GET CHALLENGE` command to the passport.
     * 
     * @param wrapper secure messaging wrapper
     * @return a byte array of length 8 containing the challenge
     * @throws CardServiceException on tranceive error
     */
    @Synchronized
    @Throws(CardServiceException::class)
    override fun sendGetChallenge(wrapper: APDUWrapper?): ByteArray? {
        val capdu = CommandAPDU(
            ISO7816.CLA_ISO7816.toInt(),
            ISO7816.INS_GET_CHALLENGE.toInt(),
            0x00,
            0x00,
            8
        )
        val rapdu = secureMessagingSender.transmit(wrapper, capdu)
        return rapdu.getData()
    }

    /**
     * Sends the EXTERNAL AUTHENTICATE command.
     * This is used in EAC-TA.
     * 
     * @param wrapper   secure messaging wrapper
     * @param signature terminal signature
     * @throws CardServiceException if the resulting status word different from 9000
     */
    @Synchronized
    @Throws(CardServiceException::class)
    override fun sendMutualAuthenticate(wrapper: APDUWrapper?, signature: ByteArray?) {
        val capdu = CommandAPDU(
            ISO7816.CLA_ISO7816.toInt(),
            ISO7816.INS_EXTERNAL_AUTHENTICATE.toInt(),
            0,
            0,
            signature
        )
        val rapdu = secureMessagingSender.transmit(wrapper, capdu)
        val sw = rapdu.getSW().toShort()
        if (sw != ISO7816.SW_NO_ERROR) {
            throw CardServiceException("Sending External Authenticate failed.", sw.toInt())
        }
    }
}
