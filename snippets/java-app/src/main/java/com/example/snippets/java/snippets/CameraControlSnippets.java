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

import com.example.snippets.java.TrackedMap3D;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.FlyAroundOptions;
import com.google.android.gms.maps3d.model.FlyToOptions;
import com.google.android.gms.maps3d.model.LatLngAltitude;
import com.google.android.gms.maps3d.model.CameraRestriction;
import com.google.android.gms.maps3d.model.LatLngBounds;
import com.example.snippets.java.annotations.SnippetGroup;
import com.example.snippets.java.annotations.SnippetItem;

@SnippetGroup(
    title = "Camera",
    description = "Snippets demonstrating dynamic camera orchestration and animations."
)
public class CameraControlSnippets {

    private final TrackedMap3D map;

    public CameraControlSnippets(TrackedMap3D map) {
        this.map = map;
    }

    // [START maps_android_3d_camera_fly_to_java]
    /**
     * Animates the camera to a specific position (coordinates, heading, tilt) over
     * a duration.
     */
    @SnippetItem(
        title = "1. Fly To",
        description = "Animates the camera to a specific position with a tilt and heading over 5 seconds."
    )
    public void flyCameraToPosition() {
        LatLngAltitude center = new LatLngAltitude(38.743829, -109.499512, 1460.37);
        // Create a target camera:
        Camera targetCamera = new Camera(center, 338.52, 76.16, 0.0, 191.71);

        // FlyToOptions constructor: endCamera, durationInMillis
        FlyToOptions options = new FlyToOptions(targetCamera, 5000L);

        map.flyCameraTo(options);
    }
    // [END maps_android_3d_camera_fly_to_java]

    /**
     * Orbits the camera around a specific location.
     */
    @SuppressWarnings("unused")
    @SnippetItem(
        title = "2. Fly Around",
        description = "Rotates the camera 360 degrees around a specific location over 10 seconds."
    )
    public void flyCameraAroundLocation() {
        // [START maps_android_3d_camera_fly_around_java]
        LatLngAltitude center = new LatLngAltitude(38.743502, -109.499374, 1467.0);
        // Create a target camera:
        Camera targetCamera = new Camera(center, 349.6, 58.1, 0.0, 138.2);

        // Orbit around the target
        // FlyAroundOptions constructor: center, durationInMillis, rounds
        FlyAroundOptions options = new FlyAroundOptions(targetCamera, 6000L, 2.0);

        // Although not completely necessary, the experience will be usually be better by
        // waiting for the camera to steady before starting the flyCameraAround
        map.setCamera(targetCamera);

        map.setOnMapSteadyListener(isSteady -> {
            if (isSteady) {
                map.setOnMapSteadyListener(null); // Cleanup
                map.flyCameraAround(options);
            }
        });
        // [END maps_android_3d_camera_fly_around_java]
    }

    /**
     * Stops the current camera animation.
     */
    @SuppressWarnings("unused")
    @SnippetItem(
        title = "3. Stop Animation",
        description = "Stops any currently running camera animation immediately."
    )
    public void stopAnimation() {
        // [START maps_android_3d_camera_stop_java]
        LatLngAltitude center = new LatLngAltitude(38.743502, -109.499374, 1467.0);
        Camera targetCamera = new Camera(center, 349.6, 58.1, 0.0, 138.2);

        // 1. Start a perpetual flyAround animation so we have something to stop
        map.flyCameraAround(new FlyAroundOptions(targetCamera, 30000L, 10.0));

        // 2. Schedule the stop command after 2 seconds
        new Handler(Looper.getMainLooper()).postDelayed(map::stopCameraAnimation, 2000);
        // [END maps_android_3d_camera_stop_java]
    }

    // [START maps_android_3d_camera_events_java]
    /**
     * Listens to camera change events and logs the visible region.
     */
    @SuppressWarnings("unused")
    @SnippetItem(
        title = "4. Listen Camera Events",
        description = "Demonstrates camera change listening"
    )
    public void listenToCameraEvents() {
        // [START maps_android_3d_camera_events_java]
        final long[] lastLogTime = {0L};
        map.setCameraChangedListener(camera -> {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastLogTime[0] > 500) { // Limit to 1 log per 500ms
                lastLogTime[0] = currentTime;
                Log.d("Maps3D", "Camera State: " +
                        "Center: " + camera.getCenter() +
                        ", Heading: " + camera.getHeading() +
                        ", Tilt: " + camera.getTilt() +
                        ", Roll: " + camera.getRoll() +
                        ", Range: " + camera.getRange());
            }
        });

        // Detach after 5 seconds to prevent log spam
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            map.setCameraChangedListener(null);
        }, 5000);
        // [END maps_android_3d_camera_events_java]
    }

    /**
     * Listens to map steady state events.
     */
    @SuppressWarnings("unused")
    @SnippetItem(
        title = "5. Listen Steady State",
        description = "Logs to the console when the map finishes rendering or enters a steady state."
    )
    public void listenToMapSteadyState() {
        // [START maps_android_3d_camera_steady_java]
        map.setOnMapSteadyListener(isSceneSteady -> {
            Log.d("Maps3D", "Map Is Steady: " + isSceneSteady);
        });
        // [END maps_android_3d_camera_steady_java]
    }

    /**
     * Restricts the camera to a specific altitude range and geographic bounds.
     */
    @SuppressWarnings("unused")
    @SnippetItem(
        title = "6. Camera Restriction",
        description = "Restricts the camera to a specific altitude range and bounding box."
    )
    public void setCameraRestrictions() {
        // [START maps_android_3d_camera_restriction_java]
        LatLngBounds nycBounds = new LatLngBounds(
            40.856492, -73.802409, 40.685630, -74.050304
        );

        CameraRestriction restriction = new CameraRestriction(
            500.0, 10000.0, 0.0, 360.0, 0.0, 90.0, nycBounds
        );

        map.setCameraRestriction(restriction);
        // [END maps_android_3d_camera_restriction_java]

        com.google.android.gms.maps3d.model.Camera camera = new com.google.android.gms.maps3d.model.Camera(
             new com.google.android.gms.maps3d.model.LatLngAltitude(40.748233, -73.985663, 1500.0), // center
             0.0, // heading
             45.0, // tilt
             0.0, // roll
             1000.0 // range
        );
        map.setCamera(camera);
    }
}
