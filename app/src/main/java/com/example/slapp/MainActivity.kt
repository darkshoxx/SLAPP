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
import android.view.ContextThemeWrapper
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlin.io.path.name

interface NavigationController {
    val isMock: Boolean
}


class MockNavController(context: Context, override val isMock: Boolean = true) : NavHostController(context), NavigationController{
  //do nothing
}

class RealNavController(val navController: NavHostController) : NavigationController {
    override val isMock: Boolean = false
}


class NavigationControllerHolder{
    lateinit var navController: NavigationController
    companion object {
        private var instance: NavigationControllerHolder? = null
        fun getInstance(): NavigationControllerHolder {
            if (instance == null) {
                instance = NavigationControllerHolder()
            }
            return instance!!
        }
    }
}

@Composable
fun rememberIsServiceLocked(): MutableState<Boolean> {
    return remember { mutableStateOf(false) }
}

@Composable
fun AppNavigation(navController: NavigationController, isServiceLocked: MutableState<Boolean>) {
    val realNavController = when (navController) {
        is RealNavController -> navController.navController
        is MockNavController -> rememberNavController() // Use a default navController for MockNavController
        else -> rememberNavController() // Handle other cases if needed
    }

    NavHost(navController = realNavController, startDestination = "main") {
        composable("main") { MainScreen(navController) } // Pass the original navController
        composable("lock") { LockScreen(navController, isServiceLocked) } // Pass the original navController
        composable("unlock") { UnlockScreen(navController, isServiceLocked) } // Pass the original navController
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val navigationControllerHolder = NavigationControllerHolder()
        setContent {
            SLAPPTheme {
                val navController = rememberNavController()
                navigationControllerHolder.navController = RealNavController(navController)
                val isServiceLocked = rememberIsServiceLocked()
                AppNavigation(navigationControllerHolder.navController, isServiceLocked)
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                   MainScreen(navigationControllerHolder.navController)
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
fun UnlockScreen(navController: NavigationController, isServiceLocked: MutableState<Boolean>){
    val viewModel: StateViewModel = viewModel()
    // create local context
//    var isServiceLocked by remember { mutableStateOf(false) }
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
                        if(!navController.isMock)(navController as RealNavController).navController.navigate("main")
                              },
                    enabled = !navController.isMock){

                    Text("Go to Main Screen")
                }
                Switch(
                    checked = isServiceLocked.value,
                    onCheckedChange = { isChecked ->
                        isServiceLocked.value = isChecked
                        // Update the shared preference and the service's isLocked property
                        context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
                            .edit()
                            .putBoolean("isLocked", isChecked)
                            .apply()
                        // Assuming you have a way to communicate with the service (e.g., using a bound service or broadcast receiver)
                        // Update the service's isLocked property here
                    }
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
fun LockScreen(navController: NavigationController, isServiceLocked: MutableState<Boolean>){
    val viewModel: StateViewModel = viewModel()
    // create local context
    val context = LocalContext.current

//    var isServiceLocked by remember { mutableStateOf(false) }
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
            Button(
                onClick = {if(!navController.isMock)(navController as RealNavController).navController.navigate("unlock")},
                enabled = !navController.isMock)
            {
                Text("Go to Unlock Screen")
            }
            Switch(
                checked = isServiceLocked.value,
                onCheckedChange = { isChecked ->
                    isServiceLocked.value = isChecked
                    // Update the shared preference and the service's isLocked property
                    context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
                        .edit()
                        .putBoolean("isLocked", isChecked)
                        .apply()
                    // Assuming you have a way to communicate with the service (e.g., using a bound service or broadcast receiver)
                    // Update the service's isLocked property here
                }
            )
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
fun MainScreen(navController: NavigationController){
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
            Button(onClick = { if(!navController.isMock)(navController as RealNavController).navController.navigate("lock") },
                enabled = !navController.isMock) {
                Text("Go to Lock Screen")
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

@Composable
fun MainActivityContent() {
    val context = LocalContext.current
    var isServiceLocked = remember { mutableStateOf(
        context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
            .getBoolean("isLocked", false)
    ) }

    SLAPPTheme {
        val navController = NavigationControllerHolder.getInstance().navController //
        AppNavigation(navController, isServiceLocked)
    }
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
fun MainActivityContentPreview() {
    val context: Context = LocalContext.current
    NavigationControllerHolder.getInstance().navController = RealNavController(rememberNavController())
    MainActivityContent()
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    val context = LocalContext.current
    SLAPPTheme { // Apply your app's theme
        MainScreen(MockNavController(context))
    }
}

@Preview(showBackground = true)
@Composable
fun UnlockScreenPreview() {
    val context = LocalContext.current
    val isServiceLocked = remember { mutableStateOf(false) }
    SLAPPTheme { // Apply your app's theme
        UnlockScreen(MockNavController(context), isServiceLocked)
    }
}

@Preview(showBackground = true)
@Composable
fun LockScreenPreview() {
    val context = LocalContext.current
    val isServiceLocked = remember { mutableStateOf(false) }
    SLAPPTheme { // Apply your app's theme
        LockScreen(MockNavController(context), isServiceLocked)
    }
}

class MyForegroundService : Service() {
    private val sharedPrefs by lazy { getSharedPreferences("my_prefs", Context.MODE_PRIVATE) }
    var isLocked: Boolean
        get() = sharedPrefs.getBoolean("isLocked", false) // Default to unlocked mode
        set(value) {
            sharedPrefs.edit().putBoolean("isLocked", value).apply()
        }
    // TODO: Implement your foreground service logic here:
    //      - ensure that corner icon pops up whenever screen is touched and app is active
    //      - ensure that touching the corner icon will activate lock screen settings
    //      - when LOCK icon on lock screen setting is pressed: initiate override
    //      - when combination is entered: deactivate override
    //      - consult Mike
    //      - avoid conflicts with OS-based lockscreen
    //      - timeout override as security measure
    //      - sound override as security measure
    //      - volume buttons override list by Gemini
    //      - list generated by Gemini:
    //              1. create mechanism to toggle between locked and unlocked
    //              2. Implement way to capture screen tap events
    //              3. In accessibility service, check current mode
    //              4. if unlocked business logic
    //              5. if locked business logic
    //              6. Update UI to reflect mode
    //              7. Continue Testing
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