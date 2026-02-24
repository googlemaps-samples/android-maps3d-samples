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

package com.example.maps3dkotlin.markers

import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.maps3dkotlin.sampleactivity.SampleBaseActivity
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.CollisionBehavior
import com.google.android.gms.maps3d.model.Glyph
import com.google.android.gms.maps3d.model.ImageView
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.Marker
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.gms.maps3d.model.markerOptions
import com.google.android.gms.maps3d.model.pinConfiguration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * This activity demonstrates the various altitude modes available for markers on a 3D map.
 * By showcasing four distinct markers, each with a different altitude setting, it provides a
 * clear, visual comparison of how each mode affects the marker's position relative to the
 * Earth's surface and 3D structures.
 *
 * The altitude modes illustrated are:
 * - **ABSOLUTE:** The marker is positioned at a precise altitude above sea level, irrespective
 *   of the terrain height below it.
 * - **RELATIVE_TO_GROUND:** The marker's altitude is measured from the ground level directly
 *   beneath it, making it useful for representing objects at a fixed height above the terrain.
 * - **CLAMP_TO_GROUND:** The marker is "draped" over the terrain, following its contours.
 *   Its altitude value is effectively ignored for rendering purposes.
 * - **RELATIVE_TO_MESH:** The marker's altitude is relative to the 3D mesh of the environment,
 *   including buildings and other structures. This is ideal for placing markers on or relative
 *   to 3D objects.
 */
class MarkersActivity : SampleBaseActivity() {
    override val TAG = MarkersActivity::class.java.simpleName

    val berlinCamera = camera {
        center = latLngAltitude {
            latitude = 52.51974795
            longitude = 13.40715553
            altitude = 150.0
        }
        heading = 252.7
        tilt = 79.0
        range = 1500.0
    }

    val nycCamera = camera {
        center = latLngAltitude {
            latitude = 40.748425
            longitude = -73.985590
            altitude = 348.7
        }
        heading = 22.0
        tilt = 80.0
        range = 1518.0
    }

    val tokyoCamera = camera {
        center = latLngAltitude {
            latitude = 35.658708
            longitude = 139.702206
            altitude = 23.3
        }
        heading = 117.0
        tilt = 55.0
        range = 2868.0
    }

    // The initial camera position is defined declaratively, providing a clear overview of
    // the starting view of the map. This makes it easy to understand and modify the initial
    // scene without digging into the logic of the activity.
    override val initialCamera = nycCamera

    override fun onMapReady(googleMap3D: GoogleMap3D) {
        super.onMapReady(googleMap3D)
        googleMap3D.setMapMode(Map3DMode.SATELLITE)

        findViewById<Button>(com.example.maps3dcommon.R.id.fly_berlin_button)?.apply {
            runOnUiThread {
                visibility = View.VISIBLE
            }
            setOnClickListener {
                googleMap3D.flyCameraTo(com.google.android.gms.maps3d.model.flyToOptions {
                    endCamera = berlinCamera
                    durationInMillis = 2_000
                })
            }
        }

        findViewById<Button>(com.example.maps3dcommon.R.id.fly_nyc_button)?.apply {
            runOnUiThread {
                visibility = View.VISIBLE
            }
            setOnClickListener {
                googleMap3D.flyCameraTo(com.google.android.gms.maps3d.model.flyToOptions {
                    endCamera = nycCamera
                    durationInMillis = 2_000
                })
            }
        }

        findViewById<Button>(com.example.maps3dcommon.R.id.fly_tokyo_button)?.apply {
            runOnUiThread {
                visibility = View.VISIBLE
            }
            setOnClickListener {
                googleMap3D.flyCameraTo(com.google.android.gms.maps3d.model.flyToOptions {
                    endCamera = tokyoCamera
                    durationInMillis = 2_000
                })
            }
        }

        lifecycleScope.launch(Dispatchers.Default) {
            addMarkers(googleMap3D)
        }
    }

    /**
     * Adds a series of markers to the map, each demonstrating a different altitude mode.
     * This function is designed to be called from a coroutine, as adding markers can be
     * a long-running operation.
     *
     * @param googleMap3D The map object to which the markers will be added.
     */
    private fun addMarkers(googleMap3D: GoogleMap3D) {
        Log.d(TAG, "addMarkers: start")

        // Marker 1: Absolute Altitude
        // This marker is placed at a fixed altitude of 150 meters above sea level.
        googleMap3D.addMarker(markerOptions {
            id = "marker_one"
            position = latLngAltitude {
                latitude = 52.519605780912585
                longitude = 13.406867190588198
                altitude = 150.0
            }
            label = "Absolute (150m)"
            altitudeMode = AltitudeMode.ABSOLUTE
            isExtruded = true
            isDrawnWhenOccluded = true
            collisionBehavior = CollisionBehavior.OPTIONAL_AND_HIDES_LOWER_PRIORITY
        })?.let(::setupMarkerClickListener)

        // Marker 2: Relative to Ground
        // This marker is positioned 50 meters above the ground.
        googleMap3D.addMarker(markerOptions {
            id = "relative_to_ground"
            position = latLngAltitude {
                latitude = 52.519882191069016
                longitude = 13.407410777254293
                altitude = 50.0
            }
            label = "Relative to Ground (50m)"
            altitudeMode = AltitudeMode.RELATIVE_TO_GROUND
            isExtruded = true
            isDrawnWhenOccluded = true
            collisionBehavior = CollisionBehavior.OPTIONAL_AND_HIDES_LOWER_PRIORITY
        })?.let(::setupMarkerClickListener)

        // Marker 3: Clamped to Ground
        // This marker is attached to the ground, with its altitude effectively being zero.
        googleMap3D.addMarker(markerOptions {
            id = "clamped_to_ground"
            position = latLngAltitude {
                latitude = 52.52027645136134
                longitude = 13.408271658592406
                altitude =
                    0.0  // altitude is effectively ignored by CLAMP_TO_GROUND for rendering,
                // but might be relevant if you read the marker's position later.
                // For CLAMP_TO_GROUND, it's often set to 0.0.
            }
            label = "Clamped to Ground"
            altitudeMode = AltitudeMode.CLAMP_TO_GROUND
            isExtruded = true
            isDrawnWhenOccluded = true
            collisionBehavior = CollisionBehavior.REQUIRED
        })?.let(::setupMarkerClickListener)

        // Marker 4: Relative to Mesh
        // This marker is placed 10 meters above the 3D mesh, which includes buildings.
        googleMap3D.addMarker(markerOptions {
            position = latLngAltitude {
                latitude = 52.520835071144226
                longitude = 13.409426847943774
                altitude = 10.0 // Altitude relative to 3D mesh (buildings, terrain features)
            }
            label = "Relative to Mesh (10m)"
            altitudeMode = AltitudeMode.RELATIVE_TO_MESH
            isExtruded = true
            isDrawnWhenOccluded = true
            collisionBehavior = CollisionBehavior.REQUIRED
        })?.let(::setupMarkerClickListener)

        // Marker 8: Empire State Building Ape
        googleMap3D.addMarker(markerOptions {
            position = latLngAltitude {
                latitude = 40.7484
                longitude = -73.9857
                altitude = 100.0
            }
            zIndex = 1
            label = "King Kong / Empire State Building"
            isExtruded = true
            isDrawnWhenOccluded = true
            altitudeMode = AltitudeMode.RELATIVE_TO_MESH
            setStyle(ImageView(com.example.maps3dcommon.R.drawable.ook))
        })?.let(::setupMarkerClickListener)

        // Marker 9: Custom Color Pin near ESB
        val customColorGlyph = Glyph.fromColor(android.graphics.Color.CYAN)
        googleMap3D.addMarker(markerOptions {
            position = latLngAltitude {
                latitude = 40.7486
                longitude = -73.9848
                altitude = 600.0
            }
            isExtruded = true
            isDrawnWhenOccluded = true
            label = "Custom Color Pin"
            altitudeMode = AltitudeMode.RELATIVE_TO_GROUND
            setStyle(pinConfiguration {
                backgroundColor = android.graphics.Color.RED
                borderColor = android.graphics.Color.WHITE
                setGlyph(customColorGlyph)
            })
        })?.let(::setupMarkerClickListener)

        // Marker 10: Custom Text Pin near ESB
        val textGlyph = Glyph.fromColor(android.graphics.Color.RED).apply {
            setText("NYC\n 🍎 ")
        }
        googleMap3D.addMarker(markerOptions {
            position = latLngAltitude {
                latitude = 40.7482
                longitude = -73.9862
                altitude = 600.0
            }
            isExtruded = true
            isDrawnWhenOccluded = true
            label = "Custom Text Pin"
            altitudeMode = AltitudeMode.RELATIVE_TO_GROUND
            setStyle(pinConfiguration {
                setGlyph(textGlyph)
                backgroundColor = android.graphics.Color.YELLOW
                borderColor = android.graphics.Color.BLUE
            })
        })?.let(::setupMarkerClickListener)

        // Marker 11: Shibuya Crossing
        googleMap3D.addMarker(markerOptions {
            position = latLngAltitude {
                latitude = 35.6595
                longitude = 139.7005
                altitude = 50.0
            }
            isExtruded = true
            isDrawnWhenOccluded = true
            label = "🦖 🥚"
            altitudeMode = AltitudeMode.RELATIVE_TO_GROUND
            setStyle(ImageView(com.example.maps3dcommon.R.drawable.gz))
        })?.let(::setupMarkerClickListener)

        Log.d(TAG, "addMarkers: finished")
    }

    /**
     * Sets up a click listener for a given marker. When the marker is clicked, a toast
     * message is displayed with the marker's label.
     *
     * @param marker The marker to which the click listener will be attached.
     */
    private fun setupMarkerClickListener(marker: Marker) {
        Log.d(TAG, "Marker added: ${marker.id}")
        marker.setClickListener {
            // The toast is shown on the main thread, which is the required context for UI operations.
            lifecycleScope.launch(Dispatchers.Main) {
                Toast.makeText(this@MarkersActivity, "Clicked on marker: ${marker.label}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}
