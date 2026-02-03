# Codelab: Aloha Explorer — Building with the 3D Maps SDK for Android

Welcome to the future of mobile mapping! In this codelab, we are going to build **Aloha Explorer**, a 3D interactive tour of historic Iolani Palace in Honolulu.

Using the **Google Maps 3D SDK for Android**, you will transform a standard flat map into an immersive 3D experience. We will start with a basic "Hello World" app and add:

*   **Cinematic Camera**: A smooth flight from space down to Honolulu.
*   **3D Markers**: Floating markers pinned to specific altitudes.
*   **3D Volumes**: An extruded polygon visualizing the palace grounds.
*   **3D Models**: A high-fidelity glTF model (a Balloon) floating above the scene.

### What you’ll learn

*   **Initialization**: How to set up the `Map3DView` and handle its lifecycle.
*   **Camera Control**: How to orchestrate smooth animations using Coroutines.
*   **Altitude Modes**: Understanding `ABSOLUTE`, `RELATIVE_TO_GROUND`, `CLAMP_TO_GROUND`, and `RELATIVE_TO_MESH`.
*   **Extrusion**: Turning 2D footprints into 3D volumes.
*   **3D Models**: Loading and placing glTF assets.
*   **Interaction**: Handling click events on 3D objects.

---

## Prerequisites

Before we start coding, ensure your environment is ready.

1.  **Enable the SDK**:
    *   Go to the [Google Cloud Console](https://console.cloud.google.com/marketplace/product/google/mapsandroid.googleapis.com).
    *   Select your project and click **Enable** to turn on the "Maps 3D SDK for Android".
    *   Make sure you have an API Key created in the "Credentials" section.

2.  **Dependencies**:
    *   Open `gradle/libs.versions.toml` and add the secrets plugin to the `[versions]` and `[plugins]` sections:
        ```toml
        [versions]
        # ... existing versions ...
        secretsGradlePlugin = "2.0.1"

        [plugins]
        # ... existing plugins ...
        secrets-gradle-plugin = { id = "com.google.android.libraries.mapsplatform.secrets-gradle-plugin", version.ref = "secretsGradlePlugin" }
        ```
    *   Open `app/build.gradle.kts` and apply the plugin:
        ```kotlin
        plugins {
            // ... other plugins
            alias(libs.plugins.secrets.gradle.plugin)
        }

        // Add this block to configure the plugin
        secrets {
            propertiesFileName = "secrets.properties"
            defaultPropertiesFileName = "local.defaults.properties"
        }
        ```
    *   Ensure the Maps 3D SDK dependency is also present in `dependencies { ... }`:
        ```kotlin
        implementation(libs.play.services.maps3d)
        implementation(libs.androidx.lifecycle.runtime.ktx)
        ```

3.  **API Key**:
    *   Create a file named `secrets.properties` in your project's root directory.
    *   Add your API key to this file:
        ```properties
        MAPS3D_API_KEY=YOUR_ACTUAL_API_KEY
        ```
    *   **Note**: `secrets.properties` should NOT be committed to version control.
    *   Create another file named `local.defaults.properties` in the root directory.
    *   Add a fallback value:
        ```properties
        MAPS3D_API_KEY=DEFAULT_API_KEY
        ```
    *   This file *should* be committed to version control to verify which keys are required.
    *   Open `app/src/main/AndroidManifest.xml` and add the following metadata tag inside the `<application>` element:
        ```xml
        <meta-data
            android:name="com.google.android.geo.maps3d.API_KEY"
            android:value="${MAPS3D_API_KEY}" />
        ```
        The secrets plugin will inject the key from `secrets.properties` into this placeholder.

---

## 1. Setting the Stage (Layout & Init)

Before we can explore the islands, we need to set up our "cockpit". We need a layout that gives the map full focus while keeping our controls accessible.

### Step 1.1: The Layout

Open `app/src/main/res/layout/activity_main.xml`. We want a full-screen 3D map with a scrollable row of control buttons at the bottom.

**Key Changes:**
1.  **Map3DView**: This is our main canvas. We place it at the top.
2.  **HorizontalScrollView**: This holds our buttons. We verify it's pinned to the *bottom* of the screen.

```xml
<androidx.constraintlayout.widget.ConstraintLayout ...>

    <com.google.android.gms.maps3d.Map3DView
        android:id="@+id/map3dView"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        app:mode="hybrid"
        app:centerLat="21.3069"
        app:centerLng="-157.8583"
        app:centerAlt="0"
        app:heading="0"
        app:tilt="0"
        app:range="5000000"
        app:roll="0"
        app:minAltitude="0"
        app:maxAltitude="10000000"
        app:minHeading="0"
        app:maxHeading="360"
        app:minTilt="0"
        app:maxTilt="90"
        app:layout_constraintBottom_toTopOf="@id/controls_scroll_view"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

    <HorizontalScrollView
        android:id="@+id/controls_scroll_view"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="#FFFFFF"
        android:paddingVertical="16dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent">

        <LinearLayout
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:paddingHorizontal="16dp">

            <Button
                android:id="@+id/btn_fly_honolulu"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Camera"
                android:layout_marginEnd="8dp"/>

            <Button
                android:id="@+id/btn_show_markers"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Markers"
                android:layout_marginEnd="8dp"/>

            <Button
                android:id="@+id/btn_show_polygons"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Polygons"
                android:layout_marginEnd="8dp"/>

            <Button
                android:id="@+id/btn_show_popovers"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Info"
                android:layout_marginEnd="8dp"/>

            <Button
                android:id="@+id/btn_show_balloon"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Models" />

        </LinearLayout>
    </HorizontalScrollView>

</androidx.constraintlayout.widget.ConstraintLayout>
```

### Step 1.2: Project Skeleton

Open `MainActivity.kt`. First, we'll set up the class structure, binding constants, and helper variables. We also handle the "Edge-to-Edge" UI logic here.

**Tasks**:
1.  Define **Constants** for our locations and assets.
2.  Setup **State Management** lists to track objects.
3.  Handle **Window Insets** in `onCreate`.

```kotlin
// Maps 3D SDK
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.Map3DView
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.android.gms.maps3d.model.Marker
import com.google.android.gms.maps3d.model.Model
import com.google.android.gms.maps3d.model.Polygon
import com.google.android.gms.maps3d.model.Polyline
import com.google.android.gms.maps3d.model.Popover
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.flyAroundOptions
import com.google.android.gms.maps3d.model.flyToOptions
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.gms.maps3d.model.markerOptions
import com.google.android.gms.maps3d.model.modelOptions
import com.google.android.gms.maps3d.model.orientation
import com.google.android.gms.maps3d.model.polygonOptions
import com.google.android.gms.maps3d.model.polylineOptions
import com.google.android.gms.maps3d.model.popoverOptions
import com.google.android.gms.maps3d.model.popoverShadow
import com.google.android.gms.maps3d.model.popoverStyle
import com.google.android.gms.maps3d.model.vector3D

// Android UI
import android.graphics.Color
import android.widget.TextView

// Coroutines & Lifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.seconds

class MainActivity : AppCompatActivity(), OnMap3DViewReadyCallback {

    private lateinit var map3DView: Map3DView
    private var googleMap3D: GoogleMap3D? = null

    // State Management: Track objects to remove them later
    private val activeMarkers = mutableListOf<Marker>()
    private val activePolygons = mutableListOf<Polygon>()
    private val activePolylines = mutableListOf<Polyline>()
    private val activeModels = mutableListOf<Model>()
    private val activePopovers = mutableListOf<Popover>()

    companion object {
        // Locations
        val HONOLULU = latLngAltitude { latitude = 21.3069; longitude = -157.8583; altitude = 0.0 }
        val IOLANI_PALACE = latLngAltitude { latitude = 21.306740; longitude = -157.858803; altitude = 0.0 }
        val WAIKIKI = latLngAltitude { latitude = 21.2766; longitude = -157.8286; altitude = 0.0 }
        
        // Assets
        // 3D models must be hosted online and reachable via a URL (e.g. Cloud Storage).
        const val BALLOON_MODEL_URL = "https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/balloon-pin-BlXF32yD.glb"
        const val BALLOON_SCALE = 5.0
        
        // Geometry
        val IOLANI_PALACE_GEO = listOf(
            21.307180365, -157.858769898,
            21.306765552, -157.858390366,
            21.306476932, -157.858755146,
            21.306892995, -157.859134679,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Handle Insets (Status Bar & Nav Bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.map3dView)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.controls_scroll_view)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val basePadding = (16 * resources.displayMetrics.density).toInt() // 16dp
            v.setPadding(systemBars.left, basePadding, systemBars.right, systemBars.bottom + basePadding)
            insets
        }
    }

    // Helper to clear the map before adding new content
    private fun resetMap() {
        activeMarkers.forEach { it.remove() }; activeMarkers.clear()
        activePolygons.forEach { it.remove() }; activePolygons.clear()
        activePolylines.forEach { it.remove() }; activePolylines.clear()
        activeModels.forEach { it.remove() }; activeModels.clear()
        activePopovers.forEach { it.remove() }; activePopovers.clear()
    }

    // We'll implement onMap3DViewReady in the next step!
    override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
        // TODO: Initialize map
    }
}
```

### Step 1.3: Initialize the Map

Now separate the boilerplate from the logic. We will initialize the `Map3DView` and set up our callback.

1.  **Initialize View**: Add the `map3DView` setup code to the end of `onCreate`.
2.  **Handle Callback**: Implement `onMap3DViewReady` to receive the `GoogleMap3D` controller.
3.  **Setup Controls**: Configure the bottom sheet buttons.

Add this logic to `MainActivity.kt`:

```kotlin
    override fun onCreate(savedInstanceState: Bundle?) {
        // ... previous code ...
        
        // Initialize the Map3DView
        map3DView = findViewById(R.id.map3dView)
        map3DView.onCreate(savedInstanceState)
        map3DView.getMap3DViewAsync(this)
    }

    override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
        this@MainActivity.googleMap3D = googleMap3D

        lifecycleScope.launch {
            // Start from space!
            startFromGlobalView(googleMap3D)
            
            // Wire up buttons
            setupButtons(googleMap3D)
        }
    }

    // Wire up UI buttons to functions
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
            lifecycleScope.launch { flyToMarkers(map) } // Re-use marker view for polygons
        }
        findViewById<Button>(R.id.btn_show_balloon).setOnClickListener {
            setupBalloon(map)
            lifecycleScope.launch { flyToBalloon(map) }
        }
        findViewById<Button>(R.id.btn_show_popovers).setOnClickListener {
            setupPopover(map)
            // No flight needed, we reuse the camera if we are already there, or we could fly to it.
            // Let's fly to the same spot as markers for now.
             lifecycleScope.launch { flyToMarkers(map) }
        }
    }
```
> **Note**: You will see red errors for `startFromGlobalView`, `flyToHonolulu`, and other helper functions. Don't worry! We will implement these in the next sections.
```

---

## 2. The Royal Flyover (Camera Mastery)

Now that we’re in Hawaii, let's head to the historic **Iolani Palace**. Unlike 2D maps where you just "pan", 3D maps allow cinematic camera movements.

### Objective
We want to fly from space down to Honolulu, and then orbit the palace.

### The Setup
We use `flyToOptions` to define the flight.

```kotlin
private suspend fun flyToHonolulu(map: GoogleMap3D) {
    // Duration using Kotlin Duration extension
    val flyDuration = 5.seconds
    
    println("Flying to Honolulu...")
    map.flyCameraTo(
        flyToOptions {
            endCamera = camera {
                center = HONOLULU
                tilt = 45.0 // Angled view
                range = 20000.0 // 20km away
            }
            durationInMillis = flyDuration.inWholeMilliseconds
        }
    )
    // Wait...
}
```

---

## 3. Robustness (The Secret Sauce)

Here is where many developers trip up. Animations take time. Network loading takes time. If you just chain commands together immediately, the camera might try to move before the map is ready.

**The Solution: Suspending Helper Functions**

We wrap the SDK's callback-based listeners into Kotlin Coroutine `suspend` functions. This lets us write linear, readable code like "Fly THERE, then wait, then Fly HERE".

```kotlin
// Pauses execution until the camera stops moving
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

// Pauses execution until the map tiles are fully loaded and steady
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
```

Now our flight plan looks like this:

```kotlin
    map.flyCameraTo(...)
    awaitCameraAnimation(map) // Wait for flight
    awaitMapSteady(map)       // Wait for tiles to load
    map.flyCameraAround(...)  // Start orbit
```

---

## 4. Sticking the Landing (Markers & Altitude)

Objects in a 3D world need to know where they sit on the vertical "Z-axis".

### The 3 Modes of Altitude
1.  **ABSOLUTE**: Relative to sea level (WGS84). Good for airplanes.
2.  **RELATIVE_TO_GROUND**: Relative to the terrain height. Good for things floating *above* the ground.
3.  **CLAMP_TO_GROUND**: Snaps to the terrain. Good for POIs.
4.  **RELATIVE_TO_MESH**: Relative to the actual 3D objects (buildings/trees). Good for placing things on rooftops.

### Helper: `resetMap()`
You noticed we call `resetMap()` at the start. This prevents "ghost objects"—markers from previous clicks staying on the map. We iterate through our tracking lists (`activeMarkers`, etc.), call `.remove()` on each object to clear it from the 3D engine, and then clear the list.

```kotlin
private fun addMarkers(map: GoogleMap3D) {
    resetMap()

    buildList {
        // 1. ABSOLUTE: Altitude is relative to the WGS84 ellipsoid (rough sea level).
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

        // 2. RELATIVE_TO_GROUND: Altitude is added to the terrain height at that point.
        add(map.addMarker(
            markerOptions {
                position = latLngAltitude {
                    latitude = IOLANI_PALACE.latitude + 0.001 // Offset slightly
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

        // 3. CLAMP_TO_GROUND: Snaps to the terrain.
        add(map.addMarker(
            markerOptions {
                position = latLngAltitude {
                    latitude = IOLANI_PALACE.latitude - 0.001 // Offset slightly
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

<!-- TODO: Add screenshot here.
     Subject: "Three Markers"
     Description: Show the three markers side-by-side.
     - "Absolute" marker should be floating high up.
     - "Relative" marker should be floating slightly above ground.
     - "Clamped" marker should be on the ground.
     Highlight the visual vertical line (extrusion) connecting the floating markers to the ground. 
-->
```

---

## 5. Highlighting History (Polygons & Extrusion)

Let's highlight the Palace grounds. A flat polygon is okay, but a 3D volume is better.

### Extrusion Algorithm
We can "extrude" a flat shape by:
1.  Taking the base coordinates.
2.  Duplicating them at a higher altitude (the "roof").
3.  Stitching the sides together with new polygons.

```kotlin
// Define the base (ground) shape of Iolani Palace.
// Uses the constant defined in companion object
val palaceBaseFace = IOLANI_PALACE_GEO.windowed(2, 2).map { 
     latLngAltitude { latitude = it[0]; longitude = it[1]; altitude = 0.0 }
}.let { points -> points + points.first() }

// Extrude!
val extrudedPalace = extrudePolygon(palaceBaseFace, 35.0) // 35 meters tall
```

When we add these faces to the map, we use `AltitudeMode.ABSOLUTE` to ensure the building keeps its shape perfectly.

<!-- TODO: Add screenshot here.
     Subject: "Extruded Palace"
     Description: Show the Iolani Palace with the semi-transparent gold polygon extruded upwards. 
     Camera should be tilted to show the 3D volume, not just the top-down view.
-->

---

## 6. Interaction (Touching the World)

A map isn't an image; it's an interface. Let's make our markers interactive.

```kotlin
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
```

We can do the same for our 3D Models!

---

## 7. Up, Up, and Away (The Balloon)

Finally, let's have some fun. We'll load a 3D asset (a glTF file) of a Hot Air Balloon and place it over Waikiki.

### Adding a Model
Models are just like markers, but with 3D geometry.

```kotlin
val balloon = map.addModel(
    modelOptions {
        position = latLngAltitude {
            latitude = WAIKIKI.latitude
            longitude = WAIKIKI.longitude
            altitude = 20.0 // Floating above the beach
        }
        url = "https://.../balloon.glb"
        scale = vector3D { x = 5.0; y = 5.0; z = 5.0 } // Make it big!
    }
)
```

And ensuring it's clickable:

```kotlin
balloon.setClickListener {
    lifecycleScope.launch(Dispatchers.Main) {
        Toast.makeText(this@MainActivity, "Clicked the Balloon!", Toast.LENGTH_SHORT).show()
    }
}

<!-- TODO: Add screenshot here.
     Subject: "The Balloon"
     Description: Show the 3D Balloon model floating over Waikiki.
     Include the blue polyline path visible in the background if possible.
-->
```

---

## 8. Popovers (Info Windows)

Markers are great, but sometimes you need to show more information. **Popovers** are 2D views that "stick" to a 3D location. Unlike Markers, they always face the camera and can contain any Android View (button, text, image, etc.).

### Creating a Popover

In this example, we will attach a "Hello World" message to the Iolani Palace marker.

1.  **Create the View**: First, we create a standard Android `TextView` programmatically.
2.  **Configure the Popover**: We use `popoverOptions` to define its anchor point and style.

```kotlin
    private fun setupPopover(map: GoogleMap3D) {
        resetMap()

        // 1. Add a marker to serve as a visual anchor reference
        val marker = map.addMarker(markerOptions {
            position = latLngAltitude {
                latitude = IOLANI_PALACE.latitude
                longitude = IOLANI_PALACE.longitude
                altitude = 50.0 // Floating, matching our "Relative" example
            }
            altitudeMode = AltitudeMode.RELATIVE_TO_GROUND
            label = "Click me!"
        })
        marker?.let { activeMarkers.add(it) }

        // 2. Create a simple text view for the popover content
        // In a real app, you could inflate this from XML using layoutInflater
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
            isAutoCloseEnabled = true // Close when user clicks elsewhere
            isAutoPanEnabled = true   // Move camera to ensure popover is visible
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
        popover?.let { activePopovers.add(it) }
        
        // 4. Show popover on marker click
        marker?.setClickListener {
            popover?.show()
        }
        
        // Show immediately for demo purposes
        popover?.show()
    }
```

Popovers bridge the gap between the 3D world and 2D information. They are perfect for labels, detailed info windows, or even interactive menus.

<!-- TODO: Add screenshot here.
     Subject: "Popover Example"
     Description: Show the "Welcome to Iolani Palace!" popover visible above the Iolani Palace marker.
     Highlight the popover's clean white background and shadow.
-->

---

## 9. Bonus: Jetpack Compose

Prefer **Jetpack Compose** over XML? The Maps 3D SDK is View-based, but is a perfect candidate for `AndroidView`.

We have included a full reference implementation in `Map3DComposeActivity.kt` (in the solution code). Here is a complete guide to recreating it.

### 1. Configure Options
First, define how the map should initialize.

```kotlin
val map3DOptions = Map3DOptions(
    centerLat = HONOLULU.latitude,
    centerLng = HONOLULU.longitude,
    centerAlt = 1000.0,
    heading = 0.0,
    range = 10_000_000.0, // Start from space
    mapMode = Map3DMode.HYBRID
)
```

### 2. The Composable Wrapper

We'll build the `Map3DContainer` step-by-step.

#### 2.1 State & AndroidView Skeleton

First, we need to bridge the gap between Compose and the View system using `AndroidView`. We also need to hold the `GoogleMap3D` object in our Compose state.

```kotlin
@Composable
fun Map3DContainer(
    modifier: Modifier = Modifier,
    options: Map3DOptions
) {
    // State to hold the map controller
    var googleMap by remember { mutableStateOf<GoogleMap3D?>(null) }
    
    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                // TODO: Initialize View
                Map3DView(context, options) 
            }
        )
    }
}
```

#### 2.2 Initializing the View (`factory`)

The `factory` lambda is called *once* to create the View. We must manually initialize the `Map3DView`'s lifecycle here.

```kotlin
factory = { context ->
    Map3DView(context, options).apply {
        // Manually call onCreate. In a real app, wire this to LifecycleOwner.
        onCreate(null)
    }
},
```

#### 2.3 Capturing the Map (`update`)

The `update` lambda runs when the Composable recomposes. We use `getMap3DViewAsync` to extract the `GoogleMap3D` controller and save it to our state.

```kotlin
update = { view ->
    view.getMap3DViewAsync(
        object : OnMap3DViewReadyCallback {
            override fun onMap3DViewReady(map3D: GoogleMap3D) {
                googleMap = map3D
            }
            override fun onError(e: Exception) {
                googleMap = null
                throw e
            }
        }
    )
},
```

#### 2.4 Cleanup (`onRelease`)

To prevent memory leaks, we must destroy the view when the Composable is removed from the screen.

```kotlin
onRelease = { view -> 
    googleMap = null
    view.onDestroy() 
}
```

#### 2.5 Adding Interaction

Finally, let's overlay a Button to trigger our cinematic flight. We use a `Job` to manage the animation cancellation.

```kotlin
// Add this state to track animations
var animationJob by remember { mutableStateOf<Job?>(null) }
val coroutineScope = rememberCoroutineScope()

// ... inside Box, after AndroidView ...

Button(
    // Enable only if map is ready and not currently animating
    enabled = googleMap != null && animationJob == null,
    onClick = {
        animationJob?.cancel()
        animationJob = coroutineScope.launch {
            // 1. Fly to Honolulu
            googleMap?.flyCameraTo(flyToOptions {
                endCamera = camera {
                    center = HONOLULU
                    tilt = 45.0
                    range = 20000.0
                }
                durationInMillis = 2000L
            })
            googleMap?.let { awaitCameraAnimation(it) }

            // 2. Orbit
            googleMap?.flyCameraAround(flyAroundOptions {
                center = camera {
                    center = HONOLULU
                    tilt = 45.0
                    range = 20000.0
                }
                rounds = 1.0
                durationInMillis = 5000L
            })
            googleMap?.let { awaitCameraAnimation(it) }
            
            animationJob = null
        }
    },
    modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 32.dp)
) {
    Text("Fly to Honolulu")
}
```

### 3. The Activity Setup
Finally, use `setContent` to display your Composable.

```kotlin
class Map3DComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                Map3DContainer(
                    modifier = Modifier.padding(innerPadding),
                    options = map3DOptions
                )
            }
        }
    }
}
```

This pattern gives you the best of both worlds: the power of the 3D Maps SDK and the modern UI of Jetpack Compose. Check out `Map3DComposeActivity.kt` for the complete code!

<!-- TODO: Add screenshot here.
     Subject: "Compose Integration"
     Description: Show the "Fly to Honolulu" Compose button overlaid on the 3D Map.
     The visual style of the button should clearly look like a native Compose Material3 component.
-->

---

## Finish & Next Steps

Mahalo for completing the **Aloha Explorer** codelab! You’ve gone from a empty activity to a cinematic, interactive 3D experience.

**You learned how to:**
*   Manage the `Map3DView` lifecycle.
*   Control the camera with Coroutines.
*   Master `AltitudeMode` for perfect placement.
*   Import and click 3D Models.

**The world is no longer flat—go build something amazing!**
