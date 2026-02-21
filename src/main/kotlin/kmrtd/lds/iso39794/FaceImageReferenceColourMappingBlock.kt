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
 * $Id: FaceImageReferenceColourMappingBlock.java 1889 2025-03-15 21:09:22Z martijno $
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

import net.sf.scuba.util.Hex
import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.DEROctetString
import kmrtd.ASN1Util
import java.util.*

class FaceImageReferenceColourMappingBlock : Block {
    class ReferenceColourDefinitionAndValueBlock : Block {
        var referenceColourDefinition: ByteArray?
            private set
        var referenceColourValue: ByteArray?
            private set

        constructor(referenceColourDefinition: ByteArray?, referenceColourValue: ByteArray?) {
            this.referenceColourDefinition = referenceColourDefinition
            this.referenceColourValue = referenceColourValue
        }

        //    ReferenceColourDefinitionAndValueBlock ::= SEQUENCE {
        //            referenceColourDefinition [0] OCTET STRING OPTIONAL,
        //            referenceColourValue [1] OCTET STRING OPTIONAL,
        //            ...
        //    }
        internal constructor(asn1Encodable: ASN1Encodable?) {
            val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)
            if (taggedObjects.containsKey(0)) {
                referenceColourDefinition = ASN1OctetString.getInstance(taggedObjects.get(0)).getOctets()
            }
            if (taggedObjects.containsKey(1)) {
                referenceColourValue = ASN1OctetString.getInstance(taggedObjects.get(1)).getOctets()
            }
        }

        override fun hashCode(): Int {
            val prime = 31
            var result = 1
            result = prime * result + referenceColourDefinition.contentHashCode()
            result = prime * result + referenceColourValue.contentHashCode()
            return result
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

            val other = obj as ReferenceColourDefinitionAndValueBlock
            return referenceColourDefinition.contentEquals(other.referenceColourDefinition) && referenceColourValue.contentEquals(
                other.referenceColourValue
            )
        }

        override fun toString(): String {
            return ("ReferenceColourDefinitionAndValueBlock ["
                    + "referenceColourDefinition: " + Hex.bytesToHexString(referenceColourDefinition)
                    + ", referenceColourValue: " + Hex.bytesToHexString(referenceColourValue)
                    + "]")
        }

        override fun getASN1Object(): ASN1Encodable? {
            val taggedObjects: MutableMap<Int?, ASN1Encodable?> = HashMap<Int?, ASN1Encodable?>()
            if (referenceColourDefinition != null) {
                taggedObjects[0] = DEROctetString(referenceColourDefinition)
            }
            if (referenceColourValue != null) {
                taggedObjects[1] = DEROctetString(referenceColourValue)
            }
            return ASN1Util.encodeTaggedObjects(taggedObjects)
        }

        companion object {
            private val serialVersionUID = -7927429988191532374L

            /* PACKAGE */
            fun decodeReferenceColourDefinitionAndValueBlocks(asn1Encodable: ASN1Encodable?): MutableList<ReferenceColourDefinitionAndValueBlock?> {
                if (ASN1Util.isSequenceOfSequences(asn1Encodable)) {
                    val blockASN1Objects = ASN1Util.list(asn1Encodable)
                    val blocks: MutableList<ReferenceColourDefinitionAndValueBlock?> =
                        ArrayList<ReferenceColourDefinitionAndValueBlock?>(blockASN1Objects.size)
                    for (blockASN1Object in blockASN1Objects) {
                        blocks.add(ReferenceColourDefinitionAndValueBlock(blockASN1Object))
                    }
                    return blocks
                } else {
                    return mutableListOf<ReferenceColourDefinitionAndValueBlock?>(
                        ReferenceColourDefinitionAndValueBlock(
                            asn1Encodable
                        )
                    )
                }
            }
        }
    }

    //  ReferenceColourMappingBlock ::= SEQUENCE {
    //    referenceColourSchema [0] OCTET STRING OPTIONAL,
    //    referenceColourDefinitionAndValueBlocks [1] ReferenceColourDefinitionAndValueBlocks OPTIONAL,
    //    ...
    // }
    var referenceColourSchema: ByteArray?
        private set

    var referenceColourDefinitionAndValueBlocks: MutableList<ReferenceColourDefinitionAndValueBlock?>? = null
        private set

    constructor(
        referenceColourSchema: ByteArray?,
        referenceColourDefinitionAndValueBlocks: MutableList<ReferenceColourDefinitionAndValueBlock?>?
    ) {
        this.referenceColourSchema = referenceColourSchema
        this.referenceColourDefinitionAndValueBlocks = referenceColourDefinitionAndValueBlocks
    }

    internal constructor(asn1Encodable: ASN1Encodable?) {
        val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)
        if (taggedObjects.containsKey(0)) {
            referenceColourSchema = ASN1OctetString.getInstance(taggedObjects.get(0)).getOctets()
        }
        if (taggedObjects.containsKey(1)) {
            referenceColourDefinitionAndValueBlocks =
                ReferenceColourDefinitionAndValueBlock.Companion.decodeReferenceColourDefinitionAndValueBlocks(
                    taggedObjects.get(1)
                )
        }
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + referenceColourSchema.contentHashCode()
        result = prime * result + Objects.hash(referenceColourDefinitionAndValueBlocks)
        return result
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

        val other = obj as FaceImageReferenceColourMappingBlock
        return referenceColourDefinitionAndValueBlocks == other.referenceColourDefinitionAndValueBlocks
                && referenceColourSchema.contentEquals(other.referenceColourSchema)
    }

    override fun toString(): String {
        return ("FaceImageReferenceColourMappingBlock ["
                + "referenceColourSchema: " + Hex.bytesToHexString(referenceColourSchema)
                + ", referenceColourDefinitionAndValueBlocks: " + referenceColourDefinitionAndValueBlocks
                + "]")
    }

    /* PACAKAGE */
    override fun getASN1Object(): ASN1Encodable? {
        val taggedObjects: MutableMap<Int?, ASN1Encodable?> = HashMap<Int?, ASN1Encodable?>()
        if (referenceColourSchema != null) {
            taggedObjects[0] = DEROctetString(referenceColourSchema)
        }
        if (referenceColourDefinitionAndValueBlocks != null) {
            taggedObjects[1] = ISO39794Util.encodeBlocks(referenceColourDefinitionAndValueBlocks)
        }
        return ASN1Util.encodeTaggedObjects(taggedObjects)
    }

    companion object {
        private val serialVersionUID = -347556999620185601L
    }
}
