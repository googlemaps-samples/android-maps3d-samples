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

package com.example.maps3djava.projection3d;

import static com.example.maps3d.common.UtilitiesKt.toValidCamera;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.example.maps3d.common.Projection3D;
import com.example.maps3d.common.ScreenCoordinate;
import com.example.maps3dcommon.R;
import com.example.maps3djava.sampleactivity.SampleBaseActivity;
import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.FlyToOptions;
import com.google.android.gms.maps3d.model.LatLngAltitude;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.Arrays;
import java.util.List;

/**
 * Demonstrates 3D-to-2D Screen Projection using [Projection3D] in Android Views (Java).
 *
 * Key Concepts Demonstrated:
 * 1. Perspective Screen Coordinate Transformation:
 *    - Transforms 3D landmark coordinates ([LatLngAltitude]) to 2D screen viewport pixels using
 *      [Projection3D.toScreenCoordinate] based on real-time camera parameters.
 *
 * 2. Anchoring Native Android Views over 3D World Geometry:
 *    - Dynamically translates a native 2D [CardView] overlay across (X, Y) screen space to stay
 *      tightly anchored above the landmark as the camera pans, tilts, zooms, or orbits.
 *
 * 3. Frustum Clipping & Depth Detection:
 *    - Automatically hides or displays the floating callout based on whether the 3D coordinate is
 *      inside the visible camera view frustum.
 *
 * 4. Interactive Map Tap Re-Projection:
 *    - Tapping anywhere on the 3D map surface moves the focus target to the tapped point.
 */
public class Projection3DActivity extends SampleBaseActivity {

    @NonNull
    @Override
    public String getTAG() {
        return "Projection3DActivity";
    }

    @NonNull
    @Override
    public Camera getInitialCamera() {
        return toValidCamera(new Camera(
            new LatLngAltitude(
                SF_LANDMARKS.get(0).location.getLatitude(),
                SF_LANDMARKS.get(0).location.getLongitude(),
                150.0
            ),
            45.0,
            65.0,
            0.0,
            800.0
        ));
    }

    // --- UI Elements ---

    private View floatingCallout;
    private TextView calloutTitle;
    private TextView calloutCoords;
    private TextView calloutAltitude;

    private CardView controlsCard;
    private View cardHeader;
    private View cardContent;
    private MaterialButton btnCollapse;

    private TextView tvTarget;
    private TextView tvScreenCoords;
    private TextView tvStatus;

    // --- State ---

    private String activeLandmarkName = SF_LANDMARKS.get(0).name;
    private LatLngAltitude activeLocation = SF_LANDMARKS.get(0).location;
    private boolean isCollapsed = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide default scroll view from base skeleton
        View baseScrollView = findViewById(R.id.control_scroll_view);
        if (baseScrollView != null) {
            baseScrollView.setVisibility(View.GONE);
        }

        // Inflate Projection 3D overlay panel into map container
        ViewGroup container = findViewById(R.id.map_container);
        if (container != null) {
            getLayoutInflater().inflate(R.layout.control_panel_projection_3d, container, true);
        }

        MaterialToolbar topBar = findViewById(R.id.top_bar);
        if (topBar != null) {
            topBar.setTitle(R.string.feature_title_projection_3d);
            topBar.setNavigationOnClickListener(v -> finish());
        }

        initViews();
    }

    private void initViews() {
        floatingCallout = findViewById(R.id.floating_callout);
        calloutTitle = findViewById(R.id.callout_title);
        calloutCoords = findViewById(R.id.callout_coords);
        calloutAltitude = findViewById(R.id.callout_altitude);

        controlsCard = findViewById(R.id.control_panel);
        cardHeader = findViewById(R.id.card_header);
        cardContent = findViewById(R.id.card_content);
        btnCollapse = findViewById(R.id.btn_collapse);

        tvTarget = findViewById(R.id.tv_projection_target);
        tvScreenCoords = findViewById(R.id.tv_projection_screen_coords);
        tvStatus = findViewById(R.id.tv_projection_status);

        if (btnCollapse != null) {
            btnCollapse.setOnClickListener(v -> toggleControls());
        }

        if (cardHeader != null) {
            cardHeader.setOnClickListener(v -> toggleControls());
        }

        Button btnTransamerica = findViewById(R.id.btn_landmark_transamerica);
        if (btnTransamerica != null) {
            btnTransamerica.setOnClickListener(v -> selectLandmark(SF_LANDMARKS.get(0)));
        }

        Button btnCoit = findViewById(R.id.btn_landmark_coit);
        if (btnCoit != null) {
            btnCoit.setOnClickListener(v -> selectLandmark(SF_LANDMARKS.get(1)));
        }

        Button btnFerry = findViewById(R.id.btn_landmark_ferry);
        if (btnFerry != null) {
            btnFerry.setOnClickListener(v -> selectLandmark(SF_LANDMARKS.get(2)));
        }
    }

    @Override
    public void onMap3DViewReady(@NonNull GoogleMap3D googleMap3D) {
        super.onMap3DViewReady(googleMap3D);

        // Enable surface tap to re-project arbitrary coordinates
        googleMap3D.setMap3DClickListener((location, placeId) -> {
            activeLandmarkName = "Custom Ground Pin";
            activeLocation = location;
            Camera cam = googleMap3D.getCamera();
            if (cam != null) {
                updateProjection(cam);
            }
        });

        // Listen for camera updates
        googleMap3D.setCameraChangedListener(this::updateProjection);
    }

    private void selectLandmark(LandmarkData landmark) {
        activeLandmarkName = landmark.name;
        activeLocation = landmark.location;

        if (googleMap3D != null) {
            Camera curr = googleMap3D.getCamera();
            if (curr == null) {
                curr = getInitialCamera();
            }
            double heading = curr.getHeading() != null ? curr.getHeading() : 45.0;
            double tilt = curr.getTilt() != null ? curr.getTilt() : 65.0;
            Camera endCamera = toValidCamera(new Camera(
                landmark.location,
                heading,
                tilt,
                0.0,
                750.0
            ));
            googleMap3D.flyCameraTo(new FlyToOptions(endCamera, 1500));
        }
    }

    private void updateProjection(Camera currentCamera) {
        if (map3DView == null) return;
        int width = map3DView.getWidth();
        int height = map3DView.getHeight();
        if (width <= 0 || height <= 0) return;

        Projection3D projection = new Projection3D(
            currentCamera,
            width,
            height,
            Projection3D.DEFAULT_FOV_Y_DEGREES
        );

        ScreenCoordinate screenCoord = projection.toScreenCoordinate(activeLocation);

        runOnUiThread(() -> {
            if (tvTarget != null) {
                tvTarget.setText(getString(R.string.projection_target_format, activeLandmarkName));
            }

            if (screenCoord.isVisible() && !Float.isNaN(screenCoord.getX()) && !Float.isNaN(screenCoord.getY())) {
                if (tvScreenCoords != null) {
                    tvScreenCoords.setText(getString(
                        R.string.projection_screen_coords_format,
                        (int) screenCoord.getX(),
                        (int) screenCoord.getY()
                    ));
                }
                if (tvStatus != null) {
                    tvStatus.setText(getString(R.string.projection_visible));
                    tvStatus.setTextColor(0xFF2E7D32);
                }

                if (floatingCallout != null) {
                    floatingCallout.setVisibility(View.VISIBLE);
                }
                if (calloutTitle != null) {
                    calloutTitle.setText(activeLandmarkName);
                }
                if (calloutCoords != null) {
                    calloutCoords.setText(getString(
                        R.string.projection_coords_simple_format,
                        (int) screenCoord.getX(),
                        (int) screenCoord.getY()
                    ));
                }
                if (calloutAltitude != null) {
                    calloutAltitude.setText(getString(
                        R.string.projection_depth_format,
                        (int) screenCoord.getDepth(),
                        (int) activeLocation.getAltitude()
                    ));
                }

                // Anchor callout horizontally centered and pinned directly above the target coordinate
                int calloutW = floatingCallout != null ? floatingCallout.getWidth() : 0;
                int calloutH = floatingCallout != null ? floatingCallout.getHeight() : 0;
                if (floatingCallout != null) {
                    floatingCallout.setTranslationX(screenCoord.getX() - (calloutW / 2f));
                    floatingCallout.setTranslationY(screenCoord.getY() - calloutH);
                }
            } else {
                if (tvScreenCoords != null) {
                    tvScreenCoords.setText(getString(R.string.projection_out_of_view));
                }
                if (tvStatus != null) {
                    tvStatus.setText(getString(R.string.projection_culled));
                    tvStatus.setTextColor(0xFFC62828);
                }
                if (floatingCallout != null) {
                    floatingCallout.setVisibility(View.GONE);
                }
            }
        });
    }

    private void toggleControls() {
        isCollapsed = !isCollapsed;
        if (isCollapsed) {
            if (cardContent != null) {
                cardContent.setVisibility(View.GONE);
            }
            if (btnCollapse != null) {
                btnCollapse.setIconResource(R.drawable.expand_less_24px);
            }
        } else {
            if (cardContent != null) {
                cardContent.setVisibility(View.VISIBLE);
            }
            if (btnCollapse != null) {
                btnCollapse.setIconResource(R.drawable.expand_more_24px);
            }
        }
    }

    private static class LandmarkData {
        final String name;
        final LatLngAltitude location;

        LandmarkData(String name, LatLngAltitude location) {
            this.name = name;
            this.location = location;
        }
    }

    private static final List<LandmarkData> SF_LANDMARKS = Arrays.asList(
        new LandmarkData("Transamerica Pyramid", new LatLngAltitude(37.7952, -122.4028, 260.0)),
        new LandmarkData("Coit Tower", new LatLngAltitude(37.8024, -122.4058, 110.0)),
        new LandmarkData("Ferry Building", new LatLngAltitude(37.7955, -122.3937, 75.0))
    );
}
