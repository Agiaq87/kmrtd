import kmrtd.support.DocumentNumber
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Tests for [kmrtd.support.DocumentNumber] value class.
 *
 * Verifies document number validation and ICAO-compliant padding
 * as required by ICAO Doc 9303 for MRZ key seed computation.
 */
class DocumentNumberTest {

    // ==================== Construction ====================

    @Test
    fun `valid document number is accepted`() {
        val docNum = DocumentNumber("AB1234567")
        assertEquals("AB1234567", docNum.value)
    }

    @Test
    fun `single character document number is accepted`() {
        val docNum = DocumentNumber("A")
        assertEquals("A", docNum.value)
    }

    @Test
    fun `numeric-only document number is accepted`() {
        val docNum = DocumentNumber("123456789")
        assertEquals("123456789", docNum.value)
    }

    // ==================== Validation ====================

    @Test
    fun `blank string is rejected`() {
        assertThrows<IllegalArgumentException> {
            DocumentNumber("")
        }
    }

    @Test
    fun `whitespace-only string is rejected`() {
        assertThrows<IllegalArgumentException> {
            DocumentNumber("   ")
        }
    }

    // ==================== Padding ====================

    @Test
    fun `9-char document number is not padded`() {
        val docNum = DocumentNumber("AB1234567")
        assertEquals("AB1234567", docNum.padded)
    }

    @Test
    fun `longer than 9 chars is not padded`() {
        val docNum = DocumentNumber("AB12345678901")
        assertEquals("AB12345678901", docNum.padded)
    }

    @Test
    fun `5-char document number is padded to 9 with filler`() {
        val docNum = DocumentNumber("AB123")
        // padEnd(9, '<') then trim() — the '<' chars are not whitespace so trim doesn't remove them
        assertEquals("AB123<<<<", docNum.padded)
    }

    @Test
    fun `1-char document number is padded to 9`() {
        val docNum = DocumentNumber("A")
        assertEquals("A<<<<<<<<", docNum.padded)
    }

    @Test
    fun `padding matches JMRTD Java behavior`() {
        // JMRTD Java original: documentNumber.padEnd(9, '<').trim()
        // This test verifies behavioral equivalence with the Java implementation
        val input = "L898902C"
        val docNum = DocumentNumber(input)

        // Java: StringBuilder("L898902C").append('<' until length >= 9) = "L898902C<"
        // Then .trim() -> "L898902C<"
        assertEquals("L898902C<", docNum.padded)
    }

    @Test
    fun `exactly 9 chars returns same value`() {
        val docNum = DocumentNumber("123456789")
        assertEquals("123456789", docNum.padded)
        assertEquals(docNum.value, docNum.padded)
    }

    // ==================== Equality ====================

    @Test
    fun `same document numbers are equal`() {
        val a = DocumentNumber("AB1234567")
        val b = DocumentNumber("AB1234567")
        assertEquals(a, b)
    }

    @Test
    fun `different document numbers are not equal`() {
        val a = DocumentNumber("AB1234567")
        val b = DocumentNumber("CD9876543")
        assertNotEquals(a, b)
    }

    @Test
    fun `equality is case sensitive`() {
        val a = DocumentNumber("ab1234567")
        val b = DocumentNumber("AB1234567")
        assertNotEquals(a, b)
    }
}