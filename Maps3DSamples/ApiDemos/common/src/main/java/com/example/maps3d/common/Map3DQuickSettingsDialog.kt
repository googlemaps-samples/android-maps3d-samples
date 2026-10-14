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
import android.widget.Button
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
 * and Color Scheme (Follow System, Light, Dark), plus toggles for built-in camera UI controls
 * and a Reset to Defaults action.
 * Directly applies changes to [GoogleMap3D] and app theme for instant live feedback.
 */
object Map3DQuickSettingsDialog {

    /**
     * Builds and displays the modal [AlertDialog].
     *
     * @param context Host Android UI [Context].
     * @param controller The [Map3DSettingsController] managing settings state.
     * @param googleMap3D Optional [GoogleMap3D] instance to apply settings immediately.
     * @param sampleDefaultMapMode The hosting sample's default [Map3DMode] used when resetting.
     * @param onDismiss Optional callback invoked when the dialog is closed.
     * @return The displayed [AlertDialog] instance.
     */
    fun show(
        context: Context,
        controller: Map3DSettingsController = Map3DSettingsController.shared,
        googleMap3D: GoogleMap3D? = null,
        @Map3DMode sampleDefaultMapMode: Int = Map3DMode.HYBRID,
        onDismiss: (() -> Unit)? = null,
    ): AlertDialog {
        val view = LayoutInflater.from(context).inflate(
            R.layout.dialog_map3d_quick_settings,
            null,
            false,
        )

        val rgMapMode = view.findViewById<RadioGroup>(R.id.rg_map_mode)
        val rgColorScheme = view.findViewById<RadioGroup>(R.id.rg_color_scheme)
        val switchAll = view.findViewById<MaterialSwitch>(R.id.switch_toggle_all_controls)
        val cbZoom = view.findViewById<MaterialCheckBox>(R.id.cb_control_zoom)
        val cbTilt = view.findViewById<MaterialCheckBox>(R.id.cb_control_tilt)
        val cbRotate = view.findViewById<MaterialCheckBox>(R.id.cb_control_rotate)
        val cbCompass = view.findViewById<MaterialCheckBox>(R.id.cb_control_compass)
        val cbPanV = view.findViewById<MaterialCheckBox>(R.id.cb_control_pan_v)
        val cbPanH = view.findViewById<MaterialCheckBox>(R.id.cb_control_pan_h)
        val btnReset = view.findViewById<Button>(R.id.btn_reset_settings)

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

        fun syncAllControls(settings: Map3DSettings) {
            isUpdatingUi = true
            val modeId = when (settings.mapMode) {
                Map3DMode.SATELLITE -> R.id.rb_mode_satellite
                Map3DMode.ROADMAP -> R.id.rb_mode_roadmap
                else -> R.id.rb_mode_hybrid
            }
            rgMapMode.check(modeId)

            val schemeId = when (settings.colorScheme) {
                Map3DColorScheme.LIGHT -> R.id.rb_scheme_light
                Map3DColorScheme.DARK -> R.id.rb_scheme_dark
                else -> R.id.rb_scheme_system
            }
            rgColorScheme.check(schemeId)
            isUpdatingUi = false

            syncCheckboxes(settings.uiControls)
        }

        syncAllControls(controller.getSettings())

        // Listen for Map Mode changes
        rgMapMode.setOnCheckedChangeListener { _, checkedId ->
            if (isUpdatingUi) return@setOnCheckedChangeListener
            val newMode = when (checkedId) {
                R.id.rb_mode_satellite -> Map3DMode.SATELLITE
                R.id.rb_mode_roadmap -> Map3DMode.ROADMAP
                else -> Map3DMode.HYBRID
            }
            controller.setMapMode(newMode)
        }

        // Listen for Color Scheme changes
        rgColorScheme.setOnCheckedChangeListener { _, checkedId ->
            if (isUpdatingUi) return@setOnCheckedChangeListener
            val newScheme = when (checkedId) {
                R.id.rb_scheme_light -> Map3DColorScheme.LIGHT
                R.id.rb_scheme_dark -> Map3DColorScheme.DARK
                else -> Map3DColorScheme.FOLLOW_SYSTEM
            }
            controller.setColorScheme(newScheme)
        }

        // Master switch toggle
        switchAll.setOnCheckedChangeListener { _, isChecked ->
            if (isUpdatingUi) return@setOnCheckedChangeListener
            val updated = controller.setAllUiControls(isChecked)
            syncCheckboxes(updated.uiControls)
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
        }

        cbZoom.setOnCheckedChangeListener { _, _ -> onIndividualControlToggled() }
        cbTilt.setOnCheckedChangeListener { _, _ -> onIndividualControlToggled() }
        cbRotate.setOnCheckedChangeListener { _, _ -> onIndividualControlToggled() }
        cbCompass.setOnCheckedChangeListener { _, _ -> onIndividualControlToggled() }
        cbPanV.setOnCheckedChangeListener { _, _ -> onIndividualControlToggled() }
        cbPanH.setOnCheckedChangeListener { _, _ -> onIndividualControlToggled() }

        btnReset?.setOnClickListener {
            val resetSettings = controller.reset(sampleDefaultMapMode)
            syncAllControls(resetSettings)
        }

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
