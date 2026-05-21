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

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.example.maps3d.common.toHeading
import com.example.maps3d.common.RouteEngine
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
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A premium View-based sample activity demonstrating cross-product integration with the Routes API.
 *
 * This sample performs a direct API call to compute a driving route between two waypoints in Honolulu,
 * parses the encoded polyline on a background thread, draws it on the [GoogleMap3D], loads a custom
 * 3D Car model (.glb), and choreographs a smooth frame-by-frame animation tracking the car along
 * the route with interactive camera controls.
 */
class RoutesActivity : SampleBaseActivity() {
    override val TAG = "RoutesActivity"

    // Honolulu Overview Camera looking over the beautiful island of Oahu.
    override val initialCamera: Camera = camera {
        center = latLngAltitude {
            latitude = 21.348567
            longitude = -157.803961
            altitude = 0.0
        }
        heading = 38.6
        tilt = 45.0
        range = 20000.0
    }

    // View Bindings
    private lateinit var btnPlayPause: MaterialButton
    private lateinit var progressSlider: Slider
    private lateinit var rangeSlider: Slider
    private lateinit var rangeSliderLabel: TextView
    private lateinit var speedSlider: Slider
    private lateinit var speedSliderLabel: TextView
    private lateinit var headingSlider: Slider
    private lateinit var headingSliderLabel: TextView

    // Core State Variables
    private val routeRepository = RouteRepository()
    private var decodedRoute: List<LatLng> = emptyList()
    private var cumulativeDistances: DoubleArray = doubleArrayOf(0.0)
    private var totalDistance: Double = 0.0
    private var elapsedDistance: Double = 0.0

    private var isPlaying = false
    private var isUserScrubbing = false

    // Sliders Values
    private var cameraRange = 1500f       // Slider range: 200m to 5000m
    private var vehicleSpeedMps = 150f    // Slider range: 10m/s to 500m/s
    private var yawOffset = 0f            // Slider range: -180° to 180°

    // Map References
    private var routePolyline: Polyline? = null
    private var vehicleModel: Model? = null

    // Background Coroutine Jobs
    private var animationJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize window flags and custom layouts before map callback gets triggered
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_routes)

        // Override toolbar back action
        findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.top_bar).apply {
            title = getString(R.string.feature_title_routes_api)
            setNavigationOnClickListener { finish() }
        }

        // Bind control views
        btnPlayPause = findViewById(R.id.btn_play_pause)
        progressSlider = findViewById(R.id.progress_slider)
        rangeSlider = findViewById(R.id.range_slider)
        rangeSliderLabel = findViewById(R.id.range_slider_label)
        speedSlider = findViewById(R.id.speed_slider)
        speedSliderLabel = findViewById(R.id.speed_slider_label)
        headingSlider = findViewById(R.id.heading_slider)
        headingSliderLabel = findViewById(R.id.heading_slider_label)

        setupControls()
    }

    /**
     * Registers interactive callbacks for playback buttons and material sliders.
     */
    private fun setupControls() {
        btnPlayPause.setOnClickListener {
            if (decodedRoute.isEmpty()) {
                Toast.makeText(this, "Route is still loading...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            togglePlayback(!isPlaying)
        }

        // Let the user scrub the progress slider manually
        progressSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                isUserScrubbing = true
            }

            override fun onStopTrackingTouch(slider: Slider) {
                isUserScrubbing = false
                elapsedDistance = totalDistance * slider.value.toDouble()
                updateVehiclePositionAndCamera()
            }
        })

        progressSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser && isUserScrubbing) {
                elapsedDistance = totalDistance * value.toDouble()
                updateVehiclePositionAndCamera()
            }
        }

        // Initialize Slider Labels and Listeners
        rangeSliderLabel.text = "Camera Altitude: ${cameraRange.toInt()}m"
        rangeSlider.value = cameraRange
        rangeSlider.addOnChangeListener { _, value, _ ->
            cameraRange = value
            rangeSliderLabel.text = "Camera Altitude: ${value.toInt()}m"
            updateVehiclePositionAndCamera()
        }

        speedSliderLabel.text = "Vehicle Speed: ${vehicleSpeedMps.toInt()}m/s"
        speedSlider.value = vehicleSpeedMps
        speedSlider.addOnChangeListener { _, value, _ ->
            vehicleSpeedMps = value
            speedSliderLabel.text = "Vehicle Speed: ${value.toInt()}m/s"
        }

        headingSliderLabel.text = "Camera Yaw Offset: ${yawOffset.toInt()}°"
        headingSlider.value = yawOffset
        headingSlider.addOnChangeListener { _, value, _ ->
            yawOffset = value
            headingSliderLabel.text = "Camera Yaw Offset: ${value.toInt()}°"
            updateVehiclePositionAndCamera()
        }
    }

    private fun togglePlayback(play: Boolean) {
        isPlaying = play
        if (play) {
            btnPlayPause.setIconResource(R.drawable.pause_24px)
            startAnimationLoop()
        } else {
            btnPlayPause.setIconResource(R.drawable.play_arrow_24px)
            stopAnimationLoop()
        }
    }

    override fun onMapReady(googleMap3D: GoogleMap3D) {
        super.onMapReady(googleMap3D)
        googleMap3D.setMapMode(Map3DMode.SATELLITE)

        // Trigger background route loading
        lifecycleScope.launch(Dispatchers.Default) {
            loadAndRenderRoute(googleMap3D)
        }
    }

    /**
     * Fetches driving direction coordinates from Routes API, decodes the polyline payload
     * on background threads, and populates the map geometry.
     */
    private suspend fun loadAndRenderRoute(googleMap3D: GoogleMap3D) {
        val apiKey = BuildConfig.MAPS3D_API_KEY
        if (apiKey.isEmpty() || apiKey.contains("YOUR_API_KEY")) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@RoutesActivity,
                    "API Key is missing or invalid. Cannot fetch route.",
                    Toast.LENGTH_LONG
                ).show()
            }
            return
        }

        // Waypoints corresponding to standard Honolulu coordinate locations
        val origin = LatLng(21.307043, -157.858984)
        val destination = LatLng(21.390177, -157.719454)

        try {
            val routeData = routeRepository.fetchRoute(apiKey, origin, destination)

            // CPU-heavy decoding of standard Google encoded polyline format
            val decoded = PolyUtil.decode(routeData.encodedPolyline)

            withContext(Dispatchers.Main) {
                decodedRoute = decoded
                cumulativeDistances = RouteEngine.calculateCumulativeDistances(decoded)
                totalDistance = cumulativeDistances.last()

                // 1. Draw the blue Polyline representational trail
                routePolyline = googleMap3D.addPolyline(polylineOptions {
                    path = decoded.map { latLngAltitude { latitude = it.latitude; longitude = it.longitude; altitude = 0.0 } }
                    strokeColor = Color.BLUE
                    strokeWidth = 10.0
                    altitudeMode = AltitudeMode.CLAMP_TO_GROUND
                    zIndex = 5
                })

                // 2. Place the 3D model of the Red Car at starting coordinate
                vehicleModel = googleMap3D.addModel(modelOptions {
                    id = "vehicle_car"
                    position = latLngAltitude {
                        latitude = decoded.first().latitude
                        longitude = decoded.first().longitude
                        altitude = 25.0 // Hover altitude above terrain
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

                // Position camera directly behind the starting model position
                updateVehiclePositionAndCamera()
                
                // Auto-play to start
                togglePlayback(true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch/draw route", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@RoutesActivity,
                    "Error loading route details: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * Runs the high-fidelity physics animation tick loop.
     */
    private fun startAnimationLoop() {
        animationJob = lifecycleScope.launch(Dispatchers.Main) {
            var lastTime = System.currentTimeMillis()
            while (isPlaying && totalDistance > 0.0) {
                val now = System.currentTimeMillis()
                val dt = (now - lastTime) / 1000.0 // Delta time in seconds
                lastTime = now

                // Increment geographic distance traversed
                elapsedDistance += vehicleSpeedMps * dt

                // Loop/clamp playback boundaries
                if (elapsedDistance >= totalDistance) {
                    elapsedDistance = 0.0
                }

                // Synchronize UI progress slider
                if (!isUserScrubbing) {
                    progressSlider.value = (elapsedDistance / totalDistance).toFloat()
                }

                updateVehiclePositionAndCamera()
                
                // Cap framerate to approx 60fps (16ms ticks)
                delay(16)
            }
        }
    }

    private fun stopAnimationLoop() {
        animationJob?.cancel()
        animationJob = null
    }

    /**
     * Interpolates exact geographic position & heading using precomputed binary-searches,
     * updating the 3D model coordinates and camera focus vectors.
     */
    private fun updateVehiclePositionAndCamera() {
        val route = decodedRoute
        if (route.isEmpty() || totalDistance <= 0.0) return

        // O(log N) search and linear interpolation
        val posAndHeading = RouteEngine.calculatePositionAndHeading(
            route,
            cumulativeDistances,
            elapsedDistance,
            lookaheadDistance = 30.0
        )

        // Update the Model coordinates
        vehicleModel?.apply {
            setPosition(latLngAltitude {
                latitude = posAndHeading.position.latitude
                longitude = posAndHeading.position.longitude
                altitude = 25.0 // Keep consistent vehicle altitude hover
            })
            setOrientation(orientation {
                heading = posAndHeading.heading.toDouble()
                tilt = -90.0
                roll = 0.0
            })
        }

        // Track camera following vehicle
        googleMap3D?.setCamera(camera {
            center = latLngAltitude {
                latitude = posAndHeading.position.latitude
                longitude = posAndHeading.position.longitude
                altitude = 0.0
            }
            heading = (posAndHeading.heading.toDouble() + yawOffset.toDouble()).toHeading()
            tilt = 65.0
            range = cameraRange.toDouble()
        })
    }

    override fun onPause() {
        super.onPause()
        togglePlayback(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAnimationLoop()
    }
}
