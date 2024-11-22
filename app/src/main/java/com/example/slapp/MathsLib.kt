package com.example.slapp

import kotlin.math.atan2

fun calculateRegion(px:Float, py:Float, cx:Float, cy:Float):Int{
    val segmentSixth = 2 * Math.PI / 6
    val angle: Double = atan2(-(py.toDouble()-cy.toDouble()), px.toDouble()-cx.toDouble())
    return ( (2 - (angle / segmentSixth) + 6) % 6 + 1).toInt()

}