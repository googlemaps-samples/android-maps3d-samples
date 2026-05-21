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
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Represents the result of interpolating a position along a 3D map route.
 *
 * @property position The interpolated LatLng coordinate of the target object.
 * @property heading The calculated heading (bearing) in degrees, clockwise from North.
 */
data class PositionAndHeading(
    val position: LatLng,
    val heading: Float
)

/**
 * A shared, highly-optimized math and physics engine designed to calculate real-time positions
 * and orientations for objects (like cars or drones) moving along complex geographic coordinates.
 *
 * Accessible to both Kotlin and Java View-based sample modules.
 */
object RouteEngine {

    /**
     * Precomputes a cumulative distance array in meters for a given list of coordinates.
     *
     * @param route A list of [LatLng] coordinates representing the route.
     * @return A DoubleArray where each index holds the total distance from index 0 to that index.
     */
    @JvmStatic
    fun calculateCumulativeDistances(route: List<LatLng>): DoubleArray {
        if (route.isEmpty()) return doubleArrayOf(0.0)
        
        val cumulativeDistances = DoubleArray(route.size)
        cumulativeDistances[0] = 0.0
        for (i in 1 until route.size) {
            cumulativeDistances[i] = cumulativeDistances[i - 1] + haversineDistance(route[i - 1], route[i])
        }
        return cumulativeDistances
    }

    /**
     * Calculates the absolute coordinate at a specific distance along the pre-computed route path.
     *
     * @param distance The target distance in meters to locate.
     * @param route The list of coordinates.
     * @param cumulativeDistances The pre-computed cumulative distances corresponding to the route.
     * @return The interpolated [LatLng] coordinate.
     */
    @JvmStatic
    fun getInterpolatedPoint(
        distance: Double,
        route: List<LatLng>,
        cumulativeDistances: DoubleArray
    ): LatLng {
        if (distance <= 0.0) return route.first()
        if (distance >= cumulativeDistances.last()) return route.last()

        var idx = cumulativeDistances.binarySearch(distance)
        if (idx < 0) {
            idx = -(idx + 1) - 1
        }
        idx = idx.coerceIn(0, cumulativeDistances.size - 2)

        val p1 = route[idx]
        val p2 = route[idx + 1]
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
     * Computes both the geographic position and the rotational heading of a vehicle at a
     * given distance along the route.
     *
     * @param route The list of route coordinates.
     * @param cumulativeDistances The pre-computed cumulative distances corresponding to the route.
     * @param distance The target distance in meters along the route.
     * @param lookaheadDistance The forward-looking distance in meters used to predict heading.
     * @return The calculated [PositionAndHeading] structure.
     */
    @JvmStatic
    @JvmOverloads
    fun calculatePositionAndHeading(
        route: List<LatLng>,
        cumulativeDistances: DoubleArray,
        distance: Double,
        lookaheadDistance: Double = 30.0
    ): PositionAndHeading {
        val targetPos = getInterpolatedPoint(distance, route, cumulativeDistances)
        val lookaheadPos = getInterpolatedPoint(distance + lookaheadDistance, route, cumulativeDistances)
        
        val heading = if (targetPos == lookaheadPos && distance > 0.0) {
            val prevPos = getInterpolatedPoint(distance - 1.0, route, cumulativeDistances)
            calculateHeading(prevPos, targetPos).toFloat()
        } else {
            calculateHeading(targetPos, lookaheadPos).toFloat()
        }
        
        return PositionAndHeading(targetPos, heading)
    }

    /**
     * Calculates the distance in meters between two [LatLng] points using the Haversine formula.
     */
    @JvmStatic
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

    /**
     * Calculates the bearing (heading) from one LatLng coordinate to another in degrees.
     */
    @JvmStatic
    fun calculateHeading(from: LatLng, to: LatLng): Double {
        val lat1 = Math.toRadians(from.latitude)
        val lon1 = Math.toRadians(from.longitude)
        val lat2 = Math.toRadians(to.latitude)
        val lon2 = Math.toRadians(to.longitude)

        val dLon = lon2 - lon1
        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)

        val bearing = Math.toDegrees(atan2(y, x))
        return (bearing + 360.0) % 360.0
    }
}
