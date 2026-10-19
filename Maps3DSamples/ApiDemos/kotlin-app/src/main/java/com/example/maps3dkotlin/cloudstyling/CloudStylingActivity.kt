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
import com.example.maps3d.common.Map3DSettings
import com.example.maps3d.common.Map3DSettingsController
import com.example.maps3d.common.toValidCamera
import com.example.maps3dcommon.R
import com.example.maps3dkotlin.sampleactivity.SampleBaseActivity
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.flyToOptions
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

/**
 * Showcases **Cloud-Based Map Styling** in Google Maps 3D SDK.
 * Uses standalone layout [R.layout.activity_cloud_styling] with declarative custom Map ID.
 */
class CloudStylingActivity : SampleBaseActivity() {

    override val TAG: String = this::class.java.simpleName

    override val layoutResId: Int = R.layout.activity_cloud_styling

    @Map3DMode
    override val defaultMapMode: Int = Map3DMode.ROADMAP

    override val initialCamera: Camera
        get() = camera {
            center = latLngAltitude {
                latitude = SF_LOCATION.latitude
                longitude = SF_LOCATION.longitude
                altitude = 0.0
            }
            heading = 45.0
            tilt = if (Map3DSettingsController.shared.getSettings().mapMode == Map3DMode.ROADMAP) {
                TILT_2D_ROADMAP
            } else {
                TILT_3D_HYBRID
            }
            range = 2500.0
        }.toValidCamera()

    @Map3DMode
    private var currentMapMode: Int = Map3DMode.ROADMAP
    private var rgMapMode: RadioGroup? = null
    private var btnToggle2D3D: MaterialButton? = null
    private var isUpdatingModeUI = false
    private var hasSyncedInitialMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        Map3DSettingsController.shared.onSampleEntered(defaultMapMode)
        currentMapMode = Map3DSettingsController.shared.getSettings().mapMode
        super.onCreate(savedInstanceState)

        findViewById<MaterialToolbar>(R.id.top_bar)?.apply {
            title = getString(R.string.feature_title_cloud_styling)
            setNavigationOnClickListener { finish() }
        }

        rgMapMode = findViewById(R.id.rg_map_mode)
        btnToggle2D3D = findViewById(R.id.btn_toggle_2d_3d)

        btnToggle2D3D?.setOnClickListener {
            val nextMode = if (currentMapMode == Map3DMode.ROADMAP) {
                Map3DMode.HYBRID
            } else {
                Map3DMode.ROADMAP
            }
            applyMapModeAndTilt(nextMode)
        }

        rgMapMode?.setOnCheckedChangeListener { _, checkedId ->
            if (isUpdatingModeUI) return@setOnCheckedChangeListener
            val newMode = when (checkedId) {
                R.id.rb_hybrid -> Map3DMode.HYBRID
                R.id.rb_satellite -> Map3DMode.SATELLITE
                else -> Map3DMode.ROADMAP
            }
            applyMapModeAndTilt(newMode)
        }

        updateModeUI(currentMapMode)
    }

    private fun applyMapModeAndTilt(@Map3DMode newMode: Int) {
        currentMapMode = newMode
        Map3DSettingsController.shared.setMapMode(newMode)
        updateModeUI(newMode)
        googleMap3D?.let { map ->
            map.setMapMode(newMode)
            val targetTilt = if (newMode == Map3DMode.ROADMAP) TILT_2D_ROADMAP else TILT_3D_HYBRID
            val updatedCamera = camera {
                center = latLngAltitude {
                    latitude = SF_LOCATION.latitude
                    longitude = SF_LOCATION.longitude
                    altitude = 0.0
                }
                heading = 45.0
                tilt = targetTilt
                roll = 0.0
                range = 2500.0
            }.toValidCamera()
            map.flyCameraTo(
                flyToOptions {
                    endCamera = updatedCamera
                    durationInMillis = MODE_SWITCH_DURATION_MS
                },
            )
        }
    }

    override fun onQuickSettingsChanged(settings: Map3DSettings) {
        val modeChangedAfterInit = hasSyncedInitialMode && currentMapMode != settings.mapMode
        hasSyncedInitialMode = true
        if (modeChangedAfterInit) {
            applyMapModeAndTilt(settings.mapMode)
        } else {
            currentMapMode = settings.mapMode
            updateModeUI(currentMapMode)
        }
    }

    private fun updateModeUI(@Map3DMode mode: Int) {
        isUpdatingModeUI = true
        val targetRadioId = when (mode) {
            Map3DMode.HYBRID -> R.id.rb_hybrid
            Map3DMode.SATELLITE -> R.id.rb_satellite
            else -> R.id.rb_roadmap
        }
        if (rgMapMode?.checkedRadioButtonId != targetRadioId) {
            rgMapMode?.check(targetRadioId)
        }
        btnToggle2D3D?.setText(
            if (mode == Map3DMode.ROADMAP) {
                R.string.store_locator_switch_to_3d
            } else {
                R.string.store_locator_switch_to_2d
            },
        )
        isUpdatingModeUI = false
    }

    override fun onMapReady(googleMap3D: GoogleMap3D) {
        super.onMapReady(googleMap3D)
        googleMap3D.setMapMode(currentMapMode)
    }

    companion object {
        val SF_LOCATION = LatLng(37.7915, -122.4010)
        private const val TILT_2D_ROADMAP = 0.0
        private const val TILT_3D_HYBRID = 65.0
        private const val MODE_SWITCH_DURATION_MS = 1_000L
    }
}
