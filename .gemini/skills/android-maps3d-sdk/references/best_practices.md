# Best Practices for Android Maps 3D SDK (`16.0.1+`)

This document explains the architectural decisions, initialization rules, and runtime constraints when working with the Google Maps 3D SDK for Android.

## 1. Mandatory `mapMode` at Initialization (SDK `0.2.2+` / `16.0.1`)

A valid `mapMode` (`Map3DMode.HYBRID`, `Map3DMode.SATELLITE`, or `Map3DMode.ROADMAP`) is strictly required when creating `Map3DView`.
*   **XML Layouts**: Always use `map3d:mapMode="hybrid"` (or `"satellite"` / `"roadmap"`). Never use the legacy `map3d:mode` attribute—omitting `map3d:mapMode` causes `Map3DView` to fail initialization with `IllegalStateException: Unable to load map. Please specify a valid map mode.`.
*   **Programmatic Construction**: The default `Map3DView(Context)` constructor and `Map3DOptions` class have been removed. Always pass either a `Map3DMode` (`Map3DView(context, Map3DMode.HYBRID)`) or a `Map3DInitConfig` (`Map3DView(context, Map3DInitConfig.create(..., mapMode = Map3DMode.HYBRID))`).

## 2. Lifecycle Management

The `Map3DView` is a heavy component that requires explicit lifecycle management. Failing to forward lifecycle events can lead to memory leaks, crashes, or black screens.

### The Observer Pattern
Instead of overriding lifecycle methods in the Activity (like `onStart`, `onResume`, etc.), use a `DefaultLifecycleObserver`. This keeps the Activity code clean and ensures that lifecycle events are automatically forwarded.

*Constraint*: `onCreate`, `onLowMemory`, and `onSaveInstanceState` still need manual handling in the Activity as they are not fully covered by the standard lifecycle observer or require specific arguments (like `Bundle`).

## 3. Initialization Delay & Scene Readiness (Cold Starts)

When the 3D map is first loaded, the viewport layout and binding matrix may not be fully stable immediately after `onMap3DViewReady` is called.

### The Delay & Readiness Pattern
If you attempt to add objects or set the camera immediately in `onMap3DViewReady`, it might fail or render incorrectly on cold starts.
*Rule*: Introduce a short delay (e.g., `delay(1000)` in a coroutine or `Handler.postDelayed` with 1000ms) or wait for `setOnMapReadyListener` (`sceneReadiness == 1.0`) before initializing map elements to ensure the renderer is ready. Note that `setOnMapReadyListener` may only fire once per application process, so combining it with a fallback delay (as done in `maps3d-compose`'s `GoogleMap3D`) is recommended when recreating views.

## 4. Object Management and Cleanup (`TrackedMap3D`)

The underlying `GoogleMap3D` engine instance behaves like a process-wide singleton and persists across Activity recreation.

### The Cruft Pitfall
If you add markers, polylines, polygons, 3D models, popovers, or UI control placements to the map and do not remove/reset them when the Activity is destroyed, they will remain on the map when a new Activity instance is created ("cruft").

### The Delegate Solution
Use a wrapper like `TrackedMap3D` (see `assets/samples/views_kotlin/snippets/TrackedMap3D.kt.txt` and `assets/samples/views_java/snippets/TrackedMap3D.java.txt`) to track all objects added during a session, and call `clearAll()` in `onDestroy`.

## 5. Click Listeners & Threading (SDK `16.0.1+`)

*   **Map & Place Clicks (`setMap3DClickListener`)**: Receives a `Map3DClickEvent` containing `event.location` (`LatLngAltitude`) and `event.placeId` (`String?`), and **must return a `Boolean`** (`true` if the click was consumed, `false` to allow default propagation).
*   **Object Clicks (`marker.setClickListener`, `polyline.setClickListener`, `polygon.setClickListener`, `model.setClickListener`)**: These callbacks do **NOT** execute on the Android main UI thread. Always switch to the main thread (`Handler(Looper.getMainLooper()).post { ... }` or `withContext(Dispatchers.Main)`) before showing a `Toast` or updating Android Views.

## 6. Default UI Controls Visibility

By default in sample screens outside dedicated UI control demos, keep built-in 3D camera UI controls (`ZoomControl`, `TiltControl`, `RotateControl`, `PanHorizontalControl`, `PanVerticalControl`, `CompassControl`) hidden by setting `emptyList()` on all four `ControlPlacement` edges (`TRAILING`, `LEADING`, `TOP`, `BOTTOM`), or expose a toggle in a Quick Settings overlay so overlays do not collide with status bars or custom HUD cards.
