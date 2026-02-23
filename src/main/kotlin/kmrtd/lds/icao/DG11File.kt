/*
 * JMRTD - A Java API for accessing machine readable travel documents.
 *
 * Copyright (C) 2006 - 2026  The JMRTD team
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
 * $Id: DG11File.java 1907 2026-02-06 09:24:02Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.lds.icao

import kmrtd.lds.LDSFile
import net.sf.scuba.tlv.TLVInputStream
import net.sf.scuba.tlv.TLVOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * File structure for the EF_DG11 file.
 * Datagroup 11 contains additional personal detail(s).
 * 
 * All fields are optional. See Section 16 of LDS-TR.
 * 
 *  1. Name of Holder (Primary and Secondary Identifiers, in full)
 *  1. Other Name(s)
 *  1. Personal Number
 *  1. Place of Birth
 *  1. Date of Birth (in full)
 *  1. Address
 *  1. Telephone Number(s)
 *  1. Profession
 *  1. Title
 *  1. Personal Summary
 *  1. Proof of Citizenship [see 14.5.1]
 *  1. Number of Other Valid Travel Documents
 *  1. Other Travel Document Numbers
 *  1. Custody Information
 * 
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1907 $
 */
class DG11File : AdditionalDetailDataGroup {
    /**
     * Returns the full name of the holder (primary and secondary identifiers).
     * 
     * @return the name of holder
     */
    var nameOfHolder: String? = null
        private set
    private var otherNames: MutableList<String?>? = null

    /**
     * Returns the personal number.
     * 
     * @return the personal number
     */
    var personalNumber: String? = null
        private set

    /**
     * Returns the full date of birth.
     * 
     * @return the full date of birth
     */
    var fullDateOfBirth: String? = null
        private set

    /**
     * Returns the place of birth.
     * 
     * @return the place of birth
     */
    var placeOfBirth: MutableList<String?>? = null
        private set

    /**
     * Returns the permanent address.
     * 
     * @return the permanent address
     */
    var permanentAddress: MutableList<String?>? = null
        private set

    /**
     * Returns the telephone number.
     * 
     * @return the telephone
     */
    var telephone: String? = null
        private set

    /**
     * Returns the holder's profession.
     * 
     * @return the profession
     */
    var profession: String? = null
        private set

    /**
     * Returns the holder's title.
     * 
     * @return the title
     */
    var title: String? = null
        private set

    /**
     * Returns the personal summary.
     * 
     * @return the personal summary
     */
    var personalSummary: String? = null
        private set

    /**
     * Returns the proof of citizenship.
     * 
     * @return the proof of citizenship
     */
    var proofOfCitizenship: ByteArray? = null
        private set

    /**
     * Returns the other valid travel document numbers.
     * 
     * @return the other valid travel document numbers
     */
    var otherValidTDNumbers: MutableList<String?>? = null
        private set

    /**
     * Returns the custody information.
     * 
     * @return the custody information
     */
    var custodyInformation: String? = null
        private set

    override var tagPresenceList: MutableList<Int>? = null

    /**
     * Constructs a file from binary representation.
     * 
     * @param inputStream an input stream
     * 
     * @throws IOException if reading fails
     */
    constructor(inputStream: InputStream) : super(LDSFile.Companion.EF_DG11_TAG, inputStream)

    /**
     * Constructs a new file. Use `null` if data element is not present.
     * Use `'<'` as separator.
     * 
     * @param nameOfHolder data element
     * @param otherNames data element
     * @param personalNumber data element
     * @param fullDateOfBirth data element
     * @param placeOfBirth data element
     * @param permanentAddress data element
     * @param telephone data element
     * @param profession data element
     * @param title data element
     * @param personalSummary data element
     * @param proofOfCitizenship data element
     * @param otherValidTDNumbers data element
     * @param custodyInformation data element
     */
    constructor(
        nameOfHolder: String?,
        otherNames: MutableList<String?>?, personalNumber: String?,
        fullDateOfBirth: Date?, placeOfBirth: MutableList<String?>?, permanentAddress: MutableList<String?>?,
        telephone: String?, profession: String?, title: String?,
        personalSummary: String?, proofOfCitizenship: ByteArray?,
        otherValidTDNumbers: MutableList<String?>?, custodyInformation: String?
    ) : this(
        nameOfHolder,
        otherNames, personalNumber,
        if (fullDateOfBirth == null) null else SimpleDateFormat(SDF).format(fullDateOfBirth),
        placeOfBirth, permanentAddress,
        telephone, profession, title,
        personalSummary, proofOfCitizenship,
        otherValidTDNumbers, custodyInformation
    )

    /**
     * Constructs a new file. Use `null` if data element is not present.
     * Use `'<'` as separator.
     * 
     * @param nameOfHolder data element
     * @param otherNames data element
     * @param personalNumber data element
     * @param fullDateOfBirth data element
     * @param placeOfBirth data element
     * @param permanentAddress data element
     * @param telephone data element
     * @param profession data element
     * @param title data element
     * @param personalSummary data element
     * @param proofOfCitizenship data element
     * @param otherValidTDNumbers data element
     * @param custodyInformation data element
     */
    constructor(
        nameOfHolder: String?,
        otherNames: MutableList<String?>?, personalNumber: String?,
        fullDateOfBirth: String?, placeOfBirth: MutableList<String?>?, permanentAddress: MutableList<String?>?,
        telephone: String?, profession: String?, title: String?,
        personalSummary: String?, proofOfCitizenship: ByteArray?,
        otherValidTDNumbers: MutableList<String?>?, custodyInformation: String?
    ) : super(LDSFile.Companion.EF_DG11_TAG) {
        this.nameOfHolder = nameOfHolder
        this.otherNames = if (otherNames == null) null else ArrayList<String?>(otherNames)
        this.personalNumber = personalNumber
        this.fullDateOfBirth = fullDateOfBirth
        if (placeOfBirth == null) {
            this.placeOfBirth = null
        } else if (placeOfBirth.isEmpty()) {
            this.placeOfBirth = mutableListOf<String?>("")
        } else {
            this.placeOfBirth = ArrayList<String?>(placeOfBirth)
        }
        if (permanentAddress == null) {
            this.permanentAddress = null
        } else if (permanentAddress.isEmpty()) {
            this.permanentAddress = mutableListOf<String?>("")
        } else {
            this.permanentAddress = ArrayList<String?>(permanentAddress)
        }
        this.telephone = telephone
        this.profession = profession
        this.title = title
        this.personalSummary = personalSummary
        this.proofOfCitizenship = proofOfCitizenship
        if (otherValidTDNumbers == null) {
            this.otherValidTDNumbers = null
        } else if (otherValidTDNumbers.isEmpty()) {
            this.otherValidTDNumbers = mutableListOf<String?>("")
        } else {
            this.otherValidTDNumbers = ArrayList<String?>(otherValidTDNumbers)
        }
        this.custodyInformation = custodyInformation
    }

    val tag: Int
        /* Accessors below. */
        get() = LDSFile.Companion.EF_DG11_TAG

    /**
     * Returns the list of tags of fields actually present.
     * 
     * @return list of tags
     */
    public override fun getTagPresenceList(): MutableList<Int> {
        if (tagPresenceList != null) {
            return tagPresenceList!!
        }
        tagPresenceList = ArrayList<Int>(12)
        if (nameOfHolder != null) {
            tagPresenceList!!.add(FULL_NAME_TAG)
        }
        if (otherNames != null) {
            tagPresenceList!!.add(OTHER_NAME_TAG)
        }
        if (personalNumber != null) {
            tagPresenceList!!.add(PERSONAL_NUMBER_TAG)
        }
        if (fullDateOfBirth != null) {
            tagPresenceList!!.add(FULL_DATE_OF_BIRTH_TAG)
        }
        if (placeOfBirth != null) {
            tagPresenceList!!.add(PLACE_OF_BIRTH_TAG)
        }
        if (permanentAddress != null) {
            tagPresenceList!!.add(PERMANENT_ADDRESS_TAG)
        }
        if (telephone != null) {
            tagPresenceList!!.add(TELEPHONE_TAG)
        }
        if (profession != null) {
            tagPresenceList!!.add(PROFESSION_TAG)
        }
        if (title != null) {
            tagPresenceList!!.add(TITLE_TAG)
        }
        if (personalSummary != null) {
            tagPresenceList!!.add(PERSONAL_SUMMARY_TAG)
        }
        if (proofOfCitizenship != null) {
            tagPresenceList!!.add(PROOF_OF_CITIZENSHIP_TAG)
        }
        if (otherValidTDNumbers != null) {
            tagPresenceList!!.add(OTHER_VALID_TD_NUMBERS_TAG)
        }
        if (custodyInformation != null) {
            tagPresenceList!!.add(CUSTODY_INFORMATION_TAG)
        }
        return tagPresenceList!!
    }

    /**
     * Returns the other names.
     * 
     * @return the other names, or empty list when not present
     */
    fun getOtherNames(): MutableList<String>? {
        return if (otherNames == null) null else ArrayList<String>(otherNames)
    }

    /**
     * Returns a textual representation of this file.
     * 
     * @return a textual representation of this file
     */
    public override fun toString(): String {
        return StringBuilder()
            .append("DG11File [")
            .append(if (nameOfHolder == null) "" else nameOfHolder).append(", ")
            .append(if (otherNames == null || otherNames!!.isEmpty()) "[]" else otherNames).append(", ")
            .append(if (personalNumber == null) "" else personalNumber).append(", ")
            .append(if (fullDateOfBirth == null) "" else fullDateOfBirth).append(", ")
            .append(if (placeOfBirth == null || placeOfBirth!!.isEmpty()) "[]" else placeOfBirth.toString())
            .append(", ")
            .append(if (permanentAddress == null || permanentAddress!!.isEmpty()) "[]" else permanentAddress.toString())
            .append(", ")
            .append(if (telephone == null) "" else telephone).append(", ")
            .append(if (profession == null) "" else profession).append(", ")
            .append(if (title == null) "" else title).append(", ")
            .append(if (personalSummary == null) "" else personalSummary).append(", ")
            .append(if (proofOfCitizenship == null) "" else "image (" + proofOfCitizenship!!.size + ")").append(", ")
            .append(if (otherValidTDNumbers == null || otherValidTDNumbers!!.isEmpty()) "[]" else otherValidTDNumbers.toString())
            .append(", ")
            .append(if (custodyInformation == null) "" else custodyInformation)
            .append("]")
            .toString()
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + proofOfCitizenship.contentHashCode()
        result = prime * result + Objects.hash(
            custodyInformation, fullDateOfBirth, nameOfHolder, otherNames,
            otherValidTDNumbers, permanentAddress, personalNumber, personalSummary, placeOfBirth, profession,
            telephone, title
        )
        return result
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (obj == null) {
            return false
        }
        if (javaClass != obj.javaClass) {
            return false
        }

        val other = obj as DG11File
        return custodyInformation == other.custodyInformation
                && fullDateOfBirth == other.fullDateOfBirth
                && nameOfHolder == other.nameOfHolder
                && otherNames == other.otherNames
                && otherValidTDNumbers == other.otherValidTDNumbers
                && permanentAddress == other.permanentAddress
                && personalNumber == other.personalNumber
                && personalSummary == other.personalSummary
                && placeOfBirth == other.placeOfBirth
                && profession == other.profession
                && proofOfCitizenship.contentEquals(other.proofOfCitizenship) && tagPresenceList == other.tagPresenceList
                && telephone == other.telephone
                && title == other.title
    }

    /**
     * Reads a field from a stream.
     * 
     * @param expectedTag the tag to expect
     * @param tlvInputStream the stream to read from
     * 
     * @throws IOException on error reading from the stream
     */
    @Throws(IOException::class)
    override fun readField(expectedTag: Int, tlvInputStream: TLVInputStream) {
        val tag = tlvInputStream.readTag()
        require(!(tag != CONTENT_SPECIFIC_CONSTRUCTED_TAG && tag != expectedTag)) {
            "Expected " + Integer.toHexString(
                expectedTag
            ) + ", but found " + Integer.toHexString(tag)
        }
        tlvInputStream.readLength()
        when (tag) {
            FULL_NAME_TAG -> nameOfHolder = readString(tlvInputStream)
            CONTENT_SPECIFIC_CONSTRUCTED_TAG -> otherNames = readContentSpecificFieldsList(tlvInputStream)
            OTHER_NAME_TAG ->       /* Work around non-compliant early samples. */
                otherNames = mutableListOf<String?>(readString(tlvInputStream))

            PERSONAL_NUMBER_TAG -> personalNumber = readString(tlvInputStream)
            FULL_DATE_OF_BIRTH_TAG -> fullDateOfBirth = readFullDate(tlvInputStream)
            PLACE_OF_BIRTH_TAG -> placeOfBirth = readList(tlvInputStream)
            PERMANENT_ADDRESS_TAG -> permanentAddress = readList(tlvInputStream)
            TELEPHONE_TAG -> telephone = readString(tlvInputStream)
            PROFESSION_TAG -> profession = readString(tlvInputStream)
            TITLE_TAG -> title = readString(tlvInputStream)
            PERSONAL_SUMMARY_TAG -> personalSummary = readString(tlvInputStream)
            PROOF_OF_CITIZENSHIP_TAG -> proofOfCitizenship = readBytes(tlvInputStream)
            OTHER_VALID_TD_NUMBERS_TAG -> otherValidTDNumbers = readList(tlvInputStream)
            CUSTODY_INFORMATION_TAG -> custodyInformation = readString(tlvInputStream)
            else -> throw IllegalArgumentException("Unknown field tag in DG11: " + Integer.toHexString(tag))
        }
    }

    @Throws(IOException::class)
    override fun writeField(tag: Int, tlvOut: TLVOutputStream) {
        when (tag) {
            FULL_NAME_TAG -> writeString(tag, nameOfHolder, tlvOut)
            OTHER_NAME_TAG -> writeContentSpecificFieldsList(OTHER_NAME_TAG, otherNames, tlvOut)
            PERSONAL_NUMBER_TAG -> writeString(tag, personalNumber, tlvOut)
            FULL_DATE_OF_BIRTH_TAG -> writeString(tag, fullDateOfBirth, tlvOut)
            PLACE_OF_BIRTH_TAG -> writeList(tag, placeOfBirth, tlvOut)
            PERMANENT_ADDRESS_TAG -> writeList(tag, permanentAddress, tlvOut)
            TELEPHONE_TAG -> writeString(tag, telephone, tlvOut)
            PROFESSION_TAG -> writeString(tag, profession, tlvOut)
            TITLE_TAG -> writeString(tag, title, tlvOut)
            PERSONAL_SUMMARY_TAG -> writeString(tag, personalSummary, tlvOut)
            PROOF_OF_CITIZENSHIP_TAG -> {
                tlvOut.writeTag(tag)
                tlvOut.writeValue(proofOfCitizenship)
            }

            OTHER_VALID_TD_NUMBERS_TAG -> writeList(tag, otherValidTDNumbers, tlvOut)
            CUSTODY_INFORMATION_TAG -> writeString(tag, custodyInformation, tlvOut)
            else -> throw IllegalStateException("Unknown tag in DG11: " + Integer.toHexString(tag))
        }
    }

    companion object {
        private const val serialVersionUID = 8566312538928662937L

        const val TAG_LIST_TAG: Int = 0x5C

        const val FULL_NAME_TAG: Int = 0x5F0E
        const val OTHER_NAME_TAG: Int = 0x5F0F
        const val PERSONAL_NUMBER_TAG: Int = 0x5F10
        const val FULL_DATE_OF_BIRTH_TAG: Int = 0x5F2B // In 'CCYYMMDD' format.
        const val PLACE_OF_BIRTH_TAG: Int = 0x5F11 // Fields separated by '<'
        const val PERMANENT_ADDRESS_TAG: Int = 0x5F42 // Fields separated by '<'
        const val TELEPHONE_TAG: Int = 0x5F12
        const val PROFESSION_TAG: Int = 0x5F13
        const val TITLE_TAG: Int = 0x5F14
        const val PERSONAL_SUMMARY_TAG: Int = 0x5F15
        const val PROOF_OF_CITIZENSHIP_TAG: Int = 0x5F16 // Compressed image per ISO/IEC 10918
        const val OTHER_VALID_TD_NUMBERS_TAG: Int = 0x5F17 // Separated by '<'
        const val CUSTODY_INFORMATION_TAG: Int = 0x5F18

        private const val SDF = "yyyyMMdd"
    }
}
