/*
 * Copyright 2025 Google LLC
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
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
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
import com.google.android.material.slider.Slider
import kotlin.math.tan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Showcases Field of View (FOV) perspective scaling in Google Maps 3D SDK.
 *
 * Features:
 * - Dynamic camera Field of View (FOV) perspective slider (15° to 120°).
 * - Instant FOV preset buttons (20° Telephoto, 45° Standard, 90° Wide, 120° Ultra-Wide).
 * - Safe camera validation preventing out-of-range heading/tilt crash during manual map rotation.
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
      range = 800.0
    }.toValidCamera()

  private lateinit var fovSliderLabel: TextView
  private lateinit var fovSlider: Slider

  private var currentFov = 45.0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Inflate FOV control panel overlay into map container managed by SampleBaseActivity
    findViewById<ViewGroup>(R.id.map_container)?.let { container ->
      layoutInflater.inflate(R.layout.control_panel_field_of_view, container, true)
    }

    findViewById<MaterialToolbar>(R.id.top_bar)?.apply {
      title = "Field of View (FOV)"
      setNavigationOnClickListener { finish() }
    }

    fovSliderLabel = findViewById(R.id.fov_slider_label)
    fovSlider = findViewById(R.id.fov_slider)

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
  }

  override fun onMapReady(googleMap3D: GoogleMap3D) {
    super.onMapReady(googleMap3D)
    lifecycleScope.launch(Dispatchers.Main) {
      updateFov(currentFov)
    }
  }

  private fun updateFov(fovAngle: Double) {
    currentFov = fovAngle
    runOnUiThread {
      fovSliderLabel.text = "Field of View: ${fovAngle.toInt()}°"
    }

    googleMap3D?.let { map ->
      val liveCam = map.getCamera()?.toValidCamera()
      val currCam =
        if (liveCam != null && (kotlin.math.abs(liveCam.center.latitude) > 0.001 || kotlin.math.abs(
            liveCam.center.longitude
          ) > 0.001)
        ) {
          liveCam
        } else {
          initialCamera
        }

      // Deterministic optical dolly zoom perspective range (baseline 800m at 45° FOV)
      val baseFovRad = Math.toRadians(45.0 / 2.0)
      val targetFovRad = Math.toRadians(fovAngle / 2.0)
      val targetRange = (800.0 * tan(baseFovRad) / tan(targetFovRad)).coerceIn(150.0, 3000.0)

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

  companion object {
    // San Francisco Financial District / Transamerica Pyramid area
    val SF_FINANCIAL_DISTRICT = LatLng(37.7952, -122.4028)
  }
}
