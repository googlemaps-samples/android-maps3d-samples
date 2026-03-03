/*
 * Copyright 2025 Google LLC
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
import com.google.android.gms.maps3d.model.polygonOptions

class PolygonSnippets(private val map: GoogleMap3D) {

    // [START maps_android_3d_polygon_add_kt]
    /**
     * Adds a simple polygon to the map.
     */
    fun addBasicPolygon() {
        val points = listOf(
            latLngAltitude { latitude = 37.42; longitude = -122.08; altitude = 0.0 },
            latLngAltitude { latitude = 37.42; longitude = -122.09; altitude = 0.0 },
            latLngAltitude { latitude = 37.43; longitude = -122.09; altitude = 0.0 },
            latLngAltitude { latitude = 37.43; longitude = -122.08; altitude = 0.0 },
            latLngAltitude { latitude = 37.42; longitude = -122.08; altitude = 0.0 }
        )

        val options = polygonOptions {
            path = points
            fillColor = Color.RED
            strokeColor = Color.BLUE
            strokeWidth = 5.0
            altitudeMode = AltitudeMode.CLAMP_TO_GROUND
        }
        
        val polygon = map.addPolygon(options)
    }
    // [END maps_android_3d_polygon_add_kt]

    // [START maps_android_3d_polygon_extruded_kt]
    /**
     * Adds an extruded polygon with transparency.
     */
    fun addExtrudedPolygon() {
        val points = listOf(
            latLngAltitude { latitude = 37.42; longitude = -122.08; altitude = 50.0 },
            latLngAltitude { latitude = 37.42; longitude = -122.09; altitude = 50.0 },
            latLngAltitude { latitude = 37.43; longitude = -122.09; altitude = 50.0 },
            latLngAltitude { latitude = 37.43; longitude = -122.08; altitude = 50.0 },
            latLngAltitude { latitude = 37.42; longitude = -122.08; altitude = 50.0 }
        )

        val options = polygonOptions {
            path = points
            fillColor = 0x88FF0000.toInt() // Semi-transparent red
            extruded = true
            geodesic = true
            altitudeMode = AltitudeMode.RELATIVE_TO_GROUND
        }
        
        val polygon = map.addPolygon(options)
    }
    // [END maps_android_3d_polygon_extruded_kt]
}
