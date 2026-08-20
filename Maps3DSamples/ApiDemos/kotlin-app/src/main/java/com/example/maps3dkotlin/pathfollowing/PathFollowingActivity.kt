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
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.WindowCompat
import com.example.maps3d.common.toHeading
import com.example.maps3dcommon.R
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.Map3DView
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.android.gms.maps3d.model.Polyline
import com.google.android.gms.maps3d.model.PolylineOptions
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import com.google.maps.android.SphericalUtil

/**
 * Advanced sample demonstrating ground-level path following in Kotlin.
 *
 * Features:
 * - Urban vs Rural ground-level paths
 * - Two-polyline architecture: wide blue base route (lower z-index) + narrow purple active progress route (higher z-index)
 * - In-place polyline ID updates eliminating render flickering
 * - Configurable altitude modes (Clamp to Ground default, Relative to Ground, Relative to Mesh, Absolute)
 * - Dynamic path elevation slider to eliminate z-fighting
 * - Explicit collapse dialog button and smooth slide-down controls
 * - Real-time camera controls via sliders: Range, Ground Altitude, Heading Offset, Tilt, Follow Speed
 */
class PathFollowingActivity : AppCompatActivity(), OnMap3DViewReadyCallback {

  private lateinit var map3DView: Map3DView
  private var googleMap3D: GoogleMap3D? = null

  // View Bindings
  private var controlsCard: CardView? = null
  private var cardHeader: View? = null
  private var btnCollapse: MaterialButton? = null
  private var isCollapsed = false

  private lateinit var rgEnvironment: RadioGroup
  private lateinit var rgAltitudeMode: RadioGroup
  private lateinit var pathAltitudeSlider: Slider
  private lateinit var pathAltitudeSliderLabel: TextView
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

  private val fadeHandler = Handler(Looper.getMainLooper())
  private val fadeOutRunnable = Runnable {
    if (controlsCard != null && !isCollapsed) {
      controlsCard?.animate()
        ?.alpha(0.8f)
        ?.setDuration(400)
        ?.start()
    }
  }

  private fun collapseControls() {
    val card = controlsCard ?: return
    isCollapsed = true
    fadeHandler.removeCallbacks(fadeOutRunnable)
    btnCollapse?.setIconResource(R.drawable.expand_less_24px)
    btnCollapse?.contentDescription = getString(R.string.expand_controls)

    val headerHeight = if (cardHeader != null && cardHeader!!.height > 0) {
      cardHeader!!.height
    } else {
      (48 * resources.displayMetrics.density).toInt()
    }
    val targetTranslationY = (card.height - headerHeight).coerceAtLeast(0).toFloat()
    card.animate()
      .translationY(targetTranslationY)
      .alpha(0.9f)
      .setDuration(300)
      .start()
  }

  private fun expandControls() {
    val card = controlsCard ?: return
    isCollapsed = false
    btnCollapse?.setIconResource(R.drawable.expand_more_24px)
    btnCollapse?.contentDescription = getString(R.string.collapse_controls)
    card.animate()
      .translationY(0f)
      .alpha(1.0f)
      .setDuration(250)
      .start()
    fadeHandler.removeCallbacks(fadeOutRunnable)
    fadeHandler.postDelayed(fadeOutRunnable, 3000L)
  }

  override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
    if (ev.action == MotionEvent.ACTION_DOWN || ev.action == MotionEvent.ACTION_MOVE) {
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

  // Control parameters
  private var cameraRange = 300.0
  private var groundAltitude = 20.0
  private var headingOffset = 0.0
  private var cameraTilt = 70.0
  private var followSpeedMps = 30.0
  private var pathAltitudeMode: Int = AltitudeMode.CLAMP_TO_GROUND
  private var pathAltitudeOffset: Double = 0.5

  // Path state
  private var currentPath: List<LatLng> = URBAN_PATH
  private var cumulativeDistances: DoubleArray = doubleArrayOf()
  private var totalDistance: Double = 0.0
  private var elapsedDistance: Double = 0.0
  private var isPlaying = false
  private var isUserScrubbing = false

  // Polylines
  private var staticRoutePolyline: Polyline? = null
  private var progressPolyline: Polyline? = null
  private val animationHandler = Handler(Looper.getMainLooper())
  private var animationRunnable: Runnable? = null

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
    fadeHandler.removeCallbacks(fadeOutRunnable)
    staticRoutePolyline?.remove()
    staticRoutePolyline = null
    progressPolyline?.remove()
    progressPolyline = null
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
      runOnUiThread {
        drawPathPolylines()
        updateCameraPositionForDistance(0.0)
        startAnimation()
      }
    }
  }

  private fun initViews() {
    controlsCard = findViewById(R.id.controls_card)
    cardHeader = findViewById(R.id.card_header)
    btnCollapse = findViewById(R.id.btn_collapse)

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

    fadeHandler.postDelayed(fadeOutRunnable, 3000L)

    rgEnvironment = findViewById(R.id.rg_environment)
    rgAltitudeMode = findViewById(R.id.rg_altitude_mode)
    pathAltitudeSlider = findViewById(R.id.path_altitude_slider)
    pathAltitudeSliderLabel = findViewById(R.id.path_altitude_slider_label)
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

    updateControlLabels()

    // Radio group environment selection
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

    // Radio group altitude mode selection
    rgAltitudeMode.setOnCheckedChangeListener { _, checkedId ->
      pathAltitudeMode = when (checkedId) {
        R.id.rb_relative_to_ground -> AltitudeMode.RELATIVE_TO_GROUND
        R.id.rb_relative_to_mesh -> AltitudeMode.RELATIVE_TO_MESH
        R.id.rb_absolute -> AltitudeMode.ABSOLUTE
        else -> AltitudeMode.CLAMP_TO_GROUND
      }
      drawStaticRoutePolyline()
      updateCameraPositionForDistance(elapsedDistance)
    }

    // Path height slider (relative altitude)
    pathAltitudeSlider.addOnChangeListener { _, value, _ ->
      pathAltitudeOffset = value.toDouble()
      pathAltitudeSliderLabel.text =
        getString(R.string.path_height_format, pathAltitudeOffset)
      drawStaticRoutePolyline()
      updateCameraPositionForDistance(elapsedDistance)
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

    // Sliders listeners
    rangeSlider.addOnChangeListener { _, value, _ ->
      cameraRange = value.toDouble()
      rangeSliderLabel.text = getString(R.string.camera_range_format, cameraRange.toInt())
      updateCameraPositionForDistance(elapsedDistance)
    }

    altitudeSlider.addOnChangeListener { _, value, _ ->
      groundAltitude = value.toDouble()
      altitudeSliderLabel.text =
        getString(R.string.ground_altitude_format, groundAltitude.toInt())
      updateCameraPositionForDistance(elapsedDistance)
    }

    headingSlider.addOnChangeListener { _, value, _ ->
      headingOffset = value.toDouble()
      headingSliderLabel.text =
        getString(R.string.heading_offset_format, headingOffset.toInt())
      updateCameraPositionForDistance(elapsedDistance)
    }

    tiltSlider.addOnChangeListener { _, value, _ ->
      cameraTilt = value.toDouble()
      tiltSliderLabel.text = getString(R.string.camera_tilt_format, cameraTilt.toInt())
      updateCameraPositionForDistance(elapsedDistance)
    }

    speedSlider.addOnChangeListener { _, value, _ ->
      followSpeedMps = value.toDouble()
      speedSliderLabel.text =
        getString(R.string.follow_speed_format, followSpeedMps.toInt())
    }
  }

  private fun updateControlLabels() {
    pathAltitudeSliderLabel.text =
      getString(R.string.path_height_format, pathAltitudeOffset)
    rangeSliderLabel.text = getString(R.string.camera_range_format, cameraRange.toInt())
    altitudeSliderLabel.text =
      getString(R.string.ground_altitude_format, groundAltitude.toInt())
    headingSliderLabel.text =
      getString(R.string.heading_offset_format, headingOffset.toInt())
    tiltSliderLabel.text = getString(R.string.camera_tilt_format, cameraTilt.toInt())
    speedSliderLabel.text =
      getString(R.string.follow_speed_format, followSpeedMps.toInt())
  }

  private var currentHeading: Double? = null

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
      rangeSliderLabel.text = getString(R.string.camera_range_format, 450)
      altitudeSliderLabel.text = getString(R.string.ground_altitude_format, 40)
      tiltSliderLabel.text = getString(R.string.camera_tilt_format, 75)
    } else {
      cameraRange = 300.0
      groundAltitude = 20.0
      cameraTilt = 70.0
      rangeSlider.value = 300f
      altitudeSlider.value = 20f
      tiltSlider.value = 70f
      rangeSliderLabel.text = getString(R.string.camera_range_format, 300)
      altitudeSliderLabel.text = getString(R.string.ground_altitude_format, 20)
      tiltSliderLabel.text = getString(R.string.camera_tilt_format, 70)
    }

    loadPathData(path)
    drawPathPolylines()
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

  private fun drawPathPolylines() {
    drawStaticRoutePolyline()
    if (currentPath.isNotEmpty()) {
      updateProgressPolyline(elapsedDistance, currentPath[0], 0)
    }
  }

  private fun drawStaticRoutePolyline() {
    val map = googleMap3D ?: return
    if (currentPath.isEmpty()) return

    var pathAltitude = if (pathAltitudeMode == AltitudeMode.CLAMP_TO_GROUND) {
      0.0
    } else {
      pathAltitudeOffset
    }
    if (pathAltitudeMode == AltitudeMode.ABSOLUTE) {
      pathAltitude = if (currentPath == RURAL_PATH) 40.0 else 15.0
    }

    val staticVertices = currentPath.map { latLng ->
      LatLngAltitude(latLng.latitude, latLng.longitude, pathAltitude)
    }

    val staticOptions = PolylineOptions().apply {
      id = STATIC_ROUTE_POLYLINE_ID
      path = staticVertices
      strokeColor = Color.parseColor("#4285F4") // Wide blue route
      strokeWidth = 16.0
      zIndex = 1
      altitudeMode = pathAltitudeMode
    }

    staticRoutePolyline = map.addPolyline(staticOptions)
  }

  private fun updateProgressPolyline(dist: Double, currentLatLng: LatLng, index: Int) {
    val map = googleMap3D ?: return
    if (currentPath.isEmpty() || totalDistance <= 0.0) return

    var pathAltitude = if (pathAltitudeMode == AltitudeMode.CLAMP_TO_GROUND) {
      0.0
    } else {
      pathAltitudeOffset
    }
    if (pathAltitudeMode == AltitudeMode.ABSOLUTE) {
      pathAltitude = if (currentPath == RURAL_PATH) 40.0 else 15.0
    }

    val progressAltitude = pathAltitude + 0.2

    val progressCoordinates = ArrayList<LatLngAltitude>()
    for (i in 0..index.coerceAtMost(currentPath.size - 1)) {
      val pt = currentPath[i]
      progressCoordinates.add(LatLngAltitude(pt.latitude, pt.longitude, progressAltitude))
    }
    progressCoordinates.add(
      LatLngAltitude(currentLatLng.latitude, currentLatLng.longitude, progressAltitude)
    )

    if (progressCoordinates.size < 2) {
      val startPt = currentPath[0]
      progressCoordinates.add(LatLngAltitude(startPt.latitude, startPt.longitude, progressAltitude))
    }

    val progressOptions = PolylineOptions().apply {
      id = PROGRESS_POLYLINE_ID
      path = progressCoordinates
      strokeColor = Color.parseColor("#9C27B0") // Narrow purple progress
      strokeWidth = 8.0
      zIndex = 2
      altitudeMode = pathAltitudeMode
    }

    progressPolyline = map.addPolyline(progressOptions)
  }

  private fun startAnimation() {
    if (isPlaying) return
    isPlaying = true
    btnPlayPause.setIconResource(R.drawable.pause_24px)

    val frameDurationMs = 16L
    animationRunnable = object : Runnable {
      override fun run() {
        if (!isPlaying) return

        val stepDistance = followSpeedMps * (frameDurationMs / 1000.0)
        elapsedDistance += stepDistance

        if (elapsedDistance >= totalDistance) {
          elapsedDistance = 0.0
        }

        if (!isUserScrubbing && totalDistance > 0) {
          val progress = (elapsedDistance / totalDistance).toFloat().coerceIn(0f, 1f)
          progressSlider.value = progress
        }

        updateCameraPositionForDistance(elapsedDistance)
        animationHandler.postDelayed(this, frameDurationMs)
      }
    }
    animationHandler.post(animationRunnable!!)
  }

  private fun pauseAnimation() {
    isPlaying = false
    btnPlayPause.setIconResource(R.drawable.play_arrow_24px)
    animationRunnable?.let {
      animationHandler.removeCallbacks(it)
      animationRunnable = null
    }
  }

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
    updateProgressPolyline(dist, currentLatLng, index)
  }

  companion object {
    private const val STATIC_ROUTE_POLYLINE_ID = "path_following_static_route"
    private const val PROGRESS_POLYLINE_ID = "path_following_progress_route"

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
