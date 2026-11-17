# 01b. XML Views Setup (`Map3DView`, Lifecycle & `TrackedMap3D`)

## 1. `activity_main.xml` Template (`xmlns:map3d`)

Declare `<com.google.android.gms.maps3d.Map3DView>` with `xmlns:map3d="http://schemas.android.com/apk/res-auto"`.

```xml
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:map3d="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <com.google.android.gms.maps3d.Map3DView
        android:id="@+id/map3dView"
        android:layout_width="0dp"
        android:layout_height="0dp"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        map3d:mapMode="hybrid"
        map3d:mapColorScheme="follow_system"
        map3d:uiMapModeControlEnabled="false"
        map3d:centerLat="37.7952"
        map3d:centerLng="-122.4028"
        map3d:centerAlt="0.0"
        map3d:heading="30.0"
        map3d:tilt="65.0"
        map3d:roll="0.0"
        map3d:range="1800.0" />
</androidx.constraintlayout.widget.ConstraintLayout>
```

### Supported `map3d:*` XML Attributes
- **Map Mode & Appearance:** `map3d:mapMode` (`"hybrid"`, `"satellite"`, `"roadmap"`), `map3d:mapColorScheme` (`"light"`, `"dark"`, `"follow_system"`), `map3d:uiMapModeControlEnabled` (`"true"`/`"false"`), `map3d:mapId` (Cloud Map ID string), `map3d:language` (BCP-47, e.g., `"ja"`), `map3d:region` (CLDR 2-letter, e.g., `"JP"`).
- **Initial Camera Pose:** `map3d:centerLat`, `map3d:centerLng`, `map3d:centerAlt`, `map3d:heading`, `map3d:tilt`, `map3d:roll`, `map3d:range`.
- **Initial Restrictions & Bounds:** `map3d:minAltitude`, `map3d:maxAltitude`, `map3d:minHeading`, `map3d:maxHeading`, `map3d:minTilt`, `map3d:maxTilt`, `map3d:northEastLat`, `map3d:northEastLng`, `map3d:southWestLat`, `map3d:southWestLng`.

---

## 2. Activity Lifecycle Forwarding (`DefaultLifecycleObserver` + `OnMap3DViewReadyCallback`)

`Map3DView` requires explicit lifecycle forwarding (`onCreate`, `onStart`, `onResume`, `onPause`, `onStop`, `onDestroy`, `onLowMemory`, `onSaveInstanceState`). Using `DefaultLifecycleObserver` keeps Activities clean and leak-free.

```kotlin
package com.example.maps3dkotlin

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.Map3DView
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude

class MapActivity : AppCompatActivity(), OnMap3DViewReadyCallback {

    private lateinit var map3DView: Map3DView
    private var trackedMap: TrackedMap3D? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        map3DView = findViewById(R.id.map3dView)
        map3DView.onCreate(savedInstanceState)

        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) = map3DView.onStart()
            override fun onResume(owner: LifecycleOwner) = map3DView.onResume()
            override fun onPause(owner: LifecycleOwner) = map3DView.onPause()
            override fun onStop(owner: LifecycleOwner) = map3DView.onStop()
            override fun onDestroy(owner: LifecycleOwner) {
                trackedMap?.clearAll()
                map3DView.onDestroy()
            }
        })

        map3DView.getMap3DViewAsync(this)
    }

    override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
        val map = TrackedMap3D(googleMap3D).also { trackedMap = it }
        googleMap3D.setMapMode(Map3DMode.HYBRID)
        googleMap3D.getUiControlSettings()?.isMapModeControlEnabled = false

        map.setCamera(
            camera {
                center = latLngAltitude {
                    latitude = 37.7952
                    longitude = -122.4028
                    altitude = 0.0
                }
                heading = 30.0
                tilt = 65.0
                roll = 0.0
                range = 1800.0
                altitudeMode = AltitudeMode.RELATIVE_TO_GROUND
            }
        )
    }

    override fun onError(error: Exception) {
        Log.e("MapActivity", "Map3DView initialization failed", error)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        map3DView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        map3DView.onSaveInstanceState(outState)
    }
}
```

---

## 3. Single-Instance Cleanup Delegate (`TrackedMap3D`)

Because `GoogleMap3D` uses a single shared native renderer per process, always remove added overlays and reset listeners before destroying the screen:

```kotlin
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.Popover
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.Marker
import com.google.android.gms.maps3d.model.MarkerOptions
import com.google.android.gms.maps3d.model.MarkerView
import com.google.android.gms.maps3d.model.MarkerViewOptions
import com.google.android.gms.maps3d.model.Model
import com.google.android.gms.maps3d.model.ModelOptions
import com.google.android.gms.maps3d.model.Polygon
import com.google.android.gms.maps3d.model.PolygonOptions
import com.google.android.gms.maps3d.model.Polyline
import com.google.android.gms.maps3d.model.PolylineOptions
import com.google.android.gms.maps3d.model.PopoverOptions

class TrackedMap3D(val delegate: GoogleMap3D) {
    private val items = mutableListOf<Any>()

    fun setCamera(camera: Camera) = delegate.setCamera(camera)
    fun addMarker(options: MarkerOptions): Marker? = delegate.addMarker(options)?.also { items.add(it) }
    fun addMarkerView(options: MarkerViewOptions): MarkerView? = delegate.addMarkerView(options)?.also { items.add(it) }
    fun addPolyline(options: PolylineOptions): Polyline = delegate.addPolyline(options).also { items.add(it) }
    fun addPolygon(options: PolygonOptions): Polygon = delegate.addPolygon(options).also { items.add(it) }
    fun addModel(options: ModelOptions): Model = delegate.addModel(options).also { items.add(it) }
    fun addPopover(options: PopoverOptions): Popover = delegate.addPopover(options).also { items.add(it) }

    fun clearAll() {
        delegate.stopCameraAnimation()
        delegate.setCameraAnimationEndListener(null)
        delegate.setCameraChangedListener(null)
        delegate.setMap3DClickListener(null)
        items.forEach { item ->
            when (item) {
                is Marker -> item.remove()
                is MarkerView -> item.remove()
                is Polyline -> item.remove()
                is Polygon -> item.remove()
                is Model -> item.remove()
                is Popover -> item.remove()
            }
        }
        items.clear()
    }
}
```

---

## 4. Programmatic Instantiation & `Choreographer` Frame Ticker

```kotlin
// Programmatic Map3DView using Map3DInitConfig.create(...)
val config = Map3DInitConfig.create(
    centerLat = 37.7952, centerLng = -122.4028, centerAlt = 0.0,
    heading = 30.0, tilt = 65.0, roll = 0.0, range = 1800.0,
    mapMode = Map3DMode.HYBRID, mapColorScheme = MapColorScheme.FOLLOW_SYSTEM
)
val map3DView = Map3DView(context, config)
containerLayout.addView(map3DView)

// Hardware-synchronized 60/120fps ticker (cancel in onPause/onDestroy)
private var lastFrameNanos = 0L
private val frameCallback = object : Choreographer.FrameCallback {
    override fun doFrame(frameTimeNanos: Long) {
        if (!isPlaying) return
        if (lastFrameNanos != 0L) {
            viewModel.onFrameTick((frameTimeNanos - lastFrameNanos) / 1_000_000_000.0)
        }
        lastFrameNanos = frameTimeNanos
        Choreographer.getInstance().postFrameCallback(this)
    }
}
```
