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

package com.example.maps3dkotlin.mapslocalization

import android.os.Bundle
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.maps3d.common.DEFAULT_LOCALIZATION_CAMERA
import com.example.maps3d.common.LocalizationState
import com.example.maps3d.common.MapLocalePreset
import com.example.maps3d.common.MapsLocalizationViewModel
import com.example.maps3dcommon.R
import com.example.maps3dkotlin.sampleactivity.SampleBaseActivity
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

/**
 * Demonstrates 3D Map Localization (`language` and `region` via
 * [com.google.android.gms.maps3d.model.LocaleOptions] and [GoogleMap3D.setLocale]).
 *
 * Dynamically updates the map language and region in-place on the live [GoogleMap3D] instance
 * without recreating `Map3DView` or reloading the map scene.
 */
class MapsLocalizationActivity : SampleBaseActivity() {

    override val TAG = "MapsLocalizationActivity"
    override val initialCamera: Camera = DEFAULT_LOCALIZATION_CAMERA

    private val viewModel: MapsLocalizationViewModel by viewModels()

    private lateinit var collapsibleContent: LinearLayout
    private lateinit var collapseToggleButton: MaterialButton
    private lateinit var changeLanguageButton: MaterialButton
    private lateinit var mapModeRadioGroup: RadioGroup

    private var isCollapsed = false
    private var isUpdatingUiFromState = false
    private var activePreset: MapLocalePreset = MapLocalePreset.ENGLISH_US
    private var activeMapMode: Int = Map3DMode.HYBRID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootContainer = findViewById<ConstraintLayout>(R.id.map_container)
        val controlPanel = LayoutInflater.from(this).inflate(
            R.layout.control_panel_maps_localization,
            rootContainer,
            false,
        )
        rootContainer.addView(controlPanel)

        ViewCompat.setOnApplyWindowInsetsListener(controlPanel) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val layoutParams = view.layoutParams as ConstraintLayout.LayoutParams
            val margin12dp = (12 * resources.displayMetrics.density).toInt()
            layoutParams.bottomMargin = insets.bottom + margin12dp
            view.layoutParams = layoutParams
            windowInsets
        }

        bindControlPanelViews(controlPanel)
        setupControlListeners()
        observeViewModel()
    }

    override fun onMapReady(googleMap3D: GoogleMap3D) {
        super.onMapReady(googleMap3D)
        val state = viewModel.uiState.value
        activePreset = state.selectedPreset
        activeMapMode = state.mapMode
        googleMap3D.setMapMode(state.mapMode)
        googleMap3D.setLocale(state.toLocaleOptions())
    }

    private fun bindControlPanelViews(panel: View) {
        collapsibleContent = panel.findViewById(R.id.card_content)
        collapseToggleButton = panel.findViewById(R.id.btn_collapse)
        changeLanguageButton = panel.findViewById(R.id.btn_change_language)
        mapModeRadioGroup = panel.findViewById(R.id.rg_localization_map_mode)

        panel.findViewById<View>(R.id.card_header).setOnClickListener {
            togglePanelCollapse()
        }
        collapseToggleButton.setOnClickListener {
            togglePanelCollapse()
        }
    }

    private fun setupControlListeners() {
        changeLanguageButton.setOnClickListener {
            showLanguageSelectionDialog()
        }

        mapModeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (isUpdatingUiFromState) return@setOnCheckedChangeListener
            val targetMode = if (checkedId == R.id.rb_mode_roadmap) {
                Map3DMode.ROADMAP
            } else {
                Map3DMode.HYBRID
            }
            viewModel.setMapMode(targetMode)
        }
    }

    private fun showLanguageSelectionDialog() {
        val presets = MapLocalePreset.entries
        val labels = presets.map { "${it.displayName} — ${it.nativeLabel}" }.toTypedArray()
        val currentState = viewModel.uiState.value
        val currentIndex = presets.indexOf(currentState.selectedPreset).coerceAtLeast(0)

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.change_language_button)
            .setSingleChoiceItems(labels, currentIndex) { dialog, which ->
                val chosenPreset = presets[which]
                dialog.dismiss()
                if (chosenPreset != currentState.selectedPreset) {
                    viewModel.selectPreset(chosenPreset)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    renderUiState(state)
                    val map = googleMap3D
                    if (map != null) {
                        applyStateToMap(map, state)
                    }
                }
            }
        }
    }

    private fun renderUiState(state: LocalizationState) {
        isUpdatingUiFromState = true
        try {
            changeLanguageButton.text = getString(
                R.string.language_button_format,
                state.selectedPreset.displayName,
                state.localeBadgeText,
            )

            val expectedRadioId = if (state.mapMode == Map3DMode.ROADMAP) {
                R.id.rb_mode_roadmap
            } else {
                R.id.rb_mode_hybrid
            }
            if (mapModeRadioGroup.checkedRadioButtonId != expectedRadioId) {
                mapModeRadioGroup.check(expectedRadioId)
            }
        } finally {
            isUpdatingUiFromState = false
        }
    }

    private fun applyStateToMap(map: GoogleMap3D, state: LocalizationState) {
        if (state.mapMode != activeMapMode) {
            activeMapMode = state.mapMode
            map.setMapMode(state.mapMode)
        }

        if (state.selectedPreset != activePreset) {
            activePreset = state.selectedPreset
            map.setLocale(state.toLocaleOptions())
            Toast.makeText(
                this,
                getString(
                    R.string.localization_applied_format,
                    state.selectedPreset.displayName,
                    state.localeBadgeText,
                ),
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    private fun togglePanelCollapse() {
        val rootContainer = findViewById<ConstraintLayout>(R.id.map_container)
        TransitionManager.beginDelayedTransition(rootContainer)
        isCollapsed = !isCollapsed
        collapsibleContent.visibility = if (isCollapsed) View.GONE else View.VISIBLE
        collapseToggleButton.setIconResource(
            if (isCollapsed) R.drawable.expand_less_24px else R.drawable.expand_more_24px,
        )
    }
}
