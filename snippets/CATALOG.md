# 🗺️ Maps3D API Snippets Catalog

This document serves as a comprehensive developer reference mapping high-level concepts directly index extracts.

## 📑 Snippet Concepts Index

This section maps high-level concepts (groups) to specific demonstration files and lines in both languages.

### Camera
> Snippets demonstrating dynamic camera orchestration and animations.

- **1. Fly To**:
  - *Description*: Animates the camera to a specific position with a tilt and heading over 5 seconds.
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L62-L81)
    - Tag: `maps_android_3d_camera_fly_to_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/CameraControlSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/CameraControlSnippets.java#L45-L64)
    - Tag: `maps_android_3d_camera_fly_to_java`
- **2. Fly Around**:
  - *Description*: Rotates the camera 360 degrees around a specific location over 10 seconds.
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L93-L125)
    - Tag: `maps_android_3d_camera_fly_around_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/CameraControlSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/CameraControlSnippets.java#L75-L94)
    - Tag: `maps_android_3d_camera_fly_around_java`
- **3. Stop Animation**:
  - *Description*: Stops any currently running camera animation immediately.
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L137-L176)
    - Tag: `maps_android_3d_camera_stop_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/CameraControlSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/CameraControlSnippets.java#L106-L115)
    - Tag: `maps_android_3d_camera_stop_java`
- **4. Listen Camera Events**:
  - *Description*: Logs camera change events to the console, printing the center coordinates as the camera moves.
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L188-L202)
    - Tag: `maps_android_3d_camera_events_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/CameraControlSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/CameraControlSnippets.java#L128-L147)
    - Tag: `maps_android_3d_camera_events_java`
- **5. Listen Steady State**:
  - *Description*: Logs to the console when the map finishes rendering or enters a steady state.
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L219-L223)
    - Tag: `maps_android_3d_camera_steady_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/CameraControlSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/CameraControlSnippets.java#L159-L163)
    - Tag: `maps_android_3d_camera_steady_java`
- **6. Camera Restriction**:
  - *Description*: Restricts the camera to a specific altitude range and bounding box.
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L235-L259)
    - Tag: `maps_android_3d_camera_restriction_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/CameraControlSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/CameraControlSnippets.java#L184-L194)
    - Tag: `maps_android_3d_camera_restriction_java`

### Map Initialization
> Snippets demonstrating map lifecycle, listeners and readiness states.

- **1. Listen Events**:
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MapInitSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MapInitSnippets.kt#L94-L114)
    - Tag: `maps_android_3d_init_listeners_kt`
- **1. Basic Map3D Initialization**:
  - *Description*: Initializes a standard 3D Map View and sets an initial camera angle.
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/MapInitSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/MapInitSnippets.java#L45-L64)
    - Tag: `maps_android_3d_init_basic_java`
- **2. Add Map to AndroidView**:
  - *Description*: Shows how to add a Map3DView to an AndroidView which bridges to Jetpack Compose.
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MapInitSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MapInitSnippets.kt#L42-L83)
    - Tag: `maps_android_3d_init_basic_kt`
- **2. Listen Map Events**:
  - *Description*: Logs map events to the console, such as clicks or idle status.
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/MapInitSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/MapInitSnippets.java#L73-L97)
    - Tag: `maps_android_3d_init_listeners_java`

### Markers
> Snippets demonstrating standard, extruded, and custom styled markers.

- **1. Basic**:
  - *Description*: Adds a standard marker.
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L53-L82)
    - Tag: `maps_android_3d_marker_add_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L64-L81)
    - Tag: `maps_android_3d_marker_add_java`
- **2. Advanced**:
  - *Description*: Adds a 'Priority Marker' that is extruded and collides with other markers.
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L94-L124)
    - Tag: `maps_android_3d_marker_options_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L93-L113)
    - Tag: `maps_android_3d_marker_options_java`
- **3. Click**:
  - *Description*: Adds a marker that logs a message when clicked.
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L136-L166)
    - Tag: `maps_android_3d_marker_click_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L125-L147)
    - Tag: `maps_android_3d_marker_click_java`
- **4. Custom Icon**:
  - *Description*: Adds a marker with a custom icon using PinConfiguration and Glyph styling.
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:173](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L173)
    - Tag: `No Tag`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L159-L189)
    - Tag: `maps_android_3d_marker_custom_icon_java`
- **5. Color Glyph**:
  - *Description*: Adds a marker with a customized glyph color.
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L227-L255)
    - Tag: `maps_android_3d_marker_glyph_color_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L201-L220)
    - Tag: `maps_android_3d_marker_glyph_color_java`
- **6. Text Glyph**:
  - *Description*: Adds a marker with text inside the glyph.
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L267-L295)
    - Tag: `maps_android_3d_marker_glyph_text_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L232-L251)
    - Tag: `maps_android_3d_marker_glyph_text_java`
- **7. Circle Glyph**:
  - *Description*: Adds a marker with a default circle glyph.
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L307-L338)
    - Tag: `maps_android_3d_marker_glyph_circle_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L263-L285)
    - Tag: `maps_android_3d_marker_glyph_circle_java`

### Models
- **1. Basic**:
  - *Description*: Loads a GLB model from a URL and places it clamped to the ground.
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/ModelSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/ModelSnippets.kt#L52-L79)
    - Tag: `maps_android_3d_model_add_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/ModelSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/ModelSnippets.java#L60-L78)
    - Tag: `maps_android_3d_model_add_java`

### Places
- **1. Listen Clicks**:
  - *Description*: Sets up a listener that logs the Place ID when a user clicks on a 3D building or POI.
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PlaceSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PlaceSnippets.kt#L44-L53)
    - Tag: `maps_android_3d_place_click_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/PlaceSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/PlaceSnippets.java#L56-L68)
    - Tag: `maps_android_3d_place_click_java`

### Polygons
> Snippets demonstrating 2D and 3D extruded polygon layers on the map.

- **1. Basic**:
  - *Description*: Draws a red polygon with a blue stroke around a small area
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L49-L74)
    - Tag: `maps_android_3d_polygon_add_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/PolygonSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/PolygonSnippets.java#L60-L84)
    - Tag: `maps_android_3d_polygon_add_java`
- **2. Extruded**:
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L100-L125)
    - Tag: `maps_android_3d_polygon_extruded_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/PolygonSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/PolygonSnippets.java#L100-L124)
    - Tag: `maps_android_3d_polygon_extruded_java`
- **3. Polygon with Hole**:
  - *Description*: Draws a polygon with an interior hole cutout.
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L151-L184)
    - Tag: `maps_android_3d_polygon_hole_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/PolygonSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/PolygonSnippets.java#L140-L174)
    - Tag: `maps_android_3d_polygon_hole_java`

### Polylines
> Snippets demonstrating 2D and 3D extruded polyline paths on the map.

- **1. Basic**:
  - *Description*: Draws a thick red polyline connecting three points
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L48-L70)
    - Tag: `maps_android_3d_polyline_add_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/PolylineSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/PolylineSnippets.java#L59-L80)
    - Tag: `maps_android_3d_polyline_add_java`
- **2. Styled**:
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L96-L122)
    - Tag: `maps_android_3d_polyline_options_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/PolylineSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/PolylineSnippets.java#L96-L121)
    - Tag: `maps_android_3d_polyline_options_java`

### Popovers
> Snippets demonstrating anchored and configured 3D Popover views.

- **1. Marker Anchor**:
  - *Description*: Adds a 'Hello Popover!' text bubble anchored to a marker
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt#L48-L73)
    - Tag: `maps_android_3d_popover_add_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/PopoverSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/PopoverSnippets.java#L58-L82)
    - Tag: `maps_android_3d_popover_add_java`
- **2. Configured**:
  - *Description*: Adds an 'Info' popover anchored to a marker with auto-close enabled and auto-pan disabled.
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt#L95-L118)
    - Tag: `maps_android_3d_popover_options_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/PopoverSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/PopoverSnippets.java#L94-L118)
    - Tag: `maps_android_3d_popover_options_java`

