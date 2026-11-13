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
 * JVM Unit Tests for [HillyRoadAlertViewModel].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HillyRoadAlertViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: HillyRoadAlertViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = HillyRoadAlertViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun beforeMapReady_doesNotCalculateAlertsOrAdvanceRoute() {
        val preReadyState = viewModel.currentState
        assertFalse(preReadyState.isMapReady)
        assertFalse(preReadyState.isPlaying)
        assertEquals(0.0, preReadyState.currentDistanceMeters, 0.001)
        assertEquals(TerrainAlertType.NONE, preReadyState.terrainAlert.alertType)

        // Frame ticks before map is ready must not advance distance
        viewModel.advance(0.5)
        assertEquals(0.0, viewModel.currentState.currentDistanceMeters, 0.001)
    }

    @Test
    fun initialState_afterMapReady_usesRollerCoasterPresetAnd500mLookahead() {
        viewModel.onMapReady()
        val state = viewModel.currentState
        assertTrue(state.isMapReady)
        assertEquals(HillyRoutePreset.ROLLER_COASTER, state.selectedPreset)
        assertEquals(500.0, state.lookaheadHorizonMeters, 0.001)
        assertTrue(state.isPlaying)
        assertEquals(0.0, state.currentDistanceMeters, 0.001)
        // Starts ~179m before the first roller-coaster dip/crest section -> early CAUTION alert
        assertEquals(TerrainAlertType.UNDULATING_ROLLER_COASTER, state.terrainAlert.alertType)
        assertEquals(AlertSeverity.CAUTION, state.terrainAlert.severity)
    }

    @Test
    fun selectRoute_switchesPresetsAndUpdatesTerrainAlert() {
        viewModel.onMapReady()
        viewModel.seekToDistance(120.0)

        viewModel.selectRoute(HillyRoutePreset.COASTAL_DRIVE)
        val coastalState = viewModel.currentState
        assertEquals(HillyRoutePreset.COASTAL_DRIVE, coastalState.selectedPreset)
        assertEquals(0.0, coastalState.currentDistanceMeters, 0.001)
        assertEquals(HillyRouteData.COASTAL_DRIVE_PATH.size, coastalState.routePoints.size)
        assertTrue(coastalState.terrainAlert.lookaheadProfile.isNotEmpty())

        viewModel.selectRoute(HillyRoutePreset.MOUNTAIN_PASS)
        val mountainState = viewModel.currentState
        assertEquals(HillyRoutePreset.MOUNTAIN_PASS, mountainState.selectedPreset)
        assertEquals(0.0, mountainState.currentDistanceMeters, 0.001)
        assertEquals(HillyRouteData.MOUNTAIN_PASS_PATH.size, mountainState.routePoints.size)
        assertTrue(mountainState.terrainAlert.lookaheadProfile.isNotEmpty())
    }

    @Test
    fun setLookaheadHorizon_shiftsEarlyAlertTriggerDistance() {
        viewModel.onMapReady()
        // Past the undulating zone (at 760m, after the 737m cluster end) with 250m horizon -> NONE
        viewModel.seekToDistance(760.0)
        viewModel.setLookaheadHorizon(250.0)
        assertEquals(250.0, viewModel.currentState.lookaheadHorizonMeters, 0.001)
        assertEquals(TerrainAlertType.NONE, viewModel.currentState.terrainAlert.alertType)

        // At 0m, 500m lookahead horizon ([0m, 500m]) reaches the ~179.4m roller-coaster section -> CAUTION
        viewModel.seekToDistance(0.0)
        viewModel.setLookaheadHorizon(500.0)
        assertEquals(500.0, viewModel.currentState.lookaheadHorizonMeters, 0.001)
        assertEquals(
            TerrainAlertType.UNDULATING_ROLLER_COASTER,
            viewModel.currentState.terrainAlert.alertType
        )
        assertEquals(AlertSeverity.CAUTION, viewModel.currentState.terrainAlert.severity)
        val countAt500 = viewModel.currentState.terrainAlert.upcomingExtrema.size

        // 750m lookahead horizon previews additional upcoming extrema (dip_82 at ~725.8m)
        viewModel.setLookaheadHorizon(750.0)
        assertEquals(750.0, viewModel.currentState.lookaheadHorizonMeters, 0.001)
        val countAt750 = viewModel.currentState.terrainAlert.upcomingExtrema.size
        assertTrue(countAt750 > countAt500)
    }

    @Test
    fun jumpToHillyZone_seeksBeforeFirstCrestAndTriggersEarlyAlert() {
        viewModel.onMapReady()
        viewModel.setLookaheadHorizon(250.0)
        viewModel.seekToDistance(760.0)
        assertEquals(TerrainAlertType.NONE, viewModel.currentState.terrainAlert.alertType)

        // Jump to the approach before the first roller-coaster section (~179m ahead at 0m)
        viewModel.jumpToHillyZone()
        val jumpedState = viewModel.currentState
        assertTrue(jumpedState.currentDistanceMeters in 0.0..50.0)
        assertEquals(
            TerrainAlertType.UNDULATING_ROLLER_COASTER,
            jumpedState.terrainAlert.alertType
        )
        assertEquals(AlertSeverity.CAUTION, jumpedState.terrainAlert.severity)
    }

    @Test
    fun playbackScrubbing_speedMultiplier_and_autoCameraPitchPreview() {
        viewModel.onMapReady()
        // Scrubbing to 50% of route (actively inside the roller-coaster undulations)
        viewModel.setScrubbing(true)
        assertTrue(viewModel.currentState.isUserScrubbing)
        viewModel.seekToProgress(0.5f)
        assertEquals(0.5f, viewModel.currentState.progressRatio, 0.01f)
        assertEquals(AlertSeverity.WARNING, viewModel.currentState.terrainAlert.severity)
        viewModel.setScrubbing(false)
        assertFalse(viewModel.currentState.isUserScrubbing)

        // Auto camera pitch preview widens range (230m) and lowers tilt (66 deg) during roller-coaster alert
        assertEquals(
            HillyRoadAlertViewModel.PREVIEW_CAMERA_RANGE,
            viewModel.currentState.cameraRange,
            0.01
        )
        assertEquals(
            HillyRoadAlertViewModel.PREVIEW_CAMERA_TILT,
            viewModel.currentState.cameraTilt,
            0.01
        )

        // Toggling off auto camera pitch preview restores normal chase range (160m) and tilt (74 deg)
        viewModel.toggleAutoCameraPitchPreview()
        assertFalse(viewModel.currentState.autoCameraPitchPreview)
        assertEquals(
            HillyRoadAlertViewModel.NORMAL_CAMERA_RANGE,
            viewModel.currentState.cameraRange,
            0.01
        )
        assertEquals(
            HillyRoadAlertViewModel.NORMAL_CAMERA_TILT,
            viewModel.currentState.cameraTilt,
            0.01
        )

        // Speed multiplier and advance
        val distBefore = viewModel.currentState.currentDistanceMeters
        viewModel.setSpeedMultiplier(2.0)
        assertEquals(2.0, viewModel.currentState.speedMultiplier, 0.001)
        viewModel.advance(0.05)
        assertTrue(viewModel.currentState.currentDistanceMeters > distBefore)

        // Pause prevents advance
        viewModel.togglePlayPause()
        assertFalse(viewModel.currentState.isPlaying)
        val pausedDist = viewModel.currentState.currentDistanceMeters
        viewModel.advance(0.05)
        assertEquals(pausedDist, viewModel.currentState.currentDistanceMeters, 0.001)
    }
}
