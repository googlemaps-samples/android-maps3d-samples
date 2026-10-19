package com.example.alohaexplorer

import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.FlyAroundOptions
import com.google.android.gms.maps3d.model.FlyToOptions
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.android.gms.maps3d.model.latLngAltitude
import kotlin.coroutines.resume
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull


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
 * `setOnMapSteadyListener` into a standard Kotlin `suspend` function with a timeout so
 * execution never hangs indefinitely if 3D tiles are slow to load.
 */
suspend fun GoogleMap3D.awaitMapSteady(timeout: Duration = 5.seconds): Boolean {
    return withTimeoutOrNull(timeout) {
        suspendCancellableCoroutine { continuation ->
            setOnMapSteadyListener { isSteady ->
                if (isSteady) {
                    setOnMapSteadyListener(null) // Cleanup the listener
                    if (continuation.isActive) {
                        continuation.resume(true) // Resume the suspended coroutine
                    }
                }
            }

            // Safety: If the coroutine is cancelled (e.g., user exits app or timeout expires), remove the listener.
            continuation.invokeOnCancellation {
                setOnMapSteadyListener(null)
            }
        }
    } ?: false
}

/**
 * **Coroutine Helper**: Awaiting a Fly-To Camera Animation.
 * Registers the animation-end listener before triggering [GoogleMap3D.flyCameraTo] to prevent
 * race conditions, and stops the camera animation if the coroutine is cancelled.
 */
suspend fun GoogleMap3D.awaitCameraAnimation(options: FlyToOptions) = suspendCancellableCoroutine { continuation ->
    setCameraAnimationEndListener {
        setCameraAnimationEndListener(null) // Cleanup
        if (continuation.isActive) {
            continuation.resume(Unit)
        }
    }
    flyCameraTo(options)

    continuation.invokeOnCancellation {
        stopCameraAnimation()
        setCameraAnimationEndListener(null)
    }
}

/**
 * **Coroutine Helper**: Awaiting a Fly-Around Camera Animation.
 * Registers the animation-end listener before triggering [GoogleMap3D.flyCameraAround] to prevent
 * race conditions, and stops the camera animation if the coroutine is cancelled.
 */
suspend fun GoogleMap3D.awaitCameraAnimation(options: FlyAroundOptions) = suspendCancellableCoroutine { continuation ->
    setCameraAnimationEndListener {
        setCameraAnimationEndListener(null) // Cleanup
        if (continuation.isActive) {
            continuation.resume(Unit)
        }
    }
    flyCameraAround(options)

    continuation.invokeOnCancellation {
        stopCameraAnimation()
        setCameraAnimationEndListener(null)
    }
}
