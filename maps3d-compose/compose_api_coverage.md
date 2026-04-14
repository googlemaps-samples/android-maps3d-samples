# Compose API Coverage Checklist

This document tracks the coverage of the Maps 3D SDK APIs in the experimental Compose wrapper.

> [!WARNING]
> This implementation is a **Work In Progress (WIP) experiment** and serves as a **reference implementation**. It is not intended for production use.

## Not Yet Exposed Functionality

The following features and listeners from the Maps 3D SDK are not yet supported or exposed in this experimental Compose wrapper:

| Feature / Class | Status | Reference / Notes |
| :--- | :--- | :--- |
| **Map3DViewUiController** | Not Supported | Gestures and UI settings controller. |
| **OnCameraChangedListener** | Not Supported | Camera event listener. |
| **OnFirstSceneListener** | Not Supported | Scene loaded event listener. |
| **OnPlaceClickListener** | Not Supported | Place click event listener. |
| **Anchorable** | Not Supported | Interface for anchorable objects. |
| **BoundingBox** | Not Supported | Spatial bounding box. |
| **DrawingState** | Not Supported | State of drawing operations. |
| **Glyph** | Not Supported | Text rendering elements. |
| **MarkerView / MarkerViewOptions** | Not Supported | View-based markers. |
| **PinConfiguration** | Not Supported | Pin styling configuration. |
| **PinView** | Not Supported | Custom pin views. |
| **VisibilityState** | Not Supported | Visibility state tracking. |

## Coverage Status

| Feature / Class | Status | Reference / Notes |
| :--- | :--- | :--- |
| **Map3DOptions** | Supported | Passed to `GoogleMap3D` in [`GoogleMap3D.kt:L69`](src/main/java/com/google/maps/android/compose3d/GoogleMap3D.kt#L69). |
| **GoogleMap3D api calls** | Partial | Some exposed via parameters, others require native instance via `onMapReady`. |
| **Map3DViewUiController** | Not Supported | Not yet exposed in the Compose wrapper. |
| **OnCameraAnimationEndListener** | Supported via Native | Used in sample extensions, not exposed as parameter. |
| **OnCameraChangedListener** | Not Supported | Not yet exposed. |
| **OnFirstSceneListener** | Not Supported | Not yet exposed. |
| **OnMap3DClickListener** | Supported | Exposed as `onMapClick` in `GoogleMap3D`. |
| **OnMap3DViewReadyCallback** | Handled Internally | Used in [`GoogleMap3D.kt:L84`](src/main/java/com/google/maps/android/compose3d/GoogleMap3D.kt#L84) to initialize. |
| **OnMarkerClickListener** | Handled Internally | Exposed as `onClick` in `MarkerConfig`. |
| **OnModelClickListener** | Handled Internally | Exposed as `onClick` in `ModelConfig`. |
| **OnPlaceClickListener** | Not Supported | Not yet exposed. |
| **OnPolygonClickListener** | Handled Internally | Exposed as `onClick` in `PolygonConfig`. |
| **OnPolylineClickListener** | Handled Internally | Exposed as `onClick` in `PolylineConfig`. |
| **AltitudeMode** | Supported | Defined in [`DataModels.kt`](src/main/java/com/google/maps/android/compose3d/DataModels.kt). |
| **Anchorable** | Not Supported | Implied by marker anchoring but not explicitly exposed. |
| **BoundingBox** | Not Supported | Not yet exposed. |
| **Camera** | Supported | Hoisted in `GoogleMap3D` [`GoogleMap3D.kt:L60`](src/main/java/com/google/maps/android/compose3d/GoogleMap3D.kt#L60). |
| **CameraRestriction** | Supported | Passed to `GoogleMap3D` [`GoogleMap3D.kt:L67`](src/main/java/com/google/maps/android/compose3d/GoogleMap3D.kt#L67). |
| **CollisionBehavior** | Supported | Used in `MarkerConfig` in [`DataModels.kt`](src/main/java/com/google/maps/android/compose3d/DataModels.kt). |
| **DrawingState** | Not Supported | Not yet exposed. |
| **FlyAround / FlyTo** | Supported via Native | Used in samples via native instance, not declarative. |
| **Glyph** | Not Supported | Not yet exposed. |
| **Hole** | Supported | Used in `PolygonConfig` in `Map3DState.kt`. |
| **ImageView** | Handled Internally | Used for Popover content rendering. |
| **Marker / MarkerOptions** | Supported | Via `MarkerConfig` in [`DataModels.kt:L35`](src/main/java/com/google/maps/android/compose3d/DataModels.kt#L35). |
| **MarkerView / MarkerViewOptions** | Not Supported | Not yet exposed. |
| **Model / ModelOptions** | Supported | Via `ModelConfig` in [`DataModels.kt:L93`](src/main/java/com/google/maps/android/compose3d/DataModels.kt#L93). |
| **Orientation** | Supported | Used in `ModelConfig` in [`DataModels.kt`](src/main/java/com/google/maps/android/compose3d/DataModels.kt). |
| **PinConfiguration** | Not Supported | Not yet exposed. |
| **PinView** | Not Supported | Not yet exposed. |
| **Polygon / PolygonOptions** | Supported | Via `PolygonConfig` in [`DataModels.kt:L70`](src/main/java/com/google/maps/android/compose3d/DataModels.kt#L70). |
| **Polyline / PolylineOptions** | Supported | Via `PolylineConfig` in [`DataModels.kt:L52`](src/main/java/com/google/maps/android/compose3d/DataModels.kt#L52). |
| **OnMapReady** | Supported | Callback in `GoogleMap3D` [`GoogleMap3D.kt:L70`](src/main/java/com/google/maps/android/compose3d/GoogleMap3D.kt#L70). |
| **OnMapSteady** | Supported | Callback in `GoogleMap3D` [`GoogleMap3D.kt:L71`](src/main/java/com/google/maps/android/compose3d/GoogleMap3D.kt#L71). |
| **Popover / PopoverContentsView...**| Supported | Via `PopoverConfig` in [`DataModels.kt:L109`](src/main/java/com/google/maps/android/compose3d/DataModels.kt#L109). |
| **Vector3D** | Supported | Used for scale in [`Map3DState.kt`](src/main/java/com/google/maps/android/compose3d/Map3DState.kt). |
| **VisibilityState** | Not Supported | Not yet exposed. |
| **GoogleMap3D Composable...** | Supported | Lifecycle handling implemented in [`GoogleMap3D.kt`](src/main/java/com/google/maps/android/compose3d/GoogleMap3D.kt). |
| **Core state management...** | Partial | Map properties supported, gestures might need `Map3DViewUiController`. |
| **Camera animation...** | Supported via Native | `flyCameraTo` and `flyCameraAround` used in samples. |

## References

### Implementation in `maps3d-compose`

- **Core Composable**: [`GoogleMap3D.kt:L59`](src/main/java/com/google/maps/android/compose3d/GoogleMap3D.kt#L59)
- **State Management (`Map3DState.kt`)**:
    - `syncMarkers`: [`Map3DState.kt:L51`](src/main/java/com/google/maps/android/compose3d/Map3DState.kt#L51)
    - `syncPolylines`: [`Map3DState.kt:L109`](src/main/java/com/google/maps/android/compose3d/Map3DState.kt#L109)
    - `syncPolygons`: [`Map3DState.kt:L154`](src/main/java/com/google/maps/android/compose3d/Map3DState.kt#L154)
    - `syncModels`: [`Map3DState.kt:L208`](src/main/java/com/google/maps/android/compose3d/Map3DState.kt#L208)
    - `syncPopovers`: [`Map3DState.kt:L253`](src/main/java/com/google/maps/android/compose3d/Map3DState.kt#L253)

### Demonstrations in `maps3d-compose-demo`

- **Basic Map & Camera**: [`BasicMapActivity.kt:L140`](../maps3d-compose-demo/src/main/java/com/example/maps3dcomposedemo/BasicMapActivity.kt#L140)
- **Map Options & Restrictions**: [`MapOptionsActivity.kt:L102`](../maps3d-compose-demo/src/main/java/com/example/maps3dcomposedemo/MapOptionsActivity.kt#L102)
- **Camera Animations**: [`CameraAnimationsActivity.kt:L88`](../maps3d-compose-demo/src/main/java/com/example/maps3dcomposedemo/CameraAnimationsActivity.kt#L88)
- **Markers**: [`MarkersActivity.kt:L89`](../maps3d-compose-demo/src/main/java/com/example/maps3dcomposedemo/MarkersActivity.kt#L89)
- **Polylines**: [`PolylinesActivity.kt:L108`](../maps3d-compose-demo/src/main/java/com/example/maps3dcomposedemo/PolylinesActivity.kt#L108)
- **Polygons**: [`PolygonsActivity.kt:L161`](../maps3d-compose-demo/src/main/java/com/example/maps3dcomposedemo/PolygonsActivity.kt#L161)
- **3D Models**: [`ModelsActivity.kt:L83`](../maps3d-compose-demo/src/main/java/com/example/maps3dcomposedemo/ModelsActivity.kt#L83)
- **Popovers**: [`PopoversActivity.kt:L102`](../maps3d-compose-demo/src/main/java/com/example/maps3dcomposedemo/PopoversActivity.kt#L102)
