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
 * $Id: StandardBiometricHeader.java 1905 2025-09-25 08:49:09Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.cbeff

import net.sf.scuba.util.Hex
import java.io.Serializable
import java.util.SortedMap
import java.util.TreeMap

/**
 * A Standard Biometric Header preceeds a Biometric Data Block.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * @version $Revision: 1905 $
 * @since 0.4.7
 */
class StandardBiometricHeader(elements: MutableMap<Int, ByteArray?>) : Serializable {
    // TODO Change in elements when fully translate to Kotlin
    val sortedElements: SortedMap<Int, ByteArray?> = TreeMap(elements)

    /**
     * Checks whether the format type is present and equals to the given value.
     * 
     * @param formatTypeValue a format type (short) value
     * @return a boolean indicating the format type is present and equal to the given value
     */
    fun hasFormatType(formatTypeValue: Int): Boolean {
        val actualFormatTypeValue = sortedElements[ISO781611.FORMAT_TYPE_TAG] ?: return false
        if (actualFormatTypeValue.size != 2) {
            return false
        }

        val actualFormatTypeInt =
            ((actualFormatTypeValue[0].toInt() and 0xFF) shl 8) or (actualFormatTypeValue[1].toInt() and 0xFF)
        return actualFormatTypeInt == formatTypeValue
    }

    override fun toString(): String {
        val result = StringBuilder()
        result.append("StandardBiometricHeader [")
        var isFirst = true
        for (entry in sortedElements.entries) {
            if (isFirst) {
                isFirst = false
            } else {
                result.append(", ")
            }
            result.append(Integer.toHexString(entry.key)).append(" -> ")
                .append(Hex.bytesToHexString(entry.value))
        }
        result.append("]")
        return result.toString()
    }

    override fun hashCode(): Int =
        sortedElements.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StandardBiometricHeader) return false
        if (sortedElements.keys != other.sortedElements.keys) return false
        return sortedElements.entries.all { (key, bytes) ->
            bytes.contentEquals(other.sortedElements[key])
        }
    }

    companion object {
        /**
         * Format owner identifier of ISO/IEC JTC1/SC37. See:
         * https://www.ibia.org/cbeff/iso/bdb-format-identifiers.
         */
        const val JTC1_SC37_FORMAT_OWNER_VALUE: Int = 0x0101

        /**
         * ISO/IEC JTC1/SC37 uses 0x0007. See:
         * https://www.ibia.org/cbeff/iso/bdb-format-identifiers.
         * (ISO FCD 19794-4 specified this as 0x0401).
         */
        const val ISO_19794_FINGER_IMAGE_FORMAT_TYPE_VALUE: Int = 0x0007

        /**
         * ISO/IEC JTC1/SC37 uses 0x0008. See:
         * https://www.ibia.org/cbeff/iso/bdb-format-identifiers.
         * Also see supplement to Doc 9303: R3-p1_v2_sII_0001.
         * (ISO FCD 19794-5 specified this as 0x0501).
         */
        const val ISO_19794_FACE_IMAGE_FORMAT_TYPE_VALUE: Int = 0x0008

        /**
         * ISO/IEC JTC1/SC37 uses 0x0009. See:
         * https://www.ibia.org/cbeff/iso/bdb-format-identifiers.
         * (ISO FCD 19794-6 specified this as 0x0601).
         */
        const val ISO_19794_IRIS_IMAGE_FORMAT_TYPE_VALUE: Int = 0x0009

        /**
         * Corresponds to `g3-binary-finger-image`. See:
         * https://www.ibia.org/cbeff/iso/bdb-format-identifiers.
         */
        const val ISO_39794_FINGER_IMAGE_FORMAT_TYPE_VALUE: Int = 0x0028

        /**
         * Corresponds to `g3-binary-face-image`. See:
         * https://www.ibia.org/cbeff/iso/bdb-format-identifiers.
         */
        const val ISO_39794_FACE_IMAGE_FORMAT_TYPE_VALUE: Int = 0x002A

        /**
         * Corresponds to `g3-binary-iris-image`. See:
         * https://www.ibia.org/cbeff/iso/bdb-format-identifiers.
         */
        const val ISO_39794_IRIS_IMAGE_FORMAT_TYPE_VALUE: Int = 0x002C
        //private const val serialVersionUID = 4113147521594478513L
    }
}
