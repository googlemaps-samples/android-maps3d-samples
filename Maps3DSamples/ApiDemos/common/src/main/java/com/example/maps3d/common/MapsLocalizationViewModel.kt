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

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.google.android.gms.maps3d.model.Map3DMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Shared Architecture ViewModel for Maps Localization across Kotlin Views, Java Views, and
 * Jetpack Compose.
 *
 * Exposes immutable [LocalizationState] via [StateFlow] (for Kotlin/Compose) and [LiveData]
 * (for Java Views via [asLiveData]).
 */
class MapsLocalizationViewModel(
    initialPreset: MapLocalePreset = MapLocalePreset.ENGLISH_US,
) : ViewModel() {

    private val controller = LocalizationController(initialPreset)

    private val _uiState = MutableStateFlow(controller.getState())
    val uiState: StateFlow<LocalizationState> = _uiState.asStateFlow()

    val localizationStateData: LiveData<LocalizationState> = _uiState.asLiveData()

    val currentState: LocalizationState
        get() = _uiState.value

    /** Selects a [MapLocalePreset] and updates the language and region. */
    fun selectPreset(preset: MapLocalePreset) {
        _uiState.value = controller.selectPreset(preset)
    }

    /** Switches between [Map3DMode.HYBRID] and [Map3DMode.ROADMAP] to inspect localized labels. */
    fun setMapMode(@Map3DMode mapMode: Int) {
        _uiState.value = controller.setMapMode(mapMode)
    }
}
