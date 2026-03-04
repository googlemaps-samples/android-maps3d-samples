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

package com.example.snippets.kotlin.snippets

import androidx.compose.foundation.layout.fillMaxSize
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.Map3DView
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude


class MapInitSnippets {

    // [START maps_android_3d_init_basic_kt]
    /**
     * Initializes a standard 3D Map View using AndroidView in Compose.
     */
    @Composable
    fun BasicMap3D() {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                Map3DView(context).apply {
                    getMap3DViewAsync(object : OnMap3DViewReadyCallback {
                        override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
                            val camera = camera {
                                center = latLngAltitude {
                                    latitude = 40.0150
                                    longitude = -105.2705
                                    altitude = 5000.0
                                }
                                heading = 0.0
                                tilt = 45.0
                                roll = 0.0
                                range = 10000.0
                            }
                            googleMap3D.setCamera(camera)
                        }
                        override fun onError(error: Exception) {
                            // Handle initialization error
                        }
                    })
                }
            }
        )
    }
    // [END maps_android_3d_init_basic_kt]

    // [START maps_android_3d_init_listeners_kt]
    /**
     * Sets up listeners for map readiness and steady state.
     */
    fun setupMapListeners(context: Context, map: GoogleMap3D) {
        val mainHandler = Handler(Looper.getMainLooper())
        
        map.setOnMapReadyListener { sceneReadiness ->
            if (sceneReadiness == 1.0) {
                // Scene is fully loaded
                mainHandler.post {
                    Toast.makeText(context, "Map Scene Ready (100%)", Toast.LENGTH_SHORT).show()
                }
            }
        }

        map.setOnMapSteadyListener { isSceneSteady ->
            if (isSceneSteady) {
                // Camera is not moving and data is loaded
                mainHandler.post {
                    Toast.makeText(context, "Map Scene Steady", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    // [END maps_android_3d_init_listeners_kt]
}
