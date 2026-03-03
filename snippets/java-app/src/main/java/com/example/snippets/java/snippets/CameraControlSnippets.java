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

package com.example.snippets.java.snippets;

import androidx.annotation.NonNull;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.OnCameraChangedListener;
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.FlyAroundOptions;
import com.google.android.gms.maps3d.model.FlyToOptions;
import com.google.android.gms.maps3d.model.LatLngAltitude;

public class CameraControlSnippets {

    private final GoogleMap3D map;

    public CameraControlSnippets(GoogleMap3D map) {
        this.map = map;
    }

    // [START maps_android_3d_camera_fly_to_java]
    /**
     * Animates the camera to a specific position (coordinates, heading, tilt) over
     * a duration.
     */
    public void flyCameraToPosition() {
        LatLngAltitude center = new LatLngAltitude(37.4220, -122.0841, 100.0);
        // Create a target camera:
        // center: LatLngAltitude target
        // heading: 90.0 (East)
        // tilt: 45.0 degrees
        // roll: 0.0 (level)
        // range: 1000.0 meters
        Camera targetCamera = new Camera(center, 90.0, 45.0, 0.0, 1000.0);

        // FlyToOptions constructor: endCamera, durationInMillis
        FlyToOptions options = new FlyToOptions(targetCamera, 5000L);

        map.flyCameraTo(options);
    }
    // [END maps_android_3d_camera_fly_to_java]

    // [START maps_android_3d_camera_fly_around_java]
    /**
     * Orbits the camera around a specific location.
     */
    public void flyCameraAroundLocation() {
        LatLngAltitude center = new LatLngAltitude(37.4220, -122.0841, 0.0);
        // Create a target camera:
        // center: LatLngAltitude target
        // heading: 0.0 (North)
        // tilt: 45.0 degrees
        // roll: 0.0 (level)
        // range: 500.0 meters
        Camera targetCamera = new Camera(center, 0.0, 45.0, 0.0, 500.0);

        // Orbit around the target
        // FlyAroundOptions constructor: center, durationInMillis, rounds
        FlyAroundOptions options = new FlyAroundOptions(targetCamera, 10000L, 1.0);

        map.flyCameraAround(options);
    }
    // [END maps_android_3d_camera_fly_around_java]

    // [START maps_android_3d_camera_stop_java]
    /**
     * Stops the current camera animation.
     */
    public void stopAnimation() {
        LatLngAltitude center = new LatLngAltitude(37.4220, -122.0841, 0.0);
        Camera targetCamera = new Camera(center, 0.0, 45.0, 0.0, 500.0);
        FlyAroundOptions options = new FlyAroundOptions(targetCamera, 30000L, 10.0);

        // 1. Start an animation so we have something to stop
        map.flyCameraAround(options);

        // 2. Schedule the stop command after 3000ms (3 seconds)
        new Handler(Looper.getMainLooper()).postDelayed(map::stopCameraAnimation, 3000);
    }
    // [END maps_android_3d_camera_stop_java]

    // [START maps_android_3d_camera_events_java]
    // [START maps_android_3d_camera_events_java]
    /**
     * Listens to camera change events and logs the visible region.
     */
    public void listenToCameraEvents() {
        map.setCameraChangedListener(camera -> Log.d("Maps3D", "Camera State: " +
                "Center: " + camera.getCenter() +
                ", Heading: " + camera.getHeading() +
                ", Tilt: " + camera.getTilt() +
                ", Roll: " + camera.getRoll() +
                ", Range: " + camera.getRange()));
    }
    // [END maps_android_3d_camera_events_java]
}
