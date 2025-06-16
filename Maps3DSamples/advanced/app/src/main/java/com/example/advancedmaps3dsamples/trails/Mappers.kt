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

package com.example.advancedmaps3dsamples.trails

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.android.gms.maps3d.model.PolylineOptions
import com.google.android.gms.maps3d.model.polylineOptions

fun Trail.toPolylineOptions() : PolylineOptions {
    val coordinates = this.coordinates.map { it.toLatLngAltitude(0.0) }

    val color = when (difficulty) {
        DifficultyLevel.EASY -> Color.Green
        DifficultyLevel.MODERATE -> Color.Blue
        DifficultyLevel.DIFFICULT -> Color.Red
    }.toArgb()

    return polylineOptions {
        id = this@toPolylineOptions.segmentId
        this.coordinates = coordinates
        strokeColor = color
        strokeWidth = 7.0
        altitudeMode = AltitudeMode.CLAMP_TO_GROUND
        zIndex = 5
        drawsOccludedSegments = true
    }
}

private fun LatLng.toLatLngAltitude(altitude: Double = 0.0) : LatLngAltitude =
    LatLngAltitude(this.latitude, this.longitude, altitude)
