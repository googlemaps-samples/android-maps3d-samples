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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * JVM Unit Tests for [PathPlaybackController].
 *
 * Verifies domain logic, kinematic progression, altitude computations, polyline slicing,
 * custom touch gesture adjustments (tilt, heading, pinch range, speed boost), and state transitions.
 */
class PathPlaybackControllerTest {

    private lateinit var controller: PathPlaybackController
    private val testPath = listOf(
        LatLngAltitude(37.7749, -122.4194, 10.0),
        LatLngAltitude(37.7755, -122.4180, 20.0),
        LatLngAltitude(37.7760, -122.4170, 30.0)
    )

    @Before
    fun setup() {
        controller = PathPlaybackController(testPath)
    }

    @Test
    fun initialState_isConfiguredCorrectly() {
        val state = controller.getState()
        assertEquals(testPath, state.route)
        assertEquals(0.0, state.elapsedDistance, 0.001)
        assertEquals(0f, state.progressRatio, 0.001f)
        assertFalse(state.isPlaying)
        assertFalse(state.isScrubbing)
        assertFalse(state.isSpeedBoosted)
        assertEquals(testPath.first().latitude, state.currentPosition.latitude, 0.0001)
        assertEquals(testPath.first().longitude, state.currentPosition.longitude, 0.0001)
        assertEquals(testPath.size, state.staticPolylineVertices.size)
        assertTrue(state.totalDistance > 0.0)
    }

    @Test
    fun togglePlayPause_updatesState() {
        assertFalse(controller.getState().isPlaying)
        val playingState = controller.togglePlayPause()
        assertTrue(playingState.isPlaying)
        val pausedState = controller.togglePlayPause()
        assertFalse(pausedState.isPlaying)
    }

    @Test
    fun advance_progressesDistanceOnlyWhenPlaying() {
        // When not playing, advance should have no effect
        val initialDist = controller.getState().elapsedDistance
        controller.advance(1.0)
        assertEquals(initialDist, controller.getState().elapsedDistance, 0.001)

        // When playing, advance increases elapsed distance based on speed * dt
        controller.setPlaying(true)
        val speed = controller.getState().followSpeedMps
        controller.advance(1.0)
        val advancedDist = controller.getState().elapsedDistance
        assertEquals(speed * 1.0, advancedDist, 0.1)
        assertTrue(controller.getState().progressRatio > 0f)
    }

    @Test
    fun advance_loopsAroundTotalDistance() {
        controller.setPlaying(true)
        val totalDist = controller.getState().totalDistance
        controller.seekToDistance(totalDist - 5.0)

        // Advancing beyond total distance wraps around
        controller.advance(1.0) // 30m step
        assertTrue(controller.getState().elapsedDistance < totalDist)
    }

    @Test
    fun speedBoost_supportsTieredMultipliers() {
        controller.setPlaying(true)
        controller.seekToDistance(0.0)
        val speed = controller.getState().followSpeedMps

        // 2x Boost
        controller.setSpeedBoostMultiplier(2.0)
        assertTrue(controller.getState().isSpeedBoosted)
        assertEquals(2.0, controller.getState().speedBoostMultiplier, 0.001)
        assertEquals(speed * 2.0, controller.getState().effectiveSpeedMps, 0.001)

        // 5x Warp Boost
        controller.setSpeedBoostMultiplier(5.0)
        assertTrue(controller.getState().isSpeedBoosted)
        assertEquals(5.0, controller.getState().speedBoostMultiplier, 0.001)
        assertEquals(speed * 5.0, controller.getState().effectiveSpeedMps, 0.001)

        controller.advance(1.0)
        assertEquals(speed * 5.0, controller.getState().elapsedDistance, 0.1)

        // Reset
        controller.setSpeedBoostMultiplier(1.0)
        assertFalse(controller.getState().isSpeedBoosted)
        assertEquals(speed, controller.getState().effectiveSpeedMps, 0.001)
    }

    @Test
    fun adjustTilt_modifiesCameraTiltWithinBounds() {
        controller.setCameraTilt(60.0)
        controller.adjustTilt(10.0)
        assertEquals(70.0, controller.getState().cameraTilt, 0.001)

        controller.adjustTilt(-80.0)
        assertEquals(0.0, controller.getState().cameraTilt, 0.001) // Clamped to 0.0

        controller.adjustTilt(100.0)
        assertEquals(85.0, controller.getState().cameraTilt, 0.001) // Clamped to 85.0
    }

    @Test
    fun adjustHeading_modifiesHeadingOffset() {
        controller.setHeadingOffset(0.0)
        controller.adjustHeading(45.0)
        assertEquals(45.0, controller.getState().headingOffset, 0.001)

        controller.adjustHeading(150.0) // 195 -> -165 normalized
        assertEquals(-165.0, controller.getState().headingOffset, 0.001)
    }

    @Test
    fun adjustRange_modifiesCameraRange() {
        controller.setCameraRange(300.0)
        controller.adjustRange(2.0) // 2x zoom in -> range 150m
        assertEquals(150.0, controller.getState().cameraRange, 0.001)

        controller.adjustRange(0.5) // zoom out -> range 300m
        assertEquals(300.0, controller.getState().cameraRange, 0.001)
    }

    @Test
    fun seekToRatio_updatesPositionAndPolylines() {
        val newState = controller.seekToRatio(0.5f)
        assertEquals(0.5f, newState.progressRatio, 0.01f)
        assertEquals(controller.getState().totalDistance * 0.5, newState.elapsedDistance, 0.5)
        assertTrue(newState.progressPolylineVertices.size >= 2)
    }

    @Test
    fun setAltitudeMode_recomputesPolylineAltitudes() {
        controller.setAltitudeMode(AltitudeMode.CLAMP_TO_GROUND)
        val clampVertices = controller.getState().staticPolylineVertices
        assertTrue(clampVertices.all { it.altitude == 0.0 })

        controller.setAltitudeMode(AltitudeMode.ABSOLUTE)
        val absVertices = controller.getState().staticPolylineVertices
        assertTrue(absVertices.any { it.altitude > 0.0 })
    }

    @Test
    fun setRoute_resetsProgressAndAppliesDefaults() {
        controller.setPlaying(true)
        controller.seekToRatio(0.8f)

        val newRoute = PathData.RURAL_PATH
        val state = controller.setRoute(newRoute, applyDefaults = true)

        assertEquals(newRoute, state.route)
        assertEquals(0.0, state.elapsedDistance, 0.001)
        assertEquals(0f, state.progressRatio, 0.001f)
        assertFalse(state.isPlaying)
        assertEquals(450.0, state.cameraRange, 0.001)
        assertEquals(75.0, state.cameraTilt, 0.001)
    }

    @Test
    fun rewind_advancesBackwardsAlongPath() {
        controller.setPlaying(true)
        val totalDist = controller.getState().totalDistance
        controller.seekToDistance(totalDist * 0.5)
        val initialDist = controller.getState().elapsedDistance

        // Set negative speed multiplier (-5x rewind)
        controller.setSpeedBoostMultiplier(-5.0)
        val speed = controller.getState().followSpeedMps
        assertEquals(-5.0 * speed, controller.getState().effectiveSpeedMps, 0.001)

        controller.advance(0.1)
        val newDist = controller.getState().elapsedDistance
        assertTrue(newDist < initialDist)
    }

    @Test
    fun skipDistance_and_skipRatio_jumpAlongPath() {
        val totalDist = controller.getState().totalDistance
        controller.seekToDistance(totalDist * 0.5)

        // Skip forward 10%
        controller.skipRatio(0.10f)
        assertEquals(totalDist * 0.60, controller.getState().elapsedDistance, 0.5)

        // Skip backward 20%
        controller.skipRatio(-0.20f)
        assertEquals(totalDist * 0.40, controller.getState().elapsedDistance, 0.5)
    }
}