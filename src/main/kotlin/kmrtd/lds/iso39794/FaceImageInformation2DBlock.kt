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
package org.jmrtd.lds.iso39794

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.jmrtd.ASN1Util

data class FaceImageInformation2DBlock(
    val imageSizeBlock: ImageSizeBlock?,
    private val imageDataFormatCode: ImageDataFormatCode?,
    val faceImageKind2DCode: FaceImageKind2DCode?,
    val postAcquisitionProcessingBlock: FaceImagePostAcquisitionProcessingBlock?,
    val lossyTransformationAttemptsCode: LossyTransformationAttemptsCode?,
    val cameraToSubjectDistance: Int?,
    val sensorDiagonal: Int?,
    val lensFocalLength: Int?,
    val imageFaceMeasurementsBlock: ImageFaceMeasurementsBlock?,
    val imageColourSpaceCode: ImageColourSpaceCode?,
    val referenceColourMappingBlock: FaceImageReferenceColourMappingBlock?
) : Block() {
    /*constructor(
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
    }*/

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
    /*internal constructor(asn1Encodable: ASN1Encodable?) {
        require(!(asn1Encodable !is ASN1Sequence && asn1Encodable !is ASN1TaggedObject)) { "Cannot decode!" }

        val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)
        if (taggedObjects.containsKey(0)) {
            imageDataFormatCode = ImageDataFormatCode.fromCode(
                ISO39794Util.decodeCodeFromChoiceExtensionBlockFallback(taggedObjects[0])
            )
        }
        if (taggedObjects.containsKey(1)) {
            faceImageKind2DCode = FaceImageKind2DCode.fromCode(
                ISO39794Util.decodeCodeFromChoiceExtensionBlockFallback(taggedObjects[1])
            )
        }
        if (taggedObjects.containsKey(2)) {
            postAcquisitionProcessingBlock =
                FaceImagePostAcquisitionProcessingBlock(taggedObjects[2])
        }
        if (taggedObjects.containsKey(3)) {
            lossyTransformationAttemptsCode = LossyTransformationAttemptsCode.fromCode(
                ISO39794Util.decodeCodeFromChoiceExtensionBlockFallback(taggedObjects[3])
            )
        }
        if (taggedObjects.containsKey(4)) {
            cameraToSubjectDistance = ASN1Util.decodeInt(taggedObjects[4])
        }
        if (taggedObjects.containsKey(5)) {
            sensorDiagonal = ASN1Util.decodeInt(taggedObjects[5])
        }
        if (taggedObjects.containsKey(6)) {
            lensFocalLength = ASN1Util.decodeInt(taggedObjects[6])
        }
        if (taggedObjects.containsKey(7)) {
            imageSizeBlock = ImageSizeBlock(taggedObjects.get(7))
        }
        if (taggedObjects.containsKey(8)) {
            imageFaceMeasurementsBlock = ImageFaceMeasurementsBlock(taggedObjects.get(8))
        }
        if (taggedObjects.containsKey(9)) {
            imageColourSpaceCode = ImageColourSpaceCode.fromCode(
                ISO39794Util.decodeCodeFromChoiceExtensionBlockFallback(taggedObjects.get(9))
            )
        }
        if (taggedObjects.containsKey(10)) {
            referenceColourMappingBlock =
                FaceImageReferenceColourMappingBlock(taggedObjects.get(10))
        }
    }*/

    /**
     * Returns the image data format.
     * One of `JPEG`, `JPEG2000_LOSSY`, or `JPEG2000_LOSSLESS`.
     * 
     * @return the image data format
     */
    fun getImageDataFormatCode(): ImageDataFormatCode? {
        return imageDataFormatCode
    }

    /*public override fun hashCode(): Int {
        return Objects.hash(
            cameraToSubjectDistance,
            faceImageKind2DCode,
            imageColourSpaceCode,
            imageDataFormatCode,
            imageFaceMeasurementsBlock,
            imageSizeBlock,
            lensFocalLength,
            lossyTransformationAttemptsCode,
            postAcquisitionProcessingBlock,
            referenceColourMappingBlock,
            sensorDiagonal
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
    }*/

    override val aSN1Object: ASN1Encodable
        get() = ASN1Util.encodeTaggedObjects(
            buildMap {
                imageDataFormatCode?.let {
                    put(
                        0,
                        ISO39794Util.encodeCodeAsChoiceExtensionBlockFallback(it.code)
                    )
                }
                faceImageKind2DCode?.let {
                    put(
                        1,
                        ISO39794Util.encodeCodeAsChoiceExtensionBlockFallback(it.code)
                    )
                }
                postAcquisitionProcessingBlock?.let {
                    put(2, it.aSN1Object)
                }
                lossyTransformationAttemptsCode?.let {
                    put(
                        3,
                        ISO39794Util.encodeCodeAsChoiceExtensionBlockFallback(
                            it.code
                        )
                    )
                }
                cameraToSubjectDistance?.let {
                    put(4, ASN1Util.encodeInt(it))
                }
                sensorDiagonal?.let {
                    put(5, ASN1Util.encodeInt(it))
                }
                lensFocalLength?.let {
                    put(6, ASN1Util.encodeInt(it))
                }
                imageSizeBlock?.let {
                    put(7, it.aSN1Object)
                }
                imageFaceMeasurementsBlock?.let {
                    put(8, it.aSN1Object)
                }
                imageColourSpaceCode?.let {
                    put(
                        9,
                        ISO39794Util.encodeCodeAsChoiceExtensionBlockFallback(it.code)
                    )
                }
                referenceColourMappingBlock?.let {
                    put(10, it.aSN1Object)
                }
            }
        )
    /* PACKAGE */
    /*get() {
        val taggedObjects: MutableMap<Int?, ASN1Encodable?> =
            HashMap<Int?, ASN1Encodable?>()
        taggedObjects.put(
            0,
            ISO39794Util.encodeCodeAsChoiceExtensionBlockFallback(imageDataFormatCode!!.getCode())
        )
        if (faceImageKind2DCode != null) {
            taggedObjects.put(
                1,
                ISO39794Util.encodeCodeAsChoiceExtensionBlockFallback(faceImageKind2DCode!!.getCode())
            )
        }
        if (postAcquisitionProcessingBlock != null) {
            taggedObjects.put(2, postAcquisitionProcessingBlock!!.aSN1Object)
        }
        if (lossyTransformationAttemptsCode != null) {
            taggedObjects.put(
                3,
                ISO39794Util.encodeCodeAsChoiceExtensionBlockFallback(
                    lossyTransformationAttemptsCode!!.getCode()
                )
            )
        }
        if (cameraToSubjectDistance != null) {
            taggedObjects.put(4, ASN1Util.encodeInt(cameraToSubjectDistance!!))
        }
        if (sensorDiagonal != null) {
            taggedObjects.put(5, ASN1Util.encodeInt(sensorDiagonal!!))
        }
        if (lensFocalLength != null) {
            taggedObjects.put(6, ASN1Util.encodeInt(lensFocalLength!!))
        }
        if (imageSizeBlock != null) {
            taggedObjects.put(7, imageSizeBlock!!.aSN1Object)
        }
        if (imageFaceMeasurementsBlock != null) {
            taggedObjects.put(8, imageFaceMeasurementsBlock!!.aSN1Object)
        }
        if (imageColourSpaceCode != null) {
            taggedObjects.put(
                9,
                ISO39794Util.encodeCodeAsChoiceExtensionBlockFallback(imageColourSpaceCode!!.getCode())
            )
        }
        if (referenceColourMappingBlock != null) {
            taggedObjects.put(10, referenceColourMappingBlock!!.aSN1Object)
        }
        return ASN1Util.encodeTaggedObjects(taggedObjects)
    }*/

    companion object {
        private const val serialVersionUID = 76880187801114756L

        /**
         * Factory method
         *
         * ImageInformation2DBlock ::= SEQUENCE {
         *   imageDataFormat [0] ImageDataFormat,
         *   faceImageKind2D [1] FaceImageKind2D OPTIONAL,
         *   postAcquisitionProcessingBlock [2] PostAcquisitionProcessingBlock OPTIONAL,
         *   lossyTransformationAttempts [3] LossyTransformationAttempts OPTIONAL,
         *   cameraToSubjectDistance [4] CameraToSubjectDistance OPTIONAL,
         *   sensorDiagonal [5] SensorDiagonal OPTIONAL,
         *   lensFocalLength [6] LensFocalLength OPTIONAL,
         *   imageSizeBlock [7] ImageSizeBlock OPTIONAL,
         *   imageFaceMeasurementsBlock [8] ImageFaceMeasurementsBlock OPTIONAL,
         *   imageColourSpace [9] ImageColourSpace OPTIONAL,
         *   referenceColourMappingBlock [10] ReferenceColourMappingBlock OPTIONAL,
         *   ...
         * }
         */
        @JvmStatic
        fun from(asn1Encodable: ASN1Encodable?): FaceImageInformation2DBlock {
            require(!(asn1Encodable !is ASN1Sequence && asn1Encodable !is ASN1TaggedObject)) { "Cannot decode!" }

            val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)

            return FaceImageInformation2DBlock(
                imageDataFormatCode = if (taggedObjects.containsKey(0)) ImageDataFormatCode.fromCode(
                    ISO39794Util.decodeCodeFromChoiceExtensionBlockFallback(taggedObjects[0])
                ) else null,
                faceImageKind2DCode = if (taggedObjects.containsKey(1)) FaceImageKind2DCode.fromCode(
                    ISO39794Util.decodeCodeFromChoiceExtensionBlockFallback(taggedObjects[1])
                ) else null,
                postAcquisitionProcessingBlock = if (taggedObjects.containsKey(2)) FaceImagePostAcquisitionProcessingBlock.from(
                    taggedObjects[2]
                ) else null,
                lossyTransformationAttemptsCode = if (taggedObjects.containsKey(3)) LossyTransformationAttemptsCode.fromCode(
                    ISO39794Util.decodeCodeFromChoiceExtensionBlockFallback(taggedObjects[3])
                ) else null,
                cameraToSubjectDistance = if (taggedObjects.containsKey(4)) ASN1Util.decodeInt(
                    taggedObjects[4]
                ) else null,
                sensorDiagonal = if (taggedObjects.containsKey(5)) ASN1Util.decodeInt(taggedObjects[5]) else null,
                lensFocalLength = if (taggedObjects.containsKey(6)) ASN1Util.decodeInt(taggedObjects[6]) else null,
                imageSizeBlock = if (taggedObjects.containsKey(7)) ImageSizeBlock.from(taggedObjects[7]) else null,
                imageFaceMeasurementsBlock = if (taggedObjects.containsKey(8)) ImageFaceMeasurementsBlock.from(
                    taggedObjects[8]
                ) else null,
                imageColourSpaceCode = if (taggedObjects.containsKey(9)) ImageColourSpaceCode.fromCode(
                    ISO39794Util.decodeCodeFromChoiceExtensionBlockFallback(taggedObjects[9])
                ) else null,
                referenceColourMappingBlock = if (taggedObjects.containsKey(10)) FaceImageReferenceColourMappingBlock.from(
                    taggedObjects[10]
                ) else null,
            )
        }
    }
}