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

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.example.snippets.common.R
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.Map3DView
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.flyToOptions
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class MapActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SNIPPET_TITLE = "snippet_title"
    }

    private lateinit var map3DView: Map3DView
    private lateinit var googleMap3D: GoogleMap3D

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_map)

        map3DView = findViewById(R.id.map)
        map3DView.onCreate(savedInstanceState)

        val groupTitle = intent.getStringExtra("group_title")
        val snippetTitle = intent.getStringExtra(EXTRA_SNIPPET_TITLE)
        val snippetList = SnippetRegistry.getSnippetGroups().flatMap { it.items }
        var currentIndex = snippetList.indexOfFirst { it.title == snippetTitle && (groupTitle == null || it.groupTitle == groupTitle) }

        val printPoseBtn = findViewById<MaterialButton>(R.id.snapshot_button).apply {
            setOnClickListener {
                if (::googleMap3D.isInitialized) {
                    val cam = googleMap3D.getCamera() ?: return@setOnClickListener
                    val center = cam.center ?: return@setOnClickListener

                    // Normalize heading to [0, 360)
                    val rawHeading = cam.heading ?: 0.0
                    val heading = (rawHeading % 360.0 + 360.0) % 360.0

                    // Normalize roll (-0.0 to 0.0)
                    val rawRoll = cam.roll ?: 0.0
                    val roll = if (rawRoll == -0.0) 0.0 else rawRoll

                    Log.d(
                        "MapActivity",
                        """
                        Camera Pose:
                        center = latLngAltitude {
                            latitude = ${"%.6f".format(center.latitude)}
                            longitude = ${"%.6f".format(center.longitude)}
                            altitude = ${"%.2f".format(center.altitude)}
                        }
                        tilt = ${"%.2f".format(cam.tilt)}
                        heading = ${"%.2f".format(heading)}
                        range = ${"%.2f".format(cam.range)}
                        roll = $roll
                        """.trimIndent(),
                    )
                } else {
                    Log.d("MapActivity", "Map NOT initialized yet")
                }
            }
        }

        val replayBtn = findViewById<MaterialButton>(R.id.reset_view_button).apply {
            setOnClickListener {
                if (currentIndex >= 0) {
                    val item = snippetList[currentIndex]
                    runSnippet(item.groupTitle, item.title)
                }
            }
        }

        // Previous and Next Nav buttons
        findViewById<MaterialButton>(R.id.previous_button).apply {
            setOnClickListener {
                if (snippetList.isEmpty()) return@setOnClickListener
                if (currentIndex > 0) {
                    currentIndex--
                } else {
                    currentIndex = snippetList.size - 1
                }
                val item = snippetList[currentIndex]
                runSnippet(item.groupTitle, item.title)
            }
        }

        findViewById<MaterialButton>(R.id.next_button).apply {
            setOnClickListener {
                if (snippetList.isEmpty()) return@setOnClickListener
                if (currentIndex < snippetList.size - 1) {
                    currentIndex++
                } else {
                    currentIndex = 0
                }
                val item = snippetList[currentIndex]
                runSnippet(item.groupTitle, item.title)
            }
        }

        map3DView.getMap3DViewAsync(object : OnMap3DViewReadyCallback {
            override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
                this@MapActivity.googleMap3D = googleMap3D

                // For simplicity, we run immediately, but add a listener for logs
                googleMap3D.setOnMapReadyListener { sceneReadiness ->
                }

                // Run snippet after initializing, resetting to global view first
                if (currentIndex >= 0) {
                    val item = snippetList[currentIndex]
                    runSnippet(item.groupTitle, item.title)
                }
            }

            override fun onError(error: Exception) {
                Toast.makeText(this@MapActivity, "Map Error: ${error.message}", Toast.LENGTH_LONG)
                    .show()
            }
        })
    }

    override fun onStart() {
        super.onStart()
        map3DView.onStart()
    }

    override fun onResume() {
        super.onResume()
        map3DView.onResume()
    }

    override fun onPause() {
        map3DView.onPause()
        super.onPause()
    }

    override fun onStop() {
        map3DView.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        map3DView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        map3DView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        map3DView.onSaveInstanceState(outState)
    }
    private fun runSnippet(groupTitle: String?, snippetTitle: String?) {
        if (snippetTitle == null) return
        val key = if (groupTitle != null) "$groupTitle - $snippetTitle" else snippetTitle
        val snippet = SnippetRegistry.snippets[key]
            ?: SnippetRegistry.getSnippetGroups().flatMap { it.items }.find { it.title == snippetTitle }
            ?: return
        if (!::googleMap3D.isInitialized) return

        SnippetRegistry.clearTrackedItems()

        lifecycleScope.launch {
            // 1. Set camera to far global view
            val globalCamera = camera {
                center = latLngAltitude {
                    latitude = 0.0
                    longitude = 0.0
                    altitude = 0.0
                }
                tilt = 0.0
                heading = 0.0
                range = 30000000.0 // Far view
            }
            googleMap3D.flyCameraTo(
                flyToOptions {
                    endCamera = globalCamera
                    durationInMillis = 2000
                },
            )

            // 2. Pause for a few seconds
            delay(4.seconds)

            // 3. Play snippet
            try {
                snippet.action(this@MapActivity, googleMap3D, lifecycleScope)
                Toast.makeText(
                    this@MapActivity,
                    "${snippet.groupTitle}: ${snippet.title}.\n${snippet.description}",
                    Toast.LENGTH_LONG,
                ).show()
            } catch (e: Exception) {
                Log.e("MapActivity", "Error running snippet: ${e.message}")
            }
        }
    }
}
