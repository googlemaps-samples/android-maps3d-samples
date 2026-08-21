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

package com.example.maps3djava.datavisualization;

import static com.example.maps3d.common.UtilitiesKt.toValidCamera;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.example.maps3dcommon.R;
import com.example.maps3djava.sampleactivity.SampleBaseActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.model.AltitudeMode;
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.FlyToOptions;
import com.google.android.gms.maps3d.model.LatLngAltitude;
import com.google.android.gms.maps3d.model.Map3DMode;
import com.google.android.gms.maps3d.model.Polygon;
import com.google.android.gms.maps3d.model.PolygonOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * =================================================================================================
 * Data Visualization: 3D Extruded Flood Simulation (Java)
 * =================================================================================================
 *
 * This sample demonstrates how to render and dynamically animate volumetric 3D extruded polygons
 * using the Google Maps 3D SDK.
 *
 * Key Concepts Demonstrated:
 * 1. 3D Volumetric Polygon Extrusion:
 *    - Uses {@link PolygonOptions#setExtruded(boolean)} to generate 3D vertical walls extending
 *      from ground level up to an absolute altitude ceiling.
 *    - Configures {@link AltitudeMode#ABSOLUTE} so the polygon elevation represents true mean sea
 *      level (MSL) altitude rather than terrain-relative offsets.
 *
 * 2. Real-Time Elevation Updates & Animation:
 *    - Updates the polygon altitude in real-time in response to slider gestures or an automated
 *      continuous tide simulation loop.
 *    - Re-uses a static {@link PolygonOptions#setId(String)} to upsert the polygon in place within
 *      the Maps 3D rendering engine.
 *
 * 3. Modern Material UI & Collapse Affordances:
 *    - Provides a bottom overlay card with dynamic flood elevation readouts and risk badges.
 *    - Features a header toggle with {@link MaterialButton} to expand/collapse controls for
 *      unobstructed 3D scene inspection.
 */
public class DataVisualizationActivity extends SampleBaseActivity {

  // --- Constants & Geographical Bounds ---

  /** Focal viewpoint centered on the San Francisco Embarcadero waterfront. */
  public static final LatLng SF_FLOOD_CENTER = new LatLng(37.8025, -122.4030);

  /** Stable identifier for upserting the flood polygon in the 3D map engine. */
  private static final String POLYGON_ID = "flood_zone_polygon";

  /** Boundary coordinates outlining the San Francisco waterfront flood study area. */
  public static final List<double[]> floodZoneCoords = Arrays.asList(
      new double[]{37.805156, -122.403256},
      new double[]{37.803370, -122.401287},
      new double[]{37.799222, -122.405080},
      new double[]{37.797500, -122.408000},
      new double[]{37.801000, -122.411000},
      new double[]{37.805156, -122.403256}
  );

  /** Translucent water body fill color. */
  private final int waterFillColor = Color.argb(140, 230, 40, 40);

  /** Opaque perimeter boundary stroke color. */
  private final int waterStrokeColor = Color.argb(255, 180, 0, 0);

  /** Width of the polygon boundary line in screen pixels. */
  private final double waterStrokeWidth = 2.5;

  // --- UI Elements ---

  private CardView controlsCard;
  private View cardHeader;
  private View cardContent;
  private MaterialButton btnCollapse;
  private TextView floodDepthLabel;
  private TextView floodRiskBadge;
  private Slider floodSlider;
  private MaterialButton btnAnimateFlood;

  // --- State Variables ---

  private Polygon floodPolygon = null;
  private double currentFloodElevation = 10.0;
  private boolean isSimulating = false;
  private boolean isCollapsed = false;

  // --- Handlers & Runnables ---

  private final Handler simulationHandler = new Handler(Looper.getMainLooper());
  private Runnable simulationRunnable;

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
    return "DataVisualizationActivity";
  }

  @NonNull
  @Override
  public Camera getInitialCamera() {
    return toValidCamera(new Camera(
        new LatLngAltitude(SF_FLOOD_CENTER.latitude, SF_FLOOD_CENTER.longitude, 120.0),
        35.0,
        64.0,
        0.0,
        1200.0
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
      getLayoutInflater().inflate(R.layout.control_panel_data_visualization, container, true);
    }

    MaterialToolbar topBar = findViewById(R.id.top_bar);
    if (topBar != null) {
      topBar.setTitle(R.string.feature_title_data_visualization);
      topBar.setNavigationOnClickListener(v -> finish());
    }

    initViews();
    updateControlLabels(currentFloodElevation);
  }

  /**
   * Initializes view references and wires up touch and click listeners.
   */
  private void initViews() {
    controlsCard = findViewById(R.id.control_panel);
    cardHeader = findViewById(R.id.card_header);
    cardContent = findViewById(R.id.card_content);
    btnCollapse = findViewById(R.id.btn_collapse);

    floodDepthLabel = findViewById(R.id.tv_flood_depth_label);
    floodRiskBadge = findViewById(R.id.tv_flood_risk_badge);
    floodSlider = findViewById(R.id.flood_slider);
    btnAnimateFlood = findViewById(R.id.btn_animate_flood);

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

    if (floodSlider != null) {
      floodSlider.addOnChangeListener((slider, value, fromUser) -> {
        if (fromUser) {
          stopSimulation();
        }
        updateFloodElevation(value);
      });
    }

    if (btnAnimateFlood != null) {
      btnAnimateFlood.setOnClickListener(v -> {
        if (isSimulating) {
          stopSimulation();
        } else {
          startSimulation();
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
    if (controlsCard == null) {
      return;
    }
    isCollapsed = true;
    fadeHandler.removeCallbacks(fadeOutRunnable);
    if (btnCollapse != null) {
      btnCollapse.setIconResource(R.drawable.expand_less_24px);
      btnCollapse.setContentDescription(getString(R.string.expand_controls));
    }
    int headerHeight = (cardHeader != null && cardHeader.getHeight() > 0)
        ? cardHeader.getHeight()
        : (int) (48 * getResources().getDisplayMetrics().density);
    float targetTranslationY = (cardContent != null && cardContent.getHeight() > 0)
        ? cardContent.getHeight()
        : Math.max(0, controlsCard.getHeight() - headerHeight);
    controlsCard.animate()
        .translationY(targetTranslationY)
        .alpha(0.9f)
        .setDuration(300)
        .start();
  }

  /**
   * Expands the control card back to its full height.
   */
  private void expandControls() {
    if (controlsCard == null) {
      return;
    }
    isCollapsed = false;
    if (btnCollapse != null) {
      btnCollapse.setIconResource(R.drawable.expand_more_24px);
      btnCollapse.setContentDescription(getString(R.string.collapse_controls));
    }
    controlsCard.animate()
        .translationY(0f)
        .alpha(1.0f)
        .setDuration(250)
        .start();
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

  // --- 3D Map Setup & Extrusion Engine ---

  @Override
  public void onMap3DViewReady(@NonNull GoogleMap3D googleMap3D) {
    super.onMap3DViewReady(googleMap3D);

    googleMap3D.setMapMode(Map3DMode.HYBRID);

    googleMap3D.setOnMapReadyListener(sceneReadiness -> {
      googleMap3D.setOnMapReadyListener(null);
      googleMap3D.flyCameraTo(new FlyToOptions(getInitialCamera(), 1200));
      runOnUiThread(() -> updateFloodElevation(currentFloodElevation));
    });
  }

  /**
   * Updates the 3D polygon height and refreshes formatted textual status indicators.
   *
   * @param currentFloodHeightMeters Target sea level elevation in meters.
   */
  public void updateFloodElevation(double currentFloodHeightMeters) {
    currentFloodElevation = currentFloodHeightMeters;

    runOnUiThread(() -> {
      updateControlLabels(currentFloodHeightMeters);

      if (googleMap3D == null) {
        return;
      }

      // Build 3D path vertices at the specified absolute altitude
      List<LatLngAltitude> path = new ArrayList<>();
      for (double[] coord : floodZoneCoords) {
        path.add(new LatLngAltitude(coord[0], coord[1], currentFloodHeightMeters));
      }

      // Configure volumetric extruded polygon options
      PolygonOptions options = new PolygonOptions();
      options.setId(POLYGON_ID);
      options.setPath(path);
      options.setFillColor(waterFillColor);
      options.setStrokeColor(waterStrokeColor);
      options.setStrokeWidth(waterStrokeWidth);
      options.setAltitudeMode(AltitudeMode.ABSOLUTE);
      options.setExtruded(true);
      options.setDrawsOccludedSegments(true);
      options.setGeodesic(false);

      floodPolygon = googleMap3D.addPolygon(options);
      if (floodPolygon != null) {
        floodPolygon.setClickListener(() -> runOnUiThread(() -> Toast.makeText(
            DataVisualizationActivity.this,
            getString(R.string.flood_toast_format, currentFloodElevation),
            Toast.LENGTH_SHORT
        ).show()));
      }
    });
  }

  /**
   * Refreshes textual status labels and risk severity badges based on water height.
   */
  private void updateControlLabels(double currentFloodHeightMeters) {
    double feet = currentFloodHeightMeters * 3.28084;
    if (floodDepthLabel != null) {
      floodDepthLabel.setText(
          getString(R.string.flood_elevation_format, currentFloodHeightMeters, feet));
    }

    if (floodRiskBadge != null) {
      if (currentFloodHeightMeters <= 2.0) {
        floodRiskBadge.setText(R.string.flood_risk_baseline);
        floodRiskBadge.setTextColor(Color.parseColor("#008800"));
        floodRiskBadge.setBackgroundColor(Color.parseColor("#2000AA00"));
      } else if (currentFloodHeightMeters <= 8.0) {
        floodRiskBadge.setText(R.string.flood_risk_minor);
        floodRiskBadge.setTextColor(Color.parseColor("#BB7700"));
        floodRiskBadge.setBackgroundColor(Color.parseColor("#20FFAA00"));
      } else if (currentFloodHeightMeters <= 20.0) {
        floodRiskBadge.setText(R.string.flood_risk_moderate);
        floodRiskBadge.setTextColor(Color.parseColor("#0077CC"));
        floodRiskBadge.setBackgroundColor(Color.parseColor("#200088FF"));
      } else if (currentFloodHeightMeters <= 35.0) {
        floodRiskBadge.setText(R.string.flood_risk_storm_surge);
        floodRiskBadge.setTextColor(Color.parseColor("#DD4400"));
        floodRiskBadge.setBackgroundColor(Color.parseColor("#25FF5500"));
      } else {
        floodRiskBadge.setText(R.string.flood_risk_extreme);
        floodRiskBadge.setTextColor(Color.parseColor("#CC0000"));
        floodRiskBadge.setBackgroundColor(Color.parseColor("#25FF0000"));
      }
    }
  }

  // --- Automated Continuous Simulation Loop ---

  /**
   * Starts continuous incremental sea level rise simulation.
   */
  private void startSimulation() {
    double maxVal = floodSlider != null ? floodSlider.getValueTo() : 100.0;
    double minVal = floodSlider != null ? floodSlider.getValueFrom() : 0.0;
    if (currentFloodElevation >= maxVal) {
      if (floodSlider != null) {
        floodSlider.setValue((float) minVal);
      }
      updateFloodElevation(minVal);
    }

    isSimulating = true;
    if (btnAnimateFlood != null) {
      btnAnimateFlood.setText(R.string.stop_simulation);
    }

    simulationRunnable = new Runnable() {
      @Override
      public void run() {
        if (!isSimulating) {
          return;
        }

        double currentMax = floodSlider != null ? floodSlider.getValueTo() : 100.0;
        double newElevation = currentFloodElevation + 0.2;
        newElevation = Math.round(newElevation * 10.0) / 10.0;
        if (floodSlider != null) {
          floodSlider.setValue((float) newElevation);
        } else {
          updateFloodElevation(newElevation);
        }
        if (newElevation >= currentMax) {
          stopSimulation();
          return;
        }

        simulationHandler.postDelayed(this, 20);
      }
    };
    simulationHandler.post(simulationRunnable);
  }

  /**
   * Stops the ongoing sea level rise simulation loop.
   */
  private void stopSimulation() {
    isSimulating = false;
    if (simulationRunnable != null) {
      simulationHandler.removeCallbacks(simulationRunnable);
      simulationRunnable = null;
    }
    if (btnAnimateFlood != null) {
      btnAnimateFlood.setText(R.string.start_simulation);
    }
  }

  // --- Teardown ---

  @Override
  protected void onDestroy() {
    stopSimulation();
    fadeHandler.removeCallbacks(fadeOutRunnable);
    if (floodPolygon != null) {
      floodPolygon.remove();
      floodPolygon = null;
    }
    super.onDestroy();
  }
}

