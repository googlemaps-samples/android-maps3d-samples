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
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * JVM Unit Tests for [EntityAnimator] implementations using Google Truth.
 */
class EntityAnimatorTest {

    private val startPose = EntityPose(
        position = LatLngAltitude(37.8199, -122.4783, 200.0),
        heading = 285.0
    )

    private val endPose = EntityPose(
        position = LatLngAltitude(37.8024, -122.4058, 200.0),
        heading = 285.0
    )

    @Test
    fun midpointJumpAnimator_holdsStartPoseBeforeMidpoint_andJumpsAtMidpoint() {
        val totalDurationMs = 5000L
        val animator = MidpointJumpAnimator(startPose, endPose)

        // At t = 0ms -> Start Pose
        val poseAt0 = animator.update(0L, totalDurationMs)
        assertThat(poseAt0.position.latitude).isEqualTo(startPose.position.latitude)
        assertThat(poseAt0.position.longitude).isEqualTo(startPose.position.longitude)
        assertThat(animator.isFinished(0L, totalDurationMs)).isFalse()

        // At t = 2499ms (just before 2500ms midpoint) -> Still Start Pose
        val poseBeforeMid = animator.update(2499L, totalDurationMs)
        assertThat(poseBeforeMid.position.latitude).isEqualTo(startPose.position.latitude)
        assertThat(poseBeforeMid.position.longitude).isEqualTo(startPose.position.longitude)

        // At t = 2500ms (exact midpoint) -> Jumps to End Pose
        val poseAtMid = animator.update(2500L, totalDurationMs)
        assertThat(poseAtMid.position.latitude).isEqualTo(endPose.position.latitude)
        assertThat(poseAtMid.position.longitude).isEqualTo(endPose.position.longitude)

        // At t = 5000ms -> End Pose and Finished
        val poseAtEnd = animator.update(5000L, totalDurationMs)
        assertThat(poseAtEnd.position.latitude).isEqualTo(endPose.position.latitude)
        assertThat(animator.isFinished(5000L, totalDurationMs)).isTrue()
    }

    @Test
    fun trajectoryFlightAnimator_interpolatesContinuouslyAlongPath() {
        val waypoints = listOf(
            LatLng(37.8199, -122.4783),
            LatLng(37.8115, -122.4475),
            LatLng(37.8024, -122.4058)
        )
        val totalDurationMs = 4000L
        val animator = TrajectoryFlightAnimator(waypoints, altitude = 200.0)

        assertThat(animator.totalDistance).isGreaterThan(5000.0)

        // At t = 0ms -> Start of path
        val pose0 = animator.update(0L, totalDurationMs)
        assertThat(pose0.position.latitude).isWithin(0.0001).of(waypoints.first().latitude)
        assertThat(pose0.position.longitude).isWithin(0.0001).of(waypoints.first().longitude)

        // At t = 2000ms (50%) -> Mid-flight point
        val poseMid = animator.update(2000L, totalDurationMs)
        assertThat(poseMid.position.longitude).isGreaterThan(waypoints.first().longitude)
        assertThat(poseMid.position.longitude).isLessThan(waypoints.last().longitude)

        // At t = 4000ms (100%) -> End of path
        val poseEnd = animator.update(4000L, totalDurationMs)
        assertThat(poseEnd.position.latitude).isWithin(0.0001).of(waypoints.last().latitude)
        assertThat(poseEnd.position.longitude).isWithin(0.0001).of(waypoints.last().longitude)
        assertThat(animator.isFinished(4000L, totalDurationMs)).isTrue()
    }

    @Test
    fun continuousOrbitAnimator_advancesHeadingLinearlyAndWraps() {
        val animator = ContinuousOrbitAnimator(
            center = LatLng(37.8199, -122.4783),
            speedDegPerSec = 30.0
        )
        animator.reset(initialHeading = 350.0)

        // Advance 1 second at 30 deg/sec: (350 + 30) % 360 = 20 deg
        val heading1 = animator.tick(1.0)
        assertThat(heading1).isWithin(0.01).of(20.0)

        // Advance another 2 seconds: (20 + 60) = 80 deg
        val heading2 = animator.tick(2.0)
        assertThat(heading2).isWithin(0.01).of(80.0)

        // Reset
        animator.reset(105.0)
        assertThat(animator.getHeading()).isEqualTo(105.0)
    }
}
