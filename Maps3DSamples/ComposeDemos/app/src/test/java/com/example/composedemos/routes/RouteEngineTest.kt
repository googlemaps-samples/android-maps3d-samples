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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RouteEngineTest {

    @Test
    fun testInterpolationAtStart() {
        val route = listOf(
            LatLng(0.0, 0.0),
            LatLng(1.0, 1.0)
        )
        val cumulativeDistances = doubleArrayOf(0.0, 157000.0) // Approx distance in meters for 1 deg
        
        val result = RouteEngine.calculatePositionAndHeading(
            route = route,
            cumulativeDistances = cumulativeDistances,
            distance = 0.0,
            lookaheadDistance = 1000.0
        )
        
        // We expect to be at the start
        assertEquals(0.0, result.position.latitude, 0.001)
        assertEquals(0.0, result.position.longitude, 0.001)
        // And heading towards (1,1) which is approx 45 degrees
        assertEquals(45.0, result.heading.toDouble(), 5.0)
    }

    @Test
    fun testGetRouteTrackingFlow() = runBlocking {
        val routeFlow = MutableStateFlow(listOf(LatLng(0.0, 0.0), LatLng(1.0, 1.0)))
        val progressFlow = MutableStateFlow(0f)
        
        val trackingFlow = RouteEngine.getRouteTrackingFlow(routeFlow, progressFlow, 1000.0)
        
        val result1 = trackingFlow.first()
        assertEquals(0.0, result1.position.latitude, 0.001)
        assertEquals(0.0, result1.position.longitude, 0.001)
        assertEquals(45.0, result1.heading.toDouble(), 5.0)
        
        // Emit new progress (halfway)
        progressFlow.value = 0.5f
        val result2 = trackingFlow.first()
        
        // Halfway between (0,0) and (1,1) should be approx (0.5, 0.5)
        assertEquals(0.5, result2.position.latitude, 0.1)
        assertEquals(0.5, result2.position.longitude, 0.1)
    }

    @Test
    fun testComplexRouteOrientation() {
        val route = listOf(
            LatLng(0.0, 0.0),
            LatLng(1.0, 0.0), // Moving North (Heading 0)
            LatLng(1.0, 1.0)  // Moving East (Heading 90)
        )
        val cumulativeDistances = doubleArrayOf(0.0, 111000.0, 222000.0) // Approx 111km per degree
        
        // Test on first segment (moving North)
        val result1 = RouteEngine.calculatePositionAndHeading(
            route = route,
            cumulativeDistances = cumulativeDistances,
            distance = 50000.0,
            lookaheadDistance = 1000.0
        )
        assertEquals(0.0, result1.heading.toDouble(), 5.0)
        
        // Test on second segment (moving East)
        val result2 = RouteEngine.calculatePositionAndHeading(
            route = route,
            cumulativeDistances = cumulativeDistances,
            distance = 160000.0,
            lookaheadDistance = 1000.0
        )
        assertEquals(90.0, result2.heading.toDouble(), 5.0)

        // Test at the end of the route
        val result3 = RouteEngine.calculatePositionAndHeading(
            route = route,
            cumulativeDistances = cumulativeDistances,
            distance = 222000.0,
            lookaheadDistance = 1000.0
        )
        // We expect it to keep the heading of the last segment (90 degrees)
        assertEquals(90.0, result3.heading.toDouble(), 5.0)
    }
}
