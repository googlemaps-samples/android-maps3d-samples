# 🗺️ Maps3D API Snippets Catalog

This document serves as a comprehensive developer reference mapping high-level concepts directly index extracts.

## 📑 Snippet Concepts Index

This section maps high-level concepts (groups) to specific demonstration files and lines in both languages.

### Camera
> Snippets demonstrating dynamic camera orchestration and animations.

- **1. Fly To**:
  - *Description*: Animates the camera to a specific position with a tilt and heading over 5 seconds.
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L63-L82)
    - Tag: `maps_android_3d_camera_fly_to_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/CameraControlSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/CameraControlSnippets.java#L43-L60)
    - Tag: `maps_android_3d_camera_fly_to_java`
- **2. Fly Around**:
  - *Description*: Rotates the camera 360 degrees around a specific location over 10 seconds.
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L94-L126)
    - Tag: `maps_android_3d_camera_fly_around_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/CameraControlSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/CameraControlSnippets.java#L69-L89)
    - Tag: `maps_android_3d_camera_fly_around_java`
- **3. Stop Animation**:
  - *Description*: Stops any currently running camera animation immediately.
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L138-L179)
    - Tag: `maps_android_3d_camera_stop_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/CameraControlSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/CameraControlSnippets.java#L98-L107)
    - Tag: `maps_android_3d_camera_stop_java`
- **4. Listen Camera Events**:
  - *Description*: Demonstrates camera change listening
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L191-L208)
    - Tag: `maps_android_3d_camera_events_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/CameraControlSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/CameraControlSnippets.java#L117-L147)
    - Tag: `maps_android_3d_camera_events_java`
- **5. Listen Steady State**:
  - *Description*: Logs to the console when the map finishes rendering or enters a steady state.
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L225-L229)
    - Tag: `maps_android_3d_camera_steady_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/CameraControlSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/CameraControlSnippets.java#L157-L162)
    - Tag: `maps_android_3d_camera_steady_java`
- **6. Camera Restriction**:
  - *Description*: Restricts the camera to a specific altitude range and bounding box.
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/CameraControlSnippets.kt#L241-L267)
    - Tag: `maps_android_3d_camera_restriction_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/CameraControlSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/CameraControlSnippets.java#L181-L188)
    - Tag: `maps_android_3d_camera_restriction_java`

### Map Initialization
> Snippets demonstrating map lifecycle, listeners and readiness states.

- **1. Basic Map3D Initialization**:
  - *Description*: Initializes a standard 3D Map View and sets an initial camera angle.
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/MapInitSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/MapInitSnippets.java#L46-L69)
    - Tag: `maps_android_3d_init_basic_java`
- **1. Listen Events**:
  - *Description*: Initializes a 3D map and logs events when the scene is ready (100% loaded) and steady (camera stopped moving).
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MapInitSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MapInitSnippets.kt#L96-L116)
    - Tag: `maps_android_3d_init_listeners_kt`
- **2. Listen Map Events**:
  - *Description*: Logs map events to the console, such as clicks or idle status.
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/MapInitSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/MapInitSnippets.java#L78-L114)
    - Tag: `maps_android_3d_init_listeners_java`
- **2. Add Map to AndroidView**:
  - *Description*: Shows how to add a Map3DView to an AndroidView which bridges to Jetpack Compose.
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MapInitSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MapInitSnippets.kt#L43-L85)
    - Tag: `maps_android_3d_init_basic_kt`

### Map Localization
> Snippets demonstrating 3D map language and region localization using LocaleOptions and GoogleMap3D.setLocale.

- **1. Chinese (zh-CN)**:
  - *Description*: Applies language='zh' and region='CN' via LocaleOptions and setLocale.
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MapLocalizationSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MapLocalizationSnippets.kt#L40-L46)
    - Tag: `maps_android_3d_localization_chinese_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/MapLocalizationSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/MapLocalizationSnippets.java#L43-L48)
    - Tag: `maps_android_3d_localization_chinese_java`
- **2. French (fr-FR)**:
  - *Description*: Applies language='fr' and region='FR' via LocaleOptions and setLocale.
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MapLocalizationSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MapLocalizationSnippets.kt#L58-L64)
    - Tag: `maps_android_3d_localization_french_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/MapLocalizationSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/MapLocalizationSnippets.java#L57-L62)
    - Tag: `maps_android_3d_localization_french_java`
- **3. Japanese (ja-JP)**:
  - *Description*: Applies language='ja' and region='JP' via LocaleOptions and setLocale.
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MapLocalizationSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MapLocalizationSnippets.kt#L76-L82)
    - Tag: `maps_android_3d_localization_japanese_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/MapLocalizationSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/MapLocalizationSnippets.java#L71-L76)
    - Tag: `maps_android_3d_localization_japanese_java`
- **4. Reset System Default**:
  - *Description*: Resets language and region to the device's system default locale.
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MapLocalizationSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MapLocalizationSnippets.kt#L94-L101)
    - Tag: `maps_android_3d_localization_default_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/MapLocalizationSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/MapLocalizationSnippets.java#L85-L91)
    - Tag: `maps_android_3d_localization_default_java`

### Markers
> Snippets demonstrating standard, extruded, and custom styled markers.

- **1. Basic**:
  - *Description*: Adds a standard marker.
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L53-L84)
    - Tag: `maps_android_3d_marker_add_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L56-L74)
    - Tag: `maps_android_3d_marker_add_java`
- **2. Advanced**:
  - *Description*: Adds a 'Priority Marker' that is extruded and collides with other markers.
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L96-L128)
    - Tag: `maps_android_3d_marker_options_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L84-L105)
    - Tag: `maps_android_3d_marker_options_java`
- **3. Click**:
  - *Description*: Adds a marker that logs a message when clicked.
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L140-L174)
    - Tag: `maps_android_3d_marker_click_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L114-L144)
    - Tag: `maps_android_3d_marker_click_java`
- **4. Custom Icon**:
  - *Description*: Adds a marker with a custom icon using PinConfiguration and Glyph styling.
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt:181](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L181)
    - Tag: `No Tag`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L154-L186)
    - Tag: `maps_android_3d_marker_custom_icon_java`
- **5. Color Glyph**:
  - *Description*: Adds a marker with a customized glyph color.
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L239-L271)
    - Tag: `maps_android_3d_marker_glyph_color_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L195-L213)
    - Tag: `maps_android_3d_marker_glyph_color_java`
- **6. Text Glyph**:
  - *Description*: Adds a marker with text inside the glyph.
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L283-L315)
    - Tag: `maps_android_3d_marker_glyph_text_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L220-L238)
    - Tag: `maps_android_3d_marker_glyph_text_java`
- **7. Circle Glyph**:
  - *Description*: Adds a marker with a default circle glyph.
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/MarkerSnippets.kt#L327-L362)
    - Tag: `maps_android_3d_marker_glyph_circle_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java#L247-L268)
    - Tag: `maps_android_3d_marker_glyph_circle_java`

### Models
> Snippets demonstrating 3D Model (GLB) integration and configuration.

- **1. Basic**:
  - *Description*: Loads a GLB model from a URL and places it clamped to the ground.
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/ModelSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/ModelSnippets.kt#L53-L84)
    - Tag: `maps_android_3d_model_add_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/ModelSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/ModelSnippets.java#L57-L82)
    - Tag: `maps_android_3d_model_add_java`

### Places
> Snippets demonstrating Place (POI/Building) interaction algorithms.

- **1. Listen Clicks**:
  - *Description*: Sets up a listener that logs the Place ID when a user clicks on a 3D building or POI.
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PlaceSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PlaceSnippets.kt#L45-L57)
    - Tag: `maps_android_3d_place_click_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/PlaceSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/PlaceSnippets.java#L53-L75)
    - Tag: `maps_android_3d_place_click_java`

### Polygons
> Snippets demonstrating 2D and 3D extruded polygon layers on the map.

- **1. Basic**:
  - *Description*: Draws a red polygon with a blue stroke around a small area
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L49-L94)
    - Tag: `maps_android_3d_polygon_add_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/PolygonSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/PolygonSnippets.java#L56-L87)
    - Tag: `maps_android_3d_polygon_add_java`
- **2. Extruded**:
  - *Description*: Draws a semi-transparent red extruded polygon (height 50m) around a small area
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L123-L168)
    - Tag: `maps_android_3d_polygon_extruded_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/PolygonSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/PolygonSnippets.java#L103-L134)
    - Tag: `maps_android_3d_polygon_extruded_java`
- **3. Polygon with Hole**:
  - *Description*: Draws a polygon with an interior hole cutout.
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolygonSnippets.kt#L196-L269)
    - Tag: `maps_android_3d_polygon_hole_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/PolygonSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/PolygonSnippets.java#L149-L190)
    - Tag: `maps_android_3d_polygon_hole_java`

### Polylines
> Snippets demonstrating 2D and 3D extruded polyline paths on the map.

- **1. Basic**:
  - *Description*: Draws a thick red polyline connecting three points
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L48-L82)
    - Tag: `maps_android_3d_polyline_add_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/PolylineSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/PolylineSnippets.java#L55-L83)
    - Tag: `maps_android_3d_polyline_add_java`
- **2. Styled**:
  - *Description*: Draws a magenta polyline with a green outline, extruded and following the ground curvature (geodesic), connecting two points.
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PolylineSnippets.kt#L111-L145)
    - Tag: `maps_android_3d_polyline_options_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/PolylineSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/PolylineSnippets.java#L98-L130)
    - Tag: `maps_android_3d_polyline_options_java`

### Popovers
> Snippets demonstrating anchored and configured 3D Popover views.

- **1. Marker Anchor**:
  - *Description*: Adds a 'Hello Popover!' text bubble anchored to a marker
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt#L48-L77)
    - Tag: `maps_android_3d_popover_add_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/PopoverSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/PopoverSnippets.java#L54-L77)
    - Tag: `maps_android_3d_popover_add_java`
- **2. Configured**:
  - *Description*: Adds an 'Info' popover anchored to a marker with auto-close enabled and auto-pan disabled.
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/PopoverSnippets.kt#L106-L133)
    - Tag: `maps_android_3d_popover_options_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/PopoverSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/PopoverSnippets.java#L87-L111)
    - Tag: `maps_android_3d_popover_options_java`

### UI Controls
> Snippets demonstrating built-in 3D map camera UI controls and screen placement.

- **1. Configure UI Controls Placement**:
  - *Description*: Configures Zoom, Tilt, and Rotate controls on the trailing edge and Pan and Compass controls on the leading edge.
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/UiControlSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/UiControlSnippets.kt#L61-L77)
    - Tag: `maps_android_3d_ui_controls_placement_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/UiControlSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/UiControlSnippets.java#L64-L81)
    - Tag: `maps_android_3d_ui_controls_placement_java`
- **2. Swap Leading & Trailing Edges**:
  - *Description*: Moves main camera controls (Zoom, Tilt, Rotate) to the leading edge and Pan/Compass controls to the trailing edge.
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/UiControlSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/UiControlSnippets.kt#L90-L106)
    - Tag: `maps_android_3d_ui_controls_swap_edges_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/UiControlSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/UiControlSnippets.java#L95-L112)
    - Tag: `maps_android_3d_ui_controls_swap_edges_java`
- **3. Horizontal & Top/Bottom Controls**:
  - *Description*: Configures horizontal control orientation with Spacers along the top and bottom edges of the map.
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/UiControlSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/UiControlSnippets.kt#L119-L137)
    - Tag: `maps_android_3d_ui_controls_orientation_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/UiControlSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/UiControlSnippets.java#L123-L142)
    - Tag: `maps_android_3d_ui_controls_orientation_java`
- **4. Hide UI Controls**:
  - *Description*: Displays UI controls briefly and then hides all controls across every screen edge using an empty list.
  - **Kotlin**
    - [kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/UiControlSnippets.kt](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets/UiControlSnippets.kt#L167-L174)
    - Tag: `maps_android_3d_ui_controls_hide_kt`
  - **Java**
    - [java-app/src/main/java/com/example/snippets/java/snippets/UiControlSnippets.java](java-app/src/main/java/com/example/snippets/java/snippets/UiControlSnippets.java#L172-L184)
    - Tag: `maps_android_3d_ui_controls_hide_java`

