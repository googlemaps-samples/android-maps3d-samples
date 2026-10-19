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

package com.example.composedemos.storelocator

import android.graphics.Point
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.maps3d.common.Map3DQuickSettingsWidget
import com.example.maps3d.common.Map3DSessionTheme
import com.example.maps3d.common.Map3DSettingsController
import com.example.maps3d.common.rememberMap3DSessionSettings
import com.example.maps3d.common.storelocator.StoreAmenity
import com.example.maps3d.common.storelocator.StoreLocation
import com.example.maps3d.common.storelocator.StoreLocatorViewModel
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.CollisionBehavior
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.flyToOptions
import com.google.android.gms.maps3d.model.popoverShadow
import com.google.android.gms.maps3d.model.popoverStyle
import com.google.maps.android.compose3d.GlyphConfig
import com.google.maps.android.compose3d.GoogleMap3D
import com.google.maps.android.compose3d.MarkerConfig
import com.google.maps.android.compose3d.PinConfig
import com.google.maps.android.compose3d.PopoverConfig
import android.graphics.Color as AndroidColor
import com.example.maps3dcommon.R as CommonR

/**
 * Jetpack Compose implementation of the canonical 2D-to-3D Store Locator sample built in
 * [Map3DMode.ROADMAP] and [Map3DMode.HYBRID].
 */
class StoreLocatorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            Map3DSessionTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    StoreLocatorScreen()
                }
            }
        }
    }
}

@Composable
fun StoreLocatorScreen(
    viewModel: StoreLocatorViewModel = viewModel(),
) {
    val settings by rememberMap3DSessionSettings(defaultMapMode = Map3DMode.ROADMAP)
    val initialCamera = remember {
        val initialMode = Map3DSettingsController.shared.getSettings().mapMode
        viewModel.syncInitialMapMode(initialMode)
        if (initialMode != Map3DMode.ROADMAP) {
            StoreLocation.OVERVIEW_CAMERA_3D
        } else {
            StoreLocation.OVERVIEW_CAMERA
        }
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var googleMap3D by remember { mutableStateOf<GoogleMap3D?>(null) }
    var isMapSteady by remember { mutableStateOf(false) }
    var isCollapsed by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val selectedStore = uiState.selectedStore
    val flyToCommand = uiState.flyToCommand

    // Sync ViewModel mapMode (2D ROADMAP vs 3D HYBRID) with Map3DSettingsController and GoogleMap3D
    LaunchedEffect(uiState.mapMode, googleMap3D) {
        if (Map3DSettingsController.shared.getSettings().mapMode != uiState.mapMode) {
            Map3DSettingsController.shared.setMapMode(uiState.mapMode)
        }
        googleMap3D?.setMapMode(uiState.mapMode)
    }

    // Consume one-shot camera flyTo transitions when selecting a store, toggling 2D/3D, or resetting to overview
    LaunchedEffect(googleMap3D, flyToCommand) {
        val map = googleMap3D
        if (map != null && flyToCommand != null) {
            map.flyCameraTo(
                flyToOptions {
                    endCamera = flyToCommand.endCamera
                    durationInMillis = flyToCommand.durationMillis
                },
            )
            viewModel.onFlyToCompleted()
        }
    }

    // Scroll store carousel to the selected store card
    LaunchedEffect(selectedStore, uiState.filteredStores) {
        if (selectedStore != null) {
            val index = uiState.filteredStores.indexOfFirst { it.id == selectedStore.id }
            if (index >= 0) {
                listState.animateScrollToItem(index)
            }
        }
    }

    val markers = remember(uiState.filteredStores, selectedStore) {
        uiState.filteredStores.map { store ->
            val isSelected = store.id == selectedStore?.id
            // Canonical Google brand colors: #F4B400 (Google Yellow) for selected, #1A73E8 (Google Blue) for open
            val bgColor = when {
                isSelected -> "#F4B400".toColorInt()
                store.isOpenNow -> "#1A73E8".toColorInt()
                else -> "#757575".toColorInt()
            }
            val scale = if (isSelected) {
                StoreLocation.SELECTED_PIN_SCALE
            } else {
                StoreLocation.DEFAULT_PIN_SCALE
            }
            MarkerConfig(
                key = "${store.id}_$isSelected",
                position = store.toLatLngAltitude(),
                label = store.name,
                altitudeMode = AltitudeMode.CLAMP_TO_GROUND,
                collisionBehavior = CollisionBehavior.REQUIRED,
                isExtruded = true,
                isDrawnWhenOccluded = true,
                zIndex = if (isSelected) 10 else 1,
                pinConfig = PinConfig(
                    scale = scale,
                    backgroundColor = bgColor,
                    borderColor = AndroidColor.WHITE,
                    glyph = GlyphConfig.Color(AndroidColor.WHITE),
                ),
                onClick = {
                    viewModel.selectStore(store)
                },
            )
        }
    }

    val popovers = remember(selectedStore) {
        if (selectedStore != null) {
            listOf(
                PopoverConfig(
                    key = "popover_${selectedStore.id}",
                    positionAnchorKey = "${selectedStore.id}_true",
                    altitudeMode = AltitudeMode.CLAMP_TO_GROUND,
                    autoCloseEnabled = true,
                    autoPanEnabled = false,
                    anchorOffset = Point(0, 0),
                    startVisible = true,
                    popoverStyle = popoverStyle {
                        padding = 16.0f
                        backgroundColor = AndroidColor.WHITE
                        borderRadius = 10.0f
                        shadow = popoverShadow {
                            color = AndroidColor.argb(77, 0, 0, 0)
                            offsetX = 2.0f
                            offsetY = 4.0f
                            radius = 4.0f
                        }
                    },
                    content = {
                        StorePopoverContent(store = selectedStore)
                    },
                ),
            )
        } else {
            emptyList()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = if (isMapSteady) "MapSteady" else "MapLoading" },
    ) {
        GoogleMap3D(
            camera = initialCamera,
            markers = markers,
            popovers = popovers,
            mapMode = settings.mapMode,
            modifier = Modifier.fillMaxSize(),
            onMapReady = { map ->
                googleMap3D = map
            },
            onMapSteady = {
                isMapSteady = true
            },
        )

        // Top Translucent Header Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.78f))
                .statusBarsPadding()
                .padding(start = 16.dp, end = 64.dp, top = 8.dp, bottom = 8.dp),
        ) {
            Text(
                text = stringResource(CommonR.string.feature_title_store_locator),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        // Bottom Collapsible Store Locator Control Panel
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(12.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isCollapsed = !isCollapsed }
                        .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(
                                if (uiState.is3DMode) {
                                    CommonR.string.store_locator_panel_title_3d
                                } else {
                                    CommonR.string.store_locator_panel_title
                                },
                            ),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(
                                CommonR.string.store_locator_count_format,
                                uiState.filteredStores.size,
                                uiState.allStores.size,
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    FilledTonalButton(
                        onClick = { viewModel.toggle2D3DMode() },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp),
                    ) {
                        Text(
                            text = stringResource(
                                if (uiState.is3DMode) {
                                    CommonR.string.store_locator_switch_to_2d
                                } else {
                                    CommonR.string.store_locator_switch_to_3d
                                },
                            ),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }

                    IconButton(onClick = { isCollapsed = !isCollapsed }) {
                        Icon(
                            imageVector = if (isCollapsed) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = stringResource(
                                if (isCollapsed) CommonR.string.expand_controls else CommonR.string.collapse_controls,
                            ),
                        )
                    }
                }

                AnimatedVisibility(visible = !isCollapsed) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                    ) {
                        // Amenity Filter Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            StoreAmenity.entries.forEach { amenity ->
                                val isChecked = amenity in uiState.selectedAmenities
                                FilterChip(
                                    selected = isChecked,
                                    onClick = { viewModel.toggleAmenity(amenity) },
                                    label = {
                                        Text("${amenity.iconEmoji} ${amenity.label}")
                                    },
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Store Cards Carousel
                        LazyRow(
                            state = listState,
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            items(
                                items = uiState.filteredStores,
                                key = { it.id },
                            ) { store ->
                                StoreCardItem(
                                    store = store,
                                    isSelected = store.id == selectedStore?.id,
                                    onClick = { viewModel.selectStore(store) },
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Settings Overlay (Defaulting to ROADMAP mode)
        Map3DQuickSettingsWidget(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 8.dp, end = 8.dp),
            sampleDefaultMapMode = Map3DMode.ROADMAP,
            googleMap3D = googleMap3D,
            onSettingsChanged = { updatedSettings ->
                viewModel.setMapMode(updatedSettings.mapMode)
            },
        )
    }
}

@Composable
private fun StoreCardItem(
    store: StoreLocation,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    // Canonical Google yellow (#F4B400) for selected store card border
    val borderColor = if (isSelected) {
        Color(0xFFF4B400)
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    Card(
        modifier = Modifier
            .width(245.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(if (isSelected) 2.5.dp else 1.dp, borderColor),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = store.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(CommonR.string.store_locator_distance_format, store.distanceKm),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Text(
                text = store.address,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp),
            )

            Text(
                text = stringResource(
                    CommonR.string.store_locator_hours_phone_format,
                    store.hours,
                    store.phone,
                ),
                style = MaterialTheme.typography.labelSmall,
                color = if (store.isOpenNow) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp),
            )

            Text(
                text = store.formattedAmenities(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
private fun StorePopoverContent(store: StoreLocation) {
    Column {
        Text(
            text = store.name,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
        )
        Text(
            text = stringResource(
                CommonR.string.store_locator_address_distance_format,
                store.address,
                store.distanceKm,
            ),
            fontSize = 12.sp,
            color = Color.DarkGray,
        )
        Text(
            text = stringResource(
                CommonR.string.store_locator_hours_phone_format,
                store.hours,
                store.phone,
            ),
            fontSize = 12.sp,
            color = if (store.isOpenNow) Color(0xFF1E8E3E) else Color.Gray,
        )
    }
}
