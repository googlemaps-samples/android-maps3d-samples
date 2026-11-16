# 03. 3D Markers, Custom Pins, MarkerViews, Popovers, Models & Map Clicks

## 1. 3D Markers, `PinConfiguration`, `Glyph`, `ImageView` & `PinView`

`googleMap3D.addMarker(...)` returns a **nullable** `Marker?`. Set `isExtruded = true` to draw a vertical stem to the ground, and `isDrawnWhenOccluded = true` so pins stay visible behind 3D buildings.

```kotlin
import android.graphics.Color
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.CollisionBehavior
import com.google.android.gms.maps3d.model.Glyph
import com.google.android.gms.maps3d.model.imageView
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.gms.maps3d.model.markerOptions
import com.google.android.gms.maps3d.model.pinConfiguration

val customGlyph = Glyph.fromText("SF").apply { color = Color.WHITE }

val marker = googleMap3D.addMarker(
    markerOptions {
        id = "coit_tower_pin"
        position = latLngAltitude { latitude = 37.8024; longitude = -122.4058; altitude = 80.0 }
        label = "Coit Tower"
        altitudeMode = AltitudeMode.RELATIVE_TO_GROUND
        isExtruded = true
        isDrawnWhenOccluded = true
        isSizePreserved = true
        collisionBehavior = CollisionBehavior.REQUIRED_AND_HIDES_OPTIONAL
        zIndex = 10
        setStyle(
            pinConfiguration {
                backgroundColor = Color.parseColor("#EA4335")
                borderColor = Color.WHITE
                scale = 1.3f
                glyph = customGlyph
            }
        )
    }
)
// Or use a drawable resource via setStyle(imageView { resourceId = R.drawable.ic_pin })
// For previewing a pin outside the map, wrap PinConfiguration in com.google.android.gms.maps3d.model.PinView(context, pinConfig)
```

---

## 2. Custom Android `View` Markers (`MarkerView`)

In `16.0.1`, `markerViewOptions { }` is `@Hide` — always instantiate `MarkerViewOptions().apply { ... }` directly. `googleMap3D.addMarkerView(...)` returns a **nullable** `MarkerView?`.

```kotlin
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.CollisionBehavior
import com.google.android.gms.maps3d.model.MarkerViewOptions
import com.google.android.gms.maps3d.model.latLngAltitude

val badgeView = layoutInflater.inflate(R.layout.view_custom_marker_badge, null)
val markerView = googleMap3D.addMarkerView(
    MarkerViewOptions().apply {
        id = "price_badge_1"
        position = latLngAltitude { latitude = 37.7955; longitude = -122.3937; altitude = 25.0 }
        content = badgeView
        altitudeMode = AltitudeMode.RELATIVE_TO_GROUND
        collisionBehavior = CollisionBehavior.REQUIRED
    }
)
```

---

## 3. Interactive 3D Info Windows (`Popover`) — Strictly `@UiThread`

`Popover` anchors an Android `View` to any `Anchorable` (`Marker`, `MarkerView`, or `LatLngAltitude`).
- `googleMap3D.addPopover(...)` and `Popover.show()` / `hide()` / `toggle()` / `remove()` are strictly `@UiThread`.
- `anchorOffset` uses `android.graphics.Point`, and `PopoverStyle` / `PopoverShadow` numeric properties are `Float` (`16.0f`, `12.0f`, `4.0f`).

```kotlin
import android.graphics.Color
import android.graphics.Point
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.popoverOptions
import com.google.android.gms.maps3d.model.popoverShadow
import com.google.android.gms.maps3d.model.popoverStyle

val infoView = layoutInflater.inflate(R.layout.popover_landmark_card, null)
runOnUiThread {
    val popover = googleMap3D.addPopover(
        popoverOptions {
            content = infoView
            positionAnchor = marker // Anchorable: Marker, MarkerView, or LatLngAltitude
            anchorOffset = Point(0, -12)
            altitudeMode = AltitudeMode.RELATIVE_TO_GROUND
            autoCloseEnabled = true
            autoPanEnabled = true
            popoverStyle = popoverStyle {
                backgroundColor = Color.WHITE
                borderRadius = 16.0f
                padding = 12.0f
                shadow = popoverShadow {
                    color = Color.argb(70, 0, 0, 0)
                    offsetX = 0.0f
                    offsetY = 4.0f
                    radius = 10.0f
                }
            }
        }
    )
    marker?.setClickListener { runOnUiThread { popover.toggle() } }
}
```

---

## 4. 3D `.glb` / `.gltf` Models (`addModel`) & Main-Thread Map Clicks (`Map3DClickEvent`)

```kotlin
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.Map3DClickEvent
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.gms.maps3d.model.modelOptions
import com.google.android.gms.maps3d.model.orientation
import com.google.android.gms.maps3d.model.vector3D

val model = googleMap3D.addModel(
    modelOptions {
        id = "tour_aircraft"
        url = "https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/Airplane.glb"
        position = latLngAltitude { latitude = 37.7952; longitude = -122.4028; altitude = 300.0 }
        altitudeMode = AltitudeMode.RELATIVE_TO_GROUND
        scale = vector3D { x = 5.0; y = 5.0; z = 5.0 }
        orientation = orientation { heading = 45.0; tilt = 270.0; roll = 0.0 }
    }
)
model.setClickListener { /* handle 3D model click */ }

// Main-thread Map3DClickEvent listener (GA 16.0.1+)
// Return false to allow default Places POI popover (requires FragmentActivity + places SDK), true to suppress
googleMap3D.setMap3DClickListener { event: Map3DClickEvent ->
    val clickedLocation = event.location // LatLngAltitude
    val clickedPlaceId = event.placeId   // String?
    false
}
```

---

## 5. Compact DSL & SDK Signature Block (`play-services-maps3d:16.0.1`)

```kotlin
// Top-level Kotlin DSL in com.google.android.gms.maps3d.model.DslKt:
// markerOptions(MarkerOptions.() -> Unit) -> id: String; position: LatLngAltitude; label: String?; @AltitudeMode altitudeMode: Int; isExtruded: Boolean; isDrawnWhenOccluded: Boolean; isSizePreserved: Boolean; @CollisionBehavior collisionBehavior: Int; collisionPriority: Int; zIndex: Int; setStyle(PinConfiguration | ImageView)
// pinConfiguration(PinConfiguration.() -> Unit) -> backgroundColor: Int; borderColor: Int; scale: Float; glyph: Glyph?
// imageView(ImageView.() -> Unit) -> resourceId: Int | MarkerStyle(imageView: ImageView?, pinConfiguration: PinConfiguration?) | PinView(context, pinConfiguration)
// Glyph: Glyph.fromCircle() | Glyph.fromColor(@ColorInt Int) | Glyph.fromText(String) -> color: Int; image: ImageView?; text: String?
// MarkerViewOptions() -> id: String; position: LatLngAltitude; content: View?; @AltitudeMode altitudeMode: Int; @CollisionBehavior collisionBehavior: Int; collisionPriority: Int
// popoverOptions(PopoverOptions.() -> Unit) -> content: View?; positionAnchor: Anchorable; anchorOffset: Point?; @AltitudeMode altitudeMode: Int; autoCloseEnabled: Boolean; autoPanEnabled: Boolean; popoverStyle: PopoverStyle
// popoverStyle(PopoverStyle.() -> Unit) -> backgroundColor: Int?; borderRadius: Float; padding: Float; shadow: PopoverShadow
// popoverShadow(PopoverShadow.() -> Unit) -> color: Int; offsetX: Float; offsetY: Float; radius: Float
// modelOptions(ModelOptions.() -> Unit) -> id: String; url: String; position: LatLngAltitude; @AltitudeMode altitudeMode: Int; scale: Vector3D; orientation: Orientation
// vector3D(Vector3D.() -> Unit) -> x: Double; y: Double; z: Double | orientation(Orientation.() -> Unit) -> heading: Double; tilt: Double; roll: Double
// CollisionBehavior: REQUIRED = 1, REQUIRED_AND_HIDES_OPTIONAL = 2, OPTIONAL_AND_HIDES_LOWER_PRIORITY = 3
// VisibilityState: VISIBILITY_VISIBLE = 0, VISIBILITY_HIDDEN = 1, VISIBILITY_FADING_IN = 2, VISIBILITY_FADING_OUT = 3
// Runtime handles: Marker (Anchorable, drawsWhenOccluded, isExtruded, sizePreserved, remove(), setClickListener(OnMarkerClickListener?)); MarkerView (Anchorable, remove()); Model (position, orientation, scale, url, remove(), setClickListener(OnModelClickListener?)); Popover (@UiThread show()/hide()/toggle()/remove(), val isVisible, val content, val positionAnchor: Anchorable, val anchorOffset: Point)
// Map Click Listener: GoogleMap3D.setMap3DClickListener(OnMap3DClickListener?) -> fun onMap3DClick(event: Map3DClickEvent): Boolean
```
