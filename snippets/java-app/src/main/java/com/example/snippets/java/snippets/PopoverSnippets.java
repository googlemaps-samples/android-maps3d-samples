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

package com.example.snippets.java.snippets;

import com.example.snippets.common.R;
import android.content.Context;
import com.example.snippets.java.TrackedMap3D;
import android.graphics.Color;
import android.widget.TextView;
import com.google.android.gms.maps3d.model.AltitudeMode;
import com.google.android.gms.maps3d.model.LatLngAltitude;
import com.google.android.gms.maps3d.model.Marker;
import com.google.android.gms.maps3d.model.MarkerOptions;
import com.google.android.gms.maps3d.Popover;
import com.google.android.gms.maps3d.model.PopoverOptions;
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.FlyToOptions;
import com.example.snippets.java.annotations.SnippetGroup;
import com.example.snippets.java.annotations.SnippetItem;

@SnippetGroup(
    title = "Popovers",
    description = "Snippets demonstrating anchored and configured 3D Popover views."
)
public class PopoverSnippets {

    private final Context context;
    private final TrackedMap3D map;

    public PopoverSnippets(Context context, TrackedMap3D map) {
        this.context = context;
        this.map = map;
    }

    /**
     * Adds a popover anchored to a marker.
     */
    @SuppressWarnings("unused")
    @SnippetItem(
        title = "1. Marker Anchor",
        description = "Adds a 'Hello Popover!' text bubble anchored to a marker"
    )
    public void addPopoverToMarker() {
        // [START maps_android_3d_popover_add_java]
        // Create a marker first
        Marker marker = map.addMarker(new MarkerOptions());
        if (marker == null)
            return;

        // Create a custom view for the popover
        TextView textView = new TextView(context);
        textView.setText(R.string.popover_hello);
        textView.setPadding(16, 16, 16, 16);
        textView.setBackgroundColor(Color.WHITE);

        // Add popover anchored to the marker
        PopoverOptions options = new PopoverOptions();
        options.setContent(textView);
        options.setPositionAnchor(marker);
        options.setAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);

        Popover popover = map.addPopover(options);

        // You can show/hide it
        if (popover != null) {
            popover.show();
        }
        // [END maps_android_3d_popover_add_java]
    }

    /**
     * Adds a configured popover (auto-close enabled, auto-pan disabled).
     */
    @SuppressWarnings("unused")
    @SnippetItem(
        title = "2. Configured",
        description = "Adds an 'Info' popover anchored to a marker with auto-close enabled and auto-pan disabled."
    )
    public void addConfiguredPopover() {
        // [START maps_android_3d_popover_options_java]
        // Create a marker at an interesting location (e.g., Golden Gate Bridge)
        MarkerOptions markerOptions = new MarkerOptions();
        LatLngAltitude position = new LatLngAltitude(37.8199, -122.4783, 0.0);
        markerOptions.setPosition(position);
        markerOptions.setLabel("Golden Gate Bridge");
        Marker marker = map.addMarker(markerOptions);
        if (marker == null) return;

        TextView textView = new TextView(context);
        textView.setText(com.example.snippets.common.R.string.popover_info);
        textView.setPadding(16, 16, 16, 16);
        textView.setBackgroundColor(Color.WHITE);

        PopoverOptions options = new PopoverOptions();
        options.setContent(textView);
        options.setPositionAnchor(marker);
        options.setAutoCloseEnabled(true); // Close when clicking elsewhere
        options.setAutoPanEnabled(false); // Do not pan to popover

        Popover popover = map.addPopover(options);
        if (popover != null) {
            popover.show();
        }
        // [END maps_android_3d_popover_options_java]

        // Position the camera to show the marker/popover
        Camera targetCamera = new Camera(position, 0.0, 45.0, 0.0, 1000.0);
        FlyToOptions flyToOptions = new FlyToOptions(targetCamera, 2000L);
        map.flyCameraTo(flyToOptions);
    }
}
