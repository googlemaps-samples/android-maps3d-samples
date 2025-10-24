## Migration Guide: Updating to v0.1.0 of Maps3D SDK

Welcome to the latest version of the Maps3D SDK! This release focuses on improving API clarity and introducing powerful new ways to make your maps interactive. This guide will walk you through the necessary changes to update your application in both Kotlin and Java.

### Summary of Changes
*   **Breaking Change:** Renamed properties for setting coordinates on Polygons and Polylines for better consistency.
*   **New Feature:** Added click listeners for the map and most map objects, including Markers, Polygons, Polylines, and Models.

---

### 1. Breaking API Changes: Renamed Path Properties

To improve API consistency and clarity, the properties used to define the vertices of Polygons and Polylines have been renamed.

#### **Polyline Path**

The `coordinates` property/method in `PolylineOptions` has been renamed to `path`.

<details>
<summary>Kotlin</summary>

**Before:**
```kotlin
// Old property
val polyline = googleMap3D.addPolyline(polylineOptions {
    coordinates = myListOfLatLngAltitudes
    strokeColor = Color.RED
})
```

**After:**
```kotlin
// New property
val polyline = googleMap3D.addPolyline(polylineOptions {
    path = myListOfLatLngAltitudes // Changed from 'coordinates'
    strokeColor = Color.RED
})
```
</details>

<details>
<summary>Java</summary>

**Before:**
```java
// Old method
PolylineOptions options = new PolylineOptions();
options.setCoordinates(myListOfLatLngAltitudes);
```

**After:**
```java
// New method
PolylineOptions options = new PolylineOptions();
options.setPath(myListOfLatLngAltitudes); // Changed from 'setCoordinates'
```
</details>

#### **Polygon Path and Holes**

In `PolygonOptions`, `outerCoordinates` has been renamed to `path`, and `innerCoordinates` has been renamed to `innerPaths`.

<details>
<summary>Kotlin</summary>

**Before:**
```kotlin
// Old properties
val polygon = googleMap3D.addPolygon(polygonOptions {
    outerCoordinates = myOuterPath
    innerCoordinates = listOf(myHolePath)
    fillColor = Color.BLUE
})
```

**After:**
```kotlin
// New properties
val polygon = googleMap3D.addPolygon(polygonOptions {
    path = myOuterPath // Changed from 'outerCoordinates'
    innerPaths = listOf(myHolePath) // Changed from 'innerCoordinates'
    fillColor = Color.BLUE
})
```
</details>

<details>
<summary>Java</summary>

**Before:**
```java
// Old methods
PolygonOptions options = new PolygonOptions();
options.setOuterCoordinates(myOuterPath);
options.setInnerCoordinates(Collections.singletonList(myHolePath));
```

**After:**
```java
// New methods
PolygonOptions options = new PolygonOptions();
options.setPath(myOuterPath); // Changed from 'setOuterCoordinates'
options.setInnerPaths(Collections.singletonList(myHolePath)); // Changed from 'setInnerCoordinates'
```
</details>

---

### 2. New Feature: Interactive Map Objects with Click Listeners

You can now add click listeners to make your map objects interactive. A `setClickListener` method is now available on `Marker`, `Polygon`, `Polyline`, and `Model` objects. Additionally, you can now listen for clicks on the map itself.

#### **Listening for Clicks on a Marker**

You can now respond directly when a user taps on a marker.

<details>
<summary>Kotlin</summary>

**Example:**
```kotlin
// Add a marker to the map
val marker = googleMap3D.addMarker(markerOptions {
    id = "my_marker"
    position = latLngAltitude {
        latitude = 52.51974795
        longitude = 13.4068735
        altitude = 150.0
    }
    label = "Reichstag Building"
})

// Set a click listener on the marker instance
marker?.setClickListener {
    // This block is executed when the marker is clicked
    lifecycleScope.launch(Dispatchers.Main) {
        Toast.makeText(this, "Clicked on ${it.label}", Toast.LENGTH_SHORT).show()
    }
}
```
</details>

<details>
<summary>Java</summary>

**Example:**
```java
// Add a marker to the map
MarkerOptions markerOptions = new MarkerOptions();
markerOptions.setPosition(new LatLngAltitude(52.51974795, 13.4068735, 150.0));
markerOptions.setLabel("Reichstag Building");

com.google.android.gms.maps3d.model.Marker marker = googleMap3D.addMarker(markerOptions);

// Set a click listener on the marker instance
marker.setClickListener(m -> {
    // This block is executed when the marker is clicked
    runOnUiThread(() -> {
        Toast.makeText(this, "Clicked on marker: " + m.getLabel(), Toast.LENGTH_SHORT).show();
    });
});
```
</details>

#### **Listening for Clicks on the Map**

You can now detect when a user taps anywhere on the map. This is useful for features like adding a new marker at the tapped location. The listener provides the location and an optional Place ID if a known place was tapped.

<details>
<summary>Kotlin</summary>

**Example:**
```kotlin
// Set a click listener on the GoogleMap3D object
googleMap3D.setMap3DClickListener { location, placeId ->
    val message = if (placeId != null) {
        "Clicked on place with ID: $placeId"
    } else {
        "Clicked on location: $location"
    }
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
```
</details>

<details>
<summary>Java</summary>

**Example:**
```java
// Set a click listener on the GoogleMap3D object
googleMap3D.setMap3DClickListener((location, placeId) -> {
    String message;
    if (placeId != null) {
        message = "Clicked on place with ID: " + placeId;
    } else {
        message = "Clicked on location: " + location;
    }
    runOnUiThread(() -> {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    });
});
```
</details>

The same `setClickListener` pattern applies to `Polygon`, `Polyline`, and `Model` objects.
