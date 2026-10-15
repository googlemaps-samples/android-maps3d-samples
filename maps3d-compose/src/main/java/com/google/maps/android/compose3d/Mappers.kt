package com.google.maps.android.compose3d

import com.google.android.gms.maps3d.model.Glyph
import com.google.android.gms.maps3d.model.Hole
import com.google.android.gms.maps3d.model.ImageView
import com.google.android.gms.maps3d.model.markerOptions
import com.google.android.gms.maps3d.model.modelOptions
import com.google.android.gms.maps3d.model.orientation
import com.google.android.gms.maps3d.model.pinConfiguration
import com.google.android.gms.maps3d.model.polygonOptions
import com.google.android.gms.maps3d.model.polylineOptions
import com.google.android.gms.maps3d.model.vector3D
import com.google.maps.android.compose3d.utils.toValidLocation

/**
 * Extension function to map [PolylineConfig] to [PolylineOptions].
 */
fun PolylineConfig.toPolylineOptions(overrideId: String? = null) = polylineOptions {
    id = overrideId ?: key
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
fun MarkerConfig.toMarkerOptions(overrideId: String? = null) = markerOptions {
    id = overrideId ?: key
    position = this@toMarkerOptions.position.toValidLocation()
    altitudeMode = this@toMarkerOptions.altitudeMode
    label = this@toMarkerOptions.label
    zIndex = this@toMarkerOptions.zIndex
    isExtruded = this@toMarkerOptions.isExtruded
    isDrawnWhenOccluded = this@toMarkerOptions.isDrawnWhenOccluded
    collisionBehavior = this@toMarkerOptions.collisionBehavior
    styleView?.let { setStyle(it) }
    pinConfig?.let { pin ->
        setStyle(
            pinConfiguration {
                pin.scale?.let { scale = it }
                pin.backgroundColor?.let { backgroundColor = it }
                pin.borderColor?.let { borderColor = it }
                pin.glyph?.let { glyphConfig ->
                    val sdkGlyph = when (glyphConfig) {
                        is GlyphConfig.Color -> Glyph.fromColor(glyphConfig.color)

                        is GlyphConfig.Text -> {
                            if (glyphConfig.color != null) {
                                Glyph.fromColor(glyphConfig.color).apply {
                                    setText(glyphConfig.text)
                                }
                            } else {
                                Glyph.fromText(glyphConfig.text)
                            }
                        }

                        is GlyphConfig.Circle -> {
                            Glyph.fromCircle().apply {
                                glyphConfig.color?.let { color = it }
                            }
                        }

                        is GlyphConfig.Image -> {
                            val glyph = if (glyphConfig.color != null) {
                                Glyph.fromColor(glyphConfig.color)
                            } else {
                                Glyph.fromCircle()
                            }
                            glyph.setImage(ImageView(glyphConfig.imageResId))
                            glyph
                        }
                    }
                    setGlyph(sdkGlyph)
                }
            },
        )
    }
}

/**
 * Extension function to map [PolygonConfig] to [PolygonOptions].
 */
fun PolygonConfig.toPolygonOptions(overrideId: String? = null) = polygonOptions {
    id = overrideId ?: key
    path = this@toPolygonOptions.path.map { it.toValidLocation() }
    innerPaths = this@toPolygonOptions.innerPaths.map { Hole(it.map { p -> p.toValidLocation() }) }
    fillColor = this@toPolygonOptions.fillColor
    strokeColor = this@toPolygonOptions.strokeColor
    strokeWidth = this@toPolygonOptions.strokeWidth.toDouble()
    altitudeMode = this@toPolygonOptions.altitudeMode
    geodesic = this@toPolygonOptions.geodesic
    drawsOccludedSegments = this@toPolygonOptions.drawsOccludedSegments
}

/**
 * Extension function to map [ModelConfig] to [ModelOptions].
 */
fun ModelConfig.toModelOptions(overrideId: String? = null) = modelOptions {
    id = overrideId ?: key
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
