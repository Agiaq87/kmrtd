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
 * $Id: FingerImageDataBlock.java 1900 2025-07-11 20:41:42Z martijno $
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
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.BERTags
import org.bouncycastle.asn1.DERTaggedObject
import org.jmrtd.ASN1Util
import org.jmrtd.cbeff.BiometricDataBlock
import org.jmrtd.cbeff.CBEFFInfo
import org.jmrtd.cbeff.ISO781611
import org.jmrtd.cbeff.StandardBiometricHeader
import java.io.InputStream
import java.util.Objects
import java.util.SortedMap
import java.util.TreeMap

class FingerImageDataBlock : Block, BiometricDataBlock {
    val versionBlock: VersionBlock
    val representationBlocks: List<FingerImageRepresentationBlock>

    private var sbh: StandardBiometricHeader?

    constructor(
        versionBlock: VersionBlock,
        representationBlocks: MutableList<FingerImageRepresentationBlock>,
        sbh: StandardBiometricHeader?
    ) {
        this.versionBlock = versionBlock
        this.representationBlocks = representationBlocks
        this.sbh = sbh
    }

    constructor(inputStream: InputStream?) : this(null, inputStream)

    constructor(sbh: StandardBiometricHeader?, inputStream: InputStream?) : this(
        sbh,
        ASN1Util.readASN1Object(inputStream)
    )

    //  FingerImageDataBlock ::= [APPLICATION 4] SEQUENCE {
    //    versionBlock [0] VersionBlock,
    //    representationBlocks [1] RepresentationBlocks,
    //    ...
    //  }
    internal constructor(sbh: StandardBiometricHeader?, asn1Encodable: ASN1Encodable?) {
        var asn1Encodable = asn1Encodable
        this.sbh = sbh
        asn1Encodable = ASN1Util.checkTag(asn1Encodable, BERTags.APPLICATION, 4)
        require(asn1Encodable is ASN1Sequence) { "Cannot decode!" }

        val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)
        versionBlock = VersionBlock.from(taggedObjects[0])
        representationBlocks =
            FingerImageRepresentationBlock.decodeRepresentationBlocks(taggedObjects[1])
    }

    override val standardBiometricHeader: StandardBiometricHeader
        /**
         * Returns the standard biometric header of this biometric data block.
         * 
         * @return the standard biometric header
         */
        get() {
            if (sbh == null) {
                val biometricType =
                    byteArrayOf(CBEFFInfo.BIOMETRIC_TYPE_FINGERPRINT.toByte())
                val biometricSubtype =
                    byteArrayOf(this.biometricSubtype.toByte())
                val formatOwner = byteArrayOf(
                    ((StandardBiometricHeader.JTC1_SC37_FORMAT_OWNER_VALUE and 0xFF00) shr 8).toByte(),
                    (StandardBiometricHeader.JTC1_SC37_FORMAT_OWNER_VALUE and 0xFF).toByte()
                )
                val formatType = byteArrayOf(
                    ((StandardBiometricHeader.ISO_39794_FINGER_IMAGE_FORMAT_TYPE_VALUE and 0xFF00) shr 8).toByte(),
                    (StandardBiometricHeader.ISO_39794_FINGER_IMAGE_FORMAT_TYPE_VALUE and 0xFF).toByte()
                )

                val elements: SortedMap<Int?, ByteArray?> =
                    TreeMap<Int?, ByteArray?>()
                elements[ISO781611.BIOMETRIC_TYPE_TAG] = biometricType // 81 -> 08 == finger
                elements[ISO781611.BIOMETRIC_SUBTYPE_TAG] =
                    biometricSubtype // 82 -> depends on left/right and finger
                elements[ISO781611.FORMAT_OWNER_TAG] = formatOwner // 87 -> 0101
                elements[ISO781611.FORMAT_TYPE_TAG] =
                    formatType // 88 -> 0028, corresponds to g3-binary-finger-image

                sbh = StandardBiometricHeader(elements)
            }
            return sbh!!
        }

    override fun hashCode(): Int {
        return Objects.hash(representationBlocks, sbh, versionBlock)
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

        val other = obj as FingerImageDataBlock
        return representationBlocks == other.representationBlocks && sbh == other.sbh
                && versionBlock == other.versionBlock
    }

    override fun toString(): String {
        return ("FingerImageDataBlock ["
                + "versionBlock: " + versionBlock
                + ", representationBlocks: " + representationBlocks
                + ", sbh: " + sbh
                + "]")
    }

    override val aSN1Object: ASN1Encodable
        /* PACKAGE */
        get() {
            val taggedObjects: MutableMap<Int, ASN1Encodable?> = mutableMapOf()
            taggedObjects[0] = versionBlock.aSN1Object
            taggedObjects[1] = ISO39794Util.encodeBlocks(representationBlocks)
            return DERTaggedObject(
                false,
                BERTags.APPLICATION,
                0x04,
                ASN1Util.encodeTaggedObjects(taggedObjects)
            )
        }

    /* PRIVATE */
    private val biometricSubtype: Int
        /**
         * Returns the biometric sub-type bit mask for the fingers in this finger info.
         * 
         * @return a biometric sub-type bit mask
         */
        get() {
            var result = CBEFFInfo.BIOMETRIC_SUBTYPE_NONE
            var isFirst = true

            val blocks =
                this.representationBlocks

            for (block in blocks) {
                val subType = block.biometricSubtype
                if (isFirst) {
                    result = subType
                    isFirst = false
                } else {
                    result = result and subType
                }
            }
            return result
        }

    companion object {
        private const val serialVersionUID = -7831183486053375281L
    }
}
