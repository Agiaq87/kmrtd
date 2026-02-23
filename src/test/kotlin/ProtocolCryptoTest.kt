import kmrtd.BACKey
import kmrtd.Util
import kmrtd.protocol.AESSecureMessagingWrapper
import kmrtd.protocol.BACProtocol
import kmrtd.protocol.DESedeSecureMessagingWrapper
import kmrtd.protocol.PACEGMWithECDHAgreement
import kmrtd.protocol.PACEProtocol
import kmrtd.protocol.SecureMessagingWrapper
import kmrtd.support.DocumentNumber
import kmrtd.support.ICAODate
import java.math.BigInteger
import java.security.InvalidKeyException
import java.security.KeyPairGenerator
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.ECParameterSpec
import java.security.spec.ECPoint
import javax.crypto.SecretKey
import javax.crypto.spec.DHParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertTrue

// ============================================================================
// Helper: generate DESede and AES keys for tests
// ============================================================================

private fun desedeKey(seed: Byte = 0x01): SecretKey {
    val bytes = ByteArray(24) { (it + seed).toByte() }
    return SecretKeySpec(bytes, "DESede")
}

private fun aesKey128(seed: Byte = 0x01): SecretKey {
    val bytes = ByteArray(16) { (it + seed).toByte() }
    return SecretKeySpec(bytes, "AES")
}

// ============================================================================
// DESedeSecureMessagingWrapper
// ============================================================================

class DESedeSecureMessagingWrapperTest {

    private val ksEnc = desedeKey(0x10)
    private val ksMac = desedeKey(0x20)

    @Test
    fun typeIsDESede() {
        val wrapper = DESedeSecureMessagingWrapper(ksEnc, ksMac, 0L)
        assertEquals("DESede", wrapper.getType())
    }

    @Test
    fun padLengthIs8() {
        val wrapper = DESedeSecureMessagingWrapper(ksEnc, ksMac, 0L)
        assertEquals(8, wrapper.getPadLength())
    }

    @Test
    fun defaultMaxTranceiveLengthIs256() {
        val wrapper = DESedeSecureMessagingWrapper(ksEnc, ksMac, 0L)
        assertEquals(256, wrapper.maxTranceiveLength)
    }

    @Test
    fun customMaxTranceiveLength() {
        val wrapper = DESedeSecureMessagingWrapper(ksEnc, ksMac, 65536, true, 0L)
        assertEquals(65536, wrapper.maxTranceiveLength)
    }

    @Test
    fun sendSequenceCounterInitialized() {
        val wrapper = DESedeSecureMessagingWrapper(ksEnc, ksMac, 42L)
        assertEquals(42L, wrapper.sendSequenceCounter)
    }

    @Test
    fun shouldCheckMACDefaultTrue() {
        val wrapper = DESedeSecureMessagingWrapper(ksEnc, ksMac, 0L)
        assertTrue(wrapper.shouldCheckMAC())
    }

    @Test
    fun shouldCheckMACSetFalse() {
        val wrapper = DESedeSecureMessagingWrapper(ksEnc, ksMac, false)
        assertFalse(wrapper.shouldCheckMAC())
    }

    @Test
    fun keysStored() {
        val wrapper = DESedeSecureMessagingWrapper(ksEnc, ksMac, 0L)
        assertEquals(ksEnc, wrapper.encryptionKey)
        assertEquals(ksMac, wrapper.mACKey)
    }

    @Test
    fun encodedSSCLength8Bytes() {
        val wrapper = DESedeSecureMessagingWrapper(ksEnc, ksMac, 0L)
        val encoded = wrapper.getEncodedSendSequenceCounter()
        assertNotNull(encoded)
        assertEquals(8, encoded.size)
    }

    @Test
    fun encodedSSCReflectsValue() {
        val wrapper = DESedeSecureMessagingWrapper(ksEnc, ksMac, 1L)
        val encoded = wrapper.getEncodedSendSequenceCounter()!!
        // SSC = 1 → last byte should be 0x01
        assertEquals(0x01.toByte(), encoded[7])
        assertEquals(0x00.toByte(), encoded[6])
    }

    @Test
    fun encodedSSCZero() {
        val wrapper = DESedeSecureMessagingWrapper(ksEnc, ksMac, 0L)
        val encoded = wrapper.getEncodedSendSequenceCounter()!!
        assertTrue(encoded.all { it == 0x00.toByte() })
    }

    @Test
    fun zeroIVConstantIs8Zeros() {
        val iv = DESedeSecureMessagingWrapper.ZERO_IV_PARAM_SPEC
        assertEquals(8, iv.iv.size)
        assertTrue(iv.iv.all { it == 0x00.toByte() })
    }

    @Test
    fun equalsSameKeys() {
        val a = DESedeSecureMessagingWrapper(ksEnc, ksMac, 0L)
        val b = DESedeSecureMessagingWrapper(ksEnc, ksMac, 0L)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun notEqualsDifferentSSC() {
        val a = DESedeSecureMessagingWrapper(ksEnc, ksMac, 0L)
        val b = DESedeSecureMessagingWrapper(ksEnc, ksMac, 1L)
        assertNotEquals(a, b)
    }

    @Test
    fun notEqualsDifferentKeys() {
        val a = DESedeSecureMessagingWrapper(ksEnc, ksMac, 0L)
        val b = DESedeSecureMessagingWrapper(desedeKey(0x30), ksMac, 0L)
        assertNotEquals(a, b)
    }

    @Test
    fun notEqualsNull() {
        val a = DESedeSecureMessagingWrapper(ksEnc, ksMac, 0L)
        assertFalse(a.equals(null))
    }

    @Test
    fun notEqualsDifferentType() {
        val a = DESedeSecureMessagingWrapper(ksEnc, ksMac, 0L)
        assertFalse(a.equals("not a wrapper"))
    }

    @Test
    fun copyConstructorCreatesEqual() {
        val original = DESedeSecureMessagingWrapper(ksEnc, ksMac, 42L)
        val copy = DESedeSecureMessagingWrapper(original)
        assertEquals(original, copy)
        assertNotSame(original, copy)
    }

    @Test
    fun toStringContainsType() {
        val wrapper = DESedeSecureMessagingWrapper(ksEnc, ksMac, 0L)
        assertTrue(wrapper.toString().contains("DESede"))
    }
}

// ============================================================================
// AESSecureMessagingWrapper
// ============================================================================

class AESSecureMessagingWrapperTest {

    private val ksEnc = aesKey128(0x10)
    private val ksMac = aesKey128(0x20)

    @Test
    fun typeIsAES() {
        val wrapper = AESSecureMessagingWrapper(ksEnc, ksMac, 0L)
        assertEquals("AES", wrapper.getType())
    }

    @Test
    fun padLengthIs16() {
        val wrapper = AESSecureMessagingWrapper(ksEnc, ksMac, 0L)
        assertEquals(16, wrapper.getPadLength())
    }

    @Test
    fun defaultMaxTranceiveLengthIs256() {
        val wrapper = AESSecureMessagingWrapper(ksEnc, ksMac, 0L)
        assertEquals(256, wrapper.maxTranceiveLength)
    }

    @Test
    fun sendSequenceCounterInitialized() {
        val wrapper = AESSecureMessagingWrapper(ksEnc, ksMac, 99L)
        assertEquals(99L, wrapper.sendSequenceCounter)
    }

    @Test
    fun encodedSSCLength16Bytes() {
        val wrapper = AESSecureMessagingWrapper(ksEnc, ksMac, 0L)
        val encoded = wrapper.getEncodedSendSequenceCounter()
        assertNotNull(encoded)
        assertEquals(16, encoded.size)
    }

    @Test
    fun encodedSSCFirst8BytesZeroPadded() {
        val wrapper = AESSecureMessagingWrapper(ksEnc, ksMac, 1L)
        val encoded = wrapper.getEncodedSendSequenceCounter()!!
        // First 8 bytes should be 0x00 (padding)
        for (i in 0..7) {
            assertEquals(0x00.toByte(), encoded[i], "byte[$i] should be 0")
        }
        // Last byte should be 0x01
        assertEquals(0x01.toByte(), encoded[15])
    }

    @Test
    fun encodedSSCZero() {
        val wrapper = AESSecureMessagingWrapper(ksEnc, ksMac, 0L)
        val encoded = wrapper.getEncodedSendSequenceCounter()!!
        assertTrue(encoded.all { it == 0x00.toByte() })
    }

    @Test
    fun encodedSSCLargeValue() {
        val wrapper = AESSecureMessagingWrapper(ksEnc, ksMac, 0x0102030405060708L)
        val encoded = wrapper.getEncodedSendSequenceCounter()!!
        assertEquals(16, encoded.size)
        // Bytes 8-15 encode the long value 0x0102030405060708
        assertEquals(0x01.toByte(), encoded[8])
        assertEquals(0x02.toByte(), encoded[9])
        assertEquals(0x03.toByte(), encoded[10])
        assertEquals(0x08.toByte(), encoded[15])
    }

    @Test
    fun differentSSCProducesDifferentEncoding() {
        val w1 = AESSecureMessagingWrapper(ksEnc, ksMac, 0L)
        val w2 = AESSecureMessagingWrapper(ksEnc, ksMac, 1L)
        val enc1 = w1.getEncodedSendSequenceCounter()!!
        val enc2 = w2.getEncodedSendSequenceCounter()!!
        assertFalse(enc1.contentEquals(enc2))
    }

    @Test
    fun equalsSameKeys() {
        val a = AESSecureMessagingWrapper(ksEnc, ksMac, 0L)
        val b = AESSecureMessagingWrapper(ksEnc, ksMac, 0L)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun notEqualsDifferentSSC() {
        val a = AESSecureMessagingWrapper(ksEnc, ksMac, 0L)
        val b = AESSecureMessagingWrapper(ksEnc, ksMac, 1L)
        assertNotEquals(a, b)
    }

    @Test
    fun notEqualsNull() {
        val a = AESSecureMessagingWrapper(ksEnc, ksMac, 0L)
        assertFalse(a.equals(null))
    }

    @Test
    fun copyConstructorCreatesEqual() {
        val original = AESSecureMessagingWrapper(ksEnc, ksMac, 42L)
        val copy = AESSecureMessagingWrapper(original)
        assertEquals(original, copy)
        assertNotSame(original, copy)
    }

    @Test
    fun toStringContainsType() {
        val wrapper = AESSecureMessagingWrapper(ksEnc, ksMac, 0L)
        assertTrue(wrapper.toString().contains("AES"))
    }
}

// ============================================================================
// SecureMessagingWrapper cross-type and getInstance factory
// ============================================================================

class SecureMessagingWrapperCrossTypeTest {

    @Test
    fun desedeNotEqualsAES() {
        val desede = DESedeSecureMessagingWrapper(desedeKey(0x10), desedeKey(0x20), 0L)
        val aes = AESSecureMessagingWrapper(aesKey128(0x10), aesKey128(0x20), 0L)
        assertNotEquals<Any>(desede, aes)
    }

    @Test
    fun getInstanceCopiesDESede() {
        val original = DESedeSecureMessagingWrapper(desedeKey(0x10), desedeKey(0x20), 42L)
        val copy = SecureMessagingWrapper.getInstance(original)
        assertNotNull(copy)
        assertNotSame(original, copy)
        assertEquals(original, copy)
        assertTrue(copy is DESedeSecureMessagingWrapper)
    }

    @Test
    fun getInstanceCopiesAES() {
        val original = AESSecureMessagingWrapper(aesKey128(0x10), aesKey128(0x20), 99L)
        val copy = SecureMessagingWrapper.getInstance(original)
        assertNotNull(copy)
        assertNotSame(original, copy)
        assertEquals(original, copy)
        assertTrue(copy is AESSecureMessagingWrapper)
    }

    @Test
    fun getInstanceNullReturnsNull() {
        val result = SecureMessagingWrapper.getInstance(null)
        assertNull(result)
    }

    @Test
    fun copyIsIndependent() {
        val original = DESedeSecureMessagingWrapper(desedeKey(0x10), desedeKey(0x20), 0L)
        val copy = SecureMessagingWrapper.getInstance(original) as DESedeSecureMessagingWrapper
        // Mutate original's SSC
        original.sendSequenceCounter = 100L
        // Copy should still be at 0
        assertEquals(0L, copy.sendSequenceCounter)
    }
}

// ============================================================================
// BACProtocol companion
// ============================================================================

class BACProtocolCompanionTest {

    @Test
    fun computeSSCBasicCase() {
        val rndICC = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x01, 0x02, 0x03, 0x04)
        val rndIFD = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x05, 0x06, 0x07, 0x08)
        val ssc = BACProtocol.computeSendSequenceCounter(rndICC, rndIFD)
        // SSC = rndICC[4..7] || rndIFD[4..7] as big-endian long
        // = 0x01020304 05060708
        assertEquals(0x0102030405060708L, ssc)
    }

    @Test
    fun computeSSCAllZeros() {
        val rndICC = ByteArray(8)
        val rndIFD = ByteArray(8)
        val ssc = BACProtocol.computeSendSequenceCounter(rndICC, rndIFD)
        assertEquals(0L, ssc)
    }

    @Test
    fun computeSSCAllFF() {
        val rndICC = ByteArray(8) { 0xFF.toByte() }
        val rndIFD = ByteArray(8) { 0xFF.toByte() }
        val ssc = BACProtocol.computeSendSequenceCounter(rndICC, rndIFD)
        assertEquals(-1L, ssc) // 0xFFFFFFFFFFFFFFFF = -1 in signed long
    }

    @Test
    fun computeSSCUsesOnlyLast4Bytes() {
        val rndICC1 = byteArrayOf(0xAA.toByte(), 0xBB.toByte(), 0xCC.toByte(), 0xDD.toByte(), 0x01, 0x02, 0x03, 0x04)
        val rndICC2 = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x01, 0x02, 0x03, 0x04)
        val rndIFD = ByteArray(8)
        assertEquals(
            BACProtocol.computeSendSequenceCounter(rndICC1, rndIFD),
            BACProtocol.computeSendSequenceCounter(rndICC2, rndIFD)
        )
    }

    @Test
    fun computeSSCWrongLengthICCThrows() {
        assertFailsWith<IllegalStateException> {
            BACProtocol.computeSendSequenceCounter(ByteArray(7), ByteArray(8))
        }
    }

    @Test
    fun computeSSCWrongLengthIFDThrows() {
        assertFailsWith<IllegalStateException> {
            BACProtocol.computeSendSequenceCounter(ByteArray(8), ByteArray(9))
        }
    }

    @Test
    fun computeKeySeedForBACProducesConsistentResult() {
        val bacKey = BACKey(DocumentNumber("AB1234567"), ICAODate("900101"), ICAODate("300101"))
        val seed1 = BACProtocol.computeKeySeedForBAC(bacKey)
        val seed2 = BACProtocol.computeKeySeedForBAC(bacKey)
        assertTrue(seed1.contentEquals(seed2))
    }

    @Test
    fun computeKeySeedForBACLength16() {
        val bacKey = BACKey(DocumentNumber("L898902C3"), ICAODate("690806"), ICAODate("940623"))
        val seed = BACProtocol.computeKeySeedForBAC(bacKey)
        assertEquals(16, seed.size)
    }

    @Test
    fun computeKeySeedDifferentKeysProduceDifferentSeeds() {
        val key1 = BACKey(DocumentNumber("AB1234567"), ICAODate("900101"), ICAODate("300101"))
        val key2 = BACKey(DocumentNumber("CD9876543"), ICAODate("900101"), ICAODate("300101"))
        val seed1 = BACProtocol.computeKeySeedForBAC(key1)
        val seed2 = BACProtocol.computeKeySeedForBAC(key2)
        assertFalse(seed1.contentEquals(seed2))
    }

    @Test
    fun computeKeySeedShortDocNumberPadsWithAngleBrackets() {
        // Document number < 9 chars should be padded with '<'
        val shortKey = BACKey(DocumentNumber("AB123"), ICAODate("900101"), ICAODate("300101"))
        val seed = BACProtocol.computeKeySeedForBAC(shortKey)
        assertEquals(16, seed.size)
    }
}

// ============================================================================
// PACEGMWithECDHAgreement — full roundtrip
// ============================================================================

class PACEGMWithECDHAgreementFullTest {

    private fun generateECKeyPair(): java.security.KeyPair {
        val gen = KeyPairGenerator.getInstance("EC")
        gen.initialize(ECGenParameterSpec("secp256r1"))
        return gen.generateKeyPair()
    }

    @Test
    fun doPhaseProducesValidPoint() {
        val alice = generateECKeyPair()
        val bob = generateECKeyPair()

        val agreement = PACEGMWithECDHAgreement()
        agreement.init(alice.private)
        val point = agreement.doPhase(bob.public)

        assertNotNull(point)
        assertNotNull(point.affineX)
        assertNotNull(point.affineY)
        // Point should not be at infinity (0,0 is not valid)
        assertFalse(point.affineX == BigInteger.ZERO && point.affineY == BigInteger.ZERO)
    }

    @Test
    fun doPhaseIsCommutative() {
        // ECDH: alice_priv * bob_pub == bob_priv * alice_pub
        val alice = generateECKeyPair()
        val bob = generateECKeyPair()

        val agreement1 = PACEGMWithECDHAgreement()
        agreement1.init(alice.private)
        val point1 = agreement1.doPhase(bob.public)

        val agreement2 = PACEGMWithECDHAgreement()
        agreement2.init(bob.private)
        val point2 = agreement2.doPhase(alice.public)

        assertEquals(point1.affineX, point2.affineX)
        assertEquals(point1.affineY, point2.affineY)
    }

    @Test
    fun doPhaseWithDifferentKeysProducesDifferentPoints() {
        val alice = generateECKeyPair()
        val bob = generateECKeyPair()
        val charlie = generateECKeyPair()

        val ab = PACEGMWithECDHAgreement()
        ab.init(alice.private)
        val pointAB = ab.doPhase(bob.public)

        val ac = PACEGMWithECDHAgreement()
        ac.init(alice.private)
        val pointAC = ac.doPhase(charlie.public)

        // Overwhelmingly unlikely to be equal with different keys
        assertFalse(pointAB.affineX == pointAC.affineX && pointAB.affineY == pointAC.affineY)
    }

    @Test
    fun canReuseAgreementWithNewInit() {
        val alice1 = generateECKeyPair()
        val alice2 = generateECKeyPair()
        val bob = generateECKeyPair()

        val agreement = PACEGMWithECDHAgreement()

        agreement.init(alice1.private)
        val point1 = agreement.doPhase(bob.public)

        agreement.init(alice2.private)
        val point2 = agreement.doPhase(bob.public)

        // Different private keys should yield different results
        assertFalse(point1.affineX == point2.affineX && point1.affineY == point2.affineY)
    }
}

// ============================================================================
// PACEProtocol companion — encoding/decoding
// ============================================================================

class PACEProtocolEncodingTest {

    private fun generateECKeyPair(): java.security.KeyPair {
        val gen = KeyPairGenerator.getInstance("EC")
        gen.initialize(ECGenParameterSpec("secp256r1"))
        return gen.generateKeyPair()
    }

    @Test
    fun encodePublicKeyForSmartCardECNotEmpty() {
        val kp = generateECKeyPair()
        val encoded = PACEProtocol.encodePublicKeyForSmartCard(kp.public)
        assertNotNull(encoded)
        assertTrue(encoded.isNotEmpty())
    }

    @Test
    fun encodeECPointForSmartCardNotEmpty() {
        val kp = generateECKeyPair()
        val pub = kp.public as ECPublicKey
        val encoded = PACEProtocol.encodeECPointForSmartCard(pub.w, pub.params.curve.field.fieldSize)
        assertNotNull(encoded)
        assertTrue(encoded.isNotEmpty())
    }

    @Test
    fun encodeDecodeECPublicKeyRoundTrip() {
        val kp = generateECKeyPair()
        val pub = kp.public as ECPublicKey
        val encoded = PACEProtocol.encodePublicKeyForSmartCard(pub)
        val decoded = PACEProtocol.decodePublicKeyFromSmartCard(encoded, pub.params)

        val decodedEC = decoded as ECPublicKey
        assertEquals(pub.w.affineX, decodedEC.w.affineX)
        assertEquals(pub.w.affineY, decodedEC.w.affineY)
    }

    @Test
    fun encodeDecodeMultipleKeysConsistent() {
        repeat(3) {
            val kp = generateECKeyPair()
            val pub = kp.public as ECPublicKey
            val encoded = PACEProtocol.encodePublicKeyForSmartCard(pub)
            val decoded = PACEProtocol.decodePublicKeyFromSmartCard(encoded, pub.params) as ECPublicKey
            assertEquals(pub.w.affineX, decoded.w.affineX)
            assertEquals(pub.w.affineY, decoded.w.affineY)
        }
    }

    @Test
    fun encodePublicKeyRejectsUnsupportedKeyType() {
        val rsaKeyPair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
        assertFailsWith<InvalidKeyException> {
            PACEProtocol.encodePublicKeyForSmartCard(rsaKeyPair.public)
        }
    }

    @Test
    fun encodePublicKeyDataObjectECNotEmpty() {
        val kp = generateECKeyPair()
        // Use a known PACE OID for ECDH with AES-CBC-128
        val oid = "0.4.0.127.0.7.2.2.4.2.2"
        val encoded = PACEProtocol.encodePublicKeyDataObject(oid, kp.public, true)
        assertNotNull(encoded)
        assertTrue(encoded.isNotEmpty())
        // Should start with 0x7F49 tag
        assertEquals(0x7F.toByte(), encoded[0])
        assertEquals(0x49.toByte(), encoded[1])
    }

    @Test
    fun encodePublicKeyDataObjectContextNotKnownIncludesParams() {
        val kp = generateECKeyPair()
        val oid = "0.4.0.127.0.7.2.2.4.2.2"
        val contextKnown = PACEProtocol.encodePublicKeyDataObject(oid, kp.public, true)
        val contextNotKnown = PACEProtocol.encodePublicKeyDataObject(oid, kp.public, false)
        // Context not known should be larger (includes domain params)
        assertTrue(contextNotKnown.size > contextKnown.size)
    }
}

// ============================================================================
// PACEProtocol companion — nonce mapping
// ============================================================================

class PACEProtocolMappingTest {

    @Test
    fun mapNonceGMWithDHProducesNewGenerator() {
        val p = BigInteger("FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD1" +
                "29024E088A67CC74020BBEA63B139B22514A08798E3404DD" +
                "EF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245" +
                "E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7ED" +
                "EE386BFB5A899FA5AE9F24117C4B1FE649286651ECE65381" +
                "FFFFFFFFFFFFFFFF", 16)
        val g = BigInteger.valueOf(2)
        val staticParams = DHParameterSpec(p, g, 1024)

        val nonce = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
            0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10)
        val sharedSecret = BigInteger.valueOf(12345)

        val result = PACEProtocol.mapNonceGMWithDH(nonce, sharedSecret, staticParams)

        assertNotNull(result)
        assertEquals(p, result.p)
        assertEquals(1024, result.l)
        // Generator should have changed
        assertNotEquals(g, result.g)
        // New generator should be in range [1, p-1]
        assertTrue(result.g > BigInteger.ZERO)
        assertTrue(result.g < p)
    }

    @Test
    fun mapNonceGMWithDHDifferentNoncesProduceDifferentGenerators() {
        val p = BigInteger("FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD1" +
                "29024E088A67CC74020BBEA63B139B22514A08798E3404DD" +
                "EF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245" +
                "E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7ED" +
                "EE386BFB5A899FA5AE9F24117C4B1FE649286651ECE65381" +
                "FFFFFFFFFFFFFFFF", 16)
        val staticParams = DHParameterSpec(p, BigInteger.valueOf(2), 1024)
        val sharedSecret = BigInteger.valueOf(42)

        val nonce1 = ByteArray(16) { 0x01 }
        val nonce2 = ByteArray(16) { 0x02 }

        val result1 = PACEProtocol.mapNonceGMWithDH(nonce1, sharedSecret, staticParams)
        val result2 = PACEProtocol.mapNonceGMWithDH(nonce2, sharedSecret, staticParams)

        assertNotEquals(result1.g, result2.g)
    }

    @Test
    fun mapNonceGMWithECDHProducesNewParams() {
        val kp = KeyPairGenerator.getInstance("EC").apply {
            initialize(ECGenParameterSpec("secp256r1"))
        }.generateKeyPair()
        val ecPub = kp.public as ECPublicKey
        val staticParams = ecPub.params

        val nonce = ByteArray(16) { (it + 1).toByte() }
        val sharedPoint = ecPub.w // Use any valid point on the curve

        val result = PACEProtocol.mapNonceGMWithECDH(nonce, sharedPoint, staticParams)

        assertNotNull(result)
        assertTrue(result is ECParameterSpec)
        val ecResult = result as ECParameterSpec
        // Same curve
        assertEquals(staticParams.curve, ecResult.curve)
        assertEquals(staticParams.order, ecResult.order)
        assertEquals(staticParams.cofactor, ecResult.cofactor)
        // But different generator
        assertFalse(
            staticParams.generator.affineX == ecResult.generator.affineX &&
                    staticParams.generator.affineY == ecResult.generator.affineY
        )
    }
}

// ============================================================================
// PACEProtocol companion — updateParameterSpec
// ============================================================================

class PACEProtocolUpdateParameterSpecTest {

    @Test
    fun updateParameterSpecECProducesValidKey() {
        val gen = KeyPairGenerator.getInstance("EC")
        gen.initialize(ECGenParameterSpec("secp256r1"))
        val kp1 = gen.generateKeyPair()
        val kp2 = gen.generateKeyPair()

        // Take public key from kp1, private key from kp2 → new public key with kp2's params
        val updated = PACEProtocol.updateParameterSpec(kp1.public, kp2.private)
        assertNotNull(updated)
        assertTrue(updated is ECPublicKey)
        val ecUpdated = updated as ECPublicKey
        // Should have the same W point as kp1
        assertEquals((kp1.public as ECPublicKey).w.affineX, ecUpdated.w.affineX)
        assertEquals((kp1.public as ECPublicKey).w.affineY, ecUpdated.w.affineY)
    }

    @Test
    fun updateParameterSpecMismatchedAlgorithmsThrows() {
        val ecKP = KeyPairGenerator.getInstance("EC").apply {
            initialize(ECGenParameterSpec("secp256r1"))
        }.generateKeyPair()
        val rsaKP = KeyPairGenerator.getInstance("RSA").apply {
            initialize(2048)
        }.generateKeyPair()

        assertFailsWith<java.security.NoSuchAlgorithmException> {
            PACEProtocol.updateParameterSpec(ecKP.public, rsaKP.private)
        }
    }

    @Test
    fun updateParameterSpecRSAPublicThrows() {
        val rsaKP = KeyPairGenerator.getInstance("RSA").apply {
            initialize(2048)
        }.generateKeyPair()

        assertFailsWith<java.security.NoSuchAlgorithmException> {
            PACEProtocol.updateParameterSpec(rsaKP.public, rsaKP.private)
        }
    }
}

// ============================================================================
// PACEProtocol companion — computeKeySeedForPACE(CAN)
// ============================================================================

class PACEProtocolKeySeedTest {

    @Test
    fun computeKeySeedForPACEFromCAN() {
        val seed = PACEProtocol.computeKeySeedForPACE("123456")
        assertNotNull(seed)
        assertTrue(seed.isNotEmpty())
    }

    @Test
    fun computeKeySeedForPACEDeterministic() {
        val seed1 = PACEProtocol.computeKeySeedForPACE("123456")
        val seed2 = PACEProtocol.computeKeySeedForPACE("123456")
        assertTrue(seed1.contentEquals(seed2))
    }

    @Test
    fun computeKeySeedForPACEDifferentCANDifferentSeed() {
        val seed1 = PACEProtocol.computeKeySeedForPACE("123456")
        val seed2 = PACEProtocol.computeKeySeedForPACE("654321")
        assertFalse(seed1.contentEquals(seed2))
    }
}

// ============================================================================
// Util.pad / Util.unpad roundtrip
// ============================================================================

class UtilPadUnpadTest {

    @Test
    fun padUnpadRoundTrip8() {
        val original = byteArrayOf(0x01, 0x02, 0x03)
        val padded = Util.pad(original, 8)
        assertTrue(padded.size % 8 == 0)
        assertTrue(padded.size >= original.size + 1) // at least 0x80 added
        val unpadded = Util.unpad(padded)
        assertTrue(original.contentEquals(unpadded))
    }

    @Test
    fun padUnpadRoundTrip16() {
        val original = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05)
        val padded = Util.pad(original, 16)
        assertTrue(padded.size % 16 == 0)
        val unpadded = Util.unpad(padded)
        assertTrue(original.contentEquals(unpadded))
    }

    @Test
    fun padAlwaysAddsAtLeastOneByte() {
        // 8 bytes already block-aligned → should still pad to next block
        val original = ByteArray(8) { it.toByte() }
        val padded = Util.pad(original, 8)
        assertEquals(16, padded.size)
    }

    @Test
    fun padStartsWithOriginalData() {
        val original = byteArrayOf(0xAA.toByte(), 0xBB.toByte(), 0xCC.toByte())
        val padded = Util.pad(original, 8)
        assertEquals(0xAA.toByte(), padded[0])
        assertEquals(0xBB.toByte(), padded[1])
        assertEquals(0xCC.toByte(), padded[2])
        assertEquals(0x80.toByte(), padded[3]) // ISO 9797-1 padding marker
    }

    @Test
    fun padWithOffsetAndLength() {
        val data = byteArrayOf(0x00, 0x01, 0x02, 0x03, 0x00)
        val padded = Util.pad(data, 1, 3, 8) // offset=1, length=3 → {0x01, 0x02, 0x03}
        assertTrue(padded.size % 8 == 0)
        assertEquals(0x01.toByte(), padded[0])
        assertEquals(0x02.toByte(), padded[1])
        assertEquals(0x03.toByte(), padded[2])
    }
}

// ============================================================================
// Util.deriveKey
// ============================================================================

class UtilDeriveKeyTest {

    @Test
    fun deriveKeyDESedeENC() {
        val seed = ByteArray(16) { it.toByte() }
        val key = Util.deriveKey(seed, Util.ENC_MODE)
        assertNotNull(key)
        assertEquals("DESede", key.algorithm)
        assertEquals(24, key.encoded.size) // 3DES = 24 bytes
    }

    @Test
    fun deriveKeyDESedeMAC() {
        val seed = ByteArray(16) { it.toByte() }
        val key = Util.deriveKey(seed, Util.MAC_MODE)
        assertNotNull(key)
        assertEquals("DESede", key.algorithm)
        assertEquals(24, key.encoded.size)
    }

    @Test
    fun deriveKeyENCAndMACAreDifferent() {
        val seed = ByteArray(16) { it.toByte() }
        val kEnc = Util.deriveKey(seed, Util.ENC_MODE)
        val kMac = Util.deriveKey(seed, Util.MAC_MODE)
        assertFalse(kEnc.encoded.contentEquals(kMac.encoded))
    }

    @Test
    fun deriveKeyDeterministic() {
        val seed = ByteArray(16) { (it + 42).toByte() }
        val key1 = Util.deriveKey(seed, Util.ENC_MODE)
        val key2 = Util.deriveKey(seed, Util.ENC_MODE)
        assertTrue(key1.encoded.contentEquals(key2.encoded))
    }

    @Test
    fun deriveKeyDifferentSeedsDifferentKeys() {
        val seed1 = ByteArray(16) { 0x00 }
        val seed2 = ByteArray(16) { 0xFF.toByte() }
        val key1 = Util.deriveKey(seed1, Util.ENC_MODE)
        val key2 = Util.deriveKey(seed2, Util.ENC_MODE)
        assertFalse(key1.encoded.contentEquals(key2.encoded))
    }

    @Test
    fun deriveKeyAES128() {
        val seed = ByteArray(16) { it.toByte() }
        val key = Util.deriveKey(seed, "AES", 128, Util.ENC_MODE)
        assertNotNull(key)
        assertEquals("AES", key.algorithm)
        assertEquals(16, key.encoded.size) // 128 / 8
    }

    @Test
    fun deriveKeyAES256() {
        val seed = ByteArray(32) { it.toByte() }
        val key = Util.deriveKey(seed, "AES", 256, Util.ENC_MODE)
        assertNotNull(key)
        assertEquals("AES", key.algorithm)
        assertEquals(32, key.encoded.size) // 256 / 8
    }
}