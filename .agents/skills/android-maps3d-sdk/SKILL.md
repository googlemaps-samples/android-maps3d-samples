---
name: android-maps3d-sdk
description: Integrates the Google Maps 3D SDK into an Android application across Jetpack Compose, Kotlin Views, and Java Views. Provides procedural guidance for setup, lifecycle management, map modes (Hybrid, Satellite, Roadmap), UI controls, map localization, camera animations and restrictions, 3D projection, and 3D object manipulation (Markers, Polylines, Polygons, Models, Popovers). Use when the user wants to build immersive 3D map experiences or migrate from 2D maps.
license: Apache-2.0
compatibility: Requires an Android project and internet access. Uses play-services-maps3d version 16.0.1 or higher.
metadata:
  version: "1.2"
  author: Google Maps Samples
---


# Android Maps 3D SDK Integration

This skill guides you through integrating the Google Maps 3D SDK into an Android project. It follows the principles of progressive disclosure and relies on environment-specific templates and verified region-tagged snippets.

> [!IMPORTANT]
> **Limited Training Data Alert**: The Google Maps 3D SDK is a rapidly evolving technology. Your internal parametric knowledge is likely outdated or incomplete. You **MUST NOT** guess API signatures (such as legacy `Map3DOptions`, `getMapAsync`, `flyTo`, `addOnMapClickListener`, or `map3d:mode`). You **MUST** consult the local templates in `assets/samples/`, the reference catalogs in `references/`, and the repository map in `references/documentation.md` before generating any code.

## Prerequisites & Skill Alignment

> [!NOTE]
> This skill should be used in conjunction with:
> - **Android Architecture Skill**: For proper MVVM/UDF layering (`StateFlow`, `asLiveData()`, injectable `CoroutineDispatcher`, and pure math/controller engines).
> - [Android Security Skill](https://github.com/kikoso/android-skills/blob/main/android-security-skill/SKILL.md): For API key protection and permissions.


## Procedural Workflow

### Step 0: Research Existing Patterns
Before asking the user for clarification or writing code, search the local workspace or repository for existing implementations of the requested feature.
1. Use your available search tools to look for keywords (e.g., `"Polygon"`, `"flyCameraTo"`, `"UiControlSettings"`, `"LocaleOptions"`, `"CameraRestriction"`, `"Projection3D"`) in the `snippets/`, `Maps3DSamples/`, `maps3d-compose/`, and `PlacesUIKit3D/` directories.
2. Consult `references/documentation.md` for a complete map of the expanded sample catalog across Jetpack Compose, Kotlin Views, Java Views, Places UI Kit 3D, and Documentation Snippets.


### Step 1: Determine the Environment and Features (Stack Detective)
You MUST proactively discover the environment by inspecting the codebase before writing any code. Do NOT ask the user unless the environment is highly ambiguous.

1.  **Run Search/Grep**:
    *   Search for `androidx.compose`, `compose-compiler`, or `maps3d-compose` in `build.gradle.kts` or `libs.versions.toml` to detect **Jetpack Compose**.
    *   Search for `com.android.application` or `com.android.library` to understand the module type.
    *   Look for `.kt` vs `.java` files to determine the dominant **Language**.
2.  **Identify Features**: Determine if the user needs automatic object management (cleanup) or specific 3D features based on their request:
    *   **Core Scene & Map Modes**: `Map3DMode.HYBRID`, `Map3DMode.SATELLITE`, `Map3DMode.ROADMAP`, Cloud Map Styling (`mapId`), Map Localization (`LocaleOptions` / `setLocale`).
    *   **Built-in UI Controls**: `UiControlSettings` (`ZoomControl`, `TiltControl`, `RotateControl`, `PanHorizontalControl`, `PanVerticalControl`, `CompassControl`, `Spacer`) placed on `LEADING`, `TRAILING`, `TOP`, or `BOTTOM` edges.
    *   **Camera & Kinematics**: `setCamera`, `flyCameraTo`, `flyCameraAround`, `stopCameraAnimation`, `setCameraChangedListener`, `setCameraRestriction` (`CameraRestriction` + `LatLngBounds`), Field of View (FOV), Path Following, Advanced Camera Animation, and 3D-to-2D Screen Projection (`Projection3D`).
    *   **3D Overlays & Shapes**: `Marker` (with `PinConfiguration` & `Glyph`), `Polyline` (extruded/geodesic/outlined), `Polygon` (extruded/holes via `Hole`), `Model` (glTF/GLB with `Orientation` & `Vector3D` scale), `Popover` (anchored `View` info bubbles), and Data Visualization.
    *   **Interactivity & Places**: `setMap3DClickListener` (`Map3DClickEvent` with `location`, `placeId`, and `Boolean` consumption return) and Places UI Kit 3D integration (`PlacesUIKit3D`).

Based on your discovery, follow the **Selection Logic** to retrieve boilerplate from `assets/samples/` and consult rules in `references/`.

### Implementation Guidance (Selection Logic)
1.  Identify the user's stack: (Language: Kotlin/Java, UI: Compose/Views).
2.  Check if Lifecycle Management is required (always for 3D maps).
3.  **Retrieve** the corresponding boilerplate from `assets/samples/` (e.g., `assets/samples/views_kotlin/MapActivity.kt.txt`).
4.  If object tracking is needed, **retrieve** the snippet from `assets/samples/views_kotlin/snippets/`.
5.  **Reference** the memory management and SDK rules in `references/best_practices.md` to ensure the generated code follows SDK safety guidelines.

### Step 2: Base Setup
Regardless of the environment, the following setup is required.

#### 1. Dependencies (Dynamic Version Resolution)
Before adding the dependency, you MUST identify the latest version.
1.  Run `./gradlew :app:dependencies | grep maps3d` to check if a version is already resolved.
2.  Or search the Google Maven repository or use available tools to find the latest version (must be **at least** `16.0.1`).

Add the necessary versions and libraries to your `libs.versions.toml` file:

```toml
[versions]
# NOTE: Verify this is the latest version of the Maps 3D SDK (must be at least "16.0.1")
playServicesMaps3d = "16.0.1"
lifecycleRuntimeKtx = "2.10.0"

[libraries]
play-services-maps3d = { group = "com.google.android.gms", name = "play-services-maps3d", version.ref = "playServicesMaps3d" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }
```

Then, add the dependencies to the app-level `build.gradle.kts` file:

```kotlin
dependencies {
    // Google Maps 3D SDK
    implementation(libs.play.services.maps3d)
    // Lifecycle Runtime KTX for Coroutine interop
    implementation(libs.androidx.lifecycle.runtime.ktx)
}
```

#### 2. API Key & Manifest
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
        <!-- Note the specific name for Maps 3D -->
        <meta-data
            android:name="com.google.android.geo.maps3d.API_KEY"
            android:value="${MAPS3D_API_KEY}" />
        ...
    </application>
</manifest>
```

### Step 3: Load Environment Template
After determining the stack, load the corresponding files from `assets/samples/`:
- Kotlin (Views):
    - Layout: `assets/samples/views_kotlin/activity_main.xml` (uses `map3d:mapMode="hybrid"`)
    - Activity: `assets/samples/views_kotlin/MapActivity.kt.txt`
    - Snippet (Object Manager): `assets/samples/views_kotlin/snippets/object_manager_usage.kt.txt` and `assets/samples/views_kotlin/snippets/TrackedMap3D.kt.txt`
- Kotlin + Compose: Refer to `references/catalog_compose.md` for both the declarative `GoogleMap3D` composable (`maps3d-compose`) and raw `AndroidView` + `Map3DView(context, Map3DMode.HYBRID)` patterns.
- Java:
    - Layout: `assets/samples/views_java/activity_main.xml` (uses `map3d:mapMode="hybrid"`)
    - Activity: `assets/samples/views_java/MapActivity.java.txt`
    - Snippet (Object Manager): `assets/samples/views_java/snippets/object_manager_usage.java.txt` and `assets/samples/views_java/snippets/TrackedMap3D.java.txt`


### Step 4: Apply Best Practices
Consult `references/best_practices.md` for detailed explanation of rules. Key rules to enforce:
1. **Valid `mapMode` Required at Initialization**: Always specify `map3d:mapMode="hybrid"` (or `"satellite"` / `"roadmap"`) in XML or pass `Map3DMode` / `Map3DInitConfig.create(..., mapMode = ...)` when constructing `Map3DView` programmatically. Never use the legacy `map3d:mode` attribute or removed `Map3DOptions` class.
2. **Initialization Delay & Readiness**: Wait for `setOnMapReadyListener` or introduce a short delay before initializing map elements on cold starts.
3. **Object Management**: Use the `TrackedMap3D` delegate to clean up objects (`Marker`, `Polyline`, `Polygon`, `Model`, `Popover`, and `UiControlSettings`) on destroy to avoid cruft across singleton engine reuse.
4. **Click Listener Contract**: `setMap3DClickListener` receives a `Map3DClickEvent` (`event.location`, `event.placeId`) and must return a `Boolean` (`true` if consumed, `false` otherwise). Object-level `setClickListener` callbacks run off the UI thread—always dispatch UI updates to the main thread.
5. **Utilities**: Use validation utilities (`toValidCamera()`) to prevent crashes, and path utilities for smoothing/simplification (see `references/utilities_kotlin.md` or `references/utilities_java.md`).
6. **Double-Wait Pattern**: For animations, wait for camera animation end AND map steady state (`setOnMapSteadyListener`).
7. **Unit Conversions**: For type-safe measurements and conversions, see `references/units_kotlin.md` or `references/units_java.md` (Optional).
8. **Secrets Security**: NEVER add `secrets.properties` to version control. NEVER add real API keys to source code or `local.defaults.properties` (see `references/secrets_enforcement.md` for Gradle enforcement snippet).
9. **Common Operations**: Consult the language-specific catalog for verified, region-tagged reference snippets (Markers, Polylines, Polygons, Models, Popovers, Camera Animation & Restrictions, Map Localization, UI Controls):
    *   Java: `references/catalog_java.md`
    *   Kotlin + Views: `references/catalog_kotlin_views.md`
    *   Jetpack Compose: `references/catalog_compose.md`
10. **Scenario Storyboards**: For complex, multi-step workflows (e.g., Immersive Arrival, Flyover Tour, Places UI Kit Fragment Interop, Continuous Route Tracking), consult `references/scenarios.md`.
