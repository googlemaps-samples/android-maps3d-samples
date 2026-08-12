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
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.maps3d.common.PositionAndHeading;
import com.example.maps3d.common.RouteEngine;
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
import java.util.Arrays;
import java.util.List;

public class AdvancedCameraAnimationActivity extends SampleBaseActivity {

  public enum AnimationApproach {
    SIMPLE_FLY_TO,
    KEYFRAME_TOUR,
    DISPATCHER_FRAME_LOOP,
    ORBIT_360_SPIN
  }

  public abstract static class Keyframe {

    public final String title;
    public final String description;

    public Keyframe(String title, String description) {
      this.title = title;
      this.description = description;
    }
  }

  public static class KeyframeFlyTo extends Keyframe {

    public final LatLng targetCenter;
    public final double targetAltitude;
    public final double targetHeading;
    public final double targetTilt;
    public final double targetRange;
    public final long durationMs;

    public KeyframeFlyTo(String title, String description, LatLng targetCenter,
        double targetAltitude, double targetHeading, double targetTilt, double targetRange,
        long durationMs) {
      super(title, description);
      this.targetCenter = targetCenter;
      this.targetAltitude = targetAltitude;
      this.targetHeading = targetHeading;
      this.targetTilt = targetTilt;
      this.targetRange = targetRange;
      this.durationMs = durationMs;
    }
  }

  public static class KeyframeDwell extends Keyframe {

    public final long durationMs;

    public KeyframeDwell(String title, String description, long durationMs) {
      super(title, description);
      this.durationMs = durationMs;
    }
  }

  public static class KeyframeOrbit extends Keyframe {

    public final LatLng center;
    public final double altitude;
    public final double range;
    public final double tilt;
    public final double startHeading;
    public final double endHeading;
    public final long durationMs;

    public KeyframeOrbit(String title, String description, LatLng center, double altitude,
        double range, double tilt, double startHeading, double endHeading, long durationMs) {
      super(title, description);
      this.center = center;
      this.altitude = altitude;
      this.range = range;
      this.tilt = tilt;
      this.startHeading = startHeading;
      this.endHeading = endHeading;
      this.durationMs = durationMs;
    }
  }

  private static final String MODEL_ID = "airplane_model";
  private static final String PLANE_URL = "https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/Airplane.glb";

  private Model airplaneModel;
  private int currentStepIndex = 0;
  private boolean isPlaying = false;
  private AnimationApproach selectedApproach = AnimationApproach.DISPATCHER_FRAME_LOOP;

  private Button btnPlayPause;

  private final Handler handler = new Handler(Looper.getMainLooper());
  private Runnable animationRunnable = null;

  // 15 Fine-Grained Waypoints on the direct route from Golden Gate Bridge to Coit Tower
  public static final List<LatLng> AIRPLANE_FLIGHT_PATH = Arrays.asList(
      new LatLng(37.8199, -122.4783), // 1. Golden Gate Bridge (Source)
      new LatLng(37.8188, -122.4735), // 2. Fort Point / Presidio Overlook
      new LatLng(37.8175, -122.4685), // 3. Crissy Field West
      new LatLng(37.8160, -122.4635), // 4. Crissy Field East
      new LatLng(37.8145, -122.4585), // 5. Marina Green West
      new LatLng(37.8130, -122.4530), // 6. Marina District Center
      new LatLng(37.8115, -122.4475), // 7. Fort Mason West
      new LatLng(37.8100, -122.4420), // 8. Fort Mason Heights
      new LatLng(37.8085, -122.4365), // 9. Aquatic Park Cove
      new LatLng(37.8070, -122.4310), // 10. Fisherman's Wharf West
      new LatLng(37.8058, -122.4250), // 11. Fisherman's Wharf Center
      new LatLng(37.8048, -122.4195), // 12. Pier 39 Promenade
      new LatLng(37.8038, -122.4140), // 13. Embarcadero North
      new LatLng(37.8030, -122.4090), // 14. Telegraph Hill Slopes
      new LatLng(37.8024, -122.4058)  // 15. Coit Tower (Destination)
  );

  public static final List<Keyframe> SAN_FRANCISCO_TOUR = Arrays.asList(
      new KeyframeFlyTo("1. Golden Gate Flight", "3D Airplane flight over Golden Gate Bridge",
          new LatLng(37.8199, -122.4783), 200.0, 105.0, 65.0, 600.0, 2500L),
      new KeyframeDwell("2. Mid-Air Observation", "Dwell pause observing 3D airplane", 1500L),
      new KeyframeOrbit("3. Golden Gate 360° Orbit",
          "360° orbital camera spin around flying airplane", new LatLng(37.8199, -122.4783), 200.0,
          600.0, 65.0, 105.0, 465.0, 4000L),
      new KeyframeFlyTo("4. Transit to Coit Tower", "Airplane flight to Coit Tower Landmark",
          new LatLng(37.8024, -122.4058), 200.0, 105.0, 65.0, 600.0, 3000L)
  );

  private double[] cumulativeDistances;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Inflate control panel overlay into the map container managed by SampleBaseActivity
    ViewGroup container = findViewById(R.id.map_container);
    if (container != null) {
      getLayoutInflater().inflate(R.layout.control_panel_advanced_animation, container, true);
    }

    MaterialToolbar topBar = findViewById(R.id.top_bar);
    if (topBar != null) {
      topBar.setTitle("Advanced Camera Animation");
      topBar.setNavigationOnClickListener(v -> finish());
    }

    RadioGroup rgApproach = findViewById(R.id.rg_approach);
    if (rgApproach != null) {
      rgApproach.setOnCheckedChangeListener((group, checkedId) -> {
        if (checkedId == R.id.rb_simple_flyto) {
          selectedApproach = AnimationApproach.SIMPLE_FLY_TO;
        } else if (checkedId == R.id.rb_keyframe_tour) {
          selectedApproach = AnimationApproach.KEYFRAME_TOUR;
        } else if (checkedId == R.id.rb_orbit_spin) {
          selectedApproach = AnimationApproach.ORBIT_360_SPIN;
        } else {
          selectedApproach = AnimationApproach.DISPATCHER_FRAME_LOOP;
        }
        resetAndRestartTour();
      });
    }

    Button btnReset = findViewById(R.id.btn_reset);
    if (btnReset != null) {
      btnReset.setOnClickListener(v -> resetAndRestartTour());
    }

    btnPlayPause = findViewById(R.id.btn_play_pause);
    if (btnPlayPause != null) {
      btnPlayPause.setOnClickListener(v -> {
        if (isPlaying) {
          stopTour();
        } else {
          startSelectedApproach();
        }
      });
    }
  }

  private void updatePlayPauseButtonState() {
    if (btnPlayPause != null) {
      btnPlayPause.setText(isPlaying ? "Pause" : "Play");
    }
  }

  @NonNull
  @Override
  public Camera getInitialCamera() {
    LatLng startLoc = AIRPLANE_FLIGHT_PATH.get(0);
    double initialHeading = SphericalUtil.computeHeading(startLoc, AIRPLANE_FLIGHT_PATH.get(1));
    return new Camera(
        new LatLngAltitude(startLoc.latitude, startLoc.longitude, 200.0),
        normalizeHeading(initialHeading),
        65.0,
        0.0,
        600.0
    );
  }

  @NonNull
  @Override
  public String getTAG() {
    return "AdvancedCameraAnimation";
  }

  @Override
  public void onMap3DViewReady(@NonNull GoogleMap3D googleMap3D) {
    super.onMap3DViewReady(googleMap3D);

    cumulativeDistances = RouteEngine.calculateCumulativeDistances(AIRPLANE_FLIGHT_PATH);

    LatLng startLoc = AIRPLANE_FLIGHT_PATH.get(0);
    double initialHeading = SphericalUtil.computeHeading(startLoc, AIRPLANE_FLIGHT_PATH.get(1));
    updateAirplaneModel(startLoc, initialHeading + 180.0);

    // Auto-start smooth flight animation after 1 second delay
    handler.postDelayed(this::startSelectedApproach, 1000L);
  }

  private void updateAirplaneModel(LatLng position, double planeHeadingDeg) {
    if (googleMap3D != null) {
      ModelOptions modelOptions = new ModelOptions();
      modelOptions.setId(MODEL_ID);
      modelOptions.setPosition(new LatLngAltitude(position.latitude, position.longitude, 200.0));
      modelOptions.setUrl(PLANE_URL);
      modelOptions.setAltitudeMode(AltitudeMode.ABSOLUTE);
      modelOptions.setScale(new Vector3D(0.08, 0.08, 0.08));
      modelOptions.setOrientation(new Orientation(normalizeHeading(planeHeadingDeg), -90.0, 0.0));

      airplaneModel = googleMap3D.addModel(modelOptions);
    }
  }

  private void startSelectedApproach() {
    stopTour();
    if (selectedApproach == AnimationApproach.SIMPLE_FLY_TO) {
      runSimpleFlyTo();
    } else if (selectedApproach == AnimationApproach.KEYFRAME_TOUR) {
      startOrResumeTour();
    } else if (selectedApproach == AnimationApproach.DISPATCHER_FRAME_LOOP) {
      runFrameDispatcherLoop();
    } else if (selectedApproach == AnimationApproach.ORBIT_360_SPIN) {
      run360OrbitSpin();
    }
  }

  private void runSimpleFlyTo() {
    stopTour();
    isPlaying = true;
    updatePlayPauseButtonState();

    LatLng target = AIRPLANE_FLIGHT_PATH.get(AIRPLANE_FLIGHT_PATH.size() - 1);
    double heading = SphericalUtil.computeHeading(
        AIRPLANE_FLIGHT_PATH.get(AIRPLANE_FLIGHT_PATH.size() - 2), target);
    updateAirplaneModel(target, heading + 180.0);

    Camera targetCam = new Camera(
        new LatLngAltitude(target.latitude, target.longitude, 200.0),
        normalizeHeading(heading),
        65.0,
        0.0,
        600.0
    );
    googleMap3D.flyCameraTo(new FlyToOptions(targetCam, 1500L));
    isPlaying = false;
    updatePlayPauseButtonState();
  }

  /**
   * Executes multi-step keyframe queue tour stage by stage in Java.
   */
  private void startOrResumeTour() {
    stopTour();
    isPlaying = true;
    updatePlayPauseButtonState();
    currentStepIndex = 0;
    executeNextKeyframeStep();
  }

  private void executeNextKeyframeStep() {
    if (!isPlaying || currentStepIndex >= SAN_FRANCISCO_TOUR.size()) {
      isPlaying = false;
      updatePlayPauseButtonState();
      return;
    }

    Keyframe step = SAN_FRANCISCO_TOUR.get(currentStepIndex);

    if (step instanceof KeyframeFlyTo) {
      KeyframeFlyTo flyToStep = (KeyframeFlyTo) step;
      updateAirplaneModel(flyToStep.targetCenter, flyToStep.targetHeading + 180.0);
      Camera targetCam = new Camera(
          new LatLngAltitude(flyToStep.targetCenter.latitude, flyToStep.targetCenter.longitude,
              flyToStep.targetAltitude),
          normalizeHeading(flyToStep.targetHeading),
          flyToStep.targetTilt,
          0.0,
          flyToStep.targetRange
      );
      if (googleMap3D != null) {
        googleMap3D.flyCameraTo(new FlyToOptions(targetCam, flyToStep.durationMs));
      }
      handler.postDelayed(() -> {
        currentStepIndex++;
        executeNextKeyframeStep();
      }, flyToStep.durationMs);

    } else if (step instanceof KeyframeDwell) {
      KeyframeDwell dwellStep = (KeyframeDwell) step;
      handler.postDelayed(() -> {
        currentStepIndex++;
        executeNextKeyframeStep();
      }, dwellStep.durationMs);

    } else if (step instanceof KeyframeOrbit) {
      KeyframeOrbit orbitStep = (KeyframeOrbit) step;
      final long frameMs = 16L;
      final long totalFrames = Math.max(1, orbitStep.durationMs / frameMs);

      animationRunnable = new Runnable() {
        private long currentFrame = 0;

        @Override
        public void run() {
            if (!isPlaying) {
                return;
            }

          double t = (double) currentFrame / totalFrames;
          double orbitHeading = interpolateAngle(orbitStep.startHeading, orbitStep.endHeading, t);

          updateAirplaneModel(orbitStep.center, orbitHeading + 180.0);

          Camera updatedCam = new Camera(
              new LatLngAltitude(orbitStep.center.latitude, orbitStep.center.longitude,
                  orbitStep.altitude),
              normalizeHeading(orbitHeading),
              orbitStep.tilt,
              0.0,
              orbitStep.range
          );
          if (googleMap3D != null) {
            googleMap3D.setCamera(updatedCam);
          }

          currentFrame++;
          if (currentFrame <= totalFrames) {
            handler.postDelayed(this, frameMs);
          } else {
            currentStepIndex++;
            executeNextKeyframeStep();
          }
        }
      };
      handler.post(animationRunnable);
    }
  }

  /**
   * Frame Dispatcher Animation Loop. High-speed flight animation (400 m/s) stopping cleanly at
   * destination.
   */
  private void runFrameDispatcherLoop() {
    stopTour();
    isPlaying = true;
    updatePlayPauseButtonState();

    final double totalDistance = Math.max(1.0, cumulativeDistances[cumulativeDistances.length - 1]);
    final double flightSpeedMps = 400.0;

    animationRunnable = new Runnable() {
      private double elapsedDistance = 0.0;
      private long lastTime = System.currentTimeMillis();

      @Override
      public void run() {
          if (!isPlaying) {
              return;
          }

        long now = System.currentTimeMillis();
        double dt = (now - lastTime) / 1000.0;
        lastTime = now;

        elapsedDistance += flightSpeedMps * dt;

        // Stop cleanly at destination
        if (elapsedDistance >= totalDistance) {
          elapsedDistance = totalDistance;
          PositionAndHeading posAndHeading = RouteEngine.calculatePositionAndHeading(
              AIRPLANE_FLIGHT_PATH,
              cumulativeDistances,
              elapsedDistance,
              30.0
          );
          double planeHeading = posAndHeading.getHeading() + 180.0;
          updateAirplaneModel(posAndHeading.getPosition(), planeHeading);

          Camera finalCam = new Camera(
              new LatLngAltitude(posAndHeading.getPosition().latitude,
                  posAndHeading.getPosition().longitude, 200.0),
              normalizeHeading(posAndHeading.getHeading()),
              65.0,
              0.0,
              600.0
          );
          if (googleMap3D != null) {
            googleMap3D.setCamera(finalCam);
          }
          isPlaying = false;
          updatePlayPauseButtonState();
          return;
        }

        PositionAndHeading posAndHeading = RouteEngine.calculatePositionAndHeading(
            AIRPLANE_FLIGHT_PATH,
            cumulativeDistances,
            elapsedDistance,
            30.0
        );

        double planeHeading = posAndHeading.getHeading() + 180.0;
        updateAirplaneModel(posAndHeading.getPosition(), planeHeading);

        Camera updatedCam = new Camera(
            new LatLngAltitude(posAndHeading.getPosition().latitude,
                posAndHeading.getPosition().longitude, 200.0),
            normalizeHeading(posAndHeading.getHeading()),
            65.0,
            0.0,
            600.0
        );

        if (googleMap3D != null) {
          googleMap3D.setCamera(updatedCam);
        }

        handler.postDelayed(this, 16L);
      }
    };
    handler.post(animationRunnable);
  }

  /**
   * Option 4: Continuous 360-degree orbital camera spin around landmark.
   */
  private void run360OrbitSpin() {
    stopTour();
    isPlaying = true;
    updatePlayPauseButtonState();

    final LatLng targetCenter = AIRPLANE_FLIGHT_PATH.get(0);
    updateAirplaneModel(targetCenter, 105.0 + 180.0);

    final long frameMs = 16L;
    final long totalMs = 6000L;
    final long totalFrames = Math.max(1, totalMs / frameMs);
    final double startHeading = 105.0;

    animationRunnable = new Runnable() {
      private long currentFrame = 0;

      @Override
      public void run() {
          if (!isPlaying) {
              return;
          }

        double t = (double) currentFrame / totalFrames;
        double headingDeg = (startHeading + t * 360.0) % 360.0;

        Camera currentCam = new Camera(
            new LatLngAltitude(targetCenter.latitude, targetCenter.longitude, 200.0),
            normalizeHeading(headingDeg),
            65.0,
            0.0,
            600.0
        );
        if (googleMap3D != null) {
          googleMap3D.setCamera(currentCam);
        }

        currentFrame++;
        if (currentFrame <= totalFrames) {
          handler.postDelayed(this, frameMs);
        } else {
          isPlaying = false;
          updatePlayPauseButtonState();
        }
      }
    };
    handler.post(animationRunnable);
  }

  private void stopTour() {
    isPlaying = false;
    updatePlayPauseButtonState();
    if (animationRunnable != null) {
      handler.removeCallbacks(animationRunnable);
      animationRunnable = null;
    }
    if (googleMap3D != null) {
      googleMap3D.stopCameraAnimation();
    }
  }

  /**
   * Resets the camera and airplane model to the initial start location and restarts animation.
   */
  public void resetAndRestartTour() {
    stopTour();
    currentStepIndex = 0;

    LatLng startLoc = AIRPLANE_FLIGHT_PATH.get(0);
    double initialHeading = SphericalUtil.computeHeading(startLoc, AIRPLANE_FLIGHT_PATH.get(1));
    updateAirplaneModel(startLoc, initialHeading + 180.0);

    Camera resetCam = new Camera(
        new LatLngAltitude(startLoc.latitude, startLoc.longitude, 200.0),
        normalizeHeading(initialHeading),
        65.0,
        0.0,
        600.0
    );
    if (googleMap3D != null) {
      googleMap3D.setCamera(resetCam);
    }

    handler.postDelayed(this::startSelectedApproach, 300L);
  }

  private static double normalizeHeading(double headingDeg) {
    double normalized = headingDeg % 360.0;
    return normalized < 0.0 ? normalized + 360.0 : normalized;
  }

  private static double interpolateAngle(double start, double end, double fraction) {
    double diff = (end - start) % 360.0;
      if (diff > 180.0) {
          diff -= 360.0;
      }
      if (diff < -180.0) {
          diff += 360.0;
      }
    return (start + diff * fraction + 360.0) % 360.0;
  }

  @Override
  protected void onPause() {
    super.onPause();
    stopTour();
  }
}
