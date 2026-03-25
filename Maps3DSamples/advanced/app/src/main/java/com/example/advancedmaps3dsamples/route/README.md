# Advanced Route Following

This module demonstrates advanced, cinematic drone-style route tracking using the Google Maps 3D SDK for Android and Jetpack Compose.

## Architecture

This sample deviates from simple waypoint jumping by treating the camera tracking operation as a continuous, frame-driven physical simulation. The UI and the logic are separated using Literate Programming principles:

1. **`RouteSampleScreen`**: The UI Shell. Handles structural elements like the App Bar and coordinates the data flow from the `RouteViewModel`. State variables (like speed, playback status, selected tracker) are hoisted here.
2. **`Map3DViewport`**: The physical bridge to the 3D map. It handles the `AndroidView` lifecycle and propagates the initialized `GoogleMap3D` instance up to the Compose hierarchy.
3. **`RouteFlightEngine`**: The core logic engine. It is entirely headless (draws no UI). It launches a `LaunchedEffect` that loops on the monotonic frame clock (`withFrameMillis`). On each frame, it calculates the drone's position, rotation, and lookahead trajectory based purely on the `elapsedDistance`, applying `lerp` inertia for silky smooth camera movements.
4. **Interactive UI Overlays**: Separated functional Composables (`StandardControlsOverlay`, `PlaybackControlsOverlay`, and `CameraControlsOverlay`) handle sliders and buttons independently, avoiding unnecessary recompositions of the heavy 3D rendering elements.

## Key Concepts Demonstrated

* **Continuous Frame Tracking (`withFrameMillis`)**: Avoiding chopped waypoint animation in favor of organic 60FPS fluid physics.
* **Primitive State Re-capture (`rememberUpdatedState`)**: Passing changing external primitives into a long-running `LaunchedEffect` loop without causing the loop to tear down and restart.
* **Geospatial Interpolation**: Calculating precise physical latitude/longitude positions given a raw distance along a complex polygonal route.
* **Detached Rotational Physics**: Tracking lookahead vectors to continuously align the camera target and underlying models using shortest-path math (`slerpHeading`).
* **SDK Object Mutation**: Forcing immediate orientation updates on 3D Models (`m.orientation = ...`) to bypass builder instantiation caching and achieve smooth per-frame model rotations.

## The Model Tracker Pattern

The user can iterate through a sequence of `RouteTracker` representations:
- 2D Default Pin Marker
- Scaled Red Car 3D object
- Scaled Banana Car 3D object

For performance and to satisfy constraints, only one tracker is visible at a time (`AltitudeMode.RELATIVE_TO_GROUND` or `CLAMP_TO_GROUND`). Inactive models are dynamically teleported to Null Island (0°N, 0°E, altitude 0.0) via `AltitudeMode.ABSOLUTE` so they don't break rendering validation but remain initialized in the SDK cache.

## Acknowledgement

This architecture embraces that for production enterprise apps, deep routing and navigation physics may sit inside background Service layers. For this sample, keeping the `RouteFlightEngine` structurally coupled into the Compose tree illustrates how Jetpack Compose primitives can synchronize with external declarative view systems like Google Maps 3D.
