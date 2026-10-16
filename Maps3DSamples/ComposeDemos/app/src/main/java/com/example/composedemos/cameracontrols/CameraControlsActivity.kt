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
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.composedemos.cameracontrols.DataModel.ESB_FLY_TO_DURATION_MS
import com.example.composedemos.cameracontrols.DataModel.MT_RAINIER_FLY_TO_DURATION_MS
import com.example.composedemos.cameracontrols.DataModel.MT_RAINIER_ORBIT_DURATION_MS
import com.example.composedemos.cameracontrols.DataModel.MT_RAINIER_ORBIT_ROUNDS
import com.example.composedemos.cameracontrols.DataModel.esbCamera
import com.example.composedemos.cameracontrols.DataModel.nycCameraRestriction
import com.example.composedemos.cameracontrols.DataModel.nycPolygonConfigs
import com.example.composedemos.cameracontrols.DataModel.targetBeaconConfig
import com.example.composedemos.cameracontrols.DataModel.targetCamera
import com.example.maps3d.common.Map3DQuickSettingsWidget
import com.example.maps3d.common.Map3DSessionTheme
import com.example.maps3d.common.Map3DSettingsController
import com.example.maps3d.common.copy
import com.example.maps3d.common.rememberMap3DSessionSettings
import com.example.maps3d.common.toCompassDirection
import com.example.maps3d.common.toValidCamera
import com.example.maps3d.common.wrapIn
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.flyAroundOptions
import com.google.android.gms.maps3d.model.flyToOptions
import com.google.maps.android.compose3d.GoogleMap3D
import kotlin.math.roundToInt
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
            Map3DSessionTheme {
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CameraControlsScreen() {
    val context = LocalContext.current
    var isMapSteady by remember { mutableStateOf(false) }
    var googleMap3D by remember { mutableStateOf<GoogleMap3D?>(null) }
    val settings by rememberMap3DSessionSettings(defaultMapMode = Map3DMode.SATELLITE)

    val initialCamera = remember {
        esbCamera(AltitudeMode.RELATIVE_TO_GROUND)
    }

    var showRestriction by remember { mutableStateOf(false) }
    var isCameraRestricted by remember { mutableStateOf(false) }
    val selectedMapMode = settings.mapMode
    var rollValue by remember { mutableFloatStateOf(0.0f) }
    var telemetryCamera by remember { mutableStateOf(initialCamera) }
    var selectedAltitudeMode by remember { mutableIntStateOf(AltitudeMode.RELATIVE_TO_GROUND) }
    var activeTargetResId by remember { mutableIntStateOf(CommonR.string.camera_target_esb) }
    var beaconRefreshToken by remember { mutableIntStateOf(0) }
    var needsMeshBeaconRefresh by remember { mutableStateOf(false) }

    val cameraStateText = remember(
        context,
        telemetryCamera,
        activeTargetResId,
        selectedAltitudeMode,
    ) {
        formatCameraState(
            context = context,
            camera = telemetryCamera,
            activeTargetResId = activeTargetResId,
            altitudeMode = selectedAltitudeMode,
        )
    }

    val isMountainTarget = (activeTargetResId == CommonR.string.camera_target_mt_rainier)
    val beaconLabel = stringResource(
        when (selectedAltitudeMode) {
            AltitudeMode.RELATIVE_TO_MESH ->
                if (isMountainTarget) CommonR.string.mt_rainier_beacon_mesh_label else CommonR.string.esb_beacon_mesh_label

            AltitudeMode.CLAMP_TO_GROUND ->
                if (isMountainTarget) CommonR.string.mt_rainier_beacon_clamp_label else CommonR.string.esb_beacon_clamp_label

            AltitudeMode.ABSOLUTE ->
                if (isMountainTarget) CommonR.string.mt_rainier_beacon_absolute_label else CommonR.string.esb_beacon_absolute_label

            else ->
                if (isMountainTarget) CommonR.string.mt_rainier_beacon_label else CommonR.string.esb_beacon_ground_label
        },
    )
    val markers = remember(isMountainTarget, beaconLabel, selectedAltitudeMode, beaconRefreshToken) {
        listOf(
            targetBeaconConfig(
                isMountain = isMountainTarget,
                label = beaconLabel,
                mode = selectedAltitudeMode,
                refreshToken = beaconRefreshToken,
            ),
        )
    }

    val polygons = remember(showRestriction) {
        if (showRestriction) nycPolygonConfigs else emptyList()
    }

    val cameraRestriction = remember(isCameraRestricted) {
        if (isCameraRestricted) nycCameraRestriction else null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = if (isMapSteady) "MapSteady" else "MapLoading" },
    ) {
        // 1. Map fills the screen above/behind the controls
        GoogleMap3D(
            camera = initialCamera,
            markers = markers,
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
                if (needsMeshBeaconRefresh) {
                    needsMeshBeaconRefresh = false
                    beaconRefreshToken++
                }
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
                .padding(12.dp)
                .fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Row 1: Fly to ESB & Fly around center
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = {
                            activeTargetResId = CommonR.string.camera_target_esb
                            needsMeshBeaconRefresh = (selectedAltitudeMode == AltitudeMode.RELATIVE_TO_MESH)
                            beaconRefreshToken++
                            flyToTarget(
                                googleMap3D = googleMap3D,
                                isMountain = false,
                                altitudeMode = selectedAltitudeMode,
                                onFlightFinished = { beaconRefreshToken++ },
                            )
                        },
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

                // Row 2: Fly to Mt. Rainier (Relative Alt)
                FilledTonalButton(
                    onClick = {
                        isCameraRestricted = false
                        activeTargetResId = CommonR.string.camera_target_mt_rainier
                        needsMeshBeaconRefresh = (selectedAltitudeMode == AltitudeMode.RELATIVE_TO_MESH)
                        beaconRefreshToken++
                        flyToTarget(
                            googleMap3D = googleMap3D,
                            isMountain = true,
                            altitudeMode = selectedAltitudeMode,
                            onFlightFinished = { beaconRefreshToken++ },
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(CommonR.string.fly_to_mountain),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                // Row 3: AltitudeMode Selector Chips (RELATIVE_TO_GROUND, RELATIVE_TO_MESH, CLAMP_TO_GROUND, ABSOLUTE)
                Text(
                    text = stringResource(CommonR.string.altitude_mode),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 4.dp),
                )

                val altitudeModes = listOf(
                    AltitudeMode.RELATIVE_TO_GROUND to stringResource(CommonR.string.relative_to_ground),
                    AltitudeMode.RELATIVE_TO_MESH to stringResource(CommonR.string.relative_to_mesh),
                    AltitudeMode.CLAMP_TO_GROUND to stringResource(CommonR.string.clamp_to_ground),
                    AltitudeMode.ABSOLUTE to stringResource(CommonR.string.absolute),
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    altitudeModes.forEach { (modeValue, modeLabel) ->
                        FilterChip(
                            selected = (selectedAltitudeMode == modeValue),
                            onClick = {
                                if (selectedAltitudeMode != modeValue) {
                                    selectedAltitudeMode = modeValue
                                    needsMeshBeaconRefresh = (modeValue == AltitudeMode.RELATIVE_TO_MESH)
                                    beaconRefreshToken++
                                    flyToTarget(
                                        googleMap3D = googleMap3D,
                                        isMountain = isMountainTarget,
                                        altitudeMode = modeValue,
                                        onFlightFinished = { beaconRefreshToken++ },
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = modeLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            },
                        )
                    }
                }

                // Row 4: Show/Hide restriction cube & Restrict/Remove camera restriction
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

                // Row 5: Map Mode RadioGroup (Hybrid / Satellite)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp)
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
                                    onClick = {
                                        Map3DSettingsController.shared.setMapMode(modeValue)
                                    },
                                    role = Role.RadioButton,
                                )
                                .padding(vertical = 2.dp),
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

                // Row 6: Roll Slider + Reset Roll Button
                Text(
                    text = stringResource(CommonR.string.camera_roll_label_dynamic, rollValue),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 4.dp),
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

                // Row 7: Camera State Telemetry
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

        // 4. Quick Settings Overlay
        Map3DQuickSettingsWidget(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 8.dp, end = 8.dp),
            sampleDefaultMapMode = Map3DMode.SATELLITE,
            googleMap3D = googleMap3D,
        )
    }
}

/**
 * Initiates a camera flight animation to the specified landmark target (`isMountain = false` for
 * the Empire State Building, `isMountain = true` for Mount Rainier) using the single unified
 * [AltitudeMode] camera and marker pipeline.
 */
private fun flyToTarget(
    googleMap3D: GoogleMap3D?,
    isMountain: Boolean,
    @AltitudeMode altitudeMode: Int,
    onFlightFinished: () -> Unit = {},
) {
    val map = googleMap3D ?: return
    if (isMountain) {
        map.setCameraRestriction(null)
    }
    val targetCam = targetCamera(isMountain = isMountain, mode = altitudeMode)
    map.setCameraAnimationEndListener {
        map.setCameraAnimationEndListener(null)
        onFlightFinished()
        if (isMountain) {
            map.flyCameraAround(
                flyAroundOptions {
                    center = targetCam
                    durationInMillis = MT_RAINIER_ORBIT_DURATION_MS
                    rounds = MT_RAINIER_ORBIT_ROUNDS
                },
            )
        }
    }
    map.flyCameraTo(
        flyToOptions {
            endCamera = targetCam
            durationInMillis =
                if (isMountain) MT_RAINIER_FLY_TO_DURATION_MS else ESB_FLY_TO_DURATION_MS
        },
    )
}

/**
 * Initiates a camera fly-around animation around the current camera's center.
 */
private fun flyAroundCurrentCenter(googleMap3D: GoogleMap3D?) {
    val camera = googleMap3D?.getCamera()?.toValidCamera() ?: return
    googleMap3D.setCameraAnimationEndListener(null)

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

private fun getAltitudeModeLabel(context: Context, @AltitudeMode mode: Int): String = when (mode) {
    AltitudeMode.RELATIVE_TO_GROUND ->
        context.getString(CommonR.string.altitude_mode_relative_to_ground_code)

    AltitudeMode.RELATIVE_TO_MESH ->
        context.getString(CommonR.string.altitude_mode_relative_to_mesh_code)

    AltitudeMode.CLAMP_TO_GROUND ->
        context.getString(CommonR.string.altitude_mode_clamp_to_ground_code)

    else ->
        context.getString(CommonR.string.altitude_mode_absolute_code)
}

/**
 * Formats the [Camera] state string matching the Kotlin View implementation, including
 * the active target and [AltitudeMode] label.
 */
private fun formatCameraState(
    context: Context,
    camera: Camera,
    @StringRes activeTargetResId: Int,
    @AltitudeMode altitudeMode: Int,
): String {
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
        append("\n")
        append(
            context.getString(
                CommonR.string.camera_target_and_altitude_mode_format,
                context.getString(activeTargetResId),
                getAltitudeModeLabel(context, altitudeMode),
            ),
        )
    }
}
