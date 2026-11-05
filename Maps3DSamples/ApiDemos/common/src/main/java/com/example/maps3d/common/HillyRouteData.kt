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
import org.json.JSONArray
import org.json.JSONObject

/**
 * Selectable 3D route presets for the Hilly & Undulating (Roller-Coaster) Road Early Alerts sample.
 *
 * @property displayName Short human-readable label for UI chips.
 * @property subtitle Short description of the route terrain character.
 */
enum class HillyRoutePreset(
    val displayName: String,
    val subtitle: String,
    val defaultBaseSpeedMps: Double = 24.0
) {
    /**
     * Famous undulating roller-coaster road with rapid crests and dips.
     */
    ROLLER_COASTER(
        displayName = "Roller-Coaster Rd",
        subtitle = "Rapid undulating crests & dips"
    ),

    /**
     * Sustained steep climb to a sharp summit crest followed by a steep descent.
     */
    MOUNTAIN_PASS(
        displayName = "Mountain Pass",
        subtitle = "Sustained steep climb & blind summit"
    ),

    /**
     * Gentle coastal route preset.
     */
    COASTAL_DRIVE(
        displayName = "Coastal Drive (Flat)",
        subtitle = "Gentle < 2% grade control route"
    )
}

/**
 * Lazily loads curated 3D route coordinates (`LatLngAltitude` with explicit `ABSOLUTE` elevations
 * in meters) from `hilly_routes.json` on the classpath for the Hilly & Undulating Road Alerts sample.
 */
object HillyRouteData {

    private const val ROUTES_RESOURCE_NAME = "hilly_routes.json"
    private const val KEY_ROLLER_COASTER = "roller_coaster"
    private const val KEY_MOUNTAIN_PASS = "mountain_pass"
    private const val KEY_COASTAL_DRIVE = "coastal_drive"

    private val routesByKey: Map<String, List<LatLngAltitude>> by lazy {
        loadAllRoutes()
    }

    /**
     * Real-world GPS/DEM route trace for the undulating Roller-Coaster road preset.
     */
    val SAN_FRANCISCO_HYDE_ST_ROLLER_COASTER: List<LatLngAltitude>
        get() = routesByKey[KEY_ROLLER_COASTER].orEmpty()

    /**
     * Alias for the default Roller-Coaster route ([SAN_FRANCISCO_HYDE_ST_ROLLER_COASTER]).
     */
    val ROLLER_COASTER_PATH: List<LatLngAltitude>
        get() = SAN_FRANCISCO_HYDE_ST_ROLLER_COASTER

    /**
     * Real-world GPS/DEM route trace for the Mountain Pass preset.
     */
    val MOUNTAIN_PASS_PATH: List<LatLngAltitude>
        get() = routesByKey[KEY_MOUNTAIN_PASS].orEmpty()

    /**
     * Real-world GPS/DEM route trace for the Coastal Drive preset.
     */
    val COASTAL_DRIVE_PATH: List<LatLngAltitude>
        get() = routesByKey[KEY_COASTAL_DRIVE].orEmpty()

    /**
     * Returns the 3D coordinate list for the given [preset].
     */
    @JvmStatic
    fun getRoute(preset: HillyRoutePreset): List<LatLngAltitude> = when (preset) {
        HillyRoutePreset.ROLLER_COASTER -> ROLLER_COASTER_PATH
        HillyRoutePreset.MOUNTAIN_PASS -> MOUNTAIN_PASS_PATH
        HillyRoutePreset.COASTAL_DRIVE -> COASTAL_DRIVE_PATH
    }

    private fun loadAllRoutes(): Map<String, List<LatLngAltitude>> {
        val stream = HillyRouteData::class.java.classLoader
            ?.getResourceAsStream(ROUTES_RESOURCE_NAME)
            ?: HillyRouteData::class.java.getResourceAsStream("/$ROUTES_RESOURCE_NAME")
            ?: error("Missing classpath resource: $ROUTES_RESOURCE_NAME")

        val jsonText = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        val root = JSONObject(jsonText)
        return mapOf(
            KEY_ROLLER_COASTER to parseCoordinateArray(root.getJSONArray(KEY_ROLLER_COASTER)),
            KEY_MOUNTAIN_PASS to parseCoordinateArray(root.getJSONArray(KEY_MOUNTAIN_PASS)),
            KEY_COASTAL_DRIVE to parseCoordinateArray(root.getJSONArray(KEY_COASTAL_DRIVE))
        )
    }

    private fun parseCoordinateArray(array: JSONArray): List<LatLngAltitude> {
        val count = array.length()
        val points = ArrayList<LatLngAltitude>(count)
        for (i in 0 until count) {
            val tuple = array.getJSONArray(i)
            points.add(
                LatLngAltitude(
                    tuple.getDouble(0),
                    tuple.getDouble(1),
                    tuple.getDouble(2)
                )
            )
        }
        return points
    }
}
