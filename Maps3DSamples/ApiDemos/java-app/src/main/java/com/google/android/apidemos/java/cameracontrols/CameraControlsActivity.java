/*
 * Copyright 2023 Google LLC
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

package com.google.android.apidemos.java.cameracontrols;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity; // Changed from SampleBaseActivity
import androidx.core.view.WindowCompat;
import com.google.android.apidemos.java.utils.JavaCameraUtils; // Corrected package
import com.google.android.apidemos.java.utils.JavaNumericUtils; // Added for wrapIn
import com.google.android.apidemos.java.utils.JavaStringUtils;   // Corrected package
// SampleBaseActivity import removed
import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.Map3DView;
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback;
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.FlyAroundOptions;
import com.google.android.gms.maps3d.model.FlyToOptions;
import com.google.android.gms.maps3d.model.LatLngAltitude;
import com.google.android.gms.maps3d.model.Map3DMode;
import com.google.android.gms.maps3d.model.Polygon;
import com.google.android.gms.maps3d.model.PolygonOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.example.maps3dcommon.R; // Common resources

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CameraControlsActivity extends AppCompatActivity implements OnMap3DViewReadyCallback { // Changed base class

    private static final String TAG = CameraControlsActivity.class.getSimpleName();
    private Map3DView map3DView; // Added field
    private GoogleMap3D googleMap3D; // Added field

    private TextView cameraStateText;
    private Slider rollSlider;
    private TextView rollSliderLabel;
    private List<Polygon> restrictionCubeFaces = new ArrayList<>();

    private static final LatLngAltitude INITIAL_CENTER_LAT_LNG_ALT = new LatLngAltitude(40.7128, -74.0060, 150.0);
    public static final Camera INITIAL_CAMERA = new Camera.Builder()
        .center(INITIAL_CENTER_LAT_LNG_ALT)
        .heading(252.7)
        .tilt(79.0)
        .range(1500.0)
        .build();

    // getInitialCamera() is no longer an override, but a local method/constant could be used if needed.
    // For now, INITIAL_CAMERA is used directly where initial camera settings are needed.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_camera_controls); // Uses its own layout

        map3DView = findViewById(R.id.map3dView); // Initialize its own map3DView
        map3DView.onCreate(savedInstanceState);
        map3DView.getMap3DViewAsync(this);

        cameraStateText = findViewById(R.id.camera_state_text);
        rollSlider = findViewById(R.id.roll_slider);
        rollSliderLabel = findViewById(R.id.roll_slider_label);

        updateRollSliderLabel(rollSlider.getValue());

        findViewById(R.id.fly_around).setOnClickListener(v -> flyAroundCurrentCenter());
        findViewById(R.id.fly_to).setOnClickListener(v -> flyToEmpireStateBuilding());

        MaterialButton toggleRestrictionButton = findViewById(R.id.toggle_restriction);
        toggleRestrictionButton.setOnClickListener(view -> {
            MaterialButton button = (MaterialButton) view;
            if (googleMap3D == null) return;

            if (button.isChecked()) {
                button.setText(R.string.camera_remove_restriction);
                googleMap3D.setCameraRestriction(DataModel.nycCameraRestriction);
            } else {
                button.setText(R.string.camera_activate_restriction);
                googleMap3D.setCameraRestriction(null);
            }
        });

        MaterialButton showRestrictionButton = findViewById(R.id.show_restriction);
        showRestrictionButton.setOnClickListener(view -> {
            MaterialButton button = (MaterialButton) view;
            if (googleMap3D == null) return;

            if (button.isChecked()) {
                button.setText(R.string.camera_hide_restriction);
                restrictionCubeFaces.forEach(Polygon::remove);
                restrictionCubeFaces.clear();

                for (PolygonOptions options : DataModel.nycPolygonOptions) {
                    Polygon polygon = googleMap3D.addPolygon(options);
                    if (polygon != null) {
                        restrictionCubeFaces.add(polygon);
                    }
                }
            } else {
                button.setText(R.string.camera_show_restriction);
                restrictionCubeFaces.forEach(Polygon::remove);
                restrictionCubeFaces.clear();
            }
        });

        RadioGroup mapModeRadioGroup = findViewById(R.id.map_mode_radio_group);
        mapModeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (googleMap3D == null) return;
            if (checkedId == R.id.map_mode_hybrid) {
                googleMap3D.setMapMode(Map3DMode.HYBRID);
            } else if (checkedId == R.id.map_mode_satellite) {
                googleMap3D.setMapMode(Map3DMode.SATELLITE);
            }
        });

        rollSlider.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                updateRollSliderLabel(value);
                updateMapRoll(value); // Changed to double for consistency with Kotlin
            }
        });

        findViewById(R.id.reset_roll_button).setOnClickListener(v -> {
            float resetValue = 0.0f;
            rollSlider.setValue(resetValue);
            updateRollSliderLabel(resetValue);
            updateMapRoll(resetValue);
        });
    }

    private void updateRollSliderLabel(float value) {
        rollSliderLabel.setText(getString(R.string.camera_roll_label_dynamic, value));
    }

    private void updateMapRoll(double newRoll) {
        if (googleMap3D == null) return;
        Camera currentCamera = googleMap3D.getCamera();
        if (currentCamera != null) {
            // Assuming JavaCameraUtils.copy and JavaCameraUtils.toValidCamera will be created
            // For now, direct builder usage:
            Camera validCamera = JavaCameraUtils.toValidCamera(currentCamera);
            googleMap3D.setCamera(JavaCameraUtils.copy(validCamera).roll(newRoll).build());
        }
    }

    @Override
    public void onMap3DViewReady(GoogleMap3D map) {
        // No super.onMap3DViewReady() call as we are not using SampleBaseActivity's map handling
        this.googleMap3D = map;
        if (this.googleMap3D == null) {
            Log.e(TAG, "GoogleMap3D is null in onMap3DViewReady");
            return;
        }

        // Set initial camera for this specific activity
        this.googleMap3D.setCamera(INITIAL_CAMERA);

        if (this.googleMap3D.getCamera() != null) {
            updateCameraPosition(this.googleMap3D.getCamera());
        }

        this.googleMap3D.setCameraChangedListener(this::updateCameraPosition);

        this.googleMap3D.setOnMapSteadyListener(isSteady -> {
            if (isSteady) {
                this.googleMap3D.setOnMapSteadyListener(null); // Remove listener after first steady
                new Handler(Looper.getMainLooper()).postDelayed(this::flyToEmpireStateBuilding, 2000);
            }
        });
    }

    private void flyToEmpireStateBuilding() {
        if (this.googleMap3D == null) return;
        FlyToOptions options = new FlyToOptions.Builder()
            .endCamera(new Camera.Builder()
                .center(new LatLngAltitude(
                    DataModel.EMPIRE_STATE_BUILDING_LATITUDE,
                    DataModel.EMPIRE_STATE_BUILDING_LONGITUDE,
                    212.0))
                .heading(34.0)
                .tilt(67.0)
                .range(750.0)
                .roll(0.0)
                .build())
            .durationInMillis(2000)
            .build();
        googleMap3D.flyCameraTo(options);
    }

    private void flyAroundCurrentCenter() {
        if (this.googleMap3D == null) return;
        Camera currentCamera = this.googleMap3D.getCamera();
        if (currentCamera == null) return;

        Camera validCamera = JavaCameraUtils.toValidCamera(currentCamera);

        FlyAroundOptions options = new FlyAroundOptions.Builder()
            .center(validCamera.getCenter())
            .heading(validCamera.getHeading())
            .tilt(validCamera.getTilt())
            .range(validCamera.getRange())
            .durationInMillis(5000)
            .rounds(1.0)
            .build();
        googleMap3D.flyCameraAround(options);
    }

    private void updateCameraPosition(Camera camera) {
        if (camera == null) return;
        final String nbsp = "\u00A0";

        // Latitude
        String latStr = getString(R.string.cam_lat_label, camera.getCenter().latitude);
        // Longitude
        String lngStr = getString(R.string.cam_lng_label, camera.getCenter().longitude);
        // Altitude
        String altStr = getString(R.string.cam_alt_label, camera.getCenter().altitude);
        // Heading
        String hdgStr = getString(R.string.cam_hdg_label, camera.getHeading());
        // Assuming JavaStringUtils.toCompassDirection will be created
        String compassStr = JavaStringUtils.toCompassDirection(camera.getHeading());
        // Tilt
        String tltStr = getString(R.string.cam_tlt_label, camera.getTilt());
        // Range
        String rngStr = getString(R.string.cam_rng_label, camera.getRange());

        String cameraStateString = String.format(Locale.getDefault(),
            "%s, %s, %s,\n%s%s(%s), %s, %s",
            latStr, lngStr, altStr, hdgStr, nbsp, compassStr, tltStr, rngStr);

        // Wrap roll value between -180 and 180
        final float resetValue = (float) JavaNumericUtils.wrapIn(camera.getRoll() != null ? camera.getRoll() : 0.0, -180.0, 180.0);

        runOnUiThread(() -> {
            cameraStateText.setText(cameraStateString);
            // Check if sliders are not null before setting value
            if (rollSlider != null && Math.abs(rollSlider.getValue() - resetValue) > 0.01f) {
                 rollSlider.setValue(resetValue);
            }
            if (rollSliderLabel != null) { // Added null check for rollSliderLabel
                updateRollSliderLabel(resetValue);
            }
        });
    }

    // Standard lifecycle methods delegating to this activity's map3DView
    @Override
    protected void onResume() {
        super.onResume();
        if (this.map3DView != null) this.map3DView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (this.map3DView != null) this.map3DView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (this.map3DView != null) this.map3DView.onDestroy();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (this.map3DView != null) this.map3DView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (this.map3DView != null) this.map3DView.onSaveInstanceState(outState);
    }

    // Add onError for OnMap3DViewReadyCallback if not already present
    @Override
    public void onError(Exception e) {
        Log.e(TAG, "Error with Map3DView: ", e);
    }
}
