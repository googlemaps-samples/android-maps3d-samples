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

package com.google.maps.android.compose3d

import com.google.android.gms.maps3d.GoogleMap3D

/**
 * A global registry to manage the single instance of [GoogleMap3D] provided by the SDK.
 *
 * The Maps 3D SDK typically creates and reuses a single [GoogleMap3D] instance across
 * multiple [com.google.android.gms.maps3d.Map3DView] instances. To adhere to Compose
 * design principles and avoid passing this imperative object through ViewModels, this
 * registry holds the instance and provides access to it for state synchronization.
 *
 * We also track whether the map has ever been "ready" via `OnMapReadyListener`,
 * as the SDK only triggers this once per application lifetime.
 */
object Map3DRegistry {
    private var mapInstance: GoogleMap3D? = null

    /**
     * Tracks whether the map has been initialized and is ready for content.
     * The SDK only calls `OnMapReadyListener` once.
     */
    var isMapReady: Boolean = false
        internal set

    /**
     * Sets the [GoogleMap3D] instance. This should be called when the map is ready
     * in the [GoogleMap3D] composable.
     */
    fun setInstance(map: GoogleMap3D) {
        mapInstance = map
    }

    /**
     * Retrieves the current [GoogleMap3D] instance, if available.
     */
    fun getInstance(): GoogleMap3D? = mapInstance

    /**
     * Clears the instance. This should be called when the map view is destroyed or
     * no longer needed to prevent potential leaks.
     */
    fun clearInstance() {
        mapInstance = null
        // We do not reset isMapReady here, as the underlying SDK instance might still be ready
        // even if we detach from a specific view.
    }

    /**
     * Marks the map as ready. Called when the `OnMapReadyListener` fires.
     */
    fun markReady() {
        isMapReady = true
    }
}
