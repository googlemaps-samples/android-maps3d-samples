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

package com.example.maps3djava.mapslocalization;

import android.os.Bundle;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import com.example.maps3d.common.LocalizationDataKt;
import com.example.maps3d.common.LocalizationState;
import com.example.maps3d.common.MapLocalePreset;
import com.example.maps3d.common.MapsLocalizationViewModel;
import com.example.maps3dcommon.R;
import com.example.maps3djava.sampleactivity.SampleBaseActivity;
import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.Map3DView;
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.Map3DMode;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.List;

/**
 * Java Views implementation of the 3D Map Localization sample (`language` and `region` via {@link
 * com.google.android.gms.maps3d.model.LocaleOptions} and {@link GoogleMap3D#setLocale}).
 *
 * <p>Dynamically updates the map language and region in-place on the live {@link GoogleMap3D}
 * instance without recreating {@link Map3DView} or reloading the map scene.
 */
public class MapsLocalizationActivity extends SampleBaseActivity {

    private static final String TAG = "MapsLocalizationActivity";

    @Override
    public Camera getInitialCamera() {
        return LocalizationDataKt.getDEFAULT_LOCALIZATION_CAMERA();
    }

    @Override
    public String getTAG() {
        return TAG;
    }

    private MapsLocalizationViewModel viewModel;

    private LinearLayout collapsibleContent;
    private MaterialButton collapseToggleButton;
    private MaterialButton changeLanguageButton;
    private RadioGroup mapModeRadioGroup;

    private boolean isCollapsed = false;
    private boolean isUpdatingUiFromState = false;
    private MapLocalePreset activePreset = MapLocalePreset.ENGLISH_US;
    private int activeMapMode = Map3DMode.HYBRID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(MapsLocalizationViewModel.class);

        ConstraintLayout rootContainer = findViewById(R.id.map_container);
        View controlPanel =
                LayoutInflater.from(this)
                        .inflate(R.layout.control_panel_maps_localization, rootContainer, false);
        rootContainer.addView(controlPanel);

        ViewCompat.setOnApplyWindowInsetsListener(
                controlPanel,
                (view, windowInsets) -> {
                    Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                    ConstraintLayout.LayoutParams layoutParams =
                            (ConstraintLayout.LayoutParams) view.getLayoutParams();
                    int margin12dp = (int) (12 * getResources().getDisplayMetrics().density);
                    layoutParams.bottomMargin = insets.bottom + margin12dp;
                    view.setLayoutParams(layoutParams);
                    return windowInsets;
                });

        bindControlPanelViews(controlPanel);
        setupControlListeners();
        observeViewModel();
    }


    @Override
    public void onMap3DViewReady(@NonNull GoogleMap3D googleMap3D) {
        super.onMap3DViewReady(googleMap3D);
        LocalizationState state = viewModel.getCurrentState();
        activePreset = state.getSelectedPreset();
        activeMapMode = state.getMapMode();
        googleMap3D.setMapMode(state.getMapMode());
        googleMap3D.setLocale(state.toLocaleOptions());
    }

    private void bindControlPanelViews(View panel) {
        collapsibleContent = panel.findViewById(R.id.card_content);
        collapseToggleButton = panel.findViewById(R.id.btn_collapse);
        changeLanguageButton = panel.findViewById(R.id.btn_change_language);
        mapModeRadioGroup = panel.findViewById(R.id.rg_localization_map_mode);

        panel.findViewById(R.id.card_header).setOnClickListener(v -> togglePanelCollapse());
        collapseToggleButton.setOnClickListener(v -> togglePanelCollapse());
    }

    private void setupControlListeners() {
        changeLanguageButton.setOnClickListener(v -> showLanguageSelectionDialog());

        mapModeRadioGroup.setOnCheckedChangeListener(
                (group, checkedId) -> {
                    if (isUpdatingUiFromState) {
                        return;
                    }
                    int targetMode =
                            (checkedId == R.id.rb_mode_roadmap)
                                    ? Map3DMode.ROADMAP
                                    : Map3DMode.HYBRID;
                    viewModel.setMapMode(targetMode);
                });
    }

    private void showLanguageSelectionDialog() {
        List<MapLocalePreset> presets = MapLocalePreset.getEntries();
        String[] labels = new String[presets.size()];
        for (int i = 0; i < presets.size(); i++) {
            MapLocalePreset preset = presets.get(i);
            labels[i] = preset.getDisplayName() + " — " + preset.getNativeLabel();
        }

        MapLocalePreset currentPreset = viewModel.getCurrentState().getSelectedPreset();
        int currentIndex = Math.max(0, presets.indexOf(currentPreset));

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.change_language_button)
                .setSingleChoiceItems(
                        labels,
                        currentIndex,
                        (dialog, which) -> {
                            MapLocalePreset chosenPreset = presets.get(which);
                            dialog.dismiss();
                            if (chosenPreset != currentPreset) {
                                viewModel.selectPreset(chosenPreset);
                            }
                        })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void observeViewModel() {
        viewModel.getLocalizationStateData()
                .observe(
                    this,
                    state -> {
                        renderUiState(state);
                        GoogleMap3D map = this.googleMap3D;
                        if (map != null) {
                            applyStateToMap(map, state);
                        }
                    });
    }

    private void renderUiState(LocalizationState state) {
        isUpdatingUiFromState = true;
        try {
            changeLanguageButton.setText(
                    getString(
                            R.string.language_button_format,
                            state.getSelectedPreset().getDisplayName(),
                            state.getLocaleBadgeText()));

            int expectedRadioId =
                    (state.getMapMode() == Map3DMode.ROADMAP)
                            ? R.id.rb_mode_roadmap
                            : R.id.rb_mode_hybrid;
            if (mapModeRadioGroup.getCheckedRadioButtonId() != expectedRadioId) {
                mapModeRadioGroup.check(expectedRadioId);
            }
        } finally {
            isUpdatingUiFromState = false;
        }
    }

    private void applyStateToMap(GoogleMap3D map, LocalizationState state) {
        if (state.getMapMode() != activeMapMode) {
            activeMapMode = state.getMapMode();
            map.setMapMode(state.getMapMode());
        }

        if (state.getSelectedPreset() != activePreset) {
            activePreset = state.getSelectedPreset();
            map.setLocale(state.toLocaleOptions());
            Toast.makeText(
                            this,
                            getString(
                                    R.string.localization_applied_format,
                                    state.getSelectedPreset().getDisplayName(),
                                    state.getLocaleBadgeText()),
                            Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void togglePanelCollapse() {
        ConstraintLayout rootContainer = findViewById(R.id.map_container);
        TransitionManager.beginDelayedTransition(rootContainer);
        isCollapsed = !isCollapsed;
        collapsibleContent.setVisibility(isCollapsed ? View.GONE : View.VISIBLE);
        collapseToggleButton.setIconResource(
                isCollapsed ? R.drawable.expand_less_24px : R.drawable.expand_more_24px);
    }
}
