package com.example.slapp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.BoxScopeInstance.align
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import com.example.slapp.ui.theme.SLAPPTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SLAPPTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                   MainScreen()
                }
            }
        }
    }
}



@Composable
fun MainScreen(){
    val viewModel: StateViewModel = viewModel()
    Box(modifier = Modifier.fillMaxSize()){
        GestureScreen()
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround){
            Button(onClick = {
                if (!viewModel.settingCombination.value) {
                    viewModel.clearCombination()
                }
                viewModel.toggleSettingCombination()
                val logsetting = viewModel.settingCombination.value
                Log.i("Setting", "Setting is now $logsetting")
            },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewModel.settingCombination.value) Color.Red else Color.Blue
                )
            ) { Text(text = if (viewModel.settingCombination.value) "SETTING" else "Set Combination") }
            Button(onClick = {
                viewModel.clearBuffer()
                viewModel.toggleBufferActive()
                val logbuffer = viewModel.bufferActive
                Log.i("Buffer", "Buffer is now $logbuffer")
            },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewModel.bufferActive) Color.Red else Color.Blue
                )
                ) { Text(text = if (viewModel.bufferActive) "TESTING" else "Test Combination") }
        }
    }
}


@Composable
fun GestureScreen() {
    // config variables
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    // size and position variables
    val screenWidth = with(density) {
         configuration.screenWidthDp.dp.toPx()
    }
    val screenHeight = with(density) {
        configuration.screenHeightDp.dp.toPx()
    }
    val centerX = screenWidth / 2
    val centerY = screenHeight / 2
    var region = 0
    // combo related variables
    val viewModel: StateViewModel = viewModel()

    val context = LocalContext.current


    viewModel.addToCombination(1)
    viewModel.addToCombination(2)
    viewModel.addToCombination(3)
    viewModel.addToCombination(4)

    Surface(
        color = colorResource(R.color.gray),
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        // Handle tap gesture here
                        region = calculateRegion(offset.x, offset.y, centerX, centerY)
                        println("Tapped at: $offset")
                        if (viewModel.bufferActive) {
                            viewModel.pushToBuffer(region)
                        }
                        Log.i(
                            "Tap",
                            "Tap in reg $region. Bufferbool is ${viewModel.bufferActive}, and contains ${viewModel.queue()}, combo is ${viewModel.combination.toList()}"
                        )
                        if (!viewModel.settingCombination.value) {
                            // Only attempt unlocking when NOT setting combination
                            val success: Boolean = viewModel.tryUnlock()
                            if (success) {
                                Log.i("Success", "Successful unlock!")
                                viewModel.clearBuffer()
                                // Make a toast pup up that says "unlocked"
                                Toast.makeText(context, "Unlocked!", Toast.LENGTH_SHORT).show()
                            } else {
                                Log.i("Failure", "Still Locked!")
                            }
                        } else {
                            // setting combination, adding to combination
                            viewModel.addToCombination(region)
                        }

                    },
                    onDoubleTap = { offset ->
                        // Handle double tap gesture here
                        println("Double tapped at: $offset")
                        Log.i("DoubleTap", "Double tapped at: $offset in region $region")
                    },
                    onLongPress = { offset ->
                        // Handle long press gesture here
                        println("Long pressed at: $offset")
                        Log.i("LongPress", "Long pressed at: $offset in region $region")
                    }
                )
            }
    ) {
        Column (
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            content = {
                CenterHexImage()
            }
                )
    }
}

@Composable
fun CenterHexImage() {
    Image(
        painter = painterResource(id = R.drawable.hexsimple_2),
        contentDescription = "The Hexagon"
    )
}

@Preview(showBackground = true)
@Composable
fun GestureScreenPreview() {
    SLAPPTheme { // Apply your app's theme
        GestureScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    SLAPPTheme { // Apply your app's theme
        MainScreen()
    }
}

class MyForegroundService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        // Your background logic here
        return START_STICKY
    }
    override fun onBind(intent: Intent?): IBinder? {
        return null}
    // ... other service methods ...

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
            .setSmallIcon(R.drawable.hex_simple) // Replace with your icon
            .setContentIntent(pendingIntent)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "my_channel_id"
        private const val CHANNEL_NAME = "My Channel"
        private const val NOTIFICATION_ID = 1
    }
}