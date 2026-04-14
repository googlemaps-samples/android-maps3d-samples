// Copyright 2026 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.maps3dcomposedemo

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.maps.android.compose3d.GoogleMap3D
import com.google.maps.android.compose3d.Map3DRegistry
import com.google.maps.android.compose3d.PolylineConfig
import com.google.maps.android.compose3d.toPolylineOptions
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

/**
 * Activity that demonstrates the use of polylines on a 3D map using Compose.
 *
 * This activity displays a polyline representing a portion of the Sanitas Loop trail
 * in Boulder, Colorado. It uses the `PolylineConfig` to create a stroked effect
 * with a red inner line and a translucent black outer line.
 */
class PolylinesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
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

    // Parse the full trail data from sanitasLoop
    val trailPoints = remember {
        sanitasLoop.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map {
                val parts = it.split(",")
                latLngAltitude {
                    latitude = parts[0].trim().toDouble()
                    longitude = parts[1].trim().toDouble()
                    altitude = 0.5
                }
            }
    }

    // Create a polyline config with a stroked effect
    val polylineConfig = remember {
        PolylineConfig(
            key = "sanitas_loop",
            points = trailPoints,
            color = Color.RED,
            width = 7f,
            altitudeMode = AltitudeMode.RELATIVE_TO_GROUND,
            zIndex = 5,
            outerColor = Color.BLACK,
            outerWidth = 13f,
            drawsOccludedSegments = true,
            onClick = { polyline ->
                Log.d("PolylinesActivity", "Polyline clicked: $polyline")
                scope.launch {
                    Toast.makeText(context, "Hiking time!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    val bgPolylineConfig = remember {
        PolylineConfig(
            key = "sanitas_loop_background",
            points = trailPoints,
            color = Color.BLACK,
            width = 11f,
            altitudeMode = AltitudeMode.RELATIVE_TO_GROUND,
            zIndex = 4,
            outerColor = Color.BLACK,
            outerWidth = 13f,
            drawsOccludedSegments = true,
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = if (isMapSteady) "MapSteady" else "MapLoading" }
    ) {
        GoogleMap3D(
            camera = boulderCamera,
            mapMode = Map3DMode.HYBRID,
            polylines = listOf(bgPolylineConfig, polylineConfig),
            modifier = Modifier.fillMaxSize(),
            onMapSteady = {
                isMapSteady = true
            }
        )
    }
}
