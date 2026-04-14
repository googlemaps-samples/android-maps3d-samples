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

package com.example.maps3dcomposedemo

import android.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
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
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.maps.android.compose3d.GoogleMap3D
import com.google.maps.android.compose3d.MarkerConfig
import com.google.maps.android.compose3d.PolylineConfig

@Composable
fun BasicMapSample() {
    // Hoist the camera state
    var cameraState by remember {
        mutableStateOf(
            camera {
                center = latLngAltitude {
                    latitude = 37.8199
                    longitude = -122.4783
                    altitude = 0.0
                }
                heading = 0.0
                tilt = 60.0
                range = 2000.0
                roll = 0.0
            }
        )
    }

    var mapMode by remember { mutableStateOf(Map3DMode.SATELLITE) }
    var isMapSteady by remember { mutableStateOf(false) }

    // Sample markers
    val markers = remember {
        listOf(
            MarkerConfig(
                key = "golden_gate",
                position = latLngAltitude {
                    latitude = 37.8199
                    longitude = -122.4783
                    altitude = 0.0
                },
                label = "Golden Gate Bridge",
                altitudeMode = AltitudeMode.CLAMP_TO_GROUND
            )
        )
    }

    // Sample polyline
    val polylines = remember {
        listOf(
            PolylineConfig(
                key = "sample_line",
                points = listOf(
                    latLngAltitude { latitude = 37.8199; longitude = -122.4783; altitude = 0.0 },
                    latLngAltitude { latitude = 37.8299; longitude = -122.4883; altitude = 0.0 }
                ),
                color = Color.RED,
                width = 10f,
                altitudeMode = AltitudeMode.CLAMP_TO_GROUND
            )
        )
    }

    Box(modifier = Modifier.fillMaxSize().semantics { contentDescription = if (isMapSteady) "MapSteady" else "MapLoading" }) {
        GoogleMap3D(
            camera = cameraState,
            markers = markers,
            polylines = polylines,
            mapMode = mapMode,
            modifier = Modifier.fillMaxSize(),
            onMapReady = {
                // Map is ready, we could perform additional setup here if needed
            },
            onMapSteady = {
                isMapSteady = true
            }
        )

        // UI Controls
        FloatingActionButton(
            onClick = {
                mapMode = if (mapMode == Map3DMode.SATELLITE) {
                    Map3DMode.HYBRID
                } else {
                    Map3DMode.SATELLITE
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Text(text = if (mapMode == Map3DMode.SATELLITE) "Hybrid" else "Satellite")
        }
        
        FloatingActionButton(
            onClick = {
                // Reset camera
                cameraState = camera {
                    center = latLngAltitude {
                        latitude = 37.8199
                        longitude = -122.4783
                        altitude = 0.0
                    }
                    heading = 0.0
                    tilt = 60.0
                    range = 2000.0
                    roll = 0.0
                }
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(text = "Reset")
        }
    }
}

@Composable
fun ModelsSample() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Models Sample Placeholder\nTODO: Implement loading 3D models (glTF).")
    }
}
