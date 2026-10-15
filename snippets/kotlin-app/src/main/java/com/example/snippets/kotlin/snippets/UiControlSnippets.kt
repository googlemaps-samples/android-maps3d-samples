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

import com.example.snippets.kotlin.TrackedMap3D
import com.example.snippets.kotlin.annotations.SnippetGroup
import com.example.snippets.kotlin.annotations.SnippetItem
import com.google.android.gms.maps3d.UiControlSettings.ControlOrientation
import com.google.android.gms.maps3d.UiControlSettings.ControlPlacement
import com.google.android.gms.maps3d.model.CompassControl
import com.google.android.gms.maps3d.model.PanHorizontalControl
import com.google.android.gms.maps3d.model.PanVerticalControl
import com.google.android.gms.maps3d.model.RotateControl
import com.google.android.gms.maps3d.model.Spacer
import com.google.android.gms.maps3d.model.TiltControl
import com.google.android.gms.maps3d.model.ZoomControl
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.flyToOptions
import com.google.android.gms.maps3d.model.latLngAltitude
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

/**
 * Snippets demonstrating built-in 3D map UI control buttons and placement configuration.
 */
@SnippetGroup(
    title = "UI Controls",
    description = "Snippets demonstrating built-in 3D map camera UI controls and screen placement.",
)
class UiControlSnippets(
    private val map: TrackedMap3D,
    private val lifecycleScope: CoroutineScope,
) {

    /**
     * Configures the built-in 3D camera UI controls on the trailing and leading edges of the map.
     */
    @Suppress("unused")
    @SnippetItem(
        title = "1. Configure UI Controls Placement",
        description = "Configures Zoom, Tilt, and Rotate controls on the trailing edge and Pan and Compass controls on the leading edge.",
    )
    fun configureUiControlsPlacement() {
        focusSampleScene()
        // [START maps_android_3d_ui_controls_placement_kt]
        map.getUiControlSettings()?.let { uiSettings ->
            val trailingControls = listOf(
                ZoomControl(ControlOrientation.VERTICAL),
                TiltControl(ControlOrientation.VERTICAL),
                RotateControl(ControlOrientation.VERTICAL),
            )
            uiSettings.setUiControlsPlacement(trailingControls, ControlPlacement.TRAILING)

            val leadingControls = listOf(
                PanVerticalControl(ControlOrientation.VERTICAL),
                PanHorizontalControl(ControlOrientation.VERTICAL),
                CompassControl(),
            )
            uiSettings.setUiControlsPlacement(leadingControls, ControlPlacement.LEADING)
        }
        // [END maps_android_3d_ui_controls_placement_kt]
    }

    /**
     * Swaps the main camera controls to the leading edge and secondary pan/compass controls to the trailing edge.
     */
    @Suppress("unused")
    @SnippetItem(
        title = "2. Swap Leading & Trailing Edges",
        description = "Moves main camera controls (Zoom, Tilt, Rotate) to the leading edge and Pan/Compass controls to the trailing edge.",
    )
    fun swapUiControlsEdges() {
        focusSampleScene()
        // [START maps_android_3d_ui_controls_swap_edges_kt]
        map.getUiControlSettings()?.let { uiSettings ->
            val leadingControls = listOf(
                ZoomControl(ControlOrientation.VERTICAL),
                TiltControl(ControlOrientation.VERTICAL),
                RotateControl(ControlOrientation.VERTICAL),
            )
            uiSettings.setUiControlsPlacement(leadingControls, ControlPlacement.LEADING)

            val trailingControls = listOf(
                PanVerticalControl(ControlOrientation.VERTICAL),
                PanHorizontalControl(ControlOrientation.VERTICAL),
                CompassControl(),
            )
            uiSettings.setUiControlsPlacement(trailingControls, ControlPlacement.TRAILING)
        }
        // [END maps_android_3d_ui_controls_swap_edges_kt]
    }

    /**
     * Configures horizontal UI controls with spacers along the top and bottom screen edges.
     */
    @Suppress("unused")
    @SnippetItem(
        title = "3. Horizontal & Top/Bottom Controls",
        description = "Configures horizontal control orientation with Spacers along the top and bottom edges of the map.",
    )
    fun configureHorizontalTopBottomControls() {
        focusSampleScene()
        // [START maps_android_3d_ui_controls_orientation_kt]
        map.getUiControlSettings()?.let { uiSettings ->
            val topControls = listOf(
                CompassControl(),
                Spacer(),
                RotateControl(ControlOrientation.HORIZONTAL),
                TiltControl(ControlOrientation.HORIZONTAL),
            )
            uiSettings.setUiControlsPlacement(topControls, ControlPlacement.TOP)

            val bottomControls = listOf(
                ZoomControl(ControlOrientation.HORIZONTAL),
                Spacer(),
                PanHorizontalControl(ControlOrientation.HORIZONTAL),
                PanVerticalControl(ControlOrientation.HORIZONTAL),
            )
            uiSettings.setUiControlsPlacement(bottomControls, ControlPlacement.BOTTOM)
        }
        // [END maps_android_3d_ui_controls_orientation_kt]
    }

    /**
     * Hides all built-in 3D camera UI controls by passing an empty list to each placement edge.
     */
    @Suppress("unused")
    @SnippetItem(
        title = "4. Hide UI Controls",
        description = "Displays UI controls briefly and then hides all controls across every screen edge using an empty list.",
    )
    fun hideUiControls() {
        focusSampleScene()
        // [START_EXCLUDE]
        map.getUiControlSettings()?.let { uiSettings ->
            uiSettings.setUiControlsPlacement(
                listOf(ZoomControl(null), TiltControl(null), RotateControl(null)),
                ControlPlacement.TRAILING,
            )
            uiSettings.setUiControlsPlacement(
                listOf(PanVerticalControl(null), PanHorizontalControl(null), CompassControl()),
                ControlPlacement.LEADING,
            )
        }
        // [END_EXCLUDE]

        lifecycleScope.launch {
            // [START_EXCLUDE]
            delay(2.seconds)
            // [END_EXCLUDE]
            // [START maps_android_3d_ui_controls_hide_kt]
            map.getUiControlSettings()?.let { uiSettings ->
                uiSettings.setUiControlsPlacement(emptyList(), ControlPlacement.TRAILING)
                uiSettings.setUiControlsPlacement(emptyList(), ControlPlacement.LEADING)
                uiSettings.setUiControlsPlacement(emptyList(), ControlPlacement.TOP)
                uiSettings.setUiControlsPlacement(emptyList(), ControlPlacement.BOTTOM)
            }
            // [END maps_android_3d_ui_controls_hide_kt]
        }
    }

    private fun focusSampleScene() {
        map.flyCameraTo(
            flyToOptions {
                endCamera = camera {
                    center = latLngAltitude {
                        latitude = 37.8199
                        longitude = -122.4783
                        altitude = 0.0
                    }
                    heading = 330.0
                    tilt = 55.0
                    range = 1500.0
                }
                durationInMillis = 1500
            },
        )
    }
}
