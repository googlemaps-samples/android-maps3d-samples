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

package com.example.composedemos.advancedcameraanimation

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.TypedValue
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.maps3d.common.AdvancedCameraAnimationViewModel
import com.example.maps3d.common.AnimationApproach
import com.example.maps3d.common.CameraKeyframe
import com.example.maps3d.common.EntityPose
import com.example.maps3d.common.SimpleFlyToMode
import com.example.maps3d.common.StationaryCameraTracker
import com.example.maps3d.common.TourData
import com.example.maps3d.common.TrajectoryFlightAnimator
import com.example.maps3d.common.WorldState
import com.example.maps3d.common.awaitCameraUpdate
import com.example.maps3d.common.showcase.ui.SampleTopBar
import com.example.maps3d.common.toCameraUpdate
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.flyAroundOptions
import com.google.android.gms.maps3d.model.flyToOptions
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.maps.android.compose3d.GoogleMap3D
import com.google.maps.android.compose3d.ModelConfig
import com.google.maps.android.compose3d.ModelScale
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import com.example.maps3dcommon.R as CommonR

/**
 * Jetpack Compose implementation of Advanced Camera Animation demo.
 */
class AdvancedCameraAnimationActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Scaffold(
                    topBar = {
                        SampleTopBar(
                            title = "Advanced Camera Animation",
                            sampleId = "advanced_camera_animation",
                        )
                    },
                    modifier = Modifier.fillMaxSize(),
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        AdvancedCameraAnimationScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun AdvancedCameraAnimationScreen(
    viewModel: AdvancedCameraAnimationViewModel = viewModel(),
) {
    val state by viewModel.worldState.collectAsStateWithLifecycle()
    var googleMap3D by remember { mutableStateOf<GoogleMap3D?>(null) }

    val initialCamera = remember {
        camera {
            center = latLngAltitude {
                latitude = TourData.AIRPLANE_FLIGHT_PATH.first().latitude
                longitude = TourData.AIRPLANE_FLIGHT_PATH.first().longitude
                altitude = 250.0
            }
            heading = 106.2
            tilt = 65.0
            range = 600.0
        }
    }

    var currentCamera by remember { mutableStateOf(initialCamera) }

    // Synchronize 3D Airplane model entity dynamically from WorldState
    val models = remember(state.entities) {
        val planePose = state.getEntityPose(TourData.AIRPLANE_MODEL_ID)
        if (planePose != null) {
            listOf(
                ModelConfig(
                    key = TourData.AIRPLANE_MODEL_ID,
                    position = planePose.position,
                    url = TourData.AIRPLANE_MODEL_URL,
                    altitudeMode = AltitudeMode.ABSOLUTE,
                    scale = ModelScale.Uniform(planePose.scale.toFloat()),
                    heading = planePose.heading,
                    tilt = planePose.pitch,
                    roll = planePose.roll,
                ),
            )
        } else {
            emptyList()
        }
    }

    // High-Rate animation loops
    LaunchedEffect(state.selectedApproach, state.isPlaying, googleMap3D) {
        val map = googleMap3D ?: return@LaunchedEffect
        if (!state.isPlaying) return@LaunchedEffect

        when (state.selectedApproach) {
            AnimationApproach.SIMPLE_FLY_TO -> {
                val targetLoc = TourData.AIRPLANE_FLIGHT_PATH.last()
                val targetCam = camera {
                    center = latLngAltitude {
                        latitude = targetLoc.latitude
                        longitude = targetLoc.longitude
                        altitude = 250.0
                    }
                    heading = 286.2 // Facing back toward Golden Gate Bridge
                    tilt = 65.0
                    range = 600.0
                }
                val finalPose = EntityPose(
                    position = LatLngAltitude(targetLoc.latitude, targetLoc.longitude, 250.0),
                    heading = TrajectoryFlightAnimator.normalizeHeading(106.2 + 180.0),
                    pitch = -90.0,
                    roll = 0.0,
                    scale = 0.08,
                )
                val options = flyToOptions {
                    endCamera = targetCam
                    durationInMillis = 5000L
                }

                if (state.simpleFlyToMode == SimpleFlyToMode.MIDPOINT_JUMP) {
                    val jumpJob = launch {
                        delay(2500L)
                        viewModel.updateAirplanePose(finalPose)
                    }
                    awaitCameraUpdate(map, options.toCameraUpdate())
                    jumpJob.cancel()
                } else {
                    val animator = TrajectoryFlightAnimator(TourData.AIRPLANE_FLIGHT_PATH, altitude = 250.0, scale = 0.08)
                    val startMs = System.currentTimeMillis()
                    val flightJob = launch {
                        while (isActive && state.isPlaying) {
                            val elapsed = System.currentTimeMillis() - startMs
                            val pose = animator.update(elapsed, 5000L)
                            viewModel.updateAirplanePose(pose)
                            if (animator.isFinished(elapsed, 5000L)) break
                            delay(16L)
                        }
                    }
                    awaitCameraUpdate(map, options.toCameraUpdate())
                    flightJob.cancel()
                }
                currentCamera = targetCam
                viewModel.updateAirplanePose(finalPose)
                viewModel.onNativeCameraAnimationFinished()
            }

            AnimationApproach.DISPATCHER_FRAME_LOOP, AnimationApproach.ORBIT_360_SPIN -> {
                var lastFrame = System.nanoTime()
                while (isActive && state.isPlaying) {
                    withFrameMillis {
                        val now = System.nanoTime()
                        val dt = (now - lastFrame) / 1_000_000_000.0
                        viewModel.tick(dt.coerceIn(0.001, 0.1))
                        lastFrame = now
                    }
                    map.setCamera(state.camera)
                    currentCamera = state.camera
                }
            }

            AnimationApproach.KEYFRAME_TOUR -> {
                for (index in TourData.SAN_FRANCISCO_TOUR.indices) {
                    if (!isActive || !state.isPlaying) break
                    viewModel.setKeyframeStep(index)

                    when (val step = TourData.SAN_FRANCISCO_TOUR[index]) {
                        is CameraKeyframe.FlyTo -> {
                            val options = flyToOptions {
                                endCamera = step.targetCamera
                                durationInMillis = step.durationMs
                            }
                            awaitCameraUpdate(map, options.toCameraUpdate())
                            currentCamera = step.targetCamera
                        }

                        is CameraKeyframe.DwellPause -> {
                            delay(step.durationMs)
                        }

                        is CameraKeyframe.FlyAround -> {
                            val options = flyAroundOptions {
                                center = step.centerCamera
                                rounds = step.rounds
                                durationInMillis = step.durationMs
                            }
                            awaitCameraUpdate(map, options.toCameraUpdate())
                            currentCamera = step.centerCamera
                        }

                        is CameraKeyframe.StationaryTrackingFlight -> {
                            val toVantageOptions = flyToOptions {
                                endCamera = step.observationCamera
                                durationInMillis = 2000L
                            }
                            awaitCameraUpdate(map, toVantageOptions.toCameraUpdate())
                            currentCamera = step.observationCamera

                            val tracker = StationaryCameraTracker.fromInitialCamera(step.observationCamera)
                            val flightAnimator = TrajectoryFlightAnimator(step.flightPath, altitude = 250.0, scale = 0.08)
                            val startMs = System.currentTimeMillis()

                            while (isActive && state.isPlaying) {
                                val elapsed = System.currentTimeMillis() - startMs
                                val targetPose = flightAnimator.update(elapsed, step.durationMs)
                                val trackingCam = tracker.computeTrackingCamera(targetPose)

                                viewModel.updateAirplanePose(targetPose)
                                map.setCamera(trackingCam)
                                currentCamera = trackingCam

                                if (flightAnimator.isFinished(elapsed, step.durationMs)) break
                                delay(16L)
                            }

                            val finalPose = flightAnimator.update(step.durationMs, step.durationMs)
                            viewModel.updateAirplanePose(finalPose)
                        }
                    }
                }
                currentCamera = TourData.COIT_TOWER_INSPECTION_CAMERA
                viewModel.onNativeCameraAnimationFinished()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap3D(
            modifier = Modifier.fillMaxSize(),
            camera = currentCamera,
            models = models,
            onMapReady = { map ->
                googleMap3D = map
            },
        )

        TourControlsOverlay(
            state = state,
            viewModel = viewModel,
            onResetCamera = {
                val targetCam = if (state.selectedApproach == AnimationApproach.KEYFRAME_TOUR) TourData.OVERVIEW_CAMERA else initialCamera
                currentCamera = targetCam
                googleMap3D?.setCamera(targetCam)
            },
            onApproachSelected = { approach ->
                val targetCam = if (approach == AnimationApproach.KEYFRAME_TOUR) TourData.OVERVIEW_CAMERA else initialCamera
                currentCamera = targetCam
                googleMap3D?.setCamera(targetCam)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(12.dp),
        )
    }
}

@Composable
private fun TourControlsOverlay(
    state: WorldState,
    viewModel: AdvancedCameraAnimationViewModel,
    onResetCamera: () -> Unit,
    onApproachSelected: (AnimationApproach) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var isCollapsed by remember { mutableStateOf(false) }
    var isIdle by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(
        targetValue = if (isIdle) 0.35f else 1.0f,
        animationSpec = tween(durationMillis = 300),
        label = "alpha",
    )

    LaunchedEffect(isIdle) {
        if (!isIdle) {
            delay(3500L)
            isIdle = true
        }
    }

    fun registerInteraction() {
        isIdle = false
    }

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .alpha(alpha)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { registerInteraction() },
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Drag Handle Bar
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(2.dp),
                    )
                    .align(Alignment.CenterHorizontally),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Header Title Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        registerInteraction()
                        isCollapsed = !isCollapsed
                    },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(CommonR.string.aerial_tour_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(CommonR.string.framework_compose),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                IconButton(
                    onClick = {
                        registerInteraction()
                        showHelpDialog = true
                    },
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = stringResource(CommonR.string.help_dialog_title),
                    )
                }

                IconButton(
                    onClick = {
                        registerInteraction()
                        isCollapsed = !isCollapsed
                    },
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = if (isCollapsed) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = stringResource(CommonR.string.collapse_controls),
                    )
                }
            }

            // Persistent Play/Pause + Status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledIconButton(
                    onClick = {
                        registerInteraction()
                        viewModel.togglePlayPause()
                    },
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = if (state.isPlaying) {
                            ImageVector.vectorResource(CommonR.drawable.pause_24px)
                        } else {
                            Icons.Default.PlayArrow
                        },
                        contentDescription = stringResource(CommonR.string.play_or_pause_animation),
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                OutlinedIconButton(
                    onClick = {
                        registerInteraction()
                        viewModel.resetTour()
                        onResetCamera()
                    },
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(CommonR.string.reset),
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = state.statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                )
            }

            // Collapsible Content
            AnimatedVisibility(visible = !isCollapsed) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    var approachMenuExpanded by remember { mutableStateOf(false) }

                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        OutlinedButton(
                            onClick = {
                                registerInteraction()
                                approachMenuExpanded = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = state.selectedApproach.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f, fill = false),
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = approachMenuExpanded,
                            onDismissRequest = { approachMenuExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f),
                        ) {
                            AnimationApproach.values().forEach { approach ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = approach.title,
                                            fontWeight = if (state.selectedApproach == approach) FontWeight.Bold else FontWeight.Normal,
                                        )
                                    },
                                    onClick = {
                                        registerInteraction()
                                        approachMenuExpanded = false
                                        viewModel.setApproach(approach)
                                        viewModel.resetTour()
                                        onApproachSelected(approach)
                                    },
                                )
                            }
                        }
                    }

                    // Sub-mode options for Simple FlyTo
                    if (state.selectedApproach == AnimationApproach.SIMPLE_FLY_TO) {
                        Text(
                            text = "3D Model Animation Mode:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            FilterChip(
                                selected = state.simpleFlyToMode == SimpleFlyToMode.SYNCHRONIZED_FLIGHT,
                                onClick = {
                                    registerInteraction()
                                    viewModel.setSimpleFlyToMode(SimpleFlyToMode.SYNCHRONIZED_FLIGHT)
                                },
                                label = { Text("Synchronized (High CPU)") },
                            )
                            FilterChip(
                                selected = state.simpleFlyToMode == SimpleFlyToMode.MIDPOINT_JUMP,
                                onClick = {
                                    registerInteraction()
                                    viewModel.setSimpleFlyToMode(SimpleFlyToMode.MIDPOINT_JUMP)
                                },
                                label = { Text("Midpoint Jump (Low CPU)") },
                            )
                        }
                    }

                    val detailText = when (state.selectedApproach) {
                        AnimationApproach.SIMPLE_FLY_TO -> "Native asynchronous SDK flight transition directly to Coit Tower."
                        AnimationApproach.KEYFRAME_TOUR -> "Declarative 5-step sequence: Swoop FlyTo → Dwell Pause → 360° Orbit → Stationary Tracking Flight → Final FlyTo."
                        AnimationApproach.DISPATCHER_FRAME_LOOP -> "Continuous 400 m/s flight synced to hardware VSYNC display frames."
                        AnimationApproach.ORBIT_360_SPIN -> "Continuous 360° orbital camera rotation around Golden Gate Bridge."
                    }

                    Text(
                        text = detailText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }

    if (showHelpDialog) {
        HelpDialog(onDismiss = { showHelpDialog = false })
    }
}

@Composable
private fun HelpDialog(
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val htmlSpanned = remember(context) {
        HtmlCompat.fromHtml(
            context.getString(CommonR.string.help_dialog_advanced_animation_message),
            HtmlCompat.FROM_HTML_MODE_LEGACY,
        )
    }
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(CommonR.string.help_dialog_advanced_animation_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxWidth(),
                    factory = { ctx ->
                        TextView(ctx).apply {
                            movementMethod = LinkMovementMethod.getInstance()
                            setTextColor(textColor)
                            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                            setLineSpacing(0f, 1.2f)
                        }
                    },
                    update = { textView ->
                        textView.text = htmlSpanned
                    },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(CommonR.string.help_dialog_ok))
            }
        },
    )
}
