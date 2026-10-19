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
import com.example.maps3d.common.storelocator.StoreAmenity
import com.example.maps3d.common.storelocator.StoreLocation
import com.example.maps3d.common.storelocator.StoreLocatorUiState
import com.example.maps3d.common.storelocator.StoreLocatorViewModel
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Tier 3 JVM Unit Tests for [StoreLocatorViewModel] and [StoreLocation].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StoreLocatorViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: StoreLocatorViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = StoreLocatorViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_hasEightStoresOrderedByDistanceAnd2DOverviewCamera() {
        val state = viewModel.uiState.value

        assertEquals(8, state.allStores.size)
        assertEquals(8, state.filteredStores.size)
        assertTrue(state.selectedAmenities.isEmpty())
        assertNull(state.selectedStore)
        assertTrue(state.isOverviewMode)
        assertFalse(state.is3DMode)
        assertEquals(Map3DMode.ROADMAP, state.mapMode)
        assertEquals(StoreLocation.OVERVIEW_CAMERA_TILT, state.targetCamera.tilt ?: 0.0, 0.001)
        assertEquals(StoreLocation.OVERVIEW_CAMERA_RANGE, state.targetCamera.range ?: 0.0, 0.001)

        // Verify stores are sorted ascending by distanceKm from reference origin
        val distances = state.filteredStores.map { it.distanceKm }
        assertEquals(distances.sorted(), distances)
    }

    @Test
    fun toggleAmenity_filtersStoresBySingleAndMultipleAmenities() {
        // Filter by DRIVE_THRU
        viewModel.toggleAmenity(StoreAmenity.DRIVE_THRU)
        val driveThruState = viewModel.uiState.value
        assertEquals(setOf(StoreAmenity.DRIVE_THRU), driveThruState.selectedAmenities)
        assertTrue(driveThruState.filteredStores.isNotEmpty())
        assertTrue(
            driveThruState.filteredStores.all { StoreAmenity.DRIVE_THRU in it.amenities },
        )

        // Add EV_CHARGING + OPEN_NOW filter (intersection)
        viewModel.toggleAmenity(StoreAmenity.EV_CHARGING)
        viewModel.toggleAmenity(StoreAmenity.OPEN_NOW)
        val multiState = viewModel.uiState.value
        assertEquals(
            setOf(StoreAmenity.DRIVE_THRU, StoreAmenity.EV_CHARGING, StoreAmenity.OPEN_NOW),
            multiState.selectedAmenities,
        )
        assertEquals(1, multiState.filteredStores.size)
        assertEquals("store_soma_ev_plaza", multiState.filteredStores.first().id)

        // Clear filters restores all 8 stores
        viewModel.clearFilters()
        assertEquals(8, viewModel.uiState.value.filteredStores.size)
        assertTrue(viewModel.uiState.value.selectedAmenities.isEmpty())
    }

    @Test
    fun selectStore_preservesSelected2DOr3DMode_andConsumesFlyTo() = runTest {
        val observedStates = mutableListOf<StoreLocatorUiState>()
        viewModel.liveData.observeForever { observedStates.add(it) }

        // 1. Selecting a store while in 2D mode keeps 2D mode (ROADMAP + 2D detail tilt)
        val targetStore = viewModel.uiState.value.allStores.first()
        viewModel.selectStore(targetStore.id)
        advanceUntilIdle()

        val selectedState2D = viewModel.uiState.value
        assertEquals(targetStore.id, selectedState2D.selectedStore?.id)
        assertFalse(selectedState2D.isOverviewMode)
        assertFalse(selectedState2D.is3DMode)
        assertEquals(Map3DMode.ROADMAP, selectedState2D.mapMode)
        assertEquals(StoreLocation.DETAIL_2D_CAMERA_TILT, selectedState2D.targetCamera.tilt ?: 0.0, 0.001)
        assertEquals(StoreLocation.DETAIL_2D_CAMERA_RANGE, selectedState2D.targetCamera.range ?: 0.0, 0.001)

        val flyToCommand2D = selectedState2D.flyToCommand
        assertNotNull(flyToCommand2D)
        assertEquals(StoreLocation.DETAIL_2D_CAMERA_TILT, flyToCommand2D?.endCamera?.tilt ?: 0.0, 0.001)
        assertEquals(StoreLocation.DETAIL_2D_CAMERA_RANGE, flyToCommand2D?.endCamera?.range ?: 0.0, 0.001)

        // Verify LiveData parity for Java Views
        assertEquals(targetStore.id, observedStates.last().selectedStore?.id)

        // Consume one-shot flyTo command
        viewModel.onFlyToCompleted()
        assertNull(viewModel.uiState.value.flyToCommand)

        // 2. Switch to 3D mode and select a second store -> keeps 3D mode (HYBRID + 3D detail tilt)
        viewModel.toggle2D3DMode()
        assertTrue(viewModel.uiState.value.is3DMode)
        assertEquals(Map3DMode.HYBRID, viewModel.uiState.value.mapMode)

        val secondStore = viewModel.uiState.value.allStores[1]
        viewModel.selectStore(secondStore.id)
        val selectedState3D = viewModel.uiState.value
        assertEquals(secondStore.id, selectedState3D.selectedStore?.id)
        assertTrue(selectedState3D.is3DMode)
        assertEquals(Map3DMode.HYBRID, selectedState3D.mapMode)
        assertEquals(StoreLocation.DETAIL_CAMERA_TILT, selectedState3D.targetCamera.tilt ?: 0.0, 0.001)
        assertEquals(StoreLocation.DETAIL_CAMERA_RANGE, selectedState3D.targetCamera.range ?: 0.0, 0.001)
    }

    @Test
    fun toggle2D3DMode_switchesBetweenRoadmap2DAndHybrid3D() {
        assertFalse(viewModel.uiState.value.is3DMode)
        assertEquals(Map3DMode.ROADMAP, viewModel.uiState.value.mapMode)

        // Toggle from 2D overview to 3D overview
        viewModel.toggle2D3DMode()
        val state3DOverview = viewModel.uiState.value
        assertTrue(state3DOverview.is3DMode)
        assertEquals(Map3DMode.HYBRID, state3DOverview.mapMode)
        assertEquals(StoreLocation.OVERVIEW_3D_CAMERA_TILT, state3DOverview.targetCamera.tilt ?: 0.0, 0.001)

        // Toggle back to 2D overview
        viewModel.toggle2D3DMode()
        val state2DOverview = viewModel.uiState.value
        assertFalse(state2DOverview.is3DMode)
        assertEquals(Map3DMode.ROADMAP, state2DOverview.mapMode)
        assertEquals(StoreLocation.OVERVIEW_CAMERA_TILT, state2DOverview.targetCamera.tilt ?: 0.0, 0.001)

        // Select a store in 2D mode -> stays in 2D mode
        val store = viewModel.uiState.value.allStores.first()
        viewModel.selectStore(store)
        assertFalse(viewModel.uiState.value.is3DMode)
        assertEquals(Map3DMode.ROADMAP, viewModel.uiState.value.mapMode)
        assertEquals(StoreLocation.DETAIL_2D_CAMERA_TILT, viewModel.uiState.value.targetCamera.tilt ?: 0.0, 0.001)

        // Toggle to 3D mode while store is selected -> enters 3D detail view
        viewModel.toggle2D3DMode()
        val state3DStore = viewModel.uiState.value
        assertTrue(state3DStore.is3DMode)
        assertEquals(Map3DMode.HYBRID, state3DStore.mapMode)
        assertEquals(StoreLocation.DETAIL_CAMERA_TILT, state3DStore.targetCamera.tilt ?: 0.0, 0.001)
    }

    @Test
    fun resetToOverview_clearsSelectedStoreAndPreservesCurrentMode() {
        val store = viewModel.uiState.value.allStores.first()
        viewModel.selectStore(store)
        viewModel.onFlyToCompleted()

        // Reset in 2D mode keeps 2D mode
        viewModel.resetToOverview()

        val state2D = viewModel.uiState.value
        assertNull(state2D.selectedStore)
        assertTrue(state2D.isOverviewMode)
        assertFalse(state2D.is3DMode)
        assertEquals(Map3DMode.ROADMAP, state2D.mapMode)
        assertEquals(StoreLocation.OVERVIEW_CAMERA_TILT, state2D.targetCamera.tilt ?: 0.0, 0.001)
        assertEquals(StoreLocation.OVERVIEW_CAMERA_RANGE, state2D.targetCamera.range ?: 0.0, 0.001)

        val flyTo = state2D.flyToCommand
        assertNotNull(flyTo)
        assertEquals(StoreLocation.OVERVIEW_CAMERA_TILT, flyTo?.endCamera?.tilt ?: 0.0, 0.001)
        assertEquals(StoreLocation.OVERVIEW_CAMERA_RANGE, flyTo?.endCamera?.range ?: 0.0, 0.001)

        // Switch to 3D mode, select a store, and reset to overview -> keeps 3D mode
        viewModel.toggle2D3DMode()
        viewModel.selectStore(store)
        viewModel.onFlyToCompleted()
        viewModel.resetToOverview()

        val state3D = viewModel.uiState.value
        assertNull(state3D.selectedStore)
        assertTrue(state3D.isOverviewMode)
        assertTrue(state3D.is3DMode)
        assertEquals(Map3DMode.HYBRID, state3D.mapMode)
        assertEquals(StoreLocation.OVERVIEW_3D_CAMERA_TILT, state3D.targetCamera.tilt ?: 0.0, 0.001)
    }

    @Test
    fun filteringOutSelectedStore_automaticallyResetsToOverviewKeepingSelectedMode() {
        // Switch to 3D mode and select Market Street store (which only has OPEN_NOW, not DRIVE_THRU)
        viewModel.toggle2D3DMode()
        viewModel.selectStore("store_market_street")
        viewModel.onFlyToCompleted()
        assertEquals("store_market_street", viewModel.uiState.value.selectedStore?.id)
        assertTrue(viewModel.uiState.value.is3DMode)

        // Toggle DRIVE_THRU which excludes Market Street store
        viewModel.toggleAmenity(StoreAmenity.DRIVE_THRU)

        val state = viewModel.uiState.value
        assertNull(state.selectedStore)
        assertTrue(state.isOverviewMode)
        assertTrue(state.is3DMode)
        assertEquals(Map3DMode.HYBRID, state.mapMode)
        assertEquals(StoreLocation.OVERVIEW_3D_CAMERA_TILT, state.targetCamera.tilt ?: 0.0, 0.001)
        assertNotNull(state.flyToCommand)
    }

    @Test
    fun syncInitialMapMode_in3DMode_setsTiltedOverviewCameraWithoutFlyToCommand() {
        viewModel.syncInitialMapMode(Map3DMode.HYBRID)

        val state = viewModel.uiState.value
        assertTrue(state.is3DMode)
        assertEquals(Map3DMode.HYBRID, state.mapMode)
        assertEquals(StoreLocation.OVERVIEW_3D_CAMERA_TILT, state.targetCamera.tilt ?: 0.0, 0.001)
        assertNull(state.flyToCommand)
    }
}
