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
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MonsterParserTest {

    @Test
    fun testParseValidJson() {
        val json = """[
            {
                "id": "godzilla",
                "label": "Godzilla",
                "latitude": 35.6586,
                "longitude": 139.7454,
                "altitude": 0.0,
                "heading": 45.0,
                "tilt": 45.0,
                "range": 1000.0,
                "markerLatitude": 35.6586,
                "markerLongitude": 139.7454,
                "markerAltitude": 0.0,
                "drawable": "godzilla_icon",
                "altitudeMode": "CLAMP_TO_GROUND"
            }
        ]"""
        
        val result = MonsterParser.parse(json)
        
        assertThat(result).hasSize(1)
        val monster = result[0]
        assertThat(monster.id).isEqualTo("godzilla")
        assertThat(monster.label).isEqualTo("Godzilla")
        assertThat(monster.latitude).isWithin(0.001).of(35.6586)
        assertThat(monster.longitude).isWithin(0.001).of(139.7454)
        assertThat(monster.drawable).isEqualTo("godzilla_icon")
        assertThat(monster.altitudeMode).isEqualTo(AltitudeMode.CLAMP_TO_GROUND)
    }
}
