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
 * $Id: FingerImageAnnotationBlock.java 1892 2025-03-18 15:15:52Z martijno $
 *
 * Based on ISO-IEC-39794-4-ed-1-v2. Disclaimer:
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
import org.jmrtd.ASN1Util
import java.util.*

class FingerImageAnnotationBlock : Block {
    enum class AnnotationReasonCode(val code: Int) : EncodableEnum<AnnotationReasonCode?> {
        UNKNOWN(0),
        OTHER(1),
        AMPUTATED(2),
        UNABLE_TO_PRINT(3),
        BANDAGED(4),
        PHYSICALLY_CHALLENGED(5),
        DISEASED(6);

        companion object {
            fun fromCode(code: Int): AnnotationReasonCode {
                return EncodableEnum.fromCode(code, AnnotationReasonCode::class.java)
            }
        }
    }

    val positionCode: FingerImagePositionCode

    val reasonCode: AnnotationReasonCode

    constructor(positionCode: FingerImagePositionCode, reasonCode: AnnotationReasonCode) {
        this.positionCode = positionCode
        this.reasonCode = reasonCode
    }

    //  AnnotationBlock ::= SEQUENCE {
    //    position [0] Position,
    //    reason [1] AnnotationReason,
    //    ...
    //  }
    internal constructor(asn1Encodable: ASN1Encodable?) {
        val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)
        positionCode =
            FingerImagePositionCode.fromCode(ISO39794Util.decodeCodeFromChoiceExtensionBlockFallback(taggedObjects.get(0)))
        reasonCode = AnnotationReasonCode.Companion.fromCode(
            ISO39794Util.decodeCodeFromChoiceExtensionBlockFallback(taggedObjects.get(1))
        )
    }

    public override fun hashCode(): Int {
        return Objects.hash(positionCode, reasonCode)
    }

    public override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (obj == null) {
            return false
        }
        if (javaClass != obj.javaClass) {
            return false
        }

        val other = obj as FingerImageAnnotationBlock
        return positionCode == other.positionCode && reasonCode == other.reasonCode
    }

    override fun toString(): String {
        return ("FingerImageAnnotationBlock ["
                + "positionCode: " + positionCode
                + ", reasonCode: " + reasonCode
                + "]")
    }

    val aSN1Object: ASN1Encodable?
        get() {
            val taggedObjects: MutableMap<Int?, ASN1Encodable?> =
                HashMap<Int?, ASN1Encodable?>()
            taggedObjects.put(
                0,
                ISO39794Util.encodeCodeAsChoiceExtensionBlockFallback(positionCode.code)
            )
            taggedObjects.put(
                1,
                ISO39794Util.encodeCodeAsChoiceExtensionBlockFallback(reasonCode.code)
            )
            return ASN1Util.encodeTaggedObjects(taggedObjects)
        }

    companion object {
        val serialversionuid: Long = -716107883353729322L

        /* PACKAGE */
        fun decodeFingerImageAnnotationBlocks(asn1Encodable: ASN1Encodable?): MutableList<FingerImageAnnotationBlock?> {
            if (ASN1Util.isSequenceOfSequences(asn1Encodable)) {
                val blockASN1Objects = ASN1Util.list(asn1Encodable)
                val blocks: MutableList<FingerImageAnnotationBlock?> =
                    ArrayList<FingerImageAnnotationBlock?>(blockASN1Objects.size)
                for (blockASN1Object in blockASN1Objects) {
                    blocks.add(FingerImageAnnotationBlock(blockASN1Object))
                }
                return blocks
            } else {
                return mutableListOf<FingerImageAnnotationBlock?>(FingerImageAnnotationBlock(asn1Encodable))
            }
        }
    }
}
