package kmrtd.cert
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
import net.sf.scuba.data.Country
/**
 * A [Country] placeholder for alpha-2 codes not recognized by scuba.
 *
 * Used when parsing CVC holder/authority references whose country
 * portion does not map to a known [Country] instance.
 *
 * @param alpha2Code the two-letter country code that was not recognized
 */
class UnknownCountry(
    val alpha2Code: String
) : Country() {
    override fun valueOf(): Int  = -1

    override fun getName(): String = "Unknown"

    override fun getNationality(): String = "Unknown"

    override fun toAlpha2Code(): String = alpha2Code

    override fun toAlpha3Code(): String = "XXX"

    companion object {
        private const val serialVersionUID = 345841304964161797L
    }
}