// Copyright 2026 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.advancedmaps3dsamples.scenarios

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.advancedmaps3dsamples.BuildConfig
import com.example.advancedmaps3dsamples.ui.theme.AdvancedMaps3DSamplesTheme
import com.example.advancedmaps3dsamples.utils.calculateHeading
import com.example.advancedmaps3dsamples.utils.haversineDistance
import com.example.advancedmaps3dsamples.utils.toValidCamera
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.Map3DOptions
import com.google.android.gms.maps3d.Map3DView
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.Marker
import com.google.android.gms.maps3d.model.Polyline
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.gms.maps3d.model.markerOptions
import com.google.android.gms.maps3d.model.polylineOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
@OptIn(ExperimentalMaterial3Api::class)
class RouteSampleActivity : ComponentActivity() {
    private val viewModel: RouteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            var map3D by remember { mutableStateOf<GoogleMap3D?>(null) }
            val coroutineScope = rememberCoroutineScope()
            var currentPolyline by remember { mutableStateOf<Polyline?>(null) }

            // Show the critical API Key leakage warning immediately on load
            var displayWarning by remember { mutableStateOf(true) }

            // Dynamic tracking state
            var cameraRange by remember { mutableStateOf(1500f) }

            AdvancedMaps3DSamplesTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(), topBar = {
                        CenterAlignedTopAppBar(
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.primary,
                            ), title = {
                                Text("Routes API Integration")
                            })
                    }) { innerPadding ->
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)) {

                        // The Map3D View wrapper
                        // WHY ANDROIDVIEW?
                        // Map3DView is a traditional Android View, but our UI is built using Modern Jetpack Compose.
                        // AndroidView acts as a bridge, allowing us to embed traditional Views directly into our Compose hierarchy.
                        // We provide a factory to create the view, an update block to handle changes, and an onRelease 
                        // block to clean up resources when the Compose node leaves the screen.
                        AndroidView(modifier = Modifier.fillMaxSize(), factory = { context ->
                            val options = Map3DOptions(
                                centerLat = 21.350,
                                centerLng = -157.800,
                                centerAlt = 0.0,
                                tilt = 60.0,
                                range = 25000.0
                            )
                            val view = Map3DView(context, options)
                            view.onCreate(savedInstanceState)
                            view
                        }, update = { view ->
                            view.getMap3DViewAsync(object : OnMap3DViewReadyCallback {
                                override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
                                    map3D = googleMap3D
                                }

                                override fun onError(e: Exception) {
                                    // Simple error log
                                }
                            })
                        }, onRelease = { view ->
                            map3D = null
                            view.onDestroy()
                        })

                        // Floating Action Button to trigger the route fetch
                        Button(
                            onClick = {
                                // Hardcoded parameters as requested (Honolulu to Kailua)
                                val origin = LatLng(21.307043, -157.858984)
                                val dest = LatLng(21.390177, -157.719454)
                                viewModel.fetchRoute(BuildConfig.MAPS3D_API_KEY, origin, dest)
                            }, modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(32.dp)
                        ) {
                            Text("Fetch & Draw Route")
                        }

                        Button(
                            onClick = {
                                val state = uiState as? RouteUiState.Success ?: return@Button
                                val safeMap = map3D ?: return@Button

                                coroutineScope.launch {
                                    val rawPath = state.decodedPolyline
                                    if (rawPath.size < 2) return@launch

                                    // 1. DITCHING WAYPOINTS: Calculate true cumulative distance for the entire pipeline.
                                    val cumulativeDistances = DoubleArray(rawPath.size)
                                    cumulativeDistances[0] = 0.0
                                    for (i in 1 until rawPath.size) {
                                        cumulativeDistances[i] =
                                            cumulativeDistances[i - 1] + haversineDistance(
                                                rawPath[i - 1],
                                                rawPath[i]
                                            )
                                    }
                                    val totalDistance = cumulativeDistances.last()

                                    // 2. PHYSICS INITIALIZATION
                                    val baseSpeedMps = 750.0
                                    val lookaheadSeconds = 8.0
                                    val lerpFactor =
                                        0.05f // 5% stiffness per frame for smooth rubber banding

                                    var elapsedDistance = 0.0
                                    var currentLat = rawPath[0].latitude
                                    var currentLng = rawPath[0].longitude
                                    var currentHeading =
                                        calculateHeading(rawPath[0], rawPath[1]).toFloat()

                                    // 3. CONTINUOUS RENDERING LOOP
                                    var lastFrameTime = 0L
                                    var progressMarkerId: String? = null

                                    while (elapsedDistance < totalDistance) {
                                        withFrameMillis { frameTime ->
                                            if (lastFrameTime == 0L) {
                                                lastFrameTime = frameTime
                                                return@withFrameMillis
                                            }

                                            val dtMs = frameTime - lastFrameTime
                                            lastFrameTime = frameTime

                                            // Advance logical distance
                                            elapsedDistance += baseSpeedMps * (dtMs / 1000.0)
                                            if (elapsedDistance > totalDistance) elapsedDistance =
                                                totalDistance

                                            // Find mathematical target point
                                            val targetPos = getInterpolatedPoint(
                                                elapsedDistance,
                                                rawPath,
                                                cumulativeDistances
                                            )

                                            // Cinematic Inertia View: find lookahead point 8 seconds in future
                                            val lookaheadDist =
                                                elapsedDistance + (baseSpeedMps * lookaheadSeconds)
                                            val lookaheadPos = getInterpolatedPoint(
                                                lookaheadDist,
                                                rawPath,
                                                cumulativeDistances
                                            )

                                            // Mathematical heading
                                            val mathHeading =
                                                calculateHeading(targetPos, lookaheadPos).toFloat()

                                            // Apply LERP for Physical Position
                                            currentLat += (targetPos.latitude - currentLat) * lerpFactor
                                            currentLng += (targetPos.longitude - currentLng) * lerpFactor

                                            // Apply SLERP for Heading
                                            currentHeading = slerpHeading(
                                                currentHeading,
                                                mathHeading,
                                                lerpFactor
                                            )

                                            // Render directly to WebGL engine (no animateTo queueing!)
                                            val frameCamera = camera {
                                                center = latLngAltitude {
                                                    latitude = currentLat
                                                    longitude = currentLng
                                                    // Provide a fixed altitude as Android lacks synchronous ElevationService
                                                    altitude = 250.0
                                                }
                                                heading = currentHeading.toDouble()
                                                tilt = 65.0
                                                range = cameraRange.toDouble() // Hooked to slider
                                            }.toValidCamera()
                                            safeMap.setCamera(frameCamera)

                                            // 4. DYNAMIC PROGRESS MARKER 
                                            // 'Upsert' pattern: Map3D allows us to update a marker by passing 
                                            // an identical ID into addMarker, bypassing standard property setters.
                                            val markerInstance = safeMap.addMarker(markerOptions {
                                                if (progressMarkerId != null) {
                                                    id = progressMarkerId!!
                                                }
                                                position = latLngAltitude {
                                                    latitude = targetPos.latitude
                                                    longitude = targetPos.longitude
                                                    altitude = 0.0 
                                                }
                                                altitudeMode = AltitudeMode.CLAMP_TO_GROUND
                                            })
                                            
                                            // Capture the generated ID on the first frame
                                            if (progressMarkerId == null && markerInstance != null) {
                                                progressMarkerId = markerInstance.id
                                            }
                                        }
                                    }
                                }
                            },
                            enabled = uiState is RouteUiState.Success,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(32.dp)
                        ) {
                            Text("Fly Along")
                        }

                        // State interpretation UI
                        when (val state = uiState) {
                            is RouteUiState.Loading -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }

                            is RouteUiState.Success -> {
                                // Draw the polyline once the map is ready and we have successful data
                                LaunchedEffect(state.decodedPolyline, map3D) {
                                    val safeMap = map3D ?: return@LaunchedEffect

                                    // Clean up previous line if it exists
                                    currentPolyline?.remove()

                                    // DRAW POLYLINE (Raw version, as requested)
                                    val polylinePath = state.decodedPolyline.map { latLng ->
                                        latLngAltitude {
                                            latitude = latLng.latitude
                                            longitude = latLng.longitude
                                            altitude = 0.0
                                        }
                                    }

                                    val lineOptions = polylineOptions {
                                        this.path = polylinePath
                                        strokeColor = android.graphics.Color.BLUE
                                        strokeWidth = 10.0
                                    }

                                    currentPolyline = safeMap.addPolyline(lineOptions)

                                    // Move the camera to see the full route
                                    val routeCamera = camera {
                                        center = latLngAltitude {
                                            latitude = 21.350
                                            longitude = -157.800
                                            altitude = 0.0
                                        }
                                        tilt = 45.0
                                        range = 35000.0
                                    }
                                    safeMap.setCamera(routeCamera)
                                }
                            }

                            is RouteUiState.Error -> {
                                // Display error overlay
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(32.dp)
                                ) {
                                    Text(
                                        text = state.message,
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            RouteUiState.Idle -> { /* Do nothing */
                            }
                        }

                        // Vertical Range Slider Hooked to Dynamic UI State
                        // 
                        // WHY A CUSTOM VERTICAL SLIDER?
                        // Jetpack Compose Material 3 currently lacks a native 'VerticalSlider' component.
                        // To achieve a vertical layout without importing heavy third-party libraries, we leverage
                        // hardware composition overlays:
                        // 1. We swap the requiredWidth and requiredHeight of the standard horizontal Slider.
                        // 2. We use graphicsLayer { rotationZ = 270f } to natively render it sideways.
                        // 3. We trap pointer events at the Initial routing pass using pointerInput so that tapping 
                        //    and dragging the slider automatically resets the interaction auto-fade timer.
                        if (uiState is RouteUiState.Success) {
                            var sliderInteractionTime by remember { mutableStateOf(System.currentTimeMillis()) }
                            var isSliderActive by remember { mutableStateOf(true) }

                            LaunchedEffect(sliderInteractionTime) {
                                isSliderActive = true
                                delay(3000)
                                isSliderActive = false
                            }

                            val sliderAlpha by animateFloatAsState(
                                targetValue = if (isSliderActive) 0.9f else 0.3f,
                                animationSpec = tween(durationMillis = 500),
                                label = "sliderAlpha"
                            )

                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 16.dp)
                                    .requiredWidth(48.dp)
                                    .requiredHeight(300.dp)
                                    .alpha(sliderAlpha)
                                    .pointerInput(Unit) {
                                        awaitPointerEventScope {
                                            while (true) {
                                                awaitPointerEvent(PointerEventPass.Initial)
                                                sliderInteractionTime = System.currentTimeMillis()
                                            }
                                        }
                                    }) {
                                Slider(
                                    value = cameraRange,
                                    onValueChange = { cameraRange = it },
                                    valueRange = 200f..10000f,
                                    modifier = Modifier
                                        .requiredWidth(300.dp)
                                        .requiredHeight(48.dp)
                                        .graphicsLayer {
                                            rotationZ = 270f
                                            transformOrigin = TransformOrigin(0.5f, 0.5f)
                                        }
                                        .align(Alignment.Center))
                            }
                        }

                        if (displayWarning) {
                            AlertDialog(onDismissRequest = { displayWarning = false }, icon = {
                                Icon(Icons.Filled.Warning, contentDescription = null)
                            }, title = {
                                Text("Security Warning")
                            }, text = {
                                Text("This sample makes a direct REST API call from a mobile client to the Google Maps Routes API. In a production application, doing this exposes your API key to malicious extraction.\n\nAlways proxy your Routes API requests through a secure backend server!")
                            }, confirmButton = {
                                TextButton(onClick = { displayWarning = false }) {
                                    Text("I Understand")
                                }
                            })
                        }
                    }
                }
            }
        }
    }
}

/**
 * Instantly finds the mathematical coordinate along the path given an absolute distance in meters.
 * Replaces expensive haversine math inside the render loop with a precomputed distance array lookup.
 */
private fun getInterpolatedPoint(
    distance: Double,
    path: List<LatLng>,
    cumulativeDistances: DoubleArray
): LatLng {
    if (distance <= 0.0) return path.first()
    if (distance >= cumulativeDistances.last()) return path.last()

    var idx = cumulativeDistances.binarySearch(distance)
    if (idx < 0) {
        idx = -(idx + 1) - 1 // insertion point - 1
    }
    idx = idx.coerceIn(0, cumulativeDistances.size - 2)

    val p1 = path[idx]
    val p2 = path[idx + 1]
    val d1 = cumulativeDistances[idx]
    val d2 = cumulativeDistances[idx + 1]

    val fraction = (distance - d1) / (d2 - d1)
    if (fraction <= 0.0) return p1
    if (fraction >= 1.0) return p2

    val lat = p1.latitude + (p2.latitude - p1.latitude) * fraction
    val lng = p1.longitude + (p2.longitude - p1.longitude) * fraction
    return LatLng(lat, lng)
}

/**
 * Ensures the camera takes the shortest rotational path (e.g., crossing 359 to 0 smoothly)
 * instead of violently spinning backward.
 */
private fun slerpHeading(current: Float, target: Float, factor: Float): Float {
    var dh = target - current
    while (dh > 180f) dh -= 360f
    while (dh <= -180f) dh += 360f
    return current + dh * factor
}
