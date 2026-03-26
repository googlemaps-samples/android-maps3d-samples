package com.example.advancedmaps3dsamples.utils

import com.google.android.gms.maps.model.LatLng

object GeoMathUtils {

    /**
     * Instantly finds the mathematical coordinate along the path given an absolute distance in meters.
     * Replaces expensive haversine math inside the render loop with a precomputed distance array lookup.
     */
    fun getInterpolatedPoint(
        distance: Double, path: List<LatLng>, cumulativeDistances: DoubleArray
    ): LatLng {
        if (distance <= 0.0) return path.first()
        if (distance >= cumulativeDistances.last()) return path.last()

        var idx = cumulativeDistances.binarySearch(distance)
        if (idx < 0) {
            idx = -(idx + 1) - 1 // insertion point - 1
        }
        idx = idx.coerceIn(0, cumulativeDistances.size - 2)

        val p1 = path[idx]
        val p2 = path[idx + 1]
        val d1 = cumulativeDistances[idx]
        val d2 = cumulativeDistances[idx + 1]

        val fraction = (distance - d1) / (d2 - d1)
        if (fraction <= 0.0) return p1
        if (fraction >= 1.0) return p2

        val lat = p1.latitude + (p2.latitude - p1.latitude) * fraction
        val lng = p1.longitude + (p2.longitude - p1.longitude) * fraction
        return LatLng(lat, lng)
    }

    /**
     * Ensures the camera takes the shortest rotational path (e.g., crossing 359 to 0 smoothly)
     * instead of violently spinning backward.
     */
    fun slerpHeading(current: Float, target: Float, factor: Float): Float {
        var dh = target - current
        while (dh > 180f) dh -= 360f
        while (dh <= -180f) dh += 360f
        return current + dh * factor
    }
}
