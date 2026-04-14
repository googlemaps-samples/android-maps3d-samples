package com.google.maps.android.compose3d

import com.google.android.gms.maps3d.model.polylineOptions
import com.google.maps.android.compose3d.utils.toValidLocation

fun PolylineConfig.toPolylineOptions() = polylineOptions {
    this.path = points.map { it.toValidLocation() }
    strokeColor = color
    strokeWidth = width.toDouble()
    altitudeMode = altitudeMode
    zIndex = zIndex
    outerColor = outerColor
    outerWidth = outerWidth
    drawsOccludedSegments = drawsOccludedSegments
}
