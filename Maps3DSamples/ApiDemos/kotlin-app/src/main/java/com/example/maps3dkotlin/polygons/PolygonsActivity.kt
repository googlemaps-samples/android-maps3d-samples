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

package com.example.maps3dkotlin.polygons

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.maps3d.common.miles
import com.example.maps3d.common.toMeters
import com.example.maps3d.common.toValidCamera
import com.example.maps3dkotlin.cameracontrols.extrudePolygon
import com.example.maps3dkotlin.sampleactivity.SampleBaseActivity
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.Hole
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.Polygon
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.flyToOptions
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.gms.maps3d.model.polygonOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * This activity demonstrates how to create and display polygons on a 3D map. It showcases
 * two types of polygons:
 *
 * 1.  An extruded polygon representing the Denver Museum of Nature & Science. This polygon is
 *     given a height to appear as a 3D object on the map.
 * 2.  A flat polygon representing the Denver Zoo, which includes a hole within it. This
 *     demonstrates how to create more complex shapes.
 *
 * The activity also includes click listeners for the polygons, displaying a toast message
 * when a polygon is tapped.
 */
class PolygonsActivity : SampleBaseActivity() {
    override val TAG = this::class.java.simpleName

    // The initial camera position is defined declaratively, providing a clear starting point
    // for the map's view. The camera is positioned to overlook the Denver area.
    override val initialCamera: Camera = camera {
        center = latLngAltitude {
            latitude = DENVER_LATITUDE
            longitude = DENVER_LONGITUDE
            altitude = 1.miles.toMeters
        }
        heading = -68.0
        tilt = 47.0
        range = 2251.0
    }.toValidCamera()  // Ensures the camera settings are within valid bounds

    // The properties for the museum polygon's appearance are defined here. This makes it
    // easy to modify the style of the polygon without searching through the code.
    private val faceFillColor = Color.argb(70, 255, 0, 255) // Semi-transparent magenta
    private val faceStrokeColor = Color.MAGENTA
    private val faceStrokeWidth = 3.0

    // The extruded museum polygon is created by taking a base shape and giving it a height.
    // The result is a list of polygon options, one for each face of the extruded shape.
    private val extrudedMuseum by lazy {
        extrudePolygon(museumBaseFace, 50.0).map { outline ->
            polygonOptions {
                path = outline
                fillColor = faceFillColor
                strokeColor = faceStrokeColor
                strokeWidth = faceStrokeWidth
                altitudeMode = AltitudeMode.ABSOLUTE
                geodesic = false
                drawsOccludedSegments = true
            }
        }
    }

    override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
        super.onMap3DViewReady(googleMap3D)
        googleMap3D.setMapMode(Map3DMode.HYBRID)

        // A coroutine is launched in the lifecycleScope to add the polygons to the map.
        // This ensures that the coroutine is automatically canceled when the activity is
        // destroyed, preventing potential memory leaks.
        lifecycleScope.launch {
            kotlinx.coroutines.delay(1.milliseconds)

            // The camera is animated to the initial position.
            googleMap3D.flyCameraTo(
                flyToOptions {
                    endCamera = initialCamera
                    durationInMillis = 1_000
                }
            )

            // The polygons for the museum and the zoo are added to the map.
            addMuseumPolygons(googleMap3D)
            addZooPolygon(googleMap3D)
        }
    }

    /**
     * Adds the extruded polygons for the museum to the map and sets up their click listeners.
     *
     * @param googleMap3D The map object to which the polygons will be added.
     */
    private fun addMuseumPolygons(googleMap3D: GoogleMap3D) {
        val museumPolygons = extrudedMuseum.map { face ->
            googleMap3D.addPolygon(face)
        }
        setupPolygonClickListener(
            polygons = museumPolygons,
            message = "Check out the Museum!",
            logMessage = "Clicked on museum polygon"
        )
    }

    /**
     * Adds the polygon for the zoo to the map and sets up its click listener.
     *
     * @param googleMap3D The map object to which the polygon will be added.
     */
    private fun addZooPolygon(googleMap3D: GoogleMap3D) {
        val zooPolygon = googleMap3D.addPolygon(zooPolygonOptions)
        setupPolygonClickListener(
            polygons = listOf(zooPolygon),
            message = "Zoo time",
            logMessage = "Clicked on zoo polygon"
        )
    }

    /**
     * Sets up a click listener for a list of polygons. When any of the polygons are clicked,
     * a toast message is displayed.
     *
     * @param polygons The list of polygons to which the click listener will be attached.
     * @param message The message to be displayed in the toast.
     * @param logMessage The message to be logged when a polygon is clicked.
     */
    private fun setupPolygonClickListener(
        polygons: List<Polygon>,
        message: String,
        logMessage: String
    ) {
        polygons.forEach { polygon ->
            polygon.setClickListener {
                Log.d(TAG, logMessage)
                lifecycleScope.launch(Dispatchers.Main) {
                    Toast.makeText(this@PolygonsActivity, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private val TAG = PolygonsActivity::class.java.simpleName
        private const val DENVER_LATITUDE = 39.748477
        private const val DENVER_LONGITUDE = -104.947575

        private val museumAltitude = 1.miles.toMeters

        // The base of the museum is defined as a list of coordinates. This list is then
        // transformed into a list of LatLngAltitude objects.
        private val museumBaseFace by lazy {
            """
            39.74812392425406, -104.94414971628434
            39.7465307929639, -104.94370889409778
            39.747031745033794, -104.9415078562927
            39.74837320615968, -104.94194414397013
            39.74812392425406, -104.94414971628434 
            """.trimIndent()
                .lines()
                .map { line -> line.split(",").map { it.trim().toDouble() } }
                .map { (lat, lng) ->
                    latLngAltitude {
                        latitude = lat
                        longitude = lng
                        altitude = museumAltitude
                    }
                }
        }

        // A hole is defined for the zoo polygon. This is a separate list of coordinates
        // that will be rendered as a hole within the main polygon.
        private val zooHole by lazy {
            """
                39.7498, -104.9535
                39.7498, -104.9525
                39.7488, -104.9525
                39.7488, -104.9535
                39.7498, -104.9535
            """.trimIndent()
                .lines()
                .map { it.split(",").map(String::trim).map(String::toDouble) }
                .map { (lat, lng) ->
                    latLngAltitude {
                        latitude = lat
                        longitude = lng
                        altitude = 0.0
                    }
                }
                .let { Hole(it) }
        }

        // The outline of the zoo is defined as a list of coordinates.
        private val zooOutline by lazy {
            """
            39.7508987, -104.9565381
            39.7502883, -104.9565489
            39.7501976, -104.9563557
            39.7501481, -104.955594
            39.7499171, -104.9553043
            39.7495872, -104.9551648
            39.7492407, -104.954961
            39.7489685, -104.9548859
            39.7484488, -104.9548966
            39.7481189, -104.9548859
            39.7479539, -104.9547679
            39.7479209, -104.9544567
            39.7476487, -104.9535341
            39.7475085, -104.9525792
            39.7474095, -104.9519247
            39.747525, -104.9513776
            39.7476734, -104.9511844
            39.7478137, -104.9506265
            39.7477559, -104.9496395
            39.7477477, -104.9486203
            39.7478467, -104.9475796
            39.7482344, -104.9465818
            39.7486138, -104.9457878
            39.7491005, -104.9454874
            39.7495789, -104.945938
            39.7500491, -104.9466998
            39.7503213, -104.9474615
            39.7505358, -104.9486954
            39.7505111, -104.950648
            39.7511215, -104.9506587
            39.7511173, -104.9527187
            39.7511091, -104.9546445
            39.7508987, -104.9565381
            """.trimIndent()
                .lines()
                .map { line -> line.split(",").map { it.trim().toDouble() } }
                .map { (lat, lng) ->
                    latLngAltitude {
                        latitude = lat
                        longitude = lng
                        altitude = 0.0
                    }
                }
        }

        // The options for the zoo polygon are defined here. This includes the outline,
        // the hole, and the styling.
        val zooPolygonOptions = polygonOptions {
            path = zooOutline
            innerPaths = listOf(zooHole)
            fillColor = Color.argb(70, 255, 255, 0)
            strokeColor = Color.GREEN
            strokeWidth = 3.0
            altitudeMode = AltitudeMode.CLAMP_TO_GROUND
        }
    }
}