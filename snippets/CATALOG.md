# 🗺️ Maps3D API Snippets Catalog

This document serves as a comprehensive developer reference and mapping matrix for the 3D Maps SDK features.

## 📑 Snippet Concepts Index

This section maps high-level concepts (groups) to specific demonstration files and lines in both languages.

### Camera
> Snippets demonstrating dynamic camera orchestration and animations.

- **1. Fly To**:
  - *Description*: Animates the camera to a specific position with a tilt and heading over 5 seconds.
  - **Kotlin**: [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt:45](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L45) (Tag: `maps_android_3d_camera_fly_to_kt`)
  - **Java**: [java-app/src/main/java/com/example/snippets/java/snippets/CameraControlSnippets.java:48](java-app/src/main/java/com/example/snippets/java/snippets/CameraControlSnippets.java#L48) (Tag: `maps_android_3d_camera_fly_to_java`)
- **2. Fly Around**:
  - *Description*: Rotates the camera 360 degrees around a specific location over 10 seconds.
  - **Kotlin**: [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt:76](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L76) (Tag: `maps_android_3d_camera_fly_around_kt`)
  - **Java**: [java-app/src/main/java/com/example/snippets/java/snippets/CameraControlSnippets.java:68](java-app/src/main/java/com/example/snippets/java/snippets/CameraControlSnippets.java#L68) (Tag: `maps_android_3d_camera_fly_around_java`)
- **3. Stop Animation**:
  - *Description*: Stops any currently running camera animation immediately.
  - **Kotlin**: [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt:120](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L120) (Tag: `maps_android_3d_camera_stop_kt`)
  - **Java**: [java-app/src/main/java/com/example/snippets/java/snippets/CameraControlSnippets.java:99](java-app/src/main/java/com/example/snippets/java/snippets/CameraControlSnippets.java#L99) (Tag: `maps_android_3d_camera_stop_java`)
- **4. Listen Camera Events**:
  - *Description*: Logs camera change events to the console, printing the center coordinates as the camera moves.
  - **Kotlin**: [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt:171](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L171) (Tag: `maps_android_3d_camera_events_kt`)
  - **Java**: [java-app/src/main/java/com/example/snippets/java/snippets/CameraControlSnippets.java:121](java-app/src/main/java/com/example/snippets/java/snippets/CameraControlSnippets.java#L121) (Tag: `maps_android_3d_camera_events_java`)
- **5. Listen Steady State**:
  - *Description*: Logs to the console when the map finishes rendering or enters a steady state.
  - **Kotlin**: [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt:202](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L202) (Tag: `maps_android_3d_camera_steady_kt`)
  - **Java**: [java-app/src/main/java/com/example/snippets/java/snippets/CameraControlSnippets.java:152](java-app/src/main/java/com/example/snippets/java/snippets/CameraControlSnippets.java#L152) (Tag: `maps_android_3d_camera_steady_java`)

### Map Initialization
> Snippets demonstrating map lifecycle, listeners and readiness states.

- **1. Listen Events**:
  - **Kotlin**: [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MapInitSnippets.kt:87](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MapInitSnippets.kt#L87) (Tag: `maps_android_3d_init_listeners_kt`)
- **1. Basic Map3D Initialization**:
  - *Description*: Initializes a standard 3D Map View and sets an initial camera angle.
  - **Java**: [java-app/src/main/java/com/example/snippets/java/snippets/MapInitSnippets.java:38](java-app/src/main/java/com/example/snippets/java/snippets/MapInitSnippets.java#L38) (Tag: `maps_android_3d_init_basic_java`)
- **2. Add Map to AndroidView**:
  - *Description*: Shows how to add a Map3DView to an AndroidView which bridges to Jetpack Compose.
  - **Kotlin**: [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MapInitSnippets.kt:48](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MapInitSnippets.kt#L48) (Tag: `maps_android_3d_init_basic_kt`)
- **2. Listen Map Events**:
  - *Description*: Logs map events to the console, such as clicks or idle status.
  - **Java**: [java-app/src/main/java/com/example/snippets/java/snippets/MapInitSnippets.java:69](java-app/src/main/java/com/example/snippets/java/snippets/MapInitSnippets.java#L69) (Tag: `maps_android_3d_init_listeners_java`)

### Markers
> Snippets demonstrating standard, extruded, and custom styled markers.

- **1. Basic**:
  - *Description*: Adds a standard marker.
  - **Kotlin**: [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:48](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L48) (Tag: `maps_android_3d_marker_add_kt`)
  - **Java**: [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java:59](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L59) (Tag: `maps_android_3d_marker_add_java`)
- **2. Advanced**:
  - *Description*: Adds a 'Priority Marker' that is extruded and collides with other markers.
  - **Kotlin**: [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:89](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L89) (Tag: `maps_android_3d_marker_options_kt`)
  - **Java**: [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java:88](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L88) (Tag: `maps_android_3d_marker_options_java`)
- **3. Click**:
  - *Description*: Adds a marker that logs a message when clicked.
  - **Kotlin**: [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:131](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L131) (Tag: `maps_android_3d_marker_click_kt`)
  - **Java**: [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java:120](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L120) (Tag: `maps_android_3d_marker_click_java`)
- **4. Custom Icon**:
  - *Description*: Adds a marker with a custom icon using PinConfiguration and Glyph styling.
  - **Kotlin**: [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:165](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L165) (Tag: `maps_android_3d_marker_custom_icon_kt`)
  - **Java**: [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java:159](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L159) (Tag: `maps_android_3d_marker_custom_icon_java`)

### Models
- **1. Basic**:
  - *Description*: Loads a GLB model from a URL and places it clamped to the ground.
  - **Kotlin**: [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/ModelSnippets.kt:43](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/ModelSnippets.kt#L43) (Tag: `maps_android_3d_model_add_kt`)
  - **Java**: [java-app/src/main/java/com/example/snippets/java/snippets/ModelSnippets.java:49](java-app/src/main/java/com/example/snippets/java/snippets/ModelSnippets.java#L49) (Tag: `maps_android_3d_model_add_java`)

### Places
- **1. Listen Clicks**:
  - *Description*: Sets up a listener that logs the Place ID when a user clicks on a 3D building or POI.
  - **Kotlin**: [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PlaceSnippets.kt:39](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PlaceSnippets.kt#L39) (Tag: `maps_android_3d_place_click_kt`)
  - **Java**: [java-app/src/main/java/com/example/snippets/java/snippets/PlaceSnippets.java:51](java-app/src/main/java/com/example/snippets/java/snippets/PlaceSnippets.java#L51) (Tag: `maps_android_3d_place_click_java`)

### Polygons
> Snippets demonstrating 2D and 3D extruded polygon layers on the map.

- **1. Basic**:
  - *Description*: Draws a red polygon with a blue stroke around a small area
  - **Kotlin**: [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:39](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L39) (Tag: `maps_android_3d_polygon_add_kt`)
  - **Java**: [java-app/src/main/java/com/example/snippets/java/snippets/PolygonSnippets.java:48](java-app/src/main/java/com/example/snippets/java/snippets/PolygonSnippets.java#L48) (Tag: `maps_android_3d_polygon_add_java`)
- **2. Extruded**:
  - **Kotlin**: [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:83](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L83) (Tag: `maps_android_3d_polygon_extruded_kt`)
  - **Java**: [java-app/src/main/java/com/example/snippets/java/snippets/PolygonSnippets.java:81](java-app/src/main/java/com/example/snippets/java/snippets/PolygonSnippets.java#L81) (Tag: `maps_android_3d_polygon_extruded_java`)

### Polylines
> Snippets demonstrating 2D and 3D extruded polyline paths on the map.

- **1. Basic**:
  - *Description*: Draws a thick red polyline connecting three points
  - **Kotlin**: [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:39](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L39) (Tag: `maps_android_3d_polyline_add_kt`)
  - **Java**: [java-app/src/main/java/com/example/snippets/java/snippets/PolylineSnippets.java:48](java-app/src/main/java/com/example/snippets/java/snippets/PolylineSnippets.java#L48) (Tag: `maps_android_3d_polyline_add_java`)
- **2. Styled**:
  - **Kotlin**: [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:80](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L80) (Tag: `maps_android_3d_polyline_options_kt`)
  - **Java**: [java-app/src/main/java/com/example/snippets/java/snippets/PolylineSnippets.java:78](java-app/src/main/java/com/example/snippets/java/snippets/PolylineSnippets.java#L78) (Tag: `maps_android_3d_polyline_options_java`)

### Popovers
> Snippets demonstrating anchored and configured 3D Popover views.

- **1. Marker Anchor**:
  - *Description*: Adds a 'Hello Popover!' text bubble anchored to a marker
  - **Kotlin**: [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt:43](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt#L43) (Tag: `maps_android_3d_popover_add_kt`)
  - **Java**: [java-app/src/main/java/com/example/snippets/java/snippets/PopoverSnippets.java:53](java-app/src/main/java/com/example/snippets/java/snippets/PopoverSnippets.java#L53) (Tag: `maps_android_3d_popover_add_java`)
- **2. Configured**:
  - *Description*: Adds an 'Info' popover anchored to a marker with auto-close enabled and auto-pan disabled.
  - **Kotlin**: [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt:90](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt#L90) (Tag: `maps_android_3d_popover_options_kt`)
  - **Java**: [java-app/src/main/java/com/example/snippets/java/snippets/PopoverSnippets.java:89](java-app/src/main/java/com/example/snippets/java/snippets/PopoverSnippets.java#L89) (Tag: `maps_android_3d_popover_options_java`)


---

## 📊 Maps3D API Coverage Matrix

This matrix ensures that every critical feature in the 3D Maps SDK is actively demonstrated inside a snippet boundary (`// [START ...]`).

## Kotlin Snippets
### `Camera`
- `getCenter`:
  - [MapActivity.kt:58](MapActivity.kt#L58) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt:183](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L183) (Tag: `maps_android_3d_camera_events_kt`)
- `setCenter`:
  - [MapActivity.kt:191](MapActivity.kt#L191) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt:128](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L128) (Tag: `maps_android_3d_camera_stop_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt:52](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L52) (Tag: `maps_android_3d_camera_fly_to_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt:84](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L84) (Tag: `maps_android_3d_camera_fly_around_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MapInitSnippets.kt:61](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MapInitSnippets.kt#L61) (Tag: `maps_android_3d_init_basic_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:112](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L112) (Tag: `maps_android_3d_marker_options_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:143](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L143) (Tag: `maps_android_3d_marker_click_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:196](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L196) (Tag: `maps_android_3d_marker_custom_icon_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:70](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L70) (Tag: `maps_android_3d_marker_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/ModelSnippets.kt:73](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/ModelSnippets.kt#L73) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PlaceSnippets.kt:58](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PlaceSnippets.kt#L58) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:110](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L110) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:66](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L66) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:108](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L108) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:63](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L63) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt:122](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt#L122) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt:77](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt#L77) (Tag: `No Tag`)
- `setHeading`:
  - [MapActivity.kt:197](MapActivity.kt#L197) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt:134](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L134) (Tag: `maps_android_3d_camera_stop_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt:58](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L58) (Tag: `maps_android_3d_camera_fly_to_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt:90](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L90) (Tag: `maps_android_3d_camera_fly_around_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MapInitSnippets.kt:66](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MapInitSnippets.kt#L66) (Tag: `maps_android_3d_init_basic_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:118](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L118) (Tag: `maps_android_3d_marker_options_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:145](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L145) (Tag: `maps_android_3d_marker_click_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:202](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L202) (Tag: `maps_android_3d_marker_custom_icon_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:76](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L76) (Tag: `maps_android_3d_marker_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/ModelSnippets.kt:75](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/ModelSnippets.kt#L75) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PlaceSnippets.kt:60](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PlaceSnippets.kt#L60) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:116](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L116) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:72](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L72) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:114](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L114) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:69](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L69) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt:124](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt#L124) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt:79](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt#L79) (Tag: `No Tag`)
- `setRange`:
  - [MapActivity.kt:198](MapActivity.kt#L198) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt:135](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L135) (Tag: `maps_android_3d_camera_stop_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt:59](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L59) (Tag: `maps_android_3d_camera_fly_to_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt:91](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L91) (Tag: `maps_android_3d_camera_fly_around_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MapInitSnippets.kt:69](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MapInitSnippets.kt#L69) (Tag: `maps_android_3d_init_basic_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:119](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L119) (Tag: `maps_android_3d_marker_options_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:146](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L146) (Tag: `maps_android_3d_marker_click_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:203](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L203) (Tag: `maps_android_3d_marker_custom_icon_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:77](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L77) (Tag: `maps_android_3d_marker_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/ModelSnippets.kt:76](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/ModelSnippets.kt#L76) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PlaceSnippets.kt:61](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PlaceSnippets.kt#L61) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:117](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L117) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:73](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L73) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:115](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L115) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:70](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L70) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt:125](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt#L125) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt:80](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt#L80) (Tag: `No Tag`)
- `setRoll`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt:136](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L136) (Tag: `maps_android_3d_camera_stop_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt:60](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L60) (Tag: `maps_android_3d_camera_fly_to_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt:92](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L92) (Tag: `maps_android_3d_camera_fly_around_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MapInitSnippets.kt:68](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MapInitSnippets.kt#L68) (Tag: `maps_android_3d_init_basic_kt`)
- `setTilt`:
  - [MapActivity.kt:196](MapActivity.kt#L196) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt:133](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L133) (Tag: `maps_android_3d_camera_stop_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt:57](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L57) (Tag: `maps_android_3d_camera_fly_to_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt:89](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L89) (Tag: `maps_android_3d_camera_fly_around_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MapInitSnippets.kt:67](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MapInitSnippets.kt#L67) (Tag: `maps_android_3d_init_basic_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:117](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L117) (Tag: `maps_android_3d_marker_options_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:144](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L144) (Tag: `maps_android_3d_marker_click_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:201](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L201) (Tag: `maps_android_3d_marker_custom_icon_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:75](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L75) (Tag: `maps_android_3d_marker_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/ModelSnippets.kt:74](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/ModelSnippets.kt#L74) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PlaceSnippets.kt:59](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PlaceSnippets.kt#L59) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:115](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L115) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:71](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L71) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:113](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L113) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:68](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L68) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt:123](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt#L123) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt:78](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt#L78) (Tag: `No Tag`)
- `writeToParcel`: ❌ No coverage

### `CameraRestriction`
- `setBounds`: ❌ No coverage
- `setMaxAltitude`: ❌ No coverage
- `setMaxHeading`: ❌ No coverage
- `setMaxTilt`: ❌ No coverage
- `setMinAltitude`: ❌ No coverage
- `setMinHeading`: ❌ No coverage
- `setMinTilt`: ❌ No coverage
- `writeToParcel`: ❌ No coverage

### `FlyAroundOptions`
- `getCenter`: ❌ No coverage
- `getDurationInMillis`: ❌ No coverage
- `getRounds`: ❌ No coverage
- `setCenter`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt:101](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L101) (Tag: `maps_android_3d_camera_fly_around_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt:153](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L153) (Tag: `maps_android_3d_camera_stop_kt`)
- `setDurationInMillis`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt:103](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L103) (Tag: `maps_android_3d_camera_fly_around_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt:155](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L155) (Tag: `maps_android_3d_camera_stop_kt`)
- `setRounds`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt:102](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L102) (Tag: `maps_android_3d_camera_fly_around_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt:154](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L154) (Tag: `maps_android_3d_camera_stop_kt`)
- `writeToParcel`: ❌ No coverage

### `FlyToOptions`
- `getDurationInMillis`: ❌ No coverage
- `getEndCamera`: ❌ No coverage
- `setDurationInMillis`:
  - [MapActivity.kt:202](MapActivity.kt#L202) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt:142](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L142) (Tag: `maps_android_3d_camera_stop_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt:65](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L65) (Tag: `maps_android_3d_camera_fly_to_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:121](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L121) (Tag: `maps_android_3d_marker_options_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:148](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L148) (Tag: `maps_android_3d_marker_click_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:205](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L205) (Tag: `maps_android_3d_marker_custom_icon_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:79](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L79) (Tag: `maps_android_3d_marker_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/ModelSnippets.kt:78](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/ModelSnippets.kt#L78) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PlaceSnippets.kt:63](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PlaceSnippets.kt#L63) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:119](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L119) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:75](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L75) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:117](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L117) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:72](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L72) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt:127](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt#L127) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt:82](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt#L82) (Tag: `No Tag`)
- `setEndCamera`:
  - [MapActivity.kt:201](MapActivity.kt#L201) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt:141](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L141) (Tag: `maps_android_3d_camera_stop_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt:64](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L64) (Tag: `maps_android_3d_camera_fly_to_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:111](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L111) (Tag: `maps_android_3d_marker_options_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:142](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L142) (Tag: `maps_android_3d_marker_click_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:195](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L195) (Tag: `maps_android_3d_marker_custom_icon_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:69](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L69) (Tag: `maps_android_3d_marker_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/ModelSnippets.kt:72](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/ModelSnippets.kt#L72) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PlaceSnippets.kt:57](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PlaceSnippets.kt#L57) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:109](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L109) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:65](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L65) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:107](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L107) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:62](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L62) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt:121](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt#L121) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt:76](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt#L76) (Tag: `No Tag`)
- `writeToParcel`: ❌ No coverage

### `Glyph`
- `equals`: ❌ No coverage
- `fromCircle`: ❌ No coverage
- `fromColor`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:172](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L172) (Tag: `maps_android_3d_marker_custom_icon_kt`)
- `fromText`: ❌ No coverage
- `getColor`: ❌ No coverage
- `getImage`: ❌ No coverage
- `getText`: ❌ No coverage
- `hashCode`: ❌ No coverage
- `setColor`: ❌ No coverage
- `setImage`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:174](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L174) (Tag: `maps_android_3d_marker_custom_icon_kt`)
- `setText`: ❌ No coverage
- `writeToParcel`: ❌ No coverage

### `GoogleMap3D`
- `addMarker`:
  - [TrackedMap3D.kt:31](TrackedMap3D.kt#L31) (Tag: `No Tag`)
- `addModel`:
  - [TrackedMap3D.kt:49](TrackedMap3D.kt#L49) (Tag: `No Tag`)
- `addPolygon`:
  - [TrackedMap3D.kt:43](TrackedMap3D.kt#L43) (Tag: `No Tag`)
- `addPolyline`:
  - [TrackedMap3D.kt:37](TrackedMap3D.kt#L37) (Tag: `No Tag`)
- `addPopover`:
  - [TrackedMap3D.kt:55](TrackedMap3D.kt#L55) (Tag: `No Tag`)
- `flyCameraAround`:
  - [TrackedMap3D.kt:68](TrackedMap3D.kt#L68) (Tag: `No Tag`)
- `flyCameraTo`:
  - [MapActivity.kt:200](MapActivity.kt#L200) (Tag: `No Tag`)
  - [TrackedMap3D.kt:67](TrackedMap3D.kt#L67) (Tag: `No Tag`)
- `getCamera`:
  - [MapActivity.kt:57](MapActivity.kt#L57) (Tag: `No Tag`)
  - [TrackedMap3D.kt:65](TrackedMap3D.kt#L65) (Tag: `No Tag`)
- `getCameraRestriction`: ❌ No coverage
- `getMapMode`: ❌ No coverage
- `setCamera`:
  - [TrackedMap3D.kt:60](TrackedMap3D.kt#L60) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MapInitSnippets.kt:71](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MapInitSnippets.kt#L71) (Tag: `maps_android_3d_init_basic_kt`)
- `setCameraAnimationEndListener`:
  - [MapExtensions.kt:19](MapExtensions.kt#L19) (Tag: `No Tag`)
  - [MapExtensions.kt:20](MapExtensions.kt#L20) (Tag: `No Tag`)
  - [MapExtensions.kt:27](MapExtensions.kt#L27) (Tag: `No Tag`)
- `setCameraChangedListener`:
  - [TrackedMap3D.kt:61](TrackedMap3D.kt#L61) (Tag: `No Tag`)
- `setCameraRestriction`: ❌ No coverage
- `setMap3DClickListener`:
  - [TrackedMap3D.kt:62](TrackedMap3D.kt#L62) (Tag: `No Tag`)
- `setMapMode`: ❌ No coverage
- `setOnMapReadyListener`:
  - [MapActivity.kt:127](MapActivity.kt#L127) (Tag: `No Tag`)
  - [TrackedMap3D.kt:69](TrackedMap3D.kt#L69) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MapInitSnippets.kt:95](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MapInitSnippets.kt#L95) (Tag: `maps_android_3d_init_listeners_kt`)
- `setOnMapSteadyListener`:
  - [MapExtensions.kt:40](MapExtensions.kt#L40) (Tag: `No Tag`)
  - [MapExtensions.kt:42](MapExtensions.kt#L42) (Tag: `No Tag`)
  - [MapExtensions.kt:51](MapExtensions.kt#L51) (Tag: `No Tag`)
  - [TrackedMap3D.kt:70](TrackedMap3D.kt#L70) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MapInitSnippets.kt:104](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MapInitSnippets.kt#L104) (Tag: `maps_android_3d_init_listeners_kt`)
- `stopCameraAnimation`:
  - [TrackedMap3D.kt:66](TrackedMap3D.kt#L66) (Tag: `No Tag`)

### `Hole`
- `getVertices`: ❌ No coverage
- `setVertices`: ❌ No coverage
- `writeToParcel`: ❌ No coverage

### `ImageView`
- `equals`: ❌ No coverage
- `getResourceId`: ❌ No coverage
- `hashCode`: ❌ No coverage
- `setResourceId`: ❌ No coverage
- `writeToParcel`: ❌ No coverage

### `LatLngAltitude`
- `getAltitude`:
  - [MapActivity.kt:73](MapActivity.kt#L73) (Tag: `No Tag`)
- `getLatitude`:
  - [MapActivity.kt:71](MapActivity.kt#L71) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:113](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L113) (Tag: `maps_android_3d_marker_options_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:197](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L197) (Tag: `maps_android_3d_marker_custom_icon_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:71](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L71) (Tag: `maps_android_3d_marker_add_kt`)
- `getLongitude`:
  - [MapActivity.kt:72](MapActivity.kt#L72) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:114](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L114) (Tag: `maps_android_3d_marker_options_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:198](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L198) (Tag: `maps_android_3d_marker_custom_icon_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:72](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L72) (Tag: `maps_android_3d_marker_add_kt`)
- `getPosition`: ❌ No coverage
- `setAltitude`:
  - [MapActivity.kt:194](MapActivity.kt#L194) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt:131](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L131) (Tag: `maps_android_3d_camera_stop_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt:55](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L55) (Tag: `maps_android_3d_camera_fly_to_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt:87](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L87) (Tag: `maps_android_3d_camera_fly_around_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MapInitSnippets.kt:64](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MapInitSnippets.kt#L64) (Tag: `maps_android_3d_init_basic_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:115](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L115) (Tag: `maps_android_3d_marker_options_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:138](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L138) (Tag: `maps_android_3d_marker_click_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:143](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L143) (Tag: `maps_android_3d_marker_click_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:180](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L180) (Tag: `maps_android_3d_marker_custom_icon_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:199](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L199) (Tag: `maps_android_3d_marker_custom_icon_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:57](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L57) (Tag: `maps_android_3d_marker_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:73](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L73) (Tag: `maps_android_3d_marker_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:99](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L99) (Tag: `maps_android_3d_marker_options_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/ModelSnippets.kt:52](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/ModelSnippets.kt#L52) (Tag: `maps_android_3d_model_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PlaceSnippets.kt:58](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PlaceSnippets.kt#L58) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:113](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L113) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:46](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L46) (Tag: `maps_android_3d_polygon_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:47](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L47) (Tag: `maps_android_3d_polygon_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:48](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L48) (Tag: `maps_android_3d_polygon_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:49](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L49) (Tag: `maps_android_3d_polygon_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:50](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L50) (Tag: `maps_android_3d_polygon_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:69](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L69) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:90](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L90) (Tag: `maps_android_3d_polygon_extruded_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:91](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L91) (Tag: `maps_android_3d_polygon_extruded_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:92](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L92) (Tag: `maps_android_3d_polygon_extruded_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:93](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L93) (Tag: `maps_android_3d_polygon_extruded_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:94](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L94) (Tag: `maps_android_3d_polygon_extruded_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:111](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L111) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:46](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L46) (Tag: `maps_android_3d_polyline_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:47](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L47) (Tag: `maps_android_3d_polyline_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:48](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L48) (Tag: `maps_android_3d_polyline_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:66](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L66) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:87](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L87) (Tag: `maps_android_3d_polyline_options_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:88](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L88) (Tag: `maps_android_3d_polyline_options_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt:122](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt#L122) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt:51](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt#L51) (Tag: `maps_android_3d_popover_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt:77](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt#L77) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt:98](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt#L98) (Tag: `maps_android_3d_popover_options_kt`)
- `setLatitude`:
  - [MapActivity.kt:192](MapActivity.kt#L192) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt:129](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L129) (Tag: `maps_android_3d_camera_stop_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt:53](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L53) (Tag: `maps_android_3d_camera_fly_to_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt:85](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L85) (Tag: `maps_android_3d_camera_fly_around_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MapInitSnippets.kt:62](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MapInitSnippets.kt#L62) (Tag: `maps_android_3d_init_basic_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:113](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L113) (Tag: `maps_android_3d_marker_options_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:138](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L138) (Tag: `maps_android_3d_marker_click_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:143](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L143) (Tag: `maps_android_3d_marker_click_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:178](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L178) (Tag: `maps_android_3d_marker_custom_icon_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:197](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L197) (Tag: `maps_android_3d_marker_custom_icon_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:55](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L55) (Tag: `maps_android_3d_marker_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:71](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L71) (Tag: `maps_android_3d_marker_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:97](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L97) (Tag: `maps_android_3d_marker_options_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/ModelSnippets.kt:50](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/ModelSnippets.kt#L50) (Tag: `maps_android_3d_model_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PlaceSnippets.kt:58](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PlaceSnippets.kt#L58) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:111](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L111) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:46](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L46) (Tag: `maps_android_3d_polygon_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:47](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L47) (Tag: `maps_android_3d_polygon_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:48](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L48) (Tag: `maps_android_3d_polygon_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:49](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L49) (Tag: `maps_android_3d_polygon_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:50](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L50) (Tag: `maps_android_3d_polygon_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:67](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L67) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:90](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L90) (Tag: `maps_android_3d_polygon_extruded_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:91](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L91) (Tag: `maps_android_3d_polygon_extruded_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:92](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L92) (Tag: `maps_android_3d_polygon_extruded_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:93](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L93) (Tag: `maps_android_3d_polygon_extruded_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:94](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L94) (Tag: `maps_android_3d_polygon_extruded_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:109](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L109) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:46](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L46) (Tag: `maps_android_3d_polyline_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:47](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L47) (Tag: `maps_android_3d_polyline_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:48](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L48) (Tag: `maps_android_3d_polyline_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:64](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L64) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:87](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L87) (Tag: `maps_android_3d_polyline_options_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:88](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L88) (Tag: `maps_android_3d_polyline_options_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt:122](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt#L122) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt:51](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt#L51) (Tag: `maps_android_3d_popover_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt:77](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt#L77) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt:98](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt#L98) (Tag: `maps_android_3d_popover_options_kt`)
- `setLongitude`:
  - [MapActivity.kt:193](MapActivity.kt#L193) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt:130](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L130) (Tag: `maps_android_3d_camera_stop_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt:54](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L54) (Tag: `maps_android_3d_camera_fly_to_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt:86](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L86) (Tag: `maps_android_3d_camera_fly_around_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MapInitSnippets.kt:63](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MapInitSnippets.kt#L63) (Tag: `maps_android_3d_init_basic_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:114](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L114) (Tag: `maps_android_3d_marker_options_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:138](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L138) (Tag: `maps_android_3d_marker_click_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:143](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L143) (Tag: `maps_android_3d_marker_click_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:179](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L179) (Tag: `maps_android_3d_marker_custom_icon_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:198](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L198) (Tag: `maps_android_3d_marker_custom_icon_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:56](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L56) (Tag: `maps_android_3d_marker_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:72](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L72) (Tag: `maps_android_3d_marker_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:98](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L98) (Tag: `maps_android_3d_marker_options_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/ModelSnippets.kt:51](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/ModelSnippets.kt#L51) (Tag: `maps_android_3d_model_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PlaceSnippets.kt:58](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PlaceSnippets.kt#L58) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:112](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L112) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:46](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L46) (Tag: `maps_android_3d_polygon_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:47](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L47) (Tag: `maps_android_3d_polygon_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:48](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L48) (Tag: `maps_android_3d_polygon_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:49](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L49) (Tag: `maps_android_3d_polygon_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:50](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L50) (Tag: `maps_android_3d_polygon_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:68](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L68) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:90](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L90) (Tag: `maps_android_3d_polygon_extruded_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:91](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L91) (Tag: `maps_android_3d_polygon_extruded_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:92](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L92) (Tag: `maps_android_3d_polygon_extruded_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:93](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L93) (Tag: `maps_android_3d_polygon_extruded_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:94](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L94) (Tag: `maps_android_3d_polygon_extruded_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:110](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L110) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:46](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L46) (Tag: `maps_android_3d_polyline_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:47](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L47) (Tag: `maps_android_3d_polyline_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:48](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L48) (Tag: `maps_android_3d_polyline_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:65](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L65) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:87](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L87) (Tag: `maps_android_3d_polyline_options_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:88](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L88) (Tag: `maps_android_3d_polyline_options_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt:122](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt#L122) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt:51](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt#L51) (Tag: `maps_android_3d_popover_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt:77](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt#L77) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt:98](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt#L98) (Tag: `maps_android_3d_popover_options_kt`)
- `writeToParcel`: ❌ No coverage

### `LatLngBounds`
- `getNorthEastLat`: ❌ No coverage
- `getNorthEastLng`: ❌ No coverage
- `getSouthWestLat`: ❌ No coverage
- `getSouthWestLng`: ❌ No coverage
- `setNorthEastLat`: ❌ No coverage
- `setNorthEastLng`: ❌ No coverage
- `setSouthWestLat`: ❌ No coverage
- `setSouthWestLng`: ❌ No coverage
- `writeToParcel`: ❌ No coverage

### `Map3DOptions`
- `attributes`: ❌ No coverage
- `bounds`: ❌ No coverage
- `centerAlt`: ❌ No coverage
- `centerLat`: ❌ No coverage
- `centerLng`: ❌ No coverage
- `defaultUiDisabled`: ❌ No coverage
- `fromAttributeSet`: ❌ No coverage
- `getAttributes`: ❌ No coverage
- `getBounds`: ❌ No coverage
- `getCenterAlt`: ❌ No coverage
- `getCenterLat`: ❌ No coverage
- `getCenterLng`: ❌ No coverage
- `getDefaultUiDisabled`: ❌ No coverage
- `getHeading`: ❌ No coverage
- `getMapId`: ❌ No coverage
- `getMapMode`: ❌ No coverage
- `getMaxAltitude`: ❌ No coverage
- `getMaxHeading`: ❌ No coverage
- `getMaxTilt`: ❌ No coverage
- `getMinAltitude`: ❌ No coverage
- `getMinHeading`: ❌ No coverage
- `getMinTilt`: ❌ No coverage
- `getOptions`: ❌ No coverage
- `getRange`: ❌ No coverage
- `getRoll`: ❌ No coverage
- `getTilt`: ❌ No coverage
- `heading`: ❌ No coverage
- `mapId`: ❌ No coverage
- `mapMode`: ❌ No coverage
- `maxAltitude`: ❌ No coverage
- `maxHeading`: ❌ No coverage
- `maxTilt`: ❌ No coverage
- `minAltitude`: ❌ No coverage
- `minHeading`: ❌ No coverage
- `minTilt`: ❌ No coverage
- `options`: ❌ No coverage
- `range`: ❌ No coverage
- `roll`: ❌ No coverage
- `tilt`: ❌ No coverage

### `Map3DView`
- `getMap3DViewAsync`:
  - [MapActivity.kt:122](MapActivity.kt#L122) (Tag: `No Tag`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MapInitSnippets.kt:58](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MapInitSnippets.kt#L58) (Tag: `maps_android_3d_init_basic_kt`)
- `getOverlayView`: ❌ No coverage
- `getParams`: ❌ No coverage
- `overlayView`: ❌ No coverage
- `params`: ❌ No coverage
- `setParams`: ❌ No coverage

### `Marker`
- `getAltitudeMode`: ❌ No coverage
- `getCollisionBehavior`: ❌ No coverage
- `getDrawsWhenOccluded`: ❌ No coverage
- `getId`: ❌ No coverage
- `getLabel`: ❌ No coverage
- `getPosition`: ❌ No coverage
- `getSizePreserved`: ❌ No coverage
- `getUrl`: ❌ No coverage
- `getZIndex`: ❌ No coverage
- `isExtruded`: ❌ No coverage
- `onMarkerClick`: ❌ No coverage
- `remove`:
  - [SnippetRegistry.kt:60](SnippetRegistry.kt#L60) (Tag: `No Tag`)
- `setAltitudeMode`: ❌ No coverage
- `setClickListener`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:152](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L152) (Tag: `maps_android_3d_marker_click_kt`)
- `setCollisionBehavior`: ❌ No coverage
- `setDrawsWhenOccluded`: ❌ No coverage
- `setExtruded`: ❌ No coverage
- `setLabel`: ❌ No coverage
- `setPosition`: ❌ No coverage
- `setSizePreserved`: ❌ No coverage
- `setUrl`: ❌ No coverage
- `setZIndex`: ❌ No coverage

### `MarkerOptions`
- `getAltitudeMode`: ❌ No coverage
- `getCollisionBehavior`: ❌ No coverage
- `getId`: ❌ No coverage
- `getLabel`: ❌ No coverage
- `getPosition`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:113](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L113) (Tag: `maps_android_3d_marker_options_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:114](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L114) (Tag: `maps_android_3d_marker_options_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:197](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L197) (Tag: `maps_android_3d_marker_custom_icon_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:198](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L198) (Tag: `maps_android_3d_marker_custom_icon_kt`)
- `getZIndex`: ❌ No coverage
- `isDrawnWhenOccluded`: ❌ No coverage
- `isExtruded`: ❌ No coverage
- `isSizePreserved`: ❌ No coverage
- `setAltitudeMode`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:101](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L101) (Tag: `maps_android_3d_marker_options_kt`)
- `setCollisionBehavior`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:103](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L103) (Tag: `maps_android_3d_marker_options_kt`)
- `setDrawnWhenOccluded`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:105](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L105) (Tag: `maps_android_3d_marker_options_kt`)
- `setExtruded`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:104](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L104) (Tag: `maps_android_3d_marker_options_kt`)
- `setId`: ❌ No coverage
- `setLabel`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:102](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L102) (Tag: `maps_android_3d_marker_options_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:182](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L182) (Tag: `maps_android_3d_marker_custom_icon_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:62](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L62) (Tag: `maps_android_3d_marker_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt:99](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt#L99) (Tag: `maps_android_3d_popover_options_kt`)
- `setPosition`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:138](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L138) (Tag: `maps_android_3d_marker_click_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:177](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L177) (Tag: `maps_android_3d_marker_custom_icon_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:61](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L61) (Tag: `maps_android_3d_marker_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:96](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L96) (Tag: `maps_android_3d_marker_options_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt:51](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt#L51) (Tag: `maps_android_3d_popover_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt:98](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt#L98) (Tag: `maps_android_3d_popover_options_kt`)
- `setSizePreserved`: ❌ No coverage
- `setZIndex`: ❌ No coverage

### `MarkerOptions.that`
- `getStyle`: ❌ No coverage
- `setStyle`: ❌ No coverage
- `writeToParcel`: ❌ No coverage

### `MarkerStyle`
- `equals`: ❌ No coverage
- `getImageView`: ❌ No coverage
- `getPinConfiguration`: ❌ No coverage
- `hashCode`: ❌ No coverage
- `writeToParcel`: ❌ No coverage

### `Model`
- `getAltitudeMode`: ❌ No coverage
- `getId`: ❌ No coverage
- `getOrientation`: ❌ No coverage
- `getPosition`: ❌ No coverage
- `getScale`: ❌ No coverage
- `getUrl`: ❌ No coverage
- `onModelClick`: ❌ No coverage
- `remove`:
  - [SnippetRegistry.kt:63](SnippetRegistry.kt#L63) (Tag: `No Tag`)
- `setAltitudeMode`: ❌ No coverage
- `setClickListener`: ❌ No coverage
- `setOrientation`: ❌ No coverage
- `setPosition`: ❌ No coverage
- `setScale`: ❌ No coverage
- `setUrl`: ❌ No coverage

### `ModelOptions`
- `getAltitudeMode`: ❌ No coverage
- `getId`: ❌ No coverage
- `getOrientation`: ❌ No coverage
- `getPosition`: ❌ No coverage
- `getScale`: ❌ No coverage
- `getUrl`: ❌ No coverage
- `setAltitudeMode`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/ModelSnippets.kt:58](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/ModelSnippets.kt#L58) (Tag: `maps_android_3d_model_add_kt`)
- `setId`: ❌ No coverage
- `setOrientation`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/ModelSnippets.kt:59](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/ModelSnippets.kt#L59) (Tag: `maps_android_3d_model_add_kt`)
- `setPosition`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/ModelSnippets.kt:56](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/ModelSnippets.kt#L56) (Tag: `maps_android_3d_model_add_kt`)
- `setScale`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/ModelSnippets.kt:64](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/ModelSnippets.kt#L64) (Tag: `maps_android_3d_model_add_kt`)
- `setUrl`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/ModelSnippets.kt:57](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/ModelSnippets.kt#L57) (Tag: `maps_android_3d_model_add_kt`)
- `writeToParcel`: ❌ No coverage

### `Orientation`
- `getHeading`: ❌ No coverage
- `getRoll`: ❌ No coverage
- `getTilt`: ❌ No coverage
- `setHeading`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/ModelSnippets.kt:61](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/ModelSnippets.kt#L61) (Tag: `maps_android_3d_model_add_kt`)
- `setRoll`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/ModelSnippets.kt:62](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/ModelSnippets.kt#L62) (Tag: `maps_android_3d_model_add_kt`)
- `setTilt`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/ModelSnippets.kt:60](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/ModelSnippets.kt#L60) (Tag: `maps_android_3d_model_add_kt`)
- `writeToParcel`: ❌ No coverage

### `PinConfiguration`
- `builder`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:184](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L184) (Tag: `maps_android_3d_marker_custom_icon_kt`)
- `equals`: ❌ No coverage
- `getBackgroundColor`: ❌ No coverage
- `getBorderColor`: ❌ No coverage
- `getGlyph`: ❌ No coverage
- `getScale`: ❌ No coverage
- `hashCode`: ❌ No coverage
- `setBackgroundColor`: ❌ No coverage
- `setBorderColor`: ❌ No coverage
- `setGlyph`: ❌ No coverage
- `setScale`: ❌ No coverage
- `writeToParcel`: ❌ No coverage

### `PinConfiguration.Builder`
- `build`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:189](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L189) (Tag: `maps_android_3d_marker_custom_icon_kt`)
- `setBackgroundColor`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:187](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L187) (Tag: `maps_android_3d_marker_custom_icon_kt`)
- `setBorderColor`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:188](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L188) (Tag: `maps_android_3d_marker_custom_icon_kt`)
- `setGlyph`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:186](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L186) (Tag: `maps_android_3d_marker_custom_icon_kt`)
- `setScale`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:185](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L185) (Tag: `maps_android_3d_marker_custom_icon_kt`)

### `Polygon`
- `getAltitudeMode`: ❌ No coverage
- `getDrawsOccludedSegments`: ❌ No coverage
- `getExtruded`: ❌ No coverage
- `getFillColor`: ❌ No coverage
- `getGeodesic`: ❌ No coverage
- `getId`: ❌ No coverage
- `getInnerCoordinates`: ❌ No coverage
- `getInnerPaths`: ❌ No coverage
- `getOuterCoordinates`: ❌ No coverage
- `getPath`: ❌ No coverage
- `getStrokeColor`: ❌ No coverage
- `getStrokeWidth`: ❌ No coverage
- `getZIndex`: ❌ No coverage
- `onPolygonClick`: ❌ No coverage
- `remove`:
  - [SnippetRegistry.kt:62](SnippetRegistry.kt#L62) (Tag: `No Tag`)
- `setAltitudeMode`: ❌ No coverage
- `setClickListener`: ❌ No coverage
- `setDrawsOccludedSegments`: ❌ No coverage
- `setExtruded`: ❌ No coverage
- `setFillColor`: ❌ No coverage
- `setGeodesic`: ❌ No coverage
- `setInnerCoordinates`: ❌ No coverage
- `setInnerPaths`: ❌ No coverage
- `setOuterCoordinates`: ❌ No coverage
- `setPath`: ❌ No coverage
- `setStrokeColor`: ❌ No coverage
- `setStrokeWidth`: ❌ No coverage
- `setZIndex`: ❌ No coverage

### `PolygonOptions`
- `getAltitudeMode`: ❌ No coverage
- `getDrawsOccludedSegments`: ❌ No coverage
- `getExtruded`: ❌ No coverage
- `getFillColor`: ❌ No coverage
- `getGeodesic`: ❌ No coverage
- `getId`: ❌ No coverage
- `getInnerPaths`: ❌ No coverage
- `getPath`: ❌ No coverage
- `getStrokeColor`: ❌ No coverage
- `getStrokeWidth`: ❌ No coverage
- `getZIndex`: ❌ No coverage
- `setAltitudeMode`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:102](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L102) (Tag: `maps_android_3d_polygon_extruded_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:58](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L58) (Tag: `maps_android_3d_polygon_add_kt`)
- `setDrawsOccludedSegments`: ❌ No coverage
- `setExtruded`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:100](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L100) (Tag: `maps_android_3d_polygon_extruded_kt`)
- `setFillColor`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:55](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L55) (Tag: `maps_android_3d_polygon_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:99](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L99) (Tag: `maps_android_3d_polygon_extruded_kt`)
- `setGeodesic`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:101](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L101) (Tag: `maps_android_3d_polygon_extruded_kt`)
- `setId`: ❌ No coverage
- `setInnerPaths`: ❌ No coverage
- `setPath`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:54](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L54) (Tag: `maps_android_3d_polygon_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:98](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L98) (Tag: `maps_android_3d_polygon_extruded_kt`)
- `setStrokeColor`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:56](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L56) (Tag: `maps_android_3d_polygon_add_kt`)
- `setStrokeWidth`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt:57](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L57) (Tag: `maps_android_3d_polygon_add_kt`)
- `setZIndex`: ❌ No coverage
- `writeToParcel`: ❌ No coverage

### `Polyline`
- `getAltitudeMode`: ❌ No coverage
- `getCoordinates`: ❌ No coverage
- `getDrawsOccludedSegments`: ❌ No coverage
- `getGeodesic`: ❌ No coverage
- `getId`: ❌ No coverage
- `getOuterColor`: ❌ No coverage
- `getOuterWidth`: ❌ No coverage
- `getPath`: ❌ No coverage
- `getStrokeColor`: ❌ No coverage
- `getStrokeWidth`: ❌ No coverage
- `getZIndex`: ❌ No coverage
- `onPolylineClick`: ❌ No coverage
- `remove`:
  - [SnippetRegistry.kt:61](SnippetRegistry.kt#L61) (Tag: `No Tag`)
- `setAltitudeMode`: ❌ No coverage
- `setClickListener`: ❌ No coverage
- `setCoordinates`: ❌ No coverage
- `setDrawsOccludedSegments`: ❌ No coverage
- `setGeodesic`: ❌ No coverage
- `setOuterColor`: ❌ No coverage
- `setOuterWidth`: ❌ No coverage
- `setPath`: ❌ No coverage
- `setStrokeColor`: ❌ No coverage
- `setStrokeWidth`: ❌ No coverage
- `setZIndex`: ❌ No coverage

### `PolylineOptions`
- `getAltitudeMode`: ❌ No coverage
- `getDrawsOccludedSegments`: ❌ No coverage
- `getExtruded`: ❌ No coverage
- `getGeodesic`: ❌ No coverage
- `getId`: ❌ No coverage
- `getOuterColor`: ❌ No coverage
- `getOuterWidth`: ❌ No coverage
- `getPath`: ❌ No coverage
- `getStrokeColor`: ❌ No coverage
- `getStrokeWidth`: ❌ No coverage
- `getZIndex`: ❌ No coverage
- `setAltitudeMode`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:55](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L55) (Tag: `maps_android_3d_polyline_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:97](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L97) (Tag: `maps_android_3d_polyline_options_kt`)
- `setDrawsOccludedSegments`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:100](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L100) (Tag: `maps_android_3d_polyline_options_kt`)
- `setExtruded`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:98](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L98) (Tag: `maps_android_3d_polyline_options_kt`)
- `setGeodesic`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:99](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L99) (Tag: `maps_android_3d_polyline_options_kt`)
- `setId`: ❌ No coverage
- `setOuterColor`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:95](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L95) (Tag: `maps_android_3d_polyline_options_kt`)
- `setOuterWidth`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:96](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L96) (Tag: `maps_android_3d_polyline_options_kt`)
- `setPath`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:52](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L52) (Tag: `maps_android_3d_polyline_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:92](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L92) (Tag: `maps_android_3d_polyline_options_kt`)
- `setStrokeColor`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:53](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L53) (Tag: `maps_android_3d_polyline_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:93](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L93) (Tag: `maps_android_3d_polyline_options_kt`)
- `setStrokeWidth`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:54](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L54) (Tag: `maps_android_3d_polyline_add_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt:94](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L94) (Tag: `maps_android_3d_polyline_options_kt`)
- `setZIndex`: ❌ No coverage
- `writeToParcel`: ❌ No coverage

### `Popover`
- `altitudeMode`: ❌ No coverage
- `anchorId`: ❌ No coverage
- `anchorOffset`: ❌ No coverage
- `autoCloseEnabled`: ❌ No coverage
- `autoPanEnabled`: ❌ No coverage
- `autoPanFn`: ❌ No coverage
- `content`: ❌ No coverage
- `getAltitudeMode`: ❌ No coverage
- `getAnchorId`: ❌ No coverage
- `getAnchorOffset`: ❌ No coverage
- `getAutoCloseEnabled`: ❌ No coverage
- `getAutoPanEnabled`: ❌ No coverage
- `getAutoPanFn`: ❌ No coverage
- `getContent`: ❌ No coverage
- `getIsVisible`: ❌ No coverage
- `getPositionAnchor`: ❌ No coverage
- `getRemoveFn`: ❌ No coverage
- `hide`: ❌ No coverage
- `isVisible`: ❌ No coverage
- `positionAnchor`: ❌ No coverage
- `remove`:
  - [SnippetRegistry.kt:64](SnippetRegistry.kt#L64) (Tag: `No Tag`)
- `removeFn`: ❌ No coverage
- `show`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt:117](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt#L117) (Tag: `maps_android_3d_popover_options_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt:72](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt#L72) (Tag: `maps_android_3d_popover_add_kt`)
- `toggle`: ❌ No coverage

### `PopoverOptions`
- `getAltitudeMode`: ❌ No coverage
- `getAnchorOffset`: ❌ No coverage
- `getAutoCloseEnabled`: ❌ No coverage
- `getAutoPanEnabled`: ❌ No coverage
- `getContent`: ❌ No coverage
- `getPopoverStyle`: ❌ No coverage
- `getPositionAnchor`: ❌ No coverage
- `setAltitudeMode`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt:66](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt#L66) (Tag: `maps_android_3d_popover_add_kt`)
- `setAnchorOffset`: ❌ No coverage
- `setAutoCloseEnabled`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt:112](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt#L112) (Tag: `maps_android_3d_popover_options_kt`)
- `setAutoPanEnabled`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt:113](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt#L113) (Tag: `maps_android_3d_popover_options_kt`)
- `setContent`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt:110](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt#L110) (Tag: `maps_android_3d_popover_options_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt:64](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt#L64) (Tag: `maps_android_3d_popover_add_kt`)
- `setPopoverStyle`: ❌ No coverage
- `setPositionAnchor`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt:111](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt#L111) (Tag: `maps_android_3d_popover_options_kt`)
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt:65](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt#L65) (Tag: `maps_android_3d_popover_add_kt`)

### `PopoverShadow`
- `getColor`: ❌ No coverage
- `getOffsetX`: ❌ No coverage
- `getOffsetY`: ❌ No coverage
- `getRadius`: ❌ No coverage
- `setColor`: ❌ No coverage
- `setOffsetX`: ❌ No coverage
- `setOffsetY`: ❌ No coverage
- `setRadius`: ❌ No coverage

### `PopoverStyle`
- `getBackgroundColor`: ❌ No coverage
- `getBorderRadius`: ❌ No coverage
- `getPadding`: ❌ No coverage
- `getShadow`: ❌ No coverage
- `setBackgroundColor`: ❌ No coverage
- `setBorderRadius`: ❌ No coverage
- `setPadding`: ❌ No coverage
- `setShadow`: ❌ No coverage

### `Validators.for`
- `Anchorable`: ❌ No coverage
- `Camera`: ❌ No coverage
- `CameraRestriction`: ❌ No coverage
- `FlyAroundOptions`: ❌ No coverage
- `FlyToOptions`: ❌ No coverage
- `Hole`: ❌ No coverage
- `LatLngAltitude`: ❌ No coverage
- `LatLngBounds`: ❌ No coverage
- `Map3DOptions`: ❌ No coverage
- `MarkerOptions`: ❌ No coverage
- `ModelOptions`: ❌ No coverage
- `Orientation`: ❌ No coverage
- `PolygonOptions`: ❌ No coverage
- `PolylineOptions`: ❌ No coverage
- `PopoverOptions`: ❌ No coverage
- `Vector3D`: ❌ No coverage
- `requireNonNegative`: ❌ No coverage
- `validateAltitude`: ❌ No coverage
- `validateFinite`: ❌ No coverage
- `validateHeading`: ❌ No coverage
- `validateLatitude`: ❌ No coverage
- `validateLongitude`: ❌ No coverage
- `validateRange`: ❌ No coverage
- `validateRoll`: ❌ No coverage
- `validateTilt`: ❌ No coverage

### `Vector3D`
- `getX`: ❌ No coverage
- `getY`: ❌ No coverage
- `getZ`: ❌ No coverage
- `setX`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/ModelSnippets.kt:64](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/ModelSnippets.kt#L64) (Tag: `maps_android_3d_model_add_kt`)
- `setY`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/ModelSnippets.kt:64](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/ModelSnippets.kt#L64) (Tag: `maps_android_3d_model_add_kt`)
- `setZ`:
  - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/ModelSnippets.kt:64](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/ModelSnippets.kt#L64) (Tag: `maps_android_3d_model_add_kt`)
- `writeToParcel`: ❌ No coverage

### Missing Extracted API Coverage (Kotlin Snippets)
The following non-getter/setter APIs currently have `0` occurrences within this section:

- `Camera.writeToParcel`
- `CameraRestriction.writeToParcel`
- `FlyAroundOptions.writeToParcel`
- `FlyToOptions.writeToParcel`
- `Glyph.equals`
- `Glyph.fromCircle`
- `Glyph.fromText`
- `Glyph.hashCode`
- `Glyph.writeToParcel`
- `Hole.writeToParcel`
- `ImageView.equals`
- `ImageView.hashCode`
- `ImageView.writeToParcel`
- `LatLngAltitude.writeToParcel`
- `LatLngBounds.writeToParcel`
- `Map3DOptions.attributes`
- `Map3DOptions.bounds`
- `Map3DOptions.centerAlt`
- `Map3DOptions.centerLat`
- `Map3DOptions.centerLng`
- `Map3DOptions.defaultUiDisabled`
- `Map3DOptions.fromAttributeSet`
- `Map3DOptions.heading`
- `Map3DOptions.mapId`
- `Map3DOptions.mapMode`
- `Map3DOptions.maxAltitude`
- `Map3DOptions.maxHeading`
- `Map3DOptions.maxTilt`
- `Map3DOptions.minAltitude`
- `Map3DOptions.minHeading`
- `Map3DOptions.minTilt`
- `Map3DOptions.options`
- `Map3DOptions.range`
- `Map3DOptions.roll`
- `Map3DOptions.tilt`
- `Map3DView.overlayView`
- `Map3DView.params`
- `Marker.isExtruded`
- `Marker.onMarkerClick`
- `MarkerOptions.isDrawnWhenOccluded`
- `MarkerOptions.isExtruded`
- `MarkerOptions.isSizePreserved`
- `MarkerOptions.that.writeToParcel`
- `MarkerStyle.equals`
- `MarkerStyle.hashCode`
- `MarkerStyle.writeToParcel`
- `Model.onModelClick`
- `ModelOptions.writeToParcel`
- `Orientation.writeToParcel`
- `PinConfiguration.equals`
- `PinConfiguration.hashCode`
- `PinConfiguration.writeToParcel`
- `Polygon.onPolygonClick`
- `PolygonOptions.writeToParcel`
- `Polyline.onPolylineClick`
- `PolylineOptions.writeToParcel`
- `Popover.altitudeMode`
- `Popover.anchorId`
- `Popover.anchorOffset`
- `Popover.autoCloseEnabled`
- `Popover.autoPanEnabled`
- `Popover.autoPanFn`
- `Popover.content`
- `Popover.hide`
- `Popover.isVisible`
- `Popover.positionAnchor`
- `Popover.removeFn`
- `Popover.toggle`
- `Validators.for.Anchorable`
- `Validators.for.Camera`
- `Validators.for.CameraRestriction`
- `Validators.for.FlyAroundOptions`
- `Validators.for.FlyToOptions`
- `Validators.for.Hole`
- `Validators.for.LatLngAltitude`
- `Validators.for.LatLngBounds`
- `Validators.for.Map3DOptions`
- `Validators.for.MarkerOptions`
- `Validators.for.ModelOptions`
- `Validators.for.Orientation`
- `Validators.for.PolygonOptions`
- `Validators.for.PolylineOptions`
- `Validators.for.PopoverOptions`
- `Validators.for.Vector3D`
- `Validators.for.requireNonNegative`
- `Validators.for.validateAltitude`
- `Validators.for.validateFinite`
- `Validators.for.validateHeading`
- `Validators.for.validateLatitude`
- `Validators.for.validateLongitude`
- `Validators.for.validateRange`
- `Validators.for.validateRoll`
- `Validators.for.validateTilt`
- `Vector3D.writeToParcel`

## Java Snippets
### `Camera`
- `getCenter`:
  - [MapActivity.java:55](MapActivity.java#L55) (Tag: `No Tag`)
  - [java-app/src/main/java/com/example/snippets/java/snippets/CameraControlSnippets.java:133](java-app/src/main/java/com/example/snippets/java/snippets/CameraControlSnippets.java#L133) (Tag: `maps_android_3d_camera_events_java`)
- `setCenter`: ❌ No coverage
- `setHeading`: ❌ No coverage
- `setRange`: ❌ No coverage
- `setRoll`: ❌ No coverage
- `setTilt`: ❌ No coverage
- `writeToParcel`: ❌ No coverage

### `CameraRestriction`
- `setBounds`: ❌ No coverage
- `setMaxAltitude`: ❌ No coverage
- `setMaxHeading`: ❌ No coverage
- `setMaxTilt`: ❌ No coverage
- `setMinAltitude`: ❌ No coverage
- `setMinHeading`: ❌ No coverage
- `setMinTilt`: ❌ No coverage
- `writeToParcel`: ❌ No coverage

### `FlyAroundOptions`
- `getCenter`: ❌ No coverage
- `getDurationInMillis`: ❌ No coverage
- `getRounds`: ❌ No coverage
- `setCenter`: ❌ No coverage
- `setDurationInMillis`: ❌ No coverage
- `setRounds`: ❌ No coverage
- `writeToParcel`: ❌ No coverage

### `FlyToOptions`
- `getDurationInMillis`: ❌ No coverage
- `getEndCamera`: ❌ No coverage
- `setDurationInMillis`: ❌ No coverage
- `setEndCamera`: ❌ No coverage
- `writeToParcel`: ❌ No coverage

### `Glyph`
- `equals`: ❌ No coverage
- `fromCircle`: ❌ No coverage
- `fromColor`:
  - [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java:168](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L168) (Tag: `maps_android_3d_marker_custom_icon_java`)
- `fromText`: ❌ No coverage
- `getColor`: ❌ No coverage
- `getImage`: ❌ No coverage
- `getText`: ❌ No coverage
- `hashCode`: ❌ No coverage
- `setColor`: ❌ No coverage
- `setImage`:
  - [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java:171](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L171) (Tag: `maps_android_3d_marker_custom_icon_java`)
- `setText`: ❌ No coverage
- `writeToParcel`: ❌ No coverage

### `GoogleMap3D`
- `addMarker`:
  - [TrackedMap3D.java:53](TrackedMap3D.java#L53) (Tag: `No Tag`)
- `addModel`:
  - [TrackedMap3D.java:71](TrackedMap3D.java#L71) (Tag: `No Tag`)
- `addPolygon`:
  - [TrackedMap3D.java:65](TrackedMap3D.java#L65) (Tag: `No Tag`)
- `addPolyline`:
  - [TrackedMap3D.java:59](TrackedMap3D.java#L59) (Tag: `No Tag`)
- `addPopover`:
  - [TrackedMap3D.java:77](TrackedMap3D.java#L77) (Tag: `No Tag`)
- `flyCameraAround`:
  - [TrackedMap3D.java:85](TrackedMap3D.java#L85) (Tag: `No Tag`)
- `flyCameraTo`:
  - [TrackedMap3D.java:90](TrackedMap3D.java#L90) (Tag: `No Tag`)
- `getCamera`:
  - [MapActivity.java:53](MapActivity.java#L53) (Tag: `No Tag`)
  - [TrackedMap3D.java:88](TrackedMap3D.java#L88) (Tag: `No Tag`)
- `getCameraRestriction`: ❌ No coverage
- `getMapMode`: ❌ No coverage
- `setCamera`:
  - [TrackedMap3D.java:82](TrackedMap3D.java#L82) (Tag: `No Tag`)
  - [java-app/src/main/java/com/example/snippets/java/snippets/MapInitSnippets.java:54](java-app/src/main/java/com/example/snippets/java/snippets/MapInitSnippets.java#L54) (Tag: `maps_android_3d_init_basic_java`)
- `setCameraAnimationEndListener`: ❌ No coverage
- `setCameraChangedListener`:
  - [TrackedMap3D.java:83](TrackedMap3D.java#L83) (Tag: `No Tag`)
- `setCameraRestriction`: ❌ No coverage
- `setMap3DClickListener`:
  - [TrackedMap3D.java:84](TrackedMap3D.java#L84) (Tag: `No Tag`)
- `setMapMode`: ❌ No coverage
- `setOnMapReadyListener`:
  - [TrackedMap3D.java:91](TrackedMap3D.java#L91) (Tag: `No Tag`)
  - [java-app/src/main/java/com/example/snippets/java/snippets/MapInitSnippets.java:74](java-app/src/main/java/com/example/snippets/java/snippets/MapInitSnippets.java#L74) (Tag: `maps_android_3d_init_listeners_java`)
- `setOnMapSteadyListener`:
  - [TrackedMap3D.java:92](TrackedMap3D.java#L92) (Tag: `No Tag`)
  - [java-app/src/main/java/com/example/snippets/java/snippets/MapInitSnippets.java:85](java-app/src/main/java/com/example/snippets/java/snippets/MapInitSnippets.java#L85) (Tag: `maps_android_3d_init_listeners_java`)
- `stopCameraAnimation`:
  - [TrackedMap3D.java:89](TrackedMap3D.java#L89) (Tag: `No Tag`)

### `Hole`
- `getVertices`: ❌ No coverage
- `setVertices`: ❌ No coverage
- `writeToParcel`: ❌ No coverage

### `ImageView`
- `equals`: ❌ No coverage
- `getResourceId`: ❌ No coverage
- `hashCode`: ❌ No coverage
- `setResourceId`: ❌ No coverage
- `writeToParcel`: ❌ No coverage

### `LatLngAltitude`
- `getAltitude`:
  - [MapActivity.java:64](MapActivity.java#L64) (Tag: `No Tag`)
- `getLatitude`:
  - [MapActivity.java:64](MapActivity.java#L64) (Tag: `No Tag`)
  - [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java:108](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L108) (Tag: `maps_android_3d_marker_options_java`)
  - [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java:134](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L134) (Tag: `maps_android_3d_marker_click_java`)
  - [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java:189](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L189) (Tag: `maps_android_3d_marker_custom_icon_java`)
  - [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java:76](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L76) (Tag: `maps_android_3d_marker_add_java`)
- `getLongitude`:
  - [MapActivity.java:64](MapActivity.java#L64) (Tag: `No Tag`)
  - [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java:108](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L108) (Tag: `maps_android_3d_marker_options_java`)
  - [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java:134](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L134) (Tag: `maps_android_3d_marker_click_java`)
  - [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java:189](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L189) (Tag: `maps_android_3d_marker_custom_icon_java`)
  - [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java:76](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L76) (Tag: `maps_android_3d_marker_add_java`)
- `getPosition`: ❌ No coverage
- `setAltitude`: ❌ No coverage
- `setLatitude`: ❌ No coverage
- `setLongitude`: ❌ No coverage
- `writeToParcel`: ❌ No coverage

### `LatLngBounds`
- `getNorthEastLat`: ❌ No coverage
- `getNorthEastLng`: ❌ No coverage
- `getSouthWestLat`: ❌ No coverage
- `getSouthWestLng`: ❌ No coverage
- `setNorthEastLat`: ❌ No coverage
- `setNorthEastLng`: ❌ No coverage
- `setSouthWestLat`: ❌ No coverage
- `setSouthWestLng`: ❌ No coverage
- `writeToParcel`: ❌ No coverage

### `Map3DOptions`
- `attributes`: ❌ No coverage
- `bounds`: ❌ No coverage
- `centerAlt`: ❌ No coverage
- `centerLat`: ❌ No coverage
- `centerLng`: ❌ No coverage
- `defaultUiDisabled`: ❌ No coverage
- `fromAttributeSet`: ❌ No coverage
- `getAttributes`: ❌ No coverage
- `getBounds`: ❌ No coverage
- `getCenterAlt`: ❌ No coverage
- `getCenterLat`: ❌ No coverage
- `getCenterLng`: ❌ No coverage
- `getDefaultUiDisabled`: ❌ No coverage
- `getHeading`: ❌ No coverage
- `getMapId`: ❌ No coverage
- `getMapMode`: ❌ No coverage
- `getMaxAltitude`: ❌ No coverage
- `getMaxHeading`: ❌ No coverage
- `getMaxTilt`: ❌ No coverage
- `getMinAltitude`: ❌ No coverage
- `getMinHeading`: ❌ No coverage
- `getMinTilt`: ❌ No coverage
- `getOptions`: ❌ No coverage
- `getRange`: ❌ No coverage
- `getRoll`: ❌ No coverage
- `getTilt`: ❌ No coverage
- `heading`: ❌ No coverage
- `mapId`: ❌ No coverage
- `mapMode`: ❌ No coverage
- `maxAltitude`: ❌ No coverage
- `maxHeading`: ❌ No coverage
- `maxTilt`: ❌ No coverage
- `minAltitude`: ❌ No coverage
- `minHeading`: ❌ No coverage
- `minTilt`: ❌ No coverage
- `options`: ❌ No coverage
- `range`: ❌ No coverage
- `roll`: ❌ No coverage
- `tilt`: ❌ No coverage

### `Map3DView`
- `getMap3DViewAsync`:
  - [MapActivity.java:115](MapActivity.java#L115) (Tag: `No Tag`)
  - [java-app/src/main/java/com/example/snippets/java/snippets/MapInitSnippets.java:48](java-app/src/main/java/com/example/snippets/java/snippets/MapInitSnippets.java#L48) (Tag: `maps_android_3d_init_basic_java`)
- `getOverlayView`: ❌ No coverage
- `getParams`: ❌ No coverage
- `overlayView`: ❌ No coverage
- `params`: ❌ No coverage
- `setParams`: ❌ No coverage

### `Marker`
- `getAltitudeMode`: ❌ No coverage
- `getCollisionBehavior`: ❌ No coverage
- `getDrawsWhenOccluded`: ❌ No coverage
- `getId`: ❌ No coverage
- `getLabel`: ❌ No coverage
- `getPosition`: ❌ No coverage
- `getSizePreserved`: ❌ No coverage
- `getUrl`: ❌ No coverage
- `getZIndex`: ❌ No coverage
- `isExtruded`: ❌ No coverage
- `onMarkerClick`: ❌ No coverage
- `remove`:
  - [SnippetRegistry.java:38](SnippetRegistry.java#L38) (Tag: `No Tag`)
- `setAltitudeMode`: ❌ No coverage
- `setClickListener`:
  - [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java:142](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L142) (Tag: `maps_android_3d_marker_click_java`)
- `setCollisionBehavior`: ❌ No coverage
- `setDrawsWhenOccluded`: ❌ No coverage
- `setExtruded`: ❌ No coverage
- `setLabel`: ❌ No coverage
- `setPosition`: ❌ No coverage
- `setSizePreserved`: ❌ No coverage
- `setUrl`: ❌ No coverage
- `setZIndex`: ❌ No coverage

### `MarkerOptions`
- `getAltitudeMode`: ❌ No coverage
- `getCollisionBehavior`: ❌ No coverage
- `getId`: ❌ No coverage
- `getLabel`: ❌ No coverage
- `getPosition`:
  - [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java:106](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L106) (Tag: `maps_android_3d_marker_options_java`)
  - [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java:132](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L132) (Tag: `maps_android_3d_marker_click_java`)
  - [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java:187](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L187) (Tag: `maps_android_3d_marker_custom_icon_java`)
  - [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java:74](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L74) (Tag: `maps_android_3d_marker_add_java`)
- `getZIndex`: ❌ No coverage
- `isDrawnWhenOccluded`: ❌ No coverage
- `isExtruded`: ❌ No coverage
- `isSizePreserved`: ❌ No coverage
- `setAltitudeMode`:
  - [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java:98](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L98) (Tag: `maps_android_3d_marker_options_java`)
- `setCollisionBehavior`:
  - [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java:100](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L100) (Tag: `maps_android_3d_marker_options_java`)
- `setDrawnWhenOccluded`:
  - [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java:102](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L102) (Tag: `maps_android_3d_marker_options_java`)
- `setExtruded`:
  - [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java:101](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L101) (Tag: `maps_android_3d_marker_options_java`)
- `setId`: ❌ No coverage
- `setLabel`:
  - [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java:175](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L175) (Tag: `maps_android_3d_marker_custom_icon_java`)
  - [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java:69](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L69) (Tag: `maps_android_3d_marker_add_java`)
  - [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java:99](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L99) (Tag: `maps_android_3d_marker_options_java`)
  - [java-app/src/main/java/com/example/snippets/java/snippets/PopoverSnippets.java:99](java-app/src/main/java/com/example/snippets/java/snippets/PopoverSnippets.java#L99) (Tag: `maps_android_3d_popover_options_java`)
- `setPosition`:
  - [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java:128](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L128) (Tag: `maps_android_3d_marker_click_java`)
  - [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java:174](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L174) (Tag: `maps_android_3d_marker_custom_icon_java`)
  - [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java:68](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L68) (Tag: `maps_android_3d_marker_add_java`)
  - [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java:97](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L97) (Tag: `maps_android_3d_marker_options_java`)
  - [java-app/src/main/java/com/example/snippets/java/snippets/PopoverSnippets.java:98](java-app/src/main/java/com/example/snippets/java/snippets/PopoverSnippets.java#L98) (Tag: `maps_android_3d_popover_options_java`)
- `setSizePreserved`: ❌ No coverage
- `setZIndex`: ❌ No coverage

### `MarkerOptions.that`
- `getStyle`: ❌ No coverage
- `setStyle`: ❌ No coverage
- `writeToParcel`: ❌ No coverage

### `MarkerStyle`
- `equals`: ❌ No coverage
- `getImageView`: ❌ No coverage
- `getPinConfiguration`: ❌ No coverage
- `hashCode`: ❌ No coverage
- `writeToParcel`: ❌ No coverage

### `Model`
- `getAltitudeMode`: ❌ No coverage
- `getId`: ❌ No coverage
- `getOrientation`: ❌ No coverage
- `getPosition`: ❌ No coverage
- `getScale`: ❌ No coverage
- `getUrl`: ❌ No coverage
- `onModelClick`: ❌ No coverage
- `remove`:
  - [SnippetRegistry.java:41](SnippetRegistry.java#L41) (Tag: `No Tag`)
- `setAltitudeMode`: ❌ No coverage
- `setClickListener`: ❌ No coverage
- `setOrientation`: ❌ No coverage
- `setPosition`: ❌ No coverage
- `setScale`: ❌ No coverage
- `setUrl`: ❌ No coverage

### `ModelOptions`
- `getAltitudeMode`: ❌ No coverage
- `getId`: ❌ No coverage
- `getOrientation`: ❌ No coverage
- `getPosition`: ❌ No coverage
- `getScale`: ❌ No coverage
- `getUrl`: ❌ No coverage
- `setAltitudeMode`:
  - [java-app/src/main/java/com/example/snippets/java/snippets/ModelSnippets.java:60](java-app/src/main/java/com/example/snippets/java/snippets/ModelSnippets.java#L60) (Tag: `maps_android_3d_model_add_java`)
- `setId`: ❌ No coverage
- `setOrientation`:
  - [java-app/src/main/java/com/example/snippets/java/snippets/ModelSnippets.java:61](java-app/src/main/java/com/example/snippets/java/snippets/ModelSnippets.java#L61) (Tag: `maps_android_3d_model_add_java`)
- `setPosition`:
  - [java-app/src/main/java/com/example/snippets/java/snippets/ModelSnippets.java:58](java-app/src/main/java/com/example/snippets/java/snippets/ModelSnippets.java#L58) (Tag: `maps_android_3d_model_add_java`)
- `setScale`:
  - [java-app/src/main/java/com/example/snippets/java/snippets/ModelSnippets.java:62](java-app/src/main/java/com/example/snippets/java/snippets/ModelSnippets.java#L62) (Tag: `maps_android_3d_model_add_java`)
- `setUrl`:
  - [java-app/src/main/java/com/example/snippets/java/snippets/ModelSnippets.java:59](java-app/src/main/java/com/example/snippets/java/snippets/ModelSnippets.java#L59) (Tag: `maps_android_3d_model_add_java`)
- `writeToParcel`: ❌ No coverage

### `Orientation`
- `getHeading`: ❌ No coverage
- `getRoll`: ❌ No coverage
- `getTilt`: ❌ No coverage
- `setHeading`: ❌ No coverage
- `setRoll`: ❌ No coverage
- `setTilt`: ❌ No coverage
- `writeToParcel`: ❌ No coverage

### `PinConfiguration`
- `builder`:
  - [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java:178](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L178) (Tag: `maps_android_3d_marker_custom_icon_java`)
- `equals`: ❌ No coverage
- `getBackgroundColor`: ❌ No coverage
- `getBorderColor`: ❌ No coverage
- `getGlyph`: ❌ No coverage
- `getScale`: ❌ No coverage
- `hashCode`: ❌ No coverage
- `setBackgroundColor`: ❌ No coverage
- `setBorderColor`: ❌ No coverage
- `setGlyph`: ❌ No coverage
- `setScale`: ❌ No coverage
- `writeToParcel`: ❌ No coverage

### `PinConfiguration.Builder`
- `build`:
  - [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java:183](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L183) (Tag: `maps_android_3d_marker_custom_icon_java`)
- `setBackgroundColor`:
  - [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java:181](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L181) (Tag: `maps_android_3d_marker_custom_icon_java`)
- `setBorderColor`:
  - [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java:182](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L182) (Tag: `maps_android_3d_marker_custom_icon_java`)
- `setGlyph`:
  - [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java:180](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L180) (Tag: `maps_android_3d_marker_custom_icon_java`)
- `setScale`:
  - [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java:179](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L179) (Tag: `maps_android_3d_marker_custom_icon_java`)

### `Polygon`
- `getAltitudeMode`: ❌ No coverage
- `getDrawsOccludedSegments`: ❌ No coverage
- `getExtruded`: ❌ No coverage
- `getFillColor`: ❌ No coverage
- `getGeodesic`: ❌ No coverage
- `getId`: ❌ No coverage
- `getInnerCoordinates`: ❌ No coverage
- `getInnerPaths`: ❌ No coverage
- `getOuterCoordinates`: ❌ No coverage
- `getPath`: ❌ No coverage
- `getStrokeColor`: ❌ No coverage
- `getStrokeWidth`: ❌ No coverage
- `getZIndex`: ❌ No coverage
- `onPolygonClick`: ❌ No coverage
- `remove`:
  - [SnippetRegistry.java:40](SnippetRegistry.java#L40) (Tag: `No Tag`)
- `setAltitudeMode`: ❌ No coverage
- `setClickListener`: ❌ No coverage
- `setDrawsOccludedSegments`: ❌ No coverage
- `setExtruded`: ❌ No coverage
- `setFillColor`: ❌ No coverage
- `setGeodesic`: ❌ No coverage
- `setInnerCoordinates`: ❌ No coverage
- `setInnerPaths`: ❌ No coverage
- `setOuterCoordinates`: ❌ No coverage
- `setPath`: ❌ No coverage
- `setStrokeColor`: ❌ No coverage
- `setStrokeWidth`: ❌ No coverage
- `setZIndex`: ❌ No coverage

### `PolygonOptions`
- `getAltitudeMode`: ❌ No coverage
- `getDrawsOccludedSegments`: ❌ No coverage
- `getExtruded`: ❌ No coverage
- `getFillColor`: ❌ No coverage
- `getGeodesic`: ❌ No coverage
- `getId`: ❌ No coverage
- `getInnerPaths`: ❌ No coverage
- `getPath`: ❌ No coverage
- `getStrokeColor`: ❌ No coverage
- `getStrokeWidth`: ❌ No coverage
- `getZIndex`: ❌ No coverage
- `setAltitudeMode`:
  - [java-app/src/main/java/com/example/snippets/java/snippets/PolygonSnippets.java:100](java-app/src/main/java/com/example/snippets/java/snippets/PolygonSnippets.java#L100) (Tag: `maps_android_3d_polygon_extruded_java`)
  - [java-app/src/main/java/com/example/snippets/java/snippets/PolygonSnippets.java:67](java-app/src/main/java/com/example/snippets/java/snippets/PolygonSnippets.java#L67) (Tag: `maps_android_3d_polygon_add_java`)
- `setDrawsOccludedSegments`: ❌ No coverage
- `setExtruded`:
  - [java-app/src/main/java/com/example/snippets/java/snippets/PolygonSnippets.java:98](java-app/src/main/java/com/example/snippets/java/snippets/PolygonSnippets.java#L98) (Tag: `maps_android_3d_polygon_extruded_java`)
- `setFillColor`:
  - [java-app/src/main/java/com/example/snippets/java/snippets/PolygonSnippets.java:64](java-app/src/main/java/com/example/snippets/java/snippets/PolygonSnippets.java#L64) (Tag: `maps_android_3d_polygon_add_java`)
  - [java-app/src/main/java/com/example/snippets/java/snippets/PolygonSnippets.java:97](java-app/src/main/java/com/example/snippets/java/snippets/PolygonSnippets.java#L97) (Tag: `maps_android_3d_polygon_extruded_java`)
- `setGeodesic`:
  - [java-app/src/main/java/com/example/snippets/java/snippets/PolygonSnippets.java:99](java-app/src/main/java/com/example/snippets/java/snippets/PolygonSnippets.java#L99) (Tag: `maps_android_3d_polygon_extruded_java`)
- `setId`: ❌ No coverage
- `setInnerPaths`: ❌ No coverage
- `setPath`:
  - [java-app/src/main/java/com/example/snippets/java/snippets/PolygonSnippets.java:63](java-app/src/main/java/com/example/snippets/java/snippets/PolygonSnippets.java#L63) (Tag: `maps_android_3d_polygon_add_java`)
  - [java-app/src/main/java/com/example/snippets/java/snippets/PolygonSnippets.java:96](java-app/src/main/java/com/example/snippets/java/snippets/PolygonSnippets.java#L96) (Tag: `maps_android_3d_polygon_extruded_java`)
- `setStrokeColor`:
  - [java-app/src/main/java/com/example/snippets/java/snippets/PolygonSnippets.java:65](java-app/src/main/java/com/example/snippets/java/snippets/PolygonSnippets.java#L65) (Tag: `maps_android_3d_polygon_add_java`)
- `setStrokeWidth`:
  - [java-app/src/main/java/com/example/snippets/java/snippets/PolygonSnippets.java:66](java-app/src/main/java/com/example/snippets/java/snippets/PolygonSnippets.java#L66) (Tag: `maps_android_3d_polygon_add_java`)
- `setZIndex`: ❌ No coverage
- `writeToParcel`: ❌ No coverage

### `Polyline`
- `getAltitudeMode`: ❌ No coverage
- `getCoordinates`: ❌ No coverage
- `getDrawsOccludedSegments`: ❌ No coverage
- `getGeodesic`: ❌ No coverage
- `getId`: ❌ No coverage
- `getOuterColor`: ❌ No coverage
- `getOuterWidth`: ❌ No coverage
- `getPath`: ❌ No coverage
- `getStrokeColor`: ❌ No coverage
- `getStrokeWidth`: ❌ No coverage
- `getZIndex`: ❌ No coverage
- `onPolylineClick`: ❌ No coverage
- `remove`:
  - [SnippetRegistry.java:39](SnippetRegistry.java#L39) (Tag: `No Tag`)
- `setAltitudeMode`: ❌ No coverage
- `setClickListener`: ❌ No coverage
- `setCoordinates`: ❌ No coverage
- `setDrawsOccludedSegments`: ❌ No coverage
- `setGeodesic`: ❌ No coverage
- `setOuterColor`: ❌ No coverage
- `setOuterWidth`: ❌ No coverage
- `setPath`: ❌ No coverage
- `setStrokeColor`: ❌ No coverage
- `setStrokeWidth`: ❌ No coverage
- `setZIndex`: ❌ No coverage

### `PolylineOptions`
- `getAltitudeMode`: ❌ No coverage
- `getDrawsOccludedSegments`: ❌ No coverage
- `getExtruded`: ❌ No coverage
- `getGeodesic`: ❌ No coverage
- `getId`: ❌ No coverage
- `getOuterColor`: ❌ No coverage
- `getOuterWidth`: ❌ No coverage
- `getPath`: ❌ No coverage
- `getStrokeColor`: ❌ No coverage
- `getStrokeWidth`: ❌ No coverage
- `getZIndex`: ❌ No coverage
- `setAltitudeMode`:
  - [java-app/src/main/java/com/example/snippets/java/snippets/PolylineSnippets.java:64](java-app/src/main/java/com/example/snippets/java/snippets/PolylineSnippets.java#L64) (Tag: `maps_android_3d_polyline_add_java`)
  - [java-app/src/main/java/com/example/snippets/java/snippets/PolylineSnippets.java:95](java-app/src/main/java/com/example/snippets/java/snippets/PolylineSnippets.java#L95) (Tag: `maps_android_3d_polyline_options_java`)
- `setDrawsOccludedSegments`:
  - [java-app/src/main/java/com/example/snippets/java/snippets/PolylineSnippets.java:98](java-app/src/main/java/com/example/snippets/java/snippets/PolylineSnippets.java#L98) (Tag: `maps_android_3d_polyline_options_java`)
- `setExtruded`:
  - [java-app/src/main/java/com/example/snippets/java/snippets/PolylineSnippets.java:96](java-app/src/main/java/com/example/snippets/java/snippets/PolylineSnippets.java#L96) (Tag: `maps_android_3d_polyline_options_java`)
- `setGeodesic`:
  - [java-app/src/main/java/com/example/snippets/java/snippets/PolylineSnippets.java:97](java-app/src/main/java/com/example/snippets/java/snippets/PolylineSnippets.java#L97) (Tag: `maps_android_3d_polyline_options_java`)
- `setId`: ❌ No coverage
- `setOuterColor`:
  - [java-app/src/main/java/com/example/snippets/java/snippets/PolylineSnippets.java:93](java-app/src/main/java/com/example/snippets/java/snippets/PolylineSnippets.java#L93) (Tag: `maps_android_3d_polyline_options_java`)
- `setOuterWidth`:
  - [java-app/src/main/java/com/example/snippets/java/snippets/PolylineSnippets.java:94](java-app/src/main/java/com/example/snippets/java/snippets/PolylineSnippets.java#L94) (Tag: `maps_android_3d_polyline_options_java`)
- `setPath`:
  - [java-app/src/main/java/com/example/snippets/java/snippets/PolylineSnippets.java:61](java-app/src/main/java/com/example/snippets/java/snippets/PolylineSnippets.java#L61) (Tag: `maps_android_3d_polyline_add_java`)
  - [java-app/src/main/java/com/example/snippets/java/snippets/PolylineSnippets.java:90](java-app/src/main/java/com/example/snippets/java/snippets/PolylineSnippets.java#L90) (Tag: `maps_android_3d_polyline_options_java`)
- `setStrokeColor`:
  - [java-app/src/main/java/com/example/snippets/java/snippets/PolylineSnippets.java:62](java-app/src/main/java/com/example/snippets/java/snippets/PolylineSnippets.java#L62) (Tag: `maps_android_3d_polyline_add_java`)
  - [java-app/src/main/java/com/example/snippets/java/snippets/PolylineSnippets.java:91](java-app/src/main/java/com/example/snippets/java/snippets/PolylineSnippets.java#L91) (Tag: `maps_android_3d_polyline_options_java`)
- `setStrokeWidth`:
  - [java-app/src/main/java/com/example/snippets/java/snippets/PolylineSnippets.java:63](java-app/src/main/java/com/example/snippets/java/snippets/PolylineSnippets.java#L63) (Tag: `maps_android_3d_polyline_add_java`)
  - [java-app/src/main/java/com/example/snippets/java/snippets/PolylineSnippets.java:92](java-app/src/main/java/com/example/snippets/java/snippets/PolylineSnippets.java#L92) (Tag: `maps_android_3d_polyline_options_java`)
- `setZIndex`: ❌ No coverage
- `writeToParcel`: ❌ No coverage

### `Popover`
- `altitudeMode`: ❌ No coverage
- `anchorId`: ❌ No coverage
- `anchorOffset`: ❌ No coverage
- `autoCloseEnabled`: ❌ No coverage
- `autoPanEnabled`: ❌ No coverage
- `autoPanFn`: ❌ No coverage
- `content`: ❌ No coverage
- `getAltitudeMode`: ❌ No coverage
- `getAnchorId`: ❌ No coverage
- `getAnchorOffset`: ❌ No coverage
- `getAutoCloseEnabled`: ❌ No coverage
- `getAutoPanEnabled`: ❌ No coverage
- `getAutoPanFn`: ❌ No coverage
- `getContent`: ❌ No coverage
- `getIsVisible`: ❌ No coverage
- `getPositionAnchor`: ❌ No coverage
- `getRemoveFn`: ❌ No coverage
- `hide`: ❌ No coverage
- `isVisible`: ❌ No coverage
- `positionAnchor`: ❌ No coverage
- `remove`:
  - [SnippetRegistry.java:42](SnippetRegistry.java#L42) (Tag: `No Tag`)
- `removeFn`: ❌ No coverage
- `show`:
  - [java-app/src/main/java/com/example/snippets/java/snippets/PopoverSnippets.java:116](java-app/src/main/java/com/example/snippets/java/snippets/PopoverSnippets.java#L116) (Tag: `maps_android_3d_popover_options_java`)
  - [java-app/src/main/java/com/example/snippets/java/snippets/PopoverSnippets.java:80](java-app/src/main/java/com/example/snippets/java/snippets/PopoverSnippets.java#L80) (Tag: `maps_android_3d_popover_add_java`)
- `toggle`: ❌ No coverage

### `PopoverOptions`
- `getAltitudeMode`: ❌ No coverage
- `getAnchorOffset`: ❌ No coverage
- `getAutoCloseEnabled`: ❌ No coverage
- `getAutoPanEnabled`: ❌ No coverage
- `getContent`: ❌ No coverage
- `getPopoverStyle`: ❌ No coverage
- `getPositionAnchor`: ❌ No coverage
- `setAltitudeMode`:
  - [java-app/src/main/java/com/example/snippets/java/snippets/PopoverSnippets.java:74](java-app/src/main/java/com/example/snippets/java/snippets/PopoverSnippets.java#L74) (Tag: `maps_android_3d_popover_add_java`)
- `setAnchorOffset`: ❌ No coverage
- `setAutoCloseEnabled`:
  - [java-app/src/main/java/com/example/snippets/java/snippets/PopoverSnippets.java:111](java-app/src/main/java/com/example/snippets/java/snippets/PopoverSnippets.java#L111) (Tag: `maps_android_3d_popover_options_java`)
- `setAutoPanEnabled`:
  - [java-app/src/main/java/com/example/snippets/java/snippets/PopoverSnippets.java:112](java-app/src/main/java/com/example/snippets/java/snippets/PopoverSnippets.java#L112) (Tag: `maps_android_3d_popover_options_java`)
- `setContent`:
  - [java-app/src/main/java/com/example/snippets/java/snippets/PopoverSnippets.java:109](java-app/src/main/java/com/example/snippets/java/snippets/PopoverSnippets.java#L109) (Tag: `maps_android_3d_popover_options_java`)
  - [java-app/src/main/java/com/example/snippets/java/snippets/PopoverSnippets.java:72](java-app/src/main/java/com/example/snippets/java/snippets/PopoverSnippets.java#L72) (Tag: `maps_android_3d_popover_add_java`)
- `setPopoverStyle`: ❌ No coverage
- `setPositionAnchor`:
  - [java-app/src/main/java/com/example/snippets/java/snippets/PopoverSnippets.java:110](java-app/src/main/java/com/example/snippets/java/snippets/PopoverSnippets.java#L110) (Tag: `maps_android_3d_popover_options_java`)
  - [java-app/src/main/java/com/example/snippets/java/snippets/PopoverSnippets.java:73](java-app/src/main/java/com/example/snippets/java/snippets/PopoverSnippets.java#L73) (Tag: `maps_android_3d_popover_add_java`)

### `PopoverShadow`
- `getColor`: ❌ No coverage
- `getOffsetX`: ❌ No coverage
- `getOffsetY`: ❌ No coverage
- `getRadius`: ❌ No coverage
- `setColor`: ❌ No coverage
- `setOffsetX`: ❌ No coverage
- `setOffsetY`: ❌ No coverage
- `setRadius`: ❌ No coverage

### `PopoverStyle`
- `getBackgroundColor`: ❌ No coverage
- `getBorderRadius`: ❌ No coverage
- `getPadding`: ❌ No coverage
- `getShadow`: ❌ No coverage
- `setBackgroundColor`: ❌ No coverage
- `setBorderRadius`: ❌ No coverage
- `setPadding`: ❌ No coverage
- `setShadow`: ❌ No coverage

### `Validators.for`
- `Anchorable`: ❌ No coverage
- `Camera`: ❌ No coverage
- `CameraRestriction`: ❌ No coverage
- `FlyAroundOptions`: ❌ No coverage
- `FlyToOptions`: ❌ No coverage
- `Hole`: ❌ No coverage
- `LatLngAltitude`: ❌ No coverage
- `LatLngBounds`: ❌ No coverage
- `Map3DOptions`: ❌ No coverage
- `MarkerOptions`: ❌ No coverage
- `ModelOptions`: ❌ No coverage
- `Orientation`: ❌ No coverage
- `PolygonOptions`: ❌ No coverage
- `PolylineOptions`: ❌ No coverage
- `PopoverOptions`: ❌ No coverage
- `Vector3D`: ❌ No coverage
- `requireNonNegative`: ❌ No coverage
- `validateAltitude`: ❌ No coverage
- `validateFinite`: ❌ No coverage
- `validateHeading`: ❌ No coverage
- `validateLatitude`: ❌ No coverage
- `validateLongitude`: ❌ No coverage
- `validateRange`: ❌ No coverage
- `validateRoll`: ❌ No coverage
- `validateTilt`: ❌ No coverage

### `Vector3D`
- `getX`: ❌ No coverage
- `getY`: ❌ No coverage
- `getZ`: ❌ No coverage
- `setX`: ❌ No coverage
- `setY`: ❌ No coverage
- `setZ`: ❌ No coverage
- `writeToParcel`: ❌ No coverage

### Missing Extracted API Coverage (Java Snippets)
The following non-getter/setter APIs currently have `0` occurrences within this section:

- `Camera.writeToParcel`
- `CameraRestriction.writeToParcel`
- `FlyAroundOptions.writeToParcel`
- `FlyToOptions.writeToParcel`
- `Glyph.equals`
- `Glyph.fromCircle`
- `Glyph.fromText`
- `Glyph.hashCode`
- `Glyph.writeToParcel`
- `Hole.writeToParcel`
- `ImageView.equals`
- `ImageView.hashCode`
- `ImageView.writeToParcel`
- `LatLngAltitude.writeToParcel`
- `LatLngBounds.writeToParcel`
- `Map3DOptions.attributes`
- `Map3DOptions.bounds`
- `Map3DOptions.centerAlt`
- `Map3DOptions.centerLat`
- `Map3DOptions.centerLng`
- `Map3DOptions.defaultUiDisabled`
- `Map3DOptions.fromAttributeSet`
- `Map3DOptions.heading`
- `Map3DOptions.mapId`
- `Map3DOptions.mapMode`
- `Map3DOptions.maxAltitude`
- `Map3DOptions.maxHeading`
- `Map3DOptions.maxTilt`
- `Map3DOptions.minAltitude`
- `Map3DOptions.minHeading`
- `Map3DOptions.minTilt`
- `Map3DOptions.options`
- `Map3DOptions.range`
- `Map3DOptions.roll`
- `Map3DOptions.tilt`
- `Map3DView.overlayView`
- `Map3DView.params`
- `Marker.isExtruded`
- `Marker.onMarkerClick`
- `MarkerOptions.isDrawnWhenOccluded`
- `MarkerOptions.isExtruded`
- `MarkerOptions.isSizePreserved`
- `MarkerOptions.that.writeToParcel`
- `MarkerStyle.equals`
- `MarkerStyle.hashCode`
- `MarkerStyle.writeToParcel`
- `Model.onModelClick`
- `ModelOptions.writeToParcel`
- `Orientation.writeToParcel`
- `PinConfiguration.equals`
- `PinConfiguration.hashCode`
- `PinConfiguration.writeToParcel`
- `Polygon.onPolygonClick`
- `PolygonOptions.writeToParcel`
- `Polyline.onPolylineClick`
- `PolylineOptions.writeToParcel`
- `Popover.altitudeMode`
- `Popover.anchorId`
- `Popover.anchorOffset`
- `Popover.autoCloseEnabled`
- `Popover.autoPanEnabled`
- `Popover.autoPanFn`
- `Popover.content`
- `Popover.hide`
- `Popover.isVisible`
- `Popover.positionAnchor`
- `Popover.removeFn`
- `Popover.toggle`
- `Validators.for.Anchorable`
- `Validators.for.Camera`
- `Validators.for.CameraRestriction`
- `Validators.for.FlyAroundOptions`
- `Validators.for.FlyToOptions`
- `Validators.for.Hole`
- `Validators.for.LatLngAltitude`
- `Validators.for.LatLngBounds`
- `Validators.for.Map3DOptions`
- `Validators.for.MarkerOptions`
- `Validators.for.ModelOptions`
- `Validators.for.Orientation`
- `Validators.for.PolygonOptions`
- `Validators.for.PolylineOptions`
- `Validators.for.PopoverOptions`
- `Validators.for.Vector3D`
- `Validators.for.requireNonNegative`
- `Validators.for.validateAltitude`
- `Validators.for.validateFinite`
- `Validators.for.validateHeading`
- `Validators.for.validateLatitude`
- `Validators.for.validateLongitude`
- `Validators.for.validateRange`
- `Validators.for.validateRoll`
- `Validators.for.validateTilt`
- `Vector3D.writeToParcel`

