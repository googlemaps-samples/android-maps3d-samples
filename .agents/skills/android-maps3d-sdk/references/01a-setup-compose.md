# 01a. Jetpack Compose Setup (`Map3DView`, `AndroidView` & `:maps3d-compose`)

## 1. Pattern A: Standalone Jetpack Compose Wrapper (Recommended for External Apps)

Google does not publish an external Maven artifact for 3D Maps Compose. Wrap `com.google.android.gms.maps3d.Map3DView` inside Jetpack Compose's standard `AndroidView` and forward lifecycle events with `LifecycleEventObserver`.

**Critical Compose Guardrails:**
- Always pass `Modifier.fillMaxSize()` (or explicit non-zero size) to `AndroidView` — a `0x0` layout prevents Maps 3D authentication from completing.
- Always pass an explicit `Map3DMode` (`Map3DMode.HYBRID`, `SATELLITE`, or `ROADMAP`) via `Map3DInitConfig.create(...)` or `Map3DView(context, Map3DMode.HYBRID)` (do NOT use the removed `0.1.0` `Map3DOptions`).
- **Never** call imperative methods (`addMarker`, `flyCameraTo`, `setCamera`) inside `AndroidView`'s `update = { ... }` lambda (which re-runs on every recomposition); run them inside `onMap3DViewReady` or `LaunchedEffect`.

```kotlin
package com.example.maps3d.ui

import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.Map3DInitConfig
import com.google.android.gms.maps3d.Map3DView
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.MapColorScheme
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import java.util.Locale

@Composable
fun Map3DScreen(
    modifier: Modifier = Modifier,
    initialCamera: Camera? = null,
    @Map3DMode mapMode: Int = Map3DMode.HYBRID,
    @MapColorScheme mapColorScheme: Int = MapColorScheme.FOLLOW_SYSTEM,
    mapId: String? = null,
    language: String = Locale.getDefault().language,
    region: String = Locale.getDefault().country,
    onMapReady: (GoogleMap3D) -> Unit = {},
    onError: (Exception) -> Unit = { Log.e("Map3DScreen", "Map3DView init failed", it) }
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val currentOnMapReady by rememberUpdatedState(onMapReady)
    val currentOnError by rememberUpdatedState(onError)

    val map3DView = remember {
        val config = Map3DInitConfig.create(
            centerLat = initialCamera?.center?.latitude ?: 37.7952,
            centerLng = initialCamera?.center?.longitude ?: -122.4028,
            centerAlt = initialCamera?.center?.altitude ?: 0.0,
            heading = initialCamera?.heading ?: 30.0,
            tilt = initialCamera?.tilt ?: 65.0,
            roll = initialCamera?.roll ?: 0.0,
            range = initialCamera?.range ?: 1800.0,
            mapMode = mapMode,
            mapId = mapId,
            language = language,
            region = region,
            mapColorScheme = mapColorScheme
        )
        Map3DView(context, config)
    }

    DisposableEffect(lifecycle, map3DView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> map3DView.onCreate(Bundle())
                Lifecycle.Event.ON_START -> map3DView.onStart()
                Lifecycle.Event.ON_RESUME -> map3DView.onResume()
                Lifecycle.Event.ON_PAUSE -> map3DView.onPause()
                Lifecycle.Event.ON_STOP -> map3DView.onStop()
                Lifecycle.Event.ON_DESTROY -> map3DView.onDestroy()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        map3DView.getMap3DViewAsync(object : OnMap3DViewReadyCallback {
            override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
                val target = initialCamera ?: camera {
                    center = latLngAltitude {
                        latitude = 37.7952
                        longitude = -122.4028
                        altitude = 0.0
                    }
                    heading = 30.0
                    tilt = 65.0
                    roll = 0.0
                    range = 1800.0
                    altitudeMode = AltitudeMode.RELATIVE_TO_GROUND
                }
                googleMap3D.setCamera(target)
                currentOnMapReady(googleMap3D)
            }

            override fun onError(error: Exception) {
                currentOnError(error)
            }
        })
        onDispose {
            lifecycle.removeObserver(observer)
            map3DView.onDestroy()
        }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { map3DView }
    )
}
```

---

## 2. Consumable Camera Commands & 60/120fps Tickers in Compose

```kotlin
// One-time fly-to command (cleared immediately to prevent recomposition loops)
val uiState by viewModel.uiState.collectAsStateWithLifecycle()
var map3D by remember { mutableStateOf<GoogleMap3D?>(null) }

val flyToCommand = uiState.flyToCommand
LaunchedEffect(map3D, flyToCommand) {
    val activeMap = map3D ?: return@LaunchedEffect
    if (flyToCommand != null) {
        activeMap.flyCameraTo(flyToCommand)
        viewModel.onFlyToCompleted()
    }
}

// Frame-synced ticker for path following / vehicle animation
LaunchedEffect(uiState.isPlaying, map3D) {
    if (!uiState.isPlaying || map3D == null) return@LaunchedEffect
    var lastFrameMs = withFrameMillis { it }
    while (isActive && uiState.isPlaying) {
        val nowMs = withFrameMillis { it }
        val deltaSeconds = ((nowMs - lastFrameMs).coerceAtLeast(0L)) / 1000.0
        lastFrameMs = nowMs
        viewModel.onFrameTick(deltaSeconds)
    }
}
```

---

## 3. Map Modes, Color Schemes, Localization & Built-In UI Controls (`16.0.1`)

```kotlin
import com.google.android.gms.maps3d.UiControlSettings.ControlOrientation
import com.google.android.gms.maps3d.UiControlSettings.ControlPlacement
import com.google.android.gms.maps3d.model.CompassControl
import com.google.android.gms.maps3d.model.LocaleOptions
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.MapColorScheme
import com.google.android.gms.maps3d.model.MapModeControl
import com.google.android.gms.maps3d.model.PanHorizontalControl
import com.google.android.gms.maps3d.model.PanVerticalControl
import com.google.android.gms.maps3d.model.RotateControl
import com.google.android.gms.maps3d.model.Spacer
import com.google.android.gms.maps3d.model.TiltControl
import com.google.android.gms.maps3d.model.ZoomControl

googleMap3D.setMapMode(Map3DMode.ROADMAP)           // HYBRID (1), SATELLITE (2), ROADMAP (3)
googleMap3D.setMapColorScheme(MapColorScheme.DARK)  // LIGHT (0), DARK (1), FOLLOW_SYSTEM (2)
googleMap3D.setLocale(LocaleOptions().apply { language = "ja"; region = "JP" })

googleMap3D.getUiControlSettings()?.apply {
    isMapModeControlEnabled = true
    setUiControlsPlacement(
        listOf(
            CompassControl(),
            Spacer(),
            ZoomControl(ControlOrientation.VERTICAL),
            TiltControl(ControlOrientation.VERTICAL),
            RotateControl(ControlOrientation.HORIZONTAL),
            PanVerticalControl(ControlOrientation.VERTICAL),
            PanHorizontalControl(ControlOrientation.HORIZONTAL),
            MapModeControl()
        ),
        ControlPlacement.TRAILING // LEADING (0), TRAILING (1), TOP (2), BOTTOM (3)
    )
}
```

---

## 4. Pattern B: In-Repo `:maps3d-compose` Wrapper (`android-maps3d-samples` Monorepo Only)

Only when `settings.gradle.kts` includes `:maps3d-compose`, use `com.google.maps.android.compose3d.GoogleMap3D`:

```kotlin
import com.google.maps.android.compose3d.GoogleMap3D

GoogleMap3D(
    modifier = Modifier.fillMaxSize(),
    initialCamera = initialCamera,
    mapMode = Map3DMode.HYBRID,
    mapColorScheme = MapColorScheme.FOLLOW_SYSTEM,
    uiControlsEnabled = false,
    markers = markersList,       // List<MarkerConfig> (supports PinConfig, GlyphConfig)
    polylines = polylinesList,   // List<PolylineConfig>
    polygons = polygonsList,     // List<PolygonConfig>
    models = modelsList,         // List<ModelConfig>
    popovers = popoversList,     // List<PopoverConfig>
    onMapReady = { googleMap3D -> /* store or configure map */ }
)
```

---

## 5. Compact SDK Signature Block (`play-services-maps3d:16.0.1`)

```kotlin
// Map3DView(context, attrs?) | Map3DView(context, attrs?, defStyleAttr) | Map3DView(context, config: Map3DInitConfig) | Map3DView(context, @Map3DMode mapMode: Int)
// Map3DInitConfig.create(centerLat: Double, centerLng: Double, centerAlt: Double, heading: Double, tilt: Double, roll: Double, range: Double, minAltitude: Double, maxAltitude: Double, minHeading: Double, maxHeading: Double, minTilt: Double, maxTilt: Double, bounds: LatLngBounds?, @Map3DMode mapMode: Int, mapId: String?, language: String, region: String, @MapColorScheme mapColorScheme: Int): Map3DInitConfig
// Map3DMode: HYBRID = 1, SATELLITE = 2, ROADMAP = 3
// MapColorScheme: LIGHT = 0, DARK = 1, FOLLOW_SYSTEM = 2
// UiControlSettings: isMapModeControlEnabled: Boolean; setUiControlsPlacement(controls: List<Map3DControl>, @ControlPlacement placement: Int)
// ControlOrientation: HORIZONTAL = 0, VERTICAL = 1 | ControlPlacement: LEADING = 0, TRAILING = 1, TOP = 2, BOTTOM = 3
// Map3DControl subclasses: CompassControl(), MapModeControl(), Spacer(), ZoomControl(orientation?), TiltControl(orientation?), RotateControl(orientation?), PanVerticalControl(orientation?), PanHorizontalControl(orientation?)
// LocaleOptions: language: String, region: String (instantiate via LocaleOptions().apply { ... })
```
