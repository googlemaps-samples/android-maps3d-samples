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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.maps3d.common.Map3DQuickSettingsWidget
import com.example.maps3d.common.Map3DSessionTheme
import com.example.maps3d.common.Map3DSettingsController
import com.example.maps3d.common.UiControlsPlacementPosition
import com.example.maps3d.common.rememberMap3DSessionSettings
import com.example.maps3dcommon.R
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.maps.android.compose3d.GoogleMap3D

class UiControlsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
    val settings by rememberMap3DSessionSettings(defaultMapMode = Map3DMode.ROADMAP)
    val uiControls = settings.uiControls

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
                                        Map3DSettingsController.shared.setAllUiControls(true)
                                    },
                                ) {
                                    Text(text = stringResource(R.string.ui_controls_show_all))
                                }

                                TextButton(
                                    onClick = {
                                        Map3DSettingsController.shared.setAllUiControls(false)
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
                                selected = uiControls.zoom,
                                onClick = {
                                    Map3DSettingsController.shared.updateUiControl(zoom = !uiControls.zoom)
                                },
                                label = { Text(stringResource(R.string.ui_control_chip_zoom)) },
                            )
                            FilterChip(
                                selected = uiControls.tilt,
                                onClick = {
                                    Map3DSettingsController.shared.updateUiControl(tilt = !uiControls.tilt)
                                },
                                label = { Text(stringResource(R.string.ui_control_chip_tilt)) },
                            )
                            FilterChip(
                                selected = uiControls.rotate,
                                onClick = {
                                    Map3DSettingsController.shared.updateUiControl(rotate = !uiControls.rotate)
                                },
                                label = { Text(stringResource(R.string.ui_control_chip_rotate)) },
                            )
                            FilterChip(
                                selected = uiControls.panVertical,
                                onClick = {
                                    Map3DSettingsController.shared.updateUiControl(panVertical = !uiControls.panVertical)
                                },
                                label = { Text(stringResource(R.string.ui_control_chip_pan_vertical)) },
                            )
                            FilterChip(
                                selected = uiControls.panHorizontal,
                                onClick = {
                                    Map3DSettingsController.shared.updateUiControl(panHorizontal = !uiControls.panHorizontal)
                                },
                                label = { Text(stringResource(R.string.ui_control_chip_pan_horizontal)) },
                            )
                            FilterChip(
                                selected = uiControls.compass,
                                onClick = {
                                    Map3DSettingsController.shared.updateUiControl(compass = !uiControls.compass)
                                },
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
                                UiControlsPlacementPosition.TRAILING to stringResource(R.string.ui_controls_placement_trailing),
                                UiControlsPlacementPosition.LEADING to stringResource(R.string.ui_controls_placement_leading),
                            )

                            placements.forEach { (positionValue, positionLabel) ->
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .selectable(
                                            selected = (uiControls.controlsPosition == positionValue),
                                            onClick = {
                                                Map3DSettingsController.shared.setControlsPosition(positionValue)
                                            },
                                            role = Role.RadioButton,
                                        )
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    RadioButton(
                                        selected = (uiControls.controlsPosition == positionValue),
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

        Map3DQuickSettingsWidget(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 8.dp, end = 8.dp),
            sampleDefaultMapMode = Map3DMode.ROADMAP,
            googleMap3D = googleMap3D,
        )
    }
}
