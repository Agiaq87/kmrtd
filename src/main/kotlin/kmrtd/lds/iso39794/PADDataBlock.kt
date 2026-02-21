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
 * $Id: PADDataBlock.java 1892 2025-03-18 15:15:52Z martijno $
 *
 * Based on ISO-IEC-39794-1-ed-1-v1. Disclaimer:
 * THE SCHEMA ON WHICH THIS SOFTWARE IS BASED IS PROVIDED BY THE COPYRIGHT
 * HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THE CODE COMPONENTS, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.lds.iso39794

import net.sf.scuba.util.Hex
import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERSequence
import org.jmrtd.ASN1Util
import java.util.*

class PADDataBlock : Block {
    enum class PADDecisionCode(private val code: Int) : EncodableEnum<PADDecisionCode?> {
        NO_ATTACK(0),
        ATTACK(1),
        FAILURE_TO_ASSESS(2);

        override fun getCode(): Int {
            return code
        }

        companion object {
            fun fromCode(code: Int): PADDecisionCode? {
                return EncodableEnum.fromCode<PADDecisionCode?>(code, PADDecisionCode::class.java)
            }
        }
    }

    enum class PADCaptureContextCode(private val code: Int) : EncodableEnum<PADCaptureContextCode?> {
        ENROLMENT(0),
        VERIFICATION(1),
        IDENTIFICATION(2);

        override fun getCode(): Int {
            return code
        }

        companion object {
            fun fromCode(code: Int): PADCaptureContextCode? {
                return EncodableEnum.fromCode<PADCaptureContextCode?>(code, PADCaptureContextCode::class.java)
            }
        }
    }

    enum class PADSupervisionLevelCode(private val code: Int) : EncodableEnum<PADSupervisionLevelCode?> {
        UNKNOWN(0),
        CONTROLLED(1),
        ASSISTED(2),
        OBSERVED(3),
        UNATTENDED(4);

        override fun getCode(): Int {
            return code
        }

        companion object {
            fun fromCode(code: Int): PADSupervisionLevelCode? {
                return EncodableEnum.fromCode<PADSupervisionLevelCode?>(code, PADSupervisionLevelCode::class.java)
            }
        }
    }

    enum class PADCriteriaCategoryCode(private val code: Int) : EncodableEnum<PADCriteriaCategoryCode?> {
        UNKNOWN(0),
        INDIVIDUAL(1),
        COMMON(2);

        override fun getCode(): Int {
            return code
        }

        companion object {
            fun fromCode(code: Int): PADCriteriaCategoryCode? {
                return EncodableEnum.fromCode<PADCriteriaCategoryCode?>(code, PADCriteriaCategoryCode::class.java)
            }
        }
    }

    var pADDecisionCode: PADDecisionCode? = null
        private set

    var padScoreBlocks: MutableList<PADScoreBlock?>? = null
        private set

    var padExtendedDataBlocks: MutableList<ExtendedDataBlock?>? = null
        private set

    var pADCaptureContextCode: PADCaptureContextCode? = null
        private set

    var pADSupervisionLevelCode: PADSupervisionLevelCode? = null
        private set

    /** INTEGER (0..100).  */
    var riskLevel: Int = 0
        private set

    var pADCriteriaCategoryCode: PADCriteriaCategoryCode? = null
        private set

    var parameter: ByteArray?
        private set

    var challenges: MutableList<ByteArray>? = null
        private set

    var captureDateTimeBlock: DateTimeBlock? = null
        private set

    constructor(
        padDecisionCode: PADDecisionCode?,
        padScoreBlocks: MutableList<PADScoreBlock?>?,
        padExtendedDataBlocks: MutableList<ExtendedDataBlock?>?,
        padCaptureContextCode: PADCaptureContextCode?,
        padSupervisionLevelCode: PADSupervisionLevelCode?,
        riskLevel: Int,
        padCriteriaCategoryCode: PADCriteriaCategoryCode?,
        parameter: ByteArray?,
        challenges: MutableList<ByteArray>?,
        captureDateTimeBlock: DateTimeBlock?
    ) {
        this.pADDecisionCode = padDecisionCode
        this.padScoreBlocks = padScoreBlocks
        this.padExtendedDataBlocks = padExtendedDataBlocks
        this.pADCaptureContextCode = padCaptureContextCode
        this.pADSupervisionLevelCode = padSupervisionLevelCode
        this.riskLevel = riskLevel
        this.pADCriteriaCategoryCode = padCriteriaCategoryCode
        this.parameter = parameter
        this.challenges = challenges
        this.captureDateTimeBlock = captureDateTimeBlock
    }

    //  PADDataBlock ::= SEQUENCE {
    //    decision              [0] PADDecision            OPTIONAL,
    //    scoreBlocks           [1] PADScoreBlocks         OPTIONAL,
    //    extendedDataBlocks    [2] PADExtendedDataBlocks  OPTIONAL,
    //    captureContext        [3] PADCaptureContext      OPTIONAL,
    //    supervisionLevel      [4] PADSupervisionLevel    OPTIONAL,
    //    riskLevel             [5] PADRiskLevel           OPTIONAL,
    //    criteriaCategory      [6] PADCriteriaCategory    OPTIONAL,
    //    parameter             [7] OCTET STRING           OPTIONAL,
    //    challenges            [8] PADChallenges          OPTIONAL,
    //    captureDateTimeBlock  [9] CaptureDateTimeBlock   OPTIONAL,
    //    ...
    //  }
    internal constructor(asn1Encodable: ASN1Encodable?) {
        val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)
        if (taggedObjects.containsKey(0)) {
            this.pADDecisionCode = PADDecisionCode.Companion.fromCode(
                ISO39794Util.decodeCodeFromChoiceExtensionBlockFallback(
                    taggedObjects.get(0)
                )
            )
        }
        if (taggedObjects.containsKey(1)) {
            padScoreBlocks = PADScoreBlock.decodePADScoreBlocks(taggedObjects.get(1))
        }
        if (taggedObjects.containsKey(2)) {
            padExtendedDataBlocks = ExtendedDataBlock.decodeExtendedDataBlocks(taggedObjects.get(2))
        }
        if (taggedObjects.containsKey(3)) {
            this.pADCaptureContextCode = PADCaptureContextCode.Companion.fromCode(
                ISO39794Util.decodeCodeFromChoiceExtensionBlockFallback(taggedObjects.get(3))
            )
        }
        if (taggedObjects.containsKey(4)) {
            this.pADSupervisionLevelCode = PADSupervisionLevelCode.Companion.fromCode(
                ISO39794Util.decodeCodeFromChoiceExtensionBlockFallback(taggedObjects.get(4))
            )
        }
        if (taggedObjects.containsKey(5)) {
            riskLevel = ASN1Util.decodeInt(taggedObjects.get(5))
        }
        if (taggedObjects.containsKey(6)) {
            this.pADCriteriaCategoryCode = PADCriteriaCategoryCode.Companion.fromCode(
                ISO39794Util.decodeCodeFromChoiceExtensionBlockFallback(taggedObjects.get(6))
            )
        }
        if (taggedObjects.containsKey(7)) {
            parameter = ASN1OctetString.getInstance(taggedObjects.get(7)).getOctets()
        }
        if (taggedObjects.containsKey(8)) {
            challenges = decodePADChallenges(taggedObjects.get(8))
        }
        if (taggedObjects.containsKey(9)) {
            captureDateTimeBlock = DateTimeBlock(taggedObjects.get(9))
        }
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + parameter.contentHashCode()
        result = (prime * result
                + Objects.hash(
            captureDateTimeBlock, challenges,
            this.pADCaptureContextCode,
            this.pADCriteriaCategoryCode,
            this.pADDecisionCode, padExtendedDataBlocks, padScoreBlocks,
            this.pADSupervisionLevelCode, riskLevel
        ))
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

        val other = obj as PADDataBlock
        return captureDateTimeBlock == other.captureDateTimeBlock
                && equalBytes(challenges, other.challenges)
                && this.pADCaptureContextCode == other.pADCaptureContextCode && this.pADCriteriaCategoryCode == other.pADCriteriaCategoryCode && this.pADDecisionCode == other.pADDecisionCode && padExtendedDataBlocks == other.padExtendedDataBlocks
                && padScoreBlocks == other.padScoreBlocks
                && this.pADSupervisionLevelCode == other.pADSupervisionLevelCode && parameter.contentEquals(other.parameter) && riskLevel == other.riskLevel
    }

    override fun toString(): String {
        return ("PADDataBlock [padDecisionCode: " + this.pADDecisionCode
                + ", padScoreBlocks: " + padScoreBlocks
                + ", padExtendedDataBlocks: " + padExtendedDataBlocks
                + ", padCaptureContextCode: " + this.pADCaptureContextCode
                + ", padSupervisionLevelCode: " + this.pADSupervisionLevelCode
                + ", riskLevel: " + riskLevel
                + ", padCriteriaCategoryCode: " + this.pADCriteriaCategoryCode
                + ", parameter: " + Hex.bytesToHexString(parameter)
                + ", challenges: " + toString(challenges)
                + ", captureDateTimeBlock: " + captureDateTimeBlock + "]")
    }

    override fun getASN1Object(): ASN1Encodable? {
        val taggedObjects: MutableMap<Int?, ASN1Encodable?> = HashMap<Int?, ASN1Encodable?>()
        if (this.pADDecisionCode != null) {
            taggedObjects.put(
                0, ISO39794Util.encodeCodeAsChoiceExtensionBlockFallback(
                    pADDecisionCode!!.getCode()
                )
            )
        }
        if (padScoreBlocks != null) {
            taggedObjects.put(1, ISO39794Util.encodeBlocks(padScoreBlocks))
        }
        if (padExtendedDataBlocks != null) {
            taggedObjects.put(2, ISO39794Util.encodeBlocks(padExtendedDataBlocks))
        }
        if (this.pADCaptureContextCode != null) {
            taggedObjects.put(
                3, ISO39794Util.encodeCodeAsChoiceExtensionBlockFallback(
                    pADCaptureContextCode!!.getCode()
                )
            )
        }
        if (this.pADSupervisionLevelCode != null) {
            taggedObjects.put(
                4, ISO39794Util.encodeCodeAsChoiceExtensionBlockFallback(
                    pADSupervisionLevelCode!!.getCode()
                )
            )
        }
        if (riskLevel >= 0) {
            taggedObjects.put(5, ASN1Util.encodeInt(riskLevel))
        }
        if (this.pADCriteriaCategoryCode != null) {
            taggedObjects.put(
                6, ISO39794Util.encodeCodeAsChoiceExtensionBlockFallback(
                    pADCriteriaCategoryCode!!.getCode()
                )
            )
        }
        if (parameter != null) {
            taggedObjects.put(7, DEROctetString(parameter))
        }
        if (challenges != null) {
            taggedObjects.put(8, Companion.encodePADChallenges(challenges!!))
        }
        if (captureDateTimeBlock != null) {
            taggedObjects.put(9, captureDateTimeBlock!!.getASN1Object())
        }
        return ASN1Util.encodeTaggedObjects(taggedObjects)
    }

    companion object {
        private const val serialVersionUID = 1498548397505331884L

        /* PACKAGE */
        fun decodePADDataBlocks(asn1Encodable: ASN1Encodable?): MutableList<PADDataBlock?> {
            if (ASN1Util.isSequenceOfSequences(asn1Encodable)) {
                val blockASN1Objects = ASN1Util.list(asn1Encodable)
                val blocks: MutableList<PADDataBlock?> = ArrayList<PADDataBlock?>(blockASN1Objects.size)
                for (blockASN1Object in blockASN1Objects) {
                    blocks.add(PADDataBlock(blockASN1Object))
                }
                return blocks
            } else {
                val block = PADDataBlock(asn1Encodable)
                return mutableListOf<PADDataBlock?>(block)
            }
        }

        /* PRIVATE */
        private fun decodePADChallenges(asn1Encodable: ASN1Encodable?): MutableList<ByteArray> {
            val challengeASN1Objects = ASN1Util.list(asn1Encodable)
            val padChallenges: MutableList<ByteArray> = ArrayList<ByteArray>(challengeASN1Objects.size)
            for (challengeASN1Object in challengeASN1Objects) {
                padChallenges.add(ASN1OctetString.getInstance(challengeASN1Object).getOctets())
            }
            return padChallenges
        }

        private fun encodePADChallenges(padChallenges: MutableList<ByteArray>): ASN1Encodable {
            val asn1Encodables = arrayOfNulls<ASN1Encodable>(padChallenges.size)
            var i = 0
            for (padChallenge in padChallenges) {
                asn1Encodables[i++] = DEROctetString(padChallenge)
            }
            return DERSequence(asn1Encodables)
        }

        private fun equalBytes(challenges1: MutableList<ByteArray>?, challenges2: MutableList<ByteArray>?): Boolean {
            if (challenges1 == challenges2) {
                return true
            }
            if (challenges1 == null && challenges2 != null) {
                return false
            }
            if (challenges1 != null && challenges2 == null) {
                return false
            }
            if (challenges1!!.size != challenges2!!.size) {
                return false
            }
            val length = challenges1.size
            for (i in 0..<length) {
                if (!(challenges1.get(i).contentEquals(challenges2.get(i)))) {
                    return false
                }
            }
            return true
        }

        private fun toString(challenges: MutableList<ByteArray>?): String {
            if (challenges == null) {
                return "null"
            }
            var isFirst = true
            val stringBuilder = StringBuilder().append("[")
            for (challenge in challenges) {
                if (isFirst) {
                    isFirst = false
                } else {
                    stringBuilder.append(", ")
                }
                stringBuilder.append(Hex.bytesToHexString(challenge))
            }
            return stringBuilder.append("]").toString()
        }
    }
}
