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

import com.example.snippets.kotlin.TrackedMap3D
import com.example.snippets.kotlin.annotations.SnippetGroup
import com.example.snippets.kotlin.annotations.SnippetItem
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.flyToOptions
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.gms.maps3d.model.modelOptions
import com.google.android.gms.maps3d.model.orientation
import com.google.android.gms.maps3d.model.vector3D

@SnippetGroup(
    title = "Models",
    description = "Snippets demonstrating 3D Model (GLB) integration and configuration."
)
class ModelSnippets(private val map: TrackedMap3D) {
    companion object {
        const val SAUCER_URL = "https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/UFO.glb"
    }

    /**
     * Adds a basic 3D model (GLB) to the map from a URL.
     */
    @Suppress("unused")
    @SnippetItem(
        title = "1. Basic",
        description = "Loads a GLB model from a URL and places it clamped to the ground."
    )
    fun addBasicModel() {
        // [START maps_android_3d_model_add_kt]
        val position = latLngAltitude {
            latitude = 37.4220
            longitude = -122.0841
            altitude = 100.0
        }

        val options = modelOptions {
            this.position = position
            url = SAUCER_URL
            altitudeMode = AltitudeMode.RELATIVE_TO_MESH
            orientation = orientation {
                tilt = 90.0
                heading = 0.0
                roll = 0.0
            }
            scale = vector3D { x = 10.0; y = 10.0; z = 10.0 }
        }

        val model = map.addModel(options)
        // [END maps_android_3d_model_add_kt]

        // Position the camera to show the model
        map.flyCameraTo(flyToOptions {
            endCamera = camera {
                center = position
                tilt = 45.0
                heading = 0.0
                range = 300.0
            }
            durationInMillis = 2000
        })
    }
}
