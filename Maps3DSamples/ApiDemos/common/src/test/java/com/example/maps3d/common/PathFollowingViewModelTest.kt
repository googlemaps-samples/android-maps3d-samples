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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.android.gms.maps3d.model.AltitudeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * JVM Unit Tests for [PathFollowingViewModel].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PathFollowingViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: PathFollowingViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = PathFollowingViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_matchesDefaultRoute() {
        val state = viewModel.currentState
        assertEquals(PathData.URBAN_PATH, state.route)
        assertFalse(state.isPlaying)
        assertEquals(0.0, state.elapsedDistance, 0.001)
    }

    @Test
    fun playPauseToggle_updatesFlowAndState() {
        assertFalse(viewModel.currentState.isPlaying)
        viewModel.togglePlayPause()
        assertTrue(viewModel.currentState.isPlaying)
        viewModel.setPlaying(false)
        assertFalse(viewModel.currentState.isPlaying)
    }

    @Test
    fun seekAndSkip_updatesProgress() {
        viewModel.seekToRatio(0.5f)
        assertEquals(0.5f, viewModel.currentState.progressRatio, 0.01f)

        viewModel.skipRatio(0.10f)
        assertEquals(0.60f, viewModel.currentState.progressRatio, 0.02f)

        viewModel.skipDistance(50.0)
        assertTrue(viewModel.currentState.elapsedDistance > 0.0)

        viewModel.seekToDistance(100.0)
        assertTrue(viewModel.currentState.elapsedDistance >= 99.0)
    }

    @Test
    fun setters_and_gestures_updateState() {
        viewModel.setAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND)
        assertEquals(AltitudeMode.RELATIVE_TO_GROUND, viewModel.currentState.altitudeMode)

        viewModel.setDrawsOccludedSegments(true)
        assertTrue(viewModel.currentState.drawsOccludedSegments)

        viewModel.setFollowSpeed(60.0)
        assertEquals(60.0, viewModel.currentState.followSpeedMps, 0.001)

        viewModel.setCameraRange(400.0)
        assertEquals(400.0, viewModel.currentState.cameraRange, 0.001)

        viewModel.setCameraTilt(65.0)
        assertEquals(65.0, viewModel.currentState.cameraTilt, 0.001)

        viewModel.setHeadingOffset(30.0)
        assertEquals(30.0, viewModel.currentState.headingOffset, 0.001)

        viewModel.setGroundAltitude(150.0)
        assertEquals(150.0, viewModel.currentState.groundAltitude, 0.001)

        viewModel.setPathAltitudeOffset(15.0)
        assertEquals(15.0, viewModel.currentState.pathAltitudeOffset, 0.001)

        viewModel.setScrubbing(true)
        assertTrue(viewModel.currentState.isScrubbing)

        // Gesture adjustments
        viewModel.adjustTilt(10.0)
        assertEquals(75.0, viewModel.currentState.cameraTilt, 0.001)

        viewModel.adjustHeading(15.0)
        assertEquals(45.0, viewModel.currentState.headingOffset, 0.001)

        viewModel.adjustRange(2.0)
        assertEquals(200.0, viewModel.currentState.cameraRange, 0.001)

        // Speed multipliers
        viewModel.setSpeedBoostMultiplier(5.0)
        assertEquals(5.0, viewModel.currentState.speedBoostMultiplier, 0.001)
        assertTrue(viewModel.currentState.isSpeedBoosted)

        viewModel.setSpeedBoosted(false)
        assertEquals(1.0, viewModel.currentState.speedBoostMultiplier, 0.001)

        // Switch route
        viewModel.setRoute(PathData.RURAL_PATH, applyDefaults = true)
        assertEquals(PathData.RURAL_PATH, viewModel.currentState.route)

        viewModel.reset()
        assertEquals(0.0, viewModel.currentState.elapsedDistance, 0.001)
    }

    @Test
    fun advance_progressesPlayback() {
        viewModel.setPlaying(true)
        viewModel.advance(1.0)
        assertTrue(viewModel.currentState.elapsedDistance > 0.0)
    }
}
