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

package com.example.maps3djava.mapinteractions;

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.maps3djava.sampleactivity.SampleBaseActivity;
import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.LatLngAltitude;
import com.google.android.gms.maps3d.model.Map3DMode;

public class MapInteractionsActivity extends SampleBaseActivity {

  private static final double BOULDER_LATITUDE = 40.029349;
  private static final double BOULDER_LONGITUDE = -105.300354;

  @NonNull
  @Override
  public String getTAG() {
    return "MapInteractionsActivity";
  }

  @NonNull
  @Override
  public Camera getInitialCamera() {
    return com.example.maps3d.common.UtilitiesKt.toValidCamera(new Camera(
        new LatLngAltitude(BOULDER_LATITUDE, BOULDER_LONGITUDE, 1833.9),
        326.0,
        75.0,
        0.0,
        3757.0));
  }

  @Override
  public void onMap3DViewReady(GoogleMap3D googleMap3D) {
    super.onMap3DViewReady(googleMap3D);
    googleMap3D.setOnMapReadyListener((map) -> {
      googleMap3D.setOnMapReadyListener(null);
      onMapReady(googleMap3D);
    });
  }

  private void onMapReady(@NonNull GoogleMap3D googleMap3D) {
    googleMap3D.setMapMode(Map3DMode.HYBRID);

    // Listeners for map clicks.
    googleMap3D.setMap3DClickListener((location, placeId) -> {
      runOnUiThread(() -> {
        if (placeId != null) {
          showToast("Clicked on place with ID: " + placeId);
        } else {
          showToast("Clicked on location: " + location);
        }
      });
    });
  }

  protected void showToast(String message) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
  }
}
