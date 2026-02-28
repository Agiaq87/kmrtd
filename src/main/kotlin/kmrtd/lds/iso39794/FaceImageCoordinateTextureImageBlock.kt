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
 * $Id: FaceImageCoordinateTextureImageBlock.java 1892 2025-03-18 15:15:52Z martijno $
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
import java.math.BigInteger

data class FaceImageCoordinateTextureImageBlock(
    val uInPixel: BigInteger,
    val vInPixel: BigInteger
) : Block(), FaceImageLandmarkCoordinates {
    /*internal constructor(asn1Encodable: ASN1Encodable?) {
        val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)
        uInPixel = ASN1Util.decodeBigInteger(taggedObjects.get(0))
        vInPixel = ASN1Util.decodeBigInteger(taggedObjects.get(1))
    }*/

    /*override fun hashCode(): Int {
        return Objects.hash(uInPixel, vInPixel)
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

        val other = obj as FaceImageCoordinateTextureImageBlock
        return uInPixel == other.uInPixel && vInPixel == other.vInPixel
    }

    override fun toString(): String {
        return ("CoordinateTextureImageBlock ["
                + "uInPixel: " + uInPixel
                + ", vInPixel: " + vInPixel
                + "]")
    }*/

    override val aSN1Object: ASN1Encodable
        get() = ASN1Util.encodeTaggedObjects(
            mapOf(
                0 to ASN1Util.encodeBigInteger(uInPixel),
                1 to ASN1Util.encodeBigInteger(vInPixel)
            )
        )
    /* PACKAGE */
    /*get() {
        val taggedObjects: MutableMap<Int?, ASN1Encodable?> =
            HashMap<Int?, ASN1Encodable?>()
        taggedObjects[0] = ASN1Util.encodeBigInteger(uInPixel)
        taggedObjects[1] = ASN1Util.encodeBigInteger(vInPixel)
        return ASN1Util.encodeTaggedObjects(taggedObjects)
    }*/

    companion object {
        private const val serialVersionUID = -563037651358748573L

        /**
         * Factory method
         *
         * CoordinateTextureImageBlock ::= SEQUENCE {
         *   uInPixel [0] INTEGER (0..MAX),
         *   vInPixel [1] INTEGER (0..MAX)
         * }
         */
        @JvmStatic
        fun from(asn1Encodable: ASN1Encodable): FaceImageCoordinateTextureImageBlock {
            val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)

            return FaceImageCoordinateTextureImageBlock(
                uInPixel = ASN1Util.decodeBigInteger(taggedObjects[0]),
                vInPixel = ASN1Util.decodeBigInteger(taggedObjects[1])
            )
        }
    }
}
