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

package com.example.maps3djava.pathfollowing;

import static com.example.maps3d.common.UtilitiesKt.toHeading;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.WindowCompat;
import com.example.maps3dcommon.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.Map3DView;
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback;
import com.google.android.gms.maps3d.model.AltitudeMode;
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.LatLngAltitude;
import com.google.android.gms.maps3d.model.Polyline;
import com.google.android.gms.maps3d.model.PolylineOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.google.maps.android.SphericalUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Advanced sample demonstrating ground-level path following in Java.
 *
 * Features: - Urban vs Rural ground-level paths - Real-time camera controls via sliders: Range,
 * Ground Altitude, Heading Offset, Tilt, Follow Speed - Smooth Handler-based frame animation along
 * the route - Contracting and fading polyline playback visualization
 */
public class PathFollowingActivity extends AppCompatActivity implements OnMap3DViewReadyCallback {

  private Map3DView map3DView;
  private GoogleMap3D googleMap3D;

  // Urban Path (New York City - Central Park Block Circuit)
  public static final List<LatLng> URBAN_PATH = Arrays.asList(
      new LatLng(40.7783119, -73.9627630),
      new LatLng(40.7776355, -73.9611664),
      new LatLng(40.7770011, -73.9616294),
      new LatLng(40.7776743, -73.9632228),
      new LatLng(40.7783119, -73.9627630)
  );

  // Rural Path
  public static final List<LatLng> RURAL_PATH = Arrays.asList(
      new LatLng(37.254529, -122.380897),
      new LatLng(37.255065, -122.381627),
      new LatLng(37.257540, -122.383720),
      new LatLng(37.261200, -122.383950),
      new LatLng(37.264780, -122.388210),
      new LatLng(37.268520, -122.392450),
      new LatLng(37.272110, -122.397640),
      new LatLng(37.276430, -122.401120),
      new LatLng(37.280850, -122.403560),
      new LatLng(37.286018, -122.405072),
      new LatLng(37.291040, -122.404210),
      new LatLng(37.295800, -122.401980),
      new LatLng(37.300120, -122.399540),
      new LatLng(37.304550, -122.397210),
      new LatLng(37.309200, -122.395100),
      new LatLng(37.313450, -122.392840),
      new LatLng(37.317200, -122.390510),
      new LatLng(37.320850, -122.388740),
      new LatLng(37.323540, -122.387600),
      new LatLng(37.325269, -122.386728)
  );

  // View Bindings
  private RadioGroup rgEnvironment;
  private RadioGroup rgAltitudeMode;
  private Slider pathAltitudeSlider;
  private TextView pathAltitudeSliderLabel;
  private MaterialButton btnPlayPause;
  private Slider progressSlider;
  private Slider rangeSlider;
  private TextView rangeSliderLabel;
  private Slider altitudeSlider;
  private TextView altitudeSliderLabel;
  private Slider headingSlider;
  private TextView headingSliderLabel;
  private Slider tiltSlider;
  private TextView tiltSliderLabel;
  private Slider speedSlider;
  private TextView speedSliderLabel;

  // Control parameters
  private double cameraRange = 300.0;
  private double groundAltitude = 20.0;
  private double headingOffset = 0.0;
  private double cameraTilt = 70.0;
  private double followSpeedMps = 30.0;
  private @AltitudeMode int pathAltitudeMode = AltitudeMode.CLAMP_TO_GROUND;
  private double pathAltitudeOffset = 0.5;

  // Path state
  private List<LatLng> currentPath = URBAN_PATH;
  private double[] cumulativeDistances = new double[0];
  private double totalDistance = 0.0;
  private double elapsedDistance = 0.0;
  private boolean isPlaying = false;
  private boolean isUserScrubbing = false;

  // Polyline IDs
  private static final String STATIC_ROUTE_POLYLINE_ID = "path_following_static_route";
  private static final String PROGRESS_POLYLINE_ID = "path_following_progress_route";

  private Polyline staticRoutePolyline = null;
  private Polyline progressPolyline = null;
  private final Handler handler = new Handler(Looper.getMainLooper());
  private Runnable animationRunnable;

  private CardView controlsCard;
  private android.view.View cardHeader;
  private MaterialButton btnCollapse;
  private boolean isCollapsed = false;

  private final Handler fadeHandler = new Handler(Looper.getMainLooper());
  private final Runnable fadeOutRunnable = () -> {
    if (controlsCard != null && !isCollapsed) {
      controlsCard.animate()
          .alpha(0.8f) // Subtle fade when idle
          .setDuration(400)
          .start();
    }
  };

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
    float targetTranslationY = Math.max(0, controlsCard.getHeight() - headerHeight);
    controlsCard.animate()
        .translationY(targetTranslationY)
        .alpha(0.9f)
        .setDuration(300)
        .start();
  }

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

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_path_following);

    map3DView = findViewById(R.id.map3dView);
    map3DView.onCreate(savedInstanceState);
    map3DView.getMap3DViewAsync(this);

    initViews();
    loadPathData(URBAN_PATH);
  }

  @Override
  protected void onResume() {
    super.onResume();
    map3DView.onResume();
  }

  @Override
  protected void onPause() {
    super.onPause();
    map3DView.onPause();
    pauseAnimation();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    pauseAnimation();
    fadeHandler.removeCallbacks(fadeOutRunnable);
    if (staticRoutePolyline != null) {
      staticRoutePolyline.remove();
      staticRoutePolyline = null;
    }
    if (progressPolyline != null) {
      progressPolyline.remove();
      progressPolyline = null;
    }
    map3DView.onDestroy();
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    map3DView.onLowMemory();
  }

  @Override
  protected void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    map3DView.onSaveInstanceState(outState);
  }

  private void initViews() {
    controlsCard = findViewById(R.id.controls_card);
    cardHeader = findViewById(R.id.card_header);
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
        }
      });
    }

    fadeHandler.postDelayed(fadeOutRunnable, 3000L);

    rgEnvironment = findViewById(R.id.rg_environment);
    rgAltitudeMode = findViewById(R.id.rg_altitude_mode);
    pathAltitudeSlider = findViewById(R.id.path_altitude_slider);
    pathAltitudeSliderLabel = findViewById(R.id.path_altitude_slider_label);
    btnPlayPause = findViewById(R.id.btn_play_pause);
    progressSlider = findViewById(R.id.progress_slider);

    rangeSlider = findViewById(R.id.range_slider);
    rangeSliderLabel = findViewById(R.id.range_slider_label);
    altitudeSlider = findViewById(R.id.altitude_slider);
    altitudeSliderLabel = findViewById(R.id.altitude_slider_label);
    headingSlider = findViewById(R.id.heading_slider);
    headingSliderLabel = findViewById(R.id.heading_slider_label);
    tiltSlider = findViewById(R.id.tilt_slider);
    tiltSliderLabel = findViewById(R.id.tilt_slider_label);
    speedSlider = findViewById(R.id.speed_slider);
    speedSliderLabel = findViewById(R.id.speed_slider_label);

    updateControlLabels();

    // Radio group environment selection
    rgEnvironment.setOnCheckedChangeListener((group, checkedId) -> {
      if (checkedId == R.id.rb_urban) {
        altitudeSlider.setValueTo(200.0f);
        switchEnvironment(URBAN_PATH);
      } else if (checkedId == R.id.rb_rural) {
        altitudeSlider.setValueTo(2000.0f);
        switchEnvironment(RURAL_PATH);
      }
    });

    // Radio group altitude mode selection
    rgAltitudeMode.setOnCheckedChangeListener((group, checkedId) -> {
      if (checkedId == R.id.rb_relative_to_ground) {
        pathAltitudeMode = AltitudeMode.RELATIVE_TO_GROUND;
      } else if (checkedId == R.id.rb_clamp_to_ground) {
        pathAltitudeMode = AltitudeMode.CLAMP_TO_GROUND;
      } else if (checkedId == R.id.rb_relative_to_mesh) {
        pathAltitudeMode = AltitudeMode.RELATIVE_TO_MESH;
      } else if (checkedId == R.id.rb_absolute) {
        pathAltitudeMode = AltitudeMode.ABSOLUTE;
      }
      drawStaticRoutePolyline();
      updateCameraPositionForDistance(elapsedDistance);
    });

    // Path height slider (relative altitude)
    pathAltitudeSlider.addOnChangeListener((slider, value, fromUser) -> {
      pathAltitudeOffset = value;
      pathAltitudeSliderLabel.setText(
          getString(R.string.path_height_format, pathAltitudeOffset));
      drawStaticRoutePolyline();
      updateCameraPositionForDistance(elapsedDistance);
    });

    // Play/Pause button
    btnPlayPause.setOnClickListener(v -> {
      if (isPlaying) {
        pauseAnimation();
      } else {
        startAnimation();
      }
    });

    // Progress slider scrubbing
    progressSlider.addOnChangeListener((slider, value, fromUser) -> {
      if (fromUser) {
        isUserScrubbing = true;
        elapsedDistance = totalDistance * value;
        updateCameraPositionForDistance(elapsedDistance);
      }
    });
    progressSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
      @Override
      public void onStartTrackingTouch(@NonNull Slider slider) {
        isUserScrubbing = true;
      }

      @Override
      public void onStopTrackingTouch(@NonNull Slider slider) {
        isUserScrubbing = false;
      }
    });

    // Sliders listeners
    rangeSlider.addOnChangeListener((slider, value, fromUser) -> {
      cameraRange = value;
      rangeSliderLabel.setText(getString(R.string.camera_range_format, (int) cameraRange));
      updateCameraPositionForDistance(elapsedDistance);
    });

    altitudeSlider.addOnChangeListener((slider, value, fromUser) -> {
      groundAltitude = value;
      altitudeSliderLabel.setText(getString(R.string.ground_altitude_format, (int) groundAltitude));
      updateCameraPositionForDistance(elapsedDistance);
    });

    headingSlider.addOnChangeListener((slider, value, fromUser) -> {
      headingOffset = value;
      headingSliderLabel.setText(getString(R.string.heading_offset_format, (int) headingOffset));
      updateCameraPositionForDistance(elapsedDistance);
    });

    tiltSlider.addOnChangeListener((slider, value, fromUser) -> {
      cameraTilt = value;
      tiltSliderLabel.setText(getString(R.string.camera_tilt_format, (int) cameraTilt));
      updateCameraPositionForDistance(elapsedDistance);
    });

    speedSlider.addOnChangeListener((slider, value, fromUser) -> {
      followSpeedMps = value;
      speedSliderLabel.setText(getString(R.string.follow_speed_format, (int) followSpeedMps));
    });
  }

  private void updateControlLabels() {
    pathAltitudeSliderLabel.setText(
        getString(R.string.path_height_format, pathAltitudeOffset));
    rangeSliderLabel.setText(getString(R.string.camera_range_format, (int) cameraRange));
    altitudeSliderLabel.setText(
        getString(R.string.ground_altitude_format, (int) groundAltitude));
    headingSliderLabel.setText(
        getString(R.string.heading_offset_format, (int) headingOffset));
    tiltSliderLabel.setText(getString(R.string.camera_tilt_format, (int) cameraTilt));
    speedSliderLabel.setText(
        getString(R.string.follow_speed_format, (int) followSpeedMps));
  }

  @Override
  public void onMap3DViewReady(@NonNull GoogleMap3D googleMap3D) {
    this.googleMap3D = googleMap3D;
    googleMap3D.setOnMapReadyListener((map) -> {
      googleMap3D.setOnMapReadyListener(null);
      runOnUiThread(() -> {
        drawPathPolylines();
        updateCameraPositionForDistance(0.0);
        startAnimation();
      });
    });
  }

  private Double currentHeading = null;

  private void switchEnvironment(List<LatLng> path) {
    pauseAnimation();
    currentHeading = null;
    elapsedDistance = 0.0;
    progressSlider.setValue(0f);

    if (path.equals(RURAL_PATH)) {
      cameraRange = 450.0;
      groundAltitude = 40.0;
      cameraTilt = 75.0;
      rangeSlider.setValue(450f);
      altitudeSlider.setValue(40f);
      tiltSlider.setValue(75f);
      rangeSliderLabel.setText(getString(R.string.camera_range_format, 450));
      altitudeSliderLabel.setText(getString(R.string.ground_altitude_format, 40));
      tiltSliderLabel.setText(getString(R.string.camera_tilt_format, 75));
    } else {
      cameraRange = 300.0;
      groundAltitude = 20.0;
      cameraTilt = 70.0;
      rangeSlider.setValue(300f);
      altitudeSlider.setValue(20f);
      tiltSlider.setValue(70f);
      rangeSliderLabel.setText(getString(R.string.camera_range_format, 300));
      altitudeSliderLabel.setText(getString(R.string.ground_altitude_format, 20));
      tiltSliderLabel.setText(getString(R.string.camera_tilt_format, 70));
    }

    loadPathData(path);
    drawPathPolylines();
    updateCameraPositionForDistance(0.0);
  }

  private void loadPathData(List<LatLng> path) {
    currentPath = path;
    cumulativeDistances = new double[path.size()];
    totalDistance = 0.0;
    cumulativeDistances[0] = 0.0;
    for (int i = 1; i < path.size(); i++) {
      double dist = SphericalUtil.computeDistanceBetween(path.get(i - 1), path.get(i));
      totalDistance += dist;
      cumulativeDistances[i] = totalDistance;
    }
  }

  private void drawPathPolylines() {
    drawStaticRoutePolyline();
    if (!currentPath.isEmpty()) {
      updateProgressPolyline(elapsedDistance, currentPath.get(0), 0);
    }
  }

  private void drawStaticRoutePolyline() {
    if (googleMap3D == null || currentPath.isEmpty()) {
      return;
    }

    double pathAltitude = (pathAltitudeMode == AltitudeMode.CLAMP_TO_GROUND)
        ? 0.0
        : pathAltitudeOffset;
    if (pathAltitudeMode == AltitudeMode.ABSOLUTE) {
      pathAltitude = (currentPath.equals(RURAL_PATH)) ? 40.0 : 15.0;
    }

    List<LatLngAltitude> staticVertices = new ArrayList<>();
    for (LatLng latLng : currentPath) {
      staticVertices.add(new LatLngAltitude(latLng.latitude, latLng.longitude, pathAltitude));
    }

    PolylineOptions staticOptions = new PolylineOptions();
    staticOptions.setId(STATIC_ROUTE_POLYLINE_ID); // Fixed ID prevents flickering and updates in place
    staticOptions.setPath(staticVertices);
    staticOptions.setStrokeColor(Color.parseColor("#4285F4")); // Route line: blue
    staticOptions.setStrokeWidth(16.0); // Route line: wider
    staticOptions.setZIndex(1); // Route line: lower z-index
    staticOptions.setAltitudeMode(pathAltitudeMode);

    staticRoutePolyline = googleMap3D.addPolyline(staticOptions);
  }

  private void updateProgressPolyline(double dist, LatLng currentLatLng, int index) {
    if (googleMap3D == null || currentPath.isEmpty() || totalDistance <= 0.0) {
      return;
    }

    double pathAltitude = (pathAltitudeMode == AltitudeMode.CLAMP_TO_GROUND)
        ? 0.0
        : pathAltitudeOffset;
    if (pathAltitudeMode == AltitudeMode.ABSOLUTE) {
      pathAltitude = (currentPath.equals(RURAL_PATH)) ? 40.0 : 15.0;
    }

    double progressAltitude = pathAltitude + 0.2;

    // Build traversed route from start to current position
    List<LatLngAltitude> progressCoordinates = new ArrayList<>();
    for (int i = 0; i <= index && i < currentPath.size(); i++) {
      LatLng pt = currentPath.get(i);
      progressCoordinates.add(new LatLngAltitude(pt.latitude, pt.longitude, progressAltitude));
    }
    progressCoordinates.add(
        new LatLngAltitude(currentLatLng.latitude, currentLatLng.longitude, progressAltitude));

    // Polyline requires at least 2 vertices
    if (progressCoordinates.size() < 2) {
      LatLng startPt = currentPath.get(0);
      progressCoordinates.add(new LatLngAltitude(startPt.latitude, startPt.longitude, progressAltitude));
    }

    // Dual-Polyline Rendering Technique:
    // We render two layered polylines to clearly visualize completed vs. remaining route:
    // 1. Base Static Polyline: A wider (#4285F4 blue, 16dp) static path at ZIndex=1.
    // 2. Traversed Progress Polyline: A narrower (#9C27B0 purple, 8dp) line at ZIndex=2.
    // We also apply a slight vertical altitude bias (+0.2m) to prevent 3D depth buffer z-fighting.
    PolylineOptions progressOptions = new PolylineOptions();
    progressOptions.setId(PROGRESS_POLYLINE_ID); // Fixed ID upserts in place to eliminate flickering
    progressOptions.setPath(progressCoordinates);
    progressOptions.setStrokeColor(Color.parseColor("#9C27B0")); // Progress line: purple
    progressOptions.setStrokeWidth(8.0); // Progress line: narrower
    progressOptions.setZIndex(2); // Progress line: higher z-index on top of static route
    progressOptions.setAltitudeMode(pathAltitudeMode);

    progressPolyline = googleMap3D.addPolyline(progressOptions);
  }

  private void startAnimation() {
    if (isPlaying) {
      return;
    }
    isPlaying = true;
    btnPlayPause.setIconResource(R.drawable.pause_24px);

    final long frameDurationMs = 16L;
    animationRunnable = new Runnable() {
      @Override
      public void run() {
        if (!isPlaying) {
          return;
        }

        double stepDistance = followSpeedMps * (frameDurationMs / 1000.0);
        elapsedDistance += stepDistance;

        if (elapsedDistance >= totalDistance) {
          elapsedDistance = 0.0;
        }

        if (!isUserScrubbing && totalDistance > 0) {
          float progress = (float) (elapsedDistance / totalDistance);
          if (progress >= 0f && progress <= 1f) {
            progressSlider.setValue(progress);
          }
        }

        updateCameraPositionForDistance(elapsedDistance);
        handler.postDelayed(this, frameDurationMs);
      }
    };

    handler.post(animationRunnable);
  }

  private void pauseAnimation() {
    isPlaying = false;
    btnPlayPause.setIconResource(R.drawable.play_arrow_24px);
    if (animationRunnable != null) {
      handler.removeCallbacks(animationRunnable);
      animationRunnable = null;
    }
  }

  private void updateCameraPositionForDistance(double dist) {
    if (googleMap3D == null || currentPath.isEmpty()) {
      return;
    }

    int index = 0;
    while (index < cumulativeDistances.length - 1 && cumulativeDistances[index + 1] < dist) {
      index++;
    }

    LatLng p1 = currentPath.get(index);
    LatLng p2 = (index < currentPath.size() - 1) ? currentPath.get(index + 1) : p1;

    double segStartDist = cumulativeDistances[index];
    double segEndDist =
        (index < cumulativeDistances.length - 1) ? cumulativeDistances[index + 1] : totalDistance;
    double segLen = segEndDist - segStartDist;

    double fraction =
        (segLen > 0) ? Math.max(0.0, Math.min(1.0, (dist - segStartDist) / segLen)) : 0.0;
    LatLng currentLatLng = SphericalUtil.interpolate(p1, p2, fraction);
    double bearing = SphericalUtil.computeHeading(p1, p2);

    // Kinematic Heading Smoothing (Exponential Moving Average):
    // Sharp polyline bends can produce disorienting camera jerks. We compute the shortest
    // angular difference wrapped to [-180, 180] and apply an exponential low-pass filter (12% lerp)
    // to smoothly turn the camera around street corners and mountain switchbacks.
    double targetHeadingRaw = toHeading(bearing + headingOffset);
    double targetHeading;
    if (currentHeading == null || isUserScrubbing || !isPlaying) {
      targetHeading = targetHeadingRaw;
    } else {
      double diff = (targetHeadingRaw - currentHeading) % 360.0;
      if (diff > 180.0) {
        diff -= 360.0;
      }
      if (diff < -180.0) {
        diff += 360.0;
      }
      targetHeading = toHeading(currentHeading + diff * 0.12);
    }
    currentHeading = targetHeading;

    Camera newCamera = new Camera(
        new LatLngAltitude(currentLatLng.latitude, currentLatLng.longitude, groundAltitude),
        targetHeading,
        cameraTilt,
        0.0,
        cameraRange
    );

    googleMap3D.setCamera(newCamera);
    updateProgressPolyline(dist, currentLatLng, index);
  }
}
