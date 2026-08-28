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

package com.example.maps3djava.roadmapmode;

import static com.example.maps3d.common.UtilitiesKt.toValidCamera;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.transition.TransitionManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.example.maps3dcommon.R;
import com.example.maps3djava.sampleactivity.SampleBaseActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.LatLngAltitude;
import com.google.android.gms.maps3d.model.Map3DMode;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

/**
 * =================================================================================================
 * 3D Roadmap Mode & Render Style Switching (Java)
 * =================================================================================================
 *
 * This sample demonstrates switching between distinct visual rendering modes provided by the
 * Google Maps 3D SDK:
 *
 * Key Concepts Demonstrated:
 * 1. 3D Map Rendering Modes ({@link Map3DMode}):
 *    - {@link Map3DMode#ROADMAP}: High-contrast 3D vector street network with clean white building
 *      massings, road labels, and stylized transit geometry.
 *    - {@link Map3DMode#HYBRID}: High-resolution 3D photorealistic mesh overlaid with prominent
 *      vector road networks, street names, and point-of-interest labels.
 *    - {@link Map3DMode#SATELLITE}: Pure photorealistic 3D mesh rendering without overlay labels
 *      or vector lines, ideal for cinematic aerial exploration.
 *
 * 2. Camera Stability & Safe Angle Validation:
 *    - Configures a dramatic 3D perspective centered on the San Francisco Financial District with
 *      {@link com.example.maps3d.common.UtilitiesKt#toValidCamera(Camera)}.
 *
 * 3. Modern Material UI & Collapse Affordances:
 *    - Bottom control card with quick radio button switching between map rendering styles.
 *    - Expandable / collapsible header bar for unobstructed 3D scene inspection.
 *    - Subtle UI idle auto-fade with touch-to-wake responsiveness.
 */
public class RoadmapModeActivity extends SampleBaseActivity {

  // --- Constants & Geographical Bounds ---

  /** Focal landmark centered on the San Francisco Financial District. */
  public static final LatLng SF_LOCATION = new LatLng(37.7915, -122.4010);

  // --- UI Elements ---

  private CardView controlsCard;
  private View cardHeader;
  private View cardContent;
  private MaterialButton btnCollapse;
  private RadioGroup rgMapMode;

  // --- State Variables ---

  private boolean isCollapsed = false;

  // --- Handlers & Runnables ---

  private final Handler fadeHandler = new Handler(Looper.getMainLooper());
  private final Runnable fadeOutRunnable = () -> {
    if (controlsCard != null && !isCollapsed) {
      controlsCard.animate()
          .alpha(0.85f)
          .setDuration(400)
          .start();
    }
  };

  // --- Base Activity Overrides ---

  @NonNull
  @Override
  public String getTAG() {
    return "RoadmapModeActivity";
  }

  @NonNull
  @Override
  public Camera getInitialCamera() {
    return toValidCamera(new Camera(
        new LatLngAltitude(SF_LOCATION.latitude, SF_LOCATION.longitude, 250.0),
        45.0,
        65.0,
        0.0,
        800.0
    ));
  }

  // --- Lifecycle & Initialization ---

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Hide base pill scroll view as this activity manages its own overlay card
    View baseScrollView = findViewById(R.id.control_scroll_view);
    if (baseScrollView != null) {
      baseScrollView.setVisibility(View.GONE);
    }

    ViewGroup container = findViewById(R.id.map_container);
    if (container != null) {
      getLayoutInflater().inflate(R.layout.control_panel_roadmap_mode, container, true);
    }

    MaterialToolbar topBar = findViewById(R.id.top_bar);
    if (topBar != null) {
      topBar.setTitle(R.string.feature_title_roadmap_mode);
      topBar.setNavigationOnClickListener(v -> finish());
    }

    initViews();
  }

  /**
   * Initializes view references and wires up touch and click listeners.
   */
  private void initViews() {
    controlsCard = findViewById(R.id.control_panel);
    cardHeader = findViewById(R.id.card_header);
    rgMapMode = findViewById(R.id.rg_map_mode);
    cardContent = rgMapMode;
    btnCollapse = findViewById(R.id.btn_collapse);

    if (btnCollapse != null) {
      btnCollapse.setOnClickListener(v -> {
        if (isCollapsed) {
          expandControls();
        } else {
          collapseControls();
        }
      });
    }

    if (cardHeader != null) {
      cardHeader.setOnClickListener(v -> {
        if (isCollapsed) {
          expandControls();
        } else {
          collapseControls();
        }
      });
    }

    if (rgMapMode != null) {
      rgMapMode.setOnCheckedChangeListener((group, checkedId) -> {
        if (googleMap3D == null) {
          return;
        }
        if (checkedId == R.id.rb_roadmap) {
          googleMap3D.setMapMode(Map3DMode.ROADMAP);
        } else if (checkedId == R.id.rb_hybrid) {
          googleMap3D.setMapMode(Map3DMode.HYBRID);
        } else if (checkedId == R.id.rb_satellite) {
          googleMap3D.setMapMode(Map3DMode.SATELLITE);
        }
      });
    }

    // Schedule subtle initial auto-fade for unobstructed viewing
    fadeHandler.postDelayed(fadeOutRunnable, 3000L);
  }

  // --- UI Collapse / Expand Mechanics ---

  /**
   * Collapses the control card downward, leaving only the title header visible.
   */
  private void collapseControls() {
    if (controlsCard == null || cardContent == null) {
      return;
    }
    isCollapsed = true;
    fadeHandler.removeCallbacks(fadeOutRunnable);
    if (btnCollapse != null) {
      btnCollapse.setIconResource(R.drawable.expand_less_24px);
      btnCollapse.setContentDescription(getString(R.string.expand_controls));
    }
    TransitionManager.beginDelayedTransition(controlsCard);
    cardContent.setVisibility(View.GONE);
  }

  /**
   * Expands the control card back to its full height.
   */
  private void expandControls() {
    if (controlsCard == null || cardContent == null) {
      return;
    }
    isCollapsed = false;
    if (btnCollapse != null) {
      btnCollapse.setIconResource(R.drawable.expand_more_24px);
      btnCollapse.setContentDescription(getString(R.string.collapse_controls));
    }
    TransitionManager.beginDelayedTransition(controlsCard);
    cardContent.setVisibility(View.VISIBLE);
    fadeHandler.removeCallbacks(fadeOutRunnable);
    fadeHandler.postDelayed(fadeOutRunnable, 3000L);
  }

  @Override
  public boolean dispatchTouchEvent(MotionEvent ev) {
    if (ev.getAction() == MotionEvent.ACTION_DOWN || ev.getAction() == MotionEvent.ACTION_MOVE) {
      if (controlsCard != null && !isCollapsed) {
        controlsCard.animate()
            .alpha(1.0f)
            .setDuration(150)
            .start();
        fadeHandler.removeCallbacks(fadeOutRunnable);
        fadeHandler.postDelayed(fadeOutRunnable, 3000L);
      }
    }
    return super.dispatchTouchEvent(ev);
  }

  // --- 3D Map Setup ---

  @Override
  public void onMap3DViewReady(@NonNull GoogleMap3D googleMap3D) {
    super.onMap3DViewReady(googleMap3D);
    googleMap3D.setOnMapReadyListener(sceneReadiness -> {
      googleMap3D.setOnMapReadyListener(null);
      googleMap3D.setMapMode(Map3DMode.ROADMAP);
      googleMap3D.setCamera(getInitialCamera());
    });
  }

  // --- Teardown ---

  @Override
  protected void onDestroy() {
    fadeHandler.removeCallbacks(fadeOutRunnable);
    super.onDestroy();
  }
}

