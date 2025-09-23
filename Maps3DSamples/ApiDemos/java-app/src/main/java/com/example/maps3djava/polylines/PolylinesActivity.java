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

package com.example.maps3djava.polylines;

import static com.example.maps3djava.polylines.SanitasLoopData.sanitasLoop;

import android.graphics.Color;

import androidx.annotation.NonNull;

import com.example.maps3djava.R;
import com.example.maps3djava.sampleactivity.SampleBaseActivity;
import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback;
import com.google.android.gms.maps3d.model.AltitudeMode;
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.LatLngAltitude;
import com.google.android.gms.maps3d.model.Map3DMode;
import com.google.android.gms.maps3d.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity that demonstrates the use of polylines on a 3D map.
 * <p>
 * This activity displays a polyline representing a trail (Sanitas Loop) on a
 * [Map3DView]. It uses two polylines to create a stroked effect, with a
 * wider, slightly translucent black polyline behind a narrower red polyline.
 * It also includes buttons to take a snapshot of the map and reset the view to the initial camera position.
 * <p>
 * The activity implements [OnMap3DViewReadyCallback] to receive a callback when the
 * [Map3DView] is ready to be used. It also handles the lifecycle events of the
 * [Map3DView] to ensure proper resource management.
 */
public class PolylinesActivity extends SampleBaseActivity implements OnMap3DViewReadyCallback {
    private final PolylineOptions trailForegroundPolylineOptions;
    private final int trailBackground = Color.argb(128, 0, 0, 0);
    private final PolylineOptions trailBackgroundPolylineOptions;
    private static final List<LatLngAltitude> trailLocations = new ArrayList<>();
    private static final double BOULDER_LATITUDE = 40.029349;
    private static final double BOULDER_LONGITUDE = -105.300354;

    static {
        String[] lines = sanitasLoop.split("\n");
        for (String line : lines) {
            if (line.trim().isEmpty()) {
                continue;
            }
            String[] latLng = line.split(",");
            if (latLng.length == 2) {
                try {
                    double latitude = Double.parseDouble(latLng[0].trim());
                    double longitude = Double.parseDouble(latLng[1].trim());
                    trailLocations.add(new LatLngAltitude(latitude, longitude, 0.0));
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing latitude/longitude in line: " + line + ". " + e.getMessage());
                }
            } else {
                System.err.println("Invalid format in line: " + line + ". Expected 'latitude,longitude'.");
            }
        }
    }

    @Override
    public Camera getInitialCamera() {
        return new Camera(new LatLngAltitude(BOULDER_LATITUDE, BOULDER_LONGITUDE, 1833.9), 326.0, 75.0, 0.0, 3757.0);
    }

    @Override
    public String getTAG() {
        return this.getClass().getSimpleName();
    }

    public PolylinesActivity() {
        trailForegroundPolylineOptions = new PolylineOptions();
        trailForegroundPolylineOptions.setPath(trailLocations);
        trailForegroundPolylineOptions.setStrokeColor(Color.RED);
        trailForegroundPolylineOptions.setStrokeWidth(7.0);
        trailForegroundPolylineOptions.setAltitudeMode(AltitudeMode.CLAMP_TO_GROUND);
        trailForegroundPolylineOptions.setZIndex(5);
        trailForegroundPolylineOptions.setDrawsOccludedSegments(true);

        trailBackgroundPolylineOptions = new PolylineOptions();
        trailBackgroundPolylineOptions.setPath(trailLocations);
        trailBackgroundPolylineOptions.setStrokeColor(trailBackground);
        trailBackgroundPolylineOptions.setStrokeWidth(13.0);
        trailBackgroundPolylineOptions.setAltitudeMode(AltitudeMode.CLAMP_TO_GROUND);
        trailBackgroundPolylineOptions.setZIndex(3);
        trailBackgroundPolylineOptions.setDrawsOccludedSegments(true);
    }

    /**
     * Called when the Map3DView is ready to be used. This is where you can add polylines,
     * set map modes, and perform other map-related operations.
     *
     * @param googleMap3D The GoogleMap3D object that is ready.
     */
    @Override
    public void onMap3DViewReady(@NonNull GoogleMap3D googleMap3D) {
        super.onMap3DViewReady(googleMap3D);
        googleMap3D.setMapMode(Map3DMode.HYBRID);
        googleMap3D.addPolyline(trailBackgroundPolylineOptions);
        com.google.android.gms.maps3d.model.Polyline foregroundPolyline = googleMap3D.addPolyline(trailForegroundPolylineOptions);
        foregroundPolyline.setClickListener(() -> showToast(getString(R.string.polyline_trail_clicked)));
    }
}