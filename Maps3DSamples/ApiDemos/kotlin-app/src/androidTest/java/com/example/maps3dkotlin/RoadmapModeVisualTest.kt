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
import com.example.maps3dkotlin.roadmapmode.RoadmapModeActivity
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * A premium visual regression test for the View-based Kotlin 3D Roadmap Mode sample.
 *
 * This test automates launching the [RoadmapModeActivity], waiting for 3D Vector Roadmap tiles (featuring 3D building
 * geometry and road network overlays in San Francisco) to initialize and settle, capturing a screenshot of the live
 * rendering scene, and verifying visual correctness using the Gemini API.
 */
@RunWith(AndroidJUnit4::class)
class RoadmapModeVisualTest : BaseVisualTest() {

  @Test
  fun verifyRoadmapModeRenders() {
    runBlocking {
      // Launch RoadmapModeActivity
      val intent = Intent(context, RoadmapModeActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)

      // Wait for the activity to be displayed in the foreground
      uiDevice.wait(Until.hasObject(By.pkg(context.packageName).depth(0)), 10000)

      // Wait 15 seconds for map tiles to render and 3D vector buildings/streets to settle
      println("Waiting 15 seconds for 3D Roadmap mode rendering...")
      delay(15000.milliseconds)

      // Capture high-resolution screenshot of the active 3D map scene
      val screenshotBitmap = captureScreenshot("roadmap_mode_screenshot.png")

      // Define the verification prompt for the visual testing agent
      val prompt = """
                Please act as a UI tester and analyze this screenshot.
                1. Confirm that a 3D map view is visible over San Francisco.
                2. Confirm that the map is rendered in 3D ROADMAP / VECTOR mode (showing street network layouts, road labels, and/or vector 3D building blocks rather than raw satellite imagery only).
                3. Confirm that the Map Mode radio selection card (with Roadmap, Hybrid, and Satellite options) is visible at the bottom of the screen.

                If and ONLY IF you can clearly see the 3D Roadmap map scene and bottom Map Mode selection card, reply with "PASSED".
                If you cannot see the 3D map scene or control card, reply with "FAILED: 3D Roadmap scene or Map Mode controls not visible".
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
