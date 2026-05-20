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

import com.example.maps3djava.popovers.PopoversActivity;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Visual test for Popovers sample in Java app.
 */
@RunWith(AndroidJUnit4.class)
public class PopoversVisualTest extends BaseVisualTest {

    @Test
    public void verifyPopoversRenders() {
        // Launch PopoversActivity
        Intent intent = new Intent(context, PopoversActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

        // Wait for the activity to be displayed
        uiDevice.wait(Until.hasObject(By.pkg(context.getPackageName()).depth(0)), 10000);

        // Wait for the map to render and tiles to load
        waitForMapRendering(60);

        // Capture a screenshot to find the marker
        Bitmap searchScreenshot = captureScreenshot("popovers_search.png");

        // Define the prompt for Gemini to find coordinates
        String promptFind = "Analyze this screenshot of a 3D map.\n" +
                "You should see a marker or label with the text \"Golden Gate Bridge\".\n" +
                "Find that marker or label.\n" +
                "Return its center coordinates as a JSON object: {\"x\": <float>, \"y\": <float>} where x and y are normalized coordinates between 0.0 and 1.0 (0.0 is top/left, 1.0 is bottom/right).\n" +
                "Return ONLY the JSON object, nothing else.";

        // Analyze the image using Gemini
        String geminiResponse = helper.analyzeImageBlocking(searchScreenshot, promptFind, geminiApiKey);
        System.out.println("Gemini's coordinate response: " + geminiResponse);

        // Parse JSON and click
        try {
            String jsonStr = geminiResponse.substring(geminiResponse.indexOf("{"), geminiResponse.lastIndexOf("}") + 1);
            JSONObject json = new JSONObject(jsonStr);
            double x = json.getDouble("x");
            double y = json.getDouble("y");

            int clickX = (int) (x * uiDevice.getDisplayWidth());
            int clickY = (int) (y * uiDevice.getDisplayHeight());

            System.out.println("Clicking at (" + clickX + ", " + clickY + ") based on Gemini response");
            uiDevice.click(clickX, clickY);
        } catch (Exception e) {
            fail("Failed to parse coordinates from Gemini response: " + geminiResponse + ". Error: " + e.getMessage());
        }

        // Wait for the popover text to appear
        boolean textFound = uiDevice.wait(
                Until.hasObject(By.text("The Golden Gate Bridge")),
                15000
        );
        assertTrue("Popover text not found", textFound);

        // Capture a screenshot for visual confirmation
        captureScreenshot("popovers_screenshot.png");
    }
}
