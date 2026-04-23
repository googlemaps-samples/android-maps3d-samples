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
import com.example.composedemos.routes.RoutesActivity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoutesVisualTest : BaseVisualTest() {

    @Test
    fun verifyRoutesRenders() {
        runBlocking {
            // Fake shared prefs to avoid security warning dialog
            val sharedPrefs = context.getSharedPreferences("route_prefs", android.content.Context.MODE_PRIVATE)
            sharedPrefs.edit().putInt("warning_count", 2).commit()

            // Launch RoutesActivity
            val intent = Intent(context, RoutesActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)

            // Wait for the activity to be displayed
            uiDevice.wait(Until.hasObject(By.pkg(context.packageName).depth(0)), 10000)

            // Wait for map to load tiles and network call to complete
            println("Waiting 20 seconds for route to load...")
            kotlinx.coroutines.delay(20000)

            // 1. Toggle tracker style to Red Car (starts at Marker, click once for Red Car)
            val toggleButton = uiDevice.wait(Until.hasObject(By.desc("Toggle Tracker Style")), 5000)
            assertTrue("Toggle tracker button not found", toggleButton)
            uiDevice.findObject(By.desc("Toggle Tracker Style")).click()
            kotlinx.coroutines.delay(1000)

            // 2. Click "Fly Along" button to enter fly mode and show slider
            val flyButton = uiDevice.wait(Until.hasObject(By.text("Fly Along")), 5000)
            assertTrue("Fly Along button not found", flyButton)
            uiDevice.findObject(By.text("Fly Along")).click()
            kotlinx.coroutines.delay(2000) // Wait for slider to appear

            // 3. Set progress to halfway point by clicking center of the slider
            val slider = uiDevice.wait(Until.hasObject(By.desc("Progress Slider")), 5000)
            assertTrue("Progress slider not found", slider)
            val sliderObj = uiDevice.findObject(By.desc("Progress Slider"))
            val bounds = sliderObj.visibleBounds
            uiDevice.click(bounds.centerX(), bounds.centerY())
            kotlinx.coroutines.delay(2000) // Wait for camera to jump to position

            // Capture a screenshot for the catalog showing the red car at halfway point!
            val screenshotBitmap = captureScreenshot("routes_screenshot.png")

            // Define the verification prompt for Gemini
            val prompt = """
                Please act as a UI tester and analyze this screenshot.
                1. Confirm that a 3D map view is visible.
                2. Confirm that a blue polyline (line) is visible on the map, representing a route.
                3. Confirm that a RED CAR 3D MODEL is clearly visible on or near the blue polyline.
                4. The route should be in Hawaii (Oahu area).

                If and ONLY IF you can clearly see a red car model on or near the blue polyline, reply with "PASSED".
                If you cannot see a red car model, reply with "FAILED: Red car model not visible".
                Report what you see in detail.
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
