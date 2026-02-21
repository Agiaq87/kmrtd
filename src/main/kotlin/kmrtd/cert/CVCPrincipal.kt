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

import net.sf.scuba.data.Country
import java.io.Serializable
import java.security.Principal
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Card verifiable certificate principal.
 * This just wraps the EJBCA implementation.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1808 $
 */
class CVCPrincipal : Principal, Serializable {
    private var country: Country? = null

    /**
     * Returns the mnemonic.
     * 
     * @return the mnemonic
     */
    val mnemonic: String

    /**
     * Returns the sequence number.
     * 
     * @return the seqNumber
     */
    val seqNumber: String

    /**
     * Constructs a principal.
     * 
     * @param name a name with format Country (2F) | Mnemonic (9V) | SeqNum (5F).
     */
    constructor(name: String) {
        requireNotNull(name) { "Name should be <Country (2F)><Mnemonic (9V)><SeqNum (5F)> formatted, found null" }
        require(!(name.length < 2 + 5 || name.length > 2 + 9 + 5)) { "Name should be <Country (2F)><Mnemonic (9V)><SeqNum (5F)> formatted, found \"" + name + "\"" }

        val alpha2Code = name.substring(0, 2).uppercase(Locale.getDefault())
        try {
            country = Country.getInstance(alpha2Code)
        } catch (iae: IllegalArgumentException) {
            LOGGER.log(Level.FINE, "Could not find country for " + alpha2Code, iae)
            country = object : Country() {
                private val serialVersionUID = 345841304964161797L

                override fun valueOf(): Int {
                    return -1
                }

                override fun getName(): String {
                    return "Unknown"
                }

                override fun getNationality(): String {
                    return "Unknown"
                }

                override fun toAlpha2Code(): String {
                    return alpha2Code
                }

                override fun toAlpha3Code(): String {
                    return "XXX"
                }
            }
        }
        mnemonic = name.substring(2, name.length - 5)
        seqNumber = name.substring(name.length - 5, name.length)
    }

    /**
     * Constructs a principal.
     * 
     * @param country the country
     * @param mnemonic the mnemonic
     * @param seqNumber the sequence number
     */
    constructor(country: Country, mnemonic: String, seqNumber: String) {
        require(!(mnemonic == null || mnemonic.length > 9)) { "Wrong length mnemonic" }
        require(!(seqNumber == null || seqNumber.length != 5)) { "Wrong length seqNumber" }
        this.country = country
        this.mnemonic = mnemonic
        this.seqNumber = seqNumber
    }

    /**
     * Consists of the concatenation of
     * country code (length 2), mnemonic (length &lt; 9) and
     * sequence number (length 5).
     * 
     * @return the name of the principal
     */
    override fun getName(): String {
        return country!!.toAlpha2Code() + mnemonic + seqNumber
    }

    /**
     * Returns a textual representation of this principal.
     * 
     * @return a textual representation of this principal
     */
    override fun toString(): String {
        return country!!.toAlpha2Code() + "/" + mnemonic + "/" + seqNumber
    }

    /**
     * Returns the country.
     * 
     * @return the country
     */
    fun getCountry(): Country {
        return country!!
    }

    /**
     * Tests for equality with respect to another object.
     * 
     * @param otherObj another object
     * 
     * @return whether this principal equals the other object
     */
    override fun equals(otherObj: Any?): Boolean {
        if (otherObj == null) {
            return false
        }
        if (otherObj === this) {
            return true
        }
        if (otherObj.javaClass != this.javaClass) {
            return false
        }

        val otherPrincipal = otherObj as CVCPrincipal
        return otherPrincipal.country == this.country
                && otherPrincipal.mnemonic == this.mnemonic
                && otherPrincipal.seqNumber == this.seqNumber
    }

    /**
     * Returns a hash code of this object.
     * 
     * @return the hash code
     */
    override fun hashCode(): Int {
        return 2 * getName().hashCode() + 1231211
    }

    companion object {
        private val serialVersionUID = -4905647207367309688L

        private val LOGGER: Logger = Logger.getLogger("org.jmrtd")
    }
}
