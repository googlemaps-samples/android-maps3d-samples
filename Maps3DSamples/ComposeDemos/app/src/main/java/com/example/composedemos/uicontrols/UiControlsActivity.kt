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

package com.example.composedemos.uicontrols

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.maps3d.common.Map3DQuickSettingsWidget
import com.example.maps3d.common.Map3DSessionTheme
import com.example.maps3d.common.Map3DSettings
import com.example.maps3d.common.Map3DUiControls
import com.example.maps3d.common.enableImmersiveMode
import com.example.maps3d.common.rememberMap3DSessionSettings
import com.example.maps3dcommon.R
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.UiControlSettings.ControlOrientation
import com.google.android.gms.maps3d.UiControlSettings.ControlPlacement
import com.google.android.gms.maps3d.model.CompassControl
import com.google.android.gms.maps3d.model.Map3DControl
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.PanHorizontalControl
import com.google.android.gms.maps3d.model.PanVerticalControl
import com.google.android.gms.maps3d.model.RotateControl
import com.google.android.gms.maps3d.model.TiltControl
import com.google.android.gms.maps3d.model.ZoomControl
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.maps.android.compose3d.GoogleMap3D

class UiControlsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        enableImmersiveMode()
        setContent {
            Map3DSessionTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    UiControlsScreen()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        enableImmersiveMode()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            enableImmersiveMode()
        }
    }

    companion object {
        // San Francisco Financial District
        val SF_LOCATION = LatLng(37.7915, -122.4010)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UiControlsScreen() {
    var googleMap3D by remember { mutableStateOf<GoogleMap3D?>(null) }
    var isCollapsed by remember { mutableStateOf(false) }
    val settings by rememberMap3DSessionSettings(defaultMapMode = Map3DMode.HYBRID)
    val view = LocalView.current

    // Local state for UI controls showcase (directly controls SDK without polluting session settings)
    var zoomEnabled by remember { mutableStateOf(true) }
    var tiltEnabled by remember { mutableStateOf(true) }
    var rotateEnabled by remember { mutableStateOf(true) }
    var panVerticalEnabled by remember { mutableStateOf(true) }
    var panHorizontalEnabled by remember { mutableStateOf(true) }
    var compassEnabled by remember { mutableStateOf(true) }
    var controlsPosition by remember { mutableIntStateOf(ControlPlacement.TRAILING) }

    val initialLocation = UiControlsActivity.SF_LOCATION
    val currentCameraState by remember {
        mutableStateOf(
            camera {
                center = latLngAltitude {
                    latitude = initialLocation.latitude
                    longitude = initialLocation.longitude
                    altitude = 250.0
                }
                heading = 45.0
                tilt = 65.0
                range = 800.0
            },
        )
    }

    // Apply UI controls directly to GoogleMap3D using SDK APIs
    val mapKey = System.identityHashCode(googleMap3D)
    LaunchedEffect(
        mapKey,
        settings,
        zoomEnabled,
        tiltEnabled,
        rotateEnabled,
        panVerticalEnabled,
        panHorizontalEnabled,
        compassEnabled,
        controlsPosition,
    ) {
        val map = googleMap3D ?: return@LaunchedEffect
        val uiSettings = map.getUiControlSettings() ?: return@LaunchedEffect

        val primaryControls = mutableListOf<Map3DControl>()
        if (zoomEnabled) primaryControls.add(ZoomControl(ControlOrientation.VERTICAL))
        if (tiltEnabled) primaryControls.add(TiltControl(ControlOrientation.VERTICAL))
        if (rotateEnabled) primaryControls.add(RotateControl(ControlOrientation.VERTICAL))

        val secondaryControls = mutableListOf<Map3DControl>()
        if (panVerticalEnabled) secondaryControls.add(PanVerticalControl(ControlOrientation.VERTICAL))
        if (panHorizontalEnabled) secondaryControls.add(PanHorizontalControl(ControlOrientation.VERTICAL))
        if (compassEnabled) secondaryControls.add(CompassControl())

        val isLeading = controlsPosition == ControlPlacement.LEADING
        val primaryPlacement = if (isLeading) ControlPlacement.LEADING else ControlPlacement.TRAILING
        val secondaryPlacement = if (isLeading) ControlPlacement.TRAILING else ControlPlacement.LEADING

        uiSettings.setUiControlsPlacement(primaryControls, primaryPlacement)
        uiSettings.setUiControlsPlacement(secondaryControls, secondaryPlacement)
        Map3DSettings.findMap3DView(view.rootView)?.let { mapView ->
            Map3DSettings.applyStatusBarInsetsToMapView(mapView)
            mapView.post {
                Map3DSettings.applyStatusBarInsetsToMapView(mapView)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap3D(
            camera = currentCameraState,
            mapMode = settings.mapMode,
            modifier = Modifier.fillMaxSize(),
            onMapReady = { map ->
                googleMap3D = map
            },
        )

        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isCollapsed = !isCollapsed },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.ui_controls_panel_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )

                    IconButton(onClick = { isCollapsed = !isCollapsed }) {
                        Icon(
                            painter = painterResource(
                                id = if (isCollapsed) {
                                    R.drawable.expand_less_24px
                                } else {
                                    R.drawable.expand_more_24px
                                },
                            ),
                            contentDescription = stringResource(
                                id = if (isCollapsed) {
                                    R.string.expand_controls
                                } else {
                                    R.string.collapse_controls
                                },
                            ),
                        )
                    }
                }

                AnimatedVisibility(visible = !isCollapsed) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(R.string.ui_controls_visibility_label),
                                style = MaterialTheme.typography.labelMedium,
                            )

                            Row {
                                TextButton(
                                    onClick = {
                                        zoomEnabled = true
                                        tiltEnabled = true
                                        rotateEnabled = true
                                        panVerticalEnabled = true
                                        panHorizontalEnabled = true
                                        compassEnabled = true
                                    },
                                ) {
                                    Text(text = stringResource(R.string.ui_controls_show_all))
                                }

                                TextButton(
                                    onClick = {
                                        zoomEnabled = false
                                        tiltEnabled = false
                                        rotateEnabled = false
                                        panVerticalEnabled = false
                                        panHorizontalEnabled = false
                                        compassEnabled = false
                                    },
                                ) {
                                    Text(text = stringResource(R.string.ui_controls_hide_all))
                                }
                            }
                        }

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            FilterChip(
                                selected = zoomEnabled,
                                onClick = { zoomEnabled = !zoomEnabled },
                                label = { Text(stringResource(R.string.ui_control_chip_zoom)) },
                            )
                            FilterChip(
                                selected = tiltEnabled,
                                onClick = { tiltEnabled = !tiltEnabled },
                                label = { Text(stringResource(R.string.ui_control_chip_tilt)) },
                            )
                            FilterChip(
                                selected = rotateEnabled,
                                onClick = { rotateEnabled = !rotateEnabled },
                                label = { Text(stringResource(R.string.ui_control_chip_rotate)) },
                            )
                            FilterChip(
                                selected = panVerticalEnabled,
                                onClick = { panVerticalEnabled = !panVerticalEnabled },
                                label = { Text(stringResource(R.string.ui_control_chip_pan_vertical)) },
                            )
                            FilterChip(
                                selected = panHorizontalEnabled,
                                onClick = { panHorizontalEnabled = !panHorizontalEnabled },
                                label = { Text(stringResource(R.string.ui_control_chip_pan_horizontal)) },
                            )
                            FilterChip(
                                selected = compassEnabled,
                                onClick = { compassEnabled = !compassEnabled },
                                label = { Text(stringResource(R.string.ui_control_chip_compass)) },
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = stringResource(R.string.ui_controls_placement_label),
                            style = MaterialTheme.typography.labelMedium,
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val placements = listOf(
                                ControlPlacement.TRAILING to stringResource(R.string.ui_controls_placement_trailing),
                                ControlPlacement.LEADING to stringResource(R.string.ui_controls_placement_leading),
                            )

                            placements.forEach { (positionValue, positionLabel) ->
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .selectable(
                                            selected = (controlsPosition == positionValue),
                                            onClick = {
                                                controlsPosition = positionValue
                                            },
                                            role = Role.RadioButton,
                                        )
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    RadioButton(
                                        selected = (controlsPosition == positionValue),
                                        onClick = null,
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = positionLabel,
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        val localUiControls = remember(
            zoomEnabled,
            tiltEnabled,
            rotateEnabled,
            compassEnabled,
            panVerticalEnabled,
            panHorizontalEnabled,
            controlsPosition,
        ) {
            Map3DUiControls(
                zoom = zoomEnabled,
                tilt = tiltEnabled,
                rotate = rotateEnabled,
                compass = compassEnabled,
                panVertical = panVerticalEnabled,
                panHorizontal = panHorizontalEnabled,
                controlsPosition = controlsPosition,
            )
        }
        Map3DQuickSettingsWidget(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 8.dp, end = 8.dp),
            sampleDefaultMapMode = Map3DMode.HYBRID,
            googleMap3D = googleMap3D,
            uiControlsOverride = localUiControls,
        )
    }
}
