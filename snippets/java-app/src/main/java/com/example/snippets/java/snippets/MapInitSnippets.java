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

package com.example.snippets.java.snippets;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.example.snippets.java.annotations.SnippetGroup;
import com.example.snippets.java.annotations.SnippetItem;
import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.Map3DView;
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback;
import com.google.android.gms.maps3d.OnMapReadyListener;
import com.google.android.gms.maps3d.OnMapSteadyListener;
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.LatLngAltitude;

@SnippetGroup(
        title = "Map Initialization",
        description = "Snippets demonstrating map lifecycle, listeners and readiness states.")
public class MapInitSnippets {

    @SnippetItem(
            title = "1. Basic Map3D Initialization",
            description = "Initializes a standard 3D Map View and sets an initial camera angle.")
    @SuppressWarnings("unused")
    public void basicMap3D(Context context) {
        // [START maps_android_3d_init_basic_java]
        Map3DView map3DView = new Map3DView(context);

        // Get the map asynchronously
        map3DView.getMap3DViewAsync(
                new OnMap3DViewReadyCallback() {
                    @Override
                    public void onMap3DViewReady(@NonNull GoogleMap3D googleMap3D) {
                        // Map is ready to be used
                        LatLngAltitude center = new LatLngAltitude(40.0150, -105.2705, 5000.0);
                        Camera camera = new Camera(center, 0.0, 45.0, 0.0, 10000.0);
                        googleMap3D.setCamera(camera);
                    }

                    @Override
                    public void onError(@NonNull Exception e) {
                        // Log the exception to guide developers debugging layout mismatches
                        Log.e(
                                "MapInitSnippets",
                                "Failed to initialize 3D Map: " + e.getMessage(),
                                e);
                    }
                });
        // [END maps_android_3d_init_basic_java]
    }

    /** Sets up listeners for map readiness and steady state. */
    @SuppressWarnings("unused")
    @SnippetItem(
            title = "2. Listen Map Events",
            description = "Logs map events to the console, such as clicks or idle status.")
    public void setupMapListeners(Context context, GoogleMap3D map) {
        // [START maps_android_3d_init_listeners_java]
        Handler mainHandler = new Handler(Looper.getMainLooper());

        map.setOnMapReadyListener(
                new OnMapReadyListener() {
                    @Override
                    public void onMapReady(double sceneReadiness) {
                        if (sceneReadiness == 1.0) {
                            // Scene is fully loaded
                            mainHandler.post(
                                    () ->
                                            Toast.makeText(
                                                            context,
                                                            "Map Scene Ready (100%)",
                                                            Toast.LENGTH_SHORT)
                                                    .show());
                        }
                    }
                });

        map.setOnMapSteadyListener(
                new OnMapSteadyListener() {
                    @Override
                    public void onMapSteadyChange(boolean isSceneSteady) {
                        if (isSceneSteady) {
                            // Camera is not moving and data is loaded
                            mainHandler.post(
                                    () ->
                                            Toast.makeText(
                                                            context,
                                                            "Map Scene Steady",
                                                            Toast.LENGTH_SHORT)
                                                    .show());
                        }
                    }
                });
        // [END maps_android_3d_init_listeners_java]
    }
}
