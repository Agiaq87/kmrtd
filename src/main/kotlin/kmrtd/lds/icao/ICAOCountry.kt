/*
 * JMRTD - A Java API for accessing machine readable travel documents.
 *
 * Copyright (C) 2006 - 2023  The JMRTD team
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
 * $Id: ICAOCountry.java 1872 2023-03-10 21:52:01Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.lds.icao

import net.sf.scuba.data.Country
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Special ICAO countries not covered in [net.sf.scuba.data.ISOCountry].
 * Contributed by Aleksandar Kamburov (wise_guybg).
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1872 $
 */
class ICAOCountry : Country {
    private var name: String? = null
    private var nationality: String? = null
    private var alpha2Code: String? = null
    private var alpha3Code: String? = null

    /**
     * Prevent caller from creating instance.
     */
    private constructor()

    /**
     * Constructs a country.
     * 
     * @param alpha2Code the 2-letter alpha code
     * @param alpha3Code the 3-letter alpha code
     * @param name a name for the country
     * @param nationality a name for nationals of the country
     */
    /**
     * Constructs a country.
     * 
     * @param alpha2Code the two-digit alpha code
     * @param alpha3Code the three-digit alpha code
     * @param name a name for the country
     * (which will also be used to indicate the nationality of the country)
     */
    private constructor(alpha2Code: String?, alpha3Code: String, name: String?, nationality: String? = name) {
        this.alpha2Code = alpha2Code
        this.alpha3Code = alpha3Code
        this.name = name
        this.nationality = nationality
    }

    override fun valueOf(): Int {
        return -1
    }

    /**
     * Returns the full name of the country.
     * 
     * @return a country name
     */
    override fun getName(): String? {
        return name
    }

    /**
     * Returns the adjectival form corresponding to the country.
     * 
     * @return the nationality
     */
    override fun getNationality(): String? {
        return nationality
    }

    /**
     * Returns the two-digit country code.
     * 
     * @return a two-digit country code
     */
    override fun toAlpha2Code(): String? {
        return alpha2Code
    }

    /**
     * Returns the three-digit country code.
     * 
     * @return a three-digit country code
     */
    override fun toAlpha3Code(): String {
        return alpha3Code!!
    }

    companion object {
        private const val serialVersionUID = 2942942609311086138L

        private val LOGGER: Logger = Logger.getLogger("kmrtd")

        val DE: ICAOCountry = ICAOCountry("DE", "D<<", "Germany", "German")
        val RKS: ICAOCountry = ICAOCountry("KS", "RKS", "Republic of Kosovo", "Kosovar")

        val GBD: ICAOCountry = ICAOCountry("GB", "GBD", "British Dependent territories citizen")
        val GBN: ICAOCountry = ICAOCountry("GB", "GBN", "British National (Overseas)")
        val GBO: ICAOCountry = ICAOCountry("GB", "GBO", "British Overseas citizen")
        val GBP: ICAOCountry = ICAOCountry("GB", "GBP", "British Protected person")
        val GBS: ICAOCountry = ICAOCountry("GB", "GBS", "British Subject")

        val XXA: ICAOCountry = ICAOCountry("XX", "XXA", "Stateless person", "Stateless")
        val XXB: ICAOCountry = ICAOCountry("XX", "XXB", "Refugee", "Refugee")
        val XXC: ICAOCountry = ICAOCountry("XX", "XXC", "Refugee (other)", "Refugee (other)")
        val XXX: ICAOCountry = ICAOCountry("XX", "XXX", "Unspecified", "Unspecified")

        /** Part B: Europe.  */
        val EUE: ICAOCountry = ICAOCountry("EU", "EUE", "Europe", "European")

        /** Part C: Codes for Use in United Nations Travel Documents.  */
        val UNO: ICAOCountry = ICAOCountry("UN", "UNO", "United Nations Organization")
        val UNA: ICAOCountry = ICAOCountry("UN", "UNA", "United Nations Agency")
        val UNK: ICAOCountry = ICAOCountry("UN", "UNK", "United Nations Interim Administration Mission in Kosovo")

        /** Part D: Other issuing authorities.  */
        val XBA: ICAOCountry = ICAOCountry("XX", "XBA", "African Development Bank (ADB)")
        val XIM: ICAOCountry = ICAOCountry("XX", "XIM", "African Export-Import Bank (AFREXIM bank)")
        val XCC: ICAOCountry = ICAOCountry("XC", "XCC", "Carribean Community or one of its emissaries (CARICOM)")
        val XCE: ICAOCountry = ICAOCountry("XX", "XCE", "Council of Europe")
        val XCO: ICAOCountry = ICAOCountry("XX", "XCO", "Common Market for Eastern an Southern Africa (COMESA)")
        val XEC: ICAOCountry = ICAOCountry("XX", "XEC", "Economic Community of West African States (ECOWAS)")
        val XPO: ICAOCountry = ICAOCountry("XP", "XPO", "International Criminal Police Organization (INTERPOL)")
        val XES: ICAOCountry = ICAOCountry("XX", "XES", "Organization of Eastern Caribbean States (OECS)")
        val XMP: ICAOCountry = ICAOCountry("XX", "XMP", "Parliamentary Assembly of the Mediterranean (PAM)")
        val XOM: ICAOCountry = ICAOCountry("XO", "XOM", "Sovereign Military Order of Malta or one of its emissaries")
        val XDC: ICAOCountry = ICAOCountry("XX", "XDC", "Southern African Development Community")

        private val VALUES = arrayOf<ICAOCountry>(
            DE, RKS,
            GBD, GBN, GBO, GBP, GBS,
            XXA, XXB, XXC, XXX,
            EUE,
            UNO, UNA, UNK,
            XBA, XIM, XCC, XCO, XEC, XPO, XOM
        )

        /**
         * Returns an ICAO country instance.
         * 
         * @param alpha3Code a three-digit ICAO country code
         * 
         * @return an ICAO country
         */
        fun getInstance(alpha3Code: String): Country? {
            for (country in VALUES) {
                if (country.alpha3Code == alpha3Code) {
                    return country
                }
            }
            try {
                return Country.getInstance(alpha3Code)
            } catch (e: Exception) {
                /* NOTE: ignore this exception if it's not a legal 3 digit code. */
                LOGGER.log(Level.FINE, "Unknown country", e)
            }
            throw IllegalArgumentException("Illegal ICAO country alpha 3 code $alpha3Code")
        }
    }
}
