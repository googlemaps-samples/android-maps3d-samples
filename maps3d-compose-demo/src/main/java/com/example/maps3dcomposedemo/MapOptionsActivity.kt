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
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.cameraRestriction
import com.google.android.gms.maps3d.model.latLngBounds
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.maps.android.compose3d.GoogleMap3D

class MapOptionsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MapOptionsScreen()
                }
            }
        }
    }
}

@Composable
fun MapOptionsScreen() {
    var mapMode by remember { mutableStateOf(Map3DMode.SATELLITE) }
    var isRestricted by remember { mutableStateOf(false) }

    // Camera centered on Devils Tower
    val devilsTowerCamera = remember {
        camera {
            center = latLngAltitude {
                latitude = 44.589994
                longitude = -104.715326
                altitude = 1508.9
            }
            heading = 1.0
            tilt = 75.0
            range = 1635.0
            roll = 0.0
        }
    }

    // Define a restriction area around Devils Tower
    val restriction = remember {
        cameraRestriction {
            bounds = latLngBounds {
                northEastLat = 44.595
                northEastLng = -104.710
                southWestLat = 44.585
                southWestLng = -104.720
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap3D(
            camera = devilsTowerCamera,
            mapMode = mapMode,
            cameraRestriction = if (isRestricted) restriction else null,
            modifier = Modifier.fillMaxSize()
        )

        // Control Panel
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Map Options",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { mapMode = Map3DMode.SATELLITE },
                    modifier = Modifier.weight(1f),
                    enabled = mapMode != Map3DMode.SATELLITE
                ) {
                    Text("Satellite")
                }
                Button(
                    onClick = { mapMode = Map3DMode.HYBRID },
                    modifier = Modifier.weight(1f),
                    enabled = mapMode != Map3DMode.HYBRID
                ) {
                    Text("Hybrid")
                }
            }

            Button(
                onClick = { isRestricted = !isRestricted },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isRestricted) "Clear Restriction" else "Restrict to Area")
            }
        }
    }
}
