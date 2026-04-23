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

package com.example.composedemos.routes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface RouteUiState {
    object Idle : RouteUiState
    object Loading : RouteUiState
    data class Success(
        val decodedPolyline: List<LatLng>,
        val navigationPoints: List<LatLng>,
    ) : RouteUiState
    data class Error(val message: String) : RouteUiState
}

class RouteViewModel(
    private val routeRepository: RouteRepository = RouteRepository(),
) : ViewModel() {

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
                val routeData = routeRepository.fetchRoute(apiKey, origin, dest)

                // Decode polyline on Default dispatcher (CPU heavy)
                val decoded = withContext(Dispatchers.Default) {
                    PolyUtil.decode(routeData.encodedPolyline)
                }

                _uiState.value = RouteUiState.Success(decoded, routeData.navPoints)
            } catch (e: Exception) {
                _uiState.value = RouteUiState.Error(e.message ?: "Unknown Error")
            }
        }
    }
}
