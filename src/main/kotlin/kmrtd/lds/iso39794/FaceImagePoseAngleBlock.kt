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
package kmrtd.lds.iso39794

import org.bouncycastle.asn1.ASN1Encodable
import org.jmrtd.ASN1Util
import java.util.*

class FaceImagePoseAngleBlock : Block {
    class AngleDataBlock : Block {
        /** INTEGER (-180..180).  */
        val angleValue: Int

        /** INTEGER (0..180).  */
        var angleUncertainty: Int = 0
            private set

        constructor(angleValue: Int, angleUncertainty: Int) {
            this.angleValue = angleValue
            this.angleUncertainty = angleUncertainty
        }

        internal constructor(asn1Encodable: ASN1Encodable?) {
            val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)
            angleValue = ASN1Util.decodeInt(taggedObjects.get(0))
            if (taggedObjects.containsKey(1)) {
                angleUncertainty = ASN1Util.decodeInt(taggedObjects.get(1))
            } else {
                angleUncertainty = -1
            }
        }

        override fun hashCode(): Int {
            return Objects.hash(angleUncertainty, angleValue)
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

            val other = obj as AngleDataBlock
            return angleUncertainty == other.angleUncertainty && angleValue == other.angleValue
        }

        override fun toString(): String {
            return ("AngleDataBlock ["
                    + "angleValue: " + angleValue
                    + ", angleUncertainty: " + angleUncertainty
                    + "]")
        }

        override fun getASN1Object(): ASN1Encodable? {
            val taggedObjects: MutableMap<Int?, ASN1Encodable?> = HashMap<Int?, ASN1Encodable?>()
            taggedObjects.put(0, ASN1Util.encodeInt(angleValue))
            if (angleUncertainty >= 0) {
                taggedObjects.put(1, ASN1Util.encodeInt(angleUncertainty))
            }
            return ASN1Util.encodeTaggedObjects(taggedObjects)
        }

        companion object {
            private const val serialVersionUID = 3589963464356857977L
        }
    }

    var yawAngleDataBlock: AngleDataBlock? = null
        private set

    var pitchAngleDataBlock: AngleDataBlock? = null
        private set

    var rollAngleDataBlock: AngleDataBlock? = null
        private set

    constructor(
        yawAngleDataBlock: AngleDataBlock?, pitchAngleDataBlock: AngleDataBlock?,
        rollAngleDataBlock: AngleDataBlock?
    ) {
        this.yawAngleDataBlock = yawAngleDataBlock
        this.pitchAngleDataBlock = pitchAngleDataBlock
        this.rollAngleDataBlock = rollAngleDataBlock
    }

    //  PoseAngleBlock ::= SEQUENCE {
    //    yawAngleBlock [0] AngleDataBlock OPTIONAL,
    //    pitchAngleBlock [1] AngleDataBlock OPTIONAL,
    //    rollAngleBlock [2] AngleDataBlock OPTIONAL
    //  }
    internal constructor(asn1Encodable: ASN1Encodable?) {
        val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)
        if (taggedObjects.containsKey(0)) {
            yawAngleDataBlock = AngleDataBlock(taggedObjects.get(0))
        }
        if (taggedObjects.containsKey(1)) {
            pitchAngleDataBlock = AngleDataBlock(taggedObjects.get(1))
        }
        if (taggedObjects.containsKey(2)) {
            rollAngleDataBlock = AngleDataBlock(taggedObjects.get(2))
        }
    }

    override fun hashCode(): Int {
        return Objects.hash(pitchAngleDataBlock, rollAngleDataBlock, yawAngleDataBlock)
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
    }

    /* PACKAGE */
    override fun getASN1Object(): ASN1Encodable? {
        val taggedObjects: MutableMap<Int?, ASN1Encodable?> = HashMap<Int?, ASN1Encodable?>()
        if (yawAngleDataBlock != null) {
            taggedObjects.put(0, yawAngleDataBlock.getASN1Object())
        }
        if (pitchAngleDataBlock != null) {
            taggedObjects.put(1, pitchAngleDataBlock.getASN1Object())
        }
        if (rollAngleDataBlock != null) {
            taggedObjects.put(2, rollAngleDataBlock.getASN1Object())
        }
        return ASN1Util.encodeTaggedObjects(taggedObjects)
    }

    companion object {
        val serialversionuid: Long = -7526271037214134760L
    }
}
