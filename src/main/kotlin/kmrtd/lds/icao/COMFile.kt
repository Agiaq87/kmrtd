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
 * $Id: COMFile.java 1808 2019-03-07 21:32:19Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.lds.icao

import kmrtd.lds.AbstractTaggedLDSFile
import kmrtd.lds.LDSFile
import kmrtd.lds.LDSFileUtil.lookupDataGroupNumberByTag
import net.sf.scuba.tlv.TLVInputStream
import net.sf.scuba.tlv.TLVOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

/**
 * File structure for the EF_COM file.
 * This file contains the common data (version and
 * data group presence table) information.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1808 $
 */
class COMFile : AbstractTaggedLDSFile {
    private var versionLDS: String? = null
    private var updateLevelLDS: String? = null
    private var majorVersionUnicode: String? = null
    private var minorVersionUnicode: String? = null
    private var releaseLevelUnicode: String? = null
    private var tagList: MutableList<Int>? = null

    /**
     * Constructs a new COM file.
     * 
     * @param versionLDS a numerical string of length 2
     * @param updateLevelLDS a numerical string of length 2
     * @param majorVersionUnicode a numerical string of length 2
     * @param minorVersionUnicode a numerical string of length 2
     * @param releaseLevelUnicode a numerical string of length 2
     * @param tagList a list of ICAO data group tags
     * 
     * @throws IllegalArgumentException if the input is not well-formed
     */
    constructor(
        versionLDS: String, updateLevelLDS: String,
        majorVersionUnicode: String, minorVersionUnicode: String,
        releaseLevelUnicode: String, tagList: IntArray
    ) : super(LDSFile.EF_COM_TAG) {
        initialize(versionLDS, updateLevelLDS, majorVersionUnicode, minorVersionUnicode, releaseLevelUnicode, tagList)
    }

    /**
     * Constructs a new COM file.
     * 
     * @param ldsVer a "x.y" version number
     * @param unicodeVer a "x.y.z" version number
     * @param tagList list of tags
     */
    constructor(ldsVer: String, unicodeVer: String, tagList: IntArray) : super(LDSFile.EF_COM_TAG) {
        try {
            requireNotNull(ldsVer) { "Null versionLDS" }
            requireNotNull(unicodeVer) { "Null versionUnicode" }
            var st = StringTokenizer(ldsVer, ".")
            require(st.countTokens() == 2) { "Could not parse LDS version. Expecting 2 level version number x.y." }
            val versionLDS = st.nextToken().trim { it <= ' ' }.toInt()
            val updateLevelLDS = st.nextToken().trim { it <= ' ' }.toInt()
            st = StringTokenizer(unicodeVer, ".")
            require(st.countTokens() == 3) { "Could not parse unicode version. Expecting 3 level version number x.y.z." }
            val majorVersionUnicode = st.nextToken().trim { it <= ' ' }.toInt()
            val minorVersionUnicode = st.nextToken().trim { it <= ' ' }.toInt()
            val releaseLevelUnicode = st.nextToken().trim { it <= ' ' }.toInt()
            initialize(
                String.format("%02d", versionLDS),
                String.format("%02d", updateLevelLDS),
                String.format("%02d", majorVersionUnicode),
                String.format("%02d", minorVersionUnicode),
                String.format("%02d", releaseLevelUnicode),
                tagList
            )
        } catch (nfe: NumberFormatException) {
            throw IllegalArgumentException("Could not parse version number", nfe)
        } catch (ifce: IllegalFormatConversionException) {
            throw IllegalArgumentException("Could not parse version number", ifce)
        }
    }

    /**
     * Constructs a new EF_COM file based on the encoded
     * value in `in`.
     * 
     * @param in should contain a TLV object with appropriate
     * tag and contents
     * 
     * @throws IOException if the input could not be decoded
     */
    constructor(`in`: InputStream?) : super(LDSFile.Companion.EF_COM_TAG, `in`)

    @Throws(IOException::class)
    override fun readContent(`in`: InputStream?) {
        val tlvIn = if (`in` is TLVInputStream) `in` else TLVInputStream(`in`)
        val versionLDSTag = tlvIn.readTag()
        require(versionLDSTag == VERSION_LDS_TAG) {
            "Excepected VERSION_LDS_TAG (" + Integer.toHexString(VERSION_LDS_TAG) + "), found " + Integer.toHexString(
                versionLDSTag
            )
        }
        val versionLDSLength = tlvIn.readLength()
        require(versionLDSLength == 4) { "Wrong length of LDS version object" }
        val versionLDSBytes = tlvIn.readValue()
        versionLDS = String(versionLDSBytes, 0, 2)
        updateLevelLDS = String(versionLDSBytes, 2, 2)

        val versionUnicodeTag = tlvIn.readTag()
        require(versionUnicodeTag == VERSION_UNICODE_TAG) {
            "Expected VERSION_UNICODE_TAG (" + Integer.toHexString(
                VERSION_UNICODE_TAG
            ) + "), found " + Integer.toHexString(versionUnicodeTag)
        }
        val versionUnicodeLength = tlvIn.readLength()
        require(versionUnicodeLength == 6) { "Wrong length of LDS version object" }
        val versionUnicodeBytes = tlvIn.readValue()
        majorVersionUnicode = String(versionUnicodeBytes, 0, 2)
        minorVersionUnicode = String(versionUnicodeBytes, 2, 2)
        releaseLevelUnicode = String(versionUnicodeBytes, 4, 2)

        val tagListTag = tlvIn.readTag()
        require(tagListTag == TAG_LIST_TAG) {
            "Expected TAG_LIST_TAG (" + Integer.toHexString(TAG_LIST_TAG) + "), found " + Integer.toHexString(
                tagListTag
            )
        }
        /* int tagListLength = */
        tlvIn.readLength()
        val tagBytes = tlvIn.readValue()
        tagList = ArrayList<Int>()
        for (tagByte in tagBytes) {
            val dgTag = (tagByte.toInt() and 0xFF)
            tagList!!.add(dgTag)
        }
    }

    val lDSVersion: String
        /**
         * Returns the LDS version as a dot seperated string
         * containing version and update level.
         * 
         * @return a string of the form "a.b"
         */
        get() {
            var ldsVersion = versionLDS + "." + updateLevelLDS
            try {
                val major = versionLDS!!.toInt()
                val minor = updateLevelLDS!!.toInt()
                ldsVersion = "$major.$minor"
            } catch (nfe: NumberFormatException) {
                /* NOTE: leave ldsVersion as is. */
            }
            return ldsVersion
        }

    val unicodeVersion: String
        /**
         * Returns the unicode version as a dot seperated string
         * containing major version, minor version, and release level.
         * 
         * @return a string of the form "aa.bb.cc"
         */
        get() {
            var unicodeVersion = (majorVersionUnicode
                    + "." + minorVersionUnicode
                    + "." + releaseLevelUnicode)

            try {
                val major = majorVersionUnicode!!.toInt()
                val minor = minorVersionUnicode!!.toInt()
                val releaseLevel = releaseLevelUnicode!!.toInt()
                unicodeVersion = "$major.$minor.$releaseLevel"
            } catch (nfe: NumberFormatException) {
                /* NOTE: leave unicodeVersion as is. */
            }

            return unicodeVersion
        }

    /**
     * Returns the ICAO datagroup tags as a list of bytes.
     * 
     * @return a list of bytes
     */
    fun getTagList(): IntArray {
        val result = IntArray(tagList!!.size)
        var i = 0
        for (tag in tagList!!) {
            result[i++] = tag
        }
        return result
    }

    /**
     * Inserts a tag in a proper place if not already present.
     * 
     * @param tag the tag to insert
     */
    fun insertTag(tag: Int?) {
        if (tagList!!.contains(tag!!)) {
            return
        }
        tagList!!.add(tag)
        Collections.sort<Int?>(tagList)
    }

    @Throws(IOException::class)
    override fun writeContent(out: OutputStream?) {
        val tlvOut = out as? TLVOutputStream ?: TLVOutputStream(out)
        tlvOut.writeTag(VERSION_LDS_TAG)
        tlvOut.writeValue((versionLDS + updateLevelLDS).toByteArray())
        tlvOut.writeTag(VERSION_UNICODE_TAG)
        tlvOut.writeValue((majorVersionUnicode + minorVersionUnicode + releaseLevelUnicode).toByteArray())
        tlvOut.writeTag(TAG_LIST_TAG)

        tlvOut.writeLength(tagList!!.size)
        for (tag in tagList!!) {
            tlvOut.write(tag.toByte().toInt())
        }
    }

    /**
     * Returns a textual representation of this file.
     * 
     * @return a textual representation of this file
     */
    public override fun toString(): String {
        val result = StringBuilder()
        result.append("COMFile ")
        result.append("LDS $versionLDS.$updateLevelLDS")
        result.append(", ")
        result.append("Unicode $majorVersionUnicode.$minorVersionUnicode.$releaseLevelUnicode")
        result.append(", ")
        var i = 0
        result.append("[")
        val dgCount = tagList!!.size
        for (tag in tagList!!) {
            result.append("DG" + lookupDataGroupNumberByTag(tag))
            if (i < dgCount - 1) {
                result.append(", ")
            }
            i++
        }
        result.append("]")
        return result.toString()
    }

    /**
     * Whether other is equal to this file.
     * 
     * @return a boolean
     */
    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        if (other === this) {
            return true
        }
        if (other.javaClass != javaClass) {
            return false
        }

        val otherCOMFile = other as COMFile
        return versionLDS == otherCOMFile.versionLDS &&
                updateLevelLDS == otherCOMFile.updateLevelLDS &&
                majorVersionUnicode == otherCOMFile.majorVersionUnicode &&
                minorVersionUnicode == otherCOMFile.minorVersionUnicode &&
                releaseLevelUnicode == otherCOMFile.releaseLevelUnicode &&
                tagList == otherCOMFile.tagList
    }

    override fun hashCode(): Int {
        return 3 * versionLDS.hashCode() + 5 * updateLevelLDS.hashCode() + 7 * majorVersionUnicode.hashCode() + 11 * minorVersionUnicode.hashCode() + 13 * releaseLevelUnicode.hashCode() + 17 * tagList.hashCode()
    }

    /**
     * Initializes this index file.
     * 
     * @param versionLDS the version of the LDS
     * @param updateLevelLDS the update level of the LDS
     * @param majorVersionUnicode the major version
     * @param minorVersionUnicode the minor version
     * @param releaseLevelUnicode the release level version
     * @param tagList the data group tag presence list
     */
    private fun initialize(
        versionLDS: String, updateLevelLDS: String,
        majorVersionUnicode: String, minorVersionUnicode: String,
        releaseLevelUnicode: String, tagList: IntArray
    ) {
        requireNotNull(tagList) { "Null tag list" }
        require(!(versionLDS == null || versionLDS.length != 2 || updateLevelLDS == null || updateLevelLDS.length != 2 || majorVersionUnicode == null || majorVersionUnicode.length != 2 || minorVersionUnicode == null || minorVersionUnicode.length != 2 || releaseLevelUnicode == null || releaseLevelUnicode.length != 2 || tagList == null))
        this.versionLDS = versionLDS
        this.updateLevelLDS = updateLevelLDS
        this.majorVersionUnicode = majorVersionUnicode
        this.minorVersionUnicode = minorVersionUnicode
        this.releaseLevelUnicode = releaseLevelUnicode
        this.tagList = ArrayList<Int>(tagList.size)
        for (tag in tagList) {
            this.tagList!!.add(tag)
        }
    }

    companion object {
        private const val serialVersionUID = 2002455279067170063L

        private const val TAG_LIST_TAG = 0x5C
        private const val VERSION_UNICODE_TAG = 0x5F36
        private const val VERSION_LDS_TAG = 0x5F01
    }
}
