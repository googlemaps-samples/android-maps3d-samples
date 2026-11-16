# 02. Camera Positioning, Fly-To / Fly-Around Animations, Restrictions & 3D Projection

## 1. Instant Camera Pose & 4-Way `AltitudeMode`

Use `googleMap3D.setCamera(...)` to jump immediately to a 3D viewpoint. `Camera.range` and `LatLngAltitude.altitude` are always in **meters** (never 2D zoom levels). Specify `AltitudeMode` explicitly (`RELATIVE_TO_GROUND` or `CLAMP_TO_GROUND` when ground elevation above sea level is unknown).

```kotlin
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude

val goldenGateCamera = camera {
    center = latLngAltitude {
        latitude = 37.8199
        longitude = -122.4783
        altitude = 67.0
    }
    heading = 340.0
    tilt = 70.0
    roll = 0.0
    range = 1400.0
    altitudeMode = AltitudeMode.RELATIVE_TO_GROUND
}
googleMap3D.setCamera(goldenGateCamera)
```

---

## 2. Smooth Animations (`flyCameraTo`, `flyCameraAround`) & Coroutine Chaining

```kotlin
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.flyAroundOptions
import com.google.android.gms.maps3d.model.flyToOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

// Sequential coroutine helpers using setCameraAnimationEndListener
suspend fun GoogleMap3D.flyToAwait(target: Camera, durationMs: Long = 3500L) =
    suspendCancellableCoroutine<Unit> { cont ->
        setCameraAnimationEndListener {
            setCameraAnimationEndListener(null)
            if (cont.isActive) cont.resume(Unit)
        }
        cont.invokeOnCancellation {
            setCameraAnimationEndListener(null)
            stopCameraAnimation()
        }
        flyCameraTo(flyToOptions { endCamera = target; durationInMillis = durationMs })
    }

suspend fun GoogleMap3D.flyAroundAwait(centerCam: Camera, durationMs: Long = 10_000L, repeats: Double = 1.0) =
    suspendCancellableCoroutine<Unit> { cont ->
        setCameraAnimationEndListener {
            setCameraAnimationEndListener(null)
            if (cont.isActive) cont.resume(Unit)
        }
        cont.invokeOnCancellation {
            setCameraAnimationEndListener(null)
            stopCameraAnimation()
        }
        flyCameraAround(flyAroundOptions { center = centerCam; durationInMillis = durationMs; repeatCount = repeats })
    }
```

---

## 3. Scene Readiness, Steady State & Camera Restrictions

```kotlin
import com.google.android.gms.maps3d.model.cameraRestriction
import com.google.android.gms.maps3d.model.latLngBounds

// Track 3D tile loading progress [0.0..1.0] and full steady-state completion
googleMap3D.setOnMapReadyListener { sceneReadiness: Double -> /* 0.0..1.0 */ }
googleMap3D.setOnMapSteadyListener { isSceneSteady: Boolean ->
    if (isSceneSteady) { /* Camera stationary AND all visible 3D tiles loaded */ }
}

// Constrain user panning, tilt, heading, and altitude
googleMap3D.setCameraRestriction(
    cameraRestriction {
        minAltitude = 50.0
        maxAltitude = 5000.0
        minHeading = 0.0
        maxHeading = 360.0
        minTilt = 20.0
        maxTilt = 80.0
        bounds = latLngBounds {
            northEastLat = 40.8200
            northEastLng = -73.9200
            southWestLat = 40.6900
            southWestLng = -74.0300
        }
    }
)
// Clear restrictions: googleMap3D.setCameraRestriction(null)
```

---

## 4. Advanced 3D Camera Kinematics (`Projection3D`, `StationaryCameraTracker`, `FieldOfView`)

- **3D-to-2D Screen Projection (`Projection3D`):** Listen to `setCameraChangedListener { camera -> ... }`, convert `(lat, lng, alt)` into local East-North-Up (ENU) meters relative to `camera.center`, transform into camera axes `(X_cam, Y_cam, Z_cam)` using `heading`, `tilt`, `roll`, and project via vertical FOV (`45.0°` baseline) to pixel coordinates (`ScreenCoordinate(x, y, isVisible, depth)`). Reference: `Maps3DSamples/ApiDemos/common/src/main/java/com/example/maps3d/common/Projection3D.kt`.
- **Stationary Eye Tracking (`StationaryCameraTracker`):** Locks the physical camera eye `(E_eye, N_eye, U_eye)` in local ENU space while recomputing inverse spherical `(range, heading, tilt)` on each frame to follow a moving aircraft/vehicle. Reference: `Maps3DSamples/ApiDemos/common/src/main/java/com/example/maps3d/common/StationaryCameraTracker.kt`.
- **Field-of-View / Dolly-Zoom Framing:** For framing a target span of diameter $W$ meters at vertical FOV $\alpha$ (default $45^\circ$), set $\text{range} \approx \frac{W / 2}{\tan(\alpha / 2)} \times \text{paddingFactor}$.

---

## 5. Compact DSL & SDK Signature Block (`play-services-maps3d:16.0.1`)

```kotlin
// Top-level Kotlin DSL in com.google.android.gms.maps3d.model.DslKt (receiver is the class itself):
// latLngAltitude(LatLngAltitude.() -> Unit) -> latitude: Double; longitude: Double; altitude: Double (MAX_ALTITUDE_METERS = 6.371E7)
// camera(Camera.() -> Unit) -> center: LatLngAltitude; heading: Double?; tilt: Double?; roll: Double?; range: Double?; @AltitudeMode altitudeMode: Int
// flyToOptions(FlyToOptions.() -> Unit) -> endCamera: Camera; durationInMillis: Long
// flyAroundOptions(FlyAroundOptions.() -> Unit) -> center: Camera; durationInMillis: Long; repeatCount: Double (rounds is @Deprecated)
// cameraRestriction(CameraRestriction.() -> Unit) -> minAltitude: Double?; maxAltitude: Double?; minHeading: Double?; maxHeading: Double?; minTilt: Double?; maxTilt: Double?; bounds: LatLngBounds?
// latLngBounds(LatLngBounds.() -> Unit) -> northEastLat: Double; northEastLng: Double; southWestLat: Double; southWestLng: Double
// AltitudeMode: ABSOLUTE = 1, CLAMP_TO_GROUND = 2, RELATIVE_TO_GROUND = 3, RELATIVE_TO_MESH = 4
// GoogleMap3D: setCamera(Camera); getCamera(): Camera?; flyCameraTo(FlyToOptions); flyCameraAround(FlyAroundOptions); stopCameraAnimation(); setCameraRestriction(CameraRestriction?); getCameraRestriction(): CameraRestriction?; setCameraAnimationEndListener(OnCameraAnimationEndListener?); setCameraChangedListener(OnCameraChangedListener?); setOnMapReadyListener(OnMapReadyListener?); setOnMapSteadyListener(OnMapSteadyListener?)
```
