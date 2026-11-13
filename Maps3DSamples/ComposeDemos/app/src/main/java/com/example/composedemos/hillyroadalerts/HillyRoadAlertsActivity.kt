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
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.maps3d.common.AlertSeverity
import com.example.maps3d.common.ExtremaType
import com.example.maps3d.common.HillyRoadAlertViewModel
import com.example.maps3d.common.Map3DQuickSettingsWidget
import com.example.maps3d.common.Map3DSessionTheme
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
                val extremumId = extremum.id
                val fallbackDist = extremum.distanceAheadMeters
                val elevMeters = extremum.position.altitude.roundToInt()
                MarkerConfig(
                    key = extremumId,
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
                            val liveDist = viewModel.currentState.terrainAlert.upcomingExtrema
                                .firstOrNull { it.id == extremumId }
                                ?.distanceAheadMeters
                                ?: fallbackDist
                            Toast.makeText(
                                context,
                                String.format(
                                    Locale.US,
                                    toastFormat,
                                    labelText,
                                    liveDist.roundToInt(),
                                    elevMeters,
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
                viewModel.onMapReady()
            },
            onMapSteady = {
                viewModel.onMapReady()
            },
        )

        // Top Header Bar + Early Warning HUD Banner + Live Elevation Sparkline
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75f))
                    .statusBarsPadding()
                    .padding(start = 16.dp, end = 64.dp, top = 8.dp, bottom = 8.dp),
            ) {
                Text(
                    text = stringResource(CommonR.string.feature_title_hilly_road_alerts),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            EarlyWarningHudBanner(
                alert = state.terrainAlert,
                lookaheadHorizonMeters = state.lookaheadHorizonMeters,
                modifier = Modifier
                    .padding(start = 12.dp, top = 8.dp, end = 64.dp)
                    .fillMaxWidth(),
            )
        }

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
