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

import android.view.View;
import android.widget.Button;

import com.example.maps3djava.sampleactivity.SampleBaseActivity;
import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.model.AltitudeMode;
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.CollisionBehavior;
import com.google.android.gms.maps3d.model.FlyToOptions;
import com.google.android.gms.maps3d.model.Glyph;
import com.google.android.gms.maps3d.model.ImageView;
import com.google.android.gms.maps3d.model.LatLngAltitude;
import com.google.android.gms.maps3d.model.Map3DMode;
import com.google.android.gms.maps3d.model.MarkerOptions;
import com.google.android.gms.maps3d.model.PinConfiguration;

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

    public final Camera getBerlinCamera() {
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

    public final Camera getTokyoCamera() {
        return toValidCamera(new Camera(
                new LatLngAltitude(
                        35.658708,
                        139.702206,
                        23.3),
                117.0,
                55.0,
                0.0,
                2868.0));
    }

    @Override
    public final Camera getInitialCamera() {
        return toValidCamera(new Camera(
                new LatLngAltitude(
                        40.748425,
                        -73.985590,
                        348.7),
                22.0,
                80.0,
                0.0,
                1518.0));
    }

    @Override
    public void onMap3DViewReady(GoogleMap3D googleMap3D) {
        super.onMap3DViewReady(googleMap3D);
        googleMap3D.setMapMode(Map3DMode.SATELLITE);

        Button flyBerlinButton = findViewById(com.example.maps3dcommon.R.id.fly_berlin_button);
        if (flyBerlinButton != null) {
            runOnUiThread(() -> flyBerlinButton.setVisibility(View.VISIBLE));
            flyBerlinButton.setOnClickListener(v -> {
                FlyToOptions options = new FlyToOptions(getBerlinCamera(), 2000L);
                googleMap3D.flyCameraTo(options);
            });
        }

        Button flyNycButton = findViewById(com.example.maps3dcommon.R.id.fly_nyc_button);
        if (flyNycButton != null) {
            runOnUiThread(() -> flyNycButton.setVisibility(View.VISIBLE));
            flyNycButton.setOnClickListener(v -> {
                FlyToOptions options = new FlyToOptions(getInitialCamera(), 2000L);
                googleMap3D.flyCameraTo(options);
            });
        }

        Button flyTokyoButton = findViewById(com.example.maps3dcommon.R.id.fly_tokyo_button);
        if (flyTokyoButton != null) {
            runOnUiThread(() -> flyTokyoButton.setVisibility(View.VISIBLE));
            flyTokyoButton.setOnClickListener(v -> {
                FlyToOptions options = new FlyToOptions(getTokyoCamera(), 2000L);
                googleMap3D.flyCameraTo(options);
            });
        }

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

        MarkerOptions apeOptions = new MarkerOptions();
        apeOptions.setPosition(new LatLngAltitude(40.7484, -73.9857, 100.0));
        apeOptions.setZIndex(1);
        apeOptions.setLabel("King Kong / Empire State Building");
        apeOptions.setAltitudeMode(AltitudeMode.RELATIVE_TO_MESH);
        apeOptions.setCollisionBehavior(CollisionBehavior.REQUIRED);
        apeOptions.setExtruded(true);
        apeOptions.setDrawnWhenOccluded(true);

        apeOptions.setStyle(new ImageView(com.example.maps3dcommon.R.drawable.ook));

        com.google.android.gms.maps3d.model.Marker apeMarker = googleMap3D.addMarker(apeOptions);
        if (apeMarker != null) {
            apeMarker.setClickListener(
                    () -> MarkersActivity.this.showToast("Clicked on marker: " + apeMarker.getLabel()));
        }

        Glyph customColorGlyph = Glyph.fromColor(android.graphics.Color.CYAN);
        MarkerOptions customColorOptions = new MarkerOptions();
        customColorOptions.setPosition(new LatLngAltitude(40.7486, -73.9848, 600.0));
        customColorOptions.setExtruded(true);
        customColorOptions.setDrawnWhenOccluded(true);
        customColorOptions.setLabel("Custom Color Pin");
        customColorOptions.setAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);
        PinConfiguration.Builder colorPinBuilder = PinConfiguration.builder();
        colorPinBuilder.setBackgroundColor(android.graphics.Color.RED);
        colorPinBuilder.setBorderColor(android.graphics.Color.WHITE);
        colorPinBuilder.setGlyph(customColorGlyph);
        customColorOptions.setStyle(colorPinBuilder.build());

        com.google.android.gms.maps3d.model.Marker colorMarker = googleMap3D.addMarker(customColorOptions);
        if (colorMarker != null) {
            colorMarker.setClickListener(
                    () -> MarkersActivity.this.showToast("Clicked on marker: " + colorMarker.getLabel()));
        }

        Glyph textGlyph = Glyph.fromColor(android.graphics.Color.RED);
        textGlyph.setText("NYC\n 🍎 ");
        MarkerOptions textOptions = new MarkerOptions();
        textOptions.setPosition(new LatLngAltitude(40.7482, -73.9862, 600.0));
        textOptions.setExtruded(true);
        textOptions.setDrawnWhenOccluded(true);
        textOptions.setLabel("Custom Text Pin");
        textOptions.setAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);
        PinConfiguration.Builder textPinBuilder = PinConfiguration.builder();
        textPinBuilder.setBackgroundColor(android.graphics.Color.YELLOW);
        textPinBuilder.setBorderColor(android.graphics.Color.BLUE);
        textPinBuilder.setGlyph(textGlyph);
        textOptions.setStyle(textPinBuilder.build());

        com.google.android.gms.maps3d.model.Marker textMarker = googleMap3D.addMarker(textOptions);
        if (textMarker != null) {
            textMarker.setClickListener(
                    () -> MarkersActivity.this.showToast("Clicked on marker: " + textMarker.getLabel()));
        }

        MarkerOptions shibuyaOptions = new MarkerOptions();
        shibuyaOptions.setPosition(new LatLngAltitude(35.6595, 139.7005, 50.0));
        shibuyaOptions.setLabel("Shibuya Crossing Easter Egg");
        shibuyaOptions.setAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);
        shibuyaOptions.setExtruded(true);
        shibuyaOptions.setDrawnWhenOccluded(true);
        shibuyaOptions.setStyle(new ImageView(com.example.maps3dcommon.R.drawable.gz));

        com.google.android.gms.maps3d.model.Marker shibuyaMarker = googleMap3D.addMarker(shibuyaOptions);
        if (shibuyaMarker != null) {
            shibuyaMarker.setClickListener(
                    () -> MarkersActivity.this.showToast("Clicked on marker: " + shibuyaMarker.getLabel()));
        }

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
        marker.setClickListener(() -> MarkersActivity.this.showToast("Clicked on marker: " + label));
    }
}