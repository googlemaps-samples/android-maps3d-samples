# Android Maps 3D SDK Migration Guide

This guide assists developers in migrating their codebases to newer versions of the Android Maps 3D SDK.

> [!TIP]
> **AI Migration Prompt**: If you are using an AI coding assistant (such as Gemini, Copilot, or Cursor), you can use this document as a prompt to automatically migrate your codebase.
> 
> **Instructions for AI**:
> 1. Read the section corresponding to the version upgrade.
> 2. Analyze the "Before" and "After" patterns.
> 3. Scan the codebase for the "Before" patterns and apply the transformations to match the "After" patterns.
> 4. Ensure all imports are updated accordingly.

---

## Migrating from 0.2.0 to 0.2.2

This release introduces breaking changes to the initialization configuration and view construction.

### 1. Dependency Update
Update the SDK dependency version in your `build.gradle` or `gradle/libs.versions.toml`.

*   **Action**: Update `playServicesMaps3d` version to `0.2.2`.

### 2. Replace `Map3DOptions` with `Map3DInitConfig`

The `Map3DOptions` class has been removed. Use `Map3DInitConfig` instead.
*   **Rule**: `Map3DInitConfig` cannot be created via a constructor. Use the static `Map3DInitConfig.create()` factory method and chain `.copy(...)` to modify properties.

#### Code Transformation:

**Kotlin**:
```diff
-val options = Map3DOptions(
-    centerLat = 21.350,
-    centerLng = -157.800,
-    centerAlt = 0.0,
-    tilt = 60.0,
-    range = 25000.0
-)
+val config = Map3DInitConfig.create().copy(
+    centerLat = 21.350,
+    centerLng = -157.800,
+    centerAlt = 0.0,
+    tilt = 60.0,
+    range = 25000.0
+)
```

**Java**:
```diff
-Map3DOptions options = new Map3DOptions();
-options.setCenterLat(21.350);
-options.setCenterLng(-157.800);
+Map3DInitConfig config = Map3DInitConfig.create(
+    21.350,   // centerLat
+    -157.800, // centerLng
+    0.0,      // centerAlt
+    0.0,      // heading
+    60.0,     // tilt
+    0.0,      // roll
+    25000.0   // range
+);
```

### 3. Update `Map3DView` Construction

The default `Map3DView(Context)` constructor has been removed. You must now pass a `Map3DInitConfig` or a `mapMode`.

#### Code Transformation:

**Kotlin**:
```diff
-val view = Map3DView(context)
+val view = Map3DView(context, Map3DInitConfig.create())
```

**Java**:
```diff
-Map3DView view = new Map3DView(context);
+Map3DView view = new Map3DView(context, Map3DInitConfig.create());
```

### 4. Update Kotlin Named Parameters
If you construct `Map3DView` using named parameters, change `options` to `config`.

#### Code Transformation:
```diff
-val view = Map3DView(context = context, options = myConfig)
+val view = Map3DView(context = context, config = myConfig)
```

### 5. Remove `FacingMode`
The `FacingMode` enum has been removed. Remove all references to `FacingMode.SCREEN` or `FacingMode.BILLBOARD`.
