# Scenario Storyboard Catalog

This catalog provides "orchestration recipes" for complex, multi-step workflows in the 3D SDK. Instead of atomic snippets, these scenarios demonstrate how to synchronize asynchronous events (camera motion, asset loading, rendering steady state) to create high-fidelity user experiences.

---

## Scenario 1: The Immersive Arrival
Description: Transitioning from a high-altitude "Global View" to a specific building with high-detail 3D assets. This pattern handles the race condition between camera movement and mesh loading.

### The Strategy (Orchestration Logic)
1.  **Travel**: Execute a cinematic `flyTo` from the current camera to the target.
2.  **Synchronization**: Use the **Double-Wait Pattern**. Wait for the camera animation to end and for the map steady-state (ensures the building mesh is loaded before placing assets).
3.  **Enhancement**: Add a high-detail 3D model (glTF) at the location.
4.  **Context**: Show a Popover with metadata.
5.  **Engagement**: Start a slow `flyAround` to provide a 360-degree context.

### The Storyboard Code (Kotlin + Coroutines)

```kotlin
/**
 * SCENARIO: The Immersive Arrival
 * Orchestrates a high-to-low altitude transition with asset loading.
 * 
 * Note: Assumes extensions like `awaitFlyTo` and `awaitMapSteady` are available
 * (see catalogs for implementation).
 */
suspend fun GoogleMap3D.executeImmersiveArrival(
    targetLocation: LatLngAltitude,
    modelUrl: String,
    title: String,
    context: Context
) {
    // 1. Travel: High-speed cinematic flight
    val targetCamera = Camera.builder()
        .center(targetLocation)
        .range(500.0) // Close zoom
        .tilt(45.0)
        .heading(0.0)
        .build()
    
    // Custom awaitFlyTo extension ensures we don't proceed until we arrive
    awaitFlyTo(FlyToOptions.builder()
        .endCamera(targetCamera)
        .durationInMillis(4000)
        .build())

    // 2. Synchronization: Wait for the 3D mesh (buildings/terrain) to settle
    // This prevents the "popping" of 3D models into empty space
    awaitMapSteady(timeout = 5000)

    // 3. Enhancement: Place the hero asset
    val model = addModel(ModelOptions.builder()
        .position(targetLocation)
        .url(modelUrl)
        .altitudeMode(AltitudeMode.RELATIVE_TO_MESH)
        .build())

    // 4. Context: Show the info UI
    val textView = TextView(context).apply { text = title }
    val popover = addPopover(PopoverOptions.builder()
        .positionAnchor(LatLngAltitude(targetLocation.latitude, targetLocation.longitude, targetLocation.altitude + 20.0))
        .content(textView)
        .build())
    popover.show()

    // 5. Engagement: Subtle rotation to show off the 3D space
    flyAround(FlyAroundOptions.builder()
        .center(camera) // Rotate around current view
        .durationInMillis(20000)
        .rounds(0.5)
        .build())
}
```

---

## Scenario 2: The Flyover Tour
Description: A guided tour that visits multiple points of interest sequentially, waiting for the scene to settle at each stop before proceeding.

### The Strategy
1.  **Fly to Stop**: Move to the first POI.
2.  **Wait**: Wait for camera and mesh to settle.
3.  **Pause/Inspect**: Hold the view for a few seconds or trigger an action (e.g., show a marker).
4.  **Repeat**: Move to the next POI.

### The Storyboard Code (Kotlin + Coroutines)

```kotlin
suspend fun GoogleMap3D.executeFlyoverTour(
    stops: List<LatLngAltitude>,
    onStopVisited: (Int) -> Unit
) {
    stops.forEachIndexed { index, stop ->
        // Fly to stop
        val camera = Camera.builder().center(stop).range(1000.0).tilt(30.0).build()
        awaitFlyTo(FlyToOptions.builder().endCamera(camera).durationInMillis(3000).build())
        
        // Wait for mesh
        awaitMapSteady(timeout = 5000)
        
        // Trigger callback (e.g., show UI or highlight asset)
        onStopVisited(index)
        
        // Hold for inspection
        delay(2000)
    }
}
```

---

## Scenario 3: The Fragment Interop Overlay (Places UI Kit)
Description: Hosting a traditional Android Fragment (like the Places UI Kit `PlaceDetailsCompactFragment`) inside a Compose UI anchored to map events. This solves the problem of rapid recomposition causing fragment recreation.

### The Strategy
1.  **Single Instantiation**: Create the `FragmentContainerView` and transact the Fragment exactly ONCE in the `factory` block of `AndroidView`.
2.  **Decoupled Updates**: Use `LaunchedEffect` keyed on the state (e.g., `placeId`) to update the existing fragment, avoiding full recreation.
3.  **Activity Support**: Use the Activity's `supportFragmentManager` directly to avoid Hilt context casting issues (cast `LocalContext.current` to `FragmentActivity`).

### The Storyboard Code (Kotlin + Compose)

```kotlin
@Composable
fun PlaceDetailsOverlay(
    placeId: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerId = remember { View.generateViewId() }
    val context = LocalContext.current
    val supportFragmentManager = remember(context) {
        (context as FragmentActivity).supportFragmentManager
    }

    // Decoupled state observer: Updates existing fragment when placeId changes
    LaunchedEffect(placeId) {
        val fragment = supportFragmentManager.findFragmentById(containerId) as? PlaceDetailsCompactFragment
        if (fragment != null) {
            fragment.loadWithPlaceId(placeId)
        }
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = { ctx ->
                FragmentContainerView(ctx).apply {
                    id = containerId
                    
                    val newFragment = PlaceDetailsCompactFragment.newInstance(
                        PlaceDetailsCompactFragment.ALL_CONTENT,
                        Orientation.VERTICAL,
                        R.style.CustomizedPlaceDetailsTheme
                    )
                    
                    supportFragmentManager.commit {
                        replace(containerId, newFragment)
                    }
                    
                    post { newFragment.loadWithPlaceId(placeId) }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
    
    // Clean up fragment when leaving composition
    DisposableEffect(containerId) {
        onDispose {
            supportFragmentManager.findFragmentById(containerId)?.let {
                supportFragmentManager.commit { remove(it) }
            }
        }
    }
}
```

---

## Scenario 4: Continuous Route Tracking (Drone View)
Description: Simulating a smooth, frame-driven flight along a complex polyline route, updating camera and markers on each frame. This avoids the jerky movement of waypoint jumping.

### The Strategy
1.  **Headless Engine**: Use a `LaunchedEffect` with `withFrameMillis` to run a continuous physics loop.
2.  **Interpolation**: Calculate the precise position along the route based on elapsed distance.
3.  **Smooth Camera**: Use linear interpolation (lerp) and spherical linear interpolation (slerp) for camera position and heading to prevent jitter.
4.  **Object Mutation**: Directly mutate existing object properties (e.g., `m.orientation = ...`) rather than recreating objects to maintain 60FPS.

### The Storyboard Code (Kotlin + Compose)

```kotlin
@Composable
fun RouteFlightEngine(
    map3D: GoogleMap3D?,
    path: List<LatLng>,
    isPlaying: Boolean,
    speedMps: Float
) {
    val safeMap = map3D ?: return
    val cumulativeDistances = remember(path) { calculateCumulativeDistances(path) }
    val totalDistance = cumulativeDistances.last()

    var elapsedDistance by remember { mutableFloatStateOf(0f) }
    var lastFrameTime by remember { mutableLongStateOf(0L) }

    LaunchedEffect(isPlaying, path) {
        if (!isPlaying) return@LaunchedEffect

        while (isPlaying) {
            withFrameMillis { frameTime ->
                if (lastFrameTime == 0L) {
                    lastFrameTime = frameTime
                    return@withFrameMillis
                }
                val dtMs = frameTime - lastFrameTime
                lastFrameTime = frameTime

                // Advance distance
                elapsedDistance += (speedMps * (dtMs / 1000.0)).toFloat()
                if (elapsedDistance >= totalDistance) elapsedDistance = totalDistance.toFloat()

                // Calculate interpolated position
                val targetPos = getInterpolatedPoint(elapsedDistance.toDouble(), path, cumulativeDistances)
                
                // Update camera smoothly
                val currentCamera = safeMap.camera
                val newCamera = Camera.builder()
                    .center(targetPos)
                    .heading(currentCamera.heading) // Or calculate lookahead heading
                    .tilt(currentCamera.tilt)
                    .range(currentCamera.range)
                    .build()
                
                safeMap.setCamera(newCamera)
            }
        }
    }
}
```
> [!NOTE]
> This is a simplified extraction. Refer to the full sample in `Maps3DSamples/advanced/app/src/main/java/com/example/advancedmaps3dsamples/route/RouteSampleActivity.kt` for advanced details on `slerpHeading`, lookahead vectors, and the "Teleport to Null Island" pattern for hiding inactive models.


