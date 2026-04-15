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
import com.google.maps.android.compose3d.GlyphConfig
import com.google.maps.android.compose3d.MarkerConfig
import com.google.maps.android.compose3d.PinConfig

class CustomMarkersActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    CustomMarkersScreen()
                }
            }
        }
    }
}

@Composable
fun CustomMarkersScreen() {
    var isMapSteady by remember { mutableStateOf(false) }

    // Camera centered on Devils Tower
    val devilsTowerCamera = remember {
        camera {
            center = latLngAltitude {
                latitude = 44.589994
                longitude = -104.715326
                altitude = 1508.9
            }
            heading = 1.0
            tilt = 60.0
            range = 5000.0
            roll = 0.0
        }
    }

    val markers = remember {
        listOf(
            MarkerConfig(
                key = "red_pin",
                position = latLngAltitude {
                    latitude = 44.5930
                    longitude = -104.7180
                    altitude = 10.0
                },
                altitudeMode = AltitudeMode.RELATIVE_TO_MESH,
                label = "Red Pin",
                pinConfig = PinConfig(
                    scale = 1.5f,
                    backgroundColor = Color.RED,
                    borderColor = Color.WHITE
                )
            ),
            MarkerConfig(
                key = "text_glyph",
                position = latLngAltitude {
                    latitude = 44.5870
                    longitude = -104.7120
                    altitude = 10.0
                },
                altitudeMode = AltitudeMode.RELATIVE_TO_MESH,
                label = "Text Glyph",
                pinConfig = PinConfig(
                    glyph = GlyphConfig.Text("DT", color = Color.BLACK),
                    backgroundColor = Color.YELLOW
                )
            ),
            MarkerConfig(
                key = "circle_glyph",
                position = latLngAltitude {
                    latitude = 44.5900
                    longitude = -104.7100
                    altitude = 10.0
                },
                altitudeMode = AltitudeMode.RELATIVE_TO_MESH,
                label = "Circle Glyph",
                pinConfig = PinConfig(
                    glyph = GlyphConfig.Circle(color = Color.BLUE),
                    backgroundColor = Color.GREEN
                )
            ),
             MarkerConfig(
                key = "image_glyph",
                position = latLngAltitude {
                    latitude = 44.5960
                    longitude = -104.7250
                    altitude = 10.0
                },
                altitudeMode = AltitudeMode.RELATIVE_TO_MESH,
                label = "Image Glyph",
                pinConfig = PinConfig(
                    glyph = GlyphConfig.Image(R.drawable.alien, color = Color.WHITE),
                    backgroundColor = Color.CYAN
                )
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = if (isMapSteady) "MapSteady" else "MapLoading" },
    ) {
        GoogleMap3D(
            camera = devilsTowerCamera,
            mapMode = Map3DMode.HYBRID,
            markers = markers,
            modifier = Modifier.fillMaxSize(),
            onMapSteady = {
                isMapSteady = true
            }
        )
    }
}
