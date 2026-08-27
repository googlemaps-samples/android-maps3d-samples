/*
 * Copyright 2025 Google LLC
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

package com.example.maps3djava.advancedcameraanimation;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Choreographer;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.maps3d.common.PositionAndHeading;
import com.example.maps3d.common.RouteEngine;
import com.example.maps3d.common.TourData;
import com.example.maps3dcommon.R;
import com.example.maps3djava.sampleactivity.SampleBaseActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.model.AltitudeMode;
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.FlyToOptions;
import com.google.android.gms.maps3d.model.LatLngAltitude;
import com.google.android.gms.maps3d.model.Model;
import com.google.android.gms.maps3d.model.ModelOptions;
import com.google.android.gms.maps3d.model.Orientation;
import com.google.android.gms.maps3d.model.Vector3D;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.maps.android.SphericalUtil;
import java.util.Arrays;
import java.util.List;

enum AnimationApproach {
    SIMPLE_FLY_TO,
    KEYFRAME_TOUR,
    DISPATCHER_FRAME_LOOP,
    ORBIT_360_SPIN
}

interface JavaCameraKeyframe {
    String getStepTitle();
    String getStepDescription();

    class FlyTo implements JavaCameraKeyframe {
        private final String stepTitle;
        private final String stepDescription;
        private final Camera targetCamera;
        private final long durationMs;

        public FlyTo(String stepTitle, String stepDescription, Camera targetCamera, long durationMs) {
            this.stepTitle = stepTitle;
            this.stepDescription = stepDescription;
            this.targetCamera = targetCamera;
            this.durationMs = durationMs;
        }

        @Override public String getStepTitle() { return stepTitle; }
        @Override public String getStepDescription() { return stepDescription; }
        public Camera getTargetCamera() { return targetCamera; }
        public long getDurationMs() { return durationMs; }
    }

    class DwellPause implements JavaCameraKeyframe {
        private final String stepTitle;
        private final String stepDescription;
        private final long durationMs;

        public DwellPause(String stepTitle, String stepDescription, long durationMs) {
            this.stepTitle = stepTitle;
            this.stepDescription = stepDescription;
            this.durationMs = durationMs;
        }

        @Override public String getStepTitle() { return stepTitle; }
        @Override public String getStepDescription() { return stepDescription; }
        public long getDurationMs() { return durationMs; }
    }

    class OrbitAround implements JavaCameraKeyframe {
        private final String stepTitle;
        private final String stepDescription;
        private final LatLng center;
        private final double altitude;
        private final double range;
        private final double tilt;
        private final double startHeading;
        private final double endHeading;
        private final long durationMs;

        public OrbitAround(
                String stepTitle,
                String stepDescription,
                LatLng center,
                double altitude,
                double range,
                double tilt,
                double startHeading,
                double endHeading,
                long durationMs) {
            this.stepTitle = stepTitle;
            this.stepDescription = stepDescription;
            this.center = center;
            this.altitude = altitude;
            this.range = range;
            this.tilt = tilt;
            this.startHeading = startHeading;
            this.endHeading = endHeading;
            this.durationMs = durationMs;
        }

        @Override public String getStepTitle() { return stepTitle; }
        @Override public String getStepDescription() { return stepDescription; }
        public LatLng getCenter() { return center; }
        public double getAltitude() { return altitude; }
        public double getRange() { return range; }
        public double getTilt() { return tilt; }
        public double getStartHeading() { return startHeading; }
        public double getEndHeading() { return endHeading; }
        public long getDurationMs() { return durationMs; }
    }
}

/**
 * Advanced Camera Animation demonstrating cinematic 3D camera controls and 3D airplane model tracking in Java.
 */
public class AdvancedCameraAnimationActivity extends SampleBaseActivity {

    @NonNull
    @Override
    public String getTAG() {
        return "AdvancedCameraAnimation";
    }

    @NonNull
    @Override
    public Camera getInitialCamera() {
        LatLng firstLoc = TourData.AIRPLANE_FLIGHT_PATH.get(0);
        double initialHeading = SphericalUtil.computeHeading(firstLoc, TourData.AIRPLANE_FLIGHT_PATH.get(1));
        return new Camera(
                /* center= */ new LatLngAltitude(
                        /* latitude= */ firstLoc.latitude,
                        /* longitude= */ firstLoc.longitude,
                        /* altitude= */ 200.0),
                /* heading= */ normalizeHeading(initialHeading),
                /* tilt= */ 65.0,
                /* roll= */ 0.0,
                /* range= */ 600.0);
    }

    private Model airplaneModel;
    private int currentStepIndex = 0;
    private boolean isPlaying = false;
    private AnimationApproach selectedApproach = AnimationApproach.DISPATCHER_FRAME_LOOP;

    private Button btnPlayPause;
    private TextView tvTourStatus;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Choreographer.FrameCallback frameDispatcherCallback;
    private Choreographer.FrameCallback orbitCallback;
    private Choreographer.FrameCallback keyframeOrbitCallback;

    private double[] cumulativeDistances;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cumulativeDistances = RouteEngine.calculateCumulativeDistances(TourData.AIRPLANE_FLIGHT_PATH);

        ViewGroup mapContainer = findViewById(R.id.map_container);
        if (mapContainer != null) {
            getLayoutInflater().inflate(R.layout.control_panel_advanced_animation, mapContainer, true);
        }

        MaterialToolbar topBar = findViewById(R.id.top_bar);
        if (topBar != null) {
            topBar.setTitle("Advanced Camera Animation");
            topBar.setNavigationOnClickListener(v -> finish());
        }

        tvTourStatus = findViewById(R.id.tv_tour_status);

        RadioGroup rgApproach = findViewById(R.id.rg_animation_approach);
        if (rgApproach != null) {
            rgApproach.setOnCheckedChangeListener(
                    (group, checkedId) -> {
                        if (checkedId == R.id.rb_simple_fly_to) {
                            selectedApproach = AnimationApproach.SIMPLE_FLY_TO;
                        } else if (checkedId == R.id.rb_keyframe_tour) {
                            selectedApproach = AnimationApproach.KEYFRAME_TOUR;
                        } else if (checkedId == R.id.rb_dispatcher_frame_loop) {
                            selectedApproach = AnimationApproach.DISPATCHER_FRAME_LOOP;
                        } else if (checkedId == R.id.rb_orbit_360_spin) {
                            selectedApproach = AnimationApproach.ORBIT_360_SPIN;
                        }
                        resetAndRestartTour();
                    });
        }

        Button btnReset = findViewById(R.id.btn_reset);
        if (btnReset != null) {
            btnReset.setOnClickListener(v -> resetAndRestartTour());
        }

        btnPlayPause = findViewById(R.id.btn_play_pause);
        if (btnPlayPause != null) {
            btnPlayPause.setOnClickListener(
                    v -> {
                        if (isPlaying) {
                            stopTour();
                        } else {
                            startSelectedApproach();
                        }
                    });
        }
    }

    private void updatePlayPauseButtonState() {
        if (btnPlayPause != null) {
            btnPlayPause.setText(isPlaying ? "Pause" : "Play");
        }
    }

    @Override
    public void onMap3DViewReady(@NonNull GoogleMap3D googleMap3D) {
        super.onMap3DViewReady(googleMap3D);

        LatLng startLoc = TourData.AIRPLANE_FLIGHT_PATH.get(0);
        double initialHeading = SphericalUtil.computeHeading(startLoc, TourData.AIRPLANE_FLIGHT_PATH.get(1));
        updateAirplaneModel(startLoc, initialHeading + 180.0);

        handler.postDelayed(this::startSelectedApproach, 1000L);
    }

    private void updateAirplaneModel(LatLng targetLatLng, double planeHeadingDeg) {
        if (googleMap3D == null) return;

        ModelOptions opts = new ModelOptions();
        opts.setId(TourData.MODEL_ID);
        opts.setPosition(
                new LatLngAltitude(
                        /* latitude= */ targetLatLng.latitude,
                        /* longitude= */ targetLatLng.longitude,
                        /* altitude= */ 200.0));
        opts.setAltitudeMode(AltitudeMode.ABSOLUTE);
        opts.setOrientation(
                new Orientation(
                        /* heading= */ normalizeHeading(planeHeadingDeg),
                        /* tilt= */ -90.0,
                        /* roll= */ 0.0));
        opts.setUrl(TourData.PLANE_URL);
        opts.setScale(new Vector3D(/* x= */ 0.08, /* y= */ 0.08, /* z= */ 0.08));

        airplaneModel = googleMap3D.addModel(opts);
    }

    private void startSelectedApproach() {
        stopTour();
        switch (selectedApproach) {
            case SIMPLE_FLY_TO:
                runSimpleFlyTo();
                break;
            case KEYFRAME_TOUR:
                startOrResumeTour();
                break;
            case DISPATCHER_FRAME_LOOP:
                runFrameDispatcherLoop();
                break;
            case ORBIT_360_SPIN:
                run360OrbitSpin();
                break;
        }
    }

    private void runSimpleFlyTo() {
        if (googleMap3D == null) return;

        stopTour();
        isPlaying = true;
        updatePlayPauseButtonState();
        if (tvTourStatus != null) {
            tvTourStatus.setText(R.string.approach_simple_fly_to);
        }

        LatLng targetLoc = TourData.AIRPLANE_FLIGHT_PATH.get(TourData.AIRPLANE_FLIGHT_PATH.size() - 1);
        double flightHeading =
                SphericalUtil.computeHeading(
                        TourData.AIRPLANE_FLIGHT_PATH.get(TourData.AIRPLANE_FLIGHT_PATH.size() - 2), targetLoc);
        updateAirplaneModel(targetLoc, flightHeading + 180.0);

        Camera targetCam =
                new Camera(
                        new LatLngAltitude(targetLoc.latitude, targetLoc.longitude, 200.0),
                        normalizeHeading(flightHeading),
                        65.0,
                        0.0,
                        600.0);

        googleMap3D.setCameraAnimationEndListener(
                () -> {
                    googleMap3D.setCameraAnimationEndListener(null);
                    isPlaying = false;
                    updatePlayPauseButtonState();
                    if (tvTourStatus != null) {
                        tvTourStatus.setText(R.string.aerial_tour_status_finished);
                    }
                });

        googleMap3D.flyCameraTo(new FlyToOptions(/* camera= */ targetCam, /* durationMs= */ 1500L));
    }

    private void startOrResumeTour() {
        stopTour();
        isPlaying = true;
        updatePlayPauseButtonState();
        currentStepIndex = 0;
        executeKeyframeStep(currentStepIndex);
    }

    private void executeKeyframeStep(int stepIndex) {
        if (!isPlaying || googleMap3D == null || stepIndex >= SAN_FRANCISCO_TOUR.size()) {
            isPlaying = false;
            updatePlayPauseButtonState();
            if (tvTourStatus != null) {
                tvTourStatus.setText(R.string.aerial_tour_status_finished);
            }
            return;
        }

        JavaCameraKeyframe step = SAN_FRANCISCO_TOUR.get(stepIndex);
        if (tvTourStatus != null) {
            tvTourStatus.setText(
                    getString(R.string.aerial_tour_status_running, stepIndex + 1, SAN_FRANCISCO_TOUR.size(), step.getStepTitle()));
        }

        if (step instanceof JavaCameraKeyframe.FlyTo) {
            JavaCameraKeyframe.FlyTo flyStep = (JavaCameraKeyframe.FlyTo) step;
            LatLng targetCenter =
                    new LatLng(
                            flyStep.getTargetCamera().getCenter().getLatitude(),
                            flyStep.getTargetCamera().getCenter().getLongitude());
            updateAirplaneModel(targetCenter, flyStep.getTargetCamera().getHeading() + 180.0);

            googleMap3D.setCameraAnimationEndListener(
                    () -> {
                        googleMap3D.setCameraAnimationEndListener(null);
                        currentStepIndex++;
                        executeKeyframeStep(currentStepIndex);
                    });
            googleMap3D.flyCameraTo(
                    new FlyToOptions(
                            /* camera= */ flyStep.getTargetCamera(),
                            /* durationMs= */ flyStep.getDurationMs()));
        } else if (step instanceof JavaCameraKeyframe.DwellPause) {
            JavaCameraKeyframe.DwellPause pauseStep = (JavaCameraKeyframe.DwellPause) step;
            handler.postDelayed(
                    () -> {
                        currentStepIndex++;
                        executeKeyframeStep(currentStepIndex);
                    },
                    pauseStep.getDurationMs());
        } else if (step instanceof JavaCameraKeyframe.OrbitAround) {
            JavaCameraKeyframe.OrbitAround orbitStep = (JavaCameraKeyframe.OrbitAround) step;
            long totalMs = orbitStep.getDurationMs();

            keyframeOrbitCallback =
                    new Choreographer.FrameCallback() {
                        private long startTimeNanos = 0L;

                        @Override
                        public void doFrame(long frameTimeNanos) {
                            if (!isPlaying || googleMap3D == null) return;

                            if (startTimeNanos == 0L) {
                                startTimeNanos = frameTimeNanos;
                                Choreographer.getInstance().postFrameCallback(this);
                                return;
                            }

                            long elapsedMs = (frameTimeNanos - startTimeNanos) / 1_000_000L;
                            double t = Math.min(1.0, (double) elapsedMs / totalMs);
                            double orbitHeading = interpolateAngle(orbitStep.getStartHeading(), orbitStep.getEndHeading(), t);

                            Camera updatedCam =
                                    new Camera(
                                            new LatLngAltitude(orbitStep.getCenter().latitude, orbitStep.getCenter().longitude, orbitStep.getAltitude()),
                                            normalizeHeading(orbitHeading),
                                            orbitStep.getTilt(),
                                            0.0,
                                            orbitStep.getRange());

                            updateAirplaneModel(orbitStep.getCenter(), orbitHeading + 180.0);
                            googleMap3D.setCamera(updatedCam);

                            if (t >= 1.0) {
                                currentStepIndex++;
                                executeKeyframeStep(currentStepIndex);
                                return;
                            }
                            Choreographer.getInstance().postFrameCallback(this);
                        }
                    };
            Choreographer.getInstance().postFrameCallback(keyframeOrbitCallback);
        }
    }

    private void runFrameDispatcherLoop() {
        stopTour();
        isPlaying = true;
        updatePlayPauseButtonState();
        if (tvTourStatus != null) {
            tvTourStatus.setText(R.string.approach_dispatcher_frame_loop);
        }

        double totalDistance = cumulativeDistances[cumulativeDistances.length - 1];
        double flightSpeedMps = 400.0;

        frameDispatcherCallback =
                new Choreographer.FrameCallback() {
                    private long lastTimeNanos = 0L;
                    private double elapsedDistance = 0.0;

                    @Override
                    public void doFrame(long frameTimeNanos) {
                        if (!isPlaying || googleMap3D == null) return;

                        if (lastTimeNanos == 0L) {
                            lastTimeNanos = frameTimeNanos;
                            Choreographer.getInstance().postFrameCallback(this);
                            return;
                        }

                        double dt = (frameTimeNanos - lastTimeNanos) / 1_000_000_000.0;
                        lastTimeNanos = frameTimeNanos;
                        elapsedDistance += flightSpeedMps * dt;

                        if (elapsedDistance >= totalDistance) {
                            elapsedDistance = totalDistance;
                            PositionAndHeading posAndHeading =
                                    RouteEngine.calculatePositionAndHeading(
                                            /* route= */ TourData.AIRPLANE_FLIGHT_PATH,
                                            /* cumulativeDistances= */ cumulativeDistances,
                                            /* distance= */ elapsedDistance,
                                            /* lookaheadDistance= */ 30.0);
                            double planeHeading = posAndHeading.getHeading() + 180.0;
                            updateAirplaneModel(posAndHeading.getPosition(), planeHeading);

                            Camera finalCam =
                                    new Camera(
                                            new LatLngAltitude(posAndHeading.getPosition().latitude, posAndHeading.getPosition().longitude, 200.0),
                                            normalizeHeading(posAndHeading.getHeading()),
                                            65.0,
                                            0.0,
                                            600.0);
                            googleMap3D.setCamera(finalCam);
                            isPlaying = false;
                            updatePlayPauseButtonState();
                            if (tvTourStatus != null) {
                                tvTourStatus.setText(R.string.aerial_tour_status_finished);
                            }
                            return;
                        }

                        PositionAndHeading posAndHeading =
                                RouteEngine.calculatePositionAndHeading(
                                        /* route= */ TourData.AIRPLANE_FLIGHT_PATH,
                                        /* cumulativeDistances= */ cumulativeDistances,
                                        /* distance= */ elapsedDistance,
                                        /* lookaheadDistance= */ 30.0);

                        double planeHeading = posAndHeading.getHeading() + 180.0;
                        updateAirplaneModel(posAndHeading.getPosition(), planeHeading);

                        Camera updatedCam =
                                new Camera(
                                        new LatLngAltitude(posAndHeading.getPosition().latitude, posAndHeading.getPosition().longitude, 200.0),
                                        normalizeHeading(posAndHeading.getHeading()),
                                        65.0,
                                        0.0,
                                        600.0);
                        googleMap3D.setCamera(updatedCam);
                        Choreographer.getInstance().postFrameCallback(this);
                    }
                };
        Choreographer.getInstance().postFrameCallback(frameDispatcherCallback);
    }

    private void run360OrbitSpin() {
        stopTour() ;
        isPlaying = true;
        updatePlayPauseButtonState();
        if (tvTourStatus != null) {
            tvTourStatus.setText(R.string.approach_orbit_360_spin);
        }

        LatLng targetCenter = TourData.AIRPLANE_FLIGHT_PATH.get(0);
        updateAirplaneModel(targetCenter, 105.0 + 180.0);

        long totalMs = 6000L;
        double startHeading = 105.0;

        orbitCallback =
                new Choreographer.FrameCallback() {
                    private long startTimeNanos = 0L;

                    @Override
                    public void doFrame(long frameTimeNanos) {
                        if (!isPlaying || googleMap3D == null) return;

                        if (startTimeNanos == 0L) {
                            startTimeNanos = frameTimeNanos;
                            Choreographer.getInstance().postFrameCallback(this);
                            return;
                        }

                        long elapsedMs = (frameTimeNanos - startTimeNanos) / 1_000_000L;
                        double t = Math.min(1.0, (double) elapsedMs / totalMs);
                        double headingDeg = (startHeading + t * 360.0) % 360.0;

                        Camera currentCam =
                                new Camera(
                                        new LatLngAltitude(targetCenter.latitude, targetCenter.longitude, 200.0),
                                        normalizeHeading(headingDeg),
                                        65.0,
                                        0.0,
                                        600.0);
                        googleMap3D.setCamera(currentCam);

                        if (t >= 1.0) {
                            isPlaying = false;
                            updatePlayPauseButtonState();
                            if (tvTourStatus != null) {
                                tvTourStatus.setText(R.string.aerial_tour_status_finished);
                            }
                            return;
                        }

                        Choreographer.getInstance().postFrameCallback(this);
                    }
                };
        Choreographer.getInstance().postFrameCallback(orbitCallback);
    }

    private void stopTour() {
        isPlaying = false;
        updatePlayPauseButtonState();
        handler.removeCallbacksAndMessages(null);
        if (frameDispatcherCallback != null) {
            Choreographer.getInstance().removeFrameCallback(frameDispatcherCallback);
            frameDispatcherCallback = null;
        }
        if (orbitCallback != null) {
            Choreographer.getInstance().removeFrameCallback(orbitCallback);
            orbitCallback = null;
        }
        if (keyframeOrbitCallback != null) {
            Choreographer.getInstance().removeFrameCallback(keyframeOrbitCallback);
            keyframeOrbitCallback = null;
        }
        if (googleMap3D != null) {
            googleMap3D.setCameraAnimationEndListener(null);
            googleMap3D.stopCameraAnimation();
        }
    }

    public void resetAndRestartTour() {
        stopTour();
        currentStepIndex = 0;

        LatLng startLoc = TourData.AIRPLANE_FLIGHT_PATH.get(0);
        double initialHeading = SphericalUtil.computeHeading(startLoc, TourData.AIRPLANE_FLIGHT_PATH.get(1));
        updateAirplaneModel(startLoc, initialHeading + 180.0);

        Camera resetCam =
                new Camera(
                        new LatLngAltitude(startLoc.latitude, startLoc.longitude, 200.0),
                        normalizeHeading(initialHeading),
                        /* tilt= */ 65.0,
                        /* roll= */ 0.0,
                        /* range= */ 600.0);

        if (googleMap3D != null) {
            googleMap3D.setCamera(resetCam);
        }

        if (tvTourStatus != null) {
            tvTourStatus.setText(R.string.aerial_tour_status_idle);
        }

        handler.postDelayed(this::startSelectedApproach, 300L);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTour();
    }

    private static final List<JavaCameraKeyframe> SAN_FRANCISCO_TOUR =
            Arrays.asList(
                    new JavaCameraKeyframe.FlyTo(
                            "1. Golden Gate Bridge Flight",
                            "3D Airplane flight over Golden Gate Bridge",
                            new Camera(
                                    new LatLngAltitude(37.8199, -122.4783, 200.0),
                                    /* heading= */ 105.0,
                                    /* tilt= */ 65.0,
                                    /* roll= */ 0.0,
                                    /* range= */ 600.0),
                            2500L),
                    new JavaCameraKeyframe.DwellPause(
                            "2. Mid-Air Observation", "Dwell pause observing 3D airplane over Golden Gate", 1500L),
                    new JavaCameraKeyframe.OrbitAround(
                            "3. Golden Gate 360° Orbit",
                            "360° orbital camera spin around flying airplane",
                            new LatLng(37.8199, -122.4783),
                            200.0,
                            600.0,
                            65.0,
                            105.0,
                            465.0,
                            4000L),
                    new JavaCameraKeyframe.FlyTo(
                            "4. Transit to Coit Tower",
                            "Airplane flight to Coit Tower Landmark",
                            new Camera(
                                    new LatLngAltitude(37.8024, -122.4058, 200.0),
                                    /* heading= */ 105.0,
                                    /* tilt= */ 65.0,
                                    /* roll= */ 0.0,
                                    /* range= */ 600.0),
                            3000L));

    private static double normalizeHeading(double headingDeg) {
        double normalized = headingDeg % 360.0;
        return normalized < 0.0 ? normalized + 360.0 : normalized;
    }

    private static double interpolateAngle(double start, double end, double fraction) {
        double diff = (end - start) % 360.0;
        if (diff > 180.0) diff -= 360.0;
        if (diff < -180.0) diff += 360.0;
        return (start + diff * fraction + 360.0) % 360.0;
    }
}
