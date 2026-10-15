package com.example.alohaexplorer

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.Job

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

    private var currentAnimationJob: Job? = null

    companion object {
        // Constants are now located in HonoluluData.kt
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Fix: Ensure the bundle can deserialize the Maps 3D SDK's Camera Parcelable on rotation
        savedInstanceState?.classLoader = javaClass.classLoader
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
        
        // TODO: Step 1.4 - Map Lifecycle

        // TODO: Step 1.5 - Configure the UI buttons
        // setupButtons()
    }

    // TODO: Step 1.3 - Implement onMap3DViewReady
    /*
    override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
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
        // Wait for the animation to finish
        // Wait for tiles to load and settle
        // Wait for the animation to finish
    }
    */

    // TODO: Step 2 - Helper to fly to markers (for other buttons)
    /*
    private suspend fun flyToMarkers(map: GoogleMap3D) {
        // Fly to the Iolani Palace
        // Wait for the animation to finish
    }
    */

    /*
    private suspend fun flyToBalloon(map: GoogleMap3D) {
        // Fly to the Balloon on Waikiki Beach
        // Wait for the animation to finish
    }
    */

    // See Utilities.kt for Robustness helpers (awaitMapSteady, awaitCameraAnimation)
    
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

        // 1. Add ABSOLUTE Marker
        
        // 2. Add RELATIVE_TO_GROUND Marker
        
        // 3. Add CLAMP_TO_GROUND Marker
    }
    */

    // TODO: Step 5 - Advanced Custom Markers
    /*
    private fun addCustomMarkers(map: GoogleMap3D) {
        // Implementation provided in Advanced Custom Markers step
    }
    */

    // TODO: Step 6 - Add Polygon
    /*
    private fun addPolygon(map: GoogleMap3D) {
        resetMap()

        // Define the base (ground) shape of Iolani Palace
        
        // Extrude!
        
        // Add all faces to the map
        
        // Step 6: Tapping the Turf
    }
    */
    
    // See Utilities.kt for extrudePolygon Algorithm
    // TODO: Step 8 - Setup Polylines
    /*
    private fun setupPolylines(map: GoogleMap3D) {
        resetMap()

        // Draw path to Waikiki
        
        // Jump the camera to the Polyline to see it
    }
    */

    // TODO: Step 9 - Setup Balloon Model
    /*
    private fun setupBalloon(map: GoogleMap3D) {
        resetMap()

        // Add "Balloon" Model (glTF)
        
        // Setup click listener
    }
    */
    
    // TODO: Step 10 - Setup Popover
    /*
    private fun setupPopover(map: GoogleMap3D) {
        resetMap()

        // 1. Add a marker to serve as a visual anchor reference
        
        // 2. Create a simple text view for the popover content
        
        // 3. Add a Popover attached to the same location
        
        // 4. Show popover on marker click
    }
    */
    
    // TODO: Step 11 - Scenic Tour Challenge
    /*
    private suspend fun flyTour(map: GoogleMap3D) {
        // Define Locations
        
        // Add all markers for the tour
        
        // Fly to each location (CHALLENGE!)
        // 1. Tell the map to flyCameraTo this 'location'. 
        // 2. Wait for the animation to finish using awaitCameraAnimation(map)
        // 3. Optional: Add a delay so the user can enjoy the view before flying to the next!
    }
    */

    // TODO: Step 1.2 - Uncomment the setupButtons boilerplate after adding Maps 3D SDK dependencies
    /*

    /**
     * Sets up click listeners for the UI buttons to trigger camera animations.
     * Note how we wrap the calls in `lifecycleScope.launch`. This is because:
     * 1.  The `flyTo...` functions are `suspend` functions (they take time to complete).
     * 2.  We want them to run on the main thread (UI interactions).
     */
    private fun setupButtons(map: GoogleMap3D) {
        findViewById<Button>(R.id.btn_fly_honolulu).setOnClickListener {
            currentAnimationJob?.cancel()
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
            resetMap()
        }
    }

    private suspend fun flyCameraToAndWait(map: GoogleMap3D, camera: Camera, duration: Duration = 2.seconds) {
        map.flyCameraTo(options = flyToOptions {
            endCamera = camera
            durationInMillis = duration.inWholeMilliseconds
        })
        // See Utilities.kt
        awaitCameraAnimation(map)
    }
    */


}