// Copyright 2025 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.advancedmaps3dsamples.scenarios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.advancedmaps3dsamples.common.DirectionsErrorException
import com.example.advancedmaps3dsamples.common.RoutesApiService
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for the Routes Sample API call.
 */
sealed interface RouteUiState {
    object Idle : RouteUiState
    object Loading : RouteUiState
    data class Success(val decodedPolyline: List<LatLng>) : RouteUiState
    data class Error(val message: String) : RouteUiState
}

/**
 * ViewModel responsible for orchestrating the route fetch and 
 * converting the API's encoded polyline into a List of LatLngs
 * to be consumed by the UI.
 */
@HiltViewModel
class RouteViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow<RouteUiState>(RouteUiState.Idle)
    val uiState: StateFlow<RouteUiState> = _uiState.asStateFlow()

    fun fetchRoute(apiKey: String, origin: LatLng, dest: LatLng) {
        if (apiKey.isEmpty() || apiKey.contains("YOUR_API_KEY")) {
            _uiState.value = RouteUiState.Error("Invalid API Key. Please provide a real key.")
            return
        }

        _uiState.value = RouteUiState.Loading

        viewModelScope.launch {
            try {
                // Execute network call via Ktor
                val response = RoutesApiService.fetchRoute(
                    apiKey = apiKey,
                    originLat = origin.latitude,
                    originLng = origin.longitude,
                    destLat = dest.latitude,
                    destLng = dest.longitude
                )

                // The Routes API returns an array of routes. Grab the first one.
                val route = response.routes.firstOrNull()
                val encodedPolyline = route?.polyline?.encodedPolyline

                if (encodedPolyline != null) {
                    // Decode the polyline so Map3D can consume it (or further convert it to Polyline3DOptions)
                    val decoded = PolyUtil.decode(encodedPolyline)
                    _uiState.value = RouteUiState.Success(decoded)
                } else {
                    _uiState.value = RouteUiState.Error("No route returned from the Maps API.")
                }
            } catch (e: DirectionsErrorException) {
                // Re-emit known, user-friendly API errors
                _uiState.value = RouteUiState.Error(e.message ?: "Unknown API Error")
            } catch (e: Exception) {
                // Catch all other network/parsing issues
                _uiState.value = RouteUiState.Error("Network Error: ${e.message}")
            }
        }
    }
}
