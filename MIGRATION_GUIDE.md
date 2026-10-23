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

## Migrating from 0.2.2 to 16.0.1

This release graduates the SDK version to `16.0.1` (superseding `16.0.0` to resolve a JDK 21+ `javac` bytecode parameter annotation issue on `Map3DControl` subclasses and to restore Jetpack Compose `$stable` fields on `GoogleMap3D`, `Map3DView`, and `Popover`) and updates the click listener API, XML attribute naming, and theme attributes.

### 1. Dependency Update
Update the SDK dependency version in your `build.gradle` or `gradle/libs.versions.toml`:

*   **Action**: Update `playServicesMaps3d` version from `0.2.2` (or `16.0.0`) to `16.0.1`.

### 2. Update `OnMap3DClickListener` Signature (`Map3DClickEvent` & `Boolean` Return)

`OnMap3DClickListener.onMap3DClick` now receives a single `com.google.android.gms.maps3d.model.Map3DClickEvent` object (providing `event.location` and `event.placeId`) and returns a `Boolean` indicating whether the click event was consumed (`true`) or should propagate (`false`).

#### Code Transformation:

**Kotlin**:
```diff
-googleMap3D.setMap3DClickListener { location, placeId ->
-    if (placeId != null) {
-        handlePlaceClick(placeId)
-    } else {
-        handleLocationClick(location)
-    }
-}
+googleMap3D.setMap3DClickListener { event ->
+    val placeId = event.placeId
+    if (placeId != null) {
+        handlePlaceClick(placeId)
+        return@setMap3DClickListener true
+    } else {
+        handleLocationClick(event.location)
+        return@setMap3DClickListener true
+    }
+}
```

**Java**:
```diff
-googleMap3D.setMap3DClickListener((location, placeId) -> {
-    if (placeId != null) {
-        handlePlaceClick(placeId);
-    } else {
-        handleLocationClick(location);
-    }
-});
+googleMap3D.setMap3DClickListener(event -> {
+    if (event.getPlaceId() != null) {
+        handlePlaceClick(event.getPlaceId());
+        return true;
+    } else {
+        handleLocationClick(event.getLocation());
+        return true;
+    }
+});
```

If implementing `OnMap3DClickListener` explicitly as an anonymous class or interface:
```diff
-import androidx.annotation.Nullable;
 import com.google.android.gms.maps3d.OnMap3DClickListener;
-import com.google.android.gms.maps3d.model.LatLngAltitude;
+import com.google.android.gms.maps3d.model.Map3DClickEvent;

 map.setMap3DClickListener(new OnMap3DClickListener() {
     @Override
-    public void onMap3DClick(@NonNull LatLngAltitude location, @Nullable String placeId) {
+    public boolean onMap3DClick(@NonNull Map3DClickEvent event) {
         ...
+        return true;
     }
 });
```

### 3. Update XML Attribute `map3d:mode` to `map3d:mapMode`

In XML layout files declaring `com.google.android.gms.maps3d.Map3DView`, replace `map3d:mode` with `map3d:mapMode` (valid values: `"hybrid"`, `"satellite"`, or `"roadmap"`).

> [!IMPORTANT]
> A valid `mapMode` is strictly required at initialization. If an XML layout still uses the old `map3d:mode` attribute or omits `map3d:mapMode`, `Map3DView` will fail to load and invoke `OnMap3DViewReadyCallback.onError` with `IllegalStateException: Unable to load map. Please specify a valid map mode.`.

```diff
 <com.google.android.gms.maps3d.Map3DView
     android:id="@+id/map3dView"
-    map3d:mode="hybrid"
+    map3d:mapMode="hybrid"
     ... />
```

### 4. Update Custom Theme Attribute `maps3dLoadingTintColor` to `maps3dTintColor`

If your app customizes the Maps 3D theme attributes in XML (`styles.xml` / `themes.xml`), replace `maps3dLoadingTintColor` with `maps3dTintColor`:

```diff
 <style name="CustomMaps3DTheme" parent="Theme.Maps3D.Dark">
-    <item name="maps3dLoadingTintColor">@color/my_tint</item>
+    <item name="maps3dTintColor">@color/my_tint</item>
 </style>
```

### 5. `maps3d-compose` `GoogleMap3D` Click Handling Behavior Change

In the `GoogleMap3D` composable (`maps3d-compose`), tapping a place on the map now invokes `onPlaceClick` if provided, and **falls back to `onMapClick(event.location)` when `onPlaceClick` is `null`** (returning `true` when either callback handles the tap, and `false` when neither callback is set). Previously, place clicks were ignored when `onPlaceClick` was not set even if `onMapClick` was provided.

---

## Migrating from 0.2.0 to 0.2.2

This release introduces breaking changes to the initialization configuration and view construction.

### 1. Dependency Update
Update the SDK dependency version in your `build.gradle` or `gradle/libs.versions.toml`.

*   **Action**: Update `playServicesMaps3d` version to `0.2.2` (or directly to `16.0.1`).

### 2. Replace `Map3DOptions` with `Map3DInitConfig`

The `Map3DOptions` class has been removed. Use `Map3DInitConfig` instead.
*   **Rule**: Construct `Map3DInitConfig` using the static `Map3DInitConfig.create(...)` factory method (and optionally `.copy(...)` in Kotlin). You **must** specify a valid `mapMode` (`Map3DMode.HYBRID`, `Map3DMode.SATELLITE`, or `Map3DMode.ROADMAP`), otherwise `Map3DView` will fail to load with `IllegalStateException: Unable to load map. Please specify a valid map mode.`.

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
+val config = Map3DInitConfig.create(
+    centerLat = 21.350,
+    centerLng = -157.800,
+    centerAlt = 0.0,
+    tilt = 60.0,
+    range = 25000.0,
+    mapMode = Map3DMode.HYBRID
+)
```

**Java**:
```diff
-Map3DOptions options = new Map3DOptions();
-options.setCenterLat(21.350);
-options.setCenterLng(-157.800);
+Map3DInitConfig config = Map3DInitConfig.create(
+    21.350,           // centerLat
+    -157.800,         // centerLng
+    0.0,              // centerAlt
+    0.0,              // heading
+    60.0,             // tilt
+    0.0,              // roll
+    25000.0,          // range
+    0.0,              // minAltitude
+    63170000.0,       // maxAltitude
+    0.0,              // minHeading
+    360.0,            // maxHeading
+    -90.0,            // minTilt
+    90.0,             // maxTilt
+    null,             // bounds
+    Map3DMode.HYBRID  // mapMode
+);
```

### 3. Update `Map3DView` Construction

The default `Map3DView(Context)` constructor has been removed. You must now pass either a `Map3DMode` (`Map3DMode.HYBRID`, `Map3DMode.SATELLITE`, `Map3DMode.ROADMAP`) or a `Map3DInitConfig` with a valid `mapMode`.

#### Code Transformation:

**Kotlin**:
```diff
-val view = Map3DView(context)
+val view = Map3DView(context, Map3DMode.HYBRID)
+// Or with a custom Map3DInitConfig:
+val view = Map3DView(context, Map3DInitConfig.create(mapMode = Map3DMode.HYBRID))
```

**Java**:
```diff
-Map3DView view = new Map3DView(context);
+Map3DView view = new Map3DView(context, Map3DMode.HYBRID);
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
