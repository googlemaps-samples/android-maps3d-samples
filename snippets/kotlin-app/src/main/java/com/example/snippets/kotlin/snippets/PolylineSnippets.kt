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

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.snippets.kotlin.TrackedMap3D
import com.example.snippets.kotlin.annotations.SnippetGroup
import com.example.snippets.kotlin.annotations.SnippetItem
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.flyToOptions
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.gms.maps3d.model.polylineOptions

@SnippetGroup(
    title = "Polylines",
    description = "Snippets demonstrating 2D and 3D extruded polyline paths on the map.",
)
class PolylineSnippets(private val context: Context, private val map: TrackedMap3D) {

    /**
     * Adds a basic polyline to the map.
     */
    @Suppress("unused")
    @SnippetItem(
        title = "1. Basic",
        description = "Draws a thick red polyline connecting three points",
    )
    fun addBasicPolyline() {
        // [START maps_android_3d_polyline_add_kt]
        val points = listOf(
            latLngAltitude {
                latitude = 37.42
                longitude = -122.08
                altitude = 0.0
            },
            latLngAltitude {
                latitude = 37.43
                longitude = -122.09
                altitude = 0.0
            },
            latLngAltitude {
                latitude = 37.44
                longitude = -122.08
                altitude = 0.0
            },
        )

        val options = polylineOptions {
            path = points
            strokeColor = Color.RED
            strokeWidth = 10.0
            altitudeMode = AltitudeMode.CLAMP_TO_GROUND
        }

        val polyline = map.addPolyline(options)
        // [START_EXCLUDE]
        polyline?.setClickListener {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "Polyline Clicked!", Toast.LENGTH_SHORT).show()
            }
        }
        // [END_EXCLUDE]
        // [END maps_android_3d_polyline_add_kt]

        map.flyCameraTo(
            flyToOptions {
                endCamera = camera {
                    center = latLngAltitude {
                        latitude = 37.43
                        longitude = -122.085
                        altitude = 0.0
                    }
                    tilt = 45.0
                    heading = 0.0
                    range = 5000.0
                }
                durationInMillis = 1000
            },
        )
    }

    /**
     * Adds a styled polyline with complex configuration.
     */
    @Suppress("unused")
    @SnippetItem(
        title = "2. Styled",
        description = "Draws a magenta polyline with a green outline, extruded and following the ground curvature (geodesic), connecting two points.",
    )
    fun addStyledPolyline() {
        // [START maps_android_3d_polyline_options_kt]
        val points = listOf(
            latLngAltitude {
                latitude = 37.42
                longitude = -122.08
                altitude = 50.0
            },
            latLngAltitude {
                latitude = 37.43
                longitude = -122.09
                altitude = 100.0
            },
        )

        val options = polylineOptions {
            path = points
            strokeColor = 0xFFFF00FF.toInt() // Magenta
            strokeWidth = 2.0
            outerColor = 0xFF00FF00.toInt() // Green
            outerWidth = 1.0
            altitudeMode = AltitudeMode.RELATIVE_TO_GROUND
            extruded = true
            geodesic = true
            drawsOccludedSegments = true
        }

        val polyline = map.addPolyline(options)
        // [START_EXCLUDE]
        polyline?.setClickListener {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "Polyline Clicked!", Toast.LENGTH_SHORT).show()
            }
        }
        // [END_EXCLUDE]
        // [END maps_android_3d_polyline_options_kt]

        map.flyCameraTo(
            flyToOptions {
                endCamera = camera {
                    center = latLngAltitude {
                        latitude = 37.425
                        longitude = -122.085
                        altitude = 0.0
                    }
                    tilt = 45.0
                    heading = 0.0
                    range = 4000.0
                }
                durationInMillis = 1000
            },
        )
    }
}
