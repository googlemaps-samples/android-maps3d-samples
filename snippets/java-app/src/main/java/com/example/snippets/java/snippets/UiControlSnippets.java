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
import com.example.snippets.java.TrackedMap3D;
import com.example.snippets.java.annotations.SnippetGroup;
import com.example.snippets.java.annotations.SnippetItem;
import com.google.android.gms.maps3d.UiControlSettings;
import com.google.android.gms.maps3d.UiControlSettings.ControlOrientation;
import com.google.android.gms.maps3d.UiControlSettings.ControlPlacement;
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.CompassControl;
import com.google.android.gms.maps3d.model.FlyToOptions;
import com.google.android.gms.maps3d.model.LatLngAltitude;
import com.google.android.gms.maps3d.model.Map3DControl;
import com.google.android.gms.maps3d.model.PanHorizontalControl;
import com.google.android.gms.maps3d.model.PanVerticalControl;
import com.google.android.gms.maps3d.model.RotateControl;
import com.google.android.gms.maps3d.model.Spacer;
import com.google.android.gms.maps3d.model.TiltControl;
import com.google.android.gms.maps3d.model.ZoomControl;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SnippetGroup(
        title = "UI Controls",
        description =
                "Snippets demonstrating built-in 3D map camera UI controls and screen placement.")
public class UiControlSnippets {

    private final TrackedMap3D map;

    public UiControlSnippets(TrackedMap3D map) {
        this.map = map;
    }

    /**
     * Configures the built-in 3D camera UI controls on the trailing and leading edges of the map.
     */
    @SuppressWarnings("unused")
    @SnippetItem(
            title = "1. Configure UI Controls Placement",
            description =
                    "Configures Zoom, Tilt, and Rotate controls on the trailing edge and Pan and Compass controls on the leading edge.")
    public void configureUiControlsPlacement() {
        focusSampleScene();
        // [START maps_android_3d_ui_controls_placement_java]
        UiControlSettings uiSettings = map.getUiControlSettings();
        if (uiSettings != null) {
            List<Map3DControl> trailingControls =
                    Arrays.asList(
                            new ZoomControl(ControlOrientation.VERTICAL),
                            new TiltControl(ControlOrientation.VERTICAL),
                            new RotateControl(ControlOrientation.VERTICAL));
            uiSettings.setUiControlsPlacement(trailingControls, ControlPlacement.TRAILING);

            List<Map3DControl> leadingControls =
                    Arrays.asList(
                            new PanVerticalControl(ControlOrientation.VERTICAL),
                            new PanHorizontalControl(ControlOrientation.VERTICAL),
                            new CompassControl());
            uiSettings.setUiControlsPlacement(leadingControls, ControlPlacement.LEADING);
        }
        // [END maps_android_3d_ui_controls_placement_java]
    }

    /**
     * Swaps the main camera controls to the leading edge and secondary pan/compass controls to the
     * trailing edge.
     */
    @SuppressWarnings("unused")
    @SnippetItem(
            title = "2. Swap Leading & Trailing Edges",
            description =
                    "Moves main camera controls (Zoom, Tilt, Rotate) to the leading edge and Pan/Compass controls to the trailing edge.")
    public void swapUiControlsEdges() {
        focusSampleScene();
        // [START maps_android_3d_ui_controls_swap_edges_java]
        UiControlSettings uiSettings = map.getUiControlSettings();
        if (uiSettings != null) {
            List<Map3DControl> leadingControls =
                    Arrays.asList(
                            new ZoomControl(ControlOrientation.VERTICAL),
                            new TiltControl(ControlOrientation.VERTICAL),
                            new RotateControl(ControlOrientation.VERTICAL));
            uiSettings.setUiControlsPlacement(leadingControls, ControlPlacement.LEADING);

            List<Map3DControl> trailingControls =
                    Arrays.asList(
                            new PanVerticalControl(ControlOrientation.VERTICAL),
                            new PanHorizontalControl(ControlOrientation.VERTICAL),
                            new CompassControl());
            uiSettings.setUiControlsPlacement(trailingControls, ControlPlacement.TRAILING);
        }
        // [END maps_android_3d_ui_controls_swap_edges_java]
    }

    /** Configures horizontal UI controls with spacers along the top and bottom screen edges. */
    @SuppressWarnings("unused")
    @SnippetItem(
            title = "3. Horizontal & Top/Bottom Controls",
            description =
                    "Configures horizontal control orientation with Spacers along the top and bottom edges of the map.")
    public void configureHorizontalTopBottomControls() {
        focusSampleScene();
        // [START maps_android_3d_ui_controls_orientation_java]
        UiControlSettings uiSettings = map.getUiControlSettings();
        if (uiSettings != null) {
            List<Map3DControl> topControls =
                    Arrays.asList(
                            new CompassControl(),
                            new Spacer(),
                            new RotateControl(ControlOrientation.HORIZONTAL),
                            new TiltControl(ControlOrientation.HORIZONTAL));
            uiSettings.setUiControlsPlacement(topControls, ControlPlacement.TOP);

            List<Map3DControl> bottomControls =
                    Arrays.asList(
                            new ZoomControl(ControlOrientation.HORIZONTAL),
                            new Spacer(),
                            new PanHorizontalControl(ControlOrientation.HORIZONTAL),
                            new PanVerticalControl(ControlOrientation.HORIZONTAL));
            uiSettings.setUiControlsPlacement(bottomControls, ControlPlacement.BOTTOM);
        }
        // [END maps_android_3d_ui_controls_orientation_java]
    }

    /** Hides all built-in 3D camera UI controls by passing an empty list to each placement edge. */
    @SuppressWarnings("unused")
    @SnippetItem(
            title = "4. Hide UI Controls",
            description =
                    "Displays UI controls briefly and then hides all controls across every screen edge using an empty list.")
    public void hideUiControls() {
        focusSampleScene();
        // [START_EXCLUDE]
        UiControlSettings initialSettings = map.getUiControlSettings();
        if (initialSettings != null) {
            initialSettings.setUiControlsPlacement(
                    Arrays.asList(
                            new ZoomControl(null), new TiltControl(null), new RotateControl(null)),
                    ControlPlacement.TRAILING);
            initialSettings.setUiControlsPlacement(
                    Arrays.asList(
                            new PanVerticalControl(null),
                            new PanHorizontalControl(null),
                            new CompassControl()),
                    ControlPlacement.LEADING);
        }
        // [END_EXCLUDE]

        new Handler(Looper.getMainLooper())
                .postDelayed(
                        () -> {
                            // [START maps_android_3d_ui_controls_hide_java]
                            UiControlSettings uiSettings = map.getUiControlSettings();
                            if (uiSettings != null) {
                                uiSettings.setUiControlsPlacement(
                                        Collections.emptyList(), ControlPlacement.TRAILING);
                                uiSettings.setUiControlsPlacement(
                                        Collections.emptyList(), ControlPlacement.LEADING);
                                uiSettings.setUiControlsPlacement(
                                        Collections.emptyList(), ControlPlacement.TOP);
                                uiSettings.setUiControlsPlacement(
                                        Collections.emptyList(), ControlPlacement.BOTTOM);
                            }
                            // [END maps_android_3d_ui_controls_hide_java]
                        },
                        2000);
    }

    private void focusSampleScene() {
        Camera targetCamera =
                new Camera(new LatLngAltitude(37.8199, -122.4783, 0.0), 330.0, 55.0, 0.0, 1500.0);
        map.flyCameraTo(new FlyToOptions(targetCamera, 1500L));
    }
}
