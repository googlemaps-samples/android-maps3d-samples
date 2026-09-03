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

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.CameraRestriction
import com.google.android.gms.maps3d.model.Map3DMode

/**
 * Robust map state resetting utility for Google Maps 3D.
 *
 * The Maps 3D SDK reuses its underlying native rendering engine across activity transitions
 * within the same application process. As a result, properties such as [Map3DMode] (e.g. SATELLITE,
 * ROADMAP, HYBRID), camera bounds/restrictions, and click listeners can persist across samples.
 *
 * This utility applies an immediate reset and a delayed stabilization reset (after the native
 * viewport layout pass completes) to guarantee every sample starts in its expected clean state.
 */
object MapResetHelper {

    private const val TAG = "MapResetHelper"

    /**
     * Standard delay in milliseconds to allow the native 3D engine viewport to settle
     * before reapplying mode, camera position, and restrictions.
     */
    const val STABILIZATION_DELAY_MS: Long = 400L

    /**
     * Resets the [GoogleMap3D] instance to the specified baseline state.
     *
     * @param map The [GoogleMap3D] instance to reset.
     * @param expectedMode The target [Map3DMode] (defaults to [Map3DMode.HYBRID]).
     * @param initialCamera The target initial [Camera] position, or null if unchanged.
     * @param cameraRestriction The target [CameraRestriction], or null to clear all restrictions.
     */
    @JvmStatic
    @JvmOverloads
    fun resetMapState(
        map: GoogleMap3D?,
        expectedMode: Int = Map3DMode.HYBRID,
        initialCamera: Camera? = null,
        cameraRestriction: CameraRestriction? = null,
    ) {
        if (map == null) return
        try {
            // 1. Force explicit Map Rendering Mode (fixes Satellite/Roadmap bleed)
            map.setMapMode(expectedMode)

            // 2. Reset / apply camera restrictions (prevents bounds clamping from bleeding)
            map.setCameraRestriction(cameraRestriction)

            // 3. Apply target camera orientation and position
            if (initialCamera != null) {
                map.setCamera(initialCamera.toValidCamera())
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error applying map state reset: ${e.message}")
        }
    }

    /**
     * Schedules a delayed stabilization reset on the main thread Looper.
     *
     * @param mapProvider A lambda returning the current [GoogleMap3D] instance.
     * @param isAlive A predicate checking if the host Activity/View is still active and not destroyed.
     * @param expectedMode The target [Map3DMode] (defaults to [Map3DMode.HYBRID]).
     * @param initialCamera The target initial [Camera] position.
     * @param cameraRestriction The target [CameraRestriction], or null.
     * @param delayMillis Delay in milliseconds (defaults to [STABILIZATION_DELAY_MS]).
     * @param onStabilized Optional callback invoked after stabilization is complete.
     */
    @JvmStatic
    @JvmOverloads
    fun scheduleStabilizationReset(
        mapProvider: () -> GoogleMap3D?,
        isAlive: () -> Boolean,
        expectedMode: Int = Map3DMode.HYBRID,
        initialCamera: Camera? = null,
        cameraRestriction: CameraRestriction? = null,
        delayMillis: Long = STABILIZATION_DELAY_MS,
        onStabilized: ((GoogleMap3D) -> Unit)? = null,
    ) {
        Handler(Looper.getMainLooper()).postDelayed({
            if (isAlive()) {
                val map = mapProvider()
                if (map != null) {
                    resetMapState(map, expectedMode, initialCamera, cameraRestriction)
                    onStabilized?.invoke(map)
                }
            }
        }, delayMillis)
    }

    /**
     * Teardown cleanup to clear listeners and restrictions when exiting a sample.
     */
    @JvmStatic
    fun teardownMap(map: GoogleMap3D?) {
        if (map == null) return
        try {
            map.setCameraChangedListener(null)
            map.setOnMapSteadyListener(null)
            map.setCameraRestriction(null)
            // Reset mode to standard HYBRID baseline for next sample
            map.setMapMode(Map3DMode.HYBRID)
        } catch (e: Exception) {
            Log.w(TAG, "Error during map teardown: ${e.message}")
        }
    }
}
