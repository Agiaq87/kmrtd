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
 * $Id: CoordinateCartesian3DUnsignedShortBlock.java 1892 2025-03-18 15:15:52Z martijno $
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

data class CoordinateCartesian3DUnsignedShortBlock(
    val x: Int,
    val y: Int,
    val z: Int
) : Block(), FaceImageLandmarkCoordinates {
/*    internal constructor(asn1Encodable: ASN1Encodable?) {
        val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)
        x = ASN1Util.decodeInt(taggedObjects[0])
        y = ASN1Util.decodeInt(taggedObjects[1])
        z = ASN1Util.decodeInt(taggedObjects[2])
    }*/

   /* override fun hashCode(): Int {
        return Objects.hash(x, y, z)
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

        val other = obj as CoordinateCartesian3DUnsignedShortBlock
        return x == other.x && y == other.y && z == other.z
    }

    override fun toString(): String {
        return ("CoordinateCartesian3DUnsignedShortBlock ["
                + "x: " + x
                + ", y: " + y
                + ", z: " + z
                + "]")
    }*/

    override val aSN1Object: ASN1Encodable
        /* PACKAGE */
        get() = ASN1Util.encodeTaggedObjects(
            mapOf(
                0 to ASN1Util.encodeInt(x),
                1 to ASN1Util.encodeInt(y),
                2 to ASN1Util.encodeInt(z)
            )
        )
        /*get() {
            val taggedObjects: MutableMap<Int?, ASN1Encodable?> =
                HashMap()
            taggedObjects[0] = ASN1Util.encodeInt(x)
            taggedObjects[1] = ASN1Util.encodeInt(y)
            taggedObjects[2] = ASN1Util.encodeInt(z)
            return ASN1Util.encodeTaggedObjects(taggedObjects)
        }*/

    companion object {
        private const val serialVersionUID = -6100355379192071041L

        /**
         * Factory method
         *
         * CoordinateCartesian3DUnsignedShortBlock ::= SEQUENCE {
         *   x               [0] INTEGER (0..65535),
         *   y               [1] INTEGER (0..65535),
         *   z               [2] INTEGER (0..65535)
         * }
         */
        fun from(asn1Encodable: ASN1Encodable?): CoordinateCartesian3DUnsignedShortBlock {
            val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)

            return CoordinateCartesian3DUnsignedShortBlock(
                x  = ASN1Util.decodeInt(taggedObjects[0]),
                y  = ASN1Util.decodeInt(taggedObjects[1]),
                z = ASN1Util.decodeInt(taggedObjects[2])
            )
        }
    }
}
