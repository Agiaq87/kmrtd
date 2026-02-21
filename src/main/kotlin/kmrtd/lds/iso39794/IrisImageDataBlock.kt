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
 * $Id: IrisImageDataBlock.java 1900 2025-07-11 20:41:42Z martijno $
 *
 * Based on ISO-IEC-39794-6-ed-1-v1. Disclaimer:
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
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.BERTags
import org.bouncycastle.asn1.DERTaggedObject
import kmrtd.ASN1Util
import kmrtd.cbeff.BiometricDataBlock
import kmrtd.cbeff.CBEFFInfo
import kmrtd.cbeff.ISO781611
import kmrtd.cbeff.StandardBiometricHeader
import java.io.InputStream
import java.util.*

class IrisImageDataBlock internal constructor(sbh: StandardBiometricHeader?, asn1Encodable: ASN1Encodable?) : Block(),
    BiometricDataBlock {
    private var sbh: StandardBiometricHeader?

    val versionBlock: VersionBlock

    val representationBlocks: MutableList<IrisImageRepresentationBlock>

    constructor(inputStream: InputStream?) : this(null, inputStream)

    constructor(sbh: StandardBiometricHeader?, inputStream: InputStream?) : this(
        sbh,
        ASN1Util.readASN1Object(inputStream)
    )

    //  IrisImageDataBlock ::= [APPLICATION 6] SEQUENCE {
    //    versionBlock         [0]   VersionBlock,
    //    representationBlocks [1]   RepresentationBlocks,
    //    ...
    //  }
    init {
        var asn1Encodable = asn1Encodable
        this.sbh = sbh
        asn1Encodable = ASN1Util.checkTag(asn1Encodable, BERTags.APPLICATION, 6)
        require(asn1Encodable is ASN1Sequence) { "Cannot decode!" }

        val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)

        versionBlock = VersionBlock(taggedObjects.get(0))

        representationBlocks = IrisImageRepresentationBlock.decodeRepresentationBlocks(taggedObjects.get(1))
    }

    /**
     * Returns the standard biometric header of this iris info.
     * 
     * @return the standard biometric header
     */
    override fun getStandardBiometricHeader(): StandardBiometricHeader {
        if (sbh == null) {
            val biometricType = byteArrayOf(CBEFFInfo.BIOMETRIC_TYPE_IRIS.toByte())
            val biometricSubtype = byteArrayOf(this.biometricSubtype.toByte())
            val formatOwner = byteArrayOf(
                ((StandardBiometricHeader.JTC1_SC37_FORMAT_OWNER_VALUE and 0xFF00) shr 8).toByte(),
                (StandardBiometricHeader.JTC1_SC37_FORMAT_OWNER_VALUE and 0xFF).toByte()
            )
            val formatType = byteArrayOf(
                ((StandardBiometricHeader.ISO_39794_IRIS_IMAGE_FORMAT_TYPE_VALUE and 0xFF00) shr 8).toByte(),
                (StandardBiometricHeader.ISO_39794_IRIS_IMAGE_FORMAT_TYPE_VALUE and 0xFF).toByte()
            )

            val elements: SortedMap<Int?, ByteArray?> = TreeMap<Int?, ByteArray?>()
            elements[ISO781611.BIOMETRIC_TYPE_TAG] = biometricType // 81 -> 0x10
            elements[ISO781611.BIOMETRIC_SUBTYPE_TAG] = biometricSubtype // 82 -> depends on left/right eye
            elements[ISO781611.FORMAT_OWNER_TAG] = formatOwner // 87 -> 0x0101
            elements[ISO781611.FORMAT_TYPE_TAG] = formatType // 88 -> 0x002c

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

        val other = obj as IrisImageDataBlock
        return representationBlocks == other.representationBlocks && sbh == other.sbh
                && versionBlock == other.versionBlock
    }

    override fun toString(): String {
        return ("IrisImageDataBlock ["
                + "sbh: " + sbh
                + ", versionBlock: " + versionBlock
                + ", representationBlocks: " + representationBlocks
                + "]")
    }

    override val aSN1Object: ASN1Encodable?
        /* PACKAGE */
        get() {
            val taggedObjects: MutableMap<Int?, ASN1Encodable?> =
                HashMap<Int?, ASN1Encodable?>()
            taggedObjects[0] = versionBlock.getASN1Object()
            taggedObjects[1] = ISO39794Util.encodeBlocks(representationBlocks)
            return DERTaggedObject(
                false,
                BERTags.APPLICATION,
                0x06,
                ASN1Util.encodeTaggedObjects(taggedObjects)
            )
        }

    /* PRIVATE */
    private val biometricSubtype: Int
        /**
         * Returns the biometric sub-type bit mask for the iris images.
         * 
         * @return a biometric sub-type bit mask
         */
        get() {
            var result = CBEFFInfo.BIOMETRIC_SUBTYPE_NONE
            var isFirst = true

            val blocks =
                this.representationBlocks

            for (block in blocks) {
                val subType = block.getBiometricSubtype()
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
        private const val serialVersionUID = 5915816895542698638L
    }
}
