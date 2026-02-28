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
 * $Id: FingerImageSegmentationBlock.java 1889 2025-03-15 21:09:22Z martijno $
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
package org.jmrtd.lds.iso39794

import org.bouncycastle.asn1.ASN1Encodable
import org.jmrtd.ASN1Util
import org.jmrtd.lds.iso39794.RegistryIdBlock.Companion.from
import java.util.Objects

class FingerImageSegmentationBlock internal constructor(asn1Encodable: ASN1Encodable?) : Block() {
    //  SegmentationBlock ::= SEQUENCE {
    //    algorithmIdBlock [0] RegistryIdBlock,
    //    segmentBlocks [1] SegmentBlocks,
    //    ...
    //  }
    val algorithmIdBlock: RegistryIdBlock

    val segmentBlocks: MutableList<FingerImageSegmentBlock?>

    init {
        val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)
        algorithmIdBlock = from(taggedObjects[0])
        segmentBlocks = FingerImageSegmentBlock.decodeFingerImageSegmentBlocks(taggedObjects.get(1))
    }

    override fun hashCode(): Int {
        return Objects.hash(algorithmIdBlock, segmentBlocks)
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

        val other = obj as FingerImageSegmentationBlock
        return algorithmIdBlock == other.algorithmIdBlock
                && segmentBlocks == other.segmentBlocks
    }

    override fun toString(): String {
        return ("FingerImageSegmentationBlock ["
                + "algorithmIdBlock: " + algorithmIdBlock
                + ", segmentBlocks: " + segmentBlocks
                + "]")
    }

    override val aSN1Object: ASN1Encodable
        get() = ASN1Util.encodeTaggedObjects(
            mapOf(
                0 to algorithmIdBlock.aSN1Object,
                1 to ISO39794Util.encodeBlocks(segmentBlocks)
            )
        )
        /*get() {
            val taggedObjects: MutableMap<Int?, ASN1Encodable?> =
                HashMap<Int?, ASN1Encodable?>()
            taggedObjects[0] = algorithmIdBlock.aSN1Object
            taggedObjects[1] = ISO39794Util.encodeBlocks(segmentBlocks)
            return ASN1Util.encodeTaggedObjects(taggedObjects)
        }*/

    companion object {
        private const val serialVersionUID = -971841765544346186L

        /* PACKAGE */
        @JvmStatic
        fun decodeFingerImageSegmentationBlocks(asn1Encodable: ASN1Encodable?): List<FingerImageSegmentationBlock?> =
            if (ASN1Util.isSequenceOfSequences(asn1Encodable)) {
                ASN1Util.list(asn1Encodable).map {
                    FingerImageSegmentationBlock(it)
                }
            } else {
                listOf(FingerImageSegmentationBlock(asn1Encodable))
            }


        /*{
            if (ASN1Util.isSequenceOfSequences(asn1Encodable)) {
                val blockASN1Objects = ASN1Util.list(asn1Encodable)
                val blocks: MutableList<FingerImageSegmentationBlock?> =
                    ArrayList<FingerImageSegmentationBlock?>(blockASN1Objects.size)
                for (blockASN1Object in blockASN1Objects) {
                    blocks.add(FingerImageSegmentationBlock(blockASN1Object))
                }
                return blocks
            } else {
                return mutableListOf<FingerImageSegmentationBlock?>(
                    FingerImageSegmentationBlock(
                        asn1Encodable
                    )
                )
            }
        }*/
    }
}
