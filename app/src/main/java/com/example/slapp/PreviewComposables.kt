package com.example.slapp

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.slapp.ui.theme.SLAPPTheme


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
