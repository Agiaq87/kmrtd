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
 * $Id: FaceImagePropertiesBlock.java 1889 2025-03-15 21:09:22Z martijno $
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

data class FaceImagePropertiesBlock(
    val hasGlasses: Boolean?,
    val hasMoustache: Boolean?,
    val hasBeard: Boolean?,
    val isTeethVisible: Boolean?,
    val isPupilOrIrisNotVisible: Boolean?,
    val isMouthOpen: Boolean?,
    val hasLeftEyePatch: Boolean?,
    val hasRightEyePatch: Boolean?,
    val hasDarkGlasses: Boolean?,
    val isBiometricAbsent: Boolean?,
    val isHeadCoveringsPresent: Boolean?
) : Block() {

    //  PropertiesBlock ::= SEQUENCE {
    //    glasses [0] BOOLEAN OPTIONAL,
    //    moustache [1] BOOLEAN OPTIONAL,
    //    beard [2] BOOLEAN OPTIONAL,
    //    teethVisible [3] BOOLEAN OPTIONAL,
    //    pupilOrIrisNotVisible [4] BOOLEAN OPTIONAL,
    //    mouthOpen [5] BOOLEAN OPTIONAL,
    //    leftEyePatch [6] BOOLEAN OPTIONAL,
    //    rightEyePatch [7] BOOLEAN OPTIONAL,
    //    darkGlasses [8] BOOLEAN OPTIONAL,
    //    biometricAbsent [9] BOOLEAN OPTIONAL,
    //    headCoveringsPresent [10] BOOLEAN OPTIONAL,
    //    ...
    //  }
    /*internal constructor(asn1Encodable: ASN1Encodable?) {
        val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)
        if (taggedObjects.containsKey(0)) {
            hasGlasses = ASN1Util.decodeBoolean(taggedObjects[0])
        }
        if (taggedObjects.containsKey(1)) {
            hasMoustache = ASN1Util.decodeBoolean(taggedObjects[1])
        }
        if (taggedObjects.containsKey(2)) {
            hasBeard = ASN1Util.decodeBoolean(taggedObjects[2])
        }
        if (taggedObjects.containsKey(3)) {
            isTeethVisible = ASN1Util.decodeBoolean(taggedObjects[3])
        }
        if (taggedObjects.containsKey(4)) {
            isPupilOrIrisNotVisible = ASN1Util.decodeBoolean(taggedObjects[4])
        }
        if (taggedObjects.containsKey(5)) {
            isMouthOpen = ASN1Util.decodeBoolean(taggedObjects[5])
        }
        if (taggedObjects.containsKey(6)) {
            hasLeftEyePatch = ASN1Util.decodeBoolean(taggedObjects[6])
        }
        if (taggedObjects.containsKey(7)) {
            hasRightEyePatch = ASN1Util.decodeBoolean(taggedObjects[7])
        }
        if (taggedObjects.containsKey(8)) {
            hasDarkGlasses = ASN1Util.decodeBoolean(taggedObjects[8])
        }
        if (taggedObjects.containsKey(9)) {
            isBiometricAbsent = ASN1Util.decodeBoolean(taggedObjects[9])
        }
        if (taggedObjects.containsKey(10)) {
            isHeadCoveringsPresent = ASN1Util.decodeBoolean(taggedObjects[10])
        }
    }*/

    /*public override fun hashCode(): Int {
        return Objects.hash(
            hasBeard,
            hasDarkGlasses,
            hasGlasses,
            hasLeftEyePatch,
            hasMoustache,
            hasRightEyePatch,
            isBiometricAbsent,
            isHeadCoveringsPresent,
            isMouthOpen,
            isPupilOrIrisNotVisible,
            isTeethVisible
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

        val other = obj as FaceImagePropertiesBlock
        return hasBeard == other.hasBeard && hasDarkGlasses == other.hasDarkGlasses
                && hasGlasses == other.hasGlasses && hasLeftEyePatch == other.hasLeftEyePatch
                && hasMoustache == other.hasMoustache && hasRightEyePatch == other.hasRightEyePatch
                && isBiometricAbsent == other.isBiometricAbsent
                && isHeadCoveringsPresent == other.isHeadCoveringsPresent
                && isMouthOpen == other.isMouthOpen
                && isPupilOrIrisNotVisible == other.isPupilOrIrisNotVisible
                && isTeethVisible == other.isTeethVisible
    }

    override fun toString(): String {
        return ("FaceImagePropertiesBlock ["
                + "hasGlasses: " + hasGlasses
                + ", hasMoustache: " + hasMoustache
                + ", hasBeard: " + hasBeard
                + ", isTeethVisible: " + isTeethVisible
                + ", isPupilOrIrisNotVisible: " + isPupilOrIrisNotVisible
                + ", isMouthOpen: " + isMouthOpen
                + ", hasLeftEyePatch: " + hasLeftEyePatch
                + ", hasRightEyePatch: " + hasRightEyePatch
                + ", hasDarkGlasses: " + hasDarkGlasses
                + ", isBiometricAbsent: " + isBiometricAbsent
                + ", isHeadCoveringsPresent: " + isHeadCoveringsPresent
                + "]")
    }*/

    override val aSN1Object: ASN1Encodable
        get() = ASN1Util.encodeTaggedObjects(
            buildMap {
                hasGlasses?.let{
                    put(0, ASN1Util.encodeBoolean(it))
                }
                hasMoustache?.let{
                    put(1, ASN1Util.encodeBoolean(it))
                }
                hasBeard?.let{
                    put(2, ASN1Util.encodeBoolean(it))
                }
                isTeethVisible?.let{
                    put(3, ASN1Util.encodeBoolean(it))
                }
                isPupilOrIrisNotVisible?.let{
                    put(4, ASN1Util.encodeBoolean(it))
                }
                isMouthOpen?.let{
                    put(5, ASN1Util.encodeBoolean(it))
                }
                hasLeftEyePatch?.let{
                    put(6, ASN1Util.encodeBoolean(it))
                }
                hasRightEyePatch?.let{
                    put(7, ASN1Util.encodeBoolean(it))
                }
                hasDarkGlasses?.let{
                    put(8, ASN1Util.encodeBoolean(it))
                }
                isBiometricAbsent?.let{
                    put(9, ASN1Util.encodeBoolean(it))
                }
                isHeadCoveringsPresent?.let{
                    put(10, ASN1Util.encodeBoolean(it))
                }
            }
        )
        /*get() {
            val taggedObjects: MutableMap<Int?, ASN1Encodable?> =
                HashMap<Int?, ASN1Encodable?>()
            if (hasGlasses != null) {
                taggedObjects[0] = ASN1Util.encodeBoolean(hasGlasses)
            }
            if (hasMoustache != null) {
                taggedObjects[1] = ASN1Util.encodeBoolean(hasMoustache)
            }
            if (hasBeard != null) {
                taggedObjects[2] = ASN1Util.encodeBoolean(hasBeard)
            }
            if (isTeethVisible != null) {
                taggedObjects[3] = ASN1Util.encodeBoolean(isTeethVisible)
            }
            if (isPupilOrIrisNotVisible != null) {
                taggedObjects[4] = ASN1Util.encodeBoolean(isPupilOrIrisNotVisible)
            }
            if (isMouthOpen != null) {
                taggedObjects[5] = ASN1Util.encodeBoolean(isMouthOpen)
            }
            if (hasLeftEyePatch != null) {
                taggedObjects[6] = ASN1Util.encodeBoolean(hasLeftEyePatch)
            }
            if (hasRightEyePatch != null) {
                taggedObjects[7] = ASN1Util.encodeBoolean(hasRightEyePatch)
            }
            if (hasDarkGlasses != null) {
                taggedObjects[8] = ASN1Util.encodeBoolean(hasDarkGlasses)
            }
            if (isBiometricAbsent != null) {
                taggedObjects[9] = ASN1Util.encodeBoolean(isBiometricAbsent)
            }
            if (isHeadCoveringsPresent != null) {
                taggedObjects[10] = ASN1Util.encodeBoolean(isHeadCoveringsPresent)
            }
            return ASN1Util.encodeTaggedObjects(taggedObjects)
        }*/

    companion object {
        private const val serialVersionUID = 4371559611288515912L

        /**
         * Factory method
         *
         * PropertiesBlock ::= SEQUENCE {
         *   glasses [0] BOOLEAN OPTIONAL,
         *   moustache [1] BOOLEAN OPTIONAL,
         *   beard [2] BOOLEAN OPTIONAL,
         *   teethVisible [3] BOOLEAN OPTIONAL,
         *   pupilOrIrisNotVisible [4] BOOLEAN OPTIONAL,
         *   mouthOpen [5] BOOLEAN OPTIONAL,
         *   leftEyePatch [6] BOOLEAN OPTIONAL,
         *   rightEyePatch [7] BOOLEAN OPTIONAL,
         *   darkGlasses [8] BOOLEAN OPTIONAL,
         *   biometricAbsent [9] BOOLEAN OPTIONAL,
         *   headCoveringsPresent [10] BOOLEAN OPTIONAL,
         *   ...
         * }
         */
        @JvmStatic
        fun from(asn1Encodable: ASN1Encodable?): FaceImagePropertiesBlock {
            val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)

            return FaceImagePropertiesBlock(
                hasGlasses = if (taggedObjects.containsKey(0)) ASN1Util.decodeBoolean(taggedObjects[0]) else null,
                hasMoustache = if (taggedObjects.containsKey(1)) ASN1Util.decodeBoolean(
                    taggedObjects[1]
                ) else null,
                hasBeard = if (taggedObjects.containsKey(2)) ASN1Util.decodeBoolean(taggedObjects[2]) else null,
                isTeethVisible = if (taggedObjects.containsKey(3)) ASN1Util.decodeBoolean(
                    taggedObjects[3]
                ) else null,
                isPupilOrIrisNotVisible = if (taggedObjects.containsKey(4)) ASN1Util.decodeBoolean(
                    taggedObjects[4]
                ) else null,
                isMouthOpen = if (taggedObjects.containsKey(5)) ASN1Util.decodeBoolean(taggedObjects[5]) else null,
                hasLeftEyePatch = if (taggedObjects.containsKey(6)) ASN1Util.decodeBoolean(
                    taggedObjects[6]
                ) else null,
                hasRightEyePatch = if (taggedObjects.containsKey(7)) ASN1Util.decodeBoolean(
                    taggedObjects[7]
                ) else null,
                hasDarkGlasses = if (taggedObjects.containsKey(8)) ASN1Util.decodeBoolean(
                    taggedObjects[8]
                ) else null,
                isBiometricAbsent = if (taggedObjects.containsKey(9)) ASN1Util.decodeBoolean(
                    taggedObjects[9]
                ) else null,
                isHeadCoveringsPresent = if (taggedObjects.containsKey(10)) ASN1Util.decodeBoolean(
                    taggedObjects[10]
                ) else null
            )
        }
    }
}
