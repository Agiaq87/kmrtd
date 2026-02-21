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
 * $Id: FingerImageSpatialSamplingRateBlock.java 1892 2025-03-18 15:15:52Z martijno $
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
package kmrtd.lds.iso39794

import org.bouncycastle.asn1.ASN1Encodable
import org.jmrtd.ASN1Util
import java.util.*

class FingerImageSpatialSamplingRateBlock : Block {
    //  UnitDimensionCode ::= ENUMERATED {
    //    inch(0),
    //    cm(1)
    //  }
    enum class UnitDimensionCode(private val code: Int) : EncodableEnum<UnitDimensionCode?> {
        INCH(0),
        CM(1);

        override fun getCode(): Int {
            return code
        }

        companion object {
            fun fromCode(code: Int): UnitDimensionCode? {
                return EncodableEnum.fromCode<UnitDimensionCode?>(code, UnitDimensionCode::class.java)
            }
        }
    }

    val samplesPerUnit: Int

    val unitDimension: UnitDimensionCode?

    constructor(samplesPerUnit: Int, unitDimension: UnitDimensionCode?) {
        this.samplesPerUnit = samplesPerUnit
        this.unitDimension = unitDimension
    }

    //  SpatialSamplingRateBlock ::= SEQUENCE {
    //    samplesPerUnit [0] INTEGER (0..65535),
    //    unitDimension [1] UnitDimensionCode
    //  }
    internal constructor(asn1Encodable: ASN1Encodable?) {
        val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)
        samplesPerUnit = ASN1Util.decodeInt(taggedObjects.get(0))
        unitDimension = UnitDimensionCode.Companion.fromCode(ASN1Util.decodeInt(taggedObjects.get(1)))
    }

    public override fun hashCode(): Int {
        return Objects.hash(samplesPerUnit, unitDimension)
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

        val other = obj as FingerImageSpatialSamplingRateBlock
        return samplesPerUnit == other.samplesPerUnit && unitDimension == other.unitDimension
    }

    override fun toString(): String {
        return ("FingerImageSpatialSamplingRateBlock ["
                + "samplesPerUnit: " + samplesPerUnit
                + ", unitDimension: " + unitDimension
                + "]")
    }

    val aSN1Object: ASN1Encodable?
        /* PACKAGE */
        get() {
            val taggedObjects: MutableMap<Int?, ASN1Encodable?> =
                HashMap<Int?, ASN1Encodable?>()
            taggedObjects.put(0, ASN1Util.encodeInt(samplesPerUnit))
            taggedObjects.put(1, ASN1Util.encodeInt(unitDimension!!.getCode()))
            return ASN1Util.encodeTaggedObjects(taggedObjects)
        }

    companion object {
        private const val serialVersionUID = 3134105261906116624L
    }
}
