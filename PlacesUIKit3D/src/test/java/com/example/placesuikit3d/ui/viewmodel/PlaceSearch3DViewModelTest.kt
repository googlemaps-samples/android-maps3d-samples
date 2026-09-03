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

package com.example.placesuikit3d.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.placesuikit3d.data.model.Camera3DTarget
import com.example.placesuikit3d.data.repository.PlacesRepositoryImpl
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for [PlaceSearch3DViewModel].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PlaceSearch3DViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: PlacesRepositoryImpl
    private lateinit var viewModel: PlaceSearch3DViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = PlacesRepositoryImpl(ioDispatcher = testDispatcher)
        viewModel = PlaceSearch3DViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_loadsDefaultLandmarks() = runTest {
        advanceUntilIdle()
        val state = viewModel.screenState.value
        assertThat(state.selectedCategory).isEqualTo("🏛️ Landmarks")
        assertThat(state.allMarkers).isNotEmpty()
    }

    @Test
    fun selectCategory_updatesCategoryAndMarkers() = runTest {
        viewModel.selectCategory("☕ Cafes")
        advanceUntilIdle()

        val state = viewModel.screenState.value
        assertThat(state.selectedCategory).isEqualTo("☕ Cafes")
        assertThat(state.allMarkers).isNotEmpty()
        assertThat(state.allMarkers.any { it.name.contains("Coffee") || it.category == "☕ Cafes" }).isTrue()
        assertThat(state.cameraMode).isInstanceOf(Camera3DMode.FlyingTo::class.java)
    }

    @Test
    fun searchByQuery_updatesResultsAndCamera() = runTest {
        viewModel.searchByQuery("Flatirons")
        advanceUntilIdle()

        val state = viewModel.screenState.value
        assertThat(state.searchQuery).isEqualTo("Flatirons")
        assertThat(state.allMarkers).isNotEmpty()
        assertThat(state.selectedPlace).isNotNull()
        assertThat(state.cameraMode).isInstanceOf(Camera3DMode.FlyingTo::class.java)
    }

    @Test
    fun onPlaceSelected_loadsDetailsAndFliesCamera() = runTest {
        val testPlaceId = "ChIJfXOTtWbsa4cRmW07qJRB6_8"
        viewModel.onPlaceSelected(testPlaceId)
        advanceUntilIdle()

        val state = viewModel.screenState.value
        assertThat(state.selectedPlaceId).isEqualTo(testPlaceId)
        assertThat(state.searchUiState).isInstanceOf(PlaceSearchUiState.PlaceDetailsLoaded::class.java)
        assertThat(state.cameraMode).isInstanceOf(Camera3DMode.FlyingTo::class.java)
    }

    @Test
    fun toggleOrbit_updatesOrbitingState() = runTest {
        val testPlaceId = "ChIJfXOTtWbsa4cRmW07qJRB6_8"
        viewModel.onPlaceSelected(testPlaceId)
        advanceUntilIdle()

        // Turn Orbit On
        viewModel.toggleOrbit()
        val stateOn = viewModel.screenState.value
        assertThat(stateOn.isOrbiting).isTrue()
        assertThat(stateOn.cameraMode).isInstanceOf(Camera3DMode.Orbiting::class.java)

        // Turn Orbit Off
        viewModel.toggleOrbit()
        val stateOff = viewModel.screenState.value
        assertThat(stateOff.isOrbiting).isFalse()
        assertThat(stateOff.cameraMode).isInstanceOf(Camera3DMode.Standard3D::class.java)
    }

    @Test
    fun toggle2D3DView_switchesPerspective() = runTest {
        assertThat(viewModel.screenState.value.is3DView).isTrue()

        // Switch to 2D
        viewModel.toggle2D3DView()
        val state2D = viewModel.screenState.value
        assertThat(state2D.is3DView).isFalse()
        val cameraMode2D = state2D.cameraMode as Camera3DMode.FlyingTo
        assertThat(cameraMode2D.target.tilt).isEqualTo(0.0)

        // Switch back to 3D
        viewModel.toggle2D3DView()
        val state3D = viewModel.screenState.value
        assertThat(state3D.is3DView).isTrue()
        val cameraMode3D = state3D.cameraMode as Camera3DMode.FlyingTo
        assertThat(cameraMode3D.target.tilt).isEqualTo(50.0)
    }

    @Test
    fun onSuggestionSelected_updatesSelectedPlaceAndCamera() = runTest {
        val testPlaceId = "ChIJfXOTtWbsa4cRmW07qJRB6_8"
        val place = com.google.android.libraries.places.compose.autocomplete.models.AutocompletePlace(
            placeId = testPlaceId,
            primaryText = PlacesRepositoryImpl.toSpannable("The Flatirons"),
            secondaryText = PlacesRepositoryImpl.toSpannable("Boulder, CO, USA"),
        )

        viewModel.onSuggestionSelected(place)
        advanceUntilIdle()

        val state = viewModel.screenState.value
        assertThat(state.searchQuery).isEqualTo("The Flatirons")
        assertThat(state.selectedPlace?.id).isEqualTo(testPlaceId)
        assertThat(state.allMarkers).hasSize(1)
        assertThat(state.cameraMode).isInstanceOf(Camera3DMode.FlyingTo::class.java)
        val flyingTarget = (state.cameraMode as Camera3DMode.FlyingTo).target
        assertThat(flyingTarget.range).isEqualTo(1000.0)
        assertThat(flyingTarget.tilt).isEqualTo(45.0)
    }

    @Test
    fun onQueryChanged_updatesQueryAndSuggestions() = runTest {
        viewModel.onQueryChanged("Chautauqua")
        advanceUntilIdle()

        val state = viewModel.screenState.value
        assertThat(state.searchQuery).isEqualTo("Chautauqua")
        assertThat(state.autocompleteSuggestions).isNotEmpty()
        assertThat(state.autocompleteSuggestions.first().primaryText.toString()).contains("Chautauqua")
    }

    @Test
    fun resetCameraOverview_resetsToDefaultOverview() = runTest {
        viewModel.resetCameraOverview()
        advanceUntilIdle()

        val state = viewModel.screenState.value
        assertThat(state.cameraMode).isInstanceOf(Camera3DMode.FlyingTo::class.java)
        val target = (state.cameraMode as Camera3DMode.FlyingTo).target
        assertThat(target).isEqualTo(Camera3DTarget.DEFAULT)
        assertThat(state.isOrbiting).isFalse()
    }

    @Test
    fun dismissPlaceDetails_clearsSelection() = runTest {
        viewModel.onPlaceSelected("ChIJfXOTtWbsa4cRmW07qJRB6_8")
        advanceUntilIdle()
        assertThat(viewModel.screenState.value.selectedPlaceId).isNotNull()

        viewModel.dismissPlaceDetails()
        assertThat(viewModel.screenState.value.selectedPlaceId).isNull()
        assertThat(viewModel.screenState.value.selectedPlace).isNull()
    }
}
