package com.example.slapp

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.jtransforms.fft.DoubleFFT_1D
import kotlin.coroutines.coroutineContext
import kotlin.math.abs

class SoundUnlockManager(private val context: Context){
    private val sharedPrefs = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
    private var audioRecord: AudioRecord? = null
    private var isListening = false
    private var isRecording = false
    private val targetFrequency = 440.0f
    private val frequencyTolerance = AppConfig.FREQUENCY_TOLERANCE
    private var listeningJob: Job? = null
    suspend fun startListening(){
        Log.i("SoundUnlockManager", "startListening called")
        if (isListening) return
        if (!sharedPrefs.getBoolean("isLocked", false)) return
        isListening = true
        listeningJob = CoroutineScope(Dispatchers.IO).launch {
            startAudioRecording()
        }
    }
    @Synchronized
    fun stopListening(){
        if (!isListening) return
        isListening = false
        listeningJob?.cancel()
        listeningJob = null
        stopAudioRecording()
    }



    private suspend fun startAudioRecording(){
        isRecording = true
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.e("SoundUnlockManager", "RECORD_AUDIO permission not granted")
            return
        }
        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )
            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED){
                Log.e("SoundUnlockManager", "AudioRecord not initialized")
                return
            }
            audioRecord?.startRecording()

            val buffer = ShortArray(bufferSize)
            var consecutiveTargetFrequencyCount = 0
            val targetFrequencyThreshold = (AppConfig.FREQUENCY_HELD*sampleRate/bufferSize).toInt()

            while (isListening && coroutineContext.isActive) {
                val bytesRead = audioRecord?.read(buffer, 0, bufferSize) ?: 0
                if (bytesRead > 0) {
                    val frequency = calculateDominantFrequency(buffer)
                    Log.i("SoundUnlockManager", "Dominant Frequency: $frequency Hz")
                    if (isTargetFrequencyDetected(frequency)) {
                        consecutiveTargetFrequencyCount++
                        Log.i("SoundUnlockManager", "Consecutive target frequency count $consecutiveTargetFrequencyCount of $targetFrequencyThreshold")
                        if (consecutiveTargetFrequencyCount >= targetFrequencyThreshold) {
                            Log.i("SoundUnlockManager", "Target frequency held for ${AppConfig.FREQUENCY_HELD} seconds!")
                            unlock()
                            stopListening()
                        }
                    } else {
                        consecutiveTargetFrequencyCount = 0
                        Log.i("SoundUnlockManager", "Consecutive target frequency count reset:0 of $targetFrequencyThreshold")
                    }
                }
            }
        } catch (e: Exception){
            Log.e("SoundUnlockManager", "Error creating AudioRecord", e)
        } finally {
            stopAudioRecording()
            isRecording = false
        }
    }

    private fun stopAudioRecording(){
        if (audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
            try {
                audioRecord?.stop()
            } catch (e: IllegalStateException) {
                Log.e("SoundUnlockManager", "Error stopping AudioRecord", e)
            }
        }
        audioRecord?.release()
        audioRecord = null
    }

    private fun unlock() {
        sharedPrefs.edit().putBoolean("isLocked", false).apply()
    }

    private fun calculateDominantFrequency(buffer: ShortArray): Float {
        val fft = DoubleFFT_1D(buffer.size.toLong())
        val windowedBuffer = applyHannWindow(buffer)
        val doubleBuffer = windowedBuffer.map { it.toDouble() }.toDoubleArray()
        fft.realForward(doubleBuffer)

        var maxMagnitude = 0.0
        var maxMagnitudeIndex = 0

        for (i in 0 until doubleBuffer.size / 2) {
            val real = doubleBuffer[2 * i]
            val imaginary = doubleBuffer[2 * i + 1]
            val magnitude = kotlin.math.sqrt(real * real + imaginary * imaginary)

            if (magnitude > maxMagnitude) {
                maxMagnitude = magnitude
                maxMagnitudeIndex = i
            }

        }
        Log.i("SoundUnlockManager", "Max Magnitude Index: $maxMagnitudeIndex")
        return (maxMagnitudeIndex.toFloat() * sampleRate / buffer.size).toFloat()
    }

    private fun applyHannWindow(buffer: ShortArray): ShortArray {
        val windowedBuffer = ShortArray(buffer.size)
        for (i in buffer.indices) {
            val windowValue = (0.5 - 0.5 * kotlin.math.cos(2 * Math.PI * i / (buffer.size - 1))).toFloat()
            windowedBuffer[i] = (buffer[i] * windowValue).toInt().toShort()
        }
        return windowedBuffer
    }

    private fun isTargetFrequencyDetected(frequency: Float): Boolean {
        return abs(frequency - targetFrequency) <= frequencyTolerance
    }
}