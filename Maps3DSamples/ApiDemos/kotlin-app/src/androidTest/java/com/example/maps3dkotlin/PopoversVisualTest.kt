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
import com.example.maps3dkotlin.popovers.PopoversActivity
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Visual test for Popovers sample in Kotlin app.
 */
@RunWith(AndroidJUnit4::class)
class PopoversVisualTest : BaseVisualTest() {

    @Test
    fun verifyPopoversRenders() {
        runBlocking {
            // Launch PopoversActivity
            val intent = Intent(context, PopoversActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)

            // Wait for the activity to be displayed
            uiDevice.wait(Until.hasObject(By.pkg(context.packageName).depth(0)), 10000)

            // Wait for the map to render and tiles to load
            waitForMapRendering(60)

            // Capture a screenshot to find the marker
            val searchScreenshot = captureScreenshot("popovers_search.png")

            // Define the prompt for Gemini to find coordinates
            val promptFind = """
                Analyze this screenshot of a 3D map.
                You should see a marker or label with the text "Golden Gate Bridge".
                Find that marker or label.
                Return its center coordinates as a JSON object: {"x": <float>, "y": <float>} where x and y are normalized coordinates between 0.0 and 1.0 (0.0 is top/left, 1.0 is bottom/right).
                Return ONLY the JSON object, nothing else.
            """.trimIndent()

            // Analyze the image using Gemini
            val geminiResponse = helper.analyzeImage(searchScreenshot, promptFind, geminiApiKey)
            println("Gemini's coordinate response: ${'$'}geminiResponse")

            // Parse JSON and click
            try {
                val jsonStr = geminiResponse?.substringAfter("{")?.substringBeforeLast("}")?.let { "{" + it + "}" } ?: ""
                val json = JSONObject(jsonStr)
                val x = json.getDouble("x")
                val y = json.getDouble("y")

                val clickX = (x * uiDevice.displayWidth).toInt()
                val clickY = (y * uiDevice.displayHeight).toInt()

                println("Clicking at (${'$'}clickX, ${'$'}clickY) based on Gemini response")
                uiDevice.click(clickX, clickY)
            } catch (e: Exception) {
                fail("Failed to parse coordinates from Gemini response: ${'$'}geminiResponse. Error: ${'$'}{e.message}")
            }

            // Wait for the popover text to appear
            val textFound = uiDevice.wait(
                Until.hasObject(By.text("The Golden Gate Bridge")),
                15000
            )
            assertTrue("Popover text not found", textFound == true)

            // Capture a screenshot for visual confirmation
            captureScreenshot("popovers_screenshot.png")
        }
    }
}
