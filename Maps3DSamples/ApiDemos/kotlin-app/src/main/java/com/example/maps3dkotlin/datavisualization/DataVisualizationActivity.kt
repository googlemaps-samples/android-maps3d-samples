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

package com.example.maps3dkotlin.datavisualization

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.transition.TransitionManager
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.example.maps3d.common.toValidCamera
import com.example.maps3dcommon.R
import com.example.maps3dkotlin.sampleactivity.SampleBaseActivity
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.Polygon
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.flyToOptions
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.gms.maps3d.model.polygonOptions
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * =================================================================================================
 * Data Visualization: 3D Extruded Flood Simulation (Kotlin)
 * =================================================================================================
 *
 * Showcases dynamic 3D volume extrusion in Google Maps 3D SDK by simulating elevated flood tides.
 */
class DataVisualizationActivity : SampleBaseActivity() {

  override val TAG = "DataVisualizationActivity"

  override val initialCamera: Camera = camera {
    center = latLngAltitude {
      latitude = SF_FLOOD_CENTER.latitude
      longitude = SF_FLOOD_CENTER.longitude
      altitude = 120.0
    }
    heading = 35.0
    tilt = 64.0
    range = 1200.0
  }.toValidCamera()

  private var controlsCard: CardView? = null
  private var cardHeader: View? = null
  private var cardContent: View? = null
  private var btnCollapse: MaterialButton? = null
  private lateinit var floodDepthLabel: TextView
  private lateinit var floodRiskBadge: TextView
  private lateinit var floodSlider: Slider
  private lateinit var btnAnimateFlood: MaterialButton

  private var floodPolygon: Polygon? = null
  private var currentFloodElevation = 10.0
  private var simulationJob: Job? = null
  private var isCollapsed = false

  private val fadeHandler = Handler(Looper.getMainLooper())
  private val fadeOutRunnable = Runnable {
    if (controlsCard != null && !isCollapsed) {
      controlsCard?.animate()
        ?.alpha(0.85f)
        ?.setDuration(400)
        ?.start()
    }
  }

  private val waterFillColor = Color.argb(140, 230, 40, 40)
  private val waterStrokeColor = Color.argb(255, 180, 0, 0)
  private val waterStrokeWidth = 2.5

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    findViewById<View>(R.id.control_scroll_view)?.visibility = View.GONE

    findViewById<ViewGroup>(R.id.map_container)?.let { container ->
      layoutInflater.inflate(R.layout.control_panel_data_visualization, container, true)
    }

    findViewById<MaterialToolbar>(R.id.top_bar)?.apply {
      title = getString(R.string.feature_title_data_visualization)
      setNavigationOnClickListener { finish() }
    }

    initViews()
    updateControlLabels(currentFloodElevation)
  }

  private fun initViews() {
    controlsCard = findViewById(R.id.control_panel)
    cardHeader = findViewById(R.id.card_header)
    cardContent = findViewById(R.id.card_content)
    btnCollapse = findViewById(R.id.btn_collapse)

    floodDepthLabel = findViewById(R.id.tv_flood_depth_label)
    floodRiskBadge = findViewById(R.id.tv_flood_risk_badge)
    floodSlider = findViewById(R.id.flood_slider)
    btnAnimateFlood = findViewById(R.id.btn_animate_flood)

    btnCollapse?.setOnClickListener {
      if (isCollapsed) {
        expandControls()
      } else {
        collapseControls()
      }
    }

    cardHeader?.setOnClickListener {
      if (isCollapsed) {
        expandControls()
      } else {
        collapseControls()
      }
    }

    floodSlider.addOnChangeListener { _, value, fromUser ->
      if (fromUser) {
        stopSimulation()
      }
      updateFloodElevation(value.toDouble())
    }

    btnAnimateFlood.setOnClickListener {
      if (simulationJob != null) {
        stopSimulation()
      } else {
        startSimulation()
      }
    }

    fadeHandler.postDelayed(fadeOutRunnable, 3000L)
  }

  private fun collapseControls() {
    val card = controlsCard ?: return
    val content = cardContent ?: return
    if (isCollapsed) return
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
    if (!isCollapsed) return
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
      if (controlsCard != null && !isCollapsed) {
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

  override fun onMapReady(googleMap3D: GoogleMap3D) {
    super.onMapReady(googleMap3D)
    googleMap3D.setMapMode(Map3DMode.HYBRID)
    googleMap3D.flyCameraTo(
      flyToOptions {
        endCamera = initialCamera
        durationInMillis = 1200
      }
    )
    lifecycleScope.launch(Dispatchers.Main) {
      updateFloodElevation(currentFloodElevation)
    }
  }

  fun updateFloodElevation(currentFloodHeightMeters: Double) {
    currentFloodElevation = currentFloodHeightMeters

    runOnUiThread {
      updateControlLabels(currentFloodHeightMeters)

      val map = googleMap3D ?: return@runOnUiThread

      val path: List<LatLngAltitude> = floodZoneCoords.map { coord ->
        latLngAltitude {
          latitude = coord.first
          longitude = coord.second
          altitude = currentFloodHeightMeters
        }
      }

      // Volumetric 3D Polygon Extrusion Technique:
      // 1. AltitudeMode.ABSOLUTE: Water elevation represents true Mean Sea Level (MSL).
      //    Unlike RELATIVE_TO_GROUND, ABSOLUTE ensures a flat, uniform horizontal water plane.
      // 2. extruded = true: Instructs the 3D rendering engine to drop vertical skirt walls
      //    from the polygon vertices down to the ground terrain mesh, forming a 3D volumetric water body.
      // 3. id = POLYGON_ID: Re-using a stable ID upserts the existing polygon in place,
      //    eliminating render flickering during rapid slider or animation updates.
      val options = polygonOptions {
        id = POLYGON_ID
        this.path = path
        fillColor = waterFillColor
        strokeColor = waterStrokeColor
        strokeWidth = waterStrokeWidth
        altitudeMode = AltitudeMode.ABSOLUTE
        extruded = true
        drawsOccludedSegments = true
        geodesic = false
      }

      floodPolygon = map.addPolygon(options).apply {
        setClickListener {
          runOnUiThread {
            Toast.makeText(
              this@DataVisualizationActivity,
              getString(R.string.flood_toast_format, currentFloodElevation),
              Toast.LENGTH_SHORT
            ).show()
          }
        }
      }
    }
  }

  private fun updateControlLabels(currentFloodHeightMeters: Double) {
    val feet = currentFloodHeightMeters * 3.28084
    floodDepthLabel.text =
      getString(R.string.flood_elevation_format, currentFloodHeightMeters, feet)

    when {
      currentFloodHeightMeters <= 2.0 -> {
        floodRiskBadge.setText(R.string.flood_risk_baseline)
        floodRiskBadge.setTextColor(Color.parseColor("#008800"))
        floodRiskBadge.setBackgroundColor(Color.parseColor("#2000AA00"))
      }

      currentFloodHeightMeters <= 8.0 -> {
        floodRiskBadge.setText(R.string.flood_risk_minor)
        floodRiskBadge.setTextColor(Color.parseColor("#BB7700"))
        floodRiskBadge.setBackgroundColor(Color.parseColor("#20FFAA00"))
      }

      currentFloodHeightMeters <= 20.0 -> {
        floodRiskBadge.setText(R.string.flood_risk_moderate)
        floodRiskBadge.setTextColor(Color.parseColor("#0077CC"))
        floodRiskBadge.setBackgroundColor(Color.parseColor("#200088FF"))
      }

      currentFloodHeightMeters <= 35.0 -> {
        floodRiskBadge.setText(R.string.flood_risk_storm_surge)
        floodRiskBadge.setTextColor(Color.parseColor("#DD4400"))
        floodRiskBadge.setBackgroundColor(Color.parseColor("#25FF5500"))
      }

      else -> {
        floodRiskBadge.setText(R.string.flood_risk_extreme)
        floodRiskBadge.setTextColor(Color.parseColor("#CC0000"))
        floodRiskBadge.setBackgroundColor(Color.parseColor("#25FF0000"))
      }
    }
  }

  private fun startSimulation() {
    val maxVal = floodSlider.valueTo.toDouble()
    val minVal = floodSlider.valueFrom.toDouble()
    if (currentFloodElevation >= maxVal) {
      floodSlider.value = minVal.toFloat()
      updateFloodElevation(minVal)
    }

    btnAnimateFlood.setText(R.string.stop_simulation)
    simulationJob = lifecycleScope.launch {
      while (isActive) {
        val currentMax = floodSlider.valueTo.toDouble()
        var newElevation = currentFloodElevation + 0.2
        newElevation = Math.round(newElevation * 10.0) / 10.0
        floodSlider.value = newElevation.toFloat()
        if (newElevation >= currentMax) {
          stopSimulation()
          break
        }
        delay(20.milliseconds)
      }
    }
  }

  private fun stopSimulation() {
    simulationJob?.cancel()
    simulationJob = null
    btnAnimateFlood.setText(R.string.start_simulation)
  }

  override fun onPause() {
    super.onPause()
    stopSimulation()
    fadeHandler.removeCallbacks(fadeOutRunnable)
  }

  override fun onDestroy() {
    stopSimulation()
    fadeHandler.removeCallbacks(fadeOutRunnable)
    floodPolygon?.remove()
    floodPolygon = null
    super.onDestroy()
  }

  companion object {
    private const val POLYGON_ID = "flood_zone_polygon"

    val SF_FLOOD_CENTER = LatLng(37.8025, -122.4030)

    val floodZoneCoords = listOf(
      Pair(37.805156, -122.403256),
      Pair(37.803370, -122.401287),
      Pair(37.799222, -122.405080),
      Pair(37.797500, -122.408000),
      Pair(37.801000, -122.411000),
      Pair(37.805156, -122.403256)
    )
  }
}

