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

package com.example.maps3d.common

import com.google.android.gms.maps3d.model.LatLngAltitude

/**
 * Shared pre-baked route datasets for ground-level path following samples.
 *
 * Lifting coordinates into this shared repository decouples the raw geographic
 * geometry from sample application code across Java Views, Kotlin Views, and Compose.
 */
object PathData {

    /**
     * Urban Route: Downtown San Francisco (Market Street corridor).
     *
     * Features realistic per-waypoint elevation variations (1m to 10m) to demonstrate
     * 3D altitude modes (Absolute, Relative to Ground, Relative to Mesh, Clamp to Ground).
     */
    @JvmField
    val URBAN_PATH: List<LatLngAltitude> = listOf(
        LatLngAltitude(37.79323, -122.39322, 4.2),
        LatLngAltitude(37.79166, -122.39519, 6.7),
        LatLngAltitude(37.79124, -122.39571, 8.1),
        LatLngAltitude(37.79105, -122.39599, 9.5),
        LatLngAltitude(37.78893, -122.39866, 7.3),
        LatLngAltitude(37.78742, -122.40060, 5.0),
        LatLngAltitude(37.78686, -122.40129, 3.4),
        LatLngAltitude(37.78652, -122.40171, 2.1),
        LatLngAltitude(37.78632, -122.40196, 4.6),
        LatLngAltitude(37.78627, -122.40207, 6.2),
        LatLngAltitude(37.78453, -122.40429, 8.9),
        LatLngAltitude(37.78443, -122.40434, 10.0),
        LatLngAltitude(37.78155, -122.40802, 7.8),
        LatLngAltitude(37.78005, -122.40990, 5.4),
        LatLngAltitude(37.77856, -122.41180, 3.1),
        LatLngAltitude(37.77746, -122.41318, 1.8),
        LatLngAltitude(37.77624, -122.41474, 4.0),
        LatLngAltitude(37.77744, -122.41623, 6.5),
        LatLngAltitude(37.77749, -122.41636, 8.7),
        LatLngAltitude(37.77761, -122.41654, 9.8),
        LatLngAltitude(37.77769, -122.41677, 7.2),
        LatLngAltitude(37.77729, -122.41981, 4.9),
        LatLngAltitude(37.77523, -122.41938, 2.6),
        LatLngAltitude(37.77510, -122.41934, 1.2),
        LatLngAltitude(37.77442, -122.42022, 3.5),
        LatLngAltitude(37.77441, -122.42033, 5.8),
        LatLngAltitude(37.77348, -122.42157, 8.4),
        LatLngAltitude(37.77244, -122.42289, 10.0)
    )

    /**
     * Rural Route: Coastal highway and mountain switchbacks near Pescadero, CA.
     */
    @JvmField
    val RURAL_PATH: List<LatLngAltitude> = listOf(
        LatLngAltitude(37.254529, -122.380897, 0.0),
        LatLngAltitude(37.255065, -122.381627, 0.0),
        LatLngAltitude(37.257540, -122.383720, 0.0),
        LatLngAltitude(37.261200, -122.383950, 0.0),
        LatLngAltitude(37.264780, -122.388210, 0.0),
        LatLngAltitude(37.268520, -122.392450, 0.0),
        LatLngAltitude(37.272110, -122.397640, 0.0),
        LatLngAltitude(37.276430, -122.401120, 0.0),
        LatLngAltitude(37.280850, -122.403560, 0.0),
        LatLngAltitude(37.286018, -122.405072, 0.0),
        LatLngAltitude(37.291040, -122.404210, 0.0),
        LatLngAltitude(37.295800, -122.401980, 0.0),
        LatLngAltitude(37.300120, -122.399540, 0.0),
        LatLngAltitude(37.304550, -122.397210, 0.0),
        LatLngAltitude(37.309200, -122.395100, 0.0),
        LatLngAltitude(37.313450, -122.392840, 0.0),
        LatLngAltitude(37.317200, -122.390510, 0.0),
        LatLngAltitude(37.320850, -122.388740, 0.0),
        LatLngAltitude(37.323540, -122.387600, 0.0),
        LatLngAltitude(37.325269, -122.386728, 0.0)
    )
}
