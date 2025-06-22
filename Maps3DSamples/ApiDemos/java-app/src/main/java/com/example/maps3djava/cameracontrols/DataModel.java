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

package com.example.maps3djava.cameracontrols;

import android.graphics.Color;

import com.google.android.gms.maps3d.model.AltitudeMode;
import com.google.android.gms.maps3d.model.CameraRestriction;
import com.google.android.gms.maps3d.model.LatLngAltitude;
import com.google.android.gms.maps3d.model.LatLngBounds;
import com.google.android.gms.maps3d.model.PolygonOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DataModel is an object that holds constants and calculated values related to the New York City
 * area, specifically for camera restrictions and polygon definitions.
 */
public final class DataModel {
    public static final double EMPIRE_STATE_BUILDING_LATITUDE = 40.748233;
    public static final double EMPIRE_STATE_BUILDING_LONGITUDE = -73.985663;

    private static final double NYC_SOUTH_WEST_LAT = 40.68563088976172;
    private static final double NYC_SOUTH_WEST_LNG = -74.05030430240065;
    private static final double NYC_NORTH_EAST_LAT = 40.85649214337128;
    private static final double NYC_NORTH_EAST_LNG = -73.80240973771173;
    private static final double MAX_ALTITUDE_NYC_METERS = 10000.0;
    private static final double MIN_ALTITUDE_NYC_METERS = 500.0;

    private static final LatLngBounds nycBounds = new LatLngBounds(NYC_NORTH_EAST_LAT,
            NYC_NORTH_EAST_LNG,
            NYC_SOUTH_WEST_LAT,
            NYC_SOUTH_WEST_LNG);


    /**
     * Defines the camera restrictions for the NYC area.
     * <p>
     * This restriction enforces that the camera must:
     * - Maintain an altitude between [MIN_ALTITUDE_NYC_METERS] and [MAX_ALTITUDE_NYC_METERS].
     * - Stay within the geographic bounds defined by [nycBounds].
     */
    public static final CameraRestriction nycCameraRestriction = new CameraRestriction(
            MIN_ALTITUDE_NYC_METERS,
            MAX_ALTITUDE_NYC_METERS,
            0.0,
            360.0,
            0.0,
            90.0,
            nycBounds
    );

    /**
     * Defines the base face of a polygon representing the ground area of New York City.
     * This face is defined by four LatLngAltitude points that form a rectangle.
     * All points share the same minimum altitude.
     */
    private static final List<LatLngAltitude> baseFace = List.of(new LatLngAltitude(NYC_SOUTH_WEST_LAT, NYC_SOUTH_WEST_LNG, MIN_ALTITUDE_NYC_METERS), new LatLngAltitude(NYC_SOUTH_WEST_LAT, NYC_NORTH_EAST_LNG, MIN_ALTITUDE_NYC_METERS), new LatLngAltitude(NYC_NORTH_EAST_LAT, NYC_NORTH_EAST_LNG, MIN_ALTITUDE_NYC_METERS), new LatLngAltitude(NYC_NORTH_EAST_LAT, NYC_SOUTH_WEST_LNG, MIN_ALTITUDE_NYC_METERS));

    private static final List<List<LatLngAltitude>> extrudedNyc = extrudePolygon(baseFace, MAX_ALTITUDE_NYC_METERS);


    private static final int faceFillColor = Color.argb(70, 0, 120, 255); // Semi-transparent blue
    private static final int faceStrokeColor = Color.rgb(0, 80, 200);   // Solid darker blue
    private static final double faceStrokeWidth = 3.0;

    // Static initializer block to calculate nycPolygonOptions based on extrudedNyc
    public static final List<PolygonOptions> nycPolygonOptions = Collections.unmodifiableList(
            extrudedNyc.stream()
                    .map(facePoints -> {
                        PolygonOptions options = new PolygonOptions();
                        options.setOuterCoordinates(facePoints);
                        options.setFillColor(faceFillColor);
                        options.setStrokeColor(faceStrokeColor);
                        options.setStrokeWidth(faceStrokeWidth);
                        options.setAltitudeMode(AltitudeMode.ABSOLUTE);
                        options.setGeodesic(false);
                        options.setDrawsOccludedSegments(true);
                        return options;
                    })
                    .collect(Collectors.toList())
    );

    /**
     * Extrudes a flat polygon (defined by basePoints, all at the same altitude)
     * upwards by a given extrusionHeight to form a 3D prism.
     *
     * @param basePoints      A list of LatLngAltitude points defining the base polygon.
     *                        All points must have the same altitude.
     *                        The order of points defines the winding (e.g., clockwise when viewed from above).
     * @param extrusionHeight The height to extrude the polygon upwards. Must be positive.
     * @return A list of faces, where each face is a list of LatLngAltitude vertices
     * defining that face. Returns an empty list if input is invalid.
     */
    public static List<List<LatLngAltitude>> extrudePolygon(
            List<LatLngAltitude> basePoints,
            double extrusionHeight
    ) {
        if (basePoints.size() < 3) {
            System.out.println("Error: Base polygon must have at least 3 points.");
            return Collections.emptyList();
        }
        if (extrusionHeight <= 0) {
            System.out.println("Error: Extrusion height must be positive.");
            return Collections.emptyList();
        }

        double baseAltitude = basePoints.get(0).getAltitude();

        // 1. Create points for the top face
        List<LatLngAltitude> topPoints = basePoints.stream()
                .map(basePoint -> new LatLngAltitude(
                        basePoint.getLatitude(),
                        basePoint.getLongitude(),
                        baseAltitude + extrusionHeight))
                .collect(Collectors.toList());

        List<List<LatLngAltitude>> faces = new ArrayList<>();

        // 2. Add bottom face
        // If basePoints are clockwise (viewed from top), this face is "looking down"
        faces.add(new ArrayList<>(basePoints));

        // 3. Add top face
        // To make it "look up" (assuming basePoints were clockwise), reverse the order of topPoints.
        List<LatLngAltitude> reversedTopPoints = new ArrayList<>(topPoints);
        Collections.reverse(reversedTopPoints);
        faces.add(reversedTopPoints); // Defensive copy and reversed

        // 4. Add side wall faces
        for (int i = 0; i < basePoints.size(); i++) {
            LatLngAltitude p1Base = basePoints.get(i);
            LatLngAltitude p2Base = basePoints.get((i + 1) % basePoints.size()); // Next point, wraps around

            LatLngAltitude p1Top = topPoints.get(i);
            LatLngAltitude p2Top = topPoints.get((i + 1) % basePoints.size());   // Corresponding top point

            // Define the side wall (quadrilateral)
            // Order: p1Base -> p2Base -> p2Top -> p1Top makes it outward-facing
            // if basePoints are clockwise.
            faces.add(Arrays.asList(p1Base, p2Base, p2Top, p1Top));
        }

        return Collections.unmodifiableList(faces); // Return an unmodifiable list of faces
    }

    private DataModel() {
        // This is a utility class, not meant for instantiation.
    }
}