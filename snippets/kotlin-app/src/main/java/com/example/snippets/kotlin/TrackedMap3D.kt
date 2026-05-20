package com.example.snippets.kotlin

import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.OnCameraChangedListener
import com.google.android.gms.maps3d.OnMap3DClickListener
import com.google.android.gms.maps3d.OnMapReadyListener
import com.google.android.gms.maps3d.OnMapSteadyListener
import com.google.android.gms.maps3d.Popover
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.CameraRestriction
import com.google.android.gms.maps3d.model.FlyAroundOptions
import com.google.android.gms.maps3d.model.FlyToOptions
import com.google.android.gms.maps3d.model.Marker
import com.google.android.gms.maps3d.model.MarkerOptions
import com.google.android.gms.maps3d.model.Model
import com.google.android.gms.maps3d.model.ModelOptions
import com.google.android.gms.maps3d.model.Polygon
import com.google.android.gms.maps3d.model.PolygonOptions
import com.google.android.gms.maps3d.model.Polyline
import com.google.android.gms.maps3d.model.PolylineOptions
import com.google.android.gms.maps3d.model.PopoverOptions

/**
 * Decorator wrapper around GoogleMap3D to track elements added during a snippet session.
 */
class TrackedMap3D(
    val delegate: GoogleMap3D,
    private val items: MutableList<Any>,
) {

    fun addMarker(options: MarkerOptions): Marker? {
        val marker = delegate.addMarker(options)
        if (marker != null) items.add(marker)
        return marker
    }

    fun addPolyline(options: PolylineOptions): Polyline {
        val polyline = delegate.addPolyline(options)
        if (polyline != null) items.add(polyline)
        return polyline
    }

    fun addPolygon(options: PolygonOptions): Polygon {
        val polygon = delegate.addPolygon(options)
        if (polygon != null) items.add(polygon)
        return polygon
    }

    fun addModel(options: ModelOptions): Model {
        val model = delegate.addModel(options)
        if (model != null) items.add(model)
        return model
    }

    fun addPopover(options: PopoverOptions): Popover {
        val popover = delegate.addPopover(options)
        if (popover != null) items.add(popover)
        return popover
    }

    fun setCamera(camera: Camera) = delegate.setCamera(camera)
    fun setCameraChangedListener(listener: OnCameraChangedListener?) = delegate.setCameraChangedListener(listener)
    fun setMap3DClickListener(listener: OnMap3DClickListener?) = delegate.setMap3DClickListener(listener)

    // Pass-through standard map calls if any are invoked inside snippets
    fun getCamera() = delegate.getCamera()
    fun stopCameraAnimation() = delegate.stopCameraAnimation()
    fun flyCameraTo(options: FlyToOptions) = delegate.flyCameraTo(options)
    fun flyCameraAround(options: FlyAroundOptions) = delegate.flyCameraAround(options)
    fun setOnMapReadyListener(listener: OnMapReadyListener?) = delegate.setOnMapReadyListener(listener)
    fun setOnMapSteadyListener(listener: OnMapSteadyListener?) = delegate.setOnMapSteadyListener(listener)
    fun setCameraRestriction(restriction: CameraRestriction?) = delegate.setCameraRestriction(restriction)
    fun getCameraRestriction() = delegate.getCameraRestriction()
}
