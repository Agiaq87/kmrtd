import kmrtd.io.SplittableInputStream
import java.io.ByteArrayInputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SplittableInputStreamTest {

    @Test
    fun `read yields same bytes as original`() {
        val data = byteArrayOf(10, 20, 30, 40, 50)
        val sis = SplittableInputStream(ByteArrayInputStream(data), data.size)

        for (b in data) {
            assertEquals(b.toInt() and 0xFF, sis.read())
        }
        assertEquals(-1, sis.read())
    }

    @Test
    fun `getInputStream returns independent copy at position`() {
        val data = byteArrayOf(10, 20, 30, 40, 50)
        val sis = SplittableInputStream(ByteArrayInputStream(data), data.size)

        // Read first 3 bytes to buffer them
        sis.read()
        sis.read()
        sis.read()

        // Get copy at position 1
        val copy = sis.getInputStream(1)
        assertEquals(20, copy.read())
        assertEquals(30, copy.read())
    }

    @Test
    fun `position tracks bytes read`() {
        val data = byteArrayOf(1, 2, 3, 4, 5)
        val sis = SplittableInputStream(ByteArrayInputStream(data), data.size)
        assertEquals(0, sis.position)
        sis.read()
        sis.read()
        assertEquals(2, sis.position)
    }

    @Test
    fun `skip advances position`() {
        val data = ByteArray(100) { it.toByte() }
        val sis = SplittableInputStream(ByteArrayInputStream(data), data.size)
        sis.skip(10)
        assertEquals(10, sis.position)
        assertEquals(10, sis.read())
    }

    @Test
    fun `mark and reset`() {
        val data = byteArrayOf(1, 2, 3, 4, 5)
        val sis = SplittableInputStream(ByteArrayInputStream(data), data.size)
        sis.read() // 1
        sis.mark(10)
        sis.read() // 2
        sis.read() // 3
        sis.reset()
        assertEquals(2, sis.read()) // re-reads from marked position
    }

    @Test
    fun `markSupported returns true`() {
        val data = byteArrayOf(1, 2, 3)
        val sis = SplittableInputStream(ByteArrayInputStream(data), data.size)
        assertTrue(sis.markSupported())
    }

    @Test
    fun `length and bytesBuffered`() {
        val data = byteArrayOf(1, 2, 3, 4, 5)
        val sis = SplittableInputStream(ByteArrayInputStream(data), data.size)
        assertEquals(5, sis.length)
        assertEquals(0, sis.bytesBuffered)
        sis.read()
        sis.read()
        sis.read()
        assertEquals(3, sis.bytesBuffered)
    }
}