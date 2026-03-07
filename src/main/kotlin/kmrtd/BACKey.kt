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

import kmrtd.support.DocumentNumber
import kmrtd.support.ICAODate
import java.security.GeneralSecurityException
import java.util.Date

/**
 * A BAC key.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * @version $Revision: 1808 $
 */
data class BACKey(
    override val documentNumber: DocumentNumber,
    override val dateOfBirth: ICAODate,
    override val dateOfExpiry: ICAODate
) : BACKeySpec {

    override val algorithm: String = "BAC"

    override val key: ByteArray
        get() = try {
            Util.computeKeySeed(
                documentNumber.value,
                dateOfBirth.date,
                dateOfExpiry.date,
                "SHA-1",
                true
            )
        } catch (gse: GeneralSecurityException) {
            throw IllegalArgumentException("Unexpected exception", gse)
        }

    override fun getDocumentNumber(): String =
        documentNumber.value

    override fun getDateOfBirth(): String =
        dateOfBirth.date

    override fun getDateOfExpiry(): String =
        dateOfExpiry.date


    companion object {
        /**
         * Factory methods
         */
        fun from(documentNumber: String, dateOfBirth: Date, dateOfExpiry: Date): BACKey =
            BACKey(
                DocumentNumber(documentNumber),
                ICAODate.from(dateOfBirth),
                ICAODate.from(dateOfExpiry)
            )

        fun from(
            documentNumber: String,
            dateOfBirthInMillis: Long,
            dateOfExpiryInMillis: Long
        ): BACKey =
            BACKey(
                DocumentNumber(documentNumber),
                ICAODate.from(dateOfBirthInMillis),
                ICAODate.from(dateOfExpiryInMillis)
            )
    }
}
