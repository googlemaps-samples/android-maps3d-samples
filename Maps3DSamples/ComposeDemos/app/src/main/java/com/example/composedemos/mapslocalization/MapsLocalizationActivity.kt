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

package com.example.composedemos.mapslocalization

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.maps3d.common.LocalizationState
import com.example.maps3d.common.MapLocalePreset
import com.example.maps3d.common.MapsLocalizationViewModel
import com.example.maps3dcommon.R
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.maps.android.compose3d.GoogleMap3D

/**
 * Jetpack Compose implementation of the 3D Maps Localization sample.
 *
 * Uses the shared [MapsLocalizationViewModel] from `:Maps3DSamples:ApiDemos:common` and
 * dynamically updates the map language and region in-place on the live [GoogleMap3D] instance
 * via [GoogleMap3D.setLocale] without recreating the map view or reloading the scene.
 */
class MapsLocalizationActivity : ComponentActivity() {

    private val viewModel: MapsLocalizationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                MapsLocalizationScreen(
                    state = uiState,
                    onSelectPreset = { chosenPreset ->
                        if (chosenPreset != uiState.selectedPreset) {
                            viewModel.selectPreset(chosenPreset)
                            Toast.makeText(
                                this@MapsLocalizationActivity,
                                getString(
                                    R.string.localization_applied_format,
                                    chosenPreset.displayName,
                                    viewModel.currentState.localeBadgeText,
                                ),
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    },
                    onSelectMapMode = viewModel::setMapMode,
                    onNavigateUp = ::finish,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapsLocalizationScreen(
    state: LocalizationState,
    onSelectPreset: (MapLocalePreset) -> Unit,
    onSelectMapMode: (Int) -> Unit,
    onNavigateUp: () -> Unit,
) {
    var googleMap3DInstance by remember { mutableStateOf<GoogleMap3D?>(null) }
    var isPanelExpanded by remember { mutableStateOf(true) }

    LaunchedEffect(googleMap3DInstance, state.selectedPreset) {
        googleMap3DInstance?.setLocale(state.toLocaleOptions())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.feature_title_maps_localization)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                ),
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding()),
        ) {
            GoogleMap3D(
                camera = state.camera,
                mapMode = state.mapMode,
                modifier = Modifier.fillMaxSize(),
                onMapReady = { map ->
                    googleMap3DInstance = map
                    map.setLocale(state.toLocaleOptions())
                },
            )

            LocalizationControlCard(
                state = state,
                isExpanded = isPanelExpanded,
                onToggleExpanded = { isPanelExpanded = !isPanelExpanded },
                onSelectPreset = onSelectPreset,
                onSelectMapMode = onSelectMapMode,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(12.dp),
            )
        }
    }
}

@Composable
private fun LocalizationControlCard(
    state: LocalizationState,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onSelectPreset: (MapLocalePreset) -> Unit,
    onSelectMapMode: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showLanguageDialog by remember { mutableStateOf(false) }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(text = stringResource(R.string.change_language_button)) },
            text = {
                Column {
                    MapLocalePreset.entries.forEach { preset ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showLanguageDialog = false
                                    onSelectPreset(preset)
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = state.selectedPreset == preset,
                                onClick = {
                                    showLanguageDialog = false
                                    onSelectPreset(preset)
                                },
                            )
                            Text(
                                text = "${preset.displayName} — ${preset.nativeLabel}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(text = stringResource(android.R.string.cancel))
                }
            },
        )
    }

    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpanded)
                    .padding(start = 16.dp, top = 8.dp, end = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.localization_controls_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = onToggleExpanded,
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = if (isExpanded) {
                            Icons.Default.KeyboardArrowDown
                        } else {
                            Icons.Default.KeyboardArrowUp
                        },
                        contentDescription = stringResource(R.string.collapse_controls),
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
                ) {
                    OutlinedButton(
                        onClick = { showLanguageDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    ) {
                        Text(
                            text = stringResource(
                                R.string.language_button_format,
                                state.selectedPreset.displayName,
                                state.localeBadgeText,
                            ),
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                        )
                    }

                    Text(
                        text = stringResource(R.string.localization_location),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp, bottom = 2.dp),
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clickable { onSelectMapMode(Map3DMode.HYBRID) },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = state.mapMode == Map3DMode.HYBRID,
                                onClick = { onSelectMapMode(Map3DMode.HYBRID) },
                            )
                            Text(
                                text = stringResource(R.string.hybrid),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clickable { onSelectMapMode(Map3DMode.ROADMAP) },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = state.mapMode == Map3DMode.ROADMAP,
                                onClick = { onSelectMapMode(Map3DMode.ROADMAP) },
                            )
                            Text(
                                text = stringResource(R.string.roadmap),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
        }
    }
}
