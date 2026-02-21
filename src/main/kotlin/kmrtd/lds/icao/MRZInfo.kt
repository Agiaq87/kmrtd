/*
 * JMRTD - A Java API for accessing machine readable travel documents.
 *
 * Copyright (C) 2006 - 2025  The JMRTD team
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
 * $Id: MRZInfo.java 1898 2025-06-04 12:05:45Z martijno $
 */
package kmrtd.lds.icao

import kmrtd.lds.AbstractLDSInfo
import net.sf.scuba.data.Gender
import java.io.*
import java.util.*
import kotlin.math.max

/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
/**
 * Data structure for storing the MRZ information
 * as found in DG1. Based on ICAO Doc 9303 (Seventh edition)
 * part 4 (TD3),
 * part 5 (TD1),
 * part 6 (TD2),
 * and part 7 (MRV-A, MRV-B).
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1898 $
 */
class MRZInfo : AbstractLDSInfo {
    /**
     * The type of document (MRZ), determining the number of lines,
     * number of characters per line, and positions and lengths of fields,
     * and check-digits.
     */
    private enum class DocumentType
    /**
     * Constructs a document type.
     * 
     * @param code numeric code for compatibility with old constants
     */(
        /**
         * Returns the numeric code identifying this type.
         * 
         * @return the numeric code
         */
        val code: Int
    ) {
        /** Unspecified document type (do not use, choose ID1 or ID3).  */
        UNKNOWN(DOC_TYPE_UNSPECIFIED),

        /** MROTD 3 lines of 30 characters, as per part 5.  */
        TD1(DOC_TYPE_ID1),

        /** MROTD 2 lines of 36 characters, as per part 6.  */
        TD2(DOC_TYPE_ID2),

        /** MRP 2 lines of 44 characters, as per part 4.  */
        TD3(DOC_TYPE_ID3),

        /** MRV type A.  */
        MRVA(4),

        /** MRV type B.  */
        MRVB(5)
    }

    private var documentType: DocumentType? = null

    /**
     * Returns the document code.
     * 
     * @return document type
     */
    var documentCode: String? = null
        private set
    private var issuingState: String? = null
    private var primaryIdentifier: String? = null
    private var secondaryIdentifier: String? = null
    private var nationality: String? = null
    private var documentNumber: String? = null

    /**
     * Returns the date of birth of the passport holder.
     * 
     * @return date of birth
     */
    var dateOfBirth: String? = null
        private set
    private var gender: String? = null

    /**
     * Returns the date of expiry.
     * 
     * @return the date of expiry
     */
    var dateOfExpiry: String? = null
        private set
    private var documentNumberCheckDigit = 0.toChar()
    private var dateOfBirthCheckDigit = 0.toChar()
    private var dateOfExpiryCheckDigit = 0.toChar()
    private var personalNumberCheckDigit =
        0.toChar() /* NOTE: Over optionalData1, but only for TD3. When empty we prefer '<' over '0'. */
    private var compositeCheckDigit = 0.toChar()

    /**
     * Returns the contents of the first optional data field for ID-1 and ID-3 style MRZs.
     * 
     * @return optional data 1
     */
    var optionalData1: String? =
        null /* NOTE: For TD1 holds personal number for some issuing states (e.g. NL), but is used to hold (part of) document number for others. */
        private set

    /**
     * Returns the contents of the second optional data field for ID-1 style MRZs.
     * 
     * @return optional data 2
     */
    var optionalData2: String? = null
        private set

    /**
     * Creates a new 2-line MRZ compliant with ICAO Doc 9303 (pre-seventh edition) part 1 vol 1.
     * 
     * @param documentCode document code (1 or 2 digit, has to start with "P" or "V")
     * @param issuingState issuing state as 3 alpha string
     * @param primaryIdentifier card holder last name
     * @param secondaryIdentifier card holder first name(s)
     * @param documentNumber document number
     * @param nationality nationality as 3 alpha string
     * @param dateOfBirth date of birth
     * @param gender gender, must not be `null`
     * @param dateOfExpiry date of expiry
     * @param personalNumber either empty, or a personal number of maximum length 14, or other optional data of exact length 15
     * 
     */
    @Deprecated("Use the corresponding factory method {@link #createTD1MRZInfo(String, String, String, String, String, Object, String, String, String, String, String)}")
    constructor(
        documentCode: String, issuingState: String?,
        primaryIdentifier: String?, secondaryIdentifier: String?,
        documentNumber: String?, nationality: String?, dateOfBirth: String?,
        gender: Gender, dateOfExpiry: String?, personalNumber: String?
    ) : this(
        getDocumentTypeFromDocumentCode(documentCode),
        documentCode,
        issuingState,
        documentNumber,
        personalNumberToOptionalData(personalNumber),
        dateOfBirth,
        gender,
        dateOfExpiry,
        nationality,
        null,
        primaryIdentifier,
        secondaryIdentifier
    )

    /**
     * Creates a new 3-line MRZ compliant with ICAO Doc 9303 (pre-seventh edition) part 3 vol 1.
     * 
     * @param documentCode document code (1 or 2 digit, has to start with "I", "C", or "A")
     * @param issuingState issuing state as 3 alpha string
     * @param primaryIdentifier card holder last name
     * @param secondaryIdentifier card holder first name(s)
     * @param documentNumber document number
     * @param nationality nationality as 3 alpha string
     * @param dateOfBirth date of birth in YYMMDD format
     * @param gender gender, must not be `null`
     * @param dateOfExpiry date of expiry in YYMMDD format
     * @param optionalData1 optional data in line 1 of maximum length 15
     * @param optionalData2 optional data in line 2 of maximum length 11
     * 
     */
    @Deprecated("Use the corresponding factory method {@link #createTD3MRZInfo(String, String, String, String, String, String, String, Object, String, String)}")
    constructor(
        documentCode: String,
        issuingState: String?,
        documentNumber: String?,
        optionalData1: String?,
        dateOfBirth: String?,
        gender: Gender,
        dateOfExpiry: String?,
        nationality: String?,
        optionalData2: String?,
        primaryIdentifier: String?,
        secondaryIdentifier: String?
    ) : this(
        getDocumentTypeFromDocumentCode(documentCode),
        documentCode,
        issuingState,
        documentNumber,
        optionalData1,
        dateOfBirth,
        gender,
        dateOfExpiry,
        nationality,
        optionalData2,
        primaryIdentifier,
        secondaryIdentifier
    )

    /**
     * Creates a new MRZ based on an input stream.
     * 
     * @param inputStream contains the contents (value) of DG1 (without the tag and length)
     * @param length the length of the MRZInfo structure
     */
    constructor(inputStream: InputStream, length: Int) {
        try {
            readObject(inputStream, length)
        } catch (ioe: IOException) {
            throw IllegalArgumentException(ioe)
        }
    }

    /**
     * Creates a new MRZ based on the text input.
     * The text input may contain newlines, which will be ignored.
     * 
     * @param str input text
     */
    constructor(str: String) {
        var str = str
        requireNotNull(str) { "Null string" }
        str = str.trim { it <= ' ' }.replace("\n", "")
        try {
            readObject(ByteArrayInputStream(str.toByteArray(charset("UTF-8"))), str.length)
        } catch (uee: UnsupportedEncodingException) {
            /* NOTE: never happens, UTF-8 is supported. */
            throw IllegalStateException("Exception", uee)
        } catch (ioe: IOException) {
            throw IllegalArgumentException("Exception", ioe)
        }
    }

    /**
     * Constructs an MRZInfo object from components.
     * 
     * @param documentType the document-type
     * @param documentCode the document-code
     * @param issuingState the issuing state 3-alpha string
     * @param documentNumber the document number
     * @param optionalData1 optional data or personal number including check digit
     * @param dateOfBirth date of birth in yyMMdd format
     * @param genderObject the gender
     * @param dateOfExpiry the date of expiry in yyMMdd format
     * @param nationality the nationality 3 alpha string
     * @param optionalData2 optional optional data 2
     * @param primaryIdentifier the primary identifier
     * @param secondaryIdentifier the secondary identifiers
     */
    private constructor(
        documentType: DocumentType,
        documentCode: String?,
        issuingState: String?,
        documentNumber: String?,
        optionalData1: String?,
        dateOfBirth: String?,
        genderObject: Any,
        dateOfExpiry: String?,
        nationality: String?,
        optionalData2: String?,
        primaryIdentifier: String?,
        secondaryIdentifier: String?
    ) {
        this.documentType = documentType

        require(isDocumentCodeConsistentWithDocumentType(documentType, documentCode)) { "Wrong document code" }

        require(
            isOptionalDataConsistentWithDocumentType(
                documentType,
                optionalData1,
                optionalData2
            )
        ) { "Wrong optional data length" }

        requireNotNull(genderObject) { "Gender must not be null" }

        this.documentCode = trimTrailingFillerChars(documentCode)
        this.issuingState = issuingState
        this.primaryIdentifier = trimTrailingFillerChars(primaryIdentifier).replace("<", " ")
        this.secondaryIdentifier = trimTrailingFillerChars(secondaryIdentifier).replace("<", " ")
        this.documentNumber = trimTrailingFillerChars(documentNumber)
        this.nationality = nationality
        this.dateOfBirth = dateOfBirth
        this.gender = genderToString(genderObject)
        this.dateOfExpiry = dateOfExpiry
        this.optionalData1 = if (optionalData1 == null) "" else trimTrailingFillerChars(optionalData1)
        this.optionalData2 = if (optionalData2 == null) null else trimTrailingFillerChars(optionalData2)
        checkDigit()
    }

    /**
     * Returns the document number.
     * 
     * @return document number
     */
    fun getDocumentNumber(): String {
        return documentNumber!!
    }

    /**
     * Returns the document type.
     * 
     * @return document type
     * 
     */
    @Deprecated("Clients should determine type based on {@link #getDocumentCode()}")
    fun getDocumentType(): Int {
        return documentType!!.code
    }

    /**
     * Returns the issuing state as a 3 letter code.
     * 
     * @return the issuing state
     */
    fun getIssuingState(): String {
        return mrzFormat(issuingState, 3)
    }

    val nameOfHolder: String
        /**
         * Returns the name of the holder (primary and secondary identifiers).
         * Double filler separates primary from secondary identifiers.
         * Single fillers separate components within identifiers.
         * Trailing fillers are removed.
         * 
         * @return the name of holder
         */
        get() {
            when (documentType) {
                DocumentType.TD1 -> return trimTrailingFillerChars(
                    Companion.nameToString(primaryIdentifier!!, secondaryIdentifier, 30)
                )

                DocumentType.TD2, DocumentType.MRVB -> return trimTrailingFillerChars(
                    Companion.nameToString(primaryIdentifier!!, secondaryIdentifier, 31)
                )

                DocumentType.TD3, DocumentType.MRVA -> return trimTrailingFillerChars(
                    Companion.nameToString(primaryIdentifier!!, secondaryIdentifier, 39)
                )

                else -> throw IllegalStateException("Unsupported document type")
            }
        }

    /**
     * Returns the passport holder's primary identifier (last name).
     * Trailing fillers will have been removed.
     * Fillers (separating the components within this identifier)
     * will have been replaced by spaces.
     * 
     * @return the primary identifier
     */
    fun getPrimaryIdentifier(): String {
        return primaryIdentifier!!
    }

    /**
     * Returns the document holder's secondary identifier (first names).
     * Trailing fillers will have been removed.
     * Fillers (separating the components within this identifier)
     * will have been replaced by spaces.
     * 
     * @return the secondary identifier
     */
    fun getSecondaryIdentifier(): String {
        return secondaryIdentifier!!
    }

    val secondaryIdentifierComponents: Array<String?>
        /**
         * Returns the document holder's first names.
         * 
         * @return first names
         */
        get() = secondaryIdentifier!!.split(" |<".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

    /**
     * Returns the passport holder's nationality as a 3 digit code.
     * 
     * @return a country
     */
    fun getNationality(): String {
        return mrzFormat(nationality, 3)
    }

    val personalNumber: String?
        /**
         * Returns the personal number (if a personal number is encoded in optional data 1).
         * 
         * @return personal number
         */
        get() {
            if (optionalData1 == null) {
                return null
            }
            if (optionalData1!!.length > 14) {
                return trimTrailingFillerChars(optionalData1!!.substring(0, 14))
            } else {
                return trimTrailingFillerChars(optionalData1)
            }
        }

    /**
     * Returns the passport holder's gender.
     * 
     * @return gender
     * 
     */
    @Deprecated("Result type will be changed to {@linkplain String} in future version. Use {@link #getGenderCode()} instead.")
    fun getGender(): Gender {
        return stringToGender(gender)
    }

    val genderCode: Gender
        /**
         * Returns the passport holder's gender.
         * 
         * @return gender
         */
        get() = stringToGender(gender)

    /**
     * Creates a textual representation of this MRZ.
     * This is the 2 or 3 line representation
     * (depending on the document type) as it
     * appears in the document. All lines end in
     * a newline char.
     * 
     * @return the MRZ as text
     * 
     * @see Object.toString
     */
    override fun toString(): String {
        try {
            val str = String(encoded!!, charset("UTF-8"))
            when (str.length) {
                90 -> return (str.substring(0, 30) + "\n"
                        + str.substring(30, 60) + "\n"
                        + str.substring(60, 90) + "\n")

                72 -> return (str.substring(0, 36) + "\n"
                        + str.substring(36, 72) + "\n")

                88 -> return (str.substring(0, 44) + "\n"
                        + str.substring(44, 88) + "\n")

                else -> return str
            }
        } catch (uee: UnsupportedEncodingException) {
            throw IllegalStateException(uee)
        }
    }

    /**
     * Returns the hash code for this MRZ info.
     * 
     * @return the hash code
     */
    override fun hashCode(): Int {
        return 2 * toString().hashCode() + 53
    }

    /**
     * Whether this MRZ info is identical to some other object.
     * 
     * @param obj the other object
     * 
     * @return a boolean
     */
    override fun equals(obj: Any?): Boolean {
        if (obj == null) {
            return false
        }
        if (!(obj.javaClass == this.javaClass)) {
            return false
        }

        val other = obj as MRZInfo

        return equalsModuloFillerChars(documentCode, other.documentCode)
                && equalsModuloFillerChars(issuingState, other.issuingState)
                && equalsModuloFillerChars(primaryIdentifier, other.primaryIdentifier)
                && equalsModuloFillerChars(secondaryIdentifier, other.secondaryIdentifier)
                && equalsModuloFillerChars(nationality, other.nationality)
                && equalsModuloFillerChars(documentNumber, other.documentNumber)
                && (equalsModuloFillerChars(optionalData1, other.optionalData1) || equalsModuloFillerChars(
            this.personalNumber,
            other.personalNumber
        ))
                && ((dateOfBirth == null && other.dateOfBirth == null) || dateOfBirth != null && dateOfBirth == other.dateOfBirth)
                && ((gender == null && other.gender == null) || gender != null && gender == other.gender)
                && ((dateOfExpiry == null && other.dateOfExpiry == null) || dateOfExpiry != null && dateOfExpiry == other.dateOfExpiry)
                && equalsModuloFillerChars(optionalData2, other.optionalData2)
    }

    /* ONLY PRIVATE METHODS BELOW */
    /**
     * Reads the object value from a stream.
     * 
     * @param inputStream the stream to read from
     * @param length the length of the value
     * 
     * @throws IOException on error reading from the stream
     */
    @Throws(IOException::class)
    private fun readObject(inputStream: InputStream, length: Int) {
        val dataIn = inputStream as? DataInputStream ?: DataInputStream(inputStream)

        /* line 1, pos 1 to 2, Document code, all types. */
        this.documentCode = trimTrailingFillerChars(readString(dataIn, 2))
        this.documentType = Companion.getDocumentType(this.documentCode!!, length)
        when (this.documentType) {
            DocumentType.TD1 -> readObjectTD1(dataIn)
            DocumentType.TD2, DocumentType.MRVB -> readObjectTD2orMRVB(dataIn)
            DocumentType.MRVA, DocumentType.TD3 ->       /* Assume it's a ID3 document, i.e. 2-line MRZ. */
                readObjectTD3OrMRVA(dataIn)

            else ->
                readObjectTD3OrMRVA(dataIn)
        }
    }

    /**
     * Reads the object value from a stream after document-code has already
     * been read, and it is determined that we are dealing with a TD1 style MRZ.
     * 
     * @param inputStream the stream to read from
     * 
     * @throws IOException on error reading from the stream
     */
    @Throws(IOException::class)
    private fun readObjectTD1(inputStream: InputStream) {
        val dataIn = if (inputStream is DataInputStream) inputStream else DataInputStream(inputStream)

        /* line 1, pos 3 to 5 Issuing State or organization */
        this.issuingState = readCountryCode(dataIn)

        /* line 1, pos 6 to 14 Document number */
        this.documentNumber = readString(dataIn, 9)

        /* line 1, pos 15 Check digit */
        this.documentNumberCheckDigit = dataIn.readUnsignedByte().toChar()

        /* line 1, pos 16 to 30, Optional data elements */
        val rawOptionalData1: String = readString(dataIn, 15)
        this.optionalData1 = trimTrailingFillerChars(rawOptionalData1)

        if (documentNumberCheckDigit == '<' && !optionalData1!!.isEmpty()) {
            /* Interpret personal number as part of document number, see note j. */
            var extendedDocumentNumberEnd = optionalData1!!.indexOf('<')
            if (extendedDocumentNumberEnd < 0) {
                extendedDocumentNumberEnd = optionalData1!!.length
            }

            val documentNumberRemainder = optionalData1!!.substring(0, extendedDocumentNumberEnd - 1)
            this.documentNumber += documentNumberRemainder
            this.documentNumberCheckDigit = optionalData1!!.get(extendedDocumentNumberEnd - 1)

            this.optionalData1 =
                optionalData1!!.substring(Integer.min(extendedDocumentNumberEnd + 1, optionalData1!!.length))
        }
        this.documentNumber = trimTrailingFillerChars(this.documentNumber)

        /* line 2, pos 1 to 6, Date of birth */
        this.dateOfBirth = readDate(dataIn)

        /* line 2, pos 7, Check digit */
        this.dateOfBirthCheckDigit = dataIn.readUnsignedByte().toChar()

        /* line 2, pos 8, Sex */
        this.gender = readString(dataIn, 1)

        /* line 2, Pos 9 to 14, Date of expiry */
        this.dateOfExpiry = readDate(dataIn)

        /* line 2, pos 15, Check digit */
        this.dateOfExpiryCheckDigit = dataIn.readUnsignedByte().toChar()

        /* line 2, pos 16 to 18, Nationality */
        this.nationality = readCountryCode(dataIn)

        /* line 2, pos 19 to 29, Optional data elements */
        this.optionalData2 = trimTrailingFillerChars(readString(dataIn, 11))

        /* line 2, pos 30, Overall check digit */
        this.compositeCheckDigit = dataIn.readUnsignedByte().toChar()

        /* line 3 */
        readNameIdentifiers(readString(dataIn, 30))
    }

    /**
     * Reads the object value from a stream after document-code has already
     * been read, and it is determined that we are dealing with a TD2 or MRV-B style MRZ.
     * 
     * @param inputStream the stream to read from
     * 
     * @throws IOException on error reading from the stream
     */
    @Throws(IOException::class)
    private fun readObjectTD2orMRVB(inputStream: InputStream) {
        val dataIn = inputStream as? DataInputStream ?: DataInputStream(inputStream)

        /* line 1, pos 3 to 5 */
        this.issuingState = readCountryCode(dataIn)

        /* line 1, pos 6 to 36 */
        readNameIdentifiers(readString(dataIn, 31))

        /* line 2 */
        this.documentNumber = trimTrailingFillerChars(readString(dataIn, 9))
        this.documentNumberCheckDigit = dataIn.readUnsignedByte().toChar()
        this.nationality = readCountryCode(dataIn)
        this.dateOfBirth = readDate(dataIn)
        this.dateOfBirthCheckDigit = dataIn.readUnsignedByte().toChar()
        this.gender = readString(dataIn, 1)
        this.dateOfExpiry = readDate(dataIn)
        this.dateOfExpiryCheckDigit = dataIn.readUnsignedByte().toChar()
        if (documentType == DocumentType.MRVB) {
            this.optionalData1 = trimTrailingFillerChars(readString(dataIn, 8))
        } else if (documentType == DocumentType.TD2) {
            this.optionalData1 = trimTrailingFillerChars(readString(dataIn, 7))

            if (documentNumberCheckDigit == '<' && !optionalData1!!.isEmpty()) {
                /* Interpret optional data as part of document number, see note j. */
                this.documentNumber += optionalData1!!.substring(0, optionalData1!!.length - 1)
                this.documentNumberCheckDigit = optionalData1!!.get(optionalData1!!.length - 1)
                this.optionalData1 = ""
            }
        }
        this.documentNumber = trimTrailingFillerChars(this.documentNumber)

        if (documentType == DocumentType.TD2) {
            this.compositeCheckDigit = dataIn.readUnsignedByte().toChar()
        }
    }

    /**
     * Reads the object value from a stream after document-code has already
     * been read, and it is determined that we are dealing with a TD3 or MRV-A style MRZ.
     * 
     * @param inputStream the stream to read from
     * 
     * @throws IOException on error reading from the stream
     */
    @Throws(IOException::class)
    private fun readObjectTD3OrMRVA(inputStream: InputStream) {
        val dataIn = inputStream as? DataInputStream ?: DataInputStream(inputStream)

        /* line 1, pos 3 to 5 */
        this.issuingState = readCountryCode(dataIn)

        /* line 1, pos 6 to 44 */
        readNameIdentifiers(readString(dataIn, 39))

        /* line 2 */
        this.documentNumber = trimTrailingFillerChars(readString(dataIn, 9))
        this.documentNumberCheckDigit = dataIn.readUnsignedByte().toChar()
        this.nationality = readCountryCode(dataIn)
        this.dateOfBirth = readDate(dataIn)
        this.dateOfBirthCheckDigit = dataIn.readUnsignedByte().toChar()
        this.gender = readString(dataIn, 1)
        this.dateOfExpiry = readDate(dataIn)
        this.dateOfExpiryCheckDigit = dataIn.readUnsignedByte().toChar()
        if (documentType == DocumentType.MRVA) {
            this.optionalData1 = trimTrailingFillerChars(readString(dataIn, 16))
        } else {
            this.optionalData1 = trimTrailingFillerChars(readString(dataIn, 14))
            this.personalNumberCheckDigit = dataIn.readUnsignedByte().toChar()
            this.compositeCheckDigit = dataIn.readUnsignedByte().toChar()
        }
    }

    /**
     * Writes the MRZ to an output stream.
     * This just outputs the MRZ characters, and does not add newlines.
     * 
     * @param outputStream the output stream to write to
     */
    @Throws(IOException::class)
    public override fun writeObject(outputStream: OutputStream?) {
        when (documentType) {
            DocumentType.TD1 -> writeObjectTD1(outputStream)
            DocumentType.TD2, DocumentType.MRVB -> writeObjectTD2OrMRVB(outputStream)
            DocumentType.TD3, DocumentType.MRVA -> writeObjectTD3OrMRVA(outputStream)
            else -> throw IllegalStateException("Unsupported document type")
        }
    }

    /**
     * Writes this MRZ to stream.
     * 
     * @param outputStream the stream to write to
     * 
     * @throws IOException on error writing to the stream
     */
    @Throws(IOException::class)
    private fun writeObjectTD1(outputStream: OutputStream?) {
        val dataOut = outputStream as? DataOutputStream ?: DataOutputStream(outputStream)

        /* top line */
        writeDocumentType(dataOut)
        writeCountryCode(issuingState, dataOut)

        val isExtendedDocumentNumber = documentNumber!!.length > 9
        if (isExtendedDocumentNumber) {
            /*
       * If document number has more than 9 character, the 9 principal
       * character shall be shown in the MRZ in character positions 1 to 9.
       * They shall be followed by a filler character instead of a check
       * digit to indicate a truncated number. The remaining character of
       * the document number shall be shown at the beginning of the field
       * reserved of optional data element (character position 29 to 35 of
       * the lower machine readable line) followed by a check digit and a
       * filler character.
       *
       * Corresponds to Doc 9303 (pre-seventh edition) pt 3 vol 1 page V-10 (note j) (FIXED by Paulo Assumcao)
       *
       * Also see R3-p1_v2_sIV_0041 in Supplement to Doc 9303, release 11.
       */
            writeString(documentNumber!!.substring(0, 9), dataOut, 9)
            dataOut.write('<'.code) /* NOTE: instead of check digit */
            writeString(
                documentNumber!!.substring(9) + documentNumberCheckDigit.toString() + "<" + (if (optionalData1 == null) "" else optionalData1),
                dataOut,
                15
            )
        } else {
            writeString(documentNumber, dataOut, 9) /* FIXME: max size of field */
            dataOut.write(documentNumberCheckDigit.code)
            writeString(if (optionalData1 == null) "" else optionalData1, dataOut, 15) /* FIXME: max size of field */
        }

        /* middle line */
        writeDateOfBirth(dataOut)
        dataOut.write(dateOfBirthCheckDigit.code)
        writeGender(dataOut)
        writeDateOfExpiry(dataOut)
        dataOut.write(dateOfExpiryCheckDigit.code)
        writeCountryCode(nationality, dataOut)
        writeString(if (optionalData2 == null) "" else optionalData2, dataOut, 11)
        dataOut.write(compositeCheckDigit.code)

        /* bottom line */
        writeName(dataOut, 30)
    }

    /**
     * Writes this MRZ to stream.
     * 
     * @param outputStream the stream to write to
     * 
     * @throws IOException on error writing to the stream
     */
    @Throws(IOException::class)
    private fun writeObjectTD2OrMRVB(outputStream: OutputStream?) {
        val dataOut = outputStream as? DataOutputStream ?: DataOutputStream(outputStream)

        /* top line */
        writeDocumentType(dataOut)
        writeCountryCode(issuingState, dataOut)
        writeName(dataOut, 31)

        /* bottom line */
        val isExtendedDocumentNumber =
            documentType == DocumentType.TD2 && documentNumber!!.length > 9 && equalsModuloFillerChars(
                optionalData1,
                ""
            )
        if (isExtendedDocumentNumber) {
            writeString(documentNumber!!.substring(0, 9), dataOut, 9)
            dataOut.write('<'.code) /* NOTE: instead of check digit */
        } else {
            writeString(documentNumber, dataOut, 9) /* FIXME: max size of field */
            dataOut.write(documentNumberCheckDigit.code)
        }

        writeCountryCode(nationality, dataOut)
        writeDateOfBirth(dataOut)
        dataOut.write(dateOfBirthCheckDigit.code)
        writeGender(dataOut)
        writeDateOfExpiry(dataOut)
        dataOut.write(dateOfExpiryCheckDigit.code)
        if (documentType == DocumentType.MRVB) {
            writeString(if (optionalData1 == null) "" else optionalData1, dataOut, 8)
        } else if (isExtendedDocumentNumber) {
            writeString(documentNumber!!.substring(9) + documentNumberCheckDigit + "<", dataOut, 7)
            dataOut.write(compositeCheckDigit.code)
        } else {
            writeString(if (optionalData1 == null) "" else optionalData1, dataOut, 7)
            dataOut.write(compositeCheckDigit.code)
        }
    }

    /**
     * Writes this MRZ to stream.
     * 
     * @param outputStream the stream to write to
     * 
     * @throws IOException on error writing to the stream
     */
    @Throws(IOException::class)
    private fun writeObjectTD3OrMRVA(outputStream: OutputStream?) {
        val dataOut = outputStream as? DataOutputStream ?: DataOutputStream(outputStream)

        /* top line */
        writeDocumentType(dataOut)
        writeCountryCode(issuingState, dataOut)
        writeName(dataOut, 39)

        /* bottom line */
        writeString(documentNumber, dataOut, 9)
        dataOut.write(documentNumberCheckDigit.code)
        writeCountryCode(nationality, dataOut)
        writeDateOfBirth(dataOut)
        dataOut.write(dateOfBirthCheckDigit.code)
        writeGender(dataOut)
        writeDateOfExpiry(dataOut)
        dataOut.write(dateOfExpiryCheckDigit.code)
        if (documentType == DocumentType.MRVA) {
            writeString(if (optionalData1 == null) "" else optionalData1, dataOut, 16)
        } else {
            // Must be TD3.
            writeString(if (optionalData1 == null) "" else optionalData1, dataOut, 14)
            dataOut.write(personalNumberCheckDigit.code)
            dataOut.write(compositeCheckDigit.code)
        }
    }

    /**
     * Sets the name identifiers (primary and secondary identifier) based on
     * the name in the MRZ.
     * 
     * @param mrzNameString the name field as it occurs in the MRZ
     */
    private fun readNameIdentifiers(mrzNameString: String) {
        val delimIndex = mrzNameString.indexOf("<<")
        if (delimIndex < 0) {
            /* Only a primary identifier. */
            primaryIdentifier = trimTrailingFillerChars(mrzNameString).replace("<", " ")
            this.secondaryIdentifier = ""
            return
        }
        primaryIdentifier = trimTrailingFillerChars(mrzNameString.substring(0, delimIndex)).replace("<", " ")
        val rest = mrzNameString.substring(delimIndex + 2)
        readSecondaryIdentifiers(rest)
    }

    /**
     * Sets the secondary identifier.
     * 
     * @param secondaryIdentifier the new secondary identifier
     */
    private fun readSecondaryIdentifiers(secondaryIdentifier: String?) {
        this.secondaryIdentifier = trimTrailingFillerChars(secondaryIdentifier).replace("<", " ")
    }

    /**
     * Writes a MRZ string to a stream, optionally formatting the MRZ string.
     * 
     * @param string the string to write
     * @param dataOutputStream the stream to write to
     * @param width the width of the MRZ field (the string will be augmented with trailing fillers)
     * 
     * @throws IOException on error writing to the stream
     */
    @Throws(IOException::class)
    private fun writeString(string: String?, dataOutputStream: DataOutputStream, width: Int) {
        dataOutputStream.write(mrzFormat(string, width).toByteArray(charset("UTF-8")))
    }

    /**
     * Writes the date of expiry to a stream.
     * 
     * @param dateOutputStream the stream to write to
     * 
     * @throws IOException on error writing to the stream
     */
    @Throws(IOException::class)
    private fun writeDateOfExpiry(dateOutputStream: DataOutputStream) {
        dateOutputStream.write(dateOfExpiry!!.toByteArray(charset("UTF-8")))
    }

    /**
     * Writes the gender to a stream.
     * 
     * @param dataOutputStream the stream to write to
     * 
     * @throws IOException on error writing to the stream
     */
    @Throws(IOException::class)
    private fun writeGender(dataOutputStream: DataOutputStream) {
        dataOutputStream.write(gender!!.toByteArray(charset("UTF-8")))
    }

    /**
     * Writes the data of birth to a stream.
     * 
     * @param dataOutputStream the stream to write to
     * 
     * @throws IOException on error writing to the stream
     */
    @Throws(IOException::class)
    private fun writeDateOfBirth(dataOutputStream: DataOutputStream) {
        dataOutputStream.write(dateOfBirth!!.toByteArray(charset("UTF-8")))
    }

    /**
     * Writes the name to a stream.
     * 
     * @param dataOutputStream the stream to write to
     * @param width the width of the field
     * 
     * @throws IOException on error writing to the stream
     */
    @Throws(IOException::class)
    private fun writeName(dataOutputStream: DataOutputStream, width: Int) {
        dataOutputStream.write(
            Companion.nameToString(primaryIdentifier!!, secondaryIdentifier, width).toByteArray(
                charset("UTF-8")
            )
        )
    }

    /**
     * Write the document type to a stream.
     * 
     * @param dataOutputStream the stream to write to
     * 
     * @throws IOException on error writing to the stream
     */
    @Throws(IOException::class)
    private fun writeDocumentType(dataOutputStream: DataOutputStream) {
        writeString(documentCode, dataOutputStream, 2)
    }

    /**
     * Reads a date.
     * Result is typically in `"yyMMdd"` format.
     * 
     * @param inputStream the stream to read from
     * 
     * @return the date of birth
     * 
     * @throws IOException if something goes wrong
     * @throws NumberFormatException if a data could not be constructed
     */
    @Throws(IOException::class, NumberFormatException::class)
    private fun readDate(inputStream: DataInputStream): String {
        return readString(inputStream, 6)
    }

    /**
     * Returns the composite part over which the composite check digit is computed.
     * 
     * @param documentType the type of document, either `DOC_TYPE_ID1` or `DOC_TYPE_ID3`
     * 
     * @return a string with the composite part
     */
    private fun getComposite(documentType: DocumentType): String? {
        val composite = StringBuilder()
        val documentNumberLength = documentNumber!!.length

        when (documentType) {
            DocumentType.TD1 -> {
                /*
       * Upper line:
       * 6-30, i.e., documentNumber, documentNumberCheckDigit, optionaldata1(15)
       *
       * Middle line:
       * 1-7, i.e., dateOfBirth, dateOfBirthCheckDigit
       * 9-15, i.e., dateOfExpiry, dateOfExpiryCheckDigit
       * 19-29, i.e., optionalData2(11)
       */
                if (documentNumberLength <= 9) {
                    composite.append(mrzFormat(documentNumber, 9))
                    composite.append(documentNumberCheckDigit)
                    composite.append(mrzFormat(optionalData1, 15))
                } else {
                    /* Document number, first 9 characters. */
                    composite.append(documentNumber!!.substring(0, 9))
                    composite.append("<") /* Filler instead of check digit. */

                    /* Remainder of document number. */
                    val documentNumberRemainder = documentNumber!!.substring(9)
                    composite.append(documentNumberRemainder)
                    composite.append(documentNumberCheckDigit)
                    composite.append('<')

                    /* Remainder of optional data 1 (removing any prefix). */
                    val optionalData1Remainder: String =
                        mrzFormat(optionalData1, 15 - 2 - documentNumberRemainder.length)
                    composite.append(optionalData1Remainder)
                }
                composite.append(dateOfBirth)
                composite.append(dateOfBirthCheckDigit)
                composite.append(dateOfExpiry)
                composite.append(dateOfExpiryCheckDigit)
                composite.append(mrzFormat(optionalData2, 11))
                return composite.toString()
            }

            DocumentType.TD2 -> {
                /* Composite check digit lower line: 1-10, 14-20, 22-35. */
                composite.append(documentNumber)
                composite.append(documentNumberCheckDigit)
                composite.append(dateOfBirth)
                composite.append(dateOfBirthCheckDigit)
                composite.append(dateOfExpiry)
                composite.append(dateOfExpiryCheckDigit)
                composite.append(mrzFormat(optionalData1, 7))
                return composite.toString()
            }

            DocumentType.MRVB ->       /* No composite checkdigit for MRV-B. */
                return null

            DocumentType.TD3 -> {
                /* Composite check digit lower line: 1-10, 14-20, 22-43. */
                composite.append(mrzFormat(documentNumber, 9))
                composite.append(documentNumberCheckDigit)
                composite.append(dateOfBirth)
                composite.append(dateOfBirthCheckDigit)
                composite.append(dateOfExpiry)
                composite.append(dateOfExpiryCheckDigit)
                composite.append(mrzFormat(optionalData1, 14))
                composite.append(personalNumberCheckDigit)
                return composite.toString()
            }

            DocumentType.MRVA ->       /* No composite checkdigit for MRV-A. */
                return null

            else -> throw IllegalStateException("Unsupported document type")
        }
    }

    /**
     * Updates the check digit fields for document number,
     * date of birth, date of expiry, and composite.
     */
    private fun checkDigit() {
        this.documentNumberCheckDigit = checkDigit(documentNumber)
        this.dateOfBirthCheckDigit = checkDigit(dateOfBirth)
        this.dateOfExpiryCheckDigit = checkDigit(dateOfExpiry)

        if (documentType == DocumentType.TD3 && optionalData1!!.length < 15) {
            this.personalNumberCheckDigit =
                checkDigit(mrzFormat(optionalData1, 14), true) /* FIXME: Uses '<' over '0'. Where specified? */
        }

        this.compositeCheckDigit = checkDigit(getComposite(documentType!!))
    }

    companion object {
        private const val serialVersionUID = 7054965914471297804L

        /** Unspecified document type (do not use, choose ID1 or ID3).  */
        const val DOC_TYPE_UNSPECIFIED: Int = 0

        /** ID1 document type for credit card sized identity cards. Specifies a 3-line MRZ, 30 characters wide.  */
        const val DOC_TYPE_ID1: Int = 1

        /** ID2 document type. Specifies a 2-line MRZ, 36 characters wide.  */
        const val DOC_TYPE_ID2: Int = 2

        /** ID3 document type for passport booklets. Specifies a 2-line MRZ, 44 characters wide.  */
        const val DOC_TYPE_ID3: Int = 3

        /** All valid characters in MRZ.  */
        private const val MRZ_CHARS = "<0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"

        /**
         * Creates a new 3-line, 30 character (TD1) MRZ compliant with ICAO Doc 9303 (seventh edition) part 5.
         * 
         * @param documentCode document code (1 or 2 digit, has to start with "I", "C", or "A")
         * @param issuingState issuing state as 3 alpha string
         * @param primaryIdentifier card holder last name
         * @param secondaryIdentifier card holder first name(s)
         * @param documentNumber document number
         * @param nationality nationality as 3 alpha string
         * @param dateOfBirth date of birth in yyMMdd format
         * @param gender gender, must not be `null`
         * @param dateOfExpiry date of expiry in yyMMdd format
         * @param optionalData1 optional data in line 1 of maximum length 15
         * @param optionalData2 optional data in line 2 of maximum length 11
         * 
         * @return the 3-line MRZ
         */
        fun createTD1MRZInfo(
            documentCode: String?,
            issuingState: String?,
            documentNumber: String?,
            optionalData1: String?,
            dateOfBirth: String?,
            gender: Any,
            dateOfExpiry: String?,
            nationality: String?,
            optionalData2: String?,
            primaryIdentifier: String?,
            secondaryIdentifier: String?
        ): MRZInfo {
            return MRZInfo(
                DocumentType.TD1,
                documentCode,
                issuingState,
                documentNumber,
                optionalData1,
                dateOfBirth,
                gender,
                dateOfExpiry,
                nationality,
                optionalData2,
                primaryIdentifier,
                secondaryIdentifier
            )
        }

        /**
         * Creates a new 2-line, 36 character (TD2) MRZ compliant with ICAO Doc 9303 (seventh edition) part 6.
         * 
         * @param documentCode document code (1 or 2 digit, has to start with "P" or "V")
         * @param issuingState issuing state as 3 alpha string
         * @param primaryIdentifier card holder last name
         * @param secondaryIdentifier card holder first name(s)
         * @param documentNumber document number
         * @param nationality nationality as 3 alpha string
         * @param dateOfBirth date of birth
         * @param gender gender, must not be `null`
         * @param dateOfExpiry date of expiry
         * @param optionalData either empty or optional data of maximal length 9
         * 
         * @return the 2-line MRZ
         */
        fun createTD2MRZInfo(
            documentCode: String?, issuingState: String?,
            primaryIdentifier: String?, secondaryIdentifier: String?,
            documentNumber: String?, nationality: String?, dateOfBirth: String?,
            gender: Any, dateOfExpiry: String?, optionalData: String?
        ): MRZInfo {
            return MRZInfo(
                DocumentType.TD2,
                documentCode,
                issuingState,
                documentNumber,
                optionalData,
                dateOfBirth,
                gender,
                dateOfExpiry,
                nationality,
                null,
                primaryIdentifier,
                secondaryIdentifier
            )
        }

        /**
         * Creates a new 2-line, 44 character (TD3) MRZ compliant with ICAO Doc 9303 (seventh edition) part 4.
         * 
         * @param documentCode document code (1 or 2 digit, has to start with "P")
         * @param issuingState issuing state as 3 alpha string
         * @param primaryIdentifier card holder last name
         * @param secondaryIdentifier card holder first name(s)
         * @param documentNumber document number
         * @param nationality nationality as 3 alpha string
         * @param dateOfBirth date of birth
         * @param gender gender, must not be `null`
         * @param dateOfExpiry date of expiry
         * @param personalNumber either empty, or a personal number of maximum length 14, or other optional data of exact length 15
         * 
         * @return the 2-line MRZ
         */
        fun createTD3MRZInfo(
            documentCode: String?, issuingState: String?,
            primaryIdentifier: String?, secondaryIdentifier: String?,
            documentNumber: String?, nationality: String?, dateOfBirth: String?,
            gender: Any, dateOfExpiry: String?, personalNumber: String?
        ): MRZInfo {
            return MRZInfo(
                DocumentType.TD3,
                documentCode,
                issuingState,
                documentNumber,
                personalNumberToOptionalData(personalNumber),
                dateOfBirth,
                gender,
                dateOfExpiry,
                nationality,
                null,
                primaryIdentifier,
                secondaryIdentifier
            )
        }

        /**
         * Creates a new 2-line, 44 character (MRV-A) MRZ compliant with ICAO Doc 9303 (seventh edition) part 7.
         * 
         * @param documentCode document code (1 or 2 digit, has to start with "V")
         * @param issuingState issuing state as 3 alpha string
         * @param primaryIdentifier card holder last name
         * @param secondaryIdentifier card holder first name(s)
         * @param documentNumber document number
         * @param nationality nationality as 3 alpha string
         * @param dateOfBirth date of birth
         * @param gender gender, must not be `null`
         * @param dateOfExpiry date of expiry
         * @param optionalData optional data at discretion of issuing state
         * 
         * @return the 2-line MRZ
         */
        fun createMRVAMRZInfo(
            documentCode: String?, issuingState: String?,
            primaryIdentifier: String?, secondaryIdentifier: String?,
            documentNumber: String?, nationality: String?, dateOfBirth: String?,
            gender: Any, dateOfExpiry: String?, optionalData: String?
        ): MRZInfo {
            return MRZInfo(
                DocumentType.MRVA,
                documentCode,
                issuingState,
                documentNumber,
                optionalData,
                dateOfBirth,
                gender,
                dateOfExpiry,
                nationality,
                null,
                primaryIdentifier,
                secondaryIdentifier
            )
        }

        /**
         * Creates a new 2-line, 36 character (MRV-B) MRZ compliant with ICAO Doc 9303 (seventh edition) part 7.
         * 
         * @param documentCode document code (1 or 2 digit, has to start with "V")
         * @param issuingState issuing state as 3 alpha string
         * @param primaryIdentifier card holder last name
         * @param secondaryIdentifier card holder first name(s)
         * @param documentNumber document number
         * @param nationality nationality as 3 alpha string
         * @param dateOfBirth date of birth
         * @param gender gender, must not be `null`
         * @param dateOfExpiry date of expiry
         * @param optionalData optional data at discretion of issuing state
         * 
         * @return the 2-line MRZ
         */
        fun createMRVBMRZInfo(
            documentCode: String?, issuingState: String?,
            primaryIdentifier: String?, secondaryIdentifier: String?,
            documentNumber: String?, nationality: String?, dateOfBirth: String?,
            gender: Any, dateOfExpiry: String?, optionalData: String?
        ): MRZInfo {
            return MRZInfo(
                DocumentType.MRVB,
                documentCode,
                issuingState,
                documentNumber,
                optionalData,
                dateOfBirth,
                gender,
                dateOfExpiry,
                nationality,
                null,
                primaryIdentifier,
                secondaryIdentifier
            )
        }

        /**
         * Computes the 7-3-1 check digit for part of the MRZ.
         * 
         * @param str a part of the MRZ.
         * 
         * @return the resulting check digit (in '0' - '9')
         */
        fun checkDigit(str: String?): Char {
            return checkDigit(str, false)
        }

        /**
         * Tests equality of two MRZ string while ignoring extra filler characters.
         * 
         * @param str1 an MRZ string
         * @param str2 another MRZ string
         * 
         * @return a boolean indicating whether the strings are equal modulo filler characters
         */
        fun equalsModuloFillerChars(str1: String?, str2: String?): Boolean {
            var str1 = str1
            var str2 = str2
            if (str1 === str2) {
                return true
            }
            if (str1 == null) {
                str1 = ""
            }
            if (str2 == null) {
                str2 = ""
            }

            val length = max(str1.length, str2.length)
            return mrzFormat(str1, length) == mrzFormat(str2, length)
        }

        /**
         * Writes the issuing state to an stream.
         * 
         * @param dataOutputStream the stream to write to
         * 
         * @throws IOException on error writing to the stream
         */
        @Throws(IOException::class)
        private fun writeCountryCode(countryCode: String?, dataOutputStream: DataOutputStream) {
            dataOutputStream.write(mrzFormat(countryCode, 3).toByteArray(charset("UTF-8")))
        }

        /**
         * Converts a gender to a string to be used in an MRZ.
         * 
         * @param genderObject the gender
         * 
         * @return a string to be used in an MRZ
         */
        private fun genderToString(genderObject: Any?): String? {
            if (genderObject is Gender) {
                when (genderObject) {
                    Gender.MALE -> return "M"
                    Gender.FEMALE -> return "F"
                    else -> return "<"
                }
            }

            return Objects.toString(genderObject)
        }

        /**
         * Converts a gender string to a value of the [Gender] enum.
         * 
         * @param genderStr a gender string
         * @return
         */
        private fun stringToGender(genderStr: String?): Gender {
            if ("M".equals(genderStr, ignoreCase = true)) {
                return Gender.MALE
            }
            if ("F".equals(genderStr, ignoreCase = true)) {
                return Gender.FEMALE
            }
            return Gender.UNKNOWN
        }

        /**
         * Encodes the personal number as optional data in case of TD3 style MRZ.
         * If the number does not yet include a check-digit it will be added.
         * 
         * @param personalNumber the personal number (or optional data)
         * 
         * @return the optional data to include in the MRZ
         */
        private fun personalNumberToOptionalData(personalNumber: String?): String {
            if (personalNumber == null || equalsModuloFillerChars(personalNumber, "")) {
                /* optional data field is not used */
                return ""
            } else if (personalNumber.length == 15) {
                /* it's either a personalNumber with check digit included, or some other optional data. FIXME: Is this case possible? */
                return personalNumber
            } else if (personalNumber.length <= 14) {
                /* we'll assume it's a personalNumber without check digit, and we add the check digit ourselves */
                return mrzFormat(personalNumber, 14)
            } else {
                throw IllegalArgumentException("Wrong personal number: " + personalNumber)
            }
        }

        /**
         * Converts the name (primary and secondary identifier) to a single MRZ formatted name
         * field of the given length.
         * 
         * @param primaryIdentifier the primary identifier part of the name
         * @param secondaryIdentifier the secondary identifier part of the name
         * @param width the width of the resulting MRZ formatted string
         * 
         * @return the string containing the MRZ formatted name field
         */
        private fun nameToString(primaryIdentifier: String, secondaryIdentifier: String?, width: Int): String {
            val primaryComponents: Array<String?> =
                primaryIdentifier.split(" |<".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val secondaryComponents: Array<String?> =
                if (secondaryIdentifier == null || secondaryIdentifier.trim { it <= ' ' }
                        .isEmpty()) arrayOfNulls<String>(0) else secondaryIdentifier.split(" |<".toRegex())
                    .dropLastWhile { it.isEmpty() }.toTypedArray()

            val name = StringBuilder()
            var isFirstPrimaryComponent = true
            for (primaryComponent in primaryComponents) {
                if (isFirstPrimaryComponent) {
                    isFirstPrimaryComponent = false
                } else {
                    name.append('<')
                }
                name.append(primaryComponent)
            }

            if (secondaryIdentifier != null && !secondaryIdentifier.trim { it <= ' ' }.isEmpty()) {
                name.append("<<")
                var isFirstSecondaryComponent = true
                for (secondaryComponent in secondaryComponents) {
                    if (isFirstSecondaryComponent) {
                        isFirstSecondaryComponent = false
                    } else {
                        name.append('<')
                    }
                    name.append(secondaryComponent)
                }
            }

            return mrzFormat(name.toString(), width)
        }

        /**
         * Reads the issuing state as a three letter string.
         * 
         * @param inputStream the stream to read from
         * 
         * @return a string of length 3 containing an abbreviation
         * of the issuing state or organization
         * 
         * @throws IOException error reading from the stream
         */
        @Throws(IOException::class)
        private fun readCountryCode(inputStream: DataInputStream): String {
            return trimTrailingFillerChars(readString(inputStream, 3))
        }

        /**
         * Reads a fixed length string from a stream.
         * 
         * @param inputStream the stream to read from
         * @param count the fixed length
         * 
         * @return the string that was read
         * 
         * @throws IOException on error reading from the stream
         */
        @Throws(IOException::class)
        private fun readString(inputStream: DataInputStream, count: Int): String {
            val data = ByteArray(count)
            inputStream.readFully(data)
            return String(data).trim { it <= ' ' }
        }

        /**
         * Reformats the input string such that it
         * only contains ['A'-'Z'], ['0'-'9'], '<' characters
         * by replacing other characters with '<'.
         * Also extends to the given length by adding '<' to the right.
         * 
         * @param str the input string
         * @param width the (minimal) width of the result
         * 
         * @return the reformatted string
         */
        private fun mrzFormat(str: String?, width: Int): String {
            var str = str ?: return ""
            require(str.length <= width) { "Argument too wide (" + str.length + " > " + width + ")" }
            str = str.uppercase(Locale.getDefault()).trim { it <= ' ' }
            val result = StringBuilder()
            for (i in 0..<str.length) {
                val c = str.get(i)
                if (MRZ_CHARS.indexOf(c) == -1) {
                    result.append('<')
                } else {
                    result.append(c)
                }
            }
            while (result.length < width) {
                result.append("<")
            }
            return result.toString()
        }

        /**
         * Determines the document-type.
         * 
         * @param documentCode the document-code
         * @param length the length of the complete MRZ (excluding whitespace)
         * 
         * @return the document-type enum value
         */
        private fun getDocumentType(documentCode: String, length: Int): DocumentType {
            require(!(documentCode.length < 1 || documentCode.length > 2)) { "Was expecting 1 or 2 digit document code, got $documentCode" }

            when (length) {
                90 ->       /* Document-code must start with C, I, or A. */
                    return DocumentType.TD1

                72 -> if (documentCode.startsWith("V")) {
                    return DocumentType.MRVB
                } else {
                    /* Document-code must start with C, I, or A. */
                    return DocumentType.TD2
                }

                88 -> if (documentCode.startsWith("V")) {
                    return DocumentType.MRVA
                } else {
                    /* Document-code must start with P. */
                    return DocumentType.TD3
                }

                else -> return DocumentType.UNKNOWN
            }
        }

        /*
   * NOTE: Can be removed once deprecated methods are gone.
   */
        /**
         * Determines the document type based on the document code (the first two characters of the MRZ).
         * 
         * ICAO Doc 9303 part 3 vol 1 defines MRTDs with 3-line MRZs,
         * in this case the document code starts with "A", "C", or "I"
         * according to note j to Section 6.6 (page V-9).
         * 
         * ICAO Doc 9303 part 2 defines MRVs with 2-line MRZs,
         * in this case the document code starts with "V".
         * 
         * ICAO Doc 9303 part 1 vol 1 defines MRPs with 2-line MRZs,
         * in this case the document code starts with "P"
         * according to Section 9.6 (page IV-15).
         * 
         * @param documentCode a two letter code
         * 
         * @return a document type, one of [.DOC_TYPE_ID1], [.DOC_TYPE_ID2],
         * [.DOC_TYPE_ID3], or [.DOC_TYPE_UNSPECIFIED]
         */
        private fun getDocumentTypeFromDocumentCode(documentCode: String): DocumentType {
            if (documentCode.startsWith("A")
                || documentCode.startsWith("C")
                || documentCode.startsWith("I")
            ) {
                /* MRTD according to ICAO Doc 9303 (seventh edition) part 5 or 6. NOTE: Could also be TD2. */
                return DocumentType.TD1
            } else if (documentCode.startsWith("V")) {
                /* MRV according to ICAO Doc 9303 (old) part 2. NOTE: Could also be MRVA. */
                return DocumentType.MRVB
            } else if (documentCode.startsWith("P")) {
                /* MRP according to ICAO Doc 9303 (old) part 1 vol 1 */
                return DocumentType.TD3
            }
            return DocumentType.UNKNOWN
        }

        /**
         * Replaces '<' with ' ' and trims leading and trailing whitespace.
         * 
         * @param str the string to read from
         * 
         * @return a trimmed string
         */
        private fun trimTrailingFillerChars(str: String?): String {
            var str = str
            if (str == null) {
                str = ""
            }
            val chars = str.trim { it <= ' ' }.toByteArray()
            for (i in chars.indices.reversed()) {
                if (chars[i] == '<'.code.toByte()) {
                    chars[i] = ' '.code.toByte()
                } else {
                    break
                }
            }
            return (String(chars)).trim { it <= ' ' }
        }

        /**
         * Checks if the document-code is consistent with the given document-type.
         * 
         * @param documentType the document-type
         * @param documentCode the document-code
         * 
         * @return a boolean
         */
        private fun isDocumentCodeConsistentWithDocumentType(
            documentType: DocumentType,
            documentCode: String?
        ): Boolean {
            if (documentCode == null) {
                return false
            }

            if (documentCode.length != 1 && documentCode.length != 2) {
                return false
            }

            when (documentType) {
                DocumentType.TD1, DocumentType.TD2 -> return documentCode.startsWith("C") || documentCode.startsWith("I") || documentCode.startsWith(
                    "A"
                )

                DocumentType.TD3 -> return documentCode.startsWith("P")
                DocumentType.MRVA, DocumentType.MRVB -> return documentCode.startsWith("V")
                else -> return false
            }
        }

        /**
         * Checks if the optional data is consistent with the given document-type.
         * 
         * @param documentType the document-type
         * @param optionalData1 optional data 1 or personal number
         * @param optionalData2 optional data 2 or `null` if not present
         * 
         * @return a boolean
         */
        private fun isOptionalDataConsistentWithDocumentType(
            documentType: DocumentType,
            optionalData1: String?,
            optionalData2: String?
        ): Boolean {
            when (documentType) {
                DocumentType.TD1 -> return (optionalData1 == null || optionalData1.length <= 15) && (optionalData2 == null || optionalData2.length <= 11)
                DocumentType.TD2 -> return (optionalData1 == null || optionalData1.length <= 7) && optionalData2 == null
                DocumentType.MRVB -> return (optionalData1 == null || optionalData1.length <= 8) && optionalData2 == null
                DocumentType.TD3 -> return (optionalData1 == null || optionalData1.length <= 15) && optionalData2 == null
                DocumentType.MRVA -> return (optionalData1 == null || optionalData1.length <= 16) && optionalData2 == null
                else -> return false
            }
        }

        /**
         * Computes the 7-3-1 check digit for part of the MRZ.
         * If `preferFillerOverZero` is `true` then '<' will be
         * returned on check digit 0.
         * 
         * @param str a part of the MRZ
         * @param preferFillerOverZero a boolean indicating whether fillers should be preferred
         * 
         * @return the resulting check digit (in '0' - '9', '<')
         */
        private fun checkDigit(str: String?, preferFillerOverZero: Boolean): Char {
            try {
                val chars = if (str == null) byteArrayOf() else str.toByteArray(charset("UTF-8"))
                val weights = intArrayOf(7, 3, 1)
                var result = 0
                for (i in chars.indices) {
                    result = (result + weights[i % 3] * decodeMRZDigit(chars[i])) % 10
                }
                val checkDigitString = result.toString()
                check(checkDigitString.length == 1) { "Error in computing check digit." }
                var checkDigit = Char(checkDigitString.toByteArray(charset("UTF-8"))[0].toUShort())
                if (preferFillerOverZero && checkDigit == '0') {
                    checkDigit = '<'
                }
                return checkDigit
            } catch (nfe: NumberFormatException) {
                /* NOTE: never happens. */
                throw IllegalStateException("Error in computing check digit", nfe)
            } catch (usee: UnsupportedEncodingException) {
                /* NOTE: never happens. */
                throw IllegalStateException("Error in computing check digit", usee)
            } catch (e: Exception) {
                throw IllegalArgumentException("Error in computing check digit", e)
            }
        }

        /**
         * Looks up the numerical value for MRZ characters. In order to be able
         * to compute check digits.
         * 
         * @param ch a character from the MRZ.
         * 
         * @return the numerical value of the character.
         * 
         * @throws NumberFormatException if `ch` is not a valid MRZ character
         */
        private fun decodeMRZDigit(ch: Byte): Int {
            when (ch.toInt().toChar()) {
                '<', '0' -> return 0
                '1' -> return 1
                '2' -> return 2
                '3' -> return 3
                '4' -> return 4
                '5' -> return 5
                '6' -> return 6
                '7' -> return 7
                '8' -> return 8
                '9' -> return 9
                'a', 'A' -> return 10
                'b', 'B' -> return 11
                'c', 'C' -> return 12
                'd', 'D' -> return 13
                'e', 'E' -> return 14
                'f', 'F' -> return 15
                'g', 'G' -> return 16
                'h', 'H' -> return 17
                'i', 'I' -> return 18
                'j', 'J' -> return 19
                'k', 'K' -> return 20
                'l', 'L' -> return 21
                'm', 'M' -> return 22
                'n', 'N' -> return 23
                'o', 'O' -> return 24
                'p', 'P' -> return 25
                'q', 'Q' -> return 26
                'r', 'R' -> return 27
                's', 'S' -> return 28
                't', 'T' -> return 29
                'u', 'U' -> return 30
                'v', 'V' -> return 31
                'w', 'W' -> return 32
                'x', 'X' -> return 33
                'y', 'Y' -> return 34
                'z', 'Z' -> return 35
                else -> throw NumberFormatException(
                    "Could not decode MRZ character $ch ('" + Char(ch.toUShort()).toString() + "')"
                )
            }
        }
    }
}
