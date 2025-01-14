package com.example.slapp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ForegroundService : LifecycleService() {
    private lateinit var soundUnlockManager: SoundUnlockManager
    private val serviceScope = CoroutineScope(Dispatchers.Main)

    private val sharedPrefs by lazy { getSharedPreferences("my_prefs", Context.MODE_PRIVATE) }
    private val sharedPrefsListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPrefs, key ->
        if (key == "isLocked") {
            val isLocked = sharedPrefs.getBoolean("isLocked", false)
            val toastMessage = if (isLocked) "Locked" else "Unlocked"
            Toast
                .makeText(applicationContext, toastMessage, Toast.LENGTH_SHORT)
                .show()
            if (isLocked) {
                timestampWhenLocked = System.currentTimeMillis() / 1000
                serviceScope.launch {
                    soundUnlockManager.startListening()
                }
            } else {
                soundUnlockManager.stopListening()
                timestampWhenLocked = 0

            }
        }

    }

    inner class LocalBinder : Binder() {
        fun getService(): ForegroundService = this@ForegroundService
    }
    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return LocalBinder()
    }

    var timestampWhenLocked: Long
        get() = sharedPrefs.getLong("timestampWhenLocked", 0)
        set(value) {
            sharedPrefs.edit().putLong("timestampWhenLocked", value).apply()
        }

    private var varTimeout = AppConfig.TIMEOUT_SECONDS // 15 seconds when emulating, 1 in testing, 3600 by production default

    private fun startTimeoutCheck() {
        var isLocked: Boolean
        lifecycleScope.launch {
            while (true) {
                isLocked = sharedPrefs.getBoolean("isLocked", false)
                Log.i("Timeout", "isLocked is $isLocked")
                if (isLocked){
                    val currentTime = System.currentTimeMillis() / 1000
                    Log.i("Timeout", "currentTime is $currentTime")
                    Log.i("Timeout", "timestampWhenLocked is $timestampWhenLocked")
                    Log.i("Timeout", "varTimeout is $varTimeout")
                    if (currentTime - timestampWhenLocked > varTimeout) {
                        Log.i("Timeout", "Timeout triggered")
                        isLocked = false
                        sharedPrefs.edit()
                            .putBoolean("isLocked", false)
                            .apply()
                        timestampWhenLocked = 0
                    }
                }
                delay(1000)
            }

        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("Service", "SHOXX7 onStartCommand")
        super.onStartCommand(intent, flags, startId)
        createNotificationChannel()
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        // Your background logic here

        return START_NOT_STICKY
    }


    override fun onCreate() {
        super.onCreate()
        soundUnlockManager = SoundUnlockManager(this)
        sharedPrefs.registerOnSharedPreferenceChangeListener(sharedPrefsListener)
        startTimeoutCheck()
    }

    override fun onDestroy() {
        Log.i("Service", "SHOXX onDestroy")
        super.onDestroy()
        soundUnlockManager.stopListening()
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(sharedPrefsListener)
        stopForeground(true)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("My Foreground Service")
            .setContentText("Service is running")
            .setSmallIcon(R.drawable.hex_simple_icon) // Replace with your icon
            .setContentIntent(pendingIntent)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "my_channel_id"
        private const val CHANNEL_NAME = "My Channel"
        private const val NOTIFICATION_ID = 1
    }
}