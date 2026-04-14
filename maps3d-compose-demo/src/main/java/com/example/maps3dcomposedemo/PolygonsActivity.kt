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
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.maps.android.compose3d.GoogleMap3D
import com.google.maps.android.compose3d.PolygonConfig

/**
 * Activity that demonstrates the use of polygons on a 3D map using Compose.
 *
 * This activity displays a polygon representing the Denver Zoo area.
 * It uses the `PolygonConfig` to draw a filled shape with a border and a hole.
 */
class PolygonsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PolygonsScreen()
                }
            }
        }
    }
}

@Composable
fun PolygonsScreen() {
    var isMapSteady by remember { mutableStateOf(false) }

    // Define the camera position centered around Denver Zoo
    val denverCamera = remember {
        camera {
            center = latLngAltitude {
                latitude = 39.748477
                longitude = -104.947575
                altitude = 1.0
            }
            heading = -68.0
            tilt = 47.0
            roll = 0.0
            range = 2251.0
        }
    }

    // Define the outline for the zoo
    val zooOutline = remember {
        listOf(
            latLngAltitude { latitude = 39.7508987; longitude = -104.9565381; altitude = 0.0 },
            latLngAltitude { latitude = 39.7502883; longitude = -104.9565489; altitude = 0.0 },
            latLngAltitude { latitude = 39.7501976; longitude = -104.9563557; altitude = 0.0 },
            latLngAltitude { latitude = 39.7501481; longitude = -104.955594; altitude = 0.0 },
            latLngAltitude { latitude = 39.7499171; longitude = -104.9553043; altitude = 0.0 },
            latLngAltitude { latitude = 39.7508987; longitude = -104.9565381; altitude = 0.0 } // Close loop
        )
    }

    // Define a hole inside the zoo area
    val zooHole = remember {
        listOf(
            latLngAltitude { latitude = 39.7498; longitude = -104.9535; altitude = 0.0 },
            latLngAltitude { latitude = 39.7498; longitude = -104.9525; altitude = 0.0 },
            latLngAltitude { latitude = 39.7488; longitude = -104.9525; altitude = 0.0 },
            latLngAltitude { latitude = 39.7488; longitude = -104.9535; altitude = 0.0 },
            latLngAltitude { latitude = 39.7498; longitude = -104.9535; altitude = 0.0 } // Close loop
        )
    }

    // Create a polygon config
    val polygonConfig = remember {
        PolygonConfig(
            key = "denver_zoo",
            path = zooOutline,
            innerPaths = listOf(zooHole),
            fillColor = Color.argb(70, 255, 255, 0), // Translucent yellow
            strokeColor = Color.GREEN,
            strokeWidth = 3f,
            altitudeMode = AltitudeMode.CLAMP_TO_GROUND
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = if (isMapSteady) "MapSteady" else "MapLoading" }
    ) {
        GoogleMap3D(
            camera = denverCamera,
            mapMode = Map3DMode.HYBRID,
            polygons = listOf(polygonConfig),
            modifier = Modifier.fillMaxSize(),
            onMapSteady = {
                isMapSteady = true
            }
        )
    }
}
