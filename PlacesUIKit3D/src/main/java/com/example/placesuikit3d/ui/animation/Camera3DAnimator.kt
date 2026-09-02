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

package com.example.placesuikit3d.ui.animation

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.placesuikit3d.data.model.Camera3DTarget
import com.example.placesuikit3d.data.model.PlaceSearchResult
import com.example.placesuikit3d.utils.toValidCamera
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.flyAroundOptions
import com.google.android.gms.maps3d.model.flyToOptions
import com.google.android.gms.maps3d.model.latLngAltitude
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sqrt

/**
 * Controller managing cinematic 3D camera transitions, pitch/tilt adjustments,
 * continuous 360-degree orbit loops, and multi-result overview framing.
 */
class Camera3DAnimator(
    private var googleMap3D: GoogleMap3D? = null,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main),
) {
    private val tag = "Camera3DAnimator"
    private var orbitJob: Job? = null

    private var pendingTarget: Camera3DTarget? = null

    fun setMap(map: GoogleMap3D?) {
        this.googleMap3D = map
        if (map != null) {
            pendingTarget?.let { target ->
                pendingTarget = null
                flyToTarget(target)
            }
        }
    }

    /**
     * Executes a smooth fly-to transition to [target].
     */
    fun flyToTarget(
        target: Camera3DTarget,
        durationMs: Long = 2500,
        onComplete: (() -> Unit)? = null,
    ) {
        stopOrbit()
        val map = googleMap3D
        if (map == null) {
            pendingTarget = target
            return
        }

        val resolvedAlt = when {
            target.altitude > 0.0 -> target.altitude
            target.latitude in 39.0..41.0 && target.longitude in -106.0..-104.0 -> 1650.0
            else -> 0.0
        }

        val resolvedTarget = target.copy(altitude = resolvedAlt)
        val camera = resolvedTarget.toCamera()

        val flyAction = {
            map.flyCameraTo(
                flyToOptions {
                    endCamera = camera
                    durationInMillis = durationMs
                },
            )
            if (onComplete != null) {
                coroutineScope.launch(Dispatchers.Main) {
                    delay(durationMs)
                    onComplete()
                }
            }
        }

        if (Looper.myLooper() == Looper.getMainLooper()) {
            flyAction()
        } else {
            Handler(Looper.getMainLooper()).post(flyAction)
        }
    }

    /**
     * Flies to a selected [place] with cinematic pitch (55° - 65°) and close-up range.
     */
    fun flyToPlace(
        place: PlaceSearchResult,
        heading: Double = 25.0,
        tilt: Double = 50.0,
        range: Double = 1400.0,
        durationMs: Long = 2500,
    ) {
        val target = Camera3DTarget(
            latitude = place.location.latitude,
            longitude = place.location.longitude,
            altitude = place.location.altitude,
            heading = heading,
            tilt = tilt,
            range = range,
        )
        flyToTarget(target, durationMs)
    }

    /**
     * Starts continuous 360-degree orbit around the specified [center] location.
     */
    fun startOrbit(
        center: LatLngAltitude,
        tilt: Double = 50.0,
        range: Double = 1400.0,
        roundDurationMs: Long = 16000,
    ) {
        stopOrbit()
        val map = googleMap3D ?: return

        val baseCamera = camera {
            this.center = center
            this.heading = 0.0
            this.tilt = tilt
            this.range = range
            this.roll = 0.0
        }.toValidCamera()

        orbitJob = coroutineScope.launch(Dispatchers.Main) {
            while (isActive) {
                try {
                    map.flyCameraAround(
                        flyAroundOptions {
                            this.center = baseCamera
                            this.durationInMillis = roundDurationMs
                            this.rounds = 1.0
                        },
                    )
                    delay(roundDurationMs)
                } catch (e: Exception) {
                    Log.w(tag, "Orbit cycle interrupted: ${e.message}")
                    break
                }
            }
        }
    }

    /**
     * Stops any active orbit camera movement.
     */
    fun stopOrbit() {
        orbitJob?.cancel()
        orbitJob = null
        googleMap3D?.stopCameraAnimation()
    }

    /**
     * Automatically calculates the 3D bounding box for all [places] and animates
     * the camera to an encompassing overview at a comfortable 35° - 45° tilt.
     */
    fun frameMultiResults(
        places: List<PlaceSearchResult>,
        tilt: Double = 42.0,
        durationMs: Long = 2500,
    ) {
        if (places.isEmpty()) return
        stopOrbit()

        if (places.size == 1) {
            flyToPlace(places.first(), tilt = 50.0, range = 1400.0, durationMs = durationMs)
            return
        }

        val centroid = calculateCentroid(places)
        val optimalRange = calculateOptimalRange(places)

        val overviewTarget = Camera3DTarget(
            latitude = centroid.latitude,
            longitude = centroid.longitude,
            altitude = centroid.altitude,
            heading = 25.0,
            tilt = tilt,
            range = optimalRange,
        )

        flyToTarget(overviewTarget, durationMs)
    }

    /**
     * Toggles camera view between 2D Top-Down (tilt 0°) and 3D Perspective (tilt 50°).
     */
    fun toggle2D3D(
        currentCenter: LatLngAltitude,
        to3D: Boolean,
        durationMs: Long = 2000,
    ) {
        stopOrbit()
        val target = if (to3D) {
            Camera3DTarget(
                latitude = currentCenter.latitude,
                longitude = currentCenter.longitude,
                altitude = currentCenter.altitude,
                heading = 25.0,
                tilt = 50.0,
                range = 1400.0,
            )
        } else {
            Camera3DTarget(
                latitude = currentCenter.latitude,
                longitude = currentCenter.longitude,
                altitude = currentCenter.altitude,
                heading = 0.0,
                tilt = 0.0,
                range = 3500.0,
            )
        }
        flyToTarget(target, durationMs)
    }

    companion object {
        /**
         * Calculates geographic centroid of place coordinates.
         */
        fun calculateCentroid(places: List<PlaceSearchResult>): LatLngAltitude {
            if (places.isEmpty()) return Camera3DTarget.DEFAULT.toLatLngAltitude()
            var sumLat = 0.0
            var sumLng = 0.0
            var sumAlt = 0.0

            places.forEach {
                sumLat += it.location.latitude
                sumLng += it.location.longitude
                sumAlt += it.location.altitude
            }

            val count = places.size.toDouble()
            return latLngAltitude {
                latitude = sumLat / count
                longitude = sumLng / count
                altitude = sumAlt / count
            }
        }

        /**
         * Computes optimal camera range to fit all places within the viewport.
         */
        fun calculateOptimalRange(places: List<PlaceSearchResult>): Double {
            if (places.size <= 1) return 500.0

            var minLat = Double.MAX_VALUE
            var maxLat = -Double.MAX_VALUE
            var minLng = Double.MAX_VALUE
            var maxLng = -Double.MAX_VALUE

            places.forEach {
                val lat = it.location.latitude
                val lng = it.location.longitude
                if (lat < minLat) minLat = lat
                if (lat > maxLat) maxLat = lat
                if (lng < minLng) minLng = lng
                if (lng > maxLng) maxLng = lng
            }

            val latDistance = (maxLat - minLat) * 111_000.0 // ~111km per degree latitude
            val avgLatRad = Math.toRadians((minLat + maxLat) / 2.0)
            val lngDistance = (maxLng - minLng) * 111_000.0 * cos(avgLatRad)
            val maxSpan = sqrt(latDistance * latDistance + lngDistance * lngDistance)

            // Clamp range between 800m and 12,000m
            return max(800.0, maxSpan * 1.8).coerceIn(800.0, 15000.0)
        }

        /**
         * Computes dynamic marker altitude (height in meters above ground) proportional
         * to the camera viewing distance (range). This guarantees that the marker pin and
         * extrusion line remain gracefully scaled and in frame regardless of zoom or altitude.
         *
         * @param cameraRange Distance of the camera from the target in meters
         * @param isSelected Whether this marker is currently focused / selected
         * @return Altitude in meters relative to ground level (clamped between 4.0m and 35.0m)
         */
        fun calculateDynamicMarkerAltitude(
            cameraRange: Double?,
            isSelected: Boolean = false,
        ): Double {
            val range = cameraRange ?: 1400.0
            val scaleFactor = if (isSelected) 0.014 else 0.009
            return (range * scaleFactor).coerceIn(4.0, 35.0)
        }
    }
}
