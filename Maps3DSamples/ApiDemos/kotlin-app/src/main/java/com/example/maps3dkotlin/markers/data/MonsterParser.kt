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

package com.example.maps3dkotlin.markers.data

import com.google.android.gms.maps3d.model.AltitudeMode
import org.json.JSONArray

object MonsterParser {
    fun parse(jsonString: String): List<Monster> {
        val monsters = mutableListOf<Monster>()
        val jsonArray = JSONArray(jsonString)
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            
            val altitudeModeStr = obj.optString("altitudeMode", "ABSOLUTE")
            val parsedAltitudeMode = when (altitudeModeStr) {
                "RELATIVE_TO_GROUND" -> AltitudeMode.RELATIVE_TO_GROUND
                "CLAMP_TO_GROUND" -> AltitudeMode.CLAMP_TO_GROUND
                "RELATIVE_TO_MESH" -> AltitudeMode.RELATIVE_TO_MESH
                "ABSOLUTE" -> AltitudeMode.ABSOLUTE
                else -> AltitudeMode.ABSOLUTE
            }
            
            monsters.add(
                Monster(
                    id = obj.getString("id"),
                    label = obj.getString("label"),
                    latitude = obj.getDouble("latitude"),
                    longitude = obj.getDouble("longitude"),
                    altitude = obj.getDouble("altitude"),
                    heading = obj.getDouble("heading"),
                    tilt = obj.getDouble("tilt"),
                    range = obj.getDouble("range"),
                    markerLatitude = obj.getDouble("markerLatitude"),
                    markerLongitude = obj.getDouble("markerLongitude"),
                    markerAltitude = obj.getDouble("markerAltitude"),
                    drawable = obj.getString("drawable"),
                    altitudeMode = parsedAltitudeMode
                )
            )
        }
        return monsters
    }
}
