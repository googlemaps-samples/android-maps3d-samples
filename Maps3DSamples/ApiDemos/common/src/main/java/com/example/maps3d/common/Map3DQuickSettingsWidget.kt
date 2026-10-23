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

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.View
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
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
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.maps3dcommon.R
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.Map3DMode
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

// [START maps_3d_quick_settings_compose_widget]
/**
 * Remembers and observes the application-level [Map3DSettings] session state for a Compose screen.
 *
 * Automatically notifies [controller] via [Map3DSettingsController.onSampleEntered] with
 * [defaultMapMode] so that if the user has not explicitly overridden the map mode during the
 * current session, the sample starts in its natural default mode.
 *
 * @param defaultMapMode The sample's default [Map3DMode] (defaults to [Map3DMode.HYBRID]).
 * @param controller The [Map3DSettingsController] managing session state.
 */
@Composable
fun rememberMap3DSessionSettings(
    @Map3DMode defaultMapMode: Int = Map3DMode.HYBRID,
    controller: Map3DSettingsController = Map3DSettingsController.shared,
): State<Map3DSettings> {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            var ctx: Context? = view.context
            while (ctx is ContextWrapper) {
                if (ctx is Activity) {
                    val window = ctx.window
                    val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                    insetsController.systemBarsBehavior =
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    insetsController.hide(WindowInsetsCompat.Type.systemBars())
                    break
                }
                ctx = ctx.baseContext
            }
        }
        LaunchedEffect(Unit) {
            Map3DSettings.findMap3DView(view.rootView)?.let { mapView ->
                Map3DSettings.applyStatusBarInsetsToMapView(mapView)
            }
        }
    }
    remember(defaultMapMode, controller) {
        controller.applySampleDefaultMapMode(defaultMapMode)
        true
    }
    return controller.settings.collectAsState()
}

/**
 * Material3 theme wrapper that selects [darkColorScheme] or [lightColorScheme] based on
 * the current [Map3DSettings.colorScheme] in [controller].
 */
@Composable
fun Map3DSessionTheme(
    controller: Map3DSettingsController = Map3DSettingsController.shared,
    content: @Composable () -> Unit,
) {
    val settings by controller.settings.collectAsState()
    val useDarkTheme = when (settings.colorScheme) {
        Map3DColorScheme.LIGHT -> false
        Map3DColorScheme.DARK -> true
        Map3DColorScheme.FOLLOW_SYSTEM -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (useDarkTheme) darkColorScheme() else lightColorScheme(),
        content = content,
    )
}

/**
 * A self-contained Jetpack Compose quick settings widget for 3D map layouts.
 *
 * Renders a compact [FloatingActionButton] that opens a modal [AlertDialog] to select
 * Map Mode (Satellite, Hybrid, Roadmap), Color Scheme (Follow System, Light, Dark),
 * toggle camera UI controls, and reset to defaults.
 *
 * @param modifier Layout modifier applied to the button (e.g. alignment inside a Box).
 * @param settingsController The [Map3DSettingsController] backing the widget (defaults to [Map3DSettingsController.shared]).
 * @param sampleDefaultMapMode The hosting sample's default [Map3DMode] restored on Reset.
 * @param googleMap3D Optional live [GoogleMap3D] instance to automatically apply settings changes.
 * @param uiControlsOverride Optional local [Map3DUiControls] override for screens that manage SDK controls directly.
 * @param onSettingsChanged Optional callback invoked when map settings are modified.
 */
@Composable
fun Map3DQuickSettingsWidget(
    modifier: Modifier = Modifier,
    settingsController: Map3DSettingsController = Map3DSettingsController.shared,
    @Map3DMode sampleDefaultMapMode: Int = Map3DMode.HYBRID,
    googleMap3D: GoogleMap3D? = null,
    uiControlsOverride: Map3DUiControls? = null,
    onSettingsChanged: (Map3DSettings) -> Unit = {},
) {
    val settings by settingsController.settings.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    val view = LocalView.current
    val density = LocalDensity.current
    val effectiveUiControls = uiControlsOverride ?: settings.uiControls

    LaunchedEffect(googleMap3D, settings, uiControlsOverride) {
        if (googleMap3D != null) {
            val effectiveSettings = if (uiControlsOverride != null) {
                settings.copy(uiControls = uiControlsOverride)
            } else {
                settings
            }
            effectiveSettings.applyTo(googleMap3D)
            Map3DSettings.findMap3DView(view.rootView)?.let { mapView ->
                Map3DSettings.applyStatusBarInsetsToMapView(mapView)
                mapView.post { Map3DSettings.applyStatusBarInsetsToMapView(mapView) }
            }
        }
    }

    LaunchedEffect(googleMap3D, uiControlsOverride) {
        if (googleMap3D != null) {
            delay(350.milliseconds)
            val currentSettings = settingsController.settings.value
            val effectiveSettings = if (uiControlsOverride != null) {
                currentSettings.copy(uiControls = uiControlsOverride)
            } else {
                currentSettings
            }
            effectiveSettings.applyTo(googleMap3D)
            Map3DSettings.findMap3DView(view.rootView)?.let { mapView ->
                Map3DSettings.applyStatusBarInsetsToMapView(mapView)
            }
        }
    }

    val visibleStatusBarsTopPx = WindowInsets.statusBars.getTop(density)
    var overlapPx by remember { mutableIntStateOf(0) }
    if (!view.isInEditMode) {
        DisposableEffect(view, googleMap3D, visibleStatusBarsTopPx) {
            val targetView = Map3DSettings.findMap3DView(view.rootView) ?: view
            overlapPx = Map3DSettings.resolveStatusBarOverlapPx(targetView)
            val layoutListener = View.OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                overlapPx = Map3DSettings.resolveStatusBarOverlapPx(targetView)
            }
            targetView.addOnLayoutChangeListener(layoutListener)
            onDispose {
                targetView.removeOnLayoutChangeListener(layoutListener)
            }
        }
    }
    val missingStatusBarDp = with(density) {
        (overlapPx - visibleStatusBarsTopPx).coerceAtLeast(0).toDp()
    }
    val extraTopPadding =
        (effectiveUiControls.calculateFabMarginDp() - 8).coerceAtLeast(0).dp + missingStatusBarDp

    Map3DSessionTheme(controller = settingsController) {
        FloatingActionButton(
            onClick = { showDialog = true },
            modifier = modifier
                .padding(top = extraTopPadding, end = 8.dp)
                .size(40.dp),
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
                Column(
                    Modifier
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
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
                                    .height(38.dp)
                                    .selectable(
                                        selected = (settings.mapMode == mode),
                                        onClick = {
                                            val updated = settingsController.setMapMode(mode)
                                            onSettingsChanged(updated)
                                        },
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

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

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
                                    .height(38.dp)
                                    .selectable(
                                        selected = (settings.colorScheme == scheme),
                                        onClick = {
                                            val updated = settingsController.setColorScheme(scheme)
                                            onSettingsChanged(updated)
                                        },
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

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // UI Controls Section
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp),
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
                                val updated = settingsController.setAllUiControls(checked)
                                onSettingsChanged(updated)
                            },
                        )
                    }

                    val controlItems = listOf(
                        Triple(stringResource(id = R.string.map3d_control_zoom), settings.uiControls.zoom) { checked: Boolean ->
                            val updated = settingsController.updateUiControl(zoom = checked)
                            onSettingsChanged(updated)
                        },
                        Triple(stringResource(id = R.string.map3d_control_tilt), settings.uiControls.tilt) { checked: Boolean ->
                            val updated = settingsController.updateUiControl(tilt = checked)
                            onSettingsChanged(updated)
                        },
                        Triple(stringResource(id = R.string.map3d_control_rotate), settings.uiControls.rotate) { checked: Boolean ->
                            val updated = settingsController.updateUiControl(rotate = checked)
                            onSettingsChanged(updated)
                        },
                        Triple(stringResource(id = R.string.map3d_control_pan_v), settings.uiControls.panVertical) { checked: Boolean ->
                            val updated = settingsController.updateUiControl(panVertical = checked)
                            onSettingsChanged(updated)
                        },
                        Triple(stringResource(id = R.string.map3d_control_pan_h), settings.uiControls.panHorizontal) { checked: Boolean ->
                            val updated = settingsController.updateUiControl(panHorizontal = checked)
                            onSettingsChanged(updated)
                        },
                        Triple(stringResource(id = R.string.map3d_control_compass), settings.uiControls.compass) { checked: Boolean ->
                            val updated = settingsController.updateUiControl(compass = checked)
                            onSettingsChanged(updated)
                        },
                    )

                    controlItems.forEach { (label, isChecked, onToggle) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .toggleable(
                                    value = isChecked,
                                    onValueChange = onToggle,
                                    role = Role.Checkbox,
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = null,
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
            dismissButton = {
                TextButton(
                    onClick = {
                        val updated = settingsController.reset(sampleDefaultMapMode)
                        onSettingsChanged(updated)
                    },
                ) {
                    Text(text = stringResource(id = R.string.map3d_settings_reset))
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
}
// [END maps_3d_quick_settings_compose_widget]
