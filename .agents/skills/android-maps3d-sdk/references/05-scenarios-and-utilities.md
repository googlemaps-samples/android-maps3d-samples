# 05. Scenarios, Units, Camera Utilities, 2D-to-3D Migration & Showcase Blueprints

## 1. Type-Safe Units & Camera Sanitization Utilities

Prevent out-of-range camera exceptions by clamping coordinates and wrapping angles (`heading` in `[0.0, 360.0)`, `tilt` in `[0.0, 90.0]`, `range` in `[0.0, 63_170_000.0]`, `altitude` in `[0.0, LatLngAltitude.MAX_ALTITUDE_METERS]`):

```kotlin
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import kotlin.math.floor

@JvmInline
value class Meters(val value: Double) : Comparable<Meters> {
    override fun compareTo(other: Meters) = value.compareTo(other.value)
}
inline val Number.meters: Meters get() = Meters(toDouble())
inline val Number.km: Meters get() = Meters(toDouble() * 1000.0)
inline val Number.feet: Meters get() = Meters(toDouble() / 3.28084)
inline val Number.miles: Meters get() = Meters(toDouble() / 0.000621371)

fun Double.wrapIn(lower: Double, upper: Double): Double {
    val span = upper - lower
    return lower + ((this - lower) - floor((this - lower) / span) * span)
}

fun Camera?.toValidCamera(): Camera {
    val src = this ?: return Camera.DEFAULT_CAMERA
    return camera {
        center = latLngAltitude {
            latitude = src.center.latitude.coerceIn(-90.0, 90.0)
            longitude = src.center.longitude.coerceIn(-180.0, 180.0)
            altitude = src.center.altitude.coerceIn(0.0, LatLngAltitude.MAX_ALTITUDE_METERS)
        }
        heading = (src.heading ?: 0.0).wrapIn(0.0, 360.0)
        tilt = (src.tilt ?: 0.0).coerceIn(0.0, 90.0)
        roll = (src.roll ?: 0.0).wrapIn(-360.0, 360.0)
        range = (src.range ?: 1000.0).coerceIn(0.0, 63_170_000.0)
        altitudeMode = src.altitudeMode
    }
}
```
*Built-in SDK Validators (`com.google.android.gms.maps3d`):* `Validators` (`validateLatitude`, `validateLongitude`, `validateAltitude`, `validateHeading`, `validateTilt`, `validateRoll`, `validateRange`) and `ValidatorsKt` (`camera.validate()`, `markerOptions.validate()`, etc.) throw `IllegalArgumentException` on invalid values.

---

## 2. Declarative Scenario Engine (`Scenario` & `MarkerWithPopover`)

Decouple map content from Activities using immutable data-driven scenarios loaded into `TrackedMap3D`:

```kotlin
data class MarkerWithPopover(
    val markerOptions: MarkerOptions,
    val popoverOptions: PopoverOptions? = null
)

data class Scenario(
    val id: String,
    val title: String,
    val initialCamera: Camera,
    @Map3DMode val mapMode: Int = Map3DMode.HYBRID,
    val markers: List<MarkerWithPopover> = emptyList(),
    val polylines: List<PolylineOptions> = emptyList(),
    val polygons: List<PolygonOptions> = emptyList(),
    val models: List<ModelOptions> = emptyList()
)
```

---

## 3. 2D Maps (`play-services-maps`) to 3D Maps (`16.0.1`) Conversion Formula

When migrating 2D `CameraPosition.zoom` to 3D `Camera.range` (meters):
- `rangeMeters = (156543.03392 * cos(Math.toRadians(latitude)) * 256.0) / 2.0.pow(zoom)`
- Quick reference: City overview (`zoom 12`) ~ `15,000m`; Neighborhood (`zoom 15`) ~ `1,800m`; Street/Building (`zoom 18`) ~ `250m`.
- Cloud-Based Map Styling (`mapId`) is initialization-only via XML `map3d:mapId` or `Map3DInitConfig.create(..., mapId = "YOUR_MAP_ID")`; switching `mapId` at runtime requires recreating `Map3DView`.

---

## 4. Production Showcase Architectural Blueprints (`android-maps3d-samples`)

Follow Clean Architecture / MVVM separation of concerns (Pure Math Engine -> State Controller -> ViewModel exposing `StateFlow` + `asLiveData()` -> Thin View):

1. **Multi-Stop Automated 3D World Tour (`TourPlaybackController`):**
   - Cycles waypoints through `FLYING_TO` (`flyCameraTo`) -> `ORBITING` (`flyCameraAround` triggered by `setCameraAnimationEndListener`) -> `WAITING_FOR_STEADY` (`setOnMapSteadyListener` + dwell delay).
   - Reference: `Maps3DSamples/ApiDemos/common/src/main/java/com/example/maps3d/common/TourPlaybackController.kt`.
2. **Real-Time 3D Vehicle Path Following (`PathEngine`, `RouteEngine`, `PathPlaybackController`):**
   - Precomputes cumulative spherical distances with binary search for $O(\log n)$ 3D `(lat, lng, alt)` interpolation and EMA angular heading smoothing across $0^\circ / 360^\circ$.
   - Advances `model.position`, `model.orientation`, and chase `googleMap3D.setCamera(...)` on `Choreographer.FrameCallback` (Views) or `withFrameMillis` (Compose).
   - Reference: `Maps3DSamples/ApiDemos/common/src/main/java/com/example/maps3d/common/PathEngine.kt`, `RouteEngine.kt`, `PathPlaybackController.kt`.
3. **Hilly & Undulating Road Early Alerts (`RoadTerrainAnalyzer`, `HillyRoadAlertViewModel`):**
   - Scans a forward lookahead window (`250m`, `500m`, `750m`) along a 3D route, detects `ExtremaType.CREST` / `ExtremaType.DIP` inflection points, classifies `UNDULATING_ROLLER_COASTER`, `BLIND_CREST`, `STEEP_CLIMB`, and `STEEP_DESCENT`, places 3D warning markers ahead, and dynamically widens chase camera `tilt` (`74° -> 66°`) and `range` (`160m -> 230m`) for blind-crest preview.
   - Reference: `Maps3DSamples/ApiDemos/common/src/main/java/com/example/maps3d/common/RoadTerrainAnalyzer.kt`, `HillyRoadAlertViewModel.kt`.
4. **Places SDK + Places UI Kit 3D Discovery (`PlacesUIKit3D`):**
   - Integrates `PlacesClient.searchByText` (`PlacesRepositoryImpl`), `PlaceDetailsCompactFragment`, `Camera3DTarget.fromPlaces` bounding-box framing, `AltitudeMode.CLAMP_TO_GROUND` 3D pins, and `setMap3DClickListener` POI `placeId` lookup.
   - Reference: `PlacesUIKit3D/src/main/java/com/example/placesuikit3d/`.
