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
 * $Id: IrisImageCaptureDeviceBlock.java 1892 2025-03-18 15:15:52Z martijno $
 *
 * Based on ISO-IEC-39794-6-ed-1-v1. Disclaimer:
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

class IrisImageCaptureDeviceBlock(asn1Encodable: ASN1Encodable) : Block() {
    enum class CaptureDeviceTechnologyIdCode(override val code: Int) : EncodableEnum<CaptureDeviceTechnologyIdCode?> {
        UNKNOWN(0),
        CMOS_CCD(1);

        override fun getCode(): Int {
            return code
        }

        companion object {
            fun fromCode(code: Int): CaptureDeviceTechnologyIdCode? {
                return EncodableEnum.fromCode<CaptureDeviceTechnologyIdCode?>(
                    code,
                    CaptureDeviceTechnologyIdCode::class.java
                )
            }
        }
    }

    /** Identification of the model capture device.  */
    private var model: RegistryIdBlock? = null

    var captureDeviceTechnologyIdCode: CaptureDeviceTechnologyIdCode? = null
        private set

    /** Identification of certifications.  */
    var certifications: MutableList<RegistryIdBlock?>? = null
        private set

    //  CaptureDeviceBlock ::= SEQUENCE {
    //    modelIdBlock [0] RegistryIdBlock,
    //    technologyId [1] CaptureDeviceTechnologyId OPTIONAL,
    //    certificationIdBlocks [2] CertificationIdBlocks OPTIONAL,
    //    ...
    //  }
    init {
        requireNotNull(asn1Encodable) { "Cannot decode null!" }
        val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)
        if (taggedObjects.containsKey(0)) {
            model = RegistryIdBlock(taggedObjects.get(0))
        }
        if (taggedObjects.containsKey(1)) {
            captureDeviceTechnologyIdCode = CaptureDeviceTechnologyIdCode.Companion.fromCode(
                ISO39794Util.decodeCodeFromChoiceExtensionBlockFallback(taggedObjects.get(1))
            )
        }
        if (taggedObjects.containsKey(2)) {
            certifications = RegistryIdBlock.decodeRegistryIdBlocks(taggedObjects.get(2))
        }
    }

    fun getModel(): RegistryIdBlock {
        return model!!
    }

    override fun hashCode(): Int {
        return Objects.hash(captureDeviceTechnologyIdCode, certifications, model)
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

        val other = obj as IrisImageCaptureDeviceBlock
        return captureDeviceTechnologyIdCode == other.captureDeviceTechnologyIdCode && certifications == other.certifications && model == other.model
    }

    override fun toString(): String {
        return ("IrisImageCaptureDeviceBlock ["
                + "model: " + model
                + ", captureDeviceTechnologyIdCode: " + captureDeviceTechnologyIdCode
                + ", certifications: " + certifications
                + "]")
    }

    /* PACKAGE */
    override fun getASN1Object(): ASN1Encodable? {
        val taggedObjects: MutableMap<Int?, ASN1Encodable?> = HashMap<Int?, ASN1Encodable?>()
        taggedObjects[0] = model!!.getASN1Object()
        if (captureDeviceTechnologyIdCode != null) {
            taggedObjects[1] = ISO39794Util.encodeCodeAsChoiceExtensionBlockFallback(captureDeviceTechnologyIdCode!!.getCode())
        }
        if (certifications != null) {
            taggedObjects[2] = ISO39794Util.encodeBlocks(certifications)
        }
        return ASN1Util.encodeTaggedObjects(taggedObjects)
    }

    companion object {
        private val serialVersionUID = -4279701511600052026L
    }
}
