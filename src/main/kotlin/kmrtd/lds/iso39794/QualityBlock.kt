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
 * $Id: QualityBlock.java 1892 2025-03-18 15:15:52Z martijno $
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
package org.jmrtd.lds.iso39794

import org.bouncycastle.asn1.ASN1Encodable
import org.jmrtd.ASN1Util

data class QualityBlock(
    val algorithmIdBlock: RegistryIdBlock,
    val score: Int
) : Block() {
/*
    internal constructor() {

    }

    override fun hashCode(): Int {
        return Objects.hash(algorithmIdBlock, score)
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

        val other = obj as QualityBlock
        return algorithmIdBlock == other.algorithmIdBlock && score == other.score
    }

    override fun toString(): String {
        return ("QualityBlock ["
                + "algorithmIdBlock: " + algorithmIdBlock
                + ", score: " + score
                + "]")
    }*/

    override val aSN1Object: ASN1Encodable
        /*get() {
            val taggedObjects: MutableMap<Int, ASN1Encodable> = mutableMapOf()
                //HashMap<Int, ASN1Encodable>()
            taggedObjects[0] = algorithmIdBlock.aSN1Object
            if (score >= 0) {
                taggedObjects[1] = ISO39794Util.encodeScoreOrError(score)
            }
            return ASN1Util.encodeTaggedObjects(taggedObjects)
        }*/
        get() = ASN1Util.encodeTaggedObjects(
            buildMap {
                put(0, algorithmIdBlock.aSN1Object)
                if (score >= 0) {
                    put(1, ISO39794Util.encodeScoreOrError(score))
                }
            }
        )

    companion object {
        private const val serialVersionUID = 8529221328304209845L

        //  QualityBlocks ::= SEQUENCE OF QualityBlock
        @JvmStatic
        fun decodeQualityBlocks(asn1Encodable: ASN1Encodable?): List<QualityBlock> =
            if (ASN1Util.isSequenceOfSequences(asn1Encodable)) {
                ASN1Util.list(asn1Encodable).map { from(it) }
            } else {
                listOf(from(asn1Encodable))
            }
            /*if (ASN1Util.isSequenceOfSequences(asn1Encodable)) {
                val blockASN1Objects = ASN1Util.list(asn1Encodable)
                val blocks: MutableList<QualityBlock> = mutableListOf()
                for (blockASN1Object in blockASN1Objects) {
                    blocks.add(QualityBlock.from(blockASN1Object))
                }
                return blocks
            } else {
                return mutableListOf(QualityBlock.from(asn1Encodable))
            }*/

        /**
         * Factory method
         *
         * QualityBlock ::= SEQUENCE {
         *   algorithmIdBlock                [0] RegistryIdBlock,
         *   scoreOrError                    [1] ScoreOrError,
         *   ...
         * }
         */
        @JvmStatic
        fun from(asn1Encodable: ASN1Encodable?): QualityBlock {
            val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)

            return QualityBlock(
                algorithmIdBlock = RegistryIdBlock.from(taggedObjects[0]),
                score = ISO39794Util.decodeScoreOrError(taggedObjects[1])
            )
        }
    }
}
