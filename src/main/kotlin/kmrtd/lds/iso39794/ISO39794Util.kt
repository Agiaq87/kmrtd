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
 * $Id: ISO39794Util.java 1905 2025-09-25 08:49:09Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.lds.iso39794

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import kmrtd.ASN1Util

internal object ISO39794Util {
    fun decodeCodeFromChoiceExtensionBlockFallback(asn1Encodable: ASN1Encodable?): Int? {
        val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)
        if (taggedObjects.containsKey(0)) {
            return ASN1Util.decodeInt(taggedObjects.get(0))
        }
        if (taggedObjects.containsKey(1)) {
            val extensionTaggedObjects = ASN1Util.decodeTaggedObjects(taggedObjects.get(1))
            /* Fallback: */
            return ASN1Util.decodeInt(extensionTaggedObjects.get(0))
        }

        return null
    }

    fun encodeCodeAsChoiceExtensionBlockFallback(code: Int): ASN1Encodable {
        return DERSequence(DERTaggedObject(false, 0, ASN1Util.encodeInt(code)))
    }

    //  ScoreOrError ::= CHOICE {
    //    score   [0] Score,
    //    error   [1] ScoringError
    //  }
    @JvmStatic
    fun decodeScoreOrError(asn1Encodable: ASN1Encodable?): Int {
        val taggedObjects = ASN1Util.decodeTaggedObjects(asn1Encodable)
        if (taggedObjects.containsKey(0)) {
            return ASN1Util.decodeInt(taggedObjects.get(0))
        }

        /* NOTE: We could navigate the object under [1], and distinguish between failureToAssess or extension. */
        return -1
    }

    @JvmStatic
    fun encodeScoreOrError(score: Int): ASN1Encodable? {
        val taggedObjects: MutableMap<Int?, ASN1Encodable?> = HashMap<Int?, ASN1Encodable?>()
        if (score >= 0) {
            taggedObjects.put(0, ASN1Util.encodeInt(score))
        }
        return ASN1Util.encodeTaggedObjects(taggedObjects)
    }

    fun encodeBlocks(blocks: MutableList<out Block?>?): ASN1Encodable? {
        if (blocks == null) {
            return null
        }
        val asn1Objects: MutableList<ASN1Encodable?> = ArrayList<ASN1Encodable?>(blocks.size)
        for (block in blocks) {
            if (block != null) {
                asn1Objects.add(block.aSN1Object)
            }
        }
        return DERSequence(asn1Objects.toTypedArray<ASN1Encodable?>())
    }
}
