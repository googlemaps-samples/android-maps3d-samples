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
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.ImageView
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import android.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps3d.model.markerOptions
import com.google.android.gms.maps3d.model.popoverOptions
import com.google.android.gms.maps3d.Popover
import com.google.android.gms.maps3d.GoogleMap3D as NativeGoogleMap3D
import com.google.maps.android.compose3d.GoogleMap3D
import com.google.maps.android.compose3d.MarkerConfig

class MarkersActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MarkersScreen()
                }
            }
        }
    }
}

@Composable
fun MarkersScreen() {
    var isMapSteady by remember { mutableStateOf(false) }

    // Camera centered on Devils Tower
    val devilsTowerCamera = remember {
        camera {
            center = latLngAltitude {
                latitude = 44.589994
                longitude = -104.715326
                altitude = 1508.9
            }
            heading = 1.0
            tilt = 75.0
            range = 1635.0
            roll = 0.0
        }
    }

    val context = LocalContext.current
    var activePopover by remember { mutableStateOf<Popover?>(null) }
    var googleMap3DInstance by remember { mutableStateOf<NativeGoogleMap3D?>(null) }
 
    val alienMarker = remember {
        MarkerConfig(
            key = "alien",
            position = latLngAltitude {
                latitude = 44.59054845363309
                longitude = -104.715177415273
                altitude = 10.0
            },
            altitudeMode = AltitudeMode.RELATIVE_TO_MESH,
            styleView = ImageView(R.drawable.alien),
            label = "Devil's Tower Alien",
            isExtruded = true,
            isDrawnWhenOccluded = true,
            onClick = { marker ->
                googleMap3DInstance?.let { map ->
                    val textView = android.widget.TextView(context).apply {
                        text = "They didn't just come to sculpt mashed potatoes."
                        setPadding(32, 16, 32, 16)
                        setTextColor(Color.BLACK)
                        setBackgroundColor(Color.WHITE)
                    }
                    val newPopover = map.addPopover(popoverOptions {
                        positionAnchor = marker
                        altitudeMode = AltitudeMode.ABSOLUTE
                        content = textView
                        autoCloseEnabled = true
                        autoPanEnabled = false
                    })
                    
                    activePopover?.remove()
                    activePopover = newPopover
                    activePopover?.show()
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = if (isMapSteady) "MapSteady" else "MapLoading" }
    ) {
        GoogleMap3D(
            camera = devilsTowerCamera,
            mapMode = Map3DMode.HYBRID,
            markers = listOf(alienMarker),
            modifier = Modifier.fillMaxSize(),
            onMapSteady = {
                isMapSteady = true
            },
            onMapReady = { instance ->
                googleMap3DInstance = instance
            }
        )


    }
}
