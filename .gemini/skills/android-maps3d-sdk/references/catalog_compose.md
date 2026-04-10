# Common Operations Catalog (Jetpack Compose)

This catalog provides reference snippets for common operations when using the Google Maps 3D SDK with Jetpack Compose (via `AndroidView` interoperability).

## 1. Adding a Marker
Description: Markers point out points of interest.

```kotlin
var marker by remember { mutableStateOf<Marker?>(null) }

AndroidView(
    factory = { context ->
        Map3DView(context).apply {
            getMapAsync { map ->
                marker = map.addMarker(MarkerOptions()
                    .position(LatLngAltitude(37.7749, -122.4194, 0.0))
                    .label("San Francisco"))
            }
        }
    },
    update = { view ->
        // Update marker properties if needed
    }
)
```

## 2. Adding a Polyline
Description: Polylines are lines on the map.

```kotlin
var polyline by remember { mutableStateOf<Polyline?>(null) }

AndroidView(
    factory = { context ->
        Map3DView(context).apply {
            getMapAsync { map ->
                polyline = map.addPolyline(PolylineOptions()
                    .add(LatLngAltitude(37.77, -122.41, 0.0))
                    .add(LatLngAltitude(37.78, -122.42, 0.0))
                    .color(Color.RED))
            }
        }
    },
    update = { view ->
        // To update points:
        polyline?.points = newPointsList
    }
)
```

## 3. Camera Animation
Description: Animating the camera using the map instance.

```kotlin
var map3D by remember { mutableStateOf<GoogleMap3D?>(null) }

AndroidView(
    factory = { context ->
        Map3DView(context).apply {
            getMapAsync { map ->
                map3D = map
            }
        }
    }
)

// Trigger animation in a side effect
LaunchedEffect(trigger) {
    map3D?.flyTo(FlyToOptions()
        .endCamera(targetCamera)
        .durationInMillis(3000))
}
```

## 4. Handling Map Click Events
Description: Listening for clicks on the map.

```kotlin
AndroidView(
    factory = { context ->
        Map3DView(context).apply {
            getMapAsync { map ->
                map.addOnMapClickListener { location ->
                    onMapClick(location)
                }
            }
        }
    }
)
```

## 5. Object Animation (Moving Objects)
Description: Animating the position of an object using Compose animation states.

```kotlin
var marker by remember { mutableStateOf<Marker?>(null) }
val moveTrigger by remember { mutableStateOf(false) }

// Animate a fraction from 0 to 1
val fraction by animateFloatAsState(
    targetValue = if (moveTrigger) 1f else 0f,
    animationSpec = tween(durationMillis = 1000)
)

// Calculate intermediate position
val currentLat = startLat + (endLat - startLat) * fraction
val currentLng = startLng + (endLng - startLng) * fraction

// Update marker position in the update block or side effect
AndroidView(
    factory = { /* ... */ },
    update = { view ->
        marker?.position = LatLngAltitude(currentLat, currentLng, 0.0)
    }
)
```

## 6. Object Click Listeners
Description: Setting click listeners on objects within the `AndroidView` factory or update block.

```kotlin
AndroidView(
    factory = { context ->
        Map3DView(context).apply {
            getMapAsync { map ->
                val marker = map.addMarker(...)
                marker.setClickListener {
                    // Handle marker click
                    // NOTE: This is not on the UI thread!
                }
            }
        }
    }
)
```

> [!WARNING]
> The `setClickListener` callback does **NOT** execute on the UI thread. If you need to update Compose state or perform UI operations, you must switch to the main thread (e.g., using `withContext(Dispatchers.Main)` or updating a thread-safe state).


## 7. Stopping & Waiting for Camera Animations
Description: Controlling camera animations in Compose side effects.

### Stopping Animations
```kotlin
LaunchedEffect(stopTrigger) {
    if (stopTrigger) {
        map3D?.stopCameraAnimation()
    }
}
```

### Waiting for Completion
```kotlin
LaunchedEffect(trigger) {
    map3D?.let { map ->
        // Using the custom awaitFlyTo extension mentioned in Kotlin catalog
        map.awaitFlyTo(FlyToOptions()
            .endCamera(targetCamera)
            .durationInMillis(3000))
        
        // This runs AFTER animation completes
        onAnimationComplete()
    }
}
```

## 8. Waiting for Scene to Settle (Steady State)
Description: Hoisting the map steady state to Compose state.

```kotlin
var isMapSteady by remember { mutableStateOf(false) }

AndroidView(
    factory = { context ->
        Map3DView(context).apply {
            getMapAsync { map ->
                map.setOnMapSteadyListener { steady ->
                    isMapSteady = steady
                }
            }
        }
    }
)

// React to steady state
if (isMapSteady) {
    // Scene is fully rendered
}
```


