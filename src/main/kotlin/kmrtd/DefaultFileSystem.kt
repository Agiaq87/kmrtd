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
 * $Id: DefaultFileSystem.java 1908 2026-02-20 07:45:56Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd

import net.sf.scuba.smartcards.*
import net.sf.scuba.tlv.TLVInputStream
import net.sf.scuba.util.Hex
import org.jmrtd.APDULevelReadBinaryCapable
import org.jmrtd.io.FragmentBuffer
import org.jmrtd.lds.CVCAFile
import org.jmrtd.lds.LDSFileUtil
import org.jmrtd.protocol.SecureMessagingWrapper
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.Serializable
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.math.min

/**
 * A file system for ICAO MRTDs (and similar file systems).
 * This translates abstract high level selection and read binary commands to
 * concrete low level file related APDUs which are sent to the ICC through the
 * card service.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1908 $
 * 
 * @since 0.7.0
 */
class DefaultFileSystem @JvmOverloads constructor(
    private val service: APDULevelReadBinaryCapable,
    private val isSFIEnabled: Boolean,
    private val fidToSFI: MutableMap<Short?, Byte> = LDSFileUtil.FID_TO_SFI
) : FileSystemStructured {
    /** Indicates the file that is (or should be) selected.  */
    private var selectedFID: Short = 0

    /**
     * Returns the currently set maximum length to be requested in READ BINARY commands.
     * 
     * @return the currently set maximum length to be requested in READ BINARY commands
     */
    var maxReadBinaryLength: Int
        private set

    /**
     * A boolean indicating whether we actually already
     * sent the SELECT command to select {@ code selectedFID}.
     */
    private var isSelected = false

    private val fileInfos: MutableMap<Short?, DefaultFileInfo?>

    private var wrapper: APDUWrapper? = null

    private var oldWrapper: APDUWrapper? = null

    /**
     * Creates a file system.
     * 
     * @param service the card service supporting low-level `SELECT` and/or `READ BINARY` commands
     * @param isSFIEnabled whether the file system should use short file identifiers in `READ BINARY` commands
     * @param fidToSFI maps file identifiers to short file identifiers
     */
    /**
     * Creates a file system.
     * 
     * @param service the card service supporting low-level `SELECT` and/or `READ BINARY` commands
     * @param isSFIEnabled whether the file system should use short file identifiers in `READ BINARY` commands
     */
    init {
        this.fileInfos = HashMap<Short?, DefaultFileInfo?>()
        this.maxReadBinaryLength = PassportService.EXTENDED_MAX_TRANCEIVE_LENGTH
    }

    /**
     * Sets the current wrapper to the given APDU wrapper.
     * Subsequent APDUs will be wrapped before sending to the ICC.
     * 
     * @param wrapper an APDU wrapper
     */
    fun setWrapper(wrapper: APDUWrapper?) {
        oldWrapper = this.wrapper
        this.wrapper = wrapper
    }

    /**
     * Returns the wrapper (secure messaging) currently in use.
     * 
     * @return the wrapper
     */
    fun getWrapper(): APDUWrapper? {
        return wrapper
    }

    /**
     * Returns the selected path.
     * 
     * @return the path components
     * 
     * @throws CardServiceException on error
     */
    @Synchronized
    @Throws(CardServiceException::class)
    override fun getSelectedPath(): Array<FileInfo?>? {
        try {
            val fileInfo = this.fileInfo
            if (fileInfo == null) {
                return null
            } else {
                return arrayOf<DefaultFileInfo>(fileInfo)
            }
        } catch (e: Exception) {
            return null
        }
    }

    /*
   * NOTE: This doesn't actually send a select file command. ReadBinary will do so
   * if needed.
   */
    /**
     * Selects a file.
     * 
     * @param fid indicates the file to select
     * 
     * @throws CardServiceException on error communicating over the service
     */
    @Synchronized
    @Throws(CardServiceException::class)
    override fun selectFile(fid: Short) {
        if (selectedFID == fid) {
            return
        }

        selectedFID = fid
        isSelected = false
    }

    /**
     * Reads a block of bytes.
     * 
     * @param offset offset index in the selected file
     * @param length the number of bytes to read
     * 
     * @return a copy of the bytes read
     * 
     * @throws CardServiceException on error
     */
    @Synchronized
    @Throws(CardServiceException::class)
    override fun readBinary(offset: Int, length: Int): ByteArray? {
        var length = length
        var fileInfo: DefaultFileInfo? = null
        try {
            if (selectedFID <= 0) {
                throw CardServiceException("No file selected")
            }

            /* Check buffer to see if we already have some of the bytes. */
            fileInfo = this.fileInfo
            checkNotNull(fileInfo) { "Could not get file info" }

            length = min(length, maxReadBinaryLength)
            val fragment = fileInfo.getSmallestUnbufferedFragment(offset, length)

            var responseLength = length

            var bytes: ByteArray? = null
            if (fragment.getLength() > 0) {
                if (isSFIEnabled && offset < 256) {
                    val sfi: Byte = fidToSFI.get(selectedFID)!!
                    if (sfi == null) {
                        throw NumberFormatException("Unknown FID " + Integer.toHexString(selectedFID.toInt()))
                    }
                    bytes = sendReadBinary(
                        0x80 or (sfi.toInt() and 0xFF),
                        fragment.getOffset(),
                        fragment.getLength(),
                        false
                    )
                    isSelected = true
                } else {
                    if (!isSelected) {
                        sendSelectFile(selectedFID)
                        isSelected = true
                    }
                    bytes = sendReadBinary(fragment.getOffset(), fragment.getLength(), offset > 32767)
                }

                checkNotNull(bytes) { "Could not read bytes" }

                if (bytes.size > 0) {
                    /* Update buffer with newly read bytes. */
                    fileInfo.addFragment(fragment.getOffset(), bytes)
                }

                /*
         * If we request a block of data, create the return buffer from the actual response length, not the requested Le.
         * The latter causes issues when the returned block has a one byte padding (only 0x80) which ends up being removed but
         * the length is not kept track of, leaving an unwanted 0-byte at the end of the data block, which now has a length
         * of Le, but actually contained Le - 1 data bytes.
         *
         * Bug reproduced using org.jmrtd.AESSecureMessagingWrapper with AES-256.
         */
                if (bytes.size < fragment.getLength()) {
                    responseLength = bytes.size
                }
            }
            /* Shrink wrap the bytes that are now buffered. */
            /* NOTE: That arraycopy looks costly, consider using dest array and offset params instead of byte[] result... -- MO */
            val buffer = fileInfo.getBuffer()

            val result = ByteArray(responseLength)
            System.arraycopy(buffer, offset, result, 0, responseLength)

            return result
        } catch (cse: CardServiceException) {
            val sw = cse.getSW().toShort()
            if ((sw.toInt() and ISO7816.SW_WRONG_LENGTH.toInt()) == ISO7816.SW_WRONG_LENGTH.toInt() && maxReadBinaryLength > PassportService.DEFAULT_MAX_BLOCKSIZE) {
                wrapper = oldWrapper
                maxReadBinaryLength = PassportService.DEFAULT_MAX_BLOCKSIZE
                return byteArrayOf()
            }

            throw CardServiceException(
                "Read binary failed on file " + (if (fileInfo == null) Integer.toHexString(
                    selectedFID.toInt()
                ) else fileInfo), cse
            )
        } catch (e: Exception) {
            throw CardServiceException(
                "Read binary failed on file " + (if (fileInfo == null) Integer.toHexString(
                    selectedFID.toInt()
                ) else fileInfo), e
            )
        }
    }

    @get:Throws(CardServiceException::class)
    @get:Synchronized
    private val fileInfo: DefaultFileInfo?
        /**
         * Returns the file info object for the currently selected file. If this
         * executes normally the result is non-null. If the file has not been
         * read before this will send a READ_BINARY to determine length.
         * 
         * @return a non-null MRTDFileInfo
         * 
         * @throws CardServiceException on error
         */
        get() {
            if (selectedFID <= 0) {
                throw CardServiceException("No file selected")
            }

            var fileInfo = fileInfos.get(selectedFID)

            /* If known file, use file info from cache. */
            if (fileInfo != null) {
                return fileInfo
            }

            /* Not cached, actually read some bytes to determine file info. */
            try {
                /*
                  * Each passport file consists of a TLV structure, read ahead to determine length.
                  * EF.CVCA is the exception and has a fixed length of CVCAFile.LENGTH.
                  */
                var prefix: ByteArray? = null
                if (isSFIEnabled) {
                    val sfi: Byte = fidToSFI.get(selectedFID)!!
                    if (sfi == null) {
                        throw NumberFormatException(
                            "Unknown FID " + Integer.toHexString(
                                selectedFID.toInt()
                            )
                        )
                    }
                    prefix = sendReadBinary(
                        0x80 or (sfi.toInt() and 0XFF),
                        0,
                        READ_AHEAD_LENGTH,
                        false
                    )
                    isSelected = true
                } else {
                    if (!isSelected) {
                        sendSelectFile(selectedFID)
                        isSelected = true
                    }
                    prefix = sendReadBinary(0, READ_AHEAD_LENGTH, false)
                }
                if (prefix == null || prefix.size == 0) {
                    LOGGER.warning(
                        "Something is wrong with prefix, prefix = " + Hex.bytesToHexString(
                            prefix
                        )
                    )
                    return null
                }

                val fileLength: Int = getFileLength(
                    selectedFID,
                    READ_AHEAD_LENGTH,
                    prefix
                )
                if (fileLength < prefix.size) {
                    /* We got more than the file's length. Ignore trailing bytes. */
                    prefix = prefix.copyOf(fileLength)
                }
                fileInfo = DefaultFileInfo(selectedFID, fileLength)
                fileInfo.addFragment(0, prefix)
                fileInfos.put(selectedFID, fileInfo)
                return fileInfo
            } catch (ioe: IOException) {
                throw CardServiceException(
                    "Error getting file info for " + Integer.toHexString(selectedFID.toInt()),
                    ioe
                )
            }
        }

    /**
     * Selects a file within the MRTD application.
     * 
     * @param fid a file identifier
     * 
     * @throws CardServiceException on error
     */
    @Synchronized
    @Throws(CardServiceException::class)
    fun sendSelectFile(fid: Short) {
        service.sendSelectFile(wrapper, fid)
    }

    /**
     * Sends a `READ BINARY` command for the already selected file to the passport,
     * using the wrapper when a secure channel has been set up.
     * 
     * @param offset offset into the file
     * @param le the expected length of the file to read
     * @param isTLVEncodedOffsetNeeded whether to encode the offset in a TLV object (typically for offset larger than 32767)
     * 
     * @return a byte array of length `le` with (the specified part of) the contents of the currently selected file
     * 
     * @throws CardServiceException on tranceive error
     */
    @Synchronized
    @Throws(CardServiceException::class)
    fun sendReadBinary(offset: Int, le: Int, isTLVEncodedOffsetNeeded: Boolean): ByteArray? {
        oldWrapper =
            if (wrapper is SecureMessagingWrapper) SecureMessagingWrapper.getInstance(wrapper as SecureMessagingWrapper) else wrapper
        return service.sendReadBinary(wrapper, NO_SFI, offset, le, false, isTLVEncodedOffsetNeeded)
    }

    /**
     * Sends a `READ BINARY` command using an explicit short file identifier to the passport,
     * using the wrapper when a secure channel has been set up.
     * 
     * @param sfi the short file identifier byte as int value (between 0 and 255)
     * @param offset offset into the file
     * @param le the expected length of the file to read
     * @param isTLVEncodedOffsetNeeded whether to encode the offset in a TLV object (typically for offset larger than 32767)
     * 
     * @return a byte array of length `le` with (the specified part of) the contents of the currently selected file
     * 
     * @throws CardServiceException on tranceive error
     */
    @Synchronized
    @Throws(CardServiceException::class)
    fun sendReadBinary(sfi: Int, offset: Int, le: Int, isTLVEncodedOffsetNeeded: Boolean): ByteArray? {
        return service.sendReadBinary(wrapper, sfi, offset, le, true, isTLVEncodedOffsetNeeded)
    }

    /**
     * A file info for the ICAO MRTD file system.
     * 
     * @author The JMRTD team (info@jmrtd.org)
     * 
     * @version $Revision: 1908 $
     */
    private class DefaultFileInfo(private val fid: Short, length: Int) : FileInfo(), Serializable {
        private val buffer: FragmentBuffer

        /**
         * Constructs a file info.
         * 
         * @param fid indicates which file
         * @param length length of the contents of the file
         */
        init {
            this.buffer = FragmentBuffer(length)
        }

        /**
         * Returns the buffer.
         * 
         * @return the buffer
         */
        fun getBuffer(): ByteArray {
            return buffer.getBuffer()
        }

        /**
         * Returns the file identifier.
         * 
         * @return file identifier
         */
        override fun getFID(): Short {
            return fid
        }

        /**
         * Returns the length of the file.
         * 
         * @return the length of the file
         */
        override fun getFileLength(): Int {
            return buffer.getLength()
        }

        /**
         * Returns a textual representation of this file info.
         * 
         * @return a textual representation of this file info
         */
        override fun toString(): String {
            return Integer.toHexString(fid.toInt())
        }

        /**
         * Returns the smallest unbuffered fragment included in `offset` and `offset + length - 1`.
         * 
         * @param offset the offset
         * @param length the length
         * 
         * @return a fragment smaller than or equal to the fragment indicated by `offset` and `length`
         */
        fun getSmallestUnbufferedFragment(offset: Int, length: Int): FragmentBuffer.Fragment {
            return buffer.getSmallestUnbufferedFragment(offset, length)
        }

        /**
         * Adds a fragment of bytes at a specific offset to this file.
         * 
         * @param offset the offset
         * @param bytes the bytes to be added
         */
        fun addFragment(offset: Int, bytes: ByteArray?) {
            buffer.addFragment(offset, bytes)
        }

        companion object {
            private const val serialVersionUID = 6727369753765119839L
        }
    }

    companion object {
        /** Invalid short identifier.  */
        val NO_SFI: Int = -1

        private val LOGGER: Logger = Logger.getLogger("org.jmrtd")

        /** Number of bytes to read at start of file to determine file length.  */
        private const val READ_AHEAD_LENGTH = 8

        /**
         * Determines the file length by inspecting a prefix of bytes read from
         * the (TLV contents of a) file.
         * 
         * @param fid the file identifier
         * @param le the requested length while requesting the prefix
         * @param prefix the prefix read from the file
         * 
         * @return the file length
         * 
         * @throws IOException on error reading the prefix as a TLV sequence
         */
        @Throws(IOException::class)
        private fun getFileLength(fid: Short, le: Int, prefix: ByteArray): Int {
            if (prefix.size < le) {
                /* We got less than asked for, assume prefix is the complete file. */
                return prefix.size
            }
            val byteArrayInputStream = ByteArrayInputStream(prefix)
            val tlvInputStream = TLVInputStream(byteArrayInputStream)
            try {
                val tag = tlvInputStream.readTag()
                if (tag == CVCAFile.CAR_TAG.toInt()) {
                    return CVCAFile.LENGTH
                }

                /* Determine length based on TLV. */
                val valueLength = tlvInputStream.readLength()
                /* NOTE: we're using a specific property of ByteArrayInputStream's available method here! */
                val tlLength = prefix.size - byteArrayInputStream.available()
                val fileLength = tlLength + valueLength
                return fileLength
            } finally {
                try {
                    tlvInputStream.close()
                } catch (ioe: IOException) {
                    /* Never happens. */
                    LOGGER.log(Level.FINE, "Error closing stream", ioe)
                }
            }
        }
    }
}
