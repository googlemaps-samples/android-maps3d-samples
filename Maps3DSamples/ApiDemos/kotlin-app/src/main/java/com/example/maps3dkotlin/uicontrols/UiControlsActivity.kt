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
import com.example.maps3d.common.Map3DQuickSettingsButton
import com.example.maps3d.common.Map3DSettings
import com.example.maps3d.common.Map3DUiControls
import com.example.maps3d.common.toValidCamera
import com.example.maps3dcommon.R
import com.example.maps3dkotlin.sampleactivity.SampleBaseActivity
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.UiControlSettings.ControlOrientation
import com.google.android.gms.maps3d.UiControlSettings.ControlPlacement
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.CompassControl
import com.google.android.gms.maps3d.model.Map3DControl
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.PanHorizontalControl
import com.google.android.gms.maps3d.model.PanVerticalControl
import com.google.android.gms.maps3d.model.RotateControl
import com.google.android.gms.maps3d.model.TiltControl
import com.google.android.gms.maps3d.model.ZoomControl
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
 * PanVertical, PanHorizontal) and toggling their screen placement between
 * [ControlPlacement.TRAILING] and [ControlPlacement.LEADING] using direct SDK API calls.
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
            setAllChips(true)
            applyControlsToMap()
        }

        btnHideAll?.setOnClickListener {
            setAllChips(false)
            applyControlsToMap()
        }

        val chipListener = {
            if (!isSyncingUi) {
                applyControlsToMap()
            }
        }

        chipZoom?.setOnCheckedChangeListener { _, _ -> chipListener() }
        chipTilt?.setOnCheckedChangeListener { _, _ -> chipListener() }
        chipRotate?.setOnCheckedChangeListener { _, _ -> chipListener() }
        chipCompass?.setOnCheckedChangeListener { _, _ -> chipListener() }
        chipPanVertical?.setOnCheckedChangeListener { _, _ -> chipListener() }
        chipPanHorizontal?.setOnCheckedChangeListener { _, _ -> chipListener() }

        rgPlacement?.setOnCheckedChangeListener { _, _ ->
            if (!isSyncingUi) {
                applyControlsToMap()
            }
        }

        fadeHandler.postDelayed(fadeOutRunnable, 3000L)
    }

    private fun setAllChips(checked: Boolean) {
        isSyncingUi = true
        chipZoom?.isChecked = checked
        chipTilt?.isChecked = checked
        chipRotate?.isChecked = checked
        chipCompass?.isChecked = checked
        chipPanVertical?.isChecked = checked
        chipPanHorizontal?.isChecked = checked
        isSyncingUi = false
    }

    /**
     * Applies the UI controls configuration directly to the [GoogleMap3D] instance using the SDK API.
     */
    private fun applyControlsToMap() {
        val map = googleMap3D ?: return
        val uiSettings = map.getUiControlSettings() ?: return

        val primaryControls = mutableListOf<Map3DControl>()
        if (chipZoom?.isChecked == true) primaryControls.add(ZoomControl(ControlOrientation.VERTICAL))
        if (chipTilt?.isChecked == true) primaryControls.add(TiltControl(ControlOrientation.VERTICAL))
        if (chipRotate?.isChecked == true) primaryControls.add(RotateControl(ControlOrientation.VERTICAL))

        val secondaryControls = mutableListOf<Map3DControl>()
        if (chipPanVertical?.isChecked == true) secondaryControls.add(PanVerticalControl(ControlOrientation.VERTICAL))
        if (chipPanHorizontal?.isChecked == true) secondaryControls.add(PanHorizontalControl(ControlOrientation.VERTICAL))
        if (chipCompass?.isChecked == true) secondaryControls.add(CompassControl())

        val isLeading = rgPlacement?.checkedRadioButtonId == R.id.rb_placement_leading
        val primaryPlacement = if (isLeading) ControlPlacement.LEADING else ControlPlacement.TRAILING
        val secondaryPlacement = if (isLeading) ControlPlacement.TRAILING else ControlPlacement.LEADING

        uiSettings.setUiControlsPlacement(primaryControls, primaryPlacement)
        uiSettings.setUiControlsPlacement(secondaryControls, secondaryPlacement)
        val localUiControls = Map3DUiControls(
            zoom = chipZoom?.isChecked == true,
            tilt = chipTilt?.isChecked == true,
            rotate = chipRotate?.isChecked == true,
            compass = chipCompass?.isChecked == true,
            panVertical = chipPanVertical?.isChecked == true,
            panHorizontal = chipPanHorizontal?.isChecked == true,
            controlsPosition = primaryPlacement,
        )
        findViewById<Map3DQuickSettingsButton?>(R.id.quick_settings_button)
            ?.updateTopMarginForUiControls(localUiControls)
        Map3DSettings.applyStatusBarInsetsToMapView(map3DView)
        map3DView.post {
            Map3DSettings.applyStatusBarInsetsToMapView(map3DView)
        }
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

    override fun onQuickSettingsChanged(settings: Map3DSettings) {
        super.onQuickSettingsChanged(settings)
        applyControlsToMap()
    }

    override fun onMapReady(googleMap3D: GoogleMap3D) {
        super.onMapReady(googleMap3D)
        googleMap3D.setCamera(initialCamera)
        applyControlsToMap()
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
