# Scenario Storyboard Catalog

This catalog provides orchestration architectures for complex, multi-step workflows in the Google Maps 3D SDK (`16.0.1+`). Instead of atomic calls, these scenarios demonstrate how to synchronize asynchronous events (camera motion, asset loading, rendering steady state, and fragment interop) to create high-fidelity 3D user experiences.

---

## Scenario 1: The Immersive Arrival
Description: Transitioning from a high-altitude "Global View" to a specific building with high-detail 3D assets. This pattern handles the race condition between camera movement and 3D mesh loading.

### The Strategy (Orchestration Logic)
1.  **Travel**: Execute a cinematic `flyCameraTo` (`flyToOptions { ... }`) from the current camera to the target.
2.  **Synchronization**: Use the **Double-Wait Pattern** (`awaitCameraAnimation` + `awaitMapSteady` in `references/utilities_kotlin.md`). Wait for the camera animation to end (`setCameraAnimationEndListener`) and for the map steady state (`setOnMapSteadyListener`) so the building mesh is loaded before placing assets.
3.  **Enhancement**: Add a high-detail 3D model (glTF/GLB) at the location using `addModel(modelOptions { ... })`.
4.  **Context**: Anchor and show a `Popover` (`addPopover(popoverOptions { ... })` + `popover.show()`) with metadata.
5.  **Engagement**: Start a slow `flyCameraAround` (`flyAroundOptions { ... }`) to provide a 360-degree orbit context.

### Verified Building Blocks (Kotlin)
Combine the following region-tagged building blocks in a coroutine (where `SAUCER_URL = "https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/UFO.glb"`):

```kotlin
// [START maps_android_3d_camera_fly_to_kt]
val targetCamera = camera {
    center = latLngAltitude {
        latitude = 38.743829
        longitude = -109.499512
        altitude = 1460.37
    }
    tilt = 76.16
    heading = 338.52
    range = 191.71
    roll = 0.0
}

val options = flyToOptions {
    endCamera = targetCamera
    durationInMillis = 5000
}

map.flyCameraTo(options)
// [END maps_android_3d_camera_fly_to_kt]
```

```kotlin
// [START maps_android_3d_model_add_kt]
val position = latLngAltitude {
    latitude = 37.4220
    longitude = -122.0841
    altitude = 100.0
}

val options = modelOptions {
    this.position = position
    url = SAUCER_URL
    altitudeMode = AltitudeMode.RELATIVE_TO_MESH
    orientation = orientation {
        tilt = 90.0
        heading = 0.0
        roll = 0.0
    }
    scale = vector3D {
        x = 10.0
        y = 10.0
        z = 10.0
    }
}

val model = map.addModel(options)
// [END maps_android_3d_model_add_kt]
```

```kotlin
// [START maps_android_3d_camera_fly_around_kt]
lifecycleScope.launch {
    val targetCamera = camera {
        center = latLngAltitude {
            latitude = 38.743502
            longitude = -109.499374
            altitude = 1467.0
        }
        tilt = 58.1
        heading = 349.6
        range = 138.2
        roll = 0.0
    }

    // Although not completely necessary, the experience will be usually be better by
    // waiting for the camera to steady before starting the flyCameraAround
    map.setCamera(targetCamera)

    // Orbit around the target using DSL
    val options = flyAroundOptions {
        center = targetCamera
        rounds = 2.0 // 2 full rotations
        durationInMillis = 6_000
    }

    map.setOnMapSteadyListener { isSceneSteady: Boolean ->
        if (isSceneSteady) {
            map.setOnMapSteadyListener(null) // Cleanup
            map.flyCameraAround(options)
        }
    }
}
// [END maps_android_3d_camera_fly_around_kt]
```

---

## Scenario 2: The Keyframe Flyover Tour (`AdvancedCameraAnimationActivity`)
Description: A guided tour that visits multiple points of interest sequentially, waiting for the camera animation and 3D scene mesh to settle at each stop before proceeding, with support for stationary target tracking and 360° orbits.

### The Strategy
1.  **Fly to Stop**: Move to the current keyframe POI using `map.flyCameraTo(flyToOptions { ... })`.
2.  **Wait**: Wait for the camera animation to complete and `setOnMapSteadyListener` to report `true`.
3.  **Pause / Orbit**: Hold the view or execute `map.flyCameraAround(flyAroundOptions { ... })`.
4.  **Advance**: Move to the next keyframe stop.
5.  **Reference Implementation**: See `Maps3DSamples/ApiDemos/common/src/main/java/com/example/maps3d/common/AdvancedCameraEngine.kt` and `AdvancedCameraAnimationActivity` across `ComposeDemos`, `kotlin-app`, and `java-app`.

---

## Scenario 3: The Fragment Interop Overlay (`PlaceDetailsActivity` & `PlacesUIKit3D`)
Description: Hosting a traditional Android Fragment (like the Places UI Kit `PlaceDetailsCompactFragment`) inside a Compose or View UI anchored to 3D map place click events (`setMap3DClickListener`).

### The Strategy
1.  **Listen for Place Clicks**: Register `map.setMap3DClickListener { event -> ... }` and inspect `event.placeId` (returning `true` when consumed).
2.  **Single Instantiation in Compose**: Create the `FragmentContainerView` and transact `PlaceDetailsCompactFragment.newInstance(...)` once in the `factory` block of `AndroidView`.
3.  **Decoupled Updates**: Use `LaunchedEffect(placeId)` to call `fragment.loadWithPlaceId(placeId)` when the selected `placeId` changes, avoiding fragment recreation on recomposition.
4.  **Reference Implementations**:
    *   `Maps3DSamples/ComposeDemos/app/src/main/java/com/example/composedemos/placedetails/PlaceDetailsActivity.kt`
    *   `PlacesUIKit3D/` (Full standalone showcase with Place Search, Place Autocomplete, Place Details, and elevation-aware 3D camera framing).

---

## Scenario 4: Continuous Route & Path Following (`RoutesActivity` & `PathFollowingActivity`)
Description: Simulating a smooth, frame-driven flight along a 3D polyline route, updating camera and/or 3D models on each hardware frame (`withFrameNanos` / `Choreographer.FrameCallback`) with decoupled user gesture overrides.

### The Strategy
1.  **Pure Kinematic Engine**: Compute cumulative distances, interpolated `LatLngAltitude` positions, and lookahead headings in a pure Kotlin math/controller engine (`PathFollowingEngine` / `PathFollowingController`).
2.  **Frame-Synchronized Loop**: Drive time integration using `Choreographer.FrameCallback` (Views) or `withFrameNanos` (Compose).
3.  **Dual-Polyline Progress & Gesture Decoupling**: Render both the completed and remaining route polylines (`updatePolyline` / `addPolyline`), and allow user pan/tilt/zoom gestures to decouple from the automated camera heading/tilt while following the path.
4.  **Reference Implementations**:
    *   `Maps3DSamples/ApiDemos/common/src/main/java/com/example/maps3d/common/PathFollowingEngine.kt`
    *   `Maps3DSamples/ComposeDemos/app/src/main/java/com/example/composedemos/pathfollowing/PathFollowingActivity.kt`
    *   `Maps3DSamples/ComposeDemos/app/src/main/java/com/example/composedemos/routes/RoutesActivity.kt`
