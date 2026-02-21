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
 * $Id: ASN1Util.java 1905 2025-09-25 08:49:09Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd

import org.bouncycastle.asn1.*
import java.io.IOException
import java.io.InputStream
import java.math.BigInteger
import java.util.logging.Logger

object ASN1Util {
    private val LOGGER: Logger = Logger.getLogger("org.jmrtd")

    @Throws(IOException::class)
    fun readASN1Object(inputStream: InputStream?): ASN1Encodable? {
        val asn1InputStream = ASN1InputStream(inputStream, true)
        return asn1InputStream.readObject()
    }

    /**
     * Checks whether an ASN1 object is a tagged object with a specific tag class and tag number.
     * Throws an unchecked exception if not.
     * 
     * @param asn1Encodable the ASN1 object
     * @param tagClass the expected tag class
     * @param tagNo the exepected tag number
     * 
     * @return the base object
     */
    fun checkTag(asn1Encodable: ASN1Encodable, tagClass: Int, tagNo: Int): ASN1Encodable? {
        requireNotNull(asn1Encodable) { "Expected a tagged object. Found null." }

        require(asn1Encodable is ASN1TaggedObject) { "Expected a tagged object. Found " + asn1Encodable.javaClass }

        val asn1TaggedObject = ASN1TaggedObject.getInstance(asn1Encodable)
        require(!(asn1TaggedObject.getTagClass() != tagClass && asn1TaggedObject.getTagNo() != tagNo)) {
            ("Expected "
                    + "[" + tagClassToString(tagClass) + " " + tagNo + "], found "
                    + "[" + tagClassToString(asn1TaggedObject.getTagClass())
                    + " " + asn1TaggedObject.getTagNo() + "]")
        }

        return asn1TaggedObject.getBaseObject()
    }

    /**
     * Checks whether an ASN1 object is itself a sequence and contains sequences.
     * 
     * @param asn1Encodable an ASN1 object
     * 
     * @return a boolean indicating whether the ASN1 object is a sequence of only sequences
     */
    fun isSequenceOfSequences(asn1Encodable: ASN1Encodable?): Boolean {
        if (asn1Encodable !is ASN1Sequence) {
            return false
        }
        val asn1Sequence = ASN1Sequence.getInstance(asn1Encodable)
        val count = asn1Sequence.size()
        for (i in 0..<count) {
            val asn1Object = asn1Sequence.getObjectAt(i)
            if (asn1Object !is ASN1Sequence) {
                return false
            }
        }

        return true
    }

    /**
     * Converts an ASN1 sequence of tagged objects to a map.
     * Maps tag numbers to base objects.
     * 
     * @param asn1Encodable an ASN1 sequence of tagged objects
     * 
     * @return a map
     */
    fun decodeTaggedObjects(asn1Encodable: ASN1Encodable?): MutableMap<Int?, ASN1Encodable?> {
        val taggedObjects: MutableMap<Int?, ASN1Encodable?> = HashMap<Int?, ASN1Encodable?>()
        if (asn1Encodable == null) {
            return taggedObjects
        }

        if (asn1Encodable is ASN1Sequence) {
            val asn1Sequence = ASN1Sequence.getInstance(asn1Encodable)

            val count = asn1Sequence.size()
            for (i in 0..<count) {
                val asn1Object = asn1Sequence.getObjectAt(i)
                if (asn1Object !is ASN1TaggedObject) {
                    LOGGER.warning("Not a tagged object. Skipping " + asn1Object.javaClass)
                    continue
                }

                val asn1TaggedObject = ASN1TaggedObject.getInstance(asn1Object)
                val tagClass = asn1TaggedObject.getTagClass()
                val tagNo = asn1TaggedObject.getTagNo()
                if (taggedObjects.containsKey(tagNo)) {
                    LOGGER.warning("Double key " + tagNo)
                }
                val baseObject: ASN1Encodable? = asn1TaggedObject.getBaseObject()
                taggedObjects.put(tagNo, baseObject)
            }
        } else if (asn1Encodable is ASN1TaggedObject) {
            val asn1TaggedObject = ASN1TaggedObject.getInstance(asn1Encodable)
            val tagNo = asn1TaggedObject.getTagNo()
            taggedObjects.put(tagNo, asn1TaggedObject.getBaseObject())
        } else {
            throw IllegalArgumentException("Not a sequence and not a tagged object " + asn1Encodable.javaClass)
        }

        return taggedObjects
    }

    /**
     * Converts an ASN1 sequence to a list.
     * 
     * @param asn1Encodable the ASN1 sequence
     * 
     * @return a list with element ASN1 objects
     */
    fun list(asn1Encodable: ASN1Encodable?): MutableList<ASN1Encodable?>? {
        if (asn1Encodable == null) {
            return null
        }

        if (asn1Encodable is ASN1Sequence) {
            val asn1Sequence = asn1Encodable
            val count = asn1Sequence.size()
            val result: MutableList<ASN1Encodable?> = ArrayList<ASN1Encodable?>(count)
            for (i in 0..<count) {
                val subObject = asn1Sequence.getObjectAt(i)
                result.add(subObject)
            }
            return result
        }

        return mutableListOf<ASN1Encodable?>(asn1Encodable)
    }

    fun decodeInt(asn1Encodable: ASN1Encodable?): Int {
        val bigInteger = decodeBigInteger(asn1Encodable)
        if (bigInteger == null) {
            throw NumberFormatException("Could not parse integer")
        }
        return bigInteger.toInt()
    }

    fun decodeBigInteger(asn1Encodable: ASN1Encodable?): BigInteger {
        if (asn1Encodable !is ASN1OctetString) {
            throw NumberFormatException("Could not parse integer")
        }
        val octetString = ASN1OctetString.getInstance(asn1Encodable)
        if (octetString == null) {
            throw NumberFormatException("Could not parse integer")
        }
        val octets = octetString.getOctets()
        return BigInteger(octets)
    }

    fun decodeBoolean(asn1Encodable: ASN1Encodable?): Boolean {
        if (asn1Encodable is ASN1Boolean) {
            val asn1Boolean = asn1Encodable
            return asn1Boolean.isTrue()
        } else if (asn1Encodable is ASN1OctetString) {
            val octets = asn1Encodable.getOctets()
            return (octets[0].toInt() and 0xFF) != 0x00
        } else {
            throw IllegalArgumentException("Could not decode boolean from " + asn1Encodable)
        }
    }

    fun encodeBoolean(b: Boolean): ASN1Encodable {
        return DEROctetString(byteArrayOf((if (b) 0xFF else 0x00).toByte()))
    }

    fun encodeInt(n: Int): ASN1Encodable {
        return encodeBigInteger(BigInteger.valueOf(n.toLong()))
    }

    fun encodeBigInteger(n: BigInteger): ASN1Encodable {
        return DEROctetString(n.toByteArray())
    }

    fun encodeTaggedObjects(taggedObjects: MutableMap<Int?, ASN1Encodable?>?): ASN1Encodable? {
        if (taggedObjects == null) {
            return null
        }
        val asn1Objects: MutableList<ASN1Encodable?> = ArrayList<ASN1Encodable?>(taggedObjects.size)
        for (entry in taggedObjects.entries) {
            val `object` = entry.value
            if (`object` != null) {
                asn1Objects.add(DERTaggedObject(false, entry.key!!, `object`))
            }
        }
        return DERSequence(asn1Objects.toTypedArray<ASN1Encodable?>())
    }

    /* PRIVATE. */
    private fun tagClassToString(tagClass: Int): String {
        return when (tagClass) {
            BERTags.APPLICATION -> "APPLICATION"
            BERTags.UNIVERSAL -> "UNIVERSAL"
            BERTags.CONTEXT_SPECIFIC -> "CONTEXT_SPECIFIC"
            BERTags.PRIVATE -> "PRIVATE"
            else -> tagClass.toString()
        }
    }
}
