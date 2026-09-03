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

import com.example.placesuikit3d.data.model.Camera3DTarget
import com.example.placesuikit3d.data.model.PlaceSearchResult
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.compose.autocomplete.models.AutocompletePlace

/**
 * Sealed hierarchy defining the state of search and place details.
 */
sealed interface PlaceSearchUiState {
    object Idle : PlaceSearchUiState
    object Loading : PlaceSearchUiState
    data class SearchResultsLoaded(
        val places: List<PlaceSearchResult>,
        val selectedPlace: PlaceSearchResult? = null,
    ) : PlaceSearchUiState
    data class PlaceDetailsLoaded(
        val place: Place,
        val cameraTarget: Camera3DTarget,
    ) : PlaceSearchUiState
    data class Error(val message: String) : PlaceSearchUiState
}

/**
 * Sealed hierarchy representing the 3D camera controller animation state.
 */
sealed interface Camera3DMode {
    object Standard3D : Camera3DMode
    data class Orbiting(val center: LatLngAltitude, val radius: Double, val animationId: Long = System.currentTimeMillis()) : Camera3DMode
    data class FlyingTo(val target: Camera3DTarget, val animationId: Long = System.currentTimeMillis()) : Camera3DMode
}

data class PlaceSearch3DScreenState(
    val searchUiState: PlaceSearchUiState = PlaceSearchUiState.Idle,
    val cameraMode: Camera3DMode = Camera3DMode.Standard3D,
    val selectedPlace: PlaceSearchResult? = null,
    val selectedPlaceId: String? = null,
    val selectedCategory: String = "🏛️ Landmarks",
    val searchQuery: String = "",
    val isOrbiting: Boolean = false,
    val is3DView: Boolean = true,
    val allMarkers: List<PlaceSearchResult> = emptyList(),
    val autocompleteSuggestions: List<AutocompletePlace> = emptyList(),
    val infoMessage: String? = null,
)
