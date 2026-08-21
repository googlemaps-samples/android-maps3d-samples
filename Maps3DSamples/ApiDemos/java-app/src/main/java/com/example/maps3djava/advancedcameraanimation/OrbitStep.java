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

package com.example.maps3djava.advancedcameraanimation;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.LatLngAltitude;

/**
 * Keyframe step that runs a smooth frame-by-frame orbital spin around a landmark.
 */
public class OrbitStep extends AnimationStep {

  /**
   * Callback invoked on every frame of the orbital animation.
   */
  @FunctionalInterface
  public interface OnOrbitFrameListener {
    void onFrame(double fraction, double currentHeading);
  }

  private final OrbitOptions options;
  private final OnOrbitFrameListener frameListener;
  private final Handler handler = new Handler(Looper.getMainLooper());
  private Runnable frameRunnable;

  public OrbitStep(
      @NonNull String title,
      @NonNull String description,
      @NonNull OrbitOptions options,
      @Nullable OnOrbitFrameListener frameListener) {
    super(title, description, options.getDurationMs());
    this.options = options;
    this.frameListener = frameListener;
  }

  public OrbitStep(
      @NonNull String title,
      @NonNull String description,
      @NonNull OrbitOptions options) {
    this(title, description, options, null);
  }

  @NonNull
  public OrbitOptions getOptions() {
    return options;
  }

  @Override
  public void execute(@NonNull GoogleMap3D map, @NonNull StepCallback callback) {
    cancel();
    this.activeMap = map;

    final long frameMs = 16L;
    final long totalFrames = Math.max(1, options.getDurationMs() / frameMs);

    frameRunnable = new Runnable() {
      private long currentFrame = 0;

      @Override
      public void run() {
        if (activeMap == null) {
          return;
        }

        double t = (double) currentFrame / totalFrames;
        double heading = interpolateAngle(options.getStartHeading(), options.getEndHeading(), t);

        if (frameListener != null) {
          frameListener.onFrame(t, heading);
        }

        Camera orbitCam = new Camera(
            new LatLngAltitude(
                options.getCenter().latitude,
                options.getCenter().longitude,
                options.getAltitude()),
            normalizeHeading(heading),
            options.getTilt(),
            0.0,
            options.getRange()
        );
        activeMap.setCamera(orbitCam);

        currentFrame++;
        if (currentFrame <= totalFrames) {
          handler.postDelayed(this, frameMs);
        } else {
          activeMap = null;
          frameRunnable = null;
          callback.onComplete();
        }
      }
    };

    handler.post(frameRunnable);
  }

  @Override
  public void cancel() {
    super.cancel();
    if (frameRunnable != null) {
      handler.removeCallbacks(frameRunnable);
      frameRunnable = null;
    }
  }

  private static double normalizeHeading(double headingDeg) {
    double normalized = headingDeg % 360.0;
    return normalized < 0.0 ? normalized + 360.0 : normalized;
  }

  private static double interpolateAngle(double start, double end, double fraction) {
    return start + (end - start) * fraction;
  }
}
