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

import com.example.maps3djava.datavisualization.DataVisualizationActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * A premium visual regression test for the View-based Java 3D Data Visualization sample.
 *
 * Demonstrates robust programmatic testing of dynamic 3D extruded volume polygons by launching the Java-based
 * [DataVisualizationActivity], waiting for 3D map tiles, the extruded flood volume polygon over the San Francisco
 * waterfront, and control panel widgets to render, capturing a screenshot of the active map scene, and verifying visual
 * correctness using the Gemini API.
 */
@RunWith(AndroidJUnit4.class)
public class DataVisualizationVisualTest extends BaseVisualTest {

    @Test
    public void verifyDataVisualizationRenders() {
        // Launch DataVisualizationActivity
        Intent intent = new Intent(context, DataVisualizationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

        // Wait for the activity to be displayed in the foreground
        uiDevice.wait(Until.hasObject(By.pkg(context.getPackageName()).depth(0)), 10000);

        // Wait 15 seconds for map tiles to load, 3D flood polygon to extrude, and UI controls to settle
        System.out.println("Waiting 15 seconds for 3D Data Visualization rendering...");
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Capture high-resolution screenshot of the active 3D map scene
        Bitmap screenshotBitmap = captureScreenshot("data_visualization_screenshot.png");

        // Define the verification prompt for the visual testing agent
        String prompt = "Please act as a UI tester and analyze this screenshot.\n" +
                "1. Confirm that a 3D map view is visible over the San Francisco waterfront.\n" +
                "2. Confirm that an EXTRUDED 3D POLYGON or volumetric polygon shape (red tinted flood water zone) is clearly visible on top of the 3D terrain/map.\n" +
                "3. Confirm that the Data Visualization control card (showing Flood Elevation label, Risk Badge status, elevation slider, and Start/Stop Simulation button) is visible at the bottom of the screen.\n" +
                "\n" +
                "If and ONLY IF you can clearly see the 3D map view with the extruded red flood polygon and bottom simulation control panel, reply with \"PASSED\".\n" +
                "If you cannot see the extruded flood polygon or control panel, reply with \"FAILED: Extruded flood polygon or control panel not visible\".\n" +
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
