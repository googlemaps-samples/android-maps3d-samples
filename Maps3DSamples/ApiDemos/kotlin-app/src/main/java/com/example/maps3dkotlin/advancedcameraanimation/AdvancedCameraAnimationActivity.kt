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

package com.example.maps3dkotlin.advancedcameraanimation

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Choreographer
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.cardview.widget.CardView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.maps3d.common.AdvancedCameraAnimationViewModel
import com.example.maps3d.common.AnimationApproach
import com.example.maps3d.common.CameraKeyframe
import com.example.maps3d.common.HtmlUtils
import com.example.maps3d.common.Map3DModelEntity
import com.example.maps3d.common.SimpleFlyToMode
import com.example.maps3d.common.TourData
import com.example.maps3d.common.StationaryCameraTracker
import com.example.maps3d.common.TrajectoryFlightAnimator
import com.example.maps3d.common.awaitCameraUpdate
import com.example.maps3d.common.toCameraUpdate
import com.example.maps3dcommon.R as CommonR
import com.example.maps3dkotlin.R
import com.example.maps3dkotlin.sampleactivity.SampleBaseActivity
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.FlyToOptions
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.flyToOptions
import com.google.android.gms.maps3d.model.flyAroundOptions
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.math.abs
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Advanced Camera Animation demo showcasing 4 camera control paradigms in Google Maps 3D.
 *
 * Demonstrates:
 * 1. Native asynchronous SDK flyTo transitions with optional high-rate vs discrete model updates.
 * 2. Declarative sequential keyframe queuing (FlyTo -> Dwell -> Orbit -> FlyTo).
 * 3. High-rate 400 m/s continuous flight frame loop synced to display hardware VSYNC.
 * 4. 360-degree continuous orbital camera rotation around landmarks.
 */
class AdvancedCameraAnimationActivity : SampleBaseActivity() {
    override val TAG: String = "AdvancedCameraAnimationActivity"

    override val initialCamera: Camera = camera {
        center = latLngAltitude {
            latitude = TourData.AIRPLANE_FLIGHT_PATH.first().latitude
            longitude = TourData.AIRPLANE_FLIGHT_PATH.first().longitude
            altitude = 250.0
        }
        heading = 105.0
        tilt = 65.0
        range = 600.0
    }

    private val viewModel: AdvancedCameraAnimationViewModel by viewModels()
    private val airplaneEntity = Map3DModelEntity(TourData.AIRPLANE_MODEL_ID, TourData.AIRPLANE_MODEL_URL)

    // UI View References
    private lateinit var controlsCard: CardView
    private lateinit var headerTitleBar: LinearLayout
    private lateinit var btnHelp: MaterialButton
    private lateinit var btnCollapseToggle: MaterialButton
    private lateinit var btnPlayPause: MaterialButton
    private lateinit var btnReset: MaterialButton
    private lateinit var tvTourStatus: TextView
    private lateinit var collapsibleContent: LinearLayout
    private lateinit var btnSelectApproach: MaterialButton
    private lateinit var cardKeyframeTourStep: MaterialCardView
    private lateinit var tvKeyframeStepBadge: TextView
    private lateinit var tvKeyframeStepDesc: TextView
    private lateinit var progressKeyframeStep: LinearProgressIndicator
    private lateinit var tvStepDetail: TextView
    private lateinit var layoutSimpleFlyToOptions: LinearLayout
    private lateinit var chipGroupSimpleFlyToMode: ChipGroup

    private var isControlsCollapsed = false
    private val autoFadeHandler = Handler(Looper.getMainLooper())
    private val autoFadeRunnable = Runnable {
        controlsCard.animate().alpha(0.35f).setDuration(400L).start()
    }

    private var frameCallback: Choreographer.FrameCallback? = null
    private var tourJob: Job? = null

    /**
     * Suspends until the 3D map camera animation completes.
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide floating snapshot/recenter buttons from base layout
        snapshotButton.visibility = View.GONE
        recenterButton.visibility = View.GONE

        findViewById<MaterialToolbar>(CommonR.id.top_bar)?.apply {
            title = getString(CommonR.string.aerial_tour_title)
            subtitle = "Kotlin Views"
        }

        setupCustomControls()
        observeViewModel()
        resetAutoFadeTimer()
    }

    private fun setupCustomControls() {
        val rootLayout = findViewById<ViewGroup>(CommonR.id.map_container)
        val customView = layoutInflater.inflate(
            CommonR.layout.control_panel_advanced_animation,
            rootLayout,
            false
        )
        rootLayout.addView(customView)

        controlsCard = customView.findViewById(CommonR.id.control_panel)
        customView.findViewById<TextView>(CommonR.id.tv_framework_subtitle)?.apply {
            text = "Kotlin Views"
            visibility = View.VISIBLE
        }
        headerTitleBar = customView.findViewById(CommonR.id.header_title_bar)
        btnHelp = customView.findViewById(CommonR.id.btn_help)
        btnCollapseToggle = customView.findViewById(CommonR.id.btn_collapse_toggle)
        btnPlayPause = customView.findViewById(CommonR.id.btn_play_pause)
        btnReset = customView.findViewById(CommonR.id.btn_reset)
        tvTourStatus = customView.findViewById(CommonR.id.tv_tour_status)
        collapsibleContent = customView.findViewById(CommonR.id.collapsible_content)
        btnSelectApproach = customView.findViewById(CommonR.id.btn_select_approach)
        tvStepDetail = customView.findViewById(CommonR.id.tv_step_detail)
        layoutSimpleFlyToOptions = customView.findViewById(CommonR.id.layout_simple_fly_to_options)
        cardKeyframeTourStep = customView.findViewById(CommonR.id.card_keyframe_tour_step)
        tvKeyframeStepBadge = customView.findViewById(CommonR.id.tv_keyframe_step_badge)
        tvKeyframeStepDesc = customView.findViewById(CommonR.id.tv_keyframe_step_description)
        progressKeyframeStep = customView.findViewById(CommonR.id.progress_keyframe_step)
        chipGroupSimpleFlyToMode = customView.findViewById(CommonR.id.chip_group_simple_fly_to_mode)

        headerTitleBar.setOnClickListener {
            toggleControlsCollapse()
            resetAutoFadeTimer()
        }

        btnCollapseToggle.setOnClickListener {
            toggleControlsCollapse()
            resetAutoFadeTimer()
        }

        btnPlayPause.setOnClickListener {
            resetAutoFadeTimer()
            if (viewModel.currentState.isPlaying) {
                stopAnimationLoops()
                viewModel.pause()
            } else {
                startSelectedApproach()
            }
        }

        btnReset.setOnClickListener {
            resetAutoFadeTimer()
            stopAnimationLoops()
            viewModel.resetTour()
            val targetCam = if (viewModel.currentState.selectedApproach == AnimationApproach.KEYFRAME_TOUR) TourData.OVERVIEW_CAMERA else initialCamera
            googleMap3D?.setCamera(targetCam)
        }

        btnHelp.setOnClickListener {
            showHelpDialog()
            resetAutoFadeTimer()
        }

        btnSelectApproach.setOnClickListener { view ->
            resetAutoFadeTimer()
            showApproachMenu(view)
        }

        // Sub-mode switching for Simple FlyTo
        chipGroupSimpleFlyToMode.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            val mode = when (checkedIds.first()) {
                CommonR.id.chip_fly_to_midpoint -> SimpleFlyToMode.MIDPOINT_JUMP
                else -> SimpleFlyToMode.SYNCHRONIZED_FLIGHT
            }
            viewModel.setSimpleFlyToMode(mode)
            resetAutoFadeTimer()
        }

        // Swipe up/down gesture on bottom sheet
        val sheetGestureDetector = GestureDetector(
            this,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onFling(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    velocityX: Float,
                    velocityY: Float
                ): Boolean {
                    if (e1 == null) return false
                    val deltaY = e2.y - e1.y
                    if (abs(deltaY) > 50 && abs(velocityY) > 100) {
                        if (deltaY > 0 && !isControlsCollapsed) {
                            toggleControlsCollapse()
                        } else if (deltaY < 0 && isControlsCollapsed) {
                            toggleControlsCollapse()
                        }
                        return true
                    }
                    return false
                }
            }
        )

        controlsCard.setOnTouchListener { _, event ->
            resetAutoFadeTimer()
            sheetGestureDetector.onTouchEvent(event)
            false
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.worldState.collect { state ->
                    tvTourStatus.text = state.statusText
                    btnPlayPause.setIconResource(
                        if (state.isPlaying) CommonR.drawable.pause_24px else CommonR.drawable.play_arrow_24px
                    )

                    // Synchronize approach dropdown label & visibility of sub-options
                    btnSelectApproach.text = state.selectedApproach.title
                    layoutSimpleFlyToOptions.visibility =
                        if (state.selectedApproach == AnimationApproach.SIMPLE_FLY_TO) View.VISIBLE else View.GONE

                    val isKeyframeTour = state.selectedApproach == AnimationApproach.KEYFRAME_TOUR
                    cardKeyframeTourStep.visibility = if (isKeyframeTour) View.VISIBLE else View.GONE
                    if (isKeyframeTour) {
                        tvKeyframeStepBadge.text = if (state.stepTitle.isNotEmpty()) state.stepTitle else "Step ${state.currentStepIndex + 1} of ${state.totalSteps}"
                        tvKeyframeStepDesc.text = state.stepDescription
                        progressKeyframeStep.max = state.totalSteps
                        progressKeyframeStep.progress = state.currentStepIndex + 1
                    }

                    // Update detail explanation text
                    tvStepDetail.text = when (state.selectedApproach) {
                        AnimationApproach.SIMPLE_FLY_TO -> "Native asynchronous SDK flight transition directly to Coit Tower."
                        AnimationApproach.KEYFRAME_TOUR -> "Declarative 5-step sequence: Swoop FlyTo → Dwell Pause → 360° Orbit → Stationary Tracking Flight → Final FlyTo."
                        AnimationApproach.DISPATCHER_FRAME_LOOP -> "Continuous 400 m/s flight synced to hardware VSYNC display frames."
                        AnimationApproach.ORBIT_360_SPIN -> "Continuous 360° orbital camera rotation around Golden Gate Bridge."
                    }

                    // Synchronize sub-mode chip selection
                    val targetSubModeChipId = when (state.simpleFlyToMode) {
                        SimpleFlyToMode.SYNCHRONIZED_FLIGHT -> CommonR.id.chip_fly_to_synchronized
                        SimpleFlyToMode.MIDPOINT_JUMP -> CommonR.id.chip_fly_to_midpoint
                    }
                    if (chipGroupSimpleFlyToMode.checkedChipId != targetSubModeChipId) {
                        chipGroupSimpleFlyToMode.check(targetSubModeChipId)
                    }

                    // Synchronize 3D camera for frame dispatcher and continuous orbit
                    googleMap3D?.let { map ->
                        if (state.selectedApproach == AnimationApproach.DISPATCHER_FRAME_LOOP ||
                            state.selectedApproach == AnimationApproach.ORBIT_360_SPIN) {
                            map.setCamera(state.camera)
                        }
                    }

                    // Synchronize 3D Airplane model pose
                    state.getEntityPose(TourData.AIRPLANE_MODEL_ID)?.let { pose ->
                        airplaneEntity.applyPose(pose, googleMap3D)
                    }
                }
            }
        }
    }

    override fun onMapReady(googleMap3D: GoogleMap3D) {
        super.onMapReady(googleMap3D)
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isDestroyed && !isFinishing) {
                val targetCam = if (viewModel.currentState.selectedApproach == AnimationApproach.KEYFRAME_TOUR) TourData.OVERVIEW_CAMERA else initialCamera
                this.googleMap3D?.setCamera(targetCam)
                viewModel.currentState.getEntityPose(TourData.AIRPLANE_MODEL_ID)?.let { initialPose ->
                    this.googleMap3D?.let { map -> airplaneEntity.attach(map, initialPose) }
                }
            }
        }, 350L)
    }

    private fun resetAndRestartTour() {
        val targetCam = if (viewModel.currentState.selectedApproach == AnimationApproach.KEYFRAME_TOUR) TourData.OVERVIEW_CAMERA else initialCamera
            googleMap3D?.setCamera(targetCam)
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isDestroyed && !isFinishing) {
                startSelectedApproach()
            }
        }, 400L)
    }

    private fun startSelectedApproach() {
        val map = googleMap3D ?: return
        stopAnimationLoops()
        viewModel.play()

        when (viewModel.currentState.selectedApproach) {
            AnimationApproach.SIMPLE_FLY_TO -> runSimpleFlyTo(map)
            AnimationApproach.KEYFRAME_TOUR -> runKeyframeTour(map)
            AnimationApproach.DISPATCHER_FRAME_LOOP -> runFrameDispatcherLoop()
            AnimationApproach.ORBIT_360_SPIN -> runContinuousOrbitLoop()
        }
    }

    private fun runSimpleFlyTo(map: GoogleMap3D) {
        tourJob = lifecycleScope.launch {
            val targetLoc = TourData.AIRPLANE_FLIGHT_PATH.last()
            val targetCam = camera {
                center = latLngAltitude {
                    latitude = targetLoc.latitude
                    longitude = targetLoc.longitude
                    altitude = 250.0
                }
                heading = 285.0 // Facing back toward Golden Gate Bridge to watch the plane fly in
                tilt = 65.0
                range = 600.0
            }

            val options = flyToOptions {
                endCamera = targetCam
                durationInMillis = 5000L
            }

            // Start VSYNC frame callback to tick the world model & update plane entity during flyTo
            frameCallback = object : Choreographer.FrameCallback {
                private var lastTimeNanos = 0L
                override fun doFrame(frameTimeNanos: Long) {
                    if (lastTimeNanos > 0L) {
                        val dt = (frameTimeNanos - lastTimeNanos) / 1_000_000_000.0
                        viewModel.tick(dt.coerceIn(0.001, 0.1))
                    }
                    lastTimeNanos = frameTimeNanos
                    if (viewModel.currentState.isPlaying) {
                        Choreographer.getInstance().postFrameCallback(this)
                    }
                }
            }
            Choreographer.getInstance().postFrameCallback(frameCallback!!)

            map.awaitFlyCameraTo(options)

            frameCallback?.let { Choreographer.getInstance().removeFrameCallback(it) }
            viewModel.onNativeCameraAnimationFinished()
        }
    }

    private fun runKeyframeTour(map: GoogleMap3D) {
        tourJob = lifecycleScope.launch {
            for (index in TourData.SAN_FRANCISCO_TOUR.indices) {
                if (!isActive || !viewModel.currentState.isPlaying) break
                viewModel.setKeyframeStep(index)

                when (val step = TourData.SAN_FRANCISCO_TOUR[index]) {
                    is CameraKeyframe.FlyTo -> {
                        val options = flyToOptions {
                            endCamera = step.targetCamera
                            durationInMillis = step.durationMs
                        }
                        awaitCameraUpdate(map, options.toCameraUpdate())
                    }

                    is CameraKeyframe.DwellPause -> {
                        kotlinx.coroutines.delay(step.durationMs)
                    }

                    is CameraKeyframe.FlyAround -> {
                        val options = flyAroundOptions {
                            center = step.centerCamera
                            rounds = step.rounds
                            durationInMillis = step.durationMs
                        }
                        awaitCameraUpdate(map, options.toCameraUpdate())
                    }

                    is CameraKeyframe.StationaryTrackingFlight -> {
                        val toVantageOptions = flyToOptions {
                            endCamera = step.observationCamera
                            durationInMillis = 2000L
                        }
                        awaitCameraUpdate(map, toVantageOptions.toCameraUpdate())

                        val tracker = StationaryCameraTracker.fromInitialCamera(step.observationCamera)
                        val flightAnimator = TrajectoryFlightAnimator(step.flightPath, altitude = 250.0, scale = 0.08)
                        val startMs = System.currentTimeMillis()

                        while (isActive && viewModel.currentState.isPlaying) {
                            val elapsed = System.currentTimeMillis() - startMs
                            val targetPose = flightAnimator.update(elapsed, step.durationMs)
                            val trackingCam = tracker.computeTrackingCamera(targetPose)

                            viewModel.updateAirplanePose(targetPose)
                            airplaneEntity.applyPose(targetPose, map)
                            map.setCamera(trackingCam)

                            if (flightAnimator.isFinished(elapsed, step.durationMs)) break
                            kotlinx.coroutines.delay(16L)
                        }

                        val finalPose = flightAnimator.update(step.durationMs, step.durationMs)
                        viewModel.updateAirplanePose(finalPose)
                        airplaneEntity.applyPose(finalPose, map)
                    }
                }
            }
            viewModel.onNativeCameraAnimationFinished()
        }
    }

    private fun runFrameDispatcherLoop() {
        frameCallback = object : Choreographer.FrameCallback {
            private var lastTimeNanos = 0L
            override fun doFrame(frameTimeNanos: Long) {
                if (lastTimeNanos > 0L) {
                    val dt = (frameTimeNanos - lastTimeNanos) / 1_000_000_000.0
                    viewModel.tick(dt.coerceIn(0.001, 0.1))
                }
                lastTimeNanos = frameTimeNanos
                if (viewModel.currentState.isPlaying) {
                    Choreographer.getInstance().postFrameCallback(this)
                }
            }
        }
        Choreographer.getInstance().postFrameCallback(frameCallback!!)
    }

    private fun runContinuousOrbitLoop() {
        frameCallback = object : Choreographer.FrameCallback {
            private var lastTimeNanos = 0L
            override fun doFrame(frameTimeNanos: Long) {
                if (lastTimeNanos > 0L) {
                    val dt = (frameTimeNanos - lastTimeNanos) / 1_000_000_000.0
                    viewModel.tick(dt.coerceIn(0.001, 0.1))
                }
                lastTimeNanos = frameTimeNanos
                if (viewModel.currentState.isPlaying) {
                    Choreographer.getInstance().postFrameCallback(this)
                }
            }
        }
        Choreographer.getInstance().postFrameCallback(frameCallback!!)
    }

    private fun stopAnimationLoops() {
        frameCallback?.let { Choreographer.getInstance().removeFrameCallback(it) }
        frameCallback = null
        tourJob?.cancel()
        tourJob = null
    }

    private fun toggleControlsCollapse() {
        isControlsCollapsed = !isControlsCollapsed
        collapsibleContent.visibility = if (isControlsCollapsed) View.GONE else View.VISIBLE
        btnCollapseToggle.setIconResource(
            if (isControlsCollapsed) CommonR.drawable.expand_less_24px else CommonR.drawable.expand_more_24px
        )
    }

    private fun updateApproachUI(approach: AnimationApproach) {
        layoutSimpleFlyToOptions.visibility = if (approach == AnimationApproach.SIMPLE_FLY_TO) View.VISIBLE else View.GONE
        tvStepDetail.text = when (approach) {
            AnimationApproach.SIMPLE_FLY_TO -> "Native asynchronous SDK flight transition directly to Coit Tower."
            AnimationApproach.KEYFRAME_TOUR -> "Declarative 5-step sequence: Swoop FlyTo → Dwell Pause → 360° Orbit → Stationary Tracking Flight → Final FlyTo."
            AnimationApproach.DISPATCHER_FRAME_LOOP -> "Continuous 400 m/s flight synced to hardware VSYNC display frames."
            AnimationApproach.ORBIT_360_SPIN -> "Continuous 360° orbital camera rotation around Golden Gate Bridge."
        }
    }

    private fun resetAutoFadeTimer() {
        controlsCard.animate().alpha(1.0f).setDuration(150L).start()
        autoFadeHandler.removeCallbacks(autoFadeRunnable)
        autoFadeHandler.postDelayed(autoFadeRunnable, 3500L)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        resetAutoFadeTimer()
        return super.dispatchTouchEvent(ev)
    }

    private fun showApproachMenu(anchor: View) {
        val popup = androidx.appcompat.widget.PopupMenu(this, anchor)
        val approaches = AnimationApproach.values()
        approaches.forEachIndexed { index, approach ->
            popup.menu.add(0, index, index, approach.title)
        }
        popup.setOnMenuItemClickListener { menuItem ->
            resetAutoFadeTimer()
            val selected = approaches.getOrNull(menuItem.itemId) ?: return@setOnMenuItemClickListener false
            stopAnimationLoops()
            viewModel.setApproach(selected)
            viewModel.resetTour()
            val targetCam = if (viewModel.currentState.selectedApproach == AnimationApproach.KEYFRAME_TOUR) TourData.OVERVIEW_CAMERA else initialCamera
            googleMap3D?.setCamera(targetCam)
            true
        }
        popup.show()
    }

    private fun showHelpDialog() {
        val dialogView = layoutInflater.inflate(CommonR.layout.dialog_help_advanced_animation, null)
        dialogView.findViewById<TextView>(CommonR.id.tv_help_html_content)?.text =
            HtmlUtils.loadRawHtml(this, CommonR.raw.help_advanced_animation)

        MaterialAlertDialogBuilder(this)
            .setTitle(CommonR.string.help_dialog_advanced_animation_title)
            .setView(dialogView)
            .setPositiveButton(CommonR.string.help_dialog_ok) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun onPause() {
        super.onPause()
        stopAnimationLoops()
        viewModel.pause()
        autoFadeHandler.removeCallbacks(autoFadeRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        airplaneEntity.detach()
    }
}
