package com.example.slapp

import android.content.Context
import android.util.Log
import android.view.KeyEvent

class VolumeButtonManager(private val context: Context, private val viewModel: StateViewModel, private val parentName: String) {
    private var currentNumber = 0

    fun handleVolumeKeyEvent(keyCode: Int): Boolean {
        if (!viewModel.bufferActive) return false // Do nothing if the buffer is inactive

        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                incrementCurrentNumber()
                true // Consume the event
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                pushCurrentNumberToBuffer()
                true // Consume the event
            }
            else -> false // Don't consume other keys
        }
    }

    private fun incrementCurrentNumber() {
        currentNumber = if (currentNumber == 6) 1 else currentNumber + 1
        Log.i("VolumeButtonManager", "Volume Up pressed. Current number incremented to: $currentNumber")
    }

    private fun pushCurrentNumberToBuffer() {
        if (currentNumber != 0) {
            Log.i("VolumeButtonManager", "Volume Down pressed. Pushing number: $currentNumber to buffer")
            viewModel.pushToBuffer(currentNumber)
            if (!viewModel.settingCombination.value) {
                // Only attempt unlocking when NOT setting combination
                val success: Boolean = viewModel.tryUnlock()
                if (success) {
                    val lockMessage: String
                    val lockBoolean: Boolean
                    if (parentName == "LockScreen") {
                        lockMessage = "Successfully LOCKED!"
                        lockBoolean = true
                    } else {
                        lockMessage = "Successfully UNLOCKED!"
                        lockBoolean = false
                    }
                    Log.i("Success", lockMessage)
                    viewModel.clearBuffer()
                    context
                        .getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
                        .edit()
                        .putBoolean("isLocked", lockBoolean)
                        .apply()
                    Log.i("ShaPref", "Shared Preferences updated")
                } else {
                    Log.i("Failure", "Still Locked!")
                }
            } else {
                // setting combination, adding to combination
                viewModel.addToCombination(currentNumber)
            }
            currentNumber = 0 // Reset the current number after pushing
        } else {
            Log.i("VolumeButtonManager", "Volume Down pressed, but current number is 0. No action taken.")
        }
    }
}
