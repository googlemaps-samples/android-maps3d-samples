package com.google.maps.android.compose3d

import com.google.android.gms.maps3d.model.Hole
import com.google.android.gms.maps3d.model.markerOptions
import com.google.android.gms.maps3d.model.modelOptions
import com.google.android.gms.maps3d.model.orientation
import com.google.android.gms.maps3d.model.polygonOptions
import com.google.android.gms.maps3d.model.polylineOptions
import com.google.android.gms.maps3d.model.vector3D
import com.google.maps.android.compose3d.utils.toValidLocation

/**
 * Extension function to map [PolylineConfig] to [PolylineOptions].
 */
fun PolylineConfig.toPolylineOptions() = polylineOptions {
    this.path = points.map { it.toValidLocation() }
    strokeColor = color
    strokeWidth = width.toDouble()
    altitudeMode = this@toPolylineOptions.altitudeMode
    zIndex = this@toPolylineOptions.zIndex
    outerColor = this@toPolylineOptions.outerColor
    outerWidth = this@toPolylineOptions.outerWidth.toDouble()
    drawsOccludedSegments = this@toPolylineOptions.drawsOccludedSegments
}

/**
 * Extension function to map [MarkerConfig] to [MarkerOptions].
 */
fun MarkerConfig.toMarkerOptions() = markerOptions {
    id = key
    position = this@toMarkerOptions.position.toValidLocation()
    altitudeMode = this@toMarkerOptions.altitudeMode
    label = this@toMarkerOptions.label
    isExtruded = this@toMarkerOptions.isExtruded
    isDrawnWhenOccluded = this@toMarkerOptions.isDrawnWhenOccluded
    collisionBehavior = this@toMarkerOptions.collisionBehavior
    styleView?.let { setStyle(it) }
}

/**
 * Extension function to map [PolygonConfig] to [PolygonOptions].
 */
fun PolygonConfig.toPolygonOptions() = polygonOptions {
    path = this@toPolygonOptions.path.map { it.toValidLocation() }
    innerPaths = this@toPolygonOptions.innerPaths.map { Hole(it.map { p -> p.toValidLocation() }) }
    fillColor = this@toPolygonOptions.fillColor
    strokeColor = this@toPolygonOptions.strokeColor
    strokeWidth = this@toPolygonOptions.strokeWidth.toDouble()
    altitudeMode = this@toPolygonOptions.altitudeMode
}

/**
 * Extension function to map [ModelConfig] to [ModelOptions].
 */
fun ModelConfig.toModelOptions() = modelOptions {
    id = key
    position = this@toModelOptions.position.toValidLocation()
    altitudeMode = this@toModelOptions.altitudeMode
    orientation = orientation {
        heading = this@toModelOptions.heading
        tilt = this@toModelOptions.tilt
        roll = this@toModelOptions.roll
    }
    url = this@toModelOptions.url
    scale = when (val s = this@toModelOptions.scale) {
        is ModelScale.Uniform -> vector3D {
            x = s.value.toDouble()
            y = s.value.toDouble()
            z = s.value.toDouble()
        }
        is ModelScale.PerAxis -> vector3D {
            x = s.x.toDouble()
            y = s.y.toDouble()
            z = s.z.toDouble()
        }
    }
}
