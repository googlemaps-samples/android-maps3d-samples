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

package com.example.composedemos.camerarestrictions

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.cameraRestriction
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.gms.maps3d.model.latLngBounds
import com.google.maps.android.compose3d.GoogleMap3D

class CameraRestrictionsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Hide system tray (immersive mode)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    CameraRestrictionsScreen()
                }
            }
        }
    }
}

@Composable
fun CameraRestrictionsScreen() {
    var isMapSteady by remember { mutableStateOf(false) }

    // Camera centered on Space Needle
    val spaceNeedleCamera = remember {
        camera {
            center = latLngAltitude {
                latitude = 47.620527586075134
                longitude = -122.34935779313246
                altitude = 155.2680153221576
            }
            heading = 153.2
            tilt = 75.0
            range = 584.2
            roll = 0.0
        }
    }

    // Define a bounding box around the Space Needle
    val seattleBounds = remember {
        latLngBounds {
            northEastLat = 47.63
            northEastLng = -122.33
            southWestLat = 47.61
            southWestLng = -122.36
        }
    }

    // Define camera restriction
    val seattleRestriction = remember {
        cameraRestriction {
            bounds = seattleBounds
            minAltitude = 100.0
            maxAltitude = 2000.0
            minHeading = 0.0
            maxHeading = 360.0
            minTilt = 0.0
            maxTilt = 90.0
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = if (isMapSteady) "MapSteady" else "MapLoading" },
    ) {
        // 1. Map fills the entire screen with camera restriction
        GoogleMap3D(
            camera = spaceNeedleCamera,
            cameraRestriction = seattleRestriction,
            modifier = Modifier.fillMaxSize(),
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
                text = "Camera Restrictions",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        // 3. Info Card
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .padding(bottom = 32.dp),
        ) {
            Text(
                text = "Camera is restricted to the Space Needle area (47.61..47.63, -122.36..-122.33) and altitude 100m..2000m.",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
