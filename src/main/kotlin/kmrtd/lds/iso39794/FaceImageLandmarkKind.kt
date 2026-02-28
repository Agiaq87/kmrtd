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
 * $Id: FaceImageLandmarkKind.java 1893 2025-03-18 15:20:18Z martijno $
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

interface FaceImageLandmarkKind {
    override fun hashCode(): Int

    override fun equals(other: Any?): Boolean

    val code: Int

    companion object {
        //  AnthropometricLandmark ::= CHOICE {
        //    base [0] AnthropometricLandmarkBase,
        //    extensionBlock [1] AnthropometricLandmarkExtensionBlock
        //  }
        //  AnthropometricLandmarkBase ::= CHOICE {
        //    anthropometricLandmarkName [0] AnthropometricLandmarkName,
        //    anthropometricLandmarkPointName [1] AnthropometricLandmarkPointName,
        //    anthropometricLandmarkPointId [2] AnthropometricLandmarkPointId
        //  }
        @JvmStatic
        fun decodeLandmarkKind(asn1Encodable: ASN1Encodable?): FaceImageLandmarkKind? {
            val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)

            if (taggedObjects.containsKey(0)) {
                // base case...
                val baseTaggedObjects = ASN1Util.decodeTaggedObjects(taggedObjects[0])
                if (baseTaggedObjects.containsKey(0)) {
                    // MPEG feature point case...
                    return MPEGFeaturePointCode.fromCode(
                        ISO39794Util.decodeCodeFromChoiceExtensionBlockFallback(
                            baseTaggedObjects[0]
                        )
                    )
                } else if (baseTaggedObjects.containsKey(1)) {
                    decodeAnthropometricLandmark(baseTaggedObjects[1])
                }
            }

            return null
        }

        @JvmStatic
        fun decodeAnthropometricLandmark(asn1Encodable: ASN1Encodable?): FaceImageLandmarkKind? {
            val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)
            if (taggedObjects.containsKey(0)) {
                // base case...
                val baseTaggedObjects = ASN1Util.decodeTaggedObjects(taggedObjects[0])

                when {
                    baseTaggedObjects.containsKey(0) -> AnthropometricLandmarkNameCode.fromCode(
                        ISO39794Util.decodeCodeFromChoiceExtensionBlockFallback(
                            baseTaggedObjects[0]
                        )
                    )

                    baseTaggedObjects.containsKey(1) -> AnthropometricLandmarkPointNameCode.fromCode(
                        ISO39794Util.decodeCodeFromChoiceExtensionBlockFallback(
                            baseTaggedObjects[1]
                        )
                    )

                    baseTaggedObjects.containsKey(2) -> AnthropometricLandmarkPointIdCode.fromCode(
                        ISO39794Util.decodeCodeFromChoiceExtensionBlockFallback(
                            baseTaggedObjects[2]
                        )
                    )
                }

                /*if (baseTaggedObjects.containsKey(0)) {
                    return AnthropometricLandmarkNameCode.fromCode(
                        ISO39794Util.decodeCodeFromChoiceExtensionBlockFallback(
                            baseTaggedObjects[0]
                        )
                    )
                } else if (baseTaggedObjects.containsKey(1)) {
                    return AnthropometricLandmarkPointNameCode.fromCode(
                        ISO39794Util.decodeCodeFromChoiceExtensionBlockFallback(
                            baseTaggedObjects[1]
                        )
                    )
                } else if (baseTaggedObjects.containsKey(2)) {
                    return AnthropometricLandmarkPointIdCode.fromCode(
                        ISO39794Util.decodeCodeFromChoiceExtensionBlockFallback(
                            baseTaggedObjects[2]
                        )
                    )
                }*/
            }

            return null
        }

        @JvmStatic
        fun encodeLandmarkKind(landmarkKind: FaceImageLandmarkKind?): ASN1Encodable {
            val baseTaggedObjects: MutableMap<Int, ASN1Encodable> = mutableMapOf()
            if (landmarkKind is MPEGFeaturePointCode) {
                baseTaggedObjects[0] = ISO39794Util.encodeCodeAsChoiceExtensionBlockFallback(
                    landmarkKind.code
                )
            } else {
                baseTaggedObjects[1] = encodeAnthropmetricLandmark(landmarkKind)
            }

            val taggedObjects: MutableMap<Int, ASN1Encodable> = mutableMapOf()
            taggedObjects[0] = ASN1Util.encodeTaggedObjects(baseTaggedObjects)
            return ASN1Util.encodeTaggedObjects(taggedObjects)
        }

        @JvmStatic
        fun encodeAnthropmetricLandmark(landmarkKind: FaceImageLandmarkKind?): ASN1Encodable {
            val baseTaggedObjects: MutableMap<Int, ASN1Encodable> = mutableMapOf()
            when (landmarkKind) {
                is AnthropometricLandmarkNameCode -> {
                    baseTaggedObjects[0] = ISO39794Util.encodeCodeAsChoiceExtensionBlockFallback(
                        landmarkKind.code
                    )
                }

                is AnthropometricLandmarkPointNameCode -> {
                    baseTaggedObjects[1] = ISO39794Util.encodeCodeAsChoiceExtensionBlockFallback(
                        landmarkKind.code
                    )
                }

                is AnthropometricLandmarkPointIdCode -> {
                    baseTaggedObjects[2] = ISO39794Util.encodeCodeAsChoiceExtensionBlockFallback(
                        landmarkKind.code
                    )
                }
            }

            val taggedObjects: MutableMap<Int, ASN1Encodable> = mutableMapOf()
            taggedObjects[0] = ASN1Util.encodeTaggedObjects(baseTaggedObjects)
            return ASN1Util.encodeTaggedObjects(taggedObjects)
        }
    }
}