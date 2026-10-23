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

import androidx.annotation.NonNull;

import com.google.android.gms.maps3d.model.AltitudeMode;
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.CameraRestriction;
import com.google.android.gms.maps3d.model.Glyph;
import com.google.android.gms.maps3d.model.LatLngAltitude;
import com.google.android.gms.maps3d.model.LatLngBounds;
import com.google.android.gms.maps3d.model.MarkerOptions;
import com.google.android.gms.maps3d.model.PinConfiguration;
import com.google.android.gms.maps3d.model.PolygonOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DataModel is an object that holds constants and calculated values related to the New York City
 * area and Mount Rainier summit for camera controls, relative altitude animations, and restrictions.
 */
public final class DataModel {
    // Target 1: Empire State Building (443m 3D skyscraper mesh in Midtown Manhattan)
    public static final double EMPIRE_STATE_BUILDING_LATITUDE = 40.748433;
    public static final double EMPIRE_STATE_BUILDING_LONGITUDE = -73.985656;
    public static final double ESB_ABSOLUTE_ALTITUDE_METERS = 212.0;
    public static final double ESB_BASE_RANGE_METERS = 950.0;
    public static final long ESB_FLY_TO_DURATION_MS = 2_000L;
    public static final String ESB_MARKER_ID = "esb_altitude_beacon";

    // Target 2: Mount Rainier Summit (4,392m high-altitude mountain peak in Washington State)
    public static final double MT_RAINIER_LATITUDE = 46.8523;
    public static final double MT_RAINIER_LONGITUDE = -121.7603;
    public static final double MT_RAINIER_GROUND_ELEVATION_METERS = 4392.0;
    public static final double MT_RAINIER_BASE_RANGE_METERS = 1450.0;
    public static final long MT_RAINIER_FLY_TO_DURATION_MS = 3_000L;
    public static final long MT_RAINIER_ORBIT_DURATION_MS = 6_000L;
    public static final double MT_RAINIER_ORBIT_ROUNDS = 1.0;
    public static final String MT_RAINIER_MARKER_ID = "mt_rainier_summit_beacon";

    // Unified AltitudeMode clearances & camera framing shared across all targets
    public static final double RELATIVE_CLEARANCE_METERS = 100.0;
    public static final double RELATIVE_MESH_CLEARANCE_METERS = 360.0;
    public static final double CLAMP_ALTITUDE_METERS = 0.0;

    public static final double RELATIVE_GROUND_HEADING = 34.0;
    public static final double RELATIVE_GROUND_TILT = 65.0;

    public static final double RELATIVE_MESH_HEADING = 125.0;
    public static final double RELATIVE_MESH_TILT = 65.0;
    public static final double RELATIVE_MESH_RANGE_FACTOR = 1.0;

    public static final double CLAMP_HEADING = 205.0;
    public static final double CLAMP_TILT = 62.0;

    public static final double ABSOLUTE_HEADING = 310.0;
    public static final double ABSOLUTE_TILT = 52.0;
    public static final double ABSOLUTE_RANGE_FACTOR = 1.3;

    // High-visibility Google yellow (#F4B400) for the altitude reference beacon
    private static final int BEACON_BACKGROUND_COLOR = Color.rgb(244, 180, 0);

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
     * Unified {@link Camera} builder for any landmark target across all four {@link AltitudeMode} options:
     * <ul>
     *   <li>{@link AltitudeMode#RELATIVE_TO_GROUND}: {@link #RELATIVE_CLEARANCE_METERS} (+100m) above bare terrain (DTM).</li>
     *   <li>{@link AltitudeMode#RELATIVE_TO_MESH}: {@link #RELATIVE_MESH_CLEARANCE_METERS} (+360m) above the 3D surface/building mesh (DSM).</li>
     *   <li>{@link AltitudeMode#CLAMP_TO_GROUND}: {@link #CLAMP_ALTITUDE_METERS} (0m) anchored directly on the ground surface.</li>
     *   <li>{@link AltitudeMode#ABSOLUTE}: {@code absoluteAltitudeMeters} above EGM96 mean sea level (212m mid-tower at ESB,
     *   4,392m summit at Mount Rainier).</li>
     * </ul>
     */
    @NonNull
    public static Camera buildTargetCamera(
            double latitude,
            double longitude,
            double absoluteAltitudeMeters,
            double baseRangeMeters,
            @AltitudeMode int mode
    ) {
        if (mode == AltitudeMode.RELATIVE_TO_MESH) {
            return new Camera(
                    new LatLngAltitude(
                            latitude,
                            longitude,
                            RELATIVE_MESH_CLEARANCE_METERS
                    ),
                    RELATIVE_MESH_HEADING,
                    RELATIVE_MESH_TILT,
                    0.0,
                    baseRangeMeters * RELATIVE_MESH_RANGE_FACTOR,
                    AltitudeMode.RELATIVE_TO_MESH
            );
        } else if (mode == AltitudeMode.CLAMP_TO_GROUND) {
            return new Camera(
                    new LatLngAltitude(
                            latitude,
                            longitude,
                            CLAMP_ALTITUDE_METERS
                    ),
                    CLAMP_HEADING,
                    CLAMP_TILT,
                    0.0,
                    baseRangeMeters,
                    AltitudeMode.RELATIVE_TO_GROUND
            );
        } else if (mode == AltitudeMode.ABSOLUTE) {
            return new Camera(
                    new LatLngAltitude(
                            latitude,
                            longitude,
                            absoluteAltitudeMeters
                    ),
                    ABSOLUTE_HEADING,
                    ABSOLUTE_TILT,
                    0.0,
                    baseRangeMeters * ABSOLUTE_RANGE_FACTOR,
                    AltitudeMode.ABSOLUTE
            );
        } else {
            return new Camera(
                    new LatLngAltitude(
                            latitude,
                            longitude,
                            RELATIVE_CLEARANCE_METERS
                    ),
                    RELATIVE_GROUND_HEADING,
                    RELATIVE_GROUND_TILT,
                    0.0,
                    baseRangeMeters,
                    AltitudeMode.RELATIVE_TO_GROUND
            );
        }
    }

    /**
     * Unified {@link MarkerOptions} builder for any landmark target across all four {@link AltitudeMode} options:
     * <ul>
     *   <li>{@link AltitudeMode#RELATIVE_TO_GROUND}: {@link #RELATIVE_CLEARANCE_METERS} (+100m) extruded above bare ground (DTM).</li>
     *   <li>{@link AltitudeMode#RELATIVE_TO_MESH}: {@link #RELATIVE_MESH_CLEARANCE_METERS} (+360m) extruded above the 3D mesh (DSM).</li>
     *   <li>{@link AltitudeMode#CLAMP_TO_GROUND}: {@link #CLAMP_ALTITUDE_METERS} (0m) clamped flat onto the ground surface.</li>
     *   <li>{@link AltitudeMode#ABSOLUTE}: {@code absoluteAltitudeMeters} anchored at fixed EGM96 sea-level elevation.</li>
     * </ul>
     *
     * @param refreshToken Incremented only when {@link AltitudeMode#RELATIVE_TO_MESH} is active and
     *   {@code onMapSteady} fires after a flight. The Maps 3D SDK evaluates {@code RELATIVE_TO_MESH}
     *   height against loaded 3D tiles at marker creation time and does not re-sample once high-LOD
     *   destination tiles stream in, so a new ID forces re-creation against the loaded mesh.
     */
    @NonNull
    public static MarkerOptions buildTargetBeaconOptions(
            @NonNull String markerIdPrefix,
            double latitude,
            double longitude,
            double absoluteAltitudeMeters,
            @NonNull String glyphText,
            @NonNull String label,
            @AltitudeMode int mode,
            int refreshToken
    ) {
        double beaconAltitude;
        int beaconAltitudeMode;
        if (mode == AltitudeMode.RELATIVE_TO_MESH) {
            beaconAltitude = RELATIVE_MESH_CLEARANCE_METERS;
            beaconAltitudeMode = AltitudeMode.RELATIVE_TO_MESH;
        } else if (mode == AltitudeMode.CLAMP_TO_GROUND) {
            beaconAltitude = CLAMP_ALTITUDE_METERS;
            beaconAltitudeMode = AltitudeMode.CLAMP_TO_GROUND;
        } else if (mode == AltitudeMode.ABSOLUTE) {
            beaconAltitude = absoluteAltitudeMeters;
            beaconAltitudeMode = AltitudeMode.ABSOLUTE;
        } else {
            beaconAltitude = RELATIVE_CLEARANCE_METERS;
            beaconAltitudeMode = AltitudeMode.RELATIVE_TO_GROUND;
        }
        MarkerOptions options = new MarkerOptions();
        // Include refreshToken so RELATIVE_TO_MESH markers re-sample after 3D tiles load.
        options.setId(markerIdPrefix + "_" + mode + "_" + refreshToken);
        options.setPosition(new LatLngAltitude(
                latitude,
                longitude,
                beaconAltitude
        ));
        options.setLabel(label);
        options.setAltitudeMode(beaconAltitudeMode);
        options.setExtruded(mode != AltitudeMode.CLAMP_TO_GROUND);
        options.setDrawnWhenOccluded(true);
        options.setStyle(PinConfiguration.builder()
                .setScale(1.4f)
                .setBackgroundColor(BEACON_BACKGROUND_COLOR)
                .setBorderColor(Color.WHITE)
                .setGlyph(Glyph.fromText(glyphText))
                .build());
        return options;
    }

    @NonNull
    public static Camera getTargetCamera(boolean isMountain, @AltitudeMode int mode) {
        return isMountain ? getMtRainierCamera(mode) : getEsbCamera(mode);
    }

    @NonNull
    public static MarkerOptions getTargetBeaconOptions(
            boolean isMountain,
            @NonNull String label,
            @AltitudeMode int mode,
            int refreshToken
    ) {
        return isMountain
                ? getMtRainierBeaconOptions(label, mode, refreshToken)
                : getEsbBeaconOptions(label, mode, refreshToken);
    }

    @NonNull
    public static Camera getEsbCamera(@AltitudeMode int mode) {
        return buildTargetCamera(
                EMPIRE_STATE_BUILDING_LATITUDE,
                EMPIRE_STATE_BUILDING_LONGITUDE,
                ESB_ABSOLUTE_ALTITUDE_METERS,
                ESB_BASE_RANGE_METERS,
                mode
        );
    }

    @NonNull
    public static MarkerOptions getEsbBeaconOptions(
            @NonNull String label,
            @AltitudeMode int mode,
            int refreshToken
    ) {
        return buildTargetBeaconOptions(
                ESB_MARKER_ID,
                EMPIRE_STATE_BUILDING_LATITUDE,
                EMPIRE_STATE_BUILDING_LONGITUDE,
                ESB_ABSOLUTE_ALTITUDE_METERS,
                "🏢",
                label,
                mode,
                refreshToken
        );
    }

    @NonNull
    public static Camera getMtRainierCamera(@AltitudeMode int mode) {
        return buildTargetCamera(
                MT_RAINIER_LATITUDE,
                MT_RAINIER_LONGITUDE,
                MT_RAINIER_GROUND_ELEVATION_METERS,
                MT_RAINIER_BASE_RANGE_METERS,
                mode
        );
    }

    @NonNull
    public static MarkerOptions getMtRainierBeaconOptions(
            @NonNull String label,
            @AltitudeMode int mode,
            int refreshToken
    ) {
        return buildTargetBeaconOptions(
                MT_RAINIER_MARKER_ID,
                MT_RAINIER_LATITUDE,
                MT_RAINIER_LONGITUDE,
                MT_RAINIER_GROUND_ELEVATION_METERS,
                "⛰️",
                label,
                mode,
                refreshToken
        );
    }

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
                        options.setPath(facePoints);
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
