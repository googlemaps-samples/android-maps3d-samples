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

import static org.junit.Assert.assertTrue;

import android.content.Intent;
import android.graphics.Bitmap;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.Until;

import com.example.maps3djava.fieldofview.FieldOfViewActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * A premium visual regression test for the View-based Java Field of View (FOV) sample.
 *
 * Demonstrates robust programmatic testing of optical dolly-zoom camera controls by launching the Java-based
 * [FieldOfViewActivity], waiting for 3D map tiles over the San Francisco Financial District, perspective dolly-zoom
 * camera controls, and quick FOV preset buttons to load, capturing a screenshot of the active map scene, and verifying
 * visual correctness using the Gemini API.
 */
@RunWith(AndroidJUnit4.class)
public class FieldOfViewVisualTest extends BaseVisualTest {

    @Test
    public void verifyFieldOfViewRenders() {
        // Launch FieldOfViewActivity
        Intent intent = new Intent(context, FieldOfViewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

        // Wait for the activity to be displayed in the foreground
        uiDevice.wait(Until.hasObject(By.pkg(context.getPackageName()).depth(0)), 10000);

        // Wait 15 seconds for map tiles to render and optical dolly zoom view to settle
        System.out.println("Waiting 15 seconds for 3D Field of View perspective rendering...");
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Capture high-resolution screenshot of the active 3D map scene
        Bitmap screenshotBitmap = captureScreenshot("field_of_view_screenshot.png");

        // Define the verification prompt for the visual testing agent
        String prompt = "Please act as a UI tester and analyze this screenshot.\n" +
                "1. Confirm that a 3D map view is visible over the San Francisco Financial District area.\n" +
                "2. Confirm that the Field of View perspective control card (with FOV angle slider and instant preset buttons: 20° Telephoto, 45° Standard, 90° Wide, 120° Ultra-Wide) is visible at the bottom of the screen.\n" +
                "3. Confirm that the visual text label showing the current FOV angle (e.g. \"Field of View: 45°\") is visible on the card.\n" +
                "\n" +
                "If and ONLY IF you can clearly see the 3D map view and bottom Field of View control card with presets and slider, reply with \"PASSED\".\n" +
                "If you cannot see the 3D map scene or FOV control card, reply with \"FAILED: 3D map scene or FOV controls not visible\".\n" +
                "Report what you see in detail.";

        // Analyze the image using Gemini (using blocking wrapper)
        String geminiResponse = helper.analyzeImageBlocking(screenshotBitmap, prompt, geminiApiKey);
        System.out.println("Gemini's analysis: " + geminiResponse);

        // Assert on Gemini's response
        assertTrue(
            "Visual verification failed. Gemini response: " + geminiResponse,
            geminiResponse != null && geminiResponse.toUpperCase().contains("PASSED")
        );
    }
}
