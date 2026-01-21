# Codelab: Aloha Explorer — Building with the 3D Maps SDK for Android

Welcome to the future of mobile mapping! In this codelab, we are going to build **Aloha Explorer**, a travel app designed to showcase the beauty and history of Honolulu using the **Google Maps 3D SDK for Android**.

Unlike standard 2D or "2.5D" maps, the 3D SDK allows you to treat the world as a true 3D environment. You will learn how to fly cameras through mountain passes, animate models in the air, and highlight historic landmarks with 3D volumes.

### What you’ll learn

*   **Initialization**: How to set up the `Map3DView` and handle its lifecycle.
*   **Camera Control**: How to orchestrate cinematic camera movements.
*   **Altitude Modes**: How to use `ABSOLUTE`, `RELATIVE_TO_GROUND`, and `CLAMP_TO_GROUND`.
*   **Extrusion**: How to turn 2D polygons into 3D volumes.
*   **3D Models**: How to load and place glTF models (a Balloon!).
*   **Interaction**: How to handle click events on 3D objects.
*   **Coroutines**: How to use Kotlin Coroutines for smooth, asynchronous animation sequencing.

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
        app:layout_constraintBottom_toTopOf="@id/controls_scroll_view"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

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
            android:orientation="horizontal">
            <!-- Buttons go here -->
        </LinearLayout>
    </HorizontalScrollView>

</androidx.constraintlayout.widget.ConstraintLayout>
```

### Step 1.2: Initialization & Lifecycle

Open `MainActivity.kt`. The `Map3DView` is powerful, but it needs a little help to know when to start and stop rendering.

**Task**:
1.  Enable **Edge-to-Edge** display for maximum immersion.
2.  Handle **WindowInsets** so our controls don't get hidden behind the navigation bar.
3.  Forward **Lifecycle** events (`onResume`, `onPause`, `onDestroy`) to the map.

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge() // 1. Full screen!
    setContentView(R.layout.activity_main)

    // 2. Handle Insets
    // Map needs padding at Top (Status Bar)
    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.map3dView)) { v, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
        insets
    }
    // Controls need padding at Bottom (Nav Bar)
    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.controls_scroll_view)) { v, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
        insets
    }

    // 3. Initialize Map
    map3DView = findViewById(R.id.map3dView)
    map3DView.onCreate(savedInstanceState)
    map3DView.getMap3DViewAsync(this)
}
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

// Extrude!
val extrudedPalace = extrudePolygon(palaceBaseFace, 35.0) // 35 meters tall
```

When we add these faces to the map, we use `AltitudeMode.ABSOLUTE` to ensure the building keeps its shape perfectly.

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
```

---

## Finish & Next Steps

Mahalo for completing the **Aloha Explorer** codelab! You’ve gone from a empty activity to a cinematic, interactive 3D experience.

**You learned how to:**
*   Manage the `Map3DView` lifecycle.
*   Control the camera with Coroutines.
*   Master `AltitudeMode` for perfect placement.
*   Import and click 3D Models.

**The world is no longer flat—go build something amazing!**
