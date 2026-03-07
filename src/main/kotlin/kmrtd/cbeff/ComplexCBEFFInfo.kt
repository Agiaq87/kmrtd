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
 * $Id: ComplexCBEFFInfo.java 1885 2024-11-07 09:17:29Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.cbeff

/**
 * Complex (nested) CBEFF BIR.
 * Specified in ISO 19785-1 (version 2.0) and NISTIR 6529-A (version 1.1).
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * @version $Revision: 1885 $
 * @since 0.4.7
 */
class ComplexCBEFFInfo<R : BiometricDataBlock> : CBEFFInfo<R> {
    private val _subRecords: MutableList<CBEFFInfo<R>> = ArrayList<CBEFFInfo<R>>()

    /**
     * Returns the records inside this complex CBEFF info.
     * 
     * @return a list of CBEFF infos
     */
    fun getSubRecords(): List<CBEFFInfo<R>> =
        _subRecords.toList()

    /**
     * Adds a record to this complex CBEFF info.
     * 
     * @param subRecord the CBEFF info to add
     */
    fun add(subRecord: CBEFFInfo<R>) =
        _subRecords.add(subRecord)

    /**
     * Adds all records in a list to this complex CBEFF info.
     * 
     * @param subRecords a list of CBEFF infos
     */
    fun addAll(subRecords: List<CBEFFInfo<R>>) =
        _subRecords.addAll(subRecords)

    /**
     * Removes a record in this complex CBEFF info.
     * 
     * @param index the index of the CBEFF info to remove
     */
    fun remove(index: Int) =
        _subRecords.removeAt(index)

    /**
     * Tests whether the parameter equals this complex CBEFF info.
     * 
     * @param other some other object
     * @return whether the other object is equal to this complex CBEFF info
     */
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other === this) return true
        if (other !is ComplexCBEFFInfo<*>) return false

        return _subRecords == other._subRecords
    }

    /**
     * Computes a hash code.
     * 
     * @return the hash code for this complex CBEFF info
     */
    override fun hashCode(): Int =
        7 * _subRecords.hashCode() + 11
}
