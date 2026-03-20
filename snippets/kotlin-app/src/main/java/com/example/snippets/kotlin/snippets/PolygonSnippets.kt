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
import com.google.android.gms.maps3d.model.Hole
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.flyToOptions
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.gms.maps3d.model.polygonOptions

@SnippetGroup(
    title = "Polygons",
    description = "Snippets demonstrating 2D and 3D extruded polygon layers on the map."
)
class PolygonSnippets(private val context: Context, private val map: TrackedMap3D) {

    /**
     * Adds a simple polygon to the map.
     */
    @Suppress("unused")
    @SnippetItem(
        title = "1. Basic",
        description = "Draws a red polygon with a blue stroke around a small area"
    )
    fun addBasicPolygon() {
        // [START maps_android_3d_polygon_add_kt]
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
        // [START_EXCLUDE]
        polygon?.setClickListener {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "Polygon Clicked!", Toast.LENGTH_SHORT).show()
            }
        }
        // [END_EXCLUDE]
        // [END maps_android_3d_polygon_add_kt]

        map.flyCameraTo(flyToOptions {
            endCamera = camera {
                center = latLngAltitude {
                    latitude = 37.424968
                    longitude = -122.084874
                    altitude = 19.90
                }
                tilt = 45.02
                heading = 0.0
                range = 4643.0
            }
            durationInMillis = 1000
        })
    }

    /**
     * Adds an extruded polygon with transparency.
     */
    @Suppress("unused")
    @SnippetItem(
        title = "2. Extruded",
        description = "Draws a semi-transparent red extruded polygon (height 50m) around a small area"
    )
    fun addExtrudedPolygon() {
        // [START maps_android_3d_polygon_extruded_kt]
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
        // [START_EXCLUDE]
        polygon?.setClickListener {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "Polygon Clicked!", Toast.LENGTH_SHORT).show()
            }
        }
        // [END_EXCLUDE]
        // [END maps_android_3d_polygon_extruded_kt]

        map.flyCameraTo(flyToOptions {
            endCamera = camera {
                center = latLngAltitude {
                    latitude = 37.424968
                    longitude = -122.084874
                    altitude = 19.90
                }
                tilt = 45.02
                heading = 0.0
                range = 4643.0
            }
            durationInMillis = 1000
        })
    }

    /**
     * Adds a polygon with a hole cutout.
     */
    @Suppress("unused")
    @SnippetItem(
        title = "3. Polygon with Hole",
        description = "Draws a polygon with an interior hole cutout."
    )
    fun addPolygonWithHole() {
        // [START maps_android_3d_polygon_hole_kt]
        val outerPoints = listOf(
            latLngAltitude { latitude = 37.422; longitude = -122.084; altitude = 0.0 },
            latLngAltitude { latitude = 37.422; longitude = -122.086; altitude = 0.0 },
            latLngAltitude { latitude = 37.424; longitude = -122.086; altitude = 0.0 },
            latLngAltitude { latitude = 37.424; longitude = -122.084; altitude = 0.0 },
            latLngAltitude { latitude = 37.422; longitude = -122.084; altitude = 0.0 }
        )

        val innerPoints = listOf(
            latLngAltitude { latitude = 37.4225; longitude = -122.0845; altitude = 0.0 },
            latLngAltitude { latitude = 37.4225; longitude = -122.0855; altitude = 0.0 },
            latLngAltitude { latitude = 37.4235; longitude = -122.0855; altitude = 0.0 },
            latLngAltitude { latitude = 37.4235; longitude = -122.0845; altitude = 0.0 },
            latLngAltitude { latitude = 37.4225; longitude = -122.0845; altitude = 0.0 }
        )

        val options = polygonOptions {
            path = outerPoints
            fillColor = Color.GREEN
            // Inner paths is optional, wrap inner vertices with Hole
            innerPaths = listOf(Hole(innerPoints))
            altitudeMode = AltitudeMode.CLAMP_TO_GROUND
        }

        val polygon = map.addPolygon(options)
        // [START_EXCLUDE]
        polygon?.setClickListener {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "Polygon Clicked!", Toast.LENGTH_SHORT).show()
            }
        }
        // [END_EXCLUDE]
        // [END maps_android_3d_polygon_hole_kt]

        map.flyCameraTo(flyToOptions {
            endCamera = camera {
                center = latLngAltitude { latitude = 37.423600; longitude = -122.085098; altitude = 4.31 }
                tilt = 45.00; heading = 0.00; range = 1085.51; roll = 0.00
            }
            durationInMillis = 1000
        })
    }
}
