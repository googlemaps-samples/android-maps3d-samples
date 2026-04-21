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

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.snippets.kotlin.R
import com.example.snippets.kotlin.TrackedMap3D
import com.example.snippets.kotlin.annotations.SnippetGroup
import com.example.snippets.kotlin.annotations.SnippetItem
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.CollisionBehavior
import com.google.android.gms.maps3d.model.Glyph
import com.google.android.gms.maps3d.model.ImageView
import com.google.android.gms.maps3d.model.PinConfiguration
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.flyToOptions
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.gms.maps3d.model.markerOptions

@SnippetGroup(
    title = "Markers",
    description = "Snippets demonstrating standard, extruded, and custom styled markers.",
)
class MarkerSnippets(private val context: Context, private val map: TrackedMap3D) {

    /**
     * Adds a basic marker to the map.
     */
    @Suppress("unused")
    @SnippetItem(
        title = "1. Basic",
        description = "Adds a standard marker.",
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

        map.addMarker(options)
        // [START_EXCLUDE]
        map.flyCameraTo(
            flyToOptions {
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
            },
        )
        // [END_EXCLUDE]
        // [END maps_android_3d_marker_add_kt]
    }

    /**
     * Adds an advanced marker with detailed configuration options.
     */
    @Suppress("unused")
    @SnippetItem(
        title = "2. Advanced",
        description = "Adds a 'Priority Marker' that is extruded and collides with other markers.",
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

        map.addMarker(options)
        // [START_EXCLUDE]
        map.flyCameraTo(
            flyToOptions {
                endCamera = camera {
                    center = latLngAltitude {
                        latitude = options.position.latitude
                        longitude = options.position.longitude
                        altitude = 0.0
                    }
                    tilt = 45.0
                    heading = 0.0
                    range = 500.0
                }
                durationInMillis = 3000
            },
        )
        // [END_EXCLUDE]
        // [END maps_android_3d_marker_options_kt]
    }

    /**
     * Adds a marker with a click listener.
     */
    @Suppress("unused")
    @SnippetItem(
        title = "3. Click",
        description = "Adds a marker that logs a message when clicked.",
    )
    fun handleMarkerClick() {
        // [START maps_android_3d_marker_click_kt]
        val marker = map.addMarker(
            markerOptions {
                position = latLngAltitude {
                    latitude = 37.42
                    longitude = -122.08
                    altitude = 0.0
                }
            },
        )
        // [START_EXCLUDE]
        map.flyCameraTo(
            flyToOptions {
                endCamera = camera {
                    center = latLngAltitude {
                        latitude = 37.42
                        longitude = -122.08
                        altitude = 0.0
                    }
                    tilt = 45.0
                    heading = 0.0
                    range = 500.0
                }
                durationInMillis = 3000
            },
        )

        // [START_EXCLUDE]
        marker?.setClickListener {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "Marker Clicked!", Toast.LENGTH_SHORT).show()
            }
        }
        // [END_EXCLUDE]
        // [END maps_android_3d_marker_click_kt]
    }

    /**
     * Adds a marker with a custom icon using PinConfiguration.
     */
    @Suppress("unused")
    @SnippetItem(
        title = "4. Custom Icon",
        description = "Adds a marker with a custom icon using PinConfiguration and Glyph styling.",
    )
    fun addCustomMarker(context: Context) {
        // [START maps_android_3d_marker_custom_icon_kt]
        // Create a Glyph with a custom image
        val glyphImage = Glyph.fromColor(Color.YELLOW)
        // Use the Maps SDK's ImageView, not the Android widget
        glyphImage.setImage(ImageView(R.mipmap.ic_launcher))

        val options = markerOptions {
            position = latLngAltitude {
                latitude = 37.4220
                longitude = -122.0841
                altitude = 10.0
            }
            label = "Custom Icon Marker"
            // Set the style using PinConfiguration
            setStyle(
                PinConfiguration.builder()
                    .setScale(1.5f)
                    .setGlyph(glyphImage)
                    .setBackgroundColor(Color.BLUE)
                    .setBorderColor(Color.WHITE)
                    .build(),
            )
        }

        map.addMarker(options)
        // [START_EXCLUDE]
        map.flyCameraTo(
            flyToOptions {
                endCamera = camera {
                    center = latLngAltitude {
                        latitude = options.position.latitude
                        longitude = options.position.longitude
                        altitude = 0.0
                    }
                    tilt = 45.0
                    heading = 0.0
                    range = 500.0
                }
                durationInMillis = 3000
            },
        )
        // [END_EXCLUDE]
    }

    /**
     * Adds a marker with a colored Glyph.
     */
    @Suppress("unused")
    @SnippetItem(
        title = "5. Color Glyph",
        description = "Adds a marker with a customized glyph color.",
    )
    fun addMarkerWithColorGlyph() {
        // [START maps_android_3d_marker_glyph_color_kt]
        val options = markerOptions {
            position = latLngAltitude {
                latitude = 37.4220
                longitude = -122.0841
                altitude = 10.0
            }
            label = "Color Glyph"
            setStyle(
                PinConfiguration.builder()
                    .setGlyph(Glyph.fromColor(Color.CYAN))
                    .build(),
            )
        }
        map.addMarker(options)
        // [START_EXCLUDE]
        map.flyCameraTo(
            flyToOptions {
                endCamera = camera {
                    center = latLngAltitude {
                        latitude = options.position.latitude
                        longitude = options.position.longitude
                        altitude = 0.0
                    }
                    tilt = 45.0
                    heading = 0.0
                    range = 500.0
                }
                durationInMillis = 3000
            },
        )
        // [END_EXCLUDE]
        // [END maps_android_3d_marker_glyph_color_kt]
    }

    /**
     * Adds a marker with a text Glyph.
     */
    @Suppress("unused")
    @SnippetItem(
        title = "6. Text Glyph",
        description = "Adds a marker with text inside the glyph.",
    )
    fun addMarkerWithTextGlyph() {
        // [START maps_android_3d_marker_glyph_text_kt]
        val options = markerOptions {
            position = latLngAltitude {
                latitude = 37.4220
                longitude = -122.0841
                altitude = 10.0
            }
            label = "Text Glyph"
            setStyle(
                PinConfiguration.builder()
                    .setGlyph(Glyph.fromText("NYC"))
                    .build(),
            )
        }
        map.addMarker(options)
        // [START_EXCLUDE]
        map.flyCameraTo(
            flyToOptions {
                endCamera = camera {
                    center = latLngAltitude {
                        latitude = options.position.latitude
                        longitude = options.position.longitude
                        altitude = 0.0
                    }
                    tilt = 45.0
                    heading = 0.0
                    range = 500.0
                }
                durationInMillis = 3000
            },
        )
        // [END_EXCLUDE]
        // [END maps_android_3d_marker_glyph_text_kt]
    }

    /**
     * Adds a marker with a circle Glyph.
     */
    @Suppress("unused")
    @SnippetItem(
        title = "7. Circle Glyph",
        description = "Adds a marker with a default circle glyph.",
    )
    fun addMarkerWithCircleGlyph() {
        // [START maps_android_3d_marker_glyph_circle_kt]
        val glyph = Glyph.fromCircle()
        glyph.color = Color.MAGENTA

        val options = markerOptions {
            position = latLngAltitude {
                latitude = 37.4220
                longitude = -122.0841
                altitude = 10.0
            }
            label = "Circle Glyph"
            setStyle(
                PinConfiguration.builder()
                    .setGlyph(glyph)
                    .build(),
            )
        }
        map.addMarker(options)
        // [START_EXCLUDE]
        map.flyCameraTo(
            flyToOptions {
                endCamera = camera {
                    center = latLngAltitude {
                        latitude = options.position.latitude
                        longitude = options.position.longitude
                        altitude = 0.0
                    }
                    tilt = 45.0
                    heading = 0.0
                    range = 500.0
                }
                durationInMillis = 3000
            },
        )
        // [END_EXCLUDE]
        // [END maps_android_3d_marker_glyph_circle_kt]
    }
}
