import kmrtd.io.Fragment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

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