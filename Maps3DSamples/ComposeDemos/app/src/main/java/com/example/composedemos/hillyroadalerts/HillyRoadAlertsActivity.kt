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

package com.example.composedemos.hillyroadalerts

import android.app.Activity
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.maps3d.common.AlertSeverity
import com.example.maps3d.common.ElevationProfilePoint
import com.example.maps3d.common.ExtremaType
import com.example.maps3d.common.HillyRoadAlertViewModel
import com.example.maps3d.common.HillyRoadUiState
import com.example.maps3d.common.HillyRoutePreset
import com.example.maps3d.common.Map3DQuickSettingsWidget
import com.example.maps3d.common.Map3DSessionTheme
import com.example.maps3d.common.TerrainAlertState
import com.example.maps3d.common.TerrainAlertType
import com.example.maps3d.common.TerrainExtremaPoint
import com.example.maps3d.common.rememberMap3DSessionSettings
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.CollisionBehavior
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.camera
import com.google.maps.android.compose3d.GlyphConfig
import com.google.maps.android.compose3d.GoogleMap3D
import com.google.maps.android.compose3d.MarkerConfig
import com.google.maps.android.compose3d.ModelConfig
import com.google.maps.android.compose3d.ModelScale
import com.google.maps.android.compose3d.PinConfig
import com.google.maps.android.compose3d.PolylineConfig
import kotlinx.coroutines.isActive
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt
import android.graphics.Color as AndroidColor
import com.example.maps3dcommon.R as CommonR

/**
 * Jetpack Compose showcase activity for **"Hilly & Undulating (Roller-Coaster) Road Early Alerts"**.
 */
class HillyRoadAlertsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Map3DSessionTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    HillyRoadAlertsScreen()
                }
            }
        }
    }
}

/**
 * Main Jetpack Compose screen for the Hilly & Undulating Road Early Alerts sample.
 */
@Composable
fun HillyRoadAlertsScreen(
    viewModel: HillyRoadAlertViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var googleMap3D by remember { mutableStateOf<GoogleMap3D?>(null) }
    val settings by rememberMap3DSessionSettings(defaultMapMode = Map3DMode.HYBRID)
    val context = LocalContext.current

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.setPlaying(false)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Hardware VSYNC-synchronized frame ticker (starts only after 3D map is ready)
    LaunchedEffect(state.isMapReady, state.isPlaying, state.isUserScrubbing) {
        if (!state.isMapReady || !state.isPlaying || state.isUserScrubbing) return@LaunchedEffect
        var lastTimeNanos = 0L
        while (isActive) {
            withFrameMillis { frameTimeMillis ->
                val nowNanos = frameTimeMillis * 1_000_000L
                if (lastTimeNanos != 0L) {
                    val dtSeconds = (nowNanos - lastTimeNanos) / 1_000_000_000.0
                    viewModel.advance(dtSeconds)
                }
                lastTimeNanos = nowNanos
            }
        }
    }

    val dynamicCamera = remember(
        state.cameraCenter,
        state.cameraHeading,
        state.cameraTilt,
        state.cameraRange,
    ) {
        camera {
            center = state.cameraCenter
            heading = state.cameraHeading
            tilt = state.cameraTilt
            range = state.cameraRange
            roll = 0.0
        }
    }

    val basePolylineConfig = remember(state.selectedPreset, state.staticRouteVertices) {
        if (state.staticRouteVertices.size < 2) {
            null
        } else {
            PolylineConfig(
                key = HillyRoadAlertViewModel.BASE_ROUTE_POLYLINE_ID,
                points = state.staticRouteVertices,
                color = AndroidColor.argb(210, 56, 189, 248), // Sky cyan (#38BDF8)
                width = 14f,
                altitudeMode = AltitudeMode.ABSOLUTE,
                zIndex = 2,
                drawsOccludedSegments = true,
            )
        }
    }

    val traveledPolylineConfig = remember(state.traveledRouteVertices) {
        if (state.traveledRouteVertices.size < 2) {
            null
        } else {
            PolylineConfig(
                key = HillyRoadAlertViewModel.TRAVELED_ROUTE_POLYLINE_ID,
                points = state.traveledRouteVertices,
                color = AndroidColor.rgb(16, 185, 129), // Emerald (#10B981)
                width = 16f,
                altitudeMode = AltitudeMode.ABSOLUTE,
                zIndex = 3,
                drawsOccludedSegments = true,
            )
        }
    }

    val hazardPolylineConfig = remember(
        state.terrainAlert.hazardPolylineCoordinates,
        state.terrainAlert.severity,
    ) {
        val coords = state.terrainAlert.hazardPolylineCoordinates
        val severity = state.terrainAlert.severity
        if (coords.size < 2 || severity == AlertSeverity.NONE) {
            null
        } else {
            val hazardColor = if (severity == AlertSeverity.WARNING) {
                AndroidColor.rgb(234, 67, 53) // Canonical Google Red (#EA4335)
            } else {
                AndroidColor.rgb(251, 188, 4) // Canonical Google Amber (#FBBC04)
            }
            PolylineConfig(
                key = HillyRoadAlertViewModel.HAZARD_ROUTE_POLYLINE_ID,
                points = coords,
                color = hazardColor,
                width = 20f,
                altitudeMode = AltitudeMode.ABSOLUTE,
                zIndex = 5,
                drawsOccludedSegments = true,
            )
        }
    }

    val vehicleModelConfig = remember(state.vehiclePosition, state.currentHeadingDegrees) {
        ModelConfig(
            key = HillyRoadAlertViewModel.VEHICLE_MODEL_ID,
            url = HillyRoadAlertViewModel.VEHICLE_MODEL_URL,
            position = state.vehiclePosition,
            altitudeMode = AltitudeMode.ABSOLUTE,
            scale = ModelScale.Uniform(HillyRoadAlertViewModel.VEHICLE_MODEL_SCALE.toFloat()),
            heading = state.currentHeadingDegrees,
            tilt = HillyRoadAlertViewModel.VEHICLE_MODEL_TILT,
            roll = 0.0,
        )
    }

    val allModels = remember(vehicleModelConfig) {
        listOf(vehicleModelConfig)
    }

    // Cache MarkerConfig instances by stable extremum ID so Map3DState.syncMarkers only
    // adds newly entered extrema and removes exited extrema without recreating unchanged markers.
    val cachedExtremaMarkerConfigs = remember(state.selectedPreset) {
        mutableMapOf<String, MarkerConfig>()
    }
    val crestFormat = stringResource(CommonR.string.hilly_marker_crest_format)
    val dipFormat = stringResource(CommonR.string.hilly_marker_dip_format)
    val toastFormat = stringResource(CommonR.string.hilly_marker_toast_format)

    val extremaMarkerConfigs = remember(state.terrainAlert.upcomingExtrema) {
        val upcoming = state.terrainAlert.upcomingExtrema
        val activeIds = upcoming.map { it.id }.toSet()
        cachedExtremaMarkerConfigs.keys.retainAll(activeIds)

        upcoming.map { extremum ->
            cachedExtremaMarkerConfigs.getOrPut(extremum.id) {
                val isCrest = extremum.type == ExtremaType.CREST
                val bgColor = if (isCrest) {
                    AndroidColor.rgb(234, 67, 53) // Crest: Red-Orange (#EA4335)
                } else {
                    AndroidColor.rgb(2, 132, 199) // Dip: Deep Sky Blue (#0284C7)
                }
                val borderColor = if (isCrest) {
                    AndroidColor.rgb(254, 240, 138)
                } else {
                    AndroidColor.rgb(251, 188, 4) // Amber (#FBBC04)
                }
                val glyphText = if (isCrest) "^" else "v"
                val signedGrade = if (isCrest) {
                    abs(extremum.approachGradePercent)
                } else {
                    -abs(extremum.approachGradePercent)
                }
                val labelText = String.format(
                    Locale.US,
                    if (isCrest) crestFormat else dipFormat,
                    signedGrade,
                )
                MarkerConfig(
                    key = extremum.id,
                    position = extremum.position,
                    altitudeMode = AltitudeMode.ABSOLUTE,
                    label = labelText,
                    zIndex = 8,
                    isExtruded = true,
                    isDrawnWhenOccluded = true,
                    collisionBehavior = CollisionBehavior.REQUIRED_AND_HIDES_OPTIONAL,
                    pinConfig = PinConfig(
                        scale = 1.2f,
                        backgroundColor = bgColor,
                        borderColor = borderColor,
                        glyph = GlyphConfig.Text(glyphText, AndroidColor.WHITE),
                    ),
                    onClick = {
                        (context as? Activity)?.runOnUiThread {
                            Toast.makeText(
                                context,
                                String.format(
                                    Locale.US,
                                    toastFormat,
                                    labelText,
                                    extremum.distanceAheadMeters.roundToInt(),
                                    extremum.position.altitude.roundToInt(),
                                ),
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    },
                )
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap3D(
            modifier = Modifier.fillMaxSize(),
            camera = dynamicCamera,
            mapMode = settings.mapMode,
            polylines = if (state.isMapReady) {
                listOfNotNull(
                    basePolylineConfig,
                    traveledPolylineConfig,
                    hazardPolylineConfig,
                )
            } else {
                emptyList()
            },
            markers = if (state.isMapReady) extremaMarkerConfigs else emptyList(),
            models = if (state.isMapReady) allModels else emptyList(),
            onMapReady = { map ->
                googleMap3D = map
                if (HillyRoadAlertViewModel.isInternetAvailable(context)) {
                    viewModel.onMapReady()
                }
            },
            onMapSteady = {
                viewModel.onMapReady()
            },
        )

        // Top Early Warning HUD Banner + Live Elevation Sparkline
        EarlyWarningHudBanner(
            alert = state.terrainAlert,
            lookaheadHorizonMeters = state.lookaheadHorizonMeters,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = 12.dp, top = 8.dp, end = 64.dp)
                .fillMaxWidth(),
        )

        // 3D Map Quick Settings Button
        Map3DQuickSettingsWidget(
            googleMap3D = googleMap3D,
            sampleDefaultMapMode = Map3DMode.HYBRID,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 12.dp, end = 12.dp),
        )

        // Compact Collapsible Bottom Control Sheet
        HillyRoadBottomControlsCard(
            state = state,
            onTogglePlayPause = viewModel::togglePlayPause,
            onJumpToHills = viewModel::jumpToHillyZone,
            onScrubbingChanged = viewModel::setScrubbing,
            onSeekProgress = viewModel::seekToProgress,
            onSelectRoute = viewModel::selectRoute,
            onSelectHorizon = viewModel::setLookaheadHorizon,
            onSelectSpeed = viewModel::setSpeedMultiplier,
            onToggleCameraPreview = viewModel::setAutoCameraPitchPreview,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(12.dp)
                .fillMaxWidth(),
        )
    }
}

@Composable
private fun EarlyWarningHudBanner(
    alert: TerrainAlertState,
    lookaheadHorizonMeters: Double,
    modifier: Modifier = Modifier,
) {
    // Canonical Google alert colors: Red (#EA4335), Amber (#FBBC04), Emerald (#10B981)
    val borderColor = when (alert.severity) {
        AlertSeverity.WARNING -> Color(0xFFEA4335)
        AlertSeverity.CAUTION -> Color(0xFFFBBC04)
        AlertSeverity.NONE -> Color(0xFF10B981)
    }
    val pillTextColor = when (alert.severity) {
        AlertSeverity.WARNING -> Color(0xFFFECACA)
        AlertSeverity.CAUTION -> Color(0xFFFEF08A)
        AlertSeverity.NONE -> Color(0xFFA7F3D0)
    }

    val titleText = when (alert.alertType) {
        TerrainAlertType.UNDULATING_ROLLER_COASTER ->
            stringResource(CommonR.string.hilly_alert_title_roller_coaster)

        TerrainAlertType.BLIND_CREST ->
            stringResource(CommonR.string.hilly_alert_title_blind_crest)

        TerrainAlertType.STEEP_CLIMB ->
            stringResource(CommonR.string.hilly_alert_title_steep_climb)

        TerrainAlertType.STEEP_DESCENT ->
            stringResource(CommonR.string.hilly_alert_title_steep_descent)

        TerrainAlertType.NONE ->
            stringResource(CommonR.string.hilly_alert_title_clear)
    }

    val distancePillText = when {
        alert.alertType == TerrainAlertType.NONE ->
            stringResource(CommonR.string.hilly_alert_distance_clear)

        alert.distanceToHazardMeters <= 0.5 ->
            stringResource(
                CommonR.string.hilly_alert_distance_active_format,
                alert.remainingHazardDistanceMeters.roundToInt(),
            )

        else ->
            stringResource(
                CommonR.string.hilly_alert_distance_approaching_format,
                alert.distanceToHazardMeters.roundToInt(),
            )
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xE61E293B),
        ),
        border = BorderStroke(2.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .background(
                            color = Color(0x33FFFFFF),
                            shape = RoundedCornerShape(12.dp),
                        )
                        .padding(horizontal = 10.dp, vertical = 3.dp),
                ) {
                    Text(
                        text = distancePillText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = pillTextColor,
                    )
                }
            }

            // Telemetry Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                HudTelemetryPill(
                    text = stringResource(
                        CommonR.string.hilly_telemetry_extrema_format,
                        alert.crestCount,
                        alert.dipCount,
                    ),
                )
                HudTelemetryPill(
                    text = String.format(
                        Locale.US,
                        stringResource(CommonR.string.hilly_telemetry_max_grade_format),
                        alert.maxPositiveGradePercent,
                        alert.maxNegativeGradePercent,
                    ),
                )
                HudTelemetryPill(
                    text = stringResource(
                        CommonR.string.hilly_telemetry_advisory_speed_format,
                        alert.recommendedSpeedKmh,
                    ),
                    textColor = Color(0xFFFDE047),
                    isBold = true,
                )
            }

            Text(
                text = stringResource(
                    CommonR.string.hilly_sparkline_label_format,
                    lookaheadHorizonMeters.roundToInt(),
                ),
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(top = 6.dp),
            )

            LookaheadElevationSparklineCanvas(
                profile = alert.lookaheadProfile,
                extrema = alert.upcomingExtrema,
                severity = alert.severity,
                horizonMeters = lookaheadHorizonMeters,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(top = 2.dp),
            )
        }
    }
}

@Composable
private fun HudTelemetryPill(
    text: String,
    textColor: Color = Color(0xFFE2E8F0),
    isBold: Boolean = false,
) {
    Box(
        modifier = Modifier
            .background(
                color = Color(0x26FFFFFF),
                shape = RoundedCornerShape(10.dp),
            )
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = textColor,
        )
    }
}

/**
 * Compose [Canvas] plotting the upcoming 3D terrain elevation profile (`250m–750m` ahead),
 * shading undulating hazard zones in Amber/Red, marking upcoming `CREST` and `DIP` points,
 * and pinning the vehicle indicator at `x = 0`.
 */
@Composable
private fun LookaheadElevationSparklineCanvas(
    profile: List<ElevationProfilePoint>,
    extrema: List<TerrainExtremaPoint>,
    severity: AlertSeverity,
    horizonMeters: Double,
    modifier: Modifier = Modifier,
) {
    val curvePath = remember { Path() }
    val fillPath = remember { Path() }
    val hazardCurvePath = remember { Path() }
    val hazardFillPath = remember { Path() }

    Canvas(modifier = modifier) {
        if (profile.size < 2 || size.width <= 0f || size.height <= 0f) return@Canvas

        val padH = 10.dp.toPx()
        val padV = 6.dp.toPx()
        val plotLeft = padH
        val plotRight = (size.width - padH).coerceAtLeast(plotLeft + 1f)
        val plotTop = padV
        val plotBottom = (size.height - padV).coerceAtLeast(plotTop + 1f)
        val plotWidth = plotRight - plotLeft
        val plotHeight = plotBottom - plotTop

        val midY = (plotTop + plotBottom) * 0.5f
        drawLine(
            color = Color(0x2DFFFFFF),
            start = Offset(plotLeft, midY),
            end = Offset(plotRight, midY),
            strokeWidth = 1.dp.toPx(),
        )
        drawLine(
            color = Color(0x2DFFFFFF),
            start = Offset(plotLeft, plotBottom),
            end = Offset(plotRight, plotBottom),
            strokeWidth = 1.dp.toPx(),
        )

        var minAlt = profile.first().altitudeMeters
        var maxAlt = minAlt
        for (pt in profile) {
            if (pt.altitudeMeters < minAlt) minAlt = pt.altitudeMeters
            if (pt.altitudeMeters > maxAlt) maxAlt = pt.altitudeMeters
        }
        val rawSpan = maxAlt - minAlt
        val effectiveSpan = max(rawSpan, 12.0)
        val lowAlt = minAlt - (effectiveSpan - rawSpan) * 0.5
        val maxDist = max(horizonMeters, profile.last().distanceOffsetMeters.coerceAtLeast(1.0))

        fun mapX(distOffset: Double): Float {
            val ratio = (distOffset / maxDist).coerceIn(0.0, 1.0).toFloat()
            return plotLeft + ratio * plotWidth
        }

        fun mapY(altitude: Double): Float {
            val ratio = ((altitude - lowAlt) / effectiveSpan).coerceIn(0.0, 1.0).toFloat()
            return plotBottom - ratio * plotHeight
        }

        curvePath.reset()
        fillPath.reset()
        hazardCurvePath.reset()
        hazardFillPath.reset()

        var inHazardRun = false
        var hazardStartX = plotLeft

        for (i in profile.indices) {
            val pt = profile[i]
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
                    hazardStartX = x
                    hazardCurvePath.moveTo(x, y)
                    hazardFillPath.moveTo(x, plotBottom)
                    hazardFillPath.lineTo(x, y)
                } else {
                    hazardCurvePath.lineTo(x, y)
                    hazardFillPath.lineTo(x, y)
                }
            } else if (inHazardRun) {
                val prevX = mapX(profile[i - 1].distanceOffsetMeters)
                hazardFillPath.lineTo(prevX, plotBottom)
                hazardFillPath.lineTo(hazardStartX, plotBottom)
                hazardFillPath.close()
                inHazardRun = false
            }
        }

        val lastX = mapX(profile.last().distanceOffsetMeters)
        fillPath.lineTo(lastX, plotBottom)
        fillPath.lineTo(plotLeft, plotBottom)
        fillPath.close()

        if (inHazardRun) {
            hazardFillPath.lineTo(lastX, plotBottom)
            hazardFillPath.lineTo(hazardStartX, plotBottom)
            hazardFillPath.close()
        }

        drawPath(path = fillPath, color = Color(0x2D38BDF8))

        if (severity != AlertSeverity.NONE) {
            val isWarning = severity == AlertSeverity.WARNING
            val hazardFillColor = if (isWarning) Color(0x55EA4335) else Color(0x55FBBC04)
            val hazardStrokeColor = if (isWarning) Color(0xFFEA4335) else Color(0xFFFBBC04)
            drawPath(path = hazardFillPath, color = hazardFillColor)
            drawPath(
                path = curvePath,
                color = Color(0xFF38BDF8),
                style = Stroke(
                    width = 2.2.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                ),
            )
            drawPath(
                path = hazardCurvePath,
                color = hazardStrokeColor,
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                ),
            )
        } else {
            drawPath(
                path = curvePath,
                color = Color(0xFF38BDF8),
                style = Stroke(
                    width = 2.2.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                ),
            )
        }

        // Crest & Dip markers
        val dotRadius = 3.8.dp.toPx()
        val strokeWidth = 1.5.dp.toPx()
        for (ext in extrema) {
            val center = Offset(mapX(ext.distanceAheadMeters), mapY(ext.position.altitude))
            val dotColor = if (ext.type == ExtremaType.CREST) {
                Color(0xFFEA4335)
            } else {
                Color(0xFFFBBC04)
            }
            drawCircle(color = dotColor, radius = dotRadius, center = center)
            drawCircle(
                color = Color.White,
                radius = dotRadius,
                center = center,
                style = Stroke(width = strokeWidth),
            )
        }

        // Vehicle dot pinned at x = 0
        val carPt = profile.first()
        val carCenter = Offset(mapX(carPt.distanceOffsetMeters), mapY(carPt.altitudeMeters))
        val carRadius = 4.5.dp.toPx()
        drawCircle(color = Color(0xFF34D399), radius = carRadius, center = carCenter)
        drawCircle(
            color = Color.White,
            radius = carRadius,
            center = carCenter,
            style = Stroke(width = strokeWidth),
        )
    }
}

@Composable
private fun HillyRoadBottomControlsCard(
    state: HillyRoadUiState,
    onTogglePlayPause: () -> Unit,
    onJumpToHills: () -> Unit,
    onScrubbingChanged: (Boolean) -> Unit,
    onSeekProgress: (Float) -> Unit,
    onSelectRoute: (HillyRoutePreset) -> Unit,
    onSelectHorizon: (Double) -> Unit,
    onSelectSpeed: (Double) -> Unit,
    onToggleCameraPreview: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    var isExpanded by rememberSaveable(isLandscape) { mutableStateOf(!isLandscape) }

    // Cap bottom sheet height to 42% of screen height so it never occludes the 3D map viewport
    val maxCardHeight = (configuration.screenHeightDp * 0.42f).dp
    val maxBodyScrollHeight = (configuration.screenHeightDp * if (isLandscape) 0.22f else 0.28f).dp

    val sliderInteractionSource = remember { MutableInteractionSource() }
    val isDraggingSlider by sliderInteractionSource.collectIsDraggedAsState()
    LaunchedEffect(isDraggingSlider) {
        onScrubbingChanged(isDraggingSlider)
    }

    Card(
        modifier = modifier.heightIn(max = maxCardHeight),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Drag Handle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(top = 6.dp, bottom = 2.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 36.dp, height = 4.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(2.dp),
                        ),
                )
            }

            // Pinned Header: Title, Telemetry, Jump to Hills Chip, Collapse Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(start = 12.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(CommonR.string.hilly_road_controls_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = String.format(
                            Locale.US,
                            stringResource(CommonR.string.hilly_current_telemetry_format),
                            state.terrainAlert.currentGradePercent,
                            state.currentDistanceMeters.roundToInt(),
                            state.totalDistanceMeters.roundToInt(),
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                AssistChip(
                    onClick = onJumpToHills,
                    label = {
                        Text(
                            text = stringResource(CommonR.string.hilly_jump_to_hills),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    },
                )

                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                        contentDescription = stringResource(
                            if (isExpanded) CommonR.string.collapse_controls else CommonR.string.expand_controls,
                        ),
                    )
                }
            }

            // Pinned Play/Pause & Progress Slider Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledIconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = stringResource(CommonR.string.play_or_pause_animation),
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Slider(
                    value = state.progressRatio.coerceIn(0.0f, 1.0f),
                    onValueChange = onSeekProgress,
                    valueRange = 0.0f..1.0f,
                    interactionSource = sliderInteractionSource,
                    modifier = Modifier.weight(1f),
                )
            }

            // Expandable Body with internal vertical scroll
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = maxBodyScrollHeight)
                        .verticalScroll(rememberScrollState())
                        .padding(start = 12.dp, end = 12.dp, bottom = 10.dp),
                ) {
                    // Route Preset Chips
                    Text(
                        text = stringResource(CommonR.string.hilly_route_preset_label),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilterChip(
                            selected = state.selectedPreset == HillyRoutePreset.ROLLER_COASTER,
                            onClick = { onSelectRoute(HillyRoutePreset.ROLLER_COASTER) },
                            label = { Text(stringResource(CommonR.string.hilly_route_roller_coaster)) },
                        )
                        FilterChip(
                            selected = state.selectedPreset == HillyRoutePreset.MOUNTAIN_PASS,
                            onClick = { onSelectRoute(HillyRoutePreset.MOUNTAIN_PASS) },
                            label = { Text(stringResource(CommonR.string.hilly_route_mountain_pass)) },
                        )
                        FilterChip(
                            selected = state.selectedPreset == HillyRoutePreset.COASTAL_DRIVE,
                            onClick = { onSelectRoute(HillyRoutePreset.COASTAL_DRIVE) },
                            label = { Text(stringResource(CommonR.string.hilly_route_coastal)) },
                        )
                    }

                    // Lookahead Horizon Chips
                    Text(
                        text = stringResource(CommonR.string.hilly_lookahead_horizon_label),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        val horizonInt = state.lookaheadHorizonMeters.roundToInt()
                        FilterChip(
                            selected = horizonInt == 250,
                            onClick = { onSelectHorizon(250.0) },
                            label = { Text(stringResource(CommonR.string.hilly_horizon_250m)) },
                        )
                        FilterChip(
                            selected = horizonInt == 500,
                            onClick = { onSelectHorizon(500.0) },
                            label = { Text(stringResource(CommonR.string.hilly_horizon_500m)) },
                        )
                        FilterChip(
                            selected = horizonInt == 750,
                            onClick = { onSelectHorizon(750.0) },
                            label = { Text(stringResource(CommonR.string.hilly_horizon_750m)) },
                        )
                    }

                    // Speed Chips
                    Text(
                        text = stringResource(CommonR.string.hilly_speed_label),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        val speedOptions = listOf(
                            0.5 to CommonR.string.speed_05x,
                            1.0 to CommonR.string.speed_1x,
                            2.0 to CommonR.string.speed_2x,
                            3.0 to CommonR.string.speed_3x,
                            5.0 to CommonR.string.speed_5x,
                        )
                        for ((mult, labelRes) in speedOptions) {
                            FilterChip(
                                selected = abs(state.speedMultiplier - mult) < 0.05,
                                onClick = { onSelectSpeed(mult) },
                                label = { Text(stringResource(labelRes)) },
                            )
                        }
                    }

                    // Crest Camera Preview Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = stringResource(CommonR.string.hilly_crest_camera_preview),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Switch(
                            checked = state.autoCameraPitchPreview,
                            onCheckedChange = onToggleCameraPreview,
                        )
                    }
                }
            }
        }
    }
}
