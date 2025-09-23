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

package com.example.maps3djava.markers;

import static com.example.maps3d.common.UtilitiesKt.toValidCamera;

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.maps3djava.sampleactivity.SampleBaseActivity;
import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.OnMarkerClickListener;
import com.google.android.gms.maps3d.model.AltitudeMode;
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.CollisionBehavior;
import com.google.android.gms.maps3d.model.LatLngAltitude;
import com.google.android.gms.maps3d.model.Map3DMode;
import com.google.android.gms.maps3d.model.MarkerOptions;

/**
 * Demonstrates the use of different altitude modes for markers in a 3D map.
 * <p>
 * This activity showcases four markers with different altitude modes:
 * - **ABSOLUTE:** Marker at a fixed altitude above sea level.
 * - **RELATIVE_TO_GROUND:** Marker at a fixed height above the ground.
 * - **CLAMP_TO_GROUND:** Marker is always attached to the ground.
 * - **RELATIVE_TO_MESH:** Marker's altitude is relative to the 3D mesh (buildings, terrain features).
 */
public class MarkersActivity extends SampleBaseActivity {

    @Override
    public final String getTAG() {
        return this.getClass().getSimpleName();
    }

    @Override
    public final Camera getInitialCamera() {
        return toValidCamera(new Camera(
                new LatLngAltitude(
                        52.51974795,
                        13.40715553,
                        150.0
                ),
                252.7,
                79.0,
                0.0,
                1500.0
        ));
    }

    @Override
    public void onMap3DViewReady(GoogleMap3D googleMap3D) {
        super.onMap3DViewReady(googleMap3D);
        googleMap3D.setMapMode(Map3DMode.SATELLITE);

        addMarkerWithToastListener(googleMap3D,
            new LatLngAltitude(52.519605780912585, 13.406867190588198, 150.0),
            "Absolute (150m)",
            AltitudeMode.ABSOLUTE,
            CollisionBehavior.REQUIRED_AND_HIDES_OPTIONAL
        );

        addMarkerWithToastListener(googleMap3D,
            new LatLngAltitude(52.519882191069016, 13.407410777254293, 50.0),
            "Relative to Ground (50m)",
            AltitudeMode.RELATIVE_TO_GROUND,
            CollisionBehavior.OPTIONAL_AND_HIDES_LOWER_PRIORITY
        );

        addMarkerWithToastListener(googleMap3D,
            new LatLngAltitude(52.52027645136134, 13.408271658592406, 0.0),
            "Clamped to Ground",
            AltitudeMode.CLAMP_TO_GROUND,
            CollisionBehavior.REQUIRED
        );

        addMarkerWithToastListener(googleMap3D,
            new LatLngAltitude(52.520835071144226, 13.409426847943774, 10.0),
            "Relative to Mesh (10m)",
            AltitudeMode.RELATIVE_TO_MESH,
            CollisionBehavior.REQUIRED
        );
    }

    private void addMarkerWithToastListener(
        GoogleMap3D map,
        LatLngAltitude position,
        String label,
        int altitudeMode,
        int collisionBehavior
    ) {
        MarkerOptions options = new MarkerOptions();
        options.setPosition(position);
        options.setLabel(label);
        options.setAltitudeMode(altitudeMode);
        options.setCollisionBehavior(collisionBehavior);
        options.setExtruded(true);
        options.setDrawnWhenOccluded(true);

        com.google.android.gms.maps3d.model.Marker marker = map.addMarker(options);
        marker.setClickListener(() ->
                MarkersActivity.this.runOnUiThread(() ->
                        Toast.makeText(MarkersActivity.this,
                                "Clicked on marker: " + label, Toast.LENGTH_SHORT).show())
        );
    }
}