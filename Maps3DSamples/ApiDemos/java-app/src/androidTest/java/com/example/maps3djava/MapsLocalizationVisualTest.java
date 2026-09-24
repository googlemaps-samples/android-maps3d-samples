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
import com.example.maps3djava.mapslocalization.MapsLocalizationActivity;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tier 4 Visual Regression Test for the Java Views {@link MapsLocalizationActivity} sample.
 */
@RunWith(AndroidJUnit4.class)
public class MapsLocalizationVisualTest extends BaseVisualTest {

    @Test
    public void verifyMapsLocalizationRenders() {
        Intent intent = new Intent(context, MapsLocalizationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

        uiDevice.wait(Until.hasObject(By.pkg(context.getPackageName()).depth(0)), 10000);
        waitForMapRendering(15);

        Bitmap screenshotBitmap = captureScreenshot("maps_localization_screenshot.png");

        String prompt =
                "Please act as a UI tester and analyze this screenshot.\n"
                        + "1. Confirm that a 3D map view is visible in Hybrid or Roadmap mode with place labels.\n"
                        + "2. Confirm that the bottom 3D Map Localization Controls card is visible (showing the language selection button and location label below it).\n"
                        + "\n"
                        + "If and ONLY IF you can clearly see the 3D map scene and the bottom Localization Controls card, reply with \"PASSED\".\n"
                        + "If you cannot see the 3D map scene or control card, reply with \"FAILED: 3D Map Localization scene or controls not visible\".\n"
                        + "Report what you see in detail.";

        String geminiResponse = helper.analyzeImageBlocking(screenshotBitmap, prompt, geminiApiKey);
        System.out.println("Gemini's analysis: " + geminiResponse);

        assertTrue(
                "Visual verification failed. Gemini response: " + geminiResponse,
                geminiResponse != null && geminiResponse.toUpperCase().contains("PASSED"));
    }
}
