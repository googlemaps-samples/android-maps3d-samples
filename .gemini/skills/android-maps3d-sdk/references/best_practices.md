# Best Practices for Android Maps 3D SDK

This document explains the architectural decisions and constraints when working with the Google Maps 3D SDK for Android.

## 1. Lifecycle Management

The `Map3DView` is a heavy component that requires explicit lifecycle management. Failing to forward lifecycle events can lead to memory leaks, crashes, or black screens.

### The Observer Pattern
Instead of overriding lifecycle methods in the Activity (like `onStart`, `onResume`, etc.), it is best practice to use a `DefaultLifecycleObserver`. This keeps the Activity code clean and ensures that lifecycle events are automatically forwarded.

*Constraint*: `onCreate`, `onLowMemory`, and `onSaveInstanceState` still need manual handling in the Activity as they are not fully covered by the standard lifecycle observer or require specific arguments (like `Bundle`).

## 2. Initialization Delay (Cold Starts)

When the 3D map is first loaded, the viewport layout and binding matrix may not be fully stable immediately after `onMap3DViewReady` is called.

### The Delay Pattern
If you attempt to add objects or set the camera immediately in `onMap3DViewReady`, it might fail or render incorrectly on cold starts.
*Rule*: Always introduce a delay before initializing map elements to ensure the renderer is fully ready. A **1-second delay** (e.g., `delay(1000)` or `Handler.postDelayed` with 1000ms) is recommended to bypass these readiness bugs.




## 3. Object Management and Cleanup

The underlying `GoogleMap3D` engine instance is effectively created once per application lifecycle (singleton-like behavior). It persists even across Activity recreation.

### The Cruft Pitfall
If you add markers, polylines, or polygons to the map and do not remove them when the Activity is destroyed, they will remain on the map. When a new Activity instance is created, the user will see the old objects ("cruft").

### The Delegate Solution
Use a wrapper like `TrackedMap3D` to keep track of all objects added during a session. Hook into the `onDestroy` lifecycle event to call `clearAll()` on this delegate, ensuring a clean state for the next usage.
