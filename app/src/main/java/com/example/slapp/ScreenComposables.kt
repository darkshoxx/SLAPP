package com.example.slapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.launch
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.unit.sp
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
    // create local context
    val context = LocalContext.current
    val viewModel: StateViewModel = viewModel(viewModelStoreOwner = context as ComponentActivity)
    Box(modifier = Modifier.fillMaxSize()){
        GestureScreen(parentName = "UnlockScreen")
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
                ) { Text(text = if (viewModel.bufferActive) "UNLOCKING" else "Enter combination to unlock") }
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
    val context = LocalContext.current
    val viewModel: StateViewModel = viewModel(viewModelStoreOwner = context as ComponentActivity)

    Box(modifier = Modifier
        .fillMaxSize()
        .testTag("lockScreen")){
        GestureScreen(parentName = "LockScreen")
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
    val context = LocalContext.current
    val viewModel: StateViewModel = viewModel(viewModelStoreOwner = context as ComponentActivity)
    val isServiceBound by viewModel.isServiceBound.collectAsState()
    Box(modifier = Modifier.fillMaxSize()){
        GestureScreen(parentName = "MainScreen")
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
            Button(onClick = { if(!navController.isMock)(navController as RealNavController).navController.navigate("lock") },
                enabled = !navController.isMock,
            modifier = Modifier.testTag("lockButton")) {
                Text("Go to Lock Screen")
            }
            Button(onClick = {
                Log.i("Button", "SHOXX after click before changes Service bound: $isServiceBound")
                if (isServiceBound) {
                    viewModel.stopService(context)
                } else {
                    viewModel.startService(context)
                }

            }) {
                Log.i("Button", "SHOXX after changes: $isServiceBound")
                Text(if (isServiceBound) "Stop Service" else "Start Service")
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
fun GestureScreen(parentName: String = "MainScreen") {
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

    val context = LocalContext.current
    val viewModel: StateViewModel = viewModel(viewModelStoreOwner = context as ComponentActivity)

    val queue = viewModel.queue.collectAsState()
    val lastNumber = viewModel.lastNumber.collectAsState()
//    var showLastNumber by remember { mutableStateOf(false) }
//    
//    if (lastNumber.value != null && !showLastNumber) {
//        showLastNumber = true
//    }


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
                                val lockMessage: String
                                val lockBoolean: Boolean
                                if (parentName == "LockScreen") {
                                    lockMessage = "Successfully LOCKED!"
                                    lockBoolean = true
                                } else {
                                    lockMessage = "Successfully UNLOCKED!"
                                    lockBoolean = false

                                }
                                Log.i("Success", lockMessage)
                                viewModel.clearBuffer()
                                context
                                    .getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
                                    .edit()
                                    .putBoolean("isLocked", lockBoolean)
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
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize(),
                content = {
                    Box(modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ){
                        CenterHexImage()
                        if (lastNumber.value != null) {

                            Text(
                                text = "Last Number: ${lastNumber.value}",
                                fontSize = 48.sp,
                                modifier = Modifier.padding(16.dp).align(Alignment.Center)
                            )

                        }
                    }
                }
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Text(text = "Combo so far: ${queue.value.toString()}", fontSize = 24.sp)
        }
    }
