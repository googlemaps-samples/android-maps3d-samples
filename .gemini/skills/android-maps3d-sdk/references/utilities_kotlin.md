# Camera Utilities (Kotlin)

These utility functions help ensure that camera parameters are within acceptable ranges for the Maps 3D SDK, preventing crashes due to invalid values.

## Camera Validation

Use `toValidCamera()` to sanitize a `Camera` object before applying it to the map.

```kotlin
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import kotlin.math.floor

val headingRange = 0.0..360.0
val tiltRange = 0.0..90.0
val rangeRange = 0.0..63170000.0
val rollRange = -360.0..360.0

val latitudeRange = -90.0..90.0
val longitudeRange = -180.0..180.0
val altitudeRange = 0.0..LatLngAltitude.MAX_ALTITUDE_METERS

val ORIGIN = latLngAltitude {
    latitude = 0.0
    longitude = 0.0
    altitude = 0.0
}

val DEFAULT_CAMERA: Camera = camera {
    center = ORIGIN
    heading = 0.0
    tilt = 0.0
    roll = 0.0
    range = 1000.0
}

/**
 * Converts a nullable Camera object into a valid, non-null Camera object.
 */
fun Camera?.toValidCamera(): Camera {
    val source = this ?: return DEFAULT_CAMERA

    return camera {
        center = source.center.toValidLocation()
        heading = source.heading.toHeading()
        tilt = source.tilt.toTilt()
        roll = source.roll.toRoll()
        range = source.range.toRange()
    }
}

fun LatLngAltitude.toValidLocation(): LatLngAltitude {
    val objectToCopy = this
    return latLngAltitude {
        latitude = objectToCopy.latitude.coerceIn(latitudeRange)
        longitude = objectToCopy.longitude.coerceIn(longitudeRange)
        altitude = objectToCopy.altitude.coerceIn(altitudeRange)
    }
}

fun Number?.toHeading(): Double =
    this?.toDouble()?.wrapIn(headingRange.start, headingRange.endInclusive) ?: 0.0

fun Number?.toTilt(): Double = this?.toDouble()?.coerceIn(tiltRange) ?: 0.0

fun Number?.toRoll(): Double = this?.toDouble()?.wrapIn(rollRange) ?: 0.0

fun Number?.toRange(): Double = this?.toDouble()?.coerceIn(rangeRange) ?: 0.0

fun Double.wrapIn(lower: Double, upper: Double): Double {
    val range = upper - lower
    if (range <= 0) {
        throw IllegalArgumentException("Upper bound must be greater than lower bound")
    }
    val offset = this - lower
    return lower + (offset - floor(offset / range) * range)
}
```

## Path and Animation Utilities

These utilities help with path smoothing, simplification, heading calculation, and distance calculations.

```kotlin
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps3d.model.FlyAroundOptions
import com.google.android.gms.maps3d.model.FlyToOptions
import com.google.android.gms.maps3d.model.flyAroundOptions
import com.google.android.gms.maps3d.model.flyToOptions
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

fun FlyAroundOptions.copy(
    center: Camera? = null,
    durationInMillis: Long? = null,
    rounds: Double? = null,
) : FlyAroundOptions {
    val objectToCopy = this
    return flyAroundOptions {
        this.center = (center ?: objectToCopy.center)
        this.durationInMillis = durationInMillis ?: objectToCopy.durationInMillis
        this.rounds = rounds ?: objectToCopy.rounds
    }
}

fun FlyToOptions.copy(
    endCamera: Camera? = null,
    durationInMillis: Long? = null,
) : FlyToOptions {
    val objectToCopy = this
    return flyToOptions {
        this.endCamera = (endCamera ?: objectToCopy.endCamera)
        this.durationInMillis = durationInMillis ?: objectToCopy.durationInMillis
    }
}

/**
 * Smooths a path of LatLng points using Chaikin's algorithm.
 */
fun List<LatLng>.smoothPath(iterations: Int = 1): List<LatLng> {
    if (size < 3 || iterations <= 0) return this

    var currentPath = this
    repeat(iterations) {
        val nextPath = mutableListOf<LatLng>()
        nextPath.add(currentPath.first())

        for (i in 0 until currentPath.size - 1) {
            val p0 = currentPath[i]
            val p1 = currentPath[i + 1]

            val q = LatLng(
                p0.latitude * 0.75 + p1.latitude * 0.25,
                p0.longitude * 0.75 + p1.longitude * 0.25
            )

            val r = LatLng(
                p0.latitude * 0.25 + p1.latitude * 0.75,
                p0.longitude * 0.25 + p1.longitude * 0.75
            )

            nextPath.add(q)
            nextPath.add(r)
        }

        nextPath.add(currentPath.last())
        currentPath = nextPath
    }

    return currentPath
}

/**
 * Calculates the heading (bearing) from one LatLng to another.
 */
fun calculateHeading(from: LatLng, to: LatLng): Double {
    val lat1 = Math.toRadians(from.latitude)
    val lon1 = Math.toRadians(from.longitude)
    val lat2 = Math.toRadians(to.latitude)
    val lon2 = Math.toRadians(to.longitude)

    val dLon = lon2 - lon1
    val y = sin(dLon) * cos(lat2)
    val x = cos(lat1) * sin(lat2) -
            sin(lat1) * cos(lat2) * cos(dLon)
    
    val bearing = Math.toDegrees(atan2(y, x))
    return (bearing + 360.0) % 360.0
}

/**
 * Simplifies a path of LatLng points using the Ramer-Douglas-Peucker algorithm.
 */
fun List<LatLng>.simplifyPath(epsilon: Double = 0.001): List<LatLng> {
    if (size < 3) return this

    var maxDistance = 0.0
    var index = 0
    val first = first()
    val last = last()

    for (i in 1 until size - 1) {
        val distance = perpendicularDistance(this[i], first, last)
        if (distance > maxDistance) {
            index = i
            maxDistance = distance
        }
    }

    return if (maxDistance > epsilon) {
        val left = subList(0, index + 1).simplifyPath(epsilon)
        val right = subList(index, size).simplifyPath(epsilon)
        left.dropLast(1) + right
    } else {
        listOf(first, last)
    }
}

private fun perpendicularDistance(point: LatLng, start: LatLng, end: LatLng): Double {
    val x = point.longitude
    val y = point.latitude
    val x1 = start.longitude
    val y1 = start.latitude
    val x2 = end.longitude
    val y2 = end.latitude

    val area = abs((y2 - y1) * x - (x2 - x1) * y + x2 * y1 - y2 * x1)
    val bottom = sqrt((y2 - y1).pow(2.0) + (x2 - x1).pow(2.0))
    return area / bottom
}

/**
 * Calculates the distance in meters between two [LatLng] points using the Haversine formula.
 */
fun haversineDistance(p1: LatLng, p2: LatLng): Double {
    val r = 6371000.0 // Earth radius in meters
    val lat1 = Math.toRadians(p1.latitude)
    val lon1 = Math.toRadians(p1.longitude)
    val lat2 = Math.toRadians(p2.latitude)
    val lon2 = Math.toRadians(p2.longitude)

    val dLat = lat2 - lat1
    val dLon = lon2 - lon1

    val a = sin(dLat / 2).pow(2.0) +
            cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2.0)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return r * c
}
```

