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

package com.example.maps3djava.models;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.maps3dcommon.R;
import com.example.maps3djava.sampleactivity.SampleBaseActivity;
import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.OnModelClickListener;
import com.google.android.gms.maps3d.model.AltitudeMode;
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.FlyAroundOptions;
import com.google.android.gms.maps3d.model.FlyToOptions;
import com.google.android.gms.maps3d.model.LatLngAltitude;
import com.google.android.gms.maps3d.model.Map3DMode;
import com.google.android.gms.maps3d.model.ModelOptions;
import com.google.android.gms.maps3d.model.Orientation;
import com.google.android.gms.maps3d.model.Vector3D;

import static com.example.maps3d.common.UtilitiesKt.toValidCamera;


/**
 * Demonstrates the use of 3D models in a 3D map environment.
 */
public class ModelsActivity extends SampleBaseActivity {

    public static final String PLANE_URL = "https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/Airplane.glb";
    public static final double PLANE_SCALE = 0.05;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Runnable currentAnimationStep;
    private boolean isAnimationRunning = false;
    private Button recenterButton;
    private Button stopButton;

    @Override
    public final String getTAG() {
        return this.getClass().getSimpleName();
    }

    @Override
    public final Camera getInitialCamera() {
        return toValidCamera(new Camera(
                new LatLngAltitude(
                        47.133971,
                        11.333161,
                        2200.0
                ),
                221.0,
                25.0,
                0.0,
                30_000.0
        ));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        recenterButton = findViewById(R.id.reset_view_button);
        stopButton = findViewById(R.id.stop_button);

        recenterButton.setOnClickListener(view -> {
            if (googleMap3D == null) {
                return;
            }
            // 1. Stop any and all current animation activity.
            stopAnimation();

            // 2. Set the camera to its initial state.
            googleMap3D.setCamera(getInitialCamera());

            // 3. Start the animation sequence fresh.
            runAnimationSequence(googleMap3D);
        });

        stopButton.setOnClickListener(view -> stopAnimation());

        recenterButton.setVisibility(View.VISIBLE);
        stopButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onMap3DViewReady(@NonNull GoogleMap3D googleMap3D) {
        super.onMap3DViewReady(googleMap3D);
        this.googleMap3D = googleMap3D;

        googleMap3D.setMapMode(Map3DMode.SATELLITE);

        ModelOptions modelOptions = new ModelOptions();
        modelOptions.setId("plane_model");
        modelOptions.setPosition(new LatLngAltitude(
                47.133971,
                11.333161,
                2200.0
        ));
        modelOptions.setAltitudeMode(AltitudeMode.ABSOLUTE);

        Orientation orientation = new Orientation(41.5, -90.0, 0.0);
        modelOptions.setOrientation(orientation);

        modelOptions.setUrl(PLANE_URL);

        Vector3D scale = new Vector3D(PLANE_SCALE, PLANE_SCALE, PLANE_SCALE);
        modelOptions.setScale(scale);

        googleMap3D.addModel(modelOptions).setClickListener(() -> ModelsActivity.this.runOnUiThread(() -> Toast.makeText(ModelsActivity.this, "Model clicked", Toast.LENGTH_SHORT).show()));

        runAnimationSequence(googleMap3D);
    }

    /**
     * Stops any scheduled or in-progress camera animation.
     */
    private void stopAnimation() {
        if (googleMap3D == null) {
            return;
        }
        // Remove any pending runnables from the handler
        if (currentAnimationStep != null) {
            mainHandler.removeCallbacks(currentAnimationStep);
            currentAnimationStep = null;
        }
        // Remove any active animation listeners and stop camera movement
        googleMap3D.setCameraAnimationEndListener(null);
        googleMap3D.stopCameraAnimation(); // Halts any in-progress camera movement
        isAnimationRunning = false;
    }

    /**
     * Runs a sequence of camera animations using nested callbacks and delays.
     */
    private void runAnimationSequence(GoogleMap3D googleMap3D) {
        if (isAnimationRunning) {
            return;
        }
        isAnimationRunning = true;

        Camera cameraTarget = new Camera(
                new LatLngAltitude(47.133971, 11.333161, 2200.0),
                221.4,
                75.0,
                0.0,
                700.0
        );

        // --- STEP 1: Fly to the location ---
        currentAnimationStep = () -> {
            FlyToOptions flyTo = new FlyToOptions(cameraTarget, 3_500);

            googleMap3D.setCameraAnimationEndListener(() -> {
                // --- STEP 2: Fly around the location (after a short pause) ---
                currentAnimationStep = () -> {
                    FlyAroundOptions flyAround = new FlyAroundOptions(cameraTarget, 3_500, 0.5);

                    googleMap3D.setCameraAnimationEndListener(() -> {
                        // --- SEQUENCE END ---
                        googleMap3D.setCameraAnimationEndListener(null);
                        currentAnimationStep = null;
                        isAnimationRunning = false;
                    });

                    googleMap3D.flyCameraAround(flyAround);
                };
                mainHandler.postDelayed(currentAnimationStep, 500);
            });

            googleMap3D.flyCameraTo(flyTo);
        };

        mainHandler.postDelayed(currentAnimationStep, 1500);
    }
}