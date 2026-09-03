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

package com.example.placesuikit3d.data.repository

import com.example.placesuikit3d.data.model.PlaceSearchResult
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.compose.autocomplete.models.AutocompletePlace

/**
 * Repository interface for Places API and Places UI Kit data operations.
 */
interface PlacesRepository {
    /**
     * Executes text search for places matching [query] around optional [center].
     */
    suspend fun searchPlacesByText(
        query: String,
        center: LatLngAltitude? = null,
        radiusMeters: Double = 5000.0,
    ): Result<List<PlaceSearchResult>>

    /**
     * Searches places by predefined category filter around optional [center].
     */
    suspend fun searchPlacesByCategory(
        category: String,
        center: LatLngAltitude? = null,
        radiusMeters: Double = 5000.0,
    ): Result<List<PlaceSearchResult>>

    /**
     * Retrieves autocomplete typeahead predictions for the given [query] utilizing places-compose.
     */
    suspend fun getAutocompletePredictions(
        query: String,
        center: LatLngAltitude? = null,
    ): Result<List<AutocompletePlace>>

    /**
     * Fetches detailed place metadata including photos, reviews, rating, and opening hours.
     */
    suspend fun fetchPlaceDetails(
        placeId: String,
    ): Result<Place>
}
