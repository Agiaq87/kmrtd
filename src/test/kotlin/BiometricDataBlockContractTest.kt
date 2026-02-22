/*
 * Tests for kmrtd.cbeff package
 *
 * Copyright (C) 2026 Alessandro Giaquinto
 * Licensed under LGPL 3.0
 */

import kmrtd.cbeff.BiometricDataBlock
import kmrtd.cbeff.BiometricDataBlockDecoder
import kmrtd.cbeff.BiometricEncodingType
import kmrtd.cbeff.CBEFFInfo
import kmrtd.cbeff.ComplexCBEFFInfo
import kmrtd.cbeff.SimpleCBEFFInfo
import kmrtd.cbeff.StandardBiometricHeader
import java.io.InputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

// ============================================================
// Test helpers
// ============================================================

/**
 * Minimal BiometricDataBlock implementation for testing.
 */
private class TestBiometricDataBlock(
    override val standardBiometricHeader: StandardBiometricHeader
) : BiometricDataBlock {
    companion object {
        private const val serialVersionUID = 1L
    }
}

/**
 * Minimal BiometricDataBlockDecoder for testing.
 */
private class TestBDBDecoder : BiometricDataBlockDecoder<TestBiometricDataBlock> {
    override fun decode(
        inputStream: InputStream,
        sbh: StandardBiometricHeader,
        index: Int,
        length: Int
    ): TestBiometricDataBlock = TestBiometricDataBlock(sbh)
}

/**
 * Creates a StandardBiometricHeader with typical passport face image elements.
 */
private fun createFaceSBH(): StandardBiometricHeader {
    val elements = mutableMapOf(
        0x80 to byteArrayOf(0x03, 0x01),              // Patron header version
        0x81 to byteArrayOf(0x00, 0x00, 0x02),        // Biometric type: facial features
        0x82 to byteArrayOf(0x00),                     // Biometric subtype: none
        0x87 to byteArrayOf(0x01, 0x01),              // Format owner: JTC1/SC37 (0x0101)
        0x88 to byteArrayOf(0x00, 0x08)               // Format type: face image (0x0008)
    )
    return StandardBiometricHeader(elements)
}

/**
 * Creates a StandardBiometricHeader with finger image elements.
 */
private fun createFingerSBH(): StandardBiometricHeader {
    val elements = mutableMapOf(
        0x80 to byteArrayOf(0x03, 0x01),
        0x81 to byteArrayOf(0x00, 0x00, 0x08),        // Biometric type: fingerprint
        0x82 to byteArrayOf(0x01),                     // Biometric subtype: right
        0x87 to byteArrayOf(0x01, 0x01),
        0x88 to byteArrayOf(0x00, 0x07)               // Format type: finger image (0x0007)
    )
    return StandardBiometricHeader(elements)
}

// ============================================================
// BiometricEncodingType tests
// ============================================================
class BiometricEncodingTypeTest {

    // Raw constant values (matching ISO781611 definitions)
    private val BDB_TAG = 0x5F2E            // BIOMETRIC_DATA_BLOCK_TAG
    private val BDB_CONSTRUCTED_TAG = 0x7F2E // BIOMETRIC_DATA_BLOCK_CONSTRUCTED_TAG

    @Test
    fun `fromBDBTag maps 5F2E to ISO_19794`() {
        assertEquals(BiometricEncodingType.ISO_19794, BiometricEncodingType.fromBDBTag(BDB_TAG))
    }

    @Test
    fun `fromBDBTag maps 7F2E to ISO_39794`() {
        assertEquals(BiometricEncodingType.ISO_39794, BiometricEncodingType.fromBDBTag(BDB_CONSTRUCTED_TAG))
    }

    @Test
    fun `fromBDBTag maps unknown tag to UNKNOWN`() {
        assertEquals(BiometricEncodingType.UNKNOWN, BiometricEncodingType.fromBDBTag(0x0000))
        assertEquals(BiometricEncodingType.UNKNOWN, BiometricEncodingType.fromBDBTag(0xFFFF))
    }

    @Test
    fun `toBDBTag maps ISO_19794 to 5F2E`() {
        assertEquals(BDB_TAG, BiometricEncodingType.toBDBTag(BiometricEncodingType.ISO_19794))
    }

    @Test
    fun `toBDBTag maps ISO_39794 to 7F2E`() {
        assertEquals(BDB_CONSTRUCTED_TAG, BiometricEncodingType.toBDBTag(BiometricEncodingType.ISO_39794))
    }

    @Test
    fun `toBDBTag maps UNKNOWN to default 5F2E`() {
        assertEquals(BDB_TAG, BiometricEncodingType.toBDBTag(BiometricEncodingType.UNKNOWN))
    }

    @Test
    fun `roundtrip fromBDBTag then toBDBTag`() {
        val tag19794 = BiometricEncodingType.toBDBTag(BiometricEncodingType.fromBDBTag(BDB_TAG))
        assertEquals(BDB_TAG, tag19794)

        val tag39794 = BiometricEncodingType.toBDBTag(BiometricEncodingType.fromBDBTag(BDB_CONSTRUCTED_TAG))
        assertEquals(BDB_CONSTRUCTED_TAG, tag39794)
    }

    @Test
    fun `enum has exactly three values`() {
        assertEquals(3, BiometricEncodingType.entries.size)
        assertTrue(BiometricEncodingType.entries.contains(BiometricEncodingType.UNKNOWN))
        assertTrue(BiometricEncodingType.entries.contains(BiometricEncodingType.ISO_19794))
        assertTrue(BiometricEncodingType.entries.contains(BiometricEncodingType.ISO_39794))
    }
}

// ============================================================
// CBEFFInfo constants tests
// ============================================================
class CBEFFInfoConstantsTest {

    @Test
    fun `biometric type constants are distinct power-of-two flags`() {
        val types = listOf(
            CBEFFInfo.BIOMETRIC_TYPE_FACIAL_FEATURES,
            CBEFFInfo.BIOMETRIC_TYPE_VOICE,
            CBEFFInfo.BIOMETRIC_TYPE_FINGERPRINT,
            CBEFFInfo.BIOMETRIC_TYPE_IRIS,
            CBEFFInfo.BIOMETRIC_TYPE_RETINA,
            CBEFFInfo.BIOMETRIC_TYPE_HAND_GEOMETRY,
            CBEFFInfo.BIOMETRIC_TYPE_SIGNATURE_DYNAMICS,
            CBEFFInfo.BIOMETRIC_TYPE_KEYSTROKE_DYNAMICS,
            CBEFFInfo.BIOMETRIC_TYPE_LIP_MOVEMENT,
            CBEFFInfo.BIOMETRIC_TYPE_THERMAL_FACE_IMAGE,
            CBEFFInfo.BIOMETRIC_TYPE_THERMAL_HAND_IMAGE,
            CBEFFInfo.BIOMETRIC_TYPE_GAIT,
            CBEFFInfo.BIOMETRIC_TYPE_BODY_ODOR,
            CBEFFInfo.BIOMETRIC_TYPE_DNA,
            CBEFFInfo.BIOMETRIC_TYPE_EAR_SHAPE,
            CBEFFInfo.BIOMETRIC_TYPE_FINGER_GEOMETRY,
            CBEFFInfo.BIOMETRIC_TYPE_PALM_PRINT,
            CBEFFInfo.BIOMETRIC_TYPE_VEIN_PATTERN,
            CBEFFInfo.BIOMETRIC_TYPE_FOOT_PRINT,
        )
        // All distinct
        assertEquals(types.size, types.toSet().size)

        // Each is a single-bit flag (power of 2), except MULTIPLE which is 1
        for (type in types) {
            assertTrue(type > 0, "Type $type should be positive")
            assertEquals(1, Integer.bitCount(type), "Type 0x${Integer.toHexString(type)} should have exactly one bit set")
        }
    }

    @Test
    fun `facial features is 0x02`() {
        assertEquals(0x000002, CBEFFInfo.BIOMETRIC_TYPE_FACIAL_FEATURES)
    }

    @Test
    fun `fingerprint is 0x08`() {
        assertEquals(0x000008, CBEFFInfo.BIOMETRIC_TYPE_FINGERPRINT)
    }

    @Test
    fun `iris is 0x10`() {
        assertEquals(0x000010, CBEFFInfo.BIOMETRIC_TYPE_IRIS)
    }

    @Test
    fun `biometric types can be combined as bitmask`() {
        val faceAndFinger = CBEFFInfo.BIOMETRIC_TYPE_FACIAL_FEATURES or CBEFFInfo.BIOMETRIC_TYPE_FINGERPRINT
        assertEquals(0x00000A, faceAndFinger)
        assertTrue(faceAndFinger and CBEFFInfo.BIOMETRIC_TYPE_FACIAL_FEATURES != 0)
        assertTrue(faceAndFinger and CBEFFInfo.BIOMETRIC_TYPE_FINGERPRINT != 0)
        assertTrue(faceAndFinger and CBEFFInfo.BIOMETRIC_TYPE_IRIS == 0)
    }

    @Test
    fun `subtype masks`() {
        assertEquals(0x01, CBEFFInfo.BIOMETRIC_SUBTYPE_MASK_RIGHT)
        assertEquals(0x02, CBEFFInfo.BIOMETRIC_SUBTYPE_MASK_LEFT)
        assertEquals(0x04, CBEFFInfo.BIOMETRIC_SUBTYPE_MASK_THUMB)
    }
}

// ============================================================
// SimpleCBEFFInfo tests
// ============================================================
class SimpleCBEFFInfoTest {

    @Test
    fun `wraps biometric data block`() {
        val sbh = createFaceSBH()
        val bdb = TestBiometricDataBlock(sbh)
        val info = SimpleCBEFFInfo(bdb)
        assertEquals(bdb, info.biometricDataBlock)
    }

    @Test
    fun `implements CBEFFInfo`() {
        val info = SimpleCBEFFInfo(TestBiometricDataBlock(createFaceSBH()))
        assertTrue(info is CBEFFInfo)
    }

    @Test
    fun `biometricDataBlock returns same reference`() {
        val bdb = TestBiometricDataBlock(createFaceSBH())
        val info = SimpleCBEFFInfo(bdb)
        assertTrue(bdb === info.biometricDataBlock)
    }
}

// ============================================================
// ComplexCBEFFInfo tests
// ============================================================
class ComplexCBEFFInfoTest {

    @Test
    fun `starts empty`() {
        val complex = ComplexCBEFFInfo<TestBiometricDataBlock>()
        assertTrue(complex.getSubRecords().isEmpty())
    }

    @Test
    fun `add and retrieve sub-records`() {
        val complex = ComplexCBEFFInfo<TestBiometricDataBlock>()
        val simple1 = SimpleCBEFFInfo(TestBiometricDataBlock(createFaceSBH()))
        val simple2 = SimpleCBEFFInfo(TestBiometricDataBlock(createFingerSBH()))

        complex.add(simple1)
        complex.add(simple2)

        val records = complex.getSubRecords()
        assertEquals(2, records.size)
    }

    @Test
    fun `getSubRecords returns defensive copy`() {
        val complex = ComplexCBEFFInfo<TestBiometricDataBlock>()
        val simple = SimpleCBEFFInfo(TestBiometricDataBlock(createFaceSBH()))
        complex.add(simple)

        val copy = complex.getSubRecords()
        copy.clear()

        // Original should be unaffected
        assertEquals(1, complex.getSubRecords().size)
    }

    @Test
    fun `addAll adds multiple records`() {
        val complex = ComplexCBEFFInfo<TestBiometricDataBlock>()
        val records = mutableListOf<CBEFFInfo>(
            SimpleCBEFFInfo(TestBiometricDataBlock(createFaceSBH())),
            SimpleCBEFFInfo(TestBiometricDataBlock(createFingerSBH()))
        )
        complex.addAll(records)
        assertEquals(2, complex.getSubRecords().size)
    }

    @Test
    fun `remove by index`() {
        val complex = ComplexCBEFFInfo<TestBiometricDataBlock>()
        val simple1 = SimpleCBEFFInfo(TestBiometricDataBlock(createFaceSBH()))
        val simple2 = SimpleCBEFFInfo(TestBiometricDataBlock(createFingerSBH()))
        complex.add(simple1)
        complex.add(simple2)

        complex.remove(0)
        assertEquals(1, complex.getSubRecords().size)
    }

    @Test
    fun `equals with same sub-records`() {
        val complex1 = ComplexCBEFFInfo<TestBiometricDataBlock>()
        val complex2 = ComplexCBEFFInfo<TestBiometricDataBlock>()

        // Both empty — should be equal
        assertEquals(complex1, complex2)
    }

    @Test
    fun `equals reflexive`() {
        val complex = ComplexCBEFFInfo<TestBiometricDataBlock>()
        assertEquals(complex, complex)
    }

    @Test
    fun `not equals null`() {
        val complex = ComplexCBEFFInfo<TestBiometricDataBlock>()
        assertFalse(complex.equals(null))
    }

    @Test
    fun `not equals different type`() {
        val complex = ComplexCBEFFInfo<TestBiometricDataBlock>()
        assertFalse(complex.equals("not a ComplexCBEFFInfo"))
    }

    @Test
    fun `hashCode consistent with equals`() {
        val complex1 = ComplexCBEFFInfo<TestBiometricDataBlock>()
        val complex2 = ComplexCBEFFInfo<TestBiometricDataBlock>()
        assertEquals(complex1.hashCode(), complex2.hashCode())
    }

    @Test
    fun `implements CBEFFInfo`() {
        val complex = ComplexCBEFFInfo<TestBiometricDataBlock>()
        assertTrue(complex is CBEFFInfo)
    }

    @Test
    fun `can nest ComplexCBEFFInfo inside ComplexCBEFFInfo`() {
        val outer = ComplexCBEFFInfo<TestBiometricDataBlock>()
        val inner = ComplexCBEFFInfo<TestBiometricDataBlock>()
        inner.add(SimpleCBEFFInfo(TestBiometricDataBlock(createFaceSBH())))

        outer.add(inner)
        assertEquals(1, outer.getSubRecords().size)
        assertTrue(outer.getSubRecords()[0] is ComplexCBEFFInfo<*>)
    }
}

// ============================================================
// StandardBiometricHeader tests
// ============================================================
class StandardBiometricHeaderTest {

    @Test
    fun `construction stores elements`() {
        val sbh = createFaceSBH()
        val elements = sbh.getElements()
        assertEquals(5, elements.size)
        assertTrue(elements.containsKey(0x80))
        assertTrue(elements.containsKey(0x81))
        assertTrue(elements.containsKey(0x87))
        assertTrue(elements.containsKey(0x88))
    }

    @Test
    fun `getElements returns sorted defensive copy`() {
        val sbh = createFaceSBH()
        val copy = sbh.getElements()
        copy.clear()

        // Original should be unaffected
        assertEquals(5, sbh.getElements().size)
    }

    @Test
    fun `getElements returns sorted by tag`() {
        val sbh = createFaceSBH()
        val keys = sbh.getElements().keys.toList()
        assertEquals(keys.sorted(), keys)
    }

    @Test
    fun `hasFormatType returns true for matching face format`() {
        val sbh = createFaceSBH()
        assertTrue(sbh.hasFormatType(StandardBiometricHeader.ISO_19794_FACE_IMAGE_FORMAT_TYPE_VALUE))
    }

    @Test
    fun `hasFormatType returns true for matching finger format`() {
        val sbh = createFingerSBH()
        assertTrue(sbh.hasFormatType(StandardBiometricHeader.ISO_19794_FINGER_IMAGE_FORMAT_TYPE_VALUE))
    }

    @Test
    fun `hasFormatType returns false for wrong format`() {
        val sbh = createFaceSBH()
        assertFalse(sbh.hasFormatType(StandardBiometricHeader.ISO_19794_FINGER_IMAGE_FORMAT_TYPE_VALUE))
    }

    @Test
    fun `hasFormatType returns false when FORMAT_TYPE_TAG is absent`() {
        val sbh = StandardBiometricHeader(mutableMapOf(
            0x80 to byteArrayOf(0x03, 0x01)
        ))
        assertFalse(sbh.hasFormatType(0x0008))
    }

    @Test
    fun `hasFormatType returns false for wrong-size format type value`() {
        val sbh = StandardBiometricHeader(mutableMapOf(
            0x88 to byteArrayOf(0x08)  // Only 1 byte, expected 2
        ))
        assertFalse(sbh.hasFormatType(0x0008))
    }

    @Test
    fun `equals with same elements`() {
        val sbh1 = createFaceSBH()
        val sbh2 = createFaceSBH()
        assertEquals(sbh1, sbh2)
    }

    @Test
    fun `equals reflexive`() {
        val sbh = createFaceSBH()
        assertEquals(sbh, sbh)
    }

    @Test
    fun `not equals null`() {
        val sbh = createFaceSBH()
        assertFalse(sbh.equals(null))
    }

    @Test
    fun `not equals different type`() {
        val sbh = createFaceSBH()
        assertFalse(sbh.equals("string"))
    }

    @Test
    fun `not equals with different elements`() {
        val sbh1 = createFaceSBH()
        val sbh2 = createFingerSBH()
        assertNotEquals(sbh1, sbh2)
    }

    @Test
    fun `hashCode consistent with equals`() {
        val sbh1 = createFaceSBH()
        val sbh2 = createFaceSBH()
        assertEquals(sbh1.hashCode(), sbh2.hashCode())
    }

    @Test
    fun `hashCode differs for different headers`() {
        val sbh1 = createFaceSBH()
        val sbh2 = createFingerSBH()
        // Not guaranteed but very likely
        assertNotEquals(sbh1.hashCode(), sbh2.hashCode())
    }

    @Test
    fun `toString contains class name`() {
        val sbh = createFaceSBH()
        val str = sbh.toString()
        assertTrue(str.startsWith("StandardBiometricHeader ["))
        assertTrue(str.endsWith("]"))
    }

    @Test
    fun `toString contains hex tags`() {
        val sbh = StandardBiometricHeader(mutableMapOf(
            0x87 to byteArrayOf(0x01, 0x01)
        ))
        val str = sbh.toString()
        assertTrue(str.contains("87"), "toString should contain hex tag '87', got: $str")
    }

    @Test
    fun `format type constants are distinct`() {
        val values = setOf(
            StandardBiometricHeader.ISO_19794_FINGER_IMAGE_FORMAT_TYPE_VALUE,
            StandardBiometricHeader.ISO_19794_FACE_IMAGE_FORMAT_TYPE_VALUE,
            StandardBiometricHeader.ISO_19794_IRIS_IMAGE_FORMAT_TYPE_VALUE,
            StandardBiometricHeader.ISO_39794_FINGER_IMAGE_FORMAT_TYPE_VALUE,
            StandardBiometricHeader.ISO_39794_FACE_IMAGE_FORMAT_TYPE_VALUE,
            StandardBiometricHeader.ISO_39794_IRIS_IMAGE_FORMAT_TYPE_VALUE,
        )
        assertEquals(6, values.size)
    }

    @Test
    fun `JTC1_SC37 format owner constant`() {
        assertEquals(0x0101, StandardBiometricHeader.JTC1_SC37_FORMAT_OWNER_VALUE)
    }

    @Test
    fun `empty elements header`() {
        val sbh = StandardBiometricHeader(mutableMapOf())
        assertTrue(sbh.getElements().isEmpty())
        assertFalse(sbh.hasFormatType(0x0008))
    }

    @Test
    fun `implements Serializable`() {
        val sbh = createFaceSBH()
        assertTrue(sbh is java.io.Serializable)
    }
}

// ============================================================
// BiometricDataBlock interface contract tests
// ============================================================
class BiometricDataBlockContractTest {

    @Test
    fun `standardBiometricHeader is non-null`() {
        val sbh = createFaceSBH()
        val bdb = TestBiometricDataBlock(sbh)
        // Contract: standardBiometricHeader must not be null
        assertEquals(sbh, bdb.standardBiometricHeader)
    }

    @Test
    fun `implements Serializable`() {
        val bdb = TestBiometricDataBlock(createFaceSBH())
        assertTrue(bdb is java.io.Serializable)
    }
}

// ============================================================
// BiometricDataBlockDecoder interface contract tests
// ============================================================
class BiometricDataBlockDecoderContractTest {

    @Test
    fun `decode returns non-null BDB`() {
        val decoder = TestBDBDecoder()
        val sbh = createFaceSBH()
        val result = decoder.decode(
            java.io.ByteArrayInputStream(ByteArray(0)),
            sbh,
            0,
            0
        )
        assertEquals(sbh, result.standardBiometricHeader)
    }
}