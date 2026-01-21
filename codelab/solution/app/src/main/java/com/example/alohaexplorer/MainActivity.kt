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
import com.google.android.gms.maps3d.model.Marker
import com.google.android.gms.maps3d.model.Model
import com.google.android.gms.maps3d.model.Polygon
import com.google.android.gms.maps3d.model.Polyline
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.seconds

/**
 * This is the main entry point for the "Aloha Explorer" 3D Maps Codelab.
 *
 * This Activity demonstrates the core concepts of the Google Maps Platform 3D SDK for Android:
 * 1.  **Initialization**: How to set up the `Map3DView` and handle its lifecycle.
 * 2.  **Camera Control**: How to move the camera programmatically (flyTo, flyAround).
 * 3.  **Adding Content**: How to add 3D Markers, Models, and shapes (Polygons, Polylines).
 * 4.  **Interaction**: How to handle click events on 3D objects.
 * 5.  **Coroutines**: How to use Kotlin Coroutines for smooth, asynchronous animation sequencing.
 */
class MainActivity : AppCompatActivity(), OnMap3DViewReadyCallback {

    // The main view component for the 3D Map.
    private lateinit var map3DView: Map3DView
    
    // The API object we use to interact with the map once it's ready.
    private var googleMap3D: GoogleMap3D? = null

    // **State Management**:
    // We keep track of the objects we add to the map (Markers, Polygons, etc.) so we can
    // remove them later. This prevents "ghost" objects from piling up if the user
    // clicks buttons multiple times.  We do this in the activity for simplicity, but recommend
    // tracking state in a view model.
    private val activeMarkers = mutableListOf<Marker>()
    private val activePolygons = mutableListOf<Polygon>()
    private val activePolylines = mutableListOf<Polyline>()
    private val activeModels = mutableListOf<Model>()

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

        // Models must be loaded from the internet.  Here we use Cloud Storage.
        const val BALLOON_MODEL_URL = "https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/balloon-pin-BlXF32yD.glb"
        const val BALLOON_SCALE = 5.0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enables "Edge-to-Edge" mode, allowing the map to draw behind the system bars
        // (status bar and navigation bar) for a more immersive experience.
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
        //    We add an extra 16dp base padding for aesthetics.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.controls_scroll_view)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val basePadding = (16 * resources.displayMetrics.density).toInt() // 16dp
            v.setPadding(systemBars.left, basePadding, systemBars.right, systemBars.bottom + basePadding)
            insets
        }

        // Step 1: Initialize Map3DView
        // The Map3DView is not a standard View; it requires explicit lifecycle management
        // (onCreate, onResume, etc.) to function correctly.
        map3DView = findViewById(R.id.map3dView)
        map3DView.onCreate(savedInstanceState)
        
        // This starts the map loading process. onMap3DViewReady will be called when it's done.
        map3DView.getMap3DViewAsync(this)
    }

    /**
     * Called when the Map is fully loaded and ready to be used.
     * This is where we can start interacting with the `googleMap3D` object.
     */
    override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
        this@MainActivity.googleMap3D = googleMap3D

        // We use lifecycleScope to launch coroutines. This ensures our animations
        // and background work are automatically cancelled if the Activity is destroyed,
        // preventing memory leaks and crashes.
        lifecycleScope.launch {
            // Step 0: Start from Global View
            startFromGlobalView(googleMap3D)
            
            // Setup UI Buttons
            setupButtons(googleMap3D)
        }
    }

    /**
     * Sets up click listeners for the UI buttons to trigger camera animations.
     * Note how we wrap the calls in `lifecycleScope.launch`. This is because:
     * 1.  The `flyTo...` functions are `suspend` functions (they take time to complete).
     * 2.  We want them to run on the main thread (UI interactions).
     */
    private fun setupButtons(map: GoogleMap3D) {
        findViewById<Button>(R.id.btn_fly_honolulu).setOnClickListener {
            // "Launch" starts a new coroutine.
            lifecycleScope.launch { flyToHonolulu(map) }
        }

        findViewById<Button>(R.id.btn_show_markers).setOnClickListener {
            addMarkers(map)
            lifecycleScope.launch { flyToMarkers(map) }
        }

        findViewById<Button>(R.id.btn_show_polygons).setOnClickListener {
            addPolygon(map)
            // Re-use marker view for polygons as it's the same geographic location
            lifecycleScope.launch { flyToMarkers(map) }
        }

        findViewById<Button>(R.id.btn_show_balloon).setOnClickListener {
            setupBalloon(map)
            lifecycleScope.launch { flyToBalloon(map) }
        }
    }

    private suspend fun flyToMarkers(map: GoogleMap3D) {
        map.flyCameraTo(
            flyToOptions {
                endCamera = camera {
                    center = IOLANI_PALACE
                    tilt = 60.0    // Look down at a 60-degree angle
                    range = 500.0  // 500 meters away for a closer view
                    heading = 0.0  // North up
                }
                durationInMillis = 2000L
            }
        )
        // **Critical Step**: Wait for the animation to actually finish before doing anything else.
        awaitCameraAnimation(map)
    }

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

    /**
     * **Coroutine Helper**: Awaiting Map Stability.
     * Often you want to wait until the map has fully loaded (is "steady") before starting
     * the next animation or interaction. This implementation wraps the callback-based
     * `setOnMapSteadyListener` into a standard Kotlin `suspend` function.
     */
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

    /**
     * **Coroutine Helper**: Awaiting Camera Animation.
     * Similar to `awaitMapSteady`, this pauses our code execution until the camera finishes its flight.
     * This allows us to write strict sequences like: "Fly to A, THEN wait, THEN fly to B".
     */
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

    private fun startFromGlobalView(map: GoogleMap3D) {
        // Initial Position: "Global View" aka Space View
        map.setCamera(
            camera {
                center = latLngAltitude {
                    latitude = 21.3069
                    longitude = -157.8583
                    altitude = 0.0
                }
                tilt = 0.0     // Looking straight down
                range = 5_000_000.0 // 5,000 km altitude for a wide view
            }
        )
    }

    private suspend fun flyToHonolulu(map: GoogleMap3D) {
        // Duration for the animation
        val flyDuration = 5.seconds
        val orbitDuration = 10.seconds
        
        println("Flying to Honolulu...")
        map.flyCameraTo(
             // **FlyToOptions**: Configures how the camera moves.
            flyToOptions {
                endCamera = camera {
                    center = HONOLULU
                    tilt = 45.0
                    range = 20_000.0 // Zoomed out a bit from the city
                    heading = 0.0
                }
                durationInMillis = flyDuration.inWholeMilliseconds
            }
        )
        // Wait for the animation to finish
        awaitCameraAnimation(map)
        awaitMapSteady(map) // Wait for tiles to load and settle
        
        println("Orbiting Honolulu...")
        map.flyCameraAround(
            flyAroundOptions {
                // **FlyAroundOptions**: Orbits around a specific point.
                // We reuse the target parameters to be precise, or could read map.camera
                center = camera {
                    center = HONOLULU
                    tilt = 45.0
                    range = 20000.0
                    heading = 0.0
                }
                rounds = 1.0 // One full circle
                durationInMillis = orbitDuration.inWholeMilliseconds
            }
        )
        awaitCameraAnimation(map)
    }

    /**
     * Clears all objects from the map.
     * We iterate through our tracking lists and call .remove() on each object.
     * This is crucial to avoid "leaking" objects on the map if the user clicks buttons repeatedly.
     */
    private fun resetMap() {
        activeMarkers.forEach { it.remove() }
        activeMarkers.clear()

        activePolygons.forEach { it.remove() }
        activePolygons.clear()

        activePolylines.forEach { it.remove() }
        activePolylines.clear()

        activeModels.forEach { it.remove() }
        activeModels.clear()
    }

    private fun addPolygon(map: GoogleMap3D) {
        resetMap()

        // Define the base (ground) shape of Iolani Palace.
        // Note: Points are defined clockwise: North -> East -> South -> West
        val palaceBaseFace = listOf(
            21.307180365, -157.858769898,
            21.306765552, -157.858390366,
            21.306476932, -157.858755146,
            21.306892995, -157.859134679,
        ).windowed(2, 2).map {
            latLngAltitude {
                latitude = it[0]
                longitude = it[1]
                altitude = 0.0
            }
        }.let { points ->
            // Close the loop by appending the first point to the end
            points + points.first()
        }

        // **Extrusion**: Turn a 2D shape into a 3D building by duplicating the path upwards
        // and connecting the edges.
        val extrudedPalace = extrudePolygon(palaceBaseFace, 35.0) // 35 meters tall

        // Add all faces (top, bottom, sides) to the map
        val palacePolygons = extrudedPalace.map { facePoints ->
            map.addPolygon(
                polygonOptions {
                    path = facePoints
                    fillColor = Color.argb(100, 255, 215, 0) // Gold with transparency
                    strokeColor = Color.YELLOW
                    strokeWidth = 5.0
                    // **AltitudeMode.ABSOLUTE**: Interpret altitude as meters above the WGS84 ellipsoid.
                    // This is ideal for 3D shapes where geometry matters more than terrain following.
                    altitudeMode = AltitudeMode.ABSOLUTE
                }
            )
        }
        activePolygons.addAll(palacePolygons)

        // Step 6: Tapping the Turf
        // Add click listener to each face
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

    private fun setupBalloon(map: GoogleMap3D) {
        resetMap()

        // Step 6: Beach Bound
        // Draw path to Waikiki
        activePolylines.add(map.addPolyline(
            polylineOptions {
                path = listOf(IOLANI_PALACE, WAIKIKI)
                strokeWidth = 10.0
                strokeColor = Color.BLUE
            }
        ))
        
        // Add "Balloon" Model (glTF)
        // Models are external 3D assets.
        val balloon = map.addModel(
            modelOptions {
                position = latLngAltitude {
                    latitude = WAIKIKI.latitude
                    longitude = WAIKIKI.longitude
                    altitude = 20.0 // Float 20 meters above the beach
                }
                altitudeMode = AltitudeMode.ABSOLUTE
                // **Orientation**: Rotate the model in 3D space.
                orientation = orientation {
                    heading = 0.0 // North
                    tilt = 0.0    // Upright
                    roll = 0.0    // Level
                }
                url = BALLOON_MODEL_URL
                // **Scale**: Resize the model. (x=5, y=5, z=5) makes it 5x larger.
                scale = vector3D { x = BALLOON_SCALE; y = BALLOON_SCALE; z = BALLOON_SCALE }
            }
        )

        // Add click listener
        balloon.setClickListener {
            lifecycleScope.launch(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Clicked the Balloon!", Toast.LENGTH_SHORT).show()
            }
        }

        // Track it so we can remove it later
        activeModels.add(balloon)
    }

    /**
     * Demonstrates adding 3D markers with different [AltitudeMode]s.
     * Junior Dev Tip: Understanding `AltitudeMode` is key to placing objects correctly.
     */
    private fun addMarkers(map: GoogleMap3D) {
        resetMap()

        buildList {
            // 1. ABSOLUTE: Altitude is relative to the WGS84 ellipsoid (rough sea level).
            //    Good for aircraft, satellites, or data visualizations that ignore terrain.
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
                    isExtruded = true // Draws a line to the ground
                }
            )?.apply {
                setClickListener {
                    lifecycleScope.launch(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Clicked Absolute Marker", Toast.LENGTH_SHORT).show()
                    }
                }
            })

            // 2. RELATIVE_TO_GROUND: Altitude is added to the terrain height at that point.
            //    Perfect for placing things "floating 50m above the ground".
            add(map.addMarker(
                markerOptions {
                    position = latLngAltitude {
                        latitude = IOLANI_PALACE.latitude + 0.001
                        longitude = IOLANI_PALACE.longitude
                        altitude = 50.0 // 50m above whatever ground is here
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

            // 3. CLAMP_TO_GROUND: Altitude is ignored; object snaps to the terrain/mesh.
            //    Best for map pins, POIs, or anything on the surface.
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

    /**
     * Extrudes a flat polygon (defined by basePoints, all at the same altitude)
     * upwards by a given extrusionHeight to form a 3D prism (like a building).
     * 
     * **Algorithm Explanation**:
     * 1.  **Bottom Face**: The original list of points.
     * 2.  **Top Face**: Same lat/lng as bottom, but altitude += height.
     * 3.  **Side Faces**: Quads connecting each segment of the bottom to the corresponding segment of the top.
     *
     * @param basePoints List of vertices for the base.
     * @param extrusionHeight Height in meters to extrude upwards.
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

        // 3. Add top face (must be reversed for correct "winding order" so it faces up)
        faces.add(topPoints.toList().reversed())

        // 4. Add side wall faces
        for (i in basePoints.indices) {
            val p1Base = basePoints[i]
            val p2Base = basePoints[(i + 1) % basePoints.size] // Wrap around to start

            val p1Top = topPoints[i]
            val p2Top = topPoints[(i + 1) % basePoints.size]

            // Define the quad for this side
            val sideFace = listOf(p1Base, p2Base, p2Top, p1Top)
            faces.add(sideFace)
        }

        return faces
    }

    // ---------------------------------------------------------------------------------------------
    // Lifecycle Forwarding
    // The Map3DView requires us to forward standard Android Activity lifecycle events
    // so it can manage its internal 3D rendering engine (pause rendering when backgrounded, etc.).
    // ---------------------------------------------------------------------------------------------
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