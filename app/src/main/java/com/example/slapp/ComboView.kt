package com.example.slapp

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class ComboViewModel : ViewModel() {
    var combination by mutableStateOf(mutableStateListOf<Int>())
}