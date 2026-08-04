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

import android.content.Intent;
import android.graphics.Bitmap;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.Until;

import com.example.maps3djava.markers.MarkersActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

/**
 * Visual test for Markers sample in Java app.
 */
@RunWith(AndroidJUnit4.class)
public class MarkersVisualTest extends BaseVisualTest {

    @Test
    public void verifyMarkersRenders() {
        // Launch MarkersActivity
        Intent intent = new Intent(context, MarkersActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

        // Wait for the activity to be displayed
        uiDevice.wait(Until.hasObject(By.pkg(context.getPackageName()).depth(0)), 10000);

        // Wait for the map to render and tiles to load
        waitForMapRendering(60);

        // Capture a screenshot
        Bitmap screenshotBitmap = captureScreenshot("markers_screenshot.png");

        // Define the verification prompt for Gemini
        String prompt = "Please act as a UI tester and analyze this screenshot.\n" +
                "1. Confirm that a 3D satellite map view of New York City (Manhattan) is visible.\n" +
                "2. Confirm that a Giant Ape/Gorilla marker icon is visible floating near the Empire State Building.\n" +
                "3. Confirm that custom red and/or yellow pins are visible in the vicinity.\n" +
                "\n" +
                "If the map is visible and the giant ape marker and custom pins are seen, reply with \"PASSED\".\n" +
                "Otherwise, report what you see.";

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
