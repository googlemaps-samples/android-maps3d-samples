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

package com.example.maps3djava.mainactivity;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.lang.reflect.Field;
import java.util.Map;

@RunWith(RobolectricTestRunner.class)
public class MainActivityTest {

    @Test
    public void testSampleActivitiesContainsAllSamples() throws Exception {
        MainActivity activity = new MainActivity();
        Field field = MainActivity.class.getDeclaredField("sampleActivities");
        field.setAccessible(true);
        Map<Integer, Class<?>> samples = (Map<Integer, Class<?>>) field.get(activity);

        assertThat(samples).hasSize(8);
        assertThat(samples.values()).contains(com.example.maps3djava.popovers.PopoversActivity.class);
        assertThat(samples.values()).contains(com.example.maps3djava.mapinteractions.MapInteractionsActivity.class);
    }
}
