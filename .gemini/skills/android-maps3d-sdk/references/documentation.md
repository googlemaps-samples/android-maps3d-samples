# Google Maps 3D SDK Documentation & Resources

This file guides you to external and internal resources for the Google Maps 3D SDK (`16.0.1+`). Because this SDK is rapidly evolving, these resources are your primary source of truth.

## External Documentation
*   **Official Reference**: [Google Maps Platform 3D SDK for Android](https://developers.google.com/maps/documentation/android-3d-sdk)
*   **API Reference**: Refer to the Javadoc/KDoc links provided on the developer site for specific class signatures.

## Resources & Sample Catalogs

Depending on whether you are working inside the `android-maps3d-samples` repository or in a standalone project, use the appropriate source below.

### 1. Local Workspace (If working inside the sample repo)
If you are working directly in the `android-maps3d-samples` repository, consult these local catalogs and directories:
*   **Master Sample Catalog**: [`./SAMPLE_CATALOG.md`](../../../SAMPLE_CATALOG.md)
*   **SDK Migration Guide (`0.2.x` → `16.0.1`)**: [`./MIGRATION_GUIDE.md`](../../../MIGRATION_GUIDE.md)
*   **Documentation Snippets Catalog**: [`./snippets/CATALOG.md`](../../../snippets/CATALOG.md) (Standalone, region-tagged Kotlin and Java snippets in `./snippets/kotlin-app` and `./snippets/java-app`)
*   **Jetpack Compose Samples Catalog**: [`./Maps3DSamples/ComposeDemos/app/README.md`](../../../Maps3DSamples/ComposeDemos/app/README.md) (Declarative samples powered by `./maps3d-compose`)
*   **Kotlin + Views Samples Catalog**: [`./Maps3DSamples/ApiDemos/kotlin-app/README.md`](../../../Maps3DSamples/ApiDemos/kotlin-app/README.md)
*   **Java + Views Samples Catalog**: [`./Maps3DSamples/ApiDemos/java-app/README.md`](../../../Maps3DSamples/ApiDemos/java-app/README.md)
*   **Shared Common Controllers & Utilities**: `./Maps3DSamples/ApiDemos/common/` (`Map3DSettingsController`, `Projection3D`, `CameraGestureInterceptor`, `PathFollowingEngine`, `AdvancedCameraEngine`, `DataVisualizationEngine`, etc.)
*   **Places UI Kit 3D Showcase**: [`./PlacesUIKit3D/README.md`](../../../PlacesUIKit3D/README.md) (Standalone Clean Architecture + MVVM + Compose showcase integrating Places UI Kit with Maps 3D)

### Expanded Feature Matrix Across Sample Apps

| Feature Category | Sample / Module | Key SDK / Helper APIs Demonstrated |
| :--- | :--- | :--- |
| **Basic Map & Modes** | `HelloMapActivity`, `RoadmapModeActivity` | `Map3DView`, `Map3DInitConfig.create`, `Map3DMode.HYBRID`, `Map3DMode.SATELLITE`, `Map3DMode.ROADMAP`, `setMapMode` |
| **Cloud Map Styling** | `CloudStylingActivity` | `Map3DInitConfig.create(..., mapId = "...")` |
| **Map Localization** | `MapsLocalizationActivity`, `MapLocalizationSnippets` | `LocaleOptions`, `setLocale`, `AppCompatDelegate.setApplicationLocales` |
| **UI Controls** | `UiControlsActivity`, `UiControlSnippets` | `getUiControlSettings()`, `setUiControlsPlacement`, `ControlPlacement`, `ControlOrientation`, `ZoomControl`, `TiltControl`, `RotateControl`, `PanHorizontalControl`, `PanVerticalControl`, `CompassControl`, `Spacer` |
| **Camera Controls & FOV** | `CameraControlsActivity`, `FieldOfViewActivity`, `CameraControlSnippets` | `setCamera`, `flyCameraTo`, `flyCameraAround`, `stopCameraAnimation`, `setCameraChangedListener`, `setOnMapSteadyListener`, `fov` |
| **Camera Restrictions** | `CameraRestrictionsActivity`, `CameraControlSnippets` | `CameraRestriction`, `LatLngBounds`, `setCameraRestriction` |
| **Markers & Glyphs** | `MarkersActivity`, `MarkerSnippets` | `addMarker`, `MarkerOptions`, `PinConfiguration`, `Glyph` (`fromColor`, `fromText`, `fromCircle`, `setImage`), `CollisionBehavior`, `AltitudeMode` |
| **Polylines & Routes** | `PolylinesActivity`, `RoutesActivity`, `PathFollowingActivity`, `PolylineSnippets` | `addPolyline`, `PolylineOptions` (`strokeColor`, `strokeWidth`, `outerColor`, `outerWidth`, `extruded`, `geodesic`, `drawsOccludedSegments`) |
| **Polygons & Extrusion** | `PolygonsActivity`, `DataVisualizationActivity`, `PolygonSnippets` | `addPolygon`, `PolygonOptions` (`fillColor`, `strokeColor`, `extruded`, `geodesic`, `innerPaths` with `Hole`) |
| **3D Models (glTF/GLB)** | `ModelsActivity`, `ModelSnippets` | `addModel`, `ModelOptions` (`url`, `position`, `orientation`, `scale` with `Vector3D`, `altitudeMode`) |
| **Popovers** | `PopoversActivity`, `PopoverSnippets` | `addPopover`, `PopoverOptions` (`content`, `positionAnchor`, `autoCloseEnabled`, `autoPanEnabled`), `Popover.show()` |
| **Map & Place Clicks** | `MapInteractionsActivity`, `PlaceDetailsActivity`, `PlaceSnippets` | `setMap3DClickListener` (`Map3DClickEvent.location`, `Map3DClickEvent.placeId`, returning `Boolean`) |
| **3D Screen Projection** | `Projection3DActivity` | `Projection3D.toScreenLocation` (projecting 3D `LatLngAltitude` to 2D screen coordinates for HUD overlays) |
| **Places UI Kit 3D** | `PlacesUIKit3D` | `PlaceDetailsCompactFragment`, Place Search, Place Autocomplete, elevation-aware 3D camera framing |

### 2. GitHub Repository (If working in a separate project)
If you are working in a new or separate project, you can read the reference implementations directly from GitHub:
*   **Main Repository**: `https://github.com/googlemaps-samples/android-maps3d-samples`
*   **Sample Catalog**: `https://github.com/googlemaps-samples/android-maps3d-samples/blob/main/SAMPLE_CATALOG.md`
*   **Snippets Catalog**: `https://github.com/googlemaps-samples/android-maps3d-samples/blob/main/snippets/CATALOG.md`
*   **Kotlin Snippets**: `https://github.com/googlemaps-samples/android-maps3d-samples/tree/main/snippets/kotlin-app`
*   **Java Snippets**: `https://github.com/googlemaps-samples/android-maps3d-samples/tree/main/snippets/java-app`
*   **Compose Library (`maps3d-compose`)**: `https://github.com/googlemaps-samples/android-maps3d-samples/tree/main/maps3d-compose`
*   **Compose & View Sample Apps**: `https://github.com/googlemaps-samples/android-maps3d-samples/tree/main/Maps3DSamples`
*   **Places UI Kit 3D Showcase**: `https://github.com/googlemaps-samples/android-maps3d-samples/tree/main/PlacesUIKit3D`

## Search Strategies for Agents
When asked to implement a feature, do not assume the API. Use your search tools to find examples in the local workspace or repository first:
*   To find how to add or style a Marker: Search for `"markerOptions"` or `"PinConfiguration"` in `snippets/` or `Maps3DSamples/`.
*   To find camera animation examples: Search for `"flyCameraTo"` or `"flyCameraAround"`.
*   To find UI controls placement examples: Search for `"setUiControlsPlacement"` or `"UiControlSettings"`.
*   To find map localization examples: Search for `"LocaleOptions"` or `"setLocale"`.
*   To find click handling examples: Search for `"setMap3DClickListener"`.
