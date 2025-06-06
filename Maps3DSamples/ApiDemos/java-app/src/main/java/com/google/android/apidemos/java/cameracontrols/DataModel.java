/*
 * Copyright 2023 Google LLC
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

package com.google.android.apidemos.java.cameracontrols;

import android.graphics.Color;

// Placeholder imports for Maps 3D SDK components
import com.google.android.gms.maps3d.model.AltitudeMode;
import com.google.android.gms.maps3d.model.LatLngAltitude;
import com.google.android.gms.maps3d.model.PolygonOptions;
import com.google.android.gms.maps3d.model.CameraRestriction;
import com.google.android.gms.maps3d.model.LatLngBounds;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public final class DataModel {

    private DataModel() {
        // Prevent instantiation
    }

    public static final double EMPIRE_STATE_BUILDING_LATITUDE = 40.748817;
    public static final double EMPIRE_STATE_BUILDING_LONGITUDE = -73.985428;
    public static final double EMPIRE_STATE_BUILDING_ALTITUDE_METERS = 381.0;

    public static final double NYC_SOUTH_WEST_LAT = 40.70;
    public static final double NYC_SOUTH_WEST_LNG = -74.02;
    public static final double NYC_NORTH_EAST_LAT = 40.88;
    public static final double NYC_NORTH_EAST_LNG = -73.90;

    public static final double MAX_ALTITUDE_NYC_METERS = 1000.0;
    public static final double MIN_ALTITUDE_NYC_METERS = 0.0;

    public static final LatLngBounds nycBounds;
    public static final CameraRestriction nycCameraRestriction;
    public static final List<LatLngAltitude> baseFace;
    public static final List<PolygonOptions> nycPolygonOptions;

    private static final int faceFillColor = Color.argb(128, 0, 255, 0); // Semi-transparent green
    private static final int faceStrokeColor = Color.rgb(0, 0, 255); // Solid blue
    private static final double faceStrokeWidth = 2.0;

    static {
        // Initialize nycBounds
        // Assuming LatLngBounds.Builder takes LatLngAltitude or similar for southWest and northEast
        // If it takes LatLng, new LatLng(NYC_SOUTH_WEST_LAT, NYC_SOUTH_WEST_LNG) would be used.
        nycBounds = new LatLngBounds.Builder()
            .southWest(new LatLngAltitude(NYC_SOUTH_WEST_LAT, NYC_SOUTH_WEST_LNG, 0))
            .northEast(new LatLngAltitude(NYC_NORTH_EAST_LAT, NYC_NORTH_EAST_LNG, 0))
            .build();

        // Initialize nycCameraRestriction
        nycCameraRestriction = new CameraRestriction.Builder()
            .minAltitude(MIN_ALTITUDE_NYC_METERS)
            .maxAltitude(MAX_ALTITUDE_NYC_METERS)
            .bounds(nycBounds)
            .minHeading(0.0)
            .maxHeading(360.0)
            .minTilt(0.0)
            .maxTilt(90.0)
            .build();

        // Initialize baseFace
        baseFace = Arrays.asList(
            new LatLngAltitude(NYC_SOUTH_WEST_LAT, NYC_SOUTH_WEST_LNG, 0.0),
            new LatLngAltitude(NYC_SOUTH_WEST_LAT, NYC_NORTH_EAST_LNG, 0.0),
            new LatLngAltitude(NYC_NORTH_EAST_LAT, NYC_NORTH_EAST_LNG, 0.0),
            new LatLngAltitude(NYC_NORTH_EAST_LAT, NYC_SOUTH_WEST_LNG, 0.0)
        );

        // Initialize nycPolygonOptions
        List<List<LatLngAltitude>> extrudedNyc = extrudePolygon(baseFace, MAX_ALTITUDE_NYC_METERS);
        List<PolygonOptions> optionsList = new ArrayList<>();
        for (List<LatLngAltitude> facePoints : extrudedNyc) {
            PolygonOptions options = new PolygonOptions.Builder()
                .outerCoordinates(facePoints)
                .fillColor(faceFillColor)
                .strokeColor(faceStrokeColor)
                .strokeWidth(faceStrokeWidth)
                .altitudeMode(AltitudeMode.ABSOLUTE)
                .geodesic(false)
                .drawsOccludedSegments(true)
                .build();
            optionsList.add(options);
        }
        nycPolygonOptions = Collections.unmodifiableList(optionsList);
    }

    public static List<List<LatLngAltitude>> extrudePolygon(List<LatLngAltitude> basePoints, double extrusionHeight) {
        if (basePoints == null || basePoints.size() < 3) {
            throw new IllegalArgumentException("Base polygon must have at least 3 points.");
        }
        if (extrusionHeight <= 0) {
            throw new IllegalArgumentException("Extrusion height must be positive.");
        }

        List<List<LatLngAltitude>> faces = new ArrayList<>();

        // Bottom face (original base points)
        faces.add(new ArrayList<>(basePoints));

        // Top face (base points extruded by height)
        List<LatLngAltitude> topPoints = new ArrayList<>();
        for (LatLngAltitude point : basePoints) {
            topPoints.add(new LatLngAltitude(point.latitude, point.longitude, point.altitude + extrusionHeight));
        }
        List<LatLngAltitude> reversedTopPoints = new ArrayList<>(topPoints);
        Collections.reverse(reversedTopPoints); // Ensure correct winding order for the top face
        faces.add(reversedTopPoints);

        // Side walls
        for (int i = 0; i < basePoints.size(); i++) {
            LatLngAltitude p1Bottom = basePoints.get(i);
            LatLngAltitude p2Bottom = basePoints.get((i + 1) % basePoints.size()); // Next point, wraps around
            LatLngAltitude p1Top = topPoints.get(i);
            LatLngAltitude p2Top = topPoints.get((i + 1) % topPoints.size());

            List<LatLngAltitude> sideWall = Arrays.asList(
                p1Bottom,
                p2Bottom,
                p2Top,
                p1Top
            );
            faces.add(sideWall);
        }
        return faces;
    }
}
