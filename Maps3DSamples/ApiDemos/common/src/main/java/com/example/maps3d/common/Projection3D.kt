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

import android.graphics.Point
import android.graphics.PointF
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.maps.android.SphericalUtil
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * Encapsulates a projected 2D screen coordinate and its view frustum visibility state.
 *
 * @property x The horizontal pixel position on screen (0.0 = left edge, [viewportWidth] = right edge).
 * @property y The vertical pixel position on screen (0.0 = top edge, [viewportHeight] = bottom edge).
 * @property isVisible True if the point is in front of the camera and within the visible view frustum.
 * @property depth Distance in meters along the camera line of sight (depth > 0 means in front of camera).
 */
data class ScreenCoordinate(
    val x: Float,
    val y: Float,
    val isVisible: Boolean,
    val depth: Double
) {
    /** Converts the projected position to an integer [Point] for standard Android View layout positioning. */
    fun toPoint(): Point = Point(x.toInt(), y.toInt())

    /** Converts the projected position to a high-precision [PointF] for subpixel positioning. */
    fun toPointF(): PointF = PointF(x, y)
}

/**
 * Rudimentary 3D Perspective Projection Engine for the Google Maps 3D SDK.
 *
 * Provides coordinate transformations between 3D world coordinates ([LatLngAltitude]) and
 * 2D screen viewport pixel space ([ScreenCoordinate] / [PointF]).
 *
 * ### Mathematical Mechanics & Literate Formulation
 *
 * In the Google Maps 3D SDK, the camera pose is defined by its focal center $\mathbf{P}_0 = (\text{lat}_0, \text{lng}_0, \text{alt}_0)$,
 * viewing heading $H$, vertical tilt $\theta$, roll $\phi$, and observation range $R$:
 *
 * 1. **Camera Eye Vantage Point**:
 *    In local East-North-Up (ENU) tangent space centered at $\mathbf{P}_0$, the physical camera eye is located:
 *    - $D = R \cdot \sin(\theta)$ (Horizontal Ground Distance)
 *    - $\Delta Z = R \cdot \cos(\theta)$ (Vertical Height above target center)
 *    - $\mathbf{E}_{\text{eye}} = -D \cdot \sin(H)$
 *    - $\mathbf{N}_{\text{eye}} = -D \cdot \cos(H)$
 *    - $\mathbf{U}_{\text{eye}} = \Delta Z$
 *
 * 2. **Camera Orthonormal Basis**:
 *    - Forward vector: $\mathbf{f} = (\sin\theta \sin H, \; \sin\theta \cos H, \; -\cos\theta)$
 *    - Right vector: $\mathbf{r} = (\cos H, \; -\sin H, \; 0)$ (rotated by roll $\phi$)
 *    - Up vector: $\mathbf{u} = \mathbf{r} \times \mathbf{f}$ (rotated by roll $\phi$)
 *
 * 3. **Perspective Projection Matrix**:
 *    Transforms a world point $\mathbf{P} \to \mathbf{v} = \mathbf{P} - \mathbf{E}_{\text{eye}}$,
 *    projects onto camera axes $(X_{\text{cam}}, Y_{\text{cam}}, Z_{\text{cam}})$, and calculates
 *    Normalized Device Coordinates (NDC) scaled by viewport aspect ratio and Field of View (FOV).
 *
 * @param camera The active [Camera] pose from the 3D map.
 * @param viewportWidth Width of the 3D map viewport in pixels.
 * @param viewportHeight Height of the 3D map viewport in pixels.
 * @param fovYDegrees Vertical field of view in degrees (defaults to standard 45.0° baseline).
 */
class Projection3D(
    val camera: Camera,
    val viewportWidth: Int,
    val viewportHeight: Int,
    val fovYDegrees: Double = DEFAULT_FOV_Y_DEGREES
) {
    private val centerLat: Double = camera.center.latitude
    private val centerLng: Double = camera.center.longitude
    private val centerAlt: Double = camera.center.altitude

    private val headingDeg: Double = camera.heading ?: 0.0
    private val tiltDeg: Double = camera.tilt ?: 0.0
    private val rollDeg: Double = camera.roll ?: 0.0
    private val rangeMeters: Double = camera.range ?: 1000.0

    private val tiltRad = Math.toRadians(tiltDeg)
    private val headingRad = Math.toRadians(headingDeg)
    private val rollRad = Math.toRadians(rollDeg)

    // Eye position in local ENU relative to focal center
    private val horizDist = rangeMeters * sin(tiltRad)
    private val vertDist = rangeMeters * cos(tiltRad)

    private val eyeEast = -horizDist * sin(headingRad)
    private val eyeNorth = -horizDist * cos(headingRad)
    private val eyeUp = vertDist

    // Basis vectors
    private val fEast = sin(tiltRad) * sin(headingRad)
    private val fNorth = sin(tiltRad) * cos(headingRad)
    private val fUp = -cos(tiltRad)

    private val rEast: Double
    private val rNorth: Double
    private val rUp: Double

    private val uEast: Double
    private val uNorth: Double
    private val uUp: Double

    init {
        val rEast0 = cos(headingRad)
        val rNorth0 = -sin(headingRad)
        val rUp0 = 0.0

        val uEast0 = sin(headingRad) * cos(tiltRad)
        val uNorth0 = cos(headingRad) * cos(tiltRad)
        val uUp0 = sin(tiltRad)

        if (abs(rollDeg) > 1e-4) {
            val cosRoll = cos(rollRad)
            val sinRoll = sin(rollRad)

            rEast = cosRoll * rEast0 + sinRoll * uEast0
            rNorth = cosRoll * rNorth0 + sinRoll * uNorth0
            rUp = cosRoll * rUp0 + sinRoll * uUp0

            uEast = -sinRoll * rEast0 + cosRoll * uEast0
            uNorth = -sinRoll * rNorth0 + cosRoll * uNorth0
            uUp = -sinRoll * rUp0 + cosRoll * uUp0
        } else {
            rEast = rEast0
            rNorth = rNorth0
            rUp = rUp0

            uEast = uEast0
            uNorth = uNorth0
            uUp = uUp0
        }
    }

    /**
     * Converts a 3D world coordinate ([LatLngAltitude]) into 2D viewport screen coordinates.
     *
     * @param point The 3D geographical coordinate to project.
     * @return A [ScreenCoordinate] containing pixel coordinates, frustum visibility, and depth.
     */
    fun toScreenCoordinate(point: LatLngAltitude): ScreenCoordinate {
        if (viewportWidth <= 0 || viewportHeight <= 0) {
            return ScreenCoordinate(x = 0f, y = 0f, isVisible = false, depth = 0.0)
        }

        val refLatLng = LatLng(centerLat, centerLng)
        val targetLatLng = LatLng(point.latitude, point.longitude)

        val dist = SphericalUtil.computeDistanceBetween(refLatLng, targetLatLng)
        val bearingRad = if (dist > 1e-4) {
            Math.toRadians(SphericalUtil.computeHeading(refLatLng, targetLatLng))
        } else {
            0.0
        }

        val pEast = dist * sin(bearingRad)
        val pNorth = dist * cos(bearingRad)
        val pUp = point.altitude - centerAlt

        // Vector from camera eye to world point
        val vEast = pEast - eyeEast
        val vNorth = pNorth - eyeNorth
        val vUp = pUp - eyeUp

        // Project onto camera axes
        val xCam = vEast * rEast + vNorth * rNorth + vUp * rUp
        val yCam = vEast * uEast + vNorth * uNorth + vUp * uUp
        val zCam = vEast * fEast + vNorth * fNorth + vUp * fUp

        if (zCam <= 1e-3) {
            // Point is behind camera
            return ScreenCoordinate(x = Float.NaN, y = Float.NaN, isVisible = false, depth = zCam)
        }

        val halfFovYRad = Math.toRadians(fovYDegrees / 2.0)
        val tanHalfFovY = tan(halfFovYRad)
        val aspectRatio = viewportWidth.toDouble() / viewportHeight.toDouble()
        val tanHalfFovX = tanHalfFovY * aspectRatio

        val ndcX = xCam / (zCam * tanHalfFovX)
        val ndcY = yCam / (zCam * tanHalfFovY)

        val screenX = ((ndcX + 1.0) * 0.5 * viewportWidth).toFloat()
        val screenY = ((1.0 - ndcY) * 0.5 * viewportHeight).toFloat()

        val isVisible = ndcX in -1.0..1.0 && ndcY in -1.0..1.0

        return ScreenCoordinate(
            x = screenX,
            y = screenY,
            isVisible = isVisible,
            depth = zCam
        )
    }

    /**
     * Projects a 3D world coordinate to integer screen pixels [Point].
     *
     * @param point The 3D coordinate to project.
     * @return An integer [Point] on screen, or null if the point is behind the camera.
     */
    fun toScreenLocation(point: LatLngAltitude): Point? {
        val coord = toScreenCoordinate(point)
        return if (coord.depth > 0.0) coord.toPoint() else null
    }

    /**
     * Projects a 3D world coordinate to high-precision screen pixels [PointF].
     *
     * @param point The 3D coordinate to project.
     * @return A [PointF] on screen, or null if the point is behind the camera.
     */
    fun toScreenLocationF(point: LatLngAltitude): PointF? {
        val coord = toScreenCoordinate(point)
        return if (coord.depth > 0.0) coord.toPointF() else null
    }

    /**
     * Unprojects a 2D screen coordinate $(X, Y)$ onto a horizontal ground plane at [targetAltitude].
     *
     * @param screenX Horizontal pixel position on screen.
     * @param screenY Vertical pixel position on screen.
     * @param targetAltitude Ground plane altitude in meters above sea level (defaults to 0.0).
     * @return The intersected [LatLngAltitude], or null if the cast ray does not intersect the plane.
     */
    fun fromScreenLocation(screenX: Float, screenY: Float, targetAltitude: Double = 0.0): LatLngAltitude? {
        if (viewportWidth <= 0 || viewportHeight <= 0) return null

        val ndcX = (screenX.toDouble() / viewportWidth.toDouble()) * 2.0 - 1.0
        val ndcY = 1.0 - (screenY.toDouble() / viewportHeight.toDouble()) * 2.0

        val halfFovYRad = Math.toRadians(fovYDegrees / 2.0)
        val tanHalfFovY = tan(halfFovYRad)
        val aspectRatio = viewportWidth.toDouble() / viewportHeight.toDouble()
        val tanHalfFovX = tanHalfFovY * aspectRatio

        val dirCamX = ndcX * tanHalfFovX
        val dirCamY = ndcY * tanHalfFovY
        val dirCamZ = 1.0

        // Transform camera ray direction into ENU space
        val dEast = dirCamX * rEast + dirCamY * uEast + dirCamZ * fEast
        val dNorth = dirCamX * rNorth + dirCamY * uNorth + dirCamZ * fNorth
        val dUp = dirCamX * rUp + dirCamY * uUp + dirCamZ * fUp

        val targetRelUp = targetAltitude - centerAlt
        val deltaUp = targetRelUp - eyeUp

        if (abs(dUp) < 1e-6) return null // Ray parallel to ground
        val t = deltaUp / dUp
        if (t <= 0.0) return null // Intersection behind camera

        val hitEast = eyeEast + t * dEast
        val hitNorth = eyeNorth + t * dNorth

        val hitDist = sqrt(hitEast * hitEast + hitNorth * hitNorth)
        val hitBearingDeg = (Math.toDegrees(atan2(hitEast, hitNorth)) + 360.0) % 360.0

        val refLatLng = LatLng(centerLat, centerLng)
        val hitLatLng = SphericalUtil.computeOffset(refLatLng, hitDist, hitBearingDeg)

        return latLngAltitude {
            latitude = hitLatLng.latitude
            longitude = hitLatLng.longitude
            altitude = targetAltitude
        }
    }

    companion object {
        /** Baseline vertical field of view angle in degrees. */
        const val DEFAULT_FOV_Y_DEGREES = 45.0
    }
}

/**
 * Creates a [Projection3D] utility configured with this [GoogleMap3D]'s active camera and dimensions.
 */
fun GoogleMap3D.getProjection3D(
    viewportWidth: Int,
    viewportHeight: Int,
    fovYDegrees: Double = Projection3D.DEFAULT_FOV_Y_DEGREES
): Projection3D? {
    val currentCamera = getCamera()?.toValidCamera() ?: return null
    return Projection3D(
        camera = currentCamera,
        viewportWidth = viewportWidth,
        viewportHeight = viewportHeight,
        fovYDegrees = fovYDegrees
    )
}
