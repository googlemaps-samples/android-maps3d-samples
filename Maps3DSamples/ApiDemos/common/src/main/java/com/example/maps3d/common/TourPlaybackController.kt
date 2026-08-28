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

/**
 * Immutable state representing the advanced camera animation and airplane position.
 */
data class TourPlaybackState(
    val selectedApproach: AnimationApproach = AnimationApproach.DISPATCHER_FRAME_LOOP,
    val isPlaying: Boolean = false,
    val isFinished: Boolean = false,
    val currentStepIndex: Int = 0,
    val totalSteps: Int = TourData.SAN_FRANCISCO_TOUR.size,
    val statusText: String = "Press Play to start the aerial tour.",
    val currentStepTitle: String = "",
    val currentStepDescription: String = "",
    val airplanePosition: LatLng = TourData.AIRPLANE_FLIGHT_PATH.first(),
    val airplaneAltitude: Double = 250.0,
    val airplaneHeading: Double = 286.2, // Face eastward toward Coit Tower
    val cameraCenter: LatLng = TourData.AIRPLANE_FLIGHT_PATH.first(),
    val cameraAltitude: Double = 250.0,
    val cameraHeading: Double = 106.2,
    val cameraTilt: Double = 65.0,
    val cameraRange: Double = 600.0,
    val cameraRoll: Double = 0.0,
    val elapsedDistance: Double = 0.0,
    val totalDistance: Double = 0.0,
    val progressRatio: Float = 0.0f
) {
    val currentCamera: Camera
        get() = camera {
            center = latLngAltitude {
                latitude = cameraCenter.latitude
                longitude = cameraCenter.longitude
                altitude = cameraAltitude
            }
            heading = cameraHeading
            tilt = cameraTilt
            range = cameraRange
            roll = cameraRoll
        }

    val currentAirplanePositionWithAltitude: LatLngAltitude
        get() = LatLngAltitude(airplanePosition.latitude, airplanePosition.longitude, airplaneAltitude)
}

/**
 * Framework-independent domain state machine and kinematics controller for 3D Camera Tours.
 *
 * Encapsulates multi-step keyframe tours, high-rate VSYNC frame dispatching, and 360° orbital
 * camera calculations with zero Android View/UI dependencies.
 */
class TourPlaybackController(
    val flightPath: List<LatLng> = TourData.AIRPLANE_FLIGHT_PATH,
    val keyframes: List<CameraKeyframe> = TourData.SAN_FRANCISCO_TOUR
) {
    private val cumulativeDistances: DoubleArray = calculateCumulativeDistances(flightPath)
    val totalFlightDistance: Double = cumulativeDistances.lastOrNull() ?: 0.0
    private var state: TourPlaybackState

    init {
        val startLoc = flightPath.firstOrNull() ?: LatLng(0.0, 0.0)
        val initialBearing = if (flightPath.size >= 2) {
            SphericalUtil.computeHeading(flightPath[0], flightPath[1])
        } else {
            105.0
        }

        state = TourPlaybackState(
            selectedApproach = AnimationApproach.DISPATCHER_FRAME_LOOP,
            isPlaying = false,
            isFinished = false,
            currentStepIndex = 0,
            totalSteps = keyframes.size,
            statusText = "Press Play to start the aerial tour.",
            airplanePosition = startLoc,
            airplaneAltitude = 200.0,
            airplaneHeading = normalizeHeading(initialBearing + 180.0),
            cameraCenter = startLoc,
            cameraAltitude = 200.0,
            cameraHeading = normalizeHeading(initialBearing),
            cameraTilt = 65.0,
            cameraRange = 600.0,
            elapsedDistance = 0.0,
            totalDistance = totalFlightDistance,
            progressRatio = 0f
        )
    }

    fun getState(): TourPlaybackState = state

    fun setApproach(approach: AnimationApproach): TourPlaybackState {
        state = state.copy(
            selectedApproach = approach,
            isPlaying = false,
            isFinished = false,
            currentStepIndex = 0,
            elapsedDistance = 0.0,
            progressRatio = 0f,
            statusText = when (approach) {
                AnimationApproach.SIMPLE_FLY_TO -> "Native SDK flyTo transition across landmarks."
                AnimationApproach.KEYFRAME_TOUR -> "Declarative keyframe sequence (FlyTo → Dwell → Orbit → FlyTo)."
                AnimationApproach.DISPATCHER_FRAME_LOOP -> "High-rate 400 m/s flight frame dispatcher."
                AnimationApproach.ORBIT_360_SPIN -> "Continuous 360° orbital camera spin."
            }
        )
        return reset()
    }

    fun setPlaying(isPlaying: Boolean): TourPlaybackState {
        state = state.copy(isPlaying = isPlaying)
        return state
    }

    fun togglePlayPause(): TourPlaybackState {
        state = state.copy(isPlaying = !state.isPlaying)
        return state
    }

    fun setKeyframeStep(index: Int): TourPlaybackState {
        if (index !in keyframes.indices) return state
        val step = keyframes[index]
        state = state.copy(
            currentStepIndex = index,
            currentStepTitle = step.stepTitle,
            currentStepDescription = step.stepDescription,
            statusText = "Step ${index + 1} of ${keyframes.size}: ${step.stepTitle}"
        )
        return state
    }



    fun advanceFrameDispatcher(deltaTimeSeconds: Double, speedMps: Double = 400.0): TourPlaybackState {
        if (!state.isPlaying || totalFlightDistance <= 0.0) return state

        val stepDist = speedMps * deltaTimeSeconds
        val newDist = (state.elapsedDistance + stepDist).coerceAtMost(totalFlightDistance)
        val ratio = (newDist / totalFlightDistance).toFloat().coerceIn(0f, 1f)

        val point = interpolateFlightPoint(flightPath, cumulativeDistances, newDist)
        val isAtEnd = newDist >= totalFlightDistance

        state = state.copy(
            elapsedDistance = newDist,
            progressRatio = ratio,
            airplanePosition = point.position,
            airplaneAltitude = 200.0,
            airplaneHeading = normalizeHeading(point.bearing + 180.0),
            cameraCenter = point.position,
            cameraAltitude = 200.0,
            cameraHeading = normalizeHeading(point.bearing),
            isFinished = isAtEnd,
            isPlaying = if (isAtEnd) false else state.isPlaying,
            statusText = if (isAtEnd) "Flight complete: Arrived at Coit Tower." else "Flying at 400 m/s: ${(ratio * 100).toInt()}% complete"
        )
        return state
    }

    fun advanceContinuousOrbit(deltaTimeSeconds: Double, speedDegPerSec: Double = 25.0): TourPlaybackState {
        if (!state.isPlaying) return state

        val newHeading = (state.cameraHeading + speedDegPerSec * deltaTimeSeconds) % 360.0
        val targetCenter = flightPath.firstOrNull() ?: TourData.GOLDEN_GATE_BRIDGE

        state = state.copy(
            cameraCenter = targetCenter,
            cameraAltitude = 200.0,
            cameraHeading = normalizeHeading(newHeading),
            cameraTilt = 65.0,
            cameraRange = 600.0,
            statusText = "360° Orbit Spin: ${newHeading.toInt()}°"
        )
        return state
    }

    fun reset(): TourPlaybackState {
        val startLoc = flightPath.firstOrNull() ?: LatLng(0.0, 0.0)
        val initialBearing = if (flightPath.size >= 2) {
            SphericalUtil.computeHeading(flightPath[0], flightPath[1])
        } else {
            105.0
        }

        state = state.copy(
            isPlaying = false,
            isFinished = false,
            currentStepIndex = 0,
            elapsedDistance = 0.0,
            progressRatio = 0f,
            airplanePosition = startLoc,
            airplaneAltitude = 200.0,
            airplaneHeading = normalizeHeading(initialBearing + 180.0),
            cameraCenter = startLoc,
            cameraAltitude = 200.0,
            cameraHeading = normalizeHeading(initialBearing),
            cameraTilt = 65.0,
            cameraRange = 600.0,
            statusText = "Tour reset. Press Play to begin."
        )
        return state
    }

    companion object {
        fun normalizeHeading(headingDeg: Double): Double {
            val normalized = headingDeg % 360.0
            return if (normalized < 0.0) normalized + 360.0 else normalized
        }

        fun interpolateAngle(start: Double, end: Double, fraction: Double): Double {
            var diff = (end - start) % 360.0
            if (diff > 180.0) diff -= 360.0
            if (diff < -180.0) diff += 360.0
            return (start + diff * fraction + 360.0) % 360.0
        }

        fun calculateCumulativeDistances(path: List<LatLng>): DoubleArray {
            if (path.isEmpty()) return doubleArrayOf(0.0)
            val distances = DoubleArray(path.size)
            distances[0] = 0.0
            for (i in 1 until path.size) {
                distances[i] = distances[i - 1] + SphericalUtil.computeDistanceBetween(path[i - 1], path[i])
            }
            return distances
        }

        data class InterpolatedFlightPoint(
            val position: LatLng,
            val bearing: Double,
            val waypointIndex: Int
        )

        fun interpolateFlightPoint(
            path: List<LatLng>,
            cumulativeDistances: DoubleArray,
            distance: Double
        ): InterpolatedFlightPoint {
            if (path.isEmpty()) return InterpolatedFlightPoint(LatLng(0.0, 0.0), 0.0, 0)
            val totalDistance = cumulativeDistances.lastOrNull() ?: 0.0
            var index = 0
            while (index < cumulativeDistances.size - 1 && cumulativeDistances[index + 1] < distance) {
                index++
            }

            val p1 = path[index]
            val p2 = if (index < path.size - 1) path[index + 1] else p1
            val d1 = cumulativeDistances.getOrElse(index) { 0.0 }
            val d2 = cumulativeDistances.getOrElse(index + 1) { totalDistance }
            val segLen = d2 - d1
            val fraction = if (segLen > 0) ((distance - d1) / segLen).coerceIn(0.0, 1.0) else 0.0

            val currentLatLng = SphericalUtil.interpolate(p1, p2, fraction)
            val bearing = if (p1 != p2) SphericalUtil.computeHeading(p1, p2) else 105.0

            return InterpolatedFlightPoint(
                position = currentLatLng,
                bearing = normalizeHeading(bearing),
                waypointIndex = index
            )
        }
    }
}
