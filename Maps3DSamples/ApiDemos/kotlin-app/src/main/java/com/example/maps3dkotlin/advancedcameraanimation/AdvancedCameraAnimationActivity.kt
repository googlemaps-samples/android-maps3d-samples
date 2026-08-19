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

package com.example.maps3dkotlin.advancedcameraanimation

import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import androidx.lifecycle.lifecycleScope
import com.example.maps3d.common.RouteEngine
import com.example.maps3dcommon.R
import com.example.maps3dkotlin.sampleactivity.SampleBaseActivity
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.FlyToOptions
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.android.gms.maps3d.model.Model
import com.google.android.gms.maps3d.model.ModelOptions
import com.google.android.gms.maps3d.model.Orientation
import com.google.android.gms.maps3d.model.Vector3D
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.material.appbar.MaterialToolbar
import com.google.maps.android.SphericalUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

enum class AnimationApproach {
    SIMPLE_FLY_TO,
    KEYFRAME_TOUR,
    DISPATCHER_FRAME_LOOP,
    ORBIT_360_SPIN
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
        val durationMs: Long = 2500L
    ) : CameraKeyframe

    data class DwellPause(
        override val stepTitle: String,
        override val stepDescription: String,
        val durationMs: Long = 1500L
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
        val durationMs: Long = 4000L
    ) : CameraKeyframe
}

class AdvancedCameraAnimationActivity : SampleBaseActivity() {

    override val TAG = "AdvancedCameraAnimation"

    override val initialCamera: Camera
        get() {
            val firstLoc = AIRPLANE_FLIGHT_PATH.first()
            val initialHeading = SphericalUtil.computeHeading(firstLoc, AIRPLANE_FLIGHT_PATH[1])
            return camera {
                center = latLngAltitude {
                    latitude = firstLoc.latitude
                    longitude = firstLoc.longitude
                    altitude = 200.0
                }
                heading = normalizeHeading(initialHeading)
                tilt = 65.0
                range = 600.0
            }
        }

    private var airplaneModel: Model? = null
    private var currentStepIndex = 0
    private var isPlaying = false
    private var tourJob: Job? = null
    private var restartJob: Job? = null
    private var selectedApproach = AnimationApproach.DISPATCHER_FRAME_LOOP

    private var btnPlayPause: Button? = null

    private val cumulativeDistances by lazy {
        RouteEngine.calculateCumulativeDistances(AIRPLANE_FLIGHT_PATH)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate control panel overlay into the map container managed by SampleBaseActivity
        findViewById<ViewGroup>(R.id.map_container)?.let { container ->
            layoutInflater.inflate(R.layout.control_panel_advanced_animation, container, true)
        }

        findViewById<MaterialToolbar>(R.id.top_bar)?.apply {
            title = "Advanced Camera Animation"
            setNavigationOnClickListener { finish() }
        }

        findViewById<RadioGroup>(R.id.rg_approach)?.setOnCheckedChangeListener { _, checkedId ->
            selectedApproach = when (checkedId) {
                R.id.rb_simple_flyto -> AnimationApproach.SIMPLE_FLY_TO
                R.id.rb_keyframe_tour -> AnimationApproach.KEYFRAME_TOUR
                R.id.rb_orbit_spin -> AnimationApproach.ORBIT_360_SPIN
                else -> AnimationApproach.DISPATCHER_FRAME_LOOP
            }
            resetAndRestartTour()
        }

        findViewById<Button>(R.id.btn_reset)?.setOnClickListener {
            resetAndRestartTour()
        }

        btnPlayPause = findViewById(R.id.btn_play_pause)
        btnPlayPause?.setOnClickListener {
            if (isPlaying) {
                stopTour()
            } else {
                startSelectedApproach()
            }
        }
    }

    private fun updatePlayPauseButtonState() {
        btnPlayPause?.text = if (isPlaying) "Pause" else "Play"
    }

    override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
        super.onMap3DViewReady(googleMap3D)

        // Instantiate 3D Airplane Model on map at initial start position
        val startLoc = AIRPLANE_FLIGHT_PATH.first()
        val initialHeading = SphericalUtil.computeHeading(startLoc, AIRPLANE_FLIGHT_PATH[1])
        updateAirplaneModel(startLoc, initialHeading + 180.0)

        // Auto-start smooth flight animation after 1 second delay
        restartJob = lifecycleScope.launch {
            delay(1000L.milliseconds)
            startSelectedApproach()
        }
    }

    /**
     * Updates the 3D Airplane Model's position and orientation on the map.
     * Note: Calling `map.addModel(opts)` continuously with the same `id` string
     * is the recommended approach for dynamically updating a model's location.
     *
     * Remote URLs: Models should be hosted and loaded via external URL.
     */
    private fun updateAirplaneModel(targetLatLng: LatLng, planeHeadingDeg: Double) {
        googleMap3D?.let { map ->
            val opts = ModelOptions().apply {
                id = MODEL_ID
                position = LatLngAltitude(targetLatLng.latitude, targetLatLng.longitude, 200.0)
                altitudeMode = AltitudeMode.ABSOLUTE
                orientation = Orientation(normalizeHeading(planeHeadingDeg), -90.0, 0.0)
                url = PLANE_URL
                scale = Vector3D(0.08, 0.08, 0.08)
            }
            airplaneModel = map.addModel(opts)
        }
    }

    private fun startSelectedApproach() {
        stopTour()
        when (selectedApproach) {
            AnimationApproach.SIMPLE_FLY_TO -> runSimpleFlyTo()
            AnimationApproach.KEYFRAME_TOUR -> startOrResumeTour()
            AnimationApproach.DISPATCHER_FRAME_LOOP -> runFrameDispatcherLoop()
            AnimationApproach.ORBIT_360_SPIN -> run360OrbitSpin()
        }
    }

    private fun runSimpleFlyTo() {
        stopTour()
        isPlaying = true
        updatePlayPauseButtonState()

        val targetLoc = AIRPLANE_FLIGHT_PATH.last()
        val flightHeading = SphericalUtil.computeHeading(AIRPLANE_FLIGHT_PATH[AIRPLANE_FLIGHT_PATH.size - 2], targetLoc)
        updateAirplaneModel(targetLoc, flightHeading + 180.0)

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
        googleMap3D?.flyCameraTo(FlyToOptions(targetCam, 1500L))
        isPlaying = false
        updatePlayPauseButtonState()
    }

    /**
     * Executes multi-step keyframe queue tour smoothly stage by stage.
     */
    private fun startOrResumeTour() {
        stopTour()
        isPlaying = true
        updatePlayPauseButtonState()
        currentStepIndex = 0

        tourJob = lifecycleScope.launch(Dispatchers.Main) {
            val frameDurationMs = 16L

            while (currentStepIndex < SAN_FRANCISCO_TOUR.size && isActive && isPlaying) {
                val step = SAN_FRANCISCO_TOUR[currentStepIndex]

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
                        updateAirplaneModel(step.targetCenter, step.targetHeading + 180.0)
                        googleMap3D?.flyCameraTo(FlyToOptions(targetCam, step.durationMs))
                        delay(step.durationMs.milliseconds)
                    }

                    is CameraKeyframe.DwellPause -> {
                        delay(step.durationMs.milliseconds)
                    }

                    is CameraKeyframe.OrbitAround -> {
                        val totalFrames = (step.durationMs / frameDurationMs).coerceAtLeast(1)
                        for (frame in 0..totalFrames) {
                            if (!isActive || !isPlaying) break
                            val t = frame.toDouble() / totalFrames
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

                            updateAirplaneModel(step.center, orbitHeading + 180.0)
                            googleMap3D?.setCamera(updatedCam)
                            delay(frameDurationMs.milliseconds)
                        }
                    }
                }

                if (!isActive || !isPlaying) break

                if (currentStepIndex < SAN_FRANCISCO_TOUR.size - 1) {
                    currentStepIndex++
                } else {
                    isPlaying = false
                    updatePlayPauseButtonState()
                    break
                }
            }
        }
    }

    /**
     * Frame Dispatcher Animation Loop.
     * High-speed flight animation (400 m/s) stopping cleanly at destination.
     * 
     * For the most visually uniform cinematic sweeping motion, we recommend using
     * `Choreographer.FrameCallback` to sync our delta-time interpolation directly 
     * to the hardware display frames.
     */
    private fun runFrameDispatcherLoop() {
        stopTour()
        isPlaying = true
        updatePlayPauseButtonState()

        val totalDistance = cumulativeDistances.last().coerceAtLeast(1.0)
        val flightSpeedMps = 400.0 // Fast 400 m/s high-speed flight

        val frameCallback = object : android.view.Choreographer.FrameCallback {
            private var lastTimeNanos = 0L
            private var elapsedDistance = 0.0

            override fun doFrame(frameTimeNanos: Long) {
                if (!isPlaying) return

                if (lastTimeNanos == 0L) {
                    lastTimeNanos = frameTimeNanos
                    android.view.Choreographer.getInstance().postFrameCallback(this)
                    return
                }

                val dt = (frameTimeNanos - lastTimeNanos) / 1_000_000_000.0
                lastTimeNanos = frameTimeNanos

                elapsedDistance += flightSpeedMps * dt

                if (elapsedDistance >= totalDistance) {
                    elapsedDistance = totalDistance
                    val posAndHeading = RouteEngine.calculatePositionAndHeading(
                        AIRPLANE_FLIGHT_PATH,
                        cumulativeDistances,
                        elapsedDistance,
                        30.0
                    )
                    val planeHeading = posAndHeading.heading.toDouble() + 180.0
                    updateAirplaneModel(posAndHeading.position, planeHeading)

                    val finalCam = camera {
                        center = latLngAltitude {
                            latitude = posAndHeading.position.latitude
                            longitude = posAndHeading.position.longitude
                            altitude = 200.0
                        }
                        heading = normalizeHeading(posAndHeading.heading.toDouble())
                        tilt = 65.0
                        range = 600.0
                    }
                    googleMap3D?.setCamera(finalCam)
                    isPlaying = false
                    updatePlayPauseButtonState()
                    return
                }

                val posAndHeading = RouteEngine.calculatePositionAndHeading(
                    AIRPLANE_FLIGHT_PATH,
                    cumulativeDistances,
                    elapsedDistance,
                    30.0
                )

                val planeHeading = posAndHeading.heading.toDouble() + 180.0
                updateAirplaneModel(posAndHeading.position, planeHeading)

                val updatedCam = camera {
                    center = latLngAltitude {
                        latitude = posAndHeading.position.latitude
                        longitude = posAndHeading.position.longitude
                        altitude = 200.0
                    }
                    heading = normalizeHeading(posAndHeading.heading.toDouble())
                    tilt = 65.0
                    range = 600.0
                }
                googleMap3D?.setCamera(updatedCam)

                android.view.Choreographer.getInstance().postFrameCallback(this)
            }
        }
        android.view.Choreographer.getInstance().postFrameCallback(frameCallback)
    }

    /**
     * Option 4: Continuous 360-degree orbital camera spin around landmark.
     */
    private fun run360OrbitSpin() {
        stopTour()
        isPlaying = true
        updatePlayPauseButtonState()

        val targetCenter = AIRPLANE_FLIGHT_PATH.first()
        updateAirplaneModel(targetCenter, 105.0 + 180.0)

        tourJob = lifecycleScope.launch(Dispatchers.Main) {
            val frameDurationMs = 16L
            val totalMs = 6000L // 6 second smooth 360° spin
            val totalFrames = (totalMs / frameDurationMs).coerceAtLeast(1)
            val startHeading = 105.0

            for (frame in 0..totalFrames) {
                if (!isActive || !isPlaying) break
                val t = frame.toDouble() / totalFrames
                val headingDeg = (startHeading + t * 360.0) % 360.0

                val currentCam = camera {
                    center = latLngAltitude {
                        latitude = targetCenter.latitude
                        longitude = targetCenter.longitude
                        altitude = 200.0
                    }
                    heading = normalizeHeading(headingDeg)
                    tilt = 65.0
                    range = 600.0
                }
                googleMap3D?.setCamera(currentCam)
                delay(frameDurationMs.milliseconds)
            }
            isPlaying = false
            updatePlayPauseButtonState()
        }
    }

    private fun stopTour() {
        isPlaying = false
        updatePlayPauseButtonState()
        restartJob?.cancel()
        restartJob = null
        tourJob?.cancel()
        tourJob = null
        googleMap3D?.stopCameraAnimation()
    }

    /**
     * Resets the camera and airplane model to the initial start location and restarts animation.
     */
    fun resetAndRestartTour() {
        stopTour()
        currentStepIndex = 0

        val startLoc = AIRPLANE_FLIGHT_PATH.first()
        val initialHeading = SphericalUtil.computeHeading(startLoc, AIRPLANE_FLIGHT_PATH[1])
        updateAirplaneModel(startLoc, initialHeading + 180.0)

        val resetCam = camera {
            center = latLngAltitude {
                latitude = startLoc.latitude
                longitude = startLoc.longitude
                altitude = 200.0
            }
            heading = normalizeHeading(initialHeading)
            tilt = 65.0
            range = 600.0
        }
        googleMap3D?.setCamera(resetCam)

        restartJob = lifecycleScope.launch {
            delay(300.milliseconds)
            startSelectedApproach()
        }
    }

    override fun onPause() {
        super.onPause()
        stopTour()
    }

    companion object {
        private const val MODEL_ID = "airplane_model"
        private const val PLANE_URL = "https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/Airplane.glb"

        val SAN_FRANCISCO_TOUR = listOf(
            CameraKeyframe.FlyTo(
                stepTitle = "1. Golden Gate Bridge Flight",
                stepDescription = "3D Airplane flight over Golden Gate Bridge",
                targetCenter = LatLng(37.8199, -122.4783),
                targetAltitude = 200.0,
                targetHeading = 105.0,
                targetTilt = 65.0,
                targetRange = 600.0,
                durationMs = 2500L
            ),
            CameraKeyframe.DwellPause(
                stepTitle = "2. Mid-Air Observation",
                stepDescription = "Dwell pause observing 3D airplane over Golden Gate",
                durationMs = 1500L
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
                durationMs = 4000L
            ),
            CameraKeyframe.FlyTo(
                stepTitle = "4. Transit to Coit Tower",
                stepDescription = "Airplane flight to Coit Tower Landmark",
                targetCenter = LatLng(37.8024, -122.4058),
                targetAltitude = 200.0,
                targetHeading = 105.0,
                targetTilt = 65.0,
                targetRange = 600.0,
                durationMs = 3000L
            )
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
            LatLng(37.8024, -122.4058)  // 15. Coit Tower (Destination)
        )

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
    }
}
