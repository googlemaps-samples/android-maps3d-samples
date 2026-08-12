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

import android.graphics.Color
import android.os.Bundle
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.example.maps3d.common.toHeading
import com.example.maps3dcommon.R
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.Map3DView
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.Polyline
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.gms.maps3d.model.polylineOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import com.google.maps.android.SphericalUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Advanced sample demonstrating ground-level path following in Kotlin.
 *
 * Features:
 * - Urban vs Rural ground-level paths
 * - Real-time camera controls via sliders: Range, Ground Altitude, Heading Offset, Tilt, Follow Speed
 * - Smooth frame-by-frame animation along the route
 */
class PathFollowingActivity : AppCompatActivity(), OnMap3DViewReadyCallback {

  private lateinit var map3DView: Map3DView
  private var googleMap3D: GoogleMap3D? = null

  // View Bindings
  private lateinit var rgEnvironment: RadioGroup
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

  // Control parameters
  private var cameraRange = 300.0
  private var groundAltitude = 20.0
  private var headingOffset = 0.0
  private var cameraTilt = 70.0
  private var followSpeedMps = 30.0

  // Path state
  private var currentPath: List<LatLng> = URBAN_PATH
  private var cumulativeDistances: DoubleArray = doubleArrayOf()
  private var totalDistance: Double = 0.0
  private var elapsedDistance: Double = 0.0
  private var isPlaying = false
  private var isUserScrubbing = false

  private var pathPolyline: Polyline? = null
  private var animationJob: Job? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_path_following)

    map3DView = findViewById(R.id.map3dView)
    map3DView.onCreate(savedInstanceState)
    map3DView.getMap3DViewAsync(this)

    initViews()
    loadPathData(URBAN_PATH)
  }

  override fun onResume() {
    super.onResume()
    map3DView.onResume()
  }

  override fun onPause() {
    super.onPause()
    map3DView.onPause()
    pauseAnimation()
  }

  override fun onDestroy() {
    super.onDestroy()
    pauseAnimation()
    pathPolyline?.remove()
    pathPolyline = null
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

  override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
    this.googleMap3D = googleMap3D
    googleMap3D.setOnMapReadyListener {
      googleMap3D.setOnMapReadyListener(null)
      drawPathPolyline()
      updateCameraPositionForDistance(0.0)
    }
  }

  private fun initViews() {
    rgEnvironment = findViewById(R.id.rg_environment)
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

    // Radio group environment listener
    rgEnvironment.setOnCheckedChangeListener { _, checkedId ->
      when (checkedId) {
        R.id.rb_urban -> {
          altitudeSlider.valueTo = 200.0f
          switchEnvironment(URBAN_PATH)
        }

        R.id.rb_rural -> {
          altitudeSlider.valueTo = 2000.0f
          switchEnvironment(RURAL_PATH)
        }
      }
    }

    // Play/Pause button
    btnPlayPause.setOnClickListener {
      if (isPlaying) {
        pauseAnimation()
      } else {
        startAnimation()
      }
    }

    // Progress slider scrubbing
    progressSlider.addOnChangeListener { _, value, fromUser ->
      if (fromUser) {
        isUserScrubbing = true
        elapsedDistance = totalDistance * value.toDouble()
        updateCameraPositionForDistance(elapsedDistance)
      }
    }
    progressSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
      override fun onStartTrackingTouch(slider: Slider) {
        isUserScrubbing = true
      }

      override fun onStopTrackingTouch(slider: Slider) {
        isUserScrubbing = false
      }
    })

    // Slider listeners for interactive camera inputs
    rangeSlider.addOnChangeListener { _, value, _ ->
      cameraRange = value.toDouble()
      rangeSliderLabel.text = "Camera Range: ${cameraRange.toInt()}m"
      updateCameraPositionForDistance(elapsedDistance)
    }

    altitudeSlider.addOnChangeListener { _, value, _ ->
      groundAltitude = value.toDouble()
      altitudeSliderLabel.text = "Ground Altitude: ${groundAltitude.toInt()}m"
      updateCameraPositionForDistance(elapsedDistance)
    }

    headingSlider.addOnChangeListener { _, value, _ ->
      headingOffset = value.toDouble()
      headingSliderLabel.text = "Heading Offset: ${headingOffset.toInt()}°"
      updateCameraPositionForDistance(elapsedDistance)
    }

    tiltSlider.addOnChangeListener { _, value, _ ->
      cameraTilt = value.toDouble()
      tiltSliderLabel.text = "Camera Tilt: ${cameraTilt.toInt()}°"
      updateCameraPositionForDistance(elapsedDistance)
    }

    speedSlider.addOnChangeListener { _, value, _ ->
      followSpeedMps = value.toDouble()
      speedSliderLabel.text = "Follow Speed: ${followSpeedMps.toInt()} m/s"
    }
  }


  private fun switchEnvironment(path: List<LatLng>) {
    pauseAnimation()
    currentHeading = null
    elapsedDistance = 0.0
    progressSlider.value = 0f

    if (path == RURAL_PATH) {
      cameraRange = 450.0
      groundAltitude = 40.0
      cameraTilt = 75.0
      rangeSlider.value = 450f
      altitudeSlider.value = 40f
      tiltSlider.value = 75f
      rangeSliderLabel.text = "Camera Range: 450m"
      altitudeSliderLabel.text = "Ground Altitude: 40m"
      tiltSliderLabel.text = "Camera Tilt: 75°"
    } else {
      cameraRange = 300.0
      groundAltitude = 20.0
      cameraTilt = 70.0
      rangeSlider.value = 300f
      altitudeSlider.value = 20f
      tiltSlider.value = 70f
      rangeSliderLabel.text = "Camera Range: 300m"
      altitudeSliderLabel.text = "Ground Altitude: 20m"
      tiltSliderLabel.text = "Camera Tilt: 70°"
    }

    loadPathData(path)
    drawPathPolyline()
    updateCameraPositionForDistance(0.0)
  }

  private fun loadPathData(path: List<LatLng>) {
    currentPath = path
    cumulativeDistances = DoubleArray(path.size)
    totalDistance = 0.0
    cumulativeDistances[0] = 0.0
    for (i in 1 until path.size) {
      val dist = SphericalUtil.computeDistanceBetween(path[i - 1], path[i])
      totalDistance += dist
      cumulativeDistances[i] = totalDistance
    }
  }

  private fun drawPathPolyline() {
    val map = googleMap3D ?: return
    pathPolyline?.remove()
    val polyOptions = polylineOptions {
      strokeColor = Color.parseColor("#4285F4")
      strokeWidth = 10.0
      altitudeMode = AltitudeMode.RELATIVE_TO_GROUND
      path = currentPath.map { latLng ->
        latLngAltitude {
          latitude = latLng.latitude
          longitude = latLng.longitude
          altitude = 5.0
        }
      }
    }
    pathPolyline = map.addPolyline(polyOptions)
  }

  private fun startAnimation() {
    if (isPlaying) return
    isPlaying = true
    btnPlayPause.setIconResource(R.drawable.pause_24px)

    animationJob = lifecycleScope.launch(Dispatchers.Default) {
      val frameDurationMs = 16L
      while (isPlaying) {
        val stepDistance = followSpeedMps * (frameDurationMs / 1000.0)
        elapsedDistance += stepDistance

        if (elapsedDistance >= totalDistance) {
          elapsedDistance = 0.0
        }

        lifecycleScope.launch(Dispatchers.Main) {
          if (!isUserScrubbing) {
            progressSlider.value = (elapsedDistance / totalDistance).toFloat().coerceIn(0f, 1f)
          }
          updateCameraPositionForDistance(elapsedDistance)
        }

        delay(frameDurationMs)
      }
    }
  }

  private fun pauseAnimation() {
    isPlaying = false
    btnPlayPause.setIconResource(R.drawable.play_arrow_24px)
    animationJob?.cancel()
    animationJob = null
  }

  private var currentHeading: Double? = null

  private fun updateCameraPositionForDistance(dist: Double) {
    val map = googleMap3D ?: return
    if (currentPath.isEmpty()) return

    var index = 0
    while (index < cumulativeDistances.size - 1 && cumulativeDistances[index + 1] < dist) {
      index++
    }

    val p1 = currentPath[index]
    val p2 = if (index < currentPath.size - 1) currentPath[index + 1] else p1

    val segStartDist = cumulativeDistances[index]
    val segEndDist =
      if (index < cumulativeDistances.size - 1) cumulativeDistances[index + 1] else totalDistance
    val segLen = segEndDist - segStartDist

    val fraction = if (segLen > 0) ((dist - segStartDist) / segLen).coerceIn(0.0, 1.0) else 0.0
    val currentLatLng = SphericalUtil.interpolate(p1, p2, fraction)
    val bearing = SphericalUtil.computeHeading(p1, p2)

    val targetHeadingRaw = (bearing + headingOffset).toHeading()
    val targetHeading = if (currentHeading == null || isUserScrubbing || !isPlaying) {
      targetHeadingRaw
    } else {
      var diff = (targetHeadingRaw - currentHeading!!) % 360.0
      if (diff > 180.0) diff -= 360.0
      if (diff < -180.0) diff += 360.0
      (currentHeading!! + diff * 0.12).toHeading()
    }
    currentHeading = targetHeading

    val newCamera = camera {
      center = latLngAltitude {
        latitude = currentLatLng.latitude
        longitude = currentLatLng.longitude
        altitude = groundAltitude
      }
      heading = targetHeading
      tilt = cameraTilt
      range = cameraRange
    }

    map.setCamera(newCamera)
  }

  companion object {
    // Urban Path (New York City - Central Park Block Circuit)
    val URBAN_PATH = listOf(
      LatLng(40.7783119, -73.9627630),
      LatLng(40.7776355, -73.9611664),
      LatLng(40.7770011, -73.9616294),
      LatLng(40.7776743, -73.9632228),
      LatLng(40.7783119, -73.9627630)
    )

    // Rural Path
    val RURAL_PATH = listOf(
      LatLng(37.254529, -122.380897),
      LatLng(37.255065, -122.381627),
      LatLng(37.257540, -122.383720),
      LatLng(37.261200, -122.383950),
      LatLng(37.264780, -122.388210),
      LatLng(37.268520, -122.392450),
      LatLng(37.272110, -122.397640),
      LatLng(37.276430, -122.401120),
      LatLng(37.280850, -122.403560),
      LatLng(37.286018, -122.405072),
      LatLng(37.291040, -122.404210),
      LatLng(37.295800, -122.401980),
      LatLng(37.300120, -122.399540),
      LatLng(37.304550, -122.397210),
      LatLng(37.309200, -122.395100),
      LatLng(37.313450, -122.392840),
      LatLng(37.317200, -122.390510),
      LatLng(37.320850, -122.388740),
      LatLng(37.323540, -122.387600),
      LatLng(37.325269, -122.386728)
    )
  }
}
