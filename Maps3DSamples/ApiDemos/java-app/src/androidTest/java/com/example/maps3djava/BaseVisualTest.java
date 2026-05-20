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

package com.example.maps3djava;

import android.app.Instrumentation;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.Until;

import com.google.maps.android.visualtesting.GeminiVisualTestHelper;

import org.junit.Before;

import java.io.File;

import static org.junit.Assert.assertTrue;

/**
 * Base class for visual tests in Java app.
 * Provides common setup, screenshot capture, and map rendering wait utilities.
 */
public abstract class BaseVisualTest {

    protected Instrumentation instrumentation;
    protected UiDevice uiDevice;
    protected Context context;
    protected GeminiVisualTestHelper helper;
    protected String geminiApiKey;

    @Before
    public void setUp() {
        instrumentation = InstrumentationRegistry.getInstrumentation();
        uiDevice = UiDevice.getInstance(instrumentation);
        context = instrumentation.getTargetContext();
        helper = new GeminiVisualTestHelper();
        
        geminiApiKey = BuildConfig.GEMINI_API_KEY;
        assertTrue(
            "GEMINI_API_KEY is not set in secrets.properties. Please add GEMINI_API_KEY=YOUR_API_KEY to your secrets.properties file.",
            !"YOUR_GEMINI_API_KEY".equals(geminiApiKey)
        );
    }

    /**
     * Captures a screenshot and saves it to the device's files directory.
     *
     * @param filename The name of the screenshot file.
     * @return The captured screenshot as a Bitmap.
     */
    protected Bitmap captureScreenshot(String filename) {
        android.util.Log.i("BaseVisualTest", "context.getPackageName() = " + context.getPackageName());
        android.util.Log.i("BaseVisualTest", "context.getFilesDir() = " + context.getFilesDir().getAbsolutePath());
        File screenshotFile = new File(context.getFilesDir(), filename);
        boolean screenshotTaken = uiDevice.takeScreenshot(screenshotFile);
        assertTrue("Failed to take screenshot: " + filename, screenshotTaken);
        assertTrue("File does not exist after screenshot: " + screenshotFile.getAbsolutePath(), screenshotFile.exists());

        Bitmap bitmap = BitmapFactory.decodeFile(screenshotFile.getAbsolutePath());
        android.util.Log.i("BaseVisualTest", "Screenshot saved to device: " + screenshotFile.getAbsolutePath());

        return bitmap;
    }

    /**
     * Captures a screenshot with a default timestamped filename.
     */
    protected Bitmap captureScreenshot() {
        return captureScreenshot("screenshot_" + System.currentTimeMillis() + ".png");
    }

    /**
     * Waits for the map to render by looking for the "MapSteady" description.
     *
     * @param timeoutSeconds The maximum time to wait in seconds.
     */
    protected void waitForMapRendering(long timeoutSeconds) {
        // Fallback to sleep since View-based samples do not set "MapSteady" content description.
        System.out.println("Sleeping for " + timeoutSeconds + " seconds to allow map to render...");
        try {
            Thread.sleep(timeoutSeconds * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Waits for the map to render with a default timeout of 30 seconds.
     */
    protected void waitForMapRendering() {
        waitForMapRendering(30);
    }
}
