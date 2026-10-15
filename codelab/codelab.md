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

2.  **Add Your API Key**:
    To authenticate with Google Maps Platform services, your app must provide an API key. We use the **Secrets Gradle Plugin** to do this securely. 
    
    > **[!NOTE] Why the Secrets Plugin?**
    > Hardcoding an API key into your Android Manifest or source code is dangerous! If you check your code into GitHub, malicious bots can scrape your key! 
    > 
    > The Secrets Gradle Plugin extracts the key from a local text file and injects it into the build process securely without exposing it to version control.
    
    The `starter` project comes with the Secrets plugin pre-configured for you! All you need to do is create a new file called `secrets.properties` in the root of your project and add:
    ```properties
    MAPS_API_KEY=YOUR_API_KEY_HERE
    ```

3.  **Dependencies**:
    Finally, you need to import the SDK itself. Open `gradle/libs.versions.toml` and uncomment the `playServicesMaps3d` line in the `[versions]` section and the `play-services-maps3d` line in the `[libraries]` section.

    Then, open `app/build.gradle.kts` and uncomment the dependency:
    ```kotlin
    dependencies {
        // ...
        implementation(libs.play.services.maps3d)
    }
    ```
    *   Ensure the Maps 3D SDK dependency is also present in `dependencies { ... }`:
        ```kotlin
        dependencies {
            implementation(libs.play.services.maps3d)
        }
        ```

    *  **Be sure to sync the project if you haven't already!**

4.  **Uncomment SDK-Dependent Files**:
    The starter project contains several helper files (`HonoluluData.kt`, `Map3DLifecycleObserver.kt`, and `Utilities.kt`) that rely heavily on the Maps 3D SDK. They were temporarily commented out to prevent compilation errors before you synced the SDK dependency.
    *   Open `HonoluluData.kt`. Remove the `/*` at the top and the `*/` at the bottom to uncomment the file.
    *   Do the same for `Map3DLifecycleObserver.kt`.
    *   Do the same for `Utilities.kt`.

---

## 1. Setting the Stage (Layout & Init)

Before we can explore the islands, we need to set up our "cockpit". We need a layout that gives the map full focus while keeping our controls accessible.

### Step 1.1: The Layout

Open `app/src/main/res/layout/activity_main.xml`. The layout is mostly provided for you in the starter project. It features a `ConstraintLayout` containing a placeholder `TextView` at the top and a scrollable row of control buttons at the bottom.

To insert the 3D Map, find the `TextView` with the `id` of `@+id/map3dView` and replace the entire `<TextView ... />` block with the `Map3DView` component below.

**Key Changes:**
1.  **Map3DView**: We replace the placeholder with the actual SDK component. Notice all the `app:` attributes provided to configure the initial state of the camera!
2.  **Attributes**: We configure its boundaries, tilt limits, and the initial hybrid map mode.

```xml
    <!-- Replace the placeholder TextView with this Map3DView -->
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
```

### Step 1.2: Project Skeleton

Open `MainActivity.kt`. We will set up the class structure, state variables, and handle the "Edge-to-Edge" UI logic here. Note that all of our geographic constants and model URLs have been pre-defined for you in `HonoluluData.kt` to keep this class clean.

**Tasks**:
1.  Setup **State Management** lists to track objects.
2.  Handle **Window Insets** in `onCreate`.

```kotlin
class MainActivity : AppCompatActivity(), OnMap3DViewReadyCallback {

    private lateinit var map3DView: Map3DView
    private var googleMap3D: GoogleMap3D? = null

    // State Management: Track objects to remove them later
    private val activeMarkers = mutableListOf<Marker>()
    private val activePolygons = mutableListOf<Polygon>()
    private val activePolylines = mutableListOf<Polyline>()
    private val activeModels = mutableListOf<Model>()
    private val activePopovers = mutableListOf<Popover>()

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
        
        // 1.4. Map Lifecycle
        // Manually trigger onCreate (and unpack any saved state from rotation)
        val mapState = savedStateRegistry.consumeRestoredStateForKey("map3d_state_provider")
        map3DView.onCreate(mapState)
        
        // Attach the automated Lifecycle Observer
        lifecycle.addObserver(Map3DLifecycleObserver(map3DView, this))

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

    // Helper: Start the camera from a global view
    private fun startFromGlobalView(map: GoogleMap3D) {
        map.setCamera(
            camera {
                center = latLngAltitude { latitude = 0.0; longitude = 0.0; altitude = 0.0 }
                range = 25_000_000.0 // 25,000 km
                tilt = 0.0
                heading = 0.0
            }
        )
    }
```
> **Note**: You will see red errors for `startFromGlobalView` and other helper functions. Don't worry! We will implement these in the next sections. The `setupButtons` wiring has been pre-provided at the bottom of your file.

### Step 1.4: Map Lifecycle (The Modern Way)

Unlike standard Android Views, the `Map3DView` contains a powerful 3D rendering engine that must be explicitly paused and resumed to preserve battery life and system resources.

Normally, this would mean overriding `onResume`, `onPause`, `onDestroy`, and `onSaveInstanceState` in your `MainActivity` and manually forwarding those events to the map.

**To avoid this messy boilerplate, we have pre-provided `Map3DLifecycleObserver.kt`.** 

This useful utility class implements `DefaultLifecycleObserver`. By calling `lifecycle.addObserver(Map3DLifecycleObserver(map3DView, this))` inside `onCreate`, the map will automatically listen to your Activity's lifecycle events, keeping your UI controller clean!

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

> **[!NOTE] Deep Dive: The Magic of `suspendCancellableCoroutine`**
> The Maps 3D SDK relies heavily on trailing callback functions (e.g. `setCameraAnimationEndListener`). If you want to fly to five different places sequentially, chaining five callbacks inside each other quickly turns into an unreadable "Pyramid of Doom" (affectionately known as Callback Hell).
> 
> By wrapping these listeners inside Kotlin's `suspendCancellableCoroutine`, we temporarily pause the execution of our Kotlin code *without blocking the main UI thread*! Once the Maps SDK fires the callback indicating the flight is over, `continuation.resume(Unit)` allows our code to cleanly wake back up and proceed to the next line.
>
> **The Result:** We can write flat, sequential, and highly readable synchronous-looking code!

Open `Utilities.kt` to see how we wrap the SDK's callback-based listeners into Kotlin Coroutine `suspend` functions. This lets us write linear, readable code like "Fly THERE, then wait, then Fly HERE".

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

### Helper: `resetMap()` and `addMarker()`

You noticed we call `resetMap()` at the start. This prevents "ghost objects"—markers from previous clicks staying on the map. We iterate through our tracking lists (`activeMarkers`, etc.), call `.remove()` on each object to clear it from the 3D engine, and then clear the list.

We also use a custom `addMarker(options: MarkerOptions)` helper method. Instead of duplicating the `Toast` listener and the list-tracking logic for every single marker, this helper centralizes it:
```kotlin
private fun addMarker(markerOptions: MarkerOptions) {
    googleMap3D?.addMarker(markerOptions)?.also { marker ->
        marker.setClickListener {
            lifecycleScope.launch(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, getString(R.string.toast_clicked, marker.label), Toast.LENGTH_SHORT).show()
            }
        }
        activeMarkers.add(marker)
    }
}
```


```kotlin
private fun addMarkers(map: GoogleMap3D) {
    resetMap()

    // 4.1. ABSOLUTE: Altitude is relative to the WGS84 ellipsoid (rough sea level).
    addMarker(
        markerOptions {
            position = latLngAltitude {
                latitude = IOLANI_PALACE.latitude + 0.001
                longitude = IOLANI_PALACE.longitude
                altitude = 100.0 // 100 meters above sea level
            }
            altitudeMode = AltitudeMode.ABSOLUTE
            label = getString(R.string.label_absolute)
            isDrawnWhenOccluded = true
            isExtruded = true // Draws a line to the ground
        }
    )

    // 4.2. RELATIVE_TO_GROUND: Altitude is added to the terrain height at that point.
    addMarker(
        markerOptions {
            position = latLngAltitude {
                latitude = IOLANI_PALACE.latitude
                longitude = IOLANI_PALACE.longitude
                altitude = 50.0 // 50m above ground
            }
            altitudeMode = AltitudeMode.RELATIVE_TO_GROUND
            label = getString(R.string.label_relative)
            isDrawnWhenOccluded = true
            isExtruded = true
        }
    )

    // 4.3. CLAMP_TO_GROUND: Snaps to the terrain.
    addMarker(
        markerOptions {
            position = latLngAltitude {
                latitude = IOLANI_PALACE.latitude - 0.001
                longitude = IOLANI_PALACE.longitude
                altitude = 0.0 // Ignored
            }
            altitudeMode = AltitudeMode.CLAMP_TO_GROUND
            label = getString(R.string.label_clamped)
            isDrawnWhenOccluded = true
            isExtruded = true
        }
    )
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

## 5. Advanced Custom Markers

Markers don't have to be just standard red pins. The Maps 3D SDK allows you to heavily customize their appearance using the `setStyle()` method on `MarkerOptions`.

Here we'll add three new markers next to Iolani Palace to demonstrate:
1.  **A Styled Pin**: A customized standard pin with a specific color and scale.
2.  **An Image Glyph**: A marker that uses a standard Android Drawable resource (a Hibiscus flower).
3.  **A Text Glyph**: A marker that uses an emoji (`🌸`) scaled elegantly.

Add the following function below `addMarkers`:

```kotlin
    private fun addCustomMarkers(map: GoogleMap3D) {
        resetMap()

        // 5.1. Styled Pin
        addMarker(
            markerOptions {
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
            }
        )

        // 5.2. Image Glyph (Hibiscus)
        addMarker(
            markerOptions {
                position = latLngAltitude {
                    latitude = IOLANI_PALACE.latitude
                    longitude = IOLANI_PALACE.longitude + 0.004
                    altitude = 0.0
                }
                label = getString(R.string.label_hibiscus)
                isExtruded = true
                isDrawnWhenOccluded = true
                altitudeMode = AltitudeMode.CLAMP_TO_GROUND
                setStyle(ImageView(this@MainActivity).apply { 
                    setImageResource(R.drawable.hibiscus) 
                })
            }
        )

        // 5.3. Text Glyph
        val glyphText = Glyph.fromColor(Color.YELLOW).apply {
            setText("🌸")
        }

        addMarker(
            markerOptions {
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
            }
        )
    }
```

---

## 6. Highlighting History (Polygons & Extrusion)

Let's highlight the Palace grounds. A flat polygon is okay, but a 3D volume is better.

### Extrusion Algorithm
Open `Utilities.kt` to review the `extrudePolygon` helper function. We can "extrude" a flat shape by:
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

```kotlin
// Helper function to create the 3D extrusion geometry
private fun extrudePolygon(
    basePoints: List<LatLngAltitude>,
    extrusionHeight: Double
): List<List<LatLngAltitude>> {
    if (basePoints.size < 3) return emptyList()
    if (extrusionHeight <= 0) return emptyList()

    val baseAltitude = basePoints.first().altitude
    
    // 1. Create top points
    val topPoints = basePoints.map { basePoint ->
        latLngAltitude {
            latitude = basePoint.latitude
            longitude = basePoint.longitude
            altitude = baseAltitude + extrusionHeight
        }
    }

    val faces = mutableListOf<List<LatLngAltitude>>()
    faces.add(basePoints.toList()) // Bottom
    faces.add(topPoints.toList().reversed()) // Top

    // 2. Create side walls
    for (i in basePoints.indices) {
        val p1Base = basePoints[i]
        val p2Base = basePoints[(i + 1) % basePoints.size]
        val p1Top = topPoints[i]
        val p2Top = topPoints[(i + 1) % basePoints.size]
        
        faces.add(listOf(p1Base, p2Base, p2Top, p1Top))
    }
    return faces
}
```

> **[!TIP] Visualizing the Extrusion Math**
> When stitching the vertical side walls, we create a single face polygon using four vertices: two from the "ground" (Base) and two directly above them (Top) at the extrusion altitude.
> 
> ```text
>        p1Top -------- p2Top     (Altitude: 35.0)
>          |              |
>          |  (Side Wall) |
>          |              |
>       p1Base ------- p2Base     (Altitude:  0.0)
> ```
> By looping this logic for every pair of points around the perimeter, we seamlessly form an unbroken 3D perimeter wall.

When we add these faces to the map, we use `AltitudeMode.ABSOLUTE` to ensure the building keeps its shape perfectly.

<!-- TODO: Add screenshot here.
     Subject: "Extruded Palace"
     Description: Show the Iolani Palace with the semi-transparent gold polygon extruded upwards. 
     Camera should be tilted to show the 3D volume, not just the top-down view.
-->

---

## 7. Interaction (Touching the World)

A map isn't an image; it's an interface. Let's make our markers interactive.

```kotlin
// 6.1. Tapping the Turf
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

## 8. Connecting the Dots (Polylines)

While markers show discrete locations, polylines show paths. Let's draw a path from Iolani Palace to Waikiki Beach.

Just like Polygons, Polylines are drawn point-by-point. 

```kotlin
// Draw path to Waikiki
activePolylines.add(map.addPolyline(
    polylineOptions {
        path = listOf(IOLANI_PALACE, WAIKIKI)
        strokeWidth = 10.0
        strokeColor = Color.BLUE
    }
))

// Jump the camera to see the full path
map.setCamera(camera {
    center = latLngAltitude {
        latitude = 21.2893
        longitude = -157.8441
        altitude = 0.0
    }
    heading = 0.0
    tilt = 0.0
    range = 8000.0
})
```

---

## 9. Up, Up, and Away (The Balloon)

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
-->

---

## 10. Popovers (Info Windows)

Markers are great, but sometimes you need to show more information. **Popovers** are 2D views that "stick" to a 3D location. Unlike Markers, they always face the camera and can contain any Android View (button, text, image, etc.).

### Creating a Popover

In this example, we will attach a "Hello World" message to the Iolani Palace marker.

1.  **Create the View**: First, we create a standard Android `TextView` programmatically.
2.  **Configure the Popover**: We use `popoverOptions` to define its anchor point and style.

```kotlin
    private fun setupPopover(map: GoogleMap3D) {
        resetMap()

        // 9.1. Create a simple text view for the popover content
        // In a real app, you could inflate this from XML using layoutInflater
        val textView = TextView(this).apply {
            text = "Welcome to Iolani Palace!\nA symbol of Hawaiian sovereignty."
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
```

Popovers bridge the gap between the 3D world and 2D information. They are perfect for labels, detailed info windows, or even interactive menus.

<!-- TODO: Add screenshot here.
     Subject: "Popover Example"
     Description: Show the "Welcome to Iolani Palace!" popover visible above the Iolani Palace marker.
     Highlight the popover's clean white background and shadow.
-->

---

## 11. The Scenic Tour (Animating Between Markers)

Let's spread our wings and explore the whole island! In this step, we'll scatter markers across Oahu's most famous landmarks and animate the camera flying point-to-point. 

1. Add the `btn_tour` and `btn_clear` buttons to your `activity_main.xml` layout, next to the other buttons.
2. In `MainActivity.kt`, add the geographic constants for the landmarks to the `companion object`.
3. Add a helper function `flyTour` to orchestrate the flight:

```kotlin
    private suspend fun flyTour(map: GoogleMap3D) {
        val locations = listOf(
            HONOLULU to "Honolulu",
            DIAMOND_HEAD to "Diamond Head",
            HANAUMA_BAY to "Hanauma Bay",
            KOKO_HEAD to "Koko Head",
            LANIKAI_BEACH to "Lanikai Beach",
            MOUNT_KAALA to "Mount Ka'ala",
            PEARL_HARBOR to "Pearl Harbor"
        )
        
        // Add all markers for the tour
        resetMap()
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
            // TODO: CHALLENGE!
            // 1. Tell the map to flyCameraTo this 'location'. 
            //    (Hint: Use tilt = 45.0, range = 2500.0, heading = 0.0, durationInMillis = 3000L)
            // 2. Wait for the animation to finish using awaitCameraAnimation(map)
            // 3. Optional: Add a delay so the user can enjoy the view before flying to the next!
            
        }
    }
```

### Stopping Animations

If the tour is running and the user clicks another button, we must cancel the existing animation. Notice in the solution code we wrap `lifecycleScope.launch` in a variable `currentAnimationJob` and call `currentAnimationJob?.cancel()` before starting any new action!

Wire up the new buttons in `setupButtons`:

```kotlin
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
```

---

## 12. Bonus: Jetpack Compose

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
