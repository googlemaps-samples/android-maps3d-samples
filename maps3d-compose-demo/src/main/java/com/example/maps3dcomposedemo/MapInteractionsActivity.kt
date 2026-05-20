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

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.maps.android.compose3d.GoogleMap3D

class MapInteractionsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    MapInteractionsScreen()
                }
            }
        }
    }
}

@Composable
fun MapInteractionsScreen() {
    var isMapSteady by remember { mutableStateOf(false) }
    var clickedInfo by remember { mutableStateOf("Click on the map to see details") }
    var map3dInstance by remember { mutableStateOf<GoogleMap3D?>(null) }

    // Calibrated camera centered around Colorado State Capitol
    val calibratedCamera = remember {
        camera {
            center = latLngAltitude {
                latitude = 39.73924812963158
                longitude = -104.98498430890453
                altitude = 1640.448817525612
            }
            heading = 93.65938810195067
            tilt = 61.598097303273555
            range = 494.79807973388233
            roll = 0.0
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = if (isMapSteady) "MapSteady" else "MapLoading" },
    ) {
        GoogleMap3D(
            camera = calibratedCamera,
            mapMode = Map3DMode.HYBRID,
            modifier = Modifier.fillMaxSize(),
            onMapReady = { instance ->
                map3dInstance = instance
                // Set up click listener directly on the instance
                instance.setMap3DClickListener { location, placeId ->
                    Log.d("MapInteractionsActivity", "Map clicked at ${location.latitude}, ${location.longitude}")
                    clickedInfo = if (placeId != null) {
                        "Clicked Place ID: $placeId\nAt: ${location.latitude}, ${location.longitude}"
                    } else {
                        "Clicked Location: ${location.latitude}, ${location.longitude}"
                    }
                }
            },
            onMapSteady = {
                isMapSteady = true
            },
        )

        // Click Info Card
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                // Extra padding to avoid system bars
                .padding(bottom = 32.dp),
        ) {
            Text(
                text = clickedInfo,
                modifier = Modifier
                    .padding(16.dp)
                    .semantics { contentDescription = clickedInfo },
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
