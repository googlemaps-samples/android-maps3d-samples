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

// [START maps_3d_quick_settings_controller]
/**
 * Pure Kotlin state machine controller for managing 3D map settings.
 *
 * Manages transitions between map rendering modes and color schemes.
 * Completely decoupled from Android UI and Views for deterministic JVM unit testing.
 *
 * @param initialSettings Initial settings snapshot (defaults to [Map3DMode.HYBRID] and [Map3DColorScheme.FOLLOW_SYSTEM]).
 */
class Map3DSettingsController(
    initialSettings: Map3DSettings = Map3DSettings(),
) {
    private var state: Map3DSettings = initialSettings
    private val listeners = mutableListOf<(Map3DSettings) -> Unit>()

    /** Returns the current [Map3DSettings] snapshot. */
    fun getSettings(): Map3DSettings = state

    /**
     * Updates the map rendering mode ([Map3DMode.SATELLITE], [Map3DMode.HYBRID], or [Map3DMode.ROADMAP]).
     *
     * Invalid or unknown modes are ignored.
     */
    fun setMapMode(@Map3DMode mode: Int): Map3DSettings {
        val sanitized = sanitizeMapMode(mode)
        if (sanitized == null || sanitized == state.mapMode) {
            return state
        }
        state = state.copy(mapMode = sanitized)
        notifyListeners()
        return state
    }

    /**
     * Updates the color scheme ([Map3DColorScheme.FOLLOW_SYSTEM], [Map3DColorScheme.LIGHT], or [Map3DColorScheme.DARK]).
     */
    fun setColorScheme(scheme: Map3DColorScheme): Map3DSettings {
        if (scheme == state.colorScheme) {
            return state
        }
        state = state.copy(colorScheme = scheme)
        notifyListeners()
        return state
    }

    /**
     * Updates the UI controls visibility configuration.
     */
    fun setUiControls(uiControls: Map3DUiControls): Map3DSettings {
        if (uiControls == state.uiControls) {
            return state
        }
        state = state.copy(uiControls = uiControls)
        notifyListeners()
        return state
    }

    /**
     * Toggles all UI controls on or off simultaneously.
     */
    fun setAllUiControls(enabled: Boolean): Map3DSettings {
        return setUiControls(state.uiControls.withAll(enabled))
    }

    /**
     * Updates a single UI control's visibility.
     */
    fun updateUiControl(
        zoom: Boolean = state.uiControls.zoom,
        tilt: Boolean = state.uiControls.tilt,
        rotate: Boolean = state.uiControls.rotate,
        compass: Boolean = state.uiControls.compass,
        panVertical: Boolean = state.uiControls.panVertical,
        panHorizontal: Boolean = state.uiControls.panHorizontal,
    ): Map3DSettings {
        val updatedControls = state.uiControls.copy(
            zoom = zoom,
            tilt = tilt,
            rotate = rotate,
            compass = compass,
            panVertical = panVertical,
            panHorizontal = panHorizontal,
        )
        return setUiControls(updatedControls)
    }

    /**
     * Replaces the entire settings snapshot.
     */
    fun setSettings(newSettings: Map3DSettings): Map3DSettings {
        if (newSettings == state) {
            return state
        }
        val sanitizedMode = sanitizeMapMode(newSettings.mapMode) ?: state.mapMode
        state = Map3DSettings(
            mapMode = sanitizedMode,
            colorScheme = newSettings.colorScheme,
            uiControls = newSettings.uiControls,
        )
        notifyListeners()
        return state
    }

    /** Registers a listener invoked whenever settings change. */
    fun addListener(listener: (Map3DSettings) -> Unit) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    /** Unregisters a previously registered listener. */
    fun removeListener(listener: (Map3DSettings) -> Unit) {
        listeners.remove(listener)
    }

    private fun sanitizeMapMode(mode: Int): Int? {
        return when (mode) {
            Map3DMode.HYBRID, Map3DMode.SATELLITE, Map3DMode.ROADMAP -> mode
            else -> null
        }
    }

    private fun notifyListeners() {
        val currentSnapshot = state
        for (listener in listeners) {
            listener(currentSnapshot)
        }
    }
}
// [END maps_3d_quick_settings_controller]
