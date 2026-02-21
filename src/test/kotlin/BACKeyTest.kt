import kmrtd.BACKey
import kmrtd.support.DocumentNumber
import kmrtd.support.ICAODate
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Tests for [kmrtd.BACKey] data class.
 *
 * Verifies construction, factory methods, equality, and behavioral
 * equivalence with the original JMRTD Java implementation.
 */
class BACKeyTest {

    // ==================== Construction ====================

    @Test
    fun `creates BACKey with valid parameters`() {
        val key = BACKey(
            DocumentNumber("L898902C3"),
            ICAODate("690806"),
            ICAODate("940623")
        )

        assertEquals("L898902C3", key.documentNumber.value)
        assertEquals("690806", key.dateOfBirth.date)
        assertEquals("940623", key.dateOfExpiry.date)
    }

    @Test
    fun `algorithm is always BAC`() {
        val key = BACKey(
            DocumentNumber("AB1234567"),
            ICAODate("900115"),
            ICAODate("300101")
        )

        assertEquals("BAC", key.algorithm)
    }

    // ==================== Factory: from(String, Date, Date) ====================

    @Test
    fun `factory from Dates creates valid BACKey`() {
        val calendar = Calendar.getInstance()

        calendar.set(1990, Calendar.JANUARY, 15)
        val dob = calendar.time

        calendar.set(2030, Calendar.JANUARY, 1)
        val expiry = calendar.time

        val key = BACKey.Companion.from("AB1234567", dob, expiry)

        assertEquals("AB1234567", key.documentNumber.value)
        assertEquals("900115", key.dateOfBirth.date)
        assertEquals("300101", key.dateOfExpiry.date)
    }

    // ==================== Factory: from(String, Long, Long) ====================

    @Test
    fun `factory from millis creates valid BACKey`() {
        val calendar = Calendar.getInstance()

        calendar.set(1990, Calendar.JANUARY, 15, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val dobMillis = calendar.timeInMillis

        calendar.set(2030, Calendar.JANUARY, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val expiryMillis = calendar.timeInMillis

        val key = BACKey.Companion.from("AB1234567", dobMillis, expiryMillis)

        assertEquals("900115", key.dateOfBirth.date)
        assertEquals("300101", key.dateOfExpiry.date)
    }

    @Test
    fun `factory from millis and from Dates produce equal keys`() {
        val calendar = Calendar.getInstance()

        calendar.set(1985, Calendar.MARCH, 22, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val dob = calendar.time
        val dobMillis = calendar.timeInMillis

        calendar.set(2025, Calendar.DECEMBER, 31, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val expiry = calendar.time
        val expiryMillis = calendar.timeInMillis

        val fromDates = BACKey.Companion.from("XY9876543", dob, expiry)
        val fromMillis = BACKey.Companion.from("XY9876543", dobMillis, expiryMillis)

        assertEquals(fromDates, fromMillis)
    }

    // ==================== Equality (data class) ====================

    @Test
    fun `equal BACKeys have same hashCode`() {
        val a = BACKey(
            DocumentNumber("AB1234567"),
            ICAODate("900115"),
            ICAODate("300101")
        )
        val b = BACKey(
            DocumentNumber("AB1234567"),
            ICAODate("900115"),
            ICAODate("300101")
        )

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `different document numbers produce unequal keys`() {
        val a = BACKey(
            DocumentNumber("AB1234567"),
            ICAODate("900115"),
            ICAODate("300101")
        )
        val b = BACKey(
            DocumentNumber("CD9876543"),
            ICAODate("900115"),
            ICAODate("300101")
        )

        assertNotEquals(a, b)
    }

    @Test
    fun `different dates of birth produce unequal keys`() {
        val a = BACKey(
            DocumentNumber("AB1234567"),
            ICAODate("900115"),
            ICAODate("300101")
        )
        val b = BACKey(
            DocumentNumber("AB1234567"),
            ICAODate("850322"),
            ICAODate("300101")
        )

        assertNotEquals(a, b)
    }

    @Test
    fun `different expiry dates produce unequal keys`() {
        val a = BACKey(
            DocumentNumber("AB1234567"),
            ICAODate("900115"),
            ICAODate("300101")
        )
        val b = BACKey(
            DocumentNumber("AB1234567"),
            ICAODate("900115"),
            ICAODate("350601")
        )

        assertNotEquals(a, b)
    }

    // ==================== data class copy ====================

    @Test
    fun `copy with changed document number`() {
        val original = BACKey(
            DocumentNumber("AB1234567"),
            ICAODate("900115"),
            ICAODate("300101")
        )
        val copied = original.copy(documentNumber = DocumentNumber("ZZ9999999"))

        assertEquals("ZZ9999999", copied.documentNumber.value)
        assertEquals(original.dateOfBirth, copied.dateOfBirth)
        assertEquals(original.dateOfExpiry, copied.dateOfExpiry)
    }

    // ==================== JMRTD behavioral equivalence ====================

    @Test
    fun `ICAO test document - L898902C`() {
        // Standard ICAO test document number from Doc 9303 examples
        val key = BACKey(
            DocumentNumber("L898902C3"),
            ICAODate("690806"),
            ICAODate("940623")
        )

        assertEquals("BAC", key.algorithm)
        assertEquals("L898902C3", key.documentNumber.value)
    }

    // ==================== Validation via value classes ====================

    @Test
    fun `blank document number is rejected`() {
        assertThrows<IllegalArgumentException> {
            BACKey(
                DocumentNumber(""),
                ICAODate("900115"),
                ICAODate("300101")
            )
        }
    }

    @Test
    fun `invalid date format is rejected`() {
        assertThrows<IllegalArgumentException> {
            BACKey(
                DocumentNumber("AB1234567"),
                ICAODate("19900115"),  // yyyyMMdd instead of yyMMdd
                ICAODate("300101")
            )
        }
    }
}