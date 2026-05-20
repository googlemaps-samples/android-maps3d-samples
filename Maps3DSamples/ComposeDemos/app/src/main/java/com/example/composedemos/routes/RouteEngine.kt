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

package com.example.composedemos.routes

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose3d.utils.GeoMathUtils
import com.google.maps.android.compose3d.utils.calculateHeading
import com.google.maps.android.compose3d.utils.haversineDistance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class PositionAndHeading(
    val position: LatLng,
    val heading: Float
)

object RouteEngine {
    fun calculatePositionAndHeading(
        route: List<LatLng>,
        cumulativeDistances: DoubleArray,
        distance: Double,
        lookaheadDistance: Double
    ): PositionAndHeading {
        val targetPos = GeoMathUtils.getInterpolatedPoint(distance, route, cumulativeDistances)
        
        // Calculate lookahead position for heading
        val lookaheadPos = GeoMathUtils.getInterpolatedPoint(distance + lookaheadDistance, route, cumulativeDistances)
        
        val heading = if (targetPos == lookaheadPos && distance > 0.0) {
            // If at the end, look back 1 meter to determine heading
            val prevPos = GeoMathUtils.getInterpolatedPoint(distance - 1.0, route, cumulativeDistances)
            calculateHeading(prevPos, targetPos).toFloat()
        } else {
            calculateHeading(targetPos, lookaheadPos).toFloat()
        }
        
        return PositionAndHeading(targetPos, heading)
    }

    fun getRouteTrackingFlow(
        routeFlow: Flow<List<LatLng>>,
        progressFlow: Flow<Float>,
        lookaheadDistance: Double = 1000.0
    ): Flow<PositionAndHeading> = combine(routeFlow, progressFlow) { route, progress ->
        if (route.size < 2) return@combine PositionAndHeading(LatLng(0.0, 0.0), 0f)
        
        val cumulativeDistances = DoubleArray(route.size)
        cumulativeDistances[0] = 0.0
        for (i in 1 until route.size) {
            cumulativeDistances[i] = cumulativeDistances[i - 1] + haversineDistance(route[i - 1], route[i])
        }
        val totalDistance = cumulativeDistances.last()
        val distance = totalDistance * progress.toDouble()
        
        calculatePositionAndHeading(route, cumulativeDistances, distance, lookaheadDistance)
    }
}
