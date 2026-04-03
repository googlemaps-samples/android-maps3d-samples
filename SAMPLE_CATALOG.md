# Android Maps 3D Sample Catalog

This catalog documents the availability and locations of various code samples for the Android Maps 3D SDK based on the reference spreadsheet. 

## 1. Basic Map
**Description:** Simplest possible map
* **Android (Kotlin):** Found in [`HelloMapActivity.kt`](Maps3DSamples/ApiDemos/kotlin-app/src/main/java/com/example/maps3dkotlin/hellomap/HelloMapActivity.kt)
* **Android (Java):** Found in [`HelloMapActivity.java`](Maps3DSamples/ApiDemos/java-app/src/main/java/com/example/maps3djava/hellomap/HelloMapActivity.java)
* **Android (Compose):** Found in [`BasicComposeMapActivity.kt`](Maps3DSamples/advanced/app/src/main/java/com/example/advancedmaps3dsamples/basicmap/BasicComposeMapActivity.kt)

## 2. Camera animations
**Description:** `flyCameraTo` and `FlyAround`
* **Android (Kotlin):** Found in [`CameraControlsActivity.kt`](Maps3DSamples/ApiDemos/kotlin-app/src/main/java/com/example/maps3dkotlin/cameracontrols/CameraControlsActivity.kt)
* **Android (Java):** Found in [`CameraControlsActivity.java`](Maps3DSamples/ApiDemos/java-app/src/main/java/com/example/maps3djava/cameracontrols/CameraControlsActivity.java)
* **Android (Compose):** Demonstrated via [`CameraUpdate.kt`](Maps3DSamples/advanced/app/src/main/java/com/example/advancedmaps3dsamples/utils/CameraUpdate.kt) and within `ScenariosActivity.kt`.

## 3. Camera restrictions
**Description:** Restrict the camera
* **Android (Kotlin):** Found in [`CameraControlsActivity.kt`](Maps3DSamples/ApiDemos/kotlin-app/src/main/java/com/example/maps3dkotlin/cameracontrols/CameraControlsActivity.kt)
* **Android (Java):** Found in [`CameraControlsActivity.java`](Maps3DSamples/ApiDemos/java-app/src/main/java/com/example/maps3djava/cameracontrols/CameraControlsActivity.java)
* **Android (Compose):** **Not Found**. The spreadsheet claims this exists, but no Compose implementation of camera restrictions was found.

## 4. Camera controls
**Description:** Roll, tilt, heading
* **Android (Kotlin):** Found in [`CameraControlsActivity.kt`](Maps3DSamples/ApiDemos/kotlin-app/src/main/java/com/example/maps3dkotlin/cameracontrols/CameraControlsActivity.kt)
* **Android (Java):** Found in [`CameraControlsActivity.java`](Maps3DSamples/ApiDemos/java-app/src/main/java/com/example/maps3djava/cameracontrols/CameraControlsActivity.java)
* **Android (Compose):** Found in [`CameraAttributesScreen.kt`](Maps3DSamples/advanced/app/src/main/java/com/example/advancedmaps3dsamples/scenarios/CameraAttributesScreen.kt)

## 5. Flight Simulator
**Description:** Fly along a flight path
* **Android (Kotlin):** **Not Found**
* **Android (Java):** **Not Found**
* **Android (Compose):** **Not Found** ([`RouteSampleActivity.kt`](Maps3DSamples/advanced/app/src/main/java/com/example/advancedmaps3dsamples/route/RouteSampleActivity.kt) shows a route with a model being animated along it, not a flight simulator).

## 6. Map Tap
**Description:** Tap on the map or on a POI
* **Android (Kotlin):** Found in [`MapInteractionsActivity.kt`](Maps3DSamples/ApiDemos/kotlin-app/src/main/java/com/example/maps3dkotlin/mapinteractions/MapInteractionsActivity.kt)
* **Android (Java):** Found in [`MapInteractionsActivity.java`](Maps3DSamples/ApiDemos/java-app/src/main/java/com/example/maps3djava/mapinteractions/MapInteractionsActivity.java)
* **Android (Compose):** Correctly marked as missing in spreadsheet.

## 7. Markers
**Description:** Display default markers
* **Android (Kotlin):** Found in [`MarkersActivity.kt`](Maps3DSamples/ApiDemos/kotlin-app/src/main/java/com/example/maps3dkotlin/markers/MarkersActivity.kt)
* **Android (Java):** Found in [`MarkersActivity.java`](Maps3DSamples/ApiDemos/java-app/src/main/java/com/example/maps3djava/markers/MarkersActivity.java)
* **Android (Compose):** Modeled through `advanced` app's generic handling in [`MapObject.kt`](Maps3DSamples/advanced/app/src/main/java/com/example/advancedmaps3dsamples/common/MapObject.kt).

## 8. Marker Collisions
**Description:** Configure marker behaviour
* **Android (Kotlin):** Found within [`MarkersActivity.kt`](Maps3DSamples/ApiDemos/kotlin-app/src/main/java/com/example/maps3dkotlin/markers/MarkersActivity.kt)
* **Android (Java):** Found within [`MarkersActivity.java`](Maps3DSamples/ApiDemos/java-app/src/main/java/com/example/maps3djava/markers/MarkersActivity.java)
* **Android (Compose):** Correctly marked as missing in spreadsheet.

## 9. Marker Styling
**Description:** Style markers
* **Android (Kotlin):** Found within [`MarkersActivity.kt`](Maps3DSamples/ApiDemos/kotlin-app/src/main/java/com/example/maps3dkotlin/markers/MarkersActivity.kt) (covers PinConfiguration/Glyphs)
* **Android (Java):** Found within [`MarkersActivity.java`](Maps3DSamples/ApiDemos/java-app/src/main/java/com/example/maps3djava/markers/MarkersActivity.java) 
* **Android (Compose):** Correctly marked as missing in spreadsheet.

## 10. Models
**Description:** Display a 3D model
* **Android (Kotlin):** Found in [`ModelsActivity.kt`](Maps3DSamples/ApiDemos/kotlin-app/src/main/java/com/example/maps3dkotlin/models/ModelsActivity.kt)
* **Android (Java):** Found in [`ModelsActivity.java`](Maps3DSamples/ApiDemos/java-app/src/main/java/com/example/maps3djava/models/ModelsActivity.java)
* **Android (Compose):** Modeled through `advanced` app's generic handling in [`MapObject.kt`](Maps3DSamples/advanced/app/src/main/java/com/example/advancedmaps3dsamples/common/MapObject.kt).

## 11. Popovers
**Description:** Show popovers anchored to markers and coordinates
* **Android (Kotlin):** Found in [`PopoversActivity.kt`](Maps3DSamples/ApiDemos/kotlin-app/src/main/java/com/example/maps3dkotlin/popovers/PopoversActivity.kt)
* **Android (Java):** Found in [`PopoversActivity.java`](Maps3DSamples/ApiDemos/java-app/src/main/java/com/example/maps3djava/popovers/PopoversActivity.java)
* **Android (Compose):** Correctly marked as missing in spreadsheet.

## 12. Polylines
**Description:** Display polylines with onTap events
* **Android (Kotlin):** Found in [`PolylinesActivity.kt`](Maps3DSamples/ApiDemos/kotlin-app/src/main/java/com/example/maps3dkotlin/polylines/PolylinesActivity.kt)
* **Android (Java):** Found in [`PolylinesActivity.java`](Maps3DSamples/ApiDemos/java-app/src/main/java/com/example/maps3djava/polylines/PolylinesActivity.java)
* **Android (Compose):** Covered via [`MapObject.kt`](Maps3DSamples/advanced/app/src/main/java/com/example/advancedmaps3dsamples/common/MapObject.kt) abstractions.

## 13. Polygons
**Description:** Display polygons with onTap events
* **Android (Kotlin):** Found in [`PolygonsActivity.kt`](Maps3DSamples/ApiDemos/kotlin-app/src/main/java/com/example/maps3dkotlin/polygons/PolygonsActivity.kt)
* **Android (Java):** Found in [`PolygonsActivity.java`](Maps3DSamples/ApiDemos/java-app/src/main/java/com/example/maps3djava/polygons/PolygonsActivity.java)
* **Android (Compose):** Covered via [`MapObject.kt`](Maps3DSamples/advanced/app/src/main/java/com/example/advancedmaps3dsamples/common/MapObject.kt) abstractions.

## Additional Samples to Cover

The following samples have been identified as targets to cover.

## 14. Path Following
**Description:** More advanced path functionality. Follow a path at ground level: urban, rural with sliders for camera control inputs (range, altitude, heading, tilt, follow speed)
**Notes:** Show the effect of camera settings for different use cases: urban/rural; hiking/driving/flying
* **Android (Kotlin):** **Not Found**
* **Android (Java):** **Not Found**
* **Android (Compose):** Found in [`RouteSampleActivity.kt`](Maps3DSamples/advanced/app/src/main/java/com/example/advancedmaps3dsamples/route/RouteSampleActivity.kt)

## 15. Path Styling
**Description:** Style route/path data for top use cases (walking, driving, urban, rural, clamped, extruded, convert 2D to 3D, geometry utils)
**Notes:** How to deal with extrusions, occlusion, best practices when trying to style a path or driving route over different terrain, explore options with sliders for values
* **Android (Kotlin):** **Not Found**
* **Android (Java):** **Not Found**
* **Android (Compose):** **Not Found**

## 16. Animating models
**Description:** Animate a 3D Model along a path: urban/rural walking/driving examples
* **Android (Kotlin):** **Not Found**
* **Android (Java):** **Not Found**
* **Android (Compose):** Found in [`RouteSampleActivity.kt`](Maps3DSamples/advanced/app/src/main/java/com/example/advancedmaps3dsamples/route/RouteSampleActivity.kt)

## 17. Cross-Product (PUIK) - Place Search
**Description:** Add Place Search (UI Kit)
**Notes:** Go further than Place Tap demo
* **Android (Kotlin):** **Not Found**
* **Android (Java):** **Not Found**
* **Android (Compose):** **Not Found**

## 18. Cross-Product (PUIK) - Place Autocomplete
**Description:** Add Place Autocomplete (UI Kit)
* **Android (Kotlin):** **Not Found**
* **Android (Java):** **Not Found**
* **Android (Compose):** **Not Found**

## 19. Cross-Product (PUIK) - Place Details
**Description:** Display PlaceDetails results (UI Kit)
**Notes:** Go further than Place Tap demo
* **Android (Kotlin):** **Not Found**
* **Android (Java):** **Not Found**
* **Android (Compose):** Found in [`MainActivity.kt`](PlacesUIKit3D/src/main/java/com/example/placesuikit3d/MainActivity.kt)

## 20. Advanced Camera animation - Camera + Model
**Description:** Animating the camera + model positions: different approaches (simple, keyframe, dispatchqueue + other tbd), best practices
* **Android (Kotlin):** **Not Found**
* **Android (Java):** **Not Found**
* **Android (Compose):** Found in [`RouteSampleActivity.kt`](Maps3DSamples/advanced/app/src/main/java/com/example/advancedmaps3dsamples/route/RouteSampleActivity.kt)

## 21. Advanced Camera animation - Codelab
**Description:** Codelab: Working with camera animation in the Maps 3D SDK
**Notes:** Add a codelab that builds the sample
* **Android (Kotlin):** **Not Found**
* **Android (Java):** **Not Found**
* **Android (Compose):** **Not Found**

## 22. Advanced Camera animation - Tutorial
**Description:** Tutorial: Working with camera animation in the Maps 3D SDK
**Notes:** Devsite tutorial that explains the sample
* **Android (Kotlin):** **Not Found**
* **Android (Java):** **Not Found**
* **Android (Compose):** **Not Found**

## 23. Data visualisation - Flood Fill Polygons
**Description:** Flood Fill Polygons
**Notes:** Show "flood" polygon and use slider to change depth/elevation of flood
* **Android (Kotlin):** **Not Found**
* **Android (Java):** **Not Found**
* **Android (Compose):** **Not Found**
