package com.example.slapp

object AppConfig {
    var TIMEOUT_SECONDS: Int = 0
        private set
    var FREQUENCY_TOLERANCE: Float = 0f
        private set
    var FREQUENCY_HELD: Int = 0
        private set

    fun setConfig(timeoutSeconds: Int, frequencyTolerance: Float, frequencyHeld: Int = 0) {
        TIMEOUT_SECONDS = timeoutSeconds
        FREQUENCY_TOLERANCE = frequencyTolerance
        FREQUENCY_HELD = frequencyHeld

    }
}