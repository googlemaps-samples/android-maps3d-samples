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
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.LatLngAltitude

/**
 * Immutable state representation of the path following engine and camera position.
 */
data class PathPlaybackState(
    val route: List<LatLngAltitude> = PathData.URBAN_PATH,
    val totalDistance: Double = 0.0,
    val elapsedDistance: Double = 0.0,
    val progressRatio: Float = 0.0f,
    val isPlaying: Boolean = false,
    val isScrubbing: Boolean = false,
    val speedBoostMultiplier: Double = 1.0,
    val followSpeedMps: Double = 30.0,
    val cameraRange: Double = 300.0,
    val groundAltitude: Double = 20.0,
    val headingOffset: Double = 0.0,
    val cameraTilt: Double = 70.0,
    val altitudeMode: Int = AltitudeMode.CLAMP_TO_GROUND,
    val pathAltitudeOffset: Double = 0.5,
    val drawsOccludedSegments: Boolean = true,
    val currentPosition: LatLng = LatLng(0.0, 0.0),
    val currentAltitude: Double = 0.0,
    val currentHeading: Double = 0.0,
    val cameraHeading: Double = 0.0,
    val staticPolylineVertices: List<LatLngAltitude> = emptyList(),
    val progressPolylineVertices: List<LatLngAltitude> = emptyList()
) {
    val isSpeedBoosted: Boolean get() = kotlin.math.abs(speedBoostMultiplier - 1.0) > 0.01

    val baseAltitude: Double
        get() = if (route == PathData.RURAL_PATH) 45.0 else 50.0

    val cameraTargetAltitude: Double
        get() = PathEngine.calculateCameraAltitude(
            altitudeMode = altitudeMode,
            baseAltitude = baseAltitude,
            interpolatedAltitude = currentAltitude,
            groundAltitude = groundAltitude
        )

    val effectiveHeading: Double
        get() = cameraHeading

    val effectiveSpeedMps: Double
        get() = followSpeedMps * speedBoostMultiplier
}

/**
 * Framework-independent controller for 3D path following playback and camera interpolation.
 *
 * Encapsulates all domain math, distance tracking, kinematic heading smoothing, and polyline
 * vertex generation. Has zero dependencies on Android Views, UI widgets, or GoogleMap3D rendering
 * classes, making it 100% unit-testable on the JVM.
 */
class PathPlaybackController(
    initialRoute: List<LatLngAltitude> = PathData.URBAN_PATH
) {
    private var cumulativeDistances: DoubleArray
    private var state: PathPlaybackState

    init {
        cumulativeDistances = PathEngine.calculateCumulativeDistances(initialRoute)
        val totalDist = cumulativeDistances.lastOrNull() ?: 0.0
        val baseAlt = if (initialRoute == PathData.RURAL_PATH) 45.0 else 50.0
        val staticVertices = PathEngine.buildStaticVertices(
            path = initialRoute,
            altitudeMode = AltitudeMode.CLAMP_TO_GROUND,
            baseAltitude = baseAlt,
            pathAltitudeOffset = 0.5
        )

        val point = PathEngine.interpolatePoint(
            path = initialRoute,
            cumulativeDistances = cumulativeDistances,
            distance = 0.0
        )

        val progressVertices = PathEngine.buildProgressVertices(
            path = initialRoute,
            cumulativeDistances = cumulativeDistances,
            elapsedDistance = 0.0,
            currentLatLng = point.latLng,
            waypointIndex = point.waypointIndex,
            altitudeMode = AltitudeMode.CLAMP_TO_GROUND,
            baseAltitude = baseAlt,
            pathAltitudeOffset = 0.5
        )

        state = PathPlaybackState(
            route = initialRoute,
            totalDistance = totalDist,
            elapsedDistance = 0.0,
            progressRatio = 0f,
            isPlaying = false,
            isScrubbing = false,
            speedBoostMultiplier = 1.0,
            currentPosition = point.latLng,
            currentAltitude = point.altitude,
            currentHeading = point.bearing,
            cameraHeading = point.bearing,
            staticPolylineVertices = staticVertices,
            progressPolylineVertices = progressVertices
        )
    }

    fun getState(): PathPlaybackState = state

    /**
     * Advances playback by a specific time delta (in seconds).
     * Takes speed boost (long-press 2x multiplier) into account.
     */
    fun advance(deltaTimeSeconds: Double): PathPlaybackState {
        if (!state.isPlaying || state.totalDistance <= 0.0) return state

        val stepDist = state.effectiveSpeedMps * deltaTimeSeconds
        var newDist = state.elapsedDistance + stepDist
        if (newDist >= state.totalDistance) {
            newDist %= state.totalDistance
        } else if (newDist < 0.0) {
            newDist = (newDist % state.totalDistance + state.totalDistance) % state.totalDistance
        }

        return updateDistanceAndRecompute(newDistance = newDist, updateProgressRatio = !state.isScrubbing)
    }

    /**
     * Seeks to a specific normalized progress ratio [0.0, 1.0].
     */
    fun seekToRatio(ratio: Float): PathPlaybackState {
        val clampedRatio = ratio.coerceIn(0f, 1f)
        val targetDist = state.totalDistance * clampedRatio.toDouble()
        state = state.copy(progressRatio = clampedRatio)
        return updateDistanceAndRecompute(newDistance = targetDist, updateProgressRatio = false)
    }

    /**
     * Seeks to a specific distance along the route in meters.
     */
    fun skipDistance(deltaMeters: Double): PathPlaybackState {
        if (state.totalDistance <= 0.0) return state
        var newDist = state.elapsedDistance + deltaMeters
        newDist = (newDist % state.totalDistance + state.totalDistance) % state.totalDistance
        return updateDistanceAndRecompute(newDistance = newDist, updateProgressRatio = true)
    }

    fun skipRatio(deltaRatio: Float): PathPlaybackState {
        return skipDistance(state.totalDistance * deltaRatio.toDouble())
    }

    fun seekToDistance(distanceMeters: Double): PathPlaybackState {
        val targetDist = distanceMeters.coerceIn(0.0, state.totalDistance)
        val ratio = if (state.totalDistance > 0.0) (targetDist / state.totalDistance).toFloat().coerceIn(0f, 1f) else 0f
        state = state.copy(progressRatio = ratio)
        return updateDistanceAndRecompute(newDistance = targetDist, updateProgressRatio = false)
    }

    fun setScrubbing(isScrubbing: Boolean): PathPlaybackState {
        state = state.copy(isScrubbing = isScrubbing)
        return state
    }

    fun setPlaying(isPlaying: Boolean): PathPlaybackState {
        state = state.copy(isPlaying = isPlaying)
        return state
    }

    fun togglePlayPause(): PathPlaybackState {
        state = state.copy(isPlaying = !state.isPlaying)
        return state
    }

    fun setRoute(newRoute: List<LatLngAltitude>, applyDefaults: Boolean = true): PathPlaybackState {
        cumulativeDistances = PathEngine.calculateCumulativeDistances(newRoute)
        val totalDist = cumulativeDistances.lastOrNull() ?: 0.0
        val isRural = newRoute == PathData.RURAL_PATH

        val range = if (applyDefaults) (if (isRural) 450.0 else 300.0) else state.cameraRange
        val groundAlt = if (applyDefaults) (if (isRural) 40.0 else 20.0) else state.groundAltitude
        val tilt = if (applyDefaults) (if (isRural) 75.0 else 70.0) else state.cameraTilt
        val baseAlt = if (isRural) 45.0 else 50.0

        val point = PathEngine.interpolatePoint(
            path = newRoute,
            cumulativeDistances = cumulativeDistances,
            distance = 0.0
        )

        val staticVertices = PathEngine.buildStaticVertices(
            path = newRoute,
            altitudeMode = state.altitudeMode,
            baseAltitude = baseAlt,
            pathAltitudeOffset = state.pathAltitudeOffset
        )

        val progressVertices = PathEngine.buildProgressVertices(
            path = newRoute,
            cumulativeDistances = cumulativeDistances,
            elapsedDistance = 0.0,
            currentLatLng = point.latLng,
            waypointIndex = point.waypointIndex,
            altitudeMode = state.altitudeMode,
            baseAltitude = baseAlt,
            pathAltitudeOffset = state.pathAltitudeOffset
        )

        state = state.copy(
            route = newRoute,
            totalDistance = totalDist,
            elapsedDistance = 0.0,
            progressRatio = 0f,
            isPlaying = false,
            cameraRange = range,
            groundAltitude = groundAlt,
            cameraTilt = tilt,
            currentPosition = point.latLng,
            currentAltitude = point.altitude,
            currentHeading = point.bearing,
            cameraHeading = (point.bearing + state.headingOffset + 360.0) % 360.0,
            staticPolylineVertices = staticVertices,
            progressPolylineVertices = progressVertices
        )
        return state
    }

    fun setAltitudeMode(mode: Int): PathPlaybackState {
        state = state.copy(altitudeMode = mode)
        return recomputeVerticesAndAltitude()
    }

    fun setDrawsOccludedSegments(drawsOccluded: Boolean): PathPlaybackState {
        state = state.copy(drawsOccludedSegments = drawsOccluded)
        return state
    }

    fun setPathAltitudeOffset(offset: Double): PathPlaybackState {
        state = state.copy(pathAltitudeOffset = offset)
        return recomputeVerticesAndAltitude()
    }

    fun setCameraRange(range: Double): PathPlaybackState {
        state = state.copy(cameraRange = range.coerceIn(20.0, 5000.0))
        return state
    }

    fun setGroundAltitude(altitude: Double): PathPlaybackState {
        state = state.copy(groundAltitude = altitude.coerceIn(0.0, 500.0))
        return state
    }

    fun setHeadingOffset(offset: Double): PathPlaybackState {
        var normalizedOffset = offset % 360.0
        if (normalizedOffset > 180.0) normalizedOffset -= 360.0
        if (normalizedOffset < -180.0) normalizedOffset += 360.0

        val targetCamHeading = (state.currentHeading + normalizedOffset + 360.0) % 360.0
        state = state.copy(
            headingOffset = normalizedOffset,
            cameraHeading = targetCamHeading
        )
        return state
    }

    fun setCameraTilt(tilt: Double): PathPlaybackState {
        state = state.copy(cameraTilt = tilt.coerceIn(0.0, 85.0))
        return state
    }

    /**
     * Adjusts the camera tilt by delta degrees (e.g. from vertical gesture sweep).
     * Constrained between 0° (top-down) and 85° (horizon).
     */
    fun adjustTilt(deltaDeg: Double): PathPlaybackState {
        return setCameraTilt(state.cameraTilt + deltaDeg)
    }

    /**
     * Adjusts the heading offset by delta degrees (e.g. from horizontal gesture sweep).
     */
    fun adjustHeading(deltaDeg: Double): PathPlaybackState {
        return setHeadingOffset(state.headingOffset + deltaDeg)
    }

    /**
     * Adjusts camera range via pinch scaling.
     * scaleFactor > 1.0 zooms in (decreases range), scaleFactor < 1.0 zooms out (increases range).
     */
    fun adjustRange(scaleFactor: Double): PathPlaybackState {
        if (scaleFactor <= 0.0) return state
        return setCameraRange(state.cameraRange / scaleFactor)
    }

    /**
     * Sets long-press speed boost (2x multiplier).
     */
    fun setSpeedBoostMultiplier(multiplier: Double): PathPlaybackState {
        state = state.copy(speedBoostMultiplier = multiplier)
        return state
    }

    fun setSpeedBoosted(isBoosted: Boolean): PathPlaybackState {
        return setSpeedBoostMultiplier(if (isBoosted) 2.0 else 1.0)
    }

    fun setFollowSpeed(speedMps: Double): PathPlaybackState {
        state = state.copy(followSpeedMps = speedMps)
        return state
    }

    fun reset(): PathPlaybackState {
        return seekToDistance(0.0).copy(isPlaying = false, speedBoostMultiplier = 1.0)
    }

    private fun updateDistanceAndRecompute(newDistance: Double, updateProgressRatio: Boolean): PathPlaybackState {
        val point = PathEngine.interpolatePoint(
            path = state.route,
            cumulativeDistances = cumulativeDistances,
            distance = newDistance
        )

        val targetCameraHeading = (point.bearing + state.headingOffset + 360.0) % 360.0
        val smoothedCameraHeading = PathEngine.smoothHeading(
            targetHeading = targetCameraHeading,
            currentHeading = state.cameraHeading,
            isUserScrubbing = state.isScrubbing,
            isPlaying = state.isPlaying
        )

        val progressVertices = PathEngine.buildProgressVertices(
            path = state.route,
            cumulativeDistances = cumulativeDistances,
            elapsedDistance = newDistance,
            currentLatLng = point.latLng,
            waypointIndex = point.waypointIndex,
            altitudeMode = state.altitudeMode,
            baseAltitude = state.baseAltitude,
            pathAltitudeOffset = state.pathAltitudeOffset
        )

        val ratio = if (updateProgressRatio && state.totalDistance > 0.0) {
            (newDistance / state.totalDistance).toFloat().coerceIn(0f, 1f)
        } else {
            state.progressRatio
        }

        state = state.copy(
            elapsedDistance = newDistance,
            progressRatio = ratio,
            currentPosition = point.latLng,
            currentAltitude = point.altitude,
            currentHeading = point.bearing,
            cameraHeading = smoothedCameraHeading,
            progressPolylineVertices = progressVertices
        )
        return state
    }

    private fun recomputeVerticesAndAltitude(): PathPlaybackState {
        val point = PathEngine.interpolatePoint(
            path = state.route,
            cumulativeDistances = cumulativeDistances,
            distance = state.elapsedDistance
        )

        val staticVertices = PathEngine.buildStaticVertices(
            path = state.route,
            altitudeMode = state.altitudeMode,
            baseAltitude = state.baseAltitude,
            pathAltitudeOffset = state.pathAltitudeOffset
        )

        val progressVertices = PathEngine.buildProgressVertices(
            path = state.route,
            cumulativeDistances = cumulativeDistances,
            elapsedDistance = state.elapsedDistance,
            currentLatLng = point.latLng,
            waypointIndex = point.waypointIndex,
            altitudeMode = state.altitudeMode,
            baseAltitude = state.baseAltitude,
            pathAltitudeOffset = state.pathAltitudeOffset
        )

        state = state.copy(
            staticPolylineVertices = staticVertices,
            progressPolylineVertices = progressVertices
        )
        return state
    }
}
