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

package com.example.maps3dkotlin.mainactivity

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.lang.reflect.Field

@RunWith(RobolectricTestRunner::class)
class MainActivityTest {

    @Test
    fun testSamplesContainsAllSamples() {
        val activity = MainActivity()
        val field: Field = MainActivity::class.java.getDeclaredField("samples")
        field.isAccessible = true
        val samples = field.get(activity) as List<*>

        assertThat(samples).hasSize(8)
        
        // Extract the activityClass from each Sample object
        val sampleClasses = samples.map { 
            val javaClass = it!!::class.java
            val activityClassField = javaClass.getDeclaredField("activityClass")
            activityClassField.isAccessible = true
            activityClassField.get(it) as Class<*>
        }
        
        assertThat(sampleClasses).contains(com.example.maps3dkotlin.popovers.PopoversActivity::class.java)
        assertThat(sampleClasses).contains(com.example.maps3dkotlin.mapinteractions.MapInteractionsActivity::class.java)
    }
}
