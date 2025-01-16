package com.example.slapp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
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
import kotlinx.coroutines.flow.update

class StateViewModel : ViewModel() {
    private val _combination = mutableStateListOf<Int>()
    private val _inputBuffer = mutableStateOf(FILOBuffer(6))
    private val _bufferActive = mutableStateOf(false)
    private val _settingCombination = mutableStateOf(false)
    private val _isServiceBound = MutableStateFlow(false)
    private var serviceConnection: ServiceConnection? = null
    private var _queue = MutableStateFlow<List<Int>>(emptyList())
    private var _lastNumber = MutableStateFlow<Int?>(null)
    val queue: StateFlow<List<Int>> = _queue.asStateFlow()
    val lastNumber: StateFlow<Int?> = _lastNumber.asStateFlow()
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
        _queue.update { _inputBuffer.value.queue()}
        _lastNumber.update { value }
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
        _queue.update{ _inputBuffer.value.queue()}
        _lastNumber.update { null }
    }
    fun clearCombination() {
        _combination.clear()
    }
    fun startService(context: Context){
        Log.i("Service", "SHOXX Starting Service")
        Log.i("Service", "SHOXX service bound: ${_isServiceBound.value}")
        val intent = Intent(context, ForegroundService::class.java)
        ContextCompat.startForegroundService(context, intent)

        if (serviceConnection == null){
            serviceConnection = object : ServiceConnection {
                override fun onServiceConnected(className: ComponentName, service: IBinder) {
                    _isServiceBound.value = true
                    Log.i("Service", "SHOXX service connected! bound: ${_isServiceBound.value}")
                }
                override fun onServiceDisconnected(arg0: ComponentName) {
                    _isServiceBound.value = false
                    Log.i("Service", "SHOXX service disconnected! bound: ${_isServiceBound.value}")
                }
            }
        }
    context.bindService(intent, serviceConnection!!, Context.BIND_AUTO_CREATE)
    }
    fun stopService(context: Context) {
        Log.i("Service", "SHOXX Stopping Service")
        val intent = Intent(context, ForegroundService::class.java)
        if (_isServiceBound.value && serviceConnection != null) {
            context.unbindService(serviceConnection!!)
            _isServiceBound.value = false
            Log.i("Service", "SHOXX3 service bound: ${_isServiceBound.value}")



        }
        if (context.stopService(intent)){
            Log.i("Service", "SHOXX2 service stopped")
        } else {
            Log.i("Service", "SHOXX2 service not stopped")
        }
    }
    fun setServiceBound(isBound: Boolean) {
        _isServiceBound.value = isBound
    }
}

