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

package com.example.composedemos.pathfollowing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.maps3d.common.PathData
import com.example.maps3d.common.PathEngine
import com.example.maps3dcommon.R
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.maps.android.compose3d.GoogleMap3D
import com.google.maps.android.compose3d.PolylineConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

class PathFollowingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    PathFollowingScreen()
                }
            }
        }
    }

    companion object {
        val URBAN_PATH: List<LatLngAltitude> = PathData.URBAN_PATH
        val RURAL_PATH: List<LatLngAltitude> = PathData.RURAL_PATH
    }
}

enum class AltitudeModeOption(val label: String, val mode: Int) {
    CLAMP_TO_GROUND("Clamp to Ground", AltitudeMode.CLAMP_TO_GROUND),
    RELATIVE_TO_GROUND("Rel to Ground", AltitudeMode.RELATIVE_TO_GROUND),
    RELATIVE_TO_MESH("Rel to Mesh", AltitudeMode.RELATIVE_TO_MESH),
    ABSOLUTE("Absolute", AltitudeMode.ABSOLUTE),
}

/**
 * Main Path Following demo screen orchestrating 3D map rendering and interactive controls.
 */
@Composable
fun PathFollowingScreen() {
    var isMapSteady by remember { mutableStateOf(false) }
    var currentPath by remember { mutableStateOf(PathData.URBAN_PATH) }
    var selectedAltitudeMode by remember { mutableStateOf(AltitudeModeOption.CLAMP_TO_GROUND) }
    var drawsOccludedSegments by remember { mutableStateOf(true) }
    var pathAltitudeOffset by remember { mutableFloatStateOf(0.5f) }

    var cameraRange by remember { mutableFloatStateOf(300f) }
    var groundAltitude by remember { mutableFloatStateOf(20f) }
    var headingOffset by remember { mutableFloatStateOf(0f) }
    var cameraTilt by remember { mutableFloatStateOf(70f) }
    var followSpeedMps by remember { mutableFloatStateOf(30f) }

    var isPlaying by remember { mutableStateOf(false) }
    var isUserScrubbing by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var elapsedDistance by remember { mutableDoubleStateOf(0.0) }
    var currentHeading by remember { mutableStateOf<Double?>(null) }

    val cumulativeDistances = remember(currentPath) {
        PathEngine.calculateCumulativeDistances(path = currentPath)
    }
    val totalDistance = cumulativeDistances.lastOrNull() ?: 0.0
    val baseAltitude = if (currentPath == PathData.RURAL_PATH) 45.0 else 50.0

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                isPlaying = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Frame animation loop with delta-time integration
    LaunchedEffect(isPlaying, totalDistance, followSpeedMps) {
        if (!isPlaying || totalDistance <= 0.0) return@LaunchedEffect
        var lastTimeNanos = 0L

        while (isActive && isPlaying) {
            withFrameMillis { frameTimeMillis ->
                val nowNanos = frameTimeMillis * 1_000_000L
                if (lastTimeNanos == 0L) {
                    lastTimeNanos = nowNanos
                    return@withFrameMillis
                }
                val dt = (nowNanos - lastTimeNanos) / 1_000_000_000.0
                lastTimeNanos = nowNanos

                val stepDistance = followSpeedMps * dt
                elapsedDistance = (elapsedDistance + stepDistance) % totalDistance

                if (!isUserScrubbing && totalDistance > 0) {
                    progress = (elapsedDistance / totalDistance).toFloat().coerceIn(0f, 1f)
                }
            }
        }
    }

    // Interpolate camera and position
    val interpPoint = remember(currentPath, cumulativeDistances, elapsedDistance) {
        PathEngine.interpolatePoint(path = currentPath, cumulativeDistances = cumulativeDistances, distance = elapsedDistance)
    }

    val targetHeading = remember(interpPoint.bearing, headingOffset, currentHeading, isUserScrubbing, isPlaying) {
        val smoothed = PathEngine.smoothHeading(
            interpPoint.bearing + headingOffset,
            currentHeading,
            isUserScrubbing,
            isPlaying,
        )
        currentHeading = smoothed
        smoothed
    }

    val cameraTargetAltitude = remember(selectedAltitudeMode, baseAltitude, interpPoint.altitude, groundAltitude) {
        PathEngine.calculateCameraAltitude(
            selectedAltitudeMode.mode,
            baseAltitude,
            interpPoint.altitude,
            groundAltitude.toDouble(),
        )
    }

    val dynamicCamera = remember(interpPoint.latLng, targetHeading, cameraTilt, cameraRange, cameraTargetAltitude) {
        camera {
            center = latLngAltitude {
                latitude = interpPoint.latLng.latitude
                longitude = interpPoint.latLng.longitude
                altitude = cameraTargetAltitude
            }
            heading = targetHeading
            tilt = cameraTilt.toDouble()
            range = cameraRange.toDouble()
            roll = 0.0
        }
    }

    // Dual-Polyline Configurations
    val staticPolylineConfig = remember(currentPath, selectedAltitudeMode, pathAltitudeOffset, drawsOccludedSegments, baseAltitude) {
        val vertices = PathEngine.buildStaticVertices(
            path = currentPath,
            altitudeMode = selectedAltitudeMode.mode,
            baseAltitude = baseAltitude,
            pathAltitudeOffset = pathAltitudeOffset.toDouble(),
        )
        PolylineConfig(
            key = PathEngine.STATIC_POLYLINE_ID,
            points = vertices,
            color = "#4285F4".toColorInt(), // Wide blue route (16dp)
            width = 16f,
            zIndex = 1,
            altitudeMode = selectedAltitudeMode.mode,
            drawsOccludedSegments = drawsOccludedSegments,
        )
    }

    val progressPolylineConfig = remember(
        currentPath, cumulativeDistances, elapsedDistance, interpPoint,
        selectedAltitudeMode, pathAltitudeOffset, drawsOccludedSegments, baseAltitude, totalDistance,
    ) {
        if (currentPath.isEmpty() || totalDistance <= 0.0) return@remember null
        val vertices = PathEngine.buildProgressVertices(
            currentPath,
            cumulativeDistances,
            elapsedDistance,
            interpPoint.latLng,
            interpPoint.waypointIndex,
            selectedAltitudeMode.mode,
            baseAltitude,
            pathAltitudeOffset.toDouble(),
        )
        PolylineConfig(
            key = PathEngine.PROGRESS_POLYLINE_ID,
            points = vertices,
            color = "#9C27B0".toColorInt(), // Narrow purple progress (8dp)
            width = 8f,
            zIndex = 2,
            altitudeMode = selectedAltitudeMode.mode,
            drawsOccludedSegments = drawsOccludedSegments,
        )
    }

    val polylines = remember(staticPolylineConfig, progressPolylineConfig) {
        listOfNotNull(staticPolylineConfig, progressPolylineConfig)
    }

    // Control card collapse and auto-fade state
    var isCollapsed by remember { mutableStateOf(false) }
    var isTouching by remember { mutableStateOf(false) }
    var isCardIdle by remember { mutableStateOf(false) }

    LaunchedEffect(isTouching, isCollapsed) {
        if (!isTouching && !isCollapsed) {
            delay(3000L)
            isCardIdle = true
        } else {
            isCardIdle = false
        }
    }

    val controlsAlpha by animateFloatAsState(
        targetValue = if (isCardIdle && !isCollapsed) 0.8f else 1.0f,
        animationSpec = tween(durationMillis = 300),
        label = "controlsAlpha",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = if (isMapSteady) "MapSteady" else "MapLoading" },
    ) {
        GoogleMap3D(
            camera = dynamicCamera,
            polylines = polylines,
            modifier = Modifier.fillMaxSize(),
            onMapSteady = { isMapSteady = true },
        )

        PathFollowingControlCard(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth()
                .alpha(controlsAlpha)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isTouching = true
                            tryAwaitRelease()
                            isTouching = false
                        },
                    )
                },
            isCollapsed = isCollapsed,
            onToggleCollapse = { isCollapsed = !isCollapsed },
            currentPath = currentPath,
            onSwitchEnvironment = { newPath ->
                isPlaying = false
                currentHeading = null
                elapsedDistance = 0.0
                progress = 0f
                if (newPath == PathData.RURAL_PATH) {
                    cameraRange = 450f
                    groundAltitude = 40f
                    cameraTilt = 75f
                } else {
                    cameraRange = 300f
                    groundAltitude = 20f
                    cameraTilt = 70f
                }
                currentPath = newPath
            },
            selectedAltitudeMode = selectedAltitudeMode,
            onAltitudeModeChange = { selectedAltitudeMode = it },
            drawsOccludedSegments = drawsOccludedSegments,
            onDrawsOccludedChange = { drawsOccludedSegments = it },
            pathAltitudeOffset = pathAltitudeOffset,
            onPathAltitudeOffsetChange = { pathAltitudeOffset = it },
            isPlaying = isPlaying,
            onPlayPauseToggle = { isPlaying = !isPlaying },
            progress = progress,
            onProgressChange = {
                progress = it
                elapsedDistance = totalDistance * it.toDouble()
            },
            onProgressScrubbingChange = { isUserScrubbing = it },
            cameraRange = cameraRange,
            onCameraRangeChange = { cameraRange = it },
            groundAltitude = groundAltitude,
            onGroundAltitudeChange = { groundAltitude = it },
            headingOffset = headingOffset,
            onHeadingOffsetChange = { headingOffset = it },
            cameraTilt = cameraTilt,
            onCameraTiltChange = { cameraTilt = it },
            followSpeedMps = followSpeedMps,
            onFollowSpeedChange = { followSpeedMps = it },
        )
    }
}

/**
 * Floating collapsible control panel for path following parameters.
 */
@Composable
private fun PathFollowingControlCard(
    modifier: Modifier = Modifier,
    isCollapsed: Boolean,
    onToggleCollapse: () -> Unit,
    currentPath: List<LatLngAltitude>,
    onSwitchEnvironment: (List<LatLngAltitude>) -> Unit,
    selectedAltitudeMode: AltitudeModeOption,
    onAltitudeModeChange: (AltitudeModeOption) -> Unit,
    drawsOccludedSegments: Boolean,
    onDrawsOccludedChange: (Boolean) -> Unit,
    pathAltitudeOffset: Float,
    onPathAltitudeOffsetChange: (Float) -> Unit,
    isPlaying: Boolean,
    onPlayPauseToggle: () -> Unit,
    progress: Float,
    onProgressChange: (Float) -> Unit,
    onProgressScrubbingChange: (Boolean) -> Unit,
    cameraRange: Float,
    onCameraRangeChange: (Float) -> Unit,
    groundAltitude: Float,
    onGroundAltitudeChange: (Float) -> Unit,
    headingOffset: Float,
    onHeadingOffsetChange: (Float) -> Unit,
    cameraTilt: Float,
    onCameraTiltChange: (Float) -> Unit,
    followSpeedMps: Float,
    onFollowSpeedChange: (Float) -> Unit,
) {
    Card(
        modifier = modifier.animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleCollapse() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Path Following Controls",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                IconButton(onClick = onToggleCollapse) {
                    Icon(
                        imageVector = if (isCollapsed) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isCollapsed) "Expand Controls" else "Collapse Controls",
                    )
                }
            }

            AnimatedVisibility(visible = !isCollapsed) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    // Environment Selection
                    Text("Environment", style = MaterialTheme.typography.labelMedium)
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .selectable(
                                    selected = currentPath == PathData.URBAN_PATH,
                                    onClick = { onSwitchEnvironment(PathData.URBAN_PATH) },
                                    role = Role.RadioButton,
                                )
                                .padding(end = 16.dp),
                        ) {
                            RadioButton(
                                selected = currentPath == PathData.URBAN_PATH,
                                onClick = null,
                            )
                            Text("Urban (SF)", modifier = Modifier.padding(start = 4.dp))
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.selectable(
                                selected = currentPath == PathData.RURAL_PATH,
                                onClick = { onSwitchEnvironment(PathData.RURAL_PATH) },
                                role = Role.RadioButton,
                            ),
                        ) {
                            RadioButton(
                                selected = currentPath == PathData.RURAL_PATH,
                                onClick = null,
                            )
                            Text("Rural", modifier = Modifier.padding(start = 4.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Altitude Mode Selection
                    Text("Altitude Mode", style = MaterialTheme.typography.labelMedium)
                    Column {
                        AltitudeModeOption.entries.chunked(2).forEach { rowOptions ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                rowOptions.forEach { opt ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .weight(1f)
                                            .selectable(
                                                selected = selectedAltitudeMode == opt,
                                                onClick = { onAltitudeModeChange(opt) },
                                                role = Role.RadioButton,
                                            ),
                                    ) {
                                        RadioButton(
                                            selected = selectedAltitudeMode == opt,
                                            onClick = null,
                                        )
                                        Text(opt.label, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Occlusion toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Draw Occluded Segments", style = MaterialTheme.typography.labelMedium)
                        Switch(
                            checked = drawsOccludedSegments,
                            onCheckedChange = onDrawsOccludedChange,
                        )
                    }

                    // Path Height Offset
                    Text(
                        stringResource(R.string.path_height_format, pathAltitudeOffset),
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Slider(
                        value = pathAltitudeOffset,
                        onValueChange = onPathAltitudeOffsetChange,
                        valueRange = 0f..10f,
                    )

                    // Play / Pause & Progress Scrubbing
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Button(onClick = onPlayPauseToggle) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isPlaying) "Pause" else "Play")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Slider(
                            value = progress,
                            onValueChange = onProgressChange,
                            onValueChangeFinished = { onProgressScrubbingChange(false) },
                            modifier = Modifier.weight(1f),
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Camera Controls
                    Text(
                        stringResource(R.string.camera_range_format, cameraRange.toInt()),
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Slider(
                        value = cameraRange,
                        onValueChange = onCameraRangeChange,
                        valueRange = 50f..1000f,
                    )

                    Text(
                        stringResource(R.string.ground_altitude_format, groundAltitude.toInt()),
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Slider(
                        value = groundAltitude,
                        onValueChange = onGroundAltitudeChange,
                        valueRange = 2f..if (currentPath == PathData.RURAL_PATH) 2000f else 200f,
                    )

                    Text(
                        stringResource(R.string.heading_offset_format, headingOffset.toInt()),
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Slider(
                        value = headingOffset,
                        onValueChange = onHeadingOffsetChange,
                        valueRange = -180f..180f,
                    )

                    Text(
                        stringResource(R.string.camera_tilt_format, cameraTilt.toInt()),
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Slider(
                        value = cameraTilt,
                        onValueChange = onCameraTiltChange,
                        valueRange = 0f..85f,
                    )

                    Text(
                        stringResource(R.string.follow_speed_format, followSpeedMps.toInt()),
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Slider(
                        value = followSpeedMps,
                        onValueChange = onFollowSpeedChange,
                        valueRange = 5f..100f,
                    )
                }
            }
        }
    }
}
