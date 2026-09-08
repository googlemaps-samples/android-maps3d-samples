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
import kotlin.math.abs

/**
 * Result of path interpolation containing LatLng, segment index, lookahead bearing, and altitude.
 *
 * @property latLng The interpolated 2D geographic coordinates.
 * @property waypointIndex The route vertex index immediately preceding or at the current position.
 * @property bearing The forward camera heading in degrees [0.0, 360.0).
 * @property altitude The interpolated elevation in meters along the route segment.
 */
data class InterpolatedPathPoint(
    @JvmField val latLng: LatLng,
    @JvmField val waypointIndex: Int,
    @JvmField val bearing: Double,
    @JvmField val altitude: Double
)

/**
 * Metrics and dynamic camera calibration parameters computed from route geometry.
 *
 * Automatically calibrated upon route loading to configure optimal camera framing,
 * tilt angles, follow speed, and responsive slider boundaries without hardcoded values.
 *
 * @property totalDistance Total cumulative length of the route in meters.
 * @property avgSegmentLength Average distance between consecutive route vertices.
 * @property minAltitude Minimum elevation along the route in meters.
 * @property maxAltitude Maximum elevation along the route in meters.
 * @property baseAltitude Baseline reference elevation (minimum route altitude).
 * @property topographicGrade Overall vertical climb relative to total distance (Δalt / L_total).
 * @property recommendedRange Dynamically calibrated camera distance from target.
 * @property recommendedTilt Dynamically calibrated camera pitch angle.
 * @property recommendedSpeed Follow speed scaled to traverse the route in ~75 seconds.
 * @property altitudeSliderMin Lower bound for the altitude trim slider.
 * @property altitudeSliderMax Upper bound for the altitude trim slider.
 * @property rangeSliderMin Lower bound for the camera range slider.
 * @property rangeSliderMax Upper bound for the camera range slider.
 * @property speedSliderMin Minimum speed slider value.
 * @property speedSliderMax Maximum speed slider value.
 */
data class RouteProfile(
    @JvmField val totalDistance: Double,
    @JvmField val avgSegmentLength: Double,
    @JvmField val minAltitude: Double,
    @JvmField val maxAltitude: Double,
    @JvmField val baseAltitude: Double,
    @JvmField val topographicGrade: Double,
    @JvmField val recommendedRange: Float,
    @JvmField val recommendedTilt: Float,
    @JvmField val recommendedSpeed: Float,
    @JvmField val altitudeSliderMin: Float,
    @JvmField val altitudeSliderMax: Float,
    @JvmField val rangeSliderMin: Float,
    @JvmField val rangeSliderMax: Float,
    @JvmField val speedSliderMin: Float = 5.0f,
    @JvmField val speedSliderMax: Float = 150.0f
)

/**
 * Math and geometry engine for ground-level 3D path following.
 *
 * Encapsulates segment search, distance accumulation, kinematic heading smoothing,
 * dynamic route profiling, and elevation interpolation across Kotlin Views, Java Views, and Compose.
 */
object PathEngine {

    const val STATIC_POLYLINE_ID = "path_following_static_route"
    const val PROGRESS_POLYLINE_ID = "path_following_progress_route"

    /**
     * Extracts geographic and elevation metrics from an arbitrary path in O(N) time
     * to compute optimal camera framing and UI slider configurations.
     */
    @JvmStatic
    fun profileRoute(path: List<LatLngAltitude>): RouteProfile {
        if (path.isEmpty()) {
            return RouteProfile(
                totalDistance = 0.0,
                avgSegmentLength = 50.0,
                minAltitude = 0.0,
                maxAltitude = 0.0,
                baseAltitude = 0.0,
                topographicGrade = 0.0,
                recommendedRange = 100.0f,
                recommendedTilt = 55.0f,
                recommendedSpeed = 30.0f,
                altitudeSliderMin = 0.0f,
                altitudeSliderMax = 100.0f,
                rangeSliderMin = 20.0f,
                rangeSliderMax = 500.0f,
                speedSliderMin = 5.0f,
                speedSliderMax = 150.0f
            )
        }

        var totalDist = 0.0
        var minAlt = path.first().altitude
        var maxAlt = minAlt

        for (i in 1 until path.size) {
            val pPrev = LatLng(path[i - 1].latitude, path[i - 1].longitude)
            val pCurr = LatLng(path[i].latitude, path[i].longitude)
            totalDist += SphericalUtil.computeDistanceBetween(pPrev, pCurr)
            minAlt = minOf(minAlt, path[i].altitude)
            maxAlt = maxOf(maxAlt, path[i].altitude)
        }

        val avgSegLen = if (path.size > 1 && totalDist > 0.0) totalDist / (path.size - 1) else 50.0
        val altSpan = maxAlt - minAlt
        val topoGrade = if (totalDist > 0.0) altSpan / totalDist else 0.0

        // 1. Camera Range: R = clamp(2.5 * avgSegLen, 65m, 250m)
        val recRange = (avgSegLen * 2.5).toFloat().coerceIn(65.0f, 250.0f)
        val rangeMin = maxOf(20.0f, kotlin.math.round(recRange * 0.3f))
        val rangeMax = minOf(1000.0f, kotlin.math.round(recRange * 2.5f))

        // 2. Base Altitude & Altitude Slider Bounds
        val baseAlt = kotlin.math.round(minAlt)
        val altMin: Float
        val altMax: Float
        if (minAlt > 50.0) {
            altMin = maxOf(0.0f, kotlin.math.round(minAlt - 100.0).toFloat())
            altMax = kotlin.math.round(maxAlt + 350.0).toFloat()
        } else {
            altMin = 0.0f
            altMax = maxOf(100.0f, kotlin.math.round(maxAlt + 50.0).toFloat())
        }

        // 3. Camera Tilt: 48° for steep grades / mountain switchbacks, 55° for urban/open highways
        val isSteepOrSwitchback = topoGrade > 0.08 || avgSegLen < 35.0
        val recTilt = if (isSteepOrSwitchback) 48.0f else 55.0f

        // 4. Follow Speed: Scaled to complete route in ~75s
        val recSpeed = if (totalDist > 0.0) {
            (totalDist / 75.0).toFloat().coerceIn(10.0f, 40.0f)
        } else {
            30.0f
        }
        val speedMax = recSpeed * 5.0f

        return RouteProfile(
            totalDistance = totalDist,
            avgSegmentLength = avgSegLen,
            minAltitude = minAlt,
            maxAltitude = maxAlt,
            baseAltitude = baseAlt,
            topographicGrade = topoGrade,
            recommendedRange = recRange,
            recommendedTilt = recTilt,
            recommendedSpeed = recSpeed,
            altitudeSliderMin = altMin,
            altitudeSliderMax = altMax,
            rangeSliderMin = rangeMin,
            rangeSliderMax = rangeMax,
            speedSliderMin = 5.0f,
            speedSliderMax = speedMax
        )
    }

    /**
     * Calculates the shortest angular difference between two compass bearings (-180° to +180°).
     */
    @JvmStatic
    fun angularDifference(angleA: Double, angleB: Double): Double {
        var diff = (angleA - angleB) % 360.0
        if (diff > 180.0) diff -= 360.0
        if (diff < -180.0) diff += 360.0
        return diff
    }

    /**
     * Performs spherical interpolation between two angles along the shortest arc.
     */
    @JvmStatic
    fun interpolateAngle(fromAngle: Double, toAngle: Double, fraction: Double): Double {
        val diff = angularDifference(toAngle, fromAngle)
        return (fromAngle + diff * fraction.coerceIn(0.0, 1.0) + 360.0) % 360.0
    }

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
     * Helper to interpolate 2D LatLng position at any distance along the route.
     */
    @JvmStatic
    fun getInterpolatedLatLng(
        path: List<LatLngAltitude>,
        cumulativeDistances: DoubleArray,
        distance: Double
    ): LatLng {
        if (path.isEmpty()) return LatLng(0.0, 0.0)
        val totalDistance = cumulativeDistances.lastOrNull() ?: 0.0
        if (distance <= 0.0) return LatLng(path.first().latitude, path.first().longitude)
        if (distance >= totalDistance) return LatLng(path.last().latitude, path.last().longitude)

        var index = 0
        while (index < cumulativeDistances.size - 1 && cumulativeDistances[index + 1] < distance) {
            index++
        }

        val p1 = path[index]
        val p2 = if (index < path.size - 1) path[index + 1] else p1
        val d1 = cumulativeDistances[index]
        val d2 = cumulativeDistances.getOrElse(index + 1) { totalDistance }
        val segLen = d2 - d1
        val fraction = if (segLen > 0) ((distance - d1) / segLen).coerceIn(0.0, 1.0) else 0.0

        val latLng1 = LatLng(p1.latitude, p1.longitude)
        val latLng2 = LatLng(p2.latitude, p2.longitude)
        return SphericalUtil.interpolate(latLng1, latLng2, fraction)
    }

    /**
     * Finds the interpolated geographic position, smooth forward lookahead bearing with
     * corner-transition blending, and elevation at a target distance.
     */
    @JvmStatic
    @JvmOverloads
    fun interpolatePoint(
        path: List<LatLngAltitude>,
        cumulativeDistances: DoubleArray,
        distance: Double,
        lookaheadDistance: Double = 25.0
    ): InterpolatedPathPoint {
        if (path.isEmpty()) {
            return InterpolatedPathPoint(
                latLng = LatLng(0.0, 0.0),
                waypointIndex = 0,
                bearing = 0.0,
                altitude = 0.0
            )
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
        val interpAlt = p1.altitude + fraction * (p2.altitude - p1.altitude)

        // Smooth forward lookahead tangent heading calculation
        val targetLookaheadDist = (distance + lookaheadDistance).coerceAtMost(totalDistance)
        val lookaheadPos = getInterpolatedLatLng(path, cumulativeDistances, targetLookaheadDist)

        var bearing = if (targetLookaheadDist > distance && currentLatLng != lookaheadPos) {
            SphericalUtil.computeHeading(currentLatLng, lookaheadPos)
        } else if (distance > 1.0) {
            val prevPos = getInterpolatedLatLng(path, cumulativeDistances, distance - 1.0)
            SphericalUtil.computeHeading(prevPos, currentLatLng)
        } else if (path.size >= 2) {
            SphericalUtil.computeHeading(
                LatLng(path[0].latitude, path[0].longitude),
                LatLng(path[1].latitude, path[1].longitude)
            )
        } else {
            0.0
        }

        // Slerp corner blending if approaching the end of current segment (within 8 meters)
        val distToEndOfSeg = segEndDist - distance
        if (distToEndOfSeg in 0.0..8.0 && index < path.size - 2) {
            val nextP1 = LatLng(path[index + 1].latitude, path[index + 1].longitude)
            val nextP2 = LatLng(path[index + 2].latitude, path[index + 2].longitude)
            val nextBearing = SphericalUtil.computeHeading(nextP1, nextP2)
            val blendFactor = ((8.0 - distToEndOfSeg) / 8.0).coerceIn(0.0, 1.0) * 0.45
            bearing = interpolateAngle(bearing, nextBearing, blendFactor)
        }

        return InterpolatedPathPoint(
            latLng = currentLatLng,
            waypointIndex = index,
            bearing = (bearing + 360.0) % 360.0,
            altitude = interpAlt
        )
    }

    /**
     * Applies an adaptive Exponential Moving Average (EMA) filter to camera heading to smooth
     * abrupt turns around corners without lag during real-time playback.
     */
    @JvmStatic
    @JvmOverloads
    fun smoothHeading(
        targetHeading: Double,
        currentHeading: Double?,
        isUserScrubbing: Boolean,
        isPlaying: Boolean,
        smoothingFactor: Double? = null
    ): Double {
        val normalizedTarget = (targetHeading % 360.0 + 360.0) % 360.0
        if (currentHeading == null || isUserScrubbing || !isPlaying) {
            return normalizedTarget
        }

        val diff = angularDifference(normalizedTarget, currentHeading)

        val alpha = smoothingFactor ?: when {
            abs(diff) > 45.0 -> 0.40 // Fast turn tracking
            abs(diff) > 20.0 -> 0.28 // Moderate curve tracking
            else -> 0.16             // Smooth straightaway tracking
        }

        return (currentHeading + diff * alpha + 360.0) % 360.0
    }

    /**
     * Calculates camera target altitude based on the active altitude mode, route elevation,
     * ground elevation trim, and an eye-level bias to prevent terrain clipping.
     */
    @JvmStatic
    @JvmOverloads
    fun calculateCameraAltitude(
        altitudeMode: Int,
        baseAltitude: Double,
        interpolatedAltitude: Double,
        groundAltitude: Double,
        cameraRange: Double = 100.0,
        pathAltitudeOffset: Double = 0.0
    ): Double {
        val eyeLevelBias = (cameraRange * 0.035).coerceIn(2.0, 8.0)
        val groundTrim = groundAltitude - baseAltitude
        val surfaceElevation = interpolatedAltitude + groundTrim

        return when (altitudeMode) {
            AltitudeMode.CLAMP_TO_GROUND -> surfaceElevation + eyeLevelBias
            else -> surfaceElevation + pathAltitudeOffset + eyeLevelBias
        }
    }

    /**
     * Builds static route polyline vertices with altitude mode adjustments.
     */
    @JvmStatic
    fun buildStaticVertices(
        path: List<LatLngAltitude>,
        altitudeMode: Int,
        pathAltitudeOffset: Double
    ): List<LatLngAltitude> {
        return path.map { pt ->
            val vertexAltitude = when (altitudeMode) {
                AltitudeMode.CLAMP_TO_GROUND -> 0.0
                AltitudeMode.ABSOLUTE -> pt.altitude + pathAltitudeOffset
                AltitudeMode.RELATIVE_TO_GROUND, AltitudeMode.RELATIVE_TO_MESH -> pathAltitudeOffset
                else -> pathAltitudeOffset
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
        pathAltitudeOffset: Double
    ): List<LatLngAltitude> {
        if (path.isEmpty()) return emptyList()

        val progressCoordinates = mutableListOf<LatLngAltitude>()
        val clampedIndex = waypointIndex.coerceIn(0, path.size - 1)

        for (i in 0..clampedIndex) {
            val pt = path[i]
            val vertexAltitude = when (altitudeMode) {
                AltitudeMode.CLAMP_TO_GROUND -> 0.0
                AltitudeMode.ABSOLUTE -> pt.altitude + pathAltitudeOffset + 0.4
                AltitudeMode.RELATIVE_TO_GROUND, AltitudeMode.RELATIVE_TO_MESH -> pathAltitudeOffset + 0.4
                else -> pathAltitudeOffset + 0.4
            }
            progressCoordinates.add(
                LatLngAltitude(pt.latitude, pt.longitude, vertexAltitude)
            )
        }

        val lastWaypoint = path[clampedIndex]
        val lastLatLng = LatLng(lastWaypoint.latitude, lastWaypoint.longitude)
        val distToLast = SphericalUtil.computeDistanceBetween(lastLatLng, currentLatLng)

        if (distToLast >= 0.05) {
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
                AltitudeMode.ABSOLUTE -> interpAlt + pathAltitudeOffset + 0.4
                AltitudeMode.RELATIVE_TO_GROUND, AltitudeMode.RELATIVE_TO_MESH -> pathAltitudeOffset + 0.4
                else -> pathAltitudeOffset + 0.4
            }
            progressCoordinates.add(
                LatLngAltitude(currentLatLng.latitude, currentLatLng.longitude, progressAltitude)
            )
        }

        // Polyline requires at least 2 distinct vertices
        if (progressCoordinates.size < 2 && path.size >= 2) {
            val p0 = LatLng(path[0].latitude, path[0].longitude)
            val p1 = LatLng(path[1].latitude, path[1].longitude)
            val segDist = SphericalUtil.computeDistanceBetween(p0, p1)
            val tinyFraction = if (segDist > 0.0) (0.05 / segDist).coerceIn(0.0001, 0.1) else 0.001
            val tinyForward = SphericalUtil.interpolate(p0, p1, tinyFraction)
            val startAlt = when (altitudeMode) {
                AltitudeMode.CLAMP_TO_GROUND -> 0.0
                AltitudeMode.ABSOLUTE -> path[0].altitude + pathAltitudeOffset + 0.4
                AltitudeMode.RELATIVE_TO_GROUND, AltitudeMode.RELATIVE_TO_MESH -> pathAltitudeOffset + 0.4
                else -> pathAltitudeOffset + 0.4
            }
            progressCoordinates.add(
                LatLngAltitude(tinyForward.latitude, tinyForward.longitude, startAlt)
            )
        }

        return progressCoordinates
    }
}
