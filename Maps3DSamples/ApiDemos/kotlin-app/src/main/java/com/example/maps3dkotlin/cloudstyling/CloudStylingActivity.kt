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

package com.example.maps3dkotlin.cloudstyling

import android.os.Bundle
import android.widget.RadioGroup
import androidx.activity.enableEdgeToEdge
import com.example.maps3d.common.toValidCamera
import com.example.maps3dcommon.R
import com.example.maps3dkotlin.sampleactivity.SampleBaseActivity
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.Map3DView
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.material.appbar.MaterialToolbar

/**
 * Showcases **Cloud-Based Map Styling** in Google Maps 3D SDK.
 * Uses standalone layout [R.layout.activity_cloud_styling] with declarative custom Map ID.
 */
class CloudStylingActivity : SampleBaseActivity() {

    override val TAG: String = this::class.java.simpleName

    override val initialCamera: Camera = camera {
        center = latLngAltitude {
            latitude = SF_LOCATION.latitude
            longitude = SF_LOCATION.longitude
            altitude = 250.0
        }
        heading = 45.0
        tilt = 65.0
        range = 800.0
    }.toValidCamera()

    @Map3DMode
    private var currentMapMode: Int = Map3DMode.ROADMAP

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cloud_styling)

        findViewById<MaterialToolbar>(R.id.top_bar)?.apply {
            title = getString(R.string.feature_title_cloud_styling)
            setNavigationOnClickListener { finish() }
        }

        map3DView = findViewById<Map3DView>(R.id.map3dView).apply {
            onCreate(savedInstanceState)
            getMap3DViewAsync(this@CloudStylingActivity)
        }

        findViewById<RadioGroup>(R.id.rg_map_mode)?.setOnCheckedChangeListener { _, checkedId ->
            val newMode = when (checkedId) {
                R.id.rb_hybrid -> Map3DMode.HYBRID
                R.id.rb_satellite -> Map3DMode.SATELLITE
                else -> Map3DMode.ROADMAP
            }
            currentMapMode = newMode
            googleMap3D?.setMapMode(newMode)
        }
    }

    override fun onMapReady(googleMap3D: GoogleMap3D) {
        super.onMapReady(googleMap3D)
        googleMap3D.setMapMode(currentMapMode)
    }

    companion object {
        val SF_LOCATION = LatLng(37.7915, -122.4010)
    }
}
