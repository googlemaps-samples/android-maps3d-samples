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

import com.example.maps3djava.pathfollowing.PathFollowingActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * A premium visual regression test for the View-based Java Path Following sample.
 * Demonstrates robust programmatic testing of ground-level route navigation by launching the Java-based
 * [PathFollowingActivity], waiting for 3D map tiles and path coordinates to render, allowing the Handler-driven
 * play loop to animate the camera along the route, capturing a screenshot of the active map scene, and verifying
 * visual correctness using the Gemini API.
 */
@RunWith(AndroidJUnit4.class)
public class PathFollowingVisualTest extends BaseVisualTest {

    @Test
    public void verifyPathFollowingRenders() {
        // Launch PathFollowingActivity
        Intent intent = new Intent(context, PathFollowingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

        // Wait for the activity to be displayed in the foreground
        uiDevice.wait(Until.hasObject(By.pkg(context.getPackageName()).depth(0)), 10000);

        // Wait 15 seconds for map tiles to load, ground polyline to render, and initial animation to settle
        System.out.println("Waiting 15 seconds for map rendering and path navigation...");
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Capture high-resolution screenshot of the active 3D map scene
        Bitmap screenshotBitmap = captureScreenshot("path_following_screenshot.png");

        // Define the verification prompt for the visual testing agent
        String prompt = "Please act as a UI tester and analyze this screenshot.\n" +
                "1. Confirm that a 3D map view is visible.\n" +
                "2. Confirm that a BLUE POLYLINE (line) is clearly visible on the map, representing the route path.\n" +
                "3. Confirm that interactive camera control sliders (Range, Altitude, Heading, Tilt, Speed) and an Urban/Rural selector panel are visible at the bottom of the screen.\n" +
                "\n" +
                "If and ONLY IF you can clearly see the 3D map view with the blue route polyline and bottom control card, reply with \"PASSED\".\n" +
                "If you cannot see the blue route polyline or control panel, reply with \"FAILED: Blue path polyline or UI controls not visible\".\n" +
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
