# Common Operations Catalog (Jetpack Compose)

This catalog provides reference snippets for common operations when using the Google Maps 3D SDK with Jetpack Compose (via `AndroidView` interoperability).

## 0. The Reusable `Map3DContainer` Pattern
Description: The recommended way to use `Map3DView` in Compose is to create a reusable wrapper Composable that handles lifecycle and map state.

```kotlin
@Composable
fun Map3DContainer(
    modifier: Modifier = Modifier,
    options: Map3DOptions,
    onMapReady: (GoogleMap3D) -> Unit
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            Map3DView(context, options).apply {
                onCreate(null) // Handle lifecycle manually if needed
            }
        },
        update = { view ->
            view.getMap3DViewAsync(object : OnMap3DViewReadyCallback {
                override fun onMap3DViewReady(map: GoogleMap3D) {
                    onMapReady(map)
                }
                override fun onError(e: Exception) {
                    // Handle error
                }
            })
        },
        onRelease = { view ->
            view.onDestroy()
        }
    )
}
```

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

## 9. Adding a 3D Model
Description: Adding a model within `AndroidView`.

```kotlin
AndroidView(
    factory = { context ->
        Map3DView(context).apply {
            getMapAsync { map ->
                map.addModel(ModelOptions()
                    .position(LatLngAltitude(37.7749, -122.4194, 0.0))
                    .url("https://.../model.glb"))
            }
        }
    }
)
```

## 10. Adding a Popover (Info Window)
Description: Adding a popover within `AndroidView`.

```kotlin
AndroidView(
    factory = { context ->
        Map3DView(context).apply {
            getMapAsync { map ->
                val textView = TextView(context).apply { text = "Hello" }
                map.addPopover(PopoverOptions()
                    .positionAnchor(LatLngAltitude(37.7749, -122.4194, 10.0))
                    .content(textView))
            }
        }
    }
)
```

## 11. Extruded Polygons (3D Volumes)
Description: Adding extruded polygons within `AndroidView`.

```kotlin
AndroidView(
    factory = { context ->
        Map3DView(context).apply {
            getMapAsync { map ->
                val faces = extrude(basePoints, 35.0)
                faces.forEach { face ->
                    map.addPolygon(PolygonOptions()
                        .addAll(face)
                        .fillColor(Color.argb(128, 255, 215, 0)))
                }
            }
        }
    }
)
```

## 12. ViewModel Integration
Description: The advanced sample demonstrates hoisting the `GoogleMap3D` instance to a `ViewModel` to manage state and handle actions outside the UI tree.

```kotlin
class MapViewModel : ViewModel() {
    private val _googleMap3D = MutableStateFlow<GoogleMap3D?>(null)
    val googleMap3D: StateFlow<GoogleMap3D?> = _googleMap3D.asStateFlow()

    private val _isMapSteady = MutableStateFlow(false)
    val isMapSteady: StateFlow<Boolean> = _isMapSteady.asStateFlow()

    fun setGoogleMap3D(map: GoogleMap3D) {
        _googleMap3D.value = map
    }

    fun onMapSteadyChange(isSteady: Boolean) {
        _isMapSteady.value = isSteady
    }

    fun releaseGoogleMap3D() {
        _googleMap3D.value = null
    }
}
```

In your Composable:
```kotlin
val viewModel: MapViewModel = viewModel()
val map3D by viewModel.googleMap3D.collectAsState()

ThreeDMap(
    options = mapOptions,
    onMapReady = { map ->
        viewModel.setGoogleMap3D(map)
        map.setOnMapSteadyListener { isSteady ->
            viewModel.onMapSteadyChange(isSteady)
        }
    }
)
```

## 13. Camera State Logger (Debug Helper)
Description: A utility to log the current camera state to Logcat on every movement, helping you design 3D views.

```kotlin
AndroidView(
    factory = { context ->
        Map3DView(context).apply {
            getMapAsync { map ->
                map.addOnCameraMoveListener {
                    val camera = map.camera
                    Log.d("CameraLogger", "Lat: ${camera.center.latitude}, Lng: ${camera.center.longitude}, Alt: ${camera.center.altitude}, Heading: ${camera.heading}, Tilt: ${camera.tilt}, Range: ${camera.range}")
                }
            }
        }
    }
)
```

## 14. 3D View Validation (Testing)
Description: Verifying that the `Map3DView` is visible using `ComposeTestRule`.

```kotlin
@Test
fun testMapVisible() {
    composeTestRule.setContent {
        Map3DContainer(
            modifier = Modifier.testTag("map3d_container"),
            options = Map3DOptions(),
            onMapReady = {}
        )
    }
    
    // Verify AndroidView hosting Map3DView is displayed
    composeTestRule.onNodeWithTag("map3d_container")
        .assertIsDisplayed()
}
```






