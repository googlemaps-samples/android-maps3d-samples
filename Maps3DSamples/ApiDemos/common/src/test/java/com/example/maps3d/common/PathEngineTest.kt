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

import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.LatLngAltitude
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * JVM Unit Tests for [PathEngine].
 */
class PathEngineTest {

    private val samplePath = listOf(
        LatLngAltitude(37.7749, -122.4194, 10.0),
        LatLngAltitude(37.7755, -122.4180, 20.0),
        LatLngAltitude(37.7760, -122.4170, 30.0)
    )

    @Test
    fun calculateCumulativeDistances_computesMonotonicallyIncreasingArray() {
        val cumDist = PathEngine.calculateCumulativeDistances(samplePath)
        assertEquals(3, cumDist.size)
        assertEquals(0.0, cumDist[0], 0.001)
        assertTrue(cumDist[1] > 0.0)
        assertTrue(cumDist[2] > cumDist[1])
    }

    @Test
    fun calculateCumulativeDistances_handlesEmpty() {
        val empty = PathEngine.calculateCumulativeDistances(emptyList())
        assertEquals(1, empty.size)
        assertEquals(0.0, empty[0], 0.001)
    }

    @Test
    fun interpolatePoint_atEndpointsAndMidpoints() {
        val cumDist = PathEngine.calculateCumulativeDistances(samplePath)
        val totalDist = cumDist.last()

        // At start (0m)
        val startPt = PathEngine.interpolatePoint(samplePath, cumDist, 0.0)
        assertEquals(samplePath.first().latitude, startPt.latLng.latitude, 0.0001)
        assertEquals(samplePath.first().longitude, startPt.latLng.longitude, 0.0001)
        assertEquals(10.0, startPt.altitude, 0.01)

        // At end (totalDist)
        val endPt = PathEngine.interpolatePoint(samplePath, cumDist, totalDist)
        assertEquals(samplePath.last().latitude, endPt.latLng.latitude, 0.0001)
        assertEquals(samplePath.last().longitude, endPt.latLng.longitude, 0.0001)
        assertEquals(30.0, endPt.altitude, 0.01)

        // At midpoint
        val midPt = PathEngine.interpolatePoint(samplePath, cumDist, totalDist * 0.5)
        assertTrue(midPt.latLng.latitude > samplePath.first().latitude)
        assertTrue(midPt.altitude > 10.0 && midPt.altitude < 30.0)
        assertTrue(midPt.bearing >= 0.0 && midPt.bearing <= 360.0)
    }

    @Test
    fun getInterpolatedLatLng_returnsBoundaryCoordinates() {
        val cumDist = PathEngine.calculateCumulativeDistances(samplePath)
        val totalDist = cumDist.last()

        val start = PathEngine.getInterpolatedLatLng(samplePath, cumDist, -10.0)
        assertEquals(samplePath.first().latitude, start.latitude, 0.0001)

        val end = PathEngine.getInterpolatedLatLng(samplePath, cumDist, totalDist + 50.0)
        assertEquals(samplePath.last().latitude, end.latitude, 0.0001)
    }

    @Test
    fun smoothHeading_appliesEmaWhenPlaying() {
        // Not playing -> returns target heading immediately
        val initial = PathEngine.smoothHeading(90.0, currentHeading = 0.0, isUserScrubbing = false, isPlaying = false)
        assertEquals(90.0, initial, 0.001)

        // User scrubbing -> returns target heading immediately
        val scrubbing = PathEngine.smoothHeading(90.0, currentHeading = 0.0, isUserScrubbing = true, isPlaying = true)
        assertEquals(90.0, scrubbing, 0.001)

        // Playing -> smoothed EMA step
        val smoothed = PathEngine.smoothHeading(90.0, currentHeading = 0.0, isUserScrubbing = false, isPlaying = true, smoothingFactor = 0.5)
        assertEquals(45.0, smoothed, 0.01)
    }

    @Test
    fun smoothHeading_appliesAdaptiveEmaBasedOnDelta() {
        // Delta <= 20 -> alpha = 0.16
        val straight = PathEngine.smoothHeading(10.0, currentHeading = 0.0, isUserScrubbing = false, isPlaying = true)
        assertEquals(1.6, straight, 0.01)

        // Delta in (20, 45] -> alpha = 0.28 (diff = 30 -> 30 * 0.28 = 8.4)
        val curve = PathEngine.smoothHeading(30.0, currentHeading = 0.0, isUserScrubbing = false, isPlaying = true)
        assertEquals(8.4, curve, 0.01)

        // Delta > 45 -> alpha = 0.40 (diff = 90 -> 90 * 0.40 = 36.0)
        val sharp = PathEngine.smoothHeading(90.0, currentHeading = 0.0, isUserScrubbing = false, isPlaying = true)
        assertEquals(36.0, sharp, 0.01)
    }

    @Test
    fun angularDifference_and_interpolateAngle_handlesWrapAround() {
        val diff1 = PathEngine.angularDifference(10.0, 350.0)
        assertEquals(20.0, diff1, 0.001)

        val diff2 = PathEngine.angularDifference(350.0, 10.0)
        assertEquals(-20.0, diff2, 0.001)

        val interp = PathEngine.interpolateAngle(350.0, 10.0, 0.5)
        assertEquals(0.0, interp, 0.001)
    }

    @Test
    fun profileRoute_calculatesAccurateBoundsAndCalibration() {
        val urbanProfile = PathEngine.profileRoute(PathData.URBAN_PATH)
        assertTrue(urbanProfile.totalDistance > 1000.0)
        assertEquals(1.2, urbanProfile.minAltitude, 0.01)
        assertEquals(10.0, urbanProfile.maxAltitude, 0.01)
        assertEquals(1.0, urbanProfile.baseAltitude, 0.01)
        assertTrue(urbanProfile.recommendedRange in 65.0f..250.0f)
        assertEquals(55.0f, urbanProfile.recommendedTilt, 0.01f)
        assertEquals(urbanProfile.recommendedSpeed * 5.0f, urbanProfile.speedSliderMax, 0.01f)

        val mountainProfile = PathEngine.profileRoute(PathData.MOUNTAIN_PATH)
        assertTrue(mountainProfile.totalDistance > 1950.0)
        assertEquals(343.8, mountainProfile.minAltitude, 0.01)
        assertEquals(457.0, mountainProfile.maxAltitude, 0.01)
        assertEquals(344.0, mountainProfile.baseAltitude, 0.01)
        assertEquals(48.0f, mountainProfile.recommendedTilt, 0.01f)
        assertTrue(mountainProfile.altitudeSliderMax > 457.0f)
        assertEquals(mountainProfile.recommendedSpeed * 5.0f, mountainProfile.speedSliderMax, 0.01f)
    }

    @Test
    fun calculateCameraAltitude_computesCorrectAltitudePerMode() {
        // CLAMP_TO_GROUND -> returns surfaceElevation (15.0 + (120 - 50)) + eyeLevelBias (3.5m) = 88.5m
        val clampAlt = PathEngine.calculateCameraAltitude(
            altitudeMode = AltitudeMode.CLAMP_TO_GROUND,
            baseAltitude = 50.0,
            interpolatedAltitude = 15.0,
            groundAltitude = 120.0,
            cameraRange = 100.0
        )
        assertEquals(88.5, clampAlt, 0.001)

        // ABSOLUTE -> surfaceElevation + pathAltitudeOffset + eyeLevelBias
        val absAlt = PathEngine.calculateCameraAltitude(
            altitudeMode = AltitudeMode.ABSOLUTE,
            baseAltitude = 50.0,
            interpolatedAltitude = 15.0,
            groundAltitude = 120.0,
            cameraRange = 100.0,
            pathAltitudeOffset = 5.0
        )
        assertEquals(93.5, absAlt, 0.001)

        // RELATIVE_TO_GROUND -> surfaceElevation + pathAltitudeOffset + eyeLevelBias
        val relAlt = PathEngine.calculateCameraAltitude(
            altitudeMode = AltitudeMode.RELATIVE_TO_GROUND,
            baseAltitude = 50.0,
            interpolatedAltitude = 15.0,
            groundAltitude = 120.0,
            cameraRange = 100.0,
            pathAltitudeOffset = 5.0
        )
        assertEquals(93.5, relAlt, 0.001)
    }

    @Test
    fun buildStaticVertices_and_buildProgressVertices() {
        // ABSOLUTE mode uses pt.altitude + pathAltitudeOffset
        val staticVerticesAbs = PathEngine.buildStaticVertices(
            path = samplePath,
            altitudeMode = AltitudeMode.ABSOLUTE,
            pathAltitudeOffset = 5.0
        )
        assertEquals(3, staticVerticesAbs.size)
        assertEquals(15.0, staticVerticesAbs[0].altitude, 0.01) // 10 + 5
        assertEquals(25.0, staticVerticesAbs[1].altitude, 0.01) // 20 + 5

        // RELATIVE_TO_GROUND uses pathAltitudeOffset directly as clearance
        val staticVerticesRel = PathEngine.buildStaticVertices(
            path = samplePath,
            altitudeMode = AltitudeMode.RELATIVE_TO_GROUND,
            pathAltitudeOffset = 5.0
        )
        assertEquals(3, staticVerticesRel.size)
        assertEquals(5.0, staticVerticesRel[0].altitude, 0.01)
        assertEquals(5.0, staticVerticesRel[1].altitude, 0.01)

        // CLAMP_TO_GROUND uses 0.0
        val staticVerticesClamp = PathEngine.buildStaticVertices(
            path = samplePath,
            altitudeMode = AltitudeMode.CLAMP_TO_GROUND,
            pathAltitudeOffset = 5.0
        )
        assertEquals(3, staticVerticesClamp.size)
        assertEquals(0.0, staticVerticesClamp[0].altitude, 0.01)

        val cumDist = PathEngine.calculateCumulativeDistances(samplePath)
        val interp = PathEngine.interpolatePoint(samplePath, cumDist, cumDist[1])
        val progressVertices = PathEngine.buildProgressVertices(
            path = samplePath,
            cumulativeDistances = cumDist,
            elapsedDistance = cumDist[1],
            currentLatLng = interp.latLng,
            waypointIndex = interp.waypointIndex,
            altitudeMode = AltitudeMode.ABSOLUTE,
            pathAltitudeOffset = 5.0
        )
        assertTrue(progressVertices.size >= 2)
        assertEquals(15.4, progressVertices[0].altitude, 0.01) // 15 + 0.4 depth bias
    }
}
