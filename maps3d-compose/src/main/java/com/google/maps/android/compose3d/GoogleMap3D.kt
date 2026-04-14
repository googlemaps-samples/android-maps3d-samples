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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.Map3DOptions
import com.google.android.gms.maps3d.Map3DView
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.CameraRestriction
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.maps.android.compose3d.utils.toValidCamera
import com.google.maps.android.compose3d.utils.toValidCameraRestriction

/**
 * A declarative Compose wrapper for the Google Maps 3D SDK [Map3DView].
 *
 * This composable allows you to display a 3D map and control it using standard Compose state.
 * It handles the underlying view lifecycle and synchronizes state objects (markers, polylines,
 * polygons, and models) with the imperative SDK instance.
 *
 * Literate Programming Note: Initialization in the Maps 3D SDK is tricky. We cannot rely solely
 * on `getMap3DViewAsync`. We must wait for `setOnMapReadyListener` to fire before adding any
 * content, otherwise additions might be ignored. Furthermore, this listener only fires ONCE
 * in the lifetime of the application. To handle this, we track readiness globally in
 * [Map3DRegistry] and defer all state updates until we are certain the map is ready.
 *
 * @param camera The hoisted camera state to apply to the map.
 * @param markers The list of markers to display on the map.
 * @param polylines The list of polylines to display on the map.
 * @param polygons The list of polygons to display on the map.
 * @param models The list of 3D models to display on the map.
 * @param cameraRestriction The camera restriction to apply to the map.
 * @param mapMode The map mode (e.g., SATELLITE, HYBRID).
 * @param modifier The modifier to apply to the layout.
 * @param options The options to initialize the [Map3DView] with.
 * @param onMapReady Optional callback invoked when the [GoogleMap3D] instance is ready.
 */
@Composable
fun GoogleMap3D(
    camera: Camera,
    modifier: Modifier = Modifier,
    markers: List<MarkerConfig> = emptyList(),
    polylines: List<PolylineConfig> = emptyList(),
    polygons: List<PolygonConfig> = emptyList(),
    models: List<ModelConfig> = emptyList(),
    popovers: List<PopoverConfig> = emptyList(),
    cameraRestriction: CameraRestriction? = null,
    @Map3DMode mapMode: Int = Map3DMode.SATELLITE,
    options: Map3DOptions = Map3DOptions(),
    onMapReady: (GoogleMap3D) -> Unit = {},
    onMapSteady: () -> Unit = {},
    onMapClick: (() -> Unit)? = null,
) {
    val state = remember { Map3DState() }
    val hasCalledOnMapReady = remember { mutableStateOf(false) }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            val map3dView = Map3DView(context, options)
            map3dView.onCreate(null)
            map3dView
        },
        update = { map3dView ->
            map3dView.getMap3DViewAsync(object : OnMap3DViewReadyCallback {
                override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
                    Map3DRegistry.setInstance(googleMap3D)

                    googleMap3D.setOnMapSteadyListener { isSteady ->
                        if (isSteady) {
                            onMapSteady()
                        }
                    }

                    fun applyUpdates() {
                        if (!hasCalledOnMapReady.value) {
                            onMapReady(googleMap3D)
                            hasCalledOnMapReady.value = true
                        }

                        // Sync hoisted state with the imperative map instance
                        googleMap3D.setCamera(camera.toValidCamera())
                        googleMap3D.setCameraRestriction(cameraRestriction.toValidCameraRestriction())
                        googleMap3D.setMapMode(mapMode)

                        state.syncMarkers(googleMap3D, markers)
                        state.syncPolylines(googleMap3D, polylines)
                        state.syncPolygons(googleMap3D, polygons)
                        state.syncModels(googleMap3D, models)
                        state.syncPopovers(map3dView.context, googleMap3D, popovers)

                        onMapClick?.let { callback ->
                            googleMap3D.setMap3DClickListener { _, _ ->
                                callback()
                            }
                        }
                    }

                    if (Map3DRegistry.isMapReady) {
                        // Map was already ready (e.g. reused instance), apply updates immediately
                        applyUpdates()
                    } else {
                        // First time initialization, must wait for listener
                        googleMap3D.setOnMapReadyListener {
                            googleMap3D.setOnMapReadyListener(null) // Clear it immediately
                            Map3DRegistry.markReady()
                            applyUpdates()
                        }
                    }
                }

                override fun onError(error: Exception) {
                    throw error
                }
            })
        },
        onRelease = { map3dView ->
            state.clear()
            Map3DRegistry.clearInstance()
            map3dView.onDestroy()
        },
    )
}
