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
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * JVM Unit Tests for [AdvancedCameraAnimationViewModel] using Google Truth.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AdvancedCameraAnimationViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: AdvancedCameraAnimationViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AdvancedCameraAnimationViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialViewModelState_isCorrect() {
        val state = viewModel.currentState
        assertThat(state.selectedApproach).isEqualTo(AnimationApproach.SIMPLE_FLY_TO)
        assertThat(state.isPlaying).isFalse()
        assertThat(state.entities).containsKey(TourData.AIRPLANE_MODEL_ID)
    }

    @Test
    fun actions_updateStateFlowAndLiveData() {
        viewModel.setApproach(AnimationApproach.SIMPLE_FLY_TO)
        assertThat(viewModel.currentState.selectedApproach).isEqualTo(AnimationApproach.SIMPLE_FLY_TO)

        viewModel.setSimpleFlyToMode(SimpleFlyToMode.MIDPOINT_JUMP)
        assertThat(viewModel.currentState.simpleFlyToMode).isEqualTo(SimpleFlyToMode.MIDPOINT_JUMP)

        viewModel.play()
        assertThat(viewModel.currentState.isPlaying).isTrue()

        viewModel.pause()
        assertThat(viewModel.currentState.isPlaying).isFalse()

        viewModel.togglePlayPause()
        assertThat(viewModel.currentState.isPlaying).isTrue()

        viewModel.resetTour()
        assertThat(viewModel.currentState.isPlaying).isFalse()
        assertThat(viewModel.currentState.progressRatio).isEqualTo(0f)
    }

    @Test
    fun tick_advancesSimulationState() {
        viewModel.setApproach(AnimationApproach.SIMPLE_FLY_TO)
        viewModel.setSimpleFlyToMode(SimpleFlyToMode.SYNCHRONIZED_FLIGHT)
        viewModel.play()

        viewModel.tick(1.0)
        assertThat(viewModel.currentState.elapsedTimeMs).isEqualTo(1000L)
        assertThat(viewModel.currentState.progressRatio).isGreaterThan(0f)
    }
}
