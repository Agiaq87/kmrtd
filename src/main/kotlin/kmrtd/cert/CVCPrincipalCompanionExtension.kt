package org.giaquinto.kmrtd.cert
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
import kmrtd.cert.CVCPrincipal
import net.sf.scuba.data.Country
import java.util.Locale
import java.util.logging.Level

/**
 * Parses a CVC holder/authority reference string into a [CVCPrincipal].
 *
 * The expected format is `<Country (2F)><Mnemonic (9V)><SeqNum (5F)>`,
 * for a total length between 7 and 16 characters.
 *
 * If the two-letter country code is not recognized by scuba's
 * [Country.getInstance], an [UnknownCountry] fallback is used.
 *
 * @param name the holder/authority reference string
 * @return a [CVCPrincipal] parsed from the given name
 * @throws IllegalArgumentException if the name format is invalid
 */
fun CVCPrincipal.Companion.from(name: String): CVCPrincipal {
    require(name.length in 7..16) {
        "Name should be <Country (2F)><Mnemonic (9V)><SeqNum (5F)> formatted, found \"$name\""
    }

    val alpha2Code = name.substring(0, 2).uppercase(Locale.getDefault())

    val country = try {
        Country.getInstance(alpha2Code)
    } catch (iae: IllegalArgumentException) {
        LOGGER.log(Level.FINE, "Could not find country for $alpha2Code", iae)
        UnknownCountry(alpha2Code)
    }

    val mnemonic = name.substring(2, name.length - 5)
    val seqNumber = name.substring(name.length - 5)

    return CVCPrincipal(country, mnemonic, seqNumber)
}