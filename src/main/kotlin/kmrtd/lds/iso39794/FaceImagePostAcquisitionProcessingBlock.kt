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
package kmrtd.lds.iso39794

import org.bouncycastle.asn1.ASN1Encodable
import kmrtd.ASN1Util
import java.util.*

class FaceImagePostAcquisitionProcessingBlock : Block {
    var isRotated: Boolean? = null
        private set
    var isCropped: Boolean? = null
        private set
    var isDownSampled: Boolean? = null
        private set
    var isWhiteBalanceAdjusted: Boolean? = null
        private set
    var isMultiplyCompressed: Boolean? = null
        private set
    var isInterpolated: Boolean? = null
        private set
    var isContrastStretched: Boolean? = null
        private set
    var isPoseCorrected: Boolean? = null
        private set
    var isMultiViewImage: Boolean? = null
        private set
    var isAgeProgressed: Boolean? = null
        private set
    var isSuperResolutionProcessed: Boolean? = null
        private set
    var isNormalised: Boolean? = null
        private set

    constructor(
        isRotated: Boolean?,
        isCropped: Boolean?,
        isDownSampled: Boolean?,
        isWhiteBalanceAdjusted: Boolean?,
        isMultiplyCompressed: Boolean?,
        isInterpolated: Boolean?,
        isContrastStretched: Boolean?,
        isPoseCorrected: Boolean?,
        isMultiViewImage: Boolean?,
        isAgeProgressed: Boolean?,
        isSuperResolutionProcessed: Boolean?,
        isNormalised: Boolean?
    ) {
        this.isRotated = isRotated
        this.isCropped = isCropped
        this.isDownSampled = isDownSampled
        this.isWhiteBalanceAdjusted = isWhiteBalanceAdjusted
        this.isMultiplyCompressed = isMultiplyCompressed
        this.isInterpolated = isInterpolated
        this.isContrastStretched = isContrastStretched
        this.isPoseCorrected = isPoseCorrected
        this.isMultiViewImage = isMultiViewImage
        this.isAgeProgressed = isAgeProgressed
        this.isSuperResolutionProcessed = isSuperResolutionProcessed
        this.isNormalised = isNormalised
    }

    constructor(asn1Encodable: ASN1Encodable?) {
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
    }

    override fun hashCode(): Int {
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
    }

    /* PACKAGE */
    override fun getASN1Object(): ASN1Encodable? {
        val taggedObjects: MutableMap<Int?, ASN1Encodable?> = HashMap<Int?, ASN1Encodable?>()
        if (isRotated != null) {
            taggedObjects[0] = ASN1Util.encodeBoolean(isRotated!!)
        }
        if (isCropped != null) {
            taggedObjects[1] = ASN1Util.encodeBoolean(isCropped!!)
        }
        if (isDownSampled != null) {
            taggedObjects[2] = ASN1Util.encodeBoolean(isDownSampled!!)
        }
        if (isWhiteBalanceAdjusted != null) {
            taggedObjects[3] = ASN1Util.encodeBoolean(isWhiteBalanceAdjusted!!)
        }
        if (isMultiplyCompressed != null) {
            taggedObjects[4] = ASN1Util.encodeBoolean(isMultiplyCompressed!!)
        }
        if (isInterpolated != null) {
            taggedObjects[5] = ASN1Util.encodeBoolean(isInterpolated!!)
        }
        if (isContrastStretched != null) {
            taggedObjects[6] = ASN1Util.encodeBoolean(isContrastStretched!!)
        }
        if (isPoseCorrected != null) {
            taggedObjects[7] = ASN1Util.encodeBoolean(isPoseCorrected!!)
        }
        if (isMultiViewImage != null) {
            taggedObjects[8] = ASN1Util.encodeBoolean(isMultiViewImage!!)
        }
        if (isAgeProgressed != null) {
            taggedObjects[9] = ASN1Util.encodeBoolean(isAgeProgressed!!)
        }
        if (isSuperResolutionProcessed != null) {
            taggedObjects[10] = ASN1Util.encodeBoolean(isSuperResolutionProcessed!!)
        }
        if (isNormalised != null) {
            taggedObjects[11] = ASN1Util.encodeBoolean(isNormalised!!)
        }
        return ASN1Util.encodeTaggedObjects(taggedObjects)
    }

    companion object {
        private val serialVersionUID = -3603621266074466100L
    }
}
