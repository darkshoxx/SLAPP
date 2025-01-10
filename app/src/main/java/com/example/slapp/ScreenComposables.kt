package com.example.slapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.activity.result.launch
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel



@Composable
fun LockSwitch(isServiceLocked: MutableState<Boolean>){
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
    var isLockedFromPrefs by remember {
        mutableStateOf(sharedPrefs.getBoolean("isLocked", false))
    }
    val listener = remember {
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "isLocked") {
                isLockedFromPrefs = sharedPrefs.getBoolean("isLocked", false)
            }
        }
    }
    DisposableEffect(sharedPrefs) {
        sharedPrefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            sharedPrefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    Switch(
        checked = isLockedFromPrefs,
        onCheckedChange = { isChecked ->
            Log.i("Switch", "Switch Triggered")
            sharedPrefs.edit()
                .putBoolean("isLocked", isChecked)
                .apply()
            isServiceLocked.value = isChecked
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
                LockSwitch(isServiceLocked)
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
    val context = LocalContext.current

    Box(modifier = Modifier
        .fillMaxSize()
        .testTag("lockScreen")){
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
                LockSwitch(isServiceLocked)
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
                enabled = !navController.isMock,
            modifier = Modifier.testTag("lockButton")) {
                Text("Go to Lock Screen")
            }
            Button(onClick = {
                val intent = Intent(context, ForegroundService::class.java)
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
                                context
                                    .getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
                                    .edit()
                                    .putBoolean("isLocked", false)
                                    .apply()
                                Log.i("ShaPref", "Shared Preferences updated")
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