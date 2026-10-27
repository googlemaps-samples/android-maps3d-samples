# Common Operations Catalog (Jetpack Compose)

This catalog provides verified reference snippets for using the Google Maps 3D SDK with Jetpack Compose—both via raw `AndroidView` interoperability and via the declarative `maps3d-compose` (`GoogleMap3D`) library used throughout `Maps3DSamples/ComposeDemos`.

## 1. Basic `AndroidView` + `Map3DView` Bridge
Description: Bridging `Map3DView` directly into Jetpack Compose using `AndroidView`, passing a required `Map3DMode` (`Map3DMode.HYBRID`, `Map3DMode.SATELLITE`, or `Map3DMode.ROADMAP`), and awaiting `getMap3DViewAsync`.

```kotlin
// [START maps_android_3d_init_basic_kt]
@Composable
fun BasicMap3D() {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            Map3DView(context, Map3DMode.HYBRID).apply {
                getMap3DViewAsync(object : OnMap3DViewReadyCallback {
                    override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
                        val camera = camera {
                            center = latLngAltitude {
                                latitude = 40.0150
                                longitude = -105.2705
                                altitude = 5000.0
                            }
                            heading = 0.0
                            tilt = 45.0
                            roll = 0.0
                            range = 10000.0
                        }
                        googleMap3D.setCamera(camera)
                    }
                    override fun onError(error: Exception) {
                        // Log exception trace guiding developers to key deficits or mismatches
                        Log.e("MapInitSnippets", "Failed to initialize 3D Map: ${error.message}", error)
                    }
                })
            }
        },
    )
}
// [END maps_android_3d_init_basic_kt]
```

> [!IMPORTANT]
> When embedding `Map3DView` in `AndroidView` in production apps, always forward `ON_RESUME` / `ON_PAUSE` via a `LifecycleEventObserver` inside a `DisposableEffect` and call `map3dView.onDestroy()` inside `AndroidView`'s `onRelease` block, or use the `GoogleMap3D` composable from `maps3d-compose` (`Maps3DSamples/ComposeDemos`), which handles lifecycle, readiness, and state synchronization automatically.

## 2. Declarative `GoogleMap3D` Composable (`maps3d-compose`)
Description: In `Maps3DSamples/ComposeDemos` and projects using `maps3d-compose`, `com.google.maps.android.compose3d.GoogleMap3D` manages `Map3DView` lifecycle, `Map3DInitConfig`, camera synchronization, click events (`onMapClick` and `onPlaceClick`), and declarative state lists (`markers`, `polylines`, `polygons`, `models`, `popovers`, `cameraRestriction`).

### Signature & Parameters (`maps3d-compose`)
*   `camera: Camera`: Hoisted camera state (`camera { ... }`).
*   `mapMode: Int`: `Map3DMode.HYBRID`, `Map3DMode.SATELLITE`, or `Map3DMode.ROADMAP`.
*   `options: Map3DInitConfig`: Created via `Map3DInitConfig.create(..., mapMode = mapMode, mapId = ...)`.
*   `markers: List<MarkerConfig>`, `polylines: List<PolylineConfig>`, `polygons: List<PolygonConfig>`, `models: List<ModelConfig>`, `popovers: List<PopoverConfig>`.
*   `cameraRestriction: CameraRestriction?`: Bounds and altitude limits.
*   `onMapReady: (GoogleMap3D) -> Unit`: Exposes the underlying `GoogleMap3D` instance for imperative operations (`flyCameraTo`, `flyCameraAround`, `stopCameraAnimation`, `setLocale`, `getUiControlSettings()`).
*   `onMapSteady: () -> Unit` & `onMapSteadyChange: (Boolean) -> Unit`: Scene steady callbacks.
*   `onMapClick: ((LatLngAltitude) -> Unit)?` & `onPlaceClick: ((String) -> Unit)?`: Map and POI click callbacks (note: tapping a place falls back to `onMapClick(event.location)` when `onPlaceClick` is `null`).

## 3. Imperative Operations Inside Compose (`onMapReady` / `LaunchedEffect`)
Description: Once you obtain the `GoogleMap3D` reference via `onMapReady` (or `getMap3DViewAsync`), use the exact SDK methods shown below inside `LaunchedEffect` or event callbacks:

### Camera Fly-To Animation (`flyCameraTo`)
```kotlin
// [START maps_android_3d_camera_fly_to_kt]
val targetCamera = camera {
    center = latLngAltitude {
        latitude = 38.743829
        longitude = -109.499512
        altitude = 1460.37
    }
    tilt = 76.16
    heading = 338.52
    range = 191.71
    roll = 0.0
}

val options = flyToOptions {
    endCamera = targetCamera
    durationInMillis = 5000
}

map.flyCameraTo(options)
// [END maps_android_3d_camera_fly_to_kt]
```

### Map & Place Click Handling (`setMap3DClickListener`)
```kotlin
// [START maps_android_3d_place_click_kt]
map.setMap3DClickListener { event ->
    val placeId = event.placeId
    if (placeId != null) {
        // Handle place click - Show a Toast on the UI thread
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, "Clicked Place ID: $placeId", Toast.LENGTH_SHORT).show()
        }
        return@setMap3DClickListener true
    }
    return@setMap3DClickListener false
}
// [END maps_android_3d_place_click_kt]
```

### Map Localization (`LocaleOptions` & `setLocale`)
```kotlin
// [START maps_android_3d_localization_chinese_kt]
val localeOptions = LocaleOptions().apply {
    language = "zh"
    region = "CN"
}
map.setLocale(localeOptions)
// [END maps_android_3d_localization_chinese_kt]
```

### Configuring or Hiding Built-in UI Controls (`UiControlSettings`)
```kotlin
// [START maps_android_3d_ui_controls_placement_kt]
map.getUiControlSettings()?.let { uiSettings ->
    val trailingControls = listOf(
        ZoomControl(ControlOrientation.VERTICAL),
        TiltControl(ControlOrientation.VERTICAL),
        RotateControl(ControlOrientation.VERTICAL),
    )
    uiSettings.setUiControlsPlacement(trailingControls, ControlPlacement.TRAILING)

    val leadingControls = listOf(
        PanVerticalControl(ControlOrientation.VERTICAL),
        PanHorizontalControl(ControlOrientation.VERTICAL),
        CompassControl(),
    )
    uiSettings.setUiControlsPlacement(leadingControls, ControlPlacement.LEADING)
}
// [END maps_android_3d_ui_controls_placement_kt]
```

```kotlin
// [START maps_android_3d_ui_controls_hide_kt]
map.getUiControlSettings()?.let { uiSettings ->
    uiSettings.setUiControlsPlacement(emptyList(), ControlPlacement.TRAILING)
    uiSettings.setUiControlsPlacement(emptyList(), ControlPlacement.LEADING)
    uiSettings.setUiControlsPlacement(emptyList(), ControlPlacement.TOP)
    uiSettings.setUiControlsPlacement(emptyList(), ControlPlacement.BOTTOM)
}
// [END maps_android_3d_ui_controls_hide_kt]
```
