/*
 * JMRTD - A Java API for accessing machine readable travel documents.
 *
 * Copyright (C) 2006 - 2026  The JMRTD team
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
 * $Id: AdditionalDetailDataGroup.java 1907 2026-02-06 09:24:02Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.lds.icao

import kmrtd.lds.DataGroup
import kmrtd.lds.LDSFile.Companion.EF_DG11_TAG
import net.sf.scuba.tlv.TLVInputStream
import net.sf.scuba.tlv.TLVOutputStream
import net.sf.scuba.tlv.TLVUtil
import net.sf.scuba.util.Hex
import java.io.*
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Abstract superclass for DG11 and DG12.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1907 $
 */
internal abstract class AdditionalDetailDataGroup : DataGroup {
    constructor(tag: Int) : super(EF_DG11_TAG)

    /**
     * Constructs a file from binary representation.
     * 
     * @param tag the datagroup tag
     * @param inputStream an input stream
     * 
     * @throws IOException if reading fails
     */
    constructor(tag: Int, inputStream: InputStream?) : super(tag, inputStream)

    /**
     * Returns the list of tags of fields actually present.
     * 
     * @return list of tags
     */
    abstract val tagPresenceList: MutableList<Int?>

    @Throws(IOException::class)
    protected abstract fun readField(expectedTag: Int, tlvInputStream: TLVInputStream?)

    @Throws(IOException::class)
    protected abstract fun writeField(tag: Int, tlvOut: TLVOutputStream?)

    @Throws(IOException::class)
    override fun readContent(inputStream: InputStream?) {
        val tlvInputStream = inputStream as? TLVInputStream ?: TLVInputStream(inputStream)
        val tagList: MutableList<Int?> = readTagList(tlvInputStream)
        /* Now read the fields in order. */
        for (t in tagList) {
            readField(t!!, tlvInputStream)
        }
    }


    @Throws(IOException::class)
    override fun writeContent(out: OutputStream?) {
        val tlvOut = out as? TLVOutputStream ?: TLVOutputStream(out)
        val tagList = this.tagPresenceList
        writeTagList(tagList, tlvOut)
        for (tag in tagList) {
            writeField(tag!!, tlvOut)
        }
    }

    companion object {
        private const val serialVersionUID = 8566312538928662931L

        const val TAG_LIST_TAG: Int = 0x5C

        const val CONTENT_SPECIFIC_CONSTRUCTED_TAG: Int = 0xA0 // 5F0F is always used inside A0 constructed object
        const val COUNT_TAG: Int = 0x02 // Used in A0 constructed object to indicate single byte count of simple objects

        private val LOGGER: Logger = Logger.getLogger("kmrtd")

        @Throws(IOException::class)
        protected fun readTagList(tlvInputStream: TLVInputStream): MutableList<Int?> {
            val tagListTag = tlvInputStream.readTag()
            require(tagListTag == TAG_LIST_TAG) { "Expected tag list in DG11" }

            val tagListLength = tlvInputStream.readLength()
            var tagListBytesRead = 0

            val tagListBytes = tlvInputStream.readValue()
            val tagListBytesInputStream = ByteArrayInputStream(tagListBytes)
            try {
                /* Find out which tags are present. */
                val tagList: MutableList<Int?> = ArrayList<Int?>()
                while (tagListBytesRead < tagListLength) {
                    /* We're using another TLV inputstream every time to read each tag. */
                    val anotherTLVInputStream = TLVInputStream(tagListBytesInputStream)
                    val tag = anotherTLVInputStream.readTag()
                    tagListBytesRead += TLVUtil.getTagLength(tag)
                    tagList.add(tag)
                }
                return tagList
            } finally {
                tagListBytesInputStream.close()
            }
        }

        @Throws(IOException::class)
        protected fun writeTagList(tags: MutableList<Int?>, tlvOut: TLVOutputStream) {
            tlvOut.writeTag(TAG_LIST_TAG)
            val byteArrayOutputStream = ByteArrayOutputStream()
            for (tag in tags) {
                val anotherTLVOutputStream = TLVOutputStream(byteArrayOutputStream)
                anotherTLVOutputStream.writeTag(tag!!)
            }
            tlvOut.writeValue(byteArrayOutputStream.toByteArray())
            tlvOut.writeValueEnd() /* TAG_LIST_TAG */
        }

        @JvmStatic
        @Throws(IOException::class)
        protected fun readBytes(tlvInputStream: TLVInputStream): ByteArray {
            return tlvInputStream.readValue()
        }

        @JvmStatic
        @Throws(IOException::class)
        protected fun writeBytes(tag: Int, value: ByteArray?, tlvOut: TLVOutputStream) {
            tlvOut.writeTag(tag)

            if (value == null) {
                tlvOut.writeValue(byteArrayOf())
            } else {
                tlvOut.writeValue(value)
            }
        }

        @JvmStatic
        @Throws(IOException::class)
        protected fun readString(tlvIn: TLVInputStream): String {
            val value = tlvIn.readValue()
            try {
                val field = String(value, charset("UTF-8"))
                return field.trim { it <= ' ' }
            } catch (uee: UnsupportedEncodingException) {
                LOGGER.log(Level.WARNING, "Exception", uee)
                return String(value).trim { it <= ' ' }
            }
        }

        @JvmStatic
        @Throws(IOException::class)
        protected fun writeString(tag: Int, value: String?, tlvOut: TLVOutputStream) {
            writeBytes(tag, if (value == null) null else value.trim { it <= ' ' }.toByteArray(charset("UTF-8")), tlvOut)
        }

        @JvmStatic
        @Throws(IOException::class)
        protected fun readFullDate(tlvInputStream: TLVInputStream): String {
            val value = tlvInputStream.readValue()
            var field: String? = null
            if (value.size == 4) {
                /* Either France or Belgium uses this encoding for dates. */
                field = Hex.bytesToHexString(value)
            } else {
                /* Assume length 8 yyyMMdd as per spec, or whatever was put in. */
                field = String(value)
                try {
                    field = String(value, charset("UTF-8"))
                } catch (usee: UnsupportedEncodingException) {
                    LOGGER.log(Level.WARNING, "Exception", usee)
                }
            }
            return field
        }

        @JvmStatic
        @Throws(IOException::class)
        protected fun readContentSpecificFieldsList(tlvInputStream: TLVInputStream): MutableList<String?> {
            val countTag = tlvInputStream.readTag()
            require(countTag == COUNT_TAG) {
                "Expected " + Integer.toHexString(COUNT_TAG) + ", found " + Integer.toHexString(
                    countTag
                )
            }
            val countLength = tlvInputStream.readLength()
            require(countLength == 1) { "Expected length 1 count length, found $countLength" }
            val countValue = tlvInputStream.readValue()
            require(!(countValue == null || countValue.size != 1)) { "Number of content specific fields should be encoded in single byte, found " + countValue.contentToString() }
            val count = countValue[0].toInt() and 0xFF
            val list: MutableList<String?> = ArrayList<String?>(count)
            for (i in 0..<count) {
                val tag = tlvInputStream.readTag()
                /* int length = */
                tlvInputStream.readLength()
                list.add(readString(tlvInputStream))
            }
            return list
        }

        @Throws(IOException::class)
        protected fun writeContentSpecificFieldsList(tag: Int, list: MutableList<String>, tlvOut: TLVOutputStream) {
            tlvOut.writeTag(CONTENT_SPECIFIC_CONSTRUCTED_TAG)
            tlvOut.writeTag(COUNT_TAG)
            tlvOut.write(list.size)
            tlvOut.writeValueEnd() /* COUNT_TAG */
            for (otherName in list) {
                tlvOut.writeTag(tag)
                tlvOut.writeValue(otherName.trim { it <= ' ' }.toByteArray(charset("UTF-8")))
            }
            tlvOut.writeValueEnd() /* CONTENT_SPECIFIC_CONSTRUCTED_TAG */
        }

        @JvmStatic
        @Throws(IOException::class)
        protected fun readList(tlvInputStream: TLVInputStream): MutableList<String?> {
            val field: String = readString(tlvInputStream)
            val list: MutableList<String?> = ArrayList<String?>()
            val tokens = field.split("<".toRegex()).toTypedArray()
            for (token in tokens) {
                list.add(token.trim { it <= ' ' })
            }
            return list
        }

        @Throws(IOException::class)
        protected fun writeList(tag: Int, list: MutableList<String>, tlvOut: TLVOutputStream) {
            tlvOut.writeTag(tag)
            var isFirstOne = true
            if (list.isEmpty()) {
                tlvOut.writeValue(byteArrayOf())
            } else {
                val encodedString = StringBuilder()
                for (detail in list) {
                    if (isFirstOne) {
                        isFirstOne = false
                    } else {
                        encodedString.append('<')
                    }
                    encodedString.append(detail.trim { it <= ' ' })
                }
                tlvOut.writeValue(encodedString.toString().toByteArray(charset("UTF-8")))
            }
        }
    }
}
