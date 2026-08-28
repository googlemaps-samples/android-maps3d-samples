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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalViewConfiguration
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

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
    var showHelpDialog by remember { mutableStateOf(false) }
    var lastInteractionTime by remember { mutableStateOf(System.currentTimeMillis()) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.setPlaying(false)
                viewModel.setSpeedBoosted(false)
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

    val viewConfig = LocalViewConfiguration.current
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    lastInteractionTime = System.currentTimeMillis()
                }
            },
    ) {
        GoogleMap3D(
            modifier = Modifier.fillMaxSize(),
            camera = dynamicCamera,
            mapMode = Map3DMode.HYBRID,
            polylines = listOf(staticPolylineConfig, progressPolylineConfig),
        )

        // Custom Gesture Overlay replacing built-in map gestures
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    var lastTapTime = 0L
                    var lastTapX = 0f
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        lastInteractionTime = System.currentTimeMillis()
                        var isDragging = false
                        var isLongPressActive = false
                        var isPinching = false

                        val now = System.currentTimeMillis()
                        val isDoubleTap = (now - lastTapTime < viewConfig.doubleTapTimeoutMillis) &&
                            (abs(down.position.x - lastTapX) < viewConfig.touchSlop * 4)

                        var isDoubleTapHold = false
                        var wasPlayingBeforeShuttle = false

                        val longPressJob = coroutineScope.launch {
                            if (isDoubleTap) {
                                isDoubleTapHold = true
                                wasPlayingBeforeShuttle = viewModel.currentState.isPlaying
                                val isRightSide = down.position.x > 500f
                                viewModel.setPlaying(true)
                                viewModel.setSpeedBoostMultiplier(if (isRightSide) 5.0 else -5.0)
                            } else {
                                delay(viewConfig.longPressTimeoutMillis)
                                if (!isDragging && !isPinching) {
                                    isLongPressActive = true
                                    viewModel.setSpeedBoostMultiplier(2.0)
                                    delay(1500L)
                                    if (isLongPressActive && !isDragging && !isPinching) {
                                        viewModel.setSpeedBoostMultiplier(5.0)
                                    }
                                }
                            }
                        }

                        do {
                            val event = awaitPointerEvent(PointerEventPass.Main)
                            lastInteractionTime = System.currentTimeMillis()
                            val pointers = event.changes.filter { it.pressed }

                            if (pointers.size > 1) {
                                longPressJob.cancel()
                                if (isLongPressActive) {
                                    viewModel.setSpeedBoosted(false)
                                    isLongPressActive = false
                                }
                                isPinching = true

                                val zoom = event.calculateZoom()
                                if (zoom != 1f && zoom > 0.5f && zoom < 2.0f) {
                                    val dampedZoom = 1.0 + (zoom.toDouble() - 1.0) * 0.65
                                    viewModel.adjustRange(dampedZoom)
                                }
                                event.changes.forEach { it.consume() }
                            } else if (pointers.size == 1 && !isPinching) {
                                val change = pointers.first()
                                val pan = change.positionChange()

                                if (!isDragging && (
                                        abs(change.position.x - down.position.x) > viewConfig.touchSlop ||
                                            abs(change.position.y - down.position.y) > viewConfig.touchSlop
                                        )
                                ) {
                                    isDragging = true
                                    longPressJob.cancel()
                                    if (isLongPressActive) {
                                        viewModel.setSpeedBoosted(false)
                                        isLongPressActive = false
                                    }
                                }

                                if (isDragging && !isLongPressActive) {
                                    if (abs(pan.x) > 0.1f) {
                                        viewModel.adjustHeading(pan.x.toDouble() * 0.08)
                                    }
                                    if (abs(pan.y) > 0.1f) {
                                        viewModel.adjustTilt(-pan.y.toDouble() * 0.06)
                                    }
                                    change.consume()
                                }
                            }
                        } while (event.changes.any { it.pressed })

                        val endNow = System.currentTimeMillis()
                        lastInteractionTime = endNow
                        if (isDoubleTapHold) {
                            if (endNow - lastTapTime < 300L) {
                                val isRightSide = down.position.x > 500f
                                viewModel.skipRatio(if (isRightSide) 0.10f else -0.10f)
                            }
                            if (!wasPlayingBeforeShuttle) {
                                viewModel.setPlaying(false)
                            }
                        } else if (!isDragging && !isPinching) {
                            lastTapTime = endNow
                            lastTapX = down.position.x
                        }
                        longPressJob.cancel()
                        viewModel.setSpeedBoostMultiplier(1.0)
                    }
                },
        )

        PathFollowingControlCard(
            state = state,
            lastInteractionTime = lastInteractionTime,
            onUserTouch = { lastInteractionTime = System.currentTimeMillis() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            onTogglePlay = { viewModel.togglePlayPause() },
            onShowHelp = { showHelpDialog = true },
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

        if (showHelpDialog) {
            AlertDialog(
                onDismissRequest = { showHelpDialog = false },
                title = { Text("3D Path Following Controls") },
                text = {
                    Text(
                        "🧭 Camera Gestures:\n" +
                            "• Vertical Sweep (1 Finger): Adjusts camera tilt (0° to 85°)\n" +
                            "• Horizontal Sweep (1 Finger): Rotates camera heading orbit\n" +
                            "• Pinch (2 Fingers): Zooms camera in / out\n\n" +
                            "⚡ Speed & Navigation:\n" +
                            "• Long Press & Hold: 2x Boost (at 0.5s) → 5x Warp Speed (at 2s)\n" +
                            "• Double-Tap & Hold Right: +5x Fast-Forward\n" +
                            "• Double-Tap & Hold Left: -5x Rewind\n" +
                            "• Quick Double-Tap: Skip +/- 10% along path\n" +
                            "• Speed Chips (0.5x - 5x): Instant preset selection\n\n" +
                            "🎛️ Control Panel:\n" +
                            "• Swipe Up / Down or Tap Header: Expand / collapse settings panel\n" +
                            "• Idle Auto-Fade: Panel fades to transparent after 3.5s of inactivity",
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showHelpDialog = false }) {
                        Text("Got It")
                    }
                },
            )
        }
    }
}

/**
 * Collapsible floating control card exposing all path following adjustments.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PathFollowingControlCard(
    state: PathPlaybackState,
    lastInteractionTime: Long,
    onUserTouch: () -> Unit,
    modifier: Modifier = Modifier,
    onTogglePlay: () -> Unit,
    onShowHelp: () -> Unit,
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
    var isIdle by remember { mutableStateOf(false) }

    LaunchedEffect(lastInteractionTime) {
        isIdle = false
        delay(3500L)
        isIdle = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (isIdle) 0.35f else 1.0f,
        animationSpec = tween(durationMillis = 500),
        label = "controlCardAlpha",
    )

    Card(
        modifier = modifier
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    onUserTouch()
                }
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = alpha),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            // Drag Handle Affordance
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    .align(Alignment.CenterHorizontally),
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Header: Title, Help, and Collapse Toggle (Clickable row & Swipe up/down)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isCollapsed = !isCollapsed }
                    .pointerInput(Unit) {
                        var totalDragY = 0f
                        detectVerticalDragGestures(
                            onDragStart = {
                                totalDragY = 0f
                                onUserTouch()
                            },
                            onDragEnd = {
                                if (totalDragY > 40f && !isCollapsed) {
                                    isCollapsed = true // Swipe down -> collapse
                                } else if (totalDragY < -40f && isCollapsed) {
                                    isCollapsed = false // Swipe up -> expand
                                }
                            },
                            onVerticalDrag = { _, dragAmount ->
                                totalDragY += dragAmount
                                onUserTouch()
                            },
                        )
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Path Following Controls",
                    style = MaterialTheme.typography.titleMedium,
                )
                Row {
                    IconButton(onClick = onShowHelp, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = "Help",
                        )
                    }
                    IconButton(onClick = { isCollapsed = !isCollapsed }, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = if (isCollapsed) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isCollapsed) "Expand" else "Collapse",
                        )
                    }
                }
            }

            // Speed Preset Chips (Always visible)
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                listOf(15f to "0.5x", 30f to "1x", 60f to "2x", 90f to "3x", 120f to "5x").forEach { (speed, label) ->
                    FilterChip(
                        selected = abs(state.followSpeedMps.toFloat() - speed) < 1f,
                        onClick = {
                            onSpeedChange(speed)
                            onUserTouch()
                        },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                    )
                }
            }

            // Persistent Play/Pause & Progress Slider Row (Always visible)
            val progressInteractionSource = remember { MutableInteractionSource() }
            val isProgressDragged by progressInteractionSource.collectIsDraggedAsState()

            LaunchedEffect(isProgressDragged) {
                onScrubbingChange(isProgressDragged)
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                IconButton(onClick = {
                    onTogglePlay()
                    onUserTouch()
                }, modifier = Modifier.size(44.dp)) {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (state.isPlaying) "Pause" else "Play",
                    )
                }

                Slider(
                    value = state.progressRatio,
                    onValueChange = {
                        onSeekRatio(it)
                        onUserTouch()
                    },
                    valueRange = 0f..1f,
                    interactionSource = progressInteractionSource,
                    modifier = Modifier.weight(1f),
                )
            }

            // Expandable Settings Section
            AnimatedVisibility(visible = !isCollapsed) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Environment Selection
                    Text("Path Environment:", style = MaterialTheme.typography.labelLarge)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                onEnvironmentChange(true)
                                onUserTouch()
                            },
                        ) {
                            RadioButton(
                                selected = state.route == PathData.URBAN_PATH,
                                onClick = {
                                    onEnvironmentChange(true)
                                    onUserTouch()
                                },
                            )
                            Text("Urban (SF)")
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                onEnvironmentChange(false)
                                onUserTouch()
                            },
                        ) {
                            RadioButton(
                                selected = state.route == PathData.RURAL_PATH,
                                onClick = {
                                    onEnvironmentChange(false)
                                    onUserTouch()
                                },
                            )
                            Text("Rural (Marin)")
                        }
                    }

                    // Altitude Mode
                    Text("Altitude Mode:", style = MaterialTheme.typography.labelLarge)
                    Column {
                        AltitudeModeOption.values().forEach { modeOption ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onAltitudeModeChange(modeOption)
                                        onUserTouch()
                                    },
                            ) {
                                RadioButton(
                                    selected = state.altitudeMode == modeOption.mode,
                                    onClick = {
                                        onAltitudeModeChange(modeOption)
                                        onUserTouch()
                                    },
                                )
                                Text(modeOption.label)
                            }
                        }
                    }

                    // Occlusion Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Draws Occluded Segments")
                        Switch(
                            checked = state.drawsOccludedSegments,
                            onCheckedChange = {
                                onOcclusionChange(it)
                                onUserTouch()
                            },
                        )
                    }

                    // Path Altitude Offset Slider
                    val maxPathAlt = if (state.route == PathData.URBAN_PATH) 20f else 200f
                    Text("Path Height Offset: ${state.pathAltitudeOffset.toInt()} m")
                    Slider(
                        value = state.pathAltitudeOffset.toFloat().coerceIn(0f, maxPathAlt),
                        onValueChange = {
                            onPathAltitudeOffsetChange(it)
                            onUserTouch()
                        },
                        valueRange = 0f..maxPathAlt,
                    )

                    // Camera Range Slider
                    Text("Camera Range: ${state.cameraRange.toInt()} m")
                    Slider(
                        value = state.cameraRange.toFloat().coerceIn(50f, 1500f),
                        onValueChange = {
                            onCameraRangeChange(it)
                            onUserTouch()
                        },
                        valueRange = 50f..1500f,
                    )

                    // Ground Altitude Slider
                    Text("Ground Altitude: ${state.groundAltitude.toInt()} m")
                    Slider(
                        value = state.groundAltitude.toFloat().coerceIn(0f, 500f),
                        onValueChange = {
                            onGroundAltitudeChange(it)
                            onUserTouch()
                        },
                        valueRange = 0f..500f,
                    )

                    // Camera Heading Offset Slider
                    Text("Heading Offset: ${state.headingOffset.toInt()}°")
                    Slider(
                        value = state.headingOffset.toFloat().coerceIn(-180f, 180f),
                        onValueChange = {
                            onHeadingOffsetChange(it)
                            onUserTouch()
                        },
                        valueRange = -180f..180f,
                    )

                    // Camera Tilt Slider
                    Text("Camera Tilt: ${state.cameraTilt.toInt()}°")
                    Slider(
                        value = state.cameraTilt.toFloat().coerceIn(0f, 85f),
                        onValueChange = {
                            onCameraTiltChange(it)
                            onUserTouch()
                        },
                        valueRange = 0f..85f,
                    )

                    // Follow Speed Slider
                    val boostSuffix = when {
                        state.speedBoostMultiplier >= 4.5 -> " (5x Fast-Forward)"
                        state.speedBoostMultiplier <= -4.5 -> " (-5x Rewind)"
                        state.speedBoostMultiplier >= 1.5 -> " (2x Boost)"
                        else -> ""
                    }
                    Text("Follow Speed: ${state.followSpeedMps.toInt()} m/s$boostSuffix")
                    Slider(
                        value = state.followSpeedMps.toFloat().coerceIn(5f, 200f),
                        onValueChange = {
                            onSpeedChange(it)
                            onUserTouch()
                        },
                        valueRange = 5f..200f,
                    )
                }
            }
        }
    }
}
