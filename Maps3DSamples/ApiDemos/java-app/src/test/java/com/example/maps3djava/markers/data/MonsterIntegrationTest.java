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
