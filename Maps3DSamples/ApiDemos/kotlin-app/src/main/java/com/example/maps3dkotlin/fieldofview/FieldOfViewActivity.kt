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

package com.example.maps3dkotlin.fieldofview

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.example.maps3d.common.toValidCamera
import com.example.maps3dcommon.R
import com.example.maps3dkotlin.sampleactivity.SampleBaseActivity
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import kotlin.math.abs
import kotlin.math.tan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * =================================================================================================
 * Field of View (FOV) Perspective Scaling (Kotlin)
 * =================================================================================================
 *
 * This sample demonstrates perspective Field of View (FOV) scaling and optical dolly-zoom simulation
 * using the Google Maps 3D SDK.
 *
 * Key Concepts Demonstrated:
 * 1. Perspective Field of View (FOV) Adjustments:
 *    - Adjusts camera range proportionally using trigonometric perspective projection math to
 *      simulate optical lens focal length changes (Telephoto, Standard, Wide, Ultra-Wide).
 *    - Formula: range = baseRange * (tan(baseFov / 2) / tan(targetFov / 2)).
 *
 * 2. Seamless Camera State Preservation:
 *    - Inspects the active live camera before adjusting perspective, ensuring custom panning,
 *      heading rotations, and tilt angles applied by user gestures are smoothly retained.
 *
 * 3. Modern Material UI & Collapse Affordances:
 *    - Features a bottom control card with dynamic FOV angle readout and quick preset buttons.
 *    - Supports expanding and collapsing the card header to maximize the visible 3D map scene.
 *    - Implements subtle UI idle auto-fade with touch-to-wake responsiveness.
 */
class FieldOfViewActivity : SampleBaseActivity() {

  override val TAG = "FieldOfViewActivity"

  override val initialCamera: Camera
    get() = camera {
      center = latLngAltitude {
        latitude = SF_FINANCIAL_DISTRICT.latitude
        longitude = SF_FINANCIAL_DISTRICT.longitude
        altitude = 150.0
      }
      heading = 45.0
      tilt = 65.0
      roll = 0.0
      range = BASE_RANGE_METERS
    }.toValidCamera()

  // --- UI Elements ---

  private var controlsCard: CardView? = null
  private var cardHeader: View? = null
  private var cardContent: View? = null
  private var btnCollapse: MaterialButton? = null
  private lateinit var fovSliderLabel: TextView
  private lateinit var fovSlider: Slider

  // --- State Variables ---

  private var currentFov = 45.0
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

    // Inflate FOV control panel overlay into map container managed by SampleBaseActivity
    findViewById<ViewGroup>(R.id.map_container)?.let { container ->
      layoutInflater.inflate(R.layout.control_panel_field_of_view, container, true)
    }

    findViewById<MaterialToolbar>(R.id.top_bar)?.apply {
      setTitle(R.string.feature_title_field_of_view)
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

    fovSliderLabel = findViewById(R.id.fov_slider_label)
    fovSlider = findViewById(R.id.fov_slider)

    btnCollapse?.setOnClickListener {
      if (isCollapsed) expandControls() else collapseControls()
    }

    cardHeader?.setOnClickListener {
      if (isCollapsed) expandControls() else collapseControls()
    }

    fovSlider.addOnChangeListener { _, value, _ ->
      updateFov(value.toDouble())
    }

    findViewById<Button>(R.id.btn_fov_telephoto)?.setOnClickListener {
      fovSlider.value = 20.0f
    }

    findViewById<Button>(R.id.btn_fov_standard)?.setOnClickListener {
      fovSlider.value = 45.0f
    }

    findViewById<Button>(R.id.btn_fov_wide)?.setOnClickListener {
      fovSlider.value = 90.0f
    }

    findViewById<Button>(R.id.btn_fov_ultrawide)?.setOnClickListener {
      fovSlider.value = 120.0f
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

    android.transition.TransitionManager.beginDelayedTransition(card)
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

    android.transition.TransitionManager.beginDelayedTransition(card)
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

  // --- 3D Map Setup & Perspective Scaling Engine ---

  override fun onMapReady(googleMap3D: GoogleMap3D) {
    super.onMapReady(googleMap3D)
    lifecycleScope.launch(Dispatchers.Main) {
      updateFov(currentFov)
    }
  }

  /**
   * Calculates and applies the optical dolly-zoom perspective range for the specified FOV angle.
   *
   * @param fovAngle Perspective field of view angle in degrees (e.g. 15° to 120°).
   */
  private fun updateFov(fovAngle: Double) {
    currentFov = fovAngle
    runOnUiThread {
      fovSliderLabel.text = getString(R.string.field_of_view_format, fovAngle.toInt())
    }

    googleMap3D?.let { map ->
      val liveCam = map.getCamera()?.toValidCamera()
      val currCam =
        if (liveCam != null && (abs(liveCam.center.latitude) > 0.001 || abs(liveCam.center.longitude) > 0.001)) {
          liveCam
        } else {
          initialCamera
        }

      // Optical perspective transformation: range = baseRange * tan(baseFov/2) / tan(targetFov/2)
      val baseFovRad = Math.toRadians(BASE_FOV_DEGREES / 2.0)
      val targetFovRad = Math.toRadians(fovAngle / 2.0)
      val targetRange = (BASE_RANGE_METERS * tan(baseFovRad) / tan(targetFovRad))
        .coerceIn(MIN_RANGE_METERS, MAX_RANGE_METERS)

      val updatedCam = camera {
        center = currCam.center
        heading = currCam.heading
        tilt = currCam.tilt
        roll = currCam.roll
        range = targetRange
      }.toValidCamera()

      map.setCamera(updatedCam)
    }
  }

  override fun onDestroy() {
    fadeHandler.removeCallbacks(fadeOutRunnable)
    super.onDestroy()
  }

  companion object {
    /** Focal landmark centered near the San Francisco Transamerica Pyramid & Financial District. */
    val SF_FINANCIAL_DISTRICT = LatLng(37.7952, -122.4028)

    /** Baseline field of view angle in degrees (human eye / standard focal length). */
    private const val BASE_FOV_DEGREES = 45.0

    /** Baseline camera range in meters corresponding to the standard 45° FOV baseline. */
    private const val BASE_RANGE_METERS = 800.0

    /** Minimum optical range boundary in meters. */
    private const val MIN_RANGE_METERS = 150.0

    /** Maximum optical range boundary in meters. */
    private const val MAX_RANGE_METERS = 3000.0
  }
}

