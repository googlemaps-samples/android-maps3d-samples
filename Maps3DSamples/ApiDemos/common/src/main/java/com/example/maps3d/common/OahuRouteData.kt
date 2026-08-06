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

import com.google.android.gms.maps.model.LatLng

/**
 * Static pre-baked route coordinates crossing the mountains of Oahu, Hawaii along the Pali Highway.
 *
 * Serves as a robust local fallback in case the user does not have active network connectivity,
 * has quota limitations on the Routes API, or has not enabled the Routes API product in their
 * Google Cloud Console project.
 */
object OahuRouteData {

    /**
     * A pre-baked list of coordinates representing a scenic mountain drive.
     */
    @JvmStatic
    val FALLBACK_ROUTE: List<LatLng> = listOf(
        LatLng(21.307043, -157.858984), // Start: Honolulu
        LatLng(21.312821, -157.851219),
        LatLng(21.319562, -157.842987),
        LatLng(21.325890, -157.835012),
        LatLng(21.331210, -157.828910),
        LatLng(21.338760, -157.820123),
        LatLng(21.344980, -157.813990),
        LatLng(21.349020, -157.807890), // Nu'uanu Pali Lookout area
        LatLng(21.354910, -157.801210),
        LatLng(21.360890, -157.793450),
        LatLng(21.367120, -157.784980),
        LatLng(21.372900, -157.775120),
        LatLng(21.378120, -157.762100),
        LatLng(21.383910, -157.745120),
        LatLng(21.388910, -157.730100),
        LatLng(21.390177, -157.719454)  // End: Kailua
    )
}
