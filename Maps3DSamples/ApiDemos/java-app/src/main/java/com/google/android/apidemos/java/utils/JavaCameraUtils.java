/*
 * Copyright 2023 Google LLC
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

package com.google.android.apidemos.java.utils;

import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.LatLngAltitude;

import java.util.Locale;

public final class JavaCameraUtils {
    private JavaCameraUtils() {}

    public static Camera toValidCamera(Camera camera) {
        if (camera == null) {
            // Return a sensible default if camera is null
            return new Camera.Builder().center(new LatLngAltitude(0, 0, 0)).range(1000.0).build();
        }

        Camera.Builder builder = new Camera.Builder();

        // Center (LatLngAltitude)
        if (camera.getCenter() != null) {
            builder.center(camera.getCenter());
        } else {
            builder.center(new LatLngAltitude(0, 0, 0)); // Default if center is null
        }

        // Heading
        if (camera.getHeading() != null) {
            builder.heading(camera.getHeading());
        } else {
            builder.heading(0.0); // Default if heading is null
        }

        // Tilt
        if (camera.getTilt() != null) {
            builder.tilt(camera.getTilt());
        } else {
            builder.tilt(0.0); // Default if tilt is null
        }

        // Range
        if (camera.getRange() != null) {
            builder.range(camera.getRange());
        } else {
            builder.range(1000.0); // Default if range is null
        }

        // Roll
        if (camera.getRoll() != null) {
            builder.roll(camera.getRoll());
        } else {
            builder.roll(0.0); // Default if roll is null
        }

        // Add other properties if they exist on Camera object and can be null
        // For example, if future SDK versions add more optional camera parameters.

        return builder.build();
    }

    public static String toCameraString(Camera camera) {
        if (camera == null) return "Camera is null";
        LatLngAltitude center = camera.getCenter() != null ? camera.getCenter() : new LatLngAltitude(0,0,0);
        Double heading = camera.getHeading() != null ? camera.getHeading() : 0.0;
        Double tilt = camera.getTilt() != null ? camera.getTilt() : 0.0;
        Double range = camera.getRange() != null ? camera.getRange() : 1000.0;
        Double roll = camera.getRoll() != null ? camera.getRoll() : 0.0;

        return String.format(Locale.US,
            "Camera[center=(lat=%.6f, lng=%.6f, alt=%.2f), heading=%.2f, tilt=%.2f, range=%.2f, roll=%.2f]",
            center.getLatitude(),
            center.getLongitude(),
            center.getAltitude(),
            heading,
            tilt,
            range,
            roll);
    }

    public static Camera.Builder copy(Camera camera) {
         if (camera == null) return new Camera.Builder(); // Return empty builder or builder with defaults

         Camera.Builder builder = new Camera.Builder();
         if (camera.getCenter() != null) builder.center(camera.getCenter());
         if (camera.getHeading() != null) builder.heading(camera.getHeading());
         if (camera.getTilt() != null) builder.tilt(camera.getTilt());
         if (camera.getRange() != null) builder.range(camera.getRange());
         if (camera.getRoll() != null) builder.roll(camera.getRoll());
         // Add other properties from camera if they exist
         return builder;
    }
}
