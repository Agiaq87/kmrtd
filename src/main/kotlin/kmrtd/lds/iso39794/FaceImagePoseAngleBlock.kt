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
 * $Id: FaceImagePoseAngleBlock.java 1901 2025-07-15 12:31:11Z martijno $
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

data class FaceImagePoseAngleBlock(
    val yawAngleDataBlock: AngleDataBlock?,
    val pitchAngleDataBlock: AngleDataBlock?,
    val rollAngleDataBlock: AngleDataBlock?
) : Block() {
    /*var yawAngleDataBlock: AngleDataBlock? = null
        private set

    var pitchAngleDataBlock: AngleDataBlock? = null
        private set

    var rollAngleDataBlock: AngleDataBlock? = null
        private set

    constructor(
        yawAngleDataBlock: AngleDataBlock?,
        pitchAngleDataBlock: AngleDataBlock?,
        rollAngleDataBlock: AngleDataBlock?
    ) {
        this.yawAngleDataBlock = yawAngleDataBlock
        this.pitchAngleDataBlock = pitchAngleDataBlock
        this.rollAngleDataBlock = rollAngleDataBlock
    }*/

    //  PoseAngleBlock ::= SEQUENCE {
    //    yawAngleBlock [0] AngleDataBlock OPTIONAL,
    //    pitchAngleBlock [1] AngleDataBlock OPTIONAL,
    //    rollAngleBlock [2] AngleDataBlock OPTIONAL
    //  }
    /*internal constructor(asn1Encodable: ASN1Encodable?) {
        val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)
        if (taggedObjects.containsKey(0)) {
            yawAngleDataBlock = AngleDataBlock.from(taggedObjects[0])
        }
        if (taggedObjects.containsKey(1)) {
            pitchAngleDataBlock = AngleDataBlock.from(taggedObjects[1])
        }
        if (taggedObjects.containsKey(2)) {
            rollAngleDataBlock = AngleDataBlock.from(taggedObjects[2])
        }
    }*/

    /*public override fun hashCode(): Int {
        return Objects.hash(pitchAngleDataBlock, rollAngleDataBlock, yawAngleDataBlock)
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

        val other = obj as FaceImagePoseAngleBlock
        return pitchAngleDataBlock == other.pitchAngleDataBlock
                && rollAngleDataBlock == other.rollAngleDataBlock
                && yawAngleDataBlock == other.yawAngleDataBlock
    }

    override fun toString(): String {
        return ("FaceImagePoseAngleBlock ["
                + "yawAngleDataBlock: " + yawAngleDataBlock
                + ", pitchAngleDataBlock: " + pitchAngleDataBlock
                + ", rollAngleDataBlock: " + rollAngleDataBlock
                + "]")
    }*/

    override val aSN1Object: ASN1Encodable
        get() = ASN1Util.encodeTaggedObjects(
            buildMap {
                yawAngleDataBlock?.let {
                    put(0, it.aSN1Object)
                }
                pitchAngleDataBlock?.let {
                    put(1, it.aSN1Object)
                }
                rollAngleDataBlock?.let {
                    put(2, it.aSN1Object)
                }
            }
        )
        /* PACKAGE */
        /*get() {
            val taggedObjects: MutableMap<Int?, ASN1Encodable?> =
                HashMap<Int?, ASN1Encodable?>()
            if (yawAngleDataBlock != null) {
                taggedObjects.put(0, yawAngleDataBlock!!.aSN1Object)
            }
            if (pitchAngleDataBlock != null) {
                taggedObjects.put(1, pitchAngleDataBlock!!.aSN1Object)
            }
            if (rollAngleDataBlock != null) {
                taggedObjects.put(2, rollAngleDataBlock!!.aSN1Object)
            }
            return ASN1Util.encodeTaggedObjects(taggedObjects)
        }*/

    companion object {
        const val serialversionuid: Long = -7526271037214134760L

        /**
         * Factory method
         */
        @JvmStatic
        fun from(asn1Encodable: ASN1Encodable?): FaceImagePoseAngleBlock {
            val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)

            return FaceImagePoseAngleBlock(
                yawAngleDataBlock = if (taggedObjects.containsKey(0)) AngleDataBlock.from(taggedObjects[0]) else null,
                pitchAngleDataBlock = if (taggedObjects.containsKey(1)) AngleDataBlock.from(taggedObjects[1]) else null,
                rollAngleDataBlock = if (taggedObjects.containsKey(2)) AngleDataBlock.from(taggedObjects[2]) else null
            )
        }
    }
}
