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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// [START maps_3d_quick_settings_controller]
/**
 * Pure Kotlin state machine controller for managing 3D map settings.
 *
 * Manages transitions between map rendering modes, color schemes, and camera UI controls.
 * Completely decoupled from Android UI and Views for deterministic JVM unit testing.
 *
 * @param initialSettings Initial settings snapshot (defaults to [Map3DMode.HYBRID] and [Map3DColorScheme.FOLLOW_SYSTEM]).
 */
class Map3DSettingsController(
    initialSettings: Map3DSettings = Map3DSettings(),
) {
    companion object {
        /**
         * Application-scoped in-memory singleton instance.
         *
         * Persists user choices across sample activities during a single app session
         * and resets cleanly when a new process session starts.
         */
        @JvmStatic
        val shared = Map3DSettingsController()
    }

    private val _settings = MutableStateFlow(initialSettings)

    /** Reactive stream of the current [Map3DSettings] snapshot. */
    @get:JvmName("getSettingsFlow")
    val settings: StateFlow<Map3DSettings> = _settings.asStateFlow()

    /**
     * Tracks whether the user has explicitly selected a Map Mode during the current session.
     *
     * When `false`, entering a sample via [onSampleEntered] adopts that sample's default map mode.
     * When `true`, the user's explicit choice is preserved across all samples until [reset] is called.
     */
    var hasUserOverriddenMapMode: Boolean = false
        private set

    private val listeners = mutableListOf<(Map3DSettings) -> Unit>()

    /** Returns the current [Map3DSettings] snapshot. */
    fun getSettings(): Map3DSettings = _settings.value

    /**
     * Called when a sample activity or screen is entered.
     *
     * If the user has not explicitly overridden the map mode during this session,
     * updates the active map mode to [sampleDefaultMapMode].
     */
    fun onSampleEntered(@Map3DMode sampleDefaultMapMode: Int) {
        if (!hasUserOverriddenMapMode) {
            val sanitized = sanitizeMapMode(sampleDefaultMapMode) ?: return
            if (sanitized != _settings.value.mapMode) {
                _settings.value = _settings.value.copy(mapMode = sanitized)
                notifyListeners()
            }
        }
    }

    /**
     * Resets all settings to defaults (`FOLLOW_SYSTEM`, all UI controls enabled,
     * and [sampleDefaultMapMode]) and clears [hasUserOverriddenMapMode].
     */
    @JvmOverloads
    fun reset(@Map3DMode sampleDefaultMapMode: Int = Map3DMode.HYBRID): Map3DSettings {
        hasUserOverriddenMapMode = false
        val sanitized = sanitizeMapMode(sampleDefaultMapMode) ?: Map3DMode.HYBRID
        val defaultSettings = Map3DSettings(mapMode = sanitized)
        if (_settings.value != defaultSettings) {
            _settings.value = defaultSettings
            notifyListeners()
        }
        return _settings.value
    }

    /**
     * Updates the map rendering mode ([Map3DMode.SATELLITE], [Map3DMode.HYBRID], or [Map3DMode.ROADMAP]).
     *
     * Marks [hasUserOverriddenMapMode] as `true` for valid modes so the user's choice persists
     * across samples. Invalid or unknown modes are ignored.
     */
    fun setMapMode(@Map3DMode mode: Int): Map3DSettings {
        val sanitized = sanitizeMapMode(mode) ?: return _settings.value
        hasUserOverriddenMapMode = true
        if (sanitized == _settings.value.mapMode) {
            return _settings.value
        }
        _settings.value = _settings.value.copy(mapMode = sanitized)
        notifyListeners()
        return _settings.value
    }

    /**
     * Updates the color scheme ([Map3DColorScheme.FOLLOW_SYSTEM], [Map3DColorScheme.LIGHT], or [Map3DColorScheme.DARK]).
     */
    fun setColorScheme(scheme: Map3DColorScheme): Map3DSettings {
        if (scheme == _settings.value.colorScheme) {
            return _settings.value
        }
        _settings.value = _settings.value.copy(colorScheme = scheme)
        notifyListeners()
        return _settings.value
    }

    /**
     * Updates the UI controls visibility configuration.
     */
    fun setUiControls(uiControls: Map3DUiControls): Map3DSettings {
        if (uiControls == _settings.value.uiControls) {
            return _settings.value
        }
        _settings.value = _settings.value.copy(uiControls = uiControls)
        notifyListeners()
        return _settings.value
    }

    /**
     * Toggles all UI controls on or off simultaneously.
     */
    fun setAllUiControls(enabled: Boolean): Map3DSettings {
        return setUiControls(_settings.value.uiControls.withAll(enabled))
    }

    /**
     * Updates a single UI control's visibility.
     */
    fun updateUiControl(
        zoom: Boolean = _settings.value.uiControls.zoom,
        tilt: Boolean = _settings.value.uiControls.tilt,
        rotate: Boolean = _settings.value.uiControls.rotate,
        compass: Boolean = _settings.value.uiControls.compass,
        panVertical: Boolean = _settings.value.uiControls.panVertical,
        panHorizontal: Boolean = _settings.value.uiControls.panHorizontal,
    ): Map3DSettings {
        val updatedControls = _settings.value.uiControls.copy(
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
        if (newSettings == _settings.value) {
            return _settings.value
        }
        val sanitizedMode = sanitizeMapMode(newSettings.mapMode) ?: _settings.value.mapMode
        if (sanitizedMode != _settings.value.mapMode) {
            hasUserOverriddenMapMode = true
        }
        _settings.value = Map3DSettings(
            mapMode = sanitizedMode,
            colorScheme = newSettings.colorScheme,
            uiControls = newSettings.uiControls,
        )
        notifyListeners()
        return _settings.value
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
        val currentSnapshot = _settings.value
        for (listener in listeners.toList()) {
            listener(currentSnapshot)
        }
    }
}
// [END maps_3d_quick_settings_controller]
