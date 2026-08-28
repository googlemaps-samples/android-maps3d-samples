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

package com.example.maps3d.common

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude

/**
 * Supported camera animation paradigms showcased in the demo.
 */
enum class AnimationApproach(val title: String) {
    SIMPLE_FLY_TO("1. SDK Simple flyTo (Native Transition)"),
    KEYFRAME_TOUR("2. Declarative Keyframe Queue Tour"),
    DISPATCHER_FRAME_LOOP("3. High-Rate Frame Dispatcher Loop"),
    ORBIT_360_SPIN("4. 360-Degree Continuous Orbit Spin")
}

/**
 * Represents a single declarative step in a multi-step camera flight tour.
 */
sealed interface CameraKeyframe {
    val stepTitle: String
    val stepDescription: String
    val durationMs: Long

    data class FlyTo(
        override val stepTitle: String,
        override val stepDescription: String,
        val targetCamera: Camera,
        override val durationMs: Long = 3500L
    ) : CameraKeyframe

    data class DwellPause(
        override val stepTitle: String,
        override val stepDescription: String,
        override val durationMs: Long = 2000L
    ) : CameraKeyframe

    data class StationaryTrackingFlight(
        override val stepTitle: String = "Step 4 of 4: Stationary Vantage Tracking Flight",
        override val stepDescription: String = "Camera remains stationary at high vantage point while tracking the plane flying to Coit Tower.",
        val observationCamera: Camera = TourData.OVERVIEW_CAMERA,
        val flightPath: List<LatLng> = TourData.AIRPLANE_FLIGHT_PATH,
        override val durationMs: Long = 6000L
    ) : CameraKeyframe

    data class FlyAround(
        override val stepTitle: String,
        override val stepDescription: String,
        val centerCamera: Camera,
        val rounds: Double = 1.0,
        override val durationMs: Long = 6000L
    ) : CameraKeyframe
}

/**
 * Shared geographical coordinates and tour definitions for Advanced Camera Animation.
 */
object TourData {

    const val AIRPLANE_MODEL_ID = "airplane_model"
    const val AIRPLANE_MODEL_URL = "https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/Airplane.glb"

    val GOLDEN_GATE_BRIDGE = LatLng(37.8199, -122.4783)
    val COIT_TOWER = LatLng(37.8024, -122.4058)

    /**
     * High-altitude overview camera used at the start of the Keyframe Tour.
     */
    @JvmField
    val OVERVIEW_CAMERA: Camera = camera {
        center = latLngAltitude {
            latitude = 37.8199
            longitude = -122.4783
            altitude = 250.0
        }
        heading = 106.2
        tilt = 35.0
        range = 2800.0
    }

    /**
     * Close-range flight camera behind the airplane over Golden Gate Bridge.
     */
    @JvmField
    val CLOSE_INSPECTION_CAMERA: Camera = camera {
        center = latLngAltitude {
            latitude = 37.8199
            longitude = -122.4783
            altitude = 250.0
        }
        heading = 106.2
        tilt = 65.0
        range = 600.0
    }

    /**
     * Close inspection camera at Coit Tower on Telegraph Hill looking back west over San Francisco.
     */
    @JvmField
    val COIT_TOWER_INSPECTION_CAMERA: Camera = camera {
        center = latLngAltitude {
            latitude = 37.8024
            longitude = -122.4058
            altitude = 250.0
        }
        heading = 286.2 // Pointing back west in the direction the plane is coming from
        tilt = 65.0
        range = 600.0
    }

    /**
     * 15 fine-grained waypoints along the direct aerial corridor from Golden Gate Bridge to Coit Tower.
     */
    @JvmField
    val AIRPLANE_FLIGHT_PATH: List<LatLng> = listOf(
        LatLng(37.8199, -122.4783), // 1. Golden Gate Bridge (Source)
        LatLng(37.8188, -122.4735), // 2. Fort Point / Presidio Overlook
        LatLng(37.8175, -122.4685), // 3. Crissy Field West
        LatLng(37.8160, -122.4635), // 4. Crissy Field East
        LatLng(37.8145, -122.4585), // 5. Marina Green West
        LatLng(37.8130, -122.4530), // 6. Marina District Center
        LatLng(37.8115, -122.4475), // 7. Fort Mason West
        LatLng(37.8100, -122.4420), // 8. Fort Mason Heights
        LatLng(37.8085, -122.4365), // 9. Aquatic Park Cove
        LatLng(37.8070, -122.4310), // 10. Fisherman's Wharf West
        LatLng(37.8058, -122.4250), // 11. Fisherman's Wharf Center
        LatLng(37.8048, -122.4195), // 12. Pier 39 Promenade
        LatLng(37.8038, -122.4140), // 13. Embarcadero North
        LatLng(37.8030, -122.4090), // 14. Telegraph Hill Slopes
        LatLng(37.8024, -122.4058)  // 15. Coit Tower (Destination)
    )

    /**
     * Standard 4-step San Francisco aerial tour keyframe sequence.
     */
    @JvmField
    val SAN_FRANCISCO_TOUR: List<CameraKeyframe> = listOf(
        CameraKeyframe.FlyTo(
            stepTitle = "Step 1 of 5: High-Altitude Swoop In",
            stepDescription = "Swooping down from 1200m high-altitude overview into close flight alignment (250m) behind the airplane.",
            targetCamera = CLOSE_INSPECTION_CAMERA,
            durationMs = 3500L
        ),
        CameraKeyframe.DwellPause(
            stepTitle = "Step 2 of 5: Mid-Air Inspection Pause",
            stepDescription = "Dwell pause (2.0s) holding camera lock to inspect the 3D airplane model above the Golden Gate Bridge.",
            durationMs = 2000L
        ),
        CameraKeyframe.FlyAround(
            stepTitle = "Step 3 of 5: 360° Orbital Revolution",
            stepDescription = "Smooth 360° hardware-accelerated orbit around the airplane over Golden Gate Bridge using SDK flyCameraAround.",
            centerCamera = CLOSE_INSPECTION_CAMERA,
            rounds = 1.0,
            durationMs = 6000L
        ),
        CameraKeyframe.StationaryTrackingFlight(
            stepTitle = "Step 4 of 5: Stationary Vantage Tracking Flight",
            stepDescription = "Camera returns to high vantage point, remaining stationary while tracking the airplane flying to Coit Tower.",
            observationCamera = OVERVIEW_CAMERA,
            flightPath = AIRPLANE_FLIGHT_PATH,
            durationMs = 6000L
        ),
        CameraKeyframe.FlyTo(
            stepTitle = "Step 5 of 5: Native Transit Flight to Coit Tower",
            stepDescription = "Native SDK flyCameraTo transition flying directly to Coit Tower on Telegraph Hill, inspecting the destination.",
            targetCamera = COIT_TOWER_INSPECTION_CAMERA,
            durationMs = 4000L
        )
    )
}
