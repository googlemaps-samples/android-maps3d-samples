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

package com.example.maps3d.common

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.maps.android.SphericalUtil
import java.util.ArrayList

/**
 * Result of interpolating a position and orientation along a 3D path at a specific distance.
 *
 * @property latLng The interpolated 2D geographic coordinate.
 * @property waypointIndex The zero-based index of the segment start waypoint.
 * @property bearing The raw compass bearing (degrees) of the current route segment.
 * @property altitude The interpolated elevation in meters along the route segment.
 */
data class InterpolatedPathPoint(
    @JvmField val latLng: LatLng,
    @JvmField val waypointIndex: Int,
    @JvmField val bearing: Double,
    @JvmField val altitude: Double
)

/**
 * Math and geometry engine for ground-level 3D path following.
 *
 * Encapsulates segment search, distance accumulation, kinematic heading smoothing,
 * and elevation interpolation across Kotlin Views, Java Views, and Compose.
 */
object PathEngine {

    const val STATIC_POLYLINE_ID = "path_following_static_route"
    const val PROGRESS_POLYLINE_ID = "path_following_progress_route"

    /**
     * Precomputes cumulative distances along a 3D path in meters.
     */
    @JvmStatic
    fun calculateCumulativeDistances(path: List<LatLngAltitude>): DoubleArray {
        if (path.isEmpty()) return doubleArrayOf(0.0)
        val cumulativeDistances = DoubleArray(path.size)
        cumulativeDistances[0] = 0.0
        for (i in 1 until path.size) {
            val pPrev = LatLng(path[i - 1].latitude, path[i - 1].longitude)
            val pCurr = LatLng(path[i].latitude, path[i].longitude)
            cumulativeDistances[i] = cumulativeDistances[i - 1] + SphericalUtil.computeDistanceBetween(pPrev, pCurr)
        }
        return cumulativeDistances
    }

    /**
     * Finds the interpolated geographic position, segment bearing, and elevation at a target distance.
     */
    @JvmStatic
    fun interpolatePoint(
        path: List<LatLngAltitude>,
        cumulativeDistances: DoubleArray,
        distance: Double
    ): InterpolatedPathPoint {
        if (path.isEmpty()) {
            return InterpolatedPathPoint(LatLng(0.0, 0.0), 0, 0.0, 0.0)
        }

        val totalDistance = cumulativeDistances.lastOrNull() ?: 0.0
        var index = 0
        while (index < cumulativeDistances.size - 1 && cumulativeDistances[index + 1] < distance) {
            index++
        }

        val p1 = path[index]
        val p2 = if (index < path.size - 1) path[index + 1] else p1

        val segStartDist = cumulativeDistances.getOrElse(index) { 0.0 }
        val segEndDist = cumulativeDistances.getOrElse(index + 1) { totalDistance }
        val segLen = segEndDist - segStartDist

        val fraction = if (segLen > 0) ((distance - segStartDist) / segLen).coerceIn(0.0, 1.0) else 0.0
        val latLng1 = LatLng(p1.latitude, p1.longitude)
        val latLng2 = LatLng(p2.latitude, p2.longitude)
        val currentLatLng = SphericalUtil.interpolate(latLng1, latLng2, fraction)
        val bearing = SphericalUtil.computeHeading(latLng1, latLng2)
        val interpAlt = p1.altitude + fraction * (p2.altitude - p1.altitude)

        return InterpolatedPathPoint(currentLatLng, index, bearing, interpAlt)
    }

    /**
     * Applies an Exponential Moving Average (EMA) filter to camera heading to smooth
     * abrupt turns around corners during real-time playback.
     */
    @JvmStatic
    fun smoothHeading(
        targetHeading: Double,
        currentHeading: Double?,
        isUserScrubbing: Boolean,
        isPlaying: Boolean,
        smoothingFactor: Double = 0.12
    ): Double {
        val normalizedTarget = (targetHeading % 360.0 + 360.0) % 360.0
        if (currentHeading == null || isUserScrubbing || !isPlaying) {
            return normalizedTarget
        }

        var diff = (normalizedTarget - currentHeading) % 360.0
        if (diff > 180.0) diff -= 360.0
        if (diff < -180.0) diff += 360.0
        return (currentHeading + diff * smoothingFactor + 360.0) % 360.0
    }

    /**
     * Calculates camera target altitude based on the active altitude mode and route elevation.
     */
    @JvmStatic
    fun calculateCameraAltitude(
        altitudeMode: Int,
        baseAltitude: Double,
        interpolatedAltitude: Double,
        groundAltitude: Double
    ): Double {
        return if (altitudeMode == AltitudeMode.ABSOLUTE) {
            baseAltitude + interpolatedAltitude + groundAltitude
        } else {
            groundAltitude
        }
    }

    /**
     * Builds static route polyline vertices with altitude mode adjustments.
     */
    @JvmStatic
    fun buildStaticVertices(
        path: List<LatLngAltitude>,
        altitudeMode: Int,
        baseAltitude: Double,
        pathAltitudeOffset: Double
    ): List<LatLngAltitude> {
        return path.map { pt ->
            val vertexAltitude = when (altitudeMode) {
                AltitudeMode.CLAMP_TO_GROUND -> 0.0
                AltitudeMode.ABSOLUTE -> pt.altitude + baseAltitude + pathAltitudeOffset
                else -> pt.altitude + pathAltitudeOffset
            }
            LatLngAltitude(pt.latitude, pt.longitude, vertexAltitude)
        }
    }

    /**
     * Builds progress polyline vertices up to the current progress distance with +0.4m depth bias.
     */
    @JvmStatic
    fun buildProgressVertices(
        path: List<LatLngAltitude>,
        cumulativeDistances: DoubleArray,
        elapsedDistance: Double,
        currentLatLng: LatLng,
        waypointIndex: Int,
        altitudeMode: Int,
        baseAltitude: Double,
        pathAltitudeOffset: Double
    ): List<LatLngAltitude> {
        if (path.isEmpty()) return emptyList()

        val progressCoordinates = ArrayList<LatLngAltitude>()
        val clampedIndex = waypointIndex.coerceIn(0, path.size - 1)

        for (i in 0..clampedIndex) {
            val pt = path[i]
            val vertexAltitude = when (altitudeMode) {
                AltitudeMode.CLAMP_TO_GROUND -> 0.0
                AltitudeMode.ABSOLUTE -> pt.altitude + baseAltitude + pathAltitudeOffset + 0.4
                else -> pt.altitude + pathAltitudeOffset + 0.4
            }
            progressCoordinates.add(LatLngAltitude(pt.latitude, pt.longitude, vertexAltitude))
        }

        val lastWaypoint = path[clampedIndex]
        val lastLatLng = LatLng(lastWaypoint.latitude, lastWaypoint.longitude)
        val distToLast = SphericalUtil.computeDistanceBetween(lastLatLng, currentLatLng)

        if (distToLast >= 0.5) {
            val p1 = path[clampedIndex]
            val p2 = if (clampedIndex < path.size - 1) path[clampedIndex + 1] else p1
            val totalDistance = cumulativeDistances.lastOrNull() ?: 0.0
            val segStartDist = cumulativeDistances.getOrElse(clampedIndex) { 0.0 }
            val segEndDist = cumulativeDistances.getOrElse(clampedIndex + 1) { totalDistance }
            val segLen = segEndDist - segStartDist
            val fraction = if (segLen > 0) ((elapsedDistance - segStartDist) / segLen).coerceIn(0.0, 1.0) else 0.0
            val interpAlt = p1.altitude + fraction * (p2.altitude - p1.altitude)

            val progressAltitude = when (altitudeMode) {
                AltitudeMode.CLAMP_TO_GROUND -> 0.0
                AltitudeMode.ABSOLUTE -> interpAlt + baseAltitude + pathAltitudeOffset + 0.4
                else -> interpAlt + pathAltitudeOffset + 0.4
            }
            progressCoordinates.add(
                LatLngAltitude(currentLatLng.latitude, currentLatLng.longitude, progressAltitude)
            )
        }

        // Polyline requires at least 2 distinct vertices
        if (progressCoordinates.size < 2 && path.size >= 2) {
            val p0 = LatLng(path[0].latitude, path[0].longitude)
            val p1 = LatLng(path[1].latitude, path[1].longitude)
            val tinyForward = SphericalUtil.interpolate(p0, p1, 0.005)
            val startAlt = when (altitudeMode) {
                AltitudeMode.CLAMP_TO_GROUND -> 0.0
                AltitudeMode.ABSOLUTE -> path[0].altitude + baseAltitude + pathAltitudeOffset + 0.4
                else -> path[0].altitude + pathAltitudeOffset + 0.4
            }
            progressCoordinates.add(
                LatLngAltitude(tinyForward.latitude, tinyForward.longitude, startAlt)
            )
        }

        return progressCoordinates
    }
}
