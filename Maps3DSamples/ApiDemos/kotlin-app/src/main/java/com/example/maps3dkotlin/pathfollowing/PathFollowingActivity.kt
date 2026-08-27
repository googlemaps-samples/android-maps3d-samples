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

package com.example.maps3dkotlin.pathfollowing

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Choreographer
import android.view.MotionEvent
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.graphics.toColorInt
import androidx.core.view.WindowCompat
import com.example.maps3d.common.PathData
import com.example.maps3d.common.PathEngine
import com.example.maps3dcommon.R
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.Map3DView
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.android.gms.maps3d.model.Polyline
import com.google.android.gms.maps3d.model.PolylineOptions
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.Slider

/**
 * Advanced sample demonstrating ground-level path following in Kotlin.
 *
 * Key Concepts Demonstrated:
 * 1. Dual-Polyline Architecture: Layered base route (lower z-index) and traversed progress route (higher z-index).
 * 2. In-Place Polyline ID Updates: Stable IDs prevent render flickering during rapid real-time updates.
 * 3. 3D Altitude Modes: Dynamic switching between Clamp to Ground, Relative, and Absolute elevation.
 * 4. Occlusion Control: Toggling [PolylineOptions.setDrawsOccludedSegments] through terrain and buildings.
 * 5. Kinematic Heading Smoothing: Exponential moving average low-pass filter to smooth camera cornering.
 */
class PathFollowingActivity : AppCompatActivity(), OnMap3DViewReadyCallback {

    private lateinit var map3DView: Map3DView
    private var googleMap3D: GoogleMap3D? = null

    // Control panel overlay bindings
    private var controlsCard: CardView? = null
    private var cardHeader: View? = null
    private var btnCollapse: MaterialButton? = null
    private var isCollapsed = false

    private lateinit var rgEnvironment: RadioGroup
    private lateinit var rgAltitudeMode: RadioGroup
    private lateinit var switchDrawsOccludedSegments: MaterialSwitch
    private lateinit var pathAltitudeSlider: Slider
    private lateinit var pathAltitudeSliderLabel: TextView
    private lateinit var btnPlayPause: MaterialButton
    private lateinit var progressSlider: Slider
    private lateinit var rangeSlider: Slider
    private lateinit var rangeSliderLabel: TextView
    private lateinit var altitudeSlider: Slider
    private lateinit var altitudeSliderLabel: TextView
    private lateinit var headingSlider: Slider
    private lateinit var headingSliderLabel: TextView
    private lateinit var tiltSlider: Slider
    private lateinit var tiltSliderLabel: TextView
    private lateinit var speedSlider: Slider
    private lateinit var speedSliderLabel: TextView

    // Control parameters
    private var cameraRange = 300.0
    private var groundAltitude = 20.0
    private var headingOffset = 0.0
    private var cameraTilt = 70.0
    private var followSpeedMps = 30.0
    private var pathAltitudeMode: Int = AltitudeMode.CLAMP_TO_GROUND
    private var pathAltitudeOffset: Double = 0.5
    private var drawsOccludedSegments: Boolean = true

    // Path state
    private var currentPath: List<LatLngAltitude> = PathData.URBAN_PATH
    private var cumulativeDistances: DoubleArray = doubleArrayOf()
    private var totalDistance: Double = 0.0
    private var elapsedDistance: Double = 0.0
    private var isPlaying = false
    private var isUserScrubbing = false
    private var currentHeading: Double? = null

    private val baseAltitude: Double
        get() = if (currentPath == PathData.RURAL_PATH) 45.0 else 50.0

    // Polyline handles
    private var staticRoutePolyline: Polyline? = null
    private var progressPolyline: Polyline? = null

    // Animation & auto-fade handlers
    private var frameCallback: Choreographer.FrameCallback? = null
    private val fadeHandler = Handler(Looper.getMainLooper())
    private val fadeOutRunnable = Runnable {
        if (controlsCard != null && !isCollapsed) {
            controlsCard?.animate()?.alpha(0.8f)?.setDuration(400)?.start()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_path_following)

        map3DView = findViewById(R.id.map3dView)
        map3DView.onCreate(savedInstanceState)
        map3DView.getMap3DViewAsync(this)

        initViews()
        loadPath(PathData.URBAN_PATH)
    }

    override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
        this.googleMap3D = googleMap3D
        googleMap3D.setOnMapReadyListener {
            drawPathPolylines()
            updateCameraPositionForDistance(0.0)
        }

        scheduleControlsFade()
    }

    private fun initViews() {
        controlsCard = findViewById(R.id.controls_card)
        cardHeader = findViewById(R.id.card_header)
        btnCollapse = findViewById(R.id.btn_collapse)

        btnCollapse?.setOnClickListener { toggleControlsCard() }
        cardHeader?.setOnClickListener { toggleControlsCard() }

        rgEnvironment = findViewById(R.id.rg_environment)
        rgAltitudeMode = findViewById(R.id.rg_altitude_mode)
        switchDrawsOccludedSegments = findViewById(R.id.switch_draws_occluded_segments)
        pathAltitudeSlider = findViewById(R.id.path_altitude_slider)
        pathAltitudeSliderLabel = findViewById(R.id.path_altitude_slider_label)
        btnPlayPause = findViewById(R.id.btn_play_pause)
        progressSlider = findViewById(R.id.progress_slider)
        rangeSlider = findViewById(R.id.range_slider)
        rangeSliderLabel = findViewById(R.id.range_slider_label)
        altitudeSlider = findViewById(R.id.altitude_slider)
        altitudeSliderLabel = findViewById(R.id.altitude_slider_label)
        headingSlider = findViewById(R.id.heading_slider)
        headingSliderLabel = findViewById(R.id.heading_slider_label)
        tiltSlider = findViewById(R.id.tilt_slider)
        tiltSliderLabel = findViewById(R.id.tilt_slider_label)
        speedSlider = findViewById(R.id.speed_slider)
        speedSliderLabel = findViewById(R.id.speed_slider_label)

        setupEventListeners()
    }

    private fun setupEventListeners() {
        rgEnvironment.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_urban -> switchEnvironment(PathData.URBAN_PATH)
                R.id.rb_rural -> switchEnvironment(PathData.RURAL_PATH)
            }
        }

        rgAltitudeMode.setOnCheckedChangeListener { _, checkedId ->
            pathAltitudeMode = when (checkedId) {
                R.id.rb_clamp_to_ground -> AltitudeMode.CLAMP_TO_GROUND
                R.id.rb_relative_to_ground -> AltitudeMode.RELATIVE_TO_GROUND
                R.id.rb_relative_to_mesh -> AltitudeMode.RELATIVE_TO_MESH
                R.id.rb_absolute -> AltitudeMode.ABSOLUTE
                else -> AltitudeMode.CLAMP_TO_GROUND
            }
            redrawPolylinesAndCamera()
        }

        switchDrawsOccludedSegments.isChecked = drawsOccludedSegments
        switchDrawsOccludedSegments.setOnCheckedChangeListener { _, isChecked ->
            drawsOccludedSegments = isChecked
            redrawPolylinesAndCamera()
        }

        pathAltitudeSlider.addOnChangeListener { _, value, _ ->
            pathAltitudeOffset = value.toDouble()
            pathAltitudeSliderLabel.text = getString(R.string.path_height_format, pathAltitudeOffset)
            redrawPolylinesAndCamera()
        }

        btnPlayPause.setOnClickListener {
            if (isPlaying) pauseAnimation() else startAnimation()
        }

        progressSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                isUserScrubbing = true
            }

            override fun onStopTrackingTouch(slider: Slider) {
                isUserScrubbing = false
                elapsedDistance = totalDistance * slider.value.toDouble()
                updateCameraPositionForDistance(elapsedDistance)
            }
        })

        progressSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                elapsedDistance = totalDistance * value.toDouble()
                updateCameraPositionForDistance(elapsedDistance)
            }
        }

        rangeSlider.addOnChangeListener { _, value, _ ->
            cameraRange = value.toDouble()
            rangeSliderLabel.text = getString(R.string.camera_range_format, cameraRange.toInt())
            updateCameraPositionForDistance(elapsedDistance)
        }

        altitudeSlider.addOnChangeListener { _, value, _ ->
            groundAltitude = value.toDouble()
            altitudeSliderLabel.text = getString(R.string.ground_altitude_format, groundAltitude.toInt())
            updateCameraPositionForDistance(elapsedDistance)
        }

        headingSlider.addOnChangeListener { _, value, _ ->
            headingOffset = value.toDouble()
            headingSliderLabel.text = getString(R.string.heading_offset_format, headingOffset.toInt())
            updateCameraPositionForDistance(elapsedDistance)
        }

        tiltSlider.addOnChangeListener { _, value, _ ->
            cameraTilt = value.toDouble()
            tiltSliderLabel.text = getString(R.string.camera_tilt_format, cameraTilt.toInt())
            updateCameraPositionForDistance(elapsedDistance)
        }

        speedSlider.addOnChangeListener { _, value, _ ->
            followSpeedMps = value.toDouble()
            speedSliderLabel.text = getString(R.string.follow_speed_format, followSpeedMps.toInt())
        }
    }

    private fun switchEnvironment(path: List<LatLngAltitude>) {
        pauseAnimation()
        currentHeading = null
        elapsedDistance = 0.0
        progressSlider.value = 0f
        clearPolylines()

        if (path == PathData.RURAL_PATH) {
            cameraRange = 450.0
            groundAltitude = 40.0
            cameraTilt = 75.0
            altitudeSlider.valueTo = 2000f
            altitudeSlider.value = 40f
            rangeSlider.value = 450f
            tiltSlider.value = 75f
        } else {
            cameraRange = 300.0
            groundAltitude = 20.0
            cameraTilt = 70.0
            altitudeSlider.valueTo = 200f
            altitudeSlider.value = 20f
            rangeSlider.value = 300f
            tiltSlider.value = 70f
        }

        rangeSliderLabel.text = getString(R.string.camera_range_format, cameraRange.toInt())
        altitudeSliderLabel.text = getString(R.string.ground_altitude_format, groundAltitude.toInt())
        tiltSliderLabel.text = getString(R.string.camera_tilt_format, cameraTilt.toInt())

        loadPath(path)
        drawPathPolylines()
        updateCameraPositionForDistance(0.0)
    }

    private fun loadPath(path: List<LatLngAltitude>) {
        currentPath = path
        cumulativeDistances = PathEngine.calculateCumulativeDistances(path)
        totalDistance = cumulativeDistances.lastOrNull() ?: 0.0
    }

    private fun drawPathPolylines() {
        drawStaticRoutePolyline()
        if (currentPath.isNotEmpty()) {
            val firstPt = LatLng(currentPath[0].latitude, currentPath[0].longitude)
            updateProgressPolyline(elapsedDistance, firstPt, 0)
        }
    }

    private fun drawStaticRoutePolyline() {
        val map = googleMap3D ?: return
        if (currentPath.isEmpty()) return

        val vertices = PathEngine.buildStaticVertices(
            path = currentPath,
            altitudeMode = pathAltitudeMode,
            baseAltitude = baseAltitude,
            pathAltitudeOffset = pathAltitudeOffset
        )
        val staticOptions = PolylineOptions().apply {
            id = PathEngine.STATIC_POLYLINE_ID
            path = vertices
            strokeColor = "#4285F4".toColorInt() // Wide blue route (16dp)
            strokeWidth = 16.0
            zIndex = 1
            altitudeMode = pathAltitudeMode
            drawsOccludedSegments = this@PathFollowingActivity.drawsOccludedSegments
        }
        staticRoutePolyline = map.addPolyline(staticOptions)
    }

    private fun updateProgressPolyline(dist: Double, currentLatLng: LatLng, index: Int) {
        val map = googleMap3D ?: return
        if (currentPath.isEmpty() || totalDistance <= 0.0) return

        val vertices = PathEngine.buildProgressVertices(
            path = currentPath,
            cumulativeDistances = cumulativeDistances,
            elapsedDistance = dist,
            currentLatLng = currentLatLng,
            waypointIndex = index,
            altitudeMode = pathAltitudeMode,
            baseAltitude = baseAltitude,
            pathAltitudeOffset = pathAltitudeOffset
        )

        // In-place ID upsert updates the existing line in the 3D renderer without recreating objects
        val progressOptions = PolylineOptions().apply {
            id = PathEngine.PROGRESS_POLYLINE_ID
            path = vertices
            strokeColor = "#9C27B0".toColorInt() // Narrow purple progress (8dp)
            strokeWidth = 8.0
            zIndex = 2
            altitudeMode = pathAltitudeMode
            drawsOccludedSegments = this@PathFollowingActivity.drawsOccludedSegments
        }
        progressPolyline = map.addPolyline(progressOptions)
    }

    private fun updateCameraPositionForDistance(dist: Double) {
        val map = googleMap3D ?: return
        if (currentPath.isEmpty()) return

        val point = PathEngine.interpolatePoint(
            path = currentPath,
            cumulativeDistances = cumulativeDistances,
            distance = dist
        )
        val targetHeading = PathEngine.smoothHeading(
            targetHeading = point.bearing + headingOffset,
            currentHeading = currentHeading,
            isUserScrubbing = isUserScrubbing,
            isPlaying = isPlaying
        )
        currentHeading = targetHeading

        val cameraTargetAltitude = PathEngine.calculateCameraAltitude(
            altitudeMode = pathAltitudeMode,
            baseAltitude = baseAltitude,
            interpolatedAltitude = point.altitude,
            groundAltitude = groundAltitude
        )

        val newCamera = camera {
            center = latLngAltitude {
                latitude = point.latLng.latitude
                longitude = point.latLng.longitude
                altitude = cameraTargetAltitude
            }
            heading = targetHeading
            tilt = cameraTilt
            range = cameraRange
        }

        map.setCamera(newCamera)
        updateProgressPolyline(dist, point.latLng, point.waypointIndex)
    }

    private fun startAnimation() {
        if (isPlaying) return
        isPlaying = true
        btnPlayPause.setIconResource(R.drawable.pause_24px)

        var lastTimeNanos = 0L
        frameCallback = object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                if (!isPlaying) return

                if (lastTimeNanos == 0L) {
                    lastTimeNanos = frameTimeNanos
                    Choreographer.getInstance().postFrameCallback(this)
                    return
                }

                val dt = (frameTimeNanos - lastTimeNanos) / 1_000_000_000.0
                lastTimeNanos = frameTimeNanos

                elapsedDistance += followSpeedMps * dt
                if (elapsedDistance >= totalDistance) {
                    elapsedDistance = 0.0
                }

                if (!isUserScrubbing && totalDistance > 0) {
                    val progressRatio = (elapsedDistance / totalDistance).toFloat().coerceIn(0f, 1f)
                    progressSlider.value = progressRatio
                }

                updateCameraPositionForDistance(elapsedDistance)
                Choreographer.getInstance().postFrameCallback(this)
            }
        }
        Choreographer.getInstance().postFrameCallback(frameCallback!!)
    }

    private fun pauseAnimation() {
        isPlaying = false
        btnPlayPause.setIconResource(R.drawable.play_arrow_24px)
        frameCallback?.let {
            Choreographer.getInstance().removeFrameCallback(it)
            frameCallback = null
        }
    }

    private fun redrawPolylinesAndCamera() {
        clearPolylines()
        drawPathPolylines()
        updateCameraPositionForDistance(elapsedDistance)
    }

    private fun clearPolylines() {
        staticRoutePolyline?.remove()
        staticRoutePolyline = null
        progressPolyline?.remove()
        progressPolyline = null
    }

    private fun toggleControlsCard() {
        if (isCollapsed) expandControls() else collapseControls()
    }

    private fun collapseControls() {
        val card = controlsCard ?: return
        isCollapsed = true
        fadeHandler.removeCallbacks(fadeOutRunnable)
        btnCollapse?.setIconResource(R.drawable.expand_less_24px)
        btnCollapse?.contentDescription = getString(R.string.expand_controls)

        val headerHeight = if (cardHeader != null && cardHeader!!.height > 0) {
            cardHeader!!.height
        } else {
            (48 * resources.displayMetrics.density).toInt()
        }
        val targetTranslationY = (card.height - headerHeight).coerceAtLeast(0).toFloat()
        card.animate().translationY(targetTranslationY).alpha(0.9f).setDuration(300).start()
    }

    private fun expandControls() {
        val card = controlsCard ?: return
        isCollapsed = false
        btnCollapse?.setIconResource(R.drawable.expand_more_24px)
        btnCollapse?.contentDescription = getString(R.string.collapse_controls)
        card.animate().translationY(0f).alpha(1.0f).setDuration(250).start()
        scheduleControlsFade()
    }

    private fun scheduleControlsFade() {
        fadeHandler.removeCallbacks(fadeOutRunnable)
        fadeHandler.postDelayed(fadeOutRunnable, 3000L)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN || ev.action == MotionEvent.ACTION_MOVE) {
            if (controlsCard != null && !isCollapsed) {
                controlsCard?.animate()?.alpha(1.0f)?.setDuration(150)?.start()
                scheduleControlsFade()
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onResume() {
        super.onResume()
        map3DView.onResume()
    }

    override fun onPause() {
        super.onPause()
        map3DView.onPause()
        pauseAnimation()
    }

    override fun onDestroy() {
        super.onDestroy()
        pauseAnimation()
        fadeHandler.removeCallbacks(fadeOutRunnable)
        clearPolylines()
        map3DView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        map3DView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        map3DView.onSaveInstanceState(outState)
    }
}
