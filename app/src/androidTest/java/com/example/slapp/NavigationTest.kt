//package com.example.slapp
//
//import androidx.test.espresso.Espresso.onView
//import androidx.test.espresso.matcher.ViewMatchers.withTagValue
//import androidx.test.ext.junit.rules.ActivityScenarioRule
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import org.junit.Rule
//import org.junit.Test
//import org.junit.runner.RunWith
//import org.hamcrest.Matchers.is
//
//@RunWith(AndroidJUnit4::class)
//class NavigationTest {
//
//    @get:Rule
//    var activityRule = ActivityScenarioRule<MainActivity>(MainActivity::class.java)
//
//    @Test
//    fun navigateToLockScreen(){
//        // write the test such that clicking the button with the test tag "lockButton" navigates to the screen composable with the test tag "lockScreen"
//        onView(withTagValue(is("lockButton"))).perform(click())
//        onView(withTagValue(is("lockScreen"))).check(matches(isDisplayed()))
//
//
////        onView(withId(R.id.lockScreen)).check(matches(isDisplayed()))
//
//    }
//}