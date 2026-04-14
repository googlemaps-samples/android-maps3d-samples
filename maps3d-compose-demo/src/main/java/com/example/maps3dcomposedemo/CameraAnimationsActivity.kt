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

package com.example.maps3dcomposedemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.flyAroundOptions
import com.google.android.gms.maps3d.model.flyToOptions
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.maps.android.compose3d.GoogleMap3D
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class CameraAnimationsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CameraAnimationsScreen()
                }
            }
        }
    }
}

@Composable
fun CameraAnimationsScreen() {
    var mapInstance by remember { mutableStateOf<com.google.android.gms.maps3d.GoogleMap3D?>(null) }
    var statusText by remember { mutableStateOf("Idle") }
    val coroutineScope = rememberCoroutineScope()

    val newYork = latLngAltitude { latitude = 40.7128; longitude = -74.0060; altitude = 0.0 }
    val sf = latLngAltitude { latitude = 37.7749; longitude = -122.4194; altitude = 0.0 }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap3D(
            camera = camera {
                center = newYork
                range = 10000.0
                tilt = 45.0
            },
            modifier = Modifier.fillMaxSize(),
            onMapReady = { googleMap3D ->
                mapInstance = googleMap3D
                
                googleMap3D.setOnMapSteadyListener { isSteady ->
                    if (isSteady) {
                        statusText = "Steady"
                    }
                }
            }
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Status: $statusText", color = Color.White)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = {
                    mapInstance?.let { map ->
                        statusText = "Animating to SF"
                        map.flyCameraTo(
                            flyToOptions {
                                endCamera = camera {
                                    center = sf
                                    range = 5000.0
                                    tilt = 60.0
                                }
                                durationInMillis = 5000
                            }
                        )
                        coroutineScope.launch {
                            map.awaitCameraAnimation()
                            statusText = "Animation Ended (SF)"
                        }
                    }
                }) {
                    Text("Fly to SF")
                }

                Button(onClick = {
                    mapInstance?.let { map ->
                        statusText = "Orbiting"
                        map.flyCameraAround(
                            flyAroundOptions {
                                rounds = 1.0
                                durationInMillis = 10000
                            }
                        )
                        coroutineScope.launch {
                            map.awaitCameraAnimation()
                            statusText = "Orbit Ended"
                        }
                    }
                }) {
                    Text("Orbit")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = {
                    mapInstance?.let { map ->
                        map.stopCameraAnimation()
                        statusText = "Animation Stopped"
                    }
                }) {
                    Text("Stop")
                }
            }
        }
    }
}

// Helper extensions
suspend fun com.google.android.gms.maps3d.GoogleMap3D.awaitCameraAnimation() = suspendCancellableCoroutine { continuation ->
    setCameraAnimationEndListener {
        setCameraAnimationEndListener(null) // Cleanup
        if (continuation.isActive) {
            continuation.resume(Unit)
        }
    }

    continuation.invokeOnCancellation {
        setCameraAnimationEndListener(null)
    }
}
