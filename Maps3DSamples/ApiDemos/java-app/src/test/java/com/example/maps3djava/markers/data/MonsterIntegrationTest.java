package com.example.maps3djava.markers.data;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import com.google.common.truth.Truth;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = { 34 })
public class MonsterIntegrationTest {

  @Test
  public void testParseActualMonstersJson() throws Exception {
    Context context = ApplicationProvider.getApplicationContext();
    String jsonString;
    try (InputStream is = context.getAssets().open("monsters.json")) {
      byte[] buffer = new byte[is.available()];
      int bytesRead = is.read(buffer);
      Truth.assertThat(bytesRead).isGreaterThan(0); // Ensure some bytes were read
      jsonString = new String(buffer, StandardCharsets.UTF_8);
    }

    List<Monster> monsters = MonsterParser.parse(jsonString);

    Truth.assertThat(monsters.size()).isAtLeast(8);

    Monster mothra = null;
    for (Monster m : monsters) {
      if ("mothra".equals(m.id)) {
        mothra = m;
        break;
      }
    }

    Truth.assertThat(mothra).isNotNull();
    Truth.assertThat(mothra.label).isEqualTo("Tokyo Tower Mothra");
  }
}
