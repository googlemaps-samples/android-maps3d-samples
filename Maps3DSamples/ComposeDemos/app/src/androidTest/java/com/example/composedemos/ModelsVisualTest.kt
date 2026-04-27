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
import com.example.composedemos.models.ModelsActivity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ModelsVisualTest : BaseVisualTest() {

    @Test
    fun verifyModelsRenders() {
        runBlocking {
            // Launch ModelsActivity
            val intent = Intent(context, ModelsActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)

            // Wait for the activity to be displayed
            uiDevice.wait(Until.hasObject(By.pkg(context.packageName).depth(0)), 10000)

            // Wait for the map to render and tiles to load
            waitForMapRendering(60)

            // Capture a screenshot
            val screenshotBitmap = captureScreenshot("models_screenshot.png")

            // Define the verification prompt for Gemini
            val prompt = """
                Please act as a UI tester and analyze this screenshot.
                1. Confirm that a 3D map view is visible.
                2. Confirm that a 3D model of an airplane is visible on the map.

                If the map is visible and the airplane model is seen, reply with "PASSED".
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
