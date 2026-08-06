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

package com.example.maps3dkotlin

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import com.example.maps3dkotlin.routes.RoutesActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * A premium visual regression test for the View-based Kotlin Routes API sample.
 *
 * This test automates launching the [RoutesActivity], waiting for the asynchronous Routes API v2
 * network fetch to complete, allowing the auto-play engine to animate the 3D vehicle along the path,
 * capturing a screenshot of the live rendering scene, and verifying visual correctness using the Gemini API.
 */
@RunWith(AndroidJUnit4::class)
class RoutesVisualTest : BaseVisualTest() {

    @Test
    fun verifyRoutesRenders() {
        runBlocking {
            // Launch RoutesActivity
            val intent = Intent(context, RoutesActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)

            // Wait for the activity to be displayed in the foreground
            uiDevice.wait(Until.hasObject(By.pkg(context.packageName).depth(0)), 10000)

            // Wait 15 seconds for map tiles to load, route coordinates to fetch, and the vehicle model to start animating
            println("Waiting 15 seconds for map rendering and vehicle animation...")
            delay(15000)

            // Capture high-resolution screenshot of the active 3D map scene
            val screenshotBitmap = captureScreenshot("routes_screenshot.png")

            // Define the verification prompt for the visual testing agent
            val prompt = """
                Please act as a UI tester and analyze this screenshot.
                1. Confirm that a 3D map view is visible.
                2. Confirm that a blue polyline (line) is visible on the map, representing a route.
                3. Confirm that a RED CAR 3D MODEL is clearly visible on or near the blue polyline.
                4. The route should be in Hawaii (Oahu area, coastal/mountainous terrain).

                If and ONLY IF you can clearly see a red car model on or near the blue polyline, reply with "PASSED".
                If you cannot see a red car model, reply with "FAILED: Red car model not visible".
                Report what you see in detail.
            """.trimIndent()

            // Analyze the image using Gemini
            val geminiResponse = helper.analyzeImage(screenshotBitmap, prompt, geminiApiKey)
            println("Gemini's analysis: ${'$'}geminiResponse")

            // Assert on Gemini's response
            assertTrue(
                "Visual verification failed. Gemini response: ${'$'}geminiResponse",
                geminiResponse?.contains("PASSED", ignoreCase = true) == true
            )
        }
    }
}
