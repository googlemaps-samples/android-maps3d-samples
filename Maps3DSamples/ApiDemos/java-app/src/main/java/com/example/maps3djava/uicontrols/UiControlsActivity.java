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
import android.widget.RadioGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import com.example.maps3d.common.Map3DSettings;
import com.example.maps3d.common.Map3DSettingsController;
import com.example.maps3d.common.Map3DUiControls;
import com.example.maps3dcommon.R;
import com.example.maps3djava.sampleactivity.SampleBaseActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.UiControlSettings.ControlPlacement;
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.LatLngAltitude;
import com.google.android.gms.maps3d.model.Map3DMode;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

/**
 * =================================================================================================
 * Dedicated 3D Map UI Controls & Placement Demo (Java Views)
 * =================================================================================================
 *
 * Demonstrates configuring built-in 3D map UI control buttons (Zoom, Tilt, Rotate, Compass,
 * PanVertical, PanHorizontal, Spacer) and toggling their screen placement between
 * {@link ControlPlacement#TRAILING} and {@link ControlPlacement#LEADING}.
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
        Map3DSettings updated = Map3DSettingsController.getShared().setAllUiControls(true);
        if (googleMap3D != null) {
          updated.applyTo(googleMap3D);
        }
        syncControlPanelUi(updated);
      });
    }

    if (btnHideAll != null) {
      btnHideAll.setOnClickListener(v -> {
        Map3DSettings updated = Map3DSettingsController.getShared().setAllUiControls(false);
        if (googleMap3D != null) {
          updated.applyTo(googleMap3D);
        }
        syncControlPanelUi(updated);
      });
    }

    if (chipZoom != null) {
      chipZoom.setOnCheckedChangeListener((buttonView, isChecked) -> {
        if (isSyncingUi) return;
        Map3DUiControls c = Map3DSettingsController.getShared().getSettings().getUiControls();
        Map3DSettings updated = Map3DSettingsController.getShared().updateUiControl(
            isChecked, c.getTilt(), c.getRotate(), c.getCompass(), c.getPanVertical(), c.getPanHorizontal()
        );
        if (googleMap3D != null) {
          updated.applyTo(googleMap3D);
        }
      });
    }

    if (chipTilt != null) {
      chipTilt.setOnCheckedChangeListener((buttonView, isChecked) -> {
        if (isSyncingUi) return;
        Map3DUiControls c = Map3DSettingsController.getShared().getSettings().getUiControls();
        Map3DSettings updated = Map3DSettingsController.getShared().updateUiControl(
            c.getZoom(), isChecked, c.getRotate(), c.getCompass(), c.getPanVertical(), c.getPanHorizontal()
        );
        if (googleMap3D != null) {
          updated.applyTo(googleMap3D);
        }
      });
    }

    if (chipRotate != null) {
      chipRotate.setOnCheckedChangeListener((buttonView, isChecked) -> {
        if (isSyncingUi) return;
        Map3DUiControls c = Map3DSettingsController.getShared().getSettings().getUiControls();
        Map3DSettings updated = Map3DSettingsController.getShared().updateUiControl(
            c.getZoom(), c.getTilt(), isChecked, c.getCompass(), c.getPanVertical(), c.getPanHorizontal()
        );
        if (googleMap3D != null) {
          updated.applyTo(googleMap3D);
        }
      });
    }

    if (chipCompass != null) {
      chipCompass.setOnCheckedChangeListener((buttonView, isChecked) -> {
        if (isSyncingUi) return;
        Map3DUiControls c = Map3DSettingsController.getShared().getSettings().getUiControls();
        Map3DSettings updated = Map3DSettingsController.getShared().updateUiControl(
            c.getZoom(), c.getTilt(), c.getRotate(), isChecked, c.getPanVertical(), c.getPanHorizontal()
        );
        if (googleMap3D != null) {
          updated.applyTo(googleMap3D);
        }
      });
    }

    if (chipPanVertical != null) {
      chipPanVertical.setOnCheckedChangeListener((buttonView, isChecked) -> {
        if (isSyncingUi) return;
        Map3DUiControls c = Map3DSettingsController.getShared().getSettings().getUiControls();
        Map3DSettings updated = Map3DSettingsController.getShared().updateUiControl(
            c.getZoom(), c.getTilt(), c.getRotate(), c.getCompass(), isChecked, c.getPanHorizontal()
        );
        if (googleMap3D != null) {
          updated.applyTo(googleMap3D);
        }
      });
    }

    if (chipPanHorizontal != null) {
      chipPanHorizontal.setOnCheckedChangeListener((buttonView, isChecked) -> {
        if (isSyncingUi) return;
        Map3DUiControls c = Map3DSettingsController.getShared().getSettings().getUiControls();
        Map3DSettings updated = Map3DSettingsController.getShared().updateUiControl(
            c.getZoom(), c.getTilt(), c.getRotate(), c.getCompass(), c.getPanVertical(), isChecked
        );
        if (googleMap3D != null) {
          updated.applyTo(googleMap3D);
        }
      });
    }

    if (rgPlacement != null) {
      rgPlacement.setOnCheckedChangeListener((group, checkedId) -> {
        if (isSyncingUi) return;
        int position;
        if (checkedId == R.id.rb_placement_leading) {
          position = ControlPlacement.LEADING;
        } else if (checkedId == R.id.rb_placement_trailing) {
          position = ControlPlacement.TRAILING;
        } else {
          return;
        }
        Map3DSettings updated = Map3DSettingsController.getShared().setControlsPosition(position);
        if (googleMap3D != null) {
          updated.applyTo(googleMap3D);
        }
      });
    }

    syncControlPanelUi(Map3DSettingsController.getShared().getSettings());
    fadeHandler.postDelayed(fadeOutRunnable, 3000L);
  }

  @Override
  protected void onQuickSettingsChanged(@NonNull Map3DSettings settings) {
    syncControlPanelUi(settings);
  }

  private void syncControlPanelUi(@NonNull Map3DSettings settings) {
    isSyncingUi = true;
    Map3DUiControls controls = settings.getUiControls();
    if (chipZoom != null) chipZoom.setChecked(controls.getZoom());
    if (chipTilt != null) chipTilt.setChecked(controls.getTilt());
    if (chipRotate != null) chipRotate.setChecked(controls.getRotate());
    if (chipCompass != null) chipCompass.setChecked(controls.getCompass());
    if (chipPanVertical != null) chipPanVertical.setChecked(controls.getPanVertical());
    if (chipPanHorizontal != null) chipPanHorizontal.setChecked(controls.getPanHorizontal());

    if (rgPlacement != null) {
      int targetId = (controls.getControlsPosition() == ControlPlacement.LEADING)
          ? R.id.rb_placement_leading
          : R.id.rb_placement_trailing;
      if (rgPlacement.getCheckedRadioButtonId() != targetId) {
        rgPlacement.check(targetId);
      }
    }
    isSyncingUi = false;
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
  protected void onMapReady(@NonNull GoogleMap3D googleMap3D) {
    super.onMapReady(googleMap3D);
    googleMap3D.setCamera(getInitialCamera());
  }

  @Override
  protected void onDestroy() {
    fadeHandler.removeCallbacks(fadeOutRunnable);
    super.onDestroy();
  }
}
