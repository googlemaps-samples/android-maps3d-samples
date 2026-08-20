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
import com.google.android.gms.maps.model.LatLng;

/**
 * Immutable configuration options for orbital camera animations.
 *
 * Use {@link OrbitOptions.Builder} to construct instances with clear, self-documenting parameters.
 */
public class OrbitOptions {

  private final LatLng center;
  private final double altitude;
  private final double tilt;
  private final double range;
  private final double startHeading;
  private final double endHeading;
  private final long durationMs;

  private OrbitOptions(Builder builder) {
    this.center = builder.center;
    this.altitude = builder.altitude;
    this.tilt = builder.tilt;
    this.range = builder.range;
    this.startHeading = builder.startHeading;
    this.endHeading = builder.endHeading;
    this.durationMs = builder.durationMs;
  }

  @NonNull
  public LatLng getCenter() {
    return center;
  }

  public double getAltitude() {
    return altitude;
  }

  public double getTilt() {
    return tilt;
  }

  public double getRange() {
    return range;
  }

  public double getStartHeading() {
    return startHeading;
  }

  public double getEndHeading() {
    return endHeading;
  }

  public long getDurationMs() {
    return durationMs;
  }

  /**
   * Builder for creating {@link OrbitOptions} with named, self-describing parameters.
   */
  public static class Builder {

    private LatLng center;
    private double altitude = 200.0;
    private double tilt = 65.0;
    private double range = 600.0;
    private double startHeading = 0.0;
    private double endHeading = 360.0;
    private long durationMs = 4000L;

    public Builder setCenter(@NonNull LatLng center) {
      this.center = center;
      return this;
    }

    public Builder setAltitude(double altitude) {
      this.altitude = altitude;
      return this;
    }

    public Builder setTilt(double tilt) {
      this.tilt = tilt;
      return this;
    }

    public Builder setRange(double range) {
      this.range = range;
      return this;
    }

    public Builder setStartHeading(double startHeading) {
      this.startHeading = startHeading;
      return this;
    }

    public Builder setEndHeading(double endHeading) {
      this.endHeading = endHeading;
      return this;
    }

    public Builder setHeadingRange(double startHeading, double endHeading) {
      this.startHeading = startHeading;
      this.endHeading = endHeading;
      return this;
    }

    public Builder setDurationMs(long durationMs) {
      this.durationMs = durationMs;
      return this;
    }

    @NonNull
    public OrbitOptions build() {
      if (center == null) {
        throw new IllegalStateException("Center LatLng must not be null for OrbitOptions");
      }
      return new OrbitOptions(this);
    }
  }
}
