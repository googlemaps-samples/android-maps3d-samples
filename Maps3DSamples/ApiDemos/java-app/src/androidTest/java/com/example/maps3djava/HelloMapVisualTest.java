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

import com.example.maps3djava.hellomap.HelloMapActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

/**
 * Visual test for Hello Map sample in Java app.
 */
@RunWith(AndroidJUnit4.class)
public class HelloMapVisualTest extends BaseVisualTest {

    @Test
    public void verifyHelloMapRenders() {
        // Launch HelloMapActivity
        Intent intent = new Intent(context, HelloMapActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

        // Wait for the activity to be displayed
        uiDevice.wait(Until.hasObject(By.pkg(context.getPackageName()).depth(0)), 10000);

        // Wait for the map to render and tiles to load
        waitForMapRendering(60);

        // Capture a screenshot
        Bitmap screenshotBitmap = captureScreenshot("hello_map_screenshot.png");

        // Define the verification prompt for Gemini
        String prompt = "Please act as a UI tester and analyze this screenshot to verify the application is rendering correctly.\n" +
                "Check the image against the following criteria:\n" +
                "1. Confirm that a 3D map view is visible.\n" +
                "2. Confirm that the Delicate Arch itself is clearly visible and a prominent part of the scene (it should look like a large freestanding rock arch).\n" +
                "\n" +
                "If all elements are present and the Delicate Arch is clearly visible, reply with \"PASSED\".\n" +
                "If any element is missing or incorrect, please detail the discrepancy.";

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
