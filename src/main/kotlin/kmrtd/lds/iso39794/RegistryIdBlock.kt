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
 * $Id: RegistryIdBlock.java 1889 2025-03-15 21:09:22Z martijno $
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
import org.bouncycastle.asn1.ASN1Sequence
import org.jmrtd.ASN1Util

data class RegistryIdBlock(
    val organization: Int,
    val id: Int
) : Block() {
    /*constructor(organization: Int, id: Int) {
        this.organization = organization
        this.id = id
    }*/

    /*internal constructor(asn1Encodable: ASN1Encodable?) {
        require(asn1Encodable is ASN1Sequence) { "Cannot decode!" }

        val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)
        val organization = ASN1Util.decodeInt(taggedObjects[0])
        val id = ASN1Util.decodeInt(taggedObjects[1])
    }*/

    /*public override fun hashCode(): Int {
        return Objects.hash(id, organization)
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

        val other = obj as RegistryIdBlock
        return id == other.id && organization == other.organization
    }

    override fun toString(): String {
        return "RegistryIdBlock[organization: $organization, id: $id]"
    }*/

    override val aSN1Object: ASN1Encodable
        get() = ASN1Util.encodeTaggedObjects(
            mapOf(
                0 to ASN1Util.encodeInt(organization),
                1 to ASN1Util.encodeInt(id)
            )
        )
    /*get() {
        val taggedObjects: MutableMap<Int, ASN1Encodable> =
            HashMap()
        taggedObjects[0] = ASN1Util.encodeInt(organization)
        taggedObjects[1] = ASN1Util.encodeInt(id)
        return ASN1Util.encodeTaggedObjects(taggedObjects)
    }*/

    companion object {
        private const val serialVersionUID = -5942649140248216405L

        // RegistryId ::= INTEGER (1..65535)
        //
        // RegistryIdBlock ::= SEQUENCE {
        //        organization            [0] RegistryId,
        //        id                      [1] RegistryId
        // }
        // CertificationIdBlock ::= RegistryIdBlock
        //
        // CertificationIdBlocks ::= SEQUENCE OF CertificationIdBlock
        @JvmStatic
        fun decodeRegistryIdBlocks(asn1Encodable: ASN1Encodable): List<RegistryIdBlock> =
            if (ASN1Util.isSequenceOfSequences(asn1Encodable)) {
                ASN1Util.list(asn1Encodable).map { from(it) }
            } else {
                listOf(from(asn1Encodable))
            }

        /**
         * Factory method
         *
         * RegistryId ::= INTEGER (1..65535)
         * RegistryIdBlock ::= SEQUENCE {
         *   organization            [0] RegistryId,
         *   id                      [1] RegistryId
         * }
         */
        @JvmStatic
        fun from(asn1Encodable: ASN1Encodable?): RegistryIdBlock {
            require(asn1Encodable is ASN1Sequence) { "Cannot decode!" }

            val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)

            return RegistryIdBlock(
                organization = ASN1Util.decodeInt(taggedObjects[0]),
                id = ASN1Util.decodeInt(taggedObjects[1])
            )
        }
    }
}
