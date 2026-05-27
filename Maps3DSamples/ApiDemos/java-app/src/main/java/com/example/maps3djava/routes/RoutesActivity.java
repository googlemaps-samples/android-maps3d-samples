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

package com.example.maps3djava.routes;

import static com.example.maps3d.common.UtilitiesKt.toHeading;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.view.WindowCompat;

import com.example.maps3d.common.PositionAndHeading;
import com.example.maps3d.common.RouteEngine;
import com.example.maps3d.common.OahuRouteData;
import com.example.maps3dcommon.R;
import com.example.maps3djava.BuildConfig;
import com.example.maps3djava.sampleactivity.SampleBaseActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback;
import com.google.android.gms.maps3d.model.AltitudeMode;
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.Map3DMode;
import com.google.android.gms.maps3d.model.Model;
import com.google.android.gms.maps3d.model.ModelOptions;
import com.google.android.gms.maps3d.model.Orientation;
import com.google.android.gms.maps3d.model.Polyline;
import com.google.android.gms.maps3d.model.PolylineOptions;
import com.google.android.gms.maps3d.model.Vector3D;
import com.google.android.gms.maps3d.model.LatLngAltitude;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * A premium View-based sample activity demonstrating cross-product integration with the Routes API in Java.
 *
 * This sample executes background threads to fetch driving routes in Honolulu, parses the encoded polyline,
 * renders the route line on [GoogleMap3D], loads a 3D model (.glb), and implements an android.os.Handler
 * framework to animate the car smoothly along the path with real-time camera tracking.
 */
public class RoutesActivity extends SampleBaseActivity implements OnMap3DViewReadyCallback {

    @Override
    public final String getTAG() {
        return "RoutesActivity";
    }

    // Honolulu Overview starting position
    @Override
    public final Camera getInitialCamera() {
        return new Camera(
                new LatLngAltitude(21.348567, -157.803961, 0.0),
                38.6,
                45.0,
                0.0,
                20000.0
        );
    }

    // View Bindings
    private MaterialButton btnPlayPause;
    private Slider progressSlider;
    private Slider rangeSlider;
    private TextView rangeSliderLabel;
    private Slider speedSlider;
    private TextView speedSliderLabel;
    private Slider headingSlider;
    private TextView headingSliderLabel;

    // Core State Variables
    private final RouteRepository routeRepository = new RouteRepository();
    private List<LatLng> decodedRoute = new ArrayList<>();
    private double[] cumulativeDistances = new double[]{0.0};
    private double totalDistance = 0.0;
    private double elapsedDistance = 0.0;

    private boolean isPlaying = false;
    private boolean isUserScrubbing = false;

    // Slider default parameters
    private float cameraRange = 1500f;
    private float vehicleSpeedMps = 150f;
    private float yawOffset = 0f;

    // Map references
    private Polyline routePolyline;
    private Model vehicleModel;

    // Background Executors
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes);

        // Re-bind map3DView to the new active instance in activity_routes.xml and forward lifecycle
        map3DView = findViewById(R.id.map3dView);
        map3DView.onCreate(savedInstanceState);
        map3DView.getMap3DViewAsync(this);

        // Configure custom toolbar
        MaterialToolbar toolbar = findViewById(R.id.top_bar);
        toolbar.setTitle(getString(R.string.feature_title_routes_api));
        toolbar.setNavigationOnClickListener(view -> finish());

        // Bind control panel views
        btnPlayPause = findViewById(R.id.btn_play_pause);
        progressSlider = findViewById(R.id.progress_slider);
        rangeSlider = findViewById(R.id.range_slider);
        rangeSliderLabel = findViewById(R.id.range_slider_label);
        speedSlider = findViewById(R.id.speed_slider);
        speedSliderLabel = findViewById(R.id.speed_slider_label);
        headingSlider = findViewById(R.id.heading_slider);
        headingSliderLabel = findViewById(R.id.heading_slider_label);

        setupControls();
    }

    /**
     * Configures seekbars and button click behaviors.
     */
    private void setupControls() {
        btnPlayPause.setOnClickListener(view -> {
            if (decodedRoute.isEmpty()) {
                Toast.makeText(this, "Route is still loading...", Toast.LENGTH_SHORT).show();
                return;
            }
            togglePlayback(!isPlaying);
        });

        // Scrub progress manually
        progressSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
                isUserScrubbing = true;
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                isUserScrubbing = false;
                elapsedDistance = totalDistance * slider.getValue();
                updateVehiclePositionAndCamera();
            }
        });

        progressSlider.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser && isUserScrubbing) {
                elapsedDistance = totalDistance * value;
                updateVehiclePositionAndCamera();
            }
        });

        // Camera altitude adjustments
        rangeSliderLabel.setText("Camera Altitude: " + (int) cameraRange + "m");
        rangeSlider.setValue(cameraRange);
        rangeSlider.addOnChangeListener((slider, value, fromUser) -> {
            cameraRange = value;
            rangeSliderLabel.setText("Camera Altitude: " + (int) value + "m");
            updateVehiclePositionAndCamera();
        });

        // Speed configurations
        speedSliderLabel.setText("Vehicle Speed: " + (int) vehicleSpeedMps + "m/s");
        speedSlider.setValue(vehicleSpeedMps);
        speedSlider.addOnChangeListener((slider, value, fromUser) -> {
            vehicleSpeedMps = value;
            speedSliderLabel.setText("Vehicle Speed: " + (int) value + "m/s");
        });

        // Camera rotation offsets
        headingSliderLabel.setText("Camera Yaw Offset: " + (int) yawOffset + "°");
        headingSlider.setValue(yawOffset);
        headingSlider.addOnChangeListener((slider, value, fromUser) -> {
            yawOffset = value;
            headingSliderLabel.setText("Camera Yaw Offset: " + (int) value + "°");
            updateVehiclePositionAndCamera();
        });
    }

    private void togglePlayback(boolean play) {
        isPlaying = play;
        if (play) {
            btnPlayPause.setIconResource(R.drawable.pause_24px);
            startAnimationLoop();
        } else {
            btnPlayPause.setIconResource(R.drawable.play_arrow_24px);
            stopAnimationLoop();
        }
    }

    @Override
    public void onMap3DViewReady(@NonNull GoogleMap3D googleMap3D) {
        super.onMap3DViewReady(googleMap3D);
        googleMap3D.setMapMode(Map3DMode.SATELLITE);

        // Trigger async route fetch on executor thread
        loadAndRenderRouteAsync(googleMap3D);
    }

    private void loadAndRenderRouteAsync(GoogleMap3D googleMap3D) {
        String apiKey = BuildConfig.MAPS3D_API_KEY;
        LatLng origin = new LatLng(21.307043, -157.858984);
        LatLng destination = new LatLng(21.390177, -157.719454);

        executorService.execute(() -> {
            List<LatLng> decoded;
            try {
                if (apiKey.isEmpty() || apiKey.contains("YOUR_API_KEY")) {
                    throw new Exception("Invalid or missing API Key");
                }
                RouteData routeData = routeRepository.fetchRouteCallable(apiKey, origin, destination).call();
                decoded = PolyUtil.decode(routeData.getEncodedPolyline());
            } catch (Exception e) {
                Log.w(getTAG(), "Routes API fetch failed (" + e.getLocalizedMessage() + "). Falling back to pre-baked Oahu mountain route.");
                decoded = OahuRouteData.getFALLBACK_ROUTE();
                mainHandler.post(() -> Toast.makeText(
                        RoutesActivity.this,
                        "Offline: Using local Oahu fallback route",
                        Toast.LENGTH_LONG
                ).show());
            }

            final List<LatLng> finalDecoded = decoded;
            mainHandler.post(() -> {
                decodedRoute = finalDecoded;
                cumulativeDistances = RouteEngine.calculateCumulativeDistances(finalDecoded);
                totalDistance = cumulativeDistances[cumulativeDistances.length - 1];

                // 1. Draw the blue route polyline
                List<LatLngAltitude> linePath = new ArrayList<>();
                for (LatLng point : finalDecoded) {
                    linePath.add(new LatLngAltitude(point.latitude, point.longitude, 0.0));
                }

                PolylineOptions polyOptions = new PolylineOptions();
                polyOptions.setPath(linePath);
                polyOptions.setStrokeColor(Color.BLUE);
                polyOptions.setStrokeWidth(10.0);
                polyOptions.setAltitudeMode(AltitudeMode.CLAMP_TO_GROUND);
                polyOptions.setZIndex(5);
                routePolyline = googleMap3D.addPolyline(polyOptions);

                // 2. Load the 3D Car model
                ModelOptions modelOpts = new ModelOptions();
                modelOpts.setId("vehicle_car_java");
                modelOpts.setPosition(new LatLngAltitude(finalDecoded.get(0).latitude, finalDecoded.get(0).longitude, 25.0));
                modelOpts.setAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);
                modelOpts.setOrientation(new Orientation(0.0, -90.0, 0.0));
                modelOpts.setUrl("https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/red_car.glb");
                modelOpts.setScale(new Vector3D(50.0, 50.0, 50.0));
                vehicleModel = googleMap3D.addModel(modelOpts);

                updateVehiclePositionAndCamera();

                // Trigger play automatically once map is populated
                togglePlayback(true);
            });
        });
    }

    // Animation Tick Engine Runnable
    private long lastTime = 0;
    private final Runnable animationTickRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isPlaying || totalDistance <= 0.0) return;

            long now = System.currentTimeMillis();
            double dt = (now - lastTime) / 1000.0; // Delta time in seconds
            lastTime = now;

            elapsedDistance += vehicleSpeedMps * dt;

            // Clamp/loop playback
            if (elapsedDistance >= totalDistance) {
                elapsedDistance = 0.0;
            }

            // Sync seekBar
            if (!isUserScrubbing) {
                progressSlider.setValue((float) (elapsedDistance / totalDistance));
            }

            updateVehiclePositionAndCamera();

            // Post next frame with ~16ms delays (60fps targets)
            mainHandler.postDelayed(this, 16);
        }
    };

    private void startAnimationLoop() {
        lastTime = System.currentTimeMillis();
        mainHandler.post(animationTickRunnable);
    }

    private void stopAnimationLoop() {
        mainHandler.removeCallbacks(animationTickRunnable);
    }

    /**
     * Interpolates geographic vectors and repositions models/camera.
     */
    private void updateVehiclePositionAndCamera() {
        if (decodedRoute.isEmpty() || totalDistance <= 0.0) return;

        PositionAndHeading posAndHeading = RouteEngine.calculatePositionAndHeading(
                decodedRoute,
                cumulativeDistances,
                elapsedDistance,
                30.0
        );

        Log.d("RoutesActivityDebug", "Java Tick: elapsed = " + elapsedDistance + "m / " + totalDistance + "m, pos = " + posAndHeading.getPosition());

        // 1. Upsert Model position and rotation on every tick using the same ID
        if (googleMap3D != null) {
            ModelOptions modelOpts = new ModelOptions();
            modelOpts.setId("vehicle_car_java");
            modelOpts.setPosition(new LatLngAltitude(posAndHeading.getPosition().latitude, posAndHeading.getPosition().longitude, 25.0));
            modelOpts.setAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);
            modelOpts.setOrientation(new Orientation(posAndHeading.getHeading(), -90.0, 0.0));
            modelOpts.setUrl("https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/red_car.glb");
            modelOpts.setScale(new Vector3D(50.0, 50.0, 50.0));
            vehicleModel = googleMap3D.addModel(modelOpts);
        }

        // 2. Update Camera center and bearing
        if (googleMap3D != null) {
            Camera trackingCamera = new Camera(
                    new LatLngAltitude(posAndHeading.getPosition().latitude, posAndHeading.getPosition().longitude, 0.0),
                    toHeading(posAndHeading.getHeading() + yawOffset),
                    65.0,
                    0.0,
                    (double) cameraRange
            );
            googleMap3D.setCamera(trackingCamera);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        togglePlayback(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAnimationLoop();
        executorService.shutdown();
    }
}
