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

package com.example.maps3dkotlin.routes

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.transition.TransitionManager
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.example.maps3d.common.OahuRouteData
import com.example.maps3d.common.RouteEngine
import com.example.maps3d.common.toHeading
import com.example.maps3d.common.toValidCamera
import com.example.maps3dcommon.R
import com.example.maps3dkotlin.BuildConfig
import com.example.maps3dkotlin.sampleactivity.SampleBaseActivity
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.Model
import com.google.android.gms.maps3d.model.Polyline
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.gms.maps3d.model.modelOptions
import com.google.android.gms.maps3d.model.orientation
import com.google.android.gms.maps3d.model.polylineOptions
import com.google.android.gms.maps3d.model.vector3D
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Demonstrates cross-product integration between Google Maps Platform Routes API and GoogleMap3D.
 *
 * <p>Key Architecture Highlights:
 * <ul>
 *   <li><b>Async Route Calculation:</b> Dispatches background tasks via {@link RouteRepository} to
 *       fetch driving directions in Honolulu, falling back gracefully to bundled Oahu coordinates
 *       when offline or unauthenticated.</li>
 *   <li><b>3D Polyline Visualization:</b> Renders clamped-to-ground 3D route paths that conform
 *       seamlessly to elevation and terrain variations.</li>
 *   <li><b>3D glTF Model Anchoring:</b> Loads a 3D vehicle model and updates its geographic
 *       coordinates and heading on each tick along the path.</li>
 *   <li><b>Dynamic Camera Tracking:</b> Synchronizes the 3D camera to follow behind the vehicle
 *       with configurable altitude, speed, and yaw offsets.</li>
 *   <li><b>Ergonomic Collapsible UI:</b> Employs an animated Material3 floating card with
 *       touch-aware auto-fade after 3 seconds of inactivity.</li>
 * </ul>
 */
class RoutesActivity : SampleBaseActivity() {

  companion object {
    private const val FADE_DELAY_MS = 3000L
    private const val ACTIVE_ALPHA = 1.0f
    private const val FADED_ALPHA = 0.85f
  }

  override val TAG = "RoutesActivity"

  // Honolulu Overview starting camera
  override val initialCamera: Camera
    get() =
      camera {
        center = latLngAltitude {
          latitude = 21.348567
          longitude = -157.803961
          altitude = 0.0
        }
        heading = 38.6
        tilt = 45.0
        range = 20000.0
      }.toValidCamera()

  // --- View References ---

  private var controlsCard: CardView? = null
  private var cardHeader: View? = null
  private var cardContent: View? = null
  private var btnCollapse: MaterialButton? = null

  private var btnPlayPause: MaterialButton? = null
  private var progressSlider: Slider? = null
  private var rangeSlider: Slider? = null
  private var rangeSliderLabel: TextView? = null
  private var speedSlider: Slider? = null
  private var speedSliderLabel: TextView? = null
  private var headingSlider: Slider? = null
  private var headingSliderLabel: TextView? = null

  // --- State Variables ---

  private val routeRepository = RouteRepository()
  private var decodedRoute: List<LatLng> = emptyList()
  private var cumulativeDistances: DoubleArray = doubleArrayOf(0.0)
  private var totalDistance: Double = 0.0
  private var elapsedDistance: Double = 0.0

  private var isPlaying = false
  private var isUserScrubbing = false
  private var isCollapsed = false

  // --- Slider Parameters ---

  private var cameraRange = 1500f // Range: 200m to 5000m
  private var vehicleSpeedMps = 150f // Range: 10m/s to 500m/s
  private var yawOffset = 0f // Range: -180° to 180°

  // --- Map References ---

  private var routePolyline: Polyline? = null
  private var vehicleModel: Model? = null

  // --- Background Jobs & Handlers ---

  private var animationJob: Job? = null
  private val fadeHandler = Handler(Looper.getMainLooper())
  private val fadeOutRunnable = Runnable {
    if (!isCollapsed) {
      controlsCard?.animate()?.alpha(FADED_ALPHA)?.setDuration(400)?.start()
    }
  }

  // --- Lifecycle & Layout Setup ---

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Hide base pill scroll view as this activity manages its own overlay card
    findViewById<View>(R.id.control_scroll_view)?.visibility = View.GONE

    findViewById<ViewGroup>(R.id.map_container)?.let { container ->
      layoutInflater.inflate(R.layout.control_panel_routes, container, true)
    }

    findViewById<MaterialToolbar>(R.id.top_bar)?.apply {
      setTitle(R.string.feature_title_routes_api)
      setNavigationOnClickListener { finish() }
    }

    initViews()
  }

  @SuppressLint("ClickableViewAccessibility")
  private fun initViews() {
    controlsCard = findViewById(R.id.control_panel)
    cardHeader = findViewById(R.id.card_header)
    cardContent = findViewById(R.id.card_content)
    btnCollapse = findViewById(R.id.btn_collapse)

    btnPlayPause = findViewById(R.id.btn_play_pause)
    progressSlider = findViewById(R.id.progress_slider)
    rangeSlider = findViewById(R.id.range_slider)
    rangeSliderLabel = findViewById(R.id.range_slider_label)
    speedSlider = findViewById(R.id.speed_slider)
    speedSliderLabel = findViewById(R.id.speed_slider_label)
    headingSlider = findViewById(R.id.heading_slider)
    headingSliderLabel = findViewById(R.id.heading_slider_label)

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
      }
    }

    controlsCard?.setOnTouchListener { _, event ->
      if (event.action == MotionEvent.ACTION_DOWN) {
        resetFadeTimer()
      }
      false
    }

    setupControls()
    resetFadeTimer()
  }

  private fun setupControls() {
    btnPlayPause?.setOnClickListener {
      resetFadeTimer()
      if (decodedRoute.isEmpty()) {
        Toast.makeText(this, R.string.route_loading, Toast.LENGTH_SHORT).show()
        return@setOnClickListener
      }
      togglePlayback(!isPlaying)
    }

    progressSlider?.apply {
      addOnSliderTouchListener(
          object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
              resetFadeTimer()
              isUserScrubbing = true
            }

            override fun onStopTrackingTouch(slider: Slider) {
              resetFadeTimer()
              isUserScrubbing = false
              elapsedDistance = totalDistance * slider.value.toDouble()
              updateVehiclePositionAndCamera()
            }
          })

      addOnChangeListener { _, value, fromUser ->
        if (fromUser && isUserScrubbing) {
          resetFadeTimer()
          elapsedDistance = totalDistance * value.toDouble()
          updateVehiclePositionAndCamera()
        }
      }
    }

    rangeSliderLabel?.text = getString(R.string.camera_altitude_format, cameraRange.toInt())
    rangeSlider?.apply {
      value = cameraRange
      addOnChangeListener { _, value, _ ->
        resetFadeTimer()
        cameraRange = value
        rangeSliderLabel?.text = getString(R.string.camera_altitude_format, value.toInt())
        updateVehiclePositionAndCamera()
      }
    }

    speedSliderLabel?.text = getString(R.string.vehicle_speed_format, vehicleSpeedMps.toInt())
    speedSlider?.apply {
      value = vehicleSpeedMps
      addOnChangeListener { _, value, _ ->
        resetFadeTimer()
        vehicleSpeedMps = value
        speedSliderLabel?.text = getString(R.string.vehicle_speed_format, value.toInt())
      }
    }

    headingSliderLabel?.text = getString(R.string.camera_yaw_offset_format, yawOffset.toInt())
    headingSlider?.apply {
      value = yawOffset
      addOnChangeListener { _, value, _ ->
        resetFadeTimer()
        yawOffset = value
        headingSliderLabel?.text = getString(R.string.camera_yaw_offset_format, value.toInt())
        updateVehiclePositionAndCamera()
      }
    }
  }

  // --- Collapsible UI Transitions ---

  private fun collapseControls() {
    val card = controlsCard ?: return
    val content = cardContent ?: return
    if (isCollapsed) return
    isCollapsed = true
    fadeHandler.removeCallbacks(fadeOutRunnable)
    card.animate().alpha(ACTIVE_ALPHA).setDuration(150).start()
    TransitionManager.beginDelayedTransition(card)
    content.visibility = View.GONE
    btnCollapse?.apply {
      setIconResource(R.drawable.expand_less_24px)
      contentDescription = getString(R.string.expand_controls)
    }
  }

  private fun expandControls() {
    val card = controlsCard ?: return
    val content = cardContent ?: return
    if (!isCollapsed) return
    isCollapsed = false
    TransitionManager.beginDelayedTransition(card)
    content.visibility = View.VISIBLE
    btnCollapse?.apply {
      setIconResource(R.drawable.expand_more_24px)
      contentDescription = getString(R.string.collapse_controls)
    }
    resetFadeTimer()
  }

  private fun resetFadeTimer() {
    controlsCard?.animate()?.alpha(ACTIVE_ALPHA)?.setDuration(150)?.start()
    fadeHandler.removeCallbacks(fadeOutRunnable)
    if (!isCollapsed) {
      fadeHandler.postDelayed(fadeOutRunnable, FADE_DELAY_MS)
    }
  }

  private fun togglePlayback(play: Boolean) {
    isPlaying = play
    btnPlayPause?.apply {
      if (play) {
        setIconResource(R.drawable.pause_24px)
        startAnimationLoop()
      } else {
        setIconResource(R.drawable.play_arrow_24px)
        stopAnimationLoop()
      }
    }
  }

  // --- Map Readiness & Route Rendering ---

  override fun onMapReady(googleMap3D: GoogleMap3D) {
    super.onMapReady(googleMap3D)
    googleMap3D.setMapMode(Map3DMode.SATELLITE)

    lifecycleScope.launch(Dispatchers.Default) { loadAndRenderRoute(googleMap3D) }
  }

  private suspend fun loadAndRenderRoute(googleMap3D: GoogleMap3D) {
    val apiKey = BuildConfig.MAPS3D_API_KEY
    val origin = LatLng(21.307043, -157.858984)
    val destination = LatLng(21.390177, -157.719454)
    var decoded: List<LatLng>

    try {
      if (apiKey.isEmpty() || apiKey.contains("YOUR_API_KEY")) {
        throw Exception("Invalid or missing API Key")
      }
      val routeData = routeRepository.fetchRoute(apiKey, origin, destination)
      decoded = PolyUtil.decode(routeData.encodedPolyline)
    } catch (e: Exception) {
      Log.w(
          TAG,
          "Routes API fetch failed: ${e.localizedMessage}. Falling back to pre-baked Oahu mountain route.")
      decoded = OahuRouteData.FALLBACK_ROUTE
      withContext(Dispatchers.Main) {
        Toast.makeText(this@RoutesActivity, R.string.routes_offline_fallback, Toast.LENGTH_LONG)
            .show()
      }
    }

    withContext(Dispatchers.Main) {
      decodedRoute = decoded
      cumulativeDistances = RouteEngine.calculateCumulativeDistances(decoded)
      totalDistance = cumulativeDistances.last()

      // 1. Draw the blue Polyline representational trail
      routePolyline =
          googleMap3D.addPolyline(
              polylineOptions {
                path =
                    decoded.map {
                      latLngAltitude {
                        latitude = it.latitude
                        longitude = it.longitude
                        altitude = 0.0
                      }
                    }
                strokeColor = Color.BLUE
                strokeWidth = 10.0
                altitudeMode = AltitudeMode.CLAMP_TO_GROUND
                zIndex = 5
              })

      // 2. Place the 3D model of the Red Car at starting coordinate
      vehicleModel =
          googleMap3D.addModel(
              modelOptions {
                id = "vehicle_car"
                position = latLngAltitude {
                  latitude = decoded.first().latitude
                  longitude = decoded.first().longitude
                  altitude = 25.0
                }
                altitudeMode = AltitudeMode.RELATIVE_TO_GROUND
                orientation = orientation {
                  heading = 0.0
                  tilt = -90.0
                  roll = 0.0
                }
                url = "https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/red_car.glb"
                scale = vector3D {
                  x = 50.0
                  y = 50.0
                  z = 50.0
                }
              })

      updateVehiclePositionAndCamera()
      togglePlayback(true)
    }
  }

  // --- Animation Tick Loop ---

  private fun startAnimationLoop() {
    animationJob =
        lifecycleScope.launch(Dispatchers.Main) {
          var lastTime = System.currentTimeMillis()
          while (isPlaying && totalDistance > 0.0) {
            val now = System.currentTimeMillis()
            val dt = (now - lastTime) / 1000.0 // Delta time in seconds
            lastTime = now

            elapsedDistance += vehicleSpeedMps * dt

            if (elapsedDistance >= totalDistance) {
              elapsedDistance = 0.0
            }

            if (!isUserScrubbing) {
              progressSlider?.value = (elapsedDistance / totalDistance).toFloat()
            }

            updateVehiclePositionAndCamera()
            delay(16)
          }
        }
  }

  private fun stopAnimationLoop() {
    animationJob?.cancel()
    animationJob = null
  }

  private fun updateVehiclePositionAndCamera() {
    val route = decodedRoute
    if (route.isEmpty() || totalDistance <= 0.0) return

    val posAndHeading =
        RouteEngine.calculatePositionAndHeading(
            route, cumulativeDistances, elapsedDistance, 30.0)

    googleMap3D?.let { map ->
      vehicleModel =
          map.addModel(
              modelOptions {
                id = "vehicle_car"
                position = latLngAltitude {
                  latitude = posAndHeading.position.latitude
                  longitude = posAndHeading.position.longitude
                  altitude = 25.0
                }
                altitudeMode = AltitudeMode.RELATIVE_TO_GROUND
                orientation = orientation {
                  heading = posAndHeading.heading.toDouble()
                  tilt = -90.0
                  roll = 0.0
                }
                url = "https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/red_car.glb"
                scale = vector3D {
                  x = 50.0
                  y = 50.0
                  z = 50.0
                }
              })
    }

    googleMap3D?.setCamera(
        camera {
          center = latLngAltitude {
            latitude = posAndHeading.position.latitude
            longitude = posAndHeading.position.longitude
            altitude = 0.0
          }
          heading = (posAndHeading.heading.toDouble() + yawOffset.toDouble()).toHeading()
          tilt = 65.0
          range = cameraRange.toDouble()
        }.toValidCamera())
  }

  override fun onPause() {
    super.onPause()
    togglePlayback(false)
    fadeHandler.removeCallbacks(fadeOutRunnable)
  }

  override fun onDestroy() {
    super.onDestroy()
    stopAnimationLoop()
    fadeHandler.removeCallbacks(fadeOutRunnable)
  }
}
