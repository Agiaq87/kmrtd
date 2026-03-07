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
 * $Id: IrisBiometricSubtypeInfo.java 1799 2018-10-30 16:25:48Z martijno $
 */
package kmrtd.lds.iso19794

import kmrtd.lds.AbstractListInfo
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * Iris biometric subtype data block (containing iris image data blocks)
 * based on Section 6.5.3 and Table 3 of
 * ISO/IEC 19794-6 2005.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * @version $Revision: 1799 $
 */
class IrisBiometricSubtypeInfo : AbstractListInfo<IrisImageInfo?> {
    /**
     * Returns the image format used in the images encoded in this record.
     * 
     * @return the image format
     */
    val imageFormat: Int

    /**
     * The biometric subtype (feature identifier).
     * Result is one of [.EYE_UNDEF], [.EYE_RIGHT], [.EYE_LEFT].
     * 
     * @return the biometric subtype.
     */
    var biometricSubtype: Int = 0
        private set

    /**
     * Constructs a biometric subtype info.
     * 
     * @param biometricSubtype one of [.EYE_UNDEF], [.EYE_RIGHT], [.EYE_LEFT]
     * @param imageFormat      the image format as specified in the [IrisInfo] of which this is a part
     * @param irisImageInfos   the iris image info records
     */
    constructor(
        biometricSubtype: Int,
        imageFormat: Int,
        irisImageInfos: MutableList<IrisImageInfo?>?
    ) {
        this.biometricSubtype = biometricSubtype
        this.imageFormat = imageFormat
        addAll(irisImageInfos)
    }

    /**
     * Constructs an iris biometric subtype from binary encoding.
     * 
     * @param in          an input stream
     * @param imageFormat the image format used
     * @throws IOException if reading fails
     */
    constructor(`in`: InputStream, imageFormat: Int) {
        this.imageFormat = imageFormat
        readObject(`in`)
    }

    /**
     * Reads an iris biometric subtype from input stream.
     * 
     * @param inputStream an input stream
     * @throws IOException if reading fails
     */
    @Throws(IOException::class)
    override fun readObject(inputStream: InputStream) {
        val dataIn =
            if (inputStream is DataInputStream) inputStream else DataInputStream(inputStream)

        /* Iris biometric subtype header */
        this.biometricSubtype = dataIn.readUnsignedByte() /* 1 */
        val count = dataIn.readUnsignedShort() /* + 2 = 3 */

        var constructedDataLength = 0L

        for (i in 0..<count) {
            val imageInfo = IrisImageInfo(inputStream, imageFormat)
            constructedDataLength += imageInfo.getRecordLength()
            add(imageInfo)
        }
        //		if (dataLength != constructedDataLength) {
        //			throw new IllegalStateException("dataLength = " + dataLength + ", constructedDataLength = " + constructedDataLength);
        //		}
    }

    /**
     * Writes an iris biometric subtype to output stream.
     * 
     * @param outputStream an output stream
     * @throws IOException if writing fails
     */
    @Throws(IOException::class)
    override fun writeObject(outputStream: OutputStream?) {
        val dataOut =
            if (outputStream is DataOutputStream) outputStream else DataOutputStream(outputStream)

        dataOut.writeByte(biometricSubtype and 0xFF) /* 1 */

        val irisImageInfos = getSubRecords()
        dataOut.writeShort(irisImageInfos.size and 0xFFFF) /* + 2 = 3 */
        for (irisImageInfo in irisImageInfos) {
            irisImageInfo.writeObject(dataOut)
        }
    }

    val recordLength: Long
        /**
         * Returns the record length.
         * 
         * @return the record length
         */
        get() {
            var result: Long = 3
            val irisImageInfos =
                getSubRecords()
            for (irisImageInfo in irisImageInfos) {
                result += irisImageInfo.getRecordLength()
            }
            return result
        }

    override fun hashCode(): Int {
        val prime = 31
        var result = super.hashCode()
        result = prime * result + biometricSubtype
        result = prime * result + imageFormat
        return result
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (!super.equals(obj)) {
            return false
        }
        if (javaClass != obj!!.javaClass) {
            return false
        }

        val other = obj as IrisBiometricSubtypeInfo
        return biometricSubtype == other.biometricSubtype
                && imageFormat == other.imageFormat
    }

    override fun toString(): String {
        val irisImageInfos = getSubRecords()
        return ("IrisBiometricSubtypeInfo ["
                + "biometric subtype: " + biometricSubtypeToString(biometricSubtype)
                + ", imageCount = " + irisImageInfos.size
                + "]")
    }

    val irisImageInfos: MutableList<IrisImageInfo?>
        /**
         * Returns the iris image infos embedded in this iris biometric subtype info.
         * 
         * @return the embedded iris image infos
         */
        get() = getSubRecords()

    /**
     * Adds an iris image info to this iris biometric subtype info.
     * 
     * @param irisImageInfo the iris image info to add
     */
    fun addIrisImageInfo(irisImageInfo: IrisImageInfo?) {
        add(irisImageInfo)
    }

    /* ONLY PRIVATE METHODS BELOW */
    /**
     * Removes an iris image info from this iris biometric subtype info.
     * 
     * @param index the index of the iris image info to remove
     */
    fun removeIrisImageInfo(index: Int) {
        remove(index)
    }

    companion object {
        /**
         * Biometric subtype value.
         */
        const val EYE_UNDEF: Int = 0

        /**
         * Biometric subtype value.
         */
        const val EYE_RIGHT: Int = 1

        /**
         * Biometric subtype value.
         */
        const val EYE_LEFT: Int = 2
        private val serialVersionUID = -6588640634764878039L

        /**
         * Returns a textual representation of the given biometric sub-type code.
         * 
         * @param biometricSubtype the biometric sub-type code
         * @return a human readable string such as `"Left eye"`, `"Right eye"`, or `"Undefined"`
         */
        private fun biometricSubtypeToString(biometricSubtype: Int): String {
            when (biometricSubtype) {
                EYE_LEFT -> return "Left eye"
                EYE_RIGHT -> return "Right eye"
                EYE_UNDEF -> return "Undefined"
                else -> throw NumberFormatException(
                    "Unknown biometric subtype: " + Integer.toHexString(
                        biometricSubtype
                    )
                )
            }
        }
    }
}
