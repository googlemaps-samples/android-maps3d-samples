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

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import com.google.android.gms.maps3d.GoogleMap3D;

/**
 * Keyframe step that pauses for a specified duration without animating the camera.
 */
public class DwellStep implements KeyframeStep {

  private final String title;
  private final String description;
  private final long durationMs;
  private final Handler handler = new Handler(Looper.getMainLooper());
  private Runnable pendingRunnable;

  public DwellStep(@NonNull String title, @NonNull String description, long durationMs) {
    this.title = title;
    this.description = description;
    this.durationMs = durationMs;
  }

  public DwellStep(long durationMs) {
    this("Dwell Pause", "Observing current location", durationMs);
  }

  @NonNull
  @Override
  public String getTitle() {
    return title;
  }

  @NonNull
  @Override
  public String getDescription() {
    return description;
  }

  public long getDurationMs() {
    return durationMs;
  }

  @Override
  public void execute(@NonNull GoogleMap3D map, @NonNull StepCallback callback) {
    cancel();
    pendingRunnable = callback::onComplete;
    handler.postDelayed(pendingRunnable, durationMs);
  }

  @Override
  public void cancel() {
    if (pendingRunnable != null) {
      handler.removeCallbacks(pendingRunnable);
      pendingRunnable = null;
    }
  }
}
