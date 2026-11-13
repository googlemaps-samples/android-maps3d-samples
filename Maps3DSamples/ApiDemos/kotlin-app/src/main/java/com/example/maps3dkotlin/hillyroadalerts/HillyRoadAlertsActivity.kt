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

package com.example.maps3dkotlin.hillyroadalerts

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Choreographer
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.maps3d.common.AlertSeverity
import com.example.maps3d.common.ElevationSparklineView
import com.example.maps3d.common.ExtremaType
import com.example.maps3d.common.HillyRoadAlertViewModel
import com.example.maps3d.common.HillyRoadUiState
import com.example.maps3d.common.HillyRoutePreset
import com.example.maps3d.common.MaxHeightScrollView
import com.example.maps3d.common.TerrainAlertType
import com.example.maps3d.common.TerrainExtremaPoint
import com.example.maps3dcommon.R
import com.example.maps3dkotlin.sampleactivity.SampleBaseActivity
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.CollisionBehavior
import com.google.android.gms.maps3d.model.Glyph
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.android.gms.maps3d.model.Marker
import com.google.android.gms.maps3d.model.MarkerOptions
import com.google.android.gms.maps3d.model.Model
import com.google.android.gms.maps3d.model.ModelOptions
import com.google.android.gms.maps3d.model.Orientation
import com.google.android.gms.maps3d.model.PinConfiguration
import com.google.android.gms.maps3d.model.Polyline
import com.google.android.gms.maps3d.model.PolylineOptions
import com.google.android.gms.maps3d.model.Vector3D
import com.google.android.gms.maps3d.model.camera
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.Slider
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

/**
 * Kotlin Views showcase sample for **"Hilly & Undulating (Roller-Coaster) Road Early Alerts"**.
 *
 * Demonstrates real-time forward terrain analysis along 3D routes, early warning HUD alerts,
 * live lookahead elevation sparklines, dynamic hazard polylines, and diffed 3D crest/dip pins.
 */
class HillyRoadAlertsActivity :
    SampleBaseActivity(),
    Choreographer.FrameCallback {

    override val TAG: String = HillyRoadAlertsActivity::class.java.simpleName

    override val layoutResId: Int = R.layout.activity_hilly_road_alerts

    override val initialCamera: Camera = camera {
        center = LatLngAltitude(36.485940, -114.532890, 614.4)
        heading = 0.0
        tilt = HillyRoadAlertViewModel.NORMAL_CAMERA_TILT
        range = HillyRoadAlertViewModel.NORMAL_CAMERA_RANGE
        roll = 0.0
    }

    private val viewModel: HillyRoadAlertViewModel by viewModels()

    // HUD Views
    private lateinit var hudAlertCard: MaterialCardView
    private lateinit var tvAlertTitle: TextView
    private lateinit var tvAlertDistancePill: TextView
    private lateinit var tvChipExtrema: TextView
    private lateinit var tvChipMaxGrade: TextView
    private lateinit var tvSparklineLabel: TextView
    private lateinit var sparklineView: ElevationSparklineView

    // Bottom Sheet Controls
    private lateinit var controlsScroll: MaxHeightScrollView
    private lateinit var btnCollapse: MaterialButton
    private lateinit var tvCurrentTelemetry: TextView
    private lateinit var chipJumpToHills: Chip
    private lateinit var btnPlayPause: MaterialButton
    private lateinit var progressSlider: Slider
    private lateinit var chipGroupRoute: ChipGroup
    private lateinit var chipGroupHorizon: ChipGroup
    private lateinit var chipGroupSpeed: ChipGroup
    private lateinit var switchCameraPreview: MaterialSwitch

    // 3D Map Overlays & Diffing State
    private var baseRoutePolyline: Polyline? = null
    private var traveledRoutePolyline: Polyline? = null
    private var hazardRoutePolyline: Polyline? = null
    private var vehicleModel: Model? = null
    private val activeExtremaMarkers: MutableMap<String, Marker> = mutableMapOf()

    private var renderedPreset: HillyRoutePreset? = null
    private var lastRenderedTraveledDist: Double = -1.0
    private var lastRenderedHazardCoords: List<LatLngAltitude> = emptyList()
    private var renderedHazardSeverity: AlertSeverity? = null
    private var lastRenderedVehiclePos: LatLngAltitude? = null
    private var lastRenderedVehicleHeading: Double = Double.NaN
    private var isControlsExpanded: Boolean = true
    private var isUpdatingUiFromState: Boolean = false
    private var isFrameCallbackPosted: Boolean = false
    private var lastFrameTimeNanos: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Note: SampleBaseActivity already inflated layoutResId and initialized map3DView.
        bindViews()
        configureViewportGuardrails()
        setupControlListeners()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    renderUiState(state)
                }
            }
        }
    }

    override fun onMapReady(googleMap3D: GoogleMap3D) {
        super.onMapReady(googleMap3D)
        startRouteIfMapReady(googleMap3D)
    }

    private fun startRouteIfMapReady(map: GoogleMap3D) {
        if (viewModel.currentState.isMapReady) return
        lastFrameTimeNanos = 0L
        viewModel.onMapReady()
        renderMapScene(map, viewModel.currentState)
        updateFrameTicker(viewModel.currentState)
    }

    override fun onResume() {
        super.onResume()
        lastFrameTimeNanos = 0L
        updateFrameTicker(viewModel.currentState)
    }

    override fun onPause() {
        stopFrameTicker()
        viewModel.setPlaying(false)
        super.onPause()
    }

    override fun onDestroy() {
        stopFrameTicker()
        clearMapOverlays()
        super.onDestroy()
    }

    override fun doFrame(frameTimeNanos: Long) {
        isFrameCallbackPosted = false
        val state = viewModel.currentState
        if (googleMap3D == null || !state.isMapReady || !state.isPlaying || state.isUserScrubbing) {
            lastFrameTimeNanos = 0L
            return
        }
        val prevTimeNanos = lastFrameTimeNanos
        lastFrameTimeNanos = frameTimeNanos
        if (prevTimeNanos != 0L) {
            val dtSeconds = (frameTimeNanos - prevTimeNanos) / 1_000_000_000.0
            viewModel.advance(dtSeconds)
        }
        updateFrameTicker(viewModel.currentState)
    }

    private fun updateFrameTicker(state: HillyRoadUiState) {
        val shouldTick = googleMap3D != null &&
            state.isMapReady &&
            state.isPlaying &&
            !state.isUserScrubbing
        if (shouldTick && !isFrameCallbackPosted) {
            isFrameCallbackPosted = true
            Choreographer.getInstance().postFrameCallback(this)
        } else if (!shouldTick && isFrameCallbackPosted) {
            stopFrameTicker()
        }
    }

    private fun stopFrameTicker() {
        if (isFrameCallbackPosted) {
            Choreographer.getInstance().removeFrameCallback(this)
            isFrameCallbackPosted = false
        }
        lastFrameTimeNanos = 0L
    }

    private fun bindViews() {
        hudAlertCard = findViewById(R.id.hud_alert_card)
        tvAlertTitle = findViewById(R.id.tv_alert_title)
        tvAlertDistancePill = findViewById(R.id.tv_alert_distance_pill)
        tvChipExtrema = findViewById(R.id.tv_chip_extrema)
        tvChipMaxGrade = findViewById(R.id.tv_chip_max_grade)
        tvSparklineLabel = findViewById(R.id.tv_sparkline_label)
        sparklineView = findViewById(R.id.sparkline_view)

        controlsScroll = findViewById(R.id.controls_scroll)
        btnCollapse = findViewById(R.id.btn_collapse)
        tvCurrentTelemetry = findViewById(R.id.tv_current_telemetry)
        chipJumpToHills = findViewById(R.id.chip_jump_to_hills)
        btnPlayPause = findViewById(R.id.btn_play_pause)
        progressSlider = findViewById(R.id.progress_slider)
        chipGroupRoute = findViewById(R.id.chip_group_route)
        chipGroupHorizon = findViewById(R.id.chip_group_horizon)
        chipGroupSpeed = findViewById(R.id.chip_group_speed)
        switchCameraPreview = findViewById(R.id.switch_camera_preview)
    }

    private fun configureViewportGuardrails() {
        val screenHeight = resources.displayMetrics.heightPixels
        val isLandscape =
            resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val maxScrollHeight = (screenHeight * if (isLandscape) 0.28f else 0.36f).toInt()
        controlsScroll.setMaxHeightPx(maxScrollHeight)

        // Collapse expandable controls by default in landscape so 3D viewport is never occluded
        setControlsExpanded(!isLandscape)
    }

    private fun setControlsExpanded(expanded: Boolean) {
        isControlsExpanded = expanded
        controlsScroll.visibility = if (expanded) View.VISIBLE else View.GONE
        btnCollapse.setIconResource(
            if (expanded) R.drawable.expand_more_24px else R.drawable.expand_less_24px
        )
        btnCollapse.contentDescription = getString(
            if (expanded) R.string.collapse_controls else R.string.expand_controls
        )
    }

    private fun setupControlListeners() {
        val toggleCollapseListener = View.OnClickListener {
            setControlsExpanded(!isControlsExpanded)
        }
        findViewById<FrameLayout>(R.id.layout_drag_handle).setOnClickListener(toggleCollapseListener)
        findViewById<LinearLayout>(R.id.card_header).setOnClickListener(toggleCollapseListener)
        btnCollapse.setOnClickListener(toggleCollapseListener)

        btnPlayPause.setOnClickListener {
            viewModel.togglePlayPause()
        }

        chipJumpToHills.setOnClickListener {
            viewModel.jumpToHillyZone()
        }

        progressSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                viewModel.setScrubbing(true)
            }

            override fun onStopTrackingTouch(slider: Slider) {
                viewModel.setScrubbing(false)
            }
        })

        progressSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser && !isUpdatingUiFromState) {
                viewModel.seekToProgress(value)
            }
        }

        chipGroupRoute.setOnCheckedStateChangeListener { _, checkedIds ->
            if (isUpdatingUiFromState || checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            when (checkedIds.first()) {
                R.id.chip_route_roller_coaster -> viewModel.selectRoute(HillyRoutePreset.ROLLER_COASTER)
                R.id.chip_route_mountain_pass -> viewModel.selectRoute(HillyRoutePreset.MOUNTAIN_PASS)
                R.id.chip_route_coastal -> viewModel.selectRoute(HillyRoutePreset.COASTAL_DRIVE)
            }
        }

        chipGroupHorizon.setOnCheckedStateChangeListener { _, checkedIds ->
            if (isUpdatingUiFromState || checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            when (checkedIds.first()) {
                R.id.chip_horizon_250 -> viewModel.setLookaheadHorizon(250.0)
                R.id.chip_horizon_500 -> viewModel.setLookaheadHorizon(500.0)
                R.id.chip_horizon_750 -> viewModel.setLookaheadHorizon(750.0)
            }
        }

        chipGroupSpeed.setOnCheckedStateChangeListener { _, checkedIds ->
            if (isUpdatingUiFromState || checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            when (checkedIds.first()) {
                R.id.chip_speed_05x -> viewModel.setSpeedMultiplier(0.5)
                R.id.chip_speed_1x -> viewModel.setSpeedMultiplier(1.0)
                R.id.chip_speed_2x -> viewModel.setSpeedMultiplier(2.0)
                R.id.chip_speed_3x -> viewModel.setSpeedMultiplier(3.0)
                R.id.chip_speed_5x -> viewModel.setSpeedMultiplier(5.0)
            }
        }

        switchCameraPreview.setOnCheckedChangeListener { _, isChecked ->
            if (!isUpdatingUiFromState) {
                viewModel.setAutoCameraPitchPreview(isChecked)
            }
        }
    }

    private fun renderUiState(state: HillyRoadUiState) {
        isUpdatingUiFromState = true
        try {
            val alert = state.terrainAlert

            // 1. Top Early Alert Banner + Sparkline
            tvAlertTitle.text = getAlertTitleText(alert.alertType)
            tvAlertDistancePill.text = when {
                alert.alertType == TerrainAlertType.NONE ->
                    getString(R.string.hilly_alert_distance_clear)
                alert.distanceToHazardMeters <= 0.5 ->
                    getString(
                        R.string.hilly_alert_distance_active_format,
                        alert.remainingHazardDistanceMeters.roundToInt()
                    )
                else ->
                    getString(
                        R.string.hilly_alert_distance_approaching_format,
                        alert.distanceToHazardMeters.roundToInt()
                    )
            }

            // Canonical alert border colors: Red (#EA4335), Amber (#FBBC04), Emerald (#10B981)
            val strokeColor = when (alert.severity) {
                AlertSeverity.WARNING -> Color.rgb(234, 67, 53)
                AlertSeverity.CAUTION -> Color.rgb(251, 188, 4)
                AlertSeverity.NONE -> Color.rgb(16, 185, 129)
            }
            hudAlertCard.strokeColor = strokeColor
            tvAlertDistancePill.setTextColor(
                when (alert.severity) {
                    AlertSeverity.WARNING -> Color.rgb(254, 202, 202)
                    AlertSeverity.CAUTION -> Color.rgb(254, 240, 138)
                    AlertSeverity.NONE -> Color.rgb(167, 243, 208)
                }
            )

            tvChipExtrema.text = getString(
                R.string.hilly_telemetry_extrema_format,
                alert.crestCount,
                alert.dipCount
            )
            tvChipMaxGrade.text = String.format(
                Locale.US,
                getString(R.string.hilly_telemetry_max_grade_format),
                alert.maxPositiveGradePercent,
                alert.maxNegativeGradePercent
            )

            tvSparklineLabel.text = getString(
                R.string.hilly_sparkline_label_format,
                state.lookaheadHorizonMeters.roundToInt()
            )
            sparklineView.updateData(
                profile = alert.lookaheadProfile,
                extrema = alert.upcomingExtrema,
                severity = alert.severity,
                horizonMeters = state.lookaheadHorizonMeters
            )

            // 2. Bottom Sheet Controls
            tvCurrentTelemetry.text = String.format(
                Locale.US,
                getString(R.string.hilly_current_telemetry_format),
                alert.currentGradePercent,
                state.currentDistanceMeters.roundToInt(),
                state.totalDistanceMeters.roundToInt()
            )

            btnPlayPause.setIconResource(
                if (state.isPlaying) R.drawable.pause_24px else R.drawable.play_arrow_24px
            )
            if (!state.isUserScrubbing) {
                progressSlider.value = state.progressRatio.coerceIn(0.0f, 1.0f)
            }

            val expectedRouteChipId = when (state.selectedPreset) {
                HillyRoutePreset.ROLLER_COASTER -> R.id.chip_route_roller_coaster
                HillyRoutePreset.MOUNTAIN_PASS -> R.id.chip_route_mountain_pass
                HillyRoutePreset.COASTAL_DRIVE -> R.id.chip_route_coastal
            }
            if (chipGroupRoute.checkedChipId != expectedRouteChipId) {
                chipGroupRoute.check(expectedRouteChipId)
            }

            val expectedHorizonChipId = when (state.lookaheadHorizonMeters.roundToInt()) {
                250 -> R.id.chip_horizon_250
                750 -> R.id.chip_horizon_750
                else -> R.id.chip_horizon_500
            }
            if (chipGroupHorizon.checkedChipId != expectedHorizonChipId) {
                chipGroupHorizon.check(expectedHorizonChipId)
            }

            val expectedSpeedChipId = when {
                abs(state.speedMultiplier - 0.5) < 0.05 -> R.id.chip_speed_05x
                abs(state.speedMultiplier - 2.0) < 0.05 -> R.id.chip_speed_2x
                abs(state.speedMultiplier - 3.0) < 0.05 -> R.id.chip_speed_3x
                abs(state.speedMultiplier - 5.0) < 0.05 -> R.id.chip_speed_5x
                else -> R.id.chip_speed_1x
            }
            if (chipGroupSpeed.checkedChipId != expectedSpeedChipId) {
                chipGroupSpeed.check(expectedSpeedChipId)
            }

            if (switchCameraPreview.isChecked != state.autoCameraPitchPreview) {
                switchCameraPreview.isChecked = state.autoCameraPitchPreview
            }
        } finally {
            isUpdatingUiFromState = false
        }

        val map = googleMap3D
        if (map != null && state.isMapReady) {
            renderMapScene(map, state)
        }
        updateFrameTicker(state)
    }

    private fun getAlertTitleText(alertType: TerrainAlertType): String = when (alertType) {
        TerrainAlertType.UNDULATING_ROLLER_COASTER -> getString(R.string.hilly_alert_title_roller_coaster)
        TerrainAlertType.BLIND_CREST -> getString(R.string.hilly_alert_title_blind_crest)
        TerrainAlertType.STEEP_CLIMB -> getString(R.string.hilly_alert_title_steep_climb)
        TerrainAlertType.STEEP_DESCENT -> getString(R.string.hilly_alert_title_steep_descent)
        TerrainAlertType.NONE -> getString(R.string.hilly_alert_title_clear)
    }

    private fun renderMapScene(map: GoogleMap3D, state: HillyRoadUiState) {
        try {
            // 1. Update Driver Chase Camera
            map.setCamera(
                camera {
                    center = state.cameraCenter
                    heading = state.cameraHeading
                    tilt = state.cameraTilt
                    range = state.cameraRange
                    roll = 0.0
                }
            )

            // 2. Rebuild Base Route Polyline & clear extrema markers if preset changed
            if (renderedPreset != state.selectedPreset || baseRoutePolyline == null) {
                renderedPreset = state.selectedPreset
                lastRenderedTraveledDist = -1.0
                lastRenderedHazardCoords = emptyList()
                baseRoutePolyline?.remove()
                baseRoutePolyline = map.addPolyline(
                    PolylineOptions().apply {
                        id = HillyRoadAlertViewModel.BASE_ROUTE_POLYLINE_ID
                        path = state.staticRouteVertices
                        strokeColor = Color.argb(210, 56, 189, 248) // Neutral Cyan (#38BDF8)
                        strokeWidth = 14.0
                        altitudeMode = AltitudeMode.ABSOLUTE
                        zIndex = 2
                        drawsOccludedSegments = true
                    }
                )
                clearExtremaMarkers()
            }

            // 3. Update Traveled Progress Polyline
            val traveledCoords = state.traveledRouteVertices
            if (traveledCoords.size >= 2 &&
                (traveledRoutePolyline == null || abs(state.currentDistanceMeters - lastRenderedTraveledDist) > 0.05)
            ) {
                lastRenderedTraveledDist = state.currentDistanceMeters
                traveledRoutePolyline = map.addPolyline(
                    PolylineOptions().apply {
                        id = HillyRoadAlertViewModel.TRAVELED_ROUTE_POLYLINE_ID
                        path = traveledCoords
                        strokeColor = Color.rgb(16, 185, 129) // Emerald green (#10B981)
                        strokeWidth = 16.0
                        altitudeMode = AltitudeMode.ABSOLUTE
                        zIndex = 3
                        drawsOccludedSegments = true
                    }
                )
            }

            // 4. Update Active Hazard Highlight Polyline
            val hazardCoords = state.terrainAlert.hazardPolylineCoordinates
            val severity = state.terrainAlert.severity
            if (hazardCoords.size >= 2 && severity != AlertSeverity.NONE) {
                if (hazardRoutePolyline == null ||
                    renderedHazardSeverity != severity ||
                    lastRenderedHazardCoords != hazardCoords
                ) {
                    val hazardColor = if (severity == AlertSeverity.WARNING) {
                        Color.rgb(234, 67, 53) // Canonical Google Red (#EA4335)
                    } else {
                        Color.rgb(251, 188, 4) // Canonical Google Amber (#FBBC04)
                    }
                    renderedHazardSeverity = severity
                    lastRenderedHazardCoords = hazardCoords
                    hazardRoutePolyline = map.addPolyline(
                        PolylineOptions().apply {
                            id = HillyRoadAlertViewModel.HAZARD_ROUTE_POLYLINE_ID
                            path = hazardCoords
                            strokeColor = hazardColor
                            strokeWidth = 20.0
                            altitudeMode = AltitudeMode.ABSOLUTE
                            zIndex = 5
                            drawsOccludedSegments = true
                        }
                    )
                }
            } else {
                hazardRoutePolyline?.remove()
                hazardRoutePolyline = null
                renderedHazardSeverity = null
                lastRenderedHazardCoords = emptyList()
            }

            // 5. Update Simulated Vehicle 3D Model (Red Car .glb from Routes API sample)
            if (vehicleModel == null ||
                lastRenderedVehiclePos != state.vehiclePosition ||
                lastRenderedVehicleHeading != state.currentHeadingDegrees
            ) {
                lastRenderedVehiclePos = state.vehiclePosition
                lastRenderedVehicleHeading = state.currentHeadingDegrees
                vehicleModel = map.addModel(
                    ModelOptions().apply {
                        id = HillyRoadAlertViewModel.VEHICLE_MODEL_ID
                        position = state.vehiclePosition
                        altitudeMode = AltitudeMode.ABSOLUTE
                        url = HillyRoadAlertViewModel.VEHICLE_MODEL_URL
                        orientation = Orientation(
                            state.currentHeadingDegrees,
                            HillyRoadAlertViewModel.VEHICLE_MODEL_TILT,
                            0.0
                        )
                        scale = Vector3D(
                            HillyRoadAlertViewModel.VEHICLE_MODEL_SCALE,
                            HillyRoadAlertViewModel.VEHICLE_MODEL_SCALE,
                            HillyRoadAlertViewModel.VEHICLE_MODEL_SCALE
                        )
                    }
                )
            }

            // 6. Diff 3D Crest & Dip Markers by stable ID (never recreate unchanged markers)
            syncExtremaMarkers(map, state.terrainAlert.upcomingExtrema)
        } catch (e: Exception) {
            Log.w(TAG, "Map scene update skipped while map is not ready: ${e.message}")
        }
    }

    private fun syncExtremaMarkers(
        map: GoogleMap3D,
        upcomingExtrema: List<TerrainExtremaPoint>
    ) {
        val desiredIds = upcomingExtrema.map { it.id }.toSet()

        // Remove markers that exited the lookahead window
        val iterator = activeExtremaMarkers.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.key !in desiredIds) {
                entry.value.remove()
                iterator.remove()
            }
        }

        // Add only newly entered extrema markers
        for (extremum in upcomingExtrema) {
            if (activeExtremaMarkers.containsKey(extremum.id)) continue

            val isCrest = extremum.type == ExtremaType.CREST
            val pinBgColor = if (isCrest) {
                Color.rgb(234, 67, 53) // Crest: Orange/Red (#EA4335)
            } else {
                Color.rgb(2, 132, 199) // Dip: Deep Sky Blue (#0284C7)
            }
            val pinBorderColor = if (isCrest) {
                Color.rgb(254, 240, 138)
            } else {
                Color.rgb(251, 188, 4) // Amber border (#FBBC04)
            }
            val glyphSymbol = if (isCrest) "^" else "v"
            val signedGrade = if (isCrest) {
                abs(extremum.approachGradePercent)
            } else {
                -abs(extremum.approachGradePercent)
            }
            val markerLabel = String.format(
                Locale.US,
                getString(
                    if (isCrest) R.string.hilly_marker_crest_format else R.string.hilly_marker_dip_format
                ),
                signedGrade
            )

            val pinStyle = PinConfiguration.builder()
                .setScale(1.2f)
                .setBackgroundColor(pinBgColor)
                .setBorderColor(pinBorderColor)
                .setGlyph(Glyph.fromColor(Color.WHITE).apply { setText(glyphSymbol) })
                .build()

            val marker = map.addMarker(
                MarkerOptions().apply {
                    id = extremum.id
                    position = extremum.position
                    label = markerLabel
                    altitudeMode = AltitudeMode.ABSOLUTE
                    zIndex = 8
                    isExtruded = true
                    isDrawnWhenOccluded = true
                    collisionBehavior = CollisionBehavior.REQUIRED_AND_HIDES_OPTIONAL
                    setStyle(pinStyle)
                }
            )

            if (marker != null) {
                val extremumId = extremum.id
                val fallbackDist = extremum.distanceAheadMeters
                val elevMeters = extremum.position.altitude.roundToInt()
                marker.setClickListener {
                    runOnUiThread {
                        val liveDist = viewModel.currentState.terrainAlert.upcomingExtrema
                            .firstOrNull { it.id == extremumId }
                            ?.distanceAheadMeters
                            ?: fallbackDist
                        Toast.makeText(
                            this,
                            getString(
                                R.string.hilly_marker_toast_format,
                                markerLabel,
                                liveDist.roundToInt(),
                                elevMeters
                            ),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                activeExtremaMarkers[extremum.id] = marker
            }
        }
    }

    private fun clearExtremaMarkers() {
        for (marker in activeExtremaMarkers.values) {
            marker.remove()
        }
        activeExtremaMarkers.clear()
    }

    private fun clearMapOverlays() {
        baseRoutePolyline?.remove()
        baseRoutePolyline = null
        traveledRoutePolyline?.remove()
        traveledRoutePolyline = null
        hazardRoutePolyline?.remove()
        hazardRoutePolyline = null
        vehicleModel?.remove()
        vehicleModel = null
        clearExtremaMarkers()
    }
}
