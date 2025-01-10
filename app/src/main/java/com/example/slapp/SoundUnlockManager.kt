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
import kotlinx.coroutines.launch
import org.jtransforms.fft.DoubleFFT_1D
import kotlin.math.abs
import kotlin.math.max

class SoundUnlockManager(private val context: Context){
    private val sharedPrefs = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
    private var audioRecord: AudioRecord? = null
    private var isListening = false
    private val targetFrequency = 440.0f
    private val frequencyTolerance = AppConfig.FREQUENCY_TOLERANCE
    private var listeningJob: Job? = null
    suspend fun startListening(){
        if (isListening) return
        isListening = true
        listeningJob = CoroutineScope(Dispatchers.IO).launch {
            startAudioRecording()
        }
    }

    fun stopListening(){
        isListening = false
        listeningJob?.cancel()
        listeningJob = null
        stopAudioRecording()
    }



    private suspend fun startAudioRecording(){
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
            while (isListening){
                val bytesRead = audioRecord?.read(buffer, 0, bufferSize) ?: 0
                if (bytesRead > 0){
                    val frequency = calculateDominantFrequency(buffer)
                    Log.i("SoundUnlockManager", "Dominant Frequency: $frequency Hz")
                    if (isTargetFrequencyDetected(frequency)){
                        Log.i("SoundUnlockManager", "Target frequency detected!")
                        unlock()
                        stopListening()
                    }
                }


            }
        } catch (e: Exception){
            Log.e("SoundUnlockManager", "Error creating AudioRecord", e)
        } finally {
            stopAudioRecording()
        }
    }

    private fun stopAudioRecording(){
        audioRecord?.stop()
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