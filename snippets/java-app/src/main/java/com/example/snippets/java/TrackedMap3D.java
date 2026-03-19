package com.example.snippets.java;

import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.OnCameraChangedListener;
import com.google.android.gms.maps3d.OnMap3DClickListener;
import com.google.android.gms.maps3d.OnMapReadyListener;
import com.google.android.gms.maps3d.OnMapSteadyListener;
import com.google.android.gms.maps3d.OnCameraAnimationEndListener;
import com.google.android.gms.maps3d.Popover;
import com.google.android.gms.maps3d.model.Marker;
import com.google.android.gms.maps3d.model.MarkerOptions;
import com.google.android.gms.maps3d.model.Polyline;
import com.google.android.gms.maps3d.model.PolylineOptions;
import com.google.android.gms.maps3d.model.Polygon;
import com.google.android.gms.maps3d.model.PolygonOptions;
import com.google.android.gms.maps3d.model.Model;
import com.google.android.gms.maps3d.model.ModelOptions;
import com.google.android.gms.maps3d.model.PopoverOptions;

import java.util.List;

/**
 * Decorator wrapper around GoogleMap3D to track elements added during a snippet session.
 */
public class TrackedMap3D {

    private final GoogleMap3D delegate;
    private final List<Object> items;

    public TrackedMap3D(GoogleMap3D delegate, List<Object> items) {
        this.delegate = delegate;
        this.items = items;
    }

    public Marker addMarker(MarkerOptions options) {
        Marker marker = delegate.addMarker(options);
        if (marker != null) items.add(marker);
        return marker;
    }

    public Polyline addPolyline(PolylineOptions options) {
        Polyline polyline = delegate.addPolyline(options);
        if (polyline != null) items.add(polyline);
        return polyline;
    }

    public Polygon addPolygon(PolygonOptions options) {
        Polygon polygon = delegate.addPolygon(options);
        if (polygon != null) items.add(polygon);
        return polygon;
    }

    public Model addModel(ModelOptions options) {
        Model model = delegate.addModel(options);
        if (model != null) items.add(model);
        return model;
    }

    public Popover addPopover(PopoverOptions options) {
        Popover popover = delegate.addPopover(options);
        if (popover != null) items.add(popover);
        return popover;
    }

    public void setCamera(com.google.android.gms.maps3d.model.Camera camera) { delegate.setCamera(camera); }
    public void setCameraChangedListener(OnCameraChangedListener listener) { delegate.setCameraChangedListener(listener); }
    public void setMap3DClickListener(OnMap3DClickListener listener) { delegate.setMap3DClickListener(listener); }
    public void flyCameraAround(com.google.android.gms.maps3d.model.FlyAroundOptions options) { delegate.flyCameraAround(options); }

    // Pass-through standard map calls if any are invoked inside snippets
    public com.google.android.gms.maps3d.model.Camera getCamera() { return delegate.getCamera(); }
    public void stopCameraAnimation() { delegate.stopCameraAnimation(); }
    public void flyCameraTo(com.google.android.gms.maps3d.model.FlyToOptions options) { delegate.flyCameraTo(options); }
    public void setOnMapReadyListener(OnMapReadyListener listener) { delegate.setOnMapReadyListener(listener); }
    public void setOnMapSteadyListener(OnMapSteadyListener listener) { delegate.setOnMapSteadyListener(listener); }
}
