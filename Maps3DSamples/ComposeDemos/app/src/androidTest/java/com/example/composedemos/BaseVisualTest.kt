/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.composedemos

import android.app.Instrumentation
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.google.maps.android.visualtesting.GeminiVisualTestHelper
import org.junit.Assert.assertTrue
import java.io.File

abstract class BaseVisualTest {

    protected val instrumentation: Instrumentation = InstrumentationRegistry.getInstrumentation()
    protected val uiDevice = UiDevice.getInstance(instrumentation)
    protected val context: Context = instrumentation.targetContext
    protected val helper = GeminiVisualTestHelper()

    protected val geminiApiKey: String by lazy {
        val key = BuildConfig.GEMINI_API_KEY
        assertTrue(
            "GEMINI_API_KEY is not set in secrets.properties. Please add GEMINI_API_KEY=YOUR_API_KEY to your secrets.properties file.",
            key != "YOUR_GEMINI_API_KEY",
        )
        key
    }

    protected fun captureScreenshot(filename: String = "screenshot_${System.currentTimeMillis()}.png"): Bitmap {
        val screenshotFile = File(context.filesDir, filename)
        val screenshotTaken = uiDevice.takeScreenshot(screenshotFile)
        assertTrue("Failed to take screenshot: $filename", screenshotTaken)

        val bitmap = BitmapFactory.decodeFile(screenshotFile.absolutePath)
        assertTrue("Failed to decode screenshot file: $filename", bitmap != null)

        android.util.Log.i("BaseVisualTest", "Screenshot saved to device: ${screenshotFile.absolutePath}")

        return bitmap
    }

    /**
     * Waits for the map to render.
     * Since MapView content (tiles, markers) is rendered on a GL surface and not exposed as
     * accessibility nodes, we cannot rely on UiAutomator looking for text/markers.
     * We use a stable delay to ensure rendering is complete.
     */
    protected fun waitForMapRendering(timeoutSeconds: Long = 30) {
        val found = uiDevice.wait(Until.hasObject(By.desc("MapSteady")), timeoutSeconds * 1000)
        assertTrue("Map did not become steady within $timeoutSeconds seconds", found)
    }
}
