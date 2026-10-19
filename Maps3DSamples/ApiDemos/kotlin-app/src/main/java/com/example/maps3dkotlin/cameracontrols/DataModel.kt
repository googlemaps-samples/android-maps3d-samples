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

package com.example.maps3dkotlin.cameracontrols

import android.graphics.Color
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.Glyph
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.android.gms.maps3d.model.MarkerOptions
import com.google.android.gms.maps3d.model.PinConfiguration
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.cameraRestriction
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.gms.maps3d.model.latLngBounds
import com.google.android.gms.maps3d.model.markerOptions
import com.google.android.gms.maps3d.model.polygonOptions

/**
 * DataModel is an object that holds constants and calculated values related to the New York City
 * area and Mount Rainier summit for camera controls, relative altitude animations, and restrictions.
 */
object DataModel {
    // Target 1: Empire State Building (443m 3D skyscraper mesh in Midtown Manhattan)
    const val EMPIRE_STATE_BUILDING_LATITUDE = 40.748433
    const val EMPIRE_STATE_BUILDING_LONGITUDE = -73.985656
    const val ESB_ABSOLUTE_ALTITUDE_METERS = 212.0
    const val ESB_BASE_RANGE_METERS = 950.0
    const val ESB_FLY_TO_DURATION_MS = 2_000L
    const val ESB_MARKER_ID = "esb_altitude_beacon"

    // Target 2: Mount Rainier Summit (4,392m high-altitude mountain peak in Washington State)
    const val MT_RAINIER_LATITUDE = 46.8523
    const val MT_RAINIER_LONGITUDE = -121.7603
    const val MT_RAINIER_GROUND_ELEVATION_METERS = 4392.0
    const val MT_RAINIER_BASE_RANGE_METERS = 1450.0
    const val MT_RAINIER_FLY_TO_DURATION_MS = 3_000L
    const val MT_RAINIER_ORBIT_DURATION_MS = 6_000L
    const val MT_RAINIER_ORBIT_ROUNDS = 1.0
    const val MT_RAINIER_MARKER_ID = "mt_rainier_summit_beacon"

    // Unified AltitudeMode clearances & camera framing shared across all targets
    const val RELATIVE_CLEARANCE_METERS = 100.0
    const val RELATIVE_MESH_CLEARANCE_METERS = 360.0
    const val CLAMP_ALTITUDE_METERS = 0.0

    const val RELATIVE_GROUND_HEADING = 34.0
    const val RELATIVE_GROUND_TILT = 65.0

    const val RELATIVE_MESH_HEADING = 125.0
    const val RELATIVE_MESH_TILT = 65.0
    const val RELATIVE_MESH_RANGE_FACTOR = 1.0

    const val CLAMP_HEADING = 205.0
    const val CLAMP_TILT = 62.0

    const val ABSOLUTE_HEADING = 310.0
    const val ABSOLUTE_TILT = 52.0
    const val ABSOLUTE_RANGE_FACTOR = 1.3

    // High-visibility Google yellow (#F4B400) for the altitude reference beacon
    private val BEACON_BACKGROUND_COLOR = Color.rgb(244, 180, 0)

    private const val NYC_SOUTH_WEST_LAT = 40.68563088976172
    private const val NYC_SOUTH_WEST_LNG = -74.05030430240065
    private const val NYC_NORTH_EAST_LAT = 40.85649214337128
    private const val NYC_NORTH_EAST_LNG = -73.80240973771173
    private const val MAX_ALTITUDE_NYC_METERS = 10000.0
    private const val MIN_ALTITUDE_NYC_METERS = 500.0

    private val nycBounds = latLngBounds {
        northEastLat = NYC_NORTH_EAST_LAT
        northEastLng = NYC_NORTH_EAST_LNG
        southWestLat = NYC_SOUTH_WEST_LAT
        southWestLng = NYC_SOUTH_WEST_LNG
    }

    /**
     * Defines the camera restrictions for the NYC area.
     *
     * This restriction enforces that the camera must:
     * - Maintain an altitude between [MIN_ALTITUDE_NYC_METERS] and [MAX_ALTITUDE_NYC_METERS].
     * - Stay within the geographic bounds defined by [nycBounds].
     */
    val nycCameraRestriction = cameraRestriction {
        minAltitude = MIN_ALTITUDE_NYC_METERS
        maxAltitude = MAX_ALTITUDE_NYC_METERS
        bounds = nycBounds
        minHeading = 0.0
        maxHeading = 360.0
        minTilt = 0.0
        maxTilt = 90.0
    }

    /**
     * Unified [Camera] builder for any landmark target across all four [AltitudeMode] options:
     * - [AltitudeMode.RELATIVE_TO_GROUND]: [RELATIVE_CLEARANCE_METERS] (+100m) above bare terrain (DTM).
     * - [AltitudeMode.RELATIVE_TO_MESH]: [RELATIVE_MESH_CLEARANCE_METERS] (+360m) above the 3D surface/building mesh (DSM).
     * - [AltitudeMode.CLAMP_TO_GROUND]: [CLAMP_ALTITUDE_METERS] (0m) anchored directly on the ground surface.
     *   (Note: `Camera` validates `altitudeMode` and does not accept `CLAMP_TO_GROUND` directly, so a `0.0m`
     *   `RELATIVE_TO_GROUND` center is used to anchor the camera target on the DTM surface while keeping
     *   [baseRangeMeters] wide enough for the full 3D structure mesh to remain loaded in the frustum).
     * - [AltitudeMode.ABSOLUTE]: [absoluteAltitudeMeters] above EGM96 mean sea level (212m mid-tower at ESB,
     *   4,392m summit at Mount Rainier).
     */
    fun buildTargetCamera(
        latitude: Double,
        longitude: Double,
        absoluteAltitudeMeters: Double,
        baseRangeMeters: Double,
        @AltitudeMode mode: Int = AltitudeMode.RELATIVE_TO_GROUND,
    ): Camera =
        when (mode) {
            AltitudeMode.RELATIVE_TO_MESH -> camera {
                center = latLngAltitude {
                    this.latitude = latitude
                    this.longitude = longitude
                    altitude = RELATIVE_MESH_CLEARANCE_METERS
                }
                heading = RELATIVE_MESH_HEADING
                tilt = RELATIVE_MESH_TILT
                roll = 0.0
                range = baseRangeMeters * RELATIVE_MESH_RANGE_FACTOR
                altitudeMode = AltitudeMode.RELATIVE_TO_MESH
            }

            AltitudeMode.CLAMP_TO_GROUND -> camera {
                center = latLngAltitude {
                    this.latitude = latitude
                    this.longitude = longitude
                    altitude = CLAMP_ALTITUDE_METERS
                }
                heading = CLAMP_HEADING
                tilt = CLAMP_TILT
                roll = 0.0
                range = baseRangeMeters
                altitudeMode = AltitudeMode.RELATIVE_TO_GROUND
            }

            AltitudeMode.ABSOLUTE -> camera {
                center = latLngAltitude {
                    this.latitude = latitude
                    this.longitude = longitude
                    altitude = absoluteAltitudeMeters
                }
                heading = ABSOLUTE_HEADING
                tilt = ABSOLUTE_TILT
                roll = 0.0
                range = baseRangeMeters * ABSOLUTE_RANGE_FACTOR
                altitudeMode = AltitudeMode.ABSOLUTE
            }

            else -> camera {
                center = latLngAltitude {
                    this.latitude = latitude
                    this.longitude = longitude
                    altitude = RELATIVE_CLEARANCE_METERS
                }
                heading = RELATIVE_GROUND_HEADING
                tilt = RELATIVE_GROUND_TILT
                roll = 0.0
                range = baseRangeMeters
                altitudeMode = AltitudeMode.RELATIVE_TO_GROUND
            }
        }

    /**
     * Unified [MarkerOptions] builder for any landmark target across all four [AltitudeMode] options:
     * - [AltitudeMode.RELATIVE_TO_GROUND]: [RELATIVE_CLEARANCE_METERS] (+100m) extruded above bare ground (DTM).
     * - [AltitudeMode.RELATIVE_TO_MESH]: [RELATIVE_MESH_CLEARANCE_METERS] (+360m) extruded above the 3D mesh (DSM).
     * - [AltitudeMode.CLAMP_TO_GROUND]: [CLAMP_ALTITUDE_METERS] (0m) clamped flat onto the ground surface.
     * - [AltitudeMode.ABSOLUTE]: [absoluteAltitudeMeters] anchored at fixed EGM96 sea-level elevation.
     *
     * @param refreshToken Incremented only when [AltitudeMode.RELATIVE_TO_MESH] is active and
     *   `onMapSteady` fires after a flight. The Maps 3D SDK evaluates `RELATIVE_TO_MESH` height
     *   against loaded 3D tiles at marker creation time and does not re-sample once high-LOD
     *   destination tiles stream in, so a new ID forces re-creation against the loaded mesh.
     */
    fun buildTargetBeaconOptions(
        markerIdPrefix: String,
        latitude: Double,
        longitude: Double,
        absoluteAltitudeMeters: Double,
        glyphText: String,
        label: String,
        @AltitudeMode mode: Int = AltitudeMode.RELATIVE_TO_GROUND,
        refreshToken: Int = 0,
    ): MarkerOptions {
        val (beaconAltitude, beaconAltitudeMode) = when (mode) {
            AltitudeMode.RELATIVE_TO_MESH ->
                RELATIVE_MESH_CLEARANCE_METERS to AltitudeMode.RELATIVE_TO_MESH
            AltitudeMode.CLAMP_TO_GROUND ->
                CLAMP_ALTITUDE_METERS to AltitudeMode.CLAMP_TO_GROUND
            AltitudeMode.ABSOLUTE ->
                absoluteAltitudeMeters to AltitudeMode.ABSOLUTE
            else ->
                RELATIVE_CLEARANCE_METERS to AltitudeMode.RELATIVE_TO_GROUND
        }
        return markerOptions {
            // Include refreshToken so RELATIVE_TO_MESH markers re-sample after 3D tiles load.
            id = "${markerIdPrefix}_${mode}_$refreshToken"
            position = latLngAltitude {
                this.latitude = latitude
                this.longitude = longitude
                altitude = beaconAltitude
            }
            this.label = label
            altitudeMode = beaconAltitudeMode
            isExtruded = (mode != AltitudeMode.CLAMP_TO_GROUND)
            isDrawnWhenOccluded = true
            setStyle(
                PinConfiguration.builder()
                    .setScale(1.4f)
                    .setBackgroundColor(BEACON_BACKGROUND_COLOR)
                    .setBorderColor(Color.WHITE)
                    .setGlyph(Glyph.fromText(glyphText))
                    .build(),
            )
        }
    }

    /**
     * Selects the unified [Camera] for either Mount Rainier (`isMountain = true`) or the
     * Empire State Building (`isMountain = false`) using [mode].
     */
    fun targetCamera(
        isMountain: Boolean,
        @AltitudeMode mode: Int = AltitudeMode.RELATIVE_TO_GROUND,
    ): Camera =
        if (isMountain) {
            mtRainierCamera(mode)
        } else {
            esbCamera(mode)
        }

    /**
     * Selects the unified reference beacon [MarkerOptions] for either Mount Rainier
     * (`isMountain = true`) or the Empire State Building (`isMountain = false`) using [mode].
     */
    fun targetBeaconOptions(
        isMountain: Boolean,
        label: String,
        @AltitudeMode mode: Int = AltitudeMode.RELATIVE_TO_GROUND,
        refreshToken: Int = 0,
    ): MarkerOptions =
        if (isMountain) {
            mtRainierBeaconOptions(label = label, mode = mode, refreshToken = refreshToken)
        } else {
            esbBeaconOptions(label = label, mode = mode, refreshToken = refreshToken)
        }

    fun esbCamera(@AltitudeMode mode: Int = AltitudeMode.RELATIVE_TO_GROUND): Camera =
        buildTargetCamera(
            latitude = EMPIRE_STATE_BUILDING_LATITUDE,
            longitude = EMPIRE_STATE_BUILDING_LONGITUDE,
            absoluteAltitudeMeters = ESB_ABSOLUTE_ALTITUDE_METERS,
            baseRangeMeters = ESB_BASE_RANGE_METERS,
            mode = mode,
        )

    fun esbBeaconOptions(
        label: String,
        @AltitudeMode mode: Int = AltitudeMode.RELATIVE_TO_GROUND,
        refreshToken: Int = 0,
    ): MarkerOptions =
        buildTargetBeaconOptions(
            markerIdPrefix = ESB_MARKER_ID,
            latitude = EMPIRE_STATE_BUILDING_LATITUDE,
            longitude = EMPIRE_STATE_BUILDING_LONGITUDE,
            absoluteAltitudeMeters = ESB_ABSOLUTE_ALTITUDE_METERS,
            glyphText = "🏢",
            label = label,
            mode = mode,
            refreshToken = refreshToken,
        )

    fun mtRainierCamera(@AltitudeMode mode: Int = AltitudeMode.RELATIVE_TO_GROUND): Camera =
        buildTargetCamera(
            latitude = MT_RAINIER_LATITUDE,
            longitude = MT_RAINIER_LONGITUDE,
            absoluteAltitudeMeters = MT_RAINIER_GROUND_ELEVATION_METERS,
            baseRangeMeters = MT_RAINIER_BASE_RANGE_METERS,
            mode = mode,
        )

    fun mtRainierBeaconOptions(
        label: String,
        @AltitudeMode mode: Int = AltitudeMode.RELATIVE_TO_GROUND,
        refreshToken: Int = 0,
    ): MarkerOptions =
        buildTargetBeaconOptions(
            markerIdPrefix = MT_RAINIER_MARKER_ID,
            latitude = MT_RAINIER_LATITUDE,
            longitude = MT_RAINIER_LONGITUDE,
            absoluteAltitudeMeters = MT_RAINIER_GROUND_ELEVATION_METERS,
            glyphText = "⛰️",
            label = label,
            mode = mode,
            refreshToken = refreshToken,
        )

    /**
     * Defines the base face of a polygon representing the ground area of New York City.
     * This face is defined by four LatLngAltitude points that form a rectangle.
     * All points share the same minimum altitude.
     */
    private val baseFace = listOf(
        latLngAltitude {
            latitude = NYC_SOUTH_WEST_LAT
            longitude = NYC_SOUTH_WEST_LNG
            altitude = MIN_ALTITUDE_NYC_METERS
        },
        latLngAltitude {
            latitude = NYC_SOUTH_WEST_LAT
            longitude = NYC_NORTH_EAST_LNG
            altitude = MIN_ALTITUDE_NYC_METERS
        },
        latLngAltitude {
            latitude = NYC_NORTH_EAST_LAT
            longitude = NYC_NORTH_EAST_LNG
            altitude = MIN_ALTITUDE_NYC_METERS
        },
        latLngAltitude {
            latitude = NYC_NORTH_EAST_LAT
            longitude = NYC_SOUTH_WEST_LNG
            altitude = MIN_ALTITUDE_NYC_METERS
        }
    )

    private val extrudedNyc = extrudePolygon(baseFace, MAX_ALTITUDE_NYC_METERS)

    // Define style for the cube faces
    private val faceFillColor = Color.argb(70, 0, 120, 255) // Semi-transparent blue
    private val faceStrokeColor = Color.rgb(0, 80, 200)   // Solid darker blue
    private val faceStrokeWidth = 3.0

    val nycPolygonOptions = extrudedNyc.map { facePoints ->
        polygonOptions {
            path = facePoints
            fillColor = faceFillColor
            strokeColor = faceStrokeColor
            strokeWidth = faceStrokeWidth
            altitudeMode = AltitudeMode.ABSOLUTE
            geodesic = false
            drawsOccludedSegments = true
        }
    }
}

/**
 * Extrudes a flat polygon (defined by basePoints, all at the same altitude)
 * upwards by a given extrusionHeight to form a 3D prism.
 *
 * @param basePoints A list of LatLngAltitude points defining the base polygon.
 * All points must have the same altitude.
 * The order of points defines the winding (e.g., clockwise when viewed from above).
 * @param extrusionHeight The height to extrude the polygon upwards. Must be positive.
 * @return A list of faces, where each face is a list of LatLngAltitude vertices
 * defining that face. Returns an empty list if input is invalid.
 */
fun extrudePolygon(
    basePoints: List<LatLngAltitude>,
    extrusionHeight: Double
): List<List<LatLngAltitude>> {
    // Validate input
    if (basePoints.size < 3) {
        println("Error: Base polygon must have at least 3 points.")
        return emptyList()
    }
    if (extrusionHeight <= 0) {
        println("Error: Extrusion height must be positive.")
        return emptyList()
    }

    val baseAltitude = basePoints.first().altitude // Assuming all base points share this altitude

    // 1. Create points for the top face
    val topPoints = basePoints.map { basePoint ->
        latLngAltitude {
            latitude = basePoint.latitude
            longitude = basePoint.longitude
            altitude = baseAltitude + extrusionHeight
        }
    }

    val faces = mutableListOf<List<LatLngAltitude>>()

    // 2. Add bottom face
    // If basePoints are clockwise (viewed from top), this face is "looking down"
    faces.add(basePoints.toList()) // Defensive copy

    // 3. Add top face
    // To make it "look up" (assuming basePoints were clockwise), reverse the order of topPoints.
    faces.add(topPoints.toList().reversed()) // Defensive copy and reversed

    // 4. Add side wall faces
    for (i in basePoints.indices) {
        val p1Base = basePoints[i]
        val p2Base = basePoints[(i + 1) % basePoints.size] // Next point, wraps around

        val p1Top = topPoints[i]
        val p2Top = topPoints[(i + 1) % basePoints.size]   // Corresponding top point

        // Define the side wall (quadrilateral)
        // Order: p1Base -> p2Base -> p2Top -> p1Top makes it outward-facing
        // if basePoints are clockwise.
        val sideFace = listOf(p1Base, p2Base, p2Top, p1Top)
        faces.add(sideFace)
    }

    return faces
}
