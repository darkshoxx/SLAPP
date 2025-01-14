package com.example.slapp

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StateViewModel : ViewModel() {
    private val _combination = mutableStateListOf<Int>()
    private val _inputBuffer = mutableStateOf(FILOBuffer(6))
    private val _bufferActive = mutableStateOf(false)
    private val _settingCombination = mutableStateOf(false)
    private val _isServiceBound = MutableStateFlow(false)
    val settingCombination: State<Boolean> get() = _settingCombination
    val combination: List<Int> get() = _combination
    val inputBuffer: FILOBuffer get() = _inputBuffer.value
    val bufferActive:  Boolean get() = _bufferActive.value
    val isServiceBound: StateFlow<Boolean> = _isServiceBound.asStateFlow()
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
    fun startService(context: Context){
        Log.i("Service", "SHOXX Starting Service")
        Log.i("Service", "SHOXX service bound: ${_isServiceBound.value}")
        val intent = Intent(context, ForegroundService::class.java)
        ContextCompat.startForegroundService(context, intent)
    }
    fun stopService(context: Context){
        val intent = Intent(context, ForegroundService::class.java)
        context.stopService(intent)
    }
    fun setServiceBound(isBound: Boolean) {
        _isServiceBound.value = isBound
    }
}

