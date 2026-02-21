import kmrtd.AccessKeySpec
import kmrtd.BACKey
import kmrtd.BACKeySpec
import kmrtd.support.DocumentNumber
import kmrtd.support.ICAODate
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for [AccessKeySpec] interface compliance.
 *
 * Verifies that all implementations of AccessKeySpec conform to the
 * expected contract: non-null algorithm, non-null key bytes.
 */
class AccessKeySpecTest {

    @Test
    fun `BACKey implements AccessKeySpec`() {
        val key = BACKey(
            DocumentNumber("AB1234567"),
            ICAODate("900115"),
            ICAODate("300101")
        )

        // Must be an AccessKeySpec
        assertIs<AccessKeySpec>(key)
    }

    @Test
    fun `BACKey algorithm is non-null and non-empty`() {
        val key = BACKey(
            DocumentNumber("AB1234567"),
            ICAODate("900115"),
            ICAODate("300101")
        )

        assertTrue(key.algorithm.isNotEmpty())
        assertEquals("BAC", key.algorithm)
    }

    @Test
    fun `BACKey key bytes are non-null and non-empty`() {
        val key = BACKey(
            DocumentNumber("AB1234567"),
            ICAODate("900115"),
            ICAODate("300101")
        )

        // key should produce a valid byte array for key seed derivation
        assertTrue(key.key.isNotEmpty())
    }

    @Test
    fun `BACKey implements BACKeySpec`() {
        val key = BACKey(
            DocumentNumber("AB1234567"),
            ICAODate("900115"),
            ICAODate("300101")
        )

        assertIs<BACKeySpec>(key)
        assertEquals(DocumentNumber("AB1234567"), key.documentNumber)
        assertEquals(ICAODate("900115"), key.dateOfBirth)
        assertEquals(ICAODate("300101"), key.dateOfExpiry)
    }
}
