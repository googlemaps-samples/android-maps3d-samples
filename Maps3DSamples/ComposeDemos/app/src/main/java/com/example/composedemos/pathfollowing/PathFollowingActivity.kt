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

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.maps3d.common.PathData
import com.example.maps3d.common.PathEngine
import com.example.maps3d.common.PathFollowingViewModel
import com.example.maps3d.common.PathPlaybackState
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.maps.android.compose3d.GoogleMap3D
import com.google.maps.android.compose3d.PolylineConfig
import kotlinx.coroutines.isActive

class PathFollowingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        PathFollowingScreen()
                    }
                }
            }
        }
    }
}

enum class AltitudeModeOption(val label: String, val mode: Int) {
    CLAMP_TO_GROUND("Clamp to Ground", AltitudeMode.CLAMP_TO_GROUND),
    RELATIVE_TO_GROUND("Rel to Ground", AltitudeMode.RELATIVE_TO_GROUND),
    RELATIVE_TO_MESH("Rel to Mesh", AltitudeMode.RELATIVE_TO_MESH),
    ABSOLUTE("Absolute", AltitudeMode.ABSOLUTE),
}

/**
 * Main Path Following demo screen orchestrating 3D map rendering and interactive controls
 * driven by [PathFollowingViewModel].
 */
@Composable
fun PathFollowingScreen(
    viewModel: PathFollowingViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var isMapSteady by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.setPlaying(false)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Hardware VSYNC-synced frame loop using withFrameMillis
    LaunchedEffect(state.isPlaying) {
        if (!state.isPlaying) return@LaunchedEffect
        var lastTimeNanos = 0L

        while (isActive && state.isPlaying) {
            withFrameMillis { frameTimeMillis ->
                val nowNanos = frameTimeMillis * 1_000_000L
                if (lastTimeNanos == 0L) {
                    lastTimeNanos = nowNanos
                    return@withFrameMillis
                }
                val dt = (nowNanos - lastTimeNanos) / 1_000_000_000.0
                lastTimeNanos = nowNanos

                viewModel.advance(dt)
            }
        }
    }

    val dynamicCamera = remember(state.currentPosition, state.effectiveHeading, state.cameraTilt, state.cameraRange, state.cameraTargetAltitude) {
        camera {
            center = latLngAltitude {
                latitude = state.currentPosition.latitude
                longitude = state.currentPosition.longitude
                altitude = state.cameraTargetAltitude
            }
            heading = state.effectiveHeading
            tilt = state.cameraTilt
            range = state.cameraRange
            roll = 0.0
        }
    }

    val staticPolylineConfig = remember(state.staticPolylineVertices, state.altitudeMode, state.drawsOccludedSegments) {
        PolylineConfig(
            key = PathEngine.STATIC_POLYLINE_ID,
            points = state.staticPolylineVertices,
            width = 16f,
            color = Color.parseColor("#4285F4"),
            altitudeMode = state.altitudeMode,
            drawsOccludedSegments = state.drawsOccludedSegments,
            zIndex = 1,
        )
    }

    val progressPolylineConfig = remember(state.progressPolylineVertices, state.altitudeMode, state.drawsOccludedSegments) {
        PolylineConfig(
            key = PathEngine.PROGRESS_POLYLINE_ID,
            points = state.progressPolylineVertices,
            width = 8f,
            color = Color.parseColor("#9C27B0"),
            altitudeMode = state.altitudeMode,
            drawsOccludedSegments = state.drawsOccludedSegments,
            zIndex = 2,
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap3D(
            modifier = Modifier.fillMaxSize(),
            camera = dynamicCamera,
            mapMode = Map3DMode.HYBRID,
            polylines = listOf(staticPolylineConfig, progressPolylineConfig),
            onMapSteady = { isMapSteady = true },
        )

        PathFollowingControlCard(
            state = state,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            onTogglePlay = { viewModel.togglePlayPause() },
            onSeekRatio = { viewModel.seekToRatio(it) },
            onScrubbingChange = { viewModel.setScrubbing(it) },
            onAltitudeModeChange = { viewModel.setAltitudeMode(it.mode) },
            onOcclusionChange = { viewModel.setDrawsOccludedSegments(it) },
            onPathAltitudeOffsetChange = { viewModel.setPathAltitudeOffset(it.toDouble()) },
            onCameraRangeChange = { viewModel.setCameraRange(it.toDouble()) },
            onGroundAltitudeChange = { viewModel.setGroundAltitude(it.toDouble()) },
            onHeadingOffsetChange = { viewModel.setHeadingOffset(it.toDouble()) },
            onCameraTiltChange = { viewModel.setCameraTilt(it.toDouble()) },
            onSpeedChange = { viewModel.setFollowSpeed(it.toDouble()) },
            onEnvironmentChange = { isUrban ->
                viewModel.setRoute(if (isUrban) PathData.URBAN_PATH else PathData.RURAL_PATH)
            },
        )
    }
}

/**
 * Collapsible floating control card exposing all path following adjustments.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PathFollowingControlCard(
    state: PathPlaybackState,
    modifier: Modifier = Modifier,
    onTogglePlay: () -> Unit,
    onSeekRatio: (Float) -> Unit,
    onScrubbingChange: (Boolean) -> Unit,
    onAltitudeModeChange: (AltitudeModeOption) -> Unit,
    onOcclusionChange: (Boolean) -> Unit,
    onPathAltitudeOffsetChange: (Float) -> Unit,
    onCameraRangeChange: (Float) -> Unit,
    onGroundAltitudeChange: (Float) -> Unit,
    onHeadingOffsetChange: (Float) -> Unit,
    onCameraTiltChange: (Float) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onEnvironmentChange: (Boolean) -> Unit,
) {
    var isCollapsed by remember { mutableStateOf(false) }
    var isInteracting by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(
        targetValue = if (isInteracting || isCollapsed) 1.0f else 0.85f,
        animationSpec = tween(durationMillis = 300),
        label = "controlCardAlpha",
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = alpha),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            // Header: Title, Play/Pause, and Collapse Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Path Following Controls",
                    style = MaterialTheme.typography.titleMedium,
                )
                Row {
                    IconButton(onClick = onTogglePlay) {
                        Icon(
                            imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (state.isPlaying) "Pause" else "Play",
                        )
                    }
                    IconButton(onClick = { isCollapsed = !isCollapsed }) {
                        Icon(
                            imageVector = if (isCollapsed) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isCollapsed) "Expand" else "Collapse",
                        )
                    }
                }
            }

            // Progress Slider
            val progressInteractionSource = remember { MutableInteractionSource() }
            val isDragged by progressInteractionSource.collectIsDraggedAsState()
            LaunchedEffect(isDragged) {
                onScrubbingChange(isDragged)
            }

            Slider(
                value = state.progressRatio,
                onValueChange = { onSeekRatio(it) },
                interactionSource = progressInteractionSource,
                modifier = Modifier.fillMaxWidth(),
            )

            AnimatedVisibility(visible = !isCollapsed) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Altitude Mode Selection
                    Text(
                        text = "Altitude Mode",
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        AltitudeModeOption.entries.forEach { option ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = state.altitudeMode == option.mode,
                                    onClick = { onAltitudeModeChange(option) },
                                )
                                Text(
                                    text = option.label,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Occlusion Handling Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Draw Occluded Segments",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Switch(
                            checked = state.drawsOccludedSegments,
                            onCheckedChange = onOcclusionChange,
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Path Altitude Offset Slider
                    val maxOffset = if (state.route == PathData.RURAL_PATH) 200f else 20f
                    Text(
                        text = "Path Elevation Offset: %.1f m".format(state.pathAltitudeOffset),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Slider(
                        value = state.pathAltitudeOffset.toFloat(),
                        onValueChange = onPathAltitudeOffsetChange,
                        valueRange = 0f..maxOffset,
                    )

                    // Camera Range Slider
                    Text(
                        text = "Camera Distance: %d m".format(state.cameraRange.toInt()),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Slider(
                        value = state.cameraRange.toFloat(),
                        onValueChange = onCameraRangeChange,
                        valueRange = 50f..1000f,
                    )

                    // Ground Altitude Slider
                    val maxGroundAlt = if (state.route == PathData.RURAL_PATH) 200f else 100f
                    Text(
                        text = "Camera Altitude: %d m".format(state.groundAltitude.toInt()),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Slider(
                        value = state.groundAltitude.toFloat(),
                        onValueChange = onGroundAltitudeChange,
                        valueRange = 5f..maxGroundAlt,
                    )

                    // Heading Offset Slider
                    Text(
                        text = "Heading Offset: %d°".format(state.headingOffset.toInt()),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Slider(
                        value = state.headingOffset.toFloat(),
                        onValueChange = onHeadingOffsetChange,
                        valueRange = -180f..180f,
                    )

                    // Camera Tilt Slider
                    Text(
                        text = "Camera Tilt: %d°".format(state.cameraTilt.toInt()),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Slider(
                        value = state.cameraTilt.toFloat(),
                        onValueChange = onCameraTiltChange,
                        valueRange = 0f..85f,
                    )

                    // Speed Slider
                    Text(
                        text = "Follow Speed: %d m/s".format(state.followSpeedMps.toInt()),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Slider(
                        value = state.followSpeedMps.toFloat(),
                        onValueChange = onSpeedChange,
                        valueRange = 5f..120f,
                    )

                    // Environment Route Selection
                    Text(
                        text = "Environment",
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = state.route == PathData.URBAN_PATH,
                                onClick = { onEnvironmentChange(true) },
                            )
                            Text(
                                text = "Urban (SF)",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Spacer(modifier = Modifier.size(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = state.route == PathData.RURAL_PATH,
                                onClick = { onEnvironmentChange(false) },
                            )
                            Text(
                                text = "Rural",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }
    }
}
