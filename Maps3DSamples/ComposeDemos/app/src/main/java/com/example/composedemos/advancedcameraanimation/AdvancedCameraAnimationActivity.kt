/*
 * Copyright 2025 Google LLC
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
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.maps3d.common.CameraKeyframe
import com.example.maps3d.common.RouteEngine
import com.example.maps3d.common.TourData
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.FlyToOptions
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.maps.android.SphericalUtil
import com.google.maps.android.compose3d.GoogleMap3D
import com.google.maps.android.compose3d.ModelConfig
import com.google.maps.android.compose3d.ModelScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.milliseconds

/**
 * Suspends until the 3D map camera animation completes using [com.google.android.gms.maps3d.OnCameraAnimationEndListener].
 */
private suspend fun com.google.android.gms.maps3d.GoogleMap3D.awaitFlyCameraTo(options: FlyToOptions) = suspendCancellableCoroutine { continuation ->
    setCameraAnimationEndListener {
        setCameraAnimationEndListener(null)
        if (continuation.isActive) {
            continuation.resume(Unit)
        }
    }
    flyCameraTo(options)
    continuation.invokeOnCancellation {
        setCameraAnimationEndListener(null)
        stopCameraAnimation()
    }
}

enum class AnimationApproach(val title: String) {
    SIMPLE_FLY_TO("1. SDK Simple flyTo (Native Transition)"),
    KEYFRAME_TOUR("2. Keyframe Queue (Multi-step Camera Tour)"),
    DISPATCHER_FRAME_LOOP("3. Frame Dispatcher (Chase Cam + 3D Airplane)"),
    ORBIT_360_SPIN("4. 360° Orbit Spin (Continuous Orbital Camera)"),
}

class AdvancedCameraAnimationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    AdvancedCameraAnimationScreen()
                }
            }
        }
    }

    companion object {
        val SAN_FRANCISCO_TOUR = TourData.SAN_FRANCISCO_TOUR
        val AIRPLANE_FLIGHT_PATH = TourData.AIRPLANE_FLIGHT_PATH
    }
}

@Composable
fun AdvancedCameraAnimationScreen() {
    val tourSequence = TourData.SAN_FRANCISCO_TOUR
    val flightPath = TourData.AIRPLANE_FLIGHT_PATH
    val cumulativeDistances = remember(flightPath) {
        RouteEngine.calculateCumulativeDistances(flightPath)
    }

    var selectedApproach by remember { mutableStateOf(AnimationApproach.DISPATCHER_FRAME_LOOP) }
    var currentStepIndex by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }

    var mapInstance by remember { mutableStateOf<com.google.android.gms.maps3d.GoogleMap3D?>(null) }
    val scope = rememberCoroutineScope()

    var tourJob by remember { mutableStateOf<Job?>(null) }
    var restartJob by remember { mutableStateOf<Job?>(null) }

    var planePosition by remember {
        val start = flightPath.first()
        mutableStateOf(
            latLngAltitude {
                latitude = start.latitude
                longitude = start.longitude
                altitude = 200.0
            },
        )
    }

    var planeHeading by remember {
        val h = SphericalUtil.computeHeading(flightPath.first(), flightPath[1])
        mutableDoubleStateOf(normalizeHeading(h + 180.0))
    }

    val activeModels = remember(planePosition, planeHeading) {
        listOf(
            ModelConfig(
                key = TourData.MODEL_ID,
                position = planePosition,
                url = TourData.PLANE_URL,
                altitudeMode = AltitudeMode.ABSOLUTE,
                scale = ModelScale.Uniform(0.08f),
                heading = planeHeading,
                tilt = -90.0,
                roll = 0.0,
            ),
        )
    }

    var currentCameraState by remember {
        val firstLoc = flightPath.first()
        val initialHeading = SphericalUtil.computeHeading(firstLoc, flightPath[1])
        mutableStateOf(
            camera {
                center = latLngAltitude {
                    latitude = firstLoc.latitude
                    longitude = firstLoc.longitude
                    altitude = 200.0
                }
                heading = normalizeHeading(initialHeading)
                tilt = 65.0
                range = 600.0
            },
        )
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                isPlaying = false
                tourJob?.cancel()
                tourJob = null
                restartJob?.cancel()
                restartJob = null
                mapInstance?.stopCameraAnimation()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    fun stopTour() {
        isPlaying = false
        tourJob?.cancel()
        tourJob = null
        restartJob?.cancel()
        restartJob = null
        mapInstance?.setCameraAnimationEndListener(null)
        mapInstance?.stopCameraAnimation()
    }

    fun runSimpleFlyTo() {
        stopTour()
        isPlaying = true

        val targetLoc = flightPath.last()
        val flightHeading = SphericalUtil.computeHeading(
            flightPath[flightPath.size - 2],
            targetLoc,
        )

        planePosition = latLngAltitude {
            latitude = targetLoc.latitude
            longitude = targetLoc.longitude
            altitude = 200.0
        }
        planeHeading = normalizeHeading(flightHeading + 180.0)

        val targetCam = camera {
            center = latLngAltitude {
                latitude = targetLoc.latitude
                longitude = targetLoc.longitude
                altitude = 200.0
            }
            heading = normalizeHeading(flightHeading)
            tilt = 65.0
            range = 600.0
        }

        tourJob = scope.launch(Dispatchers.Main) {
            mapInstance?.awaitFlyCameraTo(FlyToOptions(targetCam, 1500L))
            isPlaying = false
        }
    }

    fun runKeyframeTour() {
        stopTour()
        isPlaying = true
        currentStepIndex = 0

        tourJob = scope.launch(Dispatchers.Main) {
            val frameDurationMs = 16L

            while (currentStepIndex < tourSequence.size && isActive && isPlaying) {
                when (val step = tourSequence[currentStepIndex]) {
                    is CameraKeyframe.FlyTo -> {
                        val targetCam = camera {
                            center = latLngAltitude {
                                latitude = step.targetCenter.latitude
                                longitude = step.targetCenter.longitude
                                altitude = step.targetAltitude
                            }
                            heading = normalizeHeading(step.targetHeading)
                            tilt = step.targetTilt
                            range = step.targetRange
                        }

                        planePosition = latLngAltitude {
                            latitude = step.targetCenter.latitude
                            longitude = step.targetCenter.longitude
                            altitude = 200.0
                        }
                        planeHeading = normalizeHeading(step.targetHeading + 180.0)

                        mapInstance?.awaitFlyCameraTo(FlyToOptions(targetCam, step.durationMs))
                    }

                    is CameraKeyframe.DwellPause -> {
                        delay(step.durationMs.milliseconds)
                    }

                    is CameraKeyframe.OrbitAround -> {
                        planePosition = latLngAltitude {
                            latitude = step.center.latitude
                            longitude = step.center.longitude
                            altitude = 200.0
                        }
                        val startTimeMs = SystemClock.uptimeMillis()
                        val totalMs = step.durationMs

                        while (isActive && isPlaying) {
                            val elapsedMs = SystemClock.uptimeMillis() - startTimeMs
                            if (elapsedMs > totalMs) break

                            val t = (elapsedMs.toDouble() / totalMs).coerceIn(0.0, 1.0)
                            val orbitHeading = interpolateAngle(step.startHeading, step.endHeading, t)

                            planeHeading = normalizeHeading(orbitHeading + 180.0)
                            currentCameraState = camera {
                                center = latLngAltitude {
                                    latitude = step.center.latitude
                                    longitude = step.center.longitude
                                    altitude = step.altitude
                                }
                                heading = normalizeHeading(orbitHeading)
                                tilt = step.tilt
                                range = step.range
                            }
                            delay(frameDurationMs.milliseconds)
                        }
                    }
                }

                if (!isActive || !isPlaying) break

                if (currentStepIndex < tourSequence.size - 1) {
                    currentStepIndex++
                } else {
                    isPlaying = false
                    break
                }
            }
        }
    }

    fun runFrameDispatcherLoop() {
        stopTour()
        isPlaying = true

        val totalDist = cumulativeDistances.last().coerceAtLeast(1.0)
        val flightSpeedMps = 400.0

        tourJob = scope.launch(Dispatchers.Main) {
            var lastTimeMs = SystemClock.uptimeMillis()
            var elapsedDistance = 0.0

            while (isActive && isPlaying) {
                val nowMs = SystemClock.uptimeMillis()
                val dt = (nowMs - lastTimeMs) / 1000.0
                lastTimeMs = nowMs

                elapsedDistance += flightSpeedMps * dt

                if (elapsedDistance >= totalDist) {
                    elapsedDistance = totalDist
                    val posAndHeading = RouteEngine.calculatePositionAndHeading(
                        flightPath,
                        cumulativeDistances,
                        elapsedDistance,
                        30.0,
                    )
                    planePosition = latLngAltitude {
                        latitude = posAndHeading.position.latitude
                        longitude = posAndHeading.position.longitude
                        altitude = 200.0
                    }
                    planeHeading = normalizeHeading(posAndHeading.heading.toDouble() + 180.0)

                    currentCameraState = camera {
                        center = latLngAltitude {
                            latitude = posAndHeading.position.latitude
                            longitude = posAndHeading.position.longitude
                            altitude = 200.0
                        }
                        heading = normalizeHeading(posAndHeading.heading.toDouble())
                        tilt = 65.0
                        range = 600.0
                    }
                    isPlaying = false
                    break
                }

                val posAndHeading = RouteEngine.calculatePositionAndHeading(
                    flightPath,
                    cumulativeDistances,
                    elapsedDistance,
                    30.0,
                )

                planePosition = latLngAltitude {
                    latitude = posAndHeading.position.latitude
                    longitude = posAndHeading.position.longitude
                    altitude = 200.0
                }
                planeHeading = normalizeHeading(posAndHeading.heading.toDouble() + 180.0)

                currentCameraState = camera {
                    center = latLngAltitude {
                        latitude = posAndHeading.position.latitude
                        longitude = posAndHeading.position.longitude
                        altitude = 200.0
                    }
                    heading = normalizeHeading(posAndHeading.heading.toDouble())
                    tilt = 65.0
                    range = 600.0
                }

                delay(16.milliseconds)
            }
        }
    }

    fun run360OrbitSpin() {
        stopTour()
        isPlaying = true

        val targetCenter = flightPath.first()
        planePosition = latLngAltitude {
            latitude = targetCenter.latitude
            longitude = targetCenter.longitude
            altitude = 200.0
        }
        planeHeading = normalizeHeading(105.0 + 180.0)

        tourJob = scope.launch(Dispatchers.Main) {
            val totalMs = 6000L
            val startHeading = 105.0
            val startTimeMs = SystemClock.uptimeMillis()

            while (isActive && isPlaying) {
                val elapsedMs = SystemClock.uptimeMillis() - startTimeMs
                if (elapsedMs > totalMs) break

                val t = (elapsedMs.toDouble() / totalMs).coerceIn(0.0, 1.0)
                val headingDeg = (startHeading + t * 360.0) % 360.0

                currentCameraState = camera {
                    center = latLngAltitude {
                        latitude = targetCenter.latitude
                        longitude = targetCenter.longitude
                        altitude = 200.0
                    }
                    heading = normalizeHeading(headingDeg)
                    tilt = 65.0
                    range = 600.0
                }
                delay(16.milliseconds)
            }
            isPlaying = false
        }
    }

    fun startSelectedApproach() {
        stopTour()
        when (selectedApproach) {
            AnimationApproach.SIMPLE_FLY_TO -> runSimpleFlyTo()
            AnimationApproach.KEYFRAME_TOUR -> runKeyframeTour()
            AnimationApproach.DISPATCHER_FRAME_LOOP -> runFrameDispatcherLoop()
            AnimationApproach.ORBIT_360_SPIN -> run360OrbitSpin()
        }
    }

    // Auto-start initial animation once after map loads
    LaunchedEffect(Unit) {
        delay(1000.milliseconds)
        startSelectedApproach()
    }

    fun resetTour() {
        stopTour()
        restartJob?.cancel()
        currentStepIndex = 0

        val p1 = flightPath.first()
        val p2 = flightPath[1]
        val rawH = SphericalUtil.computeHeading(p1, p2)
        planePosition = latLngAltitude {
            latitude = p1.latitude
            longitude = p1.longitude
            altitude = 200.0
        }
        planeHeading = normalizeHeading(rawH + 180.0)

        val firstStep = tourSequence.first() as CameraKeyframe.FlyTo
        currentCameraState = camera {
            center = latLngAltitude {
                latitude = firstStep.targetCenter.latitude
                longitude = firstStep.targetCenter.longitude
                altitude = firstStep.targetAltitude
            }
            heading = normalizeHeading(rawH)
            tilt = firstStep.targetTilt
            range = firstStep.targetRange
        }

        restartJob = scope.launch {
            delay(300.milliseconds)
            startSelectedApproach()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap3D(
            camera = currentCameraState,
            models = activeModels,
            mapMode = Map3DMode.SATELLITE,
            onMapReady = { mapInstance = it },
            modifier = Modifier.fillMaxSize(),
        )

        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            ) {
                Text(
                    text = "Select Animation Approach:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    AnimationApproach.entries.forEach { approach ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (selectedApproach == approach),
                                    onClick = {
                                        selectedApproach = approach
                                        resetTour()
                                    },
                                    role = Role.RadioButton,
                                )
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = (selectedApproach == approach),
                                onClick = null,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = approach.title,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(
                        onClick = { resetTour() },
                        modifier = Modifier.padding(end = 8.dp),
                    ) {
                        Text("Reset")
                    }

                    Button(
                        onClick = {
                            if (isPlaying) {
                                stopTour()
                            } else {
                                startSelectedApproach()
                            }
                        },
                    ) {
                        Text(if (isPlaying) "Pause" else "Play")
                    }
                }
            }
        }
    }
}

private fun normalizeHeading(headingDeg: Double): Double {
    val normalized = headingDeg % 360.0
    return if (normalized < 0.0) normalized + 360.0 else normalized
}

private fun interpolateAngle(start: Double, end: Double, fraction: Double): Double {
    var diff = (end - start) % 360.0
    if (diff > 180.0) diff -= 360.0
    if (diff < -180.0) diff += 360.0
    return (start + diff * fraction + 360.0) % 360.0
}
