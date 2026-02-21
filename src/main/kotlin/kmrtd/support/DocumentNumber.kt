package kmrtd.support

/**
 * Represents an ICAO-compliant travel document number.
 *
 * This value class ensures that the document number is never blank
 * and provides utilities for MRZ-compatible formatting. According to
 * ICAO Doc 9303, document numbers in the MRZ are padded to a minimum
 * of 9 characters using the `<` filler character.
 *
 * Example usage:
 * ```
 * val docNum = DocumentNumber("AB1234567")
 * val short = DocumentNumber("AB123")
 * println(short.padded) // "AB123<<<< "
 * ```
 *
 * @property value the raw document number string
 * @throws IllegalArgumentException if [value] is blank
 *
 * @see ICAODate
 * @see BACKey
 */
@JvmInline
value class DocumentNumber(val value: String) {
    init {
        require(value.isNotBlank()) { "Document number cannot be blank" }
    }

    /**
     * Returns the document number padded to at least 9 characters
     * with the ICAO filler character `<`, as required for MRZ
     * key seed computation.
     */
    val padded: String
        get() = value.padEnd(9, '<').trim()
}