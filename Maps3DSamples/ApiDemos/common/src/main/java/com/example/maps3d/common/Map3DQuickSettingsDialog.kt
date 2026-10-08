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
import android.view.LayoutInflater
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import com.example.maps3dcommon.R
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch

// [START maps_3d_quick_settings_dialog]
/**
 * Modal dialog for inspecting and adjusting 3D map settings.
 *
 * Provides single-choice radio controls for Map Mode (Satellite, Hybrid, Roadmap)
 * and Color Scheme (Follow System, Light, Dark).
 * Directly applies changes to [GoogleMap3D] and app theme for instant live feedback.
 */
object Map3DQuickSettingsDialog {

    /**
     * Builds and displays the modal [AlertDialog].
     *
     * @param context Host Android UI [Context].
     * @param controller The [Map3DSettingsController] managing settings state.
     * @param googleMap3D Optional [GoogleMap3D] instance to apply settings immediately.
     * @param onDismiss Optional callback invoked when the dialog is closed.
     * @return The displayed [AlertDialog] instance.
     */
    fun show(
        context: Context,
        controller: Map3DSettingsController,
        googleMap3D: GoogleMap3D? = null,
        onDismiss: (() -> Unit)? = null,
    ): AlertDialog {
        val view = LayoutInflater.from(context).inflate(
            R.layout.dialog_map3d_quick_settings,
            null,
            false,
        )

        val rgMapMode = view.findViewById<RadioGroup>(R.id.rg_map_mode)
        val rgColorScheme = view.findViewById<RadioGroup>(R.id.rg_color_scheme)
        val currentSettings = controller.getSettings()

        // Set initial Map Mode selection
        val initialModeId = when (currentSettings.mapMode) {
            Map3DMode.SATELLITE -> R.id.rb_mode_satellite
            Map3DMode.ROADMAP -> R.id.rb_mode_roadmap
            else -> R.id.rb_mode_hybrid
        }
        rgMapMode.check(initialModeId)

        // Set initial Color Scheme selection
        val initialSchemeId = when (currentSettings.colorScheme) {
            Map3DColorScheme.LIGHT -> R.id.rb_scheme_light
            Map3DColorScheme.DARK -> R.id.rb_scheme_dark
            else -> R.id.rb_scheme_system
        }
        rgColorScheme.check(initialSchemeId)

        // Listen for Map Mode changes
        rgMapMode.setOnCheckedChangeListener { _, checkedId ->
            val newMode = when (checkedId) {
                R.id.rb_mode_satellite -> Map3DMode.SATELLITE
                R.id.rb_mode_roadmap -> Map3DMode.ROADMAP
                else -> Map3DMode.HYBRID
            }
            val updated = controller.setMapMode(newMode)
            googleMap3D?.let { updated.applyTo(it) }
        }

        // Listen for Color Scheme changes
        rgColorScheme.setOnCheckedChangeListener { _, checkedId ->
            val newScheme = when (checkedId) {
                R.id.rb_scheme_light -> Map3DColorScheme.LIGHT
                R.id.rb_scheme_dark -> Map3DColorScheme.DARK
                else -> Map3DColorScheme.FOLLOW_SYSTEM
            }
            val updated = controller.setColorScheme(newScheme)
            googleMap3D?.let { updated.applyTo(it) }
        }

        // Camera UI Controls Checkboxes & Master Switch
        val switchAll = view.findViewById<MaterialSwitch>(R.id.switch_toggle_all_controls)
        val cbZoom = view.findViewById<MaterialCheckBox>(R.id.cb_control_zoom)
        val cbTilt = view.findViewById<MaterialCheckBox>(R.id.cb_control_tilt)
        val cbRotate = view.findViewById<MaterialCheckBox>(R.id.cb_control_rotate)
        val cbCompass = view.findViewById<MaterialCheckBox>(R.id.cb_control_compass)
        val cbPanV = view.findViewById<MaterialCheckBox>(R.id.cb_control_pan_v)
        val cbPanH = view.findViewById<MaterialCheckBox>(R.id.cb_control_pan_h)

        var isUpdatingUi = false
        fun syncCheckboxes(ui: Map3DUiControls) {
            isUpdatingUi = true
            switchAll.isChecked = ui.allEnabled
            cbZoom.isChecked = ui.zoom
            cbTilt.isChecked = ui.tilt
            cbRotate.isChecked = ui.rotate
            cbCompass.isChecked = ui.compass
            cbPanV.isChecked = ui.panVertical
            cbPanH.isChecked = ui.panHorizontal
            isUpdatingUi = false
        }
        syncCheckboxes(currentSettings.uiControls)

        // Master switch toggle
        switchAll.setOnCheckedChangeListener { _, isChecked ->
            if (isUpdatingUi) return@setOnCheckedChangeListener
            val updated = controller.setAllUiControls(isChecked)
            syncCheckboxes(updated.uiControls)
            googleMap3D?.let { updated.applyTo(it) }
        }

        // Helper for individual checkboxes
        fun onIndividualControlToggled() {
            if (isUpdatingUi) return
            val updated = controller.updateUiControl(
                zoom = cbZoom.isChecked,
                tilt = cbTilt.isChecked,
                rotate = cbRotate.isChecked,
                compass = cbCompass.isChecked,
                panVertical = cbPanV.isChecked,
                panHorizontal = cbPanH.isChecked,
            )
            isUpdatingUi = true
            switchAll.isChecked = updated.uiControls.allEnabled
            isUpdatingUi = false
            googleMap3D?.let { updated.applyTo(it) }
        }

        cbZoom.setOnCheckedChangeListener { _, _ -> onIndividualControlToggled() }
        cbTilt.setOnCheckedChangeListener { _, _ -> onIndividualControlToggled() }
        cbRotate.setOnCheckedChangeListener { _, _ -> onIndividualControlToggled() }
        cbCompass.setOnCheckedChangeListener { _, _ -> onIndividualControlToggled() }
        cbPanV.setOnCheckedChangeListener { _, _ -> onIndividualControlToggled() }
        cbPanH.setOnCheckedChangeListener { _, _ -> onIndividualControlToggled() }

        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle(R.string.map3d_settings_title)
            .setView(view)
            .setPositiveButton(R.string.map3d_settings_done) { d, _ ->
                d.dismiss()
            }
            .setOnDismissListener {
                onDismiss?.invoke()
            }
            .create()

        dialog.show()
        return dialog
    }
}
// [END maps_3d_quick_settings_dialog]
