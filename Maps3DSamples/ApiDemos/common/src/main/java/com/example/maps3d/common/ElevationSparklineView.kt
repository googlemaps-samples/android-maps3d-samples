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

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import kotlin.math.max

/**
 * Custom 2D sparkline [View] shared by Kotlin Views and Java Views to plot the upcoming
 * 3D terrain elevation profile (`250m–750m` ahead), shade active hazard zones in Amber/Red,
 * mark upcoming [ExtremaType.CREST] and [ExtremaType.DIP] inflection points, and pin the
 * vehicle indicator at `x = 0`.
 */
class ElevationSparklineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var profilePoints: List<ElevationProfilePoint> = emptyList()
    private var extremaPoints: List<TerrainExtremaPoint> = emptyList()
    private var alertSeverity: AlertSeverity = AlertSeverity.NONE
    private var lookaheadHorizonMeters: Double = 500.0

    private val density = resources.displayMetrics.density

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f * density
        color = Color.argb(45, 255, 255, 255)
    }

    private val baseFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(45, 56, 189, 248) // Translucent sky cyan (#38BDF8)
    }

    private val hazardFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val baseCurvePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.2f * density
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        color = Color.rgb(56, 189, 248) // Sky cyan (#38BDF8)
    }

    private val hazardCurvePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3.0f * density
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val dotFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val dotStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.5f * density
        color = Color.WHITE
    }

    private val curvePath = Path()
    private val fillPath = Path()
    private val hazardCurvePath = Path()
    private val hazardFillPath = Path()

    /**
     * Updates the sparkline dataset and triggers a redraw.
     */
    fun updateData(
        profile: List<ElevationProfilePoint>,
        extrema: List<TerrainExtremaPoint>,
        severity: AlertSeverity,
        horizonMeters: Double
    ) {
        profilePoints = profile
        extremaPoints = extrema
        alertSeverity = severity
        lookaheadHorizonMeters = max(10.0, horizonMeters)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f || profilePoints.size < 2) return

        val padH = 10f * density
        val padV = 8f * density
        val plotLeft = padH
        val plotRight = (w - padH).coerceAtLeast(plotLeft + 1f)
        val plotTop = padV
        val plotBottom = (h - padV).coerceAtLeast(plotTop + 1f)
        val plotWidth = plotRight - plotLeft
        val plotHeight = plotBottom - plotTop

        // Baseline grid lines
        val midY = (plotTop + plotBottom) * 0.5f
        canvas.drawLine(plotLeft, midY, plotRight, midY, gridPaint)
        canvas.drawLine(plotLeft, plotBottom, plotRight, plotBottom, gridPaint)

        var minAlt = profilePoints.first().altitudeMeters
        var maxAlt = minAlt
        for (pt in profilePoints) {
            if (pt.altitudeMeters < minAlt) minAlt = pt.altitudeMeters
            if (pt.altitudeMeters > maxAlt) maxAlt = pt.altitudeMeters
        }
        // Enforce at least a 12m vertical span so flat roads look authentically flat
        val rawSpan = maxAlt - minAlt
        val minVisualSpan = 12.0
        val effectiveSpan = max(rawSpan, minVisualSpan)
        val altPadding = (effectiveSpan - rawSpan) * 0.5
        val lowAlt = minAlt - altPadding

        val maxDist = max(
            lookaheadHorizonMeters,
            profilePoints.last().distanceOffsetMeters.coerceAtLeast(1.0)
        )

        fun mapX(distOffset: Double): Float {
            val ratio = (distOffset / maxDist).coerceIn(0.0, 1.0).toFloat()
            return plotLeft + ratio * plotWidth
        }

        fun mapY(altitude: Double): Float {
            val ratio = ((altitude - lowAlt) / effectiveSpan).coerceIn(0.0, 1.0).toFloat()
            return plotBottom - ratio * plotHeight
        }

        // 1. Base area fill and curve path
        curvePath.reset()
        fillPath.reset()
        hazardCurvePath.reset()
        hazardFillPath.reset()

        var inHazardRun = false
        var hazardRunStartX = plotLeft

        for (i in profilePoints.indices) {
            val pt = profilePoints[i]
            val x = mapX(pt.distanceOffsetMeters)
            val y = mapY(pt.altitudeMeters)

            if (i == 0) {
                curvePath.moveTo(x, y)
                fillPath.moveTo(x, plotBottom)
                fillPath.lineTo(x, y)
            } else {
                curvePath.lineTo(x, y)
                fillPath.lineTo(x, y)
            }

            if (pt.isHazardZone) {
                if (!inHazardRun) {
                    inHazardRun = true
                    hazardRunStartX = x
                    hazardCurvePath.moveTo(x, y)
                    hazardFillPath.moveTo(x, plotBottom)
                    hazardFillPath.lineTo(x, y)
                } else {
                    hazardCurvePath.lineTo(x, y)
                    hazardFillPath.lineTo(x, y)
                }
            } else if (inHazardRun) {
                val prevX = mapX(profilePoints[i - 1].distanceOffsetMeters)
                hazardFillPath.lineTo(prevX, plotBottom)
                hazardFillPath.lineTo(hazardRunStartX, plotBottom)
                hazardFillPath.close()
                inHazardRun = false
            }
        }

        val lastX = mapX(profilePoints.last().distanceOffsetMeters)
        fillPath.lineTo(lastX, plotBottom)
        fillPath.lineTo(plotLeft, plotBottom)
        fillPath.close()

        if (inHazardRun) {
            hazardFillPath.lineTo(lastX, plotBottom)
            hazardFillPath.lineTo(hazardRunStartX, plotBottom)
            hazardFillPath.close()
        }

        canvas.drawPath(fillPath, baseFillPaint)

        // 2. Hazard shading & highlight curve (Amber #FBBC04 for CAUTION, Red #EA4335 for WARNING)
        if (alertSeverity != AlertSeverity.NONE) {
            val isWarning = alertSeverity == AlertSeverity.WARNING
            hazardFillPaint.color = if (isWarning) {
                Color.argb(85, 234, 67, 53) // Canonical Google Red (#EA4335)
            } else {
                Color.argb(85, 251, 188, 4) // Canonical Google Amber (#FBBC04)
            }
            hazardCurvePaint.color = if (isWarning) {
                Color.rgb(234, 67, 53)
            } else {
                Color.rgb(251, 188, 4)
            }
            canvas.drawPath(hazardFillPath, hazardFillPaint)
        }

        canvas.drawPath(curvePath, baseCurvePaint)
        if (alertSeverity != AlertSeverity.NONE) {
            canvas.drawPath(hazardCurvePath, hazardCurvePaint)
        }

        // 3. Draw Crest (peak) and Dip (trough) dots
        val dotRadius = 3.8f * density
        for (ext in extremaPoints) {
            val x = mapX(ext.distanceAheadMeters)
            val y = mapY(ext.position.altitude)
            dotFillPaint.color = if (ext.type == ExtremaType.CREST) {
                Color.rgb(234, 67, 53) // Crest: Red-Orange (#EA4335)
            } else {
                Color.rgb(251, 188, 4) // Dip: Amber (#FBBC04)
            }
            canvas.drawCircle(x, y, dotRadius, dotFillPaint)
            canvas.drawCircle(x, y, dotRadius, dotStrokePaint)
        }

        // 4. Pin vehicle indicator at x = 0 (plotLeft)
        val carPoint = profilePoints.first()
        val carX = mapX(carPoint.distanceOffsetMeters)
        val carY = mapY(carPoint.altitudeMeters)
        val carRadius = 4.5f * density
        dotFillPaint.color = Color.rgb(52, 211, 153) // Emerald green (#34D399) vehicle dot
        canvas.drawCircle(carX, carY, carRadius, dotFillPaint)
        canvas.drawCircle(carX, carY, carRadius, dotStrokePaint)
    }
}
