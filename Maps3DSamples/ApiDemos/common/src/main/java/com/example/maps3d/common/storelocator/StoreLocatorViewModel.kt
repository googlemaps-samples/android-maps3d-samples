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

package com.example.maps3d.common.storelocator

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.Map3DMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Consumable one-shot camera flight command emitted when selecting a store, toggling 2D/3D mode,
 * or returning to the 2D roadmap overview.
 */
data class StoreFlyToCommand(
    val endCamera: Camera,
    val durationMillis: Long,
)

/**
 * Immutable UI state for the canonical 2D/3D Store Locator sample.
 */
data class StoreLocatorUiState(
    val allStores: List<StoreLocation> = StoreLocation.SAN_FRANCISCO_STORES,
    val filteredStores: List<StoreLocation> = StoreLocation.SAN_FRANCISCO_STORES,
    val selectedAmenities: Set<StoreAmenity> = emptySet(),
    val selectedStore: StoreLocation? = null,
    val isOverviewMode: Boolean = true,
    val is3DMode: Boolean = false,
    @param:Map3DMode @get:Map3DMode val mapMode: Int = Map3DMode.ROADMAP,
    val targetCamera: Camera = StoreLocation.OVERVIEW_CAMERA,
    val flyToCommand: StoreFlyToCommand? = null,
)

/**
 * Shared Architecture ViewModel for the Store Locator sample across Kotlin Views, Java Views,
 * and Jetpack Compose.
 *
 * Exposes immutable [StoreLocatorUiState] via [StateFlow] (for Kotlin/Compose) and [LiveData]
 * (for Java Views via [asLiveData]).
 */
class StoreLocatorViewModel(
    initialStores: List<StoreLocation> = StoreLocation.SAN_FRANCISCO_STORES,
) : ViewModel() {

    private val sortedStores = initialStores.sortedBy { it.distanceKm }

    private val _uiState = MutableStateFlow(
        StoreLocatorUiState(
            allStores = sortedStores,
            filteredStores = sortedStores,
        ),
    )
    val uiState: StateFlow<StoreLocatorUiState> = _uiState.asStateFlow()

    val liveData: LiveData<StoreLocatorUiState> = _uiState.asLiveData()

    val currentState: StoreLocatorUiState
        get() = _uiState.value

    /**
     * Toggles a single [StoreAmenity] filter chip and updates the filtered stores and map markers.
     */
    fun toggleAmenity(amenity: StoreAmenity) {
        val currentAmenities = _uiState.value.selectedAmenities
        val updatedAmenities = if (amenity in currentAmenities) {
            currentAmenities - amenity
        } else {
            currentAmenities + amenity
        }
        setAmenities(updatedAmenities)
    }

    /**
     * Replaces the active set of [StoreAmenity] filters and recalculates the visible stores.
     * If the currently selected store no longer matches the active filters, resets selection
     * back to the overview while keeping the currently selected 2D/3D mode.
     */
    fun setAmenities(amenities: Set<StoreAmenity>) {
        _uiState.update { current ->
            val filtered = current.allStores
                .filter { it.matchesAmenities(amenities) }
                .sortedBy { it.distanceKm }
            val stillSelected = current.selectedStore?.takeIf { selected ->
                filtered.any { it.id == selected.id }
            }
            val shouldReturnToOverview = current.selectedStore != null && stillSelected == null
            val overviewCamera = if (current.is3DMode) {
                StoreLocation.OVERVIEW_CAMERA_3D
            } else {
                StoreLocation.OVERVIEW_CAMERA
            }
            val nextCamera = if (shouldReturnToOverview) {
                overviewCamera
            } else {
                current.targetCamera
            }
            val nextFlyTo = if (shouldReturnToOverview) {
                StoreFlyToCommand(
                    endCamera = overviewCamera,
                    durationMillis = OVERVIEW_FLY_DURATION_MS,
                )
            } else {
                current.flyToCommand
            }

            current.copy(
                selectedAmenities = amenities,
                filteredStores = filtered,
                selectedStore = stillSelected,
                isOverviewMode = stillSelected == null,
                targetCamera = nextCamera,
                flyToCommand = nextFlyTo,
            )
        }
    }

    /**
     * Clears all active amenity filters, restoring all stores sorted by distance.
     */
    fun clearFilters() {
        setAmenities(emptySet())
    }

    /**
     * Selects a store by its [storeId], scaling up its pin and animating the camera to inspect
     * the store while preserving the currently selected 2D or 3D mode.
     */
    fun selectStore(storeId: String) {
        val store = _uiState.value.allStores.find { it.id == storeId } ?: return
        selectStore(store)
    }

    /**
     * Selects [store], scaling up its pin and animating the camera to the store's 2D or 3D
     * inspection perspective based on the currently selected [StoreLocatorUiState.is3DMode].
     */
    fun selectStore(store: StoreLocation) {
        _uiState.update { current ->
            val storeCamera = if (current.is3DMode) {
                store.toDetailCamera()
            } else {
                store.to2DCamera()
            }
            current.copy(
                selectedStore = store,
                isOverviewMode = false,
                targetCamera = storeCamera,
                flyToCommand = StoreFlyToCommand(
                    endCamera = storeCamera,
                    durationMillis = DETAIL_FLY_DURATION_MS,
                ),
            )
        }
    }

    /**
     * Toggles between 2D Map ([Map3DMode.ROADMAP], low tilt) and 3D Map ([Map3DMode.HYBRID], 55° tilt)
     * for either the currently selected store or the city-wide overview.
     */
    fun toggle2D3DMode() {
        _uiState.update { current ->
            val nextIs3D = !current.is3DMode
            val nextMapMode = if (nextIs3D) Map3DMode.HYBRID else Map3DMode.ROADMAP
            val selected = current.selectedStore
            val nextCamera = if (selected != null) {
                if (nextIs3D) selected.toDetailCamera() else selected.to2DCamera()
            } else {
                if (nextIs3D) StoreLocation.OVERVIEW_CAMERA_3D else StoreLocation.OVERVIEW_CAMERA
            }
            current.copy(
                is3DMode = nextIs3D,
                mapMode = nextMapMode,
                targetCamera = nextCamera,
                flyToCommand = StoreFlyToCommand(
                    endCamera = nextCamera,
                    durationMillis = TOGGLE_2D_3D_FLY_DURATION_MS,
                ),
            )
        }
    }

    /**
     * Synchronizes the initial session [mode] when entering the Store Locator screen so that
     * landing in 3D mode immediately configures a 3D tilted camera without triggering a
     * redundant startup camera flight.
     */
    fun syncInitialMapMode(@Map3DMode mode: Int) {
        _uiState.update { current ->
            val nextIs3D = mode != Map3DMode.ROADMAP
            val selected = current.selectedStore
            val nextCamera = if (selected != null) {
                if (nextIs3D) selected.toDetailCamera() else selected.to2DCamera()
            } else {
                if (nextIs3D) StoreLocation.OVERVIEW_CAMERA_3D else StoreLocation.OVERVIEW_CAMERA
            }
            current.copy(
                mapMode = mode,
                is3DMode = nextIs3D,
                targetCamera = nextCamera,
            )
        }
    }

    /**
     * Synchronizes [StoreLocatorUiState.mapMode] and [StoreLocatorUiState.is3DMode] when the user
     * selects a map mode via the Quick Settings overlay.
     */
    fun setMapMode(@Map3DMode mode: Int) {
        _uiState.update { current ->
            if (current.mapMode == mode) {
                current
            } else {
                val nextIs3D = mode != Map3DMode.ROADMAP
                val selected = current.selectedStore
                val nextCamera = if (current.is3DMode != nextIs3D) {
                    if (selected != null) {
                        if (nextIs3D) selected.toDetailCamera() else selected.to2DCamera()
                    } else {
                        if (nextIs3D) StoreLocation.OVERVIEW_CAMERA_3D else StoreLocation.OVERVIEW_CAMERA
                    }
                } else {
                    current.targetCamera
                }
                val nextFlyTo = if (current.is3DMode != nextIs3D) {
                    StoreFlyToCommand(
                        endCamera = nextCamera,
                        durationMillis = TOGGLE_2D_3D_FLY_DURATION_MS,
                    )
                } else {
                    current.flyToCommand
                }
                current.copy(
                    mapMode = mode,
                    is3DMode = nextIs3D,
                    targetCamera = nextCamera,
                    flyToCommand = nextFlyTo,
                )
            }
        }
    }

    /**
     * Clears any selected store and flies the camera back to the overview while keeping the
     * currently selected 2D or 3D mode.
     */
    fun resetToOverview() {
        _uiState.update { current ->
            val overviewCamera = if (current.is3DMode) {
                StoreLocation.OVERVIEW_CAMERA_3D
            } else {
                StoreLocation.OVERVIEW_CAMERA
            }
            current.copy(
                selectedStore = null,
                isOverviewMode = true,
                targetCamera = overviewCamera,
                flyToCommand = StoreFlyToCommand(
                    endCamera = overviewCamera,
                    durationMillis = OVERVIEW_FLY_DURATION_MS,
                ),
            )
        }
    }

    /**
     * Consumes the one-shot [StoreLocatorUiState.flyToCommand] after the View initiates the
     * camera flight animation.
     */
    fun onFlyToCompleted() {
        _uiState.update { it.copy(flyToCommand = null) }
    }

    companion object {
        const val DETAIL_FLY_DURATION_MS = 1800L
        const val OVERVIEW_FLY_DURATION_MS = 1500L
        const val TOGGLE_2D_3D_FLY_DURATION_MS = 1200L
    }
}
