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

import com.google.android.gms.maps3d.model.AltitudeMode;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.List;

public class MonsterParser {
    public static List<Monster> parse(String jsonString) throws JSONException {
        List<Monster> monsters = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(jsonString);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            
            String altitudeModeStr = obj.optString("altitudeMode", "ABSOLUTE");
            int parsedAltitudeMode;
            switch (altitudeModeStr) {
                case "RELATIVE_TO_GROUND":
                    parsedAltitudeMode = AltitudeMode.RELATIVE_TO_GROUND;
                    break;
                case "CLAMP_TO_GROUND":
                    parsedAltitudeMode = AltitudeMode.CLAMP_TO_GROUND;
                    break;
                case "RELATIVE_TO_MESH":
                    parsedAltitudeMode = AltitudeMode.RELATIVE_TO_MESH;
                    break;
                case "ABSOLUTE":
                default:
                    parsedAltitudeMode = AltitudeMode.ABSOLUTE;
                    break;
            }
            
            monsters.add(new Monster(
                obj.getString("id"),
                obj.getString("label"),
                obj.getDouble("latitude"),
                obj.getDouble("longitude"),
                obj.getDouble("altitude"),
                obj.getDouble("heading"),
                obj.getDouble("tilt"),
                obj.getDouble("range"),
                obj.getDouble("markerLatitude"),
                obj.getDouble("markerLongitude"),
                obj.getDouble("markerAltitude"),
                obj.getString("drawable"),
                parsedAltitudeMode
            ));
        }
        return monsters;
    }
}
