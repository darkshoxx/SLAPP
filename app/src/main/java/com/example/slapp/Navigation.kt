package com.example.slapp

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


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