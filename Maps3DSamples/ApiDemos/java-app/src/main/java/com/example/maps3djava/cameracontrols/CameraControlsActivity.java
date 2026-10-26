// Copyright 2025 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.maps3djava.cameracontrols;

import static com.example.maps3d.common.UtilitiesKt.toCompassDirection;
import static com.example.maps3d.common.UtilitiesKt.toValidCamera;
import static com.example.maps3d.common.UtilitiesKt.wrapIn;

import android.os.Bundle;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.example.maps3d.common.Map3DSettings;
import com.example.maps3d.common.Map3DSettingsController;
import com.example.maps3dcommon.R;
import com.example.maps3djava.sampleactivity.SampleBaseActivity;
import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.model.AltitudeMode;
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.FlyAroundOptions;
import com.google.android.gms.maps3d.model.FlyToOptions;
import com.google.android.gms.maps3d.model.LatLngAltitude;
import com.google.android.gms.maps3d.model.Map3DMode;
import com.google.android.gms.maps3d.model.Marker;
import com.google.android.gms.maps3d.model.MarkerOptions;
import com.google.android.gms.maps3d.model.Polygon;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.List;

public class CameraControlsActivity extends SampleBaseActivity {
    private static final String STATE_SELECTED_ALTITUDE_MODE = "state_selected_altitude_mode";
    private static final String STATE_ACTIVE_TARGET_RES_ID = "state_active_target_res_id";
    private static final String STATE_IS_COLLAPSED = "state_is_collapsed";

    @Override
    public final String getTAG() {
        return this.getClass().getSimpleName();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_camera_controls;
    }

    private TextView cameraStateText;
    private Slider rollSlider;
    private TextView rollSliderLabel;
    private boolean isSyncingRadioGroup = false;
    private boolean isCollapsed = false;

    private final List<Polygon> restrictionCubeFaces = new ArrayList<>();
    private Marker activeBeaconMarker = null;
    private int beaconRefreshToken = 0;
    private boolean needsMeshBeaconRefresh = false;

    @Map3DMode
    private int lastAppliedMapMode = Map3DMode.HYBRID;

    @AltitudeMode
    private int selectedAltitudeMode = AltitudeMode.RELATIVE_TO_GROUND;

    @StringRes
    private int activeTargetResId = R.string.camera_target_esb;

    @Override
    public final Camera getInitialCamera() {
        boolean isMountain = activeTargetResId == R.string.camera_target_mt_rainier;
        return resolveCameraForMapMode(
                DataModel.getTargetCamera(isMountain, selectedAltitudeMode),
                Map3DSettingsController.getShared().getSettings().getMapMode()
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            selectedAltitudeMode = savedInstanceState.getInt(
                    STATE_SELECTED_ALTITUDE_MODE,
                    AltitudeMode.RELATIVE_TO_GROUND
            );
            activeTargetResId = savedInstanceState.getInt(
                    STATE_ACTIVE_TARGET_RES_ID,
                    R.string.camera_target_esb
            );
            isCollapsed = savedInstanceState.getBoolean(STATE_IS_COLLAPSED, false);
        }
        super.onCreate(savedInstanceState);

        cameraStateText = findViewById(R.id.camera_state_text);

        rollSlider = findViewById(R.id.roll_slider);
        rollSliderLabel = findViewById(R.id.roll_slider_label);

        updateRollSliderLabel(rollSlider.getValue());

        ViewGroup cardContainer = findViewById(R.id.camera_state);
        View cardContent = findViewById(R.id.card_content);
        MaterialButton btnCollapse = findViewById(R.id.btn_collapse);
        if (isCollapsed) {
            if (btnCollapse != null) {
                btnCollapse.setIconResource(R.drawable.expand_less_24px);
                btnCollapse.setContentDescription(getString(R.string.expand_controls));
            }
            if (cardContent != null) {
                cardContent.setVisibility(View.GONE);
            }
        }
        View.OnClickListener toggleCollapse = view -> {
            isCollapsed = !isCollapsed;
            if (btnCollapse != null) {
                btnCollapse.setIconResource(
                        isCollapsed ? R.drawable.expand_less_24px : R.drawable.expand_more_24px
                );
                btnCollapse.setContentDescription(
                        getString(isCollapsed ? R.string.expand_controls : R.string.collapse_controls)
                );
            }
            if (cardContainer != null) {
                TransitionManager.beginDelayedTransition(cardContainer);
            }
            if (cardContent != null) {
                cardContent.setVisibility(isCollapsed ? View.GONE : View.VISIBLE);
            }
        };
        View cardHeader = findViewById(R.id.card_header);
        if (cardHeader != null) {
            cardHeader.setOnClickListener(toggleCollapse);
        }
        if (btnCollapse != null) {
            btnCollapse.setOnClickListener(toggleCollapse);
        }

        findViewById(R.id.fly_around).setOnClickListener(view -> flyAroundCurrentCenter());

        findViewById(R.id.fly_to).setOnClickListener(view -> flyToTarget(R.string.camera_target_esb));

        findViewById(R.id.fly_to_mountain_button).setOnClickListener(
                view -> flyToTarget(R.string.camera_target_mt_rainier)
        );

        ChipGroup altitudeModeChipGroup = findViewById(R.id.altitude_mode_chip_group);
        int initialChipId;
        if (selectedAltitudeMode == AltitudeMode.RELATIVE_TO_MESH) {
            initialChipId = R.id.chip_altitude_relative_to_mesh;
        } else if (selectedAltitudeMode == AltitudeMode.CLAMP_TO_GROUND) {
            initialChipId = R.id.chip_altitude_clamp_to_ground;
        } else if (selectedAltitudeMode == AltitudeMode.ABSOLUTE) {
            initialChipId = R.id.chip_altitude_absolute;
        } else {
            initialChipId = R.id.chip_altitude_relative_to_ground;
        }
        if (altitudeModeChipGroup.getCheckedChipId() != initialChipId) {
            altitudeModeChipGroup.check(initialChipId);
        }
        altitudeModeChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                return;
            }
            int checkedId = checkedIds.get(0);
            int newMode;
            if (checkedId == R.id.chip_altitude_relative_to_ground) {
                newMode = AltitudeMode.RELATIVE_TO_GROUND;
            } else if (checkedId == R.id.chip_altitude_relative_to_mesh) {
                newMode = AltitudeMode.RELATIVE_TO_MESH;
            } else if (checkedId == R.id.chip_altitude_clamp_to_ground) {
                newMode = AltitudeMode.CLAMP_TO_GROUND;
            } else if (checkedId == R.id.chip_altitude_absolute) {
                newMode = AltitudeMode.ABSOLUTE;
            } else {
                return;
            }
            if (selectedAltitudeMode != newMode) {
                selectedAltitudeMode = newMode;
                flyToTarget(activeTargetResId);
            }
        });

        findViewById(R.id.toggle_restriction).setOnClickListener(view -> {
            MaterialButton button = (MaterialButton) view;
            setCameraRestricted(button.isChecked());
        });

        findViewById(R.id.show_restriction).setOnClickListener(view -> {
            MaterialButton button = (MaterialButton) view;

            if (button.isChecked()) {
                button.setText(getText(R.string.camera_hide_restriction));

                for (Polygon polygon : restrictionCubeFaces) {
                    polygon.remove();
                }
                restrictionCubeFaces.clear();

                DataModel.nycPolygonOptions.forEach(polygonOptions -> {
                    if (googleMap3D != null) {
                        Polygon polygon = googleMap3D.addPolygon(polygonOptions);
                        restrictionCubeFaces.add(polygon);
                    }
                });
            } else {
                button.setText(getText(R.string.camera_show_restriction));
                for (Polygon polygon : restrictionCubeFaces) {
                    polygon.remove();
                }
                restrictionCubeFaces.clear();
            }
        });

        ((RadioGroup) findViewById(R.id.map_mode_radio_group)).setOnCheckedChangeListener((group, checkedId) -> {
            if (isSyncingRadioGroup) {
                return;
            }
            int mode;
            if (checkedId == R.id.map_mode_roadmap) {
                mode = Map3DMode.ROADMAP;
            } else if (checkedId == R.id.map_mode_hybrid) {
                mode = Map3DMode.HYBRID;
            } else if (checkedId == R.id.map_mode_satellite) {
                mode = Map3DMode.SATELLITE;
            } else {
                return;
            }
            Map3DSettingsController.getShared().setMapMode(mode);
            if (googleMap3D != null) {
                Map3DSettingsController.getShared().getSettings().applyTo(googleMap3D);
                applyMapModeCameraPerspective(googleMap3D, mode);
            }
        });

        rollSlider.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                updateRollSliderLabel(value);
                updateMapRoll(value);
            }
        });

        findViewById(R.id.reset_roll_button).setOnClickListener(view -> {
            float resetValue = 0.0f;
            rollSlider.setValue(resetValue);
            updateRollSliderLabel(resetValue);
            updateMapRoll(resetValue);
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_ALTITUDE_MODE, selectedAltitudeMode);
        outState.putInt(STATE_ACTIVE_TARGET_RES_ID, activeTargetResId);
        outState.putBoolean(STATE_IS_COLLAPSED, isCollapsed);
    }

    @Override
    protected void onQuickSettingsChanged(@NonNull Map3DSettings settings) {
        RadioGroup group = findViewById(R.id.map_mode_radio_group);
        if (group == null) {
            return;
        }
        int targetId = View.NO_ID;
        int mapMode = settings.getMapMode();
        if (mapMode == Map3DMode.ROADMAP) {
            targetId = R.id.map_mode_roadmap;
        } else if (mapMode == Map3DMode.HYBRID) {
            targetId = R.id.map_mode_hybrid;
        } else if (mapMode == Map3DMode.SATELLITE) {
            targetId = R.id.map_mode_satellite;
        }
        if (group.getCheckedRadioButtonId() != targetId) {
            isSyncingRadioGroup = true;
            if (targetId == View.NO_ID) {
                group.clearCheck();
            } else {
                group.check(targetId);
            }
            isSyncingRadioGroup = false;
        }
        if (googleMap3D != null) {
            applyMapModeCameraPerspective(googleMap3D, mapMode);
        }
    }

    /**
     * Adapts a landmark {@link Camera} for the active {@link Map3DMode}: in 2D
     * {@link Map3DMode#ROADMAP} mode, the map surface is a flat {@code 0m} plane, so keeping
     * {@code tilt = 0.0} and {@code altitude = 0.0} prevents high-elevation targets (like Mount
     * Rainier at 4,392m) from projecting the camera sightline tens of kilometers away from the
     * landmark.
     */
    @NonNull
    private Camera resolveCameraForMapMode(@NonNull Camera baseCamera, @Map3DMode int mapMode) {
        if (mapMode == Map3DMode.ROADMAP) {
            return new Camera(
                    new LatLngAltitude(
                            baseCamera.getCenter().getLatitude(),
                            baseCamera.getCenter().getLongitude(),
                            DataModel.CLAMP_ALTITUDE_METERS
                    ),
                    baseCamera.getHeading(),
                    0.0,
                    baseCamera.getRoll(),
                    baseCamera.getRange(),
                    AltitudeMode.RELATIVE_TO_GROUND
            );
        }
        return baseCamera;
    }

    private void applyMapModeCameraPerspective(@NonNull GoogleMap3D map, @Map3DMode int mode) {
        int previousMode = lastAppliedMapMode;
        if (previousMode == mode) {
            return;
        }
        lastAppliedMapMode = mode;
        boolean isTransitioningRoadmap =
                previousMode == Map3DMode.ROADMAP || mode == Map3DMode.ROADMAP;
        if (!isTransitioningRoadmap) {
            return;
        }

        boolean isMountain = activeTargetResId == R.string.camera_target_mt_rainier;
        Camera targetCamera = resolveCameraForMapMode(
                DataModel.getTargetCamera(isMountain, selectedAltitudeMode),
                mode
        );
        map.setCameraAnimationEndListener(null);
        map.setCamera(targetCamera);
        updateCameraPosition(targetCamera);
        if (map3DView != null) {
            map3DView.postDelayed(() -> {
                if (!isDestroyed() && !isFinishing() && this.googleMap3D != null
                        && Map3DSettingsController.getShared().getSettings().getMapMode() == mode) {
                    this.googleMap3D.setCamera(targetCamera);
                    updateCameraPosition(targetCamera);
                }
            }, 250L);
        }
    }

    private void updateRollSliderLabel(float value) {
        rollSliderLabel.setText(getString(R.string.camera_roll_label_dynamic, value));
    }

    private void updateMapRoll(double newRoll) {
        if (googleMap3D != null && googleMap3D.getCamera() != null) {
            Camera currentCamera = toValidCamera(googleMap3D.getCamera());
            currentCamera.setRoll(newRoll);
            googleMap3D.setCamera(currentCamera);
        }
    }

    /**
     * Called when the {@link com.google.android.gms.maps3d.Map3DView} is ready to be used.
     *
     * @param googleMap3D The {@link GoogleMap3D} object representing the 3D map.
     */
    @Override
    protected void onMapReady(@NonNull GoogleMap3D googleMap3D) {
        Map3DSettingsController.getShared().onSampleEntered(getDefaultMapMode());
        lastAppliedMapMode = Map3DSettingsController.getShared().getSettings().getMapMode();
        super.onMapReady(googleMap3D);
        this.googleMap3D = googleMap3D;

        upsertActiveBeacon();

        googleMap3D.setOnMapSteadyListener(isSteady -> {
            if (isSteady) {
                if (map3DView != null) {
                    map3DView.setContentDescription("MapSteady");
                }
                // Workaround: surface-relative modes sample 3D tile height at marker creation time.
                // Refresh once destination or 3D mesh tiles are steady.
                if (needsMeshBeaconRefresh) {
                    needsMeshBeaconRefresh = false;
                    runOnUiThread(this::upsertActiveBeacon);
                }
            }
        });

        if (googleMap3D.getCamera() != null) {
            updateCameraPosition(googleMap3D.getCamera());
        }

        googleMap3D.setCameraChangedListener(this::updateCameraPosition);
    }

    private void setCameraRestricted(boolean enabled) {
        MaterialButton button = findViewById(R.id.toggle_restriction);
        if (button != null) {
            button.setChecked(enabled);
            button.setText(getText(
                    enabled ? R.string.camera_remove_restriction : R.string.camera_activate_restriction
            ));
        }
        if (googleMap3D != null) {
            googleMap3D.setCameraRestriction(enabled ? DataModel.nycCameraRestriction : null);
        }
    }

    @NonNull
    private String getBeaconLabel(boolean isMountain, @AltitudeMode int mode) {
        int resId;
        if (mode == AltitudeMode.RELATIVE_TO_MESH) {
            resId = isMountain ? R.string.mt_rainier_beacon_mesh_label : R.string.esb_beacon_mesh_label;
        } else if (mode == AltitudeMode.CLAMP_TO_GROUND) {
            resId = isMountain ? R.string.mt_rainier_beacon_clamp_label : R.string.esb_beacon_clamp_label;
        } else if (mode == AltitudeMode.ABSOLUTE) {
            resId = isMountain ? R.string.mt_rainier_beacon_absolute_label : R.string.esb_beacon_absolute_label;
        } else {
            resId = isMountain ? R.string.mt_rainier_beacon_label : R.string.esb_beacon_ground_label;
        }
        return getString(resId);
    }

    /**
     * Places or updates the altitude reference beacon for the active target using the unified
     * {@link AltitudeMode} builder.
     */
    private void upsertActiveBeacon() {
        if (googleMap3D == null) {
            return;
        }
        beaconRefreshToken++;
        boolean isMountain = activeTargetResId == R.string.camera_target_mt_rainier;
        MarkerOptions beaconOptions = DataModel.getTargetBeaconOptions(
                isMountain,
                getBeaconLabel(isMountain, selectedAltitudeMode),
                selectedAltitudeMode,
                beaconRefreshToken
        );
        if (activeBeaconMarker != null) {
            activeBeaconMarker.remove();
        }
        activeBeaconMarker = googleMap3D.addMarker(beaconOptions);
    }

    /**
     * Initiates a camera flight animation to the specified landmark target ({@link R.string#camera_target_esb}
     * or {@link R.string#camera_target_mt_rainier}) using the single unified {@link AltitudeMode} camera
     * and marker pipeline.
     */
    private void flyToTarget(@StringRes int targetResId) {
        if (googleMap3D == null) {
            return;
        }
        boolean isNewLandmark = activeTargetResId != targetResId;
        activeTargetResId = targetResId;
        boolean isMountain = targetResId == R.string.camera_target_mt_rainier;

        // Clear NYC bounds restriction if active so the camera can travel to Washington State
        if (isMountain) {
            setCameraRestricted(false);
        }

        needsMeshBeaconRefresh = selectedAltitudeMode != AltitudeMode.ABSOLUTE;
        upsertActiveBeacon();

        int activeMapMode = Map3DSettingsController.getShared().getSettings().getMapMode();
        Camera targetCamera = resolveCameraForMapMode(
                DataModel.getTargetCamera(isMountain, selectedAltitudeMode),
                activeMapMode
        );
        googleMap3D.setCameraAnimationEndListener(() -> {
            if (googleMap3D != null) {
                googleMap3D.setCameraAnimationEndListener(null);
                if (isNewLandmark || selectedAltitudeMode == AltitudeMode.RELATIVE_TO_MESH) {
                    runOnUiThread(() -> {
                        upsertActiveBeacon();
                        needsMeshBeaconRefresh = selectedAltitudeMode != AltitudeMode.ABSOLUTE;
                    });
                }
                if (isMountain && activeMapMode != Map3DMode.ROADMAP) {
                    FlyAroundOptions orbitOptions = new FlyAroundOptions(
                            targetCamera,
                            DataModel.MT_RAINIER_ORBIT_DURATION_MS,
                            DataModel.MT_RAINIER_ORBIT_ROUNDS
                    );
                    googleMap3D.flyCameraAround(orbitOptions);
                }
            }
        });

        long durationMs = isMountain
                ? DataModel.MT_RAINIER_FLY_TO_DURATION_MS
                : DataModel.ESB_FLY_TO_DURATION_MS;
        FlyToOptions flyToOptions = new FlyToOptions(targetCamera, durationMs);
        googleMap3D.flyCameraTo(flyToOptions);
    }

    /**
     * Initiates a camera fly-around animation around the current camera's center.
     */
    private void flyAroundCurrentCenter() {
        if (googleMap3D == null || googleMap3D.getCamera() == null) {
            return;
        }
        googleMap3D.setCameraAnimationEndListener(null);
        Camera camera = toValidCamera(googleMap3D.getCamera());

        FlyAroundOptions flyAroundOptions = new FlyAroundOptions(camera, 5_000, 1.0);

        googleMap3D.flyCameraAround(flyAroundOptions);
    }

    // Camera does not support CLAMP_TO_GROUND; it uses RELATIVE_TO_GROUND at 0m clearance.
    // In 2D ROADMAP mode, resolveCameraForMapMode also forces RELATIVE_TO_GROUND.
    @NonNull
    private String getCameraAltitudeModeLabel(@AltitudeMode int mode, @Map3DMode int mapMode) {
        if (mapMode == Map3DMode.ROADMAP
                || mode == AltitudeMode.RELATIVE_TO_GROUND
                || mode == AltitudeMode.CLAMP_TO_GROUND) {
            return getString(R.string.altitude_mode_relative_to_ground_code);
        } else if (mode == AltitudeMode.RELATIVE_TO_MESH) {
            return getString(R.string.altitude_mode_relative_to_mesh_code);
        } else {
            return getString(R.string.altitude_mode_absolute_code);
        }
    }

    /**
     * Updates the camera state text view with the current camera's position, active target,
     * and {@link AltitudeMode} telemetry.
     */
    private void updateCameraPosition(@NonNull Camera camera) {
        double heading = camera.getHeading() != null ? camera.getHeading() : 0.0;
        double tilt = camera.getTilt() != null ? camera.getTilt() : 0.0;
        double range = camera.getRange() != null ? camera.getRange() : 0.0;
        double roll = camera.getRoll() != null ? camera.getRoll() : 0.0;
        int activeMapMode = Map3DSettingsController.getShared().getSettings().getMapMode();

        String baseCameraState = getString(R.string.camera_state_format,
                camera.getCenter().getLatitude(),
                camera.getCenter().getLongitude(),
                camera.getCenter().getAltitude(),
                heading,
                toCompassDirection(heading),
                tilt,
                range
        );
        String targetAndMode = getString(
                R.string.camera_target_and_altitude_mode_format,
                getString(activeTargetResId),
                getCameraAltitudeModeLabel(selectedAltitudeMode, activeMapMode)
        );
        String cameraStateString = baseCameraState + "\n" + targetAndMode;

        float resetValue = (float) wrapIn(roll, -180f, 180f);

        // Post the UI updates to the main thread.
        runOnUiThread(() -> {
            cameraStateText.setText(cameraStateString);
            rollSlider.setValue(resetValue);
            updateRollSliderLabel(resetValue);
        });
    }
}
