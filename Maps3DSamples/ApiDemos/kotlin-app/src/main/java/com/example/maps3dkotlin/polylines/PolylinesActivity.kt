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
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.maps3dkotlin.sampleactivity.SampleBaseActivity
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.Polyline
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.gms.maps3d.model.polylineOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * This activity demonstrates the use of polylines on a 3D map. It visualizes the
 * Sanitas Loop hiking trail in Boulder, Colorado, by drawing a line that follows the
 * terrain.
 *
 * To create a visually appealing stroked effect, two polylines are used:
 * 1.  A wider, semi-transparent black polyline that serves as the background or "stroke".
 * 2.  A narrower, opaque red polyline that represents the trail itself, drawn on top of
 *     the background.
 *
 * A click listener is attached to the foreground polyline, which displays a toast message
 * when the trail is tapped.
 */
class PolylinesActivity : SampleBaseActivity() {
    override val TAG = this::class.java.simpleName

    // The initial camera is positioned to provide a clear view of the Sanitas Loop trail.
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

    // The options for the foreground (red) polyline are defined here. This includes its
    // path, color, width, and altitude mode.
    private val trailForegroundPolylineOptions by lazy {
        polylineOptions {
            path = trailLocations
            strokeColor = Color.RED
            strokeWidth = 7.0
            altitudeMode = AltitudeMode.CLAMP_TO_GROUND
            zIndex = 5
            drawsOccludedSegments = true
        }
    }

    // The options for the background (black) polyline are defined here. This polyline is
    // wider and has a lower zIndex, so it appears as a stroke behind the foreground.
    private val trailBackgroundPolylineOptions by lazy {
        polylineOptions {
            path = trailLocations
            strokeColor = Color.argb(128, 0, 0, 0) // Semi-transparent black
            strokeWidth = 13.0
            altitudeMode = AltitudeMode.CLAMP_TO_GROUND
            zIndex = 3
            drawsOccludedSegments = true
        }
    }

    override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
        super.onMap3DViewReady(googleMap3D)
        googleMap3D.setMapMode(Map3DMode.HYBRID)

        // The polylines are added to the map within a lifecycle-aware coroutine.
        lifecycleScope.launch {
            delay(1.milliseconds)
            addPolylines(googleMap3D)
        }
    }

    /**
     * Adds the foreground and background polylines to the map and sets up the click listener.
     *
     * @param googleMap3D The map object to which the polylines will be added.
     */
    private fun addPolylines(googleMap3D: GoogleMap3D) {
        googleMap3D.addPolyline(trailBackgroundPolylineOptions)
        val foregroundPolyline = googleMap3D.addPolyline(trailForegroundPolylineOptions)
        setupPolylineClickListener(foregroundPolyline)
    }

    /**
     * Sets up a click listener for the given polyline.
     *
     * @param polyline The polyline to which the click listener will be attached.
     */
    private fun setupPolylineClickListener(polyline: Polyline) {
        polyline.setClickListener {
            Log.d(TAG, "Clicked on trail polyline")
            lifecycleScope.launch {
                Toast.makeText(this@PolylinesActivity, "Hiking time!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private val TAG = PolylinesActivity::class.java.simpleName
        private const val BOULDER_LATITUDE = 40.029349
        private const val BOULDER_LONGITUDE = -105.300354

        // The trail data is parsed from a multiline string and transformed into a list of
        // LatLngAltitude objects. This is done lazily to avoid performing the work until
        // it's actually needed.
        private val trailLocations by lazy {
            sanitasLoop.lines().map {
                val (lat, lng) = it.split(",").map(String::toDouble)
                latLngAltitude {
                    latitude = lat
                    longitude = lng
                    altitude = 0.0  // The trail will be clamped to the ground
                }
            }
        }
    }
}