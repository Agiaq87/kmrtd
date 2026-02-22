import kmrtd.io.FragmentBuffer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class FragmentBufferTest {

    @Test
    fun `default construction`() {
        val fb = FragmentBuffer()
        assertEquals(2000, fb.length)
        assertEquals(0, fb.position)
        assertEquals(0, fb.bytesBuffered)
        assertTrue(fb.fragments.isEmpty())
    }

    @Test
    fun `custom length construction`() {
        val fb = FragmentBuffer(100)
        assertEquals(100, fb.length)
    }

    @Test
    fun `add single byte fragment`() {
        val fb = FragmentBuffer(10)
        fb.addFragment(3, 0x42.toByte())
        assertTrue(fb.isCoveredByFragment(3))
        assertFalse(fb.isCoveredByFragment(2))
        assertFalse(fb.isCoveredByFragment(4))
        assertEquals(0x42.toByte(), fb.buffer[3])
    }

    @Test
    fun `add byte array fragment`() {
        val fb = FragmentBuffer(20)
        val data = byteArrayOf(1, 2, 3, 4, 5)
        fb.addFragment(5, data)

        assertTrue(fb.isCoveredByFragment(5, 5))
        assertFalse(fb.isCoveredByFragment(4))
        assertFalse(fb.isCoveredByFragment(10))

        for (i in data.indices) {
            assertEquals(data[i], fb.buffer[5 + i])
        }
    }

    @Test
    fun `add fragment with src offset and length`() {
        val fb = FragmentBuffer(20)
        val data = byteArrayOf(10, 20, 30, 40, 50)
        fb.addFragment(2, data, 1, 3) // copies [20, 30, 40] at offset 2

        assertEquals(20.toByte(), fb.buffer[2])
        assertEquals(30.toByte(), fb.buffer[3])
        assertEquals(40.toByte(), fb.buffer[4])
        assertTrue(fb.isCoveredByFragment(2, 3))
    }

    @Test
    fun `buffer auto-grows when fragment exceeds capacity`() {
        val fb = FragmentBuffer(10)
        val data = byteArrayOf(1, 2, 3, 4, 5)
        fb.addFragment(8, data) // 8 + 5 = 13 > 10
        assertTrue(fb.length > 10)
        assertTrue(fb.isCoveredByFragment(8, 5))
    }

    @Test
    fun `overlapping fragments merge - contained in existing`() {
        val fb = FragmentBuffer(20)
        fb.addFragment(2, byteArrayOf(1, 2, 3, 4, 5))  // [2..6]
        fb.addFragment(3, byteArrayOf(10, 20))           // [3..4] contained in [2..6]
        // Should still have one fragment covering [2..6]
        assertTrue(fb.isCoveredByFragment(2, 5))
    }

    @Test
    fun `overlapping fragments merge - extending right`() {
        val fb = FragmentBuffer(20)
        fb.addFragment(2, byteArrayOf(1, 2, 3))      // [2..4]
        fb.addFragment(4, byteArrayOf(10, 20, 30))    // [4..6] overlaps right
        assertTrue(fb.isCoveredByFragment(2, 5))       // merged to [2..6]
    }

    @Test
    fun `position tracks farthest buffered byte`() {
        val fb = FragmentBuffer(20)
        fb.addFragment(5, byteArrayOf(1, 2, 3))
        assertEquals(8, fb.position) // 5 + 3 = 8
    }

    @Test
    fun `bytesBuffered counts covered bytes`() {
        val fb = FragmentBuffer(20)
        fb.addFragment(0, byteArrayOf(1, 2, 3))
        fb.addFragment(10, byteArrayOf(4, 5))
        assertEquals(5, fb.bytesBuffered) // 3 + 2
    }

    @Test
    fun `getBufferedLength returns contiguous length from index`() {
        val fb = FragmentBuffer(20)
        fb.addFragment(5, byteArrayOf(1, 2, 3, 4, 5)) // [5..9]
        assertEquals(5, fb.getBufferedLength(5))
        assertEquals(3, fb.getBufferedLength(7))
        assertEquals(0, fb.getBufferedLength(10))
        assertEquals(0, fb.getBufferedLength(3))
    }

    @Test
    fun `getBufferedLength returns 0 for index beyond buffer`() {
        val fb = FragmentBuffer(10)
        assertEquals(0, fb.getBufferedLength(100))
    }

    @Test
    fun `getSmallestUnbufferedFragment - fully buffered returns zero length`() {
        val fb = FragmentBuffer(20)
        fb.addFragment(0, byteArrayOf(1, 2, 3, 4, 5))
        val result = fb.getSmallestUnbufferedFragment(1, 3)
        assertEquals(0, result.length)
    }

    @Test
    fun `getSmallestUnbufferedFragment - partially buffered trims prefix`() {
        val fb = FragmentBuffer(20)
        fb.addFragment(0, byteArrayOf(1, 2, 3)) // [0..2]
        val result = fb.getSmallestUnbufferedFragment(1, 5) // request [1..5], [1..2] buffered
        assertEquals(3, result.offset)
        assertEquals(3, result.length)
    }

    @Test
    fun `getSmallestUnbufferedFragment - nothing buffered returns original`() {
        val fb = FragmentBuffer(20)
        val result = fb.getSmallestUnbufferedFragment(5, 10)
        assertEquals(5, result.offset)
        assertEquals(10, result.length)
    }

    @Test
    fun `updateFrom copies fragments from other buffer`() {
        val fb1 = FragmentBuffer(20)
        fb1.addFragment(0, byteArrayOf(10, 20, 30))

        val fb2 = FragmentBuffer(20)
        fb2.updateFrom(fb1)

        assertTrue(fb2.isCoveredByFragment(0, 3))
        assertEquals(10.toByte(), fb2.buffer[0])
        assertEquals(20.toByte(), fb2.buffer[1])
        assertEquals(30.toByte(), fb2.buffer[2])
    }

    @Test
    fun `equals and hashCode`() {
        val fb1 = FragmentBuffer(10)
        val fb2 = FragmentBuffer(10)
        fb1.addFragment(0, byteArrayOf(1, 2, 3))
        fb2.addFragment(0, byteArrayOf(1, 2, 3))
        assertEquals(fb1, fb2)
        assertEquals(fb1.hashCode(), fb2.hashCode())
    }

    @Test
    fun `not equals with different data`() {
        val fb1 = FragmentBuffer(10)
        val fb2 = FragmentBuffer(10)
        fb1.addFragment(0, byteArrayOf(1, 2, 3))
        fb2.addFragment(0, byteArrayOf(4, 5, 6))
        assertNotEquals(fb1, fb2)
    }

    @Test
    fun `length setter does not shrink`() {
        val fb = FragmentBuffer(100)
        fb.addFragment(0, byteArrayOf(1, 2, 3))
        // length property setter is private, but addFragment triggers growth
        // Verify that existing data is preserved after growth
        fb.addFragment(200, byteArrayOf(99))
        assertTrue(fb.length >= 201)
        assertEquals(1.toByte(), fb.buffer[0])
        assertEquals(2.toByte(), fb.buffer[1])
        assertEquals(3.toByte(), fb.buffer[2])
    }
}