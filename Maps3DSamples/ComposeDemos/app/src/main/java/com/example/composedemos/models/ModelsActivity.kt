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

package com.example.composedemos.models

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.flyAroundOptions
import com.google.android.gms.maps3d.model.flyToOptions
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.maps.android.compose3d.GoogleMap3D
import com.google.maps.android.compose3d.ModelConfig
import com.google.maps.android.compose3d.ModelScale
import com.google.maps.android.compose3d.utils.awaitCameraAnimation
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ModelsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Hide system tray (immersive mode)
        val windowInsetsController = androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    ModelsScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModelsScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isMapSteady by remember { mutableStateOf(false) }

    val initialCamera = remember {
        camera {
            center = latLngAltitude {
                latitude = 47.133971
                longitude = 11.333161
                altitude = 2200.0
            }
            heading = 221.0
            tilt = 25.0
            range = 30000.0
        }
    }

    var cameraState by remember { mutableStateOf(initialCamera) }
    var mapInstance by remember { mutableStateOf<com.google.android.gms.maps3d.GoogleMap3D?>(null) }
    var animationJob by remember { mutableStateOf<Job?>(null) }

    val models = remember {
        listOf(
            ModelConfig(
                key = "plane_model",
                position = latLngAltitude {
                    latitude = 47.133971
                    longitude = 11.333161
                    altitude = 2200.0
                },
                url = "https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/Airplane.glb",
                altitudeMode = AltitudeMode.ABSOLUTE,
                scale = ModelScale.Uniform(0.05f),
                heading = 41.5,
                tilt = -90.0,
                roll = 0.0,
                onClick = {
                    Toast.makeText(context, "Clicked on Airplane!", Toast.LENGTH_SHORT).show()
                },
            ),
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = if (isMapSteady) "MapSteady" else "MapLoading" },
    ) {
        // 1. Map fills the entire screen
        GoogleMap3D(
            camera = cameraState,
            models = models,
            mapMode = Map3DMode.SATELLITE,
            modifier = Modifier.fillMaxSize(),
            onMapReady = { googleMap3D ->
                mapInstance = googleMap3D

                // Start animation sequence when map is ready
                animationJob = coroutineScope.launch {
                    runAnimationSequence(googleMap3D)
                }
            },
            onMapSteady = {
                isMapSteady = true
            },
        )

        // 2. Custom Translucent Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75f))
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                text = "Models",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        // 3. UI Controls (FABs)
        FloatingActionButton(
            onClick = {
                animationJob?.cancel()
                animationJob = coroutineScope.launch {
                    mapInstance?.let { map ->
                        // Fly back to initial camera
                        map.flyCameraTo(
                            flyToOptions {
                                endCamera = initialCamera
                                durationInMillis = 2000
                            },
                        )
                        map.awaitCameraAnimation()
                        runAnimationSequence(map)
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .padding(bottom = 32.dp), // Avoid system bars if visible
        ) {
            Text(text = "Reset")
        }

        FloatingActionButton(
            onClick = {
                animationJob?.cancel()
                animationJob = null
                Toast.makeText(context, "Animation Stopped", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .padding(bottom = 32.dp),
        ) {
            Text(text = "Stop")
        }
    }
}

private suspend fun runAnimationSequence(googleMap3D: com.google.android.gms.maps3d.GoogleMap3D) {
    delay(1500)

    val camera = camera {
        center = latLngAltitude {
            latitude = 47.133971
            longitude = 11.333161
            altitude = 2200.0
        }
        heading = 221.4
        tilt = 75.0
        range = 700.0
    }

    // Fly to the plane model
    googleMap3D.flyCameraTo(
        flyToOptions {
            endCamera = camera
            durationInMillis = 3500
        },
    )
    googleMap3D.awaitCameraAnimation()

    delay(500)

    // Fly around the plane model
    googleMap3D.flyCameraAround(
        flyAroundOptions {
            center = camera
            durationInMillis = 3500
            rounds = 0.5
        },
    )
    googleMap3D.awaitCameraAnimation()
}
