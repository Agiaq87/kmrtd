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
 * $Id: FaceImageCaptureDeviceBlock.java 1889 2025-03-15 21:09:22Z martijno $
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

data class FaceImageCaptureDeviceBlock(
    /** Identification of the model capture device.  */
    val model: RegistryIdBlock?,
    /** Identification of certifications.  */
    val certifications: List<RegistryIdBlock>?
) : Block() {

    /*var model: RegistryIdBlock? = null
        private set


    var certifications: MutableList<RegistryIdBlock?>? = null
        private set

    constructor(model: RegistryIdBlock?, certifications: MutableList<RegistryIdBlock?>?) {
        this.model = model
        this.certifications = certifications
    }*/

    //  CaptureDeviceBlock ::= SEQUENCE {
    //    modelIdBlock [0] RegistryIdBlock OPTIONAL,
    //    certificationIdBlocks [1] CertificationIdBlocks OPTIONAL,
    //    ...
    //  }
    /*internal constructor(asn1Encodable: ASN1Encodable) {
        requireNotNull(asn1Encodable) { "Cannot decode null!" }
        val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)
        if (taggedObjects.containsKey(0)) {
            model = from(taggedObjects[0])
        }
        if (taggedObjects.containsKey(1)) {
            certifications = RegistryIdBlock.decodeRegistryIdBlocks(taggedObjects.get(1))
        }
    }*/

    /*public override fun hashCode(): Int {
        return Objects.hash(certifications, model)
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

        val other = obj as FaceImageCaptureDeviceBlock
        return certifications == other.certifications && model == other.model
    }

    override fun toString(): String {
        return ("FaceImageCaptureDeviceBlock ["
                + "model: " + model
                + ", certifications: " + certifications
                + "]")
    }*/

    override val aSN1Object: ASN1Encodable
        get() = ASN1Util.encodeTaggedObjects(
            buildMap {
                model?.let{ put(0, model.aSN1Object) }
                certifications?.let{
                    put(
                        1,
                        ISO39794Util.encodeBlocks(certifications)
                    )
                }
            }
        )
        /* PACKAGE */
        /*get() {
            val taggedObjects: MutableMap<Int?, ASN1Encodable?> =
                HashMap<Int?, ASN1Encodable?>()
            if (model != null) {
                taggedObjects.put(0, model!!.aSN1Object)
            }
            if (certifications != null) {
                taggedObjects.put(
                    1,
                    ISO39794Util.encodeBlocks(certifications)
                )
            }
            return ASN1Util.encodeTaggedObjects(taggedObjects)
        }*/

    companion object {
        private const val serialVersionUID = 2537450971926807146L

        /**
         * Factory method
         *
         * CaptureDeviceBlock ::= SEQUENCE {
         *   modelIdBlock [0] RegistryIdBlock OPTIONAL,
         *   certificationIdBlocks [1] CertificationIdBlocks OPTIONAL,
         *   ...
         * }
         */
        @JvmStatic
        fun from(asn1Encodable: ASN1Encodable): FaceImageCaptureDeviceBlock {
            val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)

            return FaceImageCaptureDeviceBlock(
                model = taggedObjects[0]?.let { RegistryIdBlock.from(it) },
                certifications = taggedObjects[1]?.let { RegistryIdBlock.decodeRegistryIdBlocks(it) }
            )
        }
    }
}
