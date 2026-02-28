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
 * $Id: FaceImageDataBlock.java 1901 2025-07-15 12:31:11Z martijno $
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

class FaceImageDataBlock : Block, BiometricDataBlock {
    val versionBlock: VersionBlock
    val representationBlocks: List<FaceImageRepresentationBlock?>?

    private var sbh: StandardBiometricHeader?

    constructor(
        versionBlock: VersionBlock,
        representationBlocks: MutableList<FaceImageRepresentationBlock?>?,
        sbh: StandardBiometricHeader?
    ) {
        this.versionBlock = versionBlock
        this.representationBlocks = representationBlocks
        this.sbh = sbh
    }

    //  FaceImageDataBlock ::= [APPLICATION 5] SEQUENCE {
    //    versionBlock [0] VersionBlock,
    //    representationBlocks [1] RepresentationBlocks,
    //    ...
    //  }
    constructor(inputStream: InputStream?) : this(null, inputStream)

    constructor(sbh: StandardBiometricHeader?, inputStream: InputStream?) : this(
        sbh,
        ASN1Util.readASN1Object(inputStream)
    )

    internal constructor(sbh: StandardBiometricHeader?, asn1Encodable: ASN1Encodable?) {
        var asn1Encodable = asn1Encodable
        this.sbh = sbh
        asn1Encodable = ASN1Util.checkTag(asn1Encodable, BERTags.APPLICATION, 5)
        require(asn1Encodable is ASN1Sequence) { "Cannot decode!" }

        val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)
        versionBlock = VersionBlock.from(taggedObjects[0]!!)
        representationBlocks =
            FaceImageRepresentationBlock.decodeRepresentationBlocks(taggedObjects[1])
    }

    override val standardBiometricHeader: StandardBiometricHeader
        get() {
            if (sbh == null) {
                val biometricType =
                    byteArrayOf(CBEFFInfo.BIOMETRIC_TYPE_FACIAL_FEATURES.toByte())
                val biometricSubtype =
                    byteArrayOf(CBEFFInfo.BIOMETRIC_SUBTYPE_NONE.toByte())
                val formatOwner = byteArrayOf(
                    ((StandardBiometricHeader.JTC1_SC37_FORMAT_OWNER_VALUE and 0xFF00) shr 8).toByte(),
                    (StandardBiometricHeader.JTC1_SC37_FORMAT_OWNER_VALUE and 0xFF).toByte()
                )
                val formatType = byteArrayOf(
                    ((StandardBiometricHeader.ISO_39794_FACE_IMAGE_FORMAT_TYPE_VALUE and 0xFF00) shr 8).toByte(),
                    (StandardBiometricHeader.ISO_39794_FACE_IMAGE_FORMAT_TYPE_VALUE and 0xFF).toByte()
                )

                val elements: SortedMap<Int?, ByteArray?> =
                    TreeMap<Int?, ByteArray?>()
                elements[ISO781611.BIOMETRIC_TYPE_TAG] = biometricType // 81
                elements[ISO781611.BIOMETRIC_SUBTYPE_TAG] = biometricSubtype // 82
                elements[ISO781611.FORMAT_OWNER_TAG] = formatOwner // 87
                elements[ISO781611.FORMAT_TYPE_TAG] = formatType // 88
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

        val other = obj as FaceImageDataBlock
        return representationBlocks == other.representationBlocks
                && sbh == other.sbh
                && versionBlock == other.versionBlock
    }

    override fun toString(): String {
        return ("FaceImageDataBlock ["
                + "versionBlock: " + versionBlock
                + ", representationBlocks: " + representationBlocks
                + ", sbh: " + sbh
                + "]")
    }

    override val aSN1Object: ASN1Encodable
        get() = DERTaggedObject(
            false,
            BERTags.APPLICATION,
            0x05,
            ASN1Util.encodeTaggedObjects(
                mapOf(
                    0 to versionBlock.aSN1Object,
                    1 to ISO39794Util.encodeBlocks(representationBlocks)
                )
            )
        )
        /* PACKAGE */
        /*get() {
            val taggedObjects: MutableMap<Int?, ASN1Encodable?> =
                HashMap<Int?, ASN1Encodable?>()
            taggedObjects[0] = versionBlock.aSN1Object
            taggedObjects[1] = ISO39794Util.encodeBlocks(representationBlocks)
            return DERTaggedObject(
                false,
                BERTags.APPLICATION,
                0x05,
                ASN1Util.encodeTaggedObjects(taggedObjects)
            )
        }*/

    companion object {
        private const val serialVersionUID = -7831183488053975281L
    }
}
