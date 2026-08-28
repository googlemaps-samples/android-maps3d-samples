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
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

/**
 * JVM Unit Tests for [WorldController] using Google Truth.
 */
class WorldControllerTest {

    private lateinit var controller: WorldController

    @Before
    fun setup() {
        controller = WorldController()
    }

    @Test
    fun initialState_isConfiguredCorrectly() {
        val state = controller.getState()
        assertThat(state.executionState).isEqualTo(AnimationExecutionState.IDLE)
        assertThat(state.isPlaying).isFalse()
        assertThat(state.isFinished).isFalse()
        assertThat(state.entities).containsKey(TourData.AIRPLANE_MODEL_ID)

        val planePose = state.getEntityPose(TourData.AIRPLANE_MODEL_ID)
        assertThat(planePose).isNotNull()
        assertThat(planePose!!.position.latitude).isWithin(0.001).of(TourData.AIRPLANE_FLIGHT_PATH.first().latitude)
    }

    @Test
    fun simpleFlyTo_withMidpointJump_teleportsPlaneAtHalfDuration() {
        controller.setApproach(AnimationApproach.SIMPLE_FLY_TO)
        controller.setSimpleFlyToMode(SimpleFlyToMode.MIDPOINT_JUMP)
        val playState = controller.play()

        assertThat(playState.executionState).isEqualTo(AnimationExecutionState.RUNNING)
        assertThat(playState.pendingCameraCommand).isInstanceOf(CameraAnimationCommand.NativeFlyTo::class.java)

        // Advance 1 second (1000ms < 2500ms midpoint) -> Plane stays at start
        val state1s = controller.tick(1.0)
        val pose1s = state1s.getEntityPose(TourData.AIRPLANE_MODEL_ID)!!
        assertThat(pose1s.position.latitude).isWithin(0.001).of(TourData.AIRPLANE_FLIGHT_PATH.first().latitude)

        // Advance another 2 seconds (total 3000ms > 2500ms midpoint) -> Plane teleports to destination
        val state3s = controller.tick(2.0)
        val pose3s = state3s.getEntityPose(TourData.AIRPLANE_MODEL_ID)!!
        assertThat(pose3s.position.latitude).isWithin(0.001).of(TourData.AIRPLANE_FLIGHT_PATH.last().latitude)
    }

    @Test
    fun simpleFlyTo_withSynchronizedFlight_animatesPlaneContinuously() {
        controller.setApproach(AnimationApproach.SIMPLE_FLY_TO)
        controller.setSimpleFlyToMode(SimpleFlyToMode.SYNCHRONIZED_FLIGHT)
        controller.play()

        // Advance 2.5 seconds (50% of 5.0s)
        val state2_5s = controller.tick(2.5)
        val pose2_5s = state2_5s.getEntityPose(TourData.AIRPLANE_MODEL_ID)!!

        // Plane should be in intermediate position between source and destination
        assertThat(pose2_5s.position.longitude).isGreaterThan(TourData.AIRPLANE_FLIGHT_PATH.first().longitude)
        assertThat(pose2_5s.position.longitude).isLessThan(TourData.AIRPLANE_FLIGHT_PATH.last().longitude)
        assertThat(state2_5s.progressRatio).isWithin(0.05f).of(0.5f)

        // Advance to 5.0s total -> Completes
        val state5s = controller.tick(2.5)
        assertThat(state5s.isFinished).isTrue()
        assertThat(state5s.progressRatio).isEqualTo(1.0f)
    }

    @Test
    fun dispatcherFrameLoop_updatesBothCameraAndPlaneEntities() {
        controller.setApproach(AnimationApproach.DISPATCHER_FRAME_LOOP)
        controller.play()

        val state1s = controller.tick(1.0)
        val planePose = state1s.getEntityPose(TourData.AIRPLANE_MODEL_ID)!!
        val cameraCenter = state1s.camera.center

        // In dispatcher loop, camera is locked to plane position
        assertThat(cameraCenter.latitude).isWithin(0.0001).of(planePose.position.latitude)
        assertThat(cameraCenter.longitude).isWithin(0.0001).of(planePose.position.longitude)
    }

    @Test
    fun continuousOrbit_advancesCameraHeading() {
        controller.setApproach(AnimationApproach.ORBIT_360_SPIN)
        controller.play()

        val initialHeading = controller.getState().camera.heading ?: 105.0
        val state2s = controller.tick(2.0)
        val newHeading = state2s.camera.heading ?: 0.0

        // 2s * 25 deg/s = +50 deg
        val expectedHeading = (initialHeading + 50.0) % 360.0
        assertThat(newHeading).isWithin(0.1).of(expectedHeading)
    }

    @Test
    fun pauseAndReset_resetsStateCleanly() {
        controller.setApproach(AnimationApproach.DISPATCHER_FRAME_LOOP)
        controller.play()
        controller.tick(2.0)

        controller.pause()
        assertThat(controller.getState().executionState).isEqualTo(AnimationExecutionState.PAUSED)
        assertThat(controller.getState().pendingCameraCommand).isEqualTo(CameraAnimationCommand.StopCameraAnimation)

        controller.reset()
        val resetState = controller.getState()
        assertThat(resetState.executionState).isEqualTo(AnimationExecutionState.IDLE)
        assertThat(resetState.elapsedTimeMs).isEqualTo(0L)
        assertThat(resetState.progressRatio).isEqualTo(0f)
    }
}
