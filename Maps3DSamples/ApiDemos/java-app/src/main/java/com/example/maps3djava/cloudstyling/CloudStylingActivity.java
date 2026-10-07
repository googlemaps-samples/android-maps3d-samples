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
import com.example.maps3dcommon.R;
import com.example.maps3djava.sampleactivity.SampleBaseActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.LatLngAltitude;
import com.google.android.gms.maps3d.model.Map3DMode;
import com.google.android.material.appbar.MaterialToolbar;

/**
 * Showcases **Cloud-Based Map Styling** in Google Maps 3D SDK (Java implementation). Uses
 * standalone layout [R.layout.activity_cloud_styling] with declarative custom Map ID.
 */
public class CloudStylingActivity extends SampleBaseActivity {

  public static final LatLng SF_LOCATION = new LatLng(37.7915, -122.4010);

  private int currentMapMode = Map3DMode.ROADMAP;

  @NonNull
  @Override
  public String getTAG() {
    return getClass().getSimpleName();
  }

  @NonNull
  @Override
  public Camera getInitialCamera() {
    return toValidCamera(new Camera(
        new LatLngAltitude(SF_LOCATION.latitude, SF_LOCATION.longitude, 250.0),
        45.0,
        65.0,
        0.0,
        800.0
    ));
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Set dedicated standalone layout with declarative mapId="9a35234a36da44d2c47bf626"
    setContentView(R.layout.activity_cloud_styling);

    MaterialToolbar topBar = findViewById(R.id.top_bar);
    if (topBar != null) {
      topBar.setTitle(R.string.feature_title_cloud_styling);
      topBar.setNavigationOnClickListener(v -> finish());
    }

    map3DView = findViewById(R.id.map3dView);
    if (map3DView != null) {
      map3DView.onCreate(savedInstanceState);
      map3DView.getMap3DViewAsync(this);
    }

    RadioGroup rgMapMode = findViewById(R.id.rg_map_mode);
    if (rgMapMode != null) {
      rgMapMode.setOnCheckedChangeListener((group, checkedId) -> {
        int newMode = Map3DMode.ROADMAP;
        if (checkedId == R.id.rb_hybrid) {
          newMode = Map3DMode.HYBRID;
        } else if (checkedId == R.id.rb_satellite) {
          newMode = Map3DMode.SATELLITE;
        }
        currentMapMode = newMode;
        if (googleMap3D != null) {
          googleMap3D.setMapMode(newMode);
        }
      });
    }
  }

  @Override
  public void onMap3DViewReady(@NonNull GoogleMap3D googleMap3D) {
    super.onMap3DViewReady(googleMap3D);
    googleMap3D.setMapMode(currentMapMode);
  }
}
