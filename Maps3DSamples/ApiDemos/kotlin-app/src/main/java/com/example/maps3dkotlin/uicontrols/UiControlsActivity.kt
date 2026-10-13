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

package com.example.maps3dkotlin.uicontrols

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.transition.TransitionManager
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.cardview.widget.CardView
import com.example.maps3d.common.Map3DSettings
import com.example.maps3d.common.Map3DSettingsController
import com.example.maps3d.common.UiControlsPlacementPosition
import com.example.maps3d.common.toValidCamera
import com.example.maps3dcommon.R
import com.example.maps3dkotlin.sampleactivity.SampleBaseActivity
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip

/**
 * =================================================================================================
 * Dedicated 3D Map UI Controls & Placement Demo (Kotlin Views)
 * =================================================================================================
 *
 * Demonstrates configuring built-in 3D map UI control buttons (Zoom, Tilt, Rotate, Compass,
 * PanVertical, PanHorizontal, Spacer) and toggling their screen placement between
 * [UiControlsPlacementPosition.TRAILING] and [UiControlsPlacementPosition.LEADING].
 */
class UiControlsActivity : SampleBaseActivity() {

    override val TAG = "UiControlsActivity"

    @Map3DMode
    override val defaultMapMode: Int = Map3DMode.ROADMAP

    override val initialCamera: Camera
        get() = camera {
            center = latLngAltitude {
                latitude = SF_LOCATION.latitude
                longitude = SF_LOCATION.longitude
                altitude = 250.0
            }
            heading = 45.0
            tilt = 65.0
            roll = 0.0
            range = 800.0
        }.toValidCamera()

    // --- UI Elements ---

    private var controlsCard: CardView? = null
    private var cardHeader: View? = null
    private var cardContent: View? = null
    private var btnCollapse: MaterialButton? = null
    private var btnShowAll: MaterialButton? = null
    private var btnHideAll: MaterialButton? = null
    private var chipZoom: Chip? = null
    private var chipTilt: Chip? = null
    private var chipRotate: Chip? = null
    private var chipCompass: Chip? = null
    private var chipPanVertical: Chip? = null
    private var chipPanHorizontal: Chip? = null
    private var rgPlacement: RadioGroup? = null

    // --- State Variables ---

    private var isCollapsed = false
    private var isSyncingUi = false

    // --- Handlers & Runnables ---

    private val fadeHandler = Handler(Looper.getMainLooper())
    private val fadeOutRunnable = Runnable {
        if (!isCollapsed) {
            controlsCard?.animate()
                ?.alpha(0.85f)
                ?.setDuration(400)
                ?.start()
        }
    }

    // --- Lifecycle & Initialization ---

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide base pill scroll view as this activity manages its own overlay card
        findViewById<View>(R.id.control_scroll_view)?.visibility = View.GONE

        // Inflate UI controls panel overlay into map container managed by SampleBaseActivity
        findViewById<ViewGroup>(R.id.map_container)?.let { container ->
            layoutInflater.inflate(R.layout.control_panel_ui_controls, container, true)
        }

        findViewById<MaterialToolbar>(R.id.top_bar)?.apply {
            setTitle(R.string.feature_title_ui_controls)
            setNavigationOnClickListener { finish() }
        }

        initViews()
    }

    private fun initViews() {
        controlsCard = findViewById(R.id.control_panel)
        cardHeader = findViewById(R.id.card_header)
        cardContent = findViewById(R.id.card_content)
        btnCollapse = findViewById(R.id.btn_collapse)
        btnShowAll = findViewById(R.id.btn_show_all)
        btnHideAll = findViewById(R.id.btn_hide_all)
        chipZoom = findViewById(R.id.chip_zoom)
        chipTilt = findViewById(R.id.chip_tilt)
        chipRotate = findViewById(R.id.chip_rotate)
        chipCompass = findViewById(R.id.chip_compass)
        chipPanVertical = findViewById(R.id.chip_pan_vertical)
        chipPanHorizontal = findViewById(R.id.chip_pan_horizontal)
        rgPlacement = findViewById(R.id.rg_controls_placement)

        btnCollapse?.setOnClickListener {
            if (isCollapsed) expandControls() else collapseControls()
        }

        cardHeader?.setOnClickListener {
            if (isCollapsed) expandControls() else collapseControls()
        }

        btnShowAll?.setOnClickListener {
            val updated = Map3DSettingsController.shared.setAllUiControls(true)
            googleMap3D?.let { updated.applyTo(it) }
            syncControlPanelUi(updated)
        }

        btnHideAll?.setOnClickListener {
            val updated = Map3DSettingsController.shared.setAllUiControls(false)
            googleMap3D?.let { updated.applyTo(it) }
            syncControlPanelUi(updated)
        }

        chipZoom?.setOnCheckedChangeListener { _, isChecked ->
            if (isSyncingUi) return@setOnCheckedChangeListener
            val updated = Map3DSettingsController.shared.updateUiControl(zoom = isChecked)
            googleMap3D?.let { updated.applyTo(it) }
        }

        chipTilt?.setOnCheckedChangeListener { _, isChecked ->
            if (isSyncingUi) return@setOnCheckedChangeListener
            val updated = Map3DSettingsController.shared.updateUiControl(tilt = isChecked)
            googleMap3D?.let { updated.applyTo(it) }
        }

        chipRotate?.setOnCheckedChangeListener { _, isChecked ->
            if (isSyncingUi) return@setOnCheckedChangeListener
            val updated = Map3DSettingsController.shared.updateUiControl(rotate = isChecked)
            googleMap3D?.let { updated.applyTo(it) }
        }

        chipCompass?.setOnCheckedChangeListener { _, isChecked ->
            if (isSyncingUi) return@setOnCheckedChangeListener
            val updated = Map3DSettingsController.shared.updateUiControl(compass = isChecked)
            googleMap3D?.let { updated.applyTo(it) }
        }

        chipPanVertical?.setOnCheckedChangeListener { _, isChecked ->
            if (isSyncingUi) return@setOnCheckedChangeListener
            val updated = Map3DSettingsController.shared.updateUiControl(panVertical = isChecked)
            googleMap3D?.let { updated.applyTo(it) }
        }

        chipPanHorizontal?.setOnCheckedChangeListener { _, isChecked ->
            if (isSyncingUi) return@setOnCheckedChangeListener
            val updated = Map3DSettingsController.shared.updateUiControl(panHorizontal = isChecked)
            googleMap3D?.let { updated.applyTo(it) }
        }

        rgPlacement?.setOnCheckedChangeListener { _, checkedId ->
            if (isSyncingUi) return@setOnCheckedChangeListener
            val position = when (checkedId) {
                R.id.rb_placement_leading -> UiControlsPlacementPosition.LEADING
                R.id.rb_placement_trailing -> UiControlsPlacementPosition.TRAILING
                else -> return@setOnCheckedChangeListener
            }
            val updated = Map3DSettingsController.shared.setControlsPosition(position)
            googleMap3D?.let { updated.applyTo(it) }
        }

        syncControlPanelUi(Map3DSettingsController.shared.settings.value)
        fadeHandler.postDelayed(fadeOutRunnable, 3000L)
    }

    override fun onQuickSettingsChanged(settings: Map3DSettings) {
        syncControlPanelUi(settings)
    }

    private fun syncControlPanelUi(settings: Map3DSettings) {
        isSyncingUi = true
        val controls = settings.uiControls
        chipZoom?.isChecked = controls.zoom
        chipTilt?.isChecked = controls.tilt
        chipRotate?.isChecked = controls.rotate
        chipCompass?.isChecked = controls.compass
        chipPanVertical?.isChecked = controls.panVertical
        chipPanHorizontal?.isChecked = controls.panHorizontal

        val targetPlacementId = if (controls.controlsPosition == UiControlsPlacementPosition.LEADING) {
            R.id.rb_placement_leading
        } else {
            R.id.rb_placement_trailing
        }
        val group = rgPlacement
        if (group != null && group.checkedRadioButtonId != targetPlacementId) {
            group.check(targetPlacementId)
        }
        isSyncingUi = false
    }

    // --- UI Collapse / Expand Mechanics ---

    private fun collapseControls() {
        val card = controlsCard ?: return
        val content = cardContent ?: return
        isCollapsed = true
        fadeHandler.removeCallbacks(fadeOutRunnable)
        btnCollapse?.setIconResource(R.drawable.expand_less_24px)
        btnCollapse?.contentDescription = getString(R.string.expand_controls)

        TransitionManager.beginDelayedTransition(card)
        content.visibility = View.GONE
    }

    private fun expandControls() {
        val card = controlsCard ?: return
        val content = cardContent ?: return
        isCollapsed = false
        btnCollapse?.setIconResource(R.drawable.expand_more_24px)
        btnCollapse?.contentDescription = getString(R.string.collapse_controls)

        TransitionManager.beginDelayedTransition(card)
        content.visibility = View.VISIBLE

        fadeHandler.removeCallbacks(fadeOutRunnable)
        fadeHandler.postDelayed(fadeOutRunnable, 3000L)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN || ev?.action == MotionEvent.ACTION_MOVE) {
            if (!isCollapsed) {
                controlsCard?.animate()
                    ?.alpha(1.0f)
                    ?.setDuration(150)
                    ?.start()
                fadeHandler.removeCallbacks(fadeOutRunnable)
                fadeHandler.postDelayed(fadeOutRunnable, 3000L)
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    // --- 3D Map Setup ---

    override fun onMapReady(googleMap3D: GoogleMap3D) {
        super.onMapReady(googleMap3D)
        googleMap3D.setCamera(initialCamera)
    }

    override fun onDestroy() {
        fadeHandler.removeCallbacks(fadeOutRunnable)
        super.onDestroy()
    }

    companion object {
        /** Focal landmark centered on the San Francisco Financial District. */
        val SF_LOCATION = LatLng(37.7915, -122.4010)
    }
}
