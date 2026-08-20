/*
 * Copyright 2025 Google LLC
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

package com.example.maps3djava.advancedcameraanimation;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
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
import com.google.android.gms.maps3d.model.Model;
import com.google.android.gms.maps3d.model.ModelOptions;
import com.google.android.gms.maps3d.model.Orientation;
import com.google.android.gms.maps3d.model.Vector3D;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.maps.android.SphericalUtil;

/**
 * Demonstrates advanced camera animations on Google Maps 3D using {@link Map3DAnimator}.
 *
 * <p>Showcases a declarative multi-step aerial tour with native flight transitions ({@link
 * FlyToStep}), stationary observation pauses ({@link DwellStep}), and 360-degree orbital spins
 * ({@link OrbitStep}) around synchronized 3D assets.
 */
public class AdvancedCameraAnimationActivity extends SampleBaseActivity {

  private static final String MODEL_ID = "airplane_model";
  private static final String PLANE_URL =
      "https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/Airplane.glb";

  // Key landmarks for the aerial tour
  private static final LatLng SF_PANORAMA_CENTER = new LatLng(37.7650, -122.4400);
  private static final LatLng GOLDEN_GATE_BRIDGE = new LatLng(37.8199, -122.4783);
  private static final LatLng COIT_TOWER = new LatLng(37.8024, -122.4058);

  private final Handler handler = new Handler(Looper.getMainLooper());
  private Map3DAnimator tourAnimator;
  private Model airplaneModel;
  private boolean isPlaying = false;

  private TextView tvTourStatus;
  private Button btnPlayPause;

  /**
   * Constructs the declarative multi-step camera tour using {@link Map3DAnimator.Builder}.
   */
  private Map3DAnimator buildTourAnimator() {
    double planeHeading = SphericalUtil.computeHeading(GOLDEN_GATE_BRIDGE, COIT_TOWER);

    OrbitOptions goldenGateOrbit =
        new OrbitOptions.Builder()
            .setCenter(GOLDEN_GATE_BRIDGE)
            .setAltitude(200.0)
            .setRange(600.0)
            .setTilt(65.0)
            .setHeadingRange(/* startHeading= */ 105.0, /* endHeading= */ 465.0)
            .setDurationMs(4500L)
            .build();

    return new Map3DAnimator.Builder()
        // Step 1: Smooth swoop from high-altitude SF panorama down into Golden Gate flight path
        .flyTo(
            getString(R.string.tour_step_1_title),
            getString(R.string.tour_step_1_desc),
            new FlyToOptions(
                new Camera(
                    new LatLngAltitude(
                        GOLDEN_GATE_BRIDGE.latitude, GOLDEN_GATE_BRIDGE.longitude, 200.0),
                    /* heading= */ 105.0,
                    /* tilt= */ 65.0,
                    /* roll= */ 0.0,
                    /* range= */ 600.0),
                /* durationMs= */ 3000L),
            /* durationMs= */ 3000L,
            () -> updateAirplaneModel(GOLDEN_GATE_BRIDGE, planeHeading + 180.0))
        // Step 2: Dwell pause holding camera steady on the airplane over the bridge
        .dwell(
            getString(R.string.tour_step_2_title),
            getString(R.string.tour_step_2_desc),
            /* durationMs= */ 1500L)
        // Step 3: 360-degree orbital camera spin around the airplane
        .orbit(
            getString(R.string.tour_step_3_title),
            getString(R.string.tour_step_3_desc),
            goldenGateOrbit)
        // Step 4: High-speed transit across San Francisco coastline to Coit Tower
        .flyTo(
            getString(R.string.tour_step_4_title),
            getString(R.string.tour_step_4_desc),
            new FlyToOptions(
                new Camera(
                    new LatLngAltitude(COIT_TOWER.latitude, COIT_TOWER.longitude, 200.0),
                    /* heading= */ 115.0,
                    /* tilt= */ 65.0,
                    /* roll= */ 0.0,
                    /* range= */ 600.0),
                /* durationMs= */ 3500L),
            /* durationMs= */ 3500L,
            () -> handler.postDelayed(
                () -> updateAirplaneModel(COIT_TOWER, planeHeading + 180.0),
                /* delayMillis= */ 3500L / 2))
        .build();
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    ViewGroup container = findViewById(R.id.map_container);
    if (container != null) {
      getLayoutInflater().inflate(R.layout.control_panel_advanced_animation, container, true);
    }

    MaterialToolbar topBar = findViewById(R.id.top_bar);
    if (topBar != null) {
      topBar.setTitle(R.string.feature_title_advanced_camera_animation);
      topBar.setNavigationOnClickListener(v -> finish());
    }

    tvTourStatus = findViewById(R.id.tv_tour_status);
    btnPlayPause = findViewById(R.id.btn_play_pause);
    Button btnReset = findViewById(R.id.btn_reset);

    if (btnPlayPause != null) {
      btnPlayPause.setOnClickListener(
          v -> {
            if (isPlaying) {
              pauseTour();
            } else {
              startOrResumeTour();
            }
          });
    }

    if (btnReset != null) {
      btnReset.setOnClickListener(v -> resetTour());
    }
  }

  @NonNull
  @Override
  public Camera getInitialCamera() {
    return new Camera(
        new LatLngAltitude(SF_PANORAMA_CENTER.latitude, SF_PANORAMA_CENTER.longitude, 300.0),
        /* heading= */ 30.0,
        /* tilt= */ 60.0,
        /* roll= */ 0.0,
        /* range= */ 4500.0);
  }

  @NonNull
  @Override
  public String getTAG() {
    return "AdvancedCameraAnimation";
  }

  @Override
  public void onMap3DViewReady(@NonNull GoogleMap3D googleMap3D) {
    super.onMap3DViewReady(googleMap3D);
    googleMap3D.setCamera(getInitialCamera());
    googleMap3D.flyCameraTo(new FlyToOptions(getInitialCamera(), 0L));

    // Re-apply after a short delay to guarantee native viewport receives target coordinates
    handler.postDelayed(
        () -> {
          if (this.googleMap3D != null) {
            this.googleMap3D.setCamera(getInitialCamera());
            this.googleMap3D.flyCameraTo(new FlyToOptions(getInitialCamera(), 0L));
          }
        },
        150L);

    double planeHeading = SphericalUtil.computeHeading(GOLDEN_GATE_BRIDGE, COIT_TOWER);
    updateAirplaneModel(GOLDEN_GATE_BRIDGE, planeHeading + 180.0);
  }

  private void startOrResumeTour() {
    if (tourAnimator == null) {
      tourAnimator = buildTourAnimator();
    }
    isPlaying = true;
    updatePlayPauseButtonState();

    if (googleMap3D != null) {
      tourAnimator.start(
          googleMap3D,
          new Map3DAnimator.Listener() {
            @Override
            public void onStepStarted(int index, @NonNull KeyframeStep step) {
              Log.d(getTAG(), "Keyframe Step " + (index + 1) + " started: " + step.getTitle());
              if (tvTourStatus != null) {
                tvTourStatus.setText(
                    getString(
                        R.string.aerial_tour_status_running,
                        index + 1,
                        tourAnimator.getSteps().size(),
                        step.getTitle()));
              }
            }

            @Override
            public void onStepCompleted(int index, @NonNull KeyframeStep step) {
              Log.d(getTAG(), "Keyframe Step " + (index + 1) + " completed: " + step.getTitle());
            }

            @Override
            public void onAnimationFinished() {
              Log.d(getTAG(), "Aerial tour completed successfully.");
              isPlaying = false;
              updatePlayPauseButtonState();
              if (tvTourStatus != null) {
                tvTourStatus.setText(R.string.aerial_tour_status_finished);
              }
            }
          });
    }
  }

  private void pauseTour() {
    isPlaying = false;
    updatePlayPauseButtonState();
    handler.removeCallbacksAndMessages(null);
    if (tourAnimator != null) {
      tourAnimator.pause();
    }
  }

  private void stopTour() {
    isPlaying = false;
    updatePlayPauseButtonState();
    handler.removeCallbacksAndMessages(null);
    if (tourAnimator != null) {
      tourAnimator.stop();
    }
    if (googleMap3D != null) {
      googleMap3D.setCameraAnimationEndListener(null);
      googleMap3D.stopCameraAnimation();
    }
  }

  public void resetTour() {
    stopTour();
    tourAnimator = null;

    if (googleMap3D != null) {
      googleMap3D.setCamera(getInitialCamera());
      googleMap3D.flyCameraTo(new FlyToOptions(getInitialCamera(), 0L));
    }

    double planeHeading = SphericalUtil.computeHeading(GOLDEN_GATE_BRIDGE, COIT_TOWER);
    updateAirplaneModel(GOLDEN_GATE_BRIDGE, planeHeading + 180.0);

    if (tvTourStatus != null) {
      tvTourStatus.setText(R.string.aerial_tour_status_idle);
    }
  }

  private void updateAirplaneModel(LatLng position, double planeHeadingDeg) {
    if (googleMap3D != null) {
      ModelOptions modelOptions = new ModelOptions();
      modelOptions.setId(MODEL_ID);
      modelOptions.setPosition(new LatLngAltitude(position.latitude, position.longitude, 200.0));
      modelOptions.setUrl(PLANE_URL);
      modelOptions.setAltitudeMode(AltitudeMode.ABSOLUTE);
      modelOptions.setScale(new Vector3D(0.08, 0.08, 0.08));
      modelOptions.setOrientation(
          new Orientation(
              /* heading= */ normalizeHeading(planeHeadingDeg),
              /* tilt= */ -90.0,
              /* roll= */ 0.0));

      airplaneModel = googleMap3D.addModel(modelOptions);
    }
  }

  private void updatePlayPauseButtonState() {
    if (btnPlayPause != null) {
      btnPlayPause.setText(isPlaying ? R.string.pause : R.string.play);
    }
  }

  private static double normalizeHeading(double headingDeg) {
    double normalized = headingDeg % 360.0;
    return normalized < 0.0 ? normalized + 360.0 : normalized;
  }

  @Override
  protected void onPause() {
    super.onPause();
    pauseTour();
  }
}
