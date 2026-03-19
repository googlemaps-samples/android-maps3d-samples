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

import android.util.Log
import com.example.snippets.kotlin.TrackedMap3D
import com.example.snippets.kotlin.annotations.SnippetGroup
import com.example.snippets.kotlin.annotations.SnippetItem
import com.example.snippets.kotlin.utils.awaitAnimation
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.flyAroundOptions
import com.google.android.gms.maps3d.model.flyToOptions
import com.google.android.gms.maps3d.model.latLngAltitude
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@SnippetGroup(
    title = "Camera",
    description = "Snippets demonstrating dynamic camera orchestration and animations."
)
class CameraControlSnippets(
    private val map: TrackedMap3D,
    private val lifecycleScope: kotlinx.coroutines.CoroutineScope
) {

    /**
     * Animates the camera to a specific position (coordinates, heading, tilt) over a duration.
     */
    @Suppress("unused")
    @SnippetItem(
        title = "1. Fly To",
        description = "Animates the camera to a specific position with a tilt and heading over 5 seconds."
    )
    fun flyCameraToPosition() {
        // [START maps_android_3d_camera_fly_to_kt]
        val targetCamera = camera {
            center = latLngAltitude {
                latitude = 38.743829
                longitude = -109.499512
                altitude = 1460.37
            }
            tilt = 76.16
            heading = 338.52
            range = 191.71
            roll = 0.0
        }
        
        val options = flyToOptions {
            endCamera = targetCamera
            durationInMillis = 5000
        }

        map.flyCameraTo(options)
        // [END maps_android_3d_camera_fly_to_kt]
    }

    /**
     * Orbits the camera around a specific location.
     */
    @Suppress("unused")
    @SnippetItem(
        title = "2. Fly Around",
        description = "Rotates the camera 360 degrees around a specific location over 10 seconds."
    )
    fun flyCameraAroundLocation() {
        // [START maps_android_3d_camera_fly_around_kt]
        lifecycleScope.launch {
            val targetCamera = camera {
                center = latLngAltitude {
                    latitude = 38.743502
                    longitude = -109.499374
                    altitude = 1467.0
                }
                tilt = 58.1
                heading = 349.6
                range = 138.2
                roll = 0.0
            }

            // Although not completely necessary, the experience will be usually be better by
            // waiting for the camera to steady before starting the flyCameraAround
            map.setCamera(targetCamera)

            // Orbit around the target using DSL
            val options = flyAroundOptions {
                center = targetCamera
                rounds = 2.0 // 2 full rotations
                durationInMillis = 6_000
            }

            map.setOnMapSteadyListener { isSceneSteady: Boolean ->
                if (isSceneSteady) {
                    map.setOnMapSteadyListener(null) // Cleanup
                    map.flyCameraAround(options)
                }
            }
        }
        // [END maps_android_3d_camera_fly_around_kt]
    }

    /**
     * Stops the current camera animation.
     */
    @Suppress("unused")
    @SnippetItem(
        title = "3. Stop Animation",
        description = "Stops any currently running camera animation immediately."
    )
    fun stopAnimation() {
        // [START maps_android_3d_camera_stop_kt]
        lifecycleScope.launch {
            val targetCamera = camera {
                center = latLngAltitude {
                    latitude = 38.743502
                    longitude = -109.499374
                    altitude = 1467.0
                }
                tilt = 58.1
                heading = 349.6
                range = 138.2
                roll = 0.0
            }

            // [START_EXCLUDE]
            val options = flyToOptions {
                endCamera = targetCamera
                durationInMillis = 3000
            }

            map.awaitAnimation {
                map.flyCameraTo(options)
            }

            // [END_EXCLUDE]
            
            // 1. Start a perpetual flyAround animation so we have something to stop
            map.flyCameraAround(flyAroundOptions {
                center = targetCamera
                rounds = 10.0
                durationInMillis = 30_000
            })

            lifecycleScope.launch {
                delay(2.seconds) // Let it fly for 2 seconds
                map.stopCameraAnimation() // 2. Stop it immediately
                Log.d("Maps3D", "Camera stopped")
            }
        }
        // [END maps_android_3d_camera_stop_kt]
    }

    /**
     * Listens to camera change events and logs the visible region.
     */
    @Suppress("unused")
    @SnippetItem(
        title = "4. Listen Camera Events",
        description = "Logs camera change events to the console, printing the center coordinates as the camera moves."
    )
    fun listenToCameraEvents() {
        // [START maps_android_3d_camera_events_kt]
        var lastLogTime = 0L
        map.setCameraChangedListener { camera ->
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastLogTime > 500) { // Limit to 1 log per 500ms
                lastLogTime = currentTime
                Log.d("Maps3D", "Camera State: " +
                        "Center: ${camera.center}, " +
                        "Heading: ${camera.heading}, " +
                        "Tilt: ${camera.tilt}, " +
                        "Roll: ${camera.roll}, " +
                        "Range: ${camera.range}")
            }
        }
        // [END maps_android_3d_camera_events_kt]

        lifecycleScope.launch {
            delay(5.seconds)
            map.setCameraChangedListener(null)
        }
    }

    /**
     * Listens to map steady state events.
     */
    @Suppress("unused")
    @SnippetItem(
        title = "5. Listen Steady State",
        description = "Logs to the console when the map finishes rendering or enters a steady state."
    )
    fun listenToMapSteadyState() {
        // [START maps_android_3d_camera_steady_kt]
        map.setOnMapSteadyListener { isSceneSteady: Boolean ->
            Log.d("Maps3D", "Map Is Steady: $isSceneSteady")
        }
        // [END maps_android_3d_camera_steady_kt]
    }
}
