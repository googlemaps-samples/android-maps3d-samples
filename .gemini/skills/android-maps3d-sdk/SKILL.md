---
name: android-maps3d-sdk
description: Use this skill when the user wants to integrate the Google Maps 3D SDK into an Android application. This skill provides procedural guidance for setup, lifecycle, and 3D object manipulation. Do NOT use for standard 2D maps.
---

# Android Maps 3D SDK Integration

This skill guides you through integrating the Google Maps 3D SDK into an Android project. It follows the principles of progressive disclosure and relies on environment-specific templates.

## Prerequisites & Skill Alignment

> [!NOTE]
> This skill should be used in conjunction with:
> - **Android Architecture Skill**: For proper MVVM/MVI layering.
> - **Android Security Skill**: For API key protection and permissions.

## Procedural Workflow

### Step 1: Determine the Environment
You MUST ask the user which development environment they are targeting before providing code:
1. **Kotlin (Views)** (Prioritized)
2. **Kotlin + Compose**
3. **Java**

Additionally, ask if they want to use the **Object Tracking Delegate** pattern (useful for clean cleanup in complex apps).

Based on their response, you will load the appropriate template from the `references/` directory.

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
After the user selects the environment, load the corresponding file from:
- Kotlin (Views): `references/kotlin_views_template.md`
- Kotlin + Compose: (To be created)
- Java: (To be created)

### Step 4: Apply Best Practices
Follow these strict procedural rules to avoid common pitfalls:

1. **Initialization Delay**: Fails on cold starts due to viewport layout races. Always use a 500ms delay before initializing map elements (camera updates, adding objects).
2. **Double-Wait Pattern**: For animations, wait for camera animation end AND map steady state.
3. **Object Updates**: Use matching IDs to update Polygons/Polylines instead of removing and re-adding (prevents flickering).
4. **Null Safety**: Camera properties like heading and tilt can be null. Handle them defensively.
