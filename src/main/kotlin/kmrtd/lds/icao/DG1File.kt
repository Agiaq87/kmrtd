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
 * $Id: DG1File.java 1808 2019-03-07 21:32:19Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.lds.icao

import kmrtd.lds.DataGroup
import kmrtd.lds.LDSFile
import net.sf.scuba.tlv.TLVInputStream
import net.sf.scuba.tlv.TLVOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * File structure for the EF_DG1 file.
 * Datagroup 1 contains the Machine
 * Readable Zone information.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1808 $
 */
class DG1File : DataGroup {
    private var mrzInfo: MRZInfo? = null

    /**
     * Creates a new file based on MRZ information.
     * 
     * @param mrzInfo the MRZ information to store in this file
     */
    constructor(mrzInfo: MRZInfo) : super(LDSFile.Companion.EF_DG1_TAG) {
        this.mrzInfo = mrzInfo
    }

    /**
     * Creates a new file based on an input stream.
     * 
     * @param inputStream an input stream
     * 
     * @throws IOException if something goes wrong
     */
    constructor(inputStream: InputStream?) : super(LDSFile.Companion.EF_DG1_TAG, inputStream)

    @Throws(IOException::class)
    override fun readContent(inputStream: InputStream?) {
        val tlvIn = if (inputStream is TLVInputStream) inputStream else TLVInputStream(inputStream)
        tlvIn.skipToTag(MRZ_INFO_TAG.toInt())
        val length = tlvIn.readLength()
        this.mrzInfo = MRZInfo(tlvIn, length)
    }

    val mRZInfo: MRZInfo
        /**
         * Returns the MRZ information stored in this file.
         * 
         * @return the MRZ information
         */
        get() = mrzInfo!!

    /**
     * Returns a textual representation of this file.
     * 
     * @return a textual representation of this file
     */
    public override fun toString(): String {
        return "DG1File " + mrzInfo.toString().replace("\n".toRegex(), "").trim { it <= ' ' }
    }

    override fun equals(obj: Any?): Boolean {
        if (obj == null) {
            return false
        }
        if (!(obj.javaClass == this.javaClass)) {
            return false
        }

        val other = obj as DG1File
        return mrzInfo == other.mrzInfo
    }

    override fun hashCode(): Int {
        return 3 * mrzInfo.hashCode() + 57
    }

    @Throws(IOException::class)
    override fun writeContent(out: OutputStream?) {
        val tlvOut = if (out is TLVOutputStream) out else TLVOutputStream(out)
        tlvOut.writeTag(MRZ_INFO_TAG.toInt())
        val value = mrzInfo!!.encoded
        tlvOut.writeValue(value)
    }

    companion object {
        private const val serialVersionUID = 5091606125728809058L

        private const val MRZ_INFO_TAG: Short = 0x5F1F
    }
}
