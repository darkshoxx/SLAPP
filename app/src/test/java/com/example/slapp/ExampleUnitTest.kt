package com.example.slapp

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

class MathsLibTest{

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