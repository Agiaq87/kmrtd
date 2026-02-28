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
 * $Id: FaceImagePostAcquisitionProcessingBlock.java 1889 2025-03-15 21:09:22Z martijno $
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
import org.jmrtd.ASN1Util
import java.util.Objects

data class FaceImagePostAcquisitionProcessingBlock(
    val isRotated: Boolean?,
    val isCropped: Boolean?,
    val isDownSampled: Boolean?,
    val isWhiteBalanceAdjusted: Boolean?,
    val isMultiplyCompressed: Boolean?,
    val isInterpolated: Boolean?,
    val isContrastStretched: Boolean?,
    val isPoseCorrected: Boolean?,
    val isMultiViewImage: Boolean?,
    val isAgeProgressed: Boolean?,
    val isSuperResolutionProcessed: Boolean?,
    val isNormalised: Boolean?
) : Block() {

    /*constructor(asn1Encodable: ASN1Encodable?) {
        val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)
        if (taggedObjects.containsKey(0)) {
            isRotated = ASN1Util.decodeBoolean(taggedObjects.get(0))
        }
        if (taggedObjects.containsKey(1)) {
            isCropped = ASN1Util.decodeBoolean(taggedObjects.get(1))
        }
        if (taggedObjects.containsKey(2)) {
            isDownSampled = ASN1Util.decodeBoolean(taggedObjects.get(2))
        }
        if (taggedObjects.containsKey(3)) {
            isWhiteBalanceAdjusted = ASN1Util.decodeBoolean(taggedObjects.get(3))
        }
        if (taggedObjects.containsKey(4)) {
            isMultiplyCompressed = ASN1Util.decodeBoolean(taggedObjects.get(4))
        }
        if (taggedObjects.containsKey(5)) {
            isInterpolated = ASN1Util.decodeBoolean(taggedObjects.get(5))
        }
        if (taggedObjects.containsKey(6)) {
            isContrastStretched = ASN1Util.decodeBoolean(taggedObjects.get(6))
        }
        if (taggedObjects.containsKey(7)) {
            isPoseCorrected = ASN1Util.decodeBoolean(taggedObjects.get(7))
        }
        if (taggedObjects.containsKey(8)) {
            isMultiViewImage = ASN1Util.decodeBoolean(taggedObjects.get(8))
        }
        if (taggedObjects.containsKey(9)) {
            isAgeProgressed = ASN1Util.decodeBoolean(taggedObjects.get(9))
        }
        if (taggedObjects.containsKey(10)) {
            isSuperResolutionProcessed = ASN1Util.decodeBoolean(taggedObjects.get(10))
        }
        if (taggedObjects.containsKey(11)) {
            isNormalised = ASN1Util.decodeBoolean(taggedObjects.get(11))
        }
    }*/

    /*public override fun hashCode(): Int {
        return Objects.hash(
            isAgeProgressed,
            isContrastStretched,
            isCropped,
            isDownSampled,
            isInterpolated,
            isMultiViewImage,
            isMultiplyCompressed,
            isNormalised,
            isPoseCorrected,
            isRotated,
            isSuperResolutionProcessed,
            isWhiteBalanceAdjusted
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

        val other = obj as FaceImagePostAcquisitionProcessingBlock
        return isAgeProgressed == other.isAgeProgressed
                && isContrastStretched == other.isContrastStretched && isCropped == other.isCropped
                && isDownSampled == other.isDownSampled && isInterpolated == other.isInterpolated
                && isMultiViewImage == other.isMultiViewImage
                && isMultiplyCompressed == other.isMultiplyCompressed
                && isNormalised == other.isNormalised && isPoseCorrected == other.isPoseCorrected
                && isRotated == other.isRotated
                && isSuperResolutionProcessed == other.isSuperResolutionProcessed
                && isWhiteBalanceAdjusted == other.isWhiteBalanceAdjusted
    }

    override fun toString(): String {
        return ("FaceImagePostAcquisitionProcessingBlock ["
                + "isRotated: " + isRotated
                + ", isCropped: " + isCropped
                + ", isDownSampled: " + isDownSampled
                + ", isWhiteBalanceAdjusted: " + isWhiteBalanceAdjusted
                + ", isMultiplyCompressed: " + isMultiplyCompressed
                + ", isInterpolated: " + isInterpolated
                + ", isContrastStretched: " + isContrastStretched
                + ", isPoseCorrected: " + isPoseCorrected
                + ", isMultiViewImage: " + isMultiViewImage
                + ", isAgeProgressed: " + isAgeProgressed
                + ", isSuperResolutionProcessed: " + isSuperResolutionProcessed
                + ", isNormalised: " + isNormalised
                + "]")
    }*/

    override val aSN1Object: ASN1Encodable
        get() = ASN1Util.encodeTaggedObjects(
            buildMap {
                isRotated?.let {
                    put(0, ASN1Util.encodeBoolean(it))
                }
                isCropped?.let{
                    put(1, ASN1Util.encodeBoolean(it))
                }
                isDownSampled?.let{
                    put(2, ASN1Util.encodeBoolean(it))
                }
                isWhiteBalanceAdjusted?.let{
                    put(3, ASN1Util.encodeBoolean(it))
                }
                isMultiplyCompressed?.let{
                    put(4, ASN1Util.encodeBoolean(it))
                }
                isInterpolated?.let{
                    put(5, ASN1Util.encodeBoolean(it))
                }
                isContrastStretched?.let{
                    put(6, ASN1Util.encodeBoolean(it))
                }
                isPoseCorrected?.let{
                    put(7, ASN1Util.encodeBoolean(it))
                }
                isMultiViewImage?.let{
                    put(8, ASN1Util.encodeBoolean(it))
                }
                isAgeProgressed?.let{
                    put(9, ASN1Util.encodeBoolean(it))
                }
                isSuperResolutionProcessed?.let{
                    put(10, ASN1Util.encodeBoolean(it))
                }
                isNormalised?.let{
                    put(11, ASN1Util.encodeBoolean(it))
                }
            }
        )
        /* PACKAGE */
        /*get() {
            val taggedObjects: MutableMap<Int?, ASN1Encodable?> =
                HashMap<Int?, ASN1Encodable?>()
            if (isRotated != null) {
                taggedObjects[0] = ASN1Util.encodeBoolean(isRotated)
            }
            if (isCropped != null) {
                taggedObjects[1] = ASN1Util.encodeBoolean(isCropped)
            }
            if (isDownSampled != null) {
                taggedObjects[2] = ASN1Util.encodeBoolean(isDownSampled)
            }
            if (isWhiteBalanceAdjusted != null) {
                taggedObjects[3] = ASN1Util.encodeBoolean(isWhiteBalanceAdjusted)
            }
            if (isMultiplyCompressed != null) {
                taggedObjects[4] = ASN1Util.encodeBoolean(isMultiplyCompressed)
            }
            if (isInterpolated != null) {
                taggedObjects[5] = ASN1Util.encodeBoolean(isInterpolated)
            }
            if (isContrastStretched != null) {
                taggedObjects[6] = ASN1Util.encodeBoolean(isContrastStretched)
            }
            if (isPoseCorrected != null) {
                taggedObjects[7] = ASN1Util.encodeBoolean(isPoseCorrected)
            }
            if (isMultiViewImage != null) {
                taggedObjects[8] = ASN1Util.encodeBoolean(isMultiViewImage)
            }
            if (isAgeProgressed != null) {
                taggedObjects[9] = ASN1Util.encodeBoolean(isAgeProgressed)
            }
            if (isSuperResolutionProcessed != null) {
                taggedObjects[10] = ASN1Util.encodeBoolean(isSuperResolutionProcessed)
            }
            if (isNormalised != null) {
                taggedObjects[11] = ASN1Util.encodeBoolean(isNormalised)
            }
            return ASN1Util.encodeTaggedObjects(taggedObjects)
        }*/

    companion object {
        private const val serialVersionUID = -3603621266074466100L

        /**
         * Factory method
         */
        @JvmStatic
        fun from(asn1Encodable: ASN1Encodable?): FaceImagePostAcquisitionProcessingBlock {
            val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)

            return FaceImagePostAcquisitionProcessingBlock(
                isRotated = if (taggedObjects.containsKey(0)) ASN1Util.decodeBoolean(taggedObjects[0]) else null,
                isCropped = if (taggedObjects.containsKey(1)) ASN1Util.decodeBoolean(taggedObjects[1]) else null,
                isDownSampled = if (taggedObjects.containsKey(2)) ASN1Util.decodeBoolean(taggedObjects[2]) else null,
                isWhiteBalanceAdjusted = if (taggedObjects.containsKey(3)) ASN1Util.decodeBoolean(taggedObjects[3]) else null,
                isMultiplyCompressed = if (taggedObjects.containsKey(4)) ASN1Util.decodeBoolean(taggedObjects[4]) else null,
                isInterpolated = if (taggedObjects.containsKey(5)) ASN1Util.decodeBoolean(taggedObjects[5]) else null,
                isContrastStretched = if (taggedObjects.containsKey(6)) ASN1Util.decodeBoolean(taggedObjects[6]) else null,
                isPoseCorrected = if (taggedObjects.containsKey(7)) ASN1Util.decodeBoolean(taggedObjects[7]) else null,
                isMultiViewImage = if (taggedObjects.containsKey(8)) ASN1Util.decodeBoolean(taggedObjects[8]) else null,
                isAgeProgressed = if (taggedObjects.containsKey(9)) ASN1Util.decodeBoolean(taggedObjects[9]) else null,
                isSuperResolutionProcessed = if (taggedObjects.containsKey(10)) ASN1Util.decodeBoolean(taggedObjects[10]) else null,
                isNormalised = if (taggedObjects.containsKey(11)) ASN1Util.decodeBoolean(taggedObjects[11]) else null
            )
        }
    }
}
