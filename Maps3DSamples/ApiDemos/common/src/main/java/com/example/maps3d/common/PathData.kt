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
     * Snapped to real-time road curvature from Google Routes API (112 waypoints).
     */
    @JvmField
    val RURAL_PATH: List<LatLngAltitude> = listOf(
        LatLngAltitude(37.254420, -122.381400, 10.1),
        LatLngAltitude(37.255070, -122.381630, 9.9),
        LatLngAltitude(37.255070, -122.381920, 9.7),
        LatLngAltitude(37.255150, -122.382530, 9.8),
        LatLngAltitude(37.255240, -122.383230, 10.9),
        LatLngAltitude(37.253400, -122.383160, 10.4),
        LatLngAltitude(37.252680, -122.383100, 10.5),
        LatLngAltitude(37.252000, -122.382970, 10.2),
        LatLngAltitude(37.251880, -122.382970, 10.1),
        LatLngAltitude(37.251660, -122.382920, 9.8),
        LatLngAltitude(37.251410, -122.384860, 8.5),
        LatLngAltitude(37.250140, -122.395120, 4.4),
        LatLngAltitude(37.249730, -122.398740, 4.8),
        LatLngAltitude(37.249720, -122.399170, 4.9),
        LatLngAltitude(37.249760, -122.399590, 5.0),
        LatLngAltitude(37.249860, -122.400070, 5.0),
        LatLngAltitude(37.250060, -122.400580, 4.9),
        LatLngAltitude(37.250230, -122.400910, 5.0),
        LatLngAltitude(37.250530, -122.401310, 4.9),
        LatLngAltitude(37.250700, -122.401480, 4.7),
        LatLngAltitude(37.251970, -122.402620, 4.9),
        LatLngAltitude(37.252200, -122.402910, 4.8),
        LatLngAltitude(37.252430, -122.403320, 4.8),
        LatLngAltitude(37.252580, -122.403730, 4.9),
        LatLngAltitude(37.253270, -122.405810, 4.7),
        LatLngAltitude(37.253440, -122.406190, 4.8),
        LatLngAltitude(37.253720, -122.406620, 4.5),
        LatLngAltitude(37.253990, -122.406920, 4.3),
        LatLngAltitude(37.254320, -122.407180, 4.3),
        LatLngAltitude(37.255380, -122.407760, 6.7),
        LatLngAltitude(37.258040, -122.409140, 13.0),
        LatLngAltitude(37.258370, -122.409360, 13.8),
        LatLngAltitude(37.258730, -122.409720, 14.8),
        LatLngAltitude(37.259040, -122.410150, 15.8),
        LatLngAltitude(37.259250, -122.410530, 16.6),
        LatLngAltitude(37.259420, -122.410980, 17.3),
        LatLngAltitude(37.259540, -122.411490, 17.4),
        LatLngAltitude(37.259590, -122.411820, 17.2),
        LatLngAltitude(37.259700, -122.413170, 15.6),
        LatLngAltitude(37.261630, -122.412750, 15.0),
        LatLngAltitude(37.263680, -122.412250, 14.3),
        LatLngAltitude(37.264060, -122.412160, 14.2),
        LatLngAltitude(37.264190, -122.412210, 14.2),
        LatLngAltitude(37.264310, -122.412160, 14.0),
        LatLngAltitude(37.265160, -122.411950, 13.1),
        LatLngAltitude(37.265870, -122.411680, 9.4),
        LatLngAltitude(37.266480, -122.411390, 9.5),
        LatLngAltitude(37.267140, -122.411000, 9.6),
        LatLngAltitude(37.268110, -122.410400, 7.6),
        LatLngAltitude(37.268560, -122.410170, 6.6),
        LatLngAltitude(37.269450, -122.409840, 5.1),
        LatLngAltitude(37.270150, -122.409630, 4.5),
        LatLngAltitude(37.275560, -122.408270, 19.7),
        LatLngAltitude(37.276080, -122.408190, 23.3),
        LatLngAltitude(37.276680, -122.408140, 27.5),
        LatLngAltitude(37.277630, -122.408170, 34.0),
        LatLngAltitude(37.279400, -122.408230, 46.2),
        LatLngAltitude(37.280030, -122.408190, 50.1),
        LatLngAltitude(37.280740, -122.408060, 52.9),
        LatLngAltitude(37.281360, -122.407880, 53.9),
        LatLngAltitude(37.282140, -122.407570, 53.4),
        LatLngAltitude(37.282800, -122.407210, 51.1),
        LatLngAltitude(37.283590, -122.406680, 46.3),
        LatLngAltitude(37.285420, -122.405390, 40.8),
        LatLngAltitude(37.285900, -122.405120, 40.4),
        LatLngAltitude(37.286410, -122.404900, 40.1),
        LatLngAltitude(37.286990, -122.404730, 39.6),
        LatLngAltitude(37.287600, -122.404640, 39.3),
        LatLngAltitude(37.288070, -122.404640, 38.9),
        LatLngAltitude(37.288580, -122.404680, 38.3),
        LatLngAltitude(37.289170, -122.404810, 37.7),
        LatLngAltitude(37.290740, -122.405390, 35.8),
        LatLngAltitude(37.291860, -122.405830, 37.8),
        LatLngAltitude(37.293070, -122.406250, 44.3),
        LatLngAltitude(37.293360, -122.406320, 44.9),
        LatLngAltitude(37.293810, -122.406370, 45.2),
        LatLngAltitude(37.294420, -122.406370, 44.4),
        LatLngAltitude(37.295040, -122.406270, 41.7),
        LatLngAltitude(37.295560, -122.406110, 38.2),
        LatLngAltitude(37.296070, -122.405880, 34.0),
        LatLngAltitude(37.297320, -122.405160, 23.2),
        LatLngAltitude(37.297900, -122.404910, 18.3),
        LatLngAltitude(37.298420, -122.404760, 14.1),
        LatLngAltitude(37.298830, -122.404690, 11.0),
        LatLngAltitude(37.299490, -122.404660, 8.0),
        LatLngAltitude(37.305570, -122.404640, 48.9),
        LatLngAltitude(37.306650, -122.404590, 52.4),
        LatLngAltitude(37.307900, -122.404460, 50.9),
        LatLngAltitude(37.316640, -122.403260, 49.3),
        LatLngAltitude(37.317660, -122.403050, 41.3),
        LatLngAltitude(37.318760, -122.402760, 32.5),
        LatLngAltitude(37.319880, -122.402380, 23.4),
        LatLngAltitude(37.320920, -122.401940, 3.2),
        LatLngAltitude(37.321750, -122.401540, 10.6),
        LatLngAltitude(37.322640, -122.401060, 9.7),
        LatLngAltitude(37.323740, -122.400420, 14.8),
        LatLngAltitude(37.324560, -122.399860, 21.2),
        LatLngAltitude(37.324480, -122.399680, 20.3),
        LatLngAltitude(37.324700, -122.392770, 20.4),
        LatLngAltitude(37.324730, -122.392370, 20.6),
        LatLngAltitude(37.324880, -122.391740, 20.2),
        LatLngAltitude(37.325000, -122.391400, 19.9),
        LatLngAltitude(37.325330, -122.390750, 19.0),
        LatLngAltitude(37.326050, -122.389410, 18.9),
        LatLngAltitude(37.326210, -122.389040, 19.0),
        LatLngAltitude(37.326360, -122.388570, 19.2),
        LatLngAltitude(37.326860, -122.386510, 19.7),
        LatLngAltitude(37.326270, -122.386430, 14.6),
        LatLngAltitude(37.325810, -122.386420, 5.6),
        LatLngAltitude(37.325670, -122.386450, 12.4),
        LatLngAltitude(37.325460, -122.386550, 11.3),
        LatLngAltitude(37.325280, -122.386740, 10.6)
    )

    /**
     * Mountain hiking trail in Griffith Park ascending to Mount Hollywood Summit.
     * Snapped to real-time trail curvature from Google Routes API with monotonic terrain elevations (94 waypoints).
     * Continuous uphill climb starting at the base trailhead (343.8m) and finishing right on the summit peak (457.0m).
     */
    @JvmField
    val MOUNTAIN_PATH: List<LatLngAltitude> = listOf(
        LatLngAltitude(34.120800, -118.300500, 343.8),
        LatLngAltitude(34.120830, -118.300510, 343.8),
        LatLngAltitude(34.120880, -118.300290, 343.9),
        LatLngAltitude(34.121000, -118.300420, 344.2),
        LatLngAltitude(34.121170, -118.300510, 344.7),
        LatLngAltitude(34.121390, -118.300540, 345.9),
        LatLngAltitude(34.121630, -118.300550, 348.6),
        LatLngAltitude(34.121800, -118.300610, 350.0),
        LatLngAltitude(34.122030, -118.300800, 353.7),
        LatLngAltitude(34.122200, -118.300860, 356.8),
        LatLngAltitude(34.122630, -118.300920, 362.0),
        LatLngAltitude(34.122920, -118.301000, 363.0),
        LatLngAltitude(34.123010, -118.301100, 363.1),
        LatLngAltitude(34.123080, -118.301100, 363.2),
        LatLngAltitude(34.123430, -118.300850, 363.3),
        LatLngAltitude(34.123530, -118.300780, 363.4),
        LatLngAltitude(34.123610, -118.300780, 363.5),
        LatLngAltitude(34.123870, -118.300740, 363.6),
        LatLngAltitude(34.123940, -118.300770, 363.7),
        LatLngAltitude(34.123980, -118.300800, 363.8),
        LatLngAltitude(34.124120, -118.301120, 363.9),
        LatLngAltitude(34.124200, -118.301170, 364.0),
        LatLngAltitude(34.124300, -118.301200, 364.1),
        LatLngAltitude(34.124330, -118.301300, 364.2),
        LatLngAltitude(34.124350, -118.301700, 364.3),
        LatLngAltitude(34.124430, -118.301840, 365.0),
        LatLngAltitude(34.124560, -118.301950, 365.7),
        LatLngAltitude(34.124610, -118.301980, 366.1),
        LatLngAltitude(34.124700, -118.301960, 366.6),
        LatLngAltitude(34.125090, -118.301730, 369.2),
        LatLngAltitude(34.125160, -118.301730, 369.7),
        LatLngAltitude(34.125400, -118.301850, 371.0),
        LatLngAltitude(34.125520, -118.302010, 372.5),
        LatLngAltitude(34.125690, -118.302220, 373.8),
        LatLngAltitude(34.125860, -118.302370, 374.7),
        LatLngAltitude(34.126030, -118.302410, 375.4),
        LatLngAltitude(34.126240, -118.302620, 376.8),
        LatLngAltitude(34.126280, -118.302730, 377.9),
        LatLngAltitude(34.126330, -118.302770, 377.9),
        LatLngAltitude(34.126460, -118.302820, 377.9),
        LatLngAltitude(34.126490, -118.302870, 377.9),
        LatLngAltitude(34.126520, -118.302980, 378.4),
        LatLngAltitude(34.126590, -118.303120, 379.3),
        LatLngAltitude(34.126580, -118.303220, 379.3),
        LatLngAltitude(34.126460, -118.303760, 379.6),
        LatLngAltitude(34.126350, -118.303900, 379.6),
        LatLngAltitude(34.126310, -118.304000, 379.6),
        LatLngAltitude(34.126350, -118.304210, 380.0),
        LatLngAltitude(34.126300, -118.304370, 380.6),
        LatLngAltitude(34.126210, -118.304530, 380.9),
        LatLngAltitude(34.126190, -118.304650, 381.0),
        LatLngAltitude(34.126250, -118.304840, 381.3),
        LatLngAltitude(34.126240, -118.304960, 381.5),
        LatLngAltitude(34.126130, -118.305430, 382.7),
        LatLngAltitude(34.126160, -118.305500, 383.0),
        LatLngAltitude(34.126220, -118.305530, 383.5),
        LatLngAltitude(34.126270, -118.305510, 383.8),
        LatLngAltitude(34.126440, -118.304870, 392.3),
        LatLngAltitude(34.126550, -118.304640, 396.3),
        LatLngAltitude(34.126570, -118.304420, 398.3),
        LatLngAltitude(34.126620, -118.304320, 398.9),
        LatLngAltitude(34.126680, -118.304160, 399.7),
        LatLngAltitude(34.127060, -118.303460, 404.6),
        LatLngAltitude(34.127100, -118.303370, 405.2),
        LatLngAltitude(34.127140, -118.303020, 408.1),
        LatLngAltitude(34.127140, -118.302790, 409.9),
        LatLngAltitude(34.127270, -118.302450, 413.4),
        LatLngAltitude(34.127260, -118.302260, 415.3),
        LatLngAltitude(34.127330, -118.302000, 417.7),
        LatLngAltitude(34.127300, -118.301880, 419.2),
        LatLngAltitude(34.127240, -118.301740, 420.9),
        LatLngAltitude(34.127210, -118.301510, 424.0),
        LatLngAltitude(34.127140, -118.301450, 425.2),
        LatLngAltitude(34.126930, -118.301410, 426.5),
        LatLngAltitude(34.126840, -118.301350, 427.5),
        LatLngAltitude(34.126720, -118.301180, 429.9),
        LatLngAltitude(34.126700, -118.300700, 436.0),
        LatLngAltitude(34.126940, -118.300540, 436.4),
        LatLngAltitude(34.127010, -118.300430, 436.8),
        LatLngAltitude(34.127080, -118.300230, 437.2),
        LatLngAltitude(34.127100, -118.299990, 437.5),
        LatLngAltitude(34.127140, -118.299920, 437.9),
        LatLngAltitude(34.127240, -118.299920, 438.3),
        LatLngAltitude(34.128080, -118.300240, 438.7),
        LatLngAltitude(34.128190, -118.300260, 440.2),
        LatLngAltitude(34.128270, -118.300220, 440.8),
        LatLngAltitude(34.128400, -118.300070, 442.0),
        LatLngAltitude(34.128730, -118.299970, 444.8),
        LatLngAltitude(34.128930, -118.299860, 448.0),
        LatLngAltitude(34.129010, -118.299730, 449.4),
        LatLngAltitude(34.129060, -118.299530, 451.5),
        LatLngAltitude(34.129090, -118.299420, 452.8),
        LatLngAltitude(34.129210, -118.299270, 455.0),
        LatLngAltitude(34.129340, -118.298900, 457.0)
    )
}
