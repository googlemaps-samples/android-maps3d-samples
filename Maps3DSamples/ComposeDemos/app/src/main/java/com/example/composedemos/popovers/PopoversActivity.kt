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

package com.example.composedemos.popovers

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.maps.android.compose3d.GoogleMap3D
import com.google.maps.android.compose3d.MarkerConfig
import com.google.maps.android.compose3d.PopoverConfig

class PopoversActivity : ComponentActivity() {
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
                    PopoversScreen()
                }
            }
        }
    }
}

@Composable
fun PopoversScreen() {
    var popovers by remember { mutableStateOf(emptyList<PopoverConfig>()) }
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
            tilt = 75.0
            range = 1635.0
            roll = 0.0
        }
    }

    // Sample marker that will trigger the popover
    val marker = remember {
        MarkerConfig(
            key = "popover_marker",
            position = latLngAltitude {
                latitude = 44.59054845363309
                longitude = -104.715177415273
                altitude = 10.0
            },
            altitudeMode = AltitudeMode.RELATIVE_TO_MESH,
            label = "Click me for Popover",
            isExtruded = true,
            isDrawnWhenOccluded = true,
            onClick = {
                popovers = listOf(
                    PopoverConfig(
                        key = "popover_1",
                        positionAnchorKey = "popover_marker",
                        autoPanEnabled = false,
                        autoCloseEnabled = false,
                        content = {
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(8.dp),
                            ) {
                                Text(
                                    text = "This is a Popover anchored to a marker!",
                                    modifier = Modifier.padding(16.dp),
                                    color = Color.Black,
                                )
                            }
                        },
                    ),
                )
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
            camera = devilsTowerCamera,
            markers = listOf(marker),
            popovers = popovers,
            mapMode = Map3DMode.HYBRID,
            modifier = Modifier.fillMaxSize(),
            onMapSteady = {
                isMapSteady = true
            },
            onMapClick = {
                popovers = emptyList()
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
                text = "Popovers",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
