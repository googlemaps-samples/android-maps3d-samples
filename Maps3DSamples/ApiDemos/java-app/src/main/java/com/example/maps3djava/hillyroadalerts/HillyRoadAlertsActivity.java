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

package com.example.maps3djava.hillyroadalerts;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Choreographer;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.example.maps3d.common.AlertSeverity;
import com.example.maps3d.common.ElevationSparklineView;
import com.example.maps3d.common.ExtremaType;
import com.example.maps3d.common.HillyRoadAlertViewModel;
import com.example.maps3d.common.HillyRoadUiState;
import com.example.maps3d.common.HillyRoutePreset;
import com.example.maps3d.common.MaxHeightScrollView;
import com.example.maps3d.common.TerrainAlertState;
import com.example.maps3d.common.TerrainAlertType;
import com.example.maps3d.common.TerrainExtremaPoint;
import com.example.maps3dcommon.R;
import com.example.maps3djava.sampleactivity.SampleBaseActivity;
import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.model.AltitudeMode;
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.CollisionBehavior;
import com.google.android.gms.maps3d.model.Glyph;
import com.google.android.gms.maps3d.model.LatLngAltitude;
import com.google.android.gms.maps3d.model.Map3DMode;
import com.google.android.gms.maps3d.model.Marker;
import com.google.android.gms.maps3d.model.MarkerOptions;
import com.google.android.gms.maps3d.model.Model;
import com.google.android.gms.maps3d.model.ModelOptions;
import com.google.android.gms.maps3d.model.Orientation;
import com.google.android.gms.maps3d.model.PinConfiguration;
import com.google.android.gms.maps3d.model.Polyline;
import com.google.android.gms.maps3d.model.PolylineOptions;
import com.google.android.gms.maps3d.model.Vector3D;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Java Views showcase sample for "Hilly & Undulating (Roller-Coaster) Road Early Alerts".
 *
 * <p>Maintains 1:1 behavioral and visual parity with Kotlin Views and Jetpack Compose by observing
 * {@link HillyRoadAlertViewModel#getUiStateLiveData()}.
 */
public class HillyRoadAlertsActivity extends SampleBaseActivity
        implements Choreographer.FrameCallback {

    private static final String TAG = HillyRoadAlertsActivity.class.getSimpleName();

    private HillyRoadAlertViewModel viewModel;

    // HUD Views
    private MaterialCardView hudAlertCard;
    private TextView tvAlertTitle;
    private TextView tvAlertDistancePill;
    private TextView tvChipExtrema;
    private TextView tvChipMaxGrade;
    private TextView tvChipAdvisorySpeed;
    private TextView tvSparklineLabel;
    private ElevationSparklineView sparklineView;

    // Bottom Sheet Controls
    private MaxHeightScrollView controlsScroll;
    private MaterialButton btnCollapse;
    private TextView tvCurrentTelemetry;
    private Chip chipJumpToHills;
    private MaterialButton btnPlayPause;
    private Slider progressSlider;
    private ChipGroup chipGroupRoute;
    private ChipGroup chipGroupHorizon;
    private ChipGroup chipGroupSpeed;
    private MaterialSwitch switchCameraPreview;

    // 3D Map Overlays & Diffing State
    @Nullable private Polyline baseRoutePolyline;
    @Nullable private Polyline traveledRoutePolyline;
    @Nullable private Polyline hazardRoutePolyline;
    @Nullable private Model vehicleModel;
    private final Map<String, Marker> activeExtremaMarkers = new HashMap<>();

    @Nullable private HillyRoutePreset renderedPreset;
    private double lastRenderedTraveledDist = -1.0;
    private List<LatLngAltitude> lastRenderedHazardCoords = Collections.emptyList();
    @Nullable private AlertSeverity renderedHazardSeverity;
    @Nullable private LatLngAltitude lastRenderedVehiclePos;
    private double lastRenderedVehicleHeading = Double.NaN;
    private boolean isControlsExpanded = true;
    private boolean isUpdatingUiFromState = false;
    private long lastFrameTimeNanos = 0L;

    @Override
    public String getTAG() {
        return TAG;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_hilly_road_alerts;
    }

    @Override
    public Camera getInitialCamera() {
        return new Camera(
                new LatLngAltitude(36.485940, -114.532890, 614.4),
                0.0,
                HillyRoadAlertViewModel.NORMAL_CAMERA_TILT,
                0.0,
                HillyRoadAlertViewModel.NORMAL_CAMERA_RANGE);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Note: SampleBaseActivity already inflated getLayoutResId() and initialized map3DView.
        viewModel = new ViewModelProvider(this).get(HillyRoadAlertViewModel.class);

        bindViews();
        configureViewportGuardrails();
        setupControlListeners();

        viewModel.getUiStateLiveData().observe(this, this::renderUiState);
    }

    @Override
    protected void onMapReady(@NonNull GoogleMap3D googleMap3D) {
        super.onMapReady(googleMap3D);
        googleMap3D.setMapMode(Map3DMode.HYBRID);
        googleMap3D.setOnMapSteadyListener(
                isSceneSteady -> {
                    if (isSceneSteady) {
                        if (map3DView != null) {
                            map3DView.setContentDescription("MapSteady");
                        }
                        runOnUiThread(() -> startRouteIfMapReady(googleMap3D));
                    }
                });
        if (HillyRoadAlertViewModel.isInternetAvailable(this)) {
            startRouteIfMapReady(googleMap3D);
        }
    }

    private void startRouteIfMapReady(@NonNull GoogleMap3D map) {
        if (viewModel == null || viewModel.getCurrentState().isMapReady) {
            return;
        }
        lastFrameTimeNanos = 0L;
        viewModel.onMapReady();
        renderMapScene(map, viewModel.getCurrentState());
    }

    @Override
    protected void onResume() {
        super.onResume();
        lastFrameTimeNanos = 0L;
        Choreographer.getInstance().postFrameCallback(this);
    }

    @Override
    protected void onPause() {
        Choreographer.getInstance().removeFrameCallback(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Choreographer.getInstance().removeFrameCallback(this);
        clearMapOverlays();
        super.onDestroy();
    }

    @Override
    public void doFrame(long frameTimeNanos) {
        boolean isReady =
                googleMap3D != null && viewModel != null && viewModel.getCurrentState().isMapReady;
        if (isReady && lastFrameTimeNanos != 0L) {
            double dtSeconds = (frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000.0;
            viewModel.advance(dtSeconds);
        }
        if (isReady) {
            lastFrameTimeNanos = frameTimeNanos;
        }
        Choreographer.getInstance().postFrameCallback(this);
    }

    private void bindViews() {
        hudAlertCard = findViewById(R.id.hud_alert_card);
        tvAlertTitle = findViewById(R.id.tv_alert_title);
        tvAlertDistancePill = findViewById(R.id.tv_alert_distance_pill);
        tvChipExtrema = findViewById(R.id.tv_chip_extrema);
        tvChipMaxGrade = findViewById(R.id.tv_chip_max_grade);
        tvChipAdvisorySpeed = findViewById(R.id.tv_chip_advisory_speed);
        tvSparklineLabel = findViewById(R.id.tv_sparkline_label);
        sparklineView = findViewById(R.id.sparkline_view);

        controlsScroll = findViewById(R.id.controls_scroll);
        btnCollapse = findViewById(R.id.btn_collapse);
        tvCurrentTelemetry = findViewById(R.id.tv_current_telemetry);
        chipJumpToHills = findViewById(R.id.chip_jump_to_hills);
        btnPlayPause = findViewById(R.id.btn_play_pause);
        progressSlider = findViewById(R.id.progress_slider);
        chipGroupRoute = findViewById(R.id.chip_group_route);
        chipGroupHorizon = findViewById(R.id.chip_group_horizon);
        chipGroupSpeed = findViewById(R.id.chip_group_speed);
        switchCameraPreview = findViewById(R.id.switch_camera_preview);
    }

    private void configureViewportGuardrails() {
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        boolean isLandscape =
                getResources().getConfiguration().orientation
                        == Configuration.ORIENTATION_LANDSCAPE;
        int maxScrollHeight = (int) (screenHeight * (isLandscape ? 0.28f : 0.36f));
        controlsScroll.setMaxHeightPx(maxScrollHeight);

        setControlsExpanded(!isLandscape);
    }

    private void setControlsExpanded(boolean expanded) {
        isControlsExpanded = expanded;
        controlsScroll.setVisibility(expanded ? View.VISIBLE : View.GONE);
        btnCollapse.setIconResource(
                expanded ? R.drawable.expand_more_24px : R.drawable.expand_less_24px);
        btnCollapse.setContentDescription(
                getString(expanded ? R.string.collapse_controls : R.string.expand_controls));
    }

    private void setupControlListeners() {
        View.OnClickListener toggleCollapseListener =
                v -> setControlsExpanded(!isControlsExpanded);
        FrameLayout dragHandle = findViewById(R.id.layout_drag_handle);
        LinearLayout cardHeader = findViewById(R.id.card_header);
        dragHandle.setOnClickListener(toggleCollapseListener);
        cardHeader.setOnClickListener(toggleCollapseListener);
        btnCollapse.setOnClickListener(toggleCollapseListener);

        btnPlayPause.setOnClickListener(v -> viewModel.togglePlayPause());
        chipJumpToHills.setOnClickListener(v -> viewModel.jumpToHillyZone());

        progressSlider.addOnSliderTouchListener(
                new Slider.OnSliderTouchListener() {
                    @Override
                    public void onStartTrackingTouch(@NonNull Slider slider) {
                        viewModel.setScrubbing(true);
                    }

                    @Override
                    public void onStopTrackingTouch(@NonNull Slider slider) {
                        viewModel.setScrubbing(false);
                    }
                });

        progressSlider.addOnChangeListener(
                (slider, value, fromUser) -> {
                    if (fromUser && !isUpdatingUiFromState) {
                        viewModel.seekToProgress(value);
                    }
                });

        chipGroupRoute.setOnCheckedStateChangeListener(
                (group, checkedIds) -> {
                    if (isUpdatingUiFromState || checkedIds.isEmpty()) {
                        return;
                    }
                    int checkedId = checkedIds.get(0);
                    if (checkedId == R.id.chip_route_roller_coaster) {
                        viewModel.selectRoute(HillyRoutePreset.ROLLER_COASTER);
                    } else if (checkedId == R.id.chip_route_mountain_pass) {
                        viewModel.selectRoute(HillyRoutePreset.MOUNTAIN_PASS);
                    } else if (checkedId == R.id.chip_route_coastal) {
                        viewModel.selectRoute(HillyRoutePreset.COASTAL_DRIVE);
                    }
                });

        chipGroupHorizon.setOnCheckedStateChangeListener(
                (group, checkedIds) -> {
                    if (isUpdatingUiFromState || checkedIds.isEmpty()) {
                        return;
                    }
                    int checkedId = checkedIds.get(0);
                    if (checkedId == R.id.chip_horizon_250) {
                        viewModel.setLookaheadHorizon(250.0);
                    } else if (checkedId == R.id.chip_horizon_500) {
                        viewModel.setLookaheadHorizon(500.0);
                    } else if (checkedId == R.id.chip_horizon_750) {
                        viewModel.setLookaheadHorizon(750.0);
                    }
                });

        chipGroupSpeed.setOnCheckedStateChangeListener(
                (group, checkedIds) -> {
                    if (isUpdatingUiFromState || checkedIds.isEmpty()) {
                        return;
                    }
                    int checkedId = checkedIds.get(0);
                    if (checkedId == R.id.chip_speed_05x) {
                        viewModel.setSpeedMultiplier(0.5);
                    } else if (checkedId == R.id.chip_speed_1x) {
                        viewModel.setSpeedMultiplier(1.0);
                    } else if (checkedId == R.id.chip_speed_2x) {
                        viewModel.setSpeedMultiplier(2.0);
                    } else if (checkedId == R.id.chip_speed_3x) {
                        viewModel.setSpeedMultiplier(3.0);
                    } else if (checkedId == R.id.chip_speed_5x) {
                        viewModel.setSpeedMultiplier(5.0);
                    }
                });

        switchCameraPreview.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    if (!isUpdatingUiFromState) {
                        viewModel.setAutoCameraPitchPreview(isChecked);
                    }
                });
    }

    private void renderUiState(@NonNull HillyRoadUiState state) {
        isUpdatingUiFromState = true;
        try {
            TerrainAlertState alert = state.terrainAlert;

            tvAlertTitle.setText(getAlertTitleText(alert.alertType));
            if (alert.alertType == TerrainAlertType.NONE) {
                tvAlertDistancePill.setText(getString(R.string.hilly_alert_distance_clear));
            } else if (alert.distanceToHazardMeters <= 0.5) {
                tvAlertDistancePill.setText(
                        getString(
                                R.string.hilly_alert_distance_active_format,
                                (int) Math.round(alert.remainingHazardDistanceMeters)));
            } else {
                tvAlertDistancePill.setText(
                        getString(
                                R.string.hilly_alert_distance_approaching_format,
                                (int) Math.round(alert.distanceToHazardMeters)));
            }

            int strokeColor;
            int pillTextColor;
            if (alert.severity == AlertSeverity.WARNING) {
                strokeColor = Color.rgb(234, 67, 53); // Canonical Google Red (#EA4335)
                pillTextColor = Color.rgb(254, 202, 202);
            } else if (alert.severity == AlertSeverity.CAUTION) {
                strokeColor = Color.rgb(251, 188, 4); // Canonical Google Amber (#FBBC04)
                pillTextColor = Color.rgb(254, 240, 138);
            } else {
                strokeColor = Color.rgb(16, 185, 129); // Emerald (#10B981)
                pillTextColor = Color.rgb(167, 243, 208);
            }
            hudAlertCard.setStrokeColor(strokeColor);
            tvAlertDistancePill.setTextColor(pillTextColor);

            tvChipExtrema.setText(
                    getString(
                            R.string.hilly_telemetry_extrema_format,
                            alert.crestCount,
                            alert.dipCount));
            tvChipMaxGrade.setText(
                    String.format(
                            Locale.US,
                            getString(R.string.hilly_telemetry_max_grade_format),
                            alert.maxPositiveGradePercent,
                            alert.maxNegativeGradePercent));
            tvChipAdvisorySpeed.setText(
                    getString(
                            R.string.hilly_telemetry_advisory_speed_format,
                            alert.recommendedSpeedKmh));

            tvSparklineLabel.setText(
                    getString(
                            R.string.hilly_sparkline_label_format,
                            (int) Math.round(state.lookaheadHorizonMeters)));
            sparklineView.updateData(
                    alert.lookaheadProfile,
                    alert.upcomingExtrema,
                    alert.severity,
                    state.lookaheadHorizonMeters);

            tvCurrentTelemetry.setText(
                    String.format(
                            Locale.US,
                            getString(R.string.hilly_current_telemetry_format),
                            alert.currentGradePercent,
                            (int) Math.round(state.currentDistanceMeters),
                            (int) Math.round(state.totalDistanceMeters)));

            btnPlayPause.setIconResource(
                    state.isPlaying ? R.drawable.pause_24px : R.drawable.play_arrow_24px);
            if (!state.isUserScrubbing) {
                float clampedProgress = Math.max(0.0f, Math.min(1.0f, state.progressRatio));
                progressSlider.setValue(clampedProgress);
            }

            int expectedRouteChipId;
            if (state.selectedPreset == HillyRoutePreset.MOUNTAIN_PASS) {
                expectedRouteChipId = R.id.chip_route_mountain_pass;
            } else if (state.selectedPreset == HillyRoutePreset.COASTAL_DRIVE) {
                expectedRouteChipId = R.id.chip_route_coastal;
            } else {
                expectedRouteChipId = R.id.chip_route_roller_coaster;
            }
            if (chipGroupRoute.getCheckedChipId() != expectedRouteChipId) {
                chipGroupRoute.check(expectedRouteChipId);
            }

            int roundedHorizon = (int) Math.round(state.lookaheadHorizonMeters);
            int expectedHorizonChipId;
            if (roundedHorizon == 250) {
                expectedHorizonChipId = R.id.chip_horizon_250;
            } else if (roundedHorizon == 750) {
                expectedHorizonChipId = R.id.chip_horizon_750;
            } else {
                expectedHorizonChipId = R.id.chip_horizon_500;
            }
            if (chipGroupHorizon.getCheckedChipId() != expectedHorizonChipId) {
                chipGroupHorizon.check(expectedHorizonChipId);
            }

            int expectedSpeedChipId;
            if (Math.abs(state.speedMultiplier - 0.5) < 0.05) {
                expectedSpeedChipId = R.id.chip_speed_05x;
            } else if (Math.abs(state.speedMultiplier - 2.0) < 0.05) {
                expectedSpeedChipId = R.id.chip_speed_2x;
            } else if (Math.abs(state.speedMultiplier - 3.0) < 0.05) {
                expectedSpeedChipId = R.id.chip_speed_3x;
            } else if (Math.abs(state.speedMultiplier - 5.0) < 0.05) {
                expectedSpeedChipId = R.id.chip_speed_5x;
            } else {
                expectedSpeedChipId = R.id.chip_speed_1x;
            }
            if (chipGroupSpeed.getCheckedChipId() != expectedSpeedChipId) {
                chipGroupSpeed.check(expectedSpeedChipId);
            }

            if (switchCameraPreview.isChecked() != state.autoCameraPitchPreview) {
                switchCameraPreview.setChecked(state.autoCameraPitchPreview);
            }
        } finally {
            isUpdatingUiFromState = false;
        }

        if (googleMap3D != null && state.isMapReady) {
            renderMapScene(googleMap3D, state);
        }
    }

    private String getAlertTitleText(@NonNull TerrainAlertType alertType) {
        switch (alertType) {
            case UNDULATING_ROLLER_COASTER:
                return getString(R.string.hilly_alert_title_roller_coaster);
            case BLIND_CREST:
                return getString(R.string.hilly_alert_title_blind_crest);
            case STEEP_CLIMB:
                return getString(R.string.hilly_alert_title_steep_climb);
            case STEEP_DESCENT:
                return getString(R.string.hilly_alert_title_steep_descent);
            case NONE:
            default:
                return getString(R.string.hilly_alert_title_clear);
        }
    }

    private void renderMapScene(@NonNull GoogleMap3D map, @NonNull HillyRoadUiState state) {
        try {
            // 1. Update Driver Chase Camera
            Camera cam =
                    new Camera(
                            state.cameraCenter,
                            state.cameraHeading,
                            state.cameraTilt,
                            0.0,
                            state.cameraRange);
            map.setCamera(cam);

            // 2. Rebuild Base Route Polyline & clear extrema markers if preset changed
            if (renderedPreset != state.selectedPreset || baseRoutePolyline == null) {
                renderedPreset = state.selectedPreset;
                lastRenderedTraveledDist = -1.0;
                lastRenderedHazardCoords = Collections.emptyList();
                if (baseRoutePolyline != null) {
                    baseRoutePolyline.remove();
                }
                PolylineOptions baseOpts = new PolylineOptions();
                baseOpts.setId(HillyRoadAlertViewModel.BASE_ROUTE_POLYLINE_ID);
                baseOpts.setPath(state.staticRouteVertices);
                baseOpts.setStrokeColor(Color.argb(210, 56, 189, 248));
                baseOpts.setStrokeWidth(14.0);
                baseOpts.setAltitudeMode(AltitudeMode.ABSOLUTE);
                baseOpts.setZIndex(2);
                baseOpts.setDrawsOccludedSegments(true);
                baseRoutePolyline = map.addPolyline(baseOpts);
                clearExtremaMarkers();
            }

            // 3. Update Traveled Progress Polyline
            List<LatLngAltitude> traveledCoords = state.traveledRouteVertices;
            if (traveledCoords.size() >= 2
                    && (traveledRoutePolyline == null
                            || Math.abs(state.currentDistanceMeters - lastRenderedTraveledDist)
                                    > 0.05)) {
                lastRenderedTraveledDist = state.currentDistanceMeters;
                PolylineOptions traveledOpts = new PolylineOptions();
                traveledOpts.setId(HillyRoadAlertViewModel.TRAVELED_ROUTE_POLYLINE_ID);
                traveledOpts.setPath(traveledCoords);
                traveledOpts.setStrokeColor(Color.rgb(16, 185, 129));
                traveledOpts.setStrokeWidth(16.0);
                traveledOpts.setAltitudeMode(AltitudeMode.ABSOLUTE);
                traveledOpts.setZIndex(3);
                traveledOpts.setDrawsOccludedSegments(true);
                traveledRoutePolyline = map.addPolyline(traveledOpts);
            }

            // 4. Update Active Hazard Highlight Polyline
            List<LatLngAltitude> hazardCoords = state.terrainAlert.hazardPolylineCoordinates;
            AlertSeverity severity = state.terrainAlert.severity;
            if (hazardCoords.size() >= 2 && severity != AlertSeverity.NONE) {
                if (hazardRoutePolyline == null
                        || renderedHazardSeverity != severity
                        || !lastRenderedHazardCoords.equals(hazardCoords)) {
                    int hazardColor =
                            (severity == AlertSeverity.WARNING)
                                    ? Color.rgb(234, 67, 53)
                                    : Color.rgb(251, 188, 4);
                    renderedHazardSeverity = severity;
                    lastRenderedHazardCoords = hazardCoords;
                    PolylineOptions hazardOpts = new PolylineOptions();
                    hazardOpts.setId(HillyRoadAlertViewModel.HAZARD_ROUTE_POLYLINE_ID);
                    hazardOpts.setPath(hazardCoords);
                    hazardOpts.setStrokeColor(hazardColor);
                    hazardOpts.setStrokeWidth(20.0);
                    hazardOpts.setAltitudeMode(AltitudeMode.ABSOLUTE);
                    hazardOpts.setZIndex(5);
                    hazardOpts.setDrawsOccludedSegments(true);
                    hazardRoutePolyline = map.addPolyline(hazardOpts);
                }
            } else {
                if (hazardRoutePolyline != null) {
                    hazardRoutePolyline.remove();
                    hazardRoutePolyline = null;
                }
                renderedHazardSeverity = null;
                lastRenderedHazardCoords = Collections.emptyList();
            }

            // 5. Update Simulated Vehicle 3D Model (Red Car .glb from Routes API sample)
            if (vehicleModel == null
                    || !state.vehiclePosition.equals(lastRenderedVehiclePos)
                    || Double.compare(state.currentHeadingDegrees, lastRenderedVehicleHeading)
                            != 0) {
                lastRenderedVehiclePos = state.vehiclePosition;
                lastRenderedVehicleHeading = state.currentHeadingDegrees;
                ModelOptions vehicleOpts = new ModelOptions();
                vehicleOpts.setId(HillyRoadAlertViewModel.VEHICLE_MODEL_ID);
                vehicleOpts.setPosition(state.vehiclePosition);
                vehicleOpts.setAltitudeMode(AltitudeMode.ABSOLUTE);
                vehicleOpts.setUrl(HillyRoadAlertViewModel.VEHICLE_MODEL_URL);
                vehicleOpts.setOrientation(
                        new Orientation(
                                state.currentHeadingDegrees,
                                HillyRoadAlertViewModel.VEHICLE_MODEL_TILT,
                                0.0));
                vehicleOpts.setScale(
                        new Vector3D(
                                HillyRoadAlertViewModel.VEHICLE_MODEL_SCALE,
                                HillyRoadAlertViewModel.VEHICLE_MODEL_SCALE,
                                HillyRoadAlertViewModel.VEHICLE_MODEL_SCALE));
                vehicleModel = map.addModel(vehicleOpts);
            }

            // 6. Diff 3D Crest & Dip Markers by stable ID
            syncExtremaMarkers(map, state.terrainAlert.upcomingExtrema);
        } catch (Exception ignored) {
            // Safely ignore map scene updates while the map is offline or not yet ready
        }
    }

    private void syncExtremaMarkers(
            @NonNull GoogleMap3D map, @NonNull List<TerrainExtremaPoint> upcomingExtrema) {
        Set<String> desiredIds = new HashSet<>();
        for (TerrainExtremaPoint ext : upcomingExtrema) {
            desiredIds.add(ext.id);
        }

        Iterator<Map.Entry<String, Marker>> iterator = activeExtremaMarkers.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Marker> entry = iterator.next();
            if (!desiredIds.contains(entry.getKey())) {
                entry.getValue().remove();
                iterator.remove();
            }
        }

        for (TerrainExtremaPoint extremum : upcomingExtrema) {
            if (activeExtremaMarkers.containsKey(extremum.id)) {
                continue;
            }

            boolean isCrest = extremum.type == ExtremaType.CREST;
            int pinBgColor =
                    isCrest ? Color.rgb(234, 67, 53) : Color.rgb(2, 132, 199);
            int pinBorderColor =
                    isCrest ? Color.rgb(254, 240, 138) : Color.rgb(251, 188, 4);
            String glyphSymbol = isCrest ? "^" : "v";
            double signedGrade =
                    isCrest
                            ? Math.abs(extremum.approachGradePercent)
                            : -Math.abs(extremum.approachGradePercent);
            String markerLabel =
                    String.format(
                            Locale.US,
                            getString(
                                    isCrest
                                            ? R.string.hilly_marker_crest_format
                                            : R.string.hilly_marker_dip_format),
                            signedGrade);

            Glyph glyph = Glyph.fromColor(Color.WHITE);
            glyph.setText(glyphSymbol);
            PinConfiguration pinStyle =
                    PinConfiguration.builder()
                            .setScale(1.2f)
                            .setBackgroundColor(pinBgColor)
                            .setBorderColor(pinBorderColor)
                            .setGlyph(glyph)
                            .build();

            MarkerOptions opts = new MarkerOptions();
            opts.setId(extremum.id);
            opts.setPosition(extremum.position);
            opts.setLabel(markerLabel);
            opts.setAltitudeMode(AltitudeMode.ABSOLUTE);
            opts.setZIndex(8);
            opts.setExtruded(true);
            opts.setDrawnWhenOccluded(true);
            opts.setCollisionBehavior(CollisionBehavior.REQUIRED_AND_HIDES_OPTIONAL);
            opts.setStyle(pinStyle);

            Marker marker = map.addMarker(opts);
            if (marker != null) {
                final String toastLabel = markerLabel;
                final int distMeters = (int) Math.round(extremum.distanceAheadMeters);
                final int elevMeters = (int) Math.round(extremum.position.getAltitude());
                marker.setClickListener(
                        () ->
                                runOnUiThread(
                                        () ->
                                                Toast.makeText(
                                                                this,
                                                                getString(
                                                                        R.string
                                                                                .hilly_marker_toast_format,
                                                                        toastLabel,
                                                                        distMeters,
                                                                        elevMeters),
                                                                Toast.LENGTH_SHORT)
                                                        .show()));
                activeExtremaMarkers.put(extremum.id, marker);
            }
        }
    }

    private void clearExtremaMarkers() {
        for (Marker marker : activeExtremaMarkers.values()) {
            marker.remove();
        }
        activeExtremaMarkers.clear();
    }

    private void clearMapOverlays() {
        if (baseRoutePolyline != null) {
            baseRoutePolyline.remove();
            baseRoutePolyline = null;
        }
        if (traveledRoutePolyline != null) {
            traveledRoutePolyline.remove();
            traveledRoutePolyline = null;
        }
        if (hazardRoutePolyline != null) {
            hazardRoutePolyline.remove();
            hazardRoutePolyline = null;
        }
        if (vehicleModel != null) {
            vehicleModel.remove();
            vehicleModel = null;
        }
        clearExtremaMarkers();
    }
}
