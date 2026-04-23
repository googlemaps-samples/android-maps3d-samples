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

package com.example.composedemos.cameracontrols

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
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
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.maps.android.compose3d.GoogleMap3D

class CameraControlsActivity : ComponentActivity() {
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
                    CameraControlsScreen()
                }
            }
        }
    }
}

@Composable
fun CameraControlsScreen() {
    var isMapSteady by remember { mutableStateOf(false) }

    // Hoisted camera states
    var heading by remember { mutableStateOf(153.2f) }
    var tilt by remember { mutableStateOf(75.0f) }
    var range by remember { mutableStateOf(584.2f) }

    // Recreate camera object when state changes
    val dynamicCamera = remember(heading, tilt, range) {
        camera {
            center = latLngAltitude {
                latitude = 47.620527586075134
                longitude = -122.34935779313246
                altitude = 155.2680153221576
            }
            this.heading = heading.toDouble()
            this.tilt = tilt.toDouble()
            this.range = range.toDouble()
            roll = 0.0
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = if (isMapSteady) "MapSteady" else "MapLoading" },
    ) {
        // 1. Map fills the entire screen
        GoogleMap3D(
            camera = dynamicCamera,
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
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Camera Controls",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // 3. Controls Panel at the bottom
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .padding(bottom = 32.dp)
                .fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Heading: ${heading.toInt()}°", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = heading,
                    onValueChange = { heading = it },
                    valueRange = 0f..360f,
                    modifier = Modifier.semantics { contentDescription = "Heading Slider" }
                )

                Text(text = "Tilt: ${tilt.toInt()}°", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = tilt,
                    onValueChange = { tilt = it },
                    valueRange = 0f..90f,
                    modifier = Modifier.semantics { contentDescription = "Tilt Slider" }
                )

                Text(text = "Range: ${range.toInt()}m", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = range,
                    onValueChange = { range = it },
                    valueRange = 100f..5000f,
                    modifier = Modifier.semantics { contentDescription = "Range Slider" }
                )
            }
        }
    }
}
