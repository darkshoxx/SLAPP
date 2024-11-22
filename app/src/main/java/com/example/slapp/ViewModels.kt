package com.example.slapp

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class StateViewModel : ViewModel() {
    var combination by mutableStateOf(mutableStateListOf<Int>())
    var inputBuffer by mutableStateOf(FILOBuffer(20))
    var bufferActive by mutableStateOf(false)
}

