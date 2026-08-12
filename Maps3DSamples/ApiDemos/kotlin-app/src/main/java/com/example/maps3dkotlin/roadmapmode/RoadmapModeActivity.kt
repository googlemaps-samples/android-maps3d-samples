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

package com.example.maps3dkotlin.roadmapmode

import android.os.Bundle
import android.view.ViewGroup
import android.widget.RadioGroup
import com.example.maps3dcommon.R
import com.example.maps3dkotlin.sampleactivity.SampleBaseActivity
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.material.appbar.MaterialToolbar

/**
 * Showcases 3D Roadmap mode in Google Maps 3D SDK focused on San Francisco.
 *
 * Features:
 * - Switching between ROADMAP (Vector 3D Buildings & Street Layout), HYBRID, and SATELLITE modes.
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
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate roadmap control panel overlay into map container managed by SampleBaseActivity
        findViewById<ViewGroup>(R.id.map_container)?.let { container ->
            layoutInflater.inflate(R.layout.control_panel_roadmap_mode, container, true)
        }

        findViewById<MaterialToolbar>(R.id.top_bar)?.apply {
            title = "3D Roadmap Mode"
            setNavigationOnClickListener { finish() }
        }

        findViewById<RadioGroup>(R.id.rg_map_mode)?.setOnCheckedChangeListener { _, checkedId ->
            googleMap3D?.let { map ->
                when (checkedId) {
                    R.id.rb_roadmap -> map.setMapMode(Map3DMode.ROADMAP)
                    R.id.rb_hybrid -> map.setMapMode(Map3DMode.HYBRID)
                    R.id.rb_satellite -> map.setMapMode(Map3DMode.SATELLITE)
                }
            }
        }
    }

    override fun onMapReady(googleMap3D: GoogleMap3D) {
        super.onMapReady(googleMap3D)
        googleMap3D.setMapMode(Map3DMode.ROADMAP)
        googleMap3D.setCamera(initialCamera)
    }

    companion object {
        // San Francisco Financial District
        val SF_LOCATION = LatLng(37.7915, -122.4010)
    }
}
