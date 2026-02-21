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
 * $Id: FaceImageLandmarkCoordinates.java 1892 2025-03-18 15:15:52Z martijno $
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
import org.jmrtd.ASN1Util
import java.io.Serializable

//  LandmarkCoordinates ::= CHOICE {
//    base [0] LandmarkCoordinatesBase,
//    extensionBlock [1] LandmarkCoordinatesExtensionBlock
//  }
//  LandmarkCoordinatesBase ::= CHOICE {
//    coordinateCartesian2DBlock [0] CoordinateCartesian2DUnsignedShortBlock,
//    coordinateTextureImageBlock [1] CoordinateTextureImageBlock,
//    coordinateCartesian3DBlock [2] CoordinateCartesian3DUnsignedShortBlock
//  }
interface FaceImageLandmarkCoordinates : Serializable {
    override fun hashCode(): Int

    override fun equals(other: Any?): Boolean

    companion object {
        //  LandmarkCoordinates ::= CHOICE {
        //    base [0] LandmarkCoordinatesBase,
        //    extensionBlock [1] LandmarkCoordinatesExtensionBlock
        //  }
        //
        //  LandmarkCoordinatesBase ::= CHOICE {
        //    coordinateCartesian2DBlock [0] CoordinateCartesian2DUnsignedShortBlock,
        //    coordinateTextureImageBlock [1] CoordinateTextureImageBlock,
        //    coordinateCartesian3DBlock [2] CoordinateCartesian3DUnsignedShortBlock
        //  }
        fun decodeLandmarkCoordinates(asn1Encodable: ASN1Encodable?): FaceImageLandmarkCoordinates? {
            val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)
            if (taggedObjects.containsKey(0)) {
                val baseTaggedObjects = ASN1Util.decodeTaggedObjects(taggedObjects.get(0))
                if (baseTaggedObjects.containsKey(0)) {
                    return CoordinateCartesian2DUnsignedShortBlock(baseTaggedObjects.get(0))
                } else if (baseTaggedObjects.containsKey(1)) {
                    return FaceImageCoordinateTextureImageBlock(baseTaggedObjects.get(1))
                } else if (baseTaggedObjects.containsKey(2)) {
                    return CoordinateCartesian3DUnsignedShortBlock(baseTaggedObjects.get(2))
                }
            }

            return null
        }

        fun encodeLandmarkCoordinates(landmarkCoordinates: FaceImageLandmarkCoordinates?): ASN1Encodable? {
            val baseTaggedObjects: MutableMap<Int?, ASN1Encodable?> = HashMap<Int?, ASN1Encodable?>()
            if (landmarkCoordinates is CoordinateCartesian2DUnsignedShortBlock) {
                baseTaggedObjects.put(0, landmarkCoordinates.getASN1Object())
            } else if (landmarkCoordinates is FaceImageCoordinateTextureImageBlock) {
                baseTaggedObjects.put(1, landmarkCoordinates.aSN1Object)
            } else if (landmarkCoordinates is CoordinateCartesian3DUnsignedShortBlock) {
                baseTaggedObjects.put(2, landmarkCoordinates.aSN1Object)
            }

            val taggedObjects: MutableMap<Int?, ASN1Encodable?> = HashMap<Int?, ASN1Encodable?>()
            taggedObjects.put(0, ASN1Util.encodeTaggedObjects(baseTaggedObjects))
            return ASN1Util.encodeTaggedObjects(taggedObjects)
        }
    }
}
