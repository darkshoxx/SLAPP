package com.example.slapp

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

class UnlockTests{

    @Test
    fun testUnlockSameSize(){
        val inputBuffer = FILOBuffer(3)
        inputBuffer.push(1)
        inputBuffer.push(2)
        inputBuffer.push(3)
        val combination = listOf(3, 2, 1)
        assertTrue(tryUnlock(inputBuffer, combination))
    }

    fun testUnlockSmallerBuffer(){
        val inputBuffer = FILOBuffer(3)
        inputBuffer.push(2)
        inputBuffer.push(3)
        val combination = listOf(3, 2, 1)
        assertFalse(tryUnlock(inputBuffer, combination))
    }

    fun testUnlockLargerBuffer(){
        val inputBuffer = FILOBuffer(5)
        inputBuffer.push(1)
        inputBuffer.push(2)
        inputBuffer.push(3)
        inputBuffer.push(4)
        inputBuffer.push(5)
        val combination = listOf(5, 4, 3)
        assertTrue(tryUnlock(inputBuffer, combination))


    }
}

class FILOTests{

    @Test
    fun testBuffering(){
        val buffer = FILOBuffer(3)
        buffer.push(1)
        buffer.push(2)
        buffer.push(3)
        buffer.push(4)
        assertEquals(4, buffer.pop())
        assertEquals(3, buffer.pop())
        assertEquals(2, buffer.pop())
        assertNull(buffer.pop())
    }

    @Test
    fun testBasics() {
        val buffer = FILOBuffer(3)
        assertFalse(buffer.isFull())
        assertTrue(buffer.isEmpty())
        buffer.push(1)
        buffer.push(2)
        buffer.push(3)
        assertTrue(buffer.isFull())
        assertFalse(buffer.isEmpty())
        assertEquals(3, buffer.pop())
        assertEquals(2, buffer.peek())
        assertEquals(2, buffer.pop())
        assertEquals(1, buffer.peek())
        assertEquals(1, buffer.pop())
        assertNull(buffer.pop())
        assertFalse(buffer.isFull())
        assertTrue(buffer.isEmpty())
        assertNull(buffer.peek())
    }
}

class SegmentTests{

    @Test
    fun testAbsoluteAngleSegments(){
        val result1 = calculateRegion(px=0.0f, py=-100.0f, cx=0.0f, cy=0.0f)
        val result2 = calculateRegion(px=100.0f, py=-100.0f, cx=0.0f, cy=0.0f)
        val result3 = calculateRegion(px=100.0f, py=100.0f, cx=0.0f, cy=0.0f)
        val result4 = calculateRegion(px=0.0f, py=100.0f, cx=0.0f, cy=0.0f)
        val result5 = calculateRegion(px=-100.0f, py=100.0f, cx=0.0f, cy=0.0f)
        val result6 = calculateRegion(px=-100.0f, py=-100.0f, cx=0.0f, cy=0.0f)
        assertEquals(1, result1)
        assertEquals(2, result2)
        assertEquals(3, result3)
        assertEquals(4, result4)
        assertEquals(5, result5)
        assertEquals(6, result6)
    }

    @Test
    fun testRelativeAngleSegments(){
        // Dimensions of Pixel 8: 1080x2400
        val result1 = calculateRegion(px=540.0f, py=1100.0f, cx=540.0f, cy=1200.0f)
        val result2 = calculateRegion(px=640.0f, py=1100.0f, cx=540.0f, cy=1200.0f)
        val result3 = calculateRegion(px=640.0f, py=1300.0f, cx=540.0f, cy=1200.0f)
        val result4 = calculateRegion(px=540.0f, py=1300.0f, cx=540.0f, cy=1200.0f)
        val result5 = calculateRegion(px=440.0f, py=1300.0f, cx=540.0f, cy=1200.0f)
        val result6 = calculateRegion(px=440.0f, py=1100.0f, cx=540.0f, cy=1200.0f)
        assertEquals(1, result1)
        assertEquals(2, result2)
        assertEquals(3, result3)
        assertEquals(4, result4)
        assertEquals(5, result5)
        assertEquals(6, result6)
    }
}