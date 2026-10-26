# Common Operations Catalog (Kotlin + Views)

This catalog provides verified, region-tagged reference snippets for common operations when using the Google Maps 3D SDK (`16.0.1+`) with Kotlin and Android Views.

## 1. Scene Readiness & Steady State Listeners
Description: Listening for when the 3D scene is 100% loaded (`setOnMapReadyListener`) and when the camera stops moving and rendering settles (`setOnMapSteadyListener`).

```kotlin
// [START maps_android_3d_init_listeners_kt]
val mainHandler = Handler(Looper.getMainLooper())

map.setOnMapReadyListener { sceneReadiness ->
    if (sceneReadiness == 1.0) {
        // Scene is fully loaded
        mainHandler.post {
            Toast.makeText(context, "Map Scene Ready (100%)", Toast.LENGTH_SHORT).show()
        }
    }
}

map.setOnMapSteadyListener { isSceneSteady ->
    if (isSceneSteady) {
        // Camera is not moving and data is loaded
        mainHandler.post {
            Toast.makeText(context, "Map Scene Steady", Toast.LENGTH_SHORT).show()
        }
    }
}
// [END maps_android_3d_init_listeners_kt]
```

## 2. Adding Markers (Basic, Advanced & Custom Glyphs)
Description: Markers point out points of interest using `markerOptions { ... }`. You can set `position`, `altitudeMode`, `label`, `collisionBehavior`, `isExtruded`, `isDrawnWhenOccluded`, and custom `PinConfiguration` / `Glyph` styles.

### Basic Marker
```kotlin
// [START maps_android_3d_marker_add_kt]
val position = latLngAltitude {
    latitude = 37.4220
    longitude = -122.0841
    altitude = 10.0
}

val options = markerOptions {
    this.position = position
    label = "Basic Marker"
    // MarkerOptions uses label, not title.
}

map.addMarker(options)
// [END maps_android_3d_marker_add_kt]
```

### Advanced Priority & Extruded Marker
```kotlin
// [START maps_android_3d_marker_options_kt]
val options = markerOptions {
    position = latLngAltitude {
        latitude = 37.4220
        longitude = -122.0841
        altitude = 10.0
    }
    altitudeMode = AltitudeMode.RELATIVE_TO_GROUND
    label = "Priority Marker"
    collisionBehavior = CollisionBehavior.OPTIONAL_AND_HIDES_LOWER_PRIORITY
    isExtruded = true
    isDrawnWhenOccluded = true
}

map.addMarker(options)
// [END maps_android_3d_marker_options_kt]
```

### Custom Icon & Styled Glyphs (`PinConfiguration` & `Glyph`)
```kotlin
// [START maps_android_3d_marker_custom_icon_kt]
// Create a Glyph with a custom image
val glyphImage = Glyph.fromColor(Color.YELLOW)
// Use the Maps SDK's ImageView, not the Android widget
glyphImage.setImage(ImageView(R.mipmap.ic_launcher))

val options = markerOptions {
    position = latLngAltitude {
        latitude = 37.4220
        longitude = -122.0841
        altitude = 10.0
    }
    label = "Custom Icon Marker"
    // Set the style using PinConfiguration
    setStyle(
        PinConfiguration.builder()
            .setScale(1.5f)
            .setGlyph(glyphImage)
            .setBackgroundColor(Color.BLUE)
            .setBorderColor(Color.WHITE)
            .build(),
    )
}

map.addMarker(options)
// [END maps_android_3d_marker_custom_icon_kt]
```

```kotlin
// [START maps_android_3d_marker_glyph_text_kt]
val options = markerOptions {
    position = latLngAltitude {
        latitude = 37.4220
        longitude = -122.0841
        altitude = 10.0
    }
    label = "Text Glyph"
    setStyle(
        PinConfiguration.builder()
            .setGlyph(Glyph.fromText("NYC"))
            .build(),
    )
}
map.addMarker(options)
// [END maps_android_3d_marker_glyph_text_kt]
```
> *Note*: You can also style glyphs with `Glyph.fromColor(Color.CYAN)` (`maps_android_3d_marker_glyph_color_kt`) or `Glyph.fromCircle().apply { color = Color.MAGENTA }` (`maps_android_3d_marker_glyph_circle_kt`); see [`MarkerSnippets.kt`](../../../../snippets/kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt).

### Object Click Listeners (`setClickListener`)
```kotlin
// [START maps_android_3d_marker_click_kt]
val marker = map.addMarker(
    markerOptions {
        position = latLngAltitude {
            latitude = 37.42
            longitude = -122.08
            altitude = 0.0
        }
    },
)

marker?.setClickListener {
    Handler(Looper.getMainLooper()).post {
        Toast.makeText(context, "Marker Clicked!", Toast.LENGTH_SHORT).show()
    }
}
// [END maps_android_3d_marker_click_kt]
```

> [!WARNING]
> Object click listeners (`marker.setClickListener`, `polyline.setClickListener`, `polygon.setClickListener`, `model.setClickListener`) do **NOT** execute on the UI thread. Always switch to the main thread (`Handler(Looper.getMainLooper()).post { ... }`) when updating views or showing a `Toast`.

## 3. Adding Polylines (Basic & Styled)
Description: Polylines draw 2D or 3D extruded paths on the map using `polylineOptions { ... }`.

### Basic Polyline
```kotlin
// [START maps_android_3d_polyline_add_kt]
val points = listOf(
    latLngAltitude {
        latitude = 37.42
        longitude = -122.08
        altitude = 0.0
    },
    latLngAltitude {
        latitude = 37.43
        longitude = -122.09
        altitude = 0.0
    },
    latLngAltitude {
        latitude = 37.44
        longitude = -122.08
        altitude = 0.0
    },
)

val options = polylineOptions {
    path = points
    strokeColor = Color.RED
    strokeWidth = 10.0
    altitudeMode = AltitudeMode.CLAMP_TO_GROUND
}

val polyline = map.addPolyline(options)
// [END maps_android_3d_polyline_add_kt]
```

### Extruded & Outlined Polyline
```kotlin
// [START maps_android_3d_polyline_options_kt]
val points = listOf(
    latLngAltitude {
        latitude = 37.42
        longitude = -122.08
        altitude = 50.0
    },
    latLngAltitude {
        latitude = 37.43
        longitude = -122.09
        altitude = 100.0
    },
)

val options = polylineOptions {
    path = points
    strokeColor = 0xFFFF00FF.toInt() // Magenta
    strokeWidth = 2.0
    outerColor = 0xFF00FF00.toInt() // Green
    outerWidth = 1.0
    altitudeMode = AltitudeMode.RELATIVE_TO_GROUND
    extruded = true
    geodesic = true
    drawsOccludedSegments = true
}

val polyline = map.addPolyline(options)
// [END maps_android_3d_polyline_options_kt]
```

## 4. Adding Polygons (Basic, Extruded & With Hole)
Description: Polygons represent 2D areas or 3D extruded volumes using `polygonOptions { ... }`.

### Basic Polygon
```kotlin
// [START maps_android_3d_polygon_add_kt]
val points = listOf(
    latLngAltitude {
        latitude = 37.42
        longitude = -122.08
        altitude = 0.0
    },
    latLngAltitude {
        latitude = 37.42
        longitude = -122.09
        altitude = 0.0
    },
    latLngAltitude {
        latitude = 37.43
        longitude = -122.09
        altitude = 0.0
    },
    latLngAltitude {
        latitude = 37.43
        longitude = -122.08
        altitude = 0.0
    },
    latLngAltitude {
        latitude = 37.42
        longitude = -122.08
        altitude = 0.0
    },
)

val options = polygonOptions {
    path = points
    fillColor = Color.RED
    strokeColor = Color.BLUE
    strokeWidth = 5.0
    altitudeMode = AltitudeMode.CLAMP_TO_GROUND
}

val polygon = map.addPolygon(options)
// [END maps_android_3d_polygon_add_kt]
```

### Extruded Polygon (3D Volume)
```kotlin
// [START maps_android_3d_polygon_extruded_kt]
val points = listOf(
    latLngAltitude {
        latitude = 37.42
        longitude = -122.08
        altitude = 50.0
    },
    latLngAltitude {
        latitude = 37.42
        longitude = -122.09
        altitude = 50.0
    },
    latLngAltitude {
        latitude = 37.43
        longitude = -122.09
        altitude = 50.0
    },
    latLngAltitude {
        latitude = 37.43
        longitude = -122.08
        altitude = 50.0
    },
    latLngAltitude {
        latitude = 37.42
        longitude = -122.08
        altitude = 50.0
    },
)

val options = polygonOptions {
    path = points
    fillColor = 0x88FF0000.toInt() // Semi-transparent red
    extruded = true
    geodesic = true
    altitudeMode = AltitudeMode.RELATIVE_TO_GROUND
}

val polygon = map.addPolygon(options)
// [END maps_android_3d_polygon_extruded_kt]
```

### Polygon with Interior Hole
```kotlin
// [START maps_android_3d_polygon_hole_kt]
val outerPoints = listOf(
    latLngAltitude {
        latitude = 37.422
        longitude = -122.084
        altitude = 0.0
    },
    latLngAltitude {
        latitude = 37.422
        longitude = -122.086
        altitude = 0.0
    },
    latLngAltitude {
        latitude = 37.424
        longitude = -122.086
        altitude = 0.0
    },
    latLngAltitude {
        latitude = 37.424
        longitude = -122.084
        altitude = 0.0
    },
    latLngAltitude {
        latitude = 37.422
        longitude = -122.084
        altitude = 0.0
    },
)

val innerPoints = listOf(
    latLngAltitude {
        latitude = 37.4225
        longitude = -122.0845
        altitude = 0.0
    },
    latLngAltitude {
        latitude = 37.4225
        longitude = -122.0855
        altitude = 0.0
    },
    latLngAltitude {
        latitude = 37.4235
        longitude = -122.0855
        altitude = 0.0
    },
    latLngAltitude {
        latitude = 37.4235
        longitude = -122.0845
        altitude = 0.0
    },
    latLngAltitude {
        latitude = 37.4225
        longitude = -122.0845
        altitude = 0.0
    },
)

val options = polygonOptions {
    path = outerPoints
    fillColor = Color.GREEN
    // Inner paths is optional, wrap inner vertices with Hole
    innerPaths = listOf(Hole(innerPoints))
    altitudeMode = AltitudeMode.CLAMP_TO_GROUND
}

val polygon = map.addPolygon(options)
// [END maps_android_3d_polygon_hole_kt]
```

## 5. Adding a 3D Model (GLB)
Description: Loading and placing glTF/GLB assets on the map using `modelOptions { ... }` (where `SAUCER_URL` is a `.glb` asset URL such as `"https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/UFO.glb"`).

```kotlin
// [START maps_android_3d_model_add_kt]
val position = latLngAltitude {
    latitude = 37.4220
    longitude = -122.0841
    altitude = 100.0
}

val options = modelOptions {
    this.position = position
    url = SAUCER_URL
    altitudeMode = AltitudeMode.RELATIVE_TO_MESH
    orientation = orientation {
        tilt = 90.0
        heading = 0.0
        roll = 0.0
    }
    scale = vector3D {
        x = 10.0
        y = 10.0
        z = 10.0
    }
}

val model = map.addModel(options)
// [END maps_android_3d_model_add_kt]
```

## 6. Adding a Popover (Info Window)
Description: 2D Android Views anchored to a marker or 3D location that automatically face the camera.

### Marker-Anchored Popover
```kotlin
// [START maps_android_3d_popover_add_kt]
// Create a marker first
val markerOptions = markerOptions {
    position = latLngAltitude {
        latitude = 37.422
        longitude = -122.084
        altitude = 0.0
    }
}
val marker = map.addMarker(markerOptions) ?: return

// Create a custom view for the popover
val textView = TextView(context).apply {
    text = context.getString(R.string.popover_hello)
    setPadding(16, 16, 16, 16)
    setBackgroundColor(Color.WHITE)
}

// Add popover anchored to the marker
val options = popoverOptions {
    content = textView
    positionAnchor = marker
    altitudeMode = AltitudeMode.RELATIVE_TO_GROUND
}

val popover = map.addPopover(options)

// You can show/hide it
popover?.show()
// [END maps_android_3d_popover_add_kt]
```

### Configured Popover (`autoCloseEnabled` / `autoPanEnabled`)
```kotlin
// [START maps_android_3d_popover_options_kt]
// Create a marker at an interesting location (e.g., Golden Gate Bridge)
val markerOptions = markerOptions {
    position = latLngAltitude {
        latitude = 37.8199
        longitude = -122.4783
        altitude = 0.0
    }
    label = "Golden Gate Bridge"
}
val marker = map.addMarker(markerOptions) ?: return

val textView = TextView(context).apply {
    text = context.getString(R.string.popover_info)
    setPadding(16, 16, 16, 16)
    setBackgroundColor(Color.WHITE)
}

val options = popoverOptions {
    content = textView
    positionAnchor = marker
    autoCloseEnabled = true // Close when clicking elsewhere
    autoPanEnabled = false // Do not pan to popover
}

val popover = map.addPopover(options)
popover?.show()
// [END maps_android_3d_popover_options_kt]
```

## 7. Camera Animation, Events & Restrictions
Description: Animating the camera (`flyCameraTo`, `flyCameraAround`, `stopCameraAnimation`), listening to camera movement (`setCameraChangedListener`), and restricting camera bounds (`setCameraRestriction`).

### Fly Camera To Position
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

### Orbit Camera (`flyCameraAround`)
```kotlin
// [START maps_android_3d_camera_fly_around_kt]
lifecycleScope.launch {
    val targetCamera = camera {
        center = latLngAltitude {
            latitude = 38.743502
            longitude = -109.499374
            altitude = 1467.0
        }
        tilt = 58.1
        heading = 349.6
        range = 138.2
        roll = 0.0
    }

    // Although not completely necessary, the experience will be usually be better by
    // waiting for the camera to steady before starting the flyCameraAround
    map.setCamera(targetCamera)

    // Orbit around the target using DSL
    val options = flyAroundOptions {
        center = targetCamera
        rounds = 2.0 // 2 full rotations
        durationInMillis = 6_000
    }

    map.setOnMapSteadyListener { isSceneSteady: Boolean ->
        if (isSceneSteady) {
            map.setOnMapSteadyListener(null) // Cleanup
            map.flyCameraAround(options)
        }
    }
}
// [END maps_android_3d_camera_fly_around_kt]
```

### Stop Camera Animation
```kotlin
// [START maps_android_3d_camera_stop_kt]
lifecycleScope.launch {
    val targetCamera = camera {
        center = latLngAltitude {
            latitude = 38.743502
            longitude = -109.499374
            altitude = 1467.0
        }
        tilt = 58.1
        heading = 349.6
        range = 138.2
        roll = 0.0
    }

    // 1. Start a perpetual flyAround animation so we have something to stop
    map.flyCameraAround(
        flyAroundOptions {
            center = targetCamera
            rounds = 10.0
            durationInMillis = 30_000
        },
    )

    lifecycleScope.launch {
        delay(2.seconds) // Let it fly for 2 seconds
        map.stopCameraAnimation() // 2. Stop it immediately
        Log.d("Maps3D", "Camera stopped")
    }
}
// [END maps_android_3d_camera_stop_kt]
```

### Listen to Camera Change Events
```kotlin
// [START maps_android_3d_camera_events_kt]
var lastLogTime = 0L
map.setCameraChangedListener { camera ->
    val currentTime = System.currentTimeMillis()
    if (currentTime - lastLogTime > 500) { // Limit to 1 log per 500ms
        lastLogTime = currentTime
        Log.d(
            "Maps3D",
            "Camera State: " +
                "Center: ${camera.center}, " +
                "Heading: ${camera.heading}, " +
                "Tilt: ${camera.tilt}, " +
                "Roll: ${camera.roll}, " +
                "Range: ${camera.range}",
        )
    }
}
// [END maps_android_3d_camera_events_kt]
```

### Listen to Steady State
```kotlin
// [START maps_android_3d_camera_steady_kt]
map.setOnMapSteadyListener { isSceneSteady: Boolean ->
    Log.d("Maps3D", "Map Is Steady: $isSceneSteady")
}
// [END maps_android_3d_camera_steady_kt]
```

### Camera Restrictions (`CameraRestriction` & `LatLngBounds`)
```kotlin
// [START maps_android_3d_camera_restriction_kt]
// Move the camera inside the restricted bounds so we focus on NYC
map.setCamera(
    camera {
        center = latLngAltitude {
            latitude = 40.748233
            longitude = -73.985663
            altitude = 1500.0
        }
    },
)

val nycBounds = latLngBounds {
    northEastLat = 40.856492
    northEastLng = -73.802409
    southWestLat = 40.685630
    southWestLng = -74.050304
}

val restriction = cameraRestriction {
    minAltitude = 500.0
    maxAltitude = 10000.0
    bounds = nycBounds
}

map.setCameraRestriction(restriction)
// [END maps_android_3d_camera_restriction_kt]
```

## 8. Handling Map & Place Click Events (`Map3DClickEvent`)
Description: In SDK `16.0.1+`, `setMap3DClickListener` receives a `Map3DClickEvent` (`event.location` and `event.placeId`) and returns a `Boolean` (`true` if consumed, `false` otherwise).

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

## 9. Map Localization (`LocaleOptions` & `setLocale`)
Description: Dynamically localizing 3D map labels to a specific language and region or resetting to the device default locale.

```kotlin
// [START maps_android_3d_localization_chinese_kt]
val localeOptions = LocaleOptions().apply {
    language = "zh"
    region = "CN"
}
map.setLocale(localeOptions)
// [END maps_android_3d_localization_chinese_kt]
```

```kotlin
// [START maps_android_3d_localization_default_kt]
val defaultLocale = Locale.getDefault()
val defaultLocaleOptions = LocaleOptions().apply {
    language = defaultLocale.language
    region = defaultLocale.country
}
map.setLocale(defaultLocaleOptions)
// [END maps_android_3d_localization_default_kt]
```

## 10. Built-in 3D UI Controls (`UiControlSettings`)
Description: Configuring or hiding the built-in 3D camera UI controls (`ZoomControl`, `TiltControl`, `RotateControl`, `PanVerticalControl`, `PanHorizontalControl`, `CompassControl`, `Spacer`) across screen edges (`TRAILING`, `LEADING`, `TOP`, `BOTTOM`).

### Configure Trailing & Leading Controls
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

### Horizontal Controls with Spacers on Top & Bottom Edges
```kotlin
// [START maps_android_3d_ui_controls_orientation_kt]
map.getUiControlSettings()?.let { uiSettings ->
    val topControls = listOf(
        CompassControl(),
        Spacer(),
        RotateControl(ControlOrientation.HORIZONTAL),
        TiltControl(ControlOrientation.HORIZONTAL),
    )
    uiSettings.setUiControlsPlacement(topControls, ControlPlacement.TOP)

    val bottomControls = listOf(
        ZoomControl(ControlOrientation.HORIZONTAL),
        Spacer(),
        PanHorizontalControl(ControlOrientation.HORIZONTAL),
        PanVerticalControl(ControlOrientation.HORIZONTAL),
    )
    uiSettings.setUiControlsPlacement(bottomControls, ControlPlacement.BOTTOM)
}
// [END maps_android_3d_ui_controls_orientation_kt]
```

### Hide All UI Controls
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
