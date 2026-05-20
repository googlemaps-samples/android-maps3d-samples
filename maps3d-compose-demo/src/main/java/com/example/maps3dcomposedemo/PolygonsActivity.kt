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
import android.util.Log
import android.widget.Toast
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.maps.android.compose3d.GoogleMap3D
import com.google.maps.android.compose3d.PolygonConfig
import kotlinx.coroutines.launch

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
                    color = MaterialTheme.colorScheme.background,
                ) {
                    PolygonsScreen()
                }
            }
        }
    }
}

@Composable
fun PolygonsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isMapSteady by remember { mutableStateOf(false) }

    // Define the camera position centered around Denver Zoo
    val denverCamera = remember {
        camera {
            center = latLngAltitude {
                latitude = 39.748477
                longitude = -104.947575
                altitude = 1609.34 // Denver is a mile high!
            }
            heading = -68.0
            tilt = 47.0
            roll = 0.0
            range = 2251.0
        }
    }

    // Define the outline for the zoo
    val zooOutline = remember {
        """
        39.7508987, -104.9565381
        39.7502883, -104.9565489
        39.7501976, -104.9563557
        39.7501481, -104.955594
        39.7499171, -104.9553043
        39.7495872, -104.9551648
        39.7492407, -104.954961
        39.7489685, -104.9548859
        39.7484488, -104.9548966
        39.7481189, -104.9548859
        39.7479539, -104.9547679
        39.7479209, -104.9544567
        39.7476487, -104.9535341
        39.7475085, -104.9525792
        39.7474095, -104.9519247
        39.747525, -104.9513776
        39.7476734, -104.9511844
        39.7478137, -104.9506265
        39.7477559, -104.9496395
        39.7477477, -104.9486203
        39.7478467, -104.9475796
        39.7482344, -104.9465818
        39.7486138, -104.9457878
        39.7491005, -104.9454874
        39.7495789, -104.945938
        39.7500491, -104.9466998
        39.7503213, -104.9474615
        39.7505358, -104.9486954
        39.7505111, -104.950648
        39.7511215, -104.9506587
        39.7511173, -104.9527187
        39.7511091, -104.9546445
        39.7508987, -104.9565381
        """.trimIndent()
            .lines()
            .map { line -> line.split(",").map { it.trim().toDouble() } }
            .map { (lat, lng) ->
                latLngAltitude {
                    latitude = lat
                    longitude = lng
                    altitude = 0.0
                }
            }
    }

    // Define a hole inside the zoo area
    val zooHole = remember {
        """
        39.7498, -104.9535
        39.7498, -104.9525
        39.7488, -104.9525
        39.7488, -104.9535
        39.7498, -104.9535
        """.trimIndent()
            .lines()
            .map { line -> line.split(",").map { it.trim().toDouble() } }
            .map { (lat, lng) ->
                latLngAltitude {
                    latitude = lat
                    longitude = lng
                    altitude = 0.0
                }
            }
    }

    // Create a polygon config
    val polygonConfig = remember {
        PolygonConfig(
            key = "denver_zoo",
            path = zooOutline,
            innerPaths = listOf(zooHole),
            // Translucent yellow
            fillColor = Color.argb(70, 255, 255, 0),
            strokeColor = Color.GREEN,
            strokeWidth = 3f,
            altitudeMode = AltitudeMode.CLAMP_TO_GROUND,
            onClick = { polygon ->
                Log.d("PolygonsActivity", "Polygon clicked: $polygon")
                scope.launch {
                    Toast.makeText(context, "Zoo time!", Toast.LENGTH_SHORT).show()
                }
            },
        )
    }

    // Define the base face for the museum
    val museumBaseFace = remember {
        """
        39.74812392425406, -104.94414971628434
        39.7465307929639, -104.94370889409778
        39.747031745033794, -104.9415078562927
        39.74837320615968, -104.94194414397013
        39.74812392425406, -104.94414971628434
        """.trimIndent()
            .lines()
            .map { line -> line.split(",").map { it.trim().toDouble() } }
            .map { (lat, lng) ->
                latLngAltitude {
                    latitude = lat
                    longitude = lng
                    altitude = 1609.34 // Denver is a mile high!
                }
            }
    }

    // Create extruded polygons for the museum
    val museumPolygons = remember {
        extrudePolygon(museumBaseFace, 50.0).mapIndexed { index, outline ->
            PolygonConfig(
                key = "museum_face_$index",
                path = outline,
                // Semi-transparent magenta
                fillColor = Color.argb(70, 255, 0, 255),
                strokeColor = Color.MAGENTA,
                strokeWidth = 3f,
                altitudeMode = AltitudeMode.ABSOLUTE,
                onClick = { polygon ->
                    Log.d("PolygonsActivity", "Museum face clicked: $polygon")
                    scope.launch {
                        Toast.makeText(context, "Museum time!", Toast.LENGTH_SHORT).show()
                    }
                },
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = if (isMapSteady) "MapSteady" else "MapLoading" },
    ) {
        GoogleMap3D(
            camera = denverCamera,
            mapMode = Map3DMode.HYBRID,
            polygons = listOf(polygonConfig) + museumPolygons,
            modifier = Modifier.fillMaxSize(),
            onMapSteady = {
                isMapSteady = true
            },
        )
    }
}

/**
 * Extrudes a flat polygon (defined by basePoints, all at the same altitude)
 * upwards by a given extrusionHeight to form a 3D prism.
 */
fun extrudePolygon(
    basePoints: List<LatLngAltitude>,
    extrusionHeight: Double,
): List<List<LatLngAltitude>> {
    if (basePoints.size < 3) return emptyList()
    if (extrusionHeight <= 0) return emptyList()

    val baseAltitude = basePoints.first().altitude
    val topPoints = basePoints.map { basePoint ->
        latLngAltitude {
            latitude = basePoint.latitude
            longitude = basePoint.longitude
            altitude = baseAltitude + extrusionHeight
        }
    }

    val faces = mutableListOf<List<LatLngAltitude>>()
    faces.add(basePoints.toList())
    faces.add(topPoints.toList().reversed())

    for (i in basePoints.indices) {
        val p1Base = basePoints[i]
        val p2Base = basePoints[(i + 1) % basePoints.size]
        val p1Top = topPoints[i]
        val p2Top = topPoints[(i + 1) % basePoints.size]
        faces.add(listOf(p1Base, p2Base, p2Top, p1Top))
    }

    return faces
}
