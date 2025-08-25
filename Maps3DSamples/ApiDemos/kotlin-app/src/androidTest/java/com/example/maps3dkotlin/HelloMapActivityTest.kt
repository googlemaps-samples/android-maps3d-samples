package com.example.maps3dkotlin

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.maps3dcommon.R
import com.example.maps3dkotlin.hellomap.HelloMapActivity
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HelloMapActivityTest {
    @Test
    fun testMapIsDisplayed() {
        ActivityScenario.launch(HelloMapActivity::class.java)
        onView(withId(R.id.map3dView)).check(matches(isDisplayed()))
    }
}