/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.snippets.java;

import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.OnCameraChangedListener;
import com.google.android.gms.maps3d.OnMap3DClickListener;
import com.google.android.gms.maps3d.OnMapReadyListener;
import com.google.android.gms.maps3d.OnMapSteadyListener;
import com.google.android.gms.maps3d.Popover;
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.FlyToOptions;
import com.google.android.gms.maps3d.model.Marker;
import com.google.android.gms.maps3d.model.MarkerOptions;
import com.google.android.gms.maps3d.model.Polyline;
import com.google.android.gms.maps3d.model.PolylineOptions;
import com.google.android.gms.maps3d.model.Polygon;
import com.google.android.gms.maps3d.model.PolygonOptions;
import com.google.android.gms.maps3d.model.Model;
import com.google.android.gms.maps3d.model.ModelOptions;
import com.google.android.gms.maps3d.model.PopoverOptions;
import com.google.android.gms.maps3d.model.CameraRestriction;

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

    public void setCamera(Camera camera) { delegate.setCamera(camera); }
    public void setCameraChangedListener(OnCameraChangedListener listener) { delegate.setCameraChangedListener(listener); }
    public void setMap3DClickListener(OnMap3DClickListener listener) { delegate.setMap3DClickListener(listener); }
    public void flyCameraAround(com.google.android.gms.maps3d.model.FlyAroundOptions options) { delegate.flyCameraAround(options); }

    // Pass-through standard map calls if any are invoked inside snippets
    public Camera getCamera() { return delegate.getCamera(); }
    public void stopCameraAnimation() { delegate.stopCameraAnimation(); }
    public void flyCameraTo(FlyToOptions options) { delegate.flyCameraTo(options); }
    public void setOnMapReadyListener(OnMapReadyListener listener) { delegate.setOnMapReadyListener(listener); }
    public void setOnMapSteadyListener(OnMapSteadyListener listener) { delegate.setOnMapSteadyListener(listener); }
    public void setCameraRestriction(CameraRestriction restriction) { delegate.setCameraRestriction(restriction); }
    public CameraRestriction getCameraRestriction() { return delegate.getCameraRestriction(); }
}
