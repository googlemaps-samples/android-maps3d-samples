// Copyright 2025 Google LLC
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.advancedmaps3dsamples.BuildConfig
import com.example.advancedmaps3dsamples.ui.theme.AdvancedMaps3DSamplesTheme
import com.example.advancedmaps3dsamples.utils.awaitCameraAnimation
import com.example.advancedmaps3dsamples.utils.calculateHeading
import com.example.advancedmaps3dsamples.utils.haversineDistance
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.Map3DOptions
import com.google.android.gms.maps3d.Map3DView
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback
import com.google.android.gms.maps3d.model.Polyline
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.flyToOptions
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.gms.maps3d.model.polylineOptions
import dagger.hilt.android.AndroidEntryPoint
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

            AdvancedMaps3DSamplesTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.primary,
                            ),
                            title = {
                                Text("Routes API Integration")
                            }
                        )
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        
                        // The Map3D View wrapper
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { context ->
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
                            },
                            update = { view ->
                                view.getMap3DViewAsync(object : OnMap3DViewReadyCallback {
                                    override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
                                        map3D = googleMap3D
                                    }
                                    override fun onError(e: Exception) {
                                        // Simple error log
                                    }
                                })
                            },
                            onRelease = { view ->
                                // Optional cleanup
                            }
                        )

                        // Floating Action Button to trigger the route fetch
                        Button(
                            onClick = {
                                // Hardcoded parameters as requested (Honolulu to Kailua)
                                val origin = LatLng(21.307043, -157.858984)
                                val dest = LatLng(21.390177, -157.719454)
                                viewModel.fetchRoute(BuildConfig.MAPS_API_KEY, origin, dest)
                            },
                            modifier = Modifier
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
                                            // 1. Merge waypoints that are too close to avoid "stuttering" at start/end
                                            val thresholdMeters = 200.0
                                            val rawPoints = state.navigationPoints
                                            val turnPoints = mutableListOf<LatLng>()
                                            
                                            if (rawPoints.isNotEmpty()) {
                                                turnPoints.add(rawPoints.first())
                                                for (i in 1 until rawPoints.size - 1) {
                                                    if (haversineDistance(turnPoints.last(), rawPoints[i]) >= thresholdMeters) {
                                                        turnPoints.add(rawPoints[i])
                                                    }
                                                }
                                                // Always include the actual destination
                                                if (haversineDistance(turnPoints.last(), rawPoints.last()) > 10.0) {
                                                    turnPoints.add(rawPoints.last())
                                                }
                                            }

                                            // 2. Inject intermediate points from the raw polyline for long stretches
                                            // This ensures the camera follows highway curves rather than flying in a straight line.
                                            val finalFlightPath = mutableListOf<LatLng>()
                                            if (turnPoints.isNotEmpty()) {
                                                finalFlightPath.add(turnPoints.first())
                                                for (i in 0 until turnPoints.size - 1) {
                                                    val start = turnPoints[i]
                                                    val end = turnPoints[i + 1]
                                                    
                                                    if (haversineDistance(start, end) > 500.0) {
                                                        // Find indices in raw polyline to pull curve details
                                                        val startIndex = state.decodedPolyline.indexOfFirst { haversineDistance(it, start) < 20.0 }
                                                        val endIndex = state.decodedPolyline.indexOfFirst { haversineDistance(it, end) < 20.0 }
                                                        
                                                        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                                                            for (j in (startIndex + 1) until endIndex) {
                                                                val p = state.decodedPolyline[j]
                                                                // Subsample for smooth camera motion
                                                                if (haversineDistance(finalFlightPath.last(), p) > 400.0) {
                                                                    finalFlightPath.add(p)
                                                                }
                                                            }
                                                        }
                                                    }
                                                    if (haversineDistance(finalFlightPath.last(), end) > 10.0) {
                                                        finalFlightPath.add(end)
                                                    }
                                                }
                                            }

                                            // 3. Fly along the curve-aware path
                                            val targetSpeedMetersPerSecond = 750.0
                                            
                                            for (i in 0 until finalFlightPath.size - 1) {
                                                val current = finalFlightPath[i]
                                                val next = finalFlightPath[i + 1]
                                                
                                                // Calculate heading towards the next point
                                                val flightHeading = calculateHeading(current, next)
                                                
                                                // Calculate distance to determine duration
                                                val distanceMeters = haversineDistance(current, next)
                                                
                                                // Duration = distance / speed
                                                // Clamp duration to [100ms, 10000ms]
                                                val calculatedDuration = (distanceMeters / targetSpeedMetersPerSecond * 1000).toLong()
                                                val segmentDuration = calculatedDuration.coerceIn(250, 2000)
                                                
                                                val flightCamera = camera {
                                                    center = latLngAltitude {
                                                        latitude = current.latitude
                                                        longitude = current.longitude
                                                        altitude = 0.0
                                                    }
                                                    heading = flightHeading
                                                    tilt = 50.0
                                                    range = 3000.0
                                                }
                                                
                                                // Animate to this segment's view
                                                safeMap.flyCameraTo(
                                                    flyToOptions {
                                                        endCamera = flightCamera
                                                        durationInMillis = segmentDuration
                                                    }
                                                )
                                                
                                                // Wait for this segment to finish
                                                safeMap.awaitCameraAnimation()
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
                                Box(modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(32.dp)) {
                                    Text(
                                        text = state.message,
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            RouteUiState.Idle -> { /* Do nothing */ }
                        }

                        if (displayWarning) {
                            AlertDialog(
                                onDismissRequest = { displayWarning = false },
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
                                    TextButton(onClick = { displayWarning = false }) {
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
