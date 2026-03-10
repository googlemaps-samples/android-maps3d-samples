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

import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.maps3dcommon.R
import com.example.maps3dkotlin.sampleactivity.SampleBaseActivity
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.CollisionBehavior
import com.google.android.gms.maps3d.model.Glyph
import com.google.android.gms.maps3d.model.ImageView
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.Marker
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.flyToOptions
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.gms.maps3d.model.markerOptions
import com.google.android.gms.maps3d.model.pinConfiguration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

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
    private var activePopover: com.google.android.gms.maps3d.Popover? = null

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

    private var monsterCameras: List<Camera> = emptyList()
    private var monsterMarkers: List<Marker> = emptyList()
    private var monsterIds: List<String> = emptyList()
    private var monsterLabels: List<String> = emptyList()
    
    private var tourJob: kotlinx.coroutines.Job? = null

    // The initial camera position is defined declaratively, providing a clear overview of
    // the starting view of the map. This makes it easy to understand and modify the initial
    // scene without digging into the logic of the activity.
    override val initialCamera = nycCamera

    override fun onMapReady(googleMap3D: GoogleMap3D) {
        super.onMapReady(googleMap3D)
        googleMap3D.setMapMode(Map3DMode.SATELLITE)

        findViewById<Button>(R.id.fly_berlin_button)?.apply {
            runOnUiThread {
                visibility = View.VISIBLE
            }
            setOnClickListener {
                stopMonsterTour()
                googleMap3D.flyCameraTo(flyToOptions {
                    endCamera = berlinCamera
                    durationInMillis = 4_000
                })
            }
        }

        findViewById<Button>(R.id.fly_nyc_button)?.apply {
            runOnUiThread {
                visibility = View.VISIBLE
            }
            setOnClickListener {
                stopMonsterTour()
                googleMap3D.flyCameraTo(flyToOptions {
                    endCamera = nycCamera
                    durationInMillis = 4_000
                })
            }
        }

        findViewById<Button>(R.id.fly_random_monster_button)?.apply {
            runOnUiThread {
                visibility = View.VISIBLE
            }
            setOnClickListener {
                stopMonsterTour()
                if (monsterCameras.isNotEmpty()) {
                    googleMap3D.flyCameraTo(flyToOptions {
                        endCamera = monsterCameras.random()
                        durationInMillis = 4_000
                    })
                }
            }
            setOnLongClickListener { view ->
                if (monsterLabels.isNotEmpty()) {
                    val popup = android.widget.PopupMenu(this@MarkersActivity, view)
                    monsterLabels.forEachIndexed { index, label ->
                        popup.menu.add(0, index, index, label)
                    }
                    popup.setOnMenuItemClickListener { item ->
                        stopMonsterTour()
                        val selectedCamera = monsterCameras[item.itemId]
                        googleMap3D.flyCameraTo(flyToOptions {
                            endCamera = selectedCamera
                            durationInMillis = 4_000
                        })
                        true
                    }
                    popup.show()
                }
                true
            }
        }

        findViewById<Button>(R.id.tour_monsters_button)?.apply {
            runOnUiThread {
                visibility = View.VISIBLE
            }
            setOnClickListener {
                if (monsterCameras.isNotEmpty() && monsterMarkers.size == monsterCameras.size && tourJob == null) {
                    startMonsterTour()
                }
            }
        }
        
        findViewById<Button>(R.id.stop_button)?.apply {
            setOnClickListener {
                stopMonsterTour()
            }
        }

        lifecycleScope.launch(Dispatchers.Default) {
            addMarkers(googleMap3D)
        }
        
        googleMap3D.setMap3DClickListener { _, _ ->
            lifecycleScope.launch(Dispatchers.Main) {
                activePopover?.remove()
                activePopover = null
            }
        }
    }

    private fun startMonsterTour() {
        val stopButton = findViewById<Button>(R.id.stop_button)
        val tourButton = findViewById<Button>(R.id.tour_monsters_button)
        val map = googleMap3D ?: return
        
        runOnUiThread {
            stopButton?.visibility = View.VISIBLE
            tourButton?.visibility = View.GONE
        }
        
        tourJob = lifecycleScope.launch(Dispatchers.Main) {
            var i = 0
            while (isActive) {
                activePopover?.remove()
                activePopover = null
                
                val camera = monsterCameras[i]
                val marker = monsterMarkers[i]
                val monsterId = monsterIds[i]
                
                var isCameraDone = false
                map.setCameraAnimationEndListener {
                    isCameraDone = true
                }
                
                var isMapSteady = false
                map.setOnMapSteadyListener { steady ->
                    if (steady) {
                        isMapSteady = true
                        map.setOnMapSteadyListener(null)
                    }
                }
                
                map.flyCameraTo(flyToOptions {
                    endCamera = camera
                    durationInMillis = 4_000
                })
                
                delay(500)
                while ((!isCameraDone || !isMapSteady) && isActive) {
                    delay(100)
                }
                if (!isActive) break

                isCameraDone = false
                map.setCameraAnimationEndListener {
                    isCameraDone = true
                }
                map.flyCameraAround(com.google.android.gms.maps3d.model.flyAroundOptions {
                    center = camera
                    durationInMillis = 5_000
                    rounds = 1.0
                })
                
                delay(500)
                while (!isCameraDone && isActive) {
                    delay(100)
                }
                if (!isActive) break
                
                showMonsterPopover(marker, getMonsterBlurbResId(monsterId), map)
                
                delay(4000)
                
                i = (i + 1) % monsterCameras.size
            }
            map.setCameraAnimationEndListener(null)
        }
    }

    private fun stopMonsterTour() {
        tourJob?.cancel()
        tourJob = null
        googleMap3D?.stopCameraAnimation()
        googleMap3D?.setCameraAnimationEndListener(null)
        googleMap3D?.setOnMapSteadyListener(null)
        
        runOnUiThread {
            findViewById<Button>(R.id.stop_button)?.visibility = View.GONE
            findViewById<Button>(R.id.tour_monsters_button)?.visibility = View.VISIBLE
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
        })?.let { setupMarkerClickListener(it, null, googleMap3D) }

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
        })?.let { setupMarkerClickListener(it, null, googleMap3D) }

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
        })?.let { setupMarkerClickListener(it, null, googleMap3D) }

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
        })?.let { setupMarkerClickListener(it, null, googleMap3D) }

        // Marker 8: Empire State Building Ape
        googleMap3D.addMarker(markerOptions {
            position = latLngAltitude {
                latitude = 40.7484
                longitude = -73.9857
                altitude = 100.0
            }
            zIndex = 1
            label = "Giant Ape / Empire State Building"
            isExtruded = true
            isDrawnWhenOccluded = true
            altitudeMode = AltitudeMode.RELATIVE_TO_MESH
            setStyle(ImageView(R.drawable.ook))
        })?.let { marker ->
            Log.d(TAG, "Marker added: ${marker.id}")
            marker.setClickListener {
                lifecycleScope.launch(Dispatchers.Main) {
                    val textView = android.widget.TextView(this@MarkersActivity).apply {
                        text = getString(R.string.monster_ape_blurb)
                        setPadding(32, 16, 32, 16)
                        setTextColor(Color.BLACK)
                        setBackgroundColor(Color.WHITE)
                    }
                    val newPopover = googleMap3D.addPopover(com.google.android.gms.maps3d.model.popoverOptions {
                        positionAnchor = marker
                        altitudeMode = AltitudeMode.ABSOLUTE
                        content = textView
                        autoCloseEnabled = true
                        autoPanEnabled = true
                    })
                    
                    activePopover?.remove()
                    activePopover = newPopover
                    activePopover?.show()
                }
            }
        }

        // Marker 9: Custom Color Pin near ESB
        val customColorGlyph = Glyph.fromColor(Color.CYAN)
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
                backgroundColor = Color.RED
                borderColor = Color.WHITE
                setGlyph(customColorGlyph)
            })
        })?.let { setupMarkerClickListener(it, null, googleMap3D) }

        // Marker 10: Custom Text Pin near ESB
        val textGlyph = Glyph.fromColor(Color.RED).apply {
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
                backgroundColor = Color.YELLOW
                borderColor = Color.BLUE
            })
        })?.let { setupMarkerClickListener(it, null, googleMap3D) }

        // Monsters from JSON
        try {
            val jsonString = assets.open("monsters.json").bufferedReader().use { it.readText() }
            val parsedMonsters = com.example.maps3dkotlin.markers.data.MonsterParser.parse(jsonString)
            
            val cameras = mutableListOf<Camera>()
            val markers = mutableListOf<Marker>()
            val ids = mutableListOf<String>()
            val labels = mutableListOf<String>()
            
            for (monster in parsedMonsters) {
                val cam = camera {
                    center = latLngAltitude {
                        latitude = monster.latitude
                        longitude = monster.longitude
                        altitude = monster.altitude
                    }
                    heading = monster.heading
                    tilt = monster.tilt
                    range = monster.range
                }
                
                val markerPos = latLngAltitude {
                    latitude = monster.markerLatitude
                    longitude = monster.markerLongitude
                    altitude = monster.markerAltitude
                }
                
                val drawableId = getMonsterDrawableId(monster.drawable)
                
                if (drawableId != 0) {
                    val m = googleMap3D.addMarker(markerOptions {
                        position = markerPos
                        label = monster.label
                        isExtruded = true
                        isDrawnWhenOccluded = true
                        this.altitudeMode = monster.altitudeMode
                        setStyle(ImageView(drawableId))
                    })
                    
                    if (m != null) {
                        cameras.add(cam)
                        markers.add(m)
                        ids.add(monster.id)
                        labels.add(monster.label)
                        setupMarkerClickListener(m, getMonsterBlurbResId(monster.id), googleMap3D)
                    }
                }
            }
            monsterCameras = cameras
            monsterMarkers = markers
            monsterIds = ids
            monsterLabels = labels
        } catch (e: Exception) {
            Log.e(TAG, "Error loading monsters.json", e)
        }

        Log.d(TAG, "addMarkers: finished")
    }

    /**
     * Sets up a click listener for a given marker. When the marker is clicked, a toast
     * message is displayed with the marker's label.
     *
     * @param marker The marker to which the click listener will be attached.
     */
    private fun setupMarkerClickListener(marker: Marker, blurbResId: Int?, googleMap3D: GoogleMap3D) {
        Log.d(TAG, "Marker added: ${marker.id}")
        marker.setClickListener {
            lifecycleScope.launch(Dispatchers.Main) {
                if (blurbResId != null && blurbResId != 0) {
                    showMonsterPopover(marker, blurbResId, googleMap3D)
                } else {
                    Toast.makeText(this@MarkersActivity, "Clicked on marker: ${marker.label}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun showMonsterPopover(marker: Marker, blurbResId: Int, googleMap3D: GoogleMap3D) {
        if (blurbResId != 0) {
            val textView = android.widget.TextView(this@MarkersActivity).apply {
                text = getString(blurbResId)
                setPadding(32, 16, 32, 16)
                setTextColor(android.graphics.Color.BLACK)
                setBackgroundColor(android.graphics.Color.WHITE)
            }
            val newPopover = googleMap3D.addPopover(com.google.android.gms.maps3d.model.popoverOptions {
                positionAnchor = marker
                altitudeMode = if (marker.altitudeMode == AltitudeMode.RELATIVE_TO_MESH) AltitudeMode.ABSOLUTE else marker.altitudeMode
                content = textView
                autoCloseEnabled = true
                autoPanEnabled = true
            })

            activePopover?.remove()
            activePopover = newPopover
            activePopover?.show()
        }
    }
    /**
     * Helper to map monster IDs to their drawable resources without using discouraged reflection.
     */
    private fun getMonsterDrawableId(drawableName: String): Int {
        return when (drawableName) {
            "alien" -> R.drawable.alien
            "bigfoot" -> R.drawable.bigfoot
            "frank" -> R.drawable.frank
            "godzilla" -> R.drawable.godzilla
            "mothra" -> R.drawable.mothra
            "mummy" -> R.drawable.mummy
            "nessie" -> R.drawable.nessie
            "yeti" -> R.drawable.yeti
            else -> 0
        }
    }

    /**
     * Helper to map monster IDs to their blurb string resources.
     */
    private fun getMonsterBlurbResId(monsterId: String): Int {
        return when (monsterId) {
            "alien" -> R.string.monster_alien_blurb
            "bigfoot" -> R.string.monster_bigfoot_blurb
            "frank" -> R.string.monster_frank_blurb
            "godzilla" -> R.string.monster_godzilla_blurb
            "mothra" -> R.string.monster_mothra_blurb
            "mummy" -> R.string.monster_mummy_blurb
            "nessie" -> R.string.monster_nessie_blurb
            "yeti" -> R.string.monster_yeti_blurb
            else -> 0
        }
    }
}
