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

import com.example.maps3djava.sampleactivity.SampleBaseActivity;
import com.google.android.gms.maps3d.GoogleMap3D;
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

        // Marker 1: Absolute
        MarkerOptions marker1Options = new MarkerOptions();
        marker1Options.setPosition(new LatLngAltitude(
                52.519605780912585,
                13.406867190588198,
                150.0
        ));
        marker1Options.setLabel("Absolute (150m)");
        marker1Options.setAltitudeMode(AltitudeMode.ABSOLUTE);
        marker1Options.setExtruded(true);
        marker1Options.setDrawnWhenOccluded(true);
        marker1Options.setCollisionBehavior(CollisionBehavior.REQUIRED_AND_HIDES_OPTIONAL);
        googleMap3D.addMarker(marker1Options);

        // Marker 2: Relative to Ground
        MarkerOptions marker2Options = new MarkerOptions();
        marker2Options.setPosition(new LatLngAltitude(
                52.519882191069016,
                13.407410777254293,
                50.0
        ));
        marker2Options.setLabel("Relative to Ground (50m)");
        marker2Options.setAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);
        marker2Options.setExtruded(true);
        marker2Options.setDrawnWhenOccluded(true);
        marker2Options.setCollisionBehavior(CollisionBehavior.OPTIONAL_AND_HIDES_LOWER_PRIORITY);
        googleMap3D.addMarker(marker2Options);

        // Marker 3: Clamped to Ground
        MarkerOptions marker3Options = new MarkerOptions();
        marker3Options.setPosition(new LatLngAltitude(
                52.52027645136134,
                13.408271658592406,
                0.0
        ));
        marker3Options.setLabel("Clamped to Ground");
        marker3Options.setAltitudeMode(AltitudeMode.CLAMP_TO_GROUND);
        marker3Options.setExtruded(true);
        marker3Options.setDrawnWhenOccluded(true);
        marker3Options.setCollisionBehavior(CollisionBehavior.REQUIRED);
        googleMap3D.addMarker(marker3Options);

        MarkerOptions marker4Options = new MarkerOptions();

        // Marker 4: Relative to Mesh
        marker4Options.setPosition(new LatLngAltitude(
                52.520835071144226,
                13.409426847943774,
                10.0
        ));
        marker4Options.setLabel("Relative to Mesh (10m)");
        marker4Options.setAltitudeMode(AltitudeMode.RELATIVE_TO_MESH);
        marker4Options.setExtruded(true);
        marker4Options.setDrawnWhenOccluded(true);
        marker4Options.setCollisionBehavior(CollisionBehavior.REQUIRED);
        googleMap3D.addMarker(marker4Options);
    }
}