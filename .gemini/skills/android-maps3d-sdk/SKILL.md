---
name: android-maps3d-sdk
description: Guide for integrating the Google Maps 3D SDK into an Android Jetpack Compose application. Use when users ask to add Maps 3D, 3D maps, or Map3DView to their Android app in Compose.
---

# Android Maps 3D SDK Integration

You are an expert Android developer specializing in Jetpack Compose and modern Android architecture. Follow these instructions carefully to integrate the `play-services-maps3d` library into the user's Android application.

We should start with a few questions about how the developer want to use `Maps3DView`.

Are they using or planning on using Jetpack Compose?

Are they using or planning on using dependency injection (such as Hilt or Koin)?

## 1. Setup Dependencies

First, add the necessary versions and libraries to your `libs.versions.toml` file:

```toml
[versions]
# NOTE: Verify this is the latest version of the Maps 3D SDK, as it is subject to change.
playServicesMaps3d = "0.2.0"
# NOTE: Verify this is the latest version of lifecycle-runtime-ktx.
lifecycleRuntimeKtx = "2.8.5"

[libraries]
play-services-maps3d = { group = "com.google.android.gms", name = "play-services-maps3d", version.ref = "playServicesMaps3d" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }
```

Then, add the dependencies to the app-level `build.gradle.kts` file. 

```kotlin
dependencies {
    // Google Maps 3D SDK
    implementation(libs.play.services.maps3d)
    
    // Lifecycle Runtime KTX for Coroutine interop
    implementation(libs.androidx.lifecycle.runtime.ktx)
}
```

## 2. Setup the Secrets Gradle Plugin

Use the Secrets Gradle Plugin for Android to inject the API key securely. In app-level `build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.secrets.gradle.plugin)
}

secrets {
    propertiesFileName = "secrets.properties"
    defaultPropertiesFileName = "local.defaults.properties"
}
```

In `AndroidManifest.xml`, add the required permissions and reference the injected API key meta-data:

```xml
<manifest ...>
    <!-- Required for Google Maps 3D -->
    <uses-permission android:name="android.permission.INTERNET" />

    <application ...>
        <!-- Google Maps 3D API Key injected by Secrets Gradle Plugin -->
        <!-- Note the specific name for Maps 3D -->
        <meta-data
            android:name="com.google.android.geo.maps3d.API_KEY"
            android:value="${MAPS3D_API_KEY}" />
        ...
    </application>
</manifest>
```

Add the API Key to `secrets.properties`:

```properties
MAPS3D_API_KEY=YOUR_API_KEY
```

## 3. Implement the Map3D Container Composable

If the user is working in a Jetpack Compose app or is creating a Compose app, We can use an
`AndroidView` to bridge between the View-based `Map3DView` and Jetpack Compose.

```kotlin
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Map3DMode
import com.google.android.gms.maps.model.Map3DOptions
import com.google.android.gms.maps.Map3DView
import com.google.android.gms.maps.GoogleMap3D
import com.google.android.gms.maps.OnMap3DViewReadyCallback

@Composable
fun Map3DContainer(
    modifier: Modifier = Modifier,
    options: Map3DOptions
) {
    // 1. Hoist State: Remember the map object
    var googleMap by remember { mutableStateOf<GoogleMap3D?>(null) }
    
    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                Map3DView(context, options).apply {
                    // Manually call onCreate.
                    onCreate(null) 
                }
            },
            update = { view ->
                view.getMap3DViewAsync(
                    object : OnMap3DViewReadyCallback {
                        override fun onMap3DViewReady(map3D: GoogleMap3D) {
                            googleMap = map3D // Capture the controller
                        }
                        override fun onError(e: Exception) {
                            googleMap = null
                            throw e
                        }
                    }
                )
            },
            onRelease = { view -> 
                googleMap = null
                view.onDestroy() 
            }
        )
    }
}
```

## 4. Best Practices & Guidelines
*   **Double-Wait Pattern:** Triggering animations from Compose buttons requires the **Double-Wait** pattern (`awaitCameraAnimation` + `awaitSteady`) to ensure peak visual quality.
*   **Coroutine Bridging:** Animations in the 3D SDK are fire-and-forget. Use an `awaitCameraAnimation(map: GoogleMap3D)` suspend wrapper function using `suspendCancellableCoroutine` for structured concurrency:

```kotlin
suspend fun awaitCameraAnimation(map: GoogleMap3D) = suspendCancellableCoroutine { continuation ->
    map.setCameraAnimationEndListener {
        map.setCameraAnimationEndListener(null) // Cleanup listener to avoid leaks
        if (continuation.isActive) {
            continuation.resume(Unit)
        }
    }
    continuation.invokeOnCancellation {
        map.setCameraAnimationEndListener(null)
    }
}
```

*   **Lifecycle:** You must pass lifecycle events down to `Map3DView`. In Compose, `factory` block takes care of instantiation and `onRelease` handles cleanup (`onDestroy()`). Ensure `onCreate` is called in the factory block.
    *   *Critical Note:* The underlying `GoogleMap3D` engine instance is effectively created once per application lifecycle. If your `AndroidView` Composable leaves the composition and later returns (creating a new `Map3DView`), the underlying 3D engine may still retain previously added objects (like Polygons) from the destroyed view. You must manually clear or track your objects to avoid duplicates across recompositions or Navigation transitions.
*   **Initialization & Adding Objects:** Do **not** attempt to set the camera or add 3D objects (like Polygons) immediately after the `GoogleMap3D` reference is ready. The renderer needs time to warm up.
    *   **Initial Camera:** Always set the initial camera position declaratively via `Map3DOptions` (passed into your container view) rather than imperatively moving the camera after the map loads. This avoids dizzying "flight" animations from coordinate `(0,0)` on startup.
    *   **Adding Objects:** Only inject geometries into the scene after the map has signaled it is fully ready and stable. Typically, this means waiting for an `onMapSteady` callback.
*   **Updating Map Objects:** When updating an existing Map Object (e.g., `Polygon`, `Polyline`), do **not** use `remove()` and re-add a new one, as this causes flickering. Instead, use `getId()` from the existing object and pass it to a new `PolygonOptions` (or equivalent) builder, then call `addPolygon()` with those new options on the same `GoogleMap3D` instance. The SDK uses the matching ID to update the existing object gracefully without flickering.
*   **Nullable Camera Properties:** The 3D SDK's `Camera` object has 6 degrees of freedom. Properties like `heading`, `tilt`, `roll`, and `range` are returned as `Double?` (nullable) since the renderer does not always guarantee a value for every property. Handle these nulls defensively when extracting camera telemetry, especially when persisting position data.
*   **Parameter Validation:** The Maps 3D library will throw exceptions and crash if passed out-of-bounds telemetry for camera movements or locations. Standardize a validation/coercion layer (e.g., returning a `toValidCamera()` extension object) covering:
    *   `latitude`: clamped to `[-90.0, 90.0]`
    *   `longitude`: clamped to `[-180.0, 180.0]`
    *   `tilt`: clamped to `[0.0, 90.0]`
    *   `range`: clamped to `[0.0, 63170000.0]`
    *   `heading`: wrapped to `[0.0, 360.0]`
    *   `roll`: wrapped to `[-360.0, 360.0]`
    *   `altitude`: clamped to `[0.0, MAX_ALTITUDE_METERS]`

    **Example Extension:**
    ```kotlin
    /** Helper to wrap cyclic values like heading and roll */
    fun Double.wrapIn(lower: Double, upper: Double): Double {
        val range = upper - lower
        if (range <= 0) return this
        val offset = this - lower
        return lower + (offset - Math.floor(offset / range) * range)
    }

    /** Extension to sanitize camera telemetry before passing to engine */
    fun Camera?.toValidCamera(): Camera {
        val source = this ?: return Camera.DEFAULT_CAMERA
        return camera {
            center = latLngAltitude {
                latitude = source.center.latitude.coerceIn(-90.0..90.0)
                longitude = source.center.longitude.coerceIn(-180.0..180.0)
                altitude = source.center.altitude.coerceIn(0.0..LatLngAltitude.MAX_ALTITUDE_METERS)
            }
            heading = source.heading?.toDouble()?.wrapIn(0.0, 360.0) ?: 0.0
            tilt = source.tilt?.toDouble()?.coerceIn(0.0..90.0) ?: 60.0
            roll = source.roll?.toDouble()?.wrapIn(-360.0, 360.0) ?: 0.0
            range = source.range?.toDouble()?.coerceIn(0.0..63170000.0) ?: 1500.0
        }
    }
    ```

*   **Immutable Updates (`copy` Extensions):** The 3D SDK builders (like `camera {}` or `latLngAltitude {}`) do not natively provide a `copy()` method like Kotlin data classes. To gracefully update a single property (like altitude) while retaining the rest of the object's complex state, implement custom `.copy()` extensions:

    ```kotlin
    /** Extension to clone and modify a Camera */
    fun Camera.copy(
        center: LatLngAltitude? = null,
        heading: Double? = null,
        tilt: Double? = null,
        range: Double? = null,
        roll: Double? = null,
    ): Camera {
        val objectToCopy = this
        return camera {
            this.center = center ?: objectToCopy.center
            this.heading = heading ?: objectToCopy.heading
            this.tilt = tilt ?: objectToCopy.tilt
            this.range = range ?: objectToCopy.range
            this.roll = roll ?: objectToCopy.roll
        }
    }

    /** Extension to clone and modify a LatLngAltitude */
    fun LatLngAltitude.copy(
        latitude: Double? = null,
        longitude: Double? = null,
        altitude: Double? = null,
    ): LatLngAltitude {
        val objectToCopy = this
        return latLngAltitude {
            this.latitude = latitude ?: objectToCopy.latitude
            this.longitude = longitude ?: objectToCopy.longitude
            this.altitude = altitude ?: objectToCopy.altitude
        }
    }
    ```

## 5. A Note on Initialization

Immediate Setup (onMap3DViewReady): Fails on cold starts because the viewport layout and binding matrix are not yet stable. Camera updates are completely ignored, and overlays may render offset.
OnMapReady & OnMapSteady Listeners: These callbacks are strictly edge-triggered. While they may fire on a cold start, they will skip execution entirely on a warm restore (e.g., returning to the Activity) because the view is already considered ready/steady. This leaves the user with a frozen camera state and missing overlays.
The Solution: Timer-Based Delay Workaround
Until the SDK introduces native Coroutine support (like an .awaitMap() extension) or synchronous state getters (like isMapReady), the most reliable workaround for both cold and warm starts is a timer-based delay. By intentionally deferring the initialization logic slightly, we bypass the brittle edge-triggered listeners entirely.

Kotlin Implementation (Preferred)
Use a coroutine with delay() inside your initialization flow:

    ```kotlin
    // Ensure you are launching on the Main thread to interact with the Map3DView safely
    lifecycleScope.launch {
        // Wait for the viewport to fully inflate and bindings to stabilize.
        // 500ms is a safe brute-force threshold to avoid edge-trigger races.
        delay(500) 
        
        // Position camera and add overlays safely
        setupMapElements()
    }
    ```

Java Implementation
Use a standard Handler mapped to the Main Looper:

    ```java
    new Handler(Looper.getMainLooper()).postDelayed(() -> {
        // Wait for the viewport to fully inflate, then safely apply updates
        setupMapElements();
    }, 500);
    ```

[IMPORTANT] Even with the timer delay successfully ensuring your camera updates fire, you must still implement an isInitialized boolean latch
(or dynamically check if your layers exist) within setupMapElements(). Otherwise, you will endlessly stack duplicate markers, model nodes, 
and polyline overlays on top of each other during every warm Activity re-entry.

## 6. Execution Steps
1. Add the 3D Maps SDK dependencies.
2. Setup the Secrets Gradle plugin if not already set.
3. Update `AndroidManifest.xml` with the specific `com.google.android.geo.maps3d.API_KEY` tag.
4. Create the `Map3DContainer` composable wrapped in `AndroidView`.
5. Inform the user how to add `MAPS3D_API_KEY` securely.
