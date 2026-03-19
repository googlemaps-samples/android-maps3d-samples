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

package com.example.snippets.kotlin

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.gms.maps3d.Map3DView
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class SnippetDiscoveryTest {

    @Test
    fun verifyAllSnippetsLaunchWithoutCrash() {
        val snippets = SnippetRegistry.snippets.keys
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()

        for (snippetTitle in snippets) {
            val intent = Intent(context, MapActivity::class.java).apply {
                putExtra(MapActivity.EXTRA_SNIPPET_TITLE, snippetTitle)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            try {
                ActivityScenario.launch<MapActivity>(intent).use { scenario ->
                    val latch = CountDownLatch(1)
                    scenario.onActivity { activity ->
                        // Verify map view exists
                        val map = activity.findViewById<Map3DView>(com.example.snippets.common.R.id.map)
                        if (map != null) {
                            latch.countDown()
                        }
                    }
                    if (!latch.await(5, TimeUnit.SECONDS)) {
                        fail("Map3DView not found or activity timeout for snippet: $snippetTitle")
                    }
                }
            } catch (e: Exception) {
                fail("Crash executing snippet '$snippetTitle': ${e.message}")
            }
        }
    }

    @Test
    fun verifySnippetGroupsLoaded() {
        val groups = SnippetRegistry.getSnippetGroups()
        org.junit.Assert.assertNotNull("Snippet groups list should not be null", groups)
        org.junit.Assert.assertFalse("Snippet groups list should not be empty", groups.isEmpty())

        for (group in groups) {
            org.junit.Assert.assertNotNull("Group title should not be null", group.title)
            org.junit.Assert.assertFalse("Group title should not be empty", group.title.isEmpty())
            org.junit.Assert.assertFalse("Group '${group.title}' must have items", group.items.isEmpty())

            for (item in group.items) {
                org.junit.Assert.assertNotNull("Snippet title should not be null", item.title)
                org.junit.Assert.assertFalse("Snippet title should not be empty", item.title.isEmpty())
            }
        }
    }
}
