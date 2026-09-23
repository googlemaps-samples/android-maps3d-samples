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

import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests verifying [Projection3D] perspective projection mathematics and coordinate accuracy.
 */
class Projection3DTest {

    private val origin = LatLngAltitude(37.7952, -122.4028, 0.0) // San Francisco
    private val width = 1000
    private val height = 1000

    @Test
    fun cameraCenter_projectsToExactScreenCenter() {
        val testCamera = camera {
            center = origin
            heading = 45.0
            tilt = 30.0
            roll = 0.0
            range = 1000.0
        }

        val projection = Projection3D(
            camera = testCamera,
            viewportWidth = width,
            viewportHeight = height,
            fovYDegrees = 45.0
        )

        val screenCoord = projection.toScreenCoordinate(origin)

        assertThat(screenCoord.isVisible).isTrue()
        assertThat(screenCoord.depth).isWithin(0.1).of(1000.0)
        assertThat(screenCoord.x).isWithin(0.5f).of(500f)
        assertThat(screenCoord.y).isWithin(0.5f).of(500f)

        val point = projection.toScreenLocation(origin)
        assertThat(point).isNotNull()
    }

    @Test
    fun pointBehindCamera_isMarkedNotVisible() {
        // Camera looking North (heading 0), tilted down at 45 degrees, range 1000m.
        // Eye is located South of origin. A point placed far behind the camera eye will have negative depth.
        val testCamera = camera {
            center = origin
            heading = 0.0
            tilt = 45.0
            roll = 0.0
            range = 1000.0
        }

        val projection = Projection3D(
            camera = testCamera,
            viewportWidth = width,
            viewportHeight = height
        )

        // Point far south (behind camera eye)
        val farBehindPoint = LatLngAltitude(origin.latitude - 0.1, origin.longitude, 0.0)
        val screenCoord = projection.toScreenCoordinate(farBehindPoint)

        assertThat(screenCoord.isVisible).isFalse()
        assertThat(screenCoord.depth).isLessThan(0.0)
        assertThat(screenCoord.x.isNaN()).isTrue()
        assertThat(screenCoord.y.isNaN()).isTrue()
        assertThat(projection.toScreenLocation(farBehindPoint)).isNull()
    }

    @Test
    fun cardinalDirections_projectToCorrectScreenQuadrants() {
        // Camera looking straight down (tilt = 0, heading = 0)
        val nadirCamera = camera {
            center = origin
            heading = 0.0
            tilt = 0.0
            roll = 0.0
            range = 1000.0
        }

        val projection = Projection3D(
            camera = nadirCamera,
            viewportWidth = width,
            viewportHeight = height
        )

        // Point East of center -> should project to right half of screen (X > 500)
        val eastPoint = LatLngAltitude(origin.latitude, origin.longitude + 0.001, 0.0)
        val eastCoord = projection.toScreenCoordinate(eastPoint)
        assertThat(eastCoord.isVisible).isTrue()
        assertThat(eastCoord.x).isGreaterThan(500f)
        assertThat(eastCoord.y).isWithin(1.0f).of(500f)

        // Point West of center -> should project to left half of screen (X < 500)
        val westPoint = LatLngAltitude(origin.latitude, origin.longitude - 0.001, 0.0)
        val westCoord = projection.toScreenCoordinate(westPoint)
        assertThat(westCoord.isVisible).isTrue()
        assertThat(westCoord.x).isLessThan(500f)
        assertThat(westCoord.y).isWithin(1.0f).of(500f)

        // Point North of center -> should project to top half of screen (Y < 500 in screen pixels)
        val northPoint = LatLngAltitude(origin.latitude + 0.001, origin.longitude, 0.0)
        val northCoord = projection.toScreenCoordinate(northPoint)
        assertThat(northCoord.isVisible).isTrue()
        assertThat(northCoord.x).isWithin(1.0f).of(500f)
        assertThat(northCoord.y).isLessThan(500f)

        // Point South of center -> should project to bottom half of screen (Y > 500 in screen pixels)
        val southPoint = LatLngAltitude(origin.latitude - 0.001, origin.longitude, 0.0)
        val southCoord = projection.toScreenCoordinate(southPoint)
        assertThat(southCoord.isVisible).isTrue()
        assertThat(southCoord.x).isWithin(1.0f).of(500f)
        assertThat(southCoord.y).isGreaterThan(500f)
    }

    @Test
    fun fromScreenLocation_centerPixel_recoversCameraCenter() {
        val testCamera = camera {
            center = origin
            heading = 35.0
            tilt = 45.0
            roll = 0.0
            range = 800.0
        }

        val projection = Projection3D(
            camera = testCamera,
            viewportWidth = width,
            viewportHeight = height
        )

        // Center pixel (500, 500) should unproject back to origin coordinates
        val unprojected = projection.fromScreenLocation(500f, 500f, targetAltitude = 0.0)

        assertThat(unprojected).isNotNull()
        assertThat(unprojected?.latitude).isWithin(1e-5).of(origin.latitude)
        assertThat(unprojected?.longitude).isWithin(1e-5).of(origin.longitude)
        assertThat(unprojected?.altitude).isWithin(0.1).of(0.0)
    }

    @Test
    fun roundTrip_projectAndUnproject_matchesOriginalGroundCoordinates() {
        val testCamera = camera {
            center = origin
            heading = 60.0
            tilt = 30.0
            roll = 0.0
            range = 1200.0
        }

        val projection = Projection3D(
            camera = testCamera,
            viewportWidth = width,
            viewportHeight = height
        )

        // Nearby landmark point on the ground
        val testPoint = LatLngAltitude(origin.latitude + 0.001, origin.longitude + 0.001, 0.0)
        val screenCoord = projection.toScreenCoordinate(testPoint)
        assertThat(screenCoord.isVisible).isTrue()

        val recovered = projection.fromScreenLocation(screenCoord.x, screenCoord.y, targetAltitude = 0.0)
        assertThat(recovered).isNotNull()
        assertThat(recovered?.latitude).isWithin(1e-5).of(testPoint.latitude)
        assertThat(recovered?.longitude).isWithin(1e-5).of(testPoint.longitude)
    }
}
