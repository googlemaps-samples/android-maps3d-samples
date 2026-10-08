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

import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.UiControlSettings.ControlPlacement
import com.google.android.gms.maps3d.model.CompassControl
import com.google.android.gms.maps3d.model.Map3DControl
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.PanHorizontalControl
import com.google.android.gms.maps3d.model.PanVerticalControl
import com.google.android.gms.maps3d.model.RotateControl
import com.google.android.gms.maps3d.model.Spacer
import com.google.android.gms.maps3d.model.TiltControl
import com.google.android.gms.maps3d.model.ZoomControl

// [START maps_3d_quick_settings_model]
/**
 * Supported color schemes for 3D map and sample UI styling.
 */
enum class Map3DColorScheme(val value: Int) {
    FOLLOW_SYSTEM(2),
    LIGHT(0),
    DARK(1);

    companion object {
        fun fromValue(value: Int): Map3DColorScheme {
            return entries.firstOrNull { it.value == value } ?: FOLLOW_SYSTEM
        }
    }
}

// [START maps_3d_quick_settings_ui_controls_model]
/**
 * Visibility state for built-in 3D camera UI controls.
 *
 * @property zoom Zoom in/out (+ / -) control on trailing edge.
 * @property tilt Pitch up/down control on trailing edge.
 * @property rotate Heading rotation (CW / CCW) control on trailing edge.
 * @property compass North orientation indicator & reset control on trailing edge.
 * @property panVertical North/South pan control on leading edge.
 * @property panHorizontal East/West pan control on leading edge.
 */
data class Map3DUiControls(
    val zoom: Boolean = true,
    val tilt: Boolean = true,
    val rotate: Boolean = true,
    val compass: Boolean = true,
    val panVertical: Boolean = true,
    val panHorizontal: Boolean = true,
) {
    val allEnabled: Boolean
        get() = zoom && tilt && rotate && compass && panVertical && panHorizontal

    val noneEnabled: Boolean
        get() = !zoom && !tilt && !rotate && !compass && !panVertical && !panHorizontal

    fun withAll(enabled: Boolean): Map3DUiControls = copy(
        zoom = enabled,
        tilt = enabled,
        rotate = enabled,
        compass = enabled,
        panVertical = enabled,
        panHorizontal = enabled,
    )
}
// [END maps_3d_quick_settings_ui_controls_model]

/**
 * Immutable configuration snapshot for 3D map-level settings.
 *
 * Encapsulates global rendering options that apply to the 3D map surface itself
 * rather than individual scene objects (markers, polylines, or models).
 *
 * @property mapMode The 3D map rendering mode: [Map3DMode.HYBRID], [Map3DMode.SATELLITE], or [Map3DMode.ROADMAP].
 * @property colorScheme The map color scheme: [Map3DColorScheme.FOLLOW_SYSTEM], [Map3DColorScheme.LIGHT], or [Map3DColorScheme.DARK].
 * @property uiControls Visibility state for built-in 3D camera UI controls.
 */
data class Map3DSettings(
    @Map3DMode val mapMode: Int = Map3DMode.HYBRID,
    val colorScheme: Map3DColorScheme = Map3DColorScheme.FOLLOW_SYSTEM,
    val uiControls: Map3DUiControls = Map3DUiControls(),
) {
    /**
     * Applies this configuration to a live [GoogleMap3D] instance and updates app day/night theme.
     */
    fun applyTo(googleMap3D: GoogleMap3D) {
        googleMap3D.setMapMode(mapMode)

        // Apply dark / light mode via AppCompatDelegate
        val nightMode = when (colorScheme) {
            Map3DColorScheme.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            Map3DColorScheme.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            Map3DColorScheme.FOLLOW_SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        if (AppCompatDelegate.getDefaultNightMode() != nightMode) {
            AppCompatDelegate.setDefaultNightMode(nightMode)
        }

        // Forward to SDK delegate if available at runtime
        try {
            val method = googleMap3D.javaClass.getMethod(
                "setMapColorScheme",
                Int::class.javaPrimitiveType,
            )
            method.invoke(googleMap3D, colorScheme.value)
        } catch (_: NoSuchMethodException) {
            // Unexported in current SDK RC client stub; handled by AppCompatDelegate
        } catch (e: Exception) {
            Log.w("Map3DSettings", "Could not apply colorScheme directly to GoogleMap3D", e)
        }

        // Apply built-in UI controls configuration
        try {
            googleMap3D.getUiControlSettings()?.let { settings ->
                val trailingControls = mutableListOf<Map3DControl>()
                if (uiControls.zoom) trailingControls.add(ZoomControl(null))
                if (uiControls.tilt) trailingControls.add(TiltControl(null))
                if (uiControls.rotate) trailingControls.add(RotateControl(null))
                if (uiControls.compass) {
                    if (trailingControls.isNotEmpty()) {
                        trailingControls.add(Spacer())
                    }
                    trailingControls.add(CompassControl())
                }
                settings.setUiControlsPlacement(trailingControls, ControlPlacement.TRAILING)

                val leadingControls = mutableListOf<Map3DControl>()
                if (uiControls.panVertical) leadingControls.add(PanVerticalControl(null))
                if (uiControls.panHorizontal) leadingControls.add(PanHorizontalControl(null))
                settings.setUiControlsPlacement(leadingControls, ControlPlacement.LEADING)
            }
        } catch (e: Throwable) {
            Log.e("Map3DSettings", "Could not apply UI controls placement to GoogleMap3D", e)
        }
    }
}
// [END maps_3d_quick_settings_model]
