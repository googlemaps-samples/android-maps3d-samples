# Java Interop & Construction Guide (`play-services-maps3d:16.0.1`)

> **LOAD THIS FILE ONLY WHEN WRITING OR MODIFYING JAVA (`.java`) FILES.**

## 1. Critical Java Construction Rule in SDK `16.0.1`

In `com.google.android.gms:play-services-maps3d:16.0.1`:
- **ONLY `PinConfiguration` exposes a `.builder()` / `new PinConfiguration.Builder()` API.**
- **All other model classes do NOT have a `.Builder` class** — instantiate them with constructors and configure them via setter methods:
  - **Immutable/Parameterized Constructors:** `LatLngAltitude`, `Camera`, `FlyToOptions`, `FlyAroundOptions`, `CameraRestriction`, `LatLngBounds`, `Hole`, `Vector3D`, `Orientation`, `ImageView`.
  - **Static Factories:** `Map3DInitConfig.create(...)` (non-null primitive `double`s and `String`s; only `bounds` and `mapId` are nullable), `Glyph.fromColor(int)`, `Glyph.fromText(String)`, `Glyph.fromCircle()`.
  - **No-Arg Constructor + `void` Setters:** `MarkerOptions`, `MarkerViewOptions`, `ModelOptions`, `PolygonOptions`, `PolylineOptions`, `LocaleOptions`.
  - **No-Arg Constructor + Fluent Setters (return `this`):** `PopoverOptions`, `PopoverStyle`, `PopoverShadow` (note: `PopoverStyle`/`PopoverShadow` numeric args are `float`, NOT `double`).

---

## 2. Verified Java `16.0.1` Code Matrix

### Camera, Animations, Restrictions & Programmatic `Map3DInitConfig`

```java
LatLngAltitude position = new LatLngAltitude(37.7952, -122.4028, 120.0);

Camera camera = new Camera(
    position,
    /* heading= */ 45.0,
    /* tilt= */ 65.0,
    /* roll= */ 0.0,
    /* range= */ 1500.0,
    /* altitudeMode= */ AltitudeMode.RELATIVE_TO_GROUND
);

googleMap3D.flyCameraTo(new FlyToOptions(camera, /* durationInMillis= */ 3500L));
googleMap3D.flyCameraAround(new FlyAroundOptions(camera, /* durationInMillis= */ 8000L, /* repeatCount= */ 1.0));

LatLngBounds bounds = new LatLngBounds(37.85, -122.35, 37.70, -122.52);
CameraRestriction restriction = new CameraRestriction(
    /* minAltitude= */ 50.0, /* maxAltitude= */ 10000.0,
    /* minHeading= */ 0.0, /* maxHeading= */ 360.0,
    /* minTilt= */ 15.0, /* maxTilt= */ 80.0,
    /* bounds= */ bounds
);
googleMap3D.setCameraRestriction(restriction);

// Map3DInitConfig.create(...) — numeric args are primitive doubles, language/region are non-null Strings
Map3DInitConfig initConfig = Map3DInitConfig.create(
    /* centerLat= */ 37.7952, /* centerLng= */ -122.4028, /* centerAlt= */ 0.0,
    /* heading= */ 30.0, /* tilt= */ 65.0, /* roll= */ 0.0, /* range= */ 1800.0,
    /* minAltitude= */ 0.0, /* maxAltitude= */ 1000000.0,
    /* minHeading= */ 0.0, /* maxHeading= */ 360.0,
    /* minTilt= */ 0.0, /* maxTilt= */ 90.0,
    /* bounds= */ null,
    /* mapMode= */ Map3DMode.HYBRID,
    /* mapId= */ null,
    /* language= */ "en", /* region= */ "US",
    /* mapColorScheme= */ MapColorScheme.FOLLOW_SYSTEM
);
Map3DView programmaticMapView = new Map3DView(context, initConfig);

LocaleOptions localeOptions = new LocaleOptions();
localeOptions.setLanguage("ja");
localeOptions.setRegion("JP");
googleMap3D.setLocale(localeOptions);
```

### Markers, Pins, MarkerViews, Popovers, Models, Polygons & Polylines

```java
Glyph glyph = Glyph.fromText("SF");
glyph.setColor(Color.WHITE);

PinConfiguration pinConfig = PinConfiguration.builder()
    .setBackgroundColor(Color.parseColor("#EA4335"))
    .setBorderColor(Color.WHITE)
    .setScale(1.2f)
    .setGlyph(glyph)
    .build();

MarkerOptions markerOptions = new MarkerOptions();
markerOptions.setId("sf_ferry_building");
markerOptions.setPosition(new LatLngAltitude(37.7955, -122.3937, 15.0));
markerOptions.setLabel("Ferry Building");
markerOptions.setAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);
markerOptions.setExtruded(true);
markerOptions.setDrawnWhenOccluded(true);
markerOptions.setSizePreserved(true);
markerOptions.setCollisionBehavior(CollisionBehavior.REQUIRED_AND_HIDES_OPTIONAL);
markerOptions.setStyle(pinConfig); // Or new com.google.android.gms.maps3d.model.ImageView(R.drawable.ic_pin)
Marker marker = googleMap3D.addMarker(markerOptions);

MarkerViewOptions markerViewOptions = new MarkerViewOptions();
markerViewOptions.setId("badge_1");
markerViewOptions.setPosition(new LatLngAltitude(37.7955, -122.3937, 25.0));
markerViewOptions.setContent(customAndroidView);
markerViewOptions.setAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);
MarkerView markerView = googleMap3D.addMarkerView(markerViewOptions);

// PopoverStyle & PopoverShadow use float parameters; Popover APIs are strictly @UiThread
PopoverShadow shadow = new PopoverShadow().setColor(Color.argb(80, 0, 0, 0)).setOffsetX(0.0f).setOffsetY(4.0f).setRadius(12.0f);
PopoverStyle style = new PopoverStyle().setBackgroundColor(Color.WHITE).setBorderRadius(16.0f).setPadding(12.0f).setShadow(shadow);
PopoverOptions popoverOptions = new PopoverOptions()
    .setContent(infoCardView)
    .setPositionAnchor(marker) // Anchorable: Marker, MarkerView, or LatLngAltitude
    .setAnchorOffset(new Point(0, -12))
    .setAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND)
    .setAutoCloseEnabled(true)
    .setAutoPanEnabled(true)
    .setPopoverStyle(style);
runOnUiThread(() -> {
    Popover popover = googleMap3D.addPopover(popoverOptions);
    popover.show();
});

ModelOptions modelOptions = new ModelOptions();
modelOptions.setId("airplane");
modelOptions.setUrl("https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/Airplane.glb");
modelOptions.setPosition(new LatLngAltitude(37.7952, -122.4028, 250.0));
modelOptions.setAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);
modelOptions.setScale(new Vector3D(5.0, 5.0, 5.0));
modelOptions.setOrientation(new Orientation(90.0, 270.0, 0.0));
Model model = googleMap3D.addModel(modelOptions);

PolygonOptions polygonOptions = new PolygonOptions();
polygonOptions.setPath(outerRing);
polygonOptions.setInnerPaths(Collections.singletonList(new Hole(innerHoleVertices)));
polygonOptions.setFillColor(Color.argb(120, 66, 133, 244));
polygonOptions.setStrokeColor(Color.parseColor("#4285F4"));
polygonOptions.setStrokeWidth(4.0);
polygonOptions.setAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);
polygonOptions.setExtruded(true);
polygonOptions.setGeodesic(false);
polygonOptions.setDrawsOccludedSegments(true);
Polygon polygon = googleMap3D.addPolygon(polygonOptions);

PolylineOptions polylineOptions = new PolylineOptions();
polylineOptions.setPath(routePoints);
polylineOptions.setStrokeColor(Color.parseColor("#4285F4"));
polylineOptions.setStrokeWidth(8.0);
polylineOptions.setOuterColor(Color.WHITE);
polylineOptions.setOuterWidth(0.25);
polylineOptions.setAltitudeMode(AltitudeMode.CLAMP_TO_GROUND);
polylineOptions.setGeodesic(true);
polylineOptions.setDrawsOccludedSegments(true);
Polyline polyline = googleMap3D.addPolyline(polylineOptions);
```

---

## 3. Complete Java `MapActivity.java` & `TrackedMap3D.java` Template

```java
public class MapActivity extends AppCompatActivity implements OnMap3DViewReadyCallback {
    private Map3DView map3DView;
    private TrackedMap3D trackedMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        map3DView = findViewById(R.id.map3dView);
        map3DView.onCreate(savedInstanceState);
        map3DView.getMap3DViewAsync(this);
    }

    @Override
    public void onMap3DViewReady(@NonNull GoogleMap3D googleMap3D) {
        trackedMap = new TrackedMap3D(googleMap3D);
        googleMap3D.setMapMode(Map3DMode.HYBRID);
        // Main-thread click listener (return false for default Places popover, true to suppress)
        googleMap3D.setMap3DClickListener(event -> {
            LatLngAltitude loc = event.getLocation();
            String placeId = event.getPlaceId();
            return false;
        });
    }

    @Override public void onError(@NonNull Exception error) { Log.e("MapActivity", "Init error", error); }
    @Override protected void onStart() { super.onStart(); map3DView.onStart(); }
    @Override protected void onResume() { super.onResume(); map3DView.onResume(); }
    @Override protected void onPause() { map3DView.onPause(); super.onPause(); }
    @Override protected void onStop() { map3DView.onStop(); super.onStop(); }
    @Override protected void onDestroy() {
        if (trackedMap != null) trackedMap.clearAll();
        map3DView.onDestroy();
        super.onDestroy();
    }
    @Override public void onLowMemory() { super.onLowMemory(); map3DView.onLowMemory(); }
    @Override protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        map3DView.onSaveInstanceState(outState);
    }
}

public class TrackedMap3D {
    private final GoogleMap3D delegate;
    private final List<Object> items = new ArrayList<>();

    public TrackedMap3D(GoogleMap3D delegate) { this.delegate = delegate; }
    public Marker addMarker(MarkerOptions o) { Marker m = delegate.addMarker(o); if (m != null) items.add(m); return m; }
    public MarkerView addMarkerView(MarkerViewOptions o) { MarkerView mv = delegate.addMarkerView(o); if (mv != null) items.add(mv); return mv; }
    public Polyline addPolyline(PolylineOptions o) { Polyline p = delegate.addPolyline(o); items.add(p); return p; }
    public Polygon addPolygon(PolygonOptions o) { Polygon p = delegate.addPolygon(o); items.add(p); return p; }
    public Model addModel(ModelOptions o) { Model m = delegate.addModel(o); items.add(m); return m; }
    public Popover addPopover(PopoverOptions o) { Popover p = delegate.addPopover(o); items.add(p); return p; }

    public void clearAll() {
        delegate.stopCameraAnimation();
        delegate.setCameraAnimationEndListener(null);
        delegate.setCameraChangedListener(null);
        delegate.setMap3DClickListener(null);
        for (Object item : items) {
            if (item instanceof Marker) ((Marker) item).remove();
            else if (item instanceof MarkerView) ((MarkerView) item).remove();
            else if (item instanceof Polyline) ((Polyline) item).remove();
            else if (item instanceof Polygon) ((Polygon) item).remove();
            else if (item instanceof Model) ((Model) item).remove();
            else if (item instanceof Popover) ((Popover) item).remove();
        }
        items.clear();
    }
}
```
