import kmrtd.io.InputStreamBuffer
import java.io.ByteArrayInputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InputStreamBufferTest {

    @Test
    fun `basic construction and length`() {
        val data = byteArrayOf(1, 2, 3, 4, 5)
        val isb = InputStreamBuffer(ByteArrayInputStream(data), data.size)
        assertEquals(5, isb.length)
        assertEquals(0, isb.position)
        assertEquals(0, isb.bytesBuffered)
    }

    @Test
    fun `sub-stream reads and buffers data`() {
        val data = byteArrayOf(10, 20, 30, 40, 50)
        val isb = InputStreamBuffer(ByteArrayInputStream(data), data.size)
        val sub = isb.inputStream

        assertEquals(10, sub.read())
        assertEquals(20, sub.read())
        assertEquals(2, sub.position)
    }

    @Test
    fun `sub-stream serves from buffer on second read`() {
        val data = byteArrayOf(10, 20, 30, 40, 50)
        val isb = InputStreamBuffer(ByteArrayInputStream(data), data.size)

        // First sub-stream reads everything
        val sub1 = isb.inputStream
        val result1 = ByteArray(5)
        sub1.read(result1)

        // Second sub-stream should serve from buffer
        val sub2 = isb.inputStream
        val result2 = ByteArray(5)
        sub2.read(result2)

        assertTrue(result1.contentEquals(result2))
    }

    @Test
    fun `sub-stream mark and reset`() {
        val data = byteArrayOf(1, 2, 3, 4, 5)
        val isb = InputStreamBuffer(ByteArrayInputStream(data), data.size)
        val sub = isb.inputStream

        sub.read() // 1
        sub.mark(10)
        sub.read() // 2
        sub.read() // 3
        sub.reset()
        assertEquals(2, sub.read()) // re-reads
    }

    @Test
    fun `sub-stream markSupported returns true`() {
        val isb = InputStreamBuffer(ByteArrayInputStream(byteArrayOf(1)), 1)
        assertTrue(isb.inputStream.markSupported())
    }
}