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
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package org.jmrtd.lds.iso39794

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.DEROctetString
import org.jmrtd.ASN1Util
import org.jmrtd.lds.iso39794.ReferenceColourDefinitionAndValueBlock.Companion.decodeReferenceColourDefinitionAndValueBlocks

data class FaceImageReferenceColourMappingBlock(
    val referenceColourSchema: ByteArray?,
    val referenceColourDefinitionAndValueBlocks: List<ReferenceColourDefinitionAndValueBlock>?
) : Block() {

    /*internal constructor(asn1Encodable: ASN1Encodable?) {
        val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)
        if (taggedObjects.containsKey(0)) {
            referenceColourSchema = ASN1OctetString.getInstance(taggedObjects[0]).octets
        }
        if (taggedObjects.containsKey(1)) {
            referenceColourDefinitionAndValueBlocks =
                decodeReferenceColourDefinitionAndValueBlocks(taggedObjects[1])
        }
    }*/

    /*public override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + referenceColourSchema.contentHashCode()
        result = prime * result + Objects.hash(referenceColourDefinitionAndValueBlocks)
        return result
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

        val other = obj as FaceImageReferenceColourMappingBlock
        return referenceColourDefinitionAndValueBlocks == other.referenceColourDefinitionAndValueBlocks
                && referenceColourSchema.contentEquals(other.referenceColourSchema)
    }

    override fun toString(): String {
        return ("FaceImageReferenceColourMappingBlock ["
                + "referenceColourSchema: " + Hex.bytesToHexString(referenceColourSchema)
                + ", referenceColourDefinitionAndValueBlocks: " + referenceColourDefinitionAndValueBlocks
                + "]")
    }*/

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FaceImageReferenceColourMappingBlock

        if (!referenceColourSchema.contentEquals(other.referenceColourSchema)) return false
        if (referenceColourDefinitionAndValueBlocks != other.referenceColourDefinitionAndValueBlocks) return false
        if (aSN1Object != other.aSN1Object) return false

        return true
    }

    override fun hashCode(): Int {
        var result = referenceColourSchema?.contentHashCode() ?: 0
        result = 31 * result + (referenceColourDefinitionAndValueBlocks?.hashCode() ?: 0)
        result = 31 * result + aSN1Object.hashCode()
        return result
    }

    override val aSN1Object: ASN1Encodable
        get() = ASN1Util.encodeTaggedObjects(
            buildMap {
                referenceColourSchema?.let{
                    put(0, DEROctetString(it))
                }
                referenceColourDefinitionAndValueBlocks?.let {
                    put(1, ISO39794Util.encodeBlocks(it))
                }
            }
        )
        /* PACAKAGE */
        /*get() {
            val taggedObjects: MutableMap<Int?, ASN1Encodable?> =
                HashMap<Int?, ASN1Encodable?>()
            if (referenceColourSchema != null) {
                taggedObjects[0] = DEROctetString(referenceColourSchema)
            }
            if (referenceColourDefinitionAndValueBlocks != null) {
                taggedObjects[1] = ISO39794Util.encodeBlocks(referenceColourDefinitionAndValueBlocks)
            }
            return ASN1Util.encodeTaggedObjects(taggedObjects)
        }*/

    companion object {
        private const val serialVersionUID = -347556999620185601L

        /**
         * Factory method
         */
        @JvmStatic
        fun from(asn1Encodable: ASN1Encodable?): FaceImageReferenceColourMappingBlock {
            val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)

            return FaceImageReferenceColourMappingBlock(
                referenceColourSchema = if (taggedObjects.containsKey(0)) ASN1OctetString.getInstance(taggedObjects[0]).octets else null,
                referenceColourDefinitionAndValueBlocks = if (taggedObjects.containsKey(1)) decodeReferenceColourDefinitionAndValueBlocks(taggedObjects[1]) else null
            )
        }
    }
}
