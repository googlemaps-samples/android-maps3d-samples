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

package com.example.snippets.kotlin.snippets

import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.gms.maps3d.model.modelOptions
import com.google.android.gms.maps3d.model.orientation
import com.google.android.gms.maps3d.model.vector3D
import com.example.snippets.kotlin.annotations.SnippetGroup
import com.example.snippets.kotlin.annotations.SnippetItem

@SnippetGroup(
    title = "Models",
    description = "Snippets demonstrating 3D Model (GLB) integration and configuration."
)
class ModelSnippets(private val map: GoogleMap3D) {

    /**
     * Adds a basic 3D model (GLB) to the map from a URL.
     */
    @Suppress("unused")
    @SnippetItem(
        title = "Basic",
        description = "Loads a GLB model from a URL and places it clamped to the ground."
    )
    fun addBasicModel() {
        // [START maps_android_3d_model_add_kt]
        val position = latLngAltitude {
            latitude = 37.4220
            longitude = -122.0841
            altitude = 0.0
        }
        
        val options = modelOptions {
            this.position = position
            url = "https://example.com/model.glb"
            altitudeMode = AltitudeMode.CLAMP_TO_GROUND
        }
        
        val model = map.addModel(options)
        // [END maps_android_3d_model_add_kt]
    }

    /**
     * Adds a 3D model with advanced configuration (scale, orientation).
     */
    @Suppress("unused")
    @SnippetItem(
        title = "Advanced",
        description = "Loads a GLB model with advanced configuration (scale, orientation) from assets."
    )
    fun addAdvancedModel() {
        // [START maps_android_3d_model_options_kt]
        val options = modelOptions {
            position = latLngAltitude {
                latitude = 37.4220
                longitude = -122.0841
                altitude = 10.0
            }
            url = "file:///android_asset/my_model.glb"
            scale = vector3D { x = 2.0; y = 2.0; z = 2.0 }
            orientation = orientation {
                tilt = 45.0
                heading = 0.0
                roll = 0.0
            }
            altitudeMode = AltitudeMode.RELATIVE_TO_GROUND
        }
        
        val model = map.addModel(options)
        // [END maps_android_3d_model_options_kt]
    }
}
