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
 * $Id: FingerImageCaptureDeviceBlock.java 1892 2025-03-18 15:15:52Z martijno $
 *
 * Based on ISO-IEC-39794-4-ed-1-v2. Disclaimer:
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
data class FingerImageCaptureDeviceBlock(
    /** Identification of the model capture device.  */
    val model: RegistryIdBlock,
    val captureDeviceTechnologyIdCode: CaptureDeviceTechnologyIdCode?,
    /** Identification of certifications.  */
    val certifications: List<RegistryIdBlock>
) : Block() {
    /*public static enum CaptureDeviceTechnologyIdCode implements EncodableEnum<CaptureDeviceTechnologyIdCode> {
       UNKNOWN_CAPTURE_DEVICE_TECHNOLOGY(0),
       OTHER_CAPTURE_DEVICE_TECHNOLOGY(1),
       SCANNED_INK_ON_PAPER(2),
       OPTICAL_TIR_BRIGHT_FIELD(3),
       OPTICAL_TIR_DARK_FIELD(4),
       OPTICAL_IMAGE(5),
       OPTICAL_LOW_FREQUENCY_3D_MAPPED(6),
       OPTICAL_HIGH_FREQUENCY_3D_MAPPED(7),
       CAPACITIVE(9),
       CAPACITIVE_RF(10),
       ELECTRO_LUMINESCENCE(11),
       REFLECTED_ULTRASONIC(12),
       IMPEDIOGRAPHIC_ULTRASONIC(13),
       THERMAL(14),
       DIRECT_PRESSURE(15),
       INDIRECT_PRESSURE(16),
       LIVE_TAPE(17),
       LATENT_IMPRESSION(18),
       LATENT_PHOTO(19),
       LATENT_MOLDED(20),
       LATENT_TRACING(21),
       LATENT_LIFT(22);
   
       private int code;
   
       private CaptureDeviceTechnologyIdCode(int code) {
         this.code = code;
       }

       @Override
       public int getCode() {
         return code;
       }
   
       public static CaptureDeviceTechnologyIdCode fromCode(int code) {
         return EncodableEnum.fromCode(code, CaptureDeviceTechnologyIdCode.class);
       }
     }*/
    /** Identification of the model capture device.  */
    /*var model: RegistryIdBlock? = null
        private set

    var captureDeviceTechnologyIdCode: CaptureDeviceTechnologyIdCode? = null
        private set

    *//** Identification of certifications.  *//*
    var certifications: MutableList<RegistryIdBlock?>? = null
        private set*/

    /*constructor(
        model: RegistryIdBlock?,
        captureDeviceTechnologyIdCode: CaptureDeviceTechnologyIdCode?,
        certifications: MutableList<RegistryIdBlock?>?
    ) {
        this.model = model
        this.captureDeviceTechnologyIdCode = captureDeviceTechnologyIdCode
        this.certifications = certifications
    }*/

    //  CaptureDeviceBlock ::= SEQUENCE {
    //    modelIdBlock [0] RegistryIdBlock,
    //    technologyId [1] CaptureDeviceTechnologyId OPTIONAL,
    //    certificationIdBlocks [2] CertificationIdBlocks OPTIONAL,
    //    ...
    //  }
    /*internal constructor(asn1Encodable: ASN1Encodable) {
        requireNotNull(asn1Encodable) { "Cannot decode null!" }

        val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)
        if (taggedObjects.containsKey(0)) {
            model = from(taggedObjects.get(0))
        }
        if (taggedObjects.containsKey(1)) {
            captureDeviceTechnologyIdCode =
                fromCode(ISO39794Util.decodeCodeFromChoiceExtensionBlockFallback(taggedObjects.get(1)))
        }
        if (taggedObjects.containsKey(2)) {
            certifications = RegistryIdBlock.decodeRegistryIdBlocks(taggedObjects.get(2))
        }
    }

    public override fun hashCode(): Int {
        return Objects.hash(captureDeviceTechnologyIdCode, certifications, model)
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

        val other = obj as FingerImageCaptureDeviceBlock
        return captureDeviceTechnologyIdCode == other.captureDeviceTechnologyIdCode && certifications == other.certifications && model == other.model
    }

    override fun toString(): String {
        return ("FingerImageCaptureDeviceBlock ["
                + "model: " + model
                + ", captureDeviceTechnologyIdCode: " + captureDeviceTechnologyIdCode
                + ", certifications: " + certifications
                + "]")
    }*/

    override val aSN1Object: ASN1Encodable
        get() = ASN1Util.encodeTaggedObjects(
            buildMap {
                put(0, model.aSN1Object)
                captureDeviceTechnologyIdCode?.let {
                    put(1, ISO39794Util.encodeCodeAsChoiceExtensionBlockFallback(it.code))
                }
                put(2, ISO39794Util.encodeBlocks(certifications))
            }
        )
        /* PACKAGE */
        /*get() {
            val taggedObjects: MutableMap<Int?, ASN1Encodable?> =
                HashMap<Int?, ASN1Encodable?>()
            if (model != null) {
                taggedObjects.put(0, model.aSN1Object)
            }
            if (captureDeviceTechnologyIdCode != null) {
                taggedObjects.put(
                    1,
                    ISO39794Util.encodeCodeAsChoiceExtensionBlockFallback(
                        captureDeviceTechnologyIdCode!!.getCode()
                    )
                )
            }
            if (certifications != null) {
                taggedObjects.put(2, ISO39794Util.encodeBlocks(certifications))
            }
            return ASN1Util.encodeTaggedObjects(taggedObjects)
        }*/

    companion object {
        private const val serialVersionUID = -5356682106972185445L

        /**
         * Factory method
         *
         * CaptureDeviceBlock ::= SEQUENCE {
         *   modelIdBlock [0] RegistryIdBlock,
         *   technologyId [1] CaptureDeviceTechnologyId OPTIONAL,
         *   certificationIdBlocks [2] CertificationIdBlocks OPTIONAL,
         *   ...
         * }
         */
        fun from(asn1Encodable: ASN1Encodable): FingerImageCaptureDeviceBlock {
            val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)

            if (
                taggedObjects.containsKey(0) &&
                taggedObjects.containsKey(1) &&
                taggedObjects.containsKey(2)
            ) {
                return FingerImageCaptureDeviceBlock(
                    model = RegistryIdBlock.from(taggedObjects[0]),
                    captureDeviceTechnologyIdCode = CaptureDeviceTechnologyIdCode.fromCode(ISO39794Util.decodeCodeFromChoiceExtensionBlockFallback(taggedObjects[1])),
                    certifications = RegistryIdBlock.decodeRegistryIdBlocks(taggedObjects[2]!!)
                )
            }

            throw Exception("Cannot decode FingerImageCaptureDeviceBlock")
        }

    }
}
