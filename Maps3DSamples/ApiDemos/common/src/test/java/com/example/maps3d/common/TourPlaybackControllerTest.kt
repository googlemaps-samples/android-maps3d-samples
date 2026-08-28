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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * JVM Unit Tests for [TourPlaybackController].
 */
class TourPlaybackControllerTest {

    private lateinit var controller: TourPlaybackController

    @Before
    fun setup() {
        controller = TourPlaybackController()
    }

    @Test
    fun initialState_isConfiguredCorrectly() {
        val state = controller.getState()
        assertEquals(AnimationApproach.DISPATCHER_FRAME_LOOP, state.selectedApproach)
        assertFalse(state.isPlaying)
        assertFalse(state.isFinished)
        assertEquals(0.0, state.elapsedDistance, 0.001)
        assertEquals(0f, state.progressRatio, 0.001f)
        assertEquals(TourData.AIRPLANE_FLIGHT_PATH.first(), state.airplanePosition)
        assertEquals(TourData.AIRPLANE_FLIGHT_PATH.first(), state.cameraCenter)
        assertEquals(200.0, state.airplaneAltitude, 0.001)
        assertEquals(200.0, state.cameraAltitude, 0.001)
        assertEquals(65.0, state.cameraTilt, 0.001)
        assertEquals(600.0, state.cameraRange, 0.001)
    }

    @Test
    fun setApproach_updatesStateAndResets() {
        controller.setApproach(AnimationApproach.KEYFRAME_TOUR)
        var state = controller.getState()
        assertEquals(AnimationApproach.KEYFRAME_TOUR, state.selectedApproach)
        assertFalse(state.isPlaying)

        controller.setApproach(AnimationApproach.ORBIT_360_SPIN)
        state = controller.getState()
        assertEquals(AnimationApproach.ORBIT_360_SPIN, state.selectedApproach)
    }

    @Test
    fun playPause_togglesCorrectly() {
        assertFalse(controller.getState().isPlaying)
        controller.togglePlayPause()
        assertTrue(controller.getState().isPlaying)
        controller.setPlaying(false)
        assertFalse(controller.getState().isPlaying)
    }

    @Test
    fun frameDispatcher_advancesAlongPath() {
        controller.setPlaying(true)
        val initialDist = controller.getState().elapsedDistance

        // Advance 1 second at 400 m/s
        controller.advanceFrameDispatcher(1.0, speedMps = 400.0)
        val state = controller.getState()

        assertEquals(initialDist + 400.0, state.elapsedDistance, 0.5)
        assertTrue(state.progressRatio > 0f)
        assertTrue(state.airplanePosition.longitude > TourData.AIRPLANE_FLIGHT_PATH.first().longitude)
    }

    @Test
    fun frameDispatcher_completesAtDestination() {
        controller.setPlaying(true)
        val totalDist = controller.totalFlightDistance

        // Advance 100 seconds to exceed total distance
        controller.advanceFrameDispatcher(100.0, speedMps = 400.0)
        val state = controller.getState()

        assertEquals(totalDist, state.elapsedDistance, 0.001)
        assertEquals(1.0f, state.progressRatio, 0.001f)
        assertTrue(state.isFinished)
        assertFalse(state.isPlaying)
    }

    @Test
    fun continuousOrbit_advancesHeading() {
        controller.setPlaying(true)
        val initialHeading = controller.getState().cameraHeading

        // Advance 2 seconds at 30 deg/sec = +60 deg
        controller.advanceContinuousOrbit(2.0, speedDegPerSec = 30.0)
        val newHeading = controller.getState().cameraHeading

        val expected = (initialHeading + 60.0) % 360.0
        assertEquals(expected, newHeading, 0.01)
    }

    @Test
    fun keyframeFlyAround_containsExpectedProperties() {
        val flyAroundStep = CameraKeyframe.FlyAround(
            stepTitle = "Orbit",
            stepDescription = "Desc",
            centerCamera = TourData.CLOSE_INSPECTION_CAMERA,
            rounds = 1.0,
            durationMs = 6000L
        )

        assertEquals("Orbit", flyAroundStep.stepTitle)
        assertEquals("Desc", flyAroundStep.stepDescription)
        assertEquals(1.0, flyAroundStep.rounds, 0.001)
        assertEquals(6000L, flyAroundStep.durationMs)
    }

    @Test
    fun mathUtilities_workAsExpected() {
        assertEquals(45.0, TourPlaybackController.normalizeHeading(405.0), 0.001)
        assertEquals(315.0, TourPlaybackController.normalizeHeading(-45.0), 0.001)

        val angle = TourPlaybackController.interpolateAngle(350.0, 10.0, 0.5)
        assertEquals(0.0, angle, 0.001)
    }
}
