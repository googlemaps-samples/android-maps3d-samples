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

import com.example.maps3d.common.Map3DSettings;
import com.example.maps3d.common.Map3DSettingsController;
import com.example.maps3dcommon.R;
import com.example.maps3djava.sampleactivity.SampleBaseActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.FlyToOptions;
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
  private MaterialButton btnToggle2D3D;
  private RadioGroup rgMapMode;

  // --- State Variables ---

  private boolean isCollapsed = false;
  private boolean isSyncingRadioGroup = false;
  private boolean is3DMode = false;
  private boolean hasSyncedInitialMode = false;

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

  @Override
  @Map3DMode
  protected int getDefaultMapMode() {
    return Map3DMode.ROADMAP;
  }

  @NonNull
  @Override
  public Camera getInitialCamera() {
    double initialTilt =
        Map3DSettingsController.getShared().getSettings().getMapMode() != Map3DMode.ROADMAP
            ? 65.0
            : 0.0;
    return toValidCamera(new Camera(
        new LatLngAltitude(SF_LOCATION.latitude, SF_LOCATION.longitude, 0.0),
        45.0,
        initialTilt,
        0.0,
        2500.0
    ));
  }

  // --- Lifecycle & Initialization ---

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    Map3DSettingsController.getShared().onSampleEntered(getDefaultMapMode());
    is3DMode = Map3DSettingsController.getShared().getSettings().getMapMode() != Map3DMode.ROADMAP;
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
    btnToggle2D3D = findViewById(R.id.btn_toggle_2d_3d);
    updateToggle2D3DButtonText();

    if (btnToggle2D3D != null) {
      btnToggle2D3D.setOnClickListener(v -> {
        int nextMode = is3DMode ? Map3DMode.ROADMAP : Map3DMode.HYBRID;
        is3DMode = nextMode != Map3DMode.ROADMAP;
        updateToggle2D3DButtonText();
        Map3DSettingsController.getShared().setMapMode(nextMode);
        onQuickSettingsChanged(Map3DSettingsController.getShared().getSettings());
        if (googleMap3D != null) {
          Map3DSettingsController.getShared().getSettings().applyTo(googleMap3D);
          applyCameraPerspective(googleMap3D, is3DMode);
        }
      });
    }

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
        if (isSyncingRadioGroup) {
          return;
        }
        int mode;
        if (checkedId == R.id.rb_roadmap) {
          mode = Map3DMode.ROADMAP;
        } else if (checkedId == R.id.rb_hybrid) {
          mode = Map3DMode.HYBRID;
        } else if (checkedId == R.id.rb_satellite) {
          mode = Map3DMode.SATELLITE;
        } else {
          return;
        }
        is3DMode = mode != Map3DMode.ROADMAP;
        updateToggle2D3DButtonText();
        Map3DSettingsController.getShared().setMapMode(mode);
        if (googleMap3D != null) {
          Map3DSettingsController.getShared().getSettings().applyTo(googleMap3D);
          applyCameraPerspective(googleMap3D, is3DMode);
        }
      });
    }

    // Schedule subtle initial auto-fade for unobstructed viewing
    fadeHandler.postDelayed(fadeOutRunnable, 3000L);
  }

  private void updateToggle2D3DButtonText() {
    if (btnToggle2D3D != null) {
      btnToggle2D3D.setText(
          is3DMode
              ? R.string.store_locator_switch_to_2d
              : R.string.store_locator_switch_to_3d);
    }
  }

  private void applyCameraPerspective(@NonNull GoogleMap3D map, boolean target3D) {
    Camera base = getInitialCamera();
    base.setTilt(target3D ? 65.0 : 0.0);
    map.flyCameraTo(new FlyToOptions(base, 900));
  }

  @Override
  protected void onQuickSettingsChanged(@NonNull Map3DSettings settings) {
    boolean nextIs3D = settings.getMapMode() != Map3DMode.ROADMAP;
    boolean modeChangedAfterInit = hasSyncedInitialMode && (is3DMode != nextIs3D);
    hasSyncedInitialMode = true;
    is3DMode = nextIs3D;
    updateToggle2D3DButtonText();
    if (modeChangedAfterInit && googleMap3D != null) {
      applyCameraPerspective(googleMap3D, is3DMode);
    }
    if (rgMapMode == null) {
      return;
    }
    int targetId;
    int mapMode = settings.getMapMode();
    if (mapMode == Map3DMode.ROADMAP) {
      targetId = R.id.rb_roadmap;
    } else if (mapMode == Map3DMode.HYBRID) {
      targetId = R.id.rb_hybrid;
    } else if (mapMode == Map3DMode.SATELLITE) {
      targetId = R.id.rb_satellite;
    } else {
      return;
    }
    if (rgMapMode.getCheckedRadioButtonId() != targetId) {
      isSyncingRadioGroup = true;
      rgMapMode.check(targetId);
      isSyncingRadioGroup = false;
    }
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
  protected void onMapReady(@NonNull GoogleMap3D googleMap3D) {
    super.onMapReady(googleMap3D);
    googleMap3D.setCamera(getInitialCamera());
  }

  // --- Teardown ---

  @Override
  protected void onDestroy() {
    fadeHandler.removeCallbacks(fadeOutRunnable);
    super.onDestroy();
  }
}

