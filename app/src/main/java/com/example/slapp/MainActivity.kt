package com.example.slapp


import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.slapp.ui.theme.SLAPPTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController

// TODO: Implement your foreground service logic here:
//      - current bugs:
//          - combinations and interactions of buttons in main/lock/unlockscreen erroneous.
//      - ensure that corner icon pops up whenever screen is touched and app is active
//      - ensure that touching the corner icon will activate lock screen settings
//      - when LOCK icon on lock screen setting is pressed: initiate override
//      - when combination is entered: deactivate override
//      - consult Mike
//      - avoid conflicts with OS-based lockscreen
//      - volume buttons override list by Gemini
//              1. Create BroadcastReceiver that listens for button presses
//              2. Check booleans within BroadcastReceiver
//              3. update state of the combination in SharedPreferences
//              4. in UnlockScreen, check state of combination in SharedPreferences if code correct
//      - list generated by Gemini:
//              1. create mechanism to toggle between locked and unlocked
//              2. Implement way to capture screen tap events
//              3. In accessibility service, check current mode
//              4. if unlocked business logic
//              5. if locked business logic
//              6. Update UI to reflect mode
//              7. Continue Testing

// DONE:
// - Timing override
// - Sound override

@Composable
fun rememberIsServiceLocked(): MutableState<Boolean> {
    return remember { mutableStateOf(false) }
}


class MainActivity : ComponentActivity() {
    private lateinit var viewModel: StateViewModel
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var sharedPreferences: SharedPreferences
    private var isServiceBound = false
    private var foregroundService: ForegroundService? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as ForegroundService.LocalBinder
            foregroundService = binder.getService()
            isServiceBound = true
            viewModel.setServiceBound(true)
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isServiceBound = false
            foregroundService = null

            viewModel.setServiceBound(false)
            // Update UI or other logic here if needed
            // You now know the service is not bound
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(StateViewModel::class.java)
        viewModel.addToCombination(1)
        viewModel.addToCombination(2)
        viewModel.addToCombination(3)
        viewModel.addToCombination(4)
        sharedPreferences = getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        val mode = intent.getStringExtra("mode") ?: "production"

        initializeAppConfig(mode)

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                setupApp()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
        requestAudioPermission()
    }

    private fun initializeAppConfig(mode:String){
        val timeoutSeconds: Int
        val frequencyTolerance: Float
        val frequencyHeld: Int

        when (mode) {
            "production" -> {
                timeoutSeconds = 3600
                frequencyTolerance = 0.1f
                frequencyHeld = 10
            }
            "emulator" -> {
                timeoutSeconds = 15
                frequencyTolerance = 10f
                frequencyHeld = 5
            }
            "test" -> {
                timeoutSeconds = 1
                frequencyTolerance = 0.0001f
                frequencyHeld = 1
            }
            else -> {
                timeoutSeconds = 15
                frequencyTolerance = 10f
                frequencyHeld = 5
                Log.w("MainActivity", "Unknown mode: $mode. Using emulator settings.")
            }
        }
        AppConfig.setConfig(timeoutSeconds, frequencyTolerance, frequencyHeld)
        sharedPreferences.edit()
            .putInt("timeoutSeconds", timeoutSeconds)
            .putFloat("frequencyTolerance", frequencyTolerance)
            .putString("mode", mode)
            .apply()
    }


    private fun requestAudioPermission(){
        when {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                setupApp()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.RECORD_AUDIO) -> {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
            else -> {
                requestPermissionLauncher.launch(
                    android.Manifest.permission.RECORD_AUDIO)
                }
        }
    }


    private fun setupApp() {
        setContent {
            SLAPPTheme {
                val navController = rememberNavController()
                val realNavController = RealNavController(navController)
                val isServiceLocked = rememberIsServiceLocked()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(realNavController, isServiceLocked)
                }
            }
        }
    }



    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
        if (viewModel.isServiceBound.value){
            viewModel.stopService(this)
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


