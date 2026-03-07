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
 * $Id: CVCPrincipal.java 1808 2019-03-07 21:32:19Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.cert

import kmrtd.cert.support.UnknownCountry
import net.sf.scuba.data.Country
import java.io.Serializable
import java.security.Principal
import java.util.Locale

/**
 * Card verifiable certificate principal.
 * This just wraps the EJBCA implementation.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * @version $Revision: 1808 $
 */
data class CVCPrincipal
/**
 * Constructs a principal.
 *
 * @param country   the country
 * @param mnemonic  the mnemonic, length < 9 or throw an exception
 * @param seqNumber the sequence number, length 5 or throw an exception
 * @throws IllegalArgumentException if the mnemonic is too long
 * @throws IllegalArgumentException if the sequence number is not 5
 */
    (
    /**
     * Returns the mnemonic.
     *
     * @return the mnemonic
     */
    val mnemonic: String,
    /**
     * Returns the sequence number.
     *
     * @return the seqNumber
     */
    val seqNumber: String,
    val country: Country
) : Principal, Serializable {
    init {
        require(mnemonic.length <= 9) { "Wrong length mnemonic" }
        require(seqNumber.length == 5) { "Wrong length seqNumber" }
    }

    /**
     * Consists of the concatenation of
     * country code (length 2), mnemonic (length &lt; 9) and
     * sequence number (length 5).
     * 
     * @return the name of the principal
     */
    override fun getName(): String =
        country.toAlpha2Code() + mnemonic + seqNumber

    /**
     * Returns a textual representation of this principal.
     * 
     * @return a textual representation of this principal
     */
    override fun toString(): String =
        country.toAlpha2Code() + "/" + mnemonic + "/" + seqNumber

    companion object {
        /**
         * Factory method
         *
         * @param name a name with format Country (2F) | Mnemonic (9V) | SeqNum (5F).
         * @return a [CVCPrincipal] principal with the given name
         * @throws IllegalArgumentException if the name is not in the correct format
         */
        @JvmStatic
        fun from(name: String): CVCPrincipal {
            require(!(name.length < 2 + 5 || name.length > 2 + 9 + 5)) { "Name should be <Country (2F)><Mnemonic (9V)><SeqNum (5F)> formatted, found \"$name\"" }

            val alpha2Code = name.substring(0, 2).uppercase(Locale.getDefault())
            return CVCPrincipal(
                mnemonic = name.substring(2, name.length - 5),
                seqNumber = name.substring(name.length - 5, name.length),
                country = runCatching {
                    Country.getInstance(alpha2Code)
                }.getOrElse { UnknownCountry(alpha2Code) }
            )
        }
    }
}
