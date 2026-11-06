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

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.LatLngAltitude
import kotlin.math.abs
import kotlin.math.max
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Immutable UI state for the Hilly & Undulating (Roller-Coaster) Road Early Alerts sample.
 *
 * Shared across Kotlin Views, Java Views, and Jetpack Compose.
 *
 * @property selectedPreset Currently selected 3D route preset.
 * @property routePoints Raw 3D route waypoints.
 * @property staticRouteVertices Base route polyline vertices with +0.4m elevation bias.
 * @property traveledRouteVertices Driven route polyline vertices behind the vehicle.
 * @property isMapReady Whether the 3D map has finished initializing and is ready to start the route.
 * @property isPlaying Whether the vehicle simulation is actively advancing.
 * @property isUserScrubbing Whether the user is dragging the progress slider.
 * @property progressRatio Normalized route progress in `[0.0f, 1.0f]`.
 * @property currentDistanceMeters Traveled distance along the route in meters.
 * @property totalDistanceMeters Total horizontal length of the active route in meters.
 * @property speedMultiplier Playback speed multiplier (`0.5`, `1.0`, `2.0`, `3.0`, `5.0`).
 * @property lookaheadHorizonMeters Forward terrain scan window in meters (`250.0`, `500.0`, `750.0`).
 * @property autoCameraPitchPreview Whether the chase camera automatically widens range, lowers tilt,
 *   and adjusts camera height when approaching sharp elevation changes or blind crests.
 * @property vehiclePosition Current 3D coordinate of the simulated vehicle.
 * @property currentAltitudeMeters Current road elevation in meters at the vehicle position.
 * @property currentHeadingDegrees Smoothed forward heading of the vehicle in degrees `[0, 360)`.
 * @property cameraCenter Target 3D center point for the chase camera.
 * @property cameraHeading Chase camera heading in degrees.
 * @property cameraTilt Chase camera tilt angle in degrees (`74.0°` normal, `66.0°` crest preview).
 * @property cameraRange Chase camera range in meters (`160.0m` normal, `230.0m` crest preview).
 * @property terrainAlert Forward terrain analysis and early hazard alert state.
 */
data class HillyRoadUiState(
    @JvmField val selectedPreset: HillyRoutePreset,
    @JvmField val routePoints: List<LatLngAltitude>,
    @JvmField val staticRouteVertices: List<LatLngAltitude>,
    @JvmField val traveledRouteVertices: List<LatLngAltitude>,
    @JvmField val isMapReady: Boolean,
    @JvmField val isPlaying: Boolean,
    @JvmField val isUserScrubbing: Boolean,
    @JvmField val progressRatio: Float,
    @JvmField val currentDistanceMeters: Double,
    @JvmField val totalDistanceMeters: Double,
    @JvmField val speedMultiplier: Double,
    @JvmField val lookaheadHorizonMeters: Double,
    @JvmField val autoCameraPitchPreview: Boolean,
    @JvmField val vehiclePosition: LatLngAltitude,
    @JvmField val currentAltitudeMeters: Double,
    @JvmField val currentHeadingDegrees: Double,
    @JvmField val cameraCenter: LatLngAltitude,
    @JvmField val cameraHeading: Double,
    @JvmField val cameraTilt: Double,
    @JvmField val cameraRange: Double,
    @JvmField val terrainAlert: TerrainAlertState
)

/**
 * Shared MVVM ViewModel driving the Hilly & Undulating Road Early Alerts sample across
 * Kotlin Views, Java Views, and Jetpack Compose.
 *
 * Reuses [PathEngine] for spherical distance accumulation, 3D interpolation, and EMA heading
 * smoothing, and delegates forward terrain hazard detection to [RoadTerrainAnalyzer].
 */
class HillyRoadAlertViewModel : ViewModel() {

    companion object {
        const val BASE_ROUTE_POLYLINE_ID = "hilly_road_base_polyline"
        const val TRAVELED_ROUTE_POLYLINE_ID = "hilly_road_traveled_polyline"
        const val HAZARD_ROUTE_POLYLINE_ID = "hilly_road_hazard_polyline"
        const val VEHICLE_MARKER_ID = "hilly_road_vehicle_marker"
        const val VEHICLE_MODEL_ID = "hilly_road_vehicle_car"
        const val VEHICLE_MODEL_URL = "https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/red_car.glb"
        const val VEHICLE_MODEL_SCALE = 5.0
        const val VEHICLE_MODEL_TILT = -90.0

        const val DEFAULT_LOOKAHEAD_METERS = 500.0
        const val NORMAL_CAMERA_TILT = 74.0
        const val PREVIEW_CAMERA_TILT = 66.0
        const val NORMAL_CAMERA_RANGE = 160.0
        const val PREVIEW_CAMERA_RANGE = 230.0
        private const val ROUTE_ALTITUDE_OFFSET_METERS = 0.4
        private const val VEHICLE_ALTITUDE_OFFSET_METERS = 0.4
        private const val CAMERA_ALTITUDE_LOOKAHEAD_METERS = 35.0
        private const val CAMERA_LOOKAHEAD_ALTITUDE_WEIGHT = 0.30
        private const val CAMERA_ALTITUDE_EMA_ALPHA = 0.18
        private const val MAX_SHARP_ELEVATION_HEIGHT_BOOST_METERS = 10.0

        @JvmField
        val LOOKAHEAD_PRESETS_METERS: List<Double> = listOf(250.0, 500.0, 750.0)

        @JvmField
        val SPEED_MULTIPLIERS: List<Double> = listOf(0.5, 1.0, 2.0, 3.0, 5.0)

        /**
         * Safely checks whether the device currently has an active internet-capable network.
         * When offline, the Maps 3D SDK displays its built-in "Check your internet connection"
         * dialog and the route simulation waits until the map finishes loading.
         */
        @JvmStatic
        fun isInternetAvailable(context: Context): Boolean {
            return try {
                val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                    ?: return true
                val activeNetwork = cm.activeNetwork ?: return false
                val caps = cm.getNetworkCapabilities(activeNetwork) ?: return false
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            } catch (_: Exception) {
                true
            }
        }
    }

    private var activePreset: HillyRoutePreset = HillyRoutePreset.ROLLER_COASTER
    private var activeRoute: List<LatLngAltitude> = HillyRouteData.getRoute(activePreset)
    private var cumulativeDistances: DoubleArray = PathEngine.calculateCumulativeDistances(activeRoute)
    private var routeProfile: RouteProfile = PathEngine.profileRoute(activeRoute)
    private var staticVertices: List<LatLngAltitude> = PathEngine.buildStaticVertices(
        path = activeRoute,
        altitudeMode = AltitudeMode.ABSOLUTE,
        pathAltitudeOffset = ROUTE_ALTITUDE_OFFSET_METERS
    )

    private var currentDistanceMeters: Double = 0.0
    private var isMapReady: Boolean = false
    private var isPlaying: Boolean = false
    private var isUserScrubbing: Boolean = false
    private var speedMultiplier: Double = 1.0
    private var lookaheadHorizonMeters: Double = DEFAULT_LOOKAHEAD_METERS
    private var autoCameraPitchPreview: Boolean = true

    private var smoothedHeadingDegrees: Double? = null
    private var smoothedCameraAltitude: Double? = null
    private var currentCameraTilt: Double = NORMAL_CAMERA_TILT
    private var currentCameraRange: Double = NORMAL_CAMERA_RANGE

    private val _uiState = MutableStateFlow(buildUiState(snapCamera = true))

    /** Reactive [StateFlow] for Kotlin Views and Jetpack Compose. */
    val uiState: StateFlow<HillyRoadUiState> = _uiState.asStateFlow()

    /** Lifecycle-aware [LiveData] bridged from [uiState] for Java Views. */
    val uiStateLiveData: LiveData<HillyRoadUiState> = _uiState.asLiveData()

    /** Synchronous snapshot of the current [HillyRoadUiState]. */
    val currentState: HillyRoadUiState
        get() = _uiState.value

    /**
     * Notifies the ViewModel that the 3D map has finished initializing and is ready.
     * Starts route elevation analysis and simulation playback from the start of the route (`0.0m`).
     */
    fun onMapReady() {
        if (isMapReady) return
        isMapReady = true
        currentDistanceMeters = 0.0
        smoothedHeadingDegrees = null
        smoothedCameraAltitude = null
        isPlaying = true
        emitState(snapCamera = true)
    }

    /** Toggles vehicle playback between playing and paused. */
    fun togglePlayPause() {
        setPlaying(!isPlaying)
    }

    /** Explicitly sets the playback running state. */
    fun setPlaying(playing: Boolean) {
        if (!isMapReady && playing) {
            return
        }
        if (playing && currentDistanceMeters >= totalDistance() - 0.5) {
            currentDistanceMeters = 0.0
            smoothedHeadingDegrees = null
            smoothedCameraAltitude = null
        }
        isPlaying = playing
        emitState(snapCamera = false)
    }

    /** Updates whether the user is actively scrubbing the progress slider. */
    fun setScrubbing(scrubbing: Boolean) {
        isUserScrubbing = scrubbing
        emitState(snapCamera = scrubbing)
    }

    /** Seeks vehicle position to a normalized progress ratio in `[0.0f, 1.0f]`. */
    fun seekToProgress(ratio: Float) {
        val clampedRatio = ratio.coerceIn(0.0f, 1.0f)
        currentDistanceMeters = totalDistance() * clampedRatio.toDouble()
        smoothedHeadingDegrees = null
        smoothedCameraAltitude = null
        emitState(snapCamera = true)
    }

    /** Seeks vehicle position to an explicit distance in meters along the active route. */
    fun seekToDistance(distanceMeters: Double) {
        currentDistanceMeters = distanceMeters.coerceIn(0.0, totalDistance())
        smoothedHeadingDegrees = null
        smoothedCameraAltitude = null
        emitState(snapCamera = true)
    }

    /**
     * Seeks ~240m before the first undulating crest so the user immediately observes
     * the early warning alert trigger before the vehicle enters the hilly section.
     */
    fun jumpToHillyZone() {
        val targetDist = RoadTerrainAnalyzer.findFirstHillyAlertTriggerDistance(
            path = activeRoute,
            preCrestOffsetMeters = 240.0
        )
        currentDistanceMeters = targetDist.coerceIn(0.0, totalDistance())
        smoothedHeadingDegrees = null
        smoothedCameraAltitude = null
        emitState(snapCamera = true)
    }

    /** Sets the playback speed multiplier (`0.5x`, `1x`, `2x`, `3x`, `5x`). */
    fun setSpeedMultiplier(multiplier: Double) {
        speedMultiplier = multiplier.coerceIn(0.25, 10.0)
        emitState(snapCamera = false)
    }

    /** Switches the active 3D route preset and resets vehicle position to the start of the route. */
    fun selectRoute(preset: HillyRoutePreset) {
        activePreset = preset
        activeRoute = HillyRouteData.getRoute(preset)
        cumulativeDistances = PathEngine.calculateCumulativeDistances(activeRoute)
        routeProfile = PathEngine.profileRoute(activeRoute)
        staticVertices = PathEngine.buildStaticVertices(
            path = activeRoute,
            altitudeMode = AltitudeMode.ABSOLUTE,
            pathAltitudeOffset = ROUTE_ALTITUDE_OFFSET_METERS
        )
        currentDistanceMeters = 0.0
        smoothedHeadingDegrees = null
        smoothedCameraAltitude = null
        emitState(snapCamera = true)
    }

    /** Updates the forward terrain lookahead horizon (`250.0`, `500.0`, `750.0` meters). */
    fun setLookaheadHorizon(meters: Double) {
        lookaheadHorizonMeters = meters.coerceIn(100.0, 1500.0)
        emitState(snapCamera = true)
    }

    /** Toggles automatic camera pitch/range adjustment when approaching blind crests or undulations. */
    fun toggleAutoCameraPitchPreview() {
        setAutoCameraPitchPreview(!autoCameraPitchPreview)
    }

    /** Explicitly enables or disables automatic crest camera pitch preview. */
    fun setAutoCameraPitchPreview(enabled: Boolean) {
        autoCameraPitchPreview = enabled
        emitState(snapCamera = true)
    }

    /** Resets vehicle position to the beginning of the current route. */
    fun reset() {
        currentDistanceMeters = 0.0
        smoothedHeadingDegrees = null
        smoothedCameraAltitude = null
        emitState(snapCamera = true)
    }

    /**
     * Advances simulation playback by [deltaTimeSeconds] (invoked from Choreographer / Compose frame loop).
     * Does not advance until [onMapReady] has been called.
     */
    fun advance(deltaTimeSeconds: Double) {
        if (!isMapReady || !isPlaying || isUserScrubbing || deltaTimeSeconds <= 0.0) return

        val clampedDt = deltaTimeSeconds.coerceAtMost(0.1)
        val totalDist = totalDistance()
        if (totalDist <= 0.0) return

        val stepMeters = activePreset.defaultBaseSpeedMps * speedMultiplier * clampedDt
        val nextDist = currentDistanceMeters + stepMeters

        if (nextDist >= totalDist) {
            currentDistanceMeters = 0.0
            smoothedHeadingDegrees = null
            smoothedCameraAltitude = null
            emitState(snapCamera = true)
        } else {
            currentDistanceMeters = nextDist
            emitState(snapCamera = false)
        }
    }

    private fun totalDistance(): Double = cumulativeDistances.lastOrNull() ?: 0.0

    private fun emitState(snapCamera: Boolean) {
        _uiState.value = buildUiState(snapCamera = snapCamera)
    }

    private fun buildUiState(snapCamera: Boolean): HillyRoadUiState {
        val totalDist = totalDistance()
        val clampedDist = currentDistanceMeters.coerceIn(0.0, totalDist)
        val progressRatio = if (totalDist > 0.0) {
            (clampedDist / totalDist).toFloat().coerceIn(0.0f, 1.0f)
        } else {
            0.0f
        }

        val interp = PathEngine.interpolatePoint(
            path = activeRoute,
            cumulativeDistances = cumulativeDistances,
            distance = clampedDist,
            lookaheadDistance = 28.0
        )

        val smoothedHeading = PathEngine.smoothHeading(
            targetHeading = interp.bearing,
            currentHeading = smoothedHeadingDegrees,
            isUserScrubbing = isUserScrubbing || snapCamera,
            isPlaying = isPlaying
        )
        smoothedHeadingDegrees = smoothedHeading

        // Only start forward elevation & terrain analysis once the 3D map is ready
        val terrainAlert = if (isMapReady) {
            RoadTerrainAnalyzer.analyze(
                path = activeRoute,
                currentDistanceMeters = clampedDist,
                lookaheadHorizonMeters = lookaheadHorizonMeters
            )
        } else {
            RoadTerrainAnalyzer.emptyAlertState()
        }

        val shouldPreviewCrest = autoCameraPitchPreview && (
            terrainAlert.alertType == TerrainAlertType.UNDULATING_ROLLER_COASTER ||
                terrainAlert.alertType == TerrainAlertType.BLIND_CREST
            )
        val targetTilt = if (shouldPreviewCrest) PREVIEW_CAMERA_TILT else NORMAL_CAMERA_TILT
        val targetRange = if (shouldPreviewCrest) PREVIEW_CAMERA_RANGE else NORMAL_CAMERA_RANGE

        if (snapCamera || isUserScrubbing) {
            currentCameraTilt = targetTilt
            currentCameraRange = targetRange
        } else {
            val blend = 0.12
            currentCameraTilt += (targetTilt - currentCameraTilt) * blend
            currentCameraRange += (targetRange - currentCameraRange) * blend
        }

        // Smooth camera height adjustment for sharp elevation changes:
        // 1. Blend vehicle altitude (70%) with lookahead road altitude (30% at +35m) to round off crests/dips
        val lookaheadInterp = PathEngine.interpolatePoint(
            path = activeRoute,
            cumulativeDistances = cumulativeDistances,
            distance = (clampedDist + CAMERA_ALTITUDE_LOOKAHEAD_METERS).coerceAtMost(totalDist),
            lookaheadDistance = 15.0
        )
        val blendedSurfaceAltitude =
            interp.altitude * (1.0 - CAMERA_LOOKAHEAD_ALTITUDE_WEIGHT) +
                lookaheadInterp.altitude * CAMERA_LOOKAHEAD_ALTITUDE_WEIGHT

        // 2. Grade-proportional vertical clearance boost when traversing or approaching sharp slopes
        val activeSlopeMagnitude = max(
            abs(terrainAlert.currentGradePercent),
            if (terrainAlert.distanceToHazardMeters <= 120.0) {
                max(terrainAlert.maxPositiveGradePercent, abs(terrainAlert.maxNegativeGradePercent)) * 0.6
            } else {
                0.0
            }
        )
        val sharpElevationHeightBoost = if (autoCameraPitchPreview && isMapReady) {
            ((activeSlopeMagnitude - 4.0).coerceAtLeast(0.0) * 0.65)
                .coerceIn(0.0, MAX_SHARP_ELEVATION_HEIGHT_BOOST_METERS)
        } else {
            0.0
        }

        val targetCameraAltitude = PathEngine.calculateCameraAltitude(
            altitudeMode = AltitudeMode.ABSOLUTE,
            baseAltitude = routeProfile.baseAltitude,
            interpolatedAltitude = blendedSurfaceAltitude + sharpElevationHeightBoost,
            groundAltitude = routeProfile.baseAltitude,
            cameraRange = currentCameraRange,
            pathAltitudeOffset = ROUTE_ALTITUDE_OFFSET_METERS
        )

        // 3. EMA vertical damping during playback to eliminate harsh C0 vertex kinks
        val previousCameraAlt = smoothedCameraAltitude
        val cameraAltitude = if (previousCameraAlt == null || snapCamera || isUserScrubbing || !isPlaying) {
            targetCameraAltitude
        } else {
            previousCameraAlt + (targetCameraAltitude - previousCameraAlt) * CAMERA_ALTITUDE_EMA_ALPHA
        }
        smoothedCameraAltitude = cameraAltitude

        val vehiclePos = LatLngAltitude(
            interp.latLng.latitude,
            interp.latLng.longitude,
            interp.altitude + VEHICLE_ALTITUDE_OFFSET_METERS
        )

        val cameraCenter = LatLngAltitude(
            interp.latLng.latitude,
            interp.latLng.longitude,
            cameraAltitude
        )

        val traveledVertices = PathEngine.buildProgressVertices(
            path = activeRoute,
            cumulativeDistances = cumulativeDistances,
            elapsedDistance = clampedDist,
            currentLatLng = interp.latLng,
            waypointIndex = interp.waypointIndex,
            altitudeMode = AltitudeMode.ABSOLUTE,
            pathAltitudeOffset = ROUTE_ALTITUDE_OFFSET_METERS
        )

        return HillyRoadUiState(
            selectedPreset = activePreset,
            routePoints = activeRoute,
            staticRouteVertices = staticVertices,
            traveledRouteVertices = traveledVertices,
            isMapReady = isMapReady,
            isPlaying = isPlaying,
            isUserScrubbing = isUserScrubbing,
            progressRatio = progressRatio,
            currentDistanceMeters = clampedDist,
            totalDistanceMeters = totalDist,
            speedMultiplier = speedMultiplier,
            lookaheadHorizonMeters = lookaheadHorizonMeters,
            autoCameraPitchPreview = autoCameraPitchPreview,
            vehiclePosition = vehiclePos,
            currentAltitudeMeters = interp.altitude,
            currentHeadingDegrees = smoothedHeading,
            cameraCenter = cameraCenter,
            cameraHeading = smoothedHeading,
            cameraTilt = currentCameraTilt,
            cameraRange = currentCameraRange,
            terrainAlert = terrainAlert
        )
    }
}
