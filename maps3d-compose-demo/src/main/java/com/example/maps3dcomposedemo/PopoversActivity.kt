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

import android.graphics.Point
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps3d.Popover
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.CollisionBehavior
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.gms.maps3d.model.popoverShadow
import com.google.android.gms.maps3d.model.popoverStyle
import com.google.maps.android.compose3d.GoogleMap3D
import com.google.maps.android.compose3d.MarkerConfig
import com.google.maps.android.compose3d.PopoverConfig
import kotlinx.coroutines.launch
import android.graphics.Color as AndroidColor

private const val TAG = "PopoversActivity"
private const val CONTENT_LAT = 37.820642
private const val CONTENT_LNG = -122.478227
private const val CONTENT_ALT = 0.0

class PopoversActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
    val scope = rememberCoroutineScope()
    var isMapSteady by remember { mutableStateOf(false) }
    var popover by remember { mutableStateOf<Popover?>(null) }
    var popoverToggleCount by remember { mutableIntStateOf(0) }

    val initialCamera = remember {
        camera {
            center = latLngAltitude {
                latitude = CONTENT_LAT
                longitude = CONTENT_LNG
                altitude = CONTENT_ALT
            }
            heading = 0.0
            tilt = 45.0
            range = 4075.0
        }
    }

    val goldenGatePopoverConfig = remember {
        PopoverConfig(
            key = "golden_gate_popover",
            positionAnchorKey = "golden_gate_marker",
            altitudeMode = AltitudeMode.RELATIVE_TO_MESH,
            autoPanEnabled = true,
            autoCloseEnabled = true,
            anchorOffset = Point(0, 0),
            popoverStyle = popoverStyle {
                padding = 20.0f
                backgroundColor = AndroidColor.WHITE
                borderRadius = 8.0f
                shadow = popoverShadow {
                    color = AndroidColor.argb(77, 0, 0, 0)
                    offsetX = 2.0f
                    offsetY = 4.0f
                    radius = 4.0f
                }
            },
            onPopoverCreated = { createdPopover ->
                popover = createdPopover
                Log.d(TAG, "Popover created")
            },
            content = {
                GoldenGateInfoContent()
            },
        )
    }

    val popovers = remember { listOf(goldenGatePopoverConfig) }

    val markerInGoldenGate = remember {
        MarkerConfig(
            key = "golden_gate_marker",
            position = latLngAltitude {
                latitude = 37.819852
                longitude = -122.478549
                altitude = 0.0
            },
            label = "Golden Gate Bridge",
            zIndex = 1,
            isExtruded = true,
            isDrawnWhenOccluded = true,
            collisionBehavior = CollisionBehavior.REQUIRED,
            altitudeMode = AltitudeMode.RELATIVE_TO_MESH,
            onClick = {
                scope.launch {
                    Log.d(TAG, "Marker clicked")
                    if (popoverToggleCount > 5) {
                        popover?.remove()
                        Log.d(TAG, "Popover removed")
                        popoverToggleCount = 0
                    } else {
                        Log.d(TAG, "Popover toggled")
                        popover?.toggle()
                        popoverToggleCount++
                    }
                }
            },
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = if (isMapSteady) "MapSteady" else "MapLoading" },
    ) {
        GoogleMap3D(
            camera = initialCamera,
            markers = listOf(markerInGoldenGate),
            popovers = popovers,
            mapMode = Map3DMode.SATELLITE,
            modifier = Modifier.fillMaxSize(),
            onMapSteady = {
                isMapSteady = true
            },
        )
    }
}

@Composable
private fun GoldenGateInfoContent() {
    Column {
        Text(
            text = "The Golden Gate Bridge",
            fontSize = 18.sp,
            color = Color.Black,
        )
        Text(
            text = "San Francisco, CA",
            fontSize = 14.sp,
            color = Color.DarkGray,
        )
        Text(
            text = "The Golden Gate Bridge is a suspension bridge\n" +
                " spanning the one-mile-wide strait connecting\n" +
                " San Francisco Bay and the Pacific Ocean.\n" +
                " The bridge was completed in 1937.",
            fontSize = 12.sp,
            color = Color.Gray,
        )
    }
}
