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

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class SnippetDiscoveryTest {

    @Test
    public void verifySnippetGroupsLoaded() {
        // Use the application context for scanning if needed, although scanning currently triggers from local setup
        List<SnippetGroupInfo> groups = SnippetRegistry.getSnippetGroups();

        assertNotNull("Snippet groups list should not be null", groups);
        assertFalse("Snippet groups list should not be empty", groups.isEmpty());

        for (SnippetGroupInfo group : groups) {
            assertNotNull("Group title should not be null", group.getTitle());
            assertFalse("Group title should not be empty", group.getTitle().isEmpty());
            assertFalse("Group '" + group.getTitle() + "' must have items", group.getItems().isEmpty());

            for (SnippetItemInfo item : group.getItems()) {
                assertNotNull("Snippet title should not be null", item.getTitle());
                assertFalse("Snippet title should not be empty", item.getTitle().isEmpty());
                assertNotNull("Snippet description should not be null", item.getDescription());
            }
        }
    }
}
