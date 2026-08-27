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
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.Slider
import kotlinx.coroutines.launch

/**
 * Demonstrates 3D Path Following using an MVI / MVVM architecture with [PathFollowingViewModel].
 *
 * The Activity acts as a pure presentation layer: it observes [PathPlaybackState] from the ViewModel
 * and renders camera positions and dual polylines into [GoogleMap3D].
 */
class PathFollowingActivity : AppCompatActivity(), OnMap3DViewReadyCallback {

    private val viewModel: PathFollowingViewModel by viewModels()

    // 3D Map View
    private lateinit var map3DView: Map3DView
    private var googleMap3D: GoogleMap3D? = null

    // Polylines
    private var staticRoutePolyline: Polyline? = null
    private var progressPolyline: Polyline? = null

    // Control panel overlay bindings
    private var controlsCard: CardView? = null
    private var btnCollapse: MaterialButton? = null
    private var controlsScroll: View? = null
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

    // Auto-fade & VSYNC Choreographer
    private var frameCallback: Choreographer.FrameCallback? = null
    private val fadeHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_path_following)

        bindViews()
        setupControlListeners()
        setupTouchAutoFade()
        observeViewModelState()

        map3DView.onCreate(savedInstanceState)
        map3DView.getMap3DViewAsync(this)
    }

    override fun onMap3DViewReady(map: GoogleMap3D) {
        googleMap3D = map
        render(viewModel.currentState)
    }

    private fun bindViews() {
        map3DView = findViewById(R.id.map3dView)
        controlsCard = findViewById(R.id.controls_card)
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

    private fun setupControlListeners() {
        btnPlayPause.setOnClickListener {
            viewModel.togglePlayPause()
        }

        btnCollapse?.setOnClickListener {
            isCollapsed = !isCollapsed
            controlsScroll?.visibility = if (isCollapsed) View.GONE else View.VISIBLE
            btnCollapse?.setIconResource(
                if (isCollapsed) R.drawable.expand_less_24px else R.drawable.expand_more_24px
            )
        }

        progressSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                viewModel.seekToRatio(value)
            }
        }

        progressSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                viewModel.setScrubbing(true)
            }

            override fun onStopTrackingTouch(slider: Slider) {
                viewModel.setScrubbing(false)
                viewModel.seekToRatio(slider.value)
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
        }

        switchDrawsOccludedSegments.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setDrawsOccludedSegments(isChecked)
        }

        pathAltitudeSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) viewModel.setPathAltitudeOffset(value.toDouble())
            pathAltitudeSliderLabel.text = getString(R.string.path_height_format, value)
        }

        rangeSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) viewModel.setCameraRange(value.toDouble())
            rangeSliderLabel.text = getString(R.string.camera_range_format, value.toInt())
        }

        altitudeSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) viewModel.setGroundAltitude(value.toDouble())
            altitudeSliderLabel.text = getString(R.string.ground_altitude_format, value.toInt())
        }

        headingSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) viewModel.setHeadingOffset(value.toDouble())
            headingSliderLabel.text = getString(R.string.heading_offset_format, value.toInt())
        }

        tiltSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) viewModel.setCameraTilt(value.toDouble())
            tiltSliderLabel.text = getString(R.string.camera_tilt_format, value.toInt())
        }

        speedSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) viewModel.setFollowSpeed(value.toDouble())
            speedSliderLabel.text = getString(R.string.follow_speed_format, value.toInt())
        }

        rgEnvironment.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_urban -> {
                    viewModel.setRoute(PathData.URBAN_PATH)
                    pathAltitudeSlider.valueTo = 20.0f
                    altitudeSlider.valueTo = 100.0f
                }
                R.id.rb_rural -> {
                    viewModel.setRoute(PathData.RURAL_PATH)
                    pathAltitudeSlider.valueTo = 200.0f
                    altitudeSlider.valueTo = 200.0f
                }
            }
        }
    }

    private fun observeViewModelState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    render(state)
                    manageAnimationTicker(state.isPlaying)
                }
            }
        }
    }

    private fun render(state: PathPlaybackState) {
        val map = googleMap3D ?: return

        // Update Camera Position & Orientation
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

        // Update Static Base Route Polyline
        val staticOptions = PolylineOptions().apply {
            id = PathEngine.STATIC_POLYLINE_ID
            path = state.staticPolylineVertices
            strokeColor = "#4285F4".toColorInt()
            strokeWidth = 16.0
            zIndex = 1
            altitudeMode = state.altitudeMode
            drawsOccludedSegments = state.drawsOccludedSegments
        }
        staticRoutePolyline = map.addPolyline(staticOptions)

        // Update Progress Polyline
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

        // Update Play/Pause Button Icon
        btnPlayPause.setIconResource(
            if (state.isPlaying) R.drawable.pause_24px else R.drawable.play_arrow_24px
        )

        // Update Progress Slider without fighting touch scrubbing
        if (!state.isScrubbing) {
            progressSlider.value = state.progressRatio
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
        controlsCard?.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    fadeHandler.removeCallbacksAndMessages(null)
                    controlsCard?.alpha = 1.0f
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    scheduleControlFade()
                }
            }
            false
        }
        scheduleControlFade()
    }

    private fun scheduleControlFade() {
        fadeHandler.removeCallbacksAndMessages(null)
        fadeHandler.postDelayed({
            if (!isCollapsed) {
                controlsCard?.animate()?.alpha(0.80f)?.setDuration(400L)?.start()
            }
        }, 5000L)
    }

    override fun onResume() {
        super.onResume()
        map3DView.onResume()
    }

    override fun onPause() {
        super.onPause()
        viewModel.setPlaying(false)
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
