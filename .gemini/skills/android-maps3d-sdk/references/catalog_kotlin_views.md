# Common Operations Catalog (Kotlin + Views)

This catalog provides short, reference snippets for common operations when using the Google Maps 3D SDK with Kotlin and XML Views.

## 1. Adding a Marker
Description: Markers point out points of interest. You can set position, altitude mode, and labels.

```kotlin
val marker = map.addMarker(MarkerOptions()
    .position(LatLngAltitude(37.7749, -122.4194, 0.0))
    .altitudeMode(AltitudeMode.RELATIVE_TO_GROUND)
    .label("San Francisco"))
```

## 2. Adding a Polyline
Description: Polylines are lines on the map. Use matching IDs to update them instead of re-adding.

```kotlin
val polyline = map.addPolyline(PolylineOptions()
    .add(LatLngAltitude(37.77, -122.41, 0.0))
    .add(LatLngAltitude(37.78, -122.42, 0.0))
    .color(Color.RED)
    .width(5f))

// To update:
polyline.points = newPointsList
```

## 3. Adding a Polygon
Description: Polygons represent areas.

```kotlin
val polygon = map.addPolygon(PolygonOptions()
    .add(LatLngAltitude(37.77, -122.41, 0.0))
    .add(LatLngAltitude(37.78, -122.41, 0.0))
    .add(LatLngAltitude(37.78, -122.42, 0.0))
    .fillColor(Color.argb(128, 255, 0, 0)))
```

## 4. Camera Animation
Description: Animating the camera to a new position or flying around a center.

```kotlin
// Fly To
map.flyTo(FlyToOptions()
    .endCamera(targetCamera)
    .durationInMillis(3000))

// Fly Around
map.flyAround(FlyAroundOptions()
    .center(currentCamera)
    .durationInMillis(5000)
    .rounds(1.0))
```

## 5. Handling Map Click Events
Description: Listening for clicks on the map.

```kotlin
map.addOnMapClickListener { latLngAltitude ->
    // Handle click at location
    val lat = latLngAltitude.latitude
    val lng = latLngAltitude.longitude
}
```

## 6. Object Animation (Moving Objects)
Description: Animating the position of an object (like a Marker or Polygon) using Android `ValueAnimator` or Coroutines.

### Using ValueAnimator
```kotlin
val animator = ValueAnimator.ofFloat(0f, 1f)
animator.duration = 1000
animator.addUpdateListener { animation ->
    val fraction = animation.animatedValue as Float
    val newLat = startLat + (endLat - startLat) * fraction
    val newLng = startLng + (endLng - startLng) * fraction
    marker.position = LatLngAltitude(newLat, newLng, 0.0)
}
animator.start()
```

### Using Coroutines (Custom)
```kotlin
suspend fun animateMarker(marker: Marker, start: LatLng, end: LatLng, duration: Long = 1000) {
    val steps = duration / 20
    val stepLat = (end.latitude - start.latitude) / steps
    val stepLng = (end.longitude - start.longitude) / steps
    var currentLat = start.latitude
    var currentLng = start.longitude

    for (i in 0 until steps) {
        currentLat += stepLat
        currentLng += stepLng
        marker.position = LatLngAltitude(currentLat, currentLng, 0.0)
        delay(20)
    }
    marker.position = LatLngAltitude(end.latitude, end.longitude, 0.0)
}
## 9. Waiting for Scene to Settle (Steady State)
Description: The 3D SDK can notify you when the scene has fully rendered (terrain and mesh data loaded). This is crucial for high-fidelity snapshots or visual synchronization.

### Callback Pattern
```kotlin
map.setOnMapSteadyListener { isSteady ->
    if (isSteady) {
        // Scene is settled and fully rendered
        map.setOnMapSteadyListener(null) // Clear if one-shot
    }
}
```

### Coroutine Pattern
```kotlin
suspend fun GoogleMap3D.awaitMapSteady(timeout: Duration): Boolean = suspendCancellableCoroutine { cont ->
    setOnMapSteadyListener { isSteady ->
        if (isSteady) {
            setOnMapSteadyListener(null)
            cont.resume(true)
        }
    }
    // Add timeout logic if needed (see full samples)
}
```

## 7. Object Click Listeners
Description: Unlike the 2D SDK, click listeners are set directly on the object instances (Marker, Polyline, Polygon, Model) rather than on the map.

```kotlin
// Marker
marker.setClickListener {
    // Handle marker click
    // NOTE: This is not on the UI thread!
}

// Polyline
polyline.setClickListener {
    // Handle polyline click
    // NOTE: This is not on the UI thread!
}
```

> [!WARNING]
> The `setClickListener` callback does **NOT** execute on the UI thread. If you need to update views or perform UI operations, you must switch to the main thread (e.g., using `withContext(Dispatchers.Main)` or `Handler(Looper.getMainLooper()).post(...)`).


## 8. Stopping & Waiting for Camera Animations
Description: Controlling camera animations and waiting for their completion.

### Stopping Animations
```kotlin
// Halts any in-progress camera movement
map.stopCameraAnimation()

// Clear the listener as well if needed
map.setCameraAnimationEndListener(null)
```

### Waiting for Completion (Callback)
```kotlin
map.setCameraAnimationEndListener {
    // Trigger next action after animation ends
}
map.flyTo(...)
```

### Waiting for Completion (Coroutine)
```kotlin
// Using a custom suspend function wrapper (common pattern)
suspend fun GoogleMap3D.awaitFlyTo(options: FlyToOptions) = suspendCancellableCoroutine { cont ->
    setCameraAnimationEndListener {
        setCameraAnimationEndListener(null)
        cont.resume(Unit)
    }
    flyTo(options)
}
```

## 10. Adding a 3D Model
Description: Loading and placing glTF assets on the map.

```kotlin
val model = map.addModel(ModelOptions()
    .position(LatLngAltitude(37.7749, -122.4194, 0.0))
    .url("https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/Airplane.glb")
    .scale(Vector3D(1.0, 1.0, 1.0))
    .altitudeMode(AltitudeMode.RELATIVE_TO_GROUND))
```

## 11. Adding a Popover (Info Window)
Description: 2D views that stick to a 3D location and always face the camera.

```kotlin
val textView = TextView(context).apply {
    text = "Hello World"
    setBackgroundColor(Color.WHITE)
}

val popover = map.addPopover(PopoverOptions()
    .positionAnchor(LatLngAltitude(37.7749, -122.4194, 10.0))
    .content(textView)
    .altitudeMode(AltitudeMode.RELATIVE_TO_MESH)
    .autoCloseEnabled(true))

popover.show()
```

## 12. Extruded Polygons (3D Volumes)
Description: Turning flat footprints into 3D volumes by duplicating vertices at height and stitching sides.

```kotlin
// Helper to extrude (see full Codelab for complete implementation)
fun extrude(basePoints: List<LatLngAltitude>, height: Double): List<List<LatLngAltitude>> {
    // Implementation creates top points and side walls...
    return faces
}

// Adding extruded faces to map
val faces = extrude(basePoints, 35.0)
faces.forEach { face ->
    map.addPolygon(PolygonOptions()
        .addAll(face)
        .fillColor(Color.argb(128, 255, 215, 0))
        .altitudeMode(AltitudeMode.ABSOLUTE))
}
```

## 13. Camera State Logger (Debug Helper)
Description: A utility to log the current camera state to Logcat on every movement, helping you design 3D views.

```kotlin
map.addOnCameraMoveListener {
    val camera = map.camera
    Log.d("CameraLogger", """
        Camera State:
        Lat: ${camera.center.latitude}
        Lng: ${camera.center.longitude}
        Alt: ${camera.center.altitude}
        Heading: ${camera.heading}
        Tilt: ${camera.tilt}
        Range: ${camera.range}
    """.trimIndent())
}
```

## 14. 3D View Validation (Testing)
Description: Verifying that the `Map3DView` is visible and loads correctly using Espresso.

```kotlin
@Test
fun testMapVisible() {
    // Launch Activity
    ActivityScenario.launch(MapActivity::class.java)
    
    // Verify Map3DView is displayed
    onView(withId(R.id.map3d_view))
        .check(matches(isDisplayed()))
}
```




