// Copyright 2026 Google LLC
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
import com.example.advancedmaps3dsamples.modules.DirectionsErrorException
import com.example.advancedmaps3dsamples.modules.RouteRepository
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * UI State for the Routes Sample API call.
 */
sealed interface RouteUiState {
    object Idle : RouteUiState
    object Loading : RouteUiState
    data class Success(
        val decodedPolyline: List<LatLng>,
        val navigationPoints: List<LatLng>
    ) : RouteUiState
    data class Error(val message: String) : RouteUiState
}

/**
 * ViewModel responsible for orchestrating the route fetch and 
 * converting the API's encoded polyline into a List of LatLngs
 * to be consumed by the UI.
 * 
 * WHY A VIEWMODEL?
 * Activities and Fragments in Android can be destroyed and recreated (e.g., when the device rotates). 
 * The ViewModel survives these configuration changes! By keeping our route data here, we ensure that a user 
 * doesn't lose their downloaded route just because they turned their phone sideways.
 */
@HiltViewModel
class RouteViewModel @Inject constructor(
    private val routeRepository: RouteRepository
) : ViewModel() {

    // WHY STATEFLOW?
    // StateFlow acts as an observable data holder. The ViewModel updates the StateFlow, 
    // and the Compose UI automatically 'listens' (collects) and redraws itself whenever the state changes.
    // We use a backing property (_uiState) that is mutable within the ViewModel, but expose 
    // a read-only StateFlow (uiState) to the UI so it can't accidentally alter the data.
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
                val response = routeRepository.fetchRoute(
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
                    // WHY DECODE ON DISPATCHERS.DEFAULT?
                    // While a standard route response might only contain a few hundred points,
                    // other data sources like high-resolution GPX files can contain thousands.
                    // PolyUtil.decode is a synchronous, CPU-heavy math operation. By explicitly
                    // shifting to the Default dispatcher (optimized for CPU work), we ensure that
                    // processing massive polyline strings will never drop UI frames or cause "jank".
                    val decoded = withContext(Dispatchers.Default) {
                        PolyUtil.decode(encodedPolyline)
                    }
                    
                    // Extract important navigation points from legs/steps
                    val navPoints = route.legs.flatMap { leg ->
                        leg.steps.mapNotNull { step ->
                            step.startLocation?.latLng?.let { LatLng(it.latitude, it.longitude) }
                        }
                    }.toMutableList()
                    
                    // Ensure the destination is the last point
                    navPoints.add(dest)
                    
                    _uiState.value = RouteUiState.Success(decoded, navPoints)
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
