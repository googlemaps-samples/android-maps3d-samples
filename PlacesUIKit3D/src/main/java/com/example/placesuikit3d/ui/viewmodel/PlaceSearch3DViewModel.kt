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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.placesuikit3d.R
import com.example.placesuikit3d.data.model.Camera3DTarget
import com.example.placesuikit3d.data.model.PlaceSearchResult
import com.example.placesuikit3d.data.repository.PlacesRepository
import com.example.placesuikit3d.data.repository.PlacesRepositoryImpl
import com.example.placesuikit3d.utils.StringProvider
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.android.libraries.places.compose.autocomplete.models.AutocompletePlace
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel governing the 3D Place Search and Autocomplete showcase.
 * Adheres to Unidirectional Data Flow (UDF) and clean MVVM architecture.
 */
@HiltViewModel
class PlaceSearch3DViewModel @Inject constructor(
    private val repository: PlacesRepository,
    private val stringProvider: StringProvider = StringProvider { _, _ -> "" },
) : ViewModel() {

    private fun getString(resId: Int, vararg formatArgs: Any): String = stringProvider.getString(resId, *formatArgs)

    private val _screenState = MutableStateFlow(
        PlaceSearch3DScreenState(
            selectedCategory = "🏛️ Landmarks",
            allMarkers = PlacesRepositoryImpl.getCuratedPlacesByCategory("🏛️ Landmarks"),
        ),
    )
    val screenState: StateFlow<PlaceSearch3DScreenState> = _screenState.asStateFlow()

    private var searchJob: Job? = null
    private var autocompleteJob: Job? = null

    init {
        // Load initial US hot locations category and highlight top iconic US landmark
        loadInitialUSHotLocation()
    }

    private fun loadInitialUSHotLocation() {
        val initialPlaces = PlacesRepositoryImpl.getCuratedPlacesByCategory("🏛️ Landmarks")
        val defaultHotspot = initialPlaces.firstOrNull()

        if (defaultHotspot != null) {
            val markedPlaces = initialPlaces.map { it.copy(isSelected = it.id == defaultHotspot.id) }
            val initialTarget = defaultHotspot.toCameraTarget()

            _screenState.update { current ->
                current.copy(
                    selectedCategory = "🏛️ Landmarks",
                    allMarkers = markedPlaces,
                    selectedPlace = defaultHotspot.copy(isSelected = true),
                    selectedPlaceId = null, // Window stays closed until user taps marker
                    searchUiState = PlaceSearchUiState.SearchResultsLoaded(markedPlaces, defaultHotspot),
                    cameraMode = Camera3DMode.FlyingTo(initialTarget),
                    infoMessage = getString(R.string.highlighting_landmark, defaultHotspot.name),
                )
            }
        }
    }

    /**
     * Executes category search (e.g. "🏛️ Landmarks", "☕ Cafes", "🏨 Hotels", "🍕 Restaurants").
     */
    fun selectCategory(category: String, center: LatLngAltitude? = null) {
        searchJob?.cancel()
        _screenState.update {
            it.copy(
                selectedCategory = category,
                searchUiState = PlaceSearchUiState.Loading,
                isOrbiting = false,
                autocompleteSuggestions = emptyList(),
            )
        }

        searchJob = viewModelScope.launch {
            val result = repository.searchPlacesByCategory(category, center)
            result.onSuccess { places ->
                val markedPlaces = places.map { it.copy(isSelected = false) }
                _screenState.update { current ->
                    current.copy(
                        allMarkers = markedPlaces,
                        selectedPlace = null,
                        selectedPlaceId = null,
                        searchUiState = PlaceSearchUiState.SearchResultsLoaded(markedPlaces, null),
                        infoMessage = getString(R.string.found_places_query, markedPlaces.size, category),
                    )
                }
            }.onFailure { error ->
                _screenState.update {
                    it.copy(searchUiState = PlaceSearchUiState.Error(error.message ?: getString(R.string.error_search_failed)))
                }
            }
        }
    }

    /**
     * Executes free-text search for places.
     */
    fun searchByQuery(query: String, center: LatLngAltitude? = null) {
        if (query.isBlank()) return
        searchJob?.cancel()
        _screenState.update {
            it.copy(
                searchQuery = query,
                searchUiState = PlaceSearchUiState.Loading,
                isOrbiting = false,
                autocompleteSuggestions = emptyList(),
            )
        }

        searchJob = viewModelScope.launch {
            val result = repository.searchPlacesByText(query, center)
            result.onSuccess { places ->
                if (places.isNotEmpty()) {
                    val firstPlace = places.first()
                    val markedPlaces = places.map { it.copy(isSelected = it.id == firstPlace.id) }
                    val target = firstPlace.toCameraTarget()

                    _screenState.update { current ->
                        current.copy(
                            allMarkers = markedPlaces,
                            selectedPlace = firstPlace.copy(isSelected = true),
                            selectedPlaceId = null, // Highlighted on map first; window opens on marker tap
                            searchUiState = PlaceSearchUiState.SearchResultsLoaded(markedPlaces, firstPlace),
                            cameraMode = Camera3DMode.FlyingTo(target),
                            infoMessage = getString(R.string.found_places_query, places.size, query),
                        )
                    }
                } else {
                    _screenState.update {
                        it.copy(
                            allMarkers = emptyList(),
                            selectedPlace = null,
                            selectedPlaceId = null,
                            searchUiState = PlaceSearchUiState.SearchResultsLoaded(emptyList(), null),
                            infoMessage = getString(R.string.no_places_found_query, query),
                        )
                    }
                }
            }.onFailure { error ->
                _screenState.update {
                    it.copy(searchUiState = PlaceSearchUiState.Error(error.message ?: getString(R.string.error_search_failed)))
                }
            }
        }
    }

    /**
     * Fetches real-time autocomplete suggestions as the user types.
     */
    fun onQueryChanged(query: String, center: LatLngAltitude? = null) {
        _screenState.update { it.copy(searchQuery = query) }
        if (query.isBlank()) {
            _screenState.update { it.copy(autocompleteSuggestions = emptyList()) }
            return
        }

        autocompleteJob?.cancel()
        autocompleteJob = viewModelScope.launch {
            val result = repository.getAutocompletePredictions(query, center)
            result.onSuccess { suggestions ->
                _screenState.update { it.copy(autocompleteSuggestions = suggestions) }
            }
        }
    }

    /**
     * User selected an autocomplete prediction suggestion:
     * 1. Displays selected place name in the search box.
     * 2. Focuses camera and highlights location with 3D marker.
     * 3. Keeps details window closed until user clicks the marker.
     */
    fun onSuggestionSelected(prediction: AutocompletePlace) {
        val queryText = prediction.primaryText.toString()
        _screenState.update {
            it.copy(
                searchQuery = queryText,
                autocompleteSuggestions = emptyList(),
                isOrbiting = false,
            )
        }

        viewModelScope.launch {
            val detailsResult = repository.fetchPlaceDetails(prediction.placeId)
            detailsResult.onSuccess { place ->
                val placeSearchResult = PlaceSearchResult.fromPlace(place, isSelected = true)
                val target = placeSearchResult.toCameraTarget()

                _screenState.update { current ->
                    current.copy(
                        searchQuery = queryText,
                        selectedPlace = placeSearchResult,
                        selectedPlaceId = null, // Highlighted first; window shows on marker click
                        allMarkers = listOf(placeSearchResult),
                        searchUiState = PlaceSearchUiState.SearchResultsLoaded(listOf(placeSearchResult), placeSearchResult),
                        cameraMode = Camera3DMode.FlyingTo(target),
                        infoMessage = getString(R.string.focused_on_place, placeSearchResult.name),
                    )
                }
            }.onFailure { error ->
                _screenState.update {
                    it.copy(searchUiState = PlaceSearchUiState.Error(error.message ?: getString(R.string.error_find_location)))
                }
            }
        }
    }

    /**
     * User tapped marker on map or selected item in list: opens Place Details window.
     */
    fun onPlaceSelected(placeId: String) {
        val currentMarkers = _screenState.value.allMarkers
        val matchedSearchResult = currentMarkers.find { it.id == placeId }
            ?: PlacesRepositoryImpl.getCuratedPlaces().find { it.id == placeId }
        val immediateSelected = matchedSearchResult?.copy(isSelected = true)
        val immediateTarget = immediateSelected?.toCameraTarget()

        val updatedMarkers = currentMarkers.map {
            it.copy(isSelected = it.id == placeId)
        }

        _screenState.update { current ->
            current.copy(
                searchQuery = immediateSelected?.name ?: current.searchQuery,
                selectedPlace = immediateSelected,
                selectedPlaceId = placeId,
                allMarkers = if (updatedMarkers.any { it.id == placeId }) {
                    updatedMarkers
                } else if (immediateSelected != null) {
                    updatedMarkers + immediateSelected
                } else {
                    updatedMarkers
                },
                cameraMode = if (immediateTarget != null) Camera3DMode.FlyingTo(immediateTarget) else current.cameraMode,
                isOrbiting = false,
                autocompleteSuggestions = emptyList(),
            )
        }

        viewModelScope.launch {
            val detailsResult = repository.fetchPlaceDetails(placeId)
            detailsResult.onSuccess { place ->
                val selectedSearchResult = (immediateSelected ?: PlaceSearchResult.fromPlace(place)).copy(
                    rating = place.rating ?: immediateSelected?.rating,
                    userRatingsTotal = place.userRatingCount ?: immediateSelected?.userRatingsTotal,
                    isSelected = true,
                )
                val target = selectedSearchResult.toCameraTarget()

                val syncedMarkers = _screenState.value.allMarkers.map {
                    if (it.id == placeId) {
                        it.copy(
                            rating = place.rating ?: it.rating,
                            userRatingsTotal = place.userRatingCount ?: it.userRatingsTotal,
                            isSelected = true,
                        )
                    } else {
                        it.copy(isSelected = false)
                    }
                }

                _screenState.update { current ->
                    current.copy(
                        searchQuery = selectedSearchResult.name,
                        selectedPlace = selectedSearchResult,
                        selectedPlaceId = placeId,
                        allMarkers = if (syncedMarkers.any { it.id == placeId }) syncedMarkers else syncedMarkers + selectedSearchResult,
                        searchUiState = PlaceSearchUiState.PlaceDetailsLoaded(place, target),
                        cameraMode = Camera3DMode.FlyingTo(target),
                    )
                }
            }.onFailure { error ->
                _screenState.update {
                    it.copy(searchUiState = PlaceSearchUiState.Error(error.message ?: getString(R.string.error_load_place_details)))
                }
            }
        }
    }

    /**
     * Marker on 3D map was clicked.
     */
    fun onMarkerClicked(place: PlaceSearchResult) {
        onPlaceSelected(place.id)
    }

    /**
     * Toggles 360-degree Orbit mode around the currently selected place or camera center.
     */
    fun toggleOrbit(currentCenter: LatLngAltitude? = null) {
        val current = _screenState.value
        if (current.isOrbiting) {
            stopOrbit()
        } else {
            val centerTarget = current.selectedPlace?.location ?: currentCenter
            if (centerTarget != null) {
                _screenState.update {
                    it.copy(
                        isOrbiting = true,
                        cameraMode = Camera3DMode.Orbiting(centerTarget, 1400.0),
                        infoMessage = getString(R.string.orbiting_place, current.selectedPlace?.name ?: getString(R.string.default_location_city)),
                    )
                }
            }
        }
    }

    /**
     * Stops orbit animation.
     */
    fun stopOrbit() {
        _screenState.update {
            it.copy(
                isOrbiting = false,
                cameraMode = Camera3DMode.Standard3D,
                infoMessage = getString(R.string.stopped_orbit),
            )
        }
    }

    /**
     * Toggles between 3D perspective view (tilt 50°) and 2D top-down view (tilt 0°).
     */
    fun toggle2D3DView(currentLocation: LatLngAltitude? = null) {
        val current = _screenState.value
        val newIs3D = !current.is3DView
        val center = current.selectedPlace?.location ?: currentLocation ?: Camera3DTarget.DEFAULT.toLatLngAltitude()

        val newTarget = if (newIs3D) {
            Camera3DTarget.fromLocation(center, heading = 25.0, tilt = 50.0, range = 1400.0)
        } else {
            Camera3DTarget.fromLocation(center, heading = 0.0, tilt = 0.0, range = 3500.0)
        }

        _screenState.update {
            it.copy(
                is3DView = newIs3D,
                isOrbiting = false,
                cameraMode = Camera3DMode.FlyingTo(newTarget),
                infoMessage = if (newIs3D) getString(R.string.switched_to_3d) else getString(R.string.switched_to_2d),
            )
        }
    }

    /**
     * Resets camera to standard scenic overview.
     */
    fun resetCameraOverview() {
        stopOrbit()
        _screenState.update {
            it.copy(
                selectedPlace = null,
                selectedPlaceId = null,
                cameraMode = Camera3DMode.FlyingTo(Camera3DTarget.DEFAULT),
                infoMessage = getString(R.string.camera_reset_overview),
            )
        }
    }

    /**
     * Dismisses the place details bottom sheet overlay.
     */
    fun dismissPlaceDetails() {
        stopOrbit()
        val unselectedMarkers = _screenState.value.allMarkers.map { it.copy(isSelected = false) }
        _screenState.update {
            it.copy(
                selectedPlace = null,
                selectedPlaceId = null,
                allMarkers = unselectedMarkers,
                searchUiState = PlaceSearchUiState.SearchResultsLoaded(unselectedMarkers, null),
            )
        }
    }

    /**
     * Clears transient messages.
     */
    fun clearInfoMessage() {
        _screenState.update { it.copy(infoMessage = null) }
    }
}
