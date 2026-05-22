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

import com.example.maps3djava.routes.RoutesActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

/**
 * A premium visual regression test for the View-based Java Routes API sample.
 *
 * Demonstrates robust programmatic testing of 3D vehicle maps by launching the Java-based
 * [RoutesActivity], waiting for asynchronous REST directions coordinates, allowing the Handler-driven
 * play loop to animate the vehicle, capturing a screenshot of the active map scene, and verifying
 * visual correctness using the Gemini API.
 */
@RunWith(AndroidJUnit4.class)
public class RoutesVisualTest extends BaseVisualTest {

    @Test
    public void verifyRoutesRenders() {
        // Launch RoutesActivity
        Intent intent = new Intent(context, RoutesActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

        // Wait for the activity to be displayed in the foreground
        uiDevice.wait(Until.hasObject(By.pkg(context.getPackageName()).depth(0)), 10000);

        // Wait 15 seconds for map tiles to load, route coordinates to fetch, and the vehicle model to start animating
        System.out.println("Waiting 15 seconds for map rendering and vehicle animation...");
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Capture high-resolution screenshot of the active 3D map scene
        Bitmap screenshotBitmap = captureScreenshot("routes_screenshot.png");

        // Define the verification prompt for the visual testing agent
        String prompt = "Please act as a UI tester and analyze this screenshot.\n" +
                "1. Confirm that a 3D map view is visible.\n" +
                "2. Confirm that a blue polyline (line) is visible on the map, representing a route.\n" +
                "3. Confirm that a RED CAR 3D MODEL is clearly visible on or near the blue polyline.\n" +
                "4. The route should be in Hawaii (Oahu area, coastal/mountainous terrain).\n" +
                "\n" +
                "If and ONLY IF you can clearly see a red car model on or near the blue polyline, reply with \"PASSED\".\n" +
                "If you cannot see a red car model, reply with \"FAILED: Red car model not visible\".\n" +
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
