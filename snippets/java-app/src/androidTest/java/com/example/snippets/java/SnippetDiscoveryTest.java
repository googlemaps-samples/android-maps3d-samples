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
