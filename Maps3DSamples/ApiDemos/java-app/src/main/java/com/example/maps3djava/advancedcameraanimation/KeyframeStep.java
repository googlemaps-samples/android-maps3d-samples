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

import androidx.annotation.NonNull;
import com.google.android.gms.maps3d.GoogleMap3D;

/**
 * Interface representing an executable keyframe step in a multi-step camera tour.
 */
public interface KeyframeStep {

  /**
   * Returns human-readable title for this step.
   */
  @NonNull
  String getTitle();

  /**
   * Returns description of what occurs during this step.
   */
  @NonNull
  String getDescription();

  /**
   * Executes this step against the provided non-null {@link GoogleMap3D} instance.
   *
   * @param map The map instance to manipulate.
   * @param callback Callback to invoke when this step has finished.
   */
  void execute(@NonNull GoogleMap3D map, @NonNull StepCallback callback);

  /**
   * Cancels this step if it is currently executing, clearing any scheduled callbacks or animations.
   */
  void cancel();
}
