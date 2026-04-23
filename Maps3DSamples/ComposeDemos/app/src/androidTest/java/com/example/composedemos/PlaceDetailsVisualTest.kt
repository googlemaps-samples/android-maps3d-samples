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
import com.example.composedemos.placedetails.PlaceDetailsActivity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlaceDetailsVisualTest : BaseVisualTest() {

    @Test
    fun verifyPlaceDetailsLoads() {
        runBlocking {
            // Launch PlaceDetailsActivity
            val intent = Intent(context, PlaceDetailsActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)

            // Wait for the activity to be displayed
            uiDevice.wait(Until.hasObject(By.pkg(context.packageName).depth(0)), 10000)

            // 1. Wait for the map to load (using fixed delay as steady callback can be flaky)
            println("Waiting 20 seconds for map to load...")
            kotlinx.coroutines.delay(20000)
            
            // 2. Find and click "The Flatirons" in the list of landmarks
            val flatironsItem = uiDevice.wait(Until.hasObject(By.text("The Flatirons")), 5000)
            assertTrue("The Flatirons item not found in list", flatironsItem)
            uiDevice.findObject(By.text("The Flatirons")).click()
            
            // 3. Wait for the Place Details fragment to load content (network call)
            println("Waiting for Place Details to load...")
            kotlinx.coroutines.delay(15000)
            
            // 4. Capture screenshot
            val screenshotBitmap = captureScreenshot("place_details_screenshot.png")
            kotlinx.coroutines.delay(2000) // Wait for file to be fully written
            
            // 5. Verify with Gemini - Stricter Prompt!
            val prompt = """
                The screen should show a 3D map in the background.
                At the bottom, there should be a sheet or card containing Place Details.
                You MUST verify that the text "The Flatirons" is clearly visible in the Place Details card.
                If you can see the text "The Flatirons" in the card, reply with YES.
                If the card is missing, blank, still loading, or shows a different place, reply with NO.
            """.trimIndent()
            
            val geminiResponse = helper.analyzeImage(screenshotBitmap, prompt, geminiApiKey)
            println("Gemini result: ${geminiResponse?.trim()}")
            assertTrue("Gemini verification failed: $geminiResponse", geminiResponse?.trim()?.contains("YES", ignoreCase = true) == true)
        }
    }
}
