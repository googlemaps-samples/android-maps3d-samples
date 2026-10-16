// Copyright 2025 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.maps3dkotlin.cameracontrols

import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.view.WindowCompat
import com.example.maps3d.common.Map3DSettings
import com.example.maps3d.common.Map3DSettingsController
import com.example.maps3d.common.copy
import com.example.maps3d.common.toCompassDirection
import com.example.maps3d.common.toValidCamera
import com.example.maps3d.common.wrapIn
import com.example.maps3dcommon.R
import com.example.maps3dkotlin.cameracontrols.DataModel.ESB_FLY_TO_DURATION_MS
import com.example.maps3dkotlin.cameracontrols.DataModel.MT_RAINIER_FLY_TO_DURATION_MS
import com.example.maps3dkotlin.cameracontrols.DataModel.MT_RAINIER_ORBIT_DURATION_MS
import com.example.maps3dkotlin.cameracontrols.DataModel.MT_RAINIER_ORBIT_ROUNDS
import com.example.maps3dkotlin.cameracontrols.DataModel.esbCamera
import com.example.maps3dkotlin.cameracontrols.DataModel.nycCameraRestriction
import com.example.maps3dkotlin.cameracontrols.DataModel.nycPolygonOptions
import com.example.maps3dkotlin.cameracontrols.DataModel.targetBeaconOptions
import com.example.maps3dkotlin.cameracontrols.DataModel.targetCamera
import com.example.maps3dkotlin.sampleactivity.SampleBaseActivity
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.Map3DView
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.Marker
import com.google.android.gms.maps3d.model.Polygon
import com.google.android.gms.maps3d.model.flyAroundOptions
import com.google.android.gms.maps3d.model.flyToOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.slider.Slider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CameraControlsActivity : SampleBaseActivity(), OnMap3DViewReadyCallback {
    override val TAG = this::class.java.simpleName

    @Map3DMode
    override val defaultMapMode: Int = Map3DMode.SATELLITE

    private lateinit var cameraStateText: TextView
    private lateinit var rollSlider: Slider
    private lateinit var rollSliderLabel: TextView
    private var isSyncingRadioGroup = false

    private var restrictionCubeFaces: MutableList<Polygon> = mutableListOf()
    private var activeBeaconMarker: Marker? = null
    private var beaconRefreshToken = 0
    private var needsMeshBeaconRefresh = false

    @AltitudeMode
    private var selectedAltitudeMode: Int = AltitudeMode.RELATIVE_TO_GROUND

    @StringRes
    private var activeTargetResId: Int = R.string.camera_target_esb

    override val initialCamera: Camera = esbCamera(AltitudeMode.RELATIVE_TO_GROUND)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_camera_controls)

        map3DView = findViewById(R.id.map3dView)
        map3DView.onCreate(savedInstanceState)
        map3DView.getMap3DViewAsync(this)

        cameraStateText = findViewById(R.id.camera_state_text)

        rollSlider = findViewById(R.id.roll_slider)
        rollSliderLabel = findViewById(R.id.roll_slider_label)

        updateRollSliderLabel(rollSlider.value)

        findViewById<MaterialButton>(R.id.fly_around).setOnClickListener {
            flyAroundCurrentCenter()
        }

        findViewById<MaterialButton>(R.id.fly_to).setOnClickListener {
            flyToTarget(R.string.camera_target_esb)
        }

        findViewById<MaterialButton>(R.id.fly_to_mountain_button).setOnClickListener {
            flyToTarget(R.string.camera_target_mt_rainier)
        }

        findViewById<ChipGroup>(R.id.altitude_mode_chip_group).setOnCheckedStateChangeListener { _, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            val newMode = when (checkedId) {
                R.id.chip_altitude_relative_to_ground -> AltitudeMode.RELATIVE_TO_GROUND
                R.id.chip_altitude_relative_to_mesh -> AltitudeMode.RELATIVE_TO_MESH
                R.id.chip_altitude_clamp_to_ground -> AltitudeMode.CLAMP_TO_GROUND
                R.id.chip_altitude_absolute -> AltitudeMode.ABSOLUTE
                else -> return@setOnCheckedStateChangeListener
            }
            if (selectedAltitudeMode != newMode) {
                selectedAltitudeMode = newMode
                flyToTarget(activeTargetResId)
            }
        }

        findViewById<MaterialButton>(R.id.toggle_restriction).setOnClickListener { view ->
            val button = view as MaterialButton

            if (button.isChecked) {
                button.text = getText(R.string.camera_remove_restriction)
                googleMap3D?.setCameraRestriction(nycCameraRestriction)
            } else {
                button.text = getText(R.string.camera_activate_restriction)
                googleMap3D?.setCameraRestriction(null)
            }
        }

        findViewById<MaterialButton>(R.id.show_restriction).setOnClickListener { view ->
            val button = view as MaterialButton

            if (button.isChecked) {
                button.text = getText(R.string.camera_hide_restriction)

                // Clear any existing cube face polygons
                restrictionCubeFaces.forEach { it.remove() }
                restrictionCubeFaces.clear()

                // Create and add each face of the cube
                restrictionCubeFaces.addAll(
                    nycPolygonOptions.mapNotNull { polygonOptions ->
                        googleMap3D?.addPolygon(polygonOptions)
                    }
                )
            } else {
                button.text = getText(R.string.camera_show_restriction)
                restrictionCubeFaces.forEach { it.remove() }
                restrictionCubeFaces.clear()
            }
        }

        // Set listener for RadioGroup
        findViewById<RadioGroup>(R.id.map_mode_radio_group).setOnCheckedChangeListener { _, checkedId ->
            if (isSyncingRadioGroup) return@setOnCheckedChangeListener
            val mode = when (checkedId) {
                R.id.map_mode_roadmap -> Map3DMode.ROADMAP
                R.id.map_mode_hybrid -> Map3DMode.HYBRID
                R.id.map_mode_satellite -> Map3DMode.SATELLITE
                else -> return@setOnCheckedChangeListener
            }
            Map3DSettingsController.shared.setMapMode(mode)
            googleMap3D?.let { map ->
                Map3DSettingsController.shared.settings.value.applyTo(map)
                applyMapModeCameraPerspective(map, mode)
            }
        }

        rollSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                updateRollSliderLabel(value)
                updateMapRoll(value.toDouble())
            }
        }

        // Set listener for Reset Roll Button
        findViewById<MaterialButton>(R.id.reset_roll_button).setOnClickListener {
            val resetValue = 0.0f
            rollSlider.value = resetValue
            updateRollSliderLabel(resetValue)
            updateMapRoll(resetValue.toDouble())
        }
    }

    override fun onQuickSettingsChanged(settings: Map3DSettings) {
        val group = findViewById<RadioGroup?>(R.id.map_mode_radio_group) ?: return
        val targetId = when (settings.mapMode) {
            Map3DMode.ROADMAP -> R.id.map_mode_roadmap
            Map3DMode.HYBRID -> R.id.map_mode_hybrid
            Map3DMode.SATELLITE -> R.id.map_mode_satellite
            else -> View.NO_ID
        }
        if (group.checkedRadioButtonId != targetId) {
            isSyncingRadioGroup = true
            if (targetId == View.NO_ID) {
                group.clearCheck()
            } else {
                group.check(targetId)
            }
            isSyncingRadioGroup = false
        }
    }

    private fun applyMapModeCameraPerspective(map: GoogleMap3D, @Map3DMode mode: Int) {
        val currentCamera = map.getCamera()?.toValidCamera() ?: return
        val currentTilt = currentCamera.tilt ?: 67.0
        val targetTilt = if (mode == Map3DMode.ROADMAP) {
            0.0
        } else if (currentTilt <= 30.0) {
            67.0
        } else {
            return
        }
        map.flyCameraTo(
            flyToOptions {
                endCamera = currentCamera.copy(tilt = targetTilt)
                durationInMillis = 900
            }
        )
    }

    private fun updateRollSliderLabel(value: Float) {
        rollSliderLabel.text = getString(R.string.camera_roll_label_dynamic, value)
    }

    private fun updateMapRoll(newRoll: Double) {
        googleMap3D?.getCamera()?.let { currentCamera ->
            googleMap3D?.setCamera(
                currentCamera.toValidCamera().copy(roll = newRoll)
            )
        }
    }

    /**
     * Called when the [Map3DView] is ready to be used.
     *
     * This callback is triggered when the [GoogleMap3D] object is initialized and ready
     * for interaction. You can use this method to set up map features, add markers,
     * set the camera position, etc.
     *
     * @param googleMap3D The [GoogleMap3D] object representing the 3D map.
     */
    override fun onMapReady(googleMap3D: GoogleMap3D) {
        super.onMapReady(googleMap3D)
        this.googleMap3D = googleMap3D

        upsertActiveBeacon()

        googleMap3D.setOnMapSteadyListener { isSteady ->
            if (isSteady) {
                map3DView.contentDescription = "MapSteady"
                if (needsMeshBeaconRefresh) {
                    needsMeshBeaconRefresh = false
                    runOnUiThread { upsertActiveBeacon() }
                }
            }
        }

        // Set the initial camera position
        googleMap3D.getCamera()?.let { camera ->
            updateCameraPosition(camera)
        }

        // Track camera position changes
        googleMap3D.setCameraChangedListener { cameraPosition ->
            updateCameraPosition(cameraPosition)
        }
    }

    private fun getBeaconLabel(isMountain: Boolean, @AltitudeMode mode: Int): String {
        val resId = when (mode) {
            AltitudeMode.RELATIVE_TO_MESH ->
                if (isMountain) R.string.mt_rainier_beacon_mesh_label else R.string.esb_beacon_mesh_label
            AltitudeMode.CLAMP_TO_GROUND ->
                if (isMountain) R.string.mt_rainier_beacon_clamp_label else R.string.esb_beacon_clamp_label
            AltitudeMode.ABSOLUTE ->
                if (isMountain) R.string.mt_rainier_beacon_absolute_label else R.string.esb_beacon_absolute_label
            else ->
                if (isMountain) R.string.mt_rainier_beacon_label else R.string.esb_beacon_ground_label
        }
        return getString(resId)
    }

    /**
     * Places or updates the altitude reference beacon for the active target using the unified
     * [AltitudeMode] builder.
     */
    private fun upsertActiveBeacon() {
        val map = googleMap3D ?: return
        val isMountain = (activeTargetResId == R.string.camera_target_mt_rainier)
        val beaconOptions = targetBeaconOptions(
            isMountain = isMountain,
            label = getBeaconLabel(isMountain, selectedAltitudeMode),
            mode = selectedAltitudeMode,
            refreshToken = ++beaconRefreshToken,
        )
        activeBeaconMarker?.remove()
        activeBeaconMarker = map.addMarker(beaconOptions)
    }

    /**
     * Initiates a camera flight animation to the specified landmark target ([R.string.camera_target_esb]
     * or [R.string.camera_target_mt_rainier]) using the single unified [AltitudeMode] camera and
     * marker pipeline.
     */
    private fun flyToTarget(@StringRes targetResId: Int) {
        val map = googleMap3D ?: return
        activeTargetResId = targetResId
        val isMountain = (targetResId == R.string.camera_target_mt_rainier)

        // Clear NYC bounds restriction if active so the camera can travel to Washington State
        if (isMountain) {
            val restrictionButton = findViewById<MaterialButton?>(R.id.toggle_restriction)
            if (restrictionButton?.isChecked == true) {
                restrictionButton.isChecked = false
                restrictionButton.text = getText(R.string.camera_activate_restriction)
                map.setCameraRestriction(null)
            }
        }

        needsMeshBeaconRefresh = (selectedAltitudeMode == AltitudeMode.RELATIVE_TO_MESH)
        upsertActiveBeacon()

        val targetCam = targetCamera(isMountain = isMountain, mode = selectedAltitudeMode)
        map.setCameraAnimationEndListener {
            map.setCameraAnimationEndListener(null)
            runOnUiThread { upsertActiveBeacon() }
            if (isMountain) {
                map.flyCameraAround(
                    flyAroundOptions {
                        center = targetCam
                        durationInMillis = MT_RAINIER_ORBIT_DURATION_MS
                        rounds = MT_RAINIER_ORBIT_ROUNDS
                    }
                )
            }
        }
        map.flyCameraTo(
            flyToOptions {
                endCamera = targetCam
                durationInMillis =
                    if (isMountain) MT_RAINIER_FLY_TO_DURATION_MS else ESB_FLY_TO_DURATION_MS
            }
        )
    }

    /**
     * Initiates a camera fly-around animation around the current camera's center.
     */
    private fun flyAroundCurrentCenter() {
        val camera = googleMap3D?.getCamera()?.toValidCamera() ?: return
        googleMap3D?.setCameraAnimationEndListener(null)

        googleMap3D?.flyCameraAround(
            flyAroundOptions {
                center = camera
                durationInMillis = 5_000
                rounds = 1.0
            }
        )
    }

    private fun getAltitudeModeLabel(@AltitudeMode mode: Int): String =
        when (mode) {
            AltitudeMode.RELATIVE_TO_GROUND -> getString(R.string.altitude_mode_relative_to_ground_code)
            AltitudeMode.RELATIVE_TO_MESH -> getString(R.string.altitude_mode_relative_to_mesh_code)
            AltitudeMode.CLAMP_TO_GROUND -> getString(R.string.altitude_mode_clamp_to_ground_code)
            else -> getString(R.string.altitude_mode_absolute_code)
        }

    /**
     * Updates the camera state text view with the current camera's position, active target,
     * and [AltitudeMode] telemetry.
     */
    private fun updateCameraPosition(camera: Camera) {
        val nbsp = "\u00A0"

        val cameraStateString = buildString {
            // Latitude
            append(getString(R.string.cam_lat_label, camera.center.latitude))
            append(", ")

            // Longitude
            append(getString(R.string.cam_lng_label, camera.center.longitude))
            append(", ")

            // Altitude
            append(getString(R.string.cam_alt_label, camera.center.altitude))
            append(",\n") // Newline after altitude

            // Heading
            append(getString(R.string.cam_hdg_label, camera.heading ?: 0.0))
            val compassString = (camera.heading ?: 0.0).toCompassDirection()
            append("$nbsp($compassString)")
            append(", ")

            // Tilt
            append(getString(R.string.cam_tlt_label, camera.tilt ?: 0.0))
            append(", ")

            // Range
            append(getString(R.string.cam_rng_label, camera.range ?: 0.0))
            append("\n")

            // Active Target & AltitudeMode
            append(
                getString(
                    R.string.camera_target_and_altitude_mode_format,
                    getString(activeTargetResId),
                    getAltitudeModeLabel(selectedAltitudeMode),
                )
            )
        }

        val resetValue = (camera.roll ?: 0.0).toFloat().wrapIn(-180f..180f)

        CoroutineScope(Dispatchers.Main).launch {
            cameraStateText.text = cameraStateString
            rollSlider.value = resetValue
            updateRollSliderLabel(resetValue)
        }
    }
}
