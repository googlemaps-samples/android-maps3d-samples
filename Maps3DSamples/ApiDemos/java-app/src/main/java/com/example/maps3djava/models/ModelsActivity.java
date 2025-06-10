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

import androidx.annotation.NonNull;

import com.example.maps3dcommon.R;
import com.example.maps3djava.sampleactivity.SampleBaseActivity;

import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.model.AltitudeMode;
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.FlyAroundOptions;
import com.google.android.gms.maps3d.model.LatLngAltitude;
import com.google.android.gms.maps3d.model.Map3DMode;
import com.google.android.gms.maps3d.model.ModelOptions;
import com.google.android.gms.maps3d.model.Orientation;
import com.google.android.gms.maps3d.model.Vector3D;
import com.google.android.gms.maps3d.OnCameraAnimationEndListener;

import static com.example.maps3d.common.UtilitiesKt.toValidCamera;


/**
 * Demonstrates the use of 3D models in a 3D map environment.
 */
public class ModelsActivity extends SampleBaseActivity {

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


    private Runnable currentAnimationStepRunnable;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        recenterButton = findViewById(R.id.reset_view_button);
        recenterButton.setOnClickListener(view -> {
            GoogleMap3D controller = googleMap3D;
            if (controller == null) {
                return;
            }

            // --- STOP Current Animation Sequence ---
            // Remove any pending tasks from the handler and clear the runnable.
            if (currentAnimationStepRunnable != null) {
                mainHandler.removeCallbacks(currentAnimationStepRunnable);
                currentAnimationStepRunnable = null;
            }
            // Also, explicitly clear any ongoing listener if an animation was active.
            controller.setCameraAnimationEndListener(null);
            // --- END STOP ---


            controller.setCameraAnimationEndListener(() -> {
                controller.setCameraAnimationEndListener(null);
                runAnimationSequence(controller);
            });
            controller.setCamera(getInitialCamera());
        });
        recenterButton.setVisibility(View.VISIBLE);


        findViewById(R.id.stop_button).setOnClickListener(view -> {
            if (currentAnimationStepRunnable != null) {
                mainHandler.removeCallbacks(currentAnimationStepRunnable);
                currentAnimationStepRunnable = null;
            }

            if (googleMap3D != null) {
                googleMap3D.setCameraAnimationEndListener(null);
            }

        });
        findViewById(R.id.stop_button).setVisibility(View.VISIBLE);
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

        Orientation orientation = new Orientation(41.5, 90.0, 0.0);
        modelOptions.setOrientation(orientation);

        modelOptions.setUrl(PLANE_URL);

        Vector3D scale = new Vector3D(PLANE_SCALE, PLANE_SCALE, PLANE_SCALE);
        modelOptions.setScale(scale);

        googleMap3D.addModel(modelOptions);

        // Start the animation sequence after the map is ready.
        // No initial camera animation needed here, as runAnimationSequence will handle delays.
        runAnimationSequence(googleMap3D);
    }

    /**
     * Runs a sequence of camera animations using nested callbacks and delays.
     */
    private void runAnimationSequence(GoogleMap3D googleMap3D) {
        // --- Step 1: Initial Delay ---
        currentAnimationStepRunnable = () -> {
            Camera cameraAnimation1 = new Camera(
                    new LatLngAltitude(
                            47.133971,
                            11.333161,
                            2200.0
                    ),
                    221.4, // heading
                    75.0,  // tilt
                    0.0,   // zoom
                    700.0  // range
            );

           // --- Step 2: First Animation ---
            // Set the listener for when this FIRST animation finishes.
            googleMap3D.setCameraAnimationEndListener(new OnCameraAnimationEndListener() {
                @Override
                public void onCameraAnimationEnd() {
                    // This callback executes when the first animation is complete.
                    // IMPORTANT: Clear the listener immediately to avoid it being called multiple times.
                    googleMap3D.setCameraAnimationEndListener(null);

                    // --- Step 3: Delay between animations ---
                    currentAnimationStepRunnable = () -> {
                        FlyAroundOptions flyAroundOptions = new FlyAroundOptions(cameraAnimation1, 3_500, 0.5);

                        // --- Step 4: Second Animation ---
                        // Set the listener for when this SECOND animation finishes.
                        googleMap3D.setCameraAnimationEndListener(new OnCameraAnimationEndListener() {
                            @Override
                            public void onCameraAnimationEnd() {
                                // This callback executes when the second animation is complete.
                                // IMPORTANT: Clear the listener.
                                googleMap3D.setCameraAnimationEndListener(null);
                                // The entire animation sequence is now finished.
                                currentAnimationStepRunnable = null; // Mark sequence as done.
                            }
                        });
                        // Trigger the second animation.
                        googleMap3D.setCamera(cameraAnimation1);
                    };
                    // Schedule the second animation to start after its 500ms delay.
                    mainHandler.postDelayed(currentAnimationStepRunnable, 500L);
                }
            });
            // Trigger the first animation.
            googleMap3D.setCamera(cameraAnimation1);
        };
        // Schedule the start of the first animation step after its initial 1500ms delay.
        mainHandler.postDelayed(currentAnimationStepRunnable, 1500L);
    }



    public static final String PLANE_URL = "https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/Airplane.glb";
    public static final double PLANE_SCALE = 0.05;
}