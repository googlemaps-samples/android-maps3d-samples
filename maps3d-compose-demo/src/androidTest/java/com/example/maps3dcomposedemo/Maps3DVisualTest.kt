/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.maps3dcomposedemo

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class Maps3DVisualTest : BaseVisualTest() {

    @Before
    fun setup() {
        // Launch the app
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        context.startActivity(intent)
        uiDevice.wait(Until.hasObject(By.pkg(context.packageName).depth(0)), 10000)
    }

    @Test
    fun verifyCatalogListRenders() = runBlocking {
        // Wait for the list to appear
        val found = uiDevice.wait(Until.hasObject(By.text("Maps 3D Compose Samples")), 5000)
        assertTrue("Catalog list title not found", found)

        // Capture a screenshot
        val screenshotBitmap = captureScreenshot("catalog_list_screenshot.png")

        // Define the verification prompt for Gemini
        val prompt = """
            Please act as a UI tester and analyze this screenshot to verify the application is rendering correctly. 
            Check the image against the following criteria:
            1. Confirm that the title 'Maps 3D Compose Samples' is visible.
            2. Confirm that there are multiple list items visible (e.g., "Basic Map with Marker & Polyline", "Hello Map", etc.).
            
            If all elements are present and look reasonable for a list of samples, reply with "PASSED". 
            If any element is missing or incorrect, please detail the discrepancy.
        """.trimIndent()

        // Analyze the image using Gemini
        val geminiResponse = helper.analyzeImage(screenshotBitmap, prompt, geminiApiKey)

        println("Gemini's analysis: $geminiResponse")

        // Assert on Gemini's response
        assertTrue(
            "Visual verification failed. Gemini response: $geminiResponse",
            geminiResponse?.contains("PASSED", ignoreCase = true) == true
        )
    }

    @Test
    fun verifyHelloMapRenders() = runBlocking {
        // Launch HelloMapActivity
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = Intent(context, HelloMapActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        
        // Wait for the activity to be displayed
        uiDevice.wait(Until.hasObject(By.pkg(context.packageName).depth(0)), 10000)

        // Wait for the map to render and tiles to load (up to 120 seconds for 3D SDK)
        waitForMapRendering(120)

        // Capture a screenshot
        val screenshotBitmap = captureScreenshot("hello_map_screenshot.png")

        // Define the verification prompt for Gemini
        val prompt = """
            Please act as a UI tester and analyze this screenshot to verify the application is rendering correctly. 
            Check the image against the following criteria:
            1. Confirm that a 3D map view is visible.
            2. Confirm that the Delicate Arch itself is clearly visible and a prominent part of the scene (it should look like a large freestanding rock arch).
            
            If all elements are present and the Delicate Arch is clearly visible, reply with "PASSED". 
            If any element is missing or incorrect, please detail the discrepancy.
        """.trimIndent()

        // Analyze the image using Gemini
        val geminiResponse = helper.analyzeImage(screenshotBitmap, prompt, geminiApiKey)

        println("Gemini's analysis: $geminiResponse")

        // Assert on Gemini's response
        assertTrue(
            "Visual verification failed. Gemini response: $geminiResponse",
            geminiResponse?.contains("PASSED", ignoreCase = true) == true
        )
    }

    @org.junit.Test
    fun verifyCameraControlsRenders() = kotlinx.coroutines.runBlocking {
        // Launch CameraControlsActivity directly
        val intent = Intent(context, CameraControlsActivity::class.java).apply {
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        
        // Wait for the activity to be displayed
        uiDevice.wait(androidx.test.uiautomator.Until.hasObject(androidx.test.uiautomator.By.pkg(context.packageName).depth(0)), 10000)
        
        // Wait for the map to render and tiles to load
        waitForMapRendering(60)

        // Capture a screenshot
        val screenshotBitmap = captureScreenshot("camera_controls_screenshot.png")

        // Define the verification prompt for Gemini
        val prompt = """
            Please act as a UI tester and analyze this screenshot to verify the application is rendering correctly. 
            Check the image against the following criteria:
            1. Confirm that a 3D map view is visible.
            2. Confirm that the Space Needle or surrounding Seattle urban area (like stadiums/arenas) is visible and prominent.
            
            If all elements are present and look reasonable for a 3D map of Seattle, reply with "PASSED". 
            If any element is missing or incorrect, please detail the discrepancy.
        """.trimIndent()

        // Analyze the image using Gemini
        val geminiResponse = helper.analyzeImage(screenshotBitmap, prompt, geminiApiKey)

        println("Gemini's analysis: $geminiResponse")

        // Assert on Gemini's response
        org.junit.Assert.assertTrue(
            "Visual verification failed. Gemini response: $geminiResponse",
            geminiResponse?.contains("PASSED", ignoreCase = true) == true
        )
    }

    @org.junit.Test
    fun verifyMapInteractionsRenders() = kotlinx.coroutines.runBlocking {
        // Launch MapInteractionsActivity directly
        val intent = android.content.Intent(context, MapInteractionsActivity::class.java).apply {
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        
        // Wait for the activity to be displayed
        uiDevice.wait(androidx.test.uiautomator.Until.hasObject(androidx.test.uiautomator.By.pkg(context.packageName).depth(0)), 10000)
        
        // Wait for the map to render and tiles to load
        waitForMapRendering(60)

        // Strategy 1: Click the center of the screen and surrounding points
        val screenWidth = uiDevice.displayWidth
        val screenHeight = uiDevice.displayHeight
        
        val centerX = screenWidth / 2
        val centerY = screenHeight / 2
        
        // Click center and a few points around it to increase chances
        uiDevice.click(centerX, centerY)
        android.os.SystemClock.sleep(500)
        uiDevice.click(centerX + 50, centerY + 50)
        android.os.SystemClock.sleep(500)
        uiDevice.click(centerX - 50, centerY - 50)
        android.os.SystemClock.sleep(500)
        uiDevice.click(centerX + 50, centerY - 50)
        android.os.SystemClock.sleep(500)
        uiDevice.click(centerX - 50, centerY + 50)

        // Wait for the click info card to update (let Gemini handle verification if UiAutomator misses it)
        uiDevice.wait(
            androidx.test.uiautomator.Until.hasObject(androidx.test.uiautomator.By.textContains("Clicked")),
            5000
        )

        // Capture a screenshot
        val screenshotBitmap = captureScreenshot("map_interactions_screenshot.png")

        // Define the verification prompt for Gemini
        val prompt = """
            Please act as a UI tester and analyze this screenshot.
            1. Confirm that a 3D map view is visible.
            2. Read the text in the card at the bottom of the screen and report exactly what it says.
            3. Confirm if it shows clicked coordinates or place ID (i.e., does it contain the word 'Clicked').
            
            If the map is visible and the card shows clicked info, reply with "PASSED". 
            Otherwise, report what you see.
        """.trimIndent()

        // Analyze the image using Gemini
        val geminiResponse = helper.analyzeImage(screenshotBitmap, prompt, geminiApiKey)

        println("Gemini's analysis: $geminiResponse")

        // Assert on Gemini's response
        org.junit.Assert.assertTrue(
            "Visual verification failed. Gemini response: $geminiResponse",
            geminiResponse?.contains("PASSED", ignoreCase = true) == true
        )
    }
}
