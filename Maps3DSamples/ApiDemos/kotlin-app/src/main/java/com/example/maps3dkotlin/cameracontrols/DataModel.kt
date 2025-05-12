package com.example.maps3dkotlin.cameracontrols

import android.graphics.Color
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.android.gms.maps3d.model.cameraRestriction
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.gms.maps3d.model.latLngBounds
import com.google.android.gms.maps3d.model.polygonOptions

/**
 * DataModel is an object that holds constants and calculated values related to the New York City
 * area, specifically for camera restrictions and polygon definitions.
 */
object DataModel {
    const val EMPIRE_STATE_BUILDING_LATITUDE = 40.748233
    const val EMPIRE_STATE_BUILDING_LONGITUDE = -73.985663

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
            outerCoordinates = facePoints
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
