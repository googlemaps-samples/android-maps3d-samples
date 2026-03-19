package com.example.snippets.kotlin

import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.Marker
import com.google.android.gms.maps3d.model.MarkerOptions
import com.google.android.gms.maps3d.model.Polyline
import com.google.android.gms.maps3d.model.PolylineOptions
import com.google.android.gms.maps3d.model.Polygon
import com.google.android.gms.maps3d.model.PolygonOptions
import com.google.android.gms.maps3d.model.Model
import com.google.android.gms.maps3d.model.ModelOptions
import com.google.android.gms.maps3d.Popover
import com.google.android.gms.maps3d.model.PopoverOptions

/**
 * Decorator wrapper around GoogleMap3D to track elements added during a snippet session.
 */
class TrackedMap3D(
    val delegate: GoogleMap3D,
    private val items: MutableList<Any>
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

    fun addModel(options: ModelOptions): Model? {
        val model = delegate.addModel(options)
        if (model != null) items.add(model)
        return model
    }

    fun addPopover(options: PopoverOptions): Popover? {
        val popover = delegate.addPopover(options)
        if (popover != null) items.add(popover)
        return popover
    }

    fun setCamera(camera: com.google.android.gms.maps3d.model.Camera) = delegate.setCamera(camera)
    fun setCameraChangedListener(listener: com.google.android.gms.maps3d.OnCameraChangedListener?) = delegate.setCameraChangedListener(listener)
    fun setMap3DClickListener(listener: com.google.android.gms.maps3d.OnMap3DClickListener?) = delegate.setMap3DClickListener(listener)

    // Pass-through standard map calls if any are invoked inside snippets
    fun getCamera() = delegate.getCamera()
    fun stopCameraAnimation() = delegate.stopCameraAnimation()
    fun flyCameraTo(options: com.google.android.gms.maps3d.model.FlyToOptions) = delegate.flyCameraTo(options)
    fun flyCameraAround(options: com.google.android.gms.maps3d.model.FlyAroundOptions) = delegate.flyCameraAround(options)
    fun setOnMapReadyListener(listener: com.google.android.gms.maps3d.OnMapReadyListener?) = delegate.setOnMapReadyListener(listener)
    fun setOnMapSteadyListener(listener: com.google.android.gms.maps3d.OnMapSteadyListener?) = delegate.setOnMapSteadyListener(listener)
}
