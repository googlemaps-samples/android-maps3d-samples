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

package com.example.snippets.kotlin.snippets

import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.camera
import android.util.Log
import com.google.android.gms.maps3d.model.flyAroundOptions
import com.google.android.gms.maps3d.model.flyToOptions
import com.google.android.gms.maps3d.model.latLngAltitude

class CameraControlSnippets(private val map: GoogleMap3D) {

    // [START maps_android_3d_camera_fly_to_kt]
    /**
     * Animates the camera to a specific position (coordinates, heading, tilt) over a duration.
     */
    fun flyCameraToPosition() {
        val targetCamera = camera {
            center = latLngAltitude {
                latitude = 37.4220
                longitude = -122.0841
                altitude = 100.0
            }
            tilt = 45.0
            heading = 90.0
        }
        
        val options = flyToOptions {
            endCamera = targetCamera
            durationInMillis = 5000
        }

        map.flyCameraTo(options)
    }
    // [END maps_android_3d_camera_fly_to_kt]

    // [START maps_android_3d_camera_fly_around_kt]
    /**
     * Orbits the camera around a specific location.
     */
    fun flyCameraAroundLocation() {
        val targetCamera = camera {
             center = latLngAltitude {
                latitude = 37.4220
                longitude = -122.0841
                altitude = 0.0
             }
        }
        
        // Orbit around the target using DSL
        val options = flyAroundOptions {
            center = targetCamera
            rounds = 1.0 // 1 full rotation
            durationInMillis = 10000
        }

        map.flyCameraAround(options)
    }
    // [END maps_android_3d_camera_fly_around_kt]

    // [START maps_android_3d_camera_stop_kt]
    /**
     * Stops the current camera animation.
     */
    fun stopAnimation() {
        map.stopCameraAnimation()
    }
    // [END maps_android_3d_camera_stop_kt]

    // [START maps_android_3d_camera_events_kt]
    // [START maps_android_3d_camera_events_kt]
    /**
     * Listens to camera change events and logs the visible region.
     */
    fun listenToCameraEvents() {
        map.setCameraChangedListener { camera ->
            Log.d("Maps3D", "Camera State: " +
                    "Center: ${camera.center}, " +
                    "Heading: ${camera.heading}, " +
                    "Tilt: ${camera.tilt}, " +
                    "Roll: ${camera.roll}, " +
                    "Range: ${camera.range}")
        }
    }
    // [END maps_android_3d_camera_events_kt]
}
