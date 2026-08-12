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

package com.example.maps3djava.roadmapmode;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.maps3dcommon.R;
import com.example.maps3djava.sampleactivity.SampleBaseActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.LatLngAltitude;
import com.google.android.gms.maps3d.model.Map3DMode;
import com.google.android.material.appbar.MaterialToolbar;

/**
 * Showcases 3D Roadmap mode in Google Maps 3D SDK (Java implementation).
 * Features: - Switching between ROADMAP, HYBRID, and SATELLITE 3D render modes focused on San
 * Francisco.
 */
public class RoadmapModeActivity extends SampleBaseActivity {

  public static final LatLng SF_LOCATION = new LatLng(37.7915, -122.4010);

  @NonNull
  @Override
  public String getTAG() {
    return "RoadmapModeActivity";
  }

  @NonNull
  @Override
  public Camera getInitialCamera() {
    return new Camera(
        new LatLngAltitude(SF_LOCATION.latitude, SF_LOCATION.longitude, 250.0),
        45.0,
        65.0,
        0.0,
        800.0
    );
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Inflate roadmap control panel overlay into map container managed by SampleBaseActivity
    ViewGroup container = findViewById(R.id.map_container);
    if (container != null) {
      getLayoutInflater().inflate(R.layout.control_panel_roadmap_mode, container, true);
    }

    MaterialToolbar topBar = findViewById(R.id.top_bar);
    if (topBar != null) {
      topBar.setTitle("3D Roadmap Mode");
      topBar.setNavigationOnClickListener(v -> finish());
    }

    RadioGroup rgMapMode = findViewById(R.id.rg_map_mode);
    if (rgMapMode != null) {
      rgMapMode.setOnCheckedChangeListener((group, checkedId) -> {
        if (googleMap3D != null) {
          if (checkedId == R.id.rb_roadmap) {
            googleMap3D.setMapMode(Map3DMode.ROADMAP);
          } else if (checkedId == R.id.rb_hybrid) {
            googleMap3D.setMapMode(Map3DMode.HYBRID);
          } else if (checkedId == R.id.rb_satellite) {
            googleMap3D.setMapMode(Map3DMode.SATELLITE);
          }
        }
      });
    }
  }

  @Override
  public void onMap3DViewReady(@NonNull GoogleMap3D googleMap3D) {
    super.onMap3DViewReady(googleMap3D);
    googleMap3D.setOnMapReadyListener((map) -> {
      googleMap3D.setOnMapReadyListener(null);
      googleMap3D.setMapMode(Map3DMode.ROADMAP);
      googleMap3D.setCamera(getInitialCamera());
    });

  }
}
