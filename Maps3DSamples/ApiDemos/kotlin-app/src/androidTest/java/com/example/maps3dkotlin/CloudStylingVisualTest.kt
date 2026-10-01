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
import com.example.maps3dkotlin.cloudstyling.CloudStylingActivity
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * A premium visual regression test for the View-based Kotlin Cloud-Based Map Styling sample.
 *
 * This test automates launching the [CloudStylingActivity], waiting for cloud-styled 3D map tiles configured via custom
 * Map ID over San Francisco to initialize, capturing a screenshot of the live rendering scene, and verifying visual
 * correctness using the Gemini API.
 */
@RunWith(AndroidJUnit4::class)
class CloudStylingVisualTest : BaseVisualTest() {

  @Test
  fun verifyCloudStylingRenders() {
    runBlocking {
      // Launch CloudStylingActivity
      val intent = Intent(context, CloudStylingActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)

      // Wait for the activity to be displayed in the foreground
      uiDevice.wait(Until.hasObject(By.pkg(context.packageName).depth(0)), 10000)

      // Wait for Cloud-styled 3D map tiles to render and feature styling to settle
      waitForMapRendering(15)

      // Capture high-resolution screenshot of the active 3D map scene
      val screenshotBitmap = captureScreenshot("cloud_styling_screenshot.png")

      // Define the verification prompt for the visual testing agent
      val prompt = """
                Please act as a UI tester and analyze this screenshot.
                1. Confirm that a 3D map view is visible over San Francisco.
                2. Confirm that custom cloud-based stylized map tiles / elements are displayed on the 3D map scene.
                3. Confirm that the Map Mode selector card (with radio options for Roadmap, Hybrid, and Satellite) is visible at the bottom of the screen.

                If and ONLY IF you can clearly see the 3D Cloud-styled map view and bottom Map Mode selection card, reply with "PASSED".
                If you cannot see the 3D map scene or control card, reply with "FAILED: 3D map scene or Map Mode controls not visible".
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
