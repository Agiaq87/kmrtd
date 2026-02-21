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
 * $Id: FaceImageInformation2DBlock.java 1901 2025-07-15 12:31:11Z martijno $
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

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import kmrtd.ASN1Util
import java.math.BigInteger
import java.util.*

class FaceImageInformation2DBlock : Block {
    enum class ImageDataFormatCode(override val code: Int, val mimeType: String) : EncodableEnum<ImageDataFormatCode?> {
        UNKNOWN(0, "image/raw"),
        JPEG(2, "image/jpeg"),
        JPEG2000_LOSSY(3, "image/jp2"),
        JPEG2000_LOSSLESS(4, "image/jp2");

        companion object {
            fun fromCode(code: Int): ImageDataFormatCode {
                return EncodableEnum.fromCode(code, ImageDataFormatCode::class.java)
            }

            fun toMimeType(imageDataFormatCode: ImageDataFormatCode?): String? {
                if (imageDataFormatCode == null) {
                    return "image/raw"
                }
                return imageDataFormatCode.mimeType
            }
        }
    }

    enum class FaceImageKind2DCode(override val code: Int) : EncodableEnum<FaceImageKind2DCode?> {
        MRTD(0),
        GENERAL_PURPOSE(1);

        companion object {
            fun fromCode(code: Int): FaceImageKind2DCode {
                return EncodableEnum.fromCode(code, FaceImageKind2DCode::class.java)
            }
        }
    }

    enum class LossyTransformationAttemptsCode(override val code: Int) : EncodableEnum<LossyTransformationAttemptsCode?> {
        UNKNOWN(0),
        ZERO(1),
        ONE(2),
        MORE_THAN_ONE(3);

        companion object {
            fun fromCode(code: Int): LossyTransformationAttemptsCode {
                return EncodableEnum.fromCode(code, LossyTransformationAttemptsCode::class.java)
            }
        }
    }

    enum class ImageColourSpaceCode(override val code: Int) : EncodableEnum<ImageColourSpaceCode?> {
        UNKNOWN(0),
        OTHER(1),
        RGB_24BIT(2),
        RGB_48BIT(3),
        YUV_422(4),
        GREYSCALE_8BIT(5),
        GREYSCALE_16BIT(6);

        companion object {
            fun fromCode(code: Int): ImageColourSpaceCode {
                return EncodableEnum.fromCode(code, ImageColourSpaceCode::class.java)
            }
        }
    }

    //  ImageSizeBlock ::= SEQUENCE {
    //    width [0] ImageSize,
    //    height [1] ImageSize
    //  }
    class ImageSizeBlock : Block {
        @JvmField
        val width: Int
        @JvmField
        val height: Int

        constructor(width: Int, height: Int) {
            this.width = width
            this.height = height
        }

        constructor(asn1Encodable: ASN1Encodable?) {
            require(asn1Encodable is ASN1Sequence) { "Cannot decode!" }

            val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)
            width = ASN1Util.decodeInt(taggedObjects.get(0))
            height = ASN1Util.decodeInt(taggedObjects.get(1))
        }

        public override fun hashCode(): Int {
            return Objects.hash(height, width)
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

            val other = obj as ImageSizeBlock
            return height == other.height && width == other.width
        }

        override fun toString(): String {
            return "ImageSizeBlock [width: $width, height: $height]"
        }

        val aSN1Object: ASN1Encodable?
            get() {
                val taggedObjects: MutableMap<Int?, ASN1Encodable?> =
                    HashMap<Int?, ASN1Encodable?>()
                taggedObjects[0] = ASN1Util.encodeInt(width)
                taggedObjects[1] = ASN1Util.encodeInt(height)
                return ASN1Util.encodeTaggedObjects(taggedObjects)
            }

        companion object {
            private val serialVersionUID = -261040653361008230L
        }
    }

    class ImageFaceMeasurementsBlock : Block {
        var imageHeadWidth: BigInteger? = null
            private set
        var imageInterEyeDistance: BigInteger? = null
            private set
        var imageEyeToMouthDistance: BigInteger? = null
            private set
        var imageHeadLength: BigInteger? = null
            private set

        constructor(
            imageHeadWidth: BigInteger?, imageInterEyeDistance: BigInteger?,
            imageEyeToMouthDistance: BigInteger?, imageHeadLength: BigInteger?
        ) {
            this.imageHeadWidth = imageHeadWidth
            this.imageInterEyeDistance = imageInterEyeDistance
            this.imageEyeToMouthDistance = imageEyeToMouthDistance
            this.imageHeadLength = imageHeadLength
        }

        internal constructor(asn1Encodable: ASN1Encodable?) {
            val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)
            if (taggedObjects.containsKey(0)) {
                imageHeadWidth = ASN1Util.decodeBigInteger(taggedObjects[0])
            }
            if (taggedObjects.containsKey(1)) {
                imageInterEyeDistance = ASN1Util.decodeBigInteger(taggedObjects[1])
            }
            if (taggedObjects.containsKey(2)) {
                imageEyeToMouthDistance = ASN1Util.decodeBigInteger(taggedObjects[2])
            }
            if (taggedObjects.containsKey(3)) {
                imageHeadLength = ASN1Util.decodeBigInteger(taggedObjects[3])
            }
        }

        public override fun hashCode(): Int {
            return Objects.hash(imageEyeToMouthDistance, imageHeadLength, imageHeadWidth, imageInterEyeDistance)
        }

        public override fun equals(obj: Any?): Boolean {
            if (this === obj) return true
            if (obj == null) return false
            if (javaClass != obj.javaClass) return false
            val other = obj as ImageFaceMeasurementsBlock
            return imageEyeToMouthDistance == other.imageEyeToMouthDistance
                    && imageHeadLength == other.imageHeadLength
                    && imageHeadWidth == other.imageHeadWidth
                    && imageInterEyeDistance == other.imageInterEyeDistance
        }

        override fun toString(): String {
            return ("ImageFaceMeasurementsBlock ["
                    + "imageHeadWidth: " + imageHeadWidth
                    + ", imageInterEyeDistance: " + imageInterEyeDistance
                    + ", imageEyeToMouthDistance: " + imageEyeToMouthDistance
                    + ", imageHeadLength: " + imageHeadLength
                    + "]")
        }

        override val aSN1Object: ASN1Encodable?
            /* PACKAGE */
            get() {
                val taggedObjects: MutableMap<Int?, ASN1Encodable?> =
                    HashMap<Int?, ASN1Encodable?>()
                if (imageHeadWidth != null) {
                    taggedObjects[0] = ASN1Util.encodeBigInteger(imageHeadWidth)
                }
                if (imageInterEyeDistance != null) {
                    taggedObjects[1] = ASN1Util.encodeBigInteger(imageInterEyeDistance)
                }
                if (imageEyeToMouthDistance != null) {
                    taggedObjects[2] = ASN1Util.encodeBigInteger(imageEyeToMouthDistance)
                }
                if (imageHeadLength != null) {
                    taggedObjects[3] = ASN1Util.encodeBigInteger(imageHeadLength)
                }
                return ASN1Util.encodeTaggedObjects(taggedObjects)
            }

        companion object {
            private val serialVersionUID = -5665022845073986540L
        }
    }

    var imageSizeBlock: ImageSizeBlock? = null
        private set

    private var imageDataFormatCode: ImageDataFormatCode? = null

    var faceImageKind2DCode: FaceImageKind2DCode? = null
        private set

    var postAcquisitionProcessingBlock: FaceImagePostAcquisitionProcessingBlock? = null
        private set

    var lossyTransformationAttemptsCode: LossyTransformationAttemptsCode? = null
        private set

    var cameraToSubjectDistance: Int? = null
        private set

    var sensorDiagonal: Int? = null
        private set

    var lensFocalLength: Int? = null
        private set

    var imageFaceMeasurementsBlock: ImageFaceMeasurementsBlock? = null
        private set

    var imageColourSpaceCode: ImageColourSpaceCode? = null
        private set

    var referenceColourMappingBlock: FaceImageReferenceColourMappingBlock? = null
        private set

    constructor(
        imageDataFormatCode: ImageDataFormatCode,
        faceImageKind2DCode: FaceImageKind2DCode?,
        postAcquisitionProcessingBlock: FaceImagePostAcquisitionProcessingBlock?,
        lossyTransformationAttemptsCode: LossyTransformationAttemptsCode?,
        cameraToSubjectDistance: Int?,
        sensorDiagonal: Int?,
        lensFocalLength: Int?,
        imageSizeBlock: ImageSizeBlock?,
        imageFaceMeasurementsBlock: ImageFaceMeasurementsBlock?,
        imageColourSpaceCode: ImageColourSpaceCode?,
        referenceColourMappingBlock: FaceImageReferenceColourMappingBlock?
    ) {
        this.imageDataFormatCode = imageDataFormatCode
        this.faceImageKind2DCode = faceImageKind2DCode
        this.postAcquisitionProcessingBlock = postAcquisitionProcessingBlock
        this.lossyTransformationAttemptsCode = lossyTransformationAttemptsCode
        this.cameraToSubjectDistance = cameraToSubjectDistance
        this.sensorDiagonal = sensorDiagonal
        this.lensFocalLength = lensFocalLength
        this.imageSizeBlock = imageSizeBlock
        this.imageFaceMeasurementsBlock = imageFaceMeasurementsBlock
        this.imageColourSpaceCode = imageColourSpaceCode
        this.referenceColourMappingBlock = referenceColourMappingBlock
    }

    //  ImageInformation2DBlock ::= SEQUENCE {
    //    imageDataFormat [0] ImageDataFormat,
    //    faceImageKind2D [1] FaceImageKind2D OPTIONAL,
    //    postAcquisitionProcessingBlock [2] PostAcquisitionProcessingBlock OPTIONAL,
    //    lossyTransformationAttempts [3] LossyTransformationAttempts OPTIONAL,
    //    cameraToSubjectDistance [4] CameraToSubjectDistance OPTIONAL,
    //    sensorDiagonal [5] SensorDiagonal OPTIONAL,
    //    lensFocalLength [6] LensFocalLength OPTIONAL,
    //    imageSizeBlock [7] ImageSizeBlock OPTIONAL,
    //    imageFaceMeasurementsBlock [8] ImageFaceMeasurementsBlock OPTIONAL,
    //    imageColourSpace [9] ImageColourSpace OPTIONAL,
    //    referenceColourMappingBlock [10] ReferenceColourMappingBlock OPTIONAL,
    //    ...
    //  }
    internal constructor(asn1Encodable: ASN1Encodable?) {
        require(!(asn1Encodable !is ASN1Sequence && asn1Encodable !is ASN1TaggedObject)) { "Cannot decode!" }

        val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)
        if (taggedObjects.containsKey(0)) {
            imageDataFormatCode = ImageDataFormatCode.Companion.fromCode(
                ISO39794Util.decodeCodeFromChoiceExtensionBlockFallback(taggedObjects.get(0))
            )
        }
        if (taggedObjects.containsKey(1)) {
            faceImageKind2DCode = FaceImageKind2DCode.Companion.fromCode(
                ISO39794Util.decodeCodeFromChoiceExtensionBlockFallback(taggedObjects.get(1))
            )
        }
        if (taggedObjects.containsKey(2)) {
            postAcquisitionProcessingBlock = FaceImagePostAcquisitionProcessingBlock(taggedObjects.get(2))
        }
        if (taggedObjects.containsKey(3)) {
            lossyTransformationAttemptsCode = LossyTransformationAttemptsCode.Companion.fromCode(
                ISO39794Util.decodeCodeFromChoiceExtensionBlockFallback(taggedObjects.get(3))
            )
        }
        if (taggedObjects.containsKey(4)) {
            cameraToSubjectDistance = ASN1Util.decodeInt(taggedObjects.get(4))
        }
        if (taggedObjects.containsKey(5)) {
            sensorDiagonal = ASN1Util.decodeInt(taggedObjects.get(5))
        }
        if (taggedObjects.containsKey(6)) {
            lensFocalLength = ASN1Util.decodeInt(taggedObjects.get(6))
        }
        if (taggedObjects.containsKey(7)) {
            imageSizeBlock = ImageSizeBlock(taggedObjects.get(7))
        }
        if (taggedObjects.containsKey(8)) {
            imageFaceMeasurementsBlock = ImageFaceMeasurementsBlock(taggedObjects.get(8))
        }
        if (taggedObjects.containsKey(9)) {
            imageColourSpaceCode = ImageColourSpaceCode.Companion.fromCode(
                ISO39794Util.decodeCodeFromChoiceExtensionBlockFallback(taggedObjects.get(9))
            )
        }
        if (taggedObjects.containsKey(10)) {
            referenceColourMappingBlock = FaceImageReferenceColourMappingBlock(taggedObjects.get(10))
        }
    }

    /**
     * Returns the image data format.
     * One of `JPEG`, `JPEG2000_LOSSY`, or `JPEG2000_LOSSLESS`.
     * 
     * @return the image data format
     */
    fun getImageDataFormatCode(): ImageDataFormatCode {
        return imageDataFormatCode!!
    }

    public override fun hashCode(): Int {
        return Objects.hash(
            cameraToSubjectDistance, faceImageKind2DCode, imageColourSpaceCode, imageDataFormatCode,
            imageFaceMeasurementsBlock, imageSizeBlock, lensFocalLength, lossyTransformationAttemptsCode,
            postAcquisitionProcessingBlock, referenceColourMappingBlock, sensorDiagonal
        )
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

        val other = obj as FaceImageInformation2DBlock
        return cameraToSubjectDistance == other.cameraToSubjectDistance
                && faceImageKind2DCode == other.faceImageKind2DCode && imageColourSpaceCode == other.imageColourSpaceCode && imageDataFormatCode == other.imageDataFormatCode && imageFaceMeasurementsBlock == other.imageFaceMeasurementsBlock
                && imageSizeBlock == other.imageSizeBlock
                && lensFocalLength == other.lensFocalLength
                && lossyTransformationAttemptsCode == other.lossyTransformationAttemptsCode && postAcquisitionProcessingBlock == other.postAcquisitionProcessingBlock
                && referenceColourMappingBlock == other.referenceColourMappingBlock
                && sensorDiagonal == other.sensorDiagonal
    }

    override fun toString(): String {
        return ("FaceImageInformation2DBlock ["
                + "imageSizeBlock: " + imageSizeBlock
                + ", imageDataFormatCode: " + imageDataFormatCode
                + ", faceImageKind2DCode: " + faceImageKind2DCode
                + ", postAcquisitionProcessingBlock: " + postAcquisitionProcessingBlock
                + ", lossyTransformationAttemptsCode: " + lossyTransformationAttemptsCode
                + ", cameraToSubjectDistance: " + cameraToSubjectDistance
                + ", sensorDiagonal: " + sensorDiagonal
                + ", lensFocalLength: " + lensFocalLength
                + ", imageFaceMeasurementsBlock: " + imageFaceMeasurementsBlock
                + ", imageColourSpaceCode: " + imageColourSpaceCode
                + ", referenceColourMappingBlock: " + referenceColourMappingBlock
                + "]")
    }

    override val aSN1Object: ASN1Encodable?
        /* PACAKAGE */
        get() {
            val taggedObjects: MutableMap<Int?, ASN1Encodable?> =
                HashMap<Int?, ASN1Encodable?>()
            taggedObjects[0] = ISO39794Util.encodeCodeAsChoiceExtensionBlockFallback(imageDataFormatCode!!.code)
            if (faceImageKind2DCode != null) {
                taggedObjects[1] = ISO39794Util.encodeCodeAsChoiceExtensionBlockFallback(faceImageKind2DCode!!.code)
            }
            if (postAcquisitionProcessingBlock != null) {
                taggedObjects[2] = postAcquisitionProcessingBlock!!.getASN1Object()
            }
            if (lossyTransformationAttemptsCode != null) {
                taggedObjects[3] = ISO39794Util.encodeCodeAsChoiceExtensionBlockFallback(
                    lossyTransformationAttemptsCode!!.code
                )
            }
            if (cameraToSubjectDistance != null) {
                taggedObjects[4] = ASN1Util.encodeInt(cameraToSubjectDistance!!)
            }
            if (sensorDiagonal != null) {
                taggedObjects[5] = ASN1Util.encodeInt(sensorDiagonal!!)
            }
            if (lensFocalLength != null) {
                taggedObjects[6] = ASN1Util.encodeInt(lensFocalLength!!)
            }
            if (imageSizeBlock != null) {
                taggedObjects[7] = imageSizeBlock!!.aSN1Object
            }
            if (imageFaceMeasurementsBlock != null) {
                taggedObjects[8] = imageFaceMeasurementsBlock!!.aSN1Object
            }
            if (imageColourSpaceCode != null) {
                taggedObjects[9] = ISO39794Util.encodeCodeAsChoiceExtensionBlockFallback(imageColourSpaceCode!!.code)
            }
            if (referenceColourMappingBlock != null) {
                taggedObjects[10] = referenceColourMappingBlock!!.getASN1Object()
            }
            return ASN1Util.encodeTaggedObjects(taggedObjects)
        }

    companion object {
        private const val serialVersionUID = 76880187801114756L
    }
}
