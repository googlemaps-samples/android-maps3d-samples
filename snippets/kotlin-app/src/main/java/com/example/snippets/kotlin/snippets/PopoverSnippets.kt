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
import android.widget.TextView
import com.example.snippets.common.R
import com.example.snippets.kotlin.TrackedMap3D
import com.example.snippets.kotlin.annotations.SnippetGroup
import com.example.snippets.kotlin.annotations.SnippetItem
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.flyToOptions
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.gms.maps3d.model.markerOptions
import com.google.android.gms.maps3d.model.popoverOptions

@SnippetGroup(
    title = "Popovers",
    description = "Snippets demonstrating anchored and configured 3D Popover views.",
)
class PopoverSnippets(private val context: Context, private val map: TrackedMap3D) {

    /**
     * Adds a popover anchored to a marker.
     */
    @Suppress("unused")
    @SnippetItem(
        title = "1. Marker Anchor",
        description = "Adds a 'Hello Popover!' text bubble anchored to a marker",
    )
    fun addPopoverToMarker() {
        // [START maps_android_3d_popover_add_kt]
        // Create a marker first
        val markerOptions = markerOptions {
            position = latLngAltitude {
                latitude = 37.422
                longitude = -122.084
                altitude = 0.0
            }
        }
        val marker = map.addMarker(markerOptions) ?: return

        // Create a custom view for the popover
        val textView = TextView(context).apply {
            text = context.getString(R.string.popover_hello)
            setPadding(16, 16, 16, 16)
            setBackgroundColor(Color.WHITE)
        }

        // Add popover anchored to the marker
        val options = popoverOptions {
            content = textView
            positionAnchor = marker
            altitudeMode = AltitudeMode.RELATIVE_TO_GROUND
        }

        val popover = map.addPopover(options)

        // You can show/hide it
        popover?.show()
        // [END maps_android_3d_popover_add_kt]

        map.flyCameraTo(
            flyToOptions {
                endCamera = camera {
                    center = latLngAltitude {
                        latitude = 37.422
                        longitude = -122.084
                        altitude = 0.0
                    }
                    tilt = 45.0
                    heading = 0.0
                    range = 2000.0
                }
                durationInMillis = 1000
            },
        )
    }

    /**
     * Adds a configured popover (auto-close enabled, auto-pan disabled).
     */
    @Suppress("unused")
    @SnippetItem(
        title = "2. Configured",
        description = "Adds an 'Info' popover anchored to a marker with auto-close enabled and auto-pan disabled.",
    )
    fun addConfiguredPopover() {
        // [START maps_android_3d_popover_options_kt]
        // Create a marker at an interesting location (e.g., Golden Gate Bridge)
        val markerOptions = markerOptions {
            position = latLngAltitude {
                latitude = 37.8199
                longitude = -122.4783
                altitude = 0.0
            }
            label = "Golden Gate Bridge"
        }
        val marker = map.addMarker(markerOptions) ?: return

        val textView = TextView(context).apply {
            text = context.getString(R.string.popover_info)
            setPadding(16, 16, 16, 16)
            setBackgroundColor(Color.WHITE)
        }

        val options = popoverOptions {
            content = textView
            positionAnchor = marker
            autoCloseEnabled = true // Close when clicking elsewhere
            autoPanEnabled = false // Do not pan to popover
        }

        val popover = map.addPopover(options)
        popover?.show()
        // [END maps_android_3d_popover_options_kt]

        map.flyCameraTo(
            flyToOptions {
                endCamera = camera {
                    center = latLngAltitude {
                        latitude = 37.8199
                        longitude = -122.4783
                        altitude = 0.0
                    }
                    tilt = 45.0
                    heading = 0.0
                    range = 1000.0
                }
                durationInMillis = 2000
            },
        )
    }
}
