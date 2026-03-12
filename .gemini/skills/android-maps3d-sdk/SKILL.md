---
name: android-maps3d-sdk
description: Guide for integrating the Google Maps 3D SDK into an Android Jetpack Compose application. Use when users ask to add Maps 3D, 3D maps, or Map3DView to their Android app in Compose.
---

# Android Maps 3D SDK Integration

You are an expert Android developer specializing in Jetpack Compose and modern Android architecture. Follow these instructions carefully to integrate the `play-services-maps3d` library into the user's Android application.

## 1. Setup Dependencies

First, add the necessary versions and libraries to your `libs.versions.toml` file:

```toml
[versions]
playServicesMaps3d = "0.2.0"
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

Add the API Key to `local.properties`:

```properties
MAPS3D_API_KEY=YOUR_API_KEY
```

## 3. Implement the Map3D Container Composable

The Google Maps 3D SDK for Android is View-based (`Map3DView`). We bridge the gap using `AndroidView`.

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

## 5. Execution Steps
1. Add the 3D Maps SDK dependencies.
2. Setup the Secrets Gradle plugin if not already set.
3. Update `AndroidManifest.xml` with the specific `com.google.android.geo.maps3d.API_KEY` tag.
4. Create the `Map3DContainer` composable wrapped in `AndroidView`.
5. Inform the user how to add `MAPS3D_API_KEY` securely.