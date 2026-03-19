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

package com.example.snippets.java.snippets;

import android.graphics.Color;
import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.model.AltitudeMode;
import com.google.android.gms.maps3d.model.LatLngAltitude;
import com.google.android.gms.maps3d.model.Polyline;
import com.google.android.gms.maps3d.model.PolylineOptions;
import java.util.Arrays;
import java.util.List;
import com.example.snippets.java.annotations.SnippetGroup;
import com.example.snippets.java.annotations.SnippetItem;

@SnippetGroup(
    title = "Polylines",
    description = "Snippets demonstrating 2D and 3D extruded polyline paths on the map."
)
public class PolylineSnippets {

    private final GoogleMap3D map;

    public PolylineSnippets(GoogleMap3D map) {
        this.map = map;
    }

    /**
     * Adds a basic polyline to the map.
     */
    @SuppressWarnings("unused")
    @SnippetItem(
        title = "1. Basic",
        description = "Draws a thick red polyline connecting three points near Lat: 37.42, Lng: -122.08."
    )
    public void addBasicPolyline() {
        // [START maps_android_3d_polyline_add_java]
        List<LatLngAltitude> points = Arrays.asList(
            new LatLngAltitude(37.42, -122.08, 0.0),
            new LatLngAltitude(37.43, -122.09, 0.0),
            new LatLngAltitude(37.44, -122.08, 0.0)
        );

        PolylineOptions options = new PolylineOptions();
        options.setPath(points);
        options.setStrokeColor(Color.RED);
        options.setStrokeWidth(10.0);
        options.setAltitudeMode(AltitudeMode.CLAMP_TO_GROUND);
        
        Polyline polyline = map.addPolyline(options);
        // [END maps_android_3d_polyline_add_java]
    }

    /**
     * Adds a styled polyline with complex configuration.
     */
    @SuppressWarnings("unused")
    @SnippetItem(
        title = "2. Styled",
        description = "Draws a magenta polyline with a green outline, extruded and following the ground curvature (geodesic), connecting two points."
    )
    public void addStyledPolyline() {
        // [START maps_android_3d_polyline_options_java]
        List<LatLngAltitude> points = Arrays.asList(
            new LatLngAltitude(37.42, -122.08, 50.0),
            new LatLngAltitude(37.43, -122.09, 100.0)
        );

        PolylineOptions options = new PolylineOptions();
        options.setPath(points);
        options.setStrokeColor(0xFFFF00FF); // Magenta
        options.setStrokeWidth(20.0);
        options.setOuterColor(0xFF00FF00); // Green
        options.setOuterWidth(2.0);
        options.setAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);
        options.setExtruded(true);
        options.setGeodesic(true);
        options.setDrawsOccludedSegments(true);
        
        Polyline polyline = map.addPolyline(options);
        // [END maps_android_3d_polyline_options_java]
    }
}
