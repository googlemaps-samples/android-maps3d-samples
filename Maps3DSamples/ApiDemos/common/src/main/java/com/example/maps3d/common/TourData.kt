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

/**
 * Keyframe representation for scripted camera tour stages.
 */
sealed class CameraKeyframe(val stepTitle: String, val stepDescription: String) {

    class FlyTo(
        stepTitle: String,
        stepDescription: String,
        val targetCenter: LatLng,
        val targetAltitude: Double,
        val targetHeading: Double,
        val targetTilt: Double,
        val targetRange: Double,
        val durationMs: Long
    ) : CameraKeyframe(stepTitle, stepDescription)

    class DwellPause(
        stepTitle: String,
        stepDescription: String,
        val durationMs: Long
    ) : CameraKeyframe(stepTitle, stepDescription)

    class OrbitAround(
        stepTitle: String,
        stepDescription: String,
        val center: LatLng,
        val altitude: Double,
        val range: Double,
        val tilt: Double,
        val startHeading: Double,
        val endHeading: Double,
        val durationMs: Long
    ) : CameraKeyframe(stepTitle, stepDescription)
}

/**
 * Shared pre-baked flight route and tour keyframes for Advanced Camera Animation samples.
 */
object TourData {

    const val MODEL_ID = "airplane_model"
    const val PLANE_URL = "https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/Airplane.glb"

    /**
     * 4-Stage cinematic camera tour across San Francisco landmarks.
     */
    @JvmField
    val SAN_FRANCISCO_TOUR: List<CameraKeyframe> = listOf(
        CameraKeyframe.FlyTo(
            stepTitle = "1. Golden Gate Bridge Flight",
            stepDescription = "3D Airplane flight over Golden Gate Bridge",
            targetCenter = LatLng(37.8199, -122.4783),
            targetAltitude = 200.0,
            targetHeading = 105.0,
            targetTilt = 65.0,
            targetRange = 600.0,
            durationMs = 2500L
        ),
        CameraKeyframe.DwellPause(
            stepTitle = "2. Mid-Air Observation",
            stepDescription = "Dwell pause observing 3D airplane over Golden Gate",
            durationMs = 1500L
        ),
        CameraKeyframe.OrbitAround(
            stepTitle = "3. Golden Gate 360° Orbit",
            stepDescription = "360° orbital camera spin around flying airplane",
            center = LatLng(37.8199, -122.4783),
            altitude = 200.0,
            range = 600.0,
            tilt = 65.0,
            startHeading = 105.0,
            endHeading = 465.0,
            durationMs = 4000L
        ),
        CameraKeyframe.FlyTo(
            stepTitle = "4. Transit to Coit Tower",
            stepDescription = "Airplane flight to Coit Tower Landmark",
            targetCenter = LatLng(37.8024, -122.4058),
            targetAltitude = 200.0,
            targetHeading = 105.0,
            targetTilt = 65.0,
            targetRange = 600.0,
            durationMs = 3000L
        )
    )

    /**
     * 15 Fine-grained waypoints along the scenic flight corridor from Golden Gate Bridge to Coit Tower.
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
        LatLng(37.8095, -122.4415), // 8. Fort Mason Great Meadow
        LatLng(37.8075, -122.4350), // 9. Aquatic Park / Ghirardelli Square
        LatLng(37.8060, -122.4285), // 10. Fisherman's Wharf West
        LatLng(37.8050, -122.4220), // 11. Pier 39 / North Waterfront
        LatLng(37.8040, -122.4160), // 12. Washington Square Park / North Beach
        LatLng(37.8032, -122.4105), // 13. Telegraph Hill West Approach
        LatLng(37.8026, -122.4075), // 14. Telegraph Hill Boulevard
        LatLng(37.8024, -122.4058)  // 15. Coit Tower Landmark (Destination)
    )
}
