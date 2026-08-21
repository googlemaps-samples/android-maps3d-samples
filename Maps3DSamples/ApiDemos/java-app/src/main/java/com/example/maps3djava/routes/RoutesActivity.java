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

package com.example.maps3djava.routes;

import static com.example.maps3d.common.UtilitiesKt.toHeading;
import static com.example.maps3d.common.UtilitiesKt.toValidCamera;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.example.maps3d.common.OahuRouteData;
import com.example.maps3d.common.PositionAndHeading;
import com.example.maps3d.common.RouteEngine;
import com.example.maps3dcommon.R;
import com.example.maps3djava.BuildConfig;
import com.example.maps3djava.sampleactivity.SampleBaseActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback;
import com.google.android.gms.maps3d.model.AltitudeMode;
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.LatLngAltitude;
import com.google.android.gms.maps3d.model.Map3DMode;
import com.google.android.gms.maps3d.model.Model;
import com.google.android.gms.maps3d.model.ModelOptions;
import com.google.android.gms.maps3d.model.Orientation;
import com.google.android.gms.maps3d.model.Polyline;
import com.google.android.gms.maps3d.model.PolylineOptions;
import com.google.android.gms.maps3d.model.Vector3D;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Demonstrates cross-product integration between Google Maps Platform Routes API and GoogleMap3D.
 *
 * <p>Key Architecture Highlights:
 * <ul>
 *   <li><b>Async Route Calculation:</b> Dispatches background tasks via {@link RouteRepository} to
 *       fetch driving directions in Honolulu, falling back gracefully to bundled Oahu coordinates
 *       when offline or unauthenticated.</li>
 *   <li><b>3D Polyline Visualization:</b> Renders clamped-to-ground 3D route paths that conform
 *       seamlessly to elevation and terrain variations.</li>
 *   <li><b>3D glTF Model Anchoring:</b> Loads a 3D vehicle model and updates its geographic
 *       coordinates and heading on each tick along the path.</li>
 *   <li><b>Dynamic Camera Tracking:</b> Synchronizes the 3D camera to follow behind the vehicle
 *       with configurable altitude, speed, and yaw offsets.</li>
 *   <li><b>Ergonomic Collapsible UI:</b> Employs an animated Material3 floating card with
 *       touch-aware auto-fade after 3 seconds of inactivity.</li>
 * </ul>
 */
public class RoutesActivity extends SampleBaseActivity implements OnMap3DViewReadyCallback {

  // --- Constants ---

  private static final long FADE_DELAY_MS = 3000L;
  private static final float ACTIVE_ALPHA = 1.0f;
  private static final float FADED_ALPHA = 0.85f;

  // --- View Bindings ---

  private CardView controlsCard;
  private View cardHeader;
  private View cardContent;
  private MaterialButton btnCollapse;
  private MaterialButton btnPlayPause;
  private Slider progressSlider;
  private Slider rangeSlider;
  private TextView rangeSliderLabel;
  private Slider speedSlider;
  private TextView speedSliderLabel;
  private Slider headingSlider;
  private TextView headingSliderLabel;

  // --- State Variables ---

  private final RouteRepository routeRepository = new RouteRepository();
  private List<LatLng> decodedRoute = new ArrayList<>();
  private double[] cumulativeDistances = new double[] {0.0};
  private double totalDistance = 0.0;
  private double elapsedDistance = 0.0;

  private boolean isPlaying = false;
  private boolean isUserScrubbing = false;
  private boolean isCollapsed = false;

  // --- Slider Parameters ---

  private float cameraRange = 1500f;
  private float vehicleSpeedMps = 150f;
  private float yawOffset = 0f;

  // --- Map References ---

  private Polyline routePolyline;
  private Model vehicleModel;

  // --- Background Executors & Handlers ---

  private final ExecutorService executorService = Executors.newSingleThreadExecutor();
  private final Handler mainHandler = new Handler(Looper.getMainLooper());
  private final Handler fadeHandler = new Handler(Looper.getMainLooper());

  private final Runnable fadeOutRunnable =
      () -> {
        if (controlsCard != null && !isCollapsed) {
          controlsCard.animate().alpha(FADED_ALPHA).setDuration(400).start();
        }
      };

  // --- Base Activity Overrides ---

  @NonNull
  @Override
  public String getTAG() {
    return "RoutesActivity";
  }

  @NonNull
  @Override
  public Camera getInitialCamera() {
    return toValidCamera(
        new Camera(
            new LatLngAltitude(21.348567, -157.803961, 0.0),
            38.6,
            45.0,
            0.0,
            20000.0));
  }

  // --- Lifecycle & Layout Setup ---

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
      getLayoutInflater().inflate(R.layout.control_panel_routes, container, true);
    }

    MaterialToolbar topBar = findViewById(R.id.top_bar);
    if (topBar != null) {
      topBar.setTitle(R.string.feature_title_routes_api);
      topBar.setNavigationOnClickListener(v -> finish());
    }

    initViews();
  }

  /** Initializes view references and wires up touch and click listeners. */
  @SuppressLint("ClickableViewAccessibility")
  private void initViews() {
    controlsCard = findViewById(R.id.control_panel);
    cardHeader = findViewById(R.id.card_header);
    cardContent = findViewById(R.id.card_content);
    btnCollapse = findViewById(R.id.btn_collapse);

    btnPlayPause = findViewById(R.id.btn_play_pause);
    progressSlider = findViewById(R.id.progress_slider);
    rangeSlider = findViewById(R.id.range_slider);
    rangeSliderLabel = findViewById(R.id.range_slider_label);
    speedSlider = findViewById(R.id.speed_slider);
    speedSliderLabel = findViewById(R.id.speed_slider_label);
    headingSlider = findViewById(R.id.heading_slider);
    headingSliderLabel = findViewById(R.id.heading_slider_label);

    if (btnCollapse != null) {
      btnCollapse.setOnClickListener(
          v -> {
            if (isCollapsed) {
              expandControls();
            } else {
              collapseControls();
            }
          });
    }

    if (cardHeader != null) {
      cardHeader.setOnClickListener(
          v -> {
            if (isCollapsed) {
              expandControls();
            }
          });
    }

    if (controlsCard != null) {
      controlsCard.setOnTouchListener(
          (v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
              resetFadeTimer();
            }
            return false;
          });
    }

    setupControls();
    resetFadeTimer();
  }

  /** Configures sliders and playback button behaviors. */
  private void setupControls() {
    if (btnPlayPause != null) {
      btnPlayPause.setOnClickListener(
          view -> {
            resetFadeTimer();
            if (decodedRoute.isEmpty()) {
              Toast.makeText(this, R.string.route_loading, Toast.LENGTH_SHORT).show();
              return;
            }
            togglePlayback(!isPlaying);
          });
    }

    // Scrub progress manually
    if (progressSlider != null) {
      progressSlider.addOnSliderTouchListener(
          new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
              resetFadeTimer();
              isUserScrubbing = true;
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
              resetFadeTimer();
              isUserScrubbing = false;
              elapsedDistance = totalDistance * slider.getValue();
              updateVehiclePositionAndCamera();
            }
          });

      progressSlider.addOnChangeListener(
          (slider, value, fromUser) -> {
            if (fromUser && isUserScrubbing) {
              resetFadeTimer();
              elapsedDistance = totalDistance * value;
              updateVehiclePositionAndCamera();
            }
          });
    }

    // Camera altitude adjustments
    if (rangeSliderLabel != null && rangeSlider != null) {
      rangeSliderLabel.setText(getString(R.string.camera_altitude_format, (int) cameraRange));
      rangeSlider.setValue(cameraRange);
      rangeSlider.addOnChangeListener(
          (slider, value, fromUser) -> {
            resetFadeTimer();
            cameraRange = value;
            rangeSliderLabel.setText(getString(R.string.camera_altitude_format, (int) value));
            updateVehiclePositionAndCamera();
          });
    }

    // Speed configurations
    if (speedSliderLabel != null && speedSlider != null) {
      speedSliderLabel.setText(getString(R.string.vehicle_speed_format, (int) vehicleSpeedMps));
      speedSlider.setValue(vehicleSpeedMps);
      speedSlider.addOnChangeListener(
          (slider, value, fromUser) -> {
            resetFadeTimer();
            vehicleSpeedMps = value;
            speedSliderLabel.setText(getString(R.string.vehicle_speed_format, (int) value));
          });
    }

    // Camera yaw offset adjustments
    if (headingSliderLabel != null && headingSlider != null) {
      headingSliderLabel.setText(getString(R.string.camera_yaw_offset_format, (int) yawOffset));
      headingSlider.setValue(yawOffset);
      headingSlider.addOnChangeListener(
          (slider, value, fromUser) -> {
            resetFadeTimer();
            yawOffset = value;
            headingSliderLabel.setText(getString(R.string.camera_yaw_offset_format, (int) value));
            updateVehiclePositionAndCamera();
          });
    }
  }

  // --- Collapsible UI Transitions ---

  private void collapseControls() {
    if (controlsCard == null || cardContent == null || isCollapsed) return;
    isCollapsed = true;
    fadeHandler.removeCallbacks(fadeOutRunnable);
    controlsCard.animate().alpha(ACTIVE_ALPHA).setDuration(150).start();
    TransitionManager.beginDelayedTransition(controlsCard);
    cardContent.setVisibility(View.GONE);
    if (btnCollapse != null) {
      btnCollapse.setIconResource(R.drawable.expand_less_24px);
      btnCollapse.setContentDescription(getString(R.string.expand_controls));
    }
  }

  private void expandControls() {
    if (controlsCard == null || cardContent == null || !isCollapsed) return;
    isCollapsed = false;
    TransitionManager.beginDelayedTransition(controlsCard);
    cardContent.setVisibility(View.VISIBLE);
    if (btnCollapse != null) {
      btnCollapse.setIconResource(R.drawable.expand_more_24px);
      btnCollapse.setContentDescription(getString(R.string.collapse_controls));
    }
    resetFadeTimer();
  }

  private void resetFadeTimer() {
    if (controlsCard != null) {
      controlsCard.animate().alpha(ACTIVE_ALPHA).setDuration(150).start();
    }
    fadeHandler.removeCallbacks(fadeOutRunnable);
    if (!isCollapsed) {
      fadeHandler.postDelayed(fadeOutRunnable, FADE_DELAY_MS);
    }
  }

  private void togglePlayback(boolean play) {
    isPlaying = play;
    if (btnPlayPause != null) {
      if (play) {
        btnPlayPause.setIconResource(R.drawable.pause_24px);
        startAnimationLoop();
      } else {
        btnPlayPause.setIconResource(R.drawable.play_arrow_24px);
        stopAnimationLoop();
      }
    }
  }

  // --- Map Readiness & Route Rendering ---

  @Override
  public void onMap3DViewReady(@NonNull GoogleMap3D googleMap3D) {
    super.onMap3DViewReady(googleMap3D);
    googleMap3D.setMapMode(Map3DMode.SATELLITE);

    // Trigger async route fetch on executor thread
    loadAndRenderRouteAsync(googleMap3D);
  }

  private void loadAndRenderRouteAsync(@NonNull GoogleMap3D googleMap3D) {
    String apiKey = BuildConfig.MAPS3D_API_KEY;
    LatLng origin = new LatLng(21.307043, -157.858984);
    LatLng destination = new LatLng(21.390177, -157.719454);

    executorService.execute(
        () -> {
          List<LatLng> decoded;
          try {
            if (apiKey.isEmpty() || apiKey.contains("YOUR_API_KEY")) {
              throw new Exception("Invalid or missing API Key");
            }
            RouteData routeData =
                routeRepository.fetchRouteCallable(apiKey, origin, destination).call();
            decoded = PolyUtil.decode(routeData.getEncodedPolyline());
          } catch (Exception e) {
            Log.w(
                getTAG(),
                "Routes API fetch failed ("
                    + e.getLocalizedMessage()
                    + "). Falling back to pre-baked Oahu mountain route.");
            decoded = OahuRouteData.getFALLBACK_ROUTE();
            mainHandler.post(
                () ->
                    Toast.makeText(
                            RoutesActivity.this,
                            R.string.routes_offline_fallback,
                            Toast.LENGTH_LONG)
                        .show());
          }

          final List<LatLng> finalDecoded = decoded;
          mainHandler.post(
              () -> {
                decodedRoute = finalDecoded;
                cumulativeDistances = RouteEngine.calculateCumulativeDistances(finalDecoded);
                totalDistance = cumulativeDistances[cumulativeDistances.length - 1];

                // 1. Draw the blue route polyline
                List<LatLngAltitude> linePath = new ArrayList<>();
                for (LatLng point : finalDecoded) {
                  linePath.add(new LatLngAltitude(point.latitude, point.longitude, 0.0));
                }

                PolylineOptions polyOptions = new PolylineOptions();
                polyOptions.setPath(linePath);
                polyOptions.setStrokeColor(Color.BLUE);
                polyOptions.setStrokeWidth(10.0);
                polyOptions.setAltitudeMode(AltitudeMode.CLAMP_TO_GROUND);
                polyOptions.setZIndex(5);
                routePolyline = googleMap3D.addPolyline(polyOptions);

                // 2. Load the 3D Car model
                ModelOptions modelOpts = new ModelOptions();
                modelOpts.setId("vehicle_car_java");
                modelOpts.setPosition(
                    new LatLngAltitude(
                        finalDecoded.get(0).latitude, finalDecoded.get(0).longitude, 25.0));
                modelOpts.setAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);
                modelOpts.setOrientation(new Orientation(0.0, -90.0, 0.0));
                modelOpts.setUrl(
                    "https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/red_car.glb");
                modelOpts.setScale(new Vector3D(50.0, 50.0, 50.0));
                vehicleModel = googleMap3D.addModel(modelOpts);

                updateVehiclePositionAndCamera();

                // Trigger play automatically once map is populated
                togglePlayback(true);
              });
        });
  }

  // --- Animation Tick Engine ---

  private long lastTime = 0;
  private final Runnable animationTickRunnable =
      new Runnable() {
        @Override
        public void run() {
          if (!isPlaying || totalDistance <= 0.0) return;

          long now = System.currentTimeMillis();
          double dt = (now - lastTime) / 1000.0; // Delta time in seconds
          lastTime = now;

          elapsedDistance += vehicleSpeedMps * dt;

          // Clamp / loop playback
          if (elapsedDistance >= totalDistance) {
            elapsedDistance = 0.0;
          }

          // Sync progress slider
          if (!isUserScrubbing && progressSlider != null) {
            progressSlider.setValue((float) (elapsedDistance / totalDistance));
          }

          updateVehiclePositionAndCamera();

          // Post next frame with ~16ms delays (60fps target)
          mainHandler.postDelayed(this, 16);
        }
      };

  private void startAnimationLoop() {
    lastTime = System.currentTimeMillis();
    mainHandler.post(animationTickRunnable);
  }

  private void stopAnimationLoop() {
    mainHandler.removeCallbacks(animationTickRunnable);
  }

  /** Interpolates geographic vectors and repositions models/camera. */
  private void updateVehiclePositionAndCamera() {
    if (decodedRoute.isEmpty() || totalDistance <= 0.0) return;

    PositionAndHeading posAndHeading =
        RouteEngine.calculatePositionAndHeading(
            decodedRoute, cumulativeDistances, elapsedDistance, 30.0);

    // 1. Upsert Model position and rotation on every tick using the same ID
    if (googleMap3D != null) {
      ModelOptions modelOpts = new ModelOptions();
      modelOpts.setId("vehicle_car_java");
      modelOpts.setPosition(
          new LatLngAltitude(
              posAndHeading.getPosition().latitude, posAndHeading.getPosition().longitude, 25.0));
      modelOpts.setAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);
      modelOpts.setOrientation(new Orientation(posAndHeading.getHeading(), -90.0, 0.0));
      modelOpts.setUrl("https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/red_car.glb");
      modelOpts.setScale(new Vector3D(50.0, 50.0, 50.0));
      vehicleModel = googleMap3D.addModel(modelOpts);
    }

    // 2. Update Camera center and bearing
    if (googleMap3D != null) {
      Camera trackingCamera =
          toValidCamera(
              new Camera(
                  new LatLngAltitude(
                      posAndHeading.getPosition().latitude,
                      posAndHeading.getPosition().longitude,
                      0.0),
                  toHeading(posAndHeading.getHeading() + yawOffset),
                  65.0,
                  0.0,
                  (double) cameraRange));
      googleMap3D.setCamera(trackingCamera);
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    togglePlayback(false);
    fadeHandler.removeCallbacks(fadeOutRunnable);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    stopAnimationLoop();
    fadeHandler.removeCallbacks(fadeOutRunnable);
    executorService.shutdown();
  }
}
