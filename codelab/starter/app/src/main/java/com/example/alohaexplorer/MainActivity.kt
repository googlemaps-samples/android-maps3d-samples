package com.example.alohaexplorer

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.Job
// TODO: Step 1.2 - Uncomment imports after adding the Maps 3D SDK dependency
// import android.graphics.Color
// import android.widget.Button
// import android.widget.TextView
// import android.widget.Toast
// import androidx.lifecycle.lifecycleScope
// import com.google.android.gms.maps3d.GoogleMap3D
// import com.google.android.gms.maps3d.Map3DView
// import com.google.android.gms.maps3d.OnMap3DViewReadyCallback
// import com.google.android.gms.maps3d.Popover
// import com.google.android.gms.maps3d.model.AltitudeMode
// import com.google.android.gms.maps3d.model.Camera
// import com.google.android.gms.maps3d.model.Glyph
// import com.google.android.gms.maps3d.model.ImageView
// import com.google.android.gms.maps3d.model.Marker
// import com.google.android.gms.maps3d.model.MarkerOptions
// import com.google.android.gms.maps3d.model.Model
// import com.google.android.gms.maps3d.model.Polygon
// import com.google.android.gms.maps3d.model.Polyline
// import com.google.android.gms.maps3d.model.camera
// import com.google.android.gms.maps3d.model.flyAroundOptions
// import com.google.android.gms.maps3d.model.flyToOptions
// import com.google.android.gms.maps3d.model.latLngAltitude
// import com.google.android.gms.maps3d.model.markerOptions
// import com.google.android.gms.maps3d.model.modelOptions
// import com.google.android.gms.maps3d.model.orientation
// import com.google.android.gms.maps3d.model.pinConfiguration
// import com.google.android.gms.maps3d.model.polygonOptions
// import com.google.android.gms.maps3d.model.polylineOptions
// import com.google.android.gms.maps3d.model.popoverOptions
// import com.google.android.gms.maps3d.model.popoverShadow
// import com.google.android.gms.maps3d.model.popoverStyle
// import com.google.android.gms.maps3d.model.vector3D
// import kotlin.time.Duration
// import kotlin.time.Duration.Companion.seconds
// import kotlinx.coroutines.Dispatchers
// import kotlinx.coroutines.delay
// import kotlinx.coroutines.launch

/**
 * This is the main entry point for the "Aloha Explorer" 3D Maps Codelab.
 */
class MainActivity : AppCompatActivity() /*, TODO: Step 1.2 - Implement OnMap3DViewReadyCallback */ {

    // TODO: Step 1.2 - Define Map3DView and GoogleMap3D
    // private lateinit var map3DView: Map3DView
    // private var googleMap3D: GoogleMap3D? = null

    // TODO: Step 1.2 - State Management
    // private val activeMarkers = mutableListOf<Marker>()
    // private val activePolygons = mutableListOf<Polygon>()
    // private val activePolylines = mutableListOf<Polyline>()
    // private val activeModels = mutableListOf<Model>()
    // private val activePopovers = mutableListOf<Popover>()

    // Constants are located in HonoluluData.kt

    override fun onCreate(savedInstanceState: Bundle?) {
        // Fix: Ensure the bundle can deserialize the Maps 3D SDK's Camera Parcelable on rotation
        savedInstanceState?.classLoader = javaClass.classLoader
        super.onCreate(savedInstanceState)

        // Enables "Edge-to-Edge" mode, allowing the map to draw behind the system bars
        // (status bar and navigation bar) for a more immersive experience.
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        setUpInsets()

        // TODO: Step 1.3 - Initialize Map3DView and Map Lifecycle
    }

    // TODO: Step 1.2 - Reset Map Helper
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

    // TODO: Step 1.3 - Implement onMap3DViewReady
    /*
    override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
        this@MainActivity.googleMap3D = googleMap3D

        lifecycleScope.launch {
            // 1.3. Start from Global View
            startFromGlobalView(googleMap3D)

            // Setup UI Buttons
            setupButtons(googleMap3D)
        }
    }
    */

    // TODO: Step 1.3 - Start from Global View
    /*
    private fun startFromGlobalView(map: GoogleMap3D) {
        // TODO: Step 1.3 - Set the Camera
    }
    */

    // TODO: Step 2 & Step 3 - Fly to Honolulu and Orbit
    // See Utilities.kt for Robustness helpers (awaitMapSteady, awaitCameraAnimation)
    /*
    private suspend fun flyToHonolulu(map: GoogleMap3D) {
        // TODO: Step 2 & Step 3 - Fly to Honolulu, wait for tiles to settle, and orbit
    }
    */

    // TODO: Step 4 - Add Clickable Marker Helper
    /*
    private fun addClickableMarker(map: GoogleMap3D, markerOptions: MarkerOptions) {
        // Add marker, attach click listener with Toast, and track in activeMarkers
    }
    */

    // TODO: Step 4 - Add Markers
    /*
    private fun addMarkers(map: GoogleMap3D) {
        resetMap()

        // 4.1. ABSOLUTE: Altitude is relative to the WGS84 ellipsoid (rough sea level).

        // 4.2. RELATIVE_TO_GROUND: Altitude is added to the terrain height at that point.

        // 4.3. CLAMP_TO_GROUND: Snaps to the terrain.

        // 4.4. RELATIVE_TO_MESH: Relative to 3D buildings/trees.
    }
    */

    // TODO: Step 5 - Advanced Custom Markers
    /*
    private fun addCustomMarkers(map: GoogleMap3D) {
        resetMap()

        // 5.1. Styled Pin

        // 5.2. Image Glyph (Hibiscus)

        // 5.3. Text Glyph
    }
    */

    // TODO: Step 6 & Step 7 - Add Extruded Polygon & Click Listener
    // See Utilities.kt for extrudePolygon Algorithm
    /*
    private fun addPolygon(map: GoogleMap3D) {
        resetMap()

        // Define the base (ground) shape of Iolani Palace

        // Extrude!

        // Add all faces to the map and attach click listeners (Step 7: Tapping the Turf)
    }
    */

    // TODO: Step 8 - Setup Polylines
    /*
    private fun setupPolylines(map: GoogleMap3D) {
        resetMap()

        // Draw geodesic path from Iolani Palace to Waikiki
    }
    */

    // TODO: Step 9 - Setup Balloon Model
    /*
    private fun setupBalloon(map: GoogleMap3D) {
        resetMap()

        // Add "Balloon" Model (glTF), attach click listener, and track in activeModels
    }
    */

    // TODO: Step 10 - Setup Popover
    /*
    private fun setupPopover(map: GoogleMap3D) {
        resetMap()

        // 10.1. Add a marker to serve as a visual anchor reference

        // 10.2. Create a simple text view for the popover content

        // 10.3. Add a Popover attached to the marker

        // 10.4. Show popover on marker click (and show immediately for demo purposes)
    }
    */

    // TODO: Step 11 - Scenic Tour Challenge
    /*
    private suspend fun flyTour(map: GoogleMap3D) {
        // 11.1. Define Tour Locations

        // 11.2. Reset map and add markers for all tour stops

        // 11.3. Fly to each location (CHALLENGE!):
        //   1. Create a target Camera (tilt = 45.0, range = 2500.0, heading = 0.0)
        //   2. Fly to the camera and wait using flyCameraToAndWait(map, camera, duration = 3.seconds)
        //   3. Wait for 3D tiles to settle using map.awaitMapSteady(5.seconds)
        //   4. Orbit 360 degrees using map.awaitCameraAnimation(flyAroundOptions { ... })
        //   5. Pause briefly using delay(1.5.seconds) before moving to the next stop
    }
    */

    private fun setUpInsets() {
        // **Immersive Mode**:
        // Hide the system bars (status bar and navigation bar) for a full-screen 3D map
        // experience. Swiping from the screen edge reveals them transiently.
        // Remove these lines if you prefer to keep the system bars visible at all times.
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        // **Window Insets Handling**:
        // Because we are in Edge-to-Edge mode, we ensure our bottom UI controls
        // don't get covered by the navigation bar when system bars are visible.
        // We add an extra 16dp base padding for aesthetics.
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

    // TODO: Step 1.3 - Uncomment the setupButtons boilerplate after adding Maps 3D SDK dependencies
    /*
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
                resetMap()
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
                flyCameraToAndWait(map, POLYLINE_CAMERA)
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
            resetMap()
        }
    }

    private suspend fun flyCameraToAndWait(map: GoogleMap3D, camera: Camera, duration: Duration = 2.seconds) {
        map.awaitCameraAnimation(
            flyToOptions {
                endCamera = camera
                durationInMillis = duration.inWholeMilliseconds
            }
        )
    }
    */
}
