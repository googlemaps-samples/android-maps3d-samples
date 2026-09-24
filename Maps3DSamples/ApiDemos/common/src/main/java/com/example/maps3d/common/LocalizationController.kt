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

import com.google.android.gms.maps3d.model.Map3DMode

/**
 * Pure Kotlin state machine controller for Maps Localization.
 *
 * Manages transitions between [MapLocalePreset] configurations and map rendering modes
 * ([Map3DMode.HYBRID] vs [Map3DMode.ROADMAP]).
 */
class LocalizationController(
    initialPreset: MapLocalePreset = MapLocalePreset.ENGLISH_US,
) {
    private var state = LocalizationState(
        selectedPreset = initialPreset,
    )

    /** Returns the current immutable [LocalizationState]. */
    fun getState(): LocalizationState = state

    /**
     * Selects a new [MapLocalePreset] and updates language and region.
     */
    fun selectPreset(preset: MapLocalePreset): LocalizationState {
        state = state.copy(
            selectedPreset = preset,
            languageCode = preset.language,
            regionCode = preset.region,
        )
        return state
    }

    /**
     * Updates the 3D map rendering mode (`Map3DMode.HYBRID` or `Map3DMode.ROADMAP`).
     */
    fun setMapMode(@Map3DMode mapMode: Int): LocalizationState {
        state = state.copy(mapMode = mapMode)
        return state
    }
}
