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

package com.example.maps3dkotlin.roadmapmode

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
import com.example.maps3d.common.copy
import com.example.maps3d.common.toValidCamera
import com.example.maps3dcommon.R
import com.example.maps3dkotlin.sampleactivity.SampleBaseActivity
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.flyToOptions
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

/**
 * =================================================================================================
 * 3D Roadmap Mode & Render Style Switching (Kotlin)
 * =================================================================================================
 *
 * This sample demonstrates switching between distinct visual rendering modes provided by the
 * Google Maps 3D SDK:
 *
 * Key Concepts Demonstrated:
 * 1. 3D Map Rendering Modes ([Map3DMode]):
 *    - [Map3DMode.ROADMAP]: High-contrast 3D vector street network with clean white building
 *      massings, road labels, and stylized transit geometry.
 *    - [Map3DMode.HYBRID]: High-resolution 3D photorealistic mesh overlaid with prominent vector
 *      road networks, street names, and point-of-interest labels.
 *    - [Map3DMode.SATELLITE]: Pure photorealistic 3D mesh rendering without overlay labels or
 *      vector lines, ideal for cinematic aerial exploration.
 *
 * 2. Camera Stability & Safe Angle Validation:
 *    - Configures a dramatic 3D perspective centered on the San Francisco Financial District with
 *      [toValidCamera].
 *
 * 3. Modern Material UI & Collapse Affordances:
 *    - Bottom control card with quick radio button switching between map rendering styles.
 *    - Expandable / collapsible header bar for unobstructed 3D scene inspection.
 *    - Subtle UI idle auto-fade with touch-to-wake responsiveness.
 */
class RoadmapModeActivity : SampleBaseActivity() {

  override val TAG = "RoadmapModeActivity"

  @Map3DMode
  override val defaultMapMode: Int = Map3DMode.ROADMAP

  override val initialCamera: Camera
    get() = camera {
      center = latLngAltitude {
        latitude = SF_LOCATION.latitude
        longitude = SF_LOCATION.longitude
        altitude = 0.0
      }
      heading = 45.0
      tilt = if (Map3DSettingsController.shared.getSettings().mapMode != Map3DMode.ROADMAP) 65.0 else 0.0
      roll = 0.0
      range = 2500.0
    }.toValidCamera()

  // --- UI Elements ---

  private var controlsCard: CardView? = null
  private var cardHeader: View? = null
  private var cardContent: View? = null
  private var btnCollapse: MaterialButton? = null
  private var btnToggle2D3D: MaterialButton? = null
  private var rgMapMode: RadioGroup? = null

  // --- State Variables ---

  private var isCollapsed = false
  private var isSyncingRadioGroup = false
  private var is3DMode = false
  private var hasSyncedInitialMode = false

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
    Map3DSettingsController.shared.onSampleEntered(defaultMapMode)
    is3DMode = Map3DSettingsController.shared.getSettings().mapMode != Map3DMode.ROADMAP
    super.onCreate(savedInstanceState)

    // Hide base pill scroll view as this activity manages its own overlay card
    findViewById<View>(R.id.control_scroll_view)?.visibility = View.GONE

    // Inflate roadmap control panel overlay into map container managed by SampleBaseActivity
    findViewById<ViewGroup>(R.id.map_container)?.let { container ->
      layoutInflater.inflate(R.layout.control_panel_roadmap_mode, container, true)
    }

    findViewById<MaterialToolbar>(R.id.top_bar)?.apply {
      setTitle(R.string.feature_title_roadmap_mode)
      setNavigationOnClickListener { finish() }
    }

    initViews()
  }

  /**
   * Initializes view references and wires up touch and click listeners.
   */
  private fun initViews() {
    controlsCard = findViewById(R.id.control_panel)
    cardHeader = findViewById(R.id.card_header)
    rgMapMode = findViewById(R.id.rg_map_mode)
    cardContent = rgMapMode
    btnCollapse = findViewById(R.id.btn_collapse)
    btnToggle2D3D = findViewById(R.id.btn_toggle_2d_3d)
    updateToggle2D3DButtonText()

    btnToggle2D3D?.setOnClickListener {
      val nextMode = if (is3DMode) Map3DMode.ROADMAP else Map3DMode.HYBRID
      is3DMode = nextMode != Map3DMode.ROADMAP
      updateToggle2D3DButtonText()
      Map3DSettingsController.shared.setMapMode(nextMode)
      onQuickSettingsChanged(Map3DSettingsController.shared.settings.value)
      googleMap3D?.let { map ->
        Map3DSettingsController.shared.settings.value.applyTo(map)
        applyCameraPerspective(map, is3DMode)
      }
    }

    btnCollapse?.setOnClickListener {
      if (isCollapsed) expandControls() else collapseControls()
    }

    cardHeader?.setOnClickListener {
      if (isCollapsed) expandControls() else collapseControls()
    }

    rgMapMode?.setOnCheckedChangeListener { _, checkedId ->
      if (isSyncingRadioGroup) return@setOnCheckedChangeListener
      val mode = when (checkedId) {
        R.id.rb_roadmap -> Map3DMode.ROADMAP
        R.id.rb_hybrid -> Map3DMode.HYBRID
        R.id.rb_satellite -> Map3DMode.SATELLITE
        else -> return@setOnCheckedChangeListener
      }
      is3DMode = mode != Map3DMode.ROADMAP
      updateToggle2D3DButtonText()
      Map3DSettingsController.shared.setMapMode(mode)
      googleMap3D?.let { map ->
        Map3DSettingsController.shared.settings.value.applyTo(map)
        applyCameraPerspective(map, is3DMode)
      }
    }

    // Schedule subtle initial auto-fade for unobstructed viewing
    fadeHandler.postDelayed(fadeOutRunnable, 3000L)
  }

  private fun updateToggle2D3DButtonText() {
    btnToggle2D3D?.setText(
      if (is3DMode) R.string.store_locator_switch_to_2d else R.string.store_locator_switch_to_3d
    )
  }

  private fun applyCameraPerspective(map: GoogleMap3D, target3D: Boolean) {
    val targetTilt = if (target3D) 65.0 else 0.0
    map.flyCameraTo(
      flyToOptions {
        endCamera = initialCamera.copy(tilt = targetTilt)
        durationInMillis = 900
      }
    )
  }

  override fun onQuickSettingsChanged(settings: Map3DSettings) {
    val nextIs3D = settings.mapMode != Map3DMode.ROADMAP
    val modeChangedAfterInit = hasSyncedInitialMode && is3DMode != nextIs3D
    hasSyncedInitialMode = true
    is3DMode = nextIs3D
    updateToggle2D3DButtonText()
    if (modeChangedAfterInit) {
      googleMap3D?.let { applyCameraPerspective(it, is3DMode) }
    }
    val targetId = when (settings.mapMode) {
      Map3DMode.ROADMAP -> R.id.rb_roadmap
      Map3DMode.HYBRID -> R.id.rb_hybrid
      Map3DMode.SATELLITE -> R.id.rb_satellite
      else -> return
    }
    val group = rgMapMode ?: return
    if (group.checkedRadioButtonId != targetId) {
      isSyncingRadioGroup = true
      group.check(targetId)
      isSyncingRadioGroup = false
    }
  }

  // --- UI Collapse / Expand Mechanics ---

  /**
   * Collapses the control card downward, leaving only the title header visible.
   */
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

  /**
   * Expands the control card back to its full height.
   */
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

  // --- Teardown ---

  override fun onDestroy() {
    fadeHandler.removeCallbacks(fadeOutRunnable)
    super.onDestroy()
  }

  companion object {
    /** Focal landmark centered on the San Francisco Financial District. */
    val SF_LOCATION = LatLng(37.7915, -122.4010)
  }
}

