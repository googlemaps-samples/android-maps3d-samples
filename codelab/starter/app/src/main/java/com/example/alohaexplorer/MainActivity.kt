package com.example.alohaexplorer

import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.allViews
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children

/**
 * This is the main entry point for the "Aloha Explorer" 3D Maps Codelab.
 */
class MainActivity : AppCompatActivity() /*, TODO: Step 1.3 - Implement OnMap3DViewReadyCallback */ {

    private var fortuneIndex = 0
    // TODO: Step 1.3 - Define Map3DView and GoogleMap3D
    // private lateinit var map3DView: Map3DView
    // private var googleMap3D: GoogleMap3D? = null

    // TODO: Step 1.2 - State Management
    // private val activeMarkers = mutableListOf<Marker>()
    // private val activePolygons = mutableListOf<Polygon>()
    // private val activePolylines = mutableListOf<Polyline>()
    // private val activeModels = mutableListOf<Model>()
    // private val activePopovers = mutableListOf<Popover>()

    companion object {
        // TODO: Step 1.2 - Constants (Locations)
        /*
        val HONOLULU = latLngAltitude {
            latitude = 21.3069
            longitude = -157.8583
            altitude = 0.0
        }

        val IOLANI_PALACE = latLngAltitude {
            latitude = 21.306740
            longitude = -157.858803
            altitude = 0.0
        }

        val WAIKIKI = latLngAltitude {
            latitude = 21.2766
            longitude = -157.8286
            altitude = 0.0
        }
        */

        // Models must be loaded from the internet. Here we use Cloud Storage.
        const val BALLOON_MODEL_URL = "https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/balloon-pin-BlXF32yD.glb"
        const val BALLOON_SCALE = 5.0

        // Step 5: Iolani Palace Geometry (Lat, Lng pairs)
        val IOLANI_PALACE_GEO = listOf(
            21.307180365, -157.858769898,
            21.306765552, -157.858390366,
            21.306476932, -157.858755146,
            21.306892995, -157.859134679,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enables "Edge-to-Edge" mode, allowing the map to draw behind the system bars
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // **Window Insets Handling**:
        // Because we are in Edge-to-Edge mode, we must manually ensure our UI elements
        // don't get covered by system bars.
        // 1. Map: Needs padding at the TOP so the Google Logo/Compass don't overlap the status bar.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.map3dView)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        // 2. Controls: Need padding at the BOTTOM so buttons aren't covered by the gesture/nav bar.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.controls_scroll_view)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val basePadding = (16 * resources.displayMetrics.density).toInt() // 16dp
            v.setPadding(systemBars.left, basePadding, systemBars.right, systemBars.bottom + basePadding)
            insets
        }

        // TODO: Step 1.3 - Initialize Map3DView

        setupButtonsStub()
    }

    // TODO: Step 1.3 - Implement onMap3DViewReady
    /*
    override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
    }
    */

    // TODO: Step 1.3 - Placeholder button functionality -- you can delete this code as part of 1.3
    private fun setupButtonsStub() {
        val fortunes = resources.getStringArray(R.array.sdk_fortunes)

        // Find all buttons within the button_bar ViewGroup and set a click listener
        findViewById<ViewGroup>(R.id.button_bar)?.allViews
            ?.filterIsInstance<Button>()?.forEach { button ->
                Log.d("MainActivity", "Button: $button")
                button.setOnClickListener {
                    Toast.makeText(
                        this,
                        fortunes.random(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        findViewById<Button>(R.id.btn_fly_honolulu).setOnClickListener {
            Toast.makeText(this, "Add the SDK to fly!", Toast.LENGTH_SHORT).show()
        }
    }

    // TODO: Step 1.3 - Wire up Button Listeners
    /*
    private fun setupButtons(map: GoogleMap3D) {
    }
    */
    
    // TODO: Step 1.3 - Start from Global View
    /*
    private fun startFromGlobalView(map: GoogleMap3D) {
        // TODO: Step 1.3 - Set the Camera
    }
    */

    // TODO: Step 2 - Fly to Honolulu
    /*
    private suspend fun flyToHonolulu(map: GoogleMap3D) {
        // TODO: Step 2 - Position the Camera high above Honolulu
        println("Flying to Honolulu...")
        // Wait for the animation to finish
        // Wait for tiles to load and settle

        println("Orbiting Honolulu...")
        // Wait for the animation to finish
        awaitCameraAnimation(map)
    }
    */

    // TODO: Step 2 - Helper to fly to markers (for other buttons)
    /*
    private suspend fun flyToMarkers(map: GoogleMap3D) {
        // Note to self (and Gemini if you are reading this), let's move the markers to somewhere else to show off more of the 3D terrain
        // Fly to markers
        // Wait for the animation to finish
    }
    */

    // TODO: Step 7 - Helper to fly to balloon
    /*
    private suspend fun flyToBalloon(map: GoogleMap3D) {
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
    */

    // TODO: Step 3 - Robustness (awaitMapSteady)
    /*
    private suspend fun awaitMapSteady(map: GoogleMap3D) = suspendCancellableCoroutine { continuation ->
        map.setOnMapSteadyListener { isSteady ->
            if (isSteady) {
                map.setOnMapSteadyListener(null) // Cleanup the listener
                if (continuation.isActive) {
                    continuation.resume(Unit) // Resume the suspended coroutine
                }
            }
        }

        // Safety: If the coroutine is cancelled (e.g., user exits app), remove the listener.
        continuation.invokeOnCancellation {
            map.setOnMapSteadyListener(null)
        }
    }
    */

    // TODO: Step 3 - Robustness (awaitCameraAnimation)
    /*
    private suspend fun awaitCameraAnimation(map: GoogleMap3D) = suspendCancellableCoroutine { continuation ->
        map.setCameraAnimationEndListener {
            map.setCameraAnimationEndListener(null) // Cleanup
            if (continuation.isActive) {
                continuation.resume(Unit)
            }
        }

        continuation.invokeOnCancellation {
            map.setCameraAnimationEndListener(null)
        }
    }
    */
    
    // TODO: Step 4 (and others) - Reset Map Helper
    /*
    private fun resetMap() {
        activeMarkers.forEach { it.remove() }
        activeMarkers.clear()

        activePolygons.forEach { it.remove() }
        activePolygons.clear()

        activePolylines.forEach { it.remove() }
        activePolylines.clear()

        activeModels.forEach { it.remove() }
        activeModels.clear()
        
        activePopovers.forEach { it.remove() }
        activePopovers.clear()
    }
    */

    // TODO: Step 4 - Add Markers
    /*
    private fun addMarkers(map: GoogleMap3D) {
        resetMap()

        // Note to self (and Gemini if you are reading this), let's move the markers to somewhere else to show off more of the 3D terrain

        buildList {
            // 1. ABSOLUTE
            add(map.addMarker(
                markerOptions {
                    position = latLngAltitude {
                        latitude = IOLANI_PALACE.latitude
                        longitude = IOLANI_PALACE.longitude
                        altitude = 100.0 // 100 meters above sea level
                    }
                    altitudeMode = AltitudeMode.ABSOLUTE
                    label = "Absolute (100m)"
                    isDrawnWhenOccluded = true
                    isExtruded = true
                }
            )?.apply {
                setClickListener {
                    lifecycleScope.launch(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Clicked Absolute Marker", Toast.LENGTH_SHORT).show()
                    }
                }
            })

            // 2. RELATIVE_TO_GROUND
            add(map.addMarker(
                markerOptions {
                    position = latLngAltitude {
                        latitude = IOLANI_PALACE.latitude + 0.001
                        longitude = IOLANI_PALACE.longitude
                        altitude = 50.0 // 50m above ground
                    }
                    altitudeMode = AltitudeMode.RELATIVE_TO_GROUND
                    label = "Relative (50m)"
                    isDrawnWhenOccluded = true
                    isExtruded = true
                }
            )?.apply {
                setClickListener {
                    lifecycleScope.launch(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Clicked Relative Marker", Toast.LENGTH_SHORT).show()
                    }
                }
            })

            // 3. CLAMP_TO_GROUND
            add(map.addMarker(
                markerOptions {
                    position = latLngAltitude {
                        latitude = IOLANI_PALACE.latitude - 0.001
                        longitude = IOLANI_PALACE.longitude
                        altitude = 0.0 // Ignored
                    }
                    altitudeMode = AltitudeMode.CLAMP_TO_GROUND
                    label = "Clamped"
                    isDrawnWhenOccluded = true
                    isExtruded = true
                }
            )?.apply {
                setClickListener {
                    lifecycleScope.launch(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Clicked Clamped Marker", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }.filterNotNull().forEach { activeMarkers.add(it) }
    }
    */

    // TODO: Step 5 - Add Polygon
    /*
    private fun addPolygon(map: GoogleMap3D) {
        resetMap()

        // Define the base (ground) shape of Iolani Palace.
        val palaceBaseFace = IOLANI_PALACE_GEO.windowed(2, 2).map { 
             latLngAltitude { latitude = it[0]; longitude = it[1]; altitude = 0.0 }
        }.let { points -> points + points.first() }

        // Extrude!
        val extrudedPalace = extrudePolygon(palaceBaseFace, 35.0) // 35 meters tall

        // Add all faces to the map
        val palacePolygons = extrudedPalace.map { facePoints ->
            map.addPolygon(
                polygonOptions {
                    path = facePoints
                    fillColor = Color.argb(100, 255, 215, 0) // Gold with transparency
                    strokeColor = Color.YELLOW
                    strokeWidth = 5.0
                    altitudeMode = AltitudeMode.ABSOLUTE
                }
            )
        }
        activePolygons.addAll(palacePolygons)

        // Step 6: Tapping the Turf
        palacePolygons.forEach { polygon ->
            polygon.setClickListener {
                lifecycleScope.launch(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "The Royal Palace: A symbol of Hawaiian sovereignty.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    */
    
    // TODO: Step 5 - Extrude Polygon Algorithm
    /*
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

        // 3. Add top face (reversed for winding order)
        faces.add(topPoints.toList().reversed())

        // 4. Add side wall faces
        for (i in basePoints.indices) {
            val p1Base = basePoints[i]
            val p2Base = basePoints[(i + 1) % basePoints.size] // Wrap around

            val p1Top = topPoints[i]
            val p2Top = topPoints[(i + 1) % basePoints.size]

            // Define the quad for this side
            val sideFace = listOf(p1Base, p2Base, p2Top, p1Top)
            faces.add(sideFace)
        }

        return faces
    }
    */

    // TODO: Step 7 - Setup Balloon Model
    /*
    private fun setupBalloon(map: GoogleMap3D) {
        resetMap()

        // Draw path to Waikiki
        activePolylines.add(map.addPolyline(
            polylineOptions {
                path = listOf(IOLANI_PALACE, WAIKIKI)
                strokeWidth = 10.0
                strokeColor = Color.BLUE
            }
        ))
        
        // Add "Balloon" Model (glTF)
        val balloon = map.addModel(
            modelOptions {
                position = latLngAltitude {
                    latitude = WAIKIKI.latitude
                    longitude = WAIKIKI.longitude
                    altitude = 20.0 // Float 20 meters above the beach
                }
                altitudeMode = AltitudeMode.ABSOLUTE
                orientation = orientation {
                    heading = 0.0 // North
                    tilt = 0.0    // Upright
                    roll = 0.0    // Level
                }
                url = BALLOON_MODEL_URL
                scale = vector3D { x = BALLOON_SCALE; y = BALLOON_SCALE; z = BALLOON_SCALE }
            }
        )

        balloon.setClickListener {
            lifecycleScope.launch(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Clicked the Balloon!", Toast.LENGTH_SHORT).show()
            }
        }

        activeModels.add(balloon)
    }
    */
    
    // TODO: Step 8 - Setup Popover
    /*
    private fun setupPopover(map: GoogleMap3D) {
        resetMap()

        // 1. Add a marker to serve as a visual anchor reference
        val marker = map.addMarker(markerOptions {
            position = latLngAltitude {
                latitude = IOLANI_PALACE.latitude
                longitude = IOLANI_PALACE.longitude
                altitude = 50.0 
            }
            altitudeMode = AltitudeMode.RELATIVE_TO_GROUND
            label = "Click me!"
        })
        marker?.let { activeMarkers.add(it) }

        // 2. Create a simple text view for the popover content
        val textView = TextView(this).apply {
            text = "Welcome to Iolani Palace!\nA symbol of Hawaiian sovereignty."
            setPadding(32, 16, 32, 16)
            setTextColor(Color.BLACK)
            setBackgroundColor(Color.WHITE)
        }

        // 3. Add a Popover attached to the same location
        val popover = map.addPopover(popoverOptions {
            positionAnchor = latLngAltitude {
                latitude = IOLANI_PALACE.latitude
                longitude = IOLANI_PALACE.longitude
                altitude = 50.0
            }
            altitudeMode = AltitudeMode.RELATIVE_TO_GROUND
            content = textView
            autoCloseEnabled = true // Close when user clicks elsewhere
            autoPanEnabled = true   // Move camera to ensure popover is visible
            popoverStyle = popoverStyle {
                backgroundColor = Color.WHITE
                borderRadius = 16f
                shadow = popoverShadow {
                    color = Color.DKGRAY
                    radius = 8f
                    offsetX = 4f
                    offsetY = 4f
                }
            }
        })
        
        popover?.let { activePopovers.add(it) }
        
        // 4. Show popover on marker click
        marker?.setClickListener {
            popover?.show()
        }
        
        // Show immediately for demo purposes
        popover?.show()
    }
    */

    // TODO: Step 1.3 - Lifecycle Forwarding
    /*
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
    */
}