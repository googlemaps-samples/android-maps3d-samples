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

package com.example.composedemos.cloudstyling

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps3d.Map3DInitConfig
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.maps.android.compose3d.GoogleMap3D

class CloudStylingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    CloudStylingScreen()
                }
            }
        }
    }
}

@Composable
fun CloudStylingScreen() {
    val initialLocation = LatLng(37.7915, -122.4010)
    val currentCameraState by remember {
        mutableStateOf(
            camera {
                center = latLngAltitude {
                    latitude = initialLocation.latitude
                    longitude = initialLocation.longitude
                    altitude = 250.0
                }
                heading = 45.0
                tilt = 65.0
                range = 800.0
            },
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap3D(
            camera = currentCameraState,
            mapMode = Map3DMode.ROADMAP,
            options = Map3DInitConfig.create(
                centerLat = initialLocation.latitude,
                centerLng = initialLocation.longitude,
                centerAlt = 0.0,
                heading = 45.0,
                tilt = 65.0,
                roll = 0.0,
                range = 800.0,
                minAltitude = 0.0,
                maxAltitude = 1000000.0,
                minHeading = 0.0,
                maxHeading = 360.0,
                minTilt = 0.0,
                maxTilt = 90.0,
                bounds = null,
                mapMode = Map3DMode.ROADMAP,
                mapId = "9a35234a36da44d2c47bf626",
                language = java.util.Locale.getDefault().language,
                region = java.util.Locale.getDefault().country,
            ),
            modifier = Modifier.fillMaxSize(),
        )
    }
}
