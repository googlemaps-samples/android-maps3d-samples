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
    val cumulativeDistances: DoubleArray = doubleArrayOf(0.0),
    val totalDistance: Double = 0.0,
    val elapsedDistance: Double = 0.0,
    val progressRatio: Float = 0.0f,
    val isPlaying: Boolean = false,
    val isScrubbing: Boolean = false,
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
    val staticPolylineVertices: List<LatLngAltitude> = emptyList(),
    val progressPolylineVertices: List<LatLngAltitude> = emptyList()
) {
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
        get() = (currentHeading + headingOffset + 360.0) % 360.0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PathPlaybackState) return false

        return route == other.route &&
                cumulativeDistances.contentEquals(other.cumulativeDistances) &&
                totalDistance == other.totalDistance &&
                elapsedDistance == other.elapsedDistance &&
                progressRatio == other.progressRatio &&
                isPlaying == other.isPlaying &&
                isScrubbing == other.isScrubbing &&
                followSpeedMps == other.followSpeedMps &&
                cameraRange == other.cameraRange &&
                groundAltitude == other.groundAltitude &&
                headingOffset == other.headingOffset &&
                cameraTilt == other.cameraTilt &&
                altitudeMode == other.altitudeMode &&
                pathAltitudeOffset == other.pathAltitudeOffset &&
                drawsOccludedSegments == other.drawsOccludedSegments &&
                currentPosition == other.currentPosition &&
                currentAltitude == other.currentAltitude &&
                currentHeading == other.currentHeading &&
                staticPolylineVertices == other.staticPolylineVertices &&
                progressPolylineVertices == other.progressPolylineVertices
    }

    override fun hashCode(): Int {
        var result = route.hashCode()
        result = 31 * result + cumulativeDistances.contentHashCode()
        result = 31 * result + totalDistance.hashCode()
        result = 31 * result + elapsedDistance.hashCode()
        result = 31 * result + progressRatio.hashCode()
        result = 31 * result + isPlaying.hashCode()
        result = 31 * result + isScrubbing.hashCode()
        result = 31 * result + followSpeedMps.hashCode()
        result = 31 * result + cameraRange.hashCode()
        result = 31 * result + groundAltitude.hashCode()
        result = 31 * result + headingOffset.hashCode()
        result = 31 * result + cameraTilt.hashCode()
        result = 31 * result + altitudeMode.hashCode()
        result = 31 * result + pathAltitudeOffset.hashCode()
        result = 31 * result + drawsOccludedSegments.hashCode()
        result = 31 * result + currentPosition.hashCode()
        result = 31 * result + currentAltitude.hashCode()
        result = 31 * result + currentHeading.hashCode()
        result = 31 * result + staticPolylineVertices.hashCode()
        result = 31 * result + progressPolylineVertices.hashCode()
        return result
    }
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
    private var state: PathPlaybackState

    init {
        val cumDist = PathEngine.calculateCumulativeDistances(initialRoute)
        val totalDist = cumDist.lastOrNull() ?: 0.0
        val baseAlt = if (initialRoute == PathData.RURAL_PATH) 45.0 else 50.0
        val staticVertices = PathEngine.buildStaticVertices(
            path = initialRoute,
            altitudeMode = AltitudeMode.CLAMP_TO_GROUND,
            baseAltitude = baseAlt,
            pathAltitudeOffset = 0.5
        )
        val point = PathEngine.interpolatePoint(
            path = initialRoute,
            cumulativeDistances = cumDist,
            distance = 0.0
        )
        val progressVertices = PathEngine.buildProgressVertices(
            path = initialRoute,
            cumulativeDistances = cumDist,
            elapsedDistance = 0.0,
            currentLatLng = point.latLng,
            waypointIndex = point.waypointIndex,
            altitudeMode = AltitudeMode.CLAMP_TO_GROUND,
            baseAltitude = baseAlt,
            pathAltitudeOffset = 0.5
        )

        state = PathPlaybackState(
            route = initialRoute,
            cumulativeDistances = cumDist,
            totalDistance = totalDist,
            elapsedDistance = 0.0,
            progressRatio = 0f,
            isPlaying = false,
            isScrubbing = false,
            currentPosition = point.latLng,
            currentAltitude = point.altitude,
            currentHeading = point.bearing,
            staticPolylineVertices = staticVertices,
            progressPolylineVertices = progressVertices
        )
    }

    fun getState(): PathPlaybackState = state

    /**
     * Advances playback by a specific time delta (in seconds).
     */
    fun advance(deltaTimeSeconds: Double): PathPlaybackState {
        if (!state.isPlaying || state.totalDistance <= 0.0) return state

        val stepDist = state.followSpeedMps * deltaTimeSeconds
        var newDist = state.elapsedDistance + stepDist
        if (newDist >= state.totalDistance) {
            newDist %= state.totalDistance
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
        val cumDist = PathEngine.calculateCumulativeDistances(newRoute)
        val totalDist = cumDist.lastOrNull() ?: 0.0
        val isRural = newRoute == PathData.RURAL_PATH

        val range = if (applyDefaults) (if (isRural) 450.0 else 300.0) else state.cameraRange
        val groundAlt = if (applyDefaults) (if (isRural) 40.0 else 20.0) else state.groundAltitude
        val tilt = if (applyDefaults) (if (isRural) 75.0 else 70.0) else state.cameraTilt
        val baseAlt = if (isRural) 45.0 else 50.0

        val point = PathEngine.interpolatePoint(
            path = newRoute,
            cumulativeDistances = cumDist,
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
            cumulativeDistances = cumDist,
            elapsedDistance = 0.0,
            currentLatLng = point.latLng,
            waypointIndex = point.waypointIndex,
            altitudeMode = state.altitudeMode,
            baseAltitude = baseAlt,
            pathAltitudeOffset = state.pathAltitudeOffset
        )

        state = state.copy(
            route = newRoute,
            cumulativeDistances = cumDist,
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
        state = state.copy(cameraRange = range)
        return state
    }

    fun setGroundAltitude(altitude: Double): PathPlaybackState {
        state = state.copy(groundAltitude = altitude)
        return state
    }

    fun setHeadingOffset(offset: Double): PathPlaybackState {
        state = state.copy(headingOffset = offset)
        return state
    }

    fun setCameraTilt(tilt: Double): PathPlaybackState {
        state = state.copy(cameraTilt = tilt)
        return state
    }

    fun setFollowSpeed(speedMps: Double): PathPlaybackState {
        state = state.copy(followSpeedMps = speedMps)
        return state
    }

    fun reset(): PathPlaybackState {
        return seekToDistance(0.0).copy(isPlaying = false)
    }

    private fun updateDistanceAndRecompute(newDistance: Double, updateProgressRatio: Boolean): PathPlaybackState {
        val point = PathEngine.interpolatePoint(
            path = state.route,
            cumulativeDistances = state.cumulativeDistances,
            distance = newDistance
        )

        val smoothedHeading = PathEngine.smoothHeading(
            targetHeading = point.bearing,
            currentHeading = state.currentHeading,
            isUserScrubbing = state.isScrubbing,
            isPlaying = state.isPlaying
        )

        val progressVertices = PathEngine.buildProgressVertices(
            path = state.route,
            cumulativeDistances = state.cumulativeDistances,
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
            currentHeading = smoothedHeading,
            progressPolylineVertices = progressVertices
        )
        return state
    }

    private fun recomputeVerticesAndAltitude(): PathPlaybackState {
        val staticVertices = PathEngine.buildStaticVertices(
            path = state.route,
            altitudeMode = state.altitudeMode,
            baseAltitude = state.baseAltitude,
            pathAltitudeOffset = state.pathAltitudeOffset
        )

        val progressVertices = PathEngine.buildProgressVertices(
            path = state.route,
            cumulativeDistances = state.cumulativeDistances,
            elapsedDistance = state.elapsedDistance,
            currentLatLng = state.currentPosition,
            waypointIndex = 0,
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
