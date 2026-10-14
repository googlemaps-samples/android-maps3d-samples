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

package com.example.maps3djava.uicontrols;

import static com.example.maps3d.common.UtilitiesKt.toValidCamera;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.transition.TransitionManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import com.example.maps3dcommon.R;
import com.example.maps3djava.sampleactivity.SampleBaseActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.UiControlSettings;
import com.google.android.gms.maps3d.UiControlSettings.ControlOrientation;
import com.google.android.gms.maps3d.UiControlSettings.ControlPlacement;
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.CompassControl;
import com.google.android.gms.maps3d.model.LatLngAltitude;
import com.google.android.gms.maps3d.model.Map3DControl;
import com.google.android.gms.maps3d.model.Map3DMode;
import com.google.android.gms.maps3d.model.PanHorizontalControl;
import com.google.android.gms.maps3d.model.PanVerticalControl;
import com.google.android.gms.maps3d.model.RotateControl;
import com.google.android.gms.maps3d.model.TiltControl;
import com.google.android.gms.maps3d.model.ZoomControl;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import java.util.ArrayList;
import java.util.List;

/**
 * =================================================================================================
 * Dedicated 3D Map UI Controls & Placement Demo (Java Views)
 * =================================================================================================
 *
 * Demonstrates configuring built-in 3D map UI control buttons (Zoom, Tilt, Rotate, Compass,
 * PanVertical, PanHorizontal) and toggling their screen placement between
 * {@link ControlPlacement#TRAILING} and {@link ControlPlacement#LEADING} using direct SDK API calls.
 */
public class UiControlsActivity extends SampleBaseActivity {

  /** Focal landmark centered on the San Francisco Financial District. */
  public static final LatLng SF_LOCATION = new LatLng(37.7915, -122.4010);

  // --- UI Elements ---

  private CardView controlsCard;
  private View cardHeader;
  private View cardContent;
  private MaterialButton btnCollapse;
  private MaterialButton btnShowAll;
  private MaterialButton btnHideAll;
  private Chip chipZoom;
  private Chip chipTilt;
  private Chip chipRotate;
  private Chip chipCompass;
  private Chip chipPanVertical;
  private Chip chipPanHorizontal;
  private RadioGroup rgPlacement;

  // --- State Variables ---

  private boolean isCollapsed = false;
  private boolean isSyncingUi = false;

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

  @NonNull
  @Override
  public String getTAG() {
    return "UiControlsActivity";
  }

  @Override
  @Map3DMode
  protected int getDefaultMapMode() {
    return Map3DMode.ROADMAP;
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
      getLayoutInflater().inflate(R.layout.control_panel_ui_controls, container, true);
    }

    MaterialToolbar topBar = findViewById(R.id.top_bar);
    if (topBar != null) {
      topBar.setTitle(R.string.feature_title_ui_controls);
      topBar.setNavigationOnClickListener(v -> finish());
    }

    initViews();
  }

  private void initViews() {
    controlsCard = findViewById(R.id.control_panel);
    cardHeader = findViewById(R.id.card_header);
    cardContent = findViewById(R.id.card_content);
    btnCollapse = findViewById(R.id.btn_collapse);
    btnShowAll = findViewById(R.id.btn_show_all);
    btnHideAll = findViewById(R.id.btn_hide_all);
    chipZoom = findViewById(R.id.chip_zoom);
    chipTilt = findViewById(R.id.chip_tilt);
    chipRotate = findViewById(R.id.chip_rotate);
    chipCompass = findViewById(R.id.chip_compass);
    chipPanVertical = findViewById(R.id.chip_pan_vertical);
    chipPanHorizontal = findViewById(R.id.chip_pan_horizontal);
    rgPlacement = findViewById(R.id.rg_controls_placement);

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

    if (btnShowAll != null) {
      btnShowAll.setOnClickListener(v -> {
        setAllChips(true);
        applyControlsToMap();
      });
    }

    if (btnHideAll != null) {
      btnHideAll.setOnClickListener(v -> {
        setAllChips(false);
        applyControlsToMap();
      });
    }

    CompoundButton.OnCheckedChangeListener chipListener = (buttonView, isChecked) -> {
      if (!isSyncingUi) {
        applyControlsToMap();
      }
    };

    if (chipZoom != null) chipZoom.setOnCheckedChangeListener(chipListener);
    if (chipTilt != null) chipTilt.setOnCheckedChangeListener(chipListener);
    if (chipRotate != null) chipRotate.setOnCheckedChangeListener(chipListener);
    if (chipCompass != null) chipCompass.setOnCheckedChangeListener(chipListener);
    if (chipPanVertical != null) chipPanVertical.setOnCheckedChangeListener(chipListener);
    if (chipPanHorizontal != null) chipPanHorizontal.setOnCheckedChangeListener(chipListener);

    if (rgPlacement != null) {
      rgPlacement.setOnCheckedChangeListener((group, checkedId) -> {
        if (!isSyncingUi) {
          applyControlsToMap();
        }
      });
    }

    fadeHandler.postDelayed(fadeOutRunnable, 3000L);
  }

  private void setAllChips(boolean checked) {
    isSyncingUi = true;
    if (chipZoom != null) chipZoom.setChecked(checked);
    if (chipTilt != null) chipTilt.setChecked(checked);
    if (chipRotate != null) chipRotate.setChecked(checked);
    if (chipCompass != null) chipCompass.setChecked(checked);
    if (chipPanVertical != null) chipPanVertical.setChecked(checked);
    if (chipPanHorizontal != null) chipPanHorizontal.setChecked(checked);
    isSyncingUi = false;
  }

  /**
   * Applies the UI controls configuration directly to the {@link GoogleMap3D} instance using the SDK API.
   */
  private void applyControlsToMap() {
    if (googleMap3D == null) {
      return;
    }
    UiControlSettings uiSettings = googleMap3D.getUiControlSettings();
    if (uiSettings == null) {
      return;
    }

    List<Map3DControl> primaryControls = new ArrayList<>();
    if (chipZoom != null && chipZoom.isChecked()) {
      primaryControls.add(new ZoomControl(ControlOrientation.VERTICAL));
    }
    if (chipTilt != null && chipTilt.isChecked()) {
      primaryControls.add(new TiltControl(ControlOrientation.VERTICAL));
    }
    if (chipRotate != null && chipRotate.isChecked()) {
      primaryControls.add(new RotateControl(ControlOrientation.VERTICAL));
    }

    List<Map3DControl> secondaryControls = new ArrayList<>();
    if (chipPanVertical != null && chipPanVertical.isChecked()) {
      secondaryControls.add(new PanVerticalControl(ControlOrientation.VERTICAL));
    }
    if (chipPanHorizontal != null && chipPanHorizontal.isChecked()) {
      secondaryControls.add(new PanHorizontalControl(ControlOrientation.VERTICAL));
    }
    if (chipCompass != null && chipCompass.isChecked()) {
      secondaryControls.add(new CompassControl());
    }

    boolean isLeading = rgPlacement != null && rgPlacement.getCheckedRadioButtonId() == R.id.rb_placement_leading;
    int primaryPlacement = isLeading ? ControlPlacement.LEADING : ControlPlacement.TRAILING;
    int secondaryPlacement = isLeading ? ControlPlacement.TRAILING : ControlPlacement.LEADING;

    uiSettings.setUiControlsPlacement(primaryControls, primaryPlacement);
    uiSettings.setUiControlsPlacement(secondaryControls, secondaryPlacement);
  }

  // --- UI Collapse / Expand Mechanics ---

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

  @Override
  protected void onQuickSettingsChanged(@NonNull com.example.maps3d.common.Map3DSettings settings) {
    super.onQuickSettingsChanged(settings);
    applyControlsToMap();
  }

  @Override
  protected void onMapReady(@NonNull GoogleMap3D googleMap3D) {
    super.onMapReady(googleMap3D);
    googleMap3D.setCamera(getInitialCamera());
    applyControlsToMap();
  }

  @Override
  protected void onDestroy() {
    fadeHandler.removeCallbacks(fadeOutRunnable);
    super.onDestroy();
  }
}
