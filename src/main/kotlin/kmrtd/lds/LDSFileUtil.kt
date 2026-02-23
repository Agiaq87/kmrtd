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
 * $Id: LDSFileUtil.java 1885 2024-11-07 09:17:29Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.lds

import kmrtd.PassportService
import kmrtd.lds.icao.*
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Static LDS file methods.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1885 $
 */
object LDSFileUtil {
    private val LOGGER: Logger = Logger.getLogger("kmrtd")

    val FID_TO_SFI: MutableMap<Short?, Byte> = createFIDToSFIMap()

    /**
     * Factory method for creating LDS files for a given input stream.
     * 
     * @param fid file identifier
     * @param inputStream a given input stream
     * 
     * @return a specific file
     * 
     * @throws IOException on reading error from the input stream
     */
    @Throws(IOException::class)
    fun getLDSFile(fid: Short, inputStream: InputStream): AbstractLDSFile {
        when (fid) {
            PassportService.EF_COM -> return COMFile(inputStream)
            PassportService.EF_DG1 -> return DG1File(inputStream)
            PassportService.EF_DG2 -> return DG2File(inputStream)
            PassportService.EF_DG3 -> return DG3File(inputStream)
            PassportService.EF_DG4 -> return DG4File(inputStream)
            PassportService.EF_DG5 -> return DG5File(inputStream)
            PassportService.EF_DG6 -> return DG6File(inputStream)
            PassportService.EF_DG7 -> return DG7File(inputStream)
            PassportService.EF_DG8 -> throw IllegalArgumentException("DG8 files are not yet supported")
            PassportService.EF_DG9 -> throw IllegalArgumentException("DG9 files are not yet supported")
            PassportService.EF_DG10 -> throw IllegalArgumentException("DG10 files are not yet supported")
            PassportService.EF_DG11 -> return DG11File(inputStream)
            PassportService.EF_DG12 -> return DG12File(inputStream)
            PassportService.EF_DG13 -> throw IllegalArgumentException("DG13 files are not yet supported")
            PassportService.EF_DG14 -> return DG14File(inputStream)
            PassportService.EF_DG15 -> return DG15File(inputStream)
            PassportService.EF_DG16 -> throw IllegalArgumentException("DG16 files are not yet supported")
            PassportService.EF_SOD -> return SODFile(inputStream)
            PassportService.EF_CVCA -> return CVCAFile(inputStream)
            else -> {
                val bufferedIn = BufferedInputStream(inputStream, 37)
                try {
                    bufferedIn.mark(37)
                    /* Just try, will read 36 bytes at most, and we can reset bufferedIn. */
                    return CVCAFile(fid, bufferedIn)
                } catch (e: Exception) {
                    LOGGER.log(Level.WARNING, "Unknown file " + Integer.toHexString(fid.toInt()), e)
                    bufferedIn.reset()
                    throw NumberFormatException("Unknown file " + Integer.toHexString(fid.toInt()))
                }
            }
        }
    }

    @Throws(IOException::class)
    fun getCOMFile(inputStream: InputStream?): COMFile {
        return COMFile(inputStream)
    }

    @Throws(IOException::class)
    fun getCVCAFile(inputStream: InputStream?): CVCAFile {
        return CVCAFile(inputStream)
    }

    @Throws(IOException::class)
    fun getDG1File(inputStream: InputStream?): DG1File {
        return DG1File(inputStream)
    }

    @Throws(IOException::class)
    fun getDG2File(inputStream: InputStream?): DG2File {
        return DG2File(inputStream)
    }

    @Throws(IOException::class)
    fun getDG3File(inputStream: InputStream?): DG3File {
        return DG3File(inputStream)
    }

    @Throws(IOException::class)
    fun getDG4File(inputStream: InputStream?): DG4File {
        return DG4File(inputStream)
    }

    @Throws(IOException::class)
    fun getDG5File(inputStream: InputStream?): DG5File {
        return DG5File(inputStream)
    }

    @Throws(IOException::class)
    fun getDG6File(inputStream: InputStream?): DG6File {
        return DG6File(inputStream)
    }

    @Throws(IOException::class)
    fun getDG7File(inputStream: InputStream?): DG7File {
        return DG7File(inputStream)
    }

    @Throws(IOException::class)
    fun getDG11File(inputStream: InputStream?): DG11File {
        return DG11File(inputStream)
    }

    @Throws(IOException::class)
    fun getDG12File(inputStream: InputStream?): DG12File {
        return DG12File(inputStream)
    }

    @Throws(IOException::class)
    fun getDG14File(inputStream: InputStream?): DG14File {
        return DG14File(inputStream)
    }

    @Throws(IOException::class)
    fun getDG15File(inputStream: InputStream?): DG15File {
        return DG15File(inputStream)
    }

    @Throws(IOException::class)
    fun getSODFile(inputStream: InputStream?): SODFile {
        return SODFile(inputStream)
    }

    /**
     * Finds a file identifier for an ICAO tag.
     * 
     * Corresponds to Table A1 in ICAO-TR-LDS_1.7_2004-05-18.
     * 
     * @param tag an ICAO tag (the first byte of the EF)
     * 
     * @return a file identifier.
     */
    fun lookupFIDByTag(tag: Int): Short {
        when (tag) {
            LDSFile.EF_COM_TAG -> return PassportService.EF_COM
            LDSFile.EF_DG1_TAG -> return PassportService.EF_DG1
            LDSFile.EF_DG2_TAG -> return PassportService.EF_DG2
            LDSFile.EF_DG3_TAG -> return PassportService.EF_DG3
            LDSFile.EF_DG4_TAG -> return PassportService.EF_DG4
            LDSFile.EF_DG5_TAG -> return PassportService.EF_DG5
            LDSFile.EF_DG6_TAG -> return PassportService.EF_DG6
            LDSFile.EF_DG7_TAG -> return PassportService.EF_DG7
            LDSFile.EF_DG8_TAG -> return PassportService.EF_DG8
            LDSFile.EF_DG9_TAG -> return PassportService.EF_DG9
            LDSFile.EF_DG10_TAG -> return PassportService.EF_DG10
            LDSFile.EF_DG11_TAG -> return PassportService.EF_DG11
            LDSFile.EF_DG12_TAG -> return PassportService.EF_DG12
            LDSFile.EF_DG13_TAG -> return PassportService.EF_DG13
            LDSFile.EF_DG14_TAG -> return PassportService.EF_DG14
            LDSFile.EF_DG15_TAG -> return PassportService.EF_DG15
            LDSFile.EF_DG16_TAG -> return PassportService.EF_DG16
            LDSFile.EF_SOD_TAG -> return PassportService.EF_SOD
            else -> throw NumberFormatException("Unknown tag " + Integer.toHexString(tag))
        }
    }

    /**
     * Finds a data group number for an ICAO tag.
     * 
     * @param tag an ICAO tag (the first byte of the EF)
     * 
     * @return a data group number (1-16)
     */
    @JvmStatic
    fun lookupDataGroupNumberByTag(tag: Int): Int {
        when (tag) {
            LDSFile.EF_DG1_TAG -> return 1
            LDSFile.EF_DG2_TAG -> return 2
            LDSFile.EF_DG3_TAG -> return 3
            LDSFile.EF_DG4_TAG -> return 4
            LDSFile.EF_DG5_TAG -> return 5
            LDSFile.EF_DG6_TAG -> return 6
            LDSFile.EF_DG7_TAG -> return 7
            LDSFile.EF_DG8_TAG -> return 8
            LDSFile.EF_DG9_TAG -> return 9
            LDSFile.EF_DG10_TAG -> return 10
            LDSFile.EF_DG11_TAG -> return 11
            LDSFile.EF_DG12_TAG -> return 12
            LDSFile.EF_DG13_TAG -> return 13
            LDSFile.EF_DG14_TAG -> return 14
            LDSFile.EF_DG15_TAG -> return 15
            LDSFile.EF_DG16_TAG -> return 16
            else -> throw NumberFormatException("Unknown tag " + Integer.toHexString(tag))
        }
    }

    /**
     * Finds an ICAO tag for a data group number.
     * 
     * 
     * @param number a data group number (1-16)
     * 
     * @return an ICAO tag (the first byte of the EF)
     */
    fun lookupTagByDataGroupNumber(number: Int): Int {
        when (number) {
            1 -> return LDSFile.EF_DG1_TAG
            2 -> return LDSFile.EF_DG2_TAG
            3 -> return LDSFile.EF_DG3_TAG
            4 -> return LDSFile.EF_DG4_TAG
            5 -> return LDSFile.EF_DG5_TAG
            6 -> return LDSFile.EF_DG6_TAG
            7 -> return LDSFile.EF_DG7_TAG
            8 -> return LDSFile.EF_DG8_TAG
            9 -> return LDSFile.EF_DG9_TAG
            10 -> return LDSFile.EF_DG10_TAG
            11 -> return LDSFile.EF_DG11_TAG
            12 -> return LDSFile.EF_DG12_TAG
            13 -> return LDSFile.EF_DG13_TAG
            14 -> return LDSFile.EF_DG14_TAG
            15 -> return LDSFile.EF_DG15_TAG
            16 -> return LDSFile.EF_DG16_TAG
            else -> throw NumberFormatException("Unknown number " + number)
        }
    }

    /**
     * Finds an ICAO tag for a data group number.
     * 
     * 
     * @param number a data group number (1-16)
     * 
     * @return a file identifier
     */
    fun lookupFIDByDataGroupNumber(number: Int): Short {
        when (number) {
            1 -> return PassportService.EF_DG1
            2 -> return PassportService.EF_DG2
            3 -> return PassportService.EF_DG3
            4 -> return PassportService.EF_DG4
            5 -> return PassportService.EF_DG5
            6 -> return PassportService.EF_DG6
            7 -> return PassportService.EF_DG7
            8 -> return PassportService.EF_DG8
            9 -> return PassportService.EF_DG9
            10 -> return PassportService.EF_DG10
            11 -> return PassportService.EF_DG11
            12 -> return PassportService.EF_DG12
            13 -> return PassportService.EF_DG13
            14 -> return PassportService.EF_DG14
            15 -> return PassportService.EF_DG15
            16 -> return PassportService.EF_DG16
            else -> throw NumberFormatException("Unknown number " + number)
        }
    }

    /**
     * Finds an ICAO tag for a file identifier.
     * 
     * Corresponds to Table A1 in ICAO-TR-LDS_1.7_2004-05-18.
     * 
     * @param fid a file identifier
     * 
     * @return a an ICAO tag (first byte of EF)
     */
    fun lookupTagByFID(fid: Short): Short {
        when (fid) {
            PassportService.EF_COM -> return LDSFile.EF_COM_TAG.toShort()
            PassportService.EF_DG1 -> return LDSFile.EF_DG1_TAG.toShort()
            PassportService.EF_DG2 -> return LDSFile.EF_DG2_TAG.toShort()
            PassportService.EF_DG3 -> return LDSFile.EF_DG3_TAG.toShort()
            PassportService.EF_DG4 -> return LDSFile.EF_DG4_TAG.toShort()
            PassportService.EF_DG5 -> return LDSFile.EF_DG5_TAG.toShort()
            PassportService.EF_DG6 -> return LDSFile.EF_DG6_TAG.toShort()
            PassportService.EF_DG7 -> return LDSFile.EF_DG7_TAG.toShort()
            PassportService.EF_DG8 -> return LDSFile.EF_DG8_TAG.toShort()
            PassportService.EF_DG9 -> return LDSFile.EF_DG9_TAG.toShort()
            PassportService.EF_DG10 -> return LDSFile.EF_DG10_TAG.toShort()
            PassportService.EF_DG11 -> return LDSFile.EF_DG11_TAG.toShort()
            PassportService.EF_DG12 -> return LDSFile.EF_DG12_TAG.toShort()
            PassportService.EF_DG13 -> return LDSFile.EF_DG13_TAG.toShort()
            PassportService.EF_DG14 -> return LDSFile.EF_DG14_TAG.toShort()
            PassportService.EF_DG15 -> return LDSFile.EF_DG15_TAG.toShort()
            PassportService.EF_DG16 -> return LDSFile.EF_DG16_TAG.toShort()
            PassportService.EF_SOD -> return LDSFile.EF_SOD_TAG.toShort()
            else -> throw NumberFormatException("Unknown fid " + Integer.toHexString(fid.toInt()))
        }
    }

    /**
     * Finds a data group number by file identifier.
     * 
     * @param fid a file id
     * 
     * @return a data group number
     */
    fun lookupDataGroupNumberByFID(fid: Short): Int {
        when (fid) {
            PassportService.EF_DG1 -> return 1
            PassportService.EF_DG2 -> return 2
            PassportService.EF_DG3 -> return 3
            PassportService.EF_DG4 -> return 4
            PassportService.EF_DG5 -> return 5
            PassportService.EF_DG6 -> return 6
            PassportService.EF_DG7 -> return 7
            PassportService.EF_DG8 -> return 8
            PassportService.EF_DG9 -> return 9
            PassportService.EF_DG10 -> return 10
            PassportService.EF_DG11 -> return 11
            PassportService.EF_DG12 -> return 12
            PassportService.EF_DG13 -> return 13
            PassportService.EF_DG14 -> return 14
            PassportService.EF_DG15 -> return 15
            PassportService.EF_DG16 -> return 16
            else -> throw NumberFormatException("Unknown fid " + Integer.toHexString(fid.toInt()))
        }
    }

    /**
     * Returns a mnemonic name corresponding to the file represented by the
     * given ICAO tag, such as "EF_COM", "EF_SOD", or "EF_DG1".
     * 
     * @param tag an ICAO tag (the first byte of the EF)
     * 
     * @return a mnemonic name corresponding to the file represented by the given ICAO tag
     */
    fun lookupFileNameByTag(tag: Int): String {
        when (tag) {
            LDSFile.EF_COM_TAG -> return "EF_COM"
            LDSFile.EF_DG1_TAG -> return "EF_DG1"
            LDSFile.EF_DG2_TAG -> return "EF_DG2"
            LDSFile.EF_DG3_TAG -> return "EF_DG3"
            LDSFile.EF_DG4_TAG -> return "EF_DG4"
            LDSFile.EF_DG5_TAG -> return "EF_DG5"
            LDSFile.EF_DG6_TAG -> return "EF_DG6"
            LDSFile.EF_DG7_TAG -> return "EF_DG7"
            LDSFile.EF_DG8_TAG -> return "EF_DG8"
            LDSFile.EF_DG9_TAG -> return "EF_DG9"
            LDSFile.EF_DG10_TAG -> return "EF_DG10"
            LDSFile.EF_DG11_TAG -> return "EF_DG11"
            LDSFile.EF_DG12_TAG -> return "EF_DG12"
            LDSFile.EF_DG13_TAG -> return "EF_DG13"
            LDSFile.EF_DG14_TAG -> return "EF_DG14"
            LDSFile.EF_DG15_TAG -> return "EF_DG15"
            LDSFile.EF_DG16_TAG -> return "EF_DG16"
            LDSFile.EF_SOD_TAG -> return "EF_SOD"
            else -> return "File with tag 0x" + Integer.toHexString(tag)
        }
    }

    /**
     * Returns a mnemonic name corresponding to the file represented by the
     * given file identifier, such as "EF_COM", "EF_SOD", or "EF_DG1".
     * 
     * @param fid an LDS file identifiers
     * 
     * @return a mnemonic name corresponding to the file represented by the given ICAO tag
     */
    fun lookupFileNameByFID(fid: Int): String {
        when (fid) {
            PassportService.EF_COM -> return "EF_COM"
            PassportService.EF_DG1 -> return "EF_DG1"
            PassportService.EF_DG2 -> return "EF_DG2"
            PassportService.EF_DG3 -> return "EF_DG3"
            PassportService.EF_DG4 -> return "EF_DG4"
            PassportService.EF_DG5 -> return "EF_DG5"
            PassportService.EF_DG6 -> return "EF_DG6"
            PassportService.EF_DG7 -> return "EF_DG7"
            PassportService.EF_DG8 -> return "EF_DG8"
            PassportService.EF_DG9 -> return "EF_DG9"
            PassportService.EF_DG10 -> return "EF_DG10"
            PassportService.EF_DG11 -> return "EF_DG11"
            PassportService.EF_DG12 -> return "EF_DG12"
            PassportService.EF_DG13 -> return "EF_DG13"
            PassportService.EF_DG14 -> return "EF_DG14"
            PassportService.EF_DG15 -> return "EF_DG15"
            PassportService.EF_DG16 -> return "EF_DG16"
            PassportService.EF_SOD -> return "EF_SOD"
            else -> return "File with FID 0x" + Integer.toHexString(fid)
        }
    }

    /**
     * Returns the short (one  byte) file identifier corresponding
     * to the given (two byte) file identifier.
     * 
     * @param fid a file identifier
     * 
     * @return the corresponding short file identifier
     */
    fun lookupSFIByFID(fid: Short): Int {
        val sfiByte: Byte = FID_TO_SFI.get(fid)!!
        if (sfiByte == null) {
            throw NumberFormatException("Unknown FID " + Integer.toHexString(fid.toInt()))
        }

        return sfiByte.toInt() and 0xFF
    }

    /**
     * Looks up a file identifier for a given short file identifier.
     * 
     * @param sfi the short file identifier
     * 
     * @return a file identifier
     */
    fun lookupFIDBySFI(sfi: Byte): Short {
        when (sfi) {
            PassportService.SFI_COM -> return PassportService.EF_COM
            PassportService.SFI_DG1 -> return PassportService.EF_DG1
            PassportService.SFI_DG2 -> return PassportService.EF_DG2
            PassportService.SFI_DG3 -> return PassportService.EF_DG3
            PassportService.SFI_DG4 -> return PassportService.EF_DG4
            PassportService.SFI_DG5 -> return PassportService.EF_DG5
            PassportService.SFI_DG6 -> return PassportService.EF_DG6
            PassportService.SFI_DG7 -> return PassportService.EF_DG7
            PassportService.SFI_DG8 -> return PassportService.EF_DG8
            PassportService.SFI_DG9 -> return PassportService.EF_DG9
            PassportService.SFI_DG10 -> return PassportService.EF_DG10
            PassportService.SFI_DG11 -> return PassportService.EF_DG11
            PassportService.SFI_DG12 -> return PassportService.EF_DG12
            PassportService.SFI_DG13 -> return PassportService.EF_DG13
            PassportService.SFI_DG14 -> return PassportService.EF_DG14
            PassportService.SFI_DG15 -> return PassportService.EF_DG15
            PassportService.SFI_DG16 -> return PassportService.EF_DG16
            PassportService.SFI_SOD -> return PassportService.EF_SOD
            PassportService.SFI_CVCA -> return PassportService.EF_CVCA
            else -> throw NumberFormatException("Unknown SFI " + Integer.toHexString(sfi.toInt()))
        }
    }

    /**
     * Returns the data group list from the security object (SOd).
     * 
     * @param sodFile the security object
     * 
     * @return the list of data group numbers
     */
    fun getDataGroupNumbers(sodFile: SODFile?): MutableList<Int?> {
        /* Get the list of DGs from EF.SOd, we don't trust EF.COM. */
        val dgNumbers: MutableList<Int?> = ArrayList<Int?>()
        if (sodFile == null) {
            return dgNumbers
        }

        dgNumbers.addAll(sodFile.dataGroupHashes.keys)
        Collections.sort<Int?>(dgNumbers) /* NOTE: need to sort it, since we get keys as a set. */
        return dgNumbers
    }

    /**
     * Returns the data group list from the document index file (COM).
     * 
     * @param comFile the document index file
     * 
     * @return the list with data group number according to the document index file
     */
    fun getDataGroupNumbers(comFile: COMFile?): MutableList<Int?> {
        val dgNumbers: MutableList<Int?> = ArrayList<Int?>()
        if (comFile == null) {
            return dgNumbers
        }

        val tagList = comFile.getTagList()
        dgNumbers.addAll(toDataGroupList(tagList))
        Collections.sort<Int?>(dgNumbers) // NOTE: sort it, just in case.
        return dgNumbers
    }

    /**
     * Converts a list with ICAO tags into a list of ICAO data group numbers.
     * 
     * @param tagList a list of tags specified in ICAO Doc 9303
     * 
     * @return the list with data group number according to the security object
     */
    private fun toDataGroupList(tagList: IntArray?): MutableList<Int?> {
        if (tagList == null) {
            return mutableListOf<Int?>()
        }
        val dgNumberList: MutableList<Int?> = ArrayList<Int?>(tagList.size)
        for (tag in tagList) {
            try {
                val dgNumber = lookupDataGroupNumberByTag(tag)
                dgNumberList.add(dgNumber)
            } catch (nfe: NumberFormatException) {
                LOGGER.log(Level.WARNING, "Could not find DG number for tag: " + Integer.toHexString(tag), nfe)
            }
        }
        return dgNumberList
    }

    /**
     * Creates a map for looking up short file identifiers based on file identifiers.
     * 
     * @return the lookup map
     */
    private fun createFIDToSFIMap(): MutableMap<Short?, Byte> {
        val fidToSFI: MutableMap<Short?, Byte?> = HashMap<Short?, Byte?>(20)
        fidToSFI[PassportService.EF_COM] = PassportService.SFI_COM
        fidToSFI[PassportService.EF_DG1] = PassportService.SFI_DG1
        fidToSFI[PassportService.EF_DG2] = PassportService.SFI_DG2
        fidToSFI[PassportService.EF_DG3] = PassportService.SFI_DG3
        fidToSFI[PassportService.EF_DG4] = PassportService.SFI_DG4
        fidToSFI[PassportService.EF_DG5] = PassportService.SFI_DG5
        fidToSFI[PassportService.EF_DG6] = PassportService.SFI_DG6
        fidToSFI[PassportService.EF_DG7] = PassportService.SFI_DG7
        fidToSFI[PassportService.EF_DG8] = PassportService.SFI_DG8
        fidToSFI[PassportService.EF_DG9] = PassportService.SFI_DG9
        fidToSFI[PassportService.EF_DG10] = PassportService.SFI_DG10
        fidToSFI[PassportService.EF_DG11] = PassportService.SFI_DG11
        fidToSFI[PassportService.EF_DG12] = PassportService.SFI_DG12
        fidToSFI[PassportService.EF_DG13] = PassportService.SFI_DG13
        fidToSFI[PassportService.EF_DG14] = PassportService.SFI_DG14
        fidToSFI[PassportService.EF_DG15] = PassportService.SFI_DG15
        fidToSFI[PassportService.EF_DG16] = PassportService.SFI_DG16
        fidToSFI[PassportService.EF_SOD] = PassportService.SFI_SOD
        fidToSFI[PassportService.EF_CVCA] = PassportService.SFI_CVCA
        return Collections.unmodifiableMap<Short?, Byte?>(fidToSFI)
    }
}
