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

package com.example.composedemos.polylines

import android.graphics.Color
import android.os.Bundle
import android.util.Log
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.maps.android.compose3d.GoogleMap3D
import com.google.maps.android.compose3d.PolylineConfig
import kotlinx.coroutines.launch

class PolylinesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    PolylinesScreen()
                }
            }
        }
    }
}

@Composable
fun PolylinesScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isMapSteady by remember { mutableStateOf(false) }

    // Define the camera position centered around Mount Sanitas trailhead
    val boulderCamera = remember {
        camera {
            center = latLngAltitude {
                latitude = 40.029349
                longitude = -105.300354
                altitude = 1833.9
            }
            heading = 326.0
            tilt = 75.0
            roll = 0.0
            range = 3757.0
        }
    }

    // Parse the full trail data from sanitasLoop (defined in Data.kt)
    val trailPoints = remember {
        sanitasLoop.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map {
                val parts = it.split(",")
                latLngAltitude {
                    latitude = parts[0].trim().toDouble()
                    longitude = parts[1].trim().toDouble()
                    altitude = 0.5 // Slight elevation to avoid clipping
                }
            }
    }

    // Create a polyline config (State-driven approach)
    val polylineConfig = remember {
        PolylineConfig(
            key = "sanitas_loop",
            points = trailPoints,
            color = Color.RED,
            width = 10f,
            altitudeMode = AltitudeMode.RELATIVE_TO_GROUND,
            zIndex = 10,
            drawsOccludedSegments = true,
            onClick = { polyline ->
                Log.d("PolylinesActivity", "Polyline clicked: $polyline")
                scope.launch {
                    Toast.makeText(context, "Hiking time!", Toast.LENGTH_SHORT).show()
                }
            },
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = if (isMapSteady) "MapSteady" else "MapLoading" },
    ) {
        // 1. Map fills the entire screen
        GoogleMap3D(
            camera = boulderCamera,
            mapMode = Map3DMode.HYBRID,
            polylines = listOf(polylineConfig),
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
                text = "Polylines",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
