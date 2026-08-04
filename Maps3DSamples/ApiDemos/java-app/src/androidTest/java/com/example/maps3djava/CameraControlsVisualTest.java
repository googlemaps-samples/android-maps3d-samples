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

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.Until;

import com.example.maps3djava.cameracontrols.CameraControlsActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

/**
 * Visual test for Camera Controls sample in Java app.
 */
@RunWith(AndroidJUnit4.class)
public class CameraControlsVisualTest extends BaseVisualTest {

    @Test
    public void verifyCameraControlsRenders() {
        // Launch CameraControlsActivity
        Intent intent = new Intent(context, CameraControlsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

        // Wait for the activity to be displayed
        uiDevice.wait(Until.hasObject(By.pkg(context.getPackageName()).depth(0)), 10000);

        // Wait for the map to render and tiles to load
        waitForMapRendering(60);

        // Verify that controls (e.g., a slider or text for "Heading") are visible
        boolean foundHeading = uiDevice.wait(Until.hasObject(By.textContains("Heading")), 5000);
        assertTrue("Heading control not found", foundHeading);

        // Capture a screenshot for visual confirmation
        captureScreenshot("camera_controls_screenshot.png");
    }
}
