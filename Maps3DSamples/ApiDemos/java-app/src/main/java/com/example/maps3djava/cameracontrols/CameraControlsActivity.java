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
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.view.WindowCompat;

import com.example.maps3d.common.Map3DSettings;
import com.example.maps3d.common.Map3DSettingsController;
import com.example.maps3dcommon.R;
import com.example.maps3djava.sampleactivity.SampleBaseActivity;
import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback;
import com.google.android.gms.maps3d.model.AltitudeMode;
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.FlyAroundOptions;
import com.google.android.gms.maps3d.model.FlyToOptions;
import com.google.android.gms.maps3d.model.Map3DMode;
import com.google.android.gms.maps3d.model.Marker;
import com.google.android.gms.maps3d.model.MarkerOptions;
import com.google.android.gms.maps3d.model.Polygon;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.List;

public class CameraControlsActivity extends SampleBaseActivity implements OnMap3DViewReadyCallback {
    @Override
    public final String getTAG() {
        return this.getClass().getSimpleName();
    }

    @Override
    @Map3DMode
    protected int getDefaultMapMode() {
        return Map3DMode.SATELLITE;
    }

    private TextView cameraStateText;
    private Slider rollSlider;
    private TextView rollSliderLabel;
    private boolean isSyncingRadioGroup = false;

    private final List<Polygon> restrictionCubeFaces = new ArrayList<>();
    private Marker activeBeaconMarker = null;
    private int beaconRefreshToken = 0;
    private boolean needsMeshBeaconRefresh = false;

    @AltitudeMode
    private int selectedAltitudeMode = AltitudeMode.RELATIVE_TO_GROUND;

    @StringRes
    private int activeTargetResId = R.string.camera_target_esb;

    @Override
    public final Camera getInitialCamera() {
        return DataModel.getEsbCamera(AltitudeMode.RELATIVE_TO_GROUND);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_camera_controls);

        map3DView = findViewById(R.id.map3dView);
        map3DView.onCreate(savedInstanceState);
        map3DView.getMap3DViewAsync(this);

        cameraStateText = findViewById(R.id.camera_state_text);

        rollSlider = findViewById(R.id.roll_slider);
        rollSliderLabel = findViewById(R.id.roll_slider_label);

        updateRollSliderLabel(rollSlider.getValue());

        findViewById(R.id.fly_around).setOnClickListener(view -> flyAroundCurrentCenter());

        findViewById(R.id.fly_to).setOnClickListener(view -> flyToTarget(R.string.camera_target_esb));

        findViewById(R.id.fly_to_mountain_button).setOnClickListener(
                view -> flyToTarget(R.string.camera_target_mt_rainier)
        );

        ChipGroup altitudeModeChipGroup = findViewById(R.id.altitude_mode_chip_group);
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

            if (button.isChecked()) {
                button.setText(getText(R.string.camera_remove_restriction));
                if (googleMap3D != null) {
                    googleMap3D.setCameraRestriction(DataModel.nycCameraRestriction);
                }
            } else {
                button.setText(getText(R.string.camera_activate_restriction));
                if (googleMap3D != null) {
                    googleMap3D.setCameraRestriction(null);
                }
            }
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
            if (checkedId == R.id.map_mode_hybrid) {
                mode = Map3DMode.HYBRID;
            } else if (checkedId == R.id.map_mode_satellite) {
                mode = Map3DMode.SATELLITE;
            } else {
                return;
            }
            Map3DSettingsController.getShared().setMapMode(mode);
            if (googleMap3D != null) {
                Map3DSettingsController.getShared().getSettings().applyTo(googleMap3D);
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
    protected void onQuickSettingsChanged(@NonNull Map3DSettings settings) {
        RadioGroup group = findViewById(R.id.map_mode_radio_group);
        if (group == null) {
            return;
        }
        int targetId = View.NO_ID;
        int mapMode = settings.getMapMode();
        if (mapMode == Map3DMode.HYBRID) {
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
     * Called when the [Map3DView] is ready to be used.
     *
     * @param googleMap3D The [GoogleMap3D] object representing the 3D map.
     */
    @Override
    protected void onMapReady(@NonNull GoogleMap3D googleMap3D) {
        super.onMapReady(googleMap3D);
        this.googleMap3D = googleMap3D;

        upsertActiveBeacon();

        googleMap3D.setOnMapSteadyListener(isSteady -> {
            if (isSteady) {
                if (map3DView != null) {
                    map3DView.setContentDescription("MapSteady");
                }
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
        boolean isMountain = (activeTargetResId == R.string.camera_target_mt_rainier);
        MarkerOptions beaconOptions = DataModel.getTargetBeaconOptions(
                isMountain,
                getBeaconLabel(isMountain, selectedAltitudeMode),
                selectedAltitudeMode,
                ++beaconRefreshToken
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
        activeTargetResId = targetResId;
        boolean isMountain = (targetResId == R.string.camera_target_mt_rainier);

        // Clear NYC bounds restriction if active so the camera can travel to Washington State
        if (isMountain) {
            MaterialButton restrictionButton = findViewById(R.id.toggle_restriction);
            if (restrictionButton != null && restrictionButton.isChecked()) {
                restrictionButton.setChecked(false);
                restrictionButton.setText(getText(R.string.camera_activate_restriction));
                googleMap3D.setCameraRestriction(null);
            }
        }

        needsMeshBeaconRefresh = (selectedAltitudeMode == AltitudeMode.RELATIVE_TO_MESH);
        upsertActiveBeacon();

        Camera targetCamera = DataModel.getTargetCamera(isMountain, selectedAltitudeMode);
        googleMap3D.setCameraAnimationEndListener(() -> {
            if (googleMap3D != null) {
                googleMap3D.setCameraAnimationEndListener(null);
                runOnUiThread(this::upsertActiveBeacon);
                if (isMountain) {
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

    @NonNull
    private String getAltitudeModeLabel(@AltitudeMode int mode) {
        if (mode == AltitudeMode.RELATIVE_TO_GROUND) {
            return getString(R.string.altitude_mode_relative_to_ground_code);
        } else if (mode == AltitudeMode.RELATIVE_TO_MESH) {
            return getString(R.string.altitude_mode_relative_to_mesh_code);
        } else if (mode == AltitudeMode.CLAMP_TO_GROUND) {
            return getString(R.string.altitude_mode_clamp_to_ground_code);
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
                getAltitudeModeLabel(selectedAltitudeMode)
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