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
import com.google.android.gms.maps3d.model.FlyAroundOptions;
import com.google.android.gms.maps3d.model.FlyToOptions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Orchestrates multi-step keyframe camera animations on a {@link GoogleMap3D}.
 *
 * Supports sequential playback, step lifecycle callbacks, pause, resume, and cancellation.
 */
public class Map3DAnimator {

  /**
   * Listener interface for monitoring animator lifecycle events.
   */
  public interface Listener {

    /**
     * Called when a keyframe step begins execution.
     */
    default void onStepStarted(int index, @NonNull KeyframeStep step) {}

    /**
     * Called when a keyframe step successfully finishes.
     */
    default void onStepCompleted(int index, @NonNull KeyframeStep step) {}

    /**
     * Called when all steps in the animation sequence have completed.
     */
    default void onAnimationFinished() {}

    /**
     * Called when the animation sequence is stopped or cancelled.
     */
    default void onAnimationCancelled() {}
  }

  private final List<KeyframeStep> steps;
  private final Handler mainHandler = new Handler(Looper.getMainLooper());
  private GoogleMap3D activeMap;
  private Listener listener;
  private int currentStepIndex = 0;
  private boolean isPlaying = false;
  private KeyframeStep currentlyExecutingStep;

  public Map3DAnimator(@NonNull List<KeyframeStep> steps) {
    this.steps = Collections.unmodifiableList(new ArrayList<>(steps));
  }

  @NonNull
  public List<KeyframeStep> getSteps() {
    return steps;
  }

  public int getCurrentStepIndex() {
    return currentStepIndex;
  }

  public boolean isPlaying() {
    return isPlaying;
  }

  /**
   * Starts or restarts the keyframe animation sequence from the beginning.
   */
  public void start(@NonNull GoogleMap3D map, @Nullable Listener listener) {
    stop();
    this.activeMap = map;
    this.listener = listener;
    this.isPlaying = true;
    this.currentStepIndex = 0;
    mainHandler.post(this::executeNextStep);
  }

  /**
   * Pauses the currently running animation step.
   */
  public void pause() {
    if (!isPlaying) {
      return;
    }
    isPlaying = false;
    if (currentlyExecutingStep != null) {
      currentlyExecutingStep.cancel();
      currentlyExecutingStep = null;
    }
    if (activeMap != null) {
      activeMap.setCameraAnimationEndListener(null);
      activeMap.stopCameraAnimation();
    }
  }

  /**
   * Resumes playback from the current step index.
   */
  public void resume() {
    if (isPlaying || activeMap == null || currentStepIndex >= steps.size()) {
      return;
    }
    isPlaying = true;
    mainHandler.post(this::executeNextStep);
  }

  /**
   * Stops the animation, cancels executing steps, and resets step index to 0.
   */
  public void stop() {
    isPlaying = false;
    if (currentlyExecutingStep != null) {
      currentlyExecutingStep.cancel();
      currentlyExecutingStep = null;
    }
    if (activeMap != null) {
      activeMap.setCameraAnimationEndListener(null);
      activeMap.stopCameraAnimation();
      activeMap = null;
    }
    currentStepIndex = 0;
  }

  private void executeNextStep() {
    if (!isPlaying || activeMap == null) {
      return;
    }

    if (currentStepIndex >= steps.size()) {
      isPlaying = false;
      if (listener != null) {
        listener.onAnimationFinished();
      }
      return;
    }

    KeyframeStep step = steps.get(currentStepIndex);
    currentlyExecutingStep = step;

    if (listener != null) {
      listener.onStepStarted(currentStepIndex, step);
    }

    step.execute(
        activeMap,
        () -> mainHandler.post(() -> {
          if (!isPlaying) {
            return;
          }
          currentlyExecutingStep = null;
          if (listener != null) {
            listener.onStepCompleted(currentStepIndex, step);
          }
          currentStepIndex++;
          executeNextStep();
        }));
  }

  /**
   * Builder for constructing a {@link Map3DAnimator} with a fluent API.
   */
  public static class Builder {

    private final List<KeyframeStep> steps = new ArrayList<>();

    public Builder addStep(@NonNull KeyframeStep step) {
      steps.add(step);
      return this;
    }

    public Builder flyTo(
        @NonNull String title,
        @NonNull String description,
        @NonNull Camera targetCamera,
        long durationMs) {
      return addStep(new FlyToStep(title, description, targetCamera, durationMs));
    }

    public Builder flyTo(
        @NonNull String title,
        @NonNull String description,
        @NonNull FlyToOptions options,
        long durationMs,
        @Nullable Runnable onStartAction) {
      return addStep(new FlyToStep(title, description, options, durationMs, onStartAction));
    }

    public Builder flyTo(
        @NonNull String title,
        @NonNull String description,
        @NonNull FlyToOptions options,
        long durationMs) {
      return addStep(new FlyToStep(title, description, options, durationMs));
    }

    public Builder dwell(
        @NonNull String title,
        @NonNull String description,
        long durationMs) {
      return addStep(new DwellStep(title, description, durationMs));
    }

    public Builder dwell(long durationMs) {
      return addStep(new DwellStep(durationMs));
    }

    public Builder flyAround(
        @NonNull String title,
        @NonNull String description,
        @NonNull FlyAroundOptions options,
        long durationMs,
        @Nullable Runnable onStartAction) {
      return addStep(new FlyAroundStep(title, description, options, durationMs, onStartAction));
    }

    public Builder flyAround(
        @NonNull String title,
        @NonNull String description,
        @NonNull FlyAroundOptions options,
        long durationMs) {
      return addStep(new FlyAroundStep(title, description, options, durationMs));
    }

    public Builder orbit(
        @NonNull String title,
        @NonNull String description,
        @NonNull OrbitOptions options) {
      return addStep(new OrbitStep(title, description, options));
    }

    public Builder orbit(
        @NonNull String title,
        @NonNull String description,
        @NonNull OrbitOptions options,
        @Nullable OrbitStep.OnOrbitFrameListener frameListener) {
      return addStep(new OrbitStep(title, description, options, frameListener));
    }

    @NonNull
    public Map3DAnimator build() {
      return new Map3DAnimator(steps);
    }
  }
}
