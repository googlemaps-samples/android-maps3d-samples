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

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import com.google.android.gms.maps3d.GoogleMap3D;
import com.example.snippets.java.TrackedMap3D;
import com.google.android.gms.maps3d.model.AltitudeMode;
import com.google.android.gms.maps3d.model.CollisionBehavior;
import com.google.android.gms.maps3d.model.LatLngAltitude;
import com.google.android.gms.maps3d.model.Marker;
import com.google.android.gms.maps3d.model.MarkerOptions;
import com.google.android.gms.maps3d.model.PinConfiguration;
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.FlyToOptions;
import com.google.android.gms.maps3d.model.Glyph;
import com.example.snippets.java.R;
import android.content.Context;
import android.graphics.Color;
import android.widget.ImageView;
import com.google.android.gms.maps3d.OnMarkerClickListener;
import com.example.snippets.java.annotations.SnippetGroup;
import com.example.snippets.java.annotations.SnippetItem;

@SnippetGroup(
    title = "Markers",
    description = "Snippets demonstrating standard, extruded, and custom styled markers."
)
public class MarkerSnippets {

    private final Context context;
    private final TrackedMap3D map;

    public MarkerSnippets(Context context, TrackedMap3D map) {
        this.context = context;
        this.map = map;
    }

    /**
     * Adds a basic marker to the map.
     */
    @SuppressWarnings("unused")
    @SnippetItem(
        title = "1. Basic",
        description = "Adds a standard marker."
    )
    public void addBasicMarker() {
        // [START maps_android_3d_marker_add_java]
        LatLngAltitude position = new LatLngAltitude(37.4220, -122.0841, 10.0);

        MarkerOptions options = new MarkerOptions();
        options.setPosition(position);
        options.setLabel("Basic Marker");
        // MarkerOptions uses label, not title.

        Marker marker = map.addMarker(options);
        // [START_EXCLUDE]
        LatLngAltitude markerPos = options.getPosition();
        if (markerPos != null) {
            LatLngAltitude camCenter = new LatLngAltitude(markerPos.getLatitude(), markerPos.getLongitude(), 0.0);
            Camera targetCamera = new Camera(camCenter, 0.0, 45.0, 0.0, 500.0);
            map.flyCameraTo(new FlyToOptions(targetCamera, 3000L));
        }
        // [END_EXCLUDE]
        // [END maps_android_3d_marker_add_java]
    }

    /**
     * Adds an advanced marker with detailed configuration options.
     */
    @SuppressWarnings("unused")
    @SnippetItem(
        title = "2. Advanced",
        description = "Adds a 'Priority Marker' that is extruded and collides with other markers."
    )
    public void addAdvancedMarker() {
        // [START maps_android_3d_marker_options_java]
        LatLngAltitude position = new LatLngAltitude(37.4220, -122.0841, 10.0);

        MarkerOptions options = new MarkerOptions();
        options.setPosition(position);
        options.setAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);
        options.setLabel("Priority Marker");
        options.setCollisionBehavior(CollisionBehavior.OPTIONAL_AND_HIDES_LOWER_PRIORITY);
        options.setExtruded(true);
        options.setDrawnWhenOccluded(true);

        Marker marker = map.addMarker(options);
        // [START_EXCLUDE]
        LatLngAltitude markerPos = options.getPosition();
        if (markerPos != null) {
            LatLngAltitude camCenter = new LatLngAltitude(markerPos.getLatitude(), markerPos.getLongitude(), 0.0);
            Camera targetCamera = new Camera(camCenter, 0.0, 45.0, 0.0, 500.0);
            map.flyCameraTo(new FlyToOptions(targetCamera, 3000L));
        }
        // [END_EXCLUDE]
        // [END maps_android_3d_marker_options_java]
    }

    /**
     * Adds a marker with a click listener.
     */
    @SuppressWarnings("unused")
    @SnippetItem(
        title = "3. Click",
        description = "Adds a marker that logs a message when clicked."
    )
    public void handleMarkerClick() {
        // [START maps_android_3d_marker_click_java]
        LatLngAltitude position = new LatLngAltitude(37.42, -122.08, 0.0);
        MarkerOptions options = new MarkerOptions();
        options.setPosition(position);

        Marker marker = map.addMarker(options);
        // [START_EXCLUDE]
        LatLngAltitude markerPos = options.getPosition();
        if (markerPos != null) {
            LatLngAltitude camCenter = new LatLngAltitude(markerPos.getLatitude(), markerPos.getLongitude(), 0.0);
            Camera targetCamera = new Camera(camCenter, 0.0, 45.0, 0.0, 500.0);
            map.flyCameraTo(new FlyToOptions(targetCamera, 3000L));
        }
        // [END_EXCLUDE]

        // [START_EXCLUDE]
        if (marker != null) {
            marker.setClickListener(new OnMarkerClickListener() {
                @Override
                public void onMarkerClick() {
                    new Handler(Looper.getMainLooper()).post(() -> 
                        Toast.makeText(context, "Marker Clicked!", Toast.LENGTH_SHORT).show()
                    );
                }
            });
        }
        // [END_EXCLUDE]
        // [END maps_android_3d_marker_click_java]
    }

    /**
     * Adds a marker with a custom icon using PinConfiguration.
     */
    @SuppressWarnings("unused")
    @SnippetItem(
        title = "4. Custom Icon",
        description = "Adds a marker with a custom icon using PinConfiguration and Glyph styling."
    )
    public void addCustomMarker(Context context) {
        // [START maps_android_3d_marker_custom_icon_java]
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
        // [START_EXCLUDE]
        LatLngAltitude markerPos = options.getPosition();
        if (markerPos != null) {
            LatLngAltitude camCenter = new LatLngAltitude(markerPos.getLatitude(), markerPos.getLongitude(), 0.0);
            Camera targetCamera = new Camera(camCenter, 0.0, 45.0, 0.0, 500.0);
            map.flyCameraTo(new FlyToOptions(targetCamera, 3000L));
        }
        // [END_EXCLUDE]
        // [END maps_android_3d_marker_custom_icon_java]
    }
}
