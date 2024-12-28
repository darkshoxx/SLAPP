package com.example.slapp

import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class StateViewModel : ViewModel() {
    private val _combination = mutableStateListOf<Int>()
    private val _inputBuffer = mutableStateOf(FILOBuffer(6))
    private val _bufferActive = mutableStateOf(false)
    private val _settingCombination = mutableStateOf(false)
    val settingCombination: State<Boolean> get() = _settingCombination
    val combination: List<Int> get() = _combination
    val inputBuffer: FILOBuffer get() = _inputBuffer.value
    val bufferActive:  Boolean get() = _bufferActive.value
    fun toggleSettingCombination() {
        _settingCombination.value = !_settingCombination.value
    }
    fun toggleBufferActive() {
        _bufferActive.value = !_bufferActive.value
    }
    fun pushToBuffer(value: Int) {
        _inputBuffer.value.push(value)
    }
    fun peekBuffer(): Int? {
        return _inputBuffer.value.peek()
    }
    fun queue(): List<Int> {
        return _inputBuffer.value.queue()
    }
    fun addToCombination(value: Int) {
        _combination.add(value)
    }
    fun tryUnlock(): Boolean {
        return tryUnlock(_inputBuffer.value, _combination)
    }
    fun clearBuffer() {
        _inputBuffer.value.clear()
    }
    fun clearCombination() {
        _combination.clear()
    }
}

