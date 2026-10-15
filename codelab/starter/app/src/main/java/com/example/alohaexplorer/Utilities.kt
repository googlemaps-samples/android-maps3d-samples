package com.example.alohaexplorer

/* TODO: Prerequisites - Uncomment the rest of this file after synchronizing the Maps 3D SDK dependency!

import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.android.gms.maps3d.model.latLngAltitude
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.math.floor


/**
 * Extrudes a flat polygon (defined by basePoints, all at the same altitude)
 * upwards by a given extrusionHeight to form a 3D prism (like a building).
 *
 * **Algorithm Explanation**:
 * 1.  **Bottom Face**: The original list of points.
 * 2.  **Top Face**: Same lat/lng as bottom, but altitude += height.
 * 3.  **Side Faces**: Quads connecting each segment of the bottom to the corresponding segment of the top.
 *
 * @param basePoints List of vertices for the base.
 * @param extrusionHeight Height in meters to extrude upwards.
 */
fun extrudePolygon(
    basePoints: List<LatLngAltitude>,
    extrusionHeight: Double
): List<List<LatLngAltitude>> {
    if (basePoints.size < 3) return emptyList()
    if (extrusionHeight <= 0) return emptyList()

    val baseAltitude = basePoints.first().altitude

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
    faces.add(basePoints.toList())

    // 3. Add top face (must be reversed for correct "winding order" so it faces up)
    faces.add(topPoints.toList().reversed())

    // 4. Add side wall faces
    for (i in basePoints.indices) {
        val p1Base = basePoints[i]
        val p2Base = basePoints[(i + 1) % basePoints.size] // Wrap around to start

        val p1Top = topPoints[i]
        val p2Top = topPoints[(i + 1) % basePoints.size]

        // Define the quad for this side
        val sideFace = listOf(p1Base, p2Base, p2Top, p1Top)
        faces.add(sideFace)
    }

    return faces
}

/**
 * **Coroutine Helper**: Awaiting Map Stability.
 * Often you want to wait until the map has fully loaded (is "steady") before starting
 * the next animation or interaction. This implementation wraps the callback-based
 * `setOnMapSteadyListener` into a standard Kotlin `suspend` function.
 */
suspend fun awaitMapSteady(map: GoogleMap3D) = suspendCancellableCoroutine { continuation ->
    map.setOnMapSteadyListener { isSteady ->
        if (isSteady) {
            map.setOnMapSteadyListener(null) // Cleanup the listener
            if (continuation.isActive) {
                continuation.resume(Unit) // Resume the suspended coroutine
            }
        }
    }

    // Safety: If the coroutine is cancelled (e.g., user exits app), remove the listener.
    continuation.invokeOnCancellation {
        map.setOnMapSteadyListener(null)
    }
}


/**
 * **Coroutine Helper**: Awaiting Camera Animation.
 * Similar to `awaitMapSteady`, this pauses our code execution until the camera finishes its flight.
 * This allows us to write strict sequences like: "Fly to A, THEN wait, THEN fly to B".
 */
suspend fun awaitCameraAnimation(map: GoogleMap3D) = suspendCancellableCoroutine { continuation ->
    map.setCameraAnimationEndListener {
        map.setCameraAnimationEndListener(null) // Cleanup
        if (continuation.isActive) {
            continuation.resume(Unit)
        }
    }

    continuation.invokeOnCancellation {
        map.setCameraAnimationEndListener(null)
    }
}

*/
