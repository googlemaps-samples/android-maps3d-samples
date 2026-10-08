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

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.app.AlertDialog
import com.example.maps3dcommon.R
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.material.floatingactionbutton.FloatingActionButton

// [START maps_3d_quick_settings_button]
/**
 * A self-contained settings button overlay for 3D map activities.
 *
 * Can be positioned anywhere in a layout (e.g., top-end or bottom-end of the map).
 * Clicking the button opens a modal [Map3DQuickSettingsDialog] allowing the user
 * to switch map modes (Satellite, Hybrid, Roadmap) and color schemes (System, Light, Dark).
 *
 * When attached to a [GoogleMap3D] instance via [attachMap], changes are automatically
 * applied in real time to the 3D map.
 */
class Map3DQuickSettingsButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.floatingActionButtonStyle,
) : FloatingActionButton(context, attrs, defStyleAttr) {

    fun interface OnSettingsChangedListener {
        fun onSettingsChanged(settings: Map3DSettings)
    }

    private val controller = Map3DSettingsController()
    private var attachedMap: GoogleMap3D? = null
    private var mapProvider: (() -> GoogleMap3D?)? = null
    private var activeDialog: AlertDialog? = null
    private var onSettingsChangedListener: OnSettingsChangedListener? = null

    init {
        setImageResource(R.drawable.settings_24px)
        contentDescription = context.getString(R.string.map3d_settings_button_description)
        size = SIZE_MINI

        controller.addListener { updatedSettings ->
            val map = attachedMap ?: mapProvider?.invoke()
            map?.let { updatedSettings.applyTo(it) }
            onSettingsChangedListener?.onSettingsChanged(updatedSettings)
        }

        setOnClickListener {
            showDialog()
        }
    }

    /**
     * Attaches a [GoogleMap3D] instance.
     *
     * Settings changes will automatically be applied to this map instance.
     */
    fun attachMap(googleMap3D: GoogleMap3D?) {
        this.attachedMap = googleMap3D
        if (googleMap3D != null) {
            controller.getSettings().applyTo(googleMap3D)
        }
    }

    /**
     * Sets a provider lambda that resolves the active [GoogleMap3D] instance on demand.
     */
    fun setMapProvider(provider: () -> GoogleMap3D?) {
        this.mapProvider = provider
    }

    /** Returns the current [Map3DSettings] snapshot. */
    fun getSettings(): Map3DSettings = controller.getSettings()

    /** Programmatically sets new [Map3DSettings]. */
    fun setSettings(settings: Map3DSettings) {
        controller.setSettings(settings)
    }

    /** Sets a callback invoked whenever the user updates settings in the dialog. */
    fun setOnSettingsChangedListener(listener: OnSettingsChangedListener) {
        this.onSettingsChangedListener = listener
    }

    /** Displays the [Map3DQuickSettingsDialog] modal. */
    fun showDialog() {
        val map = attachedMap ?: mapProvider?.invoke()
        activeDialog?.dismiss()
        activeDialog = Map3DQuickSettingsDialog.show(
            context = context,
            controller = controller,
            googleMap3D = map,
            onDismiss = { activeDialog = null },
        )
    }
}
// [END maps_3d_quick_settings_button]
