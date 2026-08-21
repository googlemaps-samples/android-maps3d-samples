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
  private var rgMapMode: RadioGroup? = null

  // --- State Variables ---

  private var isCollapsed = false

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
    cardContent = findViewById(R.id.card_content)
    btnCollapse = findViewById(R.id.btn_collapse)
    rgMapMode = findViewById(R.id.rg_map_mode)

    btnCollapse?.setOnClickListener {
      if (isCollapsed) expandControls() else collapseControls()
    }

    cardHeader?.setOnClickListener {
      if (isCollapsed) expandControls() else collapseControls()
    }

    rgMapMode?.setOnCheckedChangeListener { _, checkedId ->
      googleMap3D?.let { map ->
        when (checkedId) {
          R.id.rb_roadmap -> map.setMapMode(Map3DMode.ROADMAP)
          R.id.rb_hybrid -> map.setMapMode(Map3DMode.HYBRID)
          R.id.rb_satellite -> map.setMapMode(Map3DMode.SATELLITE)
        }
      }
    }

    // Schedule subtle initial auto-fade for unobstructed viewing
    fadeHandler.postDelayed(fadeOutRunnable, 3000L)
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
    googleMap3D.setMapMode(Map3DMode.ROADMAP)
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

