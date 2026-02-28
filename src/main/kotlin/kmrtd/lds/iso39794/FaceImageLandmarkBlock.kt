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
 * $Id: FaceImageLandmarkBlock.java 1892 2025-03-18 15:15:52Z martijno $
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
package org.jmrtd.lds.iso39794

import org.bouncycastle.asn1.ASN1Encodable
import org.jmrtd.ASN1Util
import java.util.Objects

data class FaceImageLandmarkBlock(
    val landmarkKind: FaceImageLandmarkKind?,
    val landmarkCoordinates: FaceImageLandmarkCoordinates?
) : Block() {

    //  LandmarkBlock ::= SEQUENCE {
    //    landmarkKind [0] LandmarkKind,
    //    landmarkCoordinates [1] LandmarkCoordinates OPTIONAL,
    //    ...
    //  }
    /*internal constructor(asn1Encodable: ASN1Encodable?) {
        val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)
        landmarkKind = FaceImageLandmarkKind.decodeLandmarkKind(taggedObjects[0])
        if (taggedObjects.containsKey(1)) {
            landmarkCoordinates =
                FaceImageLandmarkCoordinates.decodeLandmarkCoordinates(taggedObjects[1])
        }
    }*/

    /*public override fun hashCode(): Int {
        return Objects.hash(landmarkCoordinates, landmarkKind)
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

        val other = obj as FaceImageLandmarkBlock
        return landmarkCoordinates == other.landmarkCoordinates
                && landmarkKind == other.landmarkKind
    }

    override fun toString(): String {
        return ("FaceImageLandmarkBlock ["
                + "landmarkKind: " + landmarkKind
                + ", landmarkCoordinates: " + landmarkCoordinates
                + "]")
    }*/

    override val aSN1Object: ASN1Encodable
        get() = ASN1Util.encodeTaggedObjects(
            buildMap {
                put(0, FaceImageLandmarkKind.encodeLandmarkKind(landmarkKind))
                landmarkCoordinates?.let {
                    put(1, FaceImageLandmarkCoordinates.encodeLandmarkCoordinates(it))
                }
            }
        )
    /* PACKAGE */
    /*get() {
        val taggedObjects: MutableMap<Int?, ASN1Encodable?> =
            HashMap<Int?, ASN1Encodable?>()
        taggedObjects.put(0, FaceImageLandmarkKind.encodeLandmarkKind(landmarkKind))
        if (landmarkCoordinates != null) {
            taggedObjects.put(
                1,
                FaceImageLandmarkCoordinates.encodeLandmarkCoordinates(landmarkCoordinates)
            )
        }
        return ASN1Util.encodeTaggedObjects(taggedObjects)
    }*/

    companion object {
        private const val serialVersionUID = -8008877005187206392L

        @JvmStatic
        fun decodeLandmarkBlocks(asn1Encodable: ASN1Encodable): List<FaceImageLandmarkBlock> =
            if (ASN1Util.isSequenceOfSequences(asn1Encodable)) {
                ASN1Util.list(asn1Encodable).map { FaceImageLandmarkBlock.from(it) }
            } else {
                listOf(FaceImageLandmarkBlock.from(asn1Encodable))
            }


        /*fun buildLandmarkBlocks(asn1Encodable: ASN1Encodable?): MutableList<FaceImageLandmarkBlock?> {
            if (ASN1Util.isSequenceOfSequences(asn1Encodable)) {
                val blockASN1Objects = ASN1Util.list(asn1Encodable)
                val blocks: MutableList<FaceImageLandmarkBlock?> =
                    ArrayList<FaceImageLandmarkBlock?>(blockASN1Objects.size)
                for (blockASN1Object in blockASN1Objects) {
                    blocks.add(FaceImageLandmarkBlock.from(blockASN1Object))
                }
                return blocks
            } else {
                return mutableListOf<FaceImageLandmarkBlock?>(FaceImageLandmarkBlock(asn1Encodable))
            }
        }*/

        /**
         * Factory method
         *
         * LandmarkBlock ::= SEQUENCE {
         *   landmarkKind [0] LandmarkKind,
         *   landmarkCoordinates [1] LandmarkCoordinates OPTIONAL,
         *   ...
         * }
         */
        @JvmStatic
        fun from(asn1Encodable: ASN1Encodable): FaceImageLandmarkBlock {
            val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)

            return FaceImageLandmarkBlock(
                landmarkKind = FaceImageLandmarkKind.decodeLandmarkKind(taggedObjects[0]),
                landmarkCoordinates = if (taggedObjects.containsKey(1)) FaceImageLandmarkCoordinates.decodeLandmarkCoordinates(
                    taggedObjects[1]
                ) else null
            )
        }
    }
}
