package com.example.slapp

import kotlin.math.atan2
import kotlin.collections.ArrayDeque

fun calculateRegion(px:Float, py:Float, cx:Float, cy:Float):Int{
    val segmentSixth = 2 * Math.PI / 6
    val angle: Double = atan2(-(py.toDouble()-cy.toDouble()), px.toDouble()-cx.toDouble())
    return ( (2 - (angle / segmentSixth) + 6) % 6 + 1).toInt()

}


class FILOBuffer(private val maxSize: Int) {
    private val buffer = ArrayDeque<Int>(maxSize)

    fun push(value: Int) {
        if (buffer.size == maxSize) {
            buffer.removeFirst()
        }
        buffer.addLast(value)
    }

    fun queue():List<Int> {
        return buffer.toList()
    }

    fun pop(): Int? {
        return if (buffer.isNotEmpty()) buffer.removeLast() else null
    }

    fun peek(): Int? {
        return buffer.lastOrNull()
    }

    fun isEmpty(): Boolean {
        return buffer.isEmpty()
    }

    fun isFull(): Boolean {
        return buffer.size == maxSize
    }

    fun clear() {
        buffer.clear()
    }
}

fun tryUnlock(inputBuffer: FILOBuffer, combination: List<Int>): Boolean {
    val bufferContents = inputBuffer.queue()
    if (bufferContents.size < combination.size) {
        return false
    }
    for (i in combination.indices) {
        if (bufferContents[bufferContents.size - combination.size + i] != combination[i]) {
            return false
        }
    }
    return true
}