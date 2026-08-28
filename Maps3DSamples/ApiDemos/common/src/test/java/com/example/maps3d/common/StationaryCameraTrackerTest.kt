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
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

/**
 * Unit test suite verifying [StationaryCameraTracker] mathematical precision and eye spatial invariance.
 */
class StationaryCameraTrackerTest {

    private lateinit var tracker: StationaryCameraTracker
    private val startLocation = LatLngAltitude(37.8199, -122.4783, 250.0) // Golden Gate Bridge
    private val initialHeading = 106.2
    private val initialTilt = 35.0
    private val initialRange = 2800.0

    @Before
    fun setUp() {
        tracker = StationaryCameraTracker(
            referenceCenter = startLocation,
            initialHeading = initialHeading,
            initialTilt = initialTilt,
            initialRange = initialRange
        )
    }

    @Test
    fun initialPose_matchesInitialCameraParametersExactly() {
        val initialCam = tracker.computeTrackingCamera(startLocation)

        assertThat(initialCam.heading).isWithin(0.001).of(initialHeading)
        assertThat(initialCam.tilt).isWithin(0.001).of(initialTilt)
        assertThat(initialCam.range).isWithin(0.001).of(initialRange)
        assertThat(initialCam.center.latitude).isWithin(1e-6).of(startLocation.latitude)
        assertThat(initialCam.center.longitude).isWithin(1e-6).of(startLocation.longitude)
        assertThat(initialCam.center.altitude).isWithin(0.001).of(startLocation.altitude)
    }

    @Test
    fun eyePosition_remainsInvariantAlongEntireFlightPath() {
        val expectedEye = tracker.fixedEyePosition

        for (waypoint in TourData.AIRPLANE_FLIGHT_PATH) {
            val target = LatLngAltitude(waypoint.latitude, waypoint.longitude, 250.0)
            val trackingCam = tracker.computeTrackingCamera(target)

            // Reconstruct the physical eye position from the newly generated camera
            val reconstructedEye = tracker.reconstructEyePosition(trackingCam)

            // Assert that the physical camera eye has not moved in 3D space
            val eyeDrift = reconstructedEye.distanceTo(expectedEye)
            assertThat(eyeDrift).isLessThan(0.05) // Drift less than 5cm across multiple kilometers
        }
    }

    @Test
    fun range_monotonicallyIncreasesAsTargetFliesAway() {
        var lastRange = 0.0

        for (waypoint in TourData.AIRPLANE_FLIGHT_PATH) {
            val target = LatLngAltitude(waypoint.latitude, waypoint.longitude, 250.0)
            val trackingCam = tracker.computeTrackingCamera(target)

            assertThat(trackingCam.range).isAtLeast(lastRange)
            lastRange = trackingCam.range ?: 0.0
        }

        // Final range at Coit Tower should be substantially larger than the initial 2800m
        val finalTarget = LatLngAltitude(TourData.COIT_TOWER.latitude, TourData.COIT_TOWER.longitude, 250.0)
        val finalCam = tracker.computeTrackingCamera(finalTarget)
        assertThat(finalCam.range).isGreaterThan(7000.0)
    }

    @Test
    fun tilt_remainsWithinValidBounds() {
        for (waypoint in TourData.AIRPLANE_FLIGHT_PATH) {
            val target = LatLngAltitude(waypoint.latitude, waypoint.longitude, 250.0)
            val trackingCam = tracker.computeTrackingCamera(target)

            assertThat(trackingCam.tilt).isAtLeast(0.0)
            assertThat(trackingCam.tilt).isAtMost(90.0)
        }
    }

    @Test
    fun altitudeVariations_correctlyAdjustRangeAndTilt() {
        val expectedEye = tracker.fixedEyePosition

        val testAltitudes = listOf(50.0, 150.0, 250.0, 500.0, 1000.0, 1500.0)
        for (alt in testAltitudes) {
            val target = LatLngAltitude(TourData.AIRPLANE_FLIGHT_PATH[5].latitude, TourData.AIRPLANE_FLIGHT_PATH[5].longitude, alt)
            val trackingCam = tracker.computeTrackingCamera(target)

            val reconstructedEye = tracker.reconstructEyePosition(trackingCam)
            val eyeDrift = reconstructedEye.distanceTo(expectedEye)
            assertThat(eyeDrift).isLessThan(0.05)
        }
    }

    @Test
    fun factoryMethod_createsEquivalentTracker() {
        val initialCam = tracker.computeTrackingCamera(startLocation)
        val fromFactory = StationaryCameraTracker.fromInitialCamera(initialCam)

        val target = LatLngAltitude(TourData.COIT_TOWER.latitude, TourData.COIT_TOWER.longitude, 250.0)
        val cam1 = tracker.computeTrackingCamera(target)
        val cam2 = fromFactory.computeTrackingCamera(target)

        assertThat(cam1.heading ?: 0.0).isWithin(0.001).of(cam2.heading ?: 0.0)
        assertThat(cam1.tilt ?: 0.0).isWithin(0.001).of(cam2.tilt ?: 0.0)
        assertThat(cam1.range ?: 0.0).isWithin(0.001).of(cam2.range ?: 0.0)
    }
}
