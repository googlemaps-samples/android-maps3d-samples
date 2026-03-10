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
