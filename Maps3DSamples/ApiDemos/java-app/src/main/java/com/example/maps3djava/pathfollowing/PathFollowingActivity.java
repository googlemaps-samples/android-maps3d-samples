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
import android.view.MotionEvent;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.WindowCompat;
import com.example.maps3d.common.InterpolatedPathPoint;
import com.example.maps3d.common.PathData;
import com.example.maps3d.common.PathEngine;
import com.example.maps3dcommon.R;
import com.google.android.gms.maps.model.LatLng;
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
import java.util.List;

/**
 * Advanced sample demonstrating ground-level path following in Java.
 *
 * Key Concepts Demonstrated:
 * 1. Dual-Polyline Architecture: Layered base route (lower z-index) and traversed progress route (higher z-index).
 * 2. In-Place Polyline ID Updates: Stable IDs prevent render flickering during rapid real-time updates.
 * 3. 3D Altitude Modes: Dynamic switching between Clamp to Ground, Relative, and Absolute elevation.
 * 4. Occlusion Control: Toggling drawsOccludedSegments through terrain and buildings.
 * 5. Kinematic Heading Smoothing: Exponential moving average low-pass filter to smooth camera cornering.
 */
public class PathFollowingActivity extends AppCompatActivity implements OnMap3DViewReadyCallback {

    private Map3DView map3DView;
    private GoogleMap3D googleMap3D;

    // View Bindings
    private CardView controlsCard;
    private View cardHeader;
    private MaterialButton btnCollapse;
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

    // Control parameters
    private double cameraRange = 300.0;
    private double groundAltitude = 20.0;
    private double headingOffset = 0.0;
    private double cameraTilt = 70.0;
    private double followSpeedMps = 30.0;
    private int pathAltitudeMode = AltitudeMode.CLAMP_TO_GROUND;
    private double pathAltitudeOffset = 0.5;
    private boolean drawsOccludedSegments = true;

    // Path state
    private List<LatLngAltitude> currentPath = PathData.URBAN_PATH;
    private double[] cumulativeDistances = new double[0];
    private double totalDistance = 0.0;
    private double elapsedDistance = 0.0;
    private boolean isPlaying = false;
    private boolean isUserScrubbing = false;
    private Double currentHeading = null;

    private double getBaseAltitude() {
        return currentPath.equals(PathData.RURAL_PATH) ? 45.0 : 50.0;
    }

    // Polyline handles
    private Polyline staticRoutePolyline;
    private Polyline progressPolyline;

    // Animation & auto-fade handlers
    private Runnable animationRunnable;
    private final Handler animationHandler = new Handler(Looper.getMainLooper());
    private final Handler fadeHandler = new Handler(Looper.getMainLooper());
    private final Runnable fadeOutRunnable =
            () -> {
                if (controlsCard != null && !isCollapsed) {
                    controlsCard.animate().alpha(0.8f).setDuration(400).start();
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path_following);

        map3DView = findViewById(R.id.map3dView);
        map3DView.onCreate(savedInstanceState);
        map3DView.getMap3DViewAsync(this);

        initViews();
        loadPath(PathData.URBAN_PATH);
    }

    @Override
    public void onMap3DViewReady(@NonNull GoogleMap3D googleMap3D) {
        this.googleMap3D = googleMap3D;
        googleMap3D.setOnMapReadyListener(
                (map) -> {
                    drawPathPolylines();
                    updateCameraPositionForDistance(0.0);
                });

        scheduleControlsFade();
    }

    private void initViews() {
        controlsCard = findViewById(R.id.controls_card);
        cardHeader = findViewById(R.id.card_header);
        btnCollapse = findViewById(R.id.btn_collapse);

        if (btnCollapse != null) {
            btnCollapse.setOnClickListener(v -> toggleControlsCard());
        }
        if (cardHeader != null) {
            cardHeader.setOnClickListener(v -> toggleControlsCard());
        }

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

        setupEventListeners();
    }

    private void setupEventListeners() {
        rgEnvironment.setOnCheckedChangeListener(
                (group, checkedId) -> {
                    if (checkedId == R.id.rb_urban) {
                        switchEnvironment(PathData.URBAN_PATH);
                    } else if (checkedId == R.id.rb_rural) {
                        switchEnvironment(PathData.RURAL_PATH);
                    }
                });

        rgAltitudeMode.setOnCheckedChangeListener(
                (group, checkedId) -> {
                    if (checkedId == R.id.rb_clamp_to_ground) {
                        pathAltitudeMode = AltitudeMode.CLAMP_TO_GROUND;
                    } else if (checkedId == R.id.rb_relative_to_ground) {
                        pathAltitudeMode = AltitudeMode.RELATIVE_TO_GROUND;
                    } else if (checkedId == R.id.rb_relative_to_mesh) {
                        pathAltitudeMode = AltitudeMode.RELATIVE_TO_MESH;
                    } else if (checkedId == R.id.rb_absolute) {
                        pathAltitudeMode = AltitudeMode.ABSOLUTE;
                    } else {
                        pathAltitudeMode = AltitudeMode.CLAMP_TO_GROUND;
                    }
                    redrawPolylinesAndCamera();
                });

        switchDrawsOccludedSegments.setChecked(drawsOccludedSegments);
        switchDrawsOccludedSegments.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    drawsOccludedSegments = isChecked;
                    redrawPolylinesAndCamera();
                });

        pathAltitudeSlider.addOnChangeListener(
                (slider, value, fromUser) -> {
                    pathAltitudeOffset = value;
                    pathAltitudeSliderLabel.setText(
                            getString(R.string.path_height_format, pathAltitudeOffset));
                    redrawPolylinesAndCamera();
                });

        btnPlayPause.setOnClickListener(
                v -> {
                    if (isPlaying) {
                        pauseAnimation();
                    } else {
                        startAnimation();
                    }
                });

        progressSlider.addOnSliderTouchListener(
                new Slider.OnSliderTouchListener() {
                    @Override
                    public void onStartTrackingTouch(@NonNull Slider slider) {
                        isUserScrubbing = true;
                    }

                    @Override
                    public void onStopTrackingTouch(@NonNull Slider slider) {
                        isUserScrubbing = false;
                        elapsedDistance = totalDistance * slider.getValue();
                        updateCameraPositionForDistance(elapsedDistance);
                    }
                });

        progressSlider.addOnChangeListener(
                (slider, value, fromUser) -> {
                    if (fromUser) {
                        elapsedDistance = totalDistance * value;
                        updateCameraPositionForDistance(elapsedDistance);
                    }
                });

        rangeSlider.addOnChangeListener(
                (slider, value, fromUser) -> {
                    cameraRange = value;
                    rangeSliderLabel.setText(getString(R.string.camera_range_format, (int) cameraRange));
                    updateCameraPositionForDistance(elapsedDistance);
                });

        altitudeSlider.addOnChangeListener(
                (slider, value, fromUser) -> {
                    groundAltitude = value;
                    altitudeSliderLabel.setText(
                            getString(R.string.ground_altitude_format, (int) groundAltitude));
                    updateCameraPositionForDistance(elapsedDistance);
                });

        headingSlider.addOnChangeListener(
                (slider, value, fromUser) -> {
                    headingOffset = value;
                    headingSliderLabel.setText(
                            getString(R.string.heading_offset_format, (int) headingOffset));
                    updateCameraPositionForDistance(elapsedDistance);
                });

        tiltSlider.addOnChangeListener(
                (slider, value, fromUser) -> {
                    cameraTilt = value;
                    tiltSliderLabel.setText(getString(R.string.camera_tilt_format, (int) cameraTilt));
                    updateCameraPositionForDistance(elapsedDistance);
                });

        speedSlider.addOnChangeListener(
                (slider, value, fromUser) -> {
                    followSpeedMps = value;
                    speedSliderLabel.setText(getString(R.string.follow_speed_format, (int) followSpeedMps));
                });
    }

    private void switchEnvironment(List<LatLngAltitude> path) {
        pauseAnimation();
        currentHeading = null;
        elapsedDistance = 0.0;
        progressSlider.setValue(0f);
        clearPolylines();

        if (path.equals(PathData.RURAL_PATH)) {
            cameraRange = 450.0;
            groundAltitude = 40.0;
            cameraTilt = 75.0;
            altitudeSlider.setValueTo(2000f);
            altitudeSlider.setValue(40f);
            rangeSlider.setValue(450f);
            tiltSlider.setValue(75f);
        } else {
            cameraRange = 300.0;
            groundAltitude = 20.0;
            cameraTilt = 70.0;
            altitudeSlider.setValueTo(200f);
            altitudeSlider.setValue(20f);
            rangeSlider.setValue(300f);
            tiltSlider.setValue(70f);
        }

        rangeSliderLabel.setText(getString(R.string.camera_range_format, (int) cameraRange));
        altitudeSliderLabel.setText(getString(R.string.ground_altitude_format, (int) groundAltitude));
        tiltSliderLabel.setText(getString(R.string.camera_tilt_format, (int) cameraTilt));

        loadPath(path);
        drawPathPolylines();
        updateCameraPositionForDistance(0.0);
    }

    private void loadPath(List<LatLngAltitude> path) {
        currentPath = path;
        cumulativeDistances = PathEngine.calculateCumulativeDistances(path);
        totalDistance = (cumulativeDistances.length > 0) ? cumulativeDistances[cumulativeDistances.length - 1] : 0.0;
    }

    private void drawPathPolylines() {
        drawStaticRoutePolyline();
        if (!currentPath.isEmpty()) {
            LatLng firstPt = new LatLng(currentPath.get(0).getLatitude(), currentPath.get(0).getLongitude());
            updateProgressPolyline(elapsedDistance, firstPt, 0);
        }
    }

    private void drawStaticRoutePolyline() {
        if (googleMap3D == null || currentPath.isEmpty()) return;

        List<LatLngAltitude> staticVertices =
                PathEngine.buildStaticVertices(currentPath, pathAltitudeMode, getBaseAltitude(), pathAltitudeOffset);

        PolylineOptions staticOptions = new PolylineOptions();
        staticOptions.setId(PathEngine.STATIC_POLYLINE_ID); // Fixed ID prevents flickering
        staticOptions.setPath(staticVertices);
        staticOptions.setStrokeColor(Color.parseColor("#4285F4")); // Wide blue route (16dp)
        staticOptions.setStrokeWidth(16.0);
        staticOptions.setZIndex(1);
        staticOptions.setAltitudeMode(pathAltitudeMode);
        staticOptions.setDrawsOccludedSegments(drawsOccludedSegments);

        staticRoutePolyline = googleMap3D.addPolyline(staticOptions);
    }

    private void updateProgressPolyline(double dist, LatLng currentLatLng, int index) {
        if (googleMap3D == null || currentPath.isEmpty() || totalDistance <= 0.0) return;

        List<LatLngAltitude> progressCoordinates =
                PathEngine.buildProgressVertices(
                        currentPath,
                        cumulativeDistances,
                        dist,
                        currentLatLng,
                        index,
                        pathAltitudeMode,
                        getBaseAltitude(),
                        pathAltitudeOffset);

        PolylineOptions progressOptions = new PolylineOptions();
        progressOptions.setId(PathEngine.PROGRESS_POLYLINE_ID);
        progressOptions.setPath(progressCoordinates);
        progressOptions.setStrokeColor(Color.parseColor("#9C27B0")); // Narrow purple progress (8dp)
        progressOptions.setStrokeWidth(8.0);
        progressOptions.setZIndex(2);
        progressOptions.setAltitudeMode(pathAltitudeMode);
        progressOptions.setDrawsOccludedSegments(drawsOccludedSegments);

        progressPolyline = googleMap3D.addPolyline(progressOptions);
    }

    private void updateCameraPositionForDistance(double dist) {
        if (googleMap3D == null || currentPath.isEmpty()) return;

        InterpolatedPathPoint point = PathEngine.interpolatePoint(currentPath, cumulativeDistances, dist);
        double targetHeading =
                PathEngine.smoothHeading(
                        point.bearing + headingOffset,
                        currentHeading,
                        isUserScrubbing,
                        isPlaying,
                        0.12);
        currentHeading = targetHeading;

        double cameraTargetAltitude =
                PathEngine.calculateCameraAltitude(
                        pathAltitudeMode, getBaseAltitude(), point.altitude, groundAltitude);

        Camera newCamera =
                new Camera(
                        new LatLngAltitude(
                                point.latLng.latitude, point.latLng.longitude, cameraTargetAltitude),
                        targetHeading,
                        cameraTilt,
                        /* roll= */ 0.0,
                        cameraRange);

        googleMap3D.setCamera(newCamera);
        updateProgressPolyline(dist, point.latLng, point.waypointIndex);
    }

    private void startAnimation() {
        if (isPlaying) return;
        isPlaying = true;
        btnPlayPause.setIconResource(R.drawable.pause_24px);

        long frameDurationMs = 16L;
        animationRunnable =
                new Runnable() {
                    private long lastTime = System.currentTimeMillis();

                    @Override
                    public void run() {
                        if (!isPlaying) return;

                        long now = System.currentTimeMillis();
                        double dt = (now - lastTime) / 1000.0;
                        lastTime = now;

                        double stepDistance = followSpeedMps * dt;
                        elapsedDistance += stepDistance;

                        if (elapsedDistance >= totalDistance) {
                            elapsedDistance = 0.0;
                        }

                        if (!isUserScrubbing && totalDistance > 0) {
                            float progress = (float) Math.max(0.0, Math.min(1.0, elapsedDistance / totalDistance));
                            progressSlider.setValue(progress);
                        }

                        updateCameraPositionForDistance(elapsedDistance);
                        animationHandler.postDelayed(this, frameDurationMs);
                    }
                };
        animationHandler.post(animationRunnable);
    }

    private void pauseAnimation() {
        isPlaying = false;
        btnPlayPause.setIconResource(R.drawable.play_arrow_24px);
        if (animationRunnable != null) {
            animationHandler.removeCallbacks(animationRunnable);
            animationRunnable = null;
        }
    }

    private void redrawPolylinesAndCamera() {
        clearPolylines();
        drawPathPolylines();
        updateCameraPositionForDistance(elapsedDistance);
    }

    private void clearPolylines() {
        if (staticRoutePolyline != null) {
            staticRoutePolyline.remove();
            staticRoutePolyline = null;
        }
        if (progressPolyline != null) {
            progressPolyline.remove();
            progressPolyline = null;
        }
    }

    private void toggleControlsCard() {
        if (isCollapsed) expandControls(); else collapseControls();
    }

    private void collapseControls() {
        if (controlsCard == null) return;
        isCollapsed = true;
        fadeHandler.removeCallbacks(fadeOutRunnable);
        if (btnCollapse != null) {
            btnCollapse.setIconResource(R.drawable.expand_less_24px);
            btnCollapse.setContentDescription(getString(R.string.expand_controls));
        }

        int headerHeight =
                (cardHeader != null && cardHeader.getHeight() > 0)
                        ? cardHeader.getHeight()
                        : (int) (48 * getResources().getDisplayMetrics().density);
        float targetTranslationY = Math.max(0, controlsCard.getHeight() - headerHeight);
        controlsCard.animate().translationY(targetTranslationY).alpha(0.9f).setDuration(300).start();
    }

    private void expandControls() {
        if (controlsCard == null) return;
        isCollapsed = false;
        if (btnCollapse != null) {
            btnCollapse.setIconResource(R.drawable.expand_more_24px);
            btnCollapse.setContentDescription(getString(R.string.collapse_controls));
        }
        controlsCard.animate().translationY(0f).alpha(1.0f).setDuration(250).start();
        scheduleControlsFade();
    }

    private void scheduleControlsFade() {
        fadeHandler.removeCallbacks(fadeOutRunnable);
        fadeHandler.postDelayed(fadeOutRunnable, 3000L);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN || ev.getAction() == MotionEvent.ACTION_MOVE) {
            if (controlsCard != null && !isCollapsed) {
                controlsCard.animate().alpha(1.0f).setDuration(150).start();
                scheduleControlsFade();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onResume() {
        super.onResume();
        map3DView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        map3DView.onPause();
        pauseAnimation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pauseAnimation();
        fadeHandler.removeCallbacks(fadeOutRunnable);
        clearPolylines();
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
