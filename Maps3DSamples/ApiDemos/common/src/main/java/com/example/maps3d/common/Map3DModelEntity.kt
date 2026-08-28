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

package com.example.maps3d.common

import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.Model
import com.google.android.gms.maps3d.model.ModelOptions
import com.google.android.gms.maps3d.model.Orientation
import com.google.android.gms.maps3d.model.Vector3D

/**
 * Self-managing lifecycle wrapper for 3D model entities on Google Maps 3D.
 *
 * Encapsulates model creation, pose updates, and safe detachment to prevent memory leaks
 * during Activity recreations and configuration changes.
 */
class Map3DModelEntity(
    val id: String,
    val assetUrl: String,
    val altitudeMode: Int = AltitudeMode.ABSOLUTE
) {
    private var nativeModel: Model? = null

    /**
     * Attaches and renders the model onto the given [map].
     */
    fun attach(map: GoogleMap3D, pose: EntityPose): Model? {
        detach()
        val options = ModelOptions().apply {
            id = this@Map3DModelEntity.id
            position = pose.position
            url = assetUrl
            altitudeMode = this@Map3DModelEntity.altitudeMode
            scale = Vector3D(pose.scale, pose.scale, pose.scale)
            orientation = Orientation(pose.heading, pose.pitch, pose.roll)
        }
        nativeModel = map.addModel(options)
        return nativeModel
    }

    /**
     * Updates the position and orientation of the entity.
     * In the current 3D Maps SDK, re-adding or updating the model is encapsulated here.
     */
    fun applyPose(pose: EntityPose, map: GoogleMap3D?) {
        if (map == null) return
        attach(map, pose)
    }

    /**
     * Safely detaches the entity and releases references to prevent leaking the map context.
     */
    fun detach() {
        nativeModel = null
    }
}
