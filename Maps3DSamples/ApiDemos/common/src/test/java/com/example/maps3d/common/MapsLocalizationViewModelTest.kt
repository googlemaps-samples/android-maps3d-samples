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
import com.google.android.gms.maps3d.model.Map3DMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Tier 3 JVM Unit Tests for [MapsLocalizationViewModel].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MapsLocalizationViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: MapsLocalizationViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = MapsLocalizationViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun selectPreset_updatesStateFlowAndLiveData() = runTest {
        val observedStates = mutableListOf<LocalizationState>()
        viewModel.localizationStateData.observeForever { observedStates.add(it) }

        viewModel.selectPreset(MapLocalePreset.CHINESE_CHINA)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(MapLocalePreset.CHINESE_CHINA, state.selectedPreset)
        assertEquals("zh", state.languageCode)
        assertEquals("CN", state.regionCode)
        assertEquals(MapLocalePreset.CHINESE_CHINA, observedStates.last().selectedPreset)
    }

    @Test
    fun setMapMode_dispatchesCleanly() = runTest {
        viewModel.selectPreset(MapLocalePreset.GERMAN_GERMANY)

        viewModel.setMapMode(Map3DMode.ROADMAP)
        assertEquals(Map3DMode.ROADMAP, viewModel.currentState.mapMode)
    }
}
