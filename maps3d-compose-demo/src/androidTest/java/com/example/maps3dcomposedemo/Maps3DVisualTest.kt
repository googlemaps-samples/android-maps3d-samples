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

        // Wait for the map to render and tiles to load (up to 60 seconds for 3D SDK)
        waitForMapRendering(60)

        // Capture a screenshot
        val screenshotBitmap = captureScreenshot("hello_map_screenshot.png")

        // Define the verification prompt for Gemini
        val prompt = """
            Please act as a UI tester and analyze this screenshot to verify the application is rendering correctly. 
            Check the image against the following criteria:
            1. Confirm that a 3D map view is visible.
            2. Confirm that the landscape appears to be a mountain range (Flatirons in Boulder).
            
            If all elements are present and look reasonable for a 3D map of a mountain area, reply with "PASSED". 
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
}
