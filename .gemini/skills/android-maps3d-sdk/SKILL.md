---
name: android-maps3d-sdk
description: Use this skill when the user wants to integrate the Google Maps 3D SDK into an Android application. This skill provides procedural guidance for setup, lifecycle, and 3D object manipulation. Do NOT use for standard 2D maps.
---

# Android Maps 3D SDK Integration

This skill guides you through integrating the Google Maps 3D SDK into an Android project. It follows the principles of progressive disclosure and relies on environment-specific templates.

> [!IMPORTANT]
> **Limited Training Data Alert**: The Google Maps 3D SDK is a new technology. Your internal parametric knowledge is likely outdated or incomplete. You **MUST NOT** guess API signatures. You **MUST** consult the local samples in `assets/samples/` and the external documentation listed in `references/documentation.md` before generating any code.

## Prerequisites & Skill Alignment

> [!NOTE]
> This skill should be used in conjunction with:
> - **Android Architecture Skill**: For proper MVVM/MVI layering.
> - [Android Security Skill](https://github.com/kikoso/android-skills/blob/main/android-security-skill/SKILL.md): For API key protection and permissions.


## Procedural Workflow

### Step 0: Research Existing Patterns
Before asking the user for clarification or writing code, search the local workspace or repository for existing implementations of the requested feature.
1. Use your available search tools to look for keywords (e.g., "Polygon", "FlyTo") in the `snippets/` and `Maps3DSamples/` directories.
2. Consult `references/documentation.md` for a map of where to look.


### Step 1: Determine the Environment and Features
You MUST ask the user to clarify their stack and needs:

1.  **Language**: Kotlin or Java?
2.  **UI Framework**: Jetpack Compose or standard XML Views?
3.  **Features**: Do they need automatic object management (cleanup)?

Based on their response, follow the **Selection Logic** to retrieve boilerplate from `assets/samples/` and consult rules in `references/`.

### Implementation Guidance (Selection Logic)
1.  Identify the user's stack: (Language: Kotlin/Java, UI: Compose/Views).
2.  Check if Lifecycle Management is required (always for 3D maps).
3.  **Retrieve** the corresponding boilerplate from `assets/samples/` (e.g., `assets/samples/views_kotlin/MapActivity.kt.txt`).
4.  If object tracking is needed, **retrieve** the snippet from `assets/samples/views_kotlin/snippets/`.
5.  **Reference** the memory management best practices in `references/best_practices.md` to ensure the generated code follows SDK safety guidelines.

### Step 2: Base Setup
Regardless of the environment, the following setup is required.

#### 1. Dependencies
Add the necessary versions and libraries to your `libs.versions.toml` file:

```toml
[versions]
# NOTE: Verify this is the latest version of the Maps 3D SDK
playServicesMaps3d = "0.2.0"
lifecycleRuntimeKtx = "2.8.5"

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
    - Layout: `assets/samples/views_kotlin/activity_main.xml`
    - Activity: `assets/samples/views_kotlin/MapActivity.kt.txt`
    - Snippet (Object Manager): `assets/samples/views_kotlin/snippets/object_manager_usage.kt.txt`
- Kotlin + Compose: (To be created in `assets/samples/compose/`)
- Java: 
    - Layout: `assets/samples/views_java/activity_main.xml`
    - Activity: `assets/samples/views_java/MapActivity.java.txt`
    - Snippet (Object Manager): `assets/samples/views_java/snippets/object_manager_usage.java.txt`


### Step 4: Apply Best Practices
Consult `references/best_practices.md` for detailed explanation of rules. Key rules to enforce:
1. **Initialization Delay**: Always use a 1-second delay before initializing map elements.
2. **Object Management**: Use the `TrackedMap3D` delegate to clean up objects on destroy to avoid cruft.
3. **Utilities**: Use validation utilities to prevent crashes, and path utilities for smoothing/simplification (see `references/utilities_kotlin.md` or `references/utilities_java.md`).

4. **Double-Wait Pattern**: For animations, wait for camera animation end AND map steady state.
5. **Object Updates**: Use matching IDs to update Polygons/Polylines instead of removing and re-adding.
6. **Unit Conversions**: For type-safe measurements and conversions, see `references/units_kotlin.md` or `references/units_java.md` (Optional).
7. **Secrets Security**: NEVER add `secrets.properties` to version control. NEVER add real API keys to source code or `local.defaults.properties` (see `references/secrets_enforcement.md` for Gradle enforcement snippet).
8. **Common Operations**: Consult the language-specific catalog for short reference snippets (Marker, Polyline, Animation, etc.):
    *   Java: `references/catalog_java.md`
    *   Kotlin + Views: `references/catalog_kotlin_views.md`
    *   Jetpack Compose: `references/catalog_compose.md`





