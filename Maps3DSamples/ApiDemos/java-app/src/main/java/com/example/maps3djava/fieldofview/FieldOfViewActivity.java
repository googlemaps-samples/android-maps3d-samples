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

package com.example.maps3djava.fieldofview;

import static com.example.maps3d.common.UtilitiesKt.toValidCamera;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.example.maps3dcommon.R;
import com.example.maps3djava.sampleactivity.SampleBaseActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.LatLngAltitude;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;

/**
 * =================================================================================================
 * Field of View (FOV) Perspective Scaling (Java)
 * =================================================================================================
 *
 * This sample demonstrates perspective Field of View (FOV) scaling and optical dolly-zoom simulation
 * using the Google Maps 3D SDK.
 *
 * Key Concepts Demonstrated:
 * 1. Perspective Field of View (FOV) Adjustments:
 *    - Adjusts the camera distance (range) proportionally using trigonometric perspective projection
 *      math to simulate optical lens focal length changes (Telephoto, Standard, Wide, Ultra-Wide).
 *    - Formula: range = baseRange * (tan(baseFov / 2) / tan(targetFov / 2)).
 *
 * 2. Seamless Camera State Preservation:
 *    - Inspects the active live camera before adjusting perspective, ensuring custom panning,
 *      heading rotations, and tilt angles applied by user gestures are smoothly retained.
 *
 * 3. Modern Material UI & Collapse Affordances:
 *    - Features a bottom control card with dynamic FOV angle readout and quick preset buttons.
 *    - Supports expanding and collapsing the card header to maximize the visible 3D map scene.
 *    - Implements subtle UI idle auto-fade with touch-to-wake responsiveness.
 */
public class FieldOfViewActivity extends SampleBaseActivity {

  // --- Constants & Perspective Geometry ---

  /** Focal landmark centered near the San Francisco Transamerica Pyramid & Financial District. */
  public static final LatLng SF_FINANCIAL_DISTRICT = new LatLng(37.7952, -122.4028);

  /** Baseline field of view angle in degrees (human eye / standard focal length). */
  private static final double BASE_FOV_DEGREES = 45.0;

  /** Baseline camera range in meters corresponding to the standard 45° FOV baseline. */
  private static final double BASE_RANGE_METERS = 800.0;

  /** Minimum optical range boundary in meters. */
  private static final double MIN_RANGE_METERS = 150.0;

  /** Maximum optical range boundary in meters. */
  private static final double MAX_RANGE_METERS = 3000.0;

  // --- UI Elements ---

  private CardView controlsCard;
  private View cardHeader;
  private View cardContent;
  private MaterialButton btnCollapse;
  private TextView fovSliderLabel;
  private Slider fovSlider;

  // --- State Variables ---

  private double currentFov = 45.0;
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
    return "FieldOfViewActivity";
  }

  @NonNull
  @Override
  public Camera getInitialCamera() {
    return toValidCamera(new Camera(
        new LatLngAltitude(SF_FINANCIAL_DISTRICT.latitude, SF_FINANCIAL_DISTRICT.longitude, 150.0),
        45.0,
        65.0,
        0.0,
        BASE_RANGE_METERS
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
      getLayoutInflater().inflate(R.layout.control_panel_field_of_view, container, true);
    }

    MaterialToolbar topBar = findViewById(R.id.top_bar);
    if (topBar != null) {
      topBar.setTitle(R.string.feature_title_field_of_view);
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
    cardContent = findViewById(R.id.card_content);
    btnCollapse = findViewById(R.id.btn_collapse);

    fovSliderLabel = findViewById(R.id.fov_slider_label);
    fovSlider = findViewById(R.id.fov_slider);

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

    if (fovSlider != null) {
      fovSlider.addOnChangeListener((slider, value, fromUser) -> updateFov(value));
    }

    Button btnTele = findViewById(R.id.btn_fov_telephoto);
    if (btnTele != null) {
      btnTele.setOnClickListener(v -> {
        if (fovSlider != null) {
          fovSlider.setValue(20.0f);
        }
      });
    }

    Button btnStd = findViewById(R.id.btn_fov_standard);
    if (btnStd != null) {
      btnStd.setOnClickListener(v -> {
        if (fovSlider != null) {
          fovSlider.setValue(45.0f);
        }
      });
    }

    Button btnWide = findViewById(R.id.btn_fov_wide);
    if (btnWide != null) {
      btnWide.setOnClickListener(v -> {
        if (fovSlider != null) {
          fovSlider.setValue(90.0f);
        }
      });
    }

    Button btnUltra = findViewById(R.id.btn_fov_ultrawide);
    if (btnUltra != null) {
      btnUltra.setOnClickListener(v -> {
        if (fovSlider != null) {
          fovSlider.setValue(120.0f);
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
    android.transition.TransitionManager.beginDelayedTransition(controlsCard);
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
    android.transition.TransitionManager.beginDelayedTransition(controlsCard);
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

  // --- 3D Map Setup & Perspective Scaling Engine ---

  @Override
  public void onMap3DViewReady(@NonNull GoogleMap3D googleMap3D) {
    super.onMap3DViewReady(googleMap3D);
    googleMap3D.setOnMapReadyListener(sceneReadiness -> {
      googleMap3D.setOnMapReadyListener(null);
      runOnUiThread(() -> updateFov((float) currentFov));
    });
  }

  /**
   * Calculates and applies the optical dolly-zoom perspective range for the specified FOV angle.
   *
   * @param fovAngle Perspective field of view angle in degrees (e.g. 15° to 120°).
   */
  private void updateFov(float fovAngle) {
    currentFov = fovAngle;
    runOnUiThread(() -> {
      if (fovSliderLabel != null) {
        fovSliderLabel.setText(getString(R.string.field_of_view_format, (int) fovAngle));
      }
    });

    if (googleMap3D != null) {
      Camera liveCam = googleMap3D.getCamera() != null ? toValidCamera(googleMap3D.getCamera()) : null;
      Camera currCam = (liveCam != null && (Math.abs(liveCam.getCenter().getLatitude()) > 0.001
          || Math.abs(liveCam.getCenter().getLongitude()) > 0.001))
          ? liveCam
          : getInitialCamera();

      // Optical perspective transformation: range = baseRange * tan(baseFov/2) / tan(targetFov/2)
      double baseFovRad = Math.toRadians(BASE_FOV_DEGREES / 2.0);
      double targetFovRad = Math.toRadians(fovAngle / 2.0);

      double targetRange = BASE_RANGE_METERS * Math.tan(baseFovRad) / Math.tan(targetFovRad);
      if (targetRange < MIN_RANGE_METERS) {
        targetRange = MIN_RANGE_METERS;
      }
      if (targetRange > MAX_RANGE_METERS) {
        targetRange = MAX_RANGE_METERS;
      }

      Camera updatedCam = toValidCamera(new Camera(
          currCam.getCenter(),
          currCam.getHeading(),
          currCam.getTilt(),
          currCam.getRoll(),
          targetRange
      ));
      googleMap3D.setCamera(updatedCam);
    }
  }

  // --- Teardown ---

  @Override
  protected void onDestroy() {
    fadeHandler.removeCallbacks(fadeOutRunnable);
    super.onDestroy();
  }
}

