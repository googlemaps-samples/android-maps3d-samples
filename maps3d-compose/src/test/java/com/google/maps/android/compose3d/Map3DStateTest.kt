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

package com.google.maps.android.compose3d

import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.Model
import com.google.android.gms.maps3d.model.latLngAltitude
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class Map3DStateTest {

    @Test
    fun testSyncModelsUpdatesInsteadOfRecreating() {
        val map = mockk<GoogleMap3D>(relaxed = true)
        val model = mockk<Model>(relaxed = true)
        
        val state = Map3DState()
        
        val config1 = ModelConfig(
            key = "test", 
            url = "url1", 
            position = latLngAltitude { latitude = 0.0; longitude = 0.0; altitude = 0.0 }
        )
        val config2 = ModelConfig(
            key = "test", 
            url = "url1", 
            position = latLngAltitude { latitude = 1.0; longitude = 1.0; altitude = 0.0 }
        ) // changed position!
        
        // Mock map.addModel to return our mocked model
        every { map.addModel(any()) } returns model
        
        // First sync adds it
        state.syncModels(map, listOf(config1))
        
        // Second sync with changed config
        state.syncModels(map, listOf(config2))
        
        // Verify that model.remove() was NOT called!
        verify(exactly = 0) { model.remove() }
        // And verify that map.addModel was called twice (once for add, once for update!)
        verify(exactly = 2) { map.addModel(any()) }
    }
}
