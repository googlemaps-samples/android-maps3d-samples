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

import com.google.android.gms.maps3d.model.LatLngAltitude
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * JVM Unit Tests for [RoadTerrainAnalyzer].
 */
class RoadTerrainAnalyzerTest {

    @Test
    fun flatAndGentleRoute_emitsNoneAlertAndNoneSeverity() {
        val flatGentleRoute = listOf(
            LatLngAltitude(37.80600, -122.44800, 8.0),
            LatLngAltitude(37.80600, -122.44630, 8.5),
            LatLngAltitude(37.80600, -122.44460, 9.2),
            LatLngAltitude(37.80600, -122.44290, 9.8),
            LatLngAltitude(37.80600, -122.44120, 9.4),
            LatLngAltitude(37.80600, -122.43950, 8.8),
            LatLngAltitude(37.80600, -122.43780, 8.3),
            LatLngAltitude(37.80600, -122.43610, 8.0)
        )
        val state = RoadTerrainAnalyzer.analyze(
            path = flatGentleRoute,
            currentDistanceMeters = 0.0,
            lookaheadHorizonMeters = 500.0
        )

        assertEquals(TerrainAlertType.NONE, state.alertType)
        assertEquals(AlertSeverity.NONE, state.severity)
        assertTrue(state.upcomingExtrema.isEmpty())
        assertTrue(state.hazardPolylineCoordinates.isEmpty())
        assertEquals(80, state.recommendedSpeedKmh)
    }

    @Test
    fun approachingUndulatingSection_emitsCautionEarlyAndEscalatesToWarning() {
        val route = HillyRouteData.ROLLER_COASTER_PATH

        // 1. At 0m with a 150m horizon (before the ~179.4m roller-coaster dip/crest zone): -> NONE
        val shortHorizonAtFlatSection = RoadTerrainAnalyzer.analyze(
            path = route,
            currentDistanceMeters = 0.0,
            lookaheadHorizonMeters = 150.0
        )
        assertEquals(TerrainAlertType.NONE, shortHorizonAtFlatSection.alertType)
        assertEquals(AlertSeverity.NONE, shortHorizonAtFlatSection.severity)

        // 2. Approaching the ~179.4m roller-coaster section from 0m with 500m horizon:
        // hazard enters lookahead window -> UNDULATING_ROLLER_COASTER with CAUTION (>= 150m ahead)
        val firstCrestEntering = RoadTerrainAnalyzer.analyze(
            path = route,
            currentDistanceMeters = 0.0,
            lookaheadHorizonMeters = 500.0
        )
        assertEquals(TerrainAlertType.UNDULATING_ROLLER_COASTER, firstCrestEntering.alertType)
        assertEquals(AlertSeverity.CAUTION, firstCrestEntering.severity)
        assertTrue(firstCrestEntering.distanceToHazardMeters >= 150.0)
        assertTrue(firstCrestEntering.upcomingExtrema.isNotEmpty())
        assertEquals(ExtremaType.DIP, firstCrestEntering.upcomingExtrema.first().type)

        // 3. Within 150m of the hazard zone (at 60m, ~119m before ~179.4m) ->
        // escalates to AlertSeverity.WARNING
        val imminentState = RoadTerrainAnalyzer.analyze(
            path = route,
            currentDistanceMeters = 60.0,
            lookaheadHorizonMeters = 500.0
        )
        assertEquals(TerrainAlertType.UNDULATING_ROLLER_COASTER, imminentState.alertType)
        assertEquals(AlertSeverity.WARNING, imminentState.severity)
        assertTrue(imminentState.distanceToHazardMeters in 1.0..149.9)

        // 4. Actively inside the undulating hazard zone (at 250m) ->
        // distanceToHazardMeters == 0.0 and AlertSeverity.WARNING
        val activeInsideState = RoadTerrainAnalyzer.analyze(
            path = route,
            currentDistanceMeters = 250.0,
            lookaheadHorizonMeters = 500.0
        )
        assertEquals(TerrainAlertType.UNDULATING_ROLLER_COASTER, activeInsideState.alertType)
        assertEquals(AlertSeverity.WARNING, activeInsideState.severity)
        assertEquals(0.0, activeInsideState.distanceToHazardMeters, 0.001)
        assertTrue(activeInsideState.remainingHazardDistanceMeters > 100.0)
        assertTrue(activeInsideState.recommendedSpeedKmh < 80)
    }

    @Test
    fun crestAndDipInflectionPoints_detectedWithStableIdsAndExpectedGradeDeltas() {
        val route = HillyRouteData.ROLLER_COASTER_PATH

        val stateAt0m = RoadTerrainAnalyzer.analyze(
            path = route,
            currentDistanceMeters = 0.0,
            lookaheadHorizonMeters = 800.0
        )
        val stateAt70m = RoadTerrainAnalyzer.analyze(
            path = route,
            currentDistanceMeters = 70.0,
            lookaheadHorizonMeters = 800.0
        )

        assertTrue(stateAt0m.crestCount >= 1)
        assertTrue(stateAt0m.dipCount >= 2)

        val firstCrestAt0 = stateAt0m.upcomingExtrema.first { it.type == ExtremaType.CREST }
        val firstDipAt0 = stateAt0m.upcomingExtrema.first { it.type == ExtremaType.DIP }

        val firstCrestAt70 = stateAt70m.upcomingExtrema.first { it.type == ExtremaType.CREST }
        val firstDipAt70 = stateAt70m.upcomingExtrema.first { it.type == ExtremaType.DIP }

        // Stable IDs across frames as the vehicle advances
        assertEquals(firstCrestAt0.id, firstCrestAt70.id)
        assertEquals(firstDipAt0.id, firstDipAt70.id)

        // Distance ahead decreases by exactly 70m as vehicle moves from 0m to 70m
        assertEquals(
            firstCrestAt0.distanceAheadMeters - 70.0,
            firstCrestAt70.distanceAheadMeters,
            0.05
        )

        // Expected grade deltas: positive for CREST, negative for DIP
        assertTrue(firstCrestAt0.gradeDeltaPercent >= 15.0)
        assertTrue(firstDipAt0.gradeDeltaPercent <= -15.0)
    }

    @Test
    fun sustainedSteepClimb_blindCrest_and_steepDescent_classifiedAccurately() {
        val mountainRoute = listOf(
            LatLngAltitude(37.85500, -122.48500, 120.0),
            LatLngAltitude(37.85626, -122.48500, 122.0),
            LatLngAltitude(37.85761, -122.48500, 138.0),
            LatLngAltitude(37.85896, -122.48500, 155.0),
            LatLngAltitude(37.86031, -122.48500, 172.0),
            LatLngAltitude(37.86085, -122.48500, 178.0), // Summit Crest
            LatLngAltitude(37.86220, -122.48500, 161.0),
            LatLngAltitude(37.86355, -122.48500, 144.0),
            LatLngAltitude(37.86490, -122.48500, 127.0),
            LatLngAltitude(37.86616, -122.48500, 125.0)
        )

        // 1. On the initial climb before the summit (~650m) enters a 250m lookahead window
        val climbState = RoadTerrainAnalyzer.analyze(
            path = mountainRoute,
            currentDistanceMeters = 160.0,
            lookaheadHorizonMeters = 250.0
        )
        assertEquals(TerrainAlertType.STEEP_CLIMB, climbState.alertType)
        assertTrue(climbState.maxPositiveGradePercent >= 9.0)

        // 2. Approaching the single sharp summit crest (~650m) within a 250m window
        val summitCrestState = RoadTerrainAnalyzer.analyze(
            path = mountainRoute,
            currentDistanceMeters = 450.0,
            lookaheadHorizonMeters = 250.0
        )
        assertEquals(TerrainAlertType.BLIND_CREST, summitCrestState.alertType)
        assertEquals(1, summitCrestState.crestCount)
        assertEquals(0, summitCrestState.dipCount)

        // 3. Past the summit (~700m), descending the sustained -10.9% grade
        val descentState = RoadTerrainAnalyzer.analyze(
            path = mountainRoute,
            currentDistanceMeters = 700.0,
            lookaheadHorizonMeters = 250.0
        )
        assertEquals(TerrainAlertType.STEEP_DESCENT, descentState.alertType)
        assertTrue(descentState.maxNegativeGradePercent <= -9.0)
    }

    @Test
    fun duplicateConsecutivePoints_and_boundaryDistances_neverProduceNaNOrCrash() {
        val pathWithDuplicates = listOf(
            LatLngAltitude(37.78500, -122.41850, 40.0),
            LatLngAltitude(37.78500, -122.41850, 45.0), // Duplicate 2D coordinate
            LatLngAltitude(37.78563, -122.41860, 48.0),
            LatLngAltitude(37.78563, -122.41860, 48.0), // Duplicate 3D coordinate
            LatLngAltitude(37.78626, -122.41870, 40.0)
        )

        // Zero horizontal distance guard
        assertEquals(0.0, RoadTerrainAnalyzer.computeGradePercent(10.0, 0.0), 0.0001)
        assertEquals(0.0, RoadTerrainAnalyzer.computeGradePercent(10.0, 1e-6), 0.0001)

        // Start, negative, middle, and past-end distances
        val testDistances = listOf(-50.0, 0.0, 35.0, 141.0, 5000.0)
        for (dist in testDistances) {
            val state = RoadTerrainAnalyzer.analyze(
                path = pathWithDuplicates,
                currentDistanceMeters = dist,
                lookaheadHorizonMeters = 500.0
            )
            assertFalse(state.currentGradePercent.isNaN())
            assertFalse(state.maxPositiveGradePercent.isNaN())
            assertFalse(state.maxNegativeGradePercent.isNaN())
            assertFalse(state.oscillationScore.isNaN())
            assertFalse(state.distanceToHazardMeters.isNaN())
            for (pt in state.lookaheadProfile) {
                assertFalse(pt.altitudeMeters.isNaN())
                assertFalse(pt.distanceOffsetMeters.isNaN())
            }
        }

        // Empty and single-point routes
        val emptyState = RoadTerrainAnalyzer.analyze(emptyList(), 0.0, 500.0)
        assertEquals(TerrainAlertType.NONE, emptyState.alertType)

        val singlePointState = RoadTerrainAnalyzer.analyze(
            listOf(LatLngAltitude(37.0, -122.0, 10.0)),
            0.0,
            500.0
        )
        assertEquals(TerrainAlertType.NONE, singlePointState.alertType)
    }
}
