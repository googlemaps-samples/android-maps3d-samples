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
import com.example.composedemos.cameracontrols.CameraControlsActivity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CameraControlsVisualTest : BaseVisualTest() {

    @Test
    fun verifyCameraControlsRenders() {
        runBlocking {
            // Launch CameraControlsActivity
            val intent = Intent(context, CameraControlsActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)

            // Wait for the activity to be displayed
            uiDevice.wait(Until.hasObject(By.pkg(context.packageName).depth(0)), 10000)

            // Wait for the map to render and tiles to load
            waitForMapRendering(60)

            // Verify that a 3D map view is visible
            // (Implicitly verified by waitForMapRendering)

            // Verify that controls (e.g., a slider or text for "Heading") are visible
            val foundHeading = uiDevice.wait(Until.hasObject(By.textContains("Heading")), 5000)
            assertTrue("Heading control not found", foundHeading)

            // Capture a screenshot for visual confirmation
            captureScreenshot("camera_controls_screenshot.png")
        }
    }
}
