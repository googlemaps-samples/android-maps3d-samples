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

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [PlacesRepositoryImpl].
 */
class PlacesRepositoryTest {

    private lateinit var repository: PlacesRepositoryImpl

    @Before
    fun setUp() {
        repository = PlacesRepositoryImpl()
    }

    @Test
    fun searchPlacesByCategory_returnsCuratedPlacesWhenUninitialized() = runTest {
        val result = repository.searchPlacesByCategory("🏛️ Landmarks")
        assertThat(result.isSuccess).isTrue()
        val places = result.getOrNull()
        assertThat(places).isNotNull()
        assertThat(places).isNotEmpty()
        assertThat(places?.any { it.name.contains("Flatirons") }).isTrue()
    }

    @Test
    fun searchPlacesByText_filtersMatchesCorrectly() = runTest {
        val result = repository.searchPlacesByText("Coffee")
        assertThat(result.isSuccess).isTrue()
        val places = result.getOrNull()
        assertThat(places).isNotNull()
        assertThat(places).isNotEmpty()
        assertThat(places?.any { it.name.contains("Coffee") || it.category?.contains("Cafes") == true }).isTrue()
    }

    @Test
    fun getAutocompletePredictions_returnsMatchingSuggestions() = runTest {
        val result = repository.getAutocompletePredictions("Chautauqua")
        assertThat(result.isSuccess).isTrue()
        val suggestions = result.getOrNull()
        assertThat(suggestions).isNotNull()
        assertThat(suggestions).isNotEmpty()
        assertThat(suggestions?.first()?.primaryText?.toString()).contains("Chautauqua")
    }

    @Test
    fun fetchPlaceDetails_returnsPlaceMetadata() = runTest {
        val result = repository.fetchPlaceDetails("ChIJfXOTtWbsa4cRmW07qJRB6_8")
        assertThat(result.isSuccess).isTrue()
        val place = result.getOrNull()
        assertThat(place).isNotNull()
        assertThat(place?.id).isEqualTo("ChIJfXOTtWbsa4cRmW07qJRB6_8")
    }
}
