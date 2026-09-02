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

package com.example.placesuikit3d.data.model

import com.example.placesuikit3d.utils.toValidCamera
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude

/**
 * Encapsulates the 3D spatial orientation and position for cinematic camera animations.
 *
 * @property latitude Center point latitude in degrees [-90.0, 90.0]
 * @property longitude Center point longitude in degrees [-180.0, 180.0]
 * @property altitude Center point altitude in meters
 * @property heading Camera bearing in degrees [0.0, 360.0)
 * @property tilt Camera pitch/tilt in degrees [0.0, 90.0]
 * @property range Distance from center target in meters
 * @property roll Camera roll in degrees
 */
data class Camera3DTarget(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val heading: Double = 25.0,
    val tilt: Double = 50.0,
    val range: Double = 1400.0,
    val roll: Double = 0.0,
) {
    /**
     * Converts this target into a Maps 3D SDK [Camera] instance.
     */
    fun toCamera(): Camera = camera {
        center = latLngAltitude {
            latitude = this@Camera3DTarget.latitude
            longitude = this@Camera3DTarget.longitude
            altitude = this@Camera3DTarget.altitude
        }
        heading = this@Camera3DTarget.heading
        tilt = this@Camera3DTarget.tilt
        range = this@Camera3DTarget.range
        roll = this@Camera3DTarget.roll
    }.toValidCamera()

    /**
     * Converts the target position into a [LatLngAltitude].
     */
    fun toLatLngAltitude(): LatLngAltitude = latLngAltitude {
        latitude = this@Camera3DTarget.latitude
        longitude = this@Camera3DTarget.longitude
        altitude = this@Camera3DTarget.altitude
    }

    companion object {
        val BOULDER_OVERVIEW = Camera3DTarget(
            latitude = 39.9989,
            longitude = -105.2828,
            altitude = 1750.0,
            heading = 25.0,
            tilt = 52.0,
            range = 3800.0,
        )

        val SAN_FRANCISCO_OVERVIEW = Camera3DTarget(
            latitude = 37.7749,
            longitude = -122.4194,
            altitude = 30.0,
            heading = 30.0,
            tilt = 52.0,
            range = 2800.0,
        )

        val DEFAULT = BOULDER_OVERVIEW

        fun fromCamera(camera: Camera): Camera3DTarget = Camera3DTarget(
            latitude = camera.center.latitude,
            longitude = camera.center.longitude,
            altitude = camera.center.altitude,
            heading = camera.heading ?: 25.0,
            tilt = camera.tilt ?: 50.0,
            range = camera.range ?: 1400.0,
            roll = camera.roll ?: 0.0,
        )

        fun fromLocation(
            location: LatLngAltitude,
            heading: Double = 25.0,
            tilt: Double = 50.0,
            range: Double = 1400.0,
            roll: Double = 0.0,
        ): Camera3DTarget = Camera3DTarget(
            latitude = location.latitude,
            longitude = location.longitude,
            altitude = location.altitude,
            heading = heading,
            tilt = tilt,
            range = range,
            roll = roll,
        )
    }
}
