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

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.Map3DInitConfig
import com.google.android.gms.maps3d.Map3DView
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.CameraRestriction
import com.google.android.gms.maps3d.model.LatLngAltitude
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
    options: Map3DInitConfig = Map3DInitConfig.create(
        centerLat = camera.center.latitude,
        centerLng = camera.center.longitude,
        centerAlt = camera.center.altitude,
        heading = camera.heading ?: 0.0,
        tilt = camera.tilt ?: 0.0,
        roll = camera.roll ?: 0.0,
        range = camera.range ?: 10_000_000.0,
        minHeading = 0.0,
        maxHeading = 360.0,
        minTilt = 0.0,
        maxTilt = 90.0,
        bounds = null,
        mapMode = mapMode,
        mapId = null,
        minAltitude = 0.0,
        maxAltitude = 1000000.0,
        language = java.util.Locale.getDefault().language,
        region = java.util.Locale.getDefault().country,
    ),
    onMapReady: (GoogleMap3D) -> Unit = {},
    onMapSteady: () -> Unit = {},
    onMapSteadyChange: (Boolean) -> Unit = {},
    onMapClick: ((LatLngAltitude) -> Unit)? = null,
    onPlaceClick: ((String) -> Unit)? = null,
    onCameraChanged: (Camera) -> Unit = {},
) {
    val hostContext = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val state = remember { Map3DState() }
    val isMapReady = remember { mutableStateOf(false) }
    val googleMap3DState = remember { mutableStateOf<GoogleMap3D?>(null) }
    val map3DViewState = remember { mutableStateOf<Map3DView?>(null) }

    // Use rememberUpdatedState to avoid capturing stale lambdas in the async callback
    val currentOnMapSteady by rememberUpdatedState(onMapSteady)
    val currentOnMapSteadyChange by rememberUpdatedState(onMapSteadyChange)
    val currentOnCameraChanged by rememberUpdatedState(onCameraChanged)
    val currentOnMapReady by rememberUpdatedState(onMapReady)
    val currentOnMapClick by rememberUpdatedState(onMapClick)
    val currentOnPlaceClick by rememberUpdatedState(onPlaceClick)

    DisposableEffect(lifecycleOwner, map3DViewState.value) {
        val map3dView = map3DViewState.value ?: return@DisposableEffect onDispose {}
        var isViewResumed = false
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (!isViewResumed) {
                        map3dView.onResume()
                        isViewResumed = true
                    }
                }

                Lifecycle.Event.ON_PAUSE -> {
                    if (isViewResumed) {
                        map3dView.onPause()
                        isViewResumed = false
                    }
                }

                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            if (isViewResumed) {
                map3dView.onPause()
                isViewResumed = false
            }
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            val map3dView = Map3DView(context, options)
            map3dView.onCreate(null)

            map3dView.getMap3DViewAsync(object : OnMap3DViewReadyCallback {
                override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
                    googleMap3DState.value = googleMap3D
                    Map3DRegistry.setInstance(googleMap3D)

                    googleMap3D.setOnMapReadyListener {
                        googleMap3D.setOnMapReadyListener(null)
                        Map3DRegistry.markReady()
                        isMapReady.value = true
                    }

                    // Ensure readiness triggers even on delayed or reused map instances
                    map3dView.postDelayed({
                        if (!isMapReady.value) {
                            Map3DRegistry.markReady()
                            isMapReady.value = true
                        }
                    }, 2_000L)

                    googleMap3D.setOnMapSteadyListener { isSteady ->
                        currentOnMapSteadyChange(isSteady)
                        if (isSteady) {
                            currentOnMapSteady()
                        }
                    }

                    googleMap3D.setCameraChangedListener { camera ->
                        currentOnCameraChanged(camera)
                    }

                    if (currentOnMapClick != null || currentOnPlaceClick != null) {
                        googleMap3D.setMap3DClickListener { location, placeId ->
                            Log.d("GoogleMap3D", "Map clicked at $location, placeId: $placeId")
                            if (placeId != null) {
                                currentOnPlaceClick?.invoke(placeId)
                            } else {
                                currentOnMapClick?.invoke(location)
                            }
                        }
                    }
                }

                override fun onError(error: Exception): Unit = throw error
            })

            map3DViewState.value = map3dView
            map3dView
        },
        update = { _ ->
            val googleMap3D = googleMap3DState.value
            if (googleMap3D != null && isMapReady.value) {
                // Sync hoisted state with the imperative map instance
                state.syncCamera(googleMap3D, camera.toValidCamera())
                state.syncCameraRestriction(
                    googleMap3D,
                    cameraRestriction.toValidCameraRestriction(),
                )
                state.syncMapMode(googleMap3D, mapMode)

                state.syncMarkers(googleMap3D, markers)
                state.syncPolylines(googleMap3D, polylines)
                state.syncPolygons(googleMap3D, polygons)
                state.syncModels(googleMap3D, models)
                state.syncPopovers(hostContext, googleMap3D, popovers)

                if (!state.hasCalledOnMapReady) {
                    state.hasCalledOnMapReady = true
                    currentOnMapReady(googleMap3D)
                }
            }
        },
        onRelease = { map3dView ->
            map3DViewState.value = null
            state.clear()
            Map3DRegistry.clearInstance()
            map3dView.onDestroy()
        },
    )
}
