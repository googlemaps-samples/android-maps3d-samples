package com.example.snippets.kotlin

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun mainActivityLaunches() {
        // Check if "Map Initialization" is displayed (substring because of description)
        composeTestRule.waitForIdle()
        try {
            composeTestRule.onNodeWithText("Map Initialization", substring = true).assertExists()
        } catch (e: AssertionError) {
            println("Hierarchy check failed. Printing tree:")
            composeTestRule.onRoot().printToLog("HierarchyTree")
            throw e
        }
    }
}
