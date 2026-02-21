/*
 * JMRTD - A Java API for accessing machine readable travel documents.
 *
 * Copyright (C) 2006 - 2025  The JMRTD team
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
 * $Id: CBEFFDataGroup.java 1896 2025-04-18 21:39:56Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.lds

import kmrtd.cbeff.BiometricDataBlock
import kmrtd.cbeff.BiometricEncodingType
import kmrtd.cbeff.ComplexCBEFFInfo
import kmrtd.cbeff.ISO781611
import kmrtd.cbeff.ISO781611Decoder
import kmrtd.cbeff.ISO781611Encoder
import kmrtd.cbeff.SimpleCBEFFInfo
import net.sf.scuba.tlv.TLVOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.SecureRandom
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Datagroup containing a list of biometric information templates (BITs).
 * The `DG2File`, `DG3File`, and `DG4File` datagroups
 * are based on this type.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1896 $
 */
abstract class CBEFFDataGroup : DataGroup {
    /** For writing the optional random data block.  */
    private val random: Random

    /** Records in the BIT group. Each record represents a single BIT.  */
    private var subRecords: MutableList<BiometricDataBlock?>? = null

    @JvmField
    protected var shouldAddRandomDataIfEmpty: Boolean

    var encodingType: BiometricEncodingType? = null
        protected set

    /**
     * Creates a CBEFF data group.
     * 
     * @param dataGroupTag the data group tag
     * @param encodingType encoding type, either ISO19794 or ISO39794
     * @param subRecords the sub-records contained in this data group
     * @param shouldAddRandomDataIfEmpty whether to include random data if there are no records
     */
    protected constructor(
        dataGroupTag: Int, encodingType: BiometricEncodingType?,
        subRecords: MutableList<out BiometricDataBlock?>, shouldAddRandomDataIfEmpty: Boolean
    ) : super(dataGroupTag) {
        addAll(subRecords)
        this.encodingType = encodingType
        this.shouldAddRandomDataIfEmpty = shouldAddRandomDataIfEmpty
        this.random = SecureRandom()
    }

    /**
     * Constructs an instance.
     * 
     * @param dataGroupTag the datagroup tag to use
     * @param inputStream an input stream
     * @param shouldAddRandomDataIfEmpty whether to include random data if there are no records
     * 
     * @throws IOException on error
     */
    protected constructor(dataGroupTag: Int, inputStream: InputStream?, shouldAddRandomDataIfEmpty: Boolean) : super(
        dataGroupTag,
        inputStream
    ) {
        this.shouldAddRandomDataIfEmpty = shouldAddRandomDataIfEmpty
        this.random = Random()
    }

    abstract val decoder: ISO781611Decoder<BiometricDataBlock?>

    abstract val encoder: ISO781611Encoder<BiometricDataBlock?>

    @Throws(IOException::class)
    override fun readContent(inputStream: InputStream?) {
        val decoder = this.decoder
        this.encodingType = decoder.getEncodingType()
        val complexCBEFFInfo = decoder.decode(inputStream)
        val records = complexCBEFFInfo.getSubRecords()
        for (cbeffInfo in records) {
            if (cbeffInfo !is SimpleCBEFFInfo<*>) {
                throw IOException("Was expecting a SimpleCBEFFInfo, found " + cbeffInfo.javaClass.getSimpleName())
            }
            val simpleCBEFFInfo = cbeffInfo as SimpleCBEFFInfo<*>
            val bdb: BiometricDataBlock? = simpleCBEFFInfo.getBiometricDataBlock()
            add(bdb)
        }
        encodingType = decoder.getEncodingType()

        /* FIXME: by symmetry, shouldn't there be a readOptionalRandomData here? */
    }

    @Throws(IOException::class)
    override fun writeContent(outputStream: OutputStream?) {
        val encoder = this.encoder
        val cbeffInfo = ComplexCBEFFInfo<BiometricDataBlock?>()
        val records = getSubRecords()
        for (record in records) {
            val simpleCBEFFInfo = SimpleCBEFFInfo<BiometricDataBlock?>(record)
            cbeffInfo.add(simpleCBEFFInfo)
        }
        encoder.encode(cbeffInfo, outputStream)

        /* NOTE: Supplement to ICAO Doc 9303 R7-p1_v2_sIII_0057. */
        if (shouldAddRandomDataIfEmpty) {
            writeOptionalRandomData(outputStream)
        }
    }

    /**
     * Returns a textual representation of this data group.
     * 
     * @return a textual representation of this data group
     */
    override fun toString(): String {
        val result = StringBuilder()
        result.append("CBEFFDataGroup [")
        if (subRecords == null) {
            result.append("null")
        } else {
            var isFirst = true
            for (subRecord in subRecords) {
                if (!isFirst) {
                    result.append(", ")
                } else {
                    isFirst = false
                }
                result.append(if (subRecord == null) "null" else subRecord.toString())
            }
        }
        result.append(']')
        return result.toString()
    }

    /**
     * Returns the records in this data group.
     * 
     * @return the records in this data group
     */
    fun getSubRecords(): MutableList<BiometricDataBlock?> {
        if (subRecords == null) {
            subRecords = ArrayList<BiometricDataBlock?>()
        }
        return ArrayList<BiometricDataBlock?>(subRecords)
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        if (other === this) {
            return true
        }
        if (other !is CBEFFDataGroup) {
            return false
        }

        try {
            val otherDG = other
            val subRecords = getSubRecords()
            val otherSubRecords = otherDG.getSubRecords()
            val subRecordCount = subRecords.size
            if (subRecordCount != otherSubRecords.size) {
                return false
            }

            for (i in 0..<subRecordCount) {
                val subRecord = subRecords.get(i)
                val otherSubRecord = otherSubRecords.get(i)
                if (subRecord == null) {
                    if (otherSubRecord != null) {
                        return false
                    }
                } else if (subRecord != otherSubRecord) {
                    return false
                }
            }

            return true
        } catch (cce: ClassCastException) {
            LOGGER.log(Level.WARNING, "Wrong class", cce)
            return false
        }
    }

    override fun hashCode(): Int {
        var result = 1234567891
        val subRecords = getSubRecords()
        for (record in subRecords) {
            if (record == null) {
                result = 3 * result + 5
            } else {
                result = 5 * (result + record.hashCode()) + 7
            }
        }
        result = if (shouldAddRandomDataIfEmpty) 13 * result + 111 else 17 * result + 123
        return 7 * result + 11
    }

    /**
     * Concrete implementations of EAC protected CBEFF DataGroups should call this
     * method at the end of their [.writeContent] method to add
     * some random data if the record contains zero biometric templates.
     * See supplement to ICAO Doc 9303 R7-p1_v2_sIII_0057.
     * 
     * @param outputStream the outputstream
     * 
     * @throws IOException on I/O errors
     */
    @Throws(IOException::class)
    protected fun writeOptionalRandomData(outputStream: OutputStream?) {
        if (!subRecords!!.isEmpty()) {
            return
        }

        val tlvOut = if (outputStream is TLVOutputStream) outputStream else TLVOutputStream(outputStream)
        tlvOut.writeTag(ISO781611.DISCRETIONARY_DATA_FOR_PAYLOAD_TAG)
        val value = ByteArray(8)
        random.nextBytes(value)
        tlvOut.writeValue(value)
    }

    /**
     * Adds a record to this data group.
     * 
     * @param record the record to add
     */
    private fun add(record: BiometricDataBlock?) {
        if (subRecords == null) {
            subRecords = ArrayList<BiometricDataBlock?>()
        }
        subRecords!!.add(record)
    }

    /**
     * Adds all records in a list to this data group.
     * 
     * @param records the records to add
     */
    private fun addAll(records: MutableList<out BiometricDataBlock?>) {
        if (subRecords == null) {
            subRecords = ArrayList<BiometricDataBlock?>()
        }
        subRecords!!.addAll(records)
    }

    companion object {
        private const val serialVersionUID = 2702959939408371946L

        @JvmField
        protected val LOGGER: Logger = Logger.getLogger("org.jmrtd.lds")
    }
}
