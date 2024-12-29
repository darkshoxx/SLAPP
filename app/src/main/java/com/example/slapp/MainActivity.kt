package com.example.slapp

import android.app.ActivityManager
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlin.io.path.name

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
fun ShowCombinationButton(context: Context, message: String) {
    Button(onClick = {
        // pop up Toast showing the combination
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    },
        colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
    ) { Text(text = "Show combination") }
}

@Composable
fun WarningText(text: String, color: Color, modifier: Modifier){
    var textSize by remember { mutableStateOf(50.sp) }
    val configuration = LocalConfiguration.current
    Text(
        text = text,
        color = color,
        style = MaterialTheme.typography.titleLarge.copy(fontSize = textSize),
        modifier = modifier
            .fillMaxWidth(1f)
            .wrapContentSize(Alignment.Center),
        onTextLayout = { textLayoutResult: TextLayoutResult ->
                    val textWidth = textLayoutResult.size.width
                    val targetWidth = configuration.screenWidthDp.dp*2
                    if (textWidth > targetWidth.value) {
                        textSize *= (targetWidth.value / textWidth)
                    }
                }
            )
}

@Composable
fun UnlockScreen(){
    val viewModel: StateViewModel = viewModel()
    // create local context
    val context = LocalContext.current
    Box(modifier = Modifier.fillMaxSize()){
        GestureScreen()
        // wrap Row into Colum to insert text at bottom of screen
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
//            .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ShowCombinationButton(
                    context,
                    "The combination is ${viewModel.combination.toList()}"
                )
                Button(
                    onClick = {

                        viewModel.clearBuffer()
                        viewModel.toggleBufferActive()
                        val logbuffer = viewModel.bufferActive
                        Log.i("Buffer", "Buffer is now $logbuffer")

                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (viewModel.bufferActive) Color.Red else Color.Blue
                    )
                ) { Text(text = if (viewModel.bufferActive) "UNLOCKING" else "Unlock") }
            }
            WarningText(
                color = Color.Green,
                text = "Unlock to release",
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}



@Composable
fun LockScreen(){
    val viewModel: StateViewModel = viewModel()
    // create local context
    val context = LocalContext.current
    Box(modifier = Modifier.fillMaxSize()){
        GestureScreen()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
            horizontalArrangement = Arrangement.SpaceAround){
            // use the show combination button here
             ShowCombinationButton(context, "The combination is ${viewModel.combination.toList()}")
            Button(onClick = {

                viewModel.clearBuffer()
                viewModel.toggleBufferActive()
                val logbuffer = viewModel.bufferActive
                Log.i("Buffer", "Buffer is now $logbuffer")

            },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewModel.bufferActive) Color.Red else Color.Blue
                )
            ) { Text(text = if (viewModel.bufferActive) "LOCKING" else "Enter combination to lock") }
        }
            WarningText(
                color = Color.Red,
                text = "WARNING! LOCKING",
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun MainScreen(){
    val viewModel: StateViewModel = viewModel()
    val context = LocalContext.current
    var isServiceRunning by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize()){
        GestureScreen()
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround){
            Button(onClick = {
                if (!viewModel.bufferActive) {
                    // Only toggle if buffer is not active
                    if (!viewModel.settingCombination.value) {
                        viewModel.clearCombination()
                    }
                    viewModel.toggleSettingCombination()
                    val logsetting = viewModel.settingCombination.value
                    Log.i("Setting", "Setting is now $logsetting")
                }
            },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (viewModel.settingCombination.value) Color.Red else Color.Blue
                    )

            ) { Text(text = if (viewModel.settingCombination.value) "SETTING" else "Set Combination") }
            LaunchedEffect(key1 = Unit) { // Execute once when the composable is initialized
                val sharedPrefs = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
                isServiceRunning = sharedPrefs.getBoolean("isServiceRunning", false)
            }
            Button(onClick = {
                val intent = Intent(context, MyForegroundService::class.java)
                if (isServiceRunning) {
                    context.stopService(intent)
                } else {
                    ContextCompat.startForegroundService(context, intent)
                }
                // Update isServiceRunning after starting/stopping the service
                isServiceRunning = !isServiceRunning
            }) {
                Text(if (isServiceRunning) "Stop Service" else "Start Service")
            }
            Button(onClick = {
                if (!viewModel.settingCombination.value) {
                    // Only toggle if buffer is not active
                    viewModel.clearBuffer()
                    viewModel.toggleBufferActive()
                    val logbuffer = viewModel.bufferActive
                    Log.i("Buffer", "Buffer is now $logbuffer")
                }
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
                                Toast
                                    .makeText(context, "Unlocked!", Toast.LENGTH_SHORT)
                                    .show()
                                // TODO: Improve Visuals of Toast
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

@Preview(showBackground = true)
@Composable
fun UnlockScreenPreview() {
    SLAPPTheme { // Apply your app's theme
        UnlockScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun LockScreenPreview() {
    SLAPPTheme { // Apply your app's theme
        LockScreen()
    }
}

class MyForegroundService : Service() {
    // TODO: Implement your foreground service logic here:
    //      - ensure that corner icon pops up whenever screen is touched and app is active
    //      - ensure that touching the corner icon will activate lock screen settings
    //      - when LOCK icon on lock screen setting is pressed: initate override
    //      - when combination is entered: deactivate override
    //      - consult Mike
    //      - avoid conflicts with OS-based lockscreen

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val sharedPrefs = getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        editor.putBoolean("isServiceRunning", true).apply()
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        // Your background logic here
        return START_STICKY
    }
    override fun onBind(intent: Intent?): IBinder? {
        return null}
    // ... other service methods ...

    override fun onDestroy() {
        super.onDestroy()
        val sharedPrefs = getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        editor.putBoolean("isServiceRunning", false).apply()
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