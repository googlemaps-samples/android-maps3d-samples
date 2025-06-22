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
import com.example.maps3dkotlin.cameracontrols.extrudePolygon
import com.example.maps3d.common.miles
import com.example.maps3d.common.toMeters
import com.example.maps3d.common.toValidCamera
import com.example.maps3dkotlin.sampleactivity.SampleBaseActivity
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.Hole
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.gms.maps3d.model.polygonOptions

class PolygonsActivity : SampleBaseActivity() {
    override val TAG = this::class.java.simpleName

    override val initialCamera: Camera = camera {
        center = latLngAltitude {
            latitude = DENVER_LATITUDE
            longitude = DENVER_LONGITUDE
            altitude = 1.miles.toMeters
        }
        heading = -68.0
        tilt = 47.0
        range = 2251.0
    }.toValidCamera()  // Ensure the camera settings are within valid bounds

    private val faceFillColor = Color.argb(70, 255, 0, 255) // Semi-transparent magenta
    private val faceStrokeColor = Color.MAGENTA
    private val faceStrokeWidth = 3.0

    private val extrudedMuseum = extrudePolygon(museumBaseFace, 50.0).map { outline ->
        polygonOptions {
            outerCoordinates = outline
            fillColor = faceFillColor
            strokeColor = faceStrokeColor
            strokeWidth = faceStrokeWidth
            altitudeMode = AltitudeMode.ABSOLUTE
            geodesic = false
            drawsOccludedSegments = true
        }
    }

    override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
        super.onMap3DViewReady(googleMap3D)
        googleMap3D.setMapMode(Map3DMode.HYBRID)

        // Add extruded polygons to the map.  The returned list of polygons can be used to remove them at a later time.
        val museumPolygons = extrudedMuseum.map { face ->
            googleMap3D.addPolygon(face)
        }

        // Add a zoo polygon to the map.
        val zooPolygon = googleMap3D.addPolygon(zooPolygonOptions)
    }

    companion object {
        private const val DENVER_LATITUDE = 39.748477
        private const val DENVER_LONGITUDE = -104.947575

        private val museumAltitude = 1.miles.toMeters

        private val museumBaseFace = """
            39.74812392425406, -104.94414971628434
            39.7465307929639, -104.94370889409778
            39.747031745033794, -104.9415078562927
            39.74837320615968, -104.94194414397013
            39.74812392425406, -104.94414971628434 
        """.trimIndent()
            .split("\n")
            .map { line -> line.split(",").map { it.trim().toDouble() } }
            .map { coords ->
                latLngAltitude {
                    latitude = coords[0]; longitude = coords[1]; altitude = museumAltitude
                }
            }

        private val zooHole = """
                39.7498, -104.9535
                39.7498, -104.9525
                39.7488, -104.9525
                39.7488, -104.9535
                39.7498, -104.9535""".trimIndent()
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


        private val zooOutline = """
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
            39.7508987, -104.9565381""".trimIndent()
            .split("\n")
            .map { line -> line.split(",").map { it.trim().toDouble() } }
            .map { coords ->
                latLngAltitude { latitude = coords[0]; longitude = coords[1]; altitude = 0.0 }
            }

        val zooPolygonOptions = polygonOptions {
            outerCoordinates = zooOutline
            innerCoordinates = listOf(zooHole)
            fillColor = Color.argb(70, 255, 255, 0)
            strokeColor = Color.GREEN
            strokeWidth = 3.0
            altitudeMode = AltitudeMode.CLAMP_TO_GROUND
        }
    }
}
