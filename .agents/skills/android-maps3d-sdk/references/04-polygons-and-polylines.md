# 04. 3D Polylines, Extruded Polygons, Holes & `AltitudeMode` Matrix

## 1. 3D Polylines (`addPolyline`)

`googleMap3D.addPolyline(...)` returns a non-null `Polyline` handle whose properties (`path`, `strokeColor`, `strokeWidth`, `outerColor`, `outerWidth`, `drawsOccludedSegments`, `geodesic`, `zIndex`) can be mutated at runtime.
- **Note on Kotlin DSL property names:** `PolygonOptions` and `PolylineOptions` expose **`extruded`** and **`geodesic`** (NOT `isExtruded` / `isGeodesic`).

```kotlin
import android.graphics.Color
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.gms.maps3d.model.polylineOptions

val routePoints = listOf(
    latLngAltitude { latitude = 37.7955; longitude = -122.3937; altitude = 2.0 },
    latLngAltitude { latitude = 37.7989; longitude = -122.4014; altitude = 2.0 },
    latLngAltitude { latitude = 37.8024; longitude = -122.4058; altitude = 2.0 }
)

val routePolyline = googleMap3D.addPolyline(
    polylineOptions {
        path = routePoints
        strokeColor = Color.parseColor("#4285F4")
        strokeWidth = 10.0
        outerColor = Color.WHITE
        outerWidth = 0.25 // Ratio [0.0..1.0] relative to strokeWidth
        altitudeMode = AltitudeMode.RELATIVE_TO_GROUND
        extruded = false
        geodesic = true
        drawsOccludedSegments = true
        zIndex = 5
    }
)
routePolyline.setClickListener { /* handle polyline tap */ }
```

---

## 2. Extruded 3D Polygons & Cutout Holes (`addPolygon` & `hole`)

- **Closed Ring Requirement:** Repeat the first vertex at the end of `path` and inside each `hole { vertices = ... }`.
- **Important:** `Hole`'s Kotlin DSL property is **`vertices`** (NOT `path`).

```kotlin
import android.graphics.Color
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.hole
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.gms.maps3d.model.polygonOptions

val outerRing = listOf(
    latLngAltitude { latitude = 37.7890; longitude = -122.4030; altitude = 120.0 },
    latLngAltitude { latitude = 37.7890; longitude = -122.3990; altitude = 120.0 },
    latLngAltitude { latitude = 37.7860; longitude = -122.3990; altitude = 120.0 },
    latLngAltitude { latitude = 37.7860; longitude = -122.4030; altitude = 120.0 },
    latLngAltitude { latitude = 37.7890; longitude = -122.4030; altitude = 120.0 }
)

val courtyardHole = hole {
    vertices = listOf(
        latLngAltitude { latitude = 37.7882; longitude = -122.4020; altitude = 120.0 },
        latLngAltitude { latitude = 37.7882; longitude = -122.4000; altitude = 120.0 },
        latLngAltitude { latitude = 37.7868; longitude = -122.4000; altitude = 120.0 },
        latLngAltitude { latitude = 37.7868; longitude = -122.4020; altitude = 120.0 },
        latLngAltitude { latitude = 37.7882; longitude = -122.4020; altitude = 120.0 }
    )
}

val extrudedZone = googleMap3D.addPolygon(
    polygonOptions {
        path = outerRing
        innerPaths = listOf(courtyardHole)
        fillColor = Color.argb(110, 52, 168, 83)
        strokeColor = Color.parseColor("#34A853")
        strokeWidth = 4.0
        altitudeMode = AltitudeMode.RELATIVE_TO_GROUND
        extruded = true
        geodesic = false
        drawsOccludedSegments = true
        zIndex = 2
    }
)
extrudedZone.setClickListener { /* handle polygon tap */ }
```

---

## 3. `AltitudeMode` vs. `extruded` Compatibility Matrix

| `AltitudeMode` | Vertex `altitude` Meaning | `extruded = true` Behavior | Recommended Use Case |
| :--- | :--- | :--- | :--- |
| `CLAMP_TO_GROUND` (`2`) | Ignored (draped directly on terrain) | No vertical wall (ground-clamped) | Surface roads, hiking trails, flat land parcels |
| `RELATIVE_TO_GROUND` (`3`) | Meters above terrain surface | Renders vertical walls/curtains down to terrain | Building massings, airspaces, elevated routes (`+0.5m` bias prevents road z-fighting) |
| `ABSOLUTE` (`1`) | Meters above EGM96/WGS84 mean sea level | Renders vertical walls down to terrain | Flight paths, elevation profiles with real MSL altitudes |
| `RELATIVE_TO_MESH` (`4`) | Meters above 3D buildings/trees mesh | Renders vertical walls down to 3D mesh | Rooftop zones, drone corridors above urban skylines |

---

## 4. Compact DSL & SDK Signature Block (`play-services-maps3d:16.0.1`)

```kotlin
// Top-level Kotlin DSL in com.google.android.gms.maps3d.model.DslKt:
// polylineOptions(PolylineOptions.() -> Unit) -> id: String; path: List<LatLngAltitude>; @AltitudeMode altitudeMode: Int; @ColorInt strokeColor: Int; strokeWidth: Double; @ColorInt outerColor: Int; outerWidth: Double; extruded: Boolean; geodesic: Boolean; drawsOccludedSegments: Boolean; zIndex: Int
// polygonOptions(PolygonOptions.() -> Unit) -> id: String; path: List<LatLngAltitude>; innerPaths: List<Hole>; @AltitudeMode altitudeMode: Int; @ColorInt fillColor: Int; @ColorInt strokeColor: Int; strokeWidth: Double; extruded: Boolean; geodesic: Boolean; drawsOccludedSegments: Boolean; zIndex: Int
// hole(Hole.() -> Unit) -> vertices: List<LatLngAltitude>
// Runtime handles (mutable properties update live 3D scene):
// Polyline: path (coordinates is @Deprecated), strokeColor, strokeWidth, outerColor, outerWidth, altitudeMode, geodesic, drawsOccludedSegments, zIndex, remove(), setClickListener(OnPolylineClickListener?)
// Polygon: path (outerCoordinates is @Deprecated), innerPaths (innerCoordinates is @Deprecated), fillColor, strokeColor, strokeWidth, altitudeMode, extruded, geodesic, drawsOccludedSegments, zIndex, remove(), setClickListener(OnPolygonClickListener?)
```
