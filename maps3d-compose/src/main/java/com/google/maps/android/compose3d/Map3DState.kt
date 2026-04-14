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
import com.google.android.gms.maps3d.model.Hole
import com.google.android.gms.maps3d.model.Marker
import com.google.android.gms.maps3d.model.Polyline
import com.google.android.gms.maps3d.model.Polygon
import com.google.android.gms.maps3d.model.Model
import com.google.android.gms.maps3d.model.markerOptions
import com.google.android.gms.maps3d.model.polylineOptions
import com.google.android.gms.maps3d.model.polygonOptions
import com.google.android.gms.maps3d.model.modelOptions
import com.google.android.gms.maps3d.model.vector3D
import com.google.android.gms.maps3d.model.orientation
import com.google.maps.android.compose3d.utils.toValidLocation
import com.google.maps.android.compose3d.utils.toHeading
import com.google.maps.android.compose3d.utils.toTilt
import com.google.maps.android.compose3d.utils.toRoll

/**
 * Internal state holder for the Maps 3D Compose library.
 *
 * This class maintains the mapping between user-provided configuration keys and the
 * actual SDK objects created on the map. Since the SDK objects are largely immutable
 * or do not support property updates in place, this class recreates them when their
 * configuration changes.
 */
class Map3DState {
    private val markers = mutableMapOf<String, Pair<MarkerConfig, Marker>>()
    private val polylines = mutableMapOf<String, Pair<PolylineConfig, Polyline>>()
    private val polygons = mutableMapOf<String, Pair<PolygonConfig, Polygon>>()
    private val models = mutableMapOf<String, Pair<ModelConfig, Model>>()

    /**
     * Synchronizes the markers on the map with the provided list of configurations.
     */
    fun syncMarkers(map: GoogleMap3D, markerConfigs: List<MarkerConfig>) {
        val keysToRemove = markers.keys.toMutableSet()

        markerConfigs.forEach { config ->
            keysToRemove.remove(config.key)
            val existing = markers[config.key]

            if (existing != null) {
                val (oldConfig, marker) = existing
                if (oldConfig != config) {
                    // Config changed, recreate
                    marker.remove()
                    val newMarker = createMarker(map, config)
                    if (newMarker != null) {
                        markers[config.key] = Pair(config, newMarker)
                    }
                }
            } else {
                // New marker
                val newMarker = createMarker(map, config)
                if (newMarker != null) {
                    markers[config.key] = Pair(config, newMarker)
                }
            }
        }

        keysToRemove.forEach { key ->
            markers[key]?.second?.remove()
            markers.remove(key)
        }
    }

    private fun createMarker(map: GoogleMap3D, config: MarkerConfig): Marker? {
        val marker = map.addMarker(markerOptions {
            position = config.position.toValidLocation()
            altitudeMode = config.altitudeMode
            config.styleView?.let { setStyle(it) }
            label = config.label
            zIndex = config.zIndex
            isExtruded = config.isExtruded
            isDrawnWhenOccluded = config.isDrawnWhenOccluded
            collisionBehavior = config.collisionBehavior
        })
        
        config.onClick?.let { callback ->
            marker?.setClickListener {
                callback(marker)
            }
        }
        
        return marker
    }

    /**
     * Synchronizes the polylines on the map with the provided list of configurations.
     */
    fun syncPolylines(map: GoogleMap3D, polylineConfigs: List<PolylineConfig>) {
        val keysToRemove = polylines.keys.toMutableSet()

        polylineConfigs.forEach { config ->
            keysToRemove.remove(config.key)
            val existing = polylines[config.key]

            if (existing != null) {
                val (oldConfig, polyline) = existing
                if (oldConfig != config) {
                    // Config changed, recreate
                    polyline.remove()
                    val newPolyline = createPolyline(map, config)
                    if (newPolyline != null) {
                        polylines[config.key] = Pair(config, newPolyline)
                    }
                }
            } else {
                // New polyline
                val newPolyline = createPolyline(map, config)
                if (newPolyline != null) {
                    polylines[config.key] = Pair(config, newPolyline)
                }
            }
        }

        keysToRemove.forEach { key ->
            polylines[key]?.second?.remove()
            polylines.remove(key)
        }
    }

    private fun createPolyline(map: GoogleMap3D, config: PolylineConfig): Polyline? {
        val polyline = map.addPolyline(config.toPolylineOptions())
        config.onClick?.let { callback ->
            polyline.setClickListener {
                callback(polyline)
            }
        }
        return polyline
    }

    /**
     * Synchronizes the polygons on the map with the provided list of configurations.
     */
    fun syncPolygons(map: GoogleMap3D, polygonConfigs: List<PolygonConfig>) {
        val keysToRemove = polygons.keys.toMutableSet()

        polygonConfigs.forEach { config ->
            keysToRemove.remove(config.key)
            val existing = polygons[config.key]

            if (existing != null) {
                val (oldConfig, polygon) = existing
                if (oldConfig != config) {
                    // Config changed, recreate
                    polygon.remove()
                    val newPolygon = createPolygon(map, config)
                    if (newPolygon != null) {
                        polygons[config.key] = Pair(config, newPolygon)
                    }
                }
            } else {
                // New polygon
                val newPolygon = createPolygon(map, config)
                if (newPolygon != null) {
                    polygons[config.key] = Pair(config, newPolygon)
                }
            }
        }

        keysToRemove.forEach { key ->
            polygons[key]?.second?.remove()
            polygons.remove(key)
        }
    }

    private fun createPolygon(map: GoogleMap3D, config: PolygonConfig): Polygon? {
        return map.addPolygon(polygonOptions {
            this.path = config.path.map { it.toValidLocation() }
            innerPaths = config.innerPaths.map { Hole(it.map { p -> p.toValidLocation() }) }
            fillColor = config.fillColor
            strokeColor = config.strokeColor
            strokeWidth = config.strokeWidth.toDouble()
            altitudeMode = config.altitudeMode
        })
    }

    /**
     * Synchronizes the 3D models on the map with the provided list of configurations.
     */
    fun syncModels(map: GoogleMap3D, modelConfigs: List<ModelConfig>) {
        val keysToRemove = models.keys.toMutableSet()

        modelConfigs.forEach { config ->
            keysToRemove.remove(config.key)
            val existing = models[config.key]

            if (existing != null) {
                val (oldConfig, model) = existing
                if (oldConfig != config) {
                    // Config changed, recreate
                    model.remove()
                    val newModel = createModel(map, config)
                    if (newModel != null) {
                        models[config.key] = Pair(config, newModel)
                    }
                }
            } else {
                // New model
                val newModel = createModel(map, config)
                if (newModel != null) {
                    models[config.key] = Pair(config, newModel)
                }
            }
        }

        keysToRemove.forEach { key ->
            models[key]?.second?.remove()
            models.remove(key)
        }
    }

    private fun createModel(map: GoogleMap3D, config: ModelConfig): Model? {
        return map.addModel(modelOptions {
            position = config.position.toValidLocation()
            url = config.url
            altitudeMode = config.altitudeMode
            scale = vector3D {
                x = config.scale.toDouble()
                y = config.scale.toDouble()
                z = config.scale.toDouble()
            }
            orientation = orientation {
                heading = config.heading.toHeading()
                tilt = config.tilt.toTilt()
                roll = config.roll.toRoll()
            }
        })
    }

    /**
     * Clears all state and removes all objects from the map.
     */
    fun clear() {
        markers.values.forEach { it.second.remove() }
        markers.clear()
        polylines.values.forEach { it.second.remove() }
        polylines.clear()
        polygons.values.forEach { it.second.remove() }
        polygons.clear()
        models.values.forEach { it.second.remove() }
        models.clear()
    }
}
