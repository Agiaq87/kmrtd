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
 * $Id: FingerImageSegmentBlock.java 1889 2025-03-15 21:09:22Z martijno $
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

import kmrtd.lds.iso39794.CoordinateCartesian2DUnsignedShortBlock.Companion.decodeCoordinateCartesian2DUnsignedShortBlocks
import kmrtd.lds.iso39794.FingerImagePositionCode.Companion.fromCode
import org.bouncycastle.asn1.ASN1Encodable
import org.jmrtd.ASN1Util
import java.util.*

class FingerImageSegmentBlock : Block {
    val positionCode: FingerImagePositionCode
    val enclosingCoordinatesBlock: MutableList<CoordinateCartesian2DUnsignedShortBlock?>? // SIZE(2..MAX)
    var orientation: Int? = null
        private set
    var qualityBlocks: MutableList<QualityBlock?>? = null
        private set
    var confidence: Int = 0
        private set

    constructor(
        positionCode: FingerImagePositionCode,
        enclosingCoordinatesBlock: MutableList<CoordinateCartesian2DUnsignedShortBlock?>?, orientation: Int?,
        qualityBlocks: MutableList<QualityBlock?>?, confidence: Int
    ) {
        this.positionCode = positionCode
        this.enclosingCoordinatesBlock = enclosingCoordinatesBlock
        this.orientation = orientation
        this.qualityBlocks = qualityBlocks
        this.confidence = confidence
    }

    //    SegmentBlock ::= SEQUENCE {
    //      position [0] Position,
    //      enclosingCoordinatesBlock [1] CoordinatesBlock,
    //      orientation [2] INTEGER (0..255) OPTIONAL,
    //      qualityBlocks [3] QualityBlocks OPTIONAL,
    //      confidence [4] ScoreOrError OPTIONAL,
    //      ...
    //  }
    internal constructor(asn1Encodable: ASN1Encodable?) {
        val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)
        positionCode = fromCode(ISO39794Util.decodeCodeFromChoiceExtensionBlockFallback(taggedObjects.get(0)))
        enclosingCoordinatesBlock = decodeCoordinateCartesian2DUnsignedShortBlocks(taggedObjects.get(1))
        if (taggedObjects.containsKey(2)) {
            orientation = ASN1Util.decodeInt(taggedObjects.get(2))
        }
        if (taggedObjects.containsKey(3)) {
            qualityBlocks = QualityBlock.decodeQualityBlocks(taggedObjects.get(3))
        }
        if (taggedObjects.containsKey(4)) {
            confidence = ISO39794Util.decodeScoreOrError(taggedObjects.get(4))
        }
    }

    public override fun hashCode(): Int {
        return Objects.hash(confidence, enclosingCoordinatesBlock, orientation, positionCode, qualityBlocks)
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

        val other = obj as FingerImageSegmentBlock
        return confidence == other.confidence && enclosingCoordinatesBlock == other.enclosingCoordinatesBlock
                && orientation == other.orientation && positionCode == other.positionCode && qualityBlocks == other.qualityBlocks
    }

    override fun toString(): String {
        return ("FingerImageSegmentBlock ["
                + "positionCode: " + positionCode
                + ", enclosingCoordinatesBlock: " + enclosingCoordinatesBlock
                + ", orientation: " + orientation
                + ", qualityBlocks: " + qualityBlocks
                + ", confidence: " + confidence
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
            taggedObjects.put(1, ISO39794Util.encodeBlocks(enclosingCoordinatesBlock))
            if (orientation != null) {
                taggedObjects.put(2, ASN1Util.encodeInt(orientation!!))
            }
            if (qualityBlocks != null) {
                taggedObjects.put(3, ISO39794Util.encodeBlocks(qualityBlocks))
            }
            if (confidence >= 0) {
                taggedObjects.put(4, ISO39794Util.encodeScoreOrError(confidence))
            }
            return ASN1Util.encodeTaggedObjects(taggedObjects)
        }

    companion object {
        private val serialVersionUID = -374626239054691564L

        /* PACAKAGE */
        fun decodeFingerImageSegmentBlocks(asn1Encodable: ASN1Encodable?): MutableList<FingerImageSegmentBlock?> {
            if (ASN1Util.isSequenceOfSequences(asn1Encodable)) {
                val blockASN1Objects = ASN1Util.list(asn1Encodable)
                val blocks: MutableList<FingerImageSegmentBlock?> =
                    ArrayList<FingerImageSegmentBlock?>(blockASN1Objects.size)
                for (blockASN1Object in blockASN1Objects) {
                    blocks.add(FingerImageSegmentBlock(blockASN1Object))
                }
                return blocks
            } else {
                return mutableListOf<FingerImageSegmentBlock?>(FingerImageSegmentBlock(asn1Encodable))
            }
        }
    }
}