package com.example.alohaexplorer

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.Map3DView
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback
import com.google.android.gms.maps3d.Popover
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.Glyph
import com.google.android.gms.maps3d.model.ImageView
import com.google.android.gms.maps3d.model.Marker
import com.google.android.gms.maps3d.model.MarkerOptions
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
import com.google.android.gms.maps3d.model.pinConfiguration
import com.google.android.gms.maps3d.model.polygonOptions
import com.google.android.gms.maps3d.model.polylineOptions
import com.google.android.gms.maps3d.model.popoverOptions
import com.google.android.gms.maps3d.model.popoverShadow
import com.google.android.gms.maps3d.model.popoverStyle
import com.google.android.gms.maps3d.model.vector3D
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration
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

    // TODO: handle rotation bug... :/
    private val activeMarkers = mutableListOf<Marker>()
    private val activePolygons = mutableListOf<Polygon>()
    private val activePolylines = mutableListOf<Polyline>()
    private val activeModels = mutableListOf<Model>()
    private val activePopovers = mutableListOf<Popover>()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Fix: Ensure the bundle can deserialize the Maps 3D SDK's Camera Parcelable on rotation
        savedInstanceState?.classLoader = javaClass.classLoader
        super.onCreate(savedInstanceState)

        // Enables "Edge-to-Edge" mode, allowing the map to draw behind the system bars
        // (status bar and navigation bar) for a more immersive experience.
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        setUpInsets()

        // 1.3. Initialize Map3DView
        map3DView = findViewById(R.id.map3dView)
        
        // 1.4. Map Lifecycle
        // Manually trigger onCreate (and unpack any saved state from rotation)
        val mapState = savedStateRegistry.consumeRestoredStateForKey("map3d_state_provider")
        map3DView.onCreate(mapState)
        
        // Attach the automated Lifecycle Observer to gracefully handle 
        // onResume, onPause, onDestroy, and onSaveInstanceState.
        lifecycle.addObserver(Map3DLifecycleObserver(map3DView, this))

        // This starts the map loading process. onMap3DViewReady will be called when it's done.
        map3DView.getMap3DViewAsync(this)
    }

    /**
     * Called when the Map is fully loaded and ready to be used.
     * This is where we can start interacting with the `googleMap3D` object.
     */
    override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
        this@MainActivity.googleMap3D = googleMap3D

        lifecycleScope.launch {
            // 1.3. Start from Global View
            startFromGlobalView(googleMap3D)
            
            // Setup UI Buttons
            setupButtons(googleMap3D)
        }
    }

    private fun setupPopover(map: GoogleMap3D) {
        clearMap()

        // 9.1. Create a simple text view for the popover content
        val textView = TextView(this@MainActivity).apply {
            text = getString(R.string.toast_palace)
            setPadding(32, 16, 32, 16)
            setTextColor(Color.BLACK)
            setBackgroundColor(Color.WHITE)
        }

        // 9.2. Add a Popover attached to the same location
        val popover = map.addPopover(popoverOptions {
            positionAnchor = latLngAltitude {
                latitude = IOLANI_PALACE.latitude
                longitude = IOLANI_PALACE.longitude
                altitude = 10.0
            }
            altitudeMode = AltitudeMode.RELATIVE_TO_MESH
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

        // Track it
        activePopovers.add(popover)

        // Show immediately for demo purposes
        popover.show()
    }

    private suspend fun flyTour(map: GoogleMap3D) {
        val locations = listOf(
            HONOLULU to "Honolulu",
            DIAMOND_HEAD to "Diamond Head",
            KOKO_HEAD to "Koko Head",
            LANIKAI_BEACH to "Lanikai Beach",
            MOUNT_KAALA to "Mount Ka'ala",
            PEARL_HARBOR to "Pearl Harbor"
        )
        
        // Add all markers for the tour
        clearMap()
        locations.forEach { (location, name) ->
            activeMarkers.add(map.addMarker(
                markerOptions {
                    position = location
                    label = name
                    altitudeMode = AltitudeMode.CLAMP_TO_GROUND
                    isExtruded = true
                }
            )!!)
        }

        // Fly to each location
        for ((location, _) in locations) {
            val camera = camera {
                center = location
                tilt = 45.0
                range = 2500.0
                heading = 0.0
            }

            flyCameraToAndWait(
                map,
                camera,
                duration = 3.seconds)

            // Wait for the scene to load
            awaitMapSteady(map)

            map.flyCameraAround(
                flyAroundOptions {
                    center = camera
                    rounds = 1.0
                    durationInMillis = 3.seconds.inWholeMilliseconds
                }
            )

            // Pause at each location
            delay(1.5.seconds)
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
    private fun clearMap() {
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

    private fun addPolygon(map: GoogleMap3D) {
        clearMap()

        // Define the base (ground) shape of Iolani Palace.
        // Take the four corners around the place and build a rectangle around the base
        val palaceBaseFace = IOLANI_PALACE_GEO.windowed(2, 2).map {
            latLngAltitude { latitude = it[0]; longitude = it[1]; altitude = 0.0 }
        }.let { points -> points + points.first() }

        // **Extrusion**: Turn a 2D shape into a 3D building by duplicating the path upwards
        // and connecting the edges.
        val palaceCube = extrudePolygon(palaceBaseFace, 35.0) // 35 meters tall

        // Add all faces (top, bottom, sides) to the map
        palaceCube.forEach { facePoints ->
            map.addPolygon(
                polygonOptions {
                    path = facePoints
                    fillColor = Color.argb(100, 255, 215, 0) // Gold with transparency
                    strokeColor = Color.YELLOW
                    strokeWidth = 5.0
                    altitudeMode = AltitudeMode.ABSOLUTE
                }
            ).also { face ->
                // Remember the polygon for clean up
                activePolygons.add(face)

                // 6.1. Tapping the Turf
                // Add click listener to each face
                face.setClickListener {
                    lifecycleScope.launch(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity,
                            getString(R.string.the_royal_palace),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun setupPolylines(map: GoogleMap3D) {
        clearMap()

        // 7. Polylines
        // Draw path to Waikiki
        activePolylines.add(map.addPolyline(
            polylineOptions {
                path = listOf(IOLANI_PALACE, WAIKIKI)
                strokeWidth = 10.0
                strokeColor = Color.BLUE
            }
        ))
    }

    private fun setupBalloon(map: GoogleMap3D) {
        clearMap()

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
                Toast.makeText(this@MainActivity,
                    getString(R.string.clicked_the_balloon), Toast.LENGTH_SHORT).show()
            }
        }

        // Track it so we can remove it later
        activeModels.add(balloon)
    }


    /**
     * Helper function for adding a clickable marker.
     */
    private fun addClickableMarker(map: GoogleMap3D, markerOptions: MarkerOptions) {
        map.addMarker(markerOptions)?.also { marker ->
            marker.setClickListener {
                lifecycleScope.launch(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.toast_clicked, marker.label),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            activeMarkers.add(marker)
        }
    }

    /**
     * Demonstrates adding 3D markers with different [AltitudeMode]s.
     * Junior Dev Tip: Understanding `AltitudeMode` is key to placing objects correctly.
     */
    private fun addMarkers(map: GoogleMap3D) {
        clearMap()

        // 4.1. ABSOLUTE: Altitude is relative to the WGS84 ellipsoid (rough sea level).
        //    Good for aircraft, satellites, or data visualizations that ignore terrain.
        addClickableMarker(map, markerOptions {
            position = latLngAltitude {
                latitude = IOLANI_PALACE.latitude
                longitude = IOLANI_PALACE.longitude
                altitude = 100.0 // 100 meters above sea level
            }
            altitudeMode = AltitudeMode.ABSOLUTE
            label = getString(R.string.label_absolute)
            isDrawnWhenOccluded = true
            isExtruded = true // Draws a line to the ground
        })

        // 4.2. RELATIVE_TO_GROUND: Altitude is added to the terrain height at that point.
        //    Perfect for placing things "floating 50m above the ground".
        addClickableMarker(map, markerOptions {
            position = latLngAltitude {
                latitude = IOLANI_PALACE.latitude + 0.001
                longitude = IOLANI_PALACE.longitude
                altitude = 50.0 // 50m above whatever ground is here
            }
            altitudeMode = AltitudeMode.RELATIVE_TO_GROUND
            label = getString(R.string.label_relative)
            isDrawnWhenOccluded = true
            isExtruded = true
        })

        // 4.3. CLAMP_TO_GROUND: Altitude is ignored; object snaps to the terrain/mesh.
        //    Best for map pins, POIs, or anything on the surface.
        addClickableMarker(map, markerOptions {
            position = latLngAltitude {
                latitude = IOLANI_PALACE.latitude - 0.001
                longitude = IOLANI_PALACE.longitude
                altitude = 0.0 // Ignored
            }
            altitudeMode = AltitudeMode.CLAMP_TO_GROUND
            label = getString(R.string.label_clamped)
            isDrawnWhenOccluded = true
            isExtruded = true
        })
    }

    private fun addCustomMarkers(map: GoogleMap3D) {
        clearMap()

        // 4.4. Styled Pin
        addClickableMarker(map, markerOptions {
            position = latLngAltitude {
                latitude = IOLANI_PALACE.latitude
                longitude = IOLANI_PALACE.longitude + 0.002
                altitude = 0.0
            }
            label = getString(R.string.label_styled_pin)
            altitudeMode = AltitudeMode.CLAMP_TO_GROUND
            setStyle(pinConfiguration {
                backgroundColor = Color.BLUE
                borderColor = Color.WHITE
                scale = 1.5f
            })
        })

        // 4.5. Image Glyph (Hibiscus)
        addClickableMarker(map, markerOptions {
            position = latLngAltitude {
                latitude = IOLANI_PALACE.latitude
                longitude = IOLANI_PALACE.longitude + 0.004
                altitude = 0.0
            }
            label = getString(R.string.label_hibiscus)
            isExtruded = true
            isDrawnWhenOccluded = true
            altitudeMode = AltitudeMode.CLAMP_TO_GROUND
            setStyle(ImageView(R.drawable.hibiscus))
        })

        // 4.6. Text Glyph
        val glyphText = Glyph.fromColor(Color.YELLOW).apply {
            setText("🌸")
        }

        addClickableMarker(map, markerOptions {
            position = latLngAltitude {
                latitude = IOLANI_PALACE.latitude
                longitude = IOLANI_PALACE.longitude + 0.006
                altitude = 0.0
            }
            label = getString(R.string.label_text_glyph)
            isExtruded = true
            isDrawnWhenOccluded = true
            altitudeMode = AltitudeMode.CLAMP_TO_GROUND
            setStyle(pinConfiguration {
                setGlyph(glyphText)
                scale = 1.2f
                backgroundColor = Color.BLUE
                borderColor = Color.GREEN
            })
        })
    }


    private fun setUpInsets() {
        // **Window Insets Handling**:
        // Because we are in Edge-to-Edge mode, we must manually ensure our UI elements
        // don't get covered by system bars.
        // 1.2.1. Map: Needs padding at the TOP so the Google Logo/Compass don't overlap the status bar.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.map3dView)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        // 1.2.2. Controls: Need padding at the BOTTOM so buttons aren't covered by the gesture/nav bar.
        //    We add an extra 16dp base padding for aesthetics.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.controls_scroll_view)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val basePadding = (16 * resources.displayMetrics.density).toInt() // 16dp
            v.setPadding(
                systemBars.left,
                basePadding,
                systemBars.right,
                systemBars.bottom + basePadding
            )
            insets
        }
    }

    // ------------------------------------------------------------------------
    // PRE-PROVIDED BOILERPLATE (Kept out of the way)
    // ------------------------------------------------------------------------

    private var currentAnimationJob: Job? = null

    /**
     * Sets up click listeners for the UI buttons to trigger camera animations.
     * Note how we wrap the calls in `lifecycleScope.launch`. This is because:
     * 1.  The `flyTo...` functions are `suspend` functions (they take time to complete).
     * 2.  We want them to run on the main thread (UI interactions).
     */
    private fun setupButtons(map: GoogleMap3D) {
        findViewById<Button>(R.id.btn_fly_honolulu).setOnClickListener {
            // Cancel any ongoing tour or flight
            currentAnimationJob?.cancel()
            // "Launch" starts a new coroutine.
            currentAnimationJob = lifecycleScope.launch {
                clearMap()
                flyToHonolulu(map)
            }
        }

        findViewById<Button>(R.id.btn_show_markers).setOnClickListener {
            currentAnimationJob?.cancel()
            currentAnimationJob = lifecycleScope.launch {
                addMarkers(map)
                flyCameraToAndWait(map, MARKER_CAMERA)
            }
        }

        findViewById<Button>(R.id.btn_show_custom_markers).setOnClickListener {
            currentAnimationJob?.cancel()
            currentAnimationJob = lifecycleScope.launch {
                addCustomMarkers(map)
                flyCameraToAndWait(map, CUSTOM_MARKER_CAMERA)
            }
        }

        findViewById<Button>(R.id.btn_show_polygons).setOnClickListener {
            currentAnimationJob?.cancel()
            currentAnimationJob = lifecycleScope.launch {
                addPolygon(map)
                flyCameraToAndWait(map, POLYGON_CAMERA)
            }
        }

        findViewById<Button>(R.id.btn_show_polylines).setOnClickListener {
            currentAnimationJob?.cancel()
            currentAnimationJob = lifecycleScope.launch {
                setupPolylines(map)
                flyCameraToAndWait(map, BALLOON_CAMERA)
            }
        }

        findViewById<Button>(R.id.btn_show_balloon).setOnClickListener {
            currentAnimationJob?.cancel()
            currentAnimationJob = lifecycleScope.launch {
                setupBalloon(map)
                flyCameraToAndWait(map, BALLOON_CAMERA)
            }
        }

        findViewById<Button>(R.id.btn_show_popovers).setOnClickListener {
            currentAnimationJob?.cancel()
            currentAnimationJob = lifecycleScope.launch {
                setupPopover(map)
                flyCameraToAndWait(map, MARKER_CAMERA)
            }
        }

        findViewById<Button>(R.id.btn_tour).setOnClickListener {
            currentAnimationJob?.cancel()
            currentAnimationJob = lifecycleScope.launch { 
                flyTour(map) 
            }
        }

        findViewById<Button>(R.id.btn_clear).setOnClickListener {
            currentAnimationJob?.cancel()
            clearMap()
        }
    }

    private suspend fun flyCameraToAndWait(map: GoogleMap3D, camera: Camera, duration: Duration = 2.seconds) {
        map.flyCameraTo(options = flyToOptions {
            endCamera = camera
            durationInMillis = duration.inWholeMilliseconds
        })

        awaitCameraAnimation(map)
    }

}
