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
 * $Id: BACKey.java 1808 2019-03-07 21:32:19Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd

import org.jmrtd.BACKeySpec
import java.security.GeneralSecurityException
import java.text.SimpleDateFormat
import java.util.*

/**
 * A BAC key.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1808 $
 */
class BACKey : BACKeySpec {
    private var documentNumber: String? = null

    private var dateOfBirth: String? = null

    private var dateOfExpiry: String? = null

    /**
     * Creates an empty BAC key entry.
     */
    protected constructor()

    /**
     * Creates a BAC key.
     * 
     * @param documentNumber the document number string, withou check digit, cannot be `null`, and may be shorter than 9
     * @param dateOfBirth the date of birth, in *yymmdd* format, cannot be `null`
     * @param dateOfExpiry the date of expiry, in *yymmdd* format, cannot be `null`
     */
    constructor(documentNumber: String, dateOfBirth: Date?, dateOfExpiry: Date?) : this(
        documentNumber,
        toString(dateOfBirth),
        toString(dateOfExpiry)
    )

    /**
     * Creates a BAC key.
     * 
     * @param documentNumber the document number string, cannot be `null`
     * @param dateOfBirth the date of birth string in *yymmdd* format, cannot be `null`
     * @param dateOfExpiry the date of expiry string in *yymmdd* format, cannot be `null`
     */
    constructor(documentNumber: String, dateOfBirth: String, dateOfExpiry: String) {
        requireNotNull(documentNumber) { "Illegal document number" }
        require(!(dateOfBirth == null || dateOfBirth.length != 6)) { "Illegal date: " + dateOfBirth }
        require(!(dateOfExpiry == null || dateOfExpiry.length != 6)) { "Illegal date: " + dateOfExpiry }

        val documentNumberBuilder = StringBuilder(documentNumber)
        while (documentNumberBuilder.length < 9) {
            documentNumberBuilder.append('<')
        }
        this.documentNumber = documentNumberBuilder.toString().trim { it <= ' ' }
        this.dateOfBirth = dateOfBirth
        this.dateOfExpiry = dateOfExpiry
    }

    /**
     * Returns the document number string.
     * 
     * @return the document number string
     */
    override fun getDocumentNumber(): String? {
        return documentNumber
    }

    /**
     * Returns the date of birth string.
     * 
     * @return a date in *yymmdd* format
     */
    override fun getDateOfBirth(): String? {
        return dateOfBirth
    }

    /**
     * Returns the date of expiry string.
     * 
     * @return a date in *yymmdd* format
     */
    override fun getDateOfExpiry(): String? {
        return dateOfExpiry
    }

    /**
     * Returns a textual representation of this BAC key.
     * 
     * @return a textual representation of this BAC key
     */
    override fun toString(): String {
        return documentNumber + ", " + dateOfBirth + ", " + dateOfExpiry
    }

    /**
     * Computes the hash code of this BAC key.
     * Document number, date of birth, and date of expiry (with year in *yy* precision) are taken into account.
     * 
     * @return a hash code
     */
    override fun hashCode(): Int {
        var result = 5
        result = 61 * result + (if (documentNumber == null) 0 else documentNumber.hashCode())
        result = 61 * result + (if (dateOfBirth == null) 0 else dateOfBirth.hashCode())
        result = 61 * result + (if (dateOfExpiry == null) 0 else dateOfExpiry.hashCode())
        return result
    }

    /**
     * Tests equality of this BAC key with respect to another object.
     * 
     * @param o another object
     * 
     * @return whether this BAC key equals another object
     */
    override fun equals(o: Any?): Boolean {
        if (o == null) {
            return false
        }
        if (o.javaClass != this.javaClass) {
            return false
        }
        if (o === this) {
            return true
        }
        val previous = o as BACKey
        return documentNumber == previous.documentNumber &&
                dateOfBirth == previous.dateOfBirth &&
                dateOfExpiry == previous.dateOfExpiry
    }

    /**
     * The algorithm of this key specification.
     * 
     * @return constant &quot;BAC&quot;
     */
    override fun getAlgorithm(): String {
        return "BAC"
    }

    /**
     * Returns the encoded key (key seed) for use in key derivation.
     * 
     * @return the encoded key
     */
    override fun getKey(): ByteArray? {
        try {
            return Util.computeKeySeed(documentNumber, dateOfBirth, dateOfExpiry, "SHA-1", true)
        } catch (gse: GeneralSecurityException) {
            throw IllegalArgumentException("Unexpected exception", gse)
        }
    }

    /**
     * Sets the document number.
     * 
     * @param documentNumber the document number to set
     */
    protected fun setDocumentNumber(documentNumber: String?) {
        this.documentNumber = documentNumber
    }

    /**
     * Sets the date of birth.
     * 
     * @param dateOfBirth the date of birth to set
     */
    protected fun setDateOfBirth(dateOfBirth: String?) {
        this.dateOfBirth = dateOfBirth
    }

    /**
     * Sets the date of expiry.
     * 
     * @param dateOfExpiry the date of expiry to set
     */
    protected fun setDateOfExpiry(dateOfExpiry: String?) {
        this.dateOfExpiry = dateOfExpiry
    }

    companion object {
        private val serialVersionUID = -1059774581180524710L

        private const val SDF = "yyMMdd"

        /**
         * Renders a date as a string.
         * 
         * @param date the date
         * 
         * @return a string representing the given date
         */
        @Synchronized
        private fun toString(date: Date?): String {
            return SimpleDateFormat(SDF).format(date)
        }
    }
}
