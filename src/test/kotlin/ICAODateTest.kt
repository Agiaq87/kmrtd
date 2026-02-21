import kmrtd.support.ICAODate
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.text.SimpleDateFormat
import java.util.*
import kotlin.test.assertEquals

/**
 * Tests for [kmrtd.support.ICAODate] value class.
 *
 * Verifies that ICAO date strings conform to the yyMMdd format
 * as required by ICAO Doc 9303 for Machine Readable Zones.
 */
class ICAODateTest {

    // ==================== Construction ====================

    @Test
    fun `valid 6-char date string is accepted`() {
        val date = ICAODate("900115")
        assertEquals("900115", date.date)
    }

    @Test
    fun `date with leading zeros is accepted`() {
        val date = ICAODate("010101")
        assertEquals("010101", date.date)
    }

    @Test
    fun `date 000101 is accepted`() {
        val date = ICAODate("000101")
        assertEquals("000101", date.date)
    }

    @Test
    fun `date 991231 is accepted`() {
        val date = ICAODate("991231")
        assertEquals("991231", date.date)
    }

    // ==================== Validation ====================

    @Test
    fun `empty string is rejected`() {
        assertThrows<IllegalArgumentException> {
            ICAODate("")
        }
    }

    @Test
    fun `string shorter than 6 chars is rejected`() {
        assertThrows<IllegalArgumentException> {
            ICAODate("90011")
        }
    }

    @Test
    fun `string longer than 6 chars is rejected`() {
        assertThrows<IllegalArgumentException> {
            ICAODate("9001150")
        }
    }

    @Test
    fun `full date format yyyyMMdd is rejected`() {
        assertThrows<IllegalArgumentException> {
            ICAODate("19900115")
        }
    }

    // ==================== Factory: from(Date) ====================

    @Test
    fun `from Date produces correct yyMMdd string`() {
        val calendar = Calendar.getInstance().apply {
            set(1990, Calendar.JANUARY, 15)
        }
        val date = ICAODate.Companion.from(calendar.time)
        assertEquals("900115", date.date)
    }

    @Test
    fun `from Date handles year 2000`() {
        val calendar = Calendar.getInstance().apply {
            set(2000, Calendar.JUNE, 30)
        }
        val date = ICAODate.Companion.from(calendar.time)
        assertEquals("000630", date.date)
    }

    @Test
    fun `from Date handles December 31`() {
        val calendar = Calendar.getInstance().apply {
            set(2030, Calendar.DECEMBER, 31)
        }
        val date = ICAODate.Companion.from(calendar.time)
        assertEquals("301231", date.date)
    }

    // ==================== Factory: from(Long) ====================

    @Test
    fun `from millis produces same result as from Date`() {
        val calendar = Calendar.getInstance().apply {
            set(1985, Calendar.MARCH, 22, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val millis = calendar.timeInMillis

        val fromDate = ICAODate.Companion.from(calendar.time)
        val fromMillis = ICAODate.Companion.from(millis)

        assertEquals(fromDate, fromMillis)
    }

    @Test
    fun `from epoch zero produces 700101`() {
        // Epoch 0 = January 1, 1970 (may vary by timezone)
        val date = ICAODate.Companion.from(0L)
        val expected = SimpleDateFormat("yyMMdd").format(Date(0L))
        assertEquals(expected, date.date)
    }

    // ==================== Equality ====================

    @Test
    fun `two ICAODates with same string are equal`() {
        val a = ICAODate("900115")
        val b = ICAODate("900115")
        assertEquals(a, b)
    }

    @Test
    fun `ICAODate from string equals ICAODate from Date`() {
        val calendar = Calendar.getInstance().apply {
            set(1990, Calendar.JANUARY, 15)
        }
        val fromString = ICAODate("900115")
        val fromDate = ICAODate.Companion.from(calendar.time)
        assertEquals(fromString, fromDate)
    }
}