# Common Operations Catalog (Java)

This catalog provides short, reference snippets for common operations when using the Google Maps 3D SDK with Java and XML Views.

## 1. Adding a Marker
Description: Markers point out points of interest. You can set position, altitude mode, and labels.

```java
Marker marker = map.addMarker(new MarkerOptions()
    .position(new LatLngAltitude(37.7749, -122.4194, 0.0))
    .altitudeMode(AltitudeMode.RELATIVE_TO_GROUND)
    .label("San Francisco"));
```

## 2. Adding a Polyline
Description: Polylines are lines on the map. Use matching IDs to update them instead of re-adding.

```java
Polyline polyline = map.addPolyline(new PolylineOptions()
    .add(new LatLngAltitude(37.77, -122.41, 0.0))
    .add(new LatLngAltitude(37.78, -122.42, 0.0))
    .color(Color.RED)
    .width(5f));

// To update:
polyline.setPoints(newPointsList);
```

## 3. Adding a Polygon
Description: Polygons represent areas.

```java
Polygon polygon = map.addPolygon(new PolygonOptions()
    .add(new LatLngAltitude(37.77, -122.41, 0.0))
    .add(new LatLngAltitude(37.78, -122.41, 0.0))
    .add(new LatLngAltitude(37.78, -122.42, 0.0))
    .fillColor(Color.argb(128, 255, 0, 0)));
```

## 4. Camera Animation
Description: Animating the camera to a new position or flying around a center.

```java
// Fly To
map.flyTo(new FlyToOptions()
    .endCamera(targetCamera)
    .durationInMillis(3000));

// Fly Around
map.flyAround(new FlyAroundOptions()
    .center(currentCamera)
    .durationInMillis(5000)
    .rounds(1.0));
```

## 5. Handling Map Click Events
Description: Listening for clicks on the map.

```java
map.addOnMapClickListener(latLngAltitude -> {
    // Handle click at location
    double lat = latLngAltitude.getLatitude();
    double lng = latLngAltitude.getLongitude();
});
```

## 6. Object Animation (Moving Objects)
Description: Animating the position of an object (like a Marker or Polygon) using Android `ValueAnimator`.

```java
ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
animator.setDuration(1000);
animator.addUpdateListener(animation -> {
    float fraction = (float) animation.getAnimatedValue();
    double newLat = startLat + (endLat - startLat) * fraction;
    double newLng = startLng + (endLng - startLng) * fraction;
    marker.setPosition(new LatLngAltitude(newLat, newLng, 0.0));
});
animator.start();
```

## 7. Object Click Listeners
Description: Unlike the 2D SDK, click listeners are set directly on the object instances (Marker, Polyline, Polygon, Model) rather than on the map.

```java
// Marker
marker.setClickListener(() -> {
    // Handle marker click
    // NOTE: This is not on the UI thread!
});

// Polyline
polyline.setClickListener(() -> {
    // Handle polyline click
    // NOTE: This is not on the UI thread!
});
```

> [!WARNING]
> The `setClickListener` callback does **NOT** execute on the UI thread. If you need to update views or perform UI operations, you must switch to the main thread (e.g., using `Handler(Looper.getMainLooper()).post(...)` or `runOnUiThread(...)` if in an Activity).


## 8. Stopping & Waiting for Camera Animations
Description: Controlling camera animations and waiting for their completion.

### Stopping Animations
```java
// Halts any in-progress camera movement
map.stopCameraAnimation();

// Clear the listener as well if needed
map.setCameraAnimationEndListener(null);
```

### Waiting for Completion (Callback)
```java
map.setCameraAnimationEndListener(() -> {
    // Trigger next action after animation ends
});
map.flyTo(...);
```

## 9. Waiting for Scene to Settle (Steady State)
Description: The 3D SDK can notify you when the scene has fully rendered (terrain and mesh data loaded). This is crucial for high-fidelity snapshots or visual synchronization.

```java
map.setOnMapSteadyListener(isSteady -> {
    if (isSteady) {
        // Scene is settled and fully rendered
        map.setOnMapSteadyListener(null); // Clear if one-shot
    }
});
```

## 10. Adding a 3D Model
Description: Loading and placing glTF assets on the map.

```java
Model model = map.addModel(new ModelOptions()
    .position(new LatLngAltitude(37.7749, -122.4194, 0.0))
    .url("https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/Airplane.glb")
    .scale(new Vector3D(1.0, 1.0, 1.0))
    .altitudeMode(AltitudeMode.RELATIVE_TO_GROUND));
```

## 11. Adding a Popover (Info Window)
Description: 2D views that stick to a 3D location and always face the camera.

```java
TextView textView = new TextView(context);
textView.setText("Hello World");
textView.setBackgroundColor(Color.WHITE);

Popover popover = map.addPopover(new PopoverOptions()
    .positionAnchor(new LatLngAltitude(37.7749, -122.4194, 10.0))
    .content(textView)
    .altitudeMode(AltitudeMode.RELATIVE_TO_MESH)
    .autoCloseEnabled(true));

popover.show();
```

## 12. Extruded Polygons (3D Volumes)
Description: Turning flat footprints into 3D volumes by duplicating vertices at height and stitching sides.

```java
// Helper to extrude (see full Codelab for complete implementation)
List<List<LatLngAltitude>> faces = extrude(basePoints, 35.0);

for (List<LatLngAltitude> face : faces) {
    map.addPolygon(new PolygonOptions()
        .addAll(face)
        .fillColor(Color.argb(128, 255, 215, 0))
        .altitudeMode(AltitudeMode.ABSOLUTE));
}
```

## 13. Camera State Logger (Debug Helper)
Description: A utility to log the current camera state to Logcat on every movement, helping you design 3D views.

```java
map.addOnCameraMoveListener(() -> {
    Camera camera = map.getCamera();
    Log.d("CameraLogger", String.format(
        "Camera State:\nLat: %f\nLng: %f\nAlt: %f\nHeading: %f\nTilt: %f\nRange: %f",
        camera.getCenter().getLatitude(),
        camera.getCenter().getLongitude(),
        camera.getCenter().getAltitude(),
        camera.getHeading(),
        camera.getTilt(),
        camera.getRange()
    ));
});
```



