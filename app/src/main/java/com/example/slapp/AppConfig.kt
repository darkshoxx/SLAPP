package com.example.slapp

object AppConfig {
    var TIMEOUT_SECONDS: Int = 0
        private set
    var FREQUENCY_TOLERANCE: Float = 0f
        private set

    fun setConfig(timeoutSeconds: Int, frequencyTolerance: Float) {
        TIMEOUT_SECONDS = timeoutSeconds
        FREQUENCY_TOLERANCE = frequencyTolerance
    }
}