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

interface CameraKeyframe {

    String getStepTitle();

    String getStepDescription();

    class FlyTo implements CameraKeyframe {

        private final String stepTitle;
        private final String stepDescription;
        private final Camera targetCamera;
        private final long durationMs;

        public FlyTo(
            String stepTitle,
            String stepDescription,
            Camera targetCamera,
            long durationMs) {
            this.stepTitle = stepTitle;
            this.stepDescription = stepDescription;
            this.targetCamera = targetCamera;
            this.durationMs = durationMs;
        }

        @Override
        public String getStepTitle() {
            return stepTitle;
        }

        @Override
        public String getStepDescription() {
            return stepDescription;
        }

        public Camera getTargetCamera() {
            return targetCamera;
        }

        public long getDurationMs() {
            return durationMs;
        }
    }

    class DwellPause implements CameraKeyframe {

        private final String stepTitle;
        private final String stepDescription;
        private final long durationMs;

        public DwellPause(String stepTitle, String stepDescription, long durationMs) {
            this.stepTitle = stepTitle;
            this.stepDescription = stepDescription;
            this.durationMs = durationMs;
        }

        @Override
        public String getStepTitle() {
            return stepTitle;
        }

        @Override
        public String getStepDescription() {
            return stepDescription;
        }

        public long getDurationMs() {
            return durationMs;
        }
    }

    class OrbitAround implements CameraKeyframe {

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

        @Override
        public String getStepTitle() {
            return stepTitle;
        }

        @Override
        public String getStepDescription() {
            return stepDescription;
        }

        public LatLng getCenter() {
            return center;
        }

        public double getAltitude() {
            return altitude;
        }

        public double getRange() {
            return range;
        }

        public double getTilt() {
            return tilt;
        }

        public double getStartHeading() {
            return startHeading;
        }

        public double getEndHeading() {
            return endHeading;
        }

        public long getDurationMs() {
            return durationMs;
        }
    }
}

public class AdvancedCameraAnimationActivity extends SampleBaseActivity {

    @NonNull
    @Override
    public String getTAG() {
        return "AdvancedCameraAnimation";
    }

    @NonNull
    @Override
    public Camera getInitialCamera() {
        LatLng firstLoc = AIRPLANE_FLIGHT_PATH.get(0);
        double initialHeading =
            SphericalUtil.computeHeading(firstLoc, AIRPLANE_FLIGHT_PATH.get(1));
        return new Camera(
            new LatLngAltitude(firstLoc.latitude, firstLoc.longitude, 200.0),
            normalizeHeading(initialHeading),
            /* tilt= */ 65.0,
            /* roll= */ 0.0,
            /* range= */ 600.0);
    }

    private Model airplaneModel;
    private int currentStepIndex = 0;
    private boolean isPlaying = false;
    private AnimationApproach selectedApproach = AnimationApproach.DISPATCHER_FRAME_LOOP;

    private Choreographer.FrameCallback frameDispatcherCallback;
    private Choreographer.FrameCallback orbitCallback;
    private Choreographer.FrameCallback keyframeOrbitCallback;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final double[] cumulativeDistances =
        RouteEngine.calculateCumulativeDistances(AIRPLANE_FLIGHT_PATH);

    private Button btnPlayPause;
    private TextView tvTourStatus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate control panel overlay into the map container managed by SampleBaseActivity
        ViewGroup container = findViewById(R.id.map_container);
        if (container != null) {
            getLayoutInflater().inflate(R.layout.control_panel_advanced_animation, container, true);
        }

        MaterialToolbar topBar = findViewById(R.id.top_bar);
        if (topBar != null) {
            topBar.setTitle(R.string.feature_title_advanced_camera_animation);
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
                    } else {
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
        runOnUiThread(() -> {
            if (btnPlayPause != null) {
                btnPlayPause.setText(isPlaying ? R.string.pause : R.string.play);
            }
        });
    }

    @Override
    public void onMap3DViewReady(@NonNull GoogleMap3D googleMap3D) {
        super.onMap3DViewReady(googleMap3D);

        // Instantiate 3D Airplane Model on map at initial start position
        LatLng startLoc = AIRPLANE_FLIGHT_PATH.get(0);
        double initialHeading =
            SphericalUtil.computeHeading(startLoc, AIRPLANE_FLIGHT_PATH.get(1));
        updateAirplaneModel(startLoc, initialHeading + 180.0);

        // Auto-start smooth flight animation after 1 second delay
        handler.postDelayed(this::startSelectedApproach, 1000L);
    }

    /**
     * Updates the 3D Airplane Model's position and orientation on the map. Note: Calling
     * `map.addModel(opts)` continuously with the same `id` string is the recommended approach for
     * dynamically updating a model's location.
     * Remote URLs: Models should be hosted and loaded via external URL.
     */
    private void updateAirplaneModel(LatLng targetLatLng, double planeHeadingDeg) {
        if (googleMap3D != null) {
            ModelOptions opts = new ModelOptions();
            opts.setId(MODEL_ID);
            opts.setPosition(
                new LatLngAltitude(targetLatLng.latitude, targetLatLng.longitude, 200.0));
            opts.setAltitudeMode(AltitudeMode.ABSOLUTE);
            opts.setOrientation(
                new Orientation(
                    /* heading= */ normalizeHeading(planeHeadingDeg),
                    /* tilt= */ -90.0,
                    /* roll= */ 0.0));
            opts.setUrl(PLANE_URL);
            opts.setScale(new Vector3D(0.08, 0.08, 0.08));
            airplaneModel = googleMap3D.addModel(opts);
        }
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
            default:
                run360OrbitSpin();
                break;
        }
    }

    private void runSimpleFlyTo() {
        stopTour();
        isPlaying = true;
        updatePlayPauseButtonState();
        if (tvTourStatus != null) {
            tvTourStatus.setText(R.string.approach_simple_fly_to);
        }

        LatLng targetLoc = AIRPLANE_FLIGHT_PATH.get(AIRPLANE_FLIGHT_PATH.size() - 1);
        LatLng prevLoc = AIRPLANE_FLIGHT_PATH.get(AIRPLANE_FLIGHT_PATH.size() - 2);
        double flightHeading = SphericalUtil.computeHeading(prevLoc, targetLoc);
        updateAirplaneModel(targetLoc, flightHeading + 180.0);

        Camera targetCam =
            new Camera(
                new LatLngAltitude(targetLoc.latitude, targetLoc.longitude, 200.0),
                normalizeHeading(flightHeading),
                /* tilt= */ 65.0,
                /* roll= */ 0.0,
                /* range= */ 600.0);

        if (googleMap3D != null) {
            googleMap3D.setCameraAnimationEndListener(
                () -> {
                    if (googleMap3D != null) {
                        googleMap3D.setCameraAnimationEndListener(null);
                    }
                    isPlaying = false;
                    updatePlayPauseButtonState();
                    if (tvTourStatus != null) {
                        tvTourStatus.setText(R.string.aerial_tour_status_finished);
                    }
                });
            googleMap3D.flyCameraTo(new FlyToOptions(targetCam, 1500L));
        }
    }

    /**
     * Executes multi-step keyframe queue tour smoothly stage by stage.
     */
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
            if (tvTourStatus != null && stepIndex >= SAN_FRANCISCO_TOUR.size()) {
                tvTourStatus.setText(R.string.aerial_tour_status_finished);
            }
            return;
        }

        CameraKeyframe step = SAN_FRANCISCO_TOUR.get(stepIndex);
        if (tvTourStatus != null) {
            runOnUiThread(() -> tvTourStatus.setText(
                getString(
                    R.string.aerial_tour_status_running,
                    stepIndex + 1,
                    SAN_FRANCISCO_TOUR.size(),
                    step.getStepTitle()))
            );
        }

        if (step instanceof CameraKeyframe.FlyTo) {
            CameraKeyframe.FlyTo flyTo = (CameraKeyframe.FlyTo) step;
            Camera targetCam = flyTo.getTargetCamera();
            LatLng center =
                new LatLng(
                    targetCam.getCenter().getLatitude(), targetCam.getCenter().getLongitude());

            updateAirplaneModel(center, targetCam.getHeading() + 180.0);
            googleMap3D.setCameraAnimationEndListener(
                () -> {
                    googleMap3D.setCameraAnimationEndListener(null);
                    if (isPlaying) {
                        currentStepIndex = stepIndex + 1;
                        executeKeyframeStep(currentStepIndex);
                    }
                });
            googleMap3D.flyCameraTo(new FlyToOptions(targetCam, flyTo.getDurationMs()));
        } else if (step instanceof CameraKeyframe.DwellPause) {
            CameraKeyframe.DwellPause dwell = (CameraKeyframe.DwellPause) step;
            handler.postDelayed(
                () -> {
                    if (isPlaying) {
                        currentStepIndex = stepIndex + 1;
                        executeKeyframeStep(currentStepIndex);
                    }
                },
                dwell.getDurationMs());
        } else if (step instanceof CameraKeyframe.OrbitAround) {
            CameraKeyframe.OrbitAround orbit = (CameraKeyframe.OrbitAround) step;
            long totalMs = orbit.getDurationMs();

            keyframeOrbitCallback =
                new Choreographer.FrameCallback() {
                    private long startTimeNanos = 0L;

                    @Override
                    public void doFrame(long frameTimeNanos) {
                        if (!isPlaying || googleMap3D == null) {
                            return;
                        }

                        if (startTimeNanos == 0L) {
                            startTimeNanos = frameTimeNanos;
                            Choreographer.getInstance().postFrameCallback(this);
                            return;
                        }

                        long elapsedMs = (frameTimeNanos - startTimeNanos) / 1_000_000L;
                        double t = Math.min(1.0, (double) elapsedMs / totalMs);
                        double orbitHeading = interpolateAngle(orbit.getStartHeading(),
                            orbit.getEndHeading(), t);

                        Camera updatedCam =
                            new Camera(
                                new LatLngAltitude(
                                    orbit.getCenter().latitude,
                                    orbit.getCenter().longitude,
                                    orbit.getAltitude()),
                                normalizeHeading(orbitHeading),
                                orbit.getTilt(),
                                /* roll= */ 0.0,
                                orbit.getRange());

                        updateAirplaneModel(orbit.getCenter(), orbitHeading + 180.0);
                        googleMap3D.setCamera(updatedCam);

                        if (t >= 1.0) {
                            keyframeOrbitCallback = null;
                            if (isPlaying) {
                                currentStepIndex = stepIndex + 1;
                                executeKeyframeStep(currentStepIndex);
                            }
                            return;
                        }

                        Choreographer.getInstance().postFrameCallback(this);
                    }
                };
            Choreographer.getInstance().postFrameCallback(keyframeOrbitCallback);
        }
    }

    /**
     * Frame Dispatcher Animation Loop. High-speed flight animation (400 m/s) stopping cleanly at
     * destination.
     *
     * For the most visually uniform cinematic sweeping motion, we recommend using
     * `Choreographer.FrameCallback` to sync our delta-time interpolation directly to the hardware
     * display frames.
     */
    private void runFrameDispatcherLoop() {
        stopTour();
        isPlaying = true;
        updatePlayPauseButtonState();
        if (tvTourStatus != null) {
            tvTourStatus.setText(R.string.approach_dispatcher_frame_loop);
        }

        double totalDistance = cumulativeDistances[cumulativeDistances.length - 1];
        double flightSpeedMps = 400.0; // Fast 400 m/s high-speed flight

        frameDispatcherCallback =
            new Choreographer.FrameCallback() {
                private long lastTimeNanos = 0L;
                private double elapsedDistance = 0.0;

                @Override
                public void doFrame(long frameTimeNanos) {
                    if (!isPlaying || googleMap3D == null) {
                        return;
                    }

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
                                AIRPLANE_FLIGHT_PATH, cumulativeDistances, elapsedDistance, 30.0);
                        double planeHeading = posAndHeading.getHeading() + 180.0;
                        updateAirplaneModel(posAndHeading.getPosition(), planeHeading);

                        Camera finalCam =
                            new Camera(
                                new LatLngAltitude(
                                    posAndHeading.getPosition().latitude,
                                    posAndHeading.getPosition().longitude,
                                    200.0),
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
                            AIRPLANE_FLIGHT_PATH, cumulativeDistances, elapsedDistance, 30.0);

                    double planeHeading = posAndHeading.getHeading() + 180.0;
                    updateAirplaneModel(posAndHeading.getPosition(), planeHeading);

                    Camera updatedCam =
                        new Camera(
                            new LatLngAltitude(
                                posAndHeading.getPosition().latitude,
                                posAndHeading.getPosition().longitude,
                                200.0),
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

    /**
     * Option 4: Continuous 360-degree orbital camera spin around landmark.
     */
    private void run360OrbitSpin() {
        stopTour();
        isPlaying = true;
        updatePlayPauseButtonState();
        if (tvTourStatus != null) {
            tvTourStatus.setText(R.string.approach_orbit_360_spin);
        }

        LatLng targetCenter = AIRPLANE_FLIGHT_PATH.get(0);
        updateAirplaneModel(targetCenter, 105.0 + 180.0);

        long totalMs = 6000L; // 6 second smooth 360° spin
        double startHeading = 105.0;

        orbitCallback =
            new Choreographer.FrameCallback() {
                private long startTimeNanos = 0L;

                @Override
                public void doFrame(long frameTimeNanos) {
                    if (!isPlaying || googleMap3D == null) {
                        return;
                    }

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
                            new LatLngAltitude(targetCenter.latitude, targetCenter.longitude,
                                200.0),
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

    /**
     * Resets the camera and airplane model to the initial start location and restarts animation.
     */
    public void resetAndRestartTour() {
        stopTour();
        currentStepIndex = 0;

        LatLng startLoc = AIRPLANE_FLIGHT_PATH.get(0);
        double initialHeading =
            SphericalUtil.computeHeading(startLoc, AIRPLANE_FLIGHT_PATH.get(1));
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

    private static final String MODEL_ID = "airplane_model";
    private static final String PLANE_URL =
        "https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/Airplane.glb";

    private static final List<CameraKeyframe> SAN_FRANCISCO_TOUR =
        Arrays.asList(
            new CameraKeyframe.FlyTo(
                "1. Golden Gate Bridge Flight",
                "3D Airplane flight over Golden Gate Bridge",
                new Camera(
                    new LatLngAltitude(37.8199, -122.4783, 200.0),
                    /* heading= */ 105.0,
                    /* tilt= */ 65.0,
                    /* roll= */ 0.0,
                    /* range= */ 600.0),
                2500L),
            new CameraKeyframe.DwellPause(
                "2. Mid-Air Observation", "Dwell pause observing 3D airplane over Golden Gate",
                1500L),
            new CameraKeyframe.OrbitAround(
                "3. Golden Gate 360° Orbit",
                "360° orbital camera spin around flying airplane",
                new LatLng(37.8199, -122.4783),
                200.0,
                600.0,
                65.0,
                105.0,
                465.0,
                4000L),
            new CameraKeyframe.FlyTo(
                "4. Transit to Coit Tower",
                "Airplane flight to Coit Tower Landmark",
                new Camera(
                    new LatLngAltitude(37.8024, -122.4058, 200.0),
                    /* heading= */ 105.0,
                    /* tilt= */ 65.0,
                    /* roll= */ 0.0,
                    /* range= */ 600.0),
                3000L));

    // 15 Fine-Grained Waypoints on the direct route from Golden Gate Bridge to Coit Tower
    private static final List<LatLng> AIRPLANE_FLIGHT_PATH =
        Arrays.asList(
            new LatLng(37.8199, -122.4783), // 1. Golden Gate Bridge (Source)
            new LatLng(37.8188, -122.4735), // 2. Fort Point / Presidio Overlook
            new LatLng(37.8175, -122.4685), // 3. Crissy Field West
            new LatLng(37.8160, -122.4635), // 4. Crissy Field East
            new LatLng(37.8145, -122.4585), // 5. Marina Green West
            new LatLng(37.8130, -122.4530), // 6. Marina District Center
            new LatLng(37.8115, -122.4475), // 7. Fort Mason West
            new LatLng(37.8100, -122.4420), // 8. Fort Mason Heights
            new LatLng(37.8085, -122.4365), // 9. Aquatic Park Cove
            new LatLng(37.8070, -122.4310), // 10. Fisherman's Wharf West
            new LatLng(37.8058, -122.4250), // 11. Fisherman's Wharf Center
            new LatLng(37.8048, -122.4195), // 12. Pier 39 Promenade
            new LatLng(37.8038, -122.4140), // 13. Embarcadero North
            new LatLng(37.8030, -122.4090), // 14. Telegraph Hill Slopes
            new LatLng(37.8024, -122.4058)  // 15. Coit Tower (Destination)
        );

    private static double normalizeHeading(double headingDeg) {
        double normalized = headingDeg % 360.0;
        return normalized < 0.0 ? normalized + 360.0 : normalized;
    }

    private static double interpolateAngle(double start, double end, double fraction) {
        double diff = (end - start) % 360.0;
        if (diff > 180.0) {
            diff -= 360.0;
        }
        if (diff < -180.0) {
            diff += 360.0;
        }
        return (start + diff * fraction + 360.0) % 360.0;
    }
}
