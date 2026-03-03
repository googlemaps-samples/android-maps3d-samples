/*
 * Copyright 2025 Google LLC
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
import android.graphics.Color;
import android.widget.TextView;
import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.model.AltitudeMode;
import com.google.android.gms.maps3d.model.LatLngAltitude;
import com.google.android.gms.maps3d.model.Marker;
import com.google.android.gms.maps3d.model.MarkerOptions;
import com.google.android.gms.maps3d.Popover;
import com.google.android.gms.maps3d.model.PopoverOptions;

public class PopoverSnippets {

    private final Context context;
    private final GoogleMap3D map;

    public PopoverSnippets(Context context, GoogleMap3D map) {
        this.context = context;
        this.map = map;
    }

    // [START maps_android_3d_popover_add_java]
    /**
     * Adds a popover anchored to a marker.
     */
    public void addPopoverToMarker() {
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
    }
    // [END maps_android_3d_popover_add_java]

    // [START maps_android_3d_popover_options_java]
    /**
     * Adds a configured popover (auto-close enabled, auto-pan disabled).
     */
    public void addConfiguredPopover() {
        TextView textView = new TextView(context);
        textView.setText(com.example.snippets.common.R.string.popover_info);

        PopoverOptions options = new PopoverOptions();
        options.setContent(textView);
        options.setPositionAnchor(new LatLngAltitude(0, 0, 0));
        options.setAutoCloseEnabled(true); // Close when clicking elsewhere
        options.setAutoPanEnabled(false); // Do not pan to popover

        map.addPopover(options);
    }
    // [END maps_android_3d_popover_options_java]
}
