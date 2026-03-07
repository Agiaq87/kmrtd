package kmrtd.support

import java.text.SimpleDateFormat
import java.util.Date

/**
 * Represents an ICAO-compliant date in `yyMMdd` format, as used in
 * Machine Readable Zones (MRZ) of travel documents.
 *
 * This value class provides compile-time type safety to prevent
 * accidental misuse of raw date strings. The wrapped value is
 * guaranteed to be exactly 6 characters long.
 *
 * Example usage:
 * ```
 * val dob = ICAODate("900115")
 * val expiry = ICAODate.from(Date())
 * val fromTimestamp = ICAODate.from(System.currentTimeMillis())
 * ```
 *
 * @property date the date string in `yyMMdd` format
 * @throws IllegalArgumentException if [date] is not exactly 6 characters
 *
 * @see DocumentNumber
 * @see BACKey
 */

@JvmInline
value class ICAODate(val date: String) {
    init {
        require(date.length == 6) { "date length should be 6 in format yyMMdd" }
    }

    companion object {
        private const val PATTERN = "yyMMdd"

        /**
         * Creates an [ICAODate] from a [Date] object.
         *
         * @param date the date to convert
         * @return an [ICAODate] formatted as `yyMMdd`
         */
        fun from(date: Date): ICAODate =
            ICAODate(SimpleDateFormat(PATTERN).format(date))

        /**
         * Creates an [ICAODate] from a Unix timestamp in milliseconds.
         *
         * @param millis milliseconds since epoch (January 1, 1970 00:00:00 UTC)
         * @return an [ICAODate] formatted as `yyMMdd`
         */
        fun from(millis: Long): ICAODate =
            ICAODate(SimpleDateFormat(PATTERN).format(Date(millis)))
    }
}