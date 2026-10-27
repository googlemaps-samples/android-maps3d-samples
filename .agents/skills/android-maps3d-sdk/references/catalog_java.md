# Common Operations Catalog (Java)

This catalog provides verified, region-tagged reference snippets for common operations when using the Google Maps 3D SDK with Java and Android Views.

## 1. Map Initialization & Readiness Listeners
Description: Initializing a `Map3DView` programmatically with a valid `Map3DMode` and listening for readiness (`OnMapReadyListener`) and steady state (`OnMapSteadyListener`).

### Basic Programmatic Initialization
```java
// [START maps_android_3d_init_basic_java]
Map3DView map3DView = new Map3DView(context, Map3DMode.HYBRID);

// Get the map asynchronously
map3DView.getMap3DViewAsync(
        new OnMap3DViewReadyCallback() {
            @Override
            public void onMap3DViewReady(@NonNull GoogleMap3D googleMap3D) {
                // Map is ready to be used
                LatLngAltitude center = new LatLngAltitude(40.0150, -105.2705, 5000.0);
                Camera camera = new Camera(center, 0.0, 45.0, 0.0, 10000.0);
                googleMap3D.setCamera(camera);
            }

            @Override
            public void onError(@NonNull Exception e) {
                // Log the exception to guide developers debugging layout mismatches
                Log.e(
                        "MapInitSnippets",
                        "Failed to initialize 3D Map: " + e.getMessage(),
                        e);
            }
        });
// [END maps_android_3d_init_basic_java]
```

### Scene Readiness & Steady State Listeners
```java
// [START maps_android_3d_init_listeners_java]
Handler mainHandler = new Handler(Looper.getMainLooper());

map.setOnMapReadyListener(
        new OnMapReadyListener() {
            @Override
            public void onMapReady(double sceneReadiness) {
                if (sceneReadiness == 1.0) {
                    // Scene is fully loaded
                    mainHandler.post(
                            () ->
                                    Toast.makeText(
                                                    context,
                                                    "Map Scene Ready (100%)",
                                                    Toast.LENGTH_SHORT)
                                            .show());
                }
            }
        });

map.setOnMapSteadyListener(
        new OnMapSteadyListener() {
            @Override
            public void onMapSteadyChange(boolean isSceneSteady) {
                if (isSceneSteady) {
                    // Camera is not moving and data is loaded
                    mainHandler.post(
                            () ->
                                    Toast.makeText(
                                                    context,
                                                    "Map Scene Steady",
                                                    Toast.LENGTH_SHORT)
                                            .show());
                }
            }
        });
// [END maps_android_3d_init_listeners_java]
```

## 2. Adding Markers (Basic, Advanced & Custom Glyphs)
Description: Markers point out points of interest using `MarkerOptions`. You can configure `setPosition`, `setAltitudeMode`, `setLabel`, `setCollisionBehavior`, `setExtruded`, `setDrawnWhenOccluded`, and custom `PinConfiguration` / `Glyph` styles.

### Basic Marker
```java
// [START maps_android_3d_marker_add_java]
LatLngAltitude position = new LatLngAltitude(37.4220, -122.0841, 10.0);

MarkerOptions options = new MarkerOptions();
options.setPosition(position);
options.setLabel("Basic Marker");
// MarkerOptions uses label, not title.

Marker marker = map.addMarker(options);
// [END maps_android_3d_marker_add_java]
```

### Advanced Priority & Extruded Marker
```java
// [START maps_android_3d_marker_options_java]
LatLngAltitude position = new LatLngAltitude(37.4220, -122.0841, 10.0);

MarkerOptions options = new MarkerOptions();
options.setPosition(position);
options.setAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);
options.setLabel("Priority Marker");
options.setCollisionBehavior(CollisionBehavior.OPTIONAL_AND_HIDES_LOWER_PRIORITY);
options.setExtruded(true);
options.setDrawnWhenOccluded(true);

Marker marker = map.addMarker(options);
// [END maps_android_3d_marker_options_java]
```

### Custom Icon & Styled Glyphs (`PinConfiguration` & `Glyph`)
```java
// [START maps_android_3d_marker_custom_icon_java]
LatLngAltitude position = new LatLngAltitude(37.4220, -122.0841, 10.0);

// Create a Glyph with a custom image
Glyph glyphImage = Glyph.fromColor(Color.YELLOW);

ImageView mapImageView = new ImageView(R.mipmap.ic_launcher);
glyphImage.setImage(mapImageView);

MarkerOptions options = new MarkerOptions();
options.setPosition(position);
options.setLabel("Custom Icon Marker");

// Set the style using PinConfiguration
options.setStyle(
        PinConfiguration.builder()
                .setScale(1.5f)
                .setGlyph(glyphImage)
                .setBackgroundColor(Color.BLUE)
                .setBorderColor(Color.WHITE)
                .build());

Marker marker = map.addMarker(options);
// [END maps_android_3d_marker_custom_icon_java]
```

```java
// [START maps_android_3d_marker_glyph_text_java]
LatLngAltitude position = new LatLngAltitude(37.4220, -122.0841, 10.0);

MarkerOptions options = new MarkerOptions();
options.setPosition(position);
options.setLabel("Text Glyph");
options.setStyle(PinConfiguration.builder().setGlyph(Glyph.fromText("NYC")).build());

map.addMarker(options);
// [END maps_android_3d_marker_glyph_text_java]
```
> *Note*: You can also style glyphs with `Glyph.fromColor(Color.CYAN)` (`maps_android_3d_marker_glyph_color_java`) or `Glyph.fromCircle()` (`maps_android_3d_marker_glyph_circle_java`); see [`MarkerSnippets.java`](../../../../snippets/java-app/src/main/java/com/example/snippets/java/snippets/MarkerSnippets.java).

### Object Click Listeners (`setClickListener`)
```java
// [START maps_android_3d_marker_click_java]
LatLngAltitude position = new LatLngAltitude(37.42, -122.08, 0.0);
MarkerOptions options = new MarkerOptions();
options.setPosition(position);

Marker marker = map.addMarker(options);

if (marker != null) {
    marker.setClickListener(
            () ->
                    new Handler(Looper.getMainLooper())
                            .post(
                                    () ->
                                            Toast.makeText(
                                                            context,
                                                            "Marker Clicked!",
                                                            Toast.LENGTH_SHORT)
                                                    .show()));
}
// [END maps_android_3d_marker_click_java]
```

> [!WARNING]
> Object click listeners (`marker.setClickListener`, `polyline.setClickListener`, `polygon.setClickListener`, `model.setClickListener`) do **NOT** execute on the UI thread. Always switch to the main thread (`new Handler(Looper.getMainLooper()).post(...)` or `runOnUiThread(...)`) when updating views or showing a `Toast`.

## 3. Adding Polylines (Basic & Styled)
Description: Polylines draw 2D or 3D extruded paths on the map using `PolylineOptions`.

### Basic Polyline
```java
// [START maps_android_3d_polyline_add_java]
List<LatLngAltitude> points =
        Arrays.asList(
                new LatLngAltitude(37.42, -122.08, 0.0),
                new LatLngAltitude(37.43, -122.09, 0.0),
                new LatLngAltitude(37.44, -122.08, 0.0));

PolylineOptions options = new PolylineOptions();
options.setPath(points);
options.setStrokeColor(Color.RED);
options.setStrokeWidth(10.0);
options.setAltitudeMode(AltitudeMode.CLAMP_TO_GROUND);

Polyline polyline = map.addPolyline(options);
// [END maps_android_3d_polyline_add_java]
```

### Extruded & Outlined Polyline
```java
// [START maps_android_3d_polyline_options_java]
List<LatLngAltitude> points =
        Arrays.asList(
                new LatLngAltitude(37.42, -122.08, 50.0),
                new LatLngAltitude(37.43, -122.09, 100.0));

PolylineOptions options = new PolylineOptions();
options.setPath(points);
options.setStrokeColor(0xFFFF00FF); // Magenta
options.setStrokeWidth(20.0);
options.setOuterColor(0xFF00FF00); // Green
options.setOuterWidth(2.0);
options.setAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);
options.setExtruded(true);
options.setGeodesic(true);
options.setDrawsOccludedSegments(true);

Polyline polyline = map.addPolyline(options);
// [END maps_android_3d_polyline_options_java]
```

## 4. Adding Polygons (Basic, Extruded & With Hole)
Description: Polygons represent 2D areas or 3D extruded volumes using `PolygonOptions`.

### Basic Polygon
```java
// [START maps_android_3d_polygon_add_java]
List<LatLngAltitude> points =
        Arrays.asList(
                new LatLngAltitude(37.42, -122.08, 0.0),
                new LatLngAltitude(37.42, -122.09, 0.0),
                new LatLngAltitude(37.43, -122.09, 0.0),
                new LatLngAltitude(37.43, -122.08, 0.0),
                new LatLngAltitude(37.42, -122.08, 0.0));

PolygonOptions options = new PolygonOptions();
options.setPath(points);
options.setFillColor(Color.RED);
options.setStrokeColor(Color.BLUE);
options.setStrokeWidth(5.0);
options.setAltitudeMode(AltitudeMode.CLAMP_TO_GROUND);

Polygon polygon = map.addPolygon(options);
// [END maps_android_3d_polygon_add_java]
```

### Extruded Polygon (3D Volume)
```java
// [START maps_android_3d_polygon_extruded_java]
List<LatLngAltitude> points =
        Arrays.asList(
                new LatLngAltitude(37.42, -122.08, 50.0),
                new LatLngAltitude(37.42, -122.09, 50.0),
                new LatLngAltitude(37.43, -122.09, 50.0),
                new LatLngAltitude(37.43, -122.08, 50.0),
                new LatLngAltitude(37.42, -122.08, 50.0));

PolygonOptions options = new PolygonOptions();
options.setPath(points);
options.setFillColor(0x88FF0000); // Semi-transparent red
options.setExtruded(true);
options.setGeodesic(true);
options.setAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);

Polygon polygon = map.addPolygon(options);
// [END maps_android_3d_polygon_extruded_java]
```

### Polygon with Interior Hole
```java
// [START maps_android_3d_polygon_hole_java]
List<LatLngAltitude> outerPoints =
        Arrays.asList(
                new LatLngAltitude(37.422, -122.084, 0.0),
                new LatLngAltitude(37.422, -122.086, 0.0),
                new LatLngAltitude(37.424, -122.086, 0.0),
                new LatLngAltitude(37.424, -122.084, 0.0),
                new LatLngAltitude(37.422, -122.084, 0.0));

List<LatLngAltitude> innerPoints =
        Arrays.asList(
                new LatLngAltitude(37.4225, -122.0845, 0.0),
                new LatLngAltitude(37.4225, -122.0855, 0.0),
                new LatLngAltitude(37.4235, -122.0855, 0.0),
                new LatLngAltitude(37.4235, -122.0845, 0.0),
                new LatLngAltitude(37.4225, -122.0845, 0.0));

PolygonOptions options = new PolygonOptions();
options.setPath(outerPoints);
options.setFillColor(Color.GREEN);
options.setAltitudeMode(AltitudeMode.CLAMP_TO_GROUND);

// Core logic: create a Hole object and set InnerPaths (Optional)
Hole innerHole = new Hole(innerPoints);
options.setInnerPaths(List.of(innerHole));

Polygon polygon = map.addPolygon(options);
// [END maps_android_3d_polygon_hole_java]
```

## 5. Adding a 3D Model (GLB)
Description: Loading and placing glTF/GLB assets on the map using `ModelOptions` (where `SAUCER_URL` is a `.glb` asset URL such as `"https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/UFO.glb"`).

```java
// [START maps_android_3d_model_add_java]
LatLngAltitude position = new LatLngAltitude(37.4220, -122.0841, 100.0);

ModelOptions options = new ModelOptions();
options.setPosition(position);
options.setUrl(SAUCER_URL);
options.setAltitudeMode(AltitudeMode.RELATIVE_TO_MESH);
options.setOrientation(new Orientation(0.0, 90.0, 0.0)); // heading, tilt, roll
options.setScale(new Vector3D(10.0, 10.0, 10.0));

Model model = map.addModel(options);
// [END maps_android_3d_model_add_java]
```

## 6. Adding a Popover (Info Window)
Description: 2D Android Views anchored to a marker or 3D location that automatically face the camera.

### Marker-Anchored Popover
```java
// [START maps_android_3d_popover_add_java]
// Create a marker first
Marker marker = map.addMarker(new MarkerOptions());
if (marker == null) return;

// Create a custom view for the popover
TextView textView = new TextView(context);
textView.setText(R.string.popover_hello);
textView.setPadding(16, 16, 16, 16);
textView.setBackgroundColor(Color.WHITE);

// Add popover anchored to the marker
PopoverOptions options = new PopoverOptions();
options.setContent(textView);
options.setPositionAnchor(marker);
options.setAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);

Popover popover = map.addPopover(options);

// You can show/hide it
if (popover != null) {
    popover.show();
}
// [END maps_android_3d_popover_add_java]
```

### Configured Popover (`setAutoCloseEnabled` / `setAutoPanEnabled`)
```java
// [START maps_android_3d_popover_options_java]
// Create a marker at an interesting location (e.g., Golden Gate Bridge)
MarkerOptions markerOptions = new MarkerOptions();
LatLngAltitude position = new LatLngAltitude(37.8199, -122.4783, 0.0);
markerOptions.setPosition(position);
markerOptions.setLabel("Golden Gate Bridge");
Marker marker = map.addMarker(markerOptions);
if (marker == null) return;

TextView textView = new TextView(context);
textView.setText(R.string.popover_info);
textView.setPadding(16, 16, 16, 16);
textView.setBackgroundColor(Color.WHITE);

PopoverOptions options = new PopoverOptions();
options.setContent(textView);
options.setPositionAnchor(marker);
options.setAutoCloseEnabled(true); // Close when clicking elsewhere
options.setAutoPanEnabled(false); // Do not pan to popover

Popover popover = map.addPopover(options);
if (popover != null) {
    popover.show();
}
// [END maps_android_3d_popover_options_java]
```

## 7. Camera Animation, Events & Restrictions
Description: Animating the camera (`flyCameraTo`, `flyCameraAround`, `stopCameraAnimation`), listening to camera movement (`setCameraChangedListener`), and restricting camera bounds (`setCameraRestriction`).

### Fly Camera To Position
```java
// [START maps_android_3d_camera_fly_to_java]
LatLngAltitude center = new LatLngAltitude(38.743829, -109.499512, 1460.37);
// Create a target camera:
Camera targetCamera = new Camera(center, 338.52, 76.16, 0.0, 191.71);

// FlyToOptions constructor: endCamera, durationInMillis
FlyToOptions options = new FlyToOptions(targetCamera, 5000L);

map.flyCameraTo(options);
// [END maps_android_3d_camera_fly_to_java]
```

### Orbit Camera (`flyCameraAround`)
```java
// [START maps_android_3d_camera_fly_around_java]
LatLngAltitude center = new LatLngAltitude(38.743502, -109.499374, 1467.0);
// Create a target camera:
Camera targetCamera = new Camera(center, 349.6, 58.1, 0.0, 138.2);

// Orbit around the target
// FlyAroundOptions constructor: center, durationInMillis, rounds
FlyAroundOptions options = new FlyAroundOptions(targetCamera, 6000L, 2.0);

// Although not completely necessary, the experience will be usually be better by
// waiting for the camera to steady before starting the flyCameraAround
map.setCamera(targetCamera);

map.setOnMapSteadyListener(
        isSteady -> {
            if (isSteady) {
                map.setOnMapSteadyListener(null); // Cleanup
                map.flyCameraAround(options);
            }
        });
// [END maps_android_3d_camera_fly_around_java]
```

### Stop Camera Animation
```java
// [START maps_android_3d_camera_stop_java]
LatLngAltitude center = new LatLngAltitude(38.743502, -109.499374, 1467.0);
Camera targetCamera = new Camera(center, 349.6, 58.1, 0.0, 138.2);

// 1. Start a perpetual flyAround animation so we have something to stop
map.flyCameraAround(new FlyAroundOptions(targetCamera, 30000L, 10.0));

// 2. Schedule the stop command after 2 seconds
new Handler(Looper.getMainLooper()).postDelayed(map::stopCameraAnimation, 2000);
// [END maps_android_3d_camera_stop_java]
```

### Listen to Camera Change Events
```java
// [START maps_android_3d_camera_events_java]
final long[] lastLogTime = {0L};
map.setCameraChangedListener(
        camera -> {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastLogTime[0] > 500) { // Limit to 1 log per 500ms
                lastLogTime[0] = currentTime;
                Log.d(
                        "Maps3D",
                        "Camera State: "
                                + "Center: "
                                + camera.getCenter()
                                + ", Heading: "
                                + camera.getHeading()
                                + ", Tilt: "
                                + camera.getTilt()
                                + ", Roll: "
                                + camera.getRoll()
                                + ", Range: "
                                + camera.getRange());
            }
        });

// Detach after 5 seconds to prevent log spam
new Handler(Looper.getMainLooper())
        .postDelayed(
                () -> {
                    map.setCameraChangedListener(null);
                },
                5000);
// [END maps_android_3d_camera_events_java]
```

### Listen to Steady State
```java
// [START maps_android_3d_camera_steady_java]
map.setOnMapSteadyListener(
        isSceneSteady -> {
            Log.d("Maps3D", "Map Is Steady: " + isSceneSteady);
        });
// [END maps_android_3d_camera_steady_java]
```

### Camera Restrictions (`CameraRestriction` & `LatLngBounds`)
```java
// [START maps_android_3d_camera_restriction_java]
LatLngBounds nycBounds = new LatLngBounds(40.856492, -73.802409, 40.685630, -74.050304);

CameraRestriction restriction =
        new CameraRestriction(500.0, 10000.0, 0.0, 360.0, 0.0, 90.0, nycBounds);

map.setCameraRestriction(restriction);
// [END maps_android_3d_camera_restriction_java]
```

## 8. Handling Map & Place Click Events (`Map3DClickEvent`)
Description: `OnMap3DClickListener.onMap3DClick` receives a `Map3DClickEvent` (`event.getLocation()` and `event.getPlaceId()`) and returns a `boolean` (`true` if consumed, `false` otherwise).

```java
// [START maps_android_3d_place_click_java]
map.setMap3DClickListener(
        new OnMap3DClickListener() {
            @Override
            public boolean onMap3DClick(@NonNull Map3DClickEvent event) {
                String placeId = event.getPlaceId();
                if (placeId != null) {
                    // Handle place click - Show a Toast on the UI thread
                    new Handler(Looper.getMainLooper())
                            .post(
                                    () -> {
                                        Toast.makeText(
                                                        context,
                                                        "Clicked Place ID: " + placeId,
                                                        Toast.LENGTH_SHORT)
                                                .show();
                                    });
                    return true;
                }
                return false;
            }
        });
// [END maps_android_3d_place_click_java]
```

## 9. Map Localization (`LocaleOptions` & `setLocale`)
Description: Dynamically localizing 3D map labels to a specific language and region or resetting to the device default locale.

```java
// [START maps_android_3d_localization_chinese_java]
LocaleOptions localeOptions = new LocaleOptions();
localeOptions.setLanguage("zh");
localeOptions.setRegion("CN");
map.setLocale(localeOptions);
// [END maps_android_3d_localization_chinese_java]
```

```java
// [START maps_android_3d_localization_default_java]
Locale defaultLocale = Locale.getDefault();
LocaleOptions defaultLocaleOptions = new LocaleOptions();
defaultLocaleOptions.setLanguage(defaultLocale.getLanguage());
defaultLocaleOptions.setRegion(defaultLocale.getCountry());
map.setLocale(defaultLocaleOptions);
// [END maps_android_3d_localization_default_java]
```

## 10. Built-in 3D UI Controls (`UiControlSettings`)
Description: Configuring or hiding the built-in 3D camera UI controls (`ZoomControl`, `TiltControl`, `RotateControl`, `PanVerticalControl`, `PanHorizontalControl`, `CompassControl`, `Spacer`) across screen edges (`TRAILING`, `LEADING`, `TOP`, `BOTTOM`).

### Configure Trailing & Leading Controls
```java
// [START maps_android_3d_ui_controls_placement_java]
UiControlSettings uiSettings = map.getUiControlSettings();
if (uiSettings != null) {
    List<Map3DControl> trailingControls =
            Arrays.asList(
                    new ZoomControl(ControlOrientation.VERTICAL),
                    new TiltControl(ControlOrientation.VERTICAL),
                    new RotateControl(ControlOrientation.VERTICAL));
    uiSettings.setUiControlsPlacement(trailingControls, ControlPlacement.TRAILING);

    List<Map3DControl> leadingControls =
            Arrays.asList(
                    new PanVerticalControl(ControlOrientation.VERTICAL),
                    new PanHorizontalControl(ControlOrientation.VERTICAL),
                    new CompassControl());
    uiSettings.setUiControlsPlacement(leadingControls, ControlPlacement.LEADING);
}
// [END maps_android_3d_ui_controls_placement_java]
```

### Horizontal Controls with Spacers on Top & Bottom Edges
```java
// [START maps_android_3d_ui_controls_orientation_java]
UiControlSettings uiSettings = map.getUiControlSettings();
if (uiSettings != null) {
    List<Map3DControl> topControls =
            Arrays.asList(
                    new CompassControl(),
                    new Spacer(),
                    new RotateControl(ControlOrientation.HORIZONTAL),
                    new TiltControl(ControlOrientation.HORIZONTAL));
    uiSettings.setUiControlsPlacement(topControls, ControlPlacement.TOP);

    List<Map3DControl> bottomControls =
            Arrays.asList(
                    new ZoomControl(ControlOrientation.HORIZONTAL),
                    new Spacer(),
                    new PanHorizontalControl(ControlOrientation.HORIZONTAL),
                    new PanVerticalControl(ControlOrientation.HORIZONTAL));
    uiSettings.setUiControlsPlacement(bottomControls, ControlPlacement.BOTTOM);
}
// [END maps_android_3d_ui_controls_orientation_java]
```

### Hide All UI Controls
```java
// [START maps_android_3d_ui_controls_hide_java]
UiControlSettings uiSettings = map.getUiControlSettings();
if (uiSettings != null) {
    uiSettings.setUiControlsPlacement(
            Collections.emptyList(), ControlPlacement.TRAILING);
    uiSettings.setUiControlsPlacement(
            Collections.emptyList(), ControlPlacement.LEADING);
    uiSettings.setUiControlsPlacement(
            Collections.emptyList(), ControlPlacement.TOP);
    uiSettings.setUiControlsPlacement(
            Collections.emptyList(), ControlPlacement.BOTTOM);
}
// [END maps_android_3d_ui_controls_hide_java]
```
