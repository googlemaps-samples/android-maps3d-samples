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

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import com.example.snippets.java.TrackedMap3D;
import com.example.snippets.java.annotations.SnippetGroup;
import com.example.snippets.java.annotations.SnippetItem;
import com.google.android.gms.maps3d.model.AltitudeMode;
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.FlyToOptions;
import com.google.android.gms.maps3d.model.Hole;
import com.google.android.gms.maps3d.model.LatLngAltitude;
import com.google.android.gms.maps3d.model.Polygon;
import com.google.android.gms.maps3d.model.PolygonOptions;
import java.util.Arrays;
import java.util.List;

@SnippetGroup(
        title = "Polygons",
        description = "Snippets demonstrating 2D and 3D extruded polygon layers on the map.")
public class PolygonSnippets {

    private final Context context;
    private final TrackedMap3D map;

    public PolygonSnippets(Context context, TrackedMap3D map) {
        this.context = context;
        this.map = map;
    }

    /** Adds a simple polygon to the map. */
    @SuppressWarnings("unused")
    @SnippetItem(
            title = "1. Basic",
            description = "Draws a red polygon with a blue stroke around a small area")
    public void addBasicPolygon() {
        // [START maps_android_3d_polygon_add_java]
        List<LatLngAltitude> points =
                Arrays.asList(
                        new LatLngAltitude(37.42, -122.08, 0.0),
                        new LatLngAltitude(37.42, -122.09, 0.0),
                        new LatLngAltitude(37.43, -122.09, 0.0),
                        new LatLngAltitude(37.43, -122.08, 0.0),
                        new LatLngAltitude(37.42, -122.08, 0.0));

        PolygonOptions options = new PolygonOptions();
        options.setPath(points);
        options.setFillColor(Color.RED);
        options.setStrokeColor(Color.BLUE);
        options.setStrokeWidth(5.0);
        options.setAltitudeMode(AltitudeMode.CLAMP_TO_GROUND);

        Polygon polygon = map.addPolygon(options);
        // [START_EXCLUDE]
        polygon.setClickListener(
                () -> {
                    new Handler(Looper.getMainLooper())
                            .post(
                                    () -> {
                                        Toast.makeText(
                                                        context,
                                                        "Polygon Clicked!",
                                                        Toast.LENGTH_SHORT)
                                                .show();
                                    });
                });
        // [END_EXCLUDE]
        // [END maps_android_3d_polygon_add_java]

        Camera cam =
                new Camera(
                        new LatLngAltitude(37.424968, -122.084874, 19.90), 0.0, 45.02, 0.0, 4643.0);
        FlyToOptions flyTo = new FlyToOptions(cam, 1000L);
        map.flyCameraTo(flyTo);
    }

    /** Adds an extruded polygon with transparency. */
    @SuppressWarnings("unused")
    @SnippetItem(
            title = "2. Extruded",
            description =
                    "Draws a semi-transparent red extruded polygon (height 50m) around a small area")
    public void addExtrudedPolygon() {
        // [START maps_android_3d_polygon_extruded_java]
        List<LatLngAltitude> points =
                Arrays.asList(
                        new LatLngAltitude(37.42, -122.08, 50.0),
                        new LatLngAltitude(37.42, -122.09, 50.0),
                        new LatLngAltitude(37.43, -122.09, 50.0),
                        new LatLngAltitude(37.43, -122.08, 50.0),
                        new LatLngAltitude(37.42, -122.08, 50.0));

        PolygonOptions options = new PolygonOptions();
        options.setPath(points);
        options.setFillColor(0x88FF0000); // Semi-transparent red
        options.setExtruded(true);
        options.setGeodesic(true);
        options.setAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);

        Polygon polygon = map.addPolygon(options);
        // [START_EXCLUDE]
        polygon.setClickListener(
                () -> {
                    new Handler(Looper.getMainLooper())
                            .post(
                                    () -> {
                                        Toast.makeText(
                                                        context,
                                                        "Polygon Clicked!",
                                                        Toast.LENGTH_SHORT)
                                                .show();
                                    });
                });
        // [END_EXCLUDE]
        // [END maps_android_3d_polygon_extruded_java]

        Camera cam =
                new Camera(
                        new LatLngAltitude(37.424968, -122.084874, 19.90), 0.0, 45.02, 0.0, 4643.0);
        FlyToOptions flyTo = new FlyToOptions(cam, 1000L);
        map.flyCameraTo(flyTo);
    }

    /** Adds a polygon with a hole cutout. */
    @SuppressWarnings("unused")
    @SnippetItem(
            title = "3. Polygon with Hole",
            description = "Draws a polygon with an interior hole cutout.")
    public void addPolygonWithHole() {
        // [START maps_android_3d_polygon_hole_java]
        List<LatLngAltitude> outerPoints =
                Arrays.asList(
                        new LatLngAltitude(37.422, -122.084, 0.0),
                        new LatLngAltitude(37.422, -122.086, 0.0),
                        new LatLngAltitude(37.424, -122.086, 0.0),
                        new LatLngAltitude(37.424, -122.084, 0.0),
                        new LatLngAltitude(37.422, -122.084, 0.0));

        List<LatLngAltitude> innerPoints =
                Arrays.asList(
                        new LatLngAltitude(37.4225, -122.0845, 0.0),
                        new LatLngAltitude(37.4225, -122.0855, 0.0),
                        new LatLngAltitude(37.4235, -122.0855, 0.0),
                        new LatLngAltitude(37.4235, -122.0845, 0.0),
                        new LatLngAltitude(37.4225, -122.0845, 0.0));

        PolygonOptions options = new PolygonOptions();
        options.setPath(outerPoints);
        options.setFillColor(Color.GREEN);
        options.setAltitudeMode(AltitudeMode.CLAMP_TO_GROUND);

        // Core logic: create a Hole object and set InnerPaths (Optional)
        Hole innerHole = new Hole(innerPoints);
        options.setInnerPaths(List.of(innerHole));

        Polygon polygon = map.addPolygon(options);
        // [START_EXCLUDE]
        polygon.setClickListener(
                () -> {
                    new Handler(Looper.getMainLooper())
                            .post(
                                    () -> {
                                        Toast.makeText(
                                                        context,
                                                        "Polygon Clicked!",
                                                        Toast.LENGTH_SHORT)
                                                .show();
                                    });
                });
        // [END_EXCLUDE]
        // [END maps_android_3d_polygon_hole_java]

        Camera cam =
                new Camera(
                        new LatLngAltitude(37.423600, -122.085098, 4.31),
                        0.00,
                        45.00,
                        0.00,
                        1085.51);
        FlyToOptions flyTo = new FlyToOptions(cam, 1000L);
        map.flyCameraTo(flyTo);
    }
}
