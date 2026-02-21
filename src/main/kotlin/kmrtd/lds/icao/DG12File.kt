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
 * $Id: DG12File.java 1907 2026-02-06 09:24:02Z martijno $
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
 * File structure for the EF_DG12 file.
 * Datagroup 12 contains additional document detail(s).
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1907 $
 */
class DG12File : AdditionalDetailDataGroup {
    /**
     * Returns the issuing authority.
     * 
     * @return the issuingAuthority
     */
    var issuingAuthority: String? = null
        private set

    /**
     * Returns the date of issuance.
     * 
     * @return the dateOfIssue
     */
    var dateOfIssue: String? = null
        private set

    /**
     * Returns name of other person.
     * 
     * @return the nameOfOtherPerson
     */
    var namesOfOtherPersons: MutableList<String?>? = null
        private set

    /**
     * Returns endorsements and observations.
     * 
     * @return the endorsementsAndObservations
     */
    var endorsementsAndObservations: String? = null
        private set

    /**
     * Returns tax or exit requirements.
     * 
     * @return the taxOrExitRequirements
     */
    var taxOrExitRequirements: String? = null
        private set

    /**
     * Returns image of front.
     * 
     * @return the imageOfFront
     */
    var imageOfFront: ByteArray?
        private set

    /**
     * Returns image of rear.
     * 
     * @return the imageOfRear
     */
    var imageOfRear: ByteArray?
        private set

    /**
     * Returns the date and time of personalization.
     * 
     * @return the dateAndTimeOfPersonalization
     */
    var dateAndTimeOfPersonalization: String? = null
        private set

    /**
     * Returns the personalization system serial number.
     * 
     * @return the personalizationSystemSerialNumber
     */
    var personalizationSystemSerialNumber: String? = null
        private set

    private var tagPresenceList: MutableList<Int?>? = null

    /**
     * Constructs a new file.
     * 
     * @param issuingAuthority the issuing authority
     * @param dateOfIssue the date of issue
     * @param namesOfOtherPersons names of other persons
     * @param endorsementsAndObservations endorsements and observations
     * @param taxOrExitRequirements tax or exit requirements
     * @param imageOfFront image of front
     * @param imageOfRear image of rear
     * @param dateAndTimeOfPersonalization date and time of personalization
     * @param personalizationSystemSerialNumber personalization system serial number
     */
    constructor(
        issuingAuthority: String?, dateOfIssue: Date?,
        namesOfOtherPersons: MutableList<String?>?, endorsementsAndObservations: String?,
        taxOrExitRequirements: String?, imageOfFront: ByteArray?,
        imageOfRear: ByteArray?, dateAndTimeOfPersonalization: Date?,
        personalizationSystemSerialNumber: String?
    ) : this(
        issuingAuthority,
        if (dateOfIssue == null) null else SimpleDateFormat(SDF).format(dateOfIssue),
        namesOfOtherPersons, endorsementsAndObservations,
        taxOrExitRequirements, imageOfFront,
        imageOfRear,
        if (dateAndTimeOfPersonalization == null) null else SimpleDateFormat(SDTF).format(dateAndTimeOfPersonalization),
        personalizationSystemSerialNumber
    )

    /**
     * Constructs a new file.
     * 
     * @param issuingAuthority the issuing authority
     * @param dateOfIssue the date of issue
     * @param namesOfOtherPersons names of other persons
     * @param endorsementsAndObservations endorsements and observations
     * @param taxOrExitRequirements tax or exit requirements
     * @param imageOfFront image of front
     * @param imageOfRear image of rear
     * @param dateAndTimeOfPersonalization date and time of personalization
     * @param personalizationSystemSerialNumber personalization system serial number
     */
    constructor(
        issuingAuthority: String?, dateOfIssue: String?,
        namesOfOtherPersons: MutableList<String?>?, endorsementsAndObservations: String?,
        taxOrExitRequirements: String?, imageOfFront: ByteArray?,
        imageOfRear: ByteArray?, dateAndTimeOfPersonalization: String?,
        personalizationSystemSerialNumber: String?
    ) : super(LDSFile.Companion.EF_DG12_TAG) {
        this.issuingAuthority = issuingAuthority
        this.dateOfIssue = dateOfIssue
        this.namesOfOtherPersons = if (namesOfOtherPersons == null) null else ArrayList<String?>(namesOfOtherPersons)
        this.endorsementsAndObservations = endorsementsAndObservations
        this.taxOrExitRequirements = taxOrExitRequirements
        this.imageOfFront = imageOfFront
        this.imageOfRear = imageOfRear
        this.dateAndTimeOfPersonalization = dateAndTimeOfPersonalization
        this.personalizationSystemSerialNumber = personalizationSystemSerialNumber
    }

    /**
     * Constructs a new file.
     * 
     * @param inputStream an input stream
     * 
     * @throws IOException on error reading from input stream
     */
    constructor(inputStream: InputStream?) : super(LDSFile.Companion.EF_DG12_TAG, inputStream)

    /**
     * Returns the tags of fields actually present in this file.
     * 
     * @return a list of tags
     */
    public override fun getTagPresenceList(): MutableList<Int?> {
        if (tagPresenceList != null) {
            return tagPresenceList!!
        }
        tagPresenceList = ArrayList<Int?>(10)
        if (issuingAuthority != null) {
            tagPresenceList!!.add(ISSUING_AUTHORITY_TAG)
        }
        if (dateOfIssue != null) {
            tagPresenceList!!.add(DATE_OF_ISSUE_TAG)
        }
        if (namesOfOtherPersons != null) {
            tagPresenceList!!.add(NAME_OF_OTHER_PERSON_TAG)
        }
        if (endorsementsAndObservations != null) {
            tagPresenceList!!.add(ENDORSEMENTS_AND_OBSERVATIONS_TAG)
        }
        if (taxOrExitRequirements != null) {
            tagPresenceList!!.add(TAX_OR_EXIT_REQUIREMENTS_TAG)
        }
        if (imageOfFront != null) {
            tagPresenceList!!.add(IMAGE_OF_FRONT_TAG)
        }
        if (imageOfRear != null) {
            tagPresenceList!!.add(IMAGE_OF_REAR_TAG)
        }
        if (dateAndTimeOfPersonalization != null) {
            tagPresenceList!!.add(DATE_AND_TIME_OF_PERSONALIZATION_TAG)
        }
        if (personalizationSystemSerialNumber != null) {
            tagPresenceList!!.add(PERSONALIZATION_SYSTEM_SERIAL_NUMBER_TAG)
        }
        return tagPresenceList!!
    }

    val tag: Int
        get() = LDSFile.Companion.EF_DG12_TAG

    /**
     * Returns a textual representation of this file.
     * 
     * @return a textual representation of this file
     */
    public override fun toString(): String {
        return StringBuilder()
            .append("DG12File [")
            .append(if (issuingAuthority == null) "" else issuingAuthority).append(", ")
            .append(if (dateOfIssue == null) "" else dateOfIssue).append(", ")
            .append(if (namesOfOtherPersons == null || namesOfOtherPersons!!.isEmpty()) "[]" else namesOfOtherPersons)
            .append(", ")
            .append(if (endorsementsAndObservations == null) "" else endorsementsAndObservations).append(", ")
            .append(if (taxOrExitRequirements == null) "" else taxOrExitRequirements).append(", ")
            .append(if (imageOfFront == null) "" else "image (" + imageOfFront!!.size + ")").append(", ")
            .append(if (imageOfRear == null) "" else "image (" + imageOfRear!!.size + ")").append(", ")
            .append(if (dateAndTimeOfPersonalization == null) "" else dateAndTimeOfPersonalization).append(", ")
            .append(if (personalizationSystemSerialNumber == null) "" else personalizationSystemSerialNumber)
            .append("]")
            .toString()
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + imageOfFront.contentHashCode()
        result = prime * result + imageOfRear.contentHashCode()
        result = (prime * result
                + Objects.hash(
            dateAndTimeOfPersonalization, dateOfIssue, endorsementsAndObservations, issuingAuthority,
            namesOfOtherPersons, personalizationSystemSerialNumber, taxOrExitRequirements
        ))
        return result
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) return true
        if (obj == null) return false
        if (javaClass != obj.javaClass) return false
        val other = obj as DG12File
        return dateAndTimeOfPersonalization == other.dateAndTimeOfPersonalization
                && dateOfIssue == other.dateOfIssue
                && endorsementsAndObservations == other.endorsementsAndObservations
                && imageOfFront.contentEquals(other.imageOfFront) && imageOfRear.contentEquals(other.imageOfRear) && issuingAuthority == other.issuingAuthority
                && namesOfOtherPersons == other.namesOfOtherPersons
                && personalizationSystemSerialNumber == other.personalizationSystemSerialNumber
                && taxOrExitRequirements == other.taxOrExitRequirements
    }

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
            ISSUING_AUTHORITY_TAG -> issuingAuthority = readString(tlvInputStream)
            DATE_OF_ISSUE_TAG -> dateOfIssue = readFullDate(tlvInputStream)
            CONTENT_SPECIFIC_CONSTRUCTED_TAG -> namesOfOtherPersons = readContentSpecificFieldsList(tlvInputStream)
            NAME_OF_OTHER_PERSON_TAG ->       /* Work around non-compliant early samples. */
                namesOfOtherPersons = mutableListOf<String?>(readString(tlvInputStream))

            ENDORSEMENTS_AND_OBSERVATIONS_TAG -> endorsementsAndObservations = readString(tlvInputStream)
            TAX_OR_EXIT_REQUIREMENTS_TAG -> taxOrExitRequirements = readString(tlvInputStream)
            IMAGE_OF_FRONT_TAG -> imageOfFront = readBytes(tlvInputStream)
            IMAGE_OF_REAR_TAG -> imageOfRear = readBytes(tlvInputStream)
            DATE_AND_TIME_OF_PERSONALIZATION_TAG -> dateAndTimeOfPersonalization = readString(tlvInputStream)
            PERSONALIZATION_SYSTEM_SERIAL_NUMBER_TAG -> personalizationSystemSerialNumber = readString(tlvInputStream)
            else -> throw IllegalArgumentException("Unknown field tag in DG12: " + Integer.toHexString(tag))
        }
    }

    @Throws(IOException::class)
    override fun writeField(tag: Int, tlvOut: TLVOutputStream) {
        when (tag) {
            ISSUING_AUTHORITY_TAG -> {
                writeString(tag, issuingAuthority, tlvOut)
            }

            DATE_OF_ISSUE_TAG -> writeString(tag, dateOfIssue, tlvOut)
            NAME_OF_OTHER_PERSON_TAG -> writeContentSpecificFieldsList(tag, namesOfOtherPersons, tlvOut)
            ENDORSEMENTS_AND_OBSERVATIONS_TAG -> writeString(tag, endorsementsAndObservations, tlvOut)
            TAX_OR_EXIT_REQUIREMENTS_TAG -> writeString(tag, taxOrExitRequirements, tlvOut)
            IMAGE_OF_FRONT_TAG -> writeBytes(tag, imageOfFront, tlvOut)
            IMAGE_OF_REAR_TAG -> writeBytes(tag, imageOfRear, tlvOut)
            DATE_AND_TIME_OF_PERSONALIZATION_TAG -> writeString(tag, dateAndTimeOfPersonalization, tlvOut)
            PERSONALIZATION_SYSTEM_SERIAL_NUMBER_TAG -> writeString(tag, personalizationSystemSerialNumber, tlvOut)
            else -> throw IllegalArgumentException("Unknown field tag in DG12: " + Integer.toHexString(tag))
        }
    }

    companion object {
        private val serialVersionUID = -1979367459379125674L

        const val ISSUING_AUTHORITY_TAG: Int = 0x5F19
        const val DATE_OF_ISSUE_TAG: Int = 0x5F26 // yyyymmdd
        const val NAME_OF_OTHER_PERSON_TAG: Int = 0x5F1A // formatted per ICAO 9303 rules
        const val ENDORSEMENTS_AND_OBSERVATIONS_TAG: Int = 0x5F1B
        const val TAX_OR_EXIT_REQUIREMENTS_TAG: Int = 0x5F1C
        const val IMAGE_OF_FRONT_TAG: Int = 0x5F1D // Image per ISO/IEC 10918
        const val IMAGE_OF_REAR_TAG: Int = 0x5F1E // Image per ISO/IEC 10918
        const val DATE_AND_TIME_OF_PERSONALIZATION_TAG: Int = 0x5F55 // yyyymmddhhmmss
        const val PERSONALIZATION_SYSTEM_SERIAL_NUMBER_TAG: Int = 0x5F56
        const val CONTENT_SPECIFIC_CONSTRUCTED_TAG: Int = 0xA0 // 5F1A is always used inside A0 constructed object
        const val COUNT_TAG: Int = 0x02 // Used in A0 constructed object to indicate single byte count of simple objects

        private const val SDF = "yyyyMMdd"
        private const val SDTF = "yyyyMMddhhmmss"
    }
}
