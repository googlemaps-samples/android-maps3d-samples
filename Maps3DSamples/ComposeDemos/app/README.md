# 🚀 Jetpack Compose Samples Catalog

This directory contains the Compose samples for the Android Maps 3D SDK. We use a state-driven approach and lean on the `maps3d-compose` library.

## 📊 Sample Status

| Feature | Status | Source Code | Screenshot | Description |
| :--- | :--- | :--- | :--- | :--- |
| **Basic Map** | ✅ Done | [HelloMapActivity.kt](src/main/java/com/example/composedemos/hellomap/HelloMapActivity.kt) | <img src="src/main/assets/screenshots/hello_map_screenshot.png" alt="Screenshot" width="121"/> | Displays a basic 3D map with standard satellite imagery and initial camera placement. |
| **Polylines** | ✅ Done | [PolylinesActivity.kt](src/main/java/com/example/composedemos/polylines/PolylinesActivity.kt) | <img src="src/main/assets/screenshots/polylines_screenshot.png" alt="Screenshot" width="121"/> | Demonstrates drawing 3D polylines on the map, including custom colors and widths. |
| **Map Interactions** | ✅ Done | [MapInteractionsActivity.kt](src/main/java/com/example/composedemos/mapinteractions/MapInteractionsActivity.kt) | | Demonstrates handling click events on map coordinates and POIs. |
| **Popovers** | ✅ Done | [PopoversActivity.kt](src/main/java/com/example/composedemos/popovers/PopoversActivity.kt) | <img src="src/main/assets/screenshots/popovers_screenshot.png" alt="Screenshot" width="121"/> | Shows how to display interactive popover overlays at specific coordinates on the map. |
| **Camera Controls** | ✅ Done | [CameraControlsActivity.kt](src/main/java/com/example/composedemos/cameracontrols/CameraControlsActivity.kt) | <img src="src/main/assets/screenshots/camera_controls_screenshot.png" alt="Screenshot" width="121"/> | Demonstrates manual control of the camera center, heading, tilt, and range using UI controls. |
| **Polygons** | ✅ Done | [PolygonsActivity.kt](src/main/java/com/example/composedemos/polygons/PolygonsActivity.kt) | <img src="src/main/assets/screenshots/polygons_screenshot.png" alt="Screenshot" width="121"/> | Demonstrates drawing 3D polygons with fill colors and outlines on the map. |
| **Models** | ✅ Done | [ModelsActivity.kt](src/main/java/com/example/composedemos/models/ModelsActivity.kt) | <img src="src/main/assets/screenshots/models_screenshot.png" alt="Screenshot" width="121"/> | Shows how to load and place custom 3D models (gLTF) on the map with position, scale, and orientation. |
| **Markers** | ✅ Done | [MarkersActivity.kt](src/main/java/com/example/composedemos/markers/MarkersActivity.kt) | <img src="src/main/assets/screenshots/markers_screenshot.png" alt="Screenshot" width="121"/> | Demonstrates 2D markers with custom icons, styled pins (pin configurations), and collision behaviors. |
| **Camera Restrictions** | ✅ Done | [CameraRestrictionsActivity.kt](src/main/java/com/example/composedemos/camerarestrictions/CameraRestrictionsActivity.kt) | <img src="src/main/assets/screenshots/camera_restrictions_screenshot.png" alt="Screenshot" width="121"/> | Shows how to restrict the camera range and center bounds to a specific area. |
| **Flight Simulator** | 🚧 Skeleton | [FlightSimulatorActivity.kt](src/main/java/com/example/composedemos/flightsimulator/FlightSimulatorActivity.kt) | | Will demonstrate a first-person camera view simulating flight. |
| **Routes API** | ✅ Done | [RoutesActivity.kt](src/main/java/com/example/composedemos/routes/RoutesActivity.kt) | <img src="src/main/assets/screenshots/routes_screenshot.png" alt="Screenshot" width="121"/> | Demonstrates loading a route from file, rendering the polyline, and animating a 3D car model along the route using a flow-based engine. |
| **Path Following** | ✅ Done | [PathFollowingActivity.kt](src/main/java/com/example/composedemos/pathfollowing/PathFollowingActivity.kt) | | Demonstrates camera traveling along a 3D path with dual-polyline tracking and decoupled gestures. |
| **Path Styling** | 🚧 Skeleton | [PathStylingActivity.kt](src/main/java/com/example/composedemos/pathstyling/PathStylingActivity.kt) | | Will demonstrate custom styling for paths. |
| **Animating Models** | 🚧 Skeleton | [AnimatingModelsActivity.kt](src/main/java/com/example/composedemos/animatingmodels/AnimatingModelsActivity.kt) | | Will demonstrate animating 3D models. |
| **Place Search** | 🚧 Skeleton | [PlaceSearchActivity.kt](src/main/java/com/example/composedemos/placesearch/PlaceSearchActivity.kt) | | Will demonstrate programmatic place search. |
| **Place Autocomplete** | 🚧 Skeleton | [PlaceAutocompleteActivity.kt](src/main/java/com/example/composedemos/placeautocomplete/PlaceAutocompleteActivity.kt) | | Will demonstrate place autocomplete widget. |
| **Place Details** | ✅ Done | [PlaceDetailsActivity.kt](src/main/java/com/example/composedemos/placedetails/PlaceDetailsActivity.kt) | <img src="src/main/assets/screenshots/place_details_screenshot.png" alt="Screenshot" width="121"/> | Demonstrates loading place details using the modern Places UI Kit fragment (Fragment Interop) with a custom 'Boulder Nature Hippie' theme. |
| **Advanced Camera Animation** | ✅ Done | [AdvancedCameraAnimationActivity.kt](src/main/java/com/example/composedemos/advancedcameraanimation/AdvancedCameraAnimationActivity.kt) | | Demonstrates reverse-trig stationary tracking, 5-step keyframe tour, and 360° orbit. |
| **Data Visualization** | ✅ Done | [DataVisualizationActivity.kt](src/main/java/com/example/composedemos/datavisualization/DataVisualizationActivity.kt) | | Demonstrates 3D extruded flood simulation polygons with dynamic elevation slider. |
| **Cloud Map Styling** | ✅ Done | [CloudStylingActivity.kt](src/main/java/com/example/composedemos/cloudstyling/CloudStylingActivity.kt) | | Demonstrates dynamic Cloud Console Map ID runtime styling. |
| **Roadmap Mode** | ✅ Done | [RoadmapModeActivity.kt](src/main/java/com/example/composedemos/roadmapmode/RoadmapModeActivity.kt) | | Demonstrates 2D vector roadmap tiles overlay on 3D photorealistic mesh. |
| **Field Of View** | ✅ Done | [FieldOfViewActivity.kt](src/main/java/com/example/composedemos/fieldofview/FieldOfViewActivity.kt) | | Demonstrates interactive camera lens focal length and FOV adjustments. |

---
> [!NOTE]
> Status `🚧 Skeleton` means the activity exists and can be launched from the main list, but contains a TODO placeholder UI. We are actively implementing these following a TDD approach.
