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
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;

/**
 * Demonstrates 3D Path Following using an MVVM architecture with [PathFollowingViewModel].
 *
 * The Activity acts as a pure presentation layer: it observes [PathPlaybackState] from the ViewModel
 * and renders camera positions and dual polylines into [GoogleMap3D].
 */
public class PathFollowingActivity extends AppCompatActivity implements OnMap3DViewReadyCallback {

    private PathFollowingViewModel viewModel;

    // 3D Map View
    private Map3DView map3DView;
    private GoogleMap3D googleMap3D;

    // Polylines
    private Polyline staticRoutePolyline;
    private Polyline progressPolyline;

    // Control panel overlay bindings
    private CardView controlsCard;
    private MaterialButton btnCollapse;
    private View controlsScroll;
    private boolean isCollapsed = false;

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
        setupControlListeners();
        setupTouchAutoFade();
        observeViewModel();

        map3DView.onCreate(savedInstanceState);
        map3DView.getMap3DViewAsync(this);
    }

    @Override
    public void onMap3DViewReady(@NonNull GoogleMap3D map) {
        this.googleMap3D = map;
        render(viewModel.getCurrentState());
    }

    private void bindViews() {
        map3DView = findViewById(R.id.map3dView);
        controlsCard = findViewById(R.id.controls_card);
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
        btnPlayPause.setOnClickListener(v -> viewModel.togglePlayPause());

        if (btnCollapse != null) {
            btnCollapse.setOnClickListener(
                    v -> {
                        isCollapsed = !isCollapsed;
                        if (controlsScroll != null) {
                            controlsScroll.setVisibility(isCollapsed ? View.GONE : View.VISIBLE);
                        }
                        btnCollapse.setIconResource(
                                isCollapsed ? R.drawable.expand_less_24px : R.drawable.expand_more_24px);
                    });
        }

        progressSlider.addOnChangeListener(
                (slider, value, fromUser) -> {
                    if (fromUser) {
                        viewModel.seekToRatio(value);
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
                });

        switchDrawsOccludedSegments.setOnCheckedChangeListener(
                (buttonView, isChecked) -> viewModel.setDrawsOccludedSegments(isChecked));

        pathAltitudeSlider.addOnChangeListener(
                (slider, value, fromUser) -> {
                    if (fromUser) viewModel.setPathAltitudeOffset(value);
                    pathAltitudeSliderLabel.setText(getString(R.string.path_height_format, value));
                });

        rangeSlider.addOnChangeListener(
                (slider, value, fromUser) -> {
                    if (fromUser) viewModel.setCameraRange(value);
                    rangeSliderLabel.setText(
                            getString(R.string.camera_range_format, (int) value));
                });

        altitudeSlider.addOnChangeListener(
                (slider, value, fromUser) -> {
                    if (fromUser) viewModel.setGroundAltitude(value);
                    altitudeSliderLabel.setText(
                            getString(R.string.ground_altitude_format, (int) value));
                });

        headingSlider.addOnChangeListener(
                (slider, value, fromUser) -> {
                    if (fromUser) viewModel.setHeadingOffset(value);
                    headingSliderLabel.setText(
                            getString(R.string.heading_offset_format, (int) value));
                });

        tiltSlider.addOnChangeListener(
                (slider, value, fromUser) -> {
                    if (fromUser) viewModel.setCameraTilt(value);
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
                        altitudeSlider.setValueTo(100.0f);
                    } else if (checkedId == R.id.rb_rural) {
                        viewModel.setRoute(PathData.RURAL_PATH, /* applyDefaults= */ true);
                        pathAltitudeSlider.setValueTo(200.0f);
                        altitudeSlider.setValueTo(200.0f);
                    }
                });
    }

    private void observeViewModel() {
        viewModel.getLiveData().observe(this, this::handleStateUpdate);
    }

    private void handleStateUpdate(PathPlaybackState state) {
        render(state);
        manageAnimationTicker(state.isPlaying());
    }

    private void render(PathPlaybackState state) {
        if (googleMap3D == null || state == null) return;

        // Update Camera
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

        // Update Static Base Route Polyline
        PolylineOptions staticOptions = new PolylineOptions();
        staticOptions.setId(PathEngine.STATIC_POLYLINE_ID);
        staticOptions.setPath(state.getStaticPolylineVertices());
        staticOptions.setStrokeColor(Color.parseColor("#4285F4"));
        staticOptions.setStrokeWidth(16.0);
        staticOptions.setZIndex(1);
        staticOptions.setAltitudeMode(state.getAltitudeMode());
        staticOptions.setDrawsOccludedSegments(state.getDrawsOccludedSegments());
        staticRoutePolyline = googleMap3D.addPolyline(staticOptions);

        // Update Progress Polyline
        PolylineOptions progressOptions = new PolylineOptions();
        progressOptions.setId(PathEngine.PROGRESS_POLYLINE_ID);
        progressOptions.setPath(state.getProgressPolylineVertices());
        progressOptions.setStrokeColor(Color.parseColor("#9C27B0"));
        progressOptions.setStrokeWidth(8.0);
        progressOptions.setZIndex(2);
        progressOptions.setAltitudeMode(state.getAltitudeMode());
        progressOptions.setDrawsOccludedSegments(state.getDrawsOccludedSegments());
        progressPolyline = googleMap3D.addPolyline(progressOptions);

        // Update Play/Pause Button Icon
        btnPlayPause.setIconResource(
                state.isPlaying() ? R.drawable.pause_24px : R.drawable.play_arrow_24px);

        // Update Progress Slider without fighting touch scrubbing
        if (!state.isScrubbing()) {
            progressSlider.setValue(state.getProgressRatio());
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
        if (controlsCard != null) {
            controlsCard.setOnTouchListener(
                    (v, event) -> {
                        switch (event.getActionMasked()) {
                            case MotionEvent.ACTION_DOWN:
                                fadeHandler.removeCallbacksAndMessages(null);
                                controlsCard.setAlpha(1.0f);
                                break;
                            case MotionEvent.ACTION_UP:
                            case MotionEvent.ACTION_CANCEL:
                                scheduleControlFade();
                                break;
                        }
                        return false;
                    });
        }
        scheduleControlFade();
    }

    private void scheduleControlFade() {
        fadeHandler.removeCallbacksAndMessages(null);
        fadeHandler.postDelayed(
                () -> {
                    if (controlsCard != null && !isCollapsed) {
                        controlsCard.animate().alpha(0.80f).setDuration(400L).start();
                    }
                },
                5000L);
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
