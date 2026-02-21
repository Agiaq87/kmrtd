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
package kmrtd.lds.iso39794

import kmrtd.ASN1Util
import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.DEROctetString
import java.util.*

class ExtendedDataBlock : Block {
    val dataTypeIdBlock: RegistryIdBlock

    val data: ByteArray

    constructor(dataTypeIdBlock: RegistryIdBlock, data: ByteArray) {
        this.dataTypeIdBlock = dataTypeIdBlock
        this.data = data
    }

    //  ExtendedDataBlock ::= SEQUENCE {
    //    dataTypeIdBlock         [0] RegistryIdBlock,
    //    data                    [1] OCTET STRING
    //  }
    internal constructor(asn1Encodable: ASN1Encodable?) {
        val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)
        dataTypeIdBlock = RegistryIdBlock(taggedObjects[0])
        data = ASN1OctetString.getInstance(taggedObjects[1]).octets
    }

    public override fun hashCode(): Int {
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
    }

    override val aSN1Object: ASN1Encodable?
        get() {
            val taggedObjects: MutableMap<Int?, ASN1Encodable?> =
                HashMap<Int?, ASN1Encodable?>()
            taggedObjects[0] = dataTypeIdBlock.getASN1Object()
            taggedObjects[1] = DEROctetString(data)
            return ASN1Util.encodeTaggedObjects(taggedObjects)
        }

    companion object {
        private val serialVersionUID = -1557206933986460059L

        /* PACKAGE */
        fun decodeExtendedDataBlocks(asn1Encodable: ASN1Encodable?): MutableList<ExtendedDataBlock?> {
            if (ASN1Util.isSequenceOfSequences(asn1Encodable)) {
                val blockASN1Objects = ASN1Util.list(asn1Encodable)
                val blocks: MutableList<ExtendedDataBlock?> = ArrayList<ExtendedDataBlock?>(blockASN1Objects.size)
                for (blockASN1Object in blockASN1Objects) {
                    blocks.add(ExtendedDataBlock(blockASN1Object))
                }
                return blocks
            } else {
                return mutableListOf<ExtendedDataBlock?>(ExtendedDataBlock(asn1Encodable))
            }
        }
    }
}
