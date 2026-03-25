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

package com.example.maps3djava.common;

import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.model.FlyAroundOptions;
import com.google.android.gms.maps3d.model.FlyToOptions;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MapUtils {

    // A single threaded executor specifically for handling timeouts
    private static final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    /**
     * Returns a CompletableFuture that resolves when the camera animation completes.
     */
    public static CompletableFuture<Void> awaitCameraAnimation(GoogleMap3D googleMap3D, FlyToOptions options) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        googleMap3D.setCameraAnimationEndListener(() -> {
            googleMap3D.setCameraAnimationEndListener(null);
            if (!future.isDone()) {
                future.complete(null);
            }
        });

        googleMap3D.flyCameraTo(options);
        return future;
    }

    /**
     * Returns a CompletableFuture that resolves when the camera orbit animation completes.
     */
    public static CompletableFuture<Void> awaitCameraAnimation(GoogleMap3D googleMap3D, FlyAroundOptions options) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        googleMap3D.setCameraAnimationEndListener(() -> {
            googleMap3D.setCameraAnimationEndListener(null);
            if (!future.isDone()) {
                future.complete(null);
            }
        });

        googleMap3D.flyCameraAround(options);
        return future;
    }

    /**
     * Returns a CompletableFuture that resolves to true when the map is steady,
     * or false if the specified timeout is reached.
     */
    public static CompletableFuture<Boolean> awaitMapSteady(
            GoogleMap3D googleMap3D,
            long timeout,
            TimeUnit unit) {

        CompletableFuture<Boolean> future = new CompletableFuture<>();

        // 1. Set up the success listener
        googleMap3D.setOnMapSteadyListener(isSteady -> {
            if (isSteady && !future.isDone()) {
                // Important: clear the listener when done
                googleMap3D.setOnMapSteadyListener(null);
                future.complete(true);
            }
        });

        // 2. Schedule the timeout
        scheduler.schedule(() -> {
            if (!future.isDone()) {
                // Important: clear the listener on timeout too
                googleMap3D.setOnMapSteadyListener(null);
                future.complete(false); // Resolve as false on timeout
            }
        }, timeout, unit);

        return future;
    }
}
