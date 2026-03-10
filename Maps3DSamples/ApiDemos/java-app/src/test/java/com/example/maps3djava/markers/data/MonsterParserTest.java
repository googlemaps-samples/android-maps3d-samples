package com.example.maps3djava.markers.data;

import com.google.android.gms.maps3d.model.AltitudeMode;
import com.google.common.truth.Truth;
import org.junit.Test;
import org.json.JSONException;
import java.util.List;

public class MonsterParserTest {

    @Test
    public void testParseValidJson() throws JSONException {
        String json = "[\n" +
                "    {\n" +
                "        \"id\": \"mothra\",\n" +
                "        \"label\": \"Mothra\",\n" +
                "        \"latitude\": 35.6586,\n" +
                "        \"longitude\": 139.7454,\n" +
                "        \"altitude\": 0.0,\n" +
                "        \"heading\": 45.0,\n" +
                "        \"tilt\": 45.0,\n" +
                "        \"range\": 1000.0,\n" +
                "        \"markerLatitude\": 35.6586,\n" +
                "        \"markerLongitude\": 139.7454,\n" +
                "        \"markerAltitude\": 0.0,\n" +
                "        \"drawable\": \"mothra_icon\",\n" +
                "        \"altitudeMode\": \"RELATIVE_TO_GROUND\"\n" +
                "    }\n" +
                "]";

        List<Monster> result = MonsterParser.parse(json);

        Truth.assertThat(result).hasSize(1);
        Monster monster = result.get(0);
        Truth.assertThat(monster.id).isEqualTo("mothra");
        Truth.assertThat(monster.label).isEqualTo("Mothra");
        Truth.assertThat(monster.latitude).isWithin(0.001).of(35.6586);
        Truth.assertThat(monster.longitude).isWithin(0.001).of(139.7454);
        Truth.assertThat(monster.drawable).isEqualTo("mothra_icon");
        Truth.assertThat(monster.altitudeMode).isEqualTo(AltitudeMode.RELATIVE_TO_GROUND);
    }
}
