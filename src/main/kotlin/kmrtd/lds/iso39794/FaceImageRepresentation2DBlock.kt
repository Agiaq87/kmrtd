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
 * $Id: FaceImageRepresentation2DBlock.java 1889 2025-03-15 21:09:22Z martijno $
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
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DEROctetString
import org.jmrtd.ASN1Util
import org.jmrtd.lds.iso39794.ImageDataFormatCode.Companion.toMimeType
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.Objects

data class FaceImageRepresentation2DBlock(
    val representationData2DBytes: ByteArray?,
    val imageInformation2DBlock: FaceImageInformation2DBlock,
    val captureDevice2DBlock: FaceImageCaptureDevice2DBlock?
) : Block() {
    /*private val representationData2DBytes: ByteArray?

    @JvmField
    val imageInformation2DBlock: FaceImageInformation2DBlock

    var captureDevice2DBlock: FaceImageCaptureDevice2DBlock? = null
        private set

    constructor(
        representationData2DBytes: ByteArray?,
        imageInformation2DBlock: FaceImageInformation2DBlock,
        captureDevice2DBlock: FaceImageCaptureDevice2DBlock?
    ) {
        this.representationData2DBytes = representationData2DBytes
        this.imageInformation2DBlock = imageInformation2DBlock
        this.captureDevice2DBlock = captureDevice2DBlock
    }*/

    //  ImageRepresentation2DBlock ::= SEQUENCE {
    //    representationData2D [0] OCTET STRING,
    //    imageInformation2DBlock [1] ImageInformation2DBlock,
    //    captureDevice2DBlock [2] CaptureDevice2DBlock OPTIONAL,
    //    ...
    //  }
    /*internal constructor(asn1Encodable: ASN1Encodable?) {
        require(!(asn1Encodable !is ASN1Sequence && asn1Encodable !is ASN1TaggedObject)) { "Cannot decode!" }

        val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)
        representationData2DBytes = ASN1OctetString.getInstance(taggedObjects[0]).octets
        imageInformation2DBlock = FaceImageInformation2DBlock(taggedObjects.get(1))
        if (taggedObjects.containsKey(2)) {
            captureDevice2DBlock = FaceImageCaptureDevice2DBlock(taggedObjects[2])
        }
    }*/

    val representationData2DInputLength: Long
        get() = (representationData2DBytes?.size ?: 0).toLong()

    val representationData2DInputMimeType: String
        get() = toMimeType(imageInformation2DBlock.getImageDataFormatCode())

    val representationData2DInputStream: InputStream
        get() = ByteArrayInputStream(representationData2DBytes)

    /*public override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + representationData2DBytes.contentHashCode()
        result = prime * result + Objects.hash(captureDevice2DBlock, imageInformation2DBlock)
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

        val other = obj as FaceImageRepresentation2DBlock
        return captureDevice2DBlock == other.captureDevice2DBlock
                && imageInformation2DBlock == other.imageInformation2DBlock
                && representationData2DBytes.contentEquals(other.representationData2DBytes)
    }

    override fun toString(): String {
        return ("FaceImageRepresentation2DBlock ["
                + "representationData2DBytes: " + (if (representationData2DBytes == null) "-" else representationData2DBytes.size)
                + ", imageInformation2DBlock: " + imageInformation2DBlock
                + ", captureDevice2DBlock: " + captureDevice2DBlock
                + "]")
    }*/

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FaceImageRepresentation2DBlock

        if (!representationData2DBytes.contentEquals(other.representationData2DBytes)) return false
        if (imageInformation2DBlock != other.imageInformation2DBlock) return false
        if (captureDevice2DBlock != other.captureDevice2DBlock) return false
        if (representationData2DInputLength != other.representationData2DInputLength) return false
        if (representationData2DInputMimeType != other.representationData2DInputMimeType) return false
        if (representationData2DInputStream != other.representationData2DInputStream) return false
        if (aSN1Object != other.aSN1Object) return false

        return true
    }

    override fun hashCode(): Int {
        var result = representationData2DBytes?.contentHashCode() ?: 0
        result = 31 * result + imageInformation2DBlock.hashCode()
        result = 31 * result + (captureDevice2DBlock?.hashCode() ?: 0)
        result = 31 * result + representationData2DInputLength.hashCode()
        result = 31 * result + representationData2DInputMimeType.hashCode()
        result = 31 * result + representationData2DInputStream.hashCode()
        result = 31 * result + aSN1Object.hashCode()
        return result
    }

    override val aSN1Object: ASN1Encodable
        get() = ASN1Util.encodeTaggedObjects(buildMap {
            put(0, DEROctetString(representationData2DBytes))
            put(1, imageInformation2DBlock.aSN1Object)
            captureDevice2DBlock?.let {
                put(2, it.aSN1Object)
            }
        })
        /* PACKAGE */
        /*get() {
            val taggedObjects: MutableMap<Int?, ASN1Encodable?> =
                HashMap<Int?, ASN1Encodable?>()
            taggedObjects[0] = DEROctetString(representationData2DBytes)
            taggedObjects[1] = imageInformation2DBlock.aSN1Object
            if (captureDevice2DBlock != null) {
                taggedObjects[2] = captureDevice2DBlock!!.aSN1Object
            }
            return ASN1Util.encodeTaggedObjects(taggedObjects)
        }*/

    companion object {
        private const val serialVersionUID = 1942286473160393593L

        /**
         * Factory method
         *
         * ImageRepresentation2DBlock ::= SEQUENCE {
         *   representationData2D [0] OCTET STRING,
         *   imageInformation2DBlock [1] ImageInformation2DBlock,
         *   captureDevice2DBlock [2] CaptureDevice2DBlock OPTIONAL,
         *   ...
         * }
         */
        @JvmStatic
        fun from(asn1Encodable: ASN1Encodable?): FaceImageRepresentation2DBlock {
            require(!(asn1Encodable !is ASN1Sequence && asn1Encodable !is ASN1TaggedObject)) { "Cannot decode!" }

            val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)

            return FaceImageRepresentation2DBlock(
                representationData2DBytes = ASN1OctetString.getInstance(taggedObjects[0]).octets,
                imageInformation2DBlock = FaceImageInformation2DBlock.from(taggedObjects[1]),
                captureDevice2DBlock = if (taggedObjects.containsKey(2)) FaceImageCaptureDevice2DBlock(taggedObjects[2]) else null
            )
        }
    }
}
