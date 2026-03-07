/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.protocol

import kmrtd.APDULevelReadBinaryCapable
import net.sf.scuba.smartcards.APDUWrapper
import net.sf.scuba.smartcards.CardService
import net.sf.scuba.smartcards.CardServiceException
import net.sf.scuba.smartcards.CommandAPDU
import net.sf.scuba.smartcards.ISO7816
import net.sf.scuba.smartcards.ResponseAPDU
import net.sf.scuba.util.Hex
import java.util.logging.Level
import java.util.logging.Logger

/**
 * An APDU sender to support reading binaries. both selection and short file identifier based.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * @version $Revision: 1850 $
 * @since 0.7.0
 */
class ReadBinaryAPDUSender(private val service: CardService) : APDULevelReadBinaryCapable {
    private val secureMessagingSender: SecureMessagingAPDUSender =
        SecureMessagingAPDUSender(service)

    /**
     * Sends a `SELECT APPLET` command to the card.
     * 
     * @param wrapper the secure messaging wrapper to use
     * @param aid     the applet to select
     * @throws CardServiceException on tranceive error
     */
    @Synchronized
    @Throws(CardServiceException::class)
    override fun sendSelectApplet(wrapper: APDUWrapper?, aid: ByteArray) {
        //requireNotNull(aid) { "AID cannot be null" }
        val commandAPDU = CommandAPDU(
            ISO7816.CLA_ISO7816.toInt(),
            ISO7816.INS_SELECT_FILE.toInt(),
            0x04.toByte().toInt(),
            0x0C.toByte().toInt(),
            aid
        )
        val responseAPDU = secureMessagingSender.transmit(wrapper, commandAPDU)
        checkStatusWordAfterFileOperation(commandAPDU, responseAPDU)
    }

    /**
     * Sends a `SELECT MF` command to the card.
     * 
     * @throws CardServiceException on tranceive error
     */
    @Synchronized
    @Throws(CardServiceException::class)
    override fun sendSelectMF() {
        val commandAPDU = CommandAPDU(
            ISO7816.CLA_ISO7816.toInt(),
            ISO7816.INS_SELECT_FILE.toInt(),
            0x00.toByte().toInt(),
            0x0C.toByte().toInt(),
            byteArrayOf(0x3F, 0x00)
        )
        val responseAPDU = secureMessagingSender.transmit(null, commandAPDU)
        checkStatusWordAfterFileOperation(commandAPDU, responseAPDU)
    }

    /* PRIVATE BELOW */
    /**
     * Sends a `SELECT FILE` command to the passport. Secure
     * messaging will be applied to the command and response apdu.
     * 
     * @param wrapper the secure messaging wrapper to use
     * @param fid     the file to select
     * @throws CardServiceException on tranceive error
     */
    @Synchronized
    @Throws(CardServiceException::class)
    override fun sendSelectFile(wrapper: APDUWrapper?, fid: Short) {
        val fiddle =
            byteArrayOf(((fid.toInt() shr 8) and 0xFF).toByte(), (fid.toInt() and 0xFF).toByte())
        val commandAPDU = CommandAPDU(
            ISO7816.CLA_ISO7816.toInt(),
            ISO7816.INS_SELECT_FILE.toInt(),
            0x02.toByte().toInt(),
            0x0c.toByte().toInt(),
            fiddle,
            0
        )
        val responseAPDU = secureMessagingSender.transmit(wrapper, commandAPDU)

        checkStatusWordAfterFileOperation(commandAPDU, responseAPDU)
    }

    /**
     * Sends a `READ BINARY` command to the passport.
     * Secure messaging will be applied to the command and response APDU.
     * 
     * @param wrapper                  the secure messaging wrapper to use, or `null` for none
     * @param sfi                      the short file identifier byte of the file to read as an int value (between 0 and 255)
     * only if `isSFIEnabled` is `true`, if not any value)
     * @param offset                   offset into the file
     * (either a value between 0 and 255 if `isSFIEnabled` is `true`,
     * of a value between 0 and 65535 if not)
     * @param le                       the expected length of the file to read
     * @param isSFIEnabled             a boolean indicating whether short file identifiers are used
     * @param isTLVEncodedOffsetNeeded a boolean indicating whether it should be a long (`INS == 0xB1`) read
     * @return a byte array of length at most `le` with (the specified part of) the contents of the currently selected file
     * @throws CardServiceException if the command was not successful
     */
    @Synchronized
    @Throws(CardServiceException::class)
    override fun sendReadBinary(
        wrapper: APDUWrapper?,
        sfi: Int,
        offset: Int,
        le: Int,
        isSFIEnabled: Boolean,
        isTLVEncodedOffsetNeeded: Boolean
    ): ByteArray? {
        var le = le
        var commandAPDU: CommandAPDU? = null
        var responseAPDU: ResponseAPDU? = null

        // In case the data ended right on the block boundary
        if (le == 0) {
            return null
        }

        val offsetMSB = ((offset and 0xFF00) shr 8).toByte()
        val offsetLSB = (offset and 0xFF).toByte()

        if (isTLVEncodedOffsetNeeded) {
            // In the case of long read 2 or 3 bytes less of the actual data will be returned,
            // because a tag and length will be sent along, here we need to account for this.
            if (le < 128) {
                le += 2
            } else if (le < 256) {
                le += 3
            }
            if (le > 256) {
                le = 256
            }

            val data = byteArrayOf(0x54, 0x02, offsetMSB, offsetLSB)
            commandAPDU = CommandAPDU(
                ISO7816.CLA_ISO7816.toInt(),
                ISO7816.INS_READ_BINARY2.toInt(),
                0,
                0,
                data,
                le
            )
        } else if (isSFIEnabled) {
            commandAPDU = CommandAPDU(
                ISO7816.CLA_ISO7816.toInt(),
                ISO7816.INS_READ_BINARY.toInt(),
                sfi.toByte().toInt(),
                offsetLSB.toInt(),
                le
            )
        } else {
            commandAPDU = CommandAPDU(
                ISO7816.CLA_ISO7816.toInt(),
                ISO7816.INS_READ_BINARY.toInt(),
                offsetMSB.toInt(),
                offsetLSB.toInt(),
                le
            )
        }

        var sw = ISO7816.SW_UNKNOWN
        try {
            responseAPDU = secureMessagingSender.transmit(wrapper, commandAPDU)
            sw = responseAPDU.sw.toShort()
        } catch (cse: CardServiceException) {
            if (service.isConnectionLost(cse)) {
                /*
                 * If fatal, we rethrow the underlying exception.
                 * If not, we will probably throw an exception later on (in checkStatusWord...).
                 * FIXME: Consider not catching this cse at all? -- MO
                 */
                throw cse
            }

            LOGGER.log(Level.FINE, "Exception during READ BINARY", cse)
            sw = cse.getSW().toShort()
        }

        val responseData: ByteArray? = getResponseData(responseAPDU, isTLVEncodedOffsetNeeded)
        if (responseData == null || responseData.isEmpty()) {
            LOGGER.warning(
                "Empty response data: response APDU bytes = " + responseData.contentToString() + ", le = " + le + ", sw = " + Integer.toHexString(
                    sw.toInt()
                )
            )
        }
        Companion.checkStatusWordAfterFileOperation(commandAPDU, responseAPDU!!)

        return responseData
    }

    companion object {
        private val LOGGER: Logger = Logger.getLogger("org.jmrtd.protocol")

        /**
         * Returns the response data from a response APDU.
         * 
         * @param responseAPDU             the response APDU
         * @param isTLVEncodedOffsetNeeded whether to expect a `0x53` tag encoded value
         * @return the response data
         * @throws CardServiceException on error
         */
        @Throws(CardServiceException::class)
        private fun getResponseData(
            responseAPDU: ResponseAPDU?,
            isTLVEncodedOffsetNeeded: Boolean
        ): ByteArray? {
            if (responseAPDU == null) {
                return null
            }

            var responseData = responseAPDU.getData()
                ?: throw CardServiceException("Malformed read binary long response data")
            if (!isTLVEncodedOffsetNeeded) {
                return responseData
            }

            /*
         * Strip the response off the tag 0x53 and the length field.
         * FIXME: Use TLVUtil.tlvEncode(...) here. -- MO
         */
            val data = responseData
            var index = 0
            if (data[index++] != 0x53.toByte()) { // FIXME: Constant for 0x53.
                throw CardServiceException("Malformed read binary long response data")
            }
            if ((data[index].toInt() and 0x80).toByte() == 0x80.toByte()) {
                index += (data[index].toInt() and 0xF)
            }
            index++
            responseData = ByteArray(data.size - index)
            System.arraycopy(data, index, responseData, 0, responseData.size)
            return responseData
        }

        /**
         * Checks the status word and throws an appropriate `CardServiceException` on error.
         * 
         * @param commandAPDU  the command APDU that was sent
         * @param responseAPDU the response APDU that was received
         * @throws CardServiceException if the response APDU's status word indicates some error
         */
        @Throws(CardServiceException::class)
        private fun checkStatusWordAfterFileOperation(
            commandAPDU: CommandAPDU,
            responseAPDU: ResponseAPDU
        ) {
            val data = responseAPDU.getData()
            val sw = responseAPDU.sw.toShort()
            val commandResponseMessage =
                "CAPDU = " + Hex.bytesToHexString(commandAPDU.bytes) + ", RAPDU = " + Hex.bytesToHexString(
                    responseAPDU.bytes
                )

            /* If wrong length (6700) and no data. We abort. */
            if ((sw.toInt() and ISO7816.SW_WRONG_LENGTH.toInt()) == ISO7816.SW_WRONG_LENGTH.toInt() && (data == null || data.isEmpty())) {
                throw CardServiceException("Wrong length, $commandResponseMessage", sw.toInt())
            }

            when (sw) {
                ISO7816.SW_NO_ERROR -> return
                ISO7816.SW_END_OF_FILE -> if (data == null || data.isEmpty()) {
                    throw CardServiceException("End of file, $commandResponseMessage", sw.toInt())
                } else {
                    /* May have data. Caller should check SW and stop calling on EOF. */
                    return
                }

                ISO7816.SW_FILE_NOT_FOUND -> throw CardServiceException(
                    "File not found, $commandResponseMessage",
                    sw.toInt()
                )

                ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED, ISO7816.SW_CONDITIONS_NOT_SATISFIED, ISO7816.SW_COMMAND_NOT_ALLOWED -> throw CardServiceException(
                    "Access to file denied, $commandResponseMessage",
                    sw.toInt()
                )

                else -> throw CardServiceException(
                    "Error occured, $commandResponseMessage",
                    sw.toInt()
                )
            }
        }
    }
}
