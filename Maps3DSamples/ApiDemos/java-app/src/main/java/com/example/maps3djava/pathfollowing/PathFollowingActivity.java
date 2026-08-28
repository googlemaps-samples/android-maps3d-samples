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

package com.example.maps3djava.pathfollowing;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Choreographer;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;
import com.example.maps3d.common.PathData;
import com.example.maps3d.common.PathEngine;
import com.example.maps3d.common.PathFollowingViewModel;
import com.example.maps3d.common.PathPlaybackState;
import com.example.maps3d.common.PathTouchHandler;
import com.example.maps3dcommon.R;
import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.Map3DView;
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback;
import com.google.android.gms.maps3d.model.AltitudeMode;
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.LatLngAltitude;
import com.google.android.gms.maps3d.model.Polyline;
import com.google.android.gms.maps3d.model.PolylineOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;
import java.util.List;

/**
 * Demonstrates 3D Path Following using an MVVM architecture with [PathFollowingViewModel].
 *
 * Decoupled gesture controls and dynamic progress polyline driven strictly by time and progress.
 */
public class PathFollowingActivity extends AppCompatActivity implements OnMap3DViewReadyCallback {

    private PathFollowingViewModel viewModel;

    // 3D Map View & Gesture Overlay
    private Map3DView map3DView;
    private View gestureOverlay;
    private GoogleMap3D googleMap3D;

    // Polylines
    private Polyline staticRoutePolyline;
    private Polyline progressPolyline;
    private List<LatLngAltitude> lastStaticVertices;
    private double lastRenderedProgressDist = -1.0;
    private long lastSliderUpdateMillis = 0L;
    private Boolean lastIsPlaying;

    // Control panel overlay bindings
    private CardView controlsCard;
    private View cardHeader;
    private MaterialButton btnHelp;
    private MaterialButton btnCollapse;
    private View controlsScroll;
    private boolean isCollapsed = false;
    private ChipGroup chipGroupSpeed;

    private RadioGroup rgEnvironment;
    private RadioGroup rgAltitudeMode;
    private MaterialSwitch switchDrawsOccludedSegments;
    private Slider pathAltitudeSlider;
    private TextView pathAltitudeSliderLabel;
    private MaterialButton btnPlayPause;
    private Slider progressSlider;
    private Slider rangeSlider;
    private TextView rangeSliderLabel;
    private Slider altitudeSlider;
    private TextView altitudeSliderLabel;
    private Slider headingSlider;
    private TextView headingSliderLabel;
    private Slider tiltSlider;
    private TextView tiltSliderLabel;
    private Slider speedSlider;
    private TextView speedSliderLabel;

    // Auto-fade & VSYNC Choreographer
    private Choreographer.FrameCallback frameCallback;
    private final Handler fadeHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path_following);

        viewModel = new ViewModelProvider(this).get(PathFollowingViewModel.class);

        bindViews();
        setupCustomGestureHandling();
        setupControlListeners();
        setupTouchAutoFade() ;
        observeViewModel();

        map3DView.onCreate(savedInstanceState);
        map3DView.getMap3DViewAsync(this);
    }

    @Override
    public void onMap3DViewReady(@NonNull GoogleMap3D googleMap3D) {
        this.googleMap3D = googleMap3D;

        googleMap3D.setOnMapReadyListener(
                initialTime -> {
                    runOnUiThread(
                            () -> {
                                lastStaticVertices = null;
                                lastRenderedProgressDist = -1.0;
                                PathPlaybackState state = viewModel.getCurrentState();
                                updateStaticPolyline(state);
                                updateProgressPolyline(state);
                                updateCameraFromState(state);
                                renderUiControls(state);
                            });
                });
    }

    private void setupCustomGestureHandling() {
        if (gestureOverlay != null) {
            gestureOverlay.setOnTouchListener(new PathTouchHandler(this, viewModel));
        }
    }

    private void bindViews() {
        map3DView = findViewById(R.id.map3dView);
        gestureOverlay = findViewById(R.id.gesture_overlay);
        controlsCard = findViewById(R.id.controls_card);
        cardHeader = findViewById(R.id.card_header);
        btnHelp = findViewById(R.id.btn_help);
        chipGroupSpeed = findViewById(R.id.chip_group_speed);
        btnCollapse = findViewById(R.id.btn_collapse);
        controlsScroll = findViewById(R.id.controls_scroll);
        rgEnvironment = findViewById(R.id.rg_environment);
        rgAltitudeMode = findViewById(R.id.rg_altitude_mode);
        switchDrawsOccludedSegments = findViewById(R.id.switch_draws_occluded_segments);
        pathAltitudeSlider = findViewById(R.id.path_altitude_slider);
        pathAltitudeSliderLabel = findViewById(R.id.path_altitude_slider_label);
        btnPlayPause = findViewById(R.id.btn_play_pause);
        progressSlider = findViewById(R.id.progress_slider);
        rangeSlider = findViewById(R.id.range_slider);
        rangeSliderLabel = findViewById(R.id.range_slider_label);
        altitudeSlider = findViewById(R.id.altitude_slider);
        altitudeSliderLabel = findViewById(R.id.altitude_slider_label);
        headingSlider = findViewById(R.id.heading_slider);
        headingSliderLabel = findViewById(R.id.heading_slider_label);
        tiltSlider = findViewById(R.id.tilt_slider);
        tiltSliderLabel = findViewById(R.id.tilt_slider_label);
        speedSlider = findViewById(R.id.speed_slider);
        speedSliderLabel = findViewById(R.id.speed_slider_label);
    }

    private void setupControlListeners() {
        if (btnHelp != null) {
            btnHelp.setOnClickListener(v -> showHelpDialog());
        }

        btnPlayPause.setOnClickListener(v -> viewModel.togglePlayPause());

        if (chipGroupSpeed != null) {
            chipGroupSpeed.setOnCheckedStateChangeListener(
                    (group, checkedIds) -> {
                        if (checkedIds.isEmpty()) return;
                        int checkedId = checkedIds.get(0);
                        double targetSpeed = 30.0;
                        if (checkedId == R.id.chip_speed_05x) targetSpeed = 15.0;
                        else if (checkedId == R.id.chip_speed_1x) targetSpeed = 30.0;
                        else if (checkedId == R.id.chip_speed_2x) targetSpeed = 60.0;
                        else if (checkedId == R.id.chip_speed_3x) targetSpeed = 90.0;
                        else if (checkedId == R.id.chip_speed_5x) targetSpeed = 120.0;

                        viewModel.setFollowSpeed(targetSpeed);
                        speedSlider.setValue((float) targetSpeed);
                    });
        }

        if (btnCollapse != null) {
            btnCollapse.setOnClickListener(v -> setPanelCollapsed(!isCollapsed));
        }

        if (cardHeader != null) {
            cardHeader.setOnClickListener(v -> setPanelCollapsed(!isCollapsed));
        }

        GestureDetector cardSwipeDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null) return false;
                float dy = e2.getY() - e1.getY();
                if (dy > 50 && velocityY > 100) {
                    setPanelCollapsed(true);
                    return true;
                } else if (dy < -50 && velocityY < -100) {
                    setPanelCollapsed(false);
                    return true;
                }
                return false;
            }
        });

        if (cardHeader != null) {
            cardHeader.setOnTouchListener((v, event) -> cardSwipeDetector.onTouchEvent(event) || v.onTouchEvent(event));
        }

        if (controlsCard != null) {
            controlsCard.setOnTouchListener((v, event) -> cardSwipeDetector.onTouchEvent(event));
        }

        progressSlider.addOnChangeListener(
                (slider, value, fromUser) -> {
                    if (fromUser) {
                        viewModel.seekToRatio(value);
                        PathPlaybackState state = viewModel.getCurrentState();
                        updateCameraFromState(state);
                        updateProgressPolyline(state);
                    }
                });

        progressSlider.addOnSliderTouchListener(
                new Slider.OnSliderTouchListener() {
                    @Override
                    public void onStartTrackingTouch(@NonNull Slider slider) {
                        viewModel.setScrubbing(true);
                    }

                    @Override
                    public void onStopTrackingTouch(@NonNull Slider slider) {
                        viewModel.setScrubbing(false);
                        viewModel.seekToRatio(slider.getValue());
                        PathPlaybackState state = viewModel.getCurrentState();
                        updateCameraFromState(state);
                        updateProgressPolyline(state);
                    }
                });

        rgAltitudeMode.setOnCheckedChangeListener(
                (group, checkedId) -> {
                    int mode = AltitudeMode.CLAMP_TO_GROUND;
                    if (checkedId == R.id.rb_relative_to_ground) {
                        mode = AltitudeMode.RELATIVE_TO_GROUND;
                    } else if (checkedId == R.id.rb_relative_to_mesh) {
                        mode = AltitudeMode.RELATIVE_TO_MESH;
                    } else if (checkedId == R.id.rb_absolute) {
                        mode = AltitudeMode.ABSOLUTE;
                    }
                    viewModel.setAltitudeMode(mode);
                    resetPolylines();
                });

        switchDrawsOccludedSegments.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    viewModel.setDrawsOccludedSegments(isChecked);
                    resetPolylines();
                });

        pathAltitudeSlider.addOnChangeListener(
                (slider, value, fromUser) -> {
                    if (fromUser) {
                        viewModel.setPathAltitudeOffset(value);
                        resetPolylines();
                    }
                    pathAltitudeSliderLabel.setText(getString(R.string.path_height_format, value));
                });

        rangeSlider.addOnChangeListener(
                (slider, value, fromUser) -> {
                    if (fromUser) {
                        viewModel.setCameraRange(value);
                        updateCameraFromState(viewModel.getCurrentState());
                    }
                    rangeSliderLabel.setText(
                            getString(R.string.camera_range_format, (int) value));
                });

        altitudeSlider.addOnChangeListener(
                (slider, value, fromUser) -> {
                    if (fromUser) {
                        viewModel.setGroundAltitude(value);
                        updateCameraFromState(viewModel.getCurrentState());
                    }
                    altitudeSliderLabel.setText(
                            getString(R.string.ground_altitude_format, (int) value));
                });

        headingSlider.addOnChangeListener(
                (slider, value, fromUser) -> {
                    if (fromUser) {
                        viewModel.setHeadingOffset(value);
                        updateCameraFromState(viewModel.getCurrentState());
                    }
                    headingSliderLabel.setText(
                            getString(R.string.heading_offset_format, (int) value));
                });

        tiltSlider.addOnChangeListener(
                (slider, value, fromUser) -> {
                    if (fromUser) {
                        viewModel.setCameraTilt(value);
                        updateCameraFromState(viewModel.getCurrentState());
                    }
                    tiltSliderLabel.setText(
                            getString(R.string.camera_tilt_format, (int) value));
                });

        speedSlider.addOnChangeListener(
                (slider, value, fromUser) -> {
                    if (fromUser) viewModel.setFollowSpeed(value);
                    speedSliderLabel.setText(getString(R.string.follow_speed_format, (int) value));
                });

        rgEnvironment.setOnCheckedChangeListener(
                (group, checkedId) -> {
                    if (checkedId == R.id.rb_urban) {
                        viewModel.setRoute(PathData.URBAN_PATH, /* applyDefaults= */ true);
                        pathAltitudeSlider.setValueTo(20.0f);
                        altitudeSlider.setValueTo(500.0f);
                    } else if (checkedId == R.id.rb_rural) {
                        viewModel.setRoute(PathData.RURAL_PATH, /* applyDefaults= */ true);
                        pathAltitudeSlider.setValueTo(200.0f);
                        altitudeSlider.setValueTo(500.0f);
                    }
                    resetPolylines();
                });
    }

    private void setPanelCollapsed(boolean collapsed) {
        if (isCollapsed == collapsed) return;
        isCollapsed = collapsed;
        if (controlsScroll != null) {
            controlsScroll.setVisibility(isCollapsed ? View.GONE : View.VISIBLE);
        }
        if (btnCollapse != null) {
            btnCollapse.setIconResource(
                    isCollapsed ? R.drawable.expand_less_24px : R.drawable.expand_more_24px);
        }
    }

    private void showHelpDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.help_dialog_title)
                .setMessage(R.string.help_dialog_message)
                .setPositiveButton(R.string.help_dialog_ok, null)
                .show();
    }

    private void resetPolylines() {
        lastStaticVertices = null;
        lastRenderedProgressDist = -1.0;
        PathPlaybackState state = viewModel.getCurrentState();
        updateStaticPolyline(state);
        updateProgressPolyline(state);
        updateCameraFromState(state);
    }

    private void updateStaticPolyline(PathPlaybackState state) {
        if (googleMap3D == null || state == null) return;
        if (lastStaticVertices != null && lastStaticVertices.equals(state.getStaticPolylineVertices()) && staticRoutePolyline != null) return;

        lastStaticVertices = state.getStaticPolylineVertices();
        PolylineOptions staticOptions = new PolylineOptions();
        staticOptions.setId(PathEngine.STATIC_POLYLINE_ID);
        staticOptions.setPath(state.getStaticPolylineVertices());
        staticOptions.setStrokeColor(Color.parseColor("#4285F4"));
        staticOptions.setStrokeWidth(16.0);
        staticOptions.setZIndex(1);
        staticOptions.setAltitudeMode(state.getAltitudeMode());
        staticOptions.setDrawsOccludedSegments(state.getDrawsOccludedSegments());
        staticRoutePolyline = googleMap3D.addPolyline(staticOptions);
    }

    private void updateProgressPolyline(PathPlaybackState state) {
        if (googleMap3D == null || state == null || state.getProgressPolylineVertices().size() < 2) return;

        lastRenderedProgressDist = state.getElapsedDistance();
        PolylineOptions progressOptions = new PolylineOptions();
        progressOptions.setId(PathEngine.PROGRESS_POLYLINE_ID);
        progressOptions.setPath(state.getProgressPolylineVertices());
        progressOptions.setStrokeColor(Color.parseColor("#9C27B0"));
        progressOptions.setStrokeWidth(8.0);
        progressOptions.setZIndex(2);
        progressOptions.setAltitudeMode(state.getAltitudeMode());
        progressOptions.setDrawsOccludedSegments(state.getDrawsOccludedSegments());
        progressPolyline = googleMap3D.addPolyline(progressOptions);
    }

    private void observeViewModel() {
        viewModel.getLiveData().observe(this, state -> {
            updateCameraFromState(state);
            if (state.isPlaying() || Math.abs(state.getElapsedDistance() - lastRenderedProgressDist) > 0.1) {
                updateProgressPolyline(state);
            }
            renderUiControls(state);
            manageAnimationTicker(state.isPlaying());
        });
    }

    private void updateCameraFromState(PathPlaybackState state) {
        if (googleMap3D == null || state == null) return;
        Camera newCamera =
                new Camera(
                        /* center= */ new LatLngAltitude(
                                /* latitude= */ state.getCurrentPosition().latitude,
                                /* longitude= */ state.getCurrentPosition().longitude,
                                /* altitude= */ state.getCameraTargetAltitude()),
                        /* heading= */ state.getEffectiveHeading(),
                        /* tilt= */ state.getCameraTilt(),
                        /* roll= */ 0.0,
                        /* range= */ state.getCameraRange());
        googleMap3D.setCamera(newCamera);
    }

    private void renderUiControls(PathPlaybackState state) {
        if (state == null) return;

        if (lastIsPlaying == null || lastIsPlaying != state.isPlaying()) {
            lastIsPlaying = state.isPlaying();
            btnPlayPause.setIconResource(
                    state.isPlaying() ? R.drawable.pause_24px : R.drawable.play_arrow_24px);
        }

        if (!state.isScrubbing()) {
            long now = System.currentTimeMillis();
            if (now - lastSliderUpdateMillis >= 100L || !state.isPlaying()) {
                lastSliderUpdateMillis = now;
                progressSlider.setValue(state.getProgressRatio());
            }
        }

        String boostSuffix = "";
        if (state.getSpeedBoostMultiplier() >= 4.5) {
            boostSuffix = " (5x Warp Speed)";
        } else if (state.getSpeedBoostMultiplier() >= 1.5) {
            boostSuffix = " (2x Boost)";
        }
        speedSliderLabel.setText(getString(R.string.follow_speed_format, (int) state.getFollowSpeedMps()) + boostSuffix);

        if (!isCollapsed) {
            float clampedRange = Math.max(rangeSlider.getValueFrom(), Math.min(rangeSlider.getValueTo(), (float) state.getCameraRange()));
            if (Math.abs(rangeSlider.getValue() - clampedRange) >= 1.0f) {
                rangeSlider.setValue(clampedRange);
                rangeSliderLabel.setText(getString(R.string.camera_range_format, (int) state.getCameraRange()));
            }

            float clampedTilt = Math.max(tiltSlider.getValueFrom(), Math.min(tiltSlider.getValueTo(), (float) state.getCameraTilt()));
            if (Math.abs(tiltSlider.getValue() - clampedTilt) >= 0.5f) {
                tiltSlider.setValue(clampedTilt);
                tiltSliderLabel.setText(getString(R.string.camera_tilt_format, (int) state.getCameraTilt()));
            }

            float clampedHeading = Math.max(headingSlider.getValueFrom(), Math.min(headingSlider.getValueTo(), (float) state.getHeadingOffset()));
            if (Math.abs(headingSlider.getValue() - clampedHeading) >= 0.5f) {
                headingSlider.setValue(clampedHeading);
                headingSliderLabel.setText(getString(R.string.heading_offset_format, (int) state.getHeadingOffset()));
            }
        }
    }

    private void manageAnimationTicker(boolean isPlaying) {
        if (isPlaying) {
            if (frameCallback == null) {
                frameCallback =
                        new Choreographer.FrameCallback() {
                            private long lastTimeNanos = 0L;

                            @Override
                            public void doFrame(long frameTimeNanos) {
                                if (!viewModel.getCurrentState().isPlaying()) return;

                                if (lastTimeNanos == 0L) {
                                    lastTimeNanos = frameTimeNanos;
                                    Choreographer.getInstance().postFrameCallback(this);
                                    return;
                                }

                                double dt = (frameTimeNanos - lastTimeNanos) / 1_000_000_000.0;
                                lastTimeNanos = frameTimeNanos;

                                viewModel.advance(dt);
                                Choreographer.getInstance().postFrameCallback(this);
                            }
                        };
                Choreographer.getInstance().postFrameCallback(frameCallback);
            }
        } else {
            if (frameCallback != null) {
                Choreographer.getInstance().removeFrameCallback(frameCallback);
                frameCallback = null;
            }
        }
    }

        private void setupTouchAutoFade() {
        scheduleControlFade();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN || ev.getAction() == MotionEvent.ACTION_MOVE) {
            fadeHandler.removeCallbacksAndMessages(null);
            if (controlsCard != null) {
                controlsCard.animate().alpha(1.0f).setDuration(150L).start();
            }
        } else if (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL) {
            scheduleControlFade();
        }
        return super.dispatchTouchEvent(ev);
    }

    private void scheduleControlFade() {
        fadeHandler.removeCallbacksAndMessages(null);
        fadeHandler.postDelayed(
                () -> {
                    if (controlsCard != null) {
                        controlsCard.animate().alpha(0.35f).setDuration(500L).start();
                    }
                },
                3500L);
    }

    @Override
    protected void onResume() {
        super.onResume();
        map3DView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        viewModel.setPlaying(false);
        viewModel.setSpeedBoosted(false);
        map3DView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.setPlaying(false);
        if (frameCallback != null) {
            Choreographer.getInstance().removeFrameCallback(frameCallback);
            frameCallback = null;
        }
        fadeHandler.removeCallbacksAndMessages(null);
        staticRoutePolyline = null;
        progressPolyline = null;
        map3DView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        map3DView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        map3DView.onSaveInstanceState(outState);
    }
}
