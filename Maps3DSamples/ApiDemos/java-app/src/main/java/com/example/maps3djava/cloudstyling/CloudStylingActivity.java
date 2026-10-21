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

package com.example.maps3djava.cloudstyling;

import static com.example.maps3d.common.UtilitiesKt.toValidCamera;

import android.os.Bundle;
import android.widget.RadioGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.maps3d.common.Map3DSettings;
import com.example.maps3d.common.Map3DSettingsController;
import com.example.maps3dcommon.R;
import com.example.maps3djava.sampleactivity.SampleBaseActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.FlyToOptions;
import com.google.android.gms.maps3d.model.LatLngAltitude;
import com.google.android.gms.maps3d.model.Map3DMode;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

/**
 * Showcases **Cloud-Based Map Styling** in Google Maps 3D SDK (Java implementation). Uses
 * standalone layout [R.layout.activity_cloud_styling] with declarative custom Map ID.
 */
public class CloudStylingActivity extends SampleBaseActivity {

    public static final LatLng SF_LOCATION = new LatLng(37.7915, -122.4010);
    private static final double TILT_2D_ROADMAP = 0.0;
    private static final double TILT_3D_HYBRID = 65.0;
    private static final long MODE_SWITCH_DURATION_MS = 1_000L;

    @Map3DMode
    private int currentMapMode = Map3DMode.ROADMAP;
    private RadioGroup rgMapMode;
    private MaterialButton btnToggle2D3D;
    private boolean isUpdatingModeUI = false;
    private boolean hasSyncedInitialMode = false;

    @NonNull
    @Override
    public String getTAG() {
        return getClass().getSimpleName();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_cloud_styling;
    }

    @Override
    @Map3DMode
    protected int getDefaultMapMode() {
        return Map3DMode.ROADMAP;
    }

    @NonNull
    @Override
    public Camera getInitialCamera() {
        double initialTilt =
                Map3DSettingsController.getShared().getSettings().getMapMode() == Map3DMode.ROADMAP
                        ? TILT_2D_ROADMAP
                        : TILT_3D_HYBRID;
        return toValidCamera(new Camera(
                new LatLngAltitude(SF_LOCATION.latitude, SF_LOCATION.longitude, 0.0),
                45.0,
                initialTilt,
                0.0,
                2500.0
        ));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Map3DSettingsController.getShared().onSampleEntered(getDefaultMapMode());
        currentMapMode = Map3DSettingsController.getShared().getSettings().getMapMode();
        super.onCreate(savedInstanceState);

        MaterialToolbar topBar = findViewById(R.id.top_bar);
        if (topBar != null) {
            topBar.setTitle(R.string.feature_title_cloud_styling);
            topBar.setNavigationOnClickListener(v -> finish());
        }

        rgMapMode = findViewById(R.id.rg_map_mode);
        btnToggle2D3D = findViewById(R.id.btn_toggle_2d_3d);

        if (btnToggle2D3D != null) {
            btnToggle2D3D.setOnClickListener(v -> {
                int nextMode = (currentMapMode == Map3DMode.ROADMAP)
                        ? Map3DMode.HYBRID
                        : Map3DMode.ROADMAP;
                applyMapModeAndTilt(nextMode);
            });
        }

        if (rgMapMode != null) {
            rgMapMode.setOnCheckedChangeListener((group, checkedId) -> {
                if (isUpdatingModeUI) {
                    return;
                }
                int newMode = Map3DMode.ROADMAP;
                if (checkedId == R.id.rb_hybrid) {
                    newMode = Map3DMode.HYBRID;
                } else if (checkedId == R.id.rb_satellite) {
                    newMode = Map3DMode.SATELLITE;
                }
                applyMapModeAndTilt(newMode);
            });
        }

        updateModeUI(currentMapMode);
    }

    private void applyMapModeAndTilt(@Map3DMode int newMode) {
        currentMapMode = newMode;
        Map3DSettingsController.getShared().setMapMode(newMode);
        updateModeUI(newMode);
        GoogleMap3D map = googleMap3D;
        if (map != null) {
            map.setMapMode(newMode);
            double targetTilt = (newMode == Map3DMode.ROADMAP) ? TILT_2D_ROADMAP : TILT_3D_HYBRID;
            Camera updatedCamera = toValidCamera(new Camera(
                    new LatLngAltitude(SF_LOCATION.latitude, SF_LOCATION.longitude, 0.0),
                    45.0,
                    targetTilt,
                    0.0,
                    2500.0
            ));
            FlyToOptions options = new FlyToOptions(updatedCamera, MODE_SWITCH_DURATION_MS);
            map.flyCameraTo(options);
        }
    }

    @Override
    protected void onQuickSettingsChanged(@NonNull Map3DSettings settings) {
        boolean modeChangedAfterInit =
                hasSyncedInitialMode && (currentMapMode != settings.getMapMode());
        hasSyncedInitialMode = true;
        if (modeChangedAfterInit) {
            applyMapModeAndTilt(settings.getMapMode());
        } else {
            currentMapMode = settings.getMapMode();
            updateModeUI(currentMapMode);
        }
    }

    private void updateModeUI(@Map3DMode int mode) {
        isUpdatingModeUI = true;
        int targetRadioId = R.id.rb_roadmap;
        if (mode == Map3DMode.HYBRID) {
            targetRadioId = R.id.rb_hybrid;
        } else if (mode == Map3DMode.SATELLITE) {
            targetRadioId = R.id.rb_satellite;
        }
        if (rgMapMode != null && rgMapMode.getCheckedRadioButtonId() != targetRadioId) {
            rgMapMode.check(targetRadioId);
        }
        if (btnToggle2D3D != null) {
            btnToggle2D3D.setText(mode == Map3DMode.ROADMAP
                    ? R.string.store_locator_switch_to_3d
                    : R.string.store_locator_switch_to_2d);
        }
        isUpdatingModeUI = false;
    }

    @Override
    protected void onMapReady(@NonNull GoogleMap3D googleMap3D) {
        super.onMapReady(googleMap3D);
        googleMap3D.setMapMode(currentMapMode);
    }
}
