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
 * $Id: FaceImageRepresentationBlock.java 1898 2025-06-04 12:05:45Z martijno $
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

import kmrtd.lds.ImageInfo
import kmrtd.lds.iso39794.FaceImageLandmarkBlock.Companion.decodeLandmarkBlocks
import kmrtd.lds.iso39794.PADDataBlock.Companion.decodePADDataBlocks
import org.bouncycastle.asn1.ASN1Encodable
import org.jmrtd.ASN1Util
import java.math.BigInteger
import java.util.*

class FaceImageRepresentationBlock : Block, ImageInfo {
    val representationId: BigInteger

    val imageRepresentation2DBlock: FaceImageRepresentation2DBlock?

    var captureDateTimeBlock: DateTimeBlock? = null
        private set

    var qualityBlocks: MutableList<QualityBlock?>? = null
        private set

    var padDataBlocks: MutableList<PADDataBlock?>? = null
        private set

    var sessionId: BigInteger? = null
        private set

    var derivedFrom: BigInteger? = null
        private set

    var captureDeviceBlock: FaceImageCaptureDeviceBlock? = null
        private set

    var identityMetadataBlock: FaceImageIdentityMetadataBlock? = null
        private set

    var landmarkBlocks: MutableList<FaceImageLandmarkBlock?>? = null
        private set

    constructor(
        representationId: BigInteger,
        imageRepresentation2DBlock: FaceImageRepresentation2DBlock?,
        captureDateTimeBlock: DateTimeBlock?,
        qualityBlocks: MutableList<QualityBlock?>?,
        padDataBlocks: MutableList<PADDataBlock?>?,
        sessionId: BigInteger?,
        derivedFrom: BigInteger?,
        captureDeviceBlock: FaceImageCaptureDeviceBlock?,
        identityMetadataBlock: FaceImageIdentityMetadataBlock?,
        landmarkBlocks: MutableList<FaceImageLandmarkBlock?>?
    ) {
        this.representationId = representationId
        this.imageRepresentation2DBlock = imageRepresentation2DBlock
        this.captureDateTimeBlock = captureDateTimeBlock
        this.qualityBlocks = qualityBlocks
        this.padDataBlocks = padDataBlocks
        this.sessionId = sessionId
        this.derivedFrom = derivedFrom
        this.captureDeviceBlock = captureDeviceBlock
        this.identityMetadataBlock = identityMetadataBlock
        this.landmarkBlocks = landmarkBlocks
    }

    //  RepresentationBlock ::= SEQUENCE {
    //    representationId [0] INTEGER (0..MAX),
    //    imageRepresentation [1] ImageRepresentation,
    //    captureDateTimeBlock [2] CaptureDateTimeBlock OPTIONAL,
    //    qualityBlocks [3] QualityBlocks OPTIONAL,
    //    padDataBlock [4] PADDataBlock OPTIONAL,
    //    sessionId [5] INTEGER (0..MAX) OPTIONAL,
    //    derivedFrom [6] INTEGER (0..MAX) OPTIONAL,
    //    captureDeviceBlock [7] CaptureDeviceBlock OPTIONAL,
    //    identityMetadataBlock [8] IdentityMetadataBlock OPTIONAL,
    //    landmarkBlocks [9] LandmarkBlocks OPTIONAL,
    //    ...
    //  }
    internal constructor(asn1Encodable: ASN1Encodable?) {
        val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)
        representationId = ASN1Util.decodeBigInteger(taggedObjects.get(0))
        imageRepresentation2DBlock = decodeImageRepresentation2DBlock(taggedObjects.get(1))
        if (taggedObjects.containsKey(2)) {
            captureDateTimeBlock = DateTimeBlock(taggedObjects.get(2))
        }
        if (taggedObjects.containsKey(3)) {
            qualityBlocks = QualityBlock.decodeQualityBlocks(taggedObjects.get(3))
        }
        if (taggedObjects.containsKey(4)) {
            padDataBlocks = decodePADDataBlocks(taggedObjects.get(4))
        }
        if (taggedObjects.containsKey(5)) {
            sessionId = ASN1Util.decodeBigInteger(taggedObjects.get(5))
        }
        if (taggedObjects.containsKey(6)) {
            derivedFrom = ASN1Util.decodeBigInteger(taggedObjects.get(6))
        }
        if (taggedObjects.containsKey(7)) {
            captureDeviceBlock = FaceImageCaptureDeviceBlock(taggedObjects.get(7)!!)
        }
        if (taggedObjects.containsKey(8)) {
            identityMetadataBlock = FaceImageIdentityMetadataBlock(taggedObjects.get(8)!!)
        }
        if (taggedObjects.containsKey(9)) {
            landmarkBlocks = decodeLandmarkBlocks(taggedObjects.get(9))
        }
    }

    override fun hashCode(): Int {
        return Objects.hash(
            captureDateTimeBlock, captureDeviceBlock, derivedFrom, identityMetadataBlock,
            imageRepresentation2DBlock, landmarkBlocks, padDataBlocks, qualityBlocks, representationId, sessionId
        )
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

        val other = obj as FaceImageRepresentationBlock
        return captureDateTimeBlock == other.captureDateTimeBlock
                && captureDeviceBlock == other.captureDeviceBlock
                && derivedFrom == other.derivedFrom
                && identityMetadataBlock == other.identityMetadataBlock
                && imageRepresentation2DBlock == other.imageRepresentation2DBlock
                && landmarkBlocks == other.landmarkBlocks && padDataBlocks == other.padDataBlocks
                && qualityBlocks == other.qualityBlocks
                && representationId == other.representationId && sessionId == other.sessionId
    }

    override fun toString(): String {
        return ("FaceImageRepresentationBlock ["
                + "representationId: " + representationId
                + ", imageRepresentation: " + imageRepresentation2DBlock
                + ", captureDateTimeBlock: " + captureDateTimeBlock
                + ", qualityBlocks: " + qualityBlocks
                + ", padDataBlocks: " + padDataBlocks
                + ", sessionId: " + sessionId
                + ", derivedFrom: " + derivedFrom
                + ", captureDeviceBlock: " + captureDeviceBlock
                + ", identityMetadataBlock: " + identityMetadataBlock
                + ", landmarkBlocks: " + landmarkBlocks
                + "]")
    }


    val type: Int
        get() = ImageInfo.Companion.TYPE_PORTRAIT

    val mimeType: String?
        get() {
            if (imageRepresentation2DBlock == null) {
                return "image/raw"
            }
            return imageRepresentation2DBlock.representationData2DInputMimeType
        }

    val width: Int
        get() {
            if (imageRepresentation2DBlock == null) {
                return 0
            }
            val faceImageInformation2DBlock: FaceImageInformation2DBlock? =
                imageRepresentation2DBlock.imageInformation2DBlock
            if (faceImageInformation2DBlock == null) {
                return 0
            }
            val imageSizeBlock =
                faceImageInformation2DBlock.imageSizeBlock
            if (imageSizeBlock == null) {
                return 0
            }
            return imageSizeBlock.width
        }

    val height: Int
        get() {
            if (imageRepresentation2DBlock == null) {
                return 0
            }
            val faceImageInformation2DBlock: FaceImageInformation2DBlock? =
                imageRepresentation2DBlock.imageInformation2DBlock
            if (faceImageInformation2DBlock == null) {
                return 0
            }
            val imageSizeBlock =
                faceImageInformation2DBlock.imageSizeBlock
            if (imageSizeBlock == null) {
                return 0
            }
            return imageSizeBlock.height
        }

    val recordLength: Long
        get() = 0

    val imageLength: Int
        get() {
            if (imageRepresentation2DBlock == null) {
                return 0
            }
            return imageRepresentation2DBlock.representationData2DInputLength.toInt()
        }

    val imageInputStream: InputStream
        get() {
            if (imageRepresentation2DBlock == null) {
                return null
            }
            return imageRepresentation2DBlock.representationData2DInputStream
        }

    val aSN1Object: ASN1Encodable?
        /* PACKAGE */
        get() {
            val taggedObjects: MutableMap<Int?, ASN1Encodable?> =
                HashMap<Int?, ASN1Encodable?>()
            taggedObjects.put(0, ASN1Util.encodeBigInteger(representationId))
            taggedObjects.put(
                1,
                Companion.encodeImageRepresentation2DBlock(
                    imageRepresentation2DBlock!!
                )
            )
            if (captureDateTimeBlock != null) {
                taggedObjects.put(2, captureDateTimeBlock!!.aSN1Object)
            }
            if (qualityBlocks != null) {
                taggedObjects.put(3, ISO39794Util.encodeBlocks(qualityBlocks))
            }
            if (padDataBlocks != null) {
                taggedObjects.put(4, ISO39794Util.encodeBlocks(padDataBlocks))
            }
            if (sessionId != null) {
                taggedObjects.put(5, ASN1Util.encodeBigInteger(sessionId))
            }
            if (derivedFrom != null) {
                taggedObjects.put(6, ASN1Util.encodeBigInteger(derivedFrom))
            }
            if (captureDeviceBlock != null) {
                taggedObjects.put(7, captureDeviceBlock!!.aSN1Object)
            }
            if (identityMetadataBlock != null) {
                taggedObjects.put(8, identityMetadataBlock!!.getASN1Object())
            }
            if (landmarkBlocks != null) {
                taggedObjects.put(9, ISO39794Util.encodeBlocks(landmarkBlocks))
            }
            return ASN1Util.encodeTaggedObjects(taggedObjects)
        }

    companion object {
        private val serialVersionUID = -8372278398595506771L

        // RepresentationBlocks ::= SEQUENCE SIZE (1) OF RepresentationBlock
        fun decodeRepresentationBlocks(asn1Encodable: ASN1Encodable?): MutableList<FaceImageRepresentationBlock?> {
            val blocks: MutableList<FaceImageRepresentationBlock?> = ArrayList<FaceImageRepresentationBlock?>()
            if (ASN1Util.isSequenceOfSequences(asn1Encodable)) {
                val blockASN1Objects = ASN1Util.list(asn1Encodable)
                for (blockASN1Object in blockASN1Objects) {
                    blocks.add(FaceImageRepresentationBlock(blockASN1Object))
                }
            } else {
                blocks.add(FaceImageRepresentationBlock(asn1Encodable))
            }

            return blocks
        }

        /* PRIVATE */ //  ImageRepresentation ::= CHOICE {
        //    base [0] ImageRepresentationBase,
        //    extensionBlock [1] ImageRepresentationExtensionBlock
        //  }
        //
        //  ImageRepresentationBase ::= CHOICE {
        //    imageRepresentation2DBlock [0] ImageRepresentation2DBlock
        //  }
        //
        //  ImageRepresentationExtensionBlock ::= SEQUENCE {
        //    ...
        //  }
        private fun decodeImageRepresentation2DBlock(asn1Encodable: ASN1Encodable?): FaceImageRepresentation2DBlock? {
            val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)
            if (taggedObjects.containsKey(0)) {
                val baseTaggedObjects = ASN1Util.decodeTaggedObjects(taggedObjects.get(0))
                if (baseTaggedObjects.containsKey(0)) {
                    return FaceImageRepresentation2DBlock(baseTaggedObjects.get(0))
                }

                /* NOTE: Not supporting [1] ShapeRepresentation3DBlock... */
            }

            return null
        }

        private fun encodeImageRepresentation2DBlock(faceImageRepresentation2DBlock: FaceImageRepresentation2DBlock): ASN1Encodable? {
            val baseTaggedObjects: MutableMap<Int?, ASN1Encodable?> = HashMap<Int?, ASN1Encodable?>()
            baseTaggedObjects.put(0, faceImageRepresentation2DBlock.aSN1Object)

            val taggedObjects: MutableMap<Int?, ASN1Encodable?> = HashMap<Int?, ASN1Encodable?>()
            taggedObjects.put(0, ASN1Util.encodeTaggedObjects(baseTaggedObjects))
            return ASN1Util.encodeTaggedObjects(taggedObjects)
        }
    }
}
