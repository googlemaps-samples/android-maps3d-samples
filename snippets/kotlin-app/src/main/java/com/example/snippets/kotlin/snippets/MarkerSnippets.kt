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

package com.example.snippets.kotlin.snippets

import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.CollisionBehavior
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.gms.maps3d.model.markerOptions
import com.google.android.gms.maps3d.model.PinConfiguration
import com.google.android.gms.maps3d.model.Glyph
import com.example.snippets.kotlin.R
import android.content.Context
import android.graphics.Color
import android.widget.ImageView

class MarkerSnippets(private val map: GoogleMap3D) {

    // [START maps_android_3d_marker_add_kt]
    /**
     * Adds a basic marker to the map.
     */
    fun addBasicMarker() {
        val position = latLngAltitude {
            latitude = 37.4220
            longitude = -122.0841
            altitude = 10.0
        }
        
        val options = markerOptions {
            this.position = position
            label = "Basic Marker"
            // MarkerOptions uses label, not title.
        }
        
        val marker = map.addMarker(options)
    }
    // [END maps_android_3d_marker_add_kt]

    // [START maps_android_3d_marker_options_kt]
    /**
     * Adds an advanced marker with detailed configuration options.
     */
    fun addAdvancedMarker() {
        val options = markerOptions {
            position = latLngAltitude {
                latitude = 37.4220
                longitude = -122.0841
                altitude = 10.0
            }
            altitudeMode = AltitudeMode.RELATIVE_TO_GROUND
            label = "Priority Marker"
            collisionBehavior = CollisionBehavior.REQUIRED
            isExtruded = true
            isDrawnWhenOccluded = true
        }
        
        val marker = map.addMarker(options)
    }
    // [END maps_android_3d_marker_options_kt]

    // [START maps_android_3d_marker_click_kt]
    /**
     * Adds a marker with a click listener.
     */
    fun handleMarkerClick() {
        val marker = map.addMarker(markerOptions {
            position = latLngAltitude { latitude = 37.42; longitude = -122.08; altitude = 0.0 }
        })

        // [START_EXCLUDE]
        marker?.setClickListener {
            // Handle click
        }
        // [END_EXCLUDE]
    }
    // [END maps_android_3d_marker_click_kt]

    // [START maps_android_3d_marker_custom_icon_kt]
    /**
     * Adds a marker with a custom icon using PinConfiguration.
     */
    fun addCustomMarker(context: Context) {
        // Create a Glyph with a custom image
        val glyphImage = Glyph.fromColor(Color.YELLOW)
        // Use the Maps SDK's ImageView, not the Android widget
        glyphImage.setImage(com.google.android.gms.maps3d.model.ImageView(R.mipmap.ic_launcher))

        val options = markerOptions {
            position = latLngAltitude {
                latitude = 37.4220
                longitude = -122.0841
                altitude = 10.0
            }
            label = "Custom Icon Marker"
            // Set the style using PinConfiguration
            setStyle(PinConfiguration.builder()
                .setScale(1.5f)
                .setGlyph(glyphImage)
                .setBackgroundColor(Color.BLUE)
                .setBorderColor(Color.WHITE)
                .build())
        }

        val marker = map.addMarker(options)
    }
    // [END maps_android_3d_marker_custom_icon_kt]
}
