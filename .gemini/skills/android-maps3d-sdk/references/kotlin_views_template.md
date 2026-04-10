# Kotlin (Views) Template for Android Maps 3D SDK

Use this template when building an Android application using Kotlin and standard XML Views (not Compose).

## 1. Layout XML

Create a layout file (e.g., `activity_main.xml`) and add the `Map3DView`.

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <com.google.android.gms.maps3d.Map3DView
        android:id="@+id/map_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

</FrameLayout>
```

## 2. Activity Code

Implement the activity to handle the map lifecycle and initialization.

```kotlin
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.Map3DView
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var mapView: Map3DView
    private var googleMap: GoogleMap3D? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mapView = findViewById(R.id.map_view)
        mapView.onCreate(savedInstanceState)

        // Use DefaultLifecycleObserver to handle most lifecycle events automatically
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) { mapView.onStart() }
            override fun onResume(owner: LifecycleOwner) { mapView.onResume() }
            override fun onPause(owner: LifecycleOwner) { mapView.onPause() }
            override fun onStop(owner: LifecycleOwner) { mapView.onStop() }
            override fun onDestroy(owner: LifecycleOwner) { mapView.onDestroy() }
        })

        mapView.getMap3DViewAsync(object : OnMap3DViewReadyCallback {
            override fun onMap3DViewReady(map: GoogleMap3D) {
                googleMap = map
                
                // Fails on cold starts because the viewport layout and binding matrix are not yet stable.
                // Use a timer-based delay workaround.
                lifecycleScope.launch {
                    // Wait for the viewport to fully inflate and bindings to stabilize.
                    delay(500) 
                    setupMapElements()
                }
            }

            override fun onError(e: Exception) {
                Log.e("MainActivity", "Error loading map", e)
            }
        })
    }

    private fun setupMapElements() {
        val map = googleMap ?: return
        
        // Example: Set initial camera position
        val initialCamera = camera {
            center = latLngAltitude {
                latitude = 40.0150
                longitude = -105.2705
                altitude = 5000.0
            }
            heading = 0.0
            tilt = 45.0
            roll = 0.0
            range = 10000.0
        }
        map.setCamera(initialCamera)
        
        // Add more initialization logic here (markers, polylines, etc.)
    }

    // onLowMemory and onSaveInstanceState still need to be forwarded manually
    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}
```

## 3. Best Practices

- **Lifecycle**: Always forward lifecycle methods to `Map3DView`.
- **Initialization Delay**: Use the 500ms delay pattern shown above to avoid edge-trigger races on startup.
- **State Latch**: Implement an `isInitialized` boolean latch in `setupMapElements()` if you plan to call it multiple times or across lifecycle events to avoid stacking duplicate objects.

## 4. Optional: Object Tracking Delegate

For advanced use cases where you need to add and remove many objects (Markers, Polylines, etc.) dynamically, it is helpful to use a delegate wrapper to track these objects for easy cleanup.

Here is a simplified version of the `TrackedMap3D` pattern used in the project samples:

```kotlin
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.Marker
import com.google.android.gms.maps3d.model.MarkerOptions
import com.google.android.gms.maps3d.model.Polygon
import com.google.android.gms.maps3d.model.PolygonOptions
import com.google.android.gms.maps3d.model.Polyline
import com.google.android.gms.maps3d.model.PolylineOptions
import com.google.android.gms.maps3d.model.Camera

class TrackedMap3D(
    val delegate: GoogleMap3D,
    private val items: MutableList<Any> = mutableListOf()
) {
    fun addMarker(options: MarkerOptions): Marker? {
        val marker = delegate.addMarker(options)
        if (marker != null) items.add(marker)
        return marker
    }

    fun addPolyline(options: PolylineOptions): Polyline? {
        val polyline = delegate.addPolyline(options)
        if (polyline != null) items.add(polyline)
        return polyline
    }

    fun addPolygon(options: PolygonOptions): Polygon? {
        val polygon = delegate.addPolygon(options)
        if (polygon != null) items.add(polygon)
        return polygon
    }

    /**
     * Clears all tracked objects from the map.
     */
    fun clearAll() {
        items.forEach { item ->
            when (item) {
                is Marker -> item.remove()
                is Polyline -> item.remove()
                is Polygon -> item.remove()
            }
        }
        items.clear()
    }

    // Forward other necessary methods to the delegate as needed
    fun setCamera(camera: Camera) = delegate.setCamera(camera)
}
```

To use it, wrap the `GoogleMap3D` instance and ensure you clear it in `onDestroy` to avoid retaining objects across Activity instances (since the 3D engine persists across Activity recreation):

```kotlin
private var trackedMap: TrackedMap3D? = null

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // ...

    lifecycle.addObserver(object : DefaultLifecycleObserver {
        // ...
        override fun onDestroy(owner: LifecycleOwner) {
            // Automatically clean up objects to avoid cruft
            trackedMap?.clearAll()
            mapView.onDestroy()
        }
    })

    mapView.getMap3DViewAsync(object : OnMap3DViewReadyCallback {
        override fun onMap3DViewReady(map: GoogleMap3D) {
            trackedMap = TrackedMap3D(map)
            // ...
        }
        // ...
    })
}
```

