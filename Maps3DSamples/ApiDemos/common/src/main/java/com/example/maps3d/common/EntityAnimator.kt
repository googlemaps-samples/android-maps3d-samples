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
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.maps.android.SphericalUtil

/**
 * Interface for pluggable entity animators that update an entity pose over time.
 */
interface EntityAnimator {
    /**
     * Calculates the entity pose for the given elapsed and total duration.
     */
    fun update(elapsedMs: Long, totalDurationMs: Long): EntityPose

    /**
     * Returns whether the animation has completed its duration.
     */
    fun isFinished(elapsedMs: Long, totalDurationMs: Long): Boolean

    /**
     * Resets internal state if necessary.
     */
    fun reset()
}

/**
 * Discrete action animator that holds the entity at [startPose] until exactly half the duration,
 * then instantly transitions to [endPose].
 *
 * Exhibits near-zero CPU overhead because intermediate geometry calculations are bypassed.
 */
class MidpointJumpAnimator(
    val startPose: EntityPose,
    val endPose: EntityPose
) : EntityAnimator {

    override fun update(elapsedMs: Long, totalDurationMs: Long): EntityPose {
        val midpoint = totalDurationMs / 2
        return if (elapsedMs < midpoint) startPose else endPose
    }

    override fun isFinished(elapsedMs: Long, totalDurationMs: Long): Boolean =
        elapsedMs >= totalDurationMs

    override fun reset() {}
}

/**
 * Continuous trajectory animator that interpolates the entity pose along a multi-waypoint path
 * based on elapsed time.
 *
 * Exhibits higher CPU load due to spherical trigonometry calculations executed on each frame tick.
 */
class TrajectoryFlightAnimator(
    val waypoints: List<LatLng>,
    val altitude: Double = 250.0,
    val scale: Double = 0.08
) : EntityAnimator {

    private val cumulativeDistances: DoubleArray = calculateCumulativeDistances(waypoints)
    val totalDistance: Double = cumulativeDistances.lastOrNull() ?: 0.0

    override fun update(elapsedMs: Long, totalDurationMs: Long): EntityPose {
        if (totalDurationMs <= 0L || waypoints.isEmpty()) {
            val defaultLoc = waypoints.firstOrNull() ?: LatLng(0.0, 0.0)
            return EntityPose(LatLngAltitude(defaultLoc.latitude, defaultLoc.longitude, altitude), 0.0, -90.0, 0.0, scale)
        }

        val fraction = (elapsedMs.toDouble() / totalDurationMs).coerceIn(0.0, 1.0)
        val targetDist = fraction * totalDistance
        val point = interpolateFlightPoint(waypoints, cumulativeDistances, targetDist)

        return EntityPose(
            position = LatLngAltitude(point.position.latitude, point.position.longitude, altitude),
            heading = normalizeHeading(point.bearing + 180.0), // Airplane glTF asset 180° mesh alignment offset
            pitch = -90.0,
            roll = 0.0,
            scale = scale
        )
    }

    override fun isFinished(elapsedMs: Long, totalDurationMs: Long): Boolean =
        elapsedMs >= totalDurationMs

    override fun reset() {}

    companion object {
        fun normalizeHeading(headingDeg: Double): Double {
            val normalized = headingDeg % 360.0
            return if (normalized < 0.0) normalized + 360.0 else normalized
        }

        fun calculateCumulativeDistances(path: List<LatLng>): DoubleArray {
            if (path.isEmpty()) return doubleArrayOf(0.0)
            val distances = DoubleArray(path.size)
            distances[0] = 0.0
            for (i in 1 until path.size) {
                distances[i] = distances[i - 1] + SphericalUtil.computeDistanceBetween(path[i - 1], path[i])
            }
            return distances
        }

        data class InterpolatedFlightPoint(
            val position: LatLng,
            val bearing: Double,
            val waypointIndex: Int
        )

        fun interpolateFlightPoint(
            path: List<LatLng>,
            cumulativeDistances: DoubleArray,
            distance: Double
        ): InterpolatedFlightPoint {
            if (path.isEmpty()) return InterpolatedFlightPoint(LatLng(0.0, 0.0), 0.0, 0)
            val totalDist = cumulativeDistances.lastOrNull() ?: 0.0
            var index = 0
            while (index < cumulativeDistances.size - 1 && cumulativeDistances[index + 1] < distance) {
                index++
            }

            val p1 = path[index]
            val p2 = if (index < path.size - 1) path[index + 1] else p1
            val d1 = cumulativeDistances.getOrElse(index) { 0.0 }
            val d2 = cumulativeDistances.getOrElse(index + 1) { totalDist }
            val segLen = d2 - d1
            val fraction = if (segLen > 0) ((distance - d1) / segLen).coerceIn(0.0, 1.0) else 0.0

            val currentLatLng = SphericalUtil.interpolate(p1, p2, fraction)
            val bearing = if (p1 != p2) SphericalUtil.computeHeading(p1, p2) else 105.0

            return InterpolatedFlightPoint(
                position = currentLatLng,
                bearing = normalizeHeading(bearing),
                waypointIndex = index
            )
        }
    }
}

/**
 * Continuous 360° orbital spin animator rotating around a central landmark.
 */
class ContinuousOrbitAnimator(
    val center: LatLng,
    initialHeading: Double = 105.0,
    val altitude: Double = 250.0,
    val speedDegPerSec: Double = 25.0
) {
    private var currentHeading: Double = initialHeading

    fun tick(deltaTimeSeconds: Double): Double {
        currentHeading = (currentHeading + speedDegPerSec * deltaTimeSeconds) % 360.0
        return currentHeading
    }

    fun getHeading(): Double = currentHeading

    fun reset(initialHeading: Double = 105.0) {
        currentHeading = initialHeading
    }
}
