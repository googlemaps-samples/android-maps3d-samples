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

package com.example.maps3djava.markers.data;

public class Monster {
    public final String id;
    public final String label;
    public final double latitude;
    public final double longitude;
    public final double altitude;
    public final double heading;
    public final double tilt;
    public final double range;
    public final double markerLatitude;
    public final double markerLongitude;
    public final double markerAltitude;
    public final String drawable;
    public final int altitudeMode;

    public Monster(
            String id,
            String label,
            double latitude,
            double longitude,
            double altitude,
            double heading,
            double tilt,
            double range,
            double markerLatitude,
            double markerLongitude,
            double markerAltitude,
            String drawable,
            int altitudeMode) {
        this.id = id;
        this.label = label;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.heading = heading;
        this.tilt = tilt;
        this.range = range;
        this.markerLatitude = markerLatitude;
        this.markerLongitude = markerLongitude;
        this.markerAltitude = markerAltitude;
        this.drawable = drawable;
        this.altitudeMode = altitudeMode;
    }
}
