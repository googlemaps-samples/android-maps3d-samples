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

import android.graphics.Point
import android.os.Bundle
import android.util.Log
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.maps3d.common.toCameraString
import com.example.maps3d.common.toValidCamera
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.Popover
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.CollisionBehavior
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.flyToOptions
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.gms.maps3d.model.popoverShadow
import com.google.android.gms.maps3d.model.popoverStyle
import com.google.maps.android.compose3d.GoogleMap3D
import com.google.maps.android.compose3d.MarkerConfig
import com.google.maps.android.compose3d.PopoverConfig
import kotlinx.coroutines.launch
import android.graphics.Color as AndroidColor
import com.example.maps3dcommon.R as CommonR

private const val TAG = "PopoversActivity"
private const val CONTENT_LAT = 37.820642
private const val CONTENT_LNG = -122.478227
private const val CONTENT_ALT = 0.0

class PopoversActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Hide system tray (immersive mode)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

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
    var googleMap3D by remember { mutableStateOf<GoogleMap3D?>(null) }
    var popover by remember { mutableStateOf<Popover?>(null) }
    var popoverToggleCount by remember { mutableIntStateOf(0) }

    // Initial camera centered on the Golden Gate Bridge in San Francisco
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
            startVisible = false,
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

    // Marker on the Golden Gate Bridge that toggles the popover when clicked
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
        // 1. Map fills the entire screen
        GoogleMap3D(
            camera = initialCamera,
            markers = listOf(markerInGoldenGate),
            popovers = popovers,
            mapMode = Map3DMode.SATELLITE,
            modifier = Modifier.fillMaxSize(),
            onMapReady = { map ->
                googleMap3D = map
            },
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                text = stringResource(CommonR.string.feature_title_popovers),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        // 3. Floating Pill Control Bar (Reset View + Snapshot, matching SampleBaseActivity)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                modifier = Modifier
                    .background(
                        color = Color(0x80FFFFFF),
                        shape = RoundedCornerShape(32.dp),
                    )
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilledTonalIconButton(
                    onClick = {
                        googleMap3D?.flyCameraTo(
                            flyToOptions {
                                endCamera = initialCamera
                                durationInMillis = 2_000
                            },
                        )
                    },
                    modifier = Modifier.alpha(0.85f),
                ) {
                    Icon(
                        painter = painterResource(id = CommonR.drawable.restart_alt_24px),
                        contentDescription = stringResource(CommonR.string.reset_view),
                    )
                }

                FilledTonalIconButton(
                    onClick = {
                        googleMap3D?.getCamera()?.let { cam ->
                            Log.d(TAG, cam.toValidCamera().toCameraString())
                        }
                    },
                    modifier = Modifier.alpha(0.85f),
                ) {
                    Icon(
                        painter = painterResource(id = CommonR.drawable.photo_camera_24px),
                        contentDescription = stringResource(CommonR.string.snapshot_camera),
                    )
                }
            }
        }
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
