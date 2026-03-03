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

import androidx.annotation.NonNull;
import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.model.AltitudeMode;
import com.google.android.gms.maps3d.model.CollisionBehavior;
import com.google.android.gms.maps3d.model.LatLngAltitude;
import com.google.android.gms.maps3d.model.Marker;
import com.google.android.gms.maps3d.model.MarkerOptions;
import com.google.android.gms.maps3d.model.PinConfiguration;
import com.google.android.gms.maps3d.model.Glyph;
import com.example.snippets.java.R;
import android.content.Context;
import android.graphics.Color;
import android.widget.ImageView;
import com.google.android.gms.maps3d.OnMarkerClickListener;

public class MarkerSnippets {

    private final GoogleMap3D map;

    public MarkerSnippets(GoogleMap3D map) {
        this.map = map;
    }

    // [START maps_android_3d_marker_add_java]
    /**
     * Adds a basic marker to the map.
     */
    public void addBasicMarker() {
        LatLngAltitude position = new LatLngAltitude(37.4220, -122.0841, 10.0);

        MarkerOptions options = new MarkerOptions();
        options.setPosition(position);
        options.setLabel("Basic Marker");
        // MarkerOptions uses label, not title.

        Marker marker = map.addMarker(options);
    }
    // [END maps_android_3d_marker_add_java]

    // [START maps_android_3d_marker_options_java]
    /**
     * Adds an advanced marker with detailed configuration options.
     */
    public void addAdvancedMarker() {
        LatLngAltitude position = new LatLngAltitude(37.4220, -122.0841, 10.0);

        MarkerOptions options = new MarkerOptions();
        options.setPosition(position);
        options.setAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);
        options.setLabel("Priority Marker");
        options.setCollisionBehavior(CollisionBehavior.REQUIRED);
        options.setExtruded(true);
        options.setDrawnWhenOccluded(true);

        Marker marker = map.addMarker(options);
    }
    // [END maps_android_3d_marker_options_java]

    // [START maps_android_3d_marker_click_java]
    /**
     * Adds a marker with a click listener.
     */
    public void handleMarkerClick() {
        LatLngAltitude position = new LatLngAltitude(37.42, -122.08, 0.0);
        MarkerOptions options = new MarkerOptions();
        options.setPosition(position);

        Marker marker = map.addMarker(options);

        // [START_EXCLUDE]
        if (marker != null) {
            marker.setClickListener(new OnMarkerClickListener() {
                @Override
                public void onMarkerClick() {
                    // Handle click
                }
            });
        }
        // [END_EXCLUDE]
    }
    // [END maps_android_3d_marker_click_java]

    // [START maps_android_3d_marker_custom_icon_java]
    /**
     * Adds a marker with a custom icon using PinConfiguration.
     */
    public void addCustomMarker(Context context) {
        LatLngAltitude position = new LatLngAltitude(37.4220, -122.0841, 10.0);

        // Create a Glyph with a custom image
        Glyph glyphImage = Glyph.fromColor(Color.YELLOW);
        ImageView imageView = new ImageView(context);
        imageView.setImageResource(R.mipmap.ic_launcher);
        
        // Glyph.setImage expects a com.google.android.gms.maps3d.model.ImageView? No, wait.
        // It likely expects an android.view.View or specialized type. 
        // Checking the Kotlin sample: glyphImage.setImage(ImageView(context)...)
        // If the error says "incompatible types: android.widget.ImageView cannot be converted to com.google.android.gms.maps3d.model.ImageView",
        // then the SDK has its own ImageView wrapper or I am importing the wrong one.
        // Actually, looking at the hierarchy, Glyph.setImage might take a View, but if there is a name collision...
        // Let's assume the SDK expects the Android ImageView but maybe the signature is weird?
        // Wait, the error `com.google.android.gms.maps3d.model.ImageView` suggests there IS a model class named ImageView.
        // I should check if I need to wrap it or use that class.
        // But the Kotlin sample used `android.widget.ImageView`. 
        // Let's look at the error again: "actual type is 'android.widget.ImageView', but 'com.google.android.gms.maps3d.model.ImageView' was expected."
        // This implies `Glyph` has a method `setImage(com.google.android.gms.maps3d.model.ImageView)`?
        // Or maybe I am shadowing it?
        
        // Let's try to use the SDK's ImageView if it exists, or check imports.
        // Actually, looking at the user provided code: `glyphImage.setImage(ImageView(R.drawable.ook))` 
        // The user code seems to use a constructor `ImageView(Int)`. Android's ImageView takes Context.
        // So `com.google.android.gms.maps3d.model.ImageView` must be a thing!
        
        com.google.android.gms.maps3d.model.ImageView mapImageView = new com.google.android.gms.maps3d.model.ImageView(R.mipmap.ic_launcher);
        glyphImage.setImage(mapImageView);

        MarkerOptions options = new MarkerOptions();
        options.setPosition(position);
        options.setLabel("Custom Icon Marker");
        
        // Set the style using PinConfiguration
        options.setStyle(PinConfiguration.builder()
                .setScale(1.5f)
                .setGlyph(glyphImage)
                .setBackgroundColor(Color.BLUE)
                .setBorderColor(Color.WHITE)
                .build());

        Marker marker = map.addMarker(options);
    }
    // [END maps_android_3d_marker_custom_icon_java]
}
