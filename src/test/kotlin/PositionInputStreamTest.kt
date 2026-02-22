import kmrtd.io.PositionInputStream
import java.io.ByteArrayInputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PositionInputStreamTest {

    @Test
    fun `position starts at zero`() {
        val pis = PositionInputStream(ByteArrayInputStream(byteArrayOf(1, 2, 3)))
        assertEquals(0L, pis.position)
    }

    @Test
    fun `read increments position`() {
        val pis = PositionInputStream(ByteArrayInputStream(byteArrayOf(10, 20, 30)))
        assertEquals(10, pis.read())
        assertEquals(1L, pis.position)
        assertEquals(20, pis.read())
        assertEquals(2L, pis.position)
    }

    @Test
    fun `read at EOF returns -1 and does not increment`() {
        val pis = PositionInputStream(ByteArrayInputStream(byteArrayOf(1)))
        pis.read()
        assertEquals(1L, pis.position)
        assertEquals(-1, pis.read())
        assertEquals(1L, pis.position)
    }

    @Test
    fun `read into byte array updates position`() {
        val data = byteArrayOf(1, 2, 3, 4, 5)
        val pis = PositionInputStream(ByteArrayInputStream(data))
        val buf = ByteArray(3)
        val read = pis.read(buf, 0, 3)
        assertEquals(3, read)
        assertEquals(3L, pis.position)
        assertEquals(1.toByte(), buf[0])
        assertEquals(2.toByte(), buf[1])
        assertEquals(3.toByte(), buf[2])
    }

    @Test
    fun `skip updates position`() {
        val data = ByteArray(100) { it.toByte() }
        val pis = PositionInputStream(ByteArrayInputStream(data))
        val skipped = pis.skip(50)
        assertEquals(50L, skipped)
        assertEquals(50L, pis.position)
    }

    @Test
    fun `mark and reset restore position`() {
        val data = byteArrayOf(1, 2, 3, 4, 5)
        val pis = PositionInputStream(ByteArrayInputStream(data))
        pis.read() // position = 1
        pis.mark(10)
        pis.read() // position = 2
        pis.read() // position = 3
        pis.reset()
        assertEquals(1L, pis.position)
        assertEquals(2, pis.read()) // re-reads byte at index 1
    }

    @Test
    fun `markSupported delegates to carrier`() {
        val pis = PositionInputStream(ByteArrayInputStream(byteArrayOf(1)))
        assertTrue(pis.markSupported()) // ByteArrayInputStream supports mark
    }
}