/*
 * Tests for kmrtd.io package
 *
 * Copyright (C) 2026 Alessandro Giaquinto
 * Licensed under LGPL 3.0
 */
package kmrtd.io

import java.io.ByteArrayInputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertNotEquals
import kotlin.test.assertFailsWith

// ============================================================
// Fragment tests
// ============================================================
class FragmentTest {

    @Test
    fun `data class equality`() {
        val f1 = Fragment(10, 20)
        val f2 = Fragment(10, 20)
        assertEquals(f1, f2)
        assertEquals(f1.hashCode(), f2.hashCode())
    }

    @Test
    fun `data class inequality`() {
        val f1 = Fragment(10, 20)
        val f2 = Fragment(10, 30)
        val f3 = Fragment(5, 20)
        assertNotEquals(f1, f2)
        assertNotEquals(f1, f3)
    }

    @Test
    fun `toString format`() {
        val f = Fragment(5, 10)
        assertEquals("[5 .. 14 (10)]", f.toString())
    }

    @Test
    fun `destructuring support`() {
        val (offset, length) = Fragment(3, 7)
        assertEquals(3, offset)
        assertEquals(7, length)
    }

    @Test
    fun `copy with modification`() {
        val f = Fragment(10, 20)
        val extended = f.copy(length = 30)
        assertEquals(10, extended.offset)
        assertEquals(30, extended.length)
    }
}

// ============================================================
// FragmentBuffer tests
// ============================================================
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

// ============================================================
// PositionInputStream tests
// ============================================================
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

// ============================================================
// InputStreamBuffer tests
// ============================================================
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

// ============================================================
// SplittableInputStream tests
// ============================================================
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