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
 * $Id: FaceImageIdentityMetadataBlock.java 1892 2025-03-18 15:15:52Z martijno $
 *
 * Based on ISO-IEC-39794-5-ed-1-v1. Disclaimer:
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

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.jmrtd.ASN1Util
import java.util.*

class FaceImageIdentityMetadataBlock : Block {
    enum class GenderCode(private val code: Int) : EncodableEnum<GenderCode?> {
        UNKNOWN(0),
        OTHER(1),
        MALE(2),
        FEMALE(3);

        override fun getCode(): Int {
            return code
        }

        companion object {
            fun fromCode(code: Int): GenderCode? {
                return EncodableEnum.fromCode<GenderCode?>(code, GenderCode::class.java)
            }
        }
    }

    enum class EyeColourCode(private val code: Int) : EncodableEnum<EyeColourCode?> {
        UNKNOWN(0),
        OTHER(1),
        BLACK(2),
        BLUE(3),
        BROWN(4),
        GREY(5),
        GREEN(6),
        HAZEL(7),
        MULTI_COLOURED(8),
        PINK(9);

        override fun getCode(): Int {
            return code
        }

        companion object {
            fun fromCode(code: Int): EyeColourCode? {
                return EncodableEnum.fromCode<EyeColourCode?>(code, EyeColourCode::class.java)
            }
        }
    }

    enum class HairColourCode(private val code: Int) : EncodableEnum<HairColourCode?> {
        UNKNOWN(0),
        OTHER(1),
        BALD(2),
        BLACK(3),
        BLONDE(4),
        BROWN(5),
        GREY(6),
        WHITE(7),
        RED(8),
        KNOWN_COLOURED(9);

        override fun getCode(): Int {
            return code
        }

        companion object {
            fun fromCode(code: Int): HairColourCode? {
                return EncodableEnum.fromCode<HairColourCode?>(code, HairColourCode::class.java)
            }
        }
    }

    var genderCode: GenderCode? = null
        private set

    var eyeColourCode: EyeColourCode? = null
        private set

    var hairColourCode: HairColourCode? = null
        private set

    var subjectHeight: Int = 0
        private set

    var propertiesBlock: FaceImagePropertiesBlock? = null
        private set

    var expressionBlock: FaceImageExpressionBlock? = null
        private set

    var poseAngleBlock: FaceImagePoseAngleBlock? = null
        private set

    constructor(
        genderCode: GenderCode?, eyeColourCode: EyeColourCode?,
        hairColourCode: HairColourCode?, subjectHeight: Int, propertiesBlock: FaceImagePropertiesBlock?,
        expressionBlock: FaceImageExpressionBlock?, poseAngleBlock: FaceImagePoseAngleBlock?
    ) {
        this.genderCode = genderCode
        this.eyeColourCode = eyeColourCode
        this.hairColourCode = hairColourCode
        this.subjectHeight = subjectHeight
        this.propertiesBlock = propertiesBlock
        this.expressionBlock = expressionBlock
        this.poseAngleBlock = poseAngleBlock
    }

    //  IdentityMetadataBlock ::= SEQUENCE {
    //    gender [0] Gender OPTIONAL,
    //    eyeColour [1] EyeColour OPTIONAL,
    //    hairColour [2] HairColour OPTIONAL,
    //    subjectHeight [3] SubjectHeight OPTIONAL,
    //    propertiesBlock [4] PropertiesBlock OPTIONAL,
    //    expressionBlock [5] ExpressionBlock OPTIONAL,
    //    poseAngleBlock [6] PoseAngleBlock OPTIONAL,
    //    ...
    //  }
    internal constructor(asn1Encodable: ASN1Encodable) {
        requireNotNull(asn1Encodable) { "Cannot decode null!" }
        require(!(asn1Encodable !is ASN1Sequence && asn1Encodable !is ASN1TaggedObject)) { "Cannot decode!" }

        val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)
        if (taggedObjects.containsKey(0)) {
            genderCode = GenderCode.Companion.fromCode(
                ISO39794Util.decodeCodeFromChoiceExtensionBlockFallback(
                    taggedObjects.get(0)
                )
            )
        }
        if (taggedObjects.containsKey(1)) {
            eyeColourCode = EyeColourCode.Companion.fromCode(
                ISO39794Util.decodeCodeFromChoiceExtensionBlockFallback(
                    taggedObjects.get(1)
                )
            )
        }
        if (taggedObjects.containsKey(2)) {
            hairColourCode = HairColourCode.Companion.fromCode(
                ISO39794Util.decodeCodeFromChoiceExtensionBlockFallback(
                    taggedObjects.get(2)
                )
            )
        }
        if (taggedObjects.containsKey(3)) {
            subjectHeight = ASN1Util.decodeInt(taggedObjects.get(3))
        }
        if (taggedObjects.containsKey(4)) {
            propertiesBlock = FaceImagePropertiesBlock(taggedObjects.get(4))
        }
        if (taggedObjects.containsKey(5)) {
            expressionBlock = FaceImageExpressionBlock(taggedObjects.get(5))
        }
        if (taggedObjects.containsKey(6)) {
            poseAngleBlock = FaceImagePoseAngleBlock(taggedObjects.get(6))
        }
    }

    override fun hashCode(): Int {
        return Objects.hash(
            expressionBlock, eyeColourCode, genderCode, hairColourCode, poseAngleBlock, propertiesBlock,
            subjectHeight
        )
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

        val other = obj as FaceImageIdentityMetadataBlock
        return expressionBlock == other.expressionBlock && eyeColourCode == other.eyeColourCode && genderCode == other.genderCode && hairColourCode == other.hairColourCode && poseAngleBlock == other.poseAngleBlock
                && propertiesBlock == other.propertiesBlock && subjectHeight == other.subjectHeight
    }

    override fun toString(): String {
        return ("FaceImageIdentityMetadataBlock ["
                + "genderCode: " + genderCode
                + ", eyeColourCode: " + eyeColourCode
                + ", hairColourCode: " + hairColourCode
                + ", subjectHeight: " + subjectHeight
                + ", propertiesBlock: " + propertiesBlock
                + ", expressionBlock: " + expressionBlock
                + ", poseAngleBlock: " + poseAngleBlock
                + "]")
    }

    /* PACKAGE */
    override fun getASN1Object(): ASN1Encodable? {
        val taggedObjects: MutableMap<Int?, ASN1Encodable?> = HashMap<Int?, ASN1Encodable?>()
        if (genderCode != null) {
            taggedObjects.put(0, ISO39794Util.encodeCodeAsChoiceExtensionBlockFallback(genderCode!!.getCode()))
        }
        if (eyeColourCode != null) {
            taggedObjects.put(1, ISO39794Util.encodeCodeAsChoiceExtensionBlockFallback(eyeColourCode!!.getCode()))
        }
        if (hairColourCode != null) {
            taggedObjects.put(2, ISO39794Util.encodeCodeAsChoiceExtensionBlockFallback(hairColourCode!!.getCode()))
        }
        if (subjectHeight >= 0) {
            taggedObjects.put(3, ASN1Util.encodeInt(subjectHeight))
        }
        if (propertiesBlock != null) {
            taggedObjects.put(4, propertiesBlock!!.getASN1Object())
        }
        if (expressionBlock != null) {
            taggedObjects.put(5, expressionBlock!!.getASN1Object())
        }
        if (poseAngleBlock != null) {
            taggedObjects.put(6, poseAngleBlock.getASN1Object())
        }
        return ASN1Util.encodeTaggedObjects(taggedObjects)
    }

    companion object {
        private const val serialVersionUID = 5968313840533997792L
    }
}
