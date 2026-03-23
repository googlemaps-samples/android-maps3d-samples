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

package com.example.snippets.java;

import static org.junit.Assert.fail;

import android.content.Context;
import android.content.Intent;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.example.snippets.common.R;
import com.google.android.gms.maps3d.Map3DView;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SnippetRunTest {

    @Test
    public void verifyAllSnippetsLaunchWithoutCrash() {
        Set<String> snippets = SnippetRegistry.snippets.keySet();
        Context context = ApplicationProvider.getApplicationContext();

        for (String snippetTitle : snippets) {
            Intent intent = new Intent(context, MapActivity.class);
            intent.putExtra(MapActivity.EXTRA_SNIPPET_TITLE, snippetTitle);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            try (ActivityScenario<MapActivity> scenario = ActivityScenario.launch(intent)) {
                CountDownLatch latch = new CountDownLatch(1);
                scenario.onActivity(
                        activity -> {
                            // Verify map view exists
                            Map3DView map = activity.findViewById(R.id.map);
                            if (map != null) {
                                latch.countDown();
                            }
                        });
                if (!latch.await(5, TimeUnit.SECONDS)) {
                    fail("Map3DView not found or activity timeout for snippet: " + snippetTitle);
                }
            } catch (Exception e) {
                fail("Crash executing snippet '" + snippetTitle + "': " + e.getMessage());
            }
        }
    }
}
