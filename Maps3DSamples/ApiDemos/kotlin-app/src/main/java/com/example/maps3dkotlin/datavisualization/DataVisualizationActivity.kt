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
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
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
import com.google.android.material.slider.Slider
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
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

  private lateinit var floodDepthLabel: TextView
  private lateinit var floodRiskBadge: TextView
  private lateinit var floodSlider: Slider
  private lateinit var btnAnimateFlood: Button

  private var floodPolygon: Polygon? = null
  private var currentFloodElevation = 10.0
  private var simulationJob: Job? = null

  private val waterFillColor = Color.argb(140, 230, 40, 40)
  private val waterStrokeColor = Color.argb(255, 180, 0, 0)
  private val waterStrokeWidth = 2.5

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    findViewById<ViewGroup>(R.id.map_container)?.let { container ->
      layoutInflater.inflate(R.layout.control_panel_data_visualization, container, true)
    }

    findViewById<MaterialToolbar>(R.id.top_bar)?.apply {
      title = getString(R.string.feature_title_data_visualization)
      setNavigationOnClickListener { finish() }
    }

    floodDepthLabel = findViewById(R.id.tv_flood_depth_label)
    floodRiskBadge = findViewById(R.id.tv_flood_risk_badge)
    floodSlider = findViewById(R.id.flood_slider)
    btnAnimateFlood = findViewById(R.id.btn_animate_flood)

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
      val feet = currentFloodHeightMeters * 3.28084
      floodDepthLabel.text =
        String.format("Flood Elevation: +%.1f m (%.1f ft)", currentFloodHeightMeters, feet)

      when {
        currentFloodHeightMeters <= 2.0 -> {
          floodRiskBadge.text = "🌊 Baseline Tide"
          floodRiskBadge.setTextColor(Color.parseColor("#008800"))
          floodRiskBadge.setBackgroundColor(Color.parseColor("#2000AA00"))
        }

        currentFloodHeightMeters <= 8.0 -> {
          floodRiskBadge.text = "⚠️ Minor Inundation"
          floodRiskBadge.setTextColor(Color.parseColor("#BB7700"))
          floodRiskBadge.setBackgroundColor(Color.parseColor("#20FFAA00"))
        }

        currentFloodHeightMeters <= 20.0 -> {
          floodRiskBadge.text = "🌊 Moderate Flooding"
          floodRiskBadge.setTextColor(Color.parseColor("#0077CC"))
          floodRiskBadge.setBackgroundColor(Color.parseColor("#200088FF"))
        }

        currentFloodHeightMeters <= 35.0 -> {
          floodRiskBadge.text = "🚨 Storm Surge (Cat 3)"
          floodRiskBadge.setTextColor(Color.parseColor("#DD4400"))
          floodRiskBadge.setBackgroundColor(Color.parseColor("#25FF5500"))
        }

        else -> {
          floodRiskBadge.text = "⛔ Extreme Inundation"
          floodRiskBadge.setTextColor(Color.parseColor("#CC0000"))
          floodRiskBadge.setBackgroundColor(Color.parseColor("#25FF0000"))
        }
      }

      val map = googleMap3D ?: return@runOnUiThread

      val path: List<LatLngAltitude> = floodZoneCoords.map { coord ->
        latLngAltitude {
          latitude = coord.first
          longitude = coord.second
          altitude = currentFloodHeightMeters
        }
      }

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

      floodPolygon = map.addPolygon(options)?.apply {
        setClickListener {
          runOnUiThread {
            Toast.makeText(
              this@DataVisualizationActivity,
              String.format(
                "San Francisco Waterfront - Water Level: +%.1f m",
                currentFloodElevation
              ),
              Toast.LENGTH_SHORT
            ).show()
          }
        }
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

    btnAnimateFlood.text = "⏹ Stop Simulation"
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
    btnAnimateFlood.text = "▶ Start Simulation"
  }

  override fun onDestroy() {
    stopSimulation()
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
