package com.example.slapp

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertIsDisplayed
import androidx.navigation.compose.rememberNavController
import com.example.slapp.ui.theme.SLAPPTheme
import org.junit.Rule
import org.junit.Test



class NavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun navigateToLockScreen() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            val realNavController = RealNavController(navController)
            SLAPPTheme {
                AppNavigation(
                    navController = realNavController,
                    isServiceLocked = remember { mutableStateOf(false) })
            }
        }
            composeTestRule.onNodeWithTag("lockButton").performClick()
            composeTestRule.onNodeWithTag("lockScreen").assertIsDisplayed()
        }
    }