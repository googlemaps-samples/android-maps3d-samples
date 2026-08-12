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
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Showcases dynamic 3D volume extrusion in Google Maps 3D SDK (Java) by simulating elevated flood
 * tides.
 */
public class DataVisualizationActivity extends SampleBaseActivity {

  public static final LatLng SF_FLOOD_CENTER = new LatLng(37.8025, -122.4030);
  private static final String POLYGON_ID = "flood_zone_polygon";

  public static final List<double[]> floodZoneCoords = Arrays.asList(
      new double[]{37.805156, -122.403256},
      new double[]{37.803370, -122.401287},
      new double[]{37.799222, -122.405080},
      new double[]{37.797500, -122.408000},
      new double[]{37.801000, -122.411000},
      new double[]{37.805156, -122.403256}
  );

  private final int waterFillColor = Color.argb(140, 230, 40, 40);
  private final int waterStrokeColor = Color.argb(255, 180, 0, 0);
  private final double waterStrokeWidth = 2.5;

  private TextView floodDepthLabel;
  private TextView floodRiskBadge;
  private Slider floodSlider;
  private Button btnAnimateFlood;

  private Polygon floodPolygon = null;
  private double currentFloodElevation = 10.0;

  private final Handler simulationHandler = new Handler(Looper.getMainLooper());
  private Runnable simulationRunnable;
  private boolean isSimulating = false;

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

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    ViewGroup container = findViewById(R.id.map_container);
    if (container != null) {
      getLayoutInflater().inflate(R.layout.control_panel_data_visualization, container, true);
    }

    MaterialToolbar topBar = findViewById(R.id.top_bar);
    if (topBar != null) {
      topBar.setTitle(R.string.feature_title_data_visualization);
      topBar.setNavigationOnClickListener(v -> finish());
    }

    floodDepthLabel = findViewById(R.id.tv_flood_depth_label);
    floodRiskBadge = findViewById(R.id.tv_flood_risk_badge);
    floodSlider = findViewById(R.id.flood_slider);
    btnAnimateFlood = findViewById(R.id.btn_animate_flood);

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
  }

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

  public void updateFloodElevation(double currentFloodHeightMeters) {
    currentFloodElevation = currentFloodHeightMeters;

    runOnUiThread(() -> {
      double feet = currentFloodHeightMeters * 3.28084;
      if (floodDepthLabel != null) {
        floodDepthLabel.setText(
            String.format(Locale.US, "Flood Elevation: +%.1f m (%.1f ft)", currentFloodHeightMeters,
                feet));
      }

      if (floodRiskBadge != null) {
        if (currentFloodHeightMeters <= 2.0) {
          floodRiskBadge.setText("🌊 Baseline Tide");
          floodRiskBadge.setTextColor(Color.parseColor("#008800"));
          floodRiskBadge.setBackgroundColor(Color.parseColor("#2000AA00"));
        } else if (currentFloodHeightMeters <= 8.0) {
          floodRiskBadge.setText("⚠️ Minor Inundation");
          floodRiskBadge.setTextColor(Color.parseColor("#BB7700"));
          floodRiskBadge.setBackgroundColor(Color.parseColor("#20FFAA00"));
        } else if (currentFloodHeightMeters <= 20.0) {
          floodRiskBadge.setText("🌊 Moderate Flooding");
          floodRiskBadge.setTextColor(Color.parseColor("#0077CC"));
          floodRiskBadge.setBackgroundColor(Color.parseColor("#200088FF"));
        } else if (currentFloodHeightMeters <= 35.0) {
          floodRiskBadge.setText("🚨 Storm Surge (Cat 3)");
          floodRiskBadge.setTextColor(Color.parseColor("#DD4400"));
          floodRiskBadge.setBackgroundColor(Color.parseColor("#25FF5500"));
        } else {
          floodRiskBadge.setText("⛔ Extreme Inundation");
          floodRiskBadge.setTextColor(Color.parseColor("#CC0000"));
          floodRiskBadge.setBackgroundColor(Color.parseColor("#25FF0000"));
        }
      }

        if (googleMap3D == null) {
            return;
        }

      List<LatLngAltitude> path = new ArrayList<>();
      for (double[] coord : floodZoneCoords) {
        path.add(new LatLngAltitude(coord[0], coord[1], currentFloodHeightMeters));
      }

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
            String.format(Locale.US, "San Francisco Waterfront - Water Level: +%.1f m",
                currentFloodElevation),
            Toast.LENGTH_SHORT
        ).show()));
      }
    });
  }

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
      btnAnimateFlood.setText("⏹ Stop Simulation");
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

  private void stopSimulation() {
    isSimulating = false;
    if (simulationRunnable != null) {
      simulationHandler.removeCallbacks(simulationRunnable);
      simulationRunnable = null;
    }
    if (btnAnimateFlood != null) {
      btnAnimateFlood.setText("▶ Start Simulation");
    }
  }

  @Override
  protected void onDestroy() {
    stopSimulation();
    super.onDestroy();
  }
}
