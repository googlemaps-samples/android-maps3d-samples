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
import com.example.maps3dkotlin.mapslocalization.MapsLocalizationActivity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tier 4 Visual Regression Test for the Kotlin Views [MapsLocalizationActivity] sample.
 */
@RunWith(AndroidJUnit4::class)
class MapsLocalizationVisualTest : BaseVisualTest() {

    @Test
    fun verifyMapsLocalizationRenders() {
        runBlocking {
            val intent = Intent(context, MapsLocalizationActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)

            uiDevice.wait(Until.hasObject(By.pkg(context.packageName).depth(0)), 10000)
            waitForMapRendering(15)

            val screenshotBitmap = captureScreenshot("maps_localization_screenshot.png")

            val prompt = """
                Please act as a UI tester and analyze this screenshot.
                1. Confirm that a 3D map view is visible in Hybrid or Roadmap mode with labels.
                2. Confirm that the bottom 3D Map Localization Controls card is visible
                   (showing the language selection button and location label below it).

                If and ONLY IF you can clearly see the 3D map scene and the bottom Localization
                Controls card, reply with "PASSED".
                If you cannot see the 3D map scene or control card, reply with
                "FAILED: 3D Map Localization scene or controls not visible".
                Report what you see in detail.
            """.trimIndent()

            val geminiResponse = helper.analyzeImage(screenshotBitmap, prompt, geminiApiKey)
            println("Gemini's analysis: $geminiResponse")

            assertTrue(
                "Visual verification failed. Gemini response: $geminiResponse",
                geminiResponse?.contains("PASSED", ignoreCase = true) == true,
            )
        }
    }
}
