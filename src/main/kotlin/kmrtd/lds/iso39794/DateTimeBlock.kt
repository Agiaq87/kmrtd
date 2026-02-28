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
 * $Id: DateTimeBlock.java 1896 2025-04-18 21:39:56Z martijno $
 *
 * Based on ISO-IEC-39794-1-ed-1-v1. Disclaimer:
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

data class DateTimeBlock(
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int,
    val minute: Int,
    val second: Int,
    val millisecond: Int
) : Block() {

    override val aSN1Object: ASN1Encodable
        /* PACKAGE */
        get() = ASN1Util.encodeTaggedObjects(
            buildMap {
                put(0, ASN1Util.encodeInt(year))
                if (month >= 0) {
                    put(1, ASN1Util.encodeInt(month))
                }
                if (day >= 0) {
                    put(2, ASN1Util.encodeInt(day))
                }
                if (hour >= 0) {
                    put(3, ASN1Util.encodeInt(hour))
                }
                if (minute >= 0) {
                    put(4, ASN1Util.encodeInt(minute))
                }
                if (second >= 0) {
                    put(5, ASN1Util.encodeInt(second))
                }
                if (millisecond >= 0) {
                    put(6, ASN1Util.encodeInt(millisecond))
                }
            }
        )
        /*get() {
            val taggedObjects: MutableMap<Int?, ASN1Encodable?> =
                HashMap<Int?, ASN1Encodable?>()
            taggedObjects[0] = ASN1Util.encodeInt(year)
            if (month >= 0) {
                taggedObjects[1] = ASN1Util.encodeInt(month)
            }
            if (day >= 0) {
                taggedObjects[2] = ASN1Util.encodeInt(day)
            }
            if (hour >= 0) {
                taggedObjects[3] = ASN1Util.encodeInt(hour)
            }
            if (minute >= 0) {
                taggedObjects[4] = ASN1Util.encodeInt(minute)
            }
            if (second >= 0) {
                taggedObjects[5] = ASN1Util.encodeInt(second)
            }
            if (millisecond >= 0) {
                taggedObjects[6] = ASN1Util.encodeInt(millisecond)
            }
            return ASN1Util.encodeTaggedObjects(taggedObjects)
        }*/

    companion object {
        private const val serialVersionUID = 2053705457048769663L

        /**
         * Factory method
         *
         * DateTimeBlock ::= SEQUENCE {
         *   year          [0] Year,
         *   month         [1] Month          OPTIONAL,
         *   day           [2] Day            OPTIONAL,
         *   hour          [3] Hour           OPTIONAL,
         *   minute        [4] Minute         OPTIONAL,
         *   second        [5] Second         OPTIONAL,
         *   millisecond   [6] Millisecond    OPTIONAL
         * }
         * Year ::= INTEGER (0..9999)
         * Month ::= INTEGER (1..12)
         * Day ::= INTEGER (1..31)
         * Hour ::= INTEGER (0..23)
         * Minute ::= INTEGER (0..59)
         * Second ::= INTEGER (0..59)
         * Millisecond ::= INTEGER (0..999)
         */
        @JvmStatic
        fun from(asn1Encodable: ASN1Encodable?): DateTimeBlock {
            val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)

            return DateTimeBlock(
                year = ASN1Util.decodeInt(taggedObjects[0]),
                month = if (taggedObjects.containsKey(1)) ASN1Util.decodeInt(taggedObjects[1]) else -1,
                day = if (taggedObjects.containsKey(2)) ASN1Util.decodeInt(taggedObjects[2]) else -1,
                hour = if (taggedObjects.containsKey(3)) ASN1Util.decodeInt(taggedObjects[3]) else -1,
                minute = if (taggedObjects.containsKey(4)) ASN1Util.decodeInt(taggedObjects[4]) else -1,
                second = if (taggedObjects.containsKey(5)) ASN1Util.decodeInt(taggedObjects[5]) else -1,
                millisecond =
                    if (taggedObjects.containsKey(6)) ASN1Util.decodeInt(taggedObjects[6]) else -1
            )
        }
    }
}
