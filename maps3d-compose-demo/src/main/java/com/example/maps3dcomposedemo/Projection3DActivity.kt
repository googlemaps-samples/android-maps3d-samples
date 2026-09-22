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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.flyToOptions
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.maps.android.compose3d.GoogleMap3D
import com.google.maps.android.compose3d.utils.Projection3D
import com.google.maps.android.compose3d.utils.ScreenCoordinate
import com.google.android.gms.maps3d.GoogleMap3D as NativeGoogleMap3D

/**
 * Demonstrates 3D-to-2D Screen Projection using [Projection3D] in Jetpack Compose.
 *
 * Highlights:
 * 1. Mathematical calculation of screen pixel positions using [Projection3D.toScreenCoordinate]
 *    from active camera pose parameters (center, heading, tilt, roll, range).
 * 2. Anchoring a rich 2D Compose HUD overlay card and crosshair reticle over a 3D architectural
 *    landmark (e.g. San Francisco Transamerica Pyramid).
 * 3. Dynamic frustum clipping detection ([ScreenCoordinate.isVisible] & [ScreenCoordinate.depth]).
 * 4. Interactive surface tap re-projection: tapping the 3D map updates the target coordinates.
 */
class Projection3DActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Projection3DScreen()
                }
            }
        }
    }
}

private data class Landmark(val name: String, val location: LatLngAltitude)

private val SF_LANDMARKS = listOf(
    Landmark("Transamerica Pyramid", LatLngAltitude(37.7952, -122.4028, 260.0)),
    Landmark("Coit Tower", LatLngAltitude(37.8024, -122.4058, 110.0)),
    Landmark("Ferry Building Clock", LatLngAltitude(37.7955, -122.3937, 75.0)),
)

@Composable
fun Projection3DScreen() {
    var selectedLandmark by remember { mutableStateOf(SF_LANDMARKS[0]) }
    var activeLocation by remember { mutableStateOf(selectedLandmark.location) }
    var nativeMap by remember { mutableStateOf<NativeGoogleMap3D?>(null) }

    val initialCamera = remember {
        camera {
            center = latLngAltitude {
                latitude = 37.7952
                longitude = -122.4028
                altitude = 150.0
            }
            heading = 45.0
            tilt = 65.0
            roll = 0.0
            range = 800.0
        }
    }

    var liveCamera by remember { mutableStateOf(initialCamera) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.roundToPx() }
        val heightPx = with(density) { maxHeight.roundToPx() }

        // Compute 3D perspective projection for current camera pose
        val projection = remember(liveCamera, widthPx, heightPx) {
            Projection3D(
                camera = liveCamera,
                viewportWidth = widthPx,
                viewportHeight = heightPx,
                fovYDegrees = Projection3D.DEFAULT_FOV_Y_DEGREES,
            )
        }

        val screenCoord = remember(projection, activeLocation) {
            projection.toScreenCoordinate(activeLocation)
        }

        // 3D Map View
        GoogleMap3D(
            camera = initialCamera,
            modifier = Modifier.fillMaxSize(),
            onMapReady = { map -> nativeMap = map },
            onCameraChanged = { updatedCamera -> liveCamera = updatedCamera },
            onMapClick = { clickedLocation ->
                activeLocation = clickedLocation
            },
        )

        // Floating 2D HUD Badge anchored to projected 3D target coordinates
        if (screenCoord.isVisible && !screenCoord.x.isNaN() && !screenCoord.y.isNaN()) {
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = screenCoord.x.toInt() - 100,
                            y = screenCoord.y.toInt() - 140,
                        )
                    }
                    .wrapContentSize(),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.wrapContentSize(),
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                alpha = 0.92f,
                            ),
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier.padding(bottom = 6.dp),
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Text(
                                text = selectedLandmark.name,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = "Screen: (${screenCoord.x.toInt()}px, " +
                                    "${screenCoord.y.toInt()}px)",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                            )
                            Text(
                                text = "Alt: ${activeLocation.altitude.toInt()}m | " +
                                    "Depth: ${screenCoord.depth.toInt()}m",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    // Target crosshair reticle
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(Color(0xFFE53935), shape = CircleShape)
                            .border(2.dp, Color.White, shape = CircleShape),
                    )
                }
            }
        }

        // Top-left informational HUD Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
            ),
            modifier = Modifier
                .align(Alignment.TopStart)
                .safeDrawingPadding()
                .padding(16.dp),
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "3D to 2D Projection Engine",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = if (screenCoord.isVisible) {
                        "Target in Frustum: VISIBLE"
                    } else {
                        "Target out of Frustum / Behind Camera"
                    },
                    color = if (screenCoord.isVisible) Color(0xFF2E7D32) else Color(0xFFC62828),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                val headingStr = "${liveCamera.heading?.toInt() ?: 0}°"
                val tiltStr = "${liveCamera.tilt?.toInt() ?: 0}°"
                val rangeStr = "${liveCamera.range?.toInt() ?: 0}m"
                Text(
                    text = "Heading: $headingStr | Tilt: $tiltStr | Range: $rangeStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Tap map anywhere to re-project target position",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }

        // Bottom Landmark Selector
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            tonalElevation = 6.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .safeDrawingPadding(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Select Landmark Focus Target:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SF_LANDMARKS.forEach { landmark ->
                        Button(
                            onClick = {
                                selectedLandmark = landmark
                                activeLocation = landmark.location
                                nativeMap?.flyCameraTo(
                                    flyToOptions {
                                        endCamera = camera {
                                            center = landmark.location
                                            heading = liveCamera.heading ?: 45.0
                                            tilt = liveCamera.tilt ?: 65.0
                                            range = 750.0
                                        }
                                        durationInMillis = 1500
                                    },
                                )
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = landmark.name.split(" ").first(),
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                }
            }
        }
    }
}
