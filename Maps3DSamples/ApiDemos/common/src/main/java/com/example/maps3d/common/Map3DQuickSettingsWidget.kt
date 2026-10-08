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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
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
import androidx.compose.ui.unit.dp
import com.example.maps3dcommon.R
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.Map3DMode

// [START maps_3d_quick_settings_compose_widget]
/**
 * A self-contained Jetpack Compose quick settings widget for 3D map layouts.
 *
 * Renders a compact [FloatingActionButton] that opens a modal [AlertDialog] to select
 * Map Mode (Satellite, Hybrid, Roadmap) and Color Scheme (Follow System, Light, Dark).
 *
 * @param modifier Layout modifier applied to the button (e.g. alignment inside a Box).
 * @param googleMap3D Optional live [GoogleMap3D] instance to apply settings directly.
 * @param initialSettings The initial [Map3DSettings] configuration.
 * @param onSettingsChanged Callback invoked when map settings are modified.
 */
@Composable
fun Map3DQuickSettingsWidget(
    modifier: Modifier = Modifier,
    googleMap3D: GoogleMap3D? = null,
    initialSettings: Map3DSettings = Map3DSettings(),
    onSettingsChanged: (Map3DSettings) -> Unit = {},
) {
    var settings by remember { mutableStateOf(initialSettings) }
    var showDialog by remember { mutableStateOf(false) }

    fun updateSettings(newSettings: Map3DSettings) {
        settings = newSettings
        googleMap3D?.let { newSettings.applyTo(it) }
        onSettingsChanged(newSettings)
    }

    FloatingActionButton(
        onClick = { showDialog = true },
        modifier = modifier.size(40.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.settings_24px),
            contentDescription = stringResource(id = R.string.map3d_settings_button_description),
            modifier = Modifier.size(20.dp),
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = stringResource(id = R.string.map3d_settings_title),
                    style = MaterialTheme.typography.titleMedium,
                )
            },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    // Map Mode Section
                    Text(
                        text = stringResource(id = R.string.map3d_mode_section),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )

                    Column(Modifier.selectableGroup()) {
                        val modes = listOf(
                            Map3DMode.SATELLITE to stringResource(id = R.string.map3d_mode_satellite),
                            Map3DMode.HYBRID to stringResource(id = R.string.map3d_mode_hybrid),
                            Map3DMode.ROADMAP to stringResource(id = R.string.map3d_mode_roadmap),
                        )
                        modes.forEach { (mode, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                                    .selectable(
                                        selected = (settings.mapMode == mode),
                                        onClick = { updateSettings(settings.copy(mapMode = mode)) },
                                        role = Role.RadioButton,
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = (settings.mapMode == mode),
                                    onClick = null,
                                )
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(start = 8.dp),
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Color Scheme Section
                    Text(
                        text = stringResource(id = R.string.map3d_color_scheme_section),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )

                    Column(Modifier.selectableGroup()) {
                        val schemes = listOf(
                            Map3DColorScheme.FOLLOW_SYSTEM to stringResource(id = R.string.map3d_color_scheme_system),
                            Map3DColorScheme.LIGHT to stringResource(id = R.string.map3d_color_scheme_light),
                            Map3DColorScheme.DARK to stringResource(id = R.string.map3d_color_scheme_dark),
                        )
                        schemes.forEach { (scheme, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                                    .selectable(
                                        selected = (settings.colorScheme == scheme),
                                        onClick = { updateSettings(settings.copy(colorScheme = scheme)) },
                                        role = Role.RadioButton,
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = (settings.colorScheme == scheme),
                                    onClick = null,
                                )
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(start = 8.dp),
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // UI Controls Section
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = stringResource(id = R.string.map3d_controls_section),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Switch(
                            checked = settings.uiControls.allEnabled,
                            onCheckedChange = { checked ->
                                updateSettings(settings.copy(uiControls = settings.uiControls.withAll(checked)))
                            },
                        )
                    }

                    val controlItems = listOf(
                        Triple(stringResource(id = R.string.map3d_control_zoom), settings.uiControls.zoom) { checked: Boolean ->
                            updateSettings(settings.copy(uiControls = settings.uiControls.copy(zoom = checked)))
                        },
                        Triple(stringResource(id = R.string.map3d_control_tilt), settings.uiControls.tilt) { checked: Boolean ->
                            updateSettings(settings.copy(uiControls = settings.uiControls.copy(tilt = checked)))
                        },
                        Triple(stringResource(id = R.string.map3d_control_rotate), settings.uiControls.rotate) { checked: Boolean ->
                            updateSettings(settings.copy(uiControls = settings.uiControls.copy(rotate = checked)))
                        },
                        Triple(stringResource(id = R.string.map3d_control_compass), settings.uiControls.compass) { checked: Boolean ->
                            updateSettings(settings.copy(uiControls = settings.uiControls.copy(compass = checked)))
                        },
                        Triple(stringResource(id = R.string.map3d_control_pan_v), settings.uiControls.panVertical) { checked: Boolean ->
                            updateSettings(settings.copy(uiControls = settings.uiControls.copy(panVertical = checked)))
                        },
                        Triple(stringResource(id = R.string.map3d_control_pan_h), settings.uiControls.panHorizontal) { checked: Boolean ->
                            updateSettings(settings.copy(uiControls = settings.uiControls.copy(panHorizontal = checked)))
                        },
                    )

                    controlItems.forEach { (label, isChecked, onToggle) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = onToggle,
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(text = stringResource(id = R.string.map3d_settings_done))
                }
            },
        )
    }
}
// [END maps_3d_quick_settings_compose_widget]
