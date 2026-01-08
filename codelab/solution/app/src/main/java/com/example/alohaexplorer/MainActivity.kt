package com.example.alohaexplorer

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.Map3DView
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.flyAroundOptions
import com.google.android.gms.maps3d.model.flyToOptions
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.gms.maps3d.model.markerOptions
import com.google.android.gms.maps3d.model.modelOptions
import com.google.android.gms.maps3d.model.orientation
import com.google.android.gms.maps3d.model.polygonOptions
import com.google.android.gms.maps3d.model.polylineOptions
import com.google.android.gms.maps3d.model.vector3D
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.seconds

class MainActivity : AppCompatActivity(), OnMap3DViewReadyCallback {

    private lateinit var map3DView: Map3DView
    private var googleMap3D: GoogleMap3D? = null

    companion object {
        // Step 1: Honolulu
        val HONOLULU = latLngAltitude {
            latitude = 21.3069
            longitude = -157.8583
            altitude = 0.0
        }

        // Step 2: Iolani Palace
        val IOLANI_PALACE = latLngAltitude {
            latitude = 21.306740
            longitude = -157.858803
            altitude = 0.0
        }

        // Step 6: Waikiki Beach
        val WAIKIKI = latLngAltitude {
            latitude = 21.2766
            longitude = -157.8286
            altitude = 0.0
        }
        
        // Placeholder URL for the shark (using a known working generic model if shark is unavailable)
//        const val SHARK_MODEL_URL = "https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/Shark.glb"
//        const val SHARK_MODEL_URL = "https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/Shark%20fin.glb"
//        const val SHARK_MODEL_URL = "https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/UFO.glb"
        const val SHARK_MODEL_URL = "https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/Airplane.glb"
        const val SHARK_SCALE = 0.05
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.map3dView)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Step 1: Initialize Map3DView
        map3DView = findViewById(R.id.map3dView)
        map3DView.onCreate(savedInstanceState)
        map3DView.getMap3DViewAsync(this)
    }

    override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
        this@MainActivity.googleMap3D = googleMap3D

        lifecycleScope.launch {
            // Step 0: Start from Global View
            startFromGlobalView(googleMap3D)
            
            // Wait for the map to settle
            awaitMapSteady(googleMap3D)

            // Setup UI Buttons
            setupButtons(googleMap3D)
        }
    }

    private fun setupButtons(map: GoogleMap3D) {
        findViewById<Button>(R.id.btn_fly_honolulu).setOnClickListener {
            lifecycleScope.launch { flyToHonolulu(map) }
        }

        findViewById<Button>(R.id.btn_show_markers).setOnClickListener {
            addMarkers(map)
            lifecycleScope.launch { flyToMarkers(map) }
        }

        findViewById<Button>(R.id.btn_show_polygons).setOnClickListener {
            addPolygon(map)
            lifecycleScope.launch { flyToMarkers(map) } // Re-use marker view for polygons as it's the same place
        }

        findViewById<Button>(R.id.btn_show_shark).setOnClickListener {
            setupShark(map)
            lifecycleScope.launch { flyToShark(map) }
        }
    }

    private suspend fun flyToMarkers(map: GoogleMap3D) {
        map.flyCameraTo(
            flyToOptions {
                endCamera = camera {
                    center = IOLANI_PALACE
                    tilt = 60.0
                    range = 500.0
                    heading = 0.0
                }
                durationInMillis = 2000L
            }
        )
        awaitCameraAnimation(map)
    }

    private suspend fun flyToShark(map: GoogleMap3D) {
        map.flyCameraTo(
            flyToOptions {
                endCamera = camera {
                    center = WAIKIKI
                    tilt = 60.0
                    range = 1000.0
                    heading = 0.0
                }
                durationInMillis = 2000L
            }
        )
        awaitCameraAnimation(map)
    }

    private suspend fun awaitMapSteady(map: GoogleMap3D) = suspendCancellableCoroutine { continuation ->
        map.setOnMapSteadyListener { isSteady ->
            if (isSteady) {
                map.setOnMapSteadyListener(null) // Cleanup
                if (continuation.isActive) {
                    continuation.resume(Unit)
                }
            }
        }
        
        // Remove listener if coroutine is cancelled
        continuation.invokeOnCancellation {
            map.setOnMapSteadyListener(null)
        }
    }

    private suspend fun awaitCameraAnimation(map: GoogleMap3D) = suspendCancellableCoroutine { continuation ->
        map.setCameraAnimationEndListener {
            map.setCameraAnimationEndListener(null) // Cleanup
            if (continuation.isActive) {
                continuation.resume(Unit)
            }
        }

        // Remove listener if coroutine is cancelled
        continuation.invokeOnCancellation {
            map.setCameraAnimationEndListener(null)
        }
    }

    private fun startFromGlobalView(map: GoogleMap3D) {
        // Initial Position: Global View
        map.setCamera(
            camera {
                center = latLngAltitude {
                    latitude = 21.3069
                    longitude = -157.8583
                    altitude = 0.0
                }
                tilt = 0.0
                range = 5000000.0 // 5000 km up
            }
        )
    }

    private suspend fun flyToHonolulu(map: GoogleMap3D) {
        // Duration for the animation
        val flyDuration = 5.seconds
        val orbitDuration = 10.seconds
        
        println("Flying to Honolulu...")
        map.flyCameraTo(
            flyToOptions {
                endCamera = camera {
                    center = HONOLULU
                    tilt = 45.0
                    range = 20000.0 // Zoomed out a bit from the city
                    heading = 0.0
                }
                durationInMillis = flyDuration.inWholeMilliseconds
            }
        )
        // Wait for the animation to finish
        awaitCameraAnimation(map)
        awaitMapSteady(map)
        
        println("Orbiting Honolulu...")
        map.flyCameraAround(
            flyAroundOptions {
                // Determine the center of the orbit (the camera we just flew to)
                // We reuse the target parameters to be precise, or could read map.camera
                center = camera {
                    center = HONOLULU
                    tilt = 45.0
                    range = 20000.0
                    heading = 0.0
                }
                rounds = 1.0
                durationInMillis = orbitDuration.inWholeMilliseconds
            }
        )
        awaitCameraAnimation(map)
    }

    private fun addPolygon(map: GoogleMap3D) {
        // Step 4: Extruded Polygon around Iolani Palace
        val palaceBaseFace = listOf(
            latLngAltitude { latitude = 21.3073; longitude = -157.8593; altitude = 0.0 },
            latLngAltitude { latitude = 21.3073; longitude = -157.8584; altitude = 0.0 },
            latLngAltitude { latitude = 21.3063; longitude = -157.8584; altitude = 0.0 },
            latLngAltitude { latitude = 21.3063; longitude = -157.8593; altitude = 0.0 },
            latLngAltitude { latitude = 21.3073; longitude = -157.8593; altitude = 0.0 }
        )

        // Extrude the polygon into faces
        val extrudedPalace = extrudePolygon(palaceBaseFace, 50.0)

        // Add all faces to the map
        val palacePolygons = extrudedPalace.map { facePoints ->
            map.addPolygon(
                polygonOptions {
                    path = facePoints
                    fillColor = Color.argb(100, 255, 215, 0) // Gold
                    strokeColor = Color.YELLOW
                    strokeWidth = 5.0
                    altitudeMode = AltitudeMode.ABSOLUTE
                }
            )
        }

        // Step 5: Tapping the Turf
        // Add click listener to each face
        palacePolygons.forEach { polygon ->
            polygon.setClickListener {
                Toast.makeText(this, "The Royal Palace: A symbol of Hawaiian sovereignty.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupShark(map: GoogleMap3D) {
        // Step 6: Beach Bound
        // Draw path to Waikiki
        map.addPolyline(
            polylineOptions {
                path = listOf(IOLANI_PALACE, WAIKIKI)
                strokeWidth = 10.0
                strokeColor = Color.BLUE
            }
        )
        
        // Add "Shark" (Airplane placeholder)
        val shark = map.addModel(
            modelOptions {
                position = latLngAltitude {
                    latitude = WAIKIKI.latitude
                    longitude = WAIKIKI.longitude
                    altitude = 20.0
                }
                altitudeMode = AltitudeMode.ABSOLUTE
                orientation = orientation {
                    heading = 0.0
                    tilt = -90.0
                    roll = 0.0
                }
                url = SHARK_MODEL_URL
                scale = vector3D { x = SHARK_SCALE; y = SHARK_SCALE; z = SHARK_SCALE } // Big shark
            }
        )

        // Animate Shark
        lifecycleScope.launch {
            // Simple animation loop provided in codelab
            shark.position = latLngAltitude {
                latitude = WAIKIKI.latitude
                longitude = WAIKIKI.longitude
                altitude = 50.0
            }
            delay(1000)
            shark.position = WAIKIKI
        }
    }

    private fun addMarkers(map: GoogleMap3D) {
        // Absolute
        map.addMarker(
            markerOptions {
                position = latLngAltitude {
                    latitude = IOLANI_PALACE.latitude
                    longitude = IOLANI_PALACE.longitude
                    altitude = 100.0
                }
                altitudeMode = AltitudeMode.ABSOLUTE
                label = "Absolute (100m)"
                isDrawnWhenOccluded = true
            }
        )

        // Relative
        map.addMarker(
            markerOptions {
                position = latLngAltitude {
                    latitude = IOLANI_PALACE.latitude + 0.001
                    longitude = IOLANI_PALACE.longitude
                    altitude = 50.0
                }
                altitudeMode = AltitudeMode.RELATIVE_TO_GROUND
                label = "Relative (50m)"
                isDrawnWhenOccluded = true
            }
        )

        // Clamped
        map.addMarker(
            markerOptions {
                position = latLngAltitude {
                    latitude = IOLANI_PALACE.latitude - 0.001
                    longitude = IOLANI_PALACE.longitude
                    altitude = 0.0
                }
                altitudeMode = AltitudeMode.CLAMP_TO_GROUND
                label = "Clamped"
                isDrawnWhenOccluded = true
            }
        )
    }

    /**
     * Extrudes a flat polygon (defined by basePoints, all at the same altitude)
     * upwards by a given extrusionHeight to form a 3D prism.
     * Copied from Maps3DSamples DataModel.kt.
     */
    private fun extrudePolygon(
        basePoints: List<LatLngAltitude>,
        extrusionHeight: Double
    ): List<List<LatLngAltitude>> {
        if (basePoints.size < 3) return emptyList()
        if (extrusionHeight <= 0) return emptyList()

        val baseAltitude = basePoints.first().altitude

        // 1. Create points for the top face
        val topPoints = basePoints.map { basePoint ->
            latLngAltitude {
                latitude = basePoint.latitude
                longitude = basePoint.longitude
                altitude = baseAltitude + extrusionHeight
            }
        }

        val faces = mutableListOf<List<LatLngAltitude>>()

        // 2. Add bottom face
        faces.add(basePoints.toList())

        // 3. Add top face (reversed for winding)
        faces.add(topPoints.toList().reversed())

        // 4. Add side wall faces
        for (i in basePoints.indices) {
            val p1Base = basePoints[i]
            val p2Base = basePoints[(i + 1) % basePoints.size]

            val p1Top = topPoints[i]
            val p2Top = topPoints[(i + 1) % basePoints.size]

            val sideFace = listOf(p1Base, p2Base, p2Top, p1Top)
            faces.add(sideFace)
        }

        return faces
    }

    // Lifecycle forwarding
    override fun onResume() {
        super.onResume()
        map3DView.onResume()
    }
    
    override fun onPause() {
        super.onPause()
        map3DView.onPause()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        map3DView.onDestroy()
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        map3DView.onSaveInstanceState(outState)
    }
    
    override fun onLowMemory() {
        super.onLowMemory()
        map3DView.onLowMemory()
    }
}