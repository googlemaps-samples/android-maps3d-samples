/*
 * Copyright 2025 Google LLC
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
import com.google.android.gms.maps3d.model.Polygon;
import com.google.android.gms.maps3d.model.PolygonOptions;
import java.util.Arrays;
import java.util.List;

public class PolygonSnippets {

    private final GoogleMap3D map;

    public PolygonSnippets(GoogleMap3D map) {
        this.map = map;
    }

    // [START maps_android_3d_polygon_add_java]
    /**
     * Adds a simple polygon to the map.
     */
    public void addBasicPolygon() {
        List<LatLngAltitude> points = Arrays.asList(
            new LatLngAltitude(37.42, -122.08, 0.0),
            new LatLngAltitude(37.42, -122.09, 0.0),
            new LatLngAltitude(37.43, -122.09, 0.0),
            new LatLngAltitude(37.43, -122.08, 0.0),
            new LatLngAltitude(37.42, -122.08, 0.0)
        );

        PolygonOptions options = new PolygonOptions();
        options.setPath(points);
        options.setFillColor(Color.RED);
        options.setStrokeColor(Color.BLUE);
        options.setStrokeWidth(5.0);
        options.setAltitudeMode(AltitudeMode.CLAMP_TO_GROUND);
        
        Polygon polygon = map.addPolygon(options);
    }
    // [END maps_android_3d_polygon_add_java]

    // [START maps_android_3d_polygon_extruded_java]
    /**
     * Adds an extruded polygon with transparency.
     */
    public void addExtrudedPolygon() {
        List<LatLngAltitude> points = Arrays.asList(
            new LatLngAltitude(37.42, -122.08, 50.0),
            new LatLngAltitude(37.42, -122.09, 50.0),
            new LatLngAltitude(37.43, -122.09, 50.0),
            new LatLngAltitude(37.43, -122.08, 50.0),
            new LatLngAltitude(37.42, -122.08, 50.0)
        );

        PolygonOptions options = new PolygonOptions();
        options.setPath(points);
        options.setFillColor(0x88FF0000); // Semi-transparent red
        options.setExtruded(true);
        options.setGeodesic(true);
        options.setAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);
        
        Polygon polygon = map.addPolygon(options);
    }
    // [END maps_android_3d_polygon_extruded_java]
}
