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

package com.example.maps3dkotlin.polylines

import android.graphics.Color
import com.example.maps3dkotlin.sampleactivity.SampleBaseActivity
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.Map3DView
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.gms.maps3d.model.polylineOptions

/**
 * Activity that demonstrates the use of polylines on a 3D map.
 *
 * This activity displays a polyline representing a trail (Sanitas Loop) on a
 * [Map3DView]. It uses two polylines to create a stroked effect, with a
 * wider, slightly translucent black polyline behind a narrower red polyline.
 * It also includes buttons to take a snapshot of the map and reset the view to the initial camera position.
 *
 * The activity implements [OnMap3DViewReadyCallback] to receive a callback when the
 * [Map3DView] is ready to be used. It also handles the lifecycle events of the
 * [Map3DView] to ensure proper resource management.
 */
class PolylinesActivity : SampleBaseActivity() {
    override val TAG = this::class.java.simpleName
    override val initialCamera = camera {
        center = latLngAltitude {
            latitude = BOULDER_LATITUDE
            longitude = BOULDER_LONGITUDE
            altitude = 1833.9
        }
        heading = 326.0
        tilt = 75.0
        range = 3757.0
    }

    private val trailForegroundPolylineOptions = polylineOptions {
        coordinates = trailLocations
        strokeColor = Color.RED
        strokeWidth = 7.0
        altitudeMode = AltitudeMode.CLAMP_TO_GROUND
        zIndex = 5
        drawsOccludedSegments = true
    }

    private val trailBackground = Color.argb(128, 0, 0, 0)

    private val trailBackgroundPolylineOptions = polylineOptions {
        coordinates = trailLocations
        strokeColor = trailBackground
        strokeWidth = 13.0
        altitudeMode = AltitudeMode.CLAMP_TO_GROUND
        zIndex = 3
        drawsOccludedSegments = true
    }

    override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
        super.onMap3DViewReady(googleMap3D)
        googleMap3D.setMapMode(Map3DMode.HYBRID)

        googleMap3D.addPolyline(trailBackgroundPolylineOptions)
        googleMap3D.addPolyline(trailForegroundPolylineOptions)
    }

    companion object {
        private const val BOULDER_LATITUDE = 40.029349
        private const val BOULDER_LONGITUDE = -105.300354

        private val trailLocations = sanitasLoop.split("\n").map {
            val (lat, lng) = it.split(",")
            latLngAltitude {
                latitude = lat.toDouble()
                longitude = lng.toDouble()
                altitude = 0.0  // The trail will be clamped to the ground
            }
        }
    }
}
