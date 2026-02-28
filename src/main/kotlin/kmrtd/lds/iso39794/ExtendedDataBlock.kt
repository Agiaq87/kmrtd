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
 * $Id: ExtendedDataBlock.java 1896 2025-04-18 21:39:56Z martijno $
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
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.DEROctetString
import org.jmrtd.ASN1Util
import org.jmrtd.lds.iso39794.RegistryIdBlock.Companion.from
import java.util.Objects

data class ExtendedDataBlock(
    val dataTypeIdBlock: RegistryIdBlock,
    val data: ByteArray
) : Block() {
    /*internal constructor(asn1Encodable: ASN1Encodable?) {
        val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)
        dataTypeIdBlock = from(taggedObjects.get(0))
        data = ASN1OctetString.getInstance(taggedObjects.get(1)).getOctets()
    }*/

    /*public override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + data.contentHashCode()
        result = prime * result + Objects.hash(dataTypeIdBlock)
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

        val other = obj as ExtendedDataBlock
        return data.contentEquals(other.data) && dataTypeIdBlock == other.dataTypeIdBlock
    }

    override fun toString(): String {
        return ("ExtendedDataBlock ["
                + "dataTypeIdBlock: " + dataTypeIdBlock
                + ", data: " + data.size
                + "]")
    }*/

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ExtendedDataBlock

        if (dataTypeIdBlock != other.dataTypeIdBlock) return false
        if (!data.contentEquals(other.data)) return false
        if (aSN1Object != other.aSN1Object) return false

        return true
    }

    override fun hashCode(): Int {
        var result = dataTypeIdBlock.hashCode()
        result = 31 * result + data.contentHashCode()
        result = 31 * result + aSN1Object.hashCode()
        return result
    }

    override val aSN1Object: ASN1Encodable
        get() = ASN1Util.encodeTaggedObjects(
            mapOf(
                0 to dataTypeIdBlock.aSN1Object,
                1 to DEROctetString(data)
            )
        )
        /*get() {
            val taggedObjects: MutableMap<Int?, ASN1Encodable?> =
                HashMap<Int?, ASN1Encodable?>()
            taggedObjects[0] = dataTypeIdBlock.aSN1Object
            taggedObjects[1] = DEROctetString(data)
            return ASN1Util.encodeTaggedObjects(taggedObjects)
        }*/

    companion object {
        private const val serialVersionUID = -1557206933986460059L

        /* PACKAGE */
        @JvmStatic
        fun decodeExtendedDataBlocks(asn1Encodable: ASN1Encodable): List<ExtendedDataBlock> =
            if (ASN1Util.isSequenceOfSequences(asn1Encodable)) {
                ASN1Util.list(asn1Encodable).map { from(it) }
            } else {
                listOf(from(asn1Encodable))
            }
        /*{
            if (ASN1Util.isSequenceOfSequences(asn1Encodable)) {
                val blockASN1Objects = ASN1Util.list(asn1Encodable)
                val blocks: MutableList<ExtendedDataBlock?> =
                    ArrayList<ExtendedDataBlock?>(blockASN1Objects.size)
                for (blockASN1Object in blockASN1Objects) {
                    blocks.add(ExtendedDataBlock.from(blockASN1Object))
                }
                return blocks
            } else {
                return mutableListOf<ExtendedDataBlock?>(ExtendedDataBlock(asn1Encodable))
            }
        }*/

        /**
         * Factory method
         *
         * ExtendedDataBlock ::= SEQUENCE {
         *   dataTypeIdBlock         [0] RegistryIdBlock,
         *   data                    [1] OCTET STRING
         * }
         */
        @JvmStatic
        fun from(asn1Encodable: ASN1Encodable): ExtendedDataBlock {
            val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)

            return ExtendedDataBlock(
                dataTypeIdBlock = from(taggedObjects[0]),
                data = ASN1OctetString.getInstance(taggedObjects[1]).octets
            )
        }
    }
}
