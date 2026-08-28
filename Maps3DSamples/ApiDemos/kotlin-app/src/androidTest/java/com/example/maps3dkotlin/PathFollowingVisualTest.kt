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
import com.example.maps3dkotlin.pathfollowing.PathFollowingActivity
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * A premium visual regression test for the View-based Kotlin Path Following sample.
 *
 * This test automates launching the [PathFollowingActivity], waiting for the 3D map scene, polyline route,
 * and ground-level camera navigation engine to initialize, capturing a screenshot of the live rendering scene,
 * and verifying visual correctness using the Gemini API.
 */
@RunWith(AndroidJUnit4::class)
class PathFollowingVisualTest : BaseVisualTest() {

  @Test
  fun verifyPathFollowingRenders() {
    runBlocking {
      // Launch PathFollowingActivity
      val intent = Intent(context, PathFollowingActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)

      // Wait for the activity to be displayed in the foreground
      uiDevice.wait(Until.hasObject(By.pkg(context.packageName).depth(0)), 10000)

      // Wait for map tiles to load, ground polyline to render, and path animation to initialize
      waitForMapRendering(15)

      // Capture high-resolution screenshot of the active 3D map scene
      val screenshotBitmap = captureScreenshot("path_following_screenshot.png")

      // Define the verification prompt for the visual testing agent
      val prompt = """
                Please act as a UI tester and analyze this screenshot.
                1. Confirm that a 3D map view is visible.
                2. Confirm that a BLUE POLYLINE (line) is clearly visible on the map, representing the navigation path.
                3. Confirm that interactive camera control sliders (Range, Altitude, Heading, Tilt, Speed) and an Urban/Rural selector panel are visible at the bottom of the screen.

                If and ONLY IF you can clearly see the 3D map view with the blue route polyline and bottom control card, reply with "PASSED".
                If you cannot see the blue route polyline or control panel, reply with "FAILED: Blue path polyline or UI controls not visible".
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
