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
import com.example.composedemos.mapinteractions.MapInteractionsActivity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MapInteractionsVisualTest : BaseVisualTest() {

    @Test
    fun verifyMapInteractionsRenders() {
        runBlocking {
            // Launch MapInteractionsActivity directly
            val intent = Intent(context, MapInteractionsActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)

            // Wait for the activity to be displayed
            uiDevice.wait(Until.hasObject(By.pkg(context.packageName).depth(0)), 10000)

            // Wait for the map to render and tiles to load
            waitForMapRendering(60)

            // Wait a bit to ensure map is interactive
            println("Waiting 5 seconds for map to be interactive...")
            kotlinx.coroutines.delay(5000)

            // Strategy: Click the center of the screen and surrounding points
            val screenWidth = uiDevice.displayWidth
            val screenHeight = uiDevice.displayHeight

            val centerX = screenWidth / 2
            val centerY = screenHeight / 2

            println("Clicking center and surrounding points...")
            uiDevice.click(centerX, centerY)
            kotlinx.coroutines.delay(500)
            uiDevice.click(centerX + 50, centerY + 50)
            kotlinx.coroutines.delay(500)
            uiDevice.click(centerX - 50, centerY - 50)
            kotlinx.coroutines.delay(500)
            uiDevice.click(centerX + 50, centerY - 50)
            kotlinx.coroutines.delay(500)
            uiDevice.click(centerX - 50, centerY + 50)

            // Wait for the click info card to update with text containing "Clicked"
            val textUpdated = uiDevice.wait(
                Until.hasObject(By.descContains("Clicked")),
                10000,
            )
            assertTrue("Card text did not update after click", textUpdated)

            // Capture a screenshot for visual confirmation
            captureScreenshot("map_interactions_screenshot.png")
            
            // TODO: Implement Gemini verification if needed to check the look of the card.
        }
    }
}
