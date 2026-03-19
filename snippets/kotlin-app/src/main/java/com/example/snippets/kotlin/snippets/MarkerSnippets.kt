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
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.flyToOptions
import com.google.android.gms.maps3d.model.PinConfiguration
import com.google.android.gms.maps3d.model.Glyph
import com.example.snippets.kotlin.R
import android.content.Context
import android.graphics.Color
import android.widget.ImageView
import com.example.snippets.kotlin.annotations.SnippetGroup
import com.example.snippets.kotlin.annotations.SnippetItem

@SnippetGroup(
    title = "Markers",
    description = "Snippets demonstrating standard, extruded, and custom styled markers."
)
class MarkerSnippets(private val map: GoogleMap3D) {

    /**
     * Adds a basic marker to the map.
     */
    @Suppress("unused")
    @SnippetItem(
        title = "Basic",
        description = "Adds a standard marker at Lat: 37.422, Lng: -122.084, Alt: 10m."
    )
    fun addBasicMarker() {
        // [START maps_android_3d_marker_add_kt]
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
        // [START_EXCLUDE]
        map.flyCameraTo(flyToOptions {
            endCamera = camera {
                center = latLngAltitude {
                    latitude = position.latitude
                    longitude = position.longitude
                    altitude = 0.0
                }
                tilt = 45.0
                heading = 0.0
                range = 500.0
            }
            durationInMillis = 3000 // Slightly longer
        })
        // [END_EXCLUDE]
        // [END maps_android_3d_marker_add_kt]
    }

    /**
     * Adds an advanced marker with detailed configuration options.
     */
    @Suppress("unused")
    @SnippetItem(
        title = "Advanced",
        description = "Adds a 'Priority Marker' at Lat: 37.422, Lng: -122.084, Alt: 10m (Relative to Ground) that is extruded and collides with other markers."
    )
    fun addAdvancedMarker() {
        // [START maps_android_3d_marker_options_kt]
        val options = markerOptions {
            position = latLngAltitude {
                latitude = 37.4220
                longitude = -122.0841
                altitude = 10.0
            }
            altitudeMode = AltitudeMode.RELATIVE_TO_GROUND
            label = "Priority Marker"
            collisionBehavior = CollisionBehavior.OPTIONAL_AND_HIDES_LOWER_PRIORITY
            isExtruded = true
            isDrawnWhenOccluded = true
        }
        
        val marker = map.addMarker(options)
        // [START_EXCLUDE]
        map.flyCameraTo(flyToOptions {
            endCamera = camera {
                center = latLngAltitude {
                    latitude = options.position!!.latitude
                    longitude = options.position!!.longitude
                    altitude = 0.0
                }
                tilt = 45.0
                heading = 0.0
                range = 500.0
            }
            durationInMillis = 3000
        })
        // [END_EXCLUDE]
        // [END maps_android_3d_marker_options_kt]
    }

    /**
     * Adds a marker with a click listener.
     */
    @Suppress("unused")
    @SnippetItem(
        title = "Click",
        description = "Adds a marker at Lat: 37.42, Lng: -122.08 that logs a message when clicked."
    )
    fun handleMarkerClick() {
        // [START maps_android_3d_marker_click_kt]
        val marker = map.addMarker(markerOptions {
            position = latLngAltitude { latitude = 37.42; longitude = -122.08; altitude = 0.0 }
        })
        // [START_EXCLUDE]
        map.flyCameraTo(flyToOptions {
            endCamera = camera {
                center = latLngAltitude { latitude = 37.42; longitude = -122.08; altitude = 0.0 }
                tilt = 45.0
                heading = 0.0
                range = 500.0
            }
            durationInMillis = 3000
        })

        // [START_EXCLUDE]
        marker?.setClickListener {
            // Handle click
        }
        // [END_EXCLUDE]
        // [END maps_android_3d_marker_click_kt]
    }

    /**
     * Adds a marker with a custom icon using PinConfiguration.
     */
    @Suppress("unused")
    @SnippetItem(
        title = "Custom Icon",
        description = "Adds a marker with a custom icon using PinConfiguration and Glyph styling."
    )
    fun addCustomMarker(context: Context) {
        // [START maps_android_3d_marker_custom_icon_kt]
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
        // [START_EXCLUDE]
        map.flyCameraTo(flyToOptions {
            endCamera = camera {
                center = latLngAltitude {
                    latitude = options.position!!.latitude
                    longitude = options.position!!.longitude
                    altitude = 0.0
                }
                tilt = 45.0
                heading = 0.0
                range = 500.0
            }
            durationInMillis = 3000
        })
        // [END_EXCLUDE]
        // [END maps_android_3d_marker_custom_icon_kt]
    }
}
