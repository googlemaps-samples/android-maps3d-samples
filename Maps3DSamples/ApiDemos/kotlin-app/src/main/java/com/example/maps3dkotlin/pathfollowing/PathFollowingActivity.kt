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

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Choreographer
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.graphics.toColorInt
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.maps3d.common.PathData
import com.example.maps3d.common.PathEngine
import com.example.maps3d.common.PathFollowingViewModel
import com.example.maps3d.common.PathPlaybackState
import com.example.maps3d.common.PathTouchHandler
import com.example.maps3d.common.RouteProfile
import com.example.maps3dcommon.R
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.Map3DView
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.android.gms.maps3d.model.Polyline
import com.google.android.gms.maps3d.model.PolylineOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.Slider
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Demonstrates 3D Path Following with decoupled gesture controls and dynamic progress polyline rendering.
 *
 * - Progress polyline is driven strictly by time and distance along the route.
 * - Touch gestures (tilt, rotation, zoom) only adjust camera pose without touching polyline pipelines.
 * - Help button with instructions dialog.
 */
class PathFollowingActivity : AppCompatActivity(), OnMap3DViewReadyCallback {

    private val TAG = "PathFollowingActivity"
    private val viewModel: PathFollowingViewModel by viewModels()

    // 3D Map View & Gesture Overlay
    private lateinit var map3DView: Map3DView
    private lateinit var gestureOverlay: View
    private var googleMap3D: GoogleMap3D? = null

    // Polylines
    private var staticRoutePolyline: Polyline? = null
    private var progressPolyline: Polyline? = null
    private var lastStaticVertices: List<LatLngAltitude>? = null
    private var lastStaticAltitudeMode: Int? = null
    private var lastStaticDrawsOccluded: Boolean? = null
    private var lastStaticAltitudeOffset: Double? = null
    private var lastRenderedProgressDist = -1.0
    private var lastProgressAltitudeMode: Int? = null
    private var lastProgressDrawsOccluded: Boolean? = null
    private var isMapInitialized = false
    private var lastSliderUpdateMillis = 0L
    private var lastIsPlaying: Boolean? = null
    private var lastRoute: List<LatLngAltitude>? = null

    // Control panel overlay bindings
    private var controlsCard: CardView? = null
    private var cardHeader: View? = null
    private var btnCollapse: MaterialButton? = null
    private var btnHelp: MaterialButton? = null
    private var btnAltitudeModeInfo: MaterialButton? = null
    private var controlsScroll: View? = null
    private var isCollapsed = false
    private var chipGroupSpeed: ChipGroup? = null

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

    // Auto-fade & VSYNC Choreographer
    private var frameCallback: Choreographer.FrameCallback? = null
    private val fadeHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_path_following)

        bindViews()
        setupCustomGestureHandling()
        setupControlListeners()
        setupTouchAutoFade()
        observeViewModelState()

        map3DView.onCreate(savedInstanceState)
        map3DView.getMap3DViewAsync(this)
    }

    override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
        this.googleMap3D = googleMap3D

        googleMap3D.setOnMapReadyListener {
            googleMap3D.setOnMapReadyListener(null)
            runOnUiThread {
                if (isMapInitialized) return@runOnUiThread
                isMapInitialized = true
                resetPolylines()
            }
        }
    }

    private fun setupCustomGestureHandling() {
        gestureOverlay.setOnTouchListener(PathTouchHandler(this, viewModel))
    }

    private fun bindViews() {
        map3DView = findViewById(R.id.map3dView)
        gestureOverlay = findViewById(R.id.gesture_overlay)
        controlsCard = findViewById(R.id.controls_card)
        cardHeader = findViewById(R.id.card_header)
        btnHelp = findViewById(R.id.btn_help)
        btnAltitudeModeInfo = findViewById(R.id.btn_altitude_mode_info)
        chipGroupSpeed = findViewById(R.id.chip_group_speed)
        btnCollapse = findViewById(R.id.btn_collapse)
        controlsScroll = findViewById(R.id.controls_scroll)
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
    }

    @SuppressLint("StringFormatInvalid", "ClickableViewAccessibility")
    private fun setupControlListeners() {
        btnHelp?.setOnClickListener {
            showHelpDialog()
        }

        btnAltitudeModeInfo?.setOnClickListener {
            showAltitudeModeInfoDialog()
        }

        btnPlayPause.setOnClickListener {
            viewModel.togglePlayPause()
        }

        chipGroupSpeed?.setOnCheckedStateChangeListener { _, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            val multiplier = when (checkedId) {
                R.id.chip_speed_05x -> 0.5
                R.id.chip_speed_1x -> 1.0
                R.id.chip_speed_2x -> 2.0
                R.id.chip_speed_3x -> 3.0
                R.id.chip_speed_5x -> 5.0
                else -> 1.0
            }
            val baseSpeed = viewModel.currentState.routeProfile.recommendedSpeed.toDouble()
            val targetSpeed = (baseSpeed * multiplier).toFloat().coerceIn(speedSlider.valueFrom, speedSlider.valueTo)
            viewModel.setFollowSpeed(targetSpeed.toDouble())
            speedSlider.value = targetSpeed
        }

        fun setPanelCollapsed(collapsed: Boolean) {
            if (isCollapsed == collapsed) return
            isCollapsed = collapsed
            controlsScroll?.visibility = if (isCollapsed) View.GONE else View.VISIBLE
            btnCollapse?.setIconResource(
                if (isCollapsed) R.drawable.expand_less_24px else R.drawable.expand_more_24px
            )
        }

        findViewById<View>(R.id.layout_drag_handle)?.setOnClickListener {
            setPanelCollapsed(!isCollapsed)
        }
        findViewById<View>(R.id.drag_handle)?.setOnClickListener {
            setPanelCollapsed(!isCollapsed)
        }

        btnCollapse?.setOnClickListener {
            setPanelCollapsed(!isCollapsed)
        }

        cardHeader?.setOnClickListener {
            setPanelCollapsed(!isCollapsed)
        }

        // Swipe Up to Expand / Swipe Down to Collapse
        val cardSwipeDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null) return false
                val dy = e2.y - e1.y
                if (dy > 50 && velocityY > 100) {
                    // Swiped Down -> Collapse
                    setPanelCollapsed(true)
                    return true
                } else if (dy < -50 && velocityY < -100) {
                    // Swiped Up -> Expand
                    setPanelCollapsed(false)
                    return true
                }
                return false
            }
        })

        cardHeader?.setOnTouchListener { v, event ->
            if (cardSwipeDetector.onTouchEvent(event)) {
                true
            } else {
                v.onTouchEvent(event)
            }
        }

        findViewById<View>(R.id.layout_drag_handle)?.setOnTouchListener { v, event ->
            if (cardSwipeDetector.onTouchEvent(event)) {
                true
            } else {
                v.onTouchEvent(event)
            }
        }

        progressSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                viewModel.seekToRatio(value)
                val state = viewModel.currentState
                updateCameraFromState(state)
                updateProgressPolyline(state, force = true)
            }
        }

        progressSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                viewModel.setScrubbing(true)
            }

            override fun onStopTrackingTouch(slider: Slider) {
                viewModel.setScrubbing(false)
                viewModel.seekToRatio(slider.value)
                val state = viewModel.currentState
                updateCameraFromState(state)
                updateProgressPolyline(state, force = true)
            }
        })

        rgAltitudeMode.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                R.id.rb_clamp_to_ground -> AltitudeMode.CLAMP_TO_GROUND
                R.id.rb_relative_to_ground -> AltitudeMode.RELATIVE_TO_GROUND
                R.id.rb_relative_to_mesh -> AltitudeMode.RELATIVE_TO_MESH
                R.id.rb_absolute -> AltitudeMode.ABSOLUTE
                else -> AltitudeMode.CLAMP_TO_GROUND
            }
            viewModel.setAltitudeMode(mode)
            resetPolylines()
        }

        switchDrawsOccludedSegments.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setDrawsOccludedSegments(isChecked)
            resetPolylines()
        }

        pathAltitudeSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                viewModel.setPathAltitudeOffset(value.toDouble())
                resetPolylines()
            }
            pathAltitudeSliderLabel.text = getString(R.string.path_height_format, value)
        }

        rangeSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                viewModel.setCameraRange(value.toDouble())
                updateCameraFromState(viewModel.currentState)
            }
            rangeSliderLabel.text = getString(R.string.camera_range_format, value.toInt())
        }

        altitudeSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                viewModel.setGroundAltitude(value.toDouble())
                updateCameraFromState(viewModel.currentState)
            }
            altitudeSliderLabel.text = getString(R.string.ground_altitude_format, value.toInt())
        }

        headingSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                viewModel.setHeadingOffset(value.toDouble())
                updateCameraFromState(viewModel.currentState)
            }
            headingSliderLabel.text = getString(R.string.heading_offset_format, value.toInt())
        }

        tiltSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                viewModel.setCameraTilt(value.toDouble())
                updateCameraFromState(viewModel.currentState)
            }
            tiltSliderLabel.text = getString(R.string.camera_tilt_format, value.toInt())
        }

        speedSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                viewModel.setFollowSpeed(value.toDouble())
                val baseSpeed = viewModel.currentState.routeProfile.recommendedSpeed
                if (baseSpeed > 0.0f) {
                    val mult = value / baseSpeed
                    when {
                        abs(mult - 0.5f) < 0.15f -> chipGroupSpeed?.check(R.id.chip_speed_05x)
                        abs(mult - 1.0f) < 0.15f -> chipGroupSpeed?.check(R.id.chip_speed_1x)
                        abs(mult - 2.0f) < 0.15f -> chipGroupSpeed?.check(R.id.chip_speed_2x)
                        abs(mult - 3.0f) < 0.15f -> chipGroupSpeed?.check(R.id.chip_speed_3x)
                        abs(mult - 5.0f) < 0.15f -> chipGroupSpeed?.check(R.id.chip_speed_5x)
                    }
                }
            }
            val boostSuffix = when {
                viewModel.currentState.speedBoostMultiplier >= 4.5 -> " (5x Fast-Forward)"
                viewModel.currentState.speedBoostMultiplier <= -4.5 -> " (-5x Rewind)"
                viewModel.currentState.speedBoostMultiplier >= 1.5 -> " (2x Boost)"
                else -> ""
            }
            speedSliderLabel.text = getString(
                R.string.follow_speed_with_suffix_format,
                value.toInt(),
                boostSuffix,
            )
        }

        rgEnvironment.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_urban -> {
                    viewModel.setRoute(PathData.URBAN_PATH, applyDefaults = true)
                    configureSlider(pathAltitudeSlider, 0.0f, 20.0f, pathAltitudeSlider.value)
                }
                R.id.rb_rural -> {
                    viewModel.setRoute(PathData.RURAL_PATH, applyDefaults = true)
                    configureSlider(pathAltitudeSlider, 0.0f, 200.0f, pathAltitudeSlider.value)
                }
                R.id.rb_mountain -> {
                    viewModel.setRoute(PathData.MOUNTAIN_PATH, applyDefaults = true)
                    configureSlider(pathAltitudeSlider, 0.0f, 200.0f, pathAltitudeSlider.value)
                }
            }
            resetPolylines()
        }
    }

    private fun configureSlider(slider: Slider, min: Float, max: Float, targetVal: Float) {
        try {
            val safeMin = minOf(min, max - 1f)
            val safeMax = maxOf(max, safeMin + 1f)
            val clampedTarget = targetVal.coerceIn(safeMin, safeMax)
            // 4-step bound adjustment ensures valueFrom <= value <= valueTo invariant at every step
            slider.valueFrom = minOf(slider.valueFrom, safeMin)
            slider.valueTo = maxOf(slider.valueTo, safeMax)
            slider.value = clampedTarget
            slider.valueFrom = safeMin
            slider.valueTo = safeMax
        } catch (e: Exception) {
            Log.e(TAG, "Error configuring slider: ${e.message}", e)
        }
    }

    private fun applyRouteProfile(profile: RouteProfile) {
        configureSlider(rangeSlider, profile.rangeSliderMin, profile.rangeSliderMax, profile.recommendedRange)
        rangeSliderLabel.text = getString(R.string.camera_range_format, profile.recommendedRange.toInt())

        configureSlider(
            altitudeSlider,
            profile.altitudeSliderMin,
            profile.altitudeSliderMax,
            profile.baseAltitude.toFloat()
        )
        altitudeSliderLabel.text = getString(R.string.ground_altitude_format, profile.baseAltitude.toInt())

        configureSlider(speedSlider, profile.speedSliderMin, profile.speedSliderMax, profile.recommendedSpeed)
        speedSliderLabel.text = getString(R.string.follow_speed_format, profile.recommendedSpeed.toInt())
        chipGroupSpeed?.check(R.id.chip_speed_1x)

        val clampedTilt = profile.recommendedTilt.coerceIn(tiltSlider.valueFrom, tiltSlider.valueTo)
        tiltSlider.value = clampedTilt
        tiltSliderLabel.text = getString(R.string.camera_tilt_format, clampedTilt.toInt())
    }

    private fun showHelpDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.help_dialog_title)
            .setMessage(R.string.help_dialog_message)
            .setPositiveButton(R.string.help_dialog_ok, null)
            .show()
    }

    private fun showAltitudeModeInfoDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.altitude_mode_info_title)
            .setMessage(R.string.altitude_mode_info_message)
            .setPositiveButton(R.string.help_dialog_ok, null)
            .show()
    }

    private fun resetPolylines() {
        lastStaticVertices = null
        lastStaticAltitudeMode = null
        lastStaticDrawsOccluded = null
        lastStaticAltitudeOffset = null
        lastProgressAltitudeMode = null
        lastProgressDrawsOccluded = null
        lastRenderedProgressDist = -1.0
        staticRoutePolyline?.remove()
        progressPolyline?.remove()
        staticRoutePolyline = null
        progressPolyline = null
        val state = viewModel.currentState
        updateStaticPolyline(state)
        updateProgressPolyline(state, force = true)
        updateCameraFromState(state)
        renderUiControls(state)
    }

    private fun updateStaticPolyline(state: PathPlaybackState) {
        val map = googleMap3D ?: return
        if (state.staticPolylineVertices.size < 2) return

        if (lastStaticVertices == state.staticPolylineVertices &&
            lastStaticAltitudeMode == state.altitudeMode &&
            lastStaticDrawsOccluded == state.drawsOccludedSegments &&
            lastStaticAltitudeOffset == state.pathAltitudeOffset &&
            staticRoutePolyline != null
        ) {
            return
        }

        lastStaticVertices = state.staticPolylineVertices
        lastStaticAltitudeMode = state.altitudeMode
        lastStaticDrawsOccluded = state.drawsOccludedSegments
        lastStaticAltitudeOffset = state.pathAltitudeOffset

        val staticOptions = PolylineOptions().apply {
            id = PathEngine.STATIC_POLYLINE_ID
            path = state.staticPolylineVertices
            strokeColor = "#4285F4".toColorInt()
            strokeWidth = 10.0
            zIndex = 1
            altitudeMode = state.altitudeMode
            drawsOccludedSegments = state.drawsOccludedSegments
        }
        staticRoutePolyline = map.addPolyline(staticOptions)
    }

    private fun updateProgressPolyline(state: PathPlaybackState, force: Boolean = false) {
        val map = googleMap3D ?: return
        if (state.progressPolylineVertices.size < 2) return

        val configChanged = progressPolyline == null ||
            lastProgressAltitudeMode != state.altitudeMode ||
            lastProgressDrawsOccluded != state.drawsOccludedSegments

        val distDelta = abs(state.elapsedDistance - lastRenderedProgressDist)
        if (!force && !configChanged && distDelta <= 0.0) {
            return
        }

        lastRenderedProgressDist = state.elapsedDistance
        lastProgressAltitudeMode = state.altitudeMode
        lastProgressDrawsOccluded = state.drawsOccludedSegments

        val progressOptions = PolylineOptions().apply {
            id = PathEngine.PROGRESS_POLYLINE_ID
            path = state.progressPolylineVertices
            strokeColor = "#9C27B0".toColorInt()
            strokeWidth = 8.0
            zIndex = 2
            altitudeMode = state.altitudeMode
            drawsOccludedSegments = state.drawsOccludedSegments
        }
        progressPolyline = map.addPolyline(progressOptions)
    }

    private fun observeViewModelState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    try {
                        updateCameraFromState(state)
                        updateStaticPolyline(state)
                        updateProgressPolyline(state)
                        renderUiControls(state)
                        manageAnimationTicker(state.isPlaying)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in UI state collection: ${e.message}", e)
                    }
                }
            }
        }
    }

    private fun updateCameraFromState(state: PathPlaybackState) {
        val map = googleMap3D ?: return
        val newCamera = Camera(
            /* center = */ LatLngAltitude(
                state.currentPosition.latitude,
                state.currentPosition.longitude,
                state.cameraTargetAltitude
            ),
            /* heading = */ state.effectiveHeading,
            /* tilt = */ state.cameraTilt,
            /* roll = */ 0.0,
            /* range = */ state.cameraRange
        )
        map.setCamera(newCamera)
    }

    private fun renderUiControls(state: PathPlaybackState) {
        if (lastRoute != state.route) {
            lastRoute = state.route
            applyRouteProfile(state.routeProfile)
        }

        if (lastIsPlaying != state.isPlaying) {
            lastIsPlaying = state.isPlaying
            btnPlayPause.setIconResource(
                if (state.isPlaying) R.drawable.pause_24px else R.drawable.play_arrow_24px
            )
        }

        if (!state.isScrubbing && !progressSlider.isPressed) {
            val now = System.currentTimeMillis()
            if (now - lastSliderUpdateMillis >= 100L || !state.isPlaying) {
                lastSliderUpdateMillis = now
                progressSlider.value = state.progressRatio
            }
        }

        val boostSuffix = when {
            state.speedBoostMultiplier >= 4.5 -> " (5x Warp Speed)"
            state.speedBoostMultiplier >= 1.5 -> " (2x Boost)"
            else -> ""
        }
        speedSliderLabel.text = getString(
            R.string.follow_speed_with_suffix_format,
            state.followSpeedMps.toInt(),
            boostSuffix,
        )

        if (!isCollapsed) {
            if (!rangeSlider.isPressed) {
                val clampedRange = state.cameraRange.toFloat().coerceIn(rangeSlider.valueFrom, rangeSlider.valueTo)
                if (abs(rangeSlider.value - clampedRange) >= 1.0f) {
                    rangeSlider.value = clampedRange
                    rangeSliderLabel.text = getString(R.string.camera_range_format, state.cameraRange.toInt())
                }
            }

            if (!tiltSlider.isPressed) {
                val clampedTilt = state.cameraTilt.toFloat().coerceIn(tiltSlider.valueFrom, tiltSlider.valueTo)
                if (abs(tiltSlider.value - clampedTilt) >= 0.5f) {
                    tiltSlider.value = clampedTilt
                    tiltSliderLabel.text = getString(R.string.camera_tilt_format, state.cameraTilt.toInt())
                }
            }

            if (!headingSlider.isPressed) {
                val clampedHeading = state.headingOffset.toFloat().coerceIn(headingSlider.valueFrom, headingSlider.valueTo)
                if (abs(headingSlider.value - clampedHeading) >= 0.5f) {
                    headingSlider.value = clampedHeading
                    headingSliderLabel.text = getString(R.string.heading_offset_format, state.headingOffset.toInt())
                }
            }

            if (!altitudeSlider.isPressed) {
                val clampedAltitude = state.groundAltitude.toFloat().coerceIn(altitudeSlider.valueFrom, altitudeSlider.valueTo)
                if (abs(altitudeSlider.value - clampedAltitude) >= 0.5f) {
                    altitudeSlider.value = clampedAltitude
                    altitudeSliderLabel.text = getString(R.string.ground_altitude_format, state.groundAltitude.toInt())
                }
            }

            if (!speedSlider.isPressed) {
                val clampedSpeed = state.followSpeedMps.toFloat().coerceIn(speedSlider.valueFrom, speedSlider.valueTo)
                if (abs(speedSlider.value - clampedSpeed) >= 0.5f) {
                    speedSlider.value = clampedSpeed
                }
            }
        }
    }

    private fun manageAnimationTicker(isPlaying: Boolean) {
        if (isPlaying) {
            if (frameCallback == null) {
                var lastTimeNanos = 0L
                frameCallback = object : Choreographer.FrameCallback {
                    override fun doFrame(frameTimeNanos: Long) {
                        if (!viewModel.currentState.isPlaying) return

                        if (lastTimeNanos == 0L) {
                            lastTimeNanos = frameTimeNanos
                            Choreographer.getInstance().postFrameCallback(this)
                            return
                        }

                        val dt = (frameTimeNanos - lastTimeNanos) / 1_000_000_000.0
                        lastTimeNanos = frameTimeNanos

                        viewModel.advance(dt)
                        Choreographer.getInstance().postFrameCallback(this)
                    }
                }
                Choreographer.getInstance().postFrameCallback(frameCallback!!)
            }
        } else {
            frameCallback?.let {
                Choreographer.getInstance().removeFrameCallback(it)
                frameCallback = null
            }
        }
    }

    private fun setupTouchAutoFade() {
        scheduleControlFade()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN || ev.action == MotionEvent.ACTION_MOVE) {
            fadeHandler.removeCallbacksAndMessages(null)
            controlsCard?.animate()?.alpha(1.0f)?.setDuration(150L)?.start()
        } else if (ev.action == MotionEvent.ACTION_UP || ev.action == MotionEvent.ACTION_CANCEL) {
            scheduleControlFade()
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun scheduleControlFade() {
        fadeHandler.removeCallbacksAndMessages(null)
        fadeHandler.postDelayed({
            controlsCard?.animate()?.alpha(0.35f)?.setDuration(500L)?.start()
        }, 3500L)
    }

    override fun onResume() {
        super.onResume()
        map3DView.onResume()
    }

    override fun onPause() {
        super.onPause()
        viewModel.setPlaying(false)
        viewModel.setSpeedBoosted(false)
        map3DView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.setPlaying(false)
        frameCallback?.let {
            Choreographer.getInstance().removeFrameCallback(it)
            frameCallback = null
        }
        fadeHandler.removeCallbacksAndMessages(null)
        staticRoutePolyline?.remove()
        progressPolyline?.remove()
        staticRoutePolyline = null
        progressPolyline = null
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
