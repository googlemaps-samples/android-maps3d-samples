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
}
