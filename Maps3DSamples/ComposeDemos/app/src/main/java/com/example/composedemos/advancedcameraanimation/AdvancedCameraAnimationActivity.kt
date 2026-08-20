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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.composedemos.routes.RouteEngine
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

sealed interface CameraKeyframe {
    val stepTitle: String
    val stepDescription: String

    data class FlyTo(
        override val stepTitle: String,
        override val stepDescription: String,
        val targetCenter: LatLng,
        val targetAltitude: Double,
        val targetHeading: Double,
        val targetTilt: Double,
        val targetRange: Double,
        val durationMs: Long = 2500L,
    ) : CameraKeyframe

    data class DwellPause(
        override val stepTitle: String,
        override val stepDescription: String,
        val durationMs: Long = 1500L,
    ) : CameraKeyframe

    data class OrbitAround(
        override val stepTitle: String,
        override val stepDescription: String,
        val center: LatLng,
        val altitude: Double,
        val range: Double,
        val tilt: Double,
        val startHeading: Double,
        val endHeading: Double,
        val durationMs: Long = 4000L,
    ) : CameraKeyframe
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
        val SAN_FRANCISCO_TOUR = listOf(
            CameraKeyframe.FlyTo(
                stepTitle = "1. Golden Gate Bridge Flight",
                stepDescription = "3D Airplane flight over Golden Gate Bridge",
                targetCenter = LatLng(37.8199, -122.4783),
                targetAltitude = 200.0,
                targetHeading = 105.0,
                targetTilt = 65.0,
                targetRange = 600.0,
                durationMs = 2500L,
            ),
            CameraKeyframe.DwellPause(
                stepTitle = "2. Mid-Air Observation",
                stepDescription = "Dwell pause observing 3D airplane over Golden Gate",
                durationMs = 1500L,
            ),
            CameraKeyframe.OrbitAround(
                stepTitle = "3. Golden Gate 360° Orbit",
                stepDescription = "360° orbital camera spin around flying airplane",
                center = LatLng(37.8199, -122.4783),
                altitude = 200.0,
                range = 600.0,
                tilt = 65.0,
                startHeading = 105.0,
                endHeading = 465.0,
                durationMs = 4000L,
            ),
            CameraKeyframe.FlyTo(
                stepTitle = "4. Transit to Coit Tower",
                stepDescription = "Airplane flight to Coit Tower Landmark",
                targetCenter = LatLng(37.8024, -122.4058),
                targetAltitude = 200.0,
                targetHeading = 105.0,
                targetTilt = 65.0,
                targetRange = 600.0,
                durationMs = 3000L,
            ),
        )

        // 15 Fine-Grained Waypoints on the direct route from Golden Gate Bridge to Coit Tower
        val AIRPLANE_FLIGHT_PATH = listOf(
            LatLng(37.8199, -122.4783), // 1. Golden Gate Bridge (Source)
            LatLng(37.8188, -122.4735), // 2. Fort Point / Presidio Overlook
            LatLng(37.8175, -122.4685), // 3. Crissy Field West
            LatLng(37.8160, -122.4635), // 4. Crissy Field East
            LatLng(37.8145, -122.4585), // 5. Marina Green West
            LatLng(37.8130, -122.4530), // 6. Marina District Center
            LatLng(37.8115, -122.4475), // 7. Fort Mason West
            LatLng(37.8100, -122.4420), // 8. Fort Mason Heights
            LatLng(37.8085, -122.4365), // 9. Aquatic Park Cove
            LatLng(37.8070, -122.4310), // 10. Fisherman's Wharf West
            LatLng(37.8058, -122.4250), // 11. Fisherman's Wharf Center
            LatLng(37.8048, -122.4195), // 12. Pier 39 Promenade
            LatLng(37.8038, -122.4140), // 13. Embarcadero North
            LatLng(37.8030, -122.4090), // 14. Telegraph Hill Slopes
            LatLng(37.8024, -122.4058), // 15. Coit Tower (Destination)
        )
    }
}

@Composable
fun AdvancedCameraAnimationScreen() {
    val tourSequence = AdvancedCameraAnimationActivity.SAN_FRANCISCO_TOUR
    val flightPath = AdvancedCameraAnimationActivity.AIRPLANE_FLIGHT_PATH
    val scope = rememberCoroutineScope()

    var mapInstance by remember { mutableStateOf<GoogleMap3D?>(null) }
    var selectedApproach by remember { mutableStateOf(AnimationApproach.DISPATCHER_FRAME_LOOP) }

    val initialStep = tourSequence.first() as CameraKeyframe.FlyTo
    var currentCameraState by remember {
        mutableStateOf(
            camera {
                center = latLngAltitude {
                    latitude = initialStep.targetCenter.latitude
                    longitude = initialStep.targetCenter.longitude
                    altitude = initialStep.targetAltitude
                }
                heading = 105.0
                tilt = initialStep.targetTilt
                range = initialStep.targetRange
            },
        )
    }

    var planePosition by remember {
        mutableStateOf(
            latLngAltitude {
                latitude = flightPath.first().latitude
                longitude = flightPath.first().longitude
                altitude = 200.0
            },
        )
    }
    var planeHeading by remember {
        val rawH = SphericalUtil.computeHeading(flightPath.first(), flightPath[1])
        mutableStateOf(normalizeHeading(rawH + 180.0))
    }

    val activeModels = remember(planePosition, planeHeading) {
        listOf(
            ModelConfig(
                key = "airplane_model",
                position = planePosition,
                url = "https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/Airplane.glb",
                altitudeMode = AltitudeMode.ABSOLUTE,
                scale = ModelScale.Uniform(0.08f),
                heading = planeHeading,
                tilt = -90.0,
                roll = 0.0,
            ),
        )
    }

    var currentStepIndex by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    var tourJob by remember { mutableStateOf<Job?>(null) }
    var restartJob by remember { mutableStateOf<Job?>(null) }

    val cumulativeDistances: DoubleArray = remember(flightPath) {
        RouteEngine.calculateCumulativeDistances(flightPath)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
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
            tourJob?.cancel()
            restartJob?.cancel()
            mapInstance?.stopCameraAnimation()
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
        val flightHeading = SphericalUtil.computeHeading(flightPath[flightPath.size - 2], targetLoc)
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
                val step = tourSequence[currentStepIndex]
                when (step) {
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
                        val totalMs = step.durationMs
                        val startTimeMs = SystemClock.uptimeMillis()
                        while (isActive && isPlaying) {
                            val elapsedMs = SystemClock.uptimeMillis() - startTimeMs
                            if (elapsedMs > totalMs) break

                            val t = (elapsedMs.toDouble() / totalMs).coerceIn(0.0, 1.0)
                            val orbitHeading = interpolateAngle(step.startHeading, step.endHeading, t)

                            val updatedCam = camera {
                                center = latLngAltitude {
                                    latitude = step.center.latitude
                                    longitude = step.center.longitude
                                    altitude = step.altitude
                                }
                                heading = normalizeHeading(orbitHeading)
                                tilt = step.tilt
                                range = step.range
                            }

                            planePosition = latLngAltitude {
                                latitude = step.center.latitude
                                longitude = step.center.longitude
                                altitude = 200.0
                            }
                            planeHeading = normalizeHeading(orbitHeading + 180.0)
                            currentCameraState = updatedCam
                            delay(16L) // Keeps dispatch rhythm steady
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

        tourJob = scope.launch(Dispatchers.Main) {
            val totalDistance = cumulativeDistances.last().coerceAtLeast(1.0)
            val flightSpeedMps = 400.0
            var elapsedDistance = 0.0
            var lastTime = System.currentTimeMillis()

            while (isPlaying && isActive) {
                val now = System.currentTimeMillis()
                val dt = (now - lastTime) / 1000.0
                lastTime = now

                elapsedDistance += flightSpeedMps * dt

                if (elapsedDistance >= totalDistance) {
                    elapsedDistance = totalDistance
                    val posAndHeading = RouteEngine.calculatePositionAndHeading(
                        flightPath,
                        cumulativeDistances,
                        elapsedDistance,
                        30.0,
                    )
                    val planeH = posAndHeading.heading.toDouble() + 180.0
                    planePosition = latLngAltitude {
                        latitude = posAndHeading.position.latitude
                        longitude = posAndHeading.position.longitude
                        altitude = 200.0
                    }
                    planeHeading = normalizeHeading(planeH)
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
                val planeH = posAndHeading.heading.toDouble() + 180.0
                planePosition = latLngAltitude {
                    latitude = posAndHeading.position.latitude
                    longitude = posAndHeading.position.longitude
                    altitude = 200.0
                }
                planeHeading = normalizeHeading(planeH)

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
