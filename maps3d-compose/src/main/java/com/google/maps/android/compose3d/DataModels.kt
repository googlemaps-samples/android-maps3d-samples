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

import androidx.annotation.WorkerThread
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.CollisionBehavior
import com.google.android.gms.maps3d.model.ImageView
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.android.gms.maps3d.model.Marker
import com.google.android.gms.maps3d.model.Model
import com.google.android.gms.maps3d.model.Polygon
import com.google.android.gms.maps3d.model.Polyline

/**
 * Data class representing a Marker to be added to the 3D map.
 */
@Immutable
data class MarkerConfig(
    val key: String,
    val position: LatLngAltitude,
    val altitudeMode: Int = AltitudeMode.CLAMP_TO_GROUND,
    val styleView: ImageView? = null,
    val label: String = "",
    val zIndex: Int = 0,
    val isExtruded: Boolean = false,
    val isDrawnWhenOccluded: Boolean = false,
    val collisionBehavior: Int = CollisionBehavior.REQUIRED,
    val onClick: ((Marker) -> Unit)? = null,
)

/**
 * Data class representing a Polyline to be added to the 3D map.
 */
@Immutable
data class PolylineConfig(
    val key: String,
    val points: List<LatLngAltitude>,
    val color: Int,
    val width: Float,
    val altitudeMode: Int = AltitudeMode.CLAMP_TO_GROUND,
    val zIndex: Int = 0,
    val outerColor: Int = 0,
    val outerWidth: Float = 0f,
    val drawsOccludedSegments: Boolean = false,
    @get:WorkerThread
    val onClick: ((Polyline) -> Unit)? = null,
)

/**
 * Data class representing a Polygon to be added to the 3D map.
 */
@Immutable
data class PolygonConfig(
    val key: String,
    val path: List<LatLngAltitude>,
    val innerPaths: List<List<LatLngAltitude>> = emptyList(),
    val fillColor: Int,
    val strokeColor: Int,
    val strokeWidth: Float,
    val altitudeMode: Int = AltitudeMode.CLAMP_TO_GROUND,
    val onClick: ((Polygon) -> Unit)? = null,
)

/**
 * Sealed class representing the scale of a 3D model.
 */
sealed class ModelScale {
    data class Uniform(val value: Float) : ModelScale()
    data class PerAxis(val x: Float, val y: Float, val z: Float) : ModelScale()
}

/**
 * Data class representing a 3D Model to be added to the 3D map.
 */
@Immutable
data class ModelConfig(
    val key: String,
    val position: LatLngAltitude,
    val url: String,
    val altitudeMode: Int = AltitudeMode.CLAMP_TO_GROUND,
    val scale: ModelScale = ModelScale.Uniform(1.0f),
    val heading: Double = 0.0,
    val tilt: Double = 0.0,
    val roll: Double = 0.0,
    val onClick: ((Model) -> Unit)? = null,
)

/**
 * Data class representing a Popover to be added to the 3D map.
 */
@Immutable
data class PopoverConfig(
    val key: String,
    val positionAnchorKey: String,
    val content: @Composable () -> Unit,
    val altitudeMode: Int = AltitudeMode.CLAMP_TO_GROUND,
    val autoCloseEnabled: Boolean = true,
    val autoPanEnabled: Boolean = true,
)
