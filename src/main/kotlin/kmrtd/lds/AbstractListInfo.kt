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
 * $Id: AbstractListInfo.java 1765 2018-02-19 21:49:52Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.lds

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.Serializable
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Abstract base class for several data structures used in the LDS
 * containing a list of elements.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1765 $
 * 
 * @param <R> the type of the elements
</R> */
abstract class AbstractListInfo<R : Serializable?> : AbstractLDSInfo() {
    private var subRecords: MutableList<R?>? = null

    /**
     * Returns the sub-records of this list.
     * 
     * @return the sub-records
     */
    protected fun getSubRecords(): MutableList<R?> {
        if (this.subRecords == null) {
            this.subRecords = ArrayList<R?>()
        }

        return ArrayList<R?>(this.subRecords)
    }

    /**
     * Adds a sub-record to this list.
     * 
     * @param subRecord the sub-record to add
     */
    protected fun add(subRecord: R?) {
        if (this.subRecords == null) {
            this.subRecords = ArrayList<R?>()
        }
        this.subRecords!!.add(subRecord)
    }

    /**
     * Adds all sub-records in a collection.
     * 
     * @param subRecords the sub-records to add
     */
    protected fun addAll(subRecords: MutableList<R?>) {
        if (this.subRecords == null) {
            this.subRecords = ArrayList<R?>()
        }
        this.subRecords!!.addAll(subRecords)
    }

    /**
     * Removes a sub-record at a given index.
     * 
     * @param index the index of the sub-record to remove
     */
    protected fun remove(index: Int) {
        if (this.subRecords == null) {
            this.subRecords = ArrayList<R?>()
        }
        this.subRecords!!.removeAt(index)
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        if (other === this) {
            return true
        }
        if (other !is AbstractListInfo<*>) {
            return false
        }

        try {
            val otherRecord = other as AbstractListInfo<R?>
            val subRecords = getSubRecords()
            val otherSubRecords = otherRecord.getSubRecords()
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
        return 7 * result + 11
    }

    /**
     * Writes this value to a stream.
     * 
     * @param outputStream the stream to write to
     */
    @Throws(IOException::class)
    abstract override fun writeObject(outputStream: OutputStream?)

    /**
     * Reads this value from a stream.
     * 
     * @param inputStream the stream to read from
     * 
     * @throws IOException on error reading from the stream
     */
    @Throws(IOException::class)
    abstract fun readObject(inputStream: InputStream?)

    companion object {
        private const val serialVersionUID = 2970076896364365191L

        private val LOGGER: Logger = Logger.getLogger("org.jmrtd")
    }
}
