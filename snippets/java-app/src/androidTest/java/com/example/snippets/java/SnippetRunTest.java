package com.example.snippets.java;

import android.content.Context;
import android.content.Intent;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.google.android.gms.maps3d.Map3DView;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.Set;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertNotNull;

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
                scenario.onActivity(activity -> {
                    // Verify map view exists
                    Map3DView map = activity.findViewById(com.example.snippets.common.R.id.map);
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
