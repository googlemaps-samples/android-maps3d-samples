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

package com.example.composedemos.cameracontrols

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.composedemos.cameracontrols.DataModel.EMPIRE_STATE_BUILDING_LATITUDE
import com.example.composedemos.cameracontrols.DataModel.EMPIRE_STATE_BUILDING_LONGITUDE
import com.example.composedemos.cameracontrols.DataModel.nycCameraRestriction
import com.example.composedemos.cameracontrols.DataModel.nycPolygonConfigs
import com.example.maps3d.common.copy
import com.example.maps3d.common.toCompassDirection
import com.example.maps3d.common.toValidCamera
import com.example.maps3d.common.wrapIn
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.flyAroundOptions
import com.google.android.gms.maps3d.model.flyToOptions
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.maps.android.compose3d.GoogleMap3D
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds
import com.example.maps3dcommon.R as CommonR

class CameraControlsActivity : ComponentActivity() {
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
                    CameraControlsScreen()
                }
            }
        }
    }
}

@Composable
fun CameraControlsScreen() {
    val context = LocalContext.current
    var isMapSteady by remember { mutableStateOf(false) }
    var hasTriggeredInitialFlyTo by remember { mutableStateOf(false) }
    var googleMap3D by remember { mutableStateOf<GoogleMap3D?>(null) }

    val initialCamera = remember {
        camera {
            center = latLngAltitude {
                latitude = 40.7128
                longitude = -74.0060
                altitude = 150.0
            }
            heading = 252.7
            tilt = 79.0
            range = 1500.0
        }
    }

    var showRestriction by remember { mutableStateOf(false) }
    var isCameraRestricted by remember { mutableStateOf(false) }
    var selectedMapMode by remember { mutableIntStateOf(Map3DMode.SATELLITE) }
    var rollValue by remember { mutableFloatStateOf(0.0f) }
    var telemetryCamera by remember { mutableStateOf(initialCamera) }

    val cameraStateText = remember(context, telemetryCamera) {
        formatCameraState(context, telemetryCamera)
    }

    val polygons = remember(showRestriction) {
        if (showRestriction) nycPolygonConfigs else emptyList()
    }

    val cameraRestriction = remember(isCameraRestricted) {
        if (isCameraRestricted) nycCameraRestriction else null
    }

    // Automatically fly to the Empire State Building 2 seconds after the map first becomes steady
    LaunchedEffect(isMapSteady, googleMap3D) {
        val map = googleMap3D
        if (isMapSteady && map != null && !hasTriggeredInitialFlyTo) {
            hasTriggeredInitialFlyTo = true
            delay(2000.milliseconds)
            flyToEmpireStateBuilding(map)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = if (isMapSteady) "MapSteady" else "MapLoading" },
    ) {
        // 1. Map fills the screen above/behind the controls
        GoogleMap3D(
            camera = initialCamera,
            polygons = polygons,
            cameraRestriction = cameraRestriction,
            mapMode = selectedMapMode,
            modifier = Modifier.fillMaxSize(),
            onMapReady = { map ->
                googleMap3D = map
                map.getCamera()?.let { cam ->
                    telemetryCamera = cam
                    rollValue = (cam.roll ?: 0.0).toFloat().wrapIn(-180f..180f)
                }
            },
            onMapSteady = {
                isMapSteady = true
            },
            onCameraChanged = { cameraPosition ->
                telemetryCamera = cameraPosition
                rollValue = (cameraPosition.roll ?: 0.0).toFloat().wrapIn(-180f..180f)
            },
        )

        // 2. Translucent Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75f))
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                text = stringResource(CommonR.string.feature_title_camera_controls),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        // 3. Controls Panel at the bottom
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(16.dp)
                .fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Row 1: Fly to ESB & Fly around center
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = { flyToEmpireStateBuilding(googleMap3D) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = stringResource(CommonR.string.fly_to),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                    OutlinedButton(
                        onClick = { flyAroundCurrentCenter(googleMap3D) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = stringResource(CommonR.string.fly_around),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }

                // Row 2: Show/Hide restriction cube & Restrict/Remove camera restriction
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = { showRestriction = !showRestriction },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (showRestriction) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                Color.Transparent
                            },
                            contentColor = if (showRestriction) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                        ),
                    ) {
                        Text(
                            text = stringResource(
                                if (showRestriction) {
                                    CommonR.string.camera_hide_restriction
                                } else {
                                    CommonR.string.camera_show_restriction
                                },
                            ),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                    OutlinedButton(
                        onClick = { isCameraRestricted = !isCameraRestricted },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isCameraRestricted) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                Color.Transparent
                            },
                            contentColor = if (isCameraRestricted) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                        ),
                    ) {
                        Text(
                            text = stringResource(
                                if (isCameraRestricted) {
                                    CommonR.string.camera_remove_restriction
                                } else {
                                    CommonR.string.camera_activate_restriction
                                },
                            ),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }

                // Row 3: Map Mode RadioGroup (Hybrid / Satellite)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .selectableGroup(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val modes = listOf(
                        Map3DMode.HYBRID to stringResource(CommonR.string.map_mode_hybrid),
                        Map3DMode.SATELLITE to stringResource(CommonR.string.map_mode_satellite),
                    )
                    modes.forEachIndexed { index, (modeValue, modeLabel) ->
                        if (index > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .selectable(
                                    selected = (selectedMapMode == modeValue),
                                    onClick = { selectedMapMode = modeValue },
                                    role = Role.RadioButton,
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = (selectedMapMode == modeValue),
                                onClick = null,
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = modeLabel,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }

                // Row 4: Roll Slider + Reset Roll Button
                Text(
                    text = stringResource(CommonR.string.camera_roll_label_dynamic, rollValue),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Slider(
                        value = rollValue,
                        onValueChange = { value ->
                            val steppedValue = value.roundToInt().toFloat()
                            rollValue = steppedValue
                            updateMapRoll(googleMap3D, steppedValue.toDouble())
                        },
                        valueRange = -360f..360f,
                        modifier = Modifier
                            .weight(1f)
                            .semantics { contentDescription = "Roll Slider" },
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    FilledIconButton(
                        onClick = {
                            val resetValue = 0.0f
                            rollValue = resetValue
                            updateMapRoll(googleMap3D, resetValue.toDouble())
                        },
                    ) {
                        Icon(
                            painter = painterResource(id = CommonR.drawable.outline_recenter_24),
                            contentDescription = stringResource(
                                CommonR.string.reset_roll_content_description,
                            ),
                        )
                    }
                }

                // Row 5: Camera State Telemetry
                Text(
                    text = stringResource(CommonR.string.camera_state_label),
                    style = MaterialTheme.typography.titleSmall,
                )

                Text(
                    text = cameraStateText,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

/**
 * Initiates a camera flight animation to the Empire State Building.
 */
private fun flyToEmpireStateBuilding(googleMap3D: GoogleMap3D?) {
    googleMap3D?.flyCameraTo(
        flyToOptions {
            endCamera = camera {
                center = latLngAltitude {
                    latitude = EMPIRE_STATE_BUILDING_LATITUDE
                    longitude = EMPIRE_STATE_BUILDING_LONGITUDE
                    altitude = 212.0
                }
                heading = 34.0
                tilt = 67.0
                range = 750.0
                roll = 0.0
            }
            durationInMillis = 2_000
        },
    )
}

/**
 * Initiates a camera fly-around animation around the current camera's center.
 */
private fun flyAroundCurrentCenter(googleMap3D: GoogleMap3D?) {
    val camera = googleMap3D?.getCamera()?.toValidCamera() ?: return

    googleMap3D.flyCameraAround(
        flyAroundOptions {
            center = camera
            durationInMillis = 5_000
            rounds = 1.0
        },
    )
}

/**
 * Updates the map camera roll angle while preserving current center, heading, tilt, and range.
 */
private fun updateMapRoll(googleMap3D: GoogleMap3D?, newRoll: Double) {
    googleMap3D?.getCamera()?.let { currentCamera ->
        googleMap3D.setCamera(
            currentCamera.toValidCamera().copy(roll = newRoll),
        )
    }
}

/**
 * Formats the [Camera] state string matching the Kotlin View implementation.
 */
private fun formatCameraState(context: Context, camera: Camera): String {
    val nbsp = "\u00A0"
    val heading = camera.heading ?: 0.0
    val compassString = heading.toCompassDirection()

    return buildString {
        append(context.getString(CommonR.string.cam_lat_label, camera.center.latitude))
        append(", ")
        append(context.getString(CommonR.string.cam_lng_label, camera.center.longitude))
        append(", ")
        append(context.getString(CommonR.string.cam_alt_label, camera.center.altitude))
        append(",\n")
        append(context.getString(CommonR.string.cam_hdg_label, heading))
        append("$nbsp($compassString)")
        append(", ")
        append(context.getString(CommonR.string.cam_tlt_label, camera.tilt ?: 0.0))
        append(", ")
        append(context.getString(CommonR.string.cam_rng_label, camera.range ?: 0.0))
    }
}
