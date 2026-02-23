import kmrtd.protocol.AAResult
import kmrtd.protocol.BACResult
import kmrtd.protocol.EACCAResult
import kmrtd.protocol.EACTAResult
import kmrtd.protocol.PACECAMResult
import kmrtd.protocol.PACEGMWithDHMappingResult
import kmrtd.protocol.PACEGMWithECDHAgreement
import kmrtd.protocol.PACEGMWithECDHMappingResult
import kmrtd.protocol.PACEIMMappingResult
import kmrtd.protocol.PACEResult
import kmrtd.lds.PACEInfo.MappingType
import java.math.BigInteger
import java.security.*
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.ECPoint
import java.security.spec.ECParameterSpec
import javax.crypto.spec.DHParameterSpec
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
// AAResult
// ============================================================================

class AAResultTest {

    private val challenge = byteArrayOf(0x01, 0x02, 0x03, 0x04)
    private val response = byteArrayOf(0x0A, 0x0B, 0x0C, 0x0D)

    @Test
    fun constructionStoresFields() {
        val result = AAResult(null, "SHA-256", "SHA256withRSA", challenge, response)
        assertNull(result.publicKey)
        assertEquals("SHA-256", result.digestAlgorithm)
        assertEquals("SHA256withRSA", result.signatureAlgorithm)
        assertEquals(4, result.challenge.size)
        assertEquals(4, result.response.size)
    }

    @Test
    fun constructionWithNullAlgorithms() {
        val result = AAResult(null, null, null, challenge, response)
        assertNull(result.digestAlgorithm)
        assertNull(result.signatureAlgorithm)
    }

    @Test
    fun equalsSameFields() {
        val a = AAResult(null, "SHA-256", "SHA256withRSA", challenge, response)
        val b = AAResult(null, "SHA-256", "SHA256withRSA", challenge, response)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun equalsSameInstance() {
        val a = AAResult(null, "SHA-256", "SHA256withRSA", challenge, response)
        assertEquals(a, a)
    }

    @Test
    fun notEqualsDifferentChallenge() {
        val a = AAResult(null, "SHA-256", "SHA256withRSA", challenge, response)
        val b = AAResult(null, "SHA-256", "SHA256withRSA", byteArrayOf(0xFF.toByte()), response)
        assertNotEquals(a, b)
    }

    @Test
    fun notEqualsDifferentDigest() {
        val a = AAResult(null, "SHA-256", "SHA256withRSA", challenge, response)
        val b = AAResult(null, "SHA-384", "SHA256withRSA", challenge, response)
        assertNotEquals(a, b)
    }

    @Test
    fun notEqualsNull() {
        val a = AAResult(null, "SHA-256", "SHA256withRSA", challenge, response)
        assertFalse(a.equals(null))
    }

    @Test
    fun notEqualsDifferentType() {
        val a = AAResult(null, "SHA-256", "SHA256withRSA", challenge, response)
        assertFalse(a.equals("not an AAResult"))
    }

    @Test
    fun toStringContainsAlgorithm() {
        val result = AAResult(null, "SHA-256", "SHA256withRSA", challenge, response)
        val str = result.toString()
        assertTrue(str.contains("AAResult"))
        assertTrue(str.contains("SHA-256"))
        assertTrue(str.contains("SHA256withRSA"))
    }
}

// ============================================================================
// BACResult
// ============================================================================

class BACResultTest {

    @Test
    fun constructionWithNulls() {
        val result = BACResult(null, null)
        assertNull(result.bACKey)
        assertNull(result.wrapper)
    }

    @Test
    fun secondaryConstructorSetsKeyNull() {
        val result = BACResult(null)
        assertNull(result.bACKey)
        assertNull(result.wrapper)
    }

    @Test
    fun equalsSameNullFields() {
        val a = BACResult(null, null)
        val b = BACResult(null, null)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun equalsSameInstance() {
        val a = BACResult(null)
        assertEquals(a, a)
    }

    @Test
    fun notEqualsNull() {
        val a = BACResult(null)
        assertFalse(a.equals(null))
    }

    @Test
    fun notEqualsDifferentType() {
        val a = BACResult(null)
        assertFalse(a.equals("not a BACResult"))
    }

    @Test
    fun toStringContainsMarker() {
        val result = BACResult(null, null)
        val str = result.toString()
        assertTrue(str.contains("BACResult"))
    }
}

// ============================================================================
// EACCAResult
// ============================================================================

class EACCAResultTest {

    private val keyHash = byteArrayOf(0x01, 0x02, 0x03)

    @Test
    fun constructionWithNulls() {
        val result = EACCAResult(null, null, keyHash, null, null, null)
        assertNull(result.keyId)
        assertNull(result.publicKey)
        assertNotNull(result.keyHash)
        assertNull(result.pCDPublicKey)
        assertNull(result.pCDPrivateKey)
        assertNull(result.wrapper)
    }

    @Test
    fun constructionWithKeyId() {
        val keyId = BigInteger.valueOf(42)
        val result = EACCAResult(keyId, null, keyHash, null, null, null)
        assertEquals(BigInteger.valueOf(42), result.keyId)
    }

    @Test
    fun equalsSameFields() {
        val a = EACCAResult(BigInteger.ONE, null, keyHash, null, null, null)
        val b = EACCAResult(BigInteger.ONE, null, keyHash, null, null, null)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun notEqualsDifferentKeyId() {
        val a = EACCAResult(BigInteger.ONE, null, keyHash, null, null, null)
        val b = EACCAResult(BigInteger.TEN, null, keyHash, null, null, null)
        assertNotEquals(a, b)
    }

    @Test
    fun notEqualsDifferentKeyHash() {
        val a = EACCAResult(null, null, byteArrayOf(0x01), null, null, null)
        val b = EACCAResult(null, null, byteArrayOf(0x02), null, null, null)
        assertNotEquals(a, b)
    }

    @Test
    fun notEqualsNull() {
        val a = EACCAResult(null, null, keyHash, null, null, null)
        assertFalse(a.equals(null))
    }

    @Test
    fun toStringContainsMarker() {
        val result = EACCAResult(BigInteger.ONE, null, keyHash, null, null, null)
        val str = result.toString()
        assertTrue(str.contains("EACCAResult"))
    }
}

// ============================================================================
// EACTAResult
// ============================================================================

class EACTAResultTest {

    private val cardChallenge = byteArrayOf(0xAA.toByte(), 0xBB.toByte())

    @Test
    fun constructionCopiesCertificates() {
        val certs = mutableListOf<kmrtd.cert.CardVerifiableCertificate>()
        val result = EACTAResult(null, null, certs, null, "AB1234567", cardChallenge)
        assertEquals(0, result.cVCertificates.size)
        assertEquals("AB1234567", result.documentNumber)
    }

    @Test
    fun equalsSameFields() {
        val a = EACTAResult(null, null, mutableListOf(), null, "AB1234567", cardChallenge)
        val b = EACTAResult(null, null, mutableListOf(), null, "AB1234567", cardChallenge)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun notEqualsDifferentDocNumber() {
        val a = EACTAResult(null, null, mutableListOf(), null, "AB1234567", cardChallenge)
        val b = EACTAResult(null, null, mutableListOf(), null, "CD9876543", cardChallenge)
        assertNotEquals(a, b)
    }

    @Test
    fun notEqualsDifferentChallenge() {
        val a = EACTAResult(null, null, mutableListOf(), null, "AB1234567", byteArrayOf(0x01))
        val b = EACTAResult(null, null, mutableListOf(), null, "AB1234567", byteArrayOf(0x02))
        assertNotEquals(a, b)
    }

    @Test
    fun notEqualsNull() {
        val a = EACTAResult(null, null, mutableListOf(), null, "AB1234567", cardChallenge)
        assertFalse(a.equals(null))
    }

    @Test
    fun toStringContainsMarker() {
        val result = EACTAResult(null, null, mutableListOf(), null, "AB1234567", cardChallenge)
        val str = result.toString()
        assertTrue(str.contains("TAResult"))
        assertTrue(str.contains("AB1234567"))
    }
}

// ============================================================================
// PACEResult
// ============================================================================

class PACEResultTest {

    @Test
    fun constructionStoresFields() {
        val result = PACEResult(
            null, MappingType.GM, "ECDH", "AES", "SHA-256",
            128, null, null, null, null
        )
        assertNull(result.pACEKey)
        assertEquals(MappingType.GM, result.mappingType)
        assertEquals("ECDH", result.agreementAlg)
        assertEquals("AES", result.cipherAlg)
        assertEquals("SHA-256", result.digestAlg)
        assertEquals(128, result.keyLength)
        assertNull(result.mappingResult)
        assertNull(result.pCDKeyPair)
        assertNull(result.pICCPublicKey)
        assertNull(result.wrapper)
    }

    @Test
    fun equalsSameFields() {
        val a = PACEResult(null, MappingType.GM, "ECDH", "AES", "SHA-256", 128, null, null, null, null)
        val b = PACEResult(null, MappingType.GM, "ECDH", "AES", "SHA-256", 128, null, null, null, null)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun notEqualsDifferentMappingType() {
        val a = PACEResult(null, MappingType.GM, "ECDH", "AES", "SHA-256", 128, null, null, null, null)
        val b = PACEResult(null, MappingType.IM, "ECDH", "AES", "SHA-256", 128, null, null, null, null)
        assertNotEquals(a, b)
    }

    @Test
    fun notEqualsDifferentKeyLength() {
        val a = PACEResult(null, MappingType.GM, "ECDH", "AES", "SHA-256", 128, null, null, null, null)
        val b = PACEResult(null, MappingType.GM, "ECDH", "AES", "SHA-256", 256, null, null, null, null)
        assertNotEquals(a, b)
    }

    @Test
    fun notEqualsDifferentCipher() {
        val a = PACEResult(null, MappingType.GM, "ECDH", "AES", "SHA-256", 128, null, null, null, null)
        val b = PACEResult(null, MappingType.GM, "ECDH", "DESede", "SHA-256", 128, null, null, null, null)
        assertNotEquals(a, b)
    }

    @Test
    fun notEqualsNull() {
        val a = PACEResult(null, MappingType.GM, "ECDH", "AES", "SHA-256", 128, null, null, null, null)
        assertFalse(a.equals(null))
    }

    @Test
    fun notEqualsDifferentType() {
        val a = PACEResult(null, MappingType.GM, "ECDH", "AES", "SHA-256", 128, null, null, null, null)
        assertFalse(a.equals("not a PACEResult"))
    }

    @Test
    fun toStringContainsMarker() {
        val result = PACEResult(null, MappingType.GM, "ECDH", "AES", "SHA-256", 128, null, null, null, null)
        val str = result.toString()
        assertTrue(str.contains("PACEResult"))
        assertTrue(str.contains("ECDH"))
        assertTrue(str.contains("AES"))
    }
}

// ============================================================================
// PACECAMResult
// ============================================================================

class PACECAMResultTest {

    private val encData = byteArrayOf(0x10, 0x20, 0x30)
    private val chipData = byteArrayOf(0xA0.toByte(), 0xB0.toByte())

    @Test
    fun constructionMakesDefensiveCopy() {
        val original = byteArrayOf(0x01, 0x02, 0x03)
        val result = PACECAMResult(
            null, "ECDH", "AES", "SHA-256", 128,
            null, null, null, original, null, null
        )
        original[0] = 0xFF.toByte()
        val retrieved = result.getEncryptedChipAuthenticationData()
        assertNotNull(retrieved)
        assertEquals(0x01.toByte(), retrieved[0])
    }

    @Test
    fun getEncryptedChipAuthenticationDataReturnsDefensiveCopy() {
        val result = PACECAMResult(
            null, "ECDH", "AES", "SHA-256", 128,
            null, null, null, encData, chipData, null
        )
        val copy1 = result.getEncryptedChipAuthenticationData()
        val copy2 = result.getEncryptedChipAuthenticationData()
        assertNotNull(copy1)
        assertNotNull(copy2)
        assertNotSame(copy1, copy2)
        assertTrue(copy1.contentEquals(copy2))
    }

    @Test
    fun getChipAuthenticationDataReturnsDefensiveCopy() {
        val result = PACECAMResult(
            null, "ECDH", "AES", "SHA-256", 128,
            null, null, null, encData, chipData, null
        )
        val copy1 = result.getChipAuthenticationData()
        val copy2 = result.getChipAuthenticationData()
        assertNotNull(copy1)
        assertNotNull(copy2)
        assertNotSame(copy1, copy2)
        assertTrue(copy1.contentEquals(copy2))
    }

    @Test
    fun getEncryptedChipAuthDataNullReturnsNull() {
        val result = PACECAMResult(
            null, "ECDH", "AES", "SHA-256", 128,
            null, null, null, null, null, null
        )
        assertNull(result.getEncryptedChipAuthenticationData())
    }

    @Test
    fun getChipAuthDataNullReturnsNull() {
        val result = PACECAMResult(
            null, "ECDH", "AES", "SHA-256", 128,
            null, null, null, null, null, null
        )
        assertNull(result.getChipAuthenticationData())
    }

    @Test
    fun equalsSameData() {
        val a = PACECAMResult(null, "ECDH", "AES", "SHA-256", 128, null, null, null, encData, chipData, null)
        val b = PACECAMResult(null, "ECDH", "AES", "SHA-256", 128, null, null, null, encData, chipData, null)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun notEqualsDifferentEncData() {
        val a = PACECAMResult(null, "ECDH", "AES", "SHA-256", 128, null, null, null, encData, chipData, null)
        val b = PACECAMResult(null, "ECDH", "AES", "SHA-256", 128, null, null, null, byteArrayOf(0xFF.toByte()), chipData, null)
        assertNotEquals(a, b)
    }

    @Test
    fun notEqualsDifferentChipData() {
        val a = PACECAMResult(null, "ECDH", "AES", "SHA-256", 128, null, null, null, encData, chipData, null)
        val b = PACECAMResult(null, "ECDH", "AES", "SHA-256", 128, null, null, null, encData, byteArrayOf(0xFF.toByte()), null)
        assertNotEquals(a, b)
    }

    @Test
    fun toStringContainsMappingType() {
        val result = PACECAMResult(null, "ECDH", "AES", "SHA-256", 128, null, null, null, encData, chipData, null)
        val str = result.toString()
        assertTrue(str.contains("PACECAMResult"))
        assertTrue(str.contains("CAM"))
    }
}

// ============================================================================
// PACEIMMappingResult
// ============================================================================

class PACEIMMappingResultTest {

    private val piccNonce = byteArrayOf(0x01, 0x02, 0x03, 0x04)
    private val pcdNonce = byteArrayOf(0x0A, 0x0B, 0x0C, 0x0D)

    @Test
    fun constructionStoresNonces() {
        val result = PACEIMMappingResult(null, piccNonce, pcdNonce, null)
        assertNotNull(result.pICCNonce)
        assertTrue(piccNonce.contentEquals(result.pICCNonce!!))
    }

    @Test
    fun constructionDefensiveCopyPICCNonce() {
        val nonce = byteArrayOf(0x01, 0x02)
        val result = PACEIMMappingResult(null, nonce, pcdNonce, null)
        nonce[0] = 0xFF.toByte()
        assertEquals(0x01.toByte(), result.pICCNonce!![0])
    }

    @Test
    fun constructionNullNonces() {
        val result = PACEIMMappingResult(null, null, null, null)
        assertNull(result.pICCNonce)
        assertNull(result.staticParameters)
        assertNull(result.ephemeralParameters)
    }

    @Test
    fun equalsSameNonces() {
        val a = PACEIMMappingResult(null, piccNonce, pcdNonce, null)
        val b = PACEIMMappingResult(null, piccNonce, pcdNonce, null)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun notEqualsDifferentPCDNonce() {
        val a = PACEIMMappingResult(null, piccNonce, pcdNonce, null)
        val b = PACEIMMappingResult(null, piccNonce, byteArrayOf(0xFF.toByte()), null)
        assertNotEquals(a, b)
    }

    @Test
    fun notEqualsDifferentPICCNonce() {
        val a = PACEIMMappingResult(null, piccNonce, pcdNonce, null)
        val b = PACEIMMappingResult(null, byteArrayOf(0xFF.toByte()), pcdNonce, null)
        assertNotEquals(a, b)
    }

    @Test
    fun notEqualsNull() {
        val a = PACEIMMappingResult(null, piccNonce, pcdNonce, null)
        assertFalse(a.equals(null))
    }

    @Test
    fun notEqualsDifferentType() {
        val a = PACEIMMappingResult(null, piccNonce, pcdNonce, null)
        assertFalse(a.equals("not a mapping result"))
    }
}

// ============================================================================
// PACEGMWithDHMappingResult
// ============================================================================

class PACEGMWithDHMappingResultTest {

    private val piccNonce = byteArrayOf(0x01, 0x02, 0x03, 0x04)
    private val sharedSecret = byteArrayOf(0xDE.toByte(), 0xAD.toByte(), 0xBE.toByte(), 0xEF.toByte())

    @Test
    fun constructionStoresFields() {
        val result = PACEGMWithDHMappingResult(null, piccNonce, null, null, sharedSecret, null)
        assertNotNull(result.pICCNonce)
        assertNull(result.pICCMappingPublicKey)
        assertNull(result.pCDMappingKeyPair)
    }

    @Test
    fun equalsSameData() {
        val a = PACEGMWithDHMappingResult(null, piccNonce, null, null, sharedSecret, null)
        val b = PACEGMWithDHMappingResult(null, piccNonce, null, null, sharedSecret, null)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun notEqualsDifferentSharedSecret() {
        val a = PACEGMWithDHMappingResult(null, piccNonce, null, null, sharedSecret, null)
        val b = PACEGMWithDHMappingResult(null, piccNonce, null, null, byteArrayOf(0x00), null)
        assertNotEquals(a, b)
    }

    @Test
    fun notEqualsNull() {
        val a = PACEGMWithDHMappingResult(null, piccNonce, null, null, sharedSecret, null)
        assertFalse(a.equals(null))
    }
}

// ============================================================================
// PACEGMWithECDHMappingResult
// ============================================================================

class PACEGMWithECDHMappingResultTest {

    private val piccNonce = byteArrayOf(0x01, 0x02)
    private val point = ECPoint(BigInteger.valueOf(17), BigInteger.valueOf(42))

    @Test
    fun constructionAndRetrieval() {
        val result = PACEGMWithECDHMappingResult(null, piccNonce, null, null, point, null)
        val retrieved = result.sharedSecretPoint
        assertEquals(BigInteger.valueOf(17), retrieved.affineX)
        assertEquals(BigInteger.valueOf(42), retrieved.affineY)
    }

    @Test
    fun sharedSecretPointReturnsFreshInstance() {
        val result = PACEGMWithECDHMappingResult(null, piccNonce, null, null, point, null)
        val p1 = result.sharedSecretPoint
        val p2 = result.sharedSecretPoint
        assertEquals(p1.affineX, p2.affineX)
        assertEquals(p1.affineY, p2.affineY)
    }

    @Test
    fun equalsSamePoint() {
        val a = PACEGMWithECDHMappingResult(null, piccNonce, null, null, point, null)
        val b = PACEGMWithECDHMappingResult(null, piccNonce, null, null, ECPoint(BigInteger.valueOf(17), BigInteger.valueOf(42)), null)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun notEqualsDifferentPoint() {
        val a = PACEGMWithECDHMappingResult(null, piccNonce, null, null, point, null)
        val b = PACEGMWithECDHMappingResult(null, piccNonce, null, null, ECPoint(BigInteger.ONE, BigInteger.ZERO), null)
        assertNotEquals(a, b)
    }

    @Test
    fun notEqualsNull() {
        val a = PACEGMWithECDHMappingResult(null, piccNonce, null, null, point, null)
        assertFalse(a.equals(null))
    }
}

// ============================================================================
// PACEGMWithECDHAgreement
// ============================================================================

class PACEGMWithECDHAgreementTest {

    @Test
    fun initRejectsNonECPrivateKey() {
        val agreement = PACEGMWithECDHAgreement()
        val rsaKeyPair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
        assertFailsWith<InvalidKeyException> {
            agreement.init(rsaKeyPair.private)
        }
    }

    @Test
    fun initRejectsNull() {
        val agreement = PACEGMWithECDHAgreement()
        assertFailsWith<InvalidKeyException> {
            agreement.init(null)
        }
    }

    @Test
    fun doPhaseWithoutInitThrows() {
        val agreement = PACEGMWithECDHAgreement()
        val ecKeyPair = KeyPairGenerator.getInstance("EC").apply {
            initialize(ECGenParameterSpec("secp256r1"))
        }.generateKeyPair()
        assertFailsWith<IllegalStateException> {
            agreement.doPhase(ecKeyPair.public)
        }
    }

    @Test
    fun doPhaseRejectsNonECPublicKey() {
        val agreement = PACEGMWithECDHAgreement()
        val ecKeyPair = KeyPairGenerator.getInstance("EC").apply {
            initialize(ECGenParameterSpec("secp256r1"))
        }.generateKeyPair()
        val rsaKeyPair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()

        agreement.init(ecKeyPair.private)
        assertFailsWith<InvalidKeyException> {
            agreement.doPhase(rsaKeyPair.public)
        }
    }

    @Test
    fun doPhaseRejectsNull() {
        val agreement = PACEGMWithECDHAgreement()
        val ecKeyPair = KeyPairGenerator.getInstance("EC").apply {
            initialize(ECGenParameterSpec("secp256r1"))
        }.generateKeyPair()
        agreement.init(ecKeyPair.private)
        assertFailsWith<InvalidKeyException> {
            agreement.doPhase(null)
        }
    }

    @Test
    fun initAcceptsECPrivateKey() {
        val agreement = PACEGMWithECDHAgreement()
        val ecKeyPair = KeyPairGenerator.getInstance("EC").apply {
            initialize(ECGenParameterSpec("secp256r1"))
        }.generateKeyPair()
        agreement.init(ecKeyPair.private)
        // no exception = success
    }
}

// ============================================================================
// Cross-type equals: Result subtypes don't match across types
// ============================================================================

class ResultCrossTypeTest {

    @Test
    fun bacResultNotEqualsAaResult() {
        val bac = BACResult(null, null)
        val aa = AAResult(null, null, null, byteArrayOf(), byteArrayOf())
        assertNotEquals<Any>(bac, aa)
    }

    @Test
    fun paceResultNotEqualsPaceCAMResult() {
        val pace = PACEResult(null, MappingType.GM, "ECDH", "AES", "SHA-256", 128, null, null, null, null)
        val cam = PACECAMResult(null, "ECDH", "AES", "SHA-256", 128, null, null, null, null, null, null)
        // CAM extends PACEResult but has MappingType.CAM vs GM
        assertNotEquals<Any>(pace, cam)
    }
}

// ============================================================================
// Equals reflexivity, symmetry, consistency
// ============================================================================

class EqualsContractTest {

    @Test
    fun aaResultEqualsIsSymmetric() {
        val a = AAResult(null, "SHA-256", "SHA256withRSA", byteArrayOf(1), byteArrayOf(2))
        val b = AAResult(null, "SHA-256", "SHA256withRSA", byteArrayOf(1), byteArrayOf(2))
        assertEquals(a, b)
        assertEquals(b, a)
    }

    @Test
    fun aaResultEqualsIsConsistent() {
        val a = AAResult(null, "SHA-256", "SHA256withRSA", byteArrayOf(1), byteArrayOf(2))
        val b = AAResult(null, "SHA-256", "SHA256withRSA", byteArrayOf(1), byteArrayOf(2))
        assertEquals(a, b)
        assertEquals(a, b)
        assertEquals(a, b)
    }

    @Test
    fun bacResultEqualsIsSymmetric() {
        val a = BACResult(null, null)
        val b = BACResult(null, null)
        assertEquals(a, b)
        assertEquals(b, a)
    }

    @Test
    fun paceResultEqualsIsSymmetric() {
        val a = PACEResult(null, MappingType.GM, "ECDH", "AES", "SHA-256", 128, null, null, null, null)
        val b = PACEResult(null, MappingType.GM, "ECDH", "AES", "SHA-256", 128, null, null, null, null)
        assertEquals(a, b)
        assertEquals(b, a)
    }

    @Test
    fun eacCaResultEqualsIsSymmetric() {
        val hash = byteArrayOf(0x01)
        val a = EACCAResult(BigInteger.ONE, null, hash, null, null, null)
        val b = EACCAResult(BigInteger.ONE, null, hash, null, null, null)
        assertEquals(a, b)
        assertEquals(b, a)
    }

    @Test
    fun immMappingResultEqualsIsSymmetric() {
        val nonce = byteArrayOf(0x01)
        val pcd = byteArrayOf(0x02)
        val a = PACEIMMappingResult(null, nonce, pcd, null)
        val b = PACEIMMappingResult(null, nonce, pcd, null)
        assertEquals(a, b)
        assertEquals(b, a)
    }
}