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

package com.example.snippets.kotlin.snippets

import android.graphics.Color
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.gms.maps3d.model.polylineOptions
import com.example.snippets.kotlin.annotations.SnippetGroup
import com.example.snippets.kotlin.annotations.SnippetItem

@SnippetGroup(
    title = "Polylines",
    description = "Snippets demonstrating 2D and 3D extruded polyline paths on the map."
)
class PolylineSnippets(private val map: GoogleMap3D) {

    /**
     * Adds a basic polyline to the map.
     */
    @Suppress("unused")
    @SnippetItem(
        title = "Basic",
        description = "Draws a thick red polyline connecting three points near Lat: 37.42, Lng: -122.08."
    )
    fun addBasicPolyline() {
        // [START maps_android_3d_polyline_add_kt]
        val points = listOf(
            latLngAltitude { latitude = 37.42; longitude = -122.08; altitude = 0.0 },
            latLngAltitude { latitude = 37.43; longitude = -122.09; altitude = 0.0 },
            latLngAltitude { latitude = 37.44; longitude = -122.08; altitude = 0.0 }
        )

        val options = polylineOptions {
            path = points
            strokeColor = Color.RED
            strokeWidth = 10.0
            altitudeMode = AltitudeMode.CLAMP_TO_GROUND
        }
        
        val polyline = map.addPolyline(options)
        // [END maps_android_3d_polyline_add_kt]
    }

    /**
     * Adds a styled polyline with complex configuration.
     */
    @Suppress("unused")
    @SnippetItem(
        title = "Styled",
        description = "Draws a magenta polyline with a green outline, extruded and following the ground curvature (geodesic), connecting two points."
    )
    fun addStyledPolyline() {
        // [START maps_android_3d_polyline_options_kt]
        val points = listOf(
            latLngAltitude { latitude = 37.42; longitude = -122.08; altitude = 50.0 },
            latLngAltitude { latitude = 37.43; longitude = -122.09; altitude = 100.0 }
        )

        val options = polylineOptions {
            path = points
            strokeColor = 0xFFFF00FF.toInt() // Magenta
            strokeWidth = 20.0
            outerColor = 0xFF00FF00.toInt() // Green
            outerWidth = 2.0
            altitudeMode = AltitudeMode.RELATIVE_TO_GROUND
            extruded = true
            geodesic = true
            drawsOccludedSegments = true
        }
        
        val polyline = map.addPolyline(options)
        // [END maps_android_3d_polyline_options_kt]
    }
}
