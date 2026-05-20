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

package com.example.composedemos

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import com.example.composedemos.camerarestrictions.CameraRestrictionsActivity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CameraRestrictionsVisualTest : BaseVisualTest() {

    @Test
    fun verifyCameraRestrictionsRenders() {
        runBlocking {
            // Launch CameraRestrictionsActivity
            val intent = Intent(context, CameraRestrictionsActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)

            // Wait for the activity to be displayed
            uiDevice.wait(Until.hasObject(By.pkg(context.packageName).depth(0)), 10000)

            // Wait for the map to render and tiles to load
            waitForMapRendering(60)

            // Attempt to swipe/pan away from the center
            val screenWidth = uiDevice.displayWidth
            val screenHeight = uiDevice.displayHeight
            uiDevice.swipe(screenWidth / 2, screenHeight / 2, 100, screenHeight / 2, 10) // Swipe left

            // Wait a bit for any correction or settling
            kotlinx.coroutines.delay(2000)

            // Capture a screenshot
            val screenshotBitmap = captureScreenshot("camera_restrictions_screenshot.png")

            // Define the verification prompt for Gemini
            // We ask if the Space Needle is still visible, implying the restriction worked.
            val prompt = """
                Please act as a UI tester and analyze this screenshot.
                1. Confirm that a 3D map view is visible.
                2. Confirm that the Space Needle (or the immediate surrounding area) is still visible and central in the view, despite an attempt to pan away.

                If the map is visible and the Space Needle area is still centered, reply with "PASSED".
                Otherwise, report what you see.
            """.trimIndent()

            // Analyze the image using Gemini
            val geminiResponse = helper.analyzeImage(screenshotBitmap, prompt, geminiApiKey)
            println("Gemini's analysis: $geminiResponse")

            // Assert on Gemini's response
            assertTrue(
                "Visual verification failed. Gemini response: $geminiResponse",
                geminiResponse?.contains("PASSED", ignoreCase = true) == true,
            )
        }
    }
}
