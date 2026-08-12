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

package com.example.maps3djava.fieldofview;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.maps3d.common.UtilitiesKt;
import com.example.maps3dcommon.R;
import com.example.maps3djava.sampleactivity.SampleBaseActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.LatLngAltitude;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.slider.Slider;

/**
 * Showcases Field of View (FOV) perspective scaling in Google Maps 3D SDK (Java implementation).
 *
 * Features:
 * - Interactive FOV slider and quick presets preserving current active map location smoothly.
 * - Robust toValidCamera validation preventing out-of-range heading/tilt crash during manual map rotation.
 */
public class FieldOfViewActivity extends SampleBaseActivity {

    public static final LatLng SF_FINANCIAL_DISTRICT = new LatLng(37.7952, -122.4028);

    private TextView fovSliderLabel;
    private Slider fovSlider;
    private double currentFov = 45.0;

    @NonNull
    @Override
    public String getTAG() {
        return "FieldOfViewActivity";
    }

    @NonNull
    @Override
    public Camera getInitialCamera() {
        return UtilitiesKt.toValidCamera(new Camera(
                new LatLngAltitude(SF_FINANCIAL_DISTRICT.latitude, SF_FINANCIAL_DISTRICT.longitude, 150.0),
                45.0,
                65.0,
                0.0,
                800.0
        ));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate FOV control panel overlay into map container managed by SampleBaseActivity
        ViewGroup container = findViewById(R.id.map_container);
        if (container != null) {
            getLayoutInflater().inflate(R.layout.control_panel_field_of_view, container, true);
        }

        MaterialToolbar topBar = findViewById(R.id.top_bar);
        if (topBar != null) {
            topBar.setTitle("Field of View (FOV)");
            topBar.setNavigationOnClickListener(v -> finish());
        }

        fovSliderLabel = findViewById(R.id.fov_slider_label);
        fovSlider = findViewById(R.id.fov_slider);

        if (fovSlider != null) {
            fovSlider.addOnChangeListener((slider, value, fromUser) -> updateFov(value));
        }

        Button btnTele = findViewById(R.id.btn_fov_telephoto);
        if (btnTele != null) {
            btnTele.setOnClickListener(v -> { if (fovSlider != null) fovSlider.setValue(20.0f); });
        }

        Button btnStd = findViewById(R.id.btn_fov_standard);
        if (btnStd != null) {
            btnStd.setOnClickListener(v -> { if (fovSlider != null) fovSlider.setValue(45.0f); });
        }

        Button btnWide = findViewById(R.id.btn_fov_wide);
        if (btnWide != null) {
            btnWide.setOnClickListener(v -> { if (fovSlider != null) fovSlider.setValue(90.0f); });
        }

        Button btnUltra = findViewById(R.id.btn_fov_ultrawide);
        if (btnUltra != null) {
            btnUltra.setOnClickListener(v -> { if (fovSlider != null) fovSlider.setValue(120.0f); });
        }
    }

    @Override
    public void onMap3DViewReady(@NonNull GoogleMap3D googleMap3D) {
        super.onMap3DViewReady(googleMap3D);
        googleMap3D.setOnMapReadyListener(sceneReadiness -> {
            googleMap3D.setOnMapReadyListener(null);
            runOnUiThread(() -> updateFov((float) currentFov));
        });
    }

    private void updateFov(float fovAngle) {
        currentFov = fovAngle;
        runOnUiThread(() -> {
            if (fovSliderLabel != null) {
                fovSliderLabel.setText("Field of View: " + (int) fovAngle + "°");
            }
        });

        if (googleMap3D != null) {
            Camera liveCam = googleMap3D.getCamera() != null ? UtilitiesKt.toValidCamera(googleMap3D.getCamera()) : null;
            Camera currCam = (liveCam != null && (Math.abs(liveCam.getCenter().getLatitude()) > 0.001 || Math.abs(liveCam.getCenter().getLongitude()) > 0.001))
                    ? liveCam
                    : getInitialCamera();
            double baseFovRad = Math.toRadians(45.0 / 2.0);
            double targetFovRad = Math.toRadians(fovAngle / 2.0);

            double targetRange = 800.0 * Math.tan(baseFovRad) / Math.tan(targetFovRad);
            if (targetRange < 150.0) targetRange = 150.0;
            if (targetRange > 3000.0) targetRange = 3000.0;

            Camera updatedCam = UtilitiesKt.toValidCamera(new Camera(
                    currCam.getCenter(),
                    currCam.getHeading(),
                    currCam.getTilt(),
                    currCam.getRoll(),
                    targetRange
            ));
            googleMap3D.setCamera(updatedCam);
        }
    }
}
