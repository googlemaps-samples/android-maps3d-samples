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

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.advancedmaps3dsamples.BuildConfig
import com.example.advancedmaps3dsamples.R
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
import com.google.android.gms.maps3d.model.ImageView
import com.google.android.gms.maps3d.model.Marker
import com.google.android.gms.maps3d.model.Polyline
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.gms.maps3d.model.markerOptions
import com.google.android.gms.maps3d.model.modelOptions
import com.google.android.gms.maps3d.model.orientation
import com.google.android.gms.maps3d.model.polylineOptions
import com.google.android.gms.maps3d.model.vector3D
import com.example.advancedmaps3dsamples.utils.toHeading
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class TrackerStyle { MARKER, RED_CAR, BANANA_CAR }

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

            val context = LocalContext.current
            val sharedPrefs = remember { context.getSharedPreferences("route_prefs", Context.MODE_PRIVATE) }
            val warningCount = remember { sharedPrefs.getInt("warning_count", 0) }

            // Show the critical API Key leakage warning immediately on load (max 2 times)
            var displayWarning by remember { mutableStateOf(warningCount < 2) }

            // Dynamic tracking state
            var cameraRange by remember { mutableStateOf(1500f) }
            var trackerStyle by remember { mutableStateOf(TrackerStyle.MARKER) }
            
            // Playback controls state
            var elapsedDistance by remember { mutableFloatStateOf(0f) }
            var totalDistance by remember { mutableFloatStateOf(0f) }
            var isPlaying by remember { mutableStateOf(false) }
            var flyModeActive by remember { mutableStateOf(false) }

            AdvancedMaps3DSamplesTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(), topBar = {
                        CenterAlignedTopAppBar(
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.primary,
                            ), title = {
                                Text("Routes API Integration")
                            }, actions = {
                                IconButton(onClick = {
                                    trackerStyle = when (trackerStyle) {
                                        TrackerStyle.MARKER -> TrackerStyle.RED_CAR
                                        TrackerStyle.RED_CAR -> TrackerStyle.BANANA_CAR
                                        TrackerStyle.BANANA_CAR -> TrackerStyle.MARKER
                                    }
                                }) {
                                    Icon(
                                        imageVector = when (trackerStyle) {
                                            TrackerStyle.MARKER -> Icons.Default.Place
                                            TrackerStyle.RED_CAR -> Icons.Default.DirectionsCar
                                            TrackerStyle.BANANA_CAR -> Icons.Default.Star
                                        },
                                        contentDescription = "Toggle Tracker Style"
                                    )
                                }
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

                        // DECOUPLED TRACKING ENGINE
                        // This engine automatically renders whatever frame 'elapsedDistance' dictates.
                        // Playback merely increments 'elapsedDistance'. Scrubbing mutates it directly.
                        LaunchedEffect(flyModeActive, uiState, map3D) {
                            if (!flyModeActive) {
                                // Clean up the tracking models if we exit fly mode
                                val safeMap = map3D ?: return@LaunchedEffect
                                val offscreen = latLngAltitude { latitude = 0.0; longitude = 0.0; altitude = -1000.0 }
                                // It is difficult to reliably know the IDs here without hoisting them.
                                // But since we re-calculate everything when we re-enter, the next entry will generate new IDs 
                                // and punt the inactive ones anyway. Ideally we would hoist the marker IDs too, but 
                                // letting them be garbage collected is fine for a demo.
                                return@LaunchedEffect
                            }
                            
                            val state = uiState as? RouteUiState.Success ?: return@LaunchedEffect
                            val safeMap = map3D ?: return@LaunchedEffect
                            val rawPath = state.decodedPolyline
                            if (rawPath.size < 2) return@LaunchedEffect

                            val cumulativeDistances = DoubleArray(rawPath.size)
                            cumulativeDistances[0] = 0.0
                            for (i in 1 until rawPath.size) {
                                cumulativeDistances[i] = cumulativeDistances[i - 1] + haversineDistance(rawPath[i - 1], rawPath[i])
                            }

                            val baseSpeedMps = 750.0
                            val lookaheadSeconds = 8.0
                            val lerpFactor = 0.05f

                            var currentLat = rawPath[0].latitude
                            var currentLng = rawPath[0].longitude
                            var currentHeading = calculateHeading(rawPath[0], rawPath[1]).toFloat()

                            var lastFrameTime = 0L
                            var progressMarkerId: String? = null
                            var progressRedCarId: String? = null
                            var progressBananaCarId: String? = null

                            while (flyModeActive) {
                                withFrameMillis { frameTime ->
                                    if (lastFrameTime == 0L) {
                                        lastFrameTime = frameTime
                                        return@withFrameMillis
                                    }
                                    val dtMs = frameTime - lastFrameTime
                                    lastFrameTime = frameTime

                                    if (isPlaying) {
                                        elapsedDistance += (baseSpeedMps * (dtMs / 1000.0)).toFloat()
                                        if (elapsedDistance >= totalDistance) {
                                            elapsedDistance = totalDistance
                                            isPlaying = false
                                        }
                                    }

                                    val doubleElapsed = elapsedDistance.toDouble()
                                    val targetPos = getInterpolatedPoint(doubleElapsed, rawPath, cumulativeDistances)
                                    val lookaheadDist = doubleElapsed + (baseSpeedMps * lookaheadSeconds)
                                    val lookaheadPos = getInterpolatedPoint(lookaheadDist, rawPath, cumulativeDistances)
                                    val mathHeading = calculateHeading(targetPos, lookaheadPos).toFloat()

                                    currentLat += (targetPos.latitude - currentLat) * lerpFactor
                                    currentLng += (targetPos.longitude - currentLng) * lerpFactor
                                    currentHeading = slerpHeading(currentHeading, mathHeading, lerpFactor)

                                    val frameCamera = camera {
                                        center = latLngAltitude {
                                            latitude = currentLat
                                            longitude = currentLng
                                            altitude = 250.0
                                        }
                                        heading = currentHeading.toDouble().toHeading()
                                        tilt = 65.0
                                        range = cameraRange.toDouble()
                                    }.toValidCamera()
                                    safeMap.setCamera(frameCamera)

                                    // MARKER / MODEL UPSERT LOGIC
                                    when (trackerStyle) {
                                        TrackerStyle.RED_CAR, TrackerStyle.BANANA_CAR -> {
                                            val isActiveRedCar = trackerStyle == TrackerStyle.RED_CAR
                                            val activeUrl = if (isActiveRedCar) "https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/red_car.glb" else "https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/banana_car.glb"
                                            val activeId = if (isActiveRedCar) progressRedCarId else progressBananaCarId

                                            val m = safeMap.addModel(modelOptions {
                                                if (activeId != null) id = activeId
                                                position = latLngAltitude {
                                                    latitude = targetPos.latitude
                                                    longitude = targetPos.longitude
                                                    altitude = 0.0
                                                }
                                                altitudeMode = AltitudeMode.CLAMP_TO_GROUND
                                                url = activeUrl
                                                    // Custom orientation parameters for different models
                                                    val customScale = if (isActiveRedCar) 20.0 else 20.0
                                                    val customTilt = -90.0 // Pitch upright 
                                                    val customHeadingOffset = 0.0 // To adjust if facing sideways
                                                    
                                                    scale = vector3D { x = customScale; y = customScale; z = customScale }
                                                    orientation = orientation {
                                                        heading = (currentHeading.toDouble() + customHeadingOffset).toHeading()
                                                        tilt = customTilt
                                                        roll = 0.0
                                                    }
                                            })
                                            if (activeId == null && m != null) {
                                                if (isActiveRedCar) progressRedCarId = m.id else progressBananaCarId = m.id
                                            }

                                            val inactiveId = if (isActiveRedCar) progressBananaCarId else progressRedCarId
                                            if (inactiveId != null) {
                                                safeMap.addModel(modelOptions {
                                                    id = inactiveId
                                                    position = latLngAltitude { latitude = 0.0; longitude = 0.0; altitude = -1000.0 }
                                                    altitudeMode = AltitudeMode.ABSOLUTE
                                                    url = if (isActiveRedCar) "https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/banana_car.glb" else "https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/red_car.glb"
                                                })
                                            }

                                            if (progressMarkerId != null) {
                                                safeMap.addMarker(markerOptions {
                                                    id = progressMarkerId!!
                                                    position = latLngAltitude { latitude = 0.0; longitude = 0.0; altitude = -1000.0 }
                                                    altitudeMode = AltitudeMode.ABSOLUTE
                                                })
                                            }
                                        }
                                        TrackerStyle.MARKER -> {
                                            val m = safeMap.addMarker(markerOptions {
                                                if (progressMarkerId != null) id = progressMarkerId!!
                                                position = latLngAltitude {
                                                    latitude = targetPos.latitude
                                                    longitude = targetPos.longitude
                                                    altitude = 0.0 
                                                }
                                                altitudeMode = AltitudeMode.CLAMP_TO_GROUND
                                                setStyle(ImageView(R.drawable.car))
                                            })
                                            if (progressMarkerId == null && m != null) progressMarkerId = m.id

                                            if (progressRedCarId != null) {
                                                safeMap.addModel(modelOptions {
                                                    id = progressRedCarId!!
                                                    position = latLngAltitude { latitude = 0.0; longitude = 0.0; altitude = -1000.0 }
                                                    altitudeMode = AltitudeMode.ABSOLUTE
                                                    url = "https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/red_car.glb"
                                                })
                                            }
                                            
                                            if (progressBananaCarId != null) {
                                                safeMap.addModel(modelOptions {
                                                    id = progressBananaCarId!!
                                                    position = latLngAltitude { latitude = 0.0; longitude = 0.0; altitude = -1000.0 }
                                                    altitudeMode = AltitudeMode.ABSOLUTE
                                                    url = "https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/banana_car.glb"
                                                })
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (!flyModeActive) { // STANDARD MODE: Fetch & Fly Buttons
                            Button(
                                onClick = {
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
                                    val rawPath = state.decodedPolyline
                                    if (rawPath.size < 2) return@Button
                                    
                                    val cumulativeDistances = DoubleArray(rawPath.size)
                                    cumulativeDistances[0] = 0.0
                                    for (i in 1 until rawPath.size) {
                                        cumulativeDistances[i] = cumulativeDistances[i - 1] + haversineDistance(rawPath[i - 1], rawPath[i])
                                    }
                                    totalDistance = cumulativeDistances.last().toFloat()
                                    elapsedDistance = 0f
                                    flyModeActive = true
                                    isPlaying = true
                                },
                                enabled = uiState is RouteUiState.Success,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(32.dp)
                            ) {
                                Text("Fly Along")
                            }
                        } else { // FLY MODE: Transport Controls
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(16.dp)
                                    .wrapContentHeight()
                                    .fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                tonalElevation = 4.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = { isPlaying = !isPlaying }) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = "Play/Pause"
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Slider(
                                        value = elapsedDistance,
                                        onValueChange = { 
                                            elapsedDistance = it
                                            isPlaying = false // Auto pause when user scrubs 
                                        },
                                        valueRange = 0f..Math.max(1f, totalDistance),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(onClick = { 
                                        flyModeActive = false 
                                        isPlaying = false 
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = "Exit Fly Mode")
                                    }
                                }
                            }
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
                            val dismissWarning = {
                                displayWarning = false
                                sharedPrefs.edit().putInt("warning_count", warningCount + 1).apply()
                            }
                            AlertDialog(
                                onDismissRequest = dismissWarning,
                                icon = {
                                    Icon(Icons.Filled.Warning, contentDescription = null)
                                },
                                title = {
                                    Text("Security Warning")
                                },
                                text = {
                                    Text("This sample makes a direct REST API call from a mobile client to the Google Maps Routes API. In a production application, doing this exposes your API key to malicious extraction.\n\nAlways proxy your Routes API requests through a secure backend server!")
                                },
                                confirmButton = {
                                    TextButton(onClick = dismissWarning) {
                                        Text("I Understand")
                                    }
                                }
                            )
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
