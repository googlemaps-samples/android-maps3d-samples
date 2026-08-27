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
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.maps.android.SphericalUtil
import com.google.maps.android.compose3d.GoogleMap3D
import com.google.maps.android.compose3d.PolylineConfig
import com.google.maps.android.compose3d.utils.toHeading
import kotlin.time.Duration.Companion.milliseconds

/**
 * Advanced sample demonstrating ground-level path following in Jetpack Compose.
 *
 * Features:
 * - Urban vs Rural ground-level paths
 * - Two-polyline architecture: wide blue base route (lower z-index) + narrow purple active progress route (higher z-index)
 * - Configurable altitude modes (Clamp to Ground default, Relative to Ground, Relative to Mesh, Absolute)
 * - Dynamic path elevation slider to eliminate z-fighting
 * - Occlusion visualization toggle (drawsOccludedSegments) rendering polylines through or behind 3D terrain and buildings
 * - Explicit collapsible controls card with smooth auto-fade on idle
 * - Real-time camera controls via sliders: Range, Ground Altitude, Heading Offset, Tilt, Follow Speed
 */
class PathFollowingActivity : ComponentActivity() {
    val touchEventFlow = kotlinx.coroutines.flow.MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN || ev.action == MotionEvent.ACTION_MOVE) {
            touchEventFlow.tryEmit(Unit)
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
                    PathFollowingScreen()
                }
            }
        }
    }

    companion object {
        // Urban Path (San Francisco Downtown - Market Street Corridor with 1 to 10m altitudes)
        val URBAN_PATH = listOf(
            LatLngAltitude(37.79323, -122.39322, 4.2),
            LatLngAltitude(37.79166, -122.39519, 6.7),
            LatLngAltitude(37.79124, -122.39571, 8.1),
            LatLngAltitude(37.79105, -122.39599, 9.5),
            LatLngAltitude(37.78893, -122.39866, 7.3),
            LatLngAltitude(37.78742, -122.40060, 5.0),
            LatLngAltitude(37.78686, -122.40129, 3.4),
            LatLngAltitude(37.78652, -122.40171, 2.1),
            LatLngAltitude(37.78632, -122.40196, 4.6),
            LatLngAltitude(37.78627, -122.40207, 6.2),
            LatLngAltitude(37.78453, -122.40429, 8.9),
            LatLngAltitude(37.78443, -122.40434, 10.0),
            LatLngAltitude(37.78155, -122.40802, 7.8),
            LatLngAltitude(37.78005, -122.40990, 5.4),
            LatLngAltitude(37.77856, -122.41180, 3.1),
            LatLngAltitude(37.77746, -122.41318, 1.8),
            LatLngAltitude(37.77624, -122.41474, 4.0),
            LatLngAltitude(37.77744, -122.41623, 6.5),
            LatLngAltitude(37.77749, -122.41636, 8.7),
            LatLngAltitude(37.77761, -122.41654, 9.8),
            LatLngAltitude(37.77769, -122.41677, 7.2),
            LatLngAltitude(37.77729, -122.41981, 4.9),
            LatLngAltitude(37.77523, -122.41938, 2.6),
            LatLngAltitude(37.77510, -122.41934, 1.2),
            LatLngAltitude(37.77442, -122.42022, 3.5),
            LatLngAltitude(37.77441, -122.42033, 5.8),
            LatLngAltitude(37.77348, -122.42157, 8.4),
            LatLngAltitude(37.77244, -122.42289, 10.0),
        )

        // Rural Path (California Coastal Highway)
        val RURAL_PATH = listOf(
            LatLngAltitude(37.254529, -122.380897, 0.0),
            LatLngAltitude(37.255065, -122.381627, 0.0),
            LatLngAltitude(37.257540, -122.383720, 0.0),
            LatLngAltitude(37.261200, -122.383950, 0.0),
            LatLngAltitude(37.264780, -122.388210, 0.0),
            LatLngAltitude(37.268520, -122.392450, 0.0),
            LatLngAltitude(37.272110, -122.397640, 0.0),
            LatLngAltitude(37.276430, -122.401120, 0.0),
            LatLngAltitude(37.280850, -122.403560, 0.0),
            LatLngAltitude(37.286018, -122.405072, 0.0),
            LatLngAltitude(37.291040, -122.404210, 0.0),
            LatLngAltitude(37.295800, -122.401980, 0.0),
            LatLngAltitude(37.300120, -122.399540, 0.0),
            LatLngAltitude(37.304550, -122.397210, 0.0),
            LatLngAltitude(37.309200, -122.395100, 0.0),
            LatLngAltitude(37.313450, -122.392840, 0.0),
            LatLngAltitude(37.317200, -122.390510, 0.0),
            LatLngAltitude(37.320850, -122.388740, 0.0),
            LatLngAltitude(37.323540, -122.387600, 0.0),
            LatLngAltitude(37.325269, -122.386728, 0.0),
        )
    }
}

enum class EnvironmentType(val label: String) {
    URBAN("Urban"),
    RURAL("Rural"),
}

enum class AltitudeModeOption(val label: String, val mode: Int) {
    CLAMP_TO_GROUND("Clamp to Ground", AltitudeMode.CLAMP_TO_GROUND),
    RELATIVE_TO_GROUND("Relative to Ground", AltitudeMode.RELATIVE_TO_GROUND),
    RELATIVE_TO_MESH("Relative to Mesh", AltitudeMode.RELATIVE_TO_MESH),
    ABSOLUTE("Absolute", AltitudeMode.ABSOLUTE),
}

@Composable
fun PathFollowingScreen() {
    var isMapSteady by remember { mutableStateOf(false) }

    // UX Fade and Collapse logic
    val activity = LocalActivity.current as PathFollowingActivity
    var showControls by remember { mutableStateOf(true) }
    var isCollapsed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        activity.touchEventFlow.collect {
            showControls = true
        }
    }

    LaunchedEffect(showControls, isCollapsed) {
        if (showControls && !isCollapsed) {
            kotlinx.coroutines.delay(3000.milliseconds)
            showControls = false
        }
    }

    val controlsAlpha by animateFloatAsState(
        targetValue = if (showControls || isCollapsed) 1f else 0.8f,
        animationSpec = tween(400),
    )

    // Environment Selection State
    var selectedEnv by remember { mutableStateOf(EnvironmentType.URBAN) }
    var currentPath by remember { mutableStateOf(PathFollowingActivity.URBAN_PATH) }

    // Altitude Mode and Height State
    var selectedAltitudeMode by remember { mutableStateOf(AltitudeModeOption.CLAMP_TO_GROUND) }
    var pathAltitudeOffset by remember { mutableFloatStateOf(0.5f) }
    var drawsOccludedSegments by remember { mutableStateOf(true) }

    // Path Calculations State
    val pathCalculations = remember(currentPath) {
        val cumulative = DoubleArray(currentPath.size)
        var total = 0.0
        cumulative[0] = 0.0
        for (i in 1 until currentPath.size) {
            val pPrev = LatLng(currentPath[i - 1].latitude, currentPath[i - 1].longitude)
            val pCurr = LatLng(currentPath[i].latitude, currentPath[i].longitude)
            val dist = SphericalUtil.computeDistanceBetween(pPrev, pCurr)
            total += dist
            cumulative[i] = total
        }
        Pair(cumulative, total)
    }
    val cumulativeDistances = pathCalculations.first
    val totalDistance = pathCalculations.second

    // Animation Controls State
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0.0f) }
    var isUserScrubbing by remember { mutableStateOf(false) }
    var elapsedDistance by remember { mutableDoubleStateOf(0.0) }
    var currentWaypointIndex by remember { mutableIntStateOf(0) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP || event == Lifecycle.Event.ON_DESTROY) {
                isPlaying = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Camera Parameter Sliders State
    var cameraRange by remember { mutableFloatStateOf(300f) }
    var groundAltitude by remember { mutableFloatStateOf(20f) }
    var headingOffset by remember { mutableFloatStateOf(0f) }
    var cameraTilt by remember { mutableFloatStateOf(70f) }
    var followSpeedMps by remember { mutableFloatStateOf(30f) }

    // Interpolated Position & Heading
    var currentLatLng by remember(currentPath) {
        val first = currentPath.first()
        mutableStateOf(LatLng(first.latitude, first.longitude))
    }
    var currentHeading by remember { mutableStateOf<Double?>(null) }
    var targetHeading by remember { mutableDoubleStateOf(0.0) }

    // Helper to calculate position for given elapsed distance
    fun updatePositionForDistance(dist: Double) {
        if (currentPath.isEmpty()) return
        var index = 0
        while (index < cumulativeDistances.size - 1 && cumulativeDistances[index + 1] < dist) {
            index++
        }
        currentWaypointIndex = index

        val p1 = currentPath[index]
        val p2 = if (index < currentPath.size - 1) currentPath[index + 1] else p1

        val segStartDist = cumulativeDistances[index]
        val segEndDist =
            if (index < cumulativeDistances.size - 1) cumulativeDistances[index + 1] else totalDistance
        val segLen = segEndDist - segStartDist

        val fraction = if (segLen > 0) ((dist - segStartDist) / segLen).coerceIn(0.0, 1.0) else 0.0
        val latLng1 = LatLng(p1.latitude, p1.longitude)
        val latLng2 = LatLng(p2.latitude, p2.longitude)
        val interpolatedLatLng = SphericalUtil.interpolate(latLng1, latLng2, fraction)
        currentLatLng = interpolatedLatLng
        val bearing = SphericalUtil.computeHeading(latLng1, latLng2)

        // Kinematic Heading Smoothing (Exponential Moving Average)
        val targetHeadingRaw = (bearing + headingOffset).toHeading()
        val computedHeading = if (currentHeading == null || isUserScrubbing || !isPlaying) {
            targetHeadingRaw
        } else {
            var diff = (targetHeadingRaw - currentHeading!!) % 360.0
            if (diff > 180.0) diff -= 360.0
            if (diff < -180.0) diff += 360.0
            (currentHeading!! + diff * 0.12).toHeading()
        }
        currentHeading = computedHeading
        targetHeading = computedHeading
    }

    // Switch Environment logic
    fun switchEnvironment(env: EnvironmentType) {
        selectedEnv = env
        isPlaying = false
        currentHeading = null
        progress = 0f
        elapsedDistance = 0.0
        if (env == EnvironmentType.RURAL) {
            currentPath = PathFollowingActivity.RURAL_PATH
            cameraRange = 450f
            groundAltitude = 40f
            cameraTilt = 75f
        } else {
            currentPath = PathFollowingActivity.URBAN_PATH
            cameraRange = 300f
            groundAltitude = 20f
            cameraTilt = 70f
        }
        updatePositionForDistance(0.0)
    }

    // Animation Loop synced to VSYNC
    LaunchedEffect(isPlaying) {
        if (!isPlaying) return@LaunchedEffect
        var lastTimeNanos = withFrameNanos { it }
        while (isPlaying) {
            val nowNanos = withFrameNanos { it }
            val dt = (nowNanos - lastTimeNanos) / 1_000_000_000.0
            lastTimeNanos = nowNanos

            val stepDistance = followSpeedMps * dt
            elapsedDistance += stepDistance

            if (elapsedDistance >= totalDistance) {
                elapsedDistance = 0.0
            }

            if (!isUserScrubbing && totalDistance > 0) {
                progress = (elapsedDistance / totalDistance).toFloat().coerceIn(0f, 1f)
            }
            updatePositionForDistance(elapsedDistance)
        }
    }

    // Dynamic Camera Altitude
    val cameraTargetAltitude = if (selectedAltitudeMode.mode == AltitudeMode.ABSOLUTE) {
        val baseAlt = if (currentPath == PathFollowingActivity.RURAL_PATH) 45.0 else 50.0
        baseAlt + groundAltitude
    } else {
        groundAltitude.toDouble()
    }

    // Dynamic Camera State
    val dynamicCamera =
        remember(currentLatLng, targetHeading, cameraTilt, cameraRange, cameraTargetAltitude) {
            camera {
                center = latLngAltitude {
                    latitude = currentLatLng.latitude
                    longitude = currentLatLng.longitude
                    altitude = cameraTargetAltitude
                }
                heading = targetHeading
                tilt = cameraTilt.toDouble()
                range = cameraRange.toDouble()
                roll = 0.0
            }
        }

    // Dual-Polyline Rendering Technique:
    // 1. Base Static Route Polyline: A wider (#4285F4 blue, 16dp) static path at ZIndex=1.
    // 2. Traversed Progress Polyline: A narrower (#9C27B0 purple, 8dp) line at ZIndex=2.
    val staticPolylineConfig = remember(
        currentPath,
        selectedAltitudeMode,
        pathAltitudeOffset,
        drawsOccludedSegments,
    ) {
        val pathAltitude = when (selectedAltitudeMode.mode) {
            AltitudeMode.CLAMP_TO_GROUND -> 0.0
            AltitudeMode.ABSOLUTE -> if (currentPath == PathFollowingActivity.RURAL_PATH) 45.0 + pathAltitudeOffset else 50.0 + pathAltitudeOffset
            else -> pathAltitudeOffset.toDouble()
        }
        val staticVertices = currentPath.map { pt ->
            LatLngAltitude(pt.latitude, pt.longitude, pathAltitude)
        }
        PolylineConfig(
            key = "path_following_static_route",
            points = staticVertices,
            color = "#4285F4".toColorInt(),
            width = 16f,
            zIndex = 1,
            altitudeMode = selectedAltitudeMode.mode,
            drawsOccludedSegments = drawsOccludedSegments,
        )
    }

    val progressPolylineConfig = remember(
        currentPath,
        currentWaypointIndex,
        currentLatLng,
        selectedAltitudeMode,
        pathAltitudeOffset,
        drawsOccludedSegments,
        totalDistance,
    ) {
        if (currentPath.isEmpty() || totalDistance <= 0.0) return@remember null

        val pathAltitude = when (selectedAltitudeMode.mode) {
            AltitudeMode.CLAMP_TO_GROUND -> 0.0
            AltitudeMode.ABSOLUTE -> if (currentPath == PathFollowingActivity.RURAL_PATH) 45.0 + pathAltitudeOffset else 50.0 + pathAltitudeOffset
            else -> pathAltitudeOffset.toDouble()
        }

        val progressAltitude = if (selectedAltitudeMode.mode == AltitudeMode.CLAMP_TO_GROUND) {
            0.0
        } else {
            pathAltitude + 0.4
        }

        val progressCoordinates = ArrayList<LatLngAltitude>()
        for (i in 0..currentWaypointIndex.coerceAtMost(currentPath.size - 1)) {
            val pt = currentPath[i]
            progressCoordinates.add(LatLngAltitude(pt.latitude, pt.longitude, progressAltitude))
        }

        val lastWaypoint = currentPath[currentWaypointIndex.coerceAtMost(currentPath.size - 1)]
        val lastLatLng = LatLng(lastWaypoint.latitude, lastWaypoint.longitude)
        val distToLast = SphericalUtil.computeDistanceBetween(lastLatLng, currentLatLng)
        if (distToLast >= 0.5) {
            progressCoordinates.add(
                LatLngAltitude(currentLatLng.latitude, currentLatLng.longitude, progressAltitude),
            )
        }

        if (progressCoordinates.size < 2 && currentPath.size >= 2) {
            val p0 = LatLng(currentPath[0].latitude, currentPath[0].longitude)
            val p1 = LatLng(currentPath[1].latitude, currentPath[1].longitude)
            val tinyForward = SphericalUtil.interpolate(p0, p1, 0.005)
            progressCoordinates.add(
                LatLngAltitude(tinyForward.latitude, tinyForward.longitude, progressAltitude),
            )
        }

        PolylineConfig(
            key = "path_following_progress_route",
            points = progressCoordinates,
            color = "#9C27B0".toColorInt(),
            width = 8f,
            zIndex = 2,
            altitudeMode = selectedAltitudeMode.mode,
            drawsOccludedSegments = drawsOccludedSegments,
        )
    }

    val polylines = remember(staticPolylineConfig, progressPolylineConfig) {
        listOfNotNull(staticPolylineConfig, progressPolylineConfig)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = if (isMapSteady) "MapSteady" else "MapLoading" },
    ) {
        // 1. Full Screen 3D Map
        GoogleMap3D(
            camera = dynamicCamera,
            polylines = polylines,
            modifier = Modifier.fillMaxSize(),
            onMapSteady = {
                isMapSteady = true
            },
        )

        // 2. Collapsible Control Panel Card at Bottom
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth()
                .alpha(controlsAlpha),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                // Header with Title and Expand / Collapse Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isCollapsed = !isCollapsed },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Path Following Controls",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    IconButton(
                        onClick = { isCollapsed = !isCollapsed },
                        modifier = Modifier.semantics {
                            contentDescription =
                                if (isCollapsed) "Expand Controls" else "Collapse Controls"
                        },
                    ) {
                        Icon(
                            imageVector = if (isCollapsed) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = if (isCollapsed) "Expand Controls" else "Collapse Controls",
                        )
                    }
                }

                AnimatedVisibility(visible = !isCollapsed) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 340.dp)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        // Environment Selector
                        Text(
                            text = "Path Environment:",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            EnvironmentType.entries.forEach { env ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .weight(1f)
                                        .selectable(
                                            selected = (selectedEnv == env),
                                            onClick = { switchEnvironment(env) },
                                            role = Role.RadioButton,
                                        ),
                                ) {
                                    RadioButton(
                                        selected = (selectedEnv == env),
                                        onClick = null,
                                    )
                                    Text(
                                        text = env.label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(start = 4.dp),
                                    )
                                }
                            }
                        }

                        // Altitude Mode Selector
                        Text(
                            text = "Altitude Mode:",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                        ) {
                            AltitudeModeOption.entries.forEach { opt ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .selectable(
                                            selected = (selectedAltitudeMode == opt),
                                            onClick = { selectedAltitudeMode = opt },
                                            role = Role.RadioButton,
                                        ),
                                ) {
                                    RadioButton(
                                        selected = (selectedAltitudeMode == opt),
                                        onClick = null,
                                    )
                                    Text(
                                        text = opt.label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(start = 4.dp),
                                    )
                                }
                            }
                        }

                        // Path Height Slider (Relative Altitude)
                        Text(
                            text = "Path Height: ${"%.1f".format(pathAltitudeOffset)}m",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        Slider(
                            value = pathAltitudeOffset,
                            onValueChange = { pathAltitudeOffset = it },
                            valueRange = 0f..10f,
                            modifier = Modifier.semantics {
                                contentDescription = "Path Height Slider"
                            },
                        )

                        // Occlusion Switch
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                        ) {
                            Text(
                                text = "Draw Occluded Segments",
                                style = MaterialTheme.typography.labelLarge,
                            )
                            Switch(
                                checked = drawsOccludedSegments,
                                onCheckedChange = { drawsOccludedSegments = it },
                                modifier = Modifier.semantics {
                                    contentDescription = "Draw Occluded Segments Switch"
                                },
                            )
                        }

                        // Play / Pause and Progress Slider Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            IconButton(
                                onClick = { isPlaying = !isPlaying },
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause animation" else "Play animation",
                                )
                            }

                            Slider(
                                value = progress,
                                onValueChange = { newValue ->
                                    isUserScrubbing = true
                                    progress = newValue
                                    elapsedDistance = totalDistance * newValue
                                    updatePositionForDistance(elapsedDistance)
                                },
                                onValueChangeFinished = {
                                    isUserScrubbing = false
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .semantics { contentDescription = "Path Progress Slider" },
                            )
                        }

                        // Camera Range Slider
                        Text(
                            text = "Camera Range: ${cameraRange.toInt()}m",
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Slider(
                            value = cameraRange,
                            onValueChange = {
                                cameraRange = it
                                updatePositionForDistance(elapsedDistance)
                            },
                            valueRange = 50f..1000f,
                            modifier = Modifier.semantics {
                                contentDescription = "Camera Range Slider"
                            },
                        )

                        // Ground Altitude Slider
                        val maxAltitude = if (selectedEnv == EnvironmentType.RURAL) 2000f else 200f
                        Text(
                            text = "Ground Altitude: ${groundAltitude.toInt()}m",
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Slider(
                            value = groundAltitude.coerceIn(2f, maxAltitude),
                            onValueChange = {
                                groundAltitude = it
                                updatePositionForDistance(elapsedDistance)
                            },
                            valueRange = 2f..maxAltitude,
                            modifier = Modifier.semantics {
                                contentDescription = "Ground Altitude Slider"
                            },
                        )

                        // Heading Offset Slider
                        Text(
                            text = "Heading Offset: ${headingOffset.toInt()}°",
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Slider(
                            value = headingOffset,
                            onValueChange = {
                                headingOffset = it
                                updatePositionForDistance(elapsedDistance)
                            },
                            valueRange = -180f..180f,
                            modifier = Modifier.semantics {
                                contentDescription = "Heading Offset Slider"
                            },
                        )

                        // Camera Tilt Slider
                        Text(
                            text = "Camera Tilt: ${cameraTilt.toInt()}°",
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Slider(
                            value = cameraTilt,
                            onValueChange = {
                                cameraTilt = it
                                updatePositionForDistance(elapsedDistance)
                            },
                            valueRange = 0f..85f,
                            modifier = Modifier.semantics {
                                contentDescription = "Camera Tilt Slider"
                            },
                        )

                        // Follow Speed Slider
                        Text(
                            text = "Follow Speed: ${followSpeedMps.toInt()} m/s",
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Slider(
                            value = followSpeedMps,
                            onValueChange = { followSpeedMps = it },
                            valueRange = 5f..100f,
                            modifier = Modifier.semantics {
                                contentDescription = "Follow Speed Slider"
                            },
                        )
                    }
                }
            }
        }
    }
}
