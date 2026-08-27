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
import android.view.Choreographer
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.maps3d.common.CameraKeyframe
import com.example.maps3d.common.RouteEngine
import com.example.maps3d.common.TourData
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
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Suspends until the 3D map camera animation completes using [com.google.android.gms.maps3d.OnCameraAnimationEndListener].
 */
private suspend fun GoogleMap3D.awaitFlyCameraTo(options: FlyToOptions) =
    suspendCancellableCoroutine { continuation ->
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

/**
 * Normalizes heading angles into standard compass range [0.0, 360.0).
 */
private fun normalizeHeading(deg: Double): Double = (deg % 360.0 + 360.0) % 360.0

/**
 * Performs shortest-arc spherical angular interpolation between two headings.
 */
private fun interpolateAngle(start: Double, end: Double, fraction: Double): Double {
    var diff = (end - start) % 360.0
    if (diff > 180.0) diff -= 360.0
    if (diff < -180.0) diff += 360.0
    return normalizeHeading(start + diff * fraction)
}

enum class AnimationApproach(val title: String) {
    SIMPLE_FLY_TO("1. SDK Simple flyTo (Native Transition)"),
    KEYFRAME_TOUR("2. Scripted Keyframe Queue (Multi-Stage Tour)"),
    DISPATCHER_FRAME_LOOP("3. High-Speed Flight (Display Sync Choreographer)"),
    ORBIT_360_SPIN("4. Orbital Spin (Continuous 360° Rotation)"),
}

/**
 * Advanced Camera Animation demonstrating cinematic 3D camera controls and 3D airplane model tracking.
 *
 * Animation Approaches:
 * 1. Simple flyTo: Native SDK camera transition.
 * 2. Keyframe Queue Tour: Multi-stage sequenced tour (fly-to, dwell pause, 360° landmark orbit).
 * 3. Frame Dispatcher Loop: High-speed flight synced directly to hardware display refresh rate via Choreographer.
 * 4. 360° Orbital Spin: Continuous circular inspection around landmarks.
 */
class AdvancedCameraAnimationActivity : SampleBaseActivity() {

    override val TAG: String = "AdvancedCameraAnimation"

    override val initialCamera: Camera
        get() {
            val firstLoc = TourData.AIRPLANE_FLIGHT_PATH.first()
            val initialHeading = SphericalUtil.computeHeading(firstLoc, TourData.AIRPLANE_FLIGHT_PATH[1])
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
    private var frameDispatcherCallback: Choreographer.FrameCallback? = null
    private var selectedApproach = AnimationApproach.DISPATCHER_FRAME_LOOP

    private var btnPlayPause: Button? = null
    private var tvTourStatus: TextView? = null

    private val cumulativeDistances by lazy {
        RouteEngine.calculateCumulativeDistances(TourData.AIRPLANE_FLIGHT_PATH)
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

        tvTourStatus = findViewById(R.id.tv_tour_status)

        findViewById<RadioGroup>(R.id.rg_animation_approach)?.setOnCheckedChangeListener { _, checkedId ->
            selectedApproach = when (checkedId) {
                R.id.rb_simple_fly_to -> AnimationApproach.SIMPLE_FLY_TO
                R.id.rb_keyframe_tour -> AnimationApproach.KEYFRAME_TOUR
                R.id.rb_dispatcher_frame_loop -> AnimationApproach.DISPATCHER_FRAME_LOOP
                R.id.rb_orbit_360_spin -> AnimationApproach.ORBIT_360_SPIN
                else -> AnimationApproach.DISPATCHER_FRAME_LOOP
            }
            resetAndRestartTour()
        }

        findViewById<Button>(R.id.btn_reset)?.setOnClickListener {
            resetAndRestartTour()
        }

        btnPlayPause = findViewById(R.id.btn_play_pause)
        btnPlayPause?.setOnClickListener {
            if (isPlaying) stopTour() else startSelectedApproach()
        }
    }

    private fun updatePlayPauseButtonState() {
        btnPlayPause?.text = if (isPlaying) "Pause" else "Play"
    }

    override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
        super.onMap3DViewReady(googleMap3D)

        // Position 3D Airplane Model at route origin
        val startLoc = TourData.AIRPLANE_FLIGHT_PATH.first()
        val initialHeading = SphericalUtil.computeHeading(startLoc, TourData.AIRPLANE_FLIGHT_PATH[1])
        updateAirplaneModel(startLoc, initialHeading + 180.0)

        // Auto-start animation after initial map load
        restartJob = lifecycleScope.launch {
            delay(1000L.milliseconds)
            startSelectedApproach()
        }
    }

    /**
     * Updates the 3D Airplane Model position and orientation on the map.
     * In-place ID upserting avoids tearing or recreation overhead.
     */
    private fun updateAirplaneModel(targetLatLng: LatLng, planeHeadingDeg: Double) {
        googleMap3D?.let { map ->
            val opts = ModelOptions().apply {
                id = TourData.MODEL_ID
                position = LatLngAltitude(targetLatLng.latitude, targetLatLng.longitude, 200.0)
                altitudeMode = AltitudeMode.ABSOLUTE
                orientation = Orientation(normalizeHeading(planeHeadingDeg), -90.0, 0.0)
                url = TourData.PLANE_URL
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

    /**
     * Option 1: Simple flyTo animation directly to destination.
     */
    private fun runSimpleFlyTo() {
        stopTour()
        isPlaying = true
        updatePlayPauseButtonState()
        tvTourStatus?.setText(R.string.approach_simple_fly_to)

        val targetLoc = TourData.AIRPLANE_FLIGHT_PATH.last()
        val flightHeading = SphericalUtil.computeHeading(
            TourData.AIRPLANE_FLIGHT_PATH[TourData.AIRPLANE_FLIGHT_PATH.size - 2],
            targetLoc
        )
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
        tourJob = lifecycleScope.launch(Dispatchers.Main) {
            googleMap3D?.awaitFlyCameraTo(FlyToOptions(targetCam, 1500L))
            isPlaying = false
            updatePlayPauseButtonState()
            tvTourStatus?.setText(R.string.aerial_tour_status_finished)
        }
    }

    /**
     * Option 2: Executes multi-step keyframe queue tour smoothly stage by stage.
     */
    private fun startOrResumeTour() {
        stopTour()
        isPlaying = true
        updatePlayPauseButtonState()
        currentStepIndex = 0

        tourJob = lifecycleScope.launch(Dispatchers.Main) {
            val frameDurationMs = 16L

            while (currentStepIndex < TourData.SAN_FRANCISCO_TOUR.size && isActive && isPlaying) {
                val step = TourData.SAN_FRANCISCO_TOUR[currentStepIndex]
                tvTourStatus?.text = getString(
                    R.string.aerial_tour_status_running,
                    currentStepIndex + 1,
                    TourData.SAN_FRANCISCO_TOUR.size,
                    step.stepTitle
                )

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
                        googleMap3D?.awaitFlyCameraTo(FlyToOptions(targetCam, step.durationMs))
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

                if (currentStepIndex < TourData.SAN_FRANCISCO_TOUR.size - 1) {
                    currentStepIndex++
                } else {
                    isPlaying = false
                    updatePlayPauseButtonState()
                    tvTourStatus?.setText(R.string.aerial_tour_status_finished)
                    break
                }
            }
        }
    }

    /**
     * Option 3: Frame Dispatcher Animation Loop.
     * High-speed flight animation (400 m/s) synced to hardware display frames via Choreographer.
     */
    private fun runFrameDispatcherLoop() {
        stopTour()
        isPlaying = true
        updatePlayPauseButtonState()
        tvTourStatus?.setText(R.string.approach_dispatcher_frame_loop)

        val totalDistance = cumulativeDistances.last().coerceAtLeast(1.0)
        val flightSpeedMps = 400.0

        val frameCallback = object : Choreographer.FrameCallback {
            private var lastTimeNanos = 0L
            private var elapsedDistance = 0.0

            override fun doFrame(frameTimeNanos: Long) {
                if (!isPlaying) return

                if (lastTimeNanos == 0L) {
                    lastTimeNanos = frameTimeNanos
                    Choreographer.getInstance().postFrameCallback(this)
                    return
                }

                val dt = (frameTimeNanos - lastTimeNanos) / 1_000_000_000.0
                lastTimeNanos = frameTimeNanos

                elapsedDistance += flightSpeedMps * dt

                if (elapsedDistance >= totalDistance) {
                    elapsedDistance = totalDistance
                    val posAndHeading = RouteEngine.calculatePositionAndHeading(
                        TourData.AIRPLANE_FLIGHT_PATH,
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
                    tvTourStatus?.setText(R.string.aerial_tour_status_finished)
                    return
                }

                val posAndHeading = RouteEngine.calculatePositionAndHeading(
                    TourData.AIRPLANE_FLIGHT_PATH,
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

                Choreographer.getInstance().postFrameCallback(this)
            }
        }
        frameDispatcherCallback = frameCallback
        Choreographer.getInstance().postFrameCallback(frameCallback)
    }

    /**
     * Option 4: Continuous 360-degree orbital camera spin around landmark.
     */
    private fun run360OrbitSpin() {
        stopTour()
        isPlaying = true
        updatePlayPauseButtonState()
        tvTourStatus?.setText(R.string.approach_orbit_360_spin)

        val targetCenter = TourData.AIRPLANE_FLIGHT_PATH.first()
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
            tvTourStatus?.setText(R.string.aerial_tour_status_finished)
        }
    }

    private fun stopTour() {
        isPlaying = false
        updatePlayPauseButtonState()
        restartJob?.cancel()
        restartJob = null
        tourJob?.cancel()
        tourJob = null
        frameDispatcherCallback?.let {
            Choreographer.getInstance().removeFrameCallback(it)
            frameDispatcherCallback = null
        }
        googleMap3D?.setCameraAnimationEndListener(null)
        googleMap3D?.stopCameraAnimation()
    }

    /**
     * Resets the camera and airplane model to the initial start location and restarts animation.
     */
    fun resetAndRestartTour() {
        stopTour()
        currentStepIndex = 0

        val startLoc = TourData.AIRPLANE_FLIGHT_PATH.first()
        val initialHeading = SphericalUtil.computeHeading(startLoc, TourData.AIRPLANE_FLIGHT_PATH[1])
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
        tvTourStatus?.setText(R.string.aerial_tour_status_idle)

        restartJob = lifecycleScope.launch {
            delay(300.milliseconds)
            startSelectedApproach()
        }
    }

    override fun onPause() {
        super.onPause()
        stopTour()
    }
}
