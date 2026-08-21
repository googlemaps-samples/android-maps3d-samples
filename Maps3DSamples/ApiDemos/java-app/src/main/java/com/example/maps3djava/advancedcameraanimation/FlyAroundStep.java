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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.model.FlyAroundOptions;

/**
 * Animation step that executes an orbital camera fly-around using the native Maps 3D SDK.
 */
public class FlyAroundStep extends AnimationStep {

  private final FlyAroundOptions options;
  private final Runnable onStartAction;

  public FlyAroundStep(
      @NonNull String title,
      @NonNull String description,
      @NonNull FlyAroundOptions options,
      long durationMs,
      @Nullable Runnable onStartAction) {
    super(title, description, durationMs);
    this.options = options;
    this.onStartAction = onStartAction;
  }

  public FlyAroundStep(
      @NonNull String title,
      @NonNull String description,
      @NonNull FlyAroundOptions options,
      long durationMs) {
    this(title, description, options, durationMs, null);
  }

  @NonNull
  public FlyAroundOptions getOptions() {
    return options;
  }

  @Override
  public void execute(@NonNull GoogleMap3D map, @NonNull StepCallback callback) {
    cancel();
    this.activeMap = map;
    if (onStartAction != null) {
      onStartAction.run();
    }
    map.setCameraAnimationEndListener(() -> {
      if (activeMap != null) {
        activeMap.setCameraAnimationEndListener(null);
        activeMap = null;
      }
      callback.onComplete();
    });
    map.flyCameraAround(options);
  }
}
