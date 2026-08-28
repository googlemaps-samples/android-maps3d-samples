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
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.maps.android.SphericalUtil
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Represents a 3D Cartesian coordinate in a local East-North-Up (ENU) metric frame.
 *
 * @property east East displacement in meters (+East / -West).
 * @property north North displacement in meters (+North / -South).
 * @property up Altitude in meters above sea level (+Up / -Down).
 */
data class Cartesian3D(
    val east: Double,
    val north: Double,
    val up: Double
) {
    /** Computes Euclidean distance to another point. */
    fun distanceTo(other: Cartesian3D): Double {
        val dx = east - other.east
        val dy = north - other.north
        val dz = up - other.up
        return sqrt(dx * dx + dy * dy + dz * dz)
    }
}

/**
 * Mathematical controller that locks the physical camera eye at a stationary 3D spatial position
 * while continuously tracking a moving entity (such as an airplane flying across San Francisco Bay).
 *
 * ### Mathematical Mechanics & Edicts of Literate Programming
 *
 * The Google Maps 3D SDK defines camera poses from the perspective of their focal center:
 * `Camera(center = target, heading = H, tilt = θ, range = R)`
 *
 * When the focal target moves from an initial location $P_0$ to a destination $P(t)$, maintaining
 * a fixed physical observation vantage point requires computing the inverse spherical parameters:
 *
 * 1. **Initial Vantage Point Derivation**:
 *    From the initial observation camera parameters $(P_0, H_0, 	heta_0, R_0)$, the physical eye is
 *    located behind the target in direction $(H_0 + 180°)$:
 *    - Horizontal Ground Distance: $D_0 = R_0 \cdot \sin(\theta_0)$
 *    - Vertical Height Above Target: $\Delta Z_0 = R_0 \cdot \cos(\theta_0)$
 *    - Fixed 3D Eye Coordinate:
 *      $$E_{\text{eye}} = - D_0 \cdot \sin(H_0)$$
 *      $$N_{\text{eye}} = - D_0 \cdot \cos(H_0)$$
 *      $$U_{\text{eye}} = P_0.\text{alt} + \Delta Z_0$$
 *
 * 2. **Target Tracking & Inverse Projection**:
 *    As the target moves to $P(t)$, its ENU position $(E_t, N_t, U_t)$ relative to $P_0$ is computed
 *    via geodesic spherical trigonometry. The line-of-sight vector from the fixed eye to the target is:
 *    $$\mathbf{V} = (E_t - E_{\text{eye}}, N_t - N_{\text{eye}}, U_t - U_{\text{eye}})$$
 *    - **Range**: $R(t) = \|\mathbf{V}\| = \sqrt{\Delta E^2 + \Delta N^2 + \Delta U^2}$
 *    - **Heading**: $H(t) = \operatorname{atan2}(\Delta E, \Delta N) \pmod{360°}$
 *    - **Tilt**: $\theta(t) = \operatorname{atan2}(\sqrt{\Delta E^2 + \Delta N^2}, U_{\text{eye}} - U_t)$
 */
class StationaryCameraTracker(
    val referenceCenter: LatLngAltitude,
    val initialHeading: Double,
    val initialTilt: Double,
    val initialRange: Double
) {
    /** The permanently fixed 3D Cartesian position of the camera eye in ENU space. */
    val fixedEyePosition: Cartesian3D = computeInitialEyePosition()

    private fun computeInitialEyePosition(): Cartesian3D {
        val tiltRad = initialTilt * (PI / 180.0)
        val headingRad = initialHeading * (PI / 180.0)
        val horizDist = initialRange * sin(tiltRad)
        val vertOffset = initialRange * cos(tiltRad)

        val eastOffset = -horizDist * sin(headingRad)
        val northOffset = -horizDist * cos(headingRad)
        val upAltitude = referenceCenter.altitude + vertOffset

        return Cartesian3D(east = eastOffset, north = northOffset, up = upAltitude)
    }

    /**
     * Calculates the dynamic [Camera] pose required to keep the physical camera eye stationary
     * while focusing on the moving target at [targetLocation].
     *
     * @param targetLocation The dynamic 3D location of the target entity.
     * @return A [Camera] configuration centered on [targetLocation] with inverse-projected orientation.
     */
    fun computeTrackingCamera(targetLocation: LatLngAltitude): Camera {
        // Geodesic offset from reference origin to current target
        val targetLatLng = LatLng(targetLocation.latitude, targetLocation.longitude)
        val refLatLng = LatLng(referenceCenter.latitude, referenceCenter.longitude)

        val dist = SphericalUtil.computeDistanceBetween(refLatLng, targetLatLng)
        val bearingRad = if (dist > 0.001) {
            SphericalUtil.computeHeading(refLatLng, targetLatLng) * (PI / 180.0)
        } else {
            0.0
        }

        val targetEast = dist * sin(bearingRad)
        val targetNorth = dist * cos(bearingRad)
        val targetUp = targetLocation.altitude

        // Line of sight vector from fixed eye to target
        val deltaEast = targetEast - fixedEyePosition.east
        val deltaNorth = targetNorth - fixedEyePosition.north
        val deltaUp = targetUp - fixedEyePosition.up

        val horizDist = sqrt(deltaEast * deltaEast + deltaNorth * deltaNorth)
        val range = sqrt(deltaEast * deltaEast + deltaNorth * deltaNorth + deltaUp * deltaUp)

        // Heading: direction from eye to target in degrees [0, 360)
        val headingDeg = (atan2(deltaEast, deltaNorth) * (180.0 / PI) + 360.0) % 360.0

        // Tilt: angle with vertical nadir in degrees [0, 90]
        val verticalDrop = fixedEyePosition.up - targetUp
        val tiltDeg = if (verticalDrop > 0.0) {
            (atan2(horizDist, verticalDrop) * (180.0 / PI)).coerceIn(0.0, 89.9)
        } else {
            89.9
        }

        return camera {
            center = latLngAltitude {
                latitude = targetLocation.latitude
                longitude = targetLocation.longitude
                altitude = targetLocation.altitude
            }
            heading = headingDeg
            tilt = tiltDeg
            this.range = range
        }
    }

    /**
     * Convenience method to compute the tracking camera for an [EntityPose].
     */
    fun computeTrackingCamera(pose: EntityPose): Camera =
        computeTrackingCamera(pose.position)

    /**
     * Verifies the invariant: reconstructs the physical eye position from any [Camera] object
     * relative to [referenceCenter].
     */
    fun reconstructEyePosition(camera: Camera): Cartesian3D {
        val targetLatLng = LatLng(camera.center.latitude, camera.center.longitude)
        val refLatLng = LatLng(referenceCenter.latitude, referenceCenter.longitude)

        val dist = SphericalUtil.computeDistanceBetween(refLatLng, targetLatLng)
        val bearingRad = if (dist > 0.001) {
            SphericalUtil.computeHeading(refLatLng, targetLatLng) * (PI / 180.0)
        } else {
            0.0
        }

        val targetEast = dist * sin(bearingRad)
        val targetNorth = dist * cos(bearingRad)
        val targetUp = camera.center.altitude

        val tilt = camera.tilt ?: 0.0
        val heading = camera.heading ?: 0.0
        val range = camera.range ?: 1000.0

        val tiltRad = tilt * (PI / 180.0)
        val headingRad = heading * (PI / 180.0)
        val horizDist = range * sin(tiltRad)
        val vertOffset = range * cos(tiltRad)

        val eyeEast = targetEast - horizDist * sin(headingRad)
        val eyeNorth = targetNorth - horizDist * cos(headingRad)
        val eyeUp = targetUp + vertOffset

        return Cartesian3D(east = eyeEast, north = eyeNorth, up = eyeUp)
    }

    companion object {
        /**
         * Factory method to construct a tracker from an initial [Camera].
         */
        fun fromInitialCamera(camera: Camera): StationaryCameraTracker =
            StationaryCameraTracker(
                referenceCenter = camera.center,
                initialHeading = camera.heading ?: 0.0,
                initialTilt = camera.tilt ?: 0.0,
                initialRange = camera.range ?: 1000.0
            )
    }
}
