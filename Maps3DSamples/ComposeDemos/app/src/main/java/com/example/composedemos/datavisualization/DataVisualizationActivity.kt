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

package com.example.composedemos.datavisualization

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.maps3d.common.Map3DQuickSettingsWidget
import com.example.maps3d.common.Map3DSessionTheme
import com.example.maps3d.common.Map3DSettingsController
import com.example.maps3d.common.rememberMap3DSessionSettings
import com.example.maps3d.common.toValidCamera
import com.example.maps3dcommon.R
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.Polygon
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.flyToOptions
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.gms.maps3d.model.polygonOptions
import com.google.maps.android.compose3d.GoogleMap3D
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.time.Duration.Companion.milliseconds
import android.graphics.Color as AndroidColor

class DataVisualizationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            Map3DSessionTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    DataVisualizationScreen()
                }
            }
        }
    }
}

@Composable
fun DataVisualizationScreen() {
    val context = LocalContext.current
    var isMapSteady by remember { mutableStateOf(false) }
    var googleMap3DInstance by remember { mutableStateOf<GoogleMap3D?>(null) }
    val settings by rememberMap3DSessionSettings(defaultMapMode = Map3DMode.ROADMAP)
    val effectiveMapMode = settings.mapMode
    var currentFloodElevation by remember { mutableDoubleStateOf(10.0) }
    var isSimulating by remember { mutableStateOf(false) }
    var activePolygon by remember { mutableStateOf<Polygon?>(null) }

    val initialCamera: Camera = remember {
        val initialMode = Map3DSettingsController.shared.getSettings().mapMode
        camera {
            center = latLngAltitude {
                latitude = SF_FLOOD_CENTER.latitude
                longitude = SF_FLOOD_CENTER.longitude
                altitude = 0.0
            }
            heading = 35.0
            tilt = if (initialMode == Map3DMode.ROADMAP) TILT_2D_ROADMAP else TILT_3D_HYBRID
            range = 2200.0
        }.toValidCamera()
    }

    val flyToModePerspective: (Int) -> Unit = { targetMode ->
        val targetTilt = if (targetMode == Map3DMode.ROADMAP) TILT_2D_ROADMAP else TILT_3D_HYBRID
        val updatedCamera = camera {
            center = latLngAltitude {
                latitude = SF_FLOOD_CENTER.latitude
                longitude = SF_FLOOD_CENTER.longitude
                altitude = 0.0
            }
            heading = 35.0
            tilt = targetTilt
            roll = 0.0
            range = 2200.0
        }.toValidCamera()
        googleMap3DInstance?.flyCameraTo(
            flyToOptions {
                endCamera = updatedCamera
                durationInMillis = 1_000L
            },
        )
    }

    // Coroutine loop for auto flood simulation animation
    LaunchedEffect(isSimulating) {
        if (isSimulating) {
            if (currentFloodElevation >= 100.0) {
                currentFloodElevation = 0.0
            }
            while (isActive && isSimulating) {
                var newElevation = currentFloodElevation + 0.2
                newElevation = Math.round(newElevation * 10.0) / 10.0
                if (newElevation >= 100.0) {
                    currentFloodElevation = 100.0
                    isSimulating = false
                    break
                }
                currentFloodElevation = newElevation
                delay(20.milliseconds)
            }
        }
    }

    val latestFloodToastMessage by rememberUpdatedState(
        stringResource(R.string.flood_toast_format, currentFloodElevation),
    )

    // Synchronize 3D extruded flood polygon whenever flood elevation or map instance updates
    LaunchedEffect(currentFloodElevation, googleMap3DInstance) {
        val map = googleMap3DInstance ?: return@LaunchedEffect
        val path = floodZoneCoords.map { (lat, lng) ->
            latLngAltitude {
                latitude = lat
                longitude = lng
                altitude = currentFloodElevation
            }
        }

        val options = polygonOptions {
            id = POLYGON_ID
            this.path = path
            fillColor = AndroidColor.argb(140, 230, 40, 40)
            strokeColor = AndroidColor.argb(255, 180, 0, 0)
            strokeWidth = 2.5
            altitudeMode = AltitudeMode.ABSOLUTE
            extruded = true
            drawsOccludedSegments = true
            geodesic = false
        }

        activePolygon = map.addPolygon(options).apply {
            setClickListener {
                Toast.makeText(
                    context,
                    latestFloodToastMessage,
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = if (isMapSteady) "MapSteady" else "MapLoading" },
    ) {
        GoogleMap3D(
            camera = initialCamera,
            mapMode = effectiveMapMode,
            modifier = Modifier.fillMaxSize(),
            onMapReady = { map ->
                googleMap3DInstance = map
            },
            onMapSteady = {
                isMapSteady = true
            },
        )

        // Translucent Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75f))
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                text = stringResource(R.string.feature_title_data_visualization),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        // Bottom Interactive Control Panel
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(12.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val feet = currentFloodElevation * 3.28084
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.flood_elevation_format, currentFloodElevation, feet),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )

                    FilledTonalButton(
                        onClick = {
                            val nextMode = if (effectiveMapMode == Map3DMode.ROADMAP) {
                                Map3DMode.HYBRID
                            } else {
                                Map3DMode.ROADMAP
                            }
                            Map3DSettingsController.shared.setMapMode(nextMode)
                            googleMap3DInstance?.setMapMode(nextMode)
                            flyToModePerspective(nextMode)
                        },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = stringResource(
                                if (effectiveMapMode == Map3DMode.ROADMAP) {
                                    R.string.store_locator_switch_to_3d
                                } else {
                                    R.string.store_locator_switch_to_2d
                                },
                            ),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }

                val (badgeText, badgeColor, badgeBg) = when {
                    currentFloodElevation <= 2.0 -> Triple("🌊 Baseline Tide", Color(0xFF008800), Color(0x2000AA00))
                    currentFloodElevation <= 8.0 -> Triple("⚠️ Minor Inundation", Color(0xFFBB7700), Color(0x20FFAA00))
                    currentFloodElevation <= 20.0 -> Triple("🌊 Moderate Flooding", Color(0xFF0077CC), Color(0x200088FF))
                    currentFloodElevation <= 35.0 -> Triple("🚨 Storm Surge (Cat 3)", Color(0xFFDD4400), Color(0x25FF5500))
                    else -> Triple("⛔ Extreme Inundation", Color(0xFFCC0000), Color(0x25FF0000))
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeBg,
                ) {
                    Text(
                        text = badgeText,
                        color = badgeColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Slider(
                        value = currentFloodElevation.toFloat(),
                        onValueChange = {
                            isSimulating = false
                            currentFloodElevation = Math.round(it.toDouble() * 10.0) / 10.0
                        },
                        valueRange = 0f..100f,
                        modifier = Modifier.weight(1f),
                    )

                    Button(
                        onClick = {
                            isSimulating = !isSimulating
                        },
                    ) {
                        Text(if (isSimulating) "⏹ Stop" else "▶ Simulate")
                    }
                }
            }
        }

        // Quick Settings Overlay
        Map3DQuickSettingsWidget(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 8.dp, end = 8.dp),
            sampleDefaultMapMode = Map3DMode.ROADMAP,
            googleMap3D = googleMap3DInstance,
            onSettingsChanged = { updatedSettings ->
                flyToModePerspective(updatedSettings.mapMode)
            },
        )
    }
}

private const val POLYGON_ID = "flood_zone_polygon"
private const val TILT_2D_ROADMAP = 0.0
private const val TILT_3D_HYBRID = 64.0

private val SF_FLOOD_CENTER = LatLng(37.8025, -122.4030)

private val floodZoneCoords = listOf(
    Pair(37.805156, -122.403256),
    Pair(37.803370, -122.401287),
    Pair(37.799222, -122.405080),
    Pair(37.797500, -122.408000),
    Pair(37.801000, -122.411000),
    Pair(37.805156, -122.403256),
)
