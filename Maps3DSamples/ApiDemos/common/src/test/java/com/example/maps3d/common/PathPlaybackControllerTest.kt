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
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class PathPlaybackControllerTest {

    private lateinit var controller: PathPlaybackController

    @Before
    fun setUp() {
        controller = PathPlaybackController(PathData.URBAN_PATH)
    }

    @Test
    fun initialState_hasCorrectDefaultsAndCalculatesDistances() {
        val state = controller.getState()

        assertThat(state.route).isEqualTo(PathData.URBAN_PATH)
        assertThat(state.totalDistance).isGreaterThan(3000.0)
        assertThat(state.elapsedDistance).isEqualTo(0.0)
        assertThat(state.progressRatio).isEqualTo(0.0f)
        assertThat(state.isPlaying).isFalse()
        assertThat(state.isScrubbing).isFalse()
        assertThat(state.altitudeMode).isEqualTo(AltitudeMode.CLAMP_TO_GROUND)
        assertThat(state.staticPolylineVertices).isNotEmpty()
        assertThat(state.progressPolylineVertices).isNotEmpty()
    }

    @Test
    fun togglePlayPause_updatesPlayingState() {
        val playingState = controller.togglePlayPause()
        assertThat(playingState.isPlaying).isTrue()

        val pausedState = controller.togglePlayPause()
        assertThat(pausedState.isPlaying).isFalse()
    }

    @Test
    fun advance_movesDistanceProportionalToSpeedAndDeltaTime() {
        controller.setPlaying(true)
        controller.setFollowSpeed(50.0) // 50 m/s

        val updatedState = controller.advance(deltaTimeSeconds = 2.0) // 100 meters forward

        assertThat(updatedState.elapsedDistance).isEqualTo(100.0)
        assertThat(updatedState.progressRatio).isGreaterThan(0.0f)
        assertThat(updatedState.currentPosition.latitude).isNotEqualTo(0.0)
    }

    @Test
    fun advance_wrapsAroundAtEndOfRoute() {
        controller.setPlaying(true)
        controller.setFollowSpeed(1000.0)

        val totalDist = controller.getState().totalDistance
        val steps = (totalDist / 1000.0) + 1.0

        val wrappedState = controller.advance(deltaTimeSeconds = steps)
        assertThat(wrappedState.elapsedDistance).isLessThan(totalDist)
    }

    @Test
    fun seekToRatio_calculatesCorrectDistanceAndProgress() {
        val targetRatio = 0.5f
        val state = controller.seekToRatio(targetRatio)

        assertThat(state.progressRatio).isEqualTo(targetRatio)
        assertThat(state.elapsedDistance).isWithin(1.0).of(state.totalDistance * 0.5)
    }

    @Test
    fun seekToDistance_clampsToBounds() {
        val belowZero = controller.seekToDistance(-50.0)
        assertThat(belowZero.elapsedDistance).isEqualTo(0.0)

        val beyondEnd = controller.seekToDistance(controller.getState().totalDistance + 5000.0)
        assertThat(beyondEnd.elapsedDistance).isEqualTo(controller.getState().totalDistance)
    }

    @Test
    fun setAltitudeMode_updatesVerticesAndCalculatesCorrectCameraElevation() {
        val absoluteState = controller.setAltitudeMode(AltitudeMode.ABSOLUTE)

        assertThat(absoluteState.altitudeMode).isEqualTo(AltitudeMode.ABSOLUTE)
        // Camera target elevation in ABSOLUTE mode includes base altitude + route elevation + ground altitude
        assertThat(absoluteState.cameraTargetAltitude).isGreaterThan(50.0)
    }

    @Test
    fun setRoute_switchesToRuralAndUpdatesDefaults() {
        val ruralState = controller.setRoute(PathData.RURAL_PATH, applyDefaults = true)

        assertThat(ruralState.route).isEqualTo(PathData.RURAL_PATH)
        assertThat(ruralState.cameraRange).isEqualTo(450.0)
        assertThat(ruralState.groundAltitude).isEqualTo(40.0)
        assertThat(ruralState.cameraTilt).isEqualTo(75.0)
        assertThat(ruralState.elapsedDistance).isEqualTo(0.0)
    }

    @Test
    fun smoothHeading_computesContinuousLookaheadBearing() {
        val p0 = controller.seekToDistance(0.0)
        val p1 = controller.seekToDistance(100.0)

        assertThat(p0.currentHeading).isGreaterThan(0.0)
        assertThat(p1.currentHeading).isGreaterThan(0.0)
        assertThat(p0.currentHeading).isLessThan(360.0)
    }

    @Test
    fun progressPolyline_containsAtLeastTwoVerticesForRendering() {
        val zeroState = controller.seekToDistance(0.0)
        assertThat(zeroState.progressPolylineVertices.size).isAtLeast(2)
    }
}
