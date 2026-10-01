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

package com.example.composedemos.markers

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.composedemos.markers.data.Monster
import com.example.composedemos.markers.data.MonsterParser
import com.example.maps3d.common.toCameraString
import com.example.maps3d.common.toValidCamera
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.CollisionBehavior
import com.google.android.gms.maps3d.model.FlyAroundOptions
import com.google.android.gms.maps3d.model.FlyToOptions
import com.google.android.gms.maps3d.model.ImageView
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.flyAroundOptions
import com.google.android.gms.maps3d.model.flyToOptions
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.maps.android.compose3d.GlyphConfig
import com.google.maps.android.compose3d.GoogleMap3D
import com.google.maps.android.compose3d.MarkerConfig
import com.google.maps.android.compose3d.PinConfig
import com.google.maps.android.compose3d.PopoverConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.seconds
import android.graphics.Color as AndroidColor
import com.example.maps3dcommon.R as CommonR

private const val TAG = "MarkersActivity"

class MarkersActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Hide system tray (immersive mode)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    MarkersScreen()
                }
            }
        }
    }
}

/**
 * Jetpack Compose implementation of the Markers sample demonstrating:
 * - Four altitude modes in Berlin (`ABSOLUTE`, `RELATIVE_TO_GROUND`, `CLAMP_TO_GROUND`, `RELATIVE_TO_MESH`)
 * - Empire State Building custom image marker with popover and custom color/text glyph pins in NYC
 * - Monster markers parsed from `monsters.json` with interactive popovers, random monster flight
 *   (with long-press monster picker menu), and an automated Monster Tour.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MarkersScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isMapSteady by remember { mutableStateOf(false) }
    val mapSteadyFlow = remember { MutableStateFlow(false) }
    var googleMap3D by remember { mutableStateOf<GoogleMap3D?>(null) }
    var popovers by remember { mutableStateOf(emptyList<PopoverConfig>()) }
    var isTouring by remember { mutableStateOf(false) }
    var showMonsterMenu by remember { mutableStateOf(false) }

    val berlinCamera = remember {
        camera {
            center = latLngAltitude {
                latitude = 52.51974795
                longitude = 13.40715553
                altitude = 150.0
            }
            heading = 252.7
            tilt = 79.0
            range = 1500.0
        }
    }

    val nycCamera = remember {
        camera {
            center = latLngAltitude {
                latitude = 40.748425
                longitude = -73.985590
                altitude = 348.7
            }
            heading = 22.0
            tilt = 80.0
            range = 1518.0
        }
    }

    val parsedMonsters = remember(context) {
        try {
            val jsonString = context.assets.open("monsters.json").bufferedReader().use {
                it.readText()
            }
            MonsterParser.parse(jsonString).filter { getMonsterDrawableId(it.drawable) != 0 }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading monsters.json", e)
            emptyList()
        }
    }

    val monsterCameras = remember(parsedMonsters) {
        parsedMonsters.map { monster ->
            camera {
                center = latLngAltitude {
                    latitude = monster.latitude
                    longitude = monster.longitude
                    altitude = monster.altitude
                }
                heading = monster.heading
                tilt = monster.tilt
                range = monster.range
            }
        }
    }

    fun showToast(label: String) {
        scope.launch {
            Toast.makeText(context, "Clicked on marker: $label", Toast.LENGTH_SHORT).show()
        }
    }

    fun showBlurbPopover(anchorKey: String, altitudeMode: Int, blurbResId: Int) {
        if (blurbResId == 0) return
        scope.launch {
            popovers = listOf(
                createPopoverConfig(
                    key = "popover_$anchorKey",
                    anchorKey = anchorKey,
                    altitudeMode = altitudeMode,
                    blurbResId = blurbResId,
                ),
            )
        }
    }

    val allMarkers = remember(parsedMonsters) {
        buildList {
            // Marker 1: Absolute Altitude (Berlin)
            add(
                MarkerConfig(
                    key = "marker_one",
                    position = latLngAltitude {
                        latitude = 52.519605780912585
                        longitude = 13.406867190588198
                        altitude = 150.0
                    },
                    label = "Absolute (150m)",
                    altitudeMode = AltitudeMode.ABSOLUTE,
                    isExtruded = true,
                    isDrawnWhenOccluded = true,
                    collisionBehavior = CollisionBehavior.OPTIONAL_AND_HIDES_LOWER_PRIORITY,
                    onClick = { showToast("Absolute (150m)") },
                ),
            )

            // Marker 2: Relative to Ground (Berlin)
            add(
                MarkerConfig(
                    key = "relative_to_ground",
                    position = latLngAltitude {
                        latitude = 52.519882191069016
                        longitude = 13.407410777254293
                        altitude = 50.0
                    },
                    label = "Relative to Ground (50m)",
                    altitudeMode = AltitudeMode.RELATIVE_TO_GROUND,
                    isExtruded = true,
                    isDrawnWhenOccluded = true,
                    collisionBehavior = CollisionBehavior.OPTIONAL_AND_HIDES_LOWER_PRIORITY,
                    onClick = { showToast("Relative to Ground (50m)") },
                ),
            )

            // Marker 3: Clamped to Ground (Berlin)
            add(
                MarkerConfig(
                    key = "clamped_to_ground",
                    position = latLngAltitude {
                        latitude = 52.52027645136134
                        longitude = 13.408271658592406
                        altitude = 0.0
                    },
                    label = "Clamped to Ground",
                    altitudeMode = AltitudeMode.CLAMP_TO_GROUND,
                    isExtruded = true,
                    isDrawnWhenOccluded = true,
                    collisionBehavior = CollisionBehavior.REQUIRED,
                    onClick = { showToast("Clamped to Ground") },
                ),
            )

            // Marker 4: Relative to Mesh (Berlin)
            add(
                MarkerConfig(
                    key = "relative_to_mesh",
                    position = latLngAltitude {
                        latitude = 52.520835071144226
                        longitude = 13.409426847943774
                        altitude = 10.0
                    },
                    label = "Relative to Mesh (10m)",
                    altitudeMode = AltitudeMode.RELATIVE_TO_MESH,
                    isExtruded = true,
                    isDrawnWhenOccluded = true,
                    collisionBehavior = CollisionBehavior.REQUIRED,
                    onClick = { showToast("Relative to Mesh (10m)") },
                ),
            )

            // Marker 8: Empire State Building Ape (NYC)
            add(
                MarkerConfig(
                    key = "esb_ape",
                    position = latLngAltitude {
                        latitude = 40.7484
                        longitude = -73.9857
                        altitude = 100.0
                    },
                    zIndex = 1,
                    label = "Giant Ape / Empire State Building",
                    isExtruded = true,
                    isDrawnWhenOccluded = true,
                    altitudeMode = AltitudeMode.RELATIVE_TO_MESH,
                    styleView = ImageView(CommonR.drawable.ook),
                    onClick = {
                        showBlurbPopover(
                            anchorKey = "esb_ape",
                            altitudeMode = AltitudeMode.ABSOLUTE,
                            blurbResId = CommonR.string.monster_ape_blurb,
                        )
                    },
                ),
            )

            // Marker 9: Custom Color Pin near ESB (NYC)
            add(
                MarkerConfig(
                    key = "custom_color_pin",
                    position = latLngAltitude {
                        latitude = 40.7486
                        longitude = -73.9848
                        altitude = 600.0
                    },
                    isExtruded = true,
                    isDrawnWhenOccluded = true,
                    label = "Custom Color Pin",
                    altitudeMode = AltitudeMode.RELATIVE_TO_GROUND,
                    pinConfig = PinConfig(
                        backgroundColor = AndroidColor.RED,
                        borderColor = AndroidColor.WHITE,
                        glyph = GlyphConfig.Color(AndroidColor.CYAN),
                    ),
                    onClick = { showToast("Custom Color Pin") },
                ),
            )

            // Marker 10: Custom Text Pin near ESB (NYC)
            add(
                MarkerConfig(
                    key = "custom_text_pin",
                    position = latLngAltitude {
                        latitude = 40.7482
                        longitude = -73.9862
                        altitude = 600.0
                    },
                    isExtruded = true,
                    isDrawnWhenOccluded = true,
                    label = "Custom Text Pin",
                    altitudeMode = AltitudeMode.RELATIVE_TO_GROUND,
                    pinConfig = PinConfig(
                        backgroundColor = AndroidColor.YELLOW,
                        borderColor = AndroidColor.BLUE,
                        glyph = GlyphConfig.Text("NYC\n 🍎 ", AndroidColor.RED),
                    ),
                    onClick = { showToast("Custom Text Pin") },
                ),
            )

            // Monsters from monsters.json
            parsedMonsters.forEach { monster ->
                val drawableId = getMonsterDrawableId(monster.drawable)
                val anchorKey = "monster_${monster.id}"
                val blurbResId = getMonsterBlurbResId(monster.id)
                add(
                    MarkerConfig(
                        key = anchorKey,
                        position = latLngAltitude {
                            latitude = monster.markerLatitude
                            longitude = monster.markerLongitude
                            altitude = monster.markerAltitude
                        },
                        label = monster.label,
                        isExtruded = true,
                        isDrawnWhenOccluded = true,
                        altitudeMode = monster.altitudeMode,
                        styleView = ImageView(drawableId),
                        onClick = {
                            if (blurbResId != 0) {
                                showBlurbPopover(
                                    anchorKey = anchorKey,
                                    altitudeMode = monster.altitudeMode,
                                    blurbResId = blurbResId,
                                )
                            } else {
                                showToast(monster.label)
                            }
                        },
                    ),
                )
            }
        }
    }

    fun stopMonsterTour() {
        if (isTouring) {
            isTouring = false
            googleMap3D?.stopCameraAnimation()
            googleMap3D?.setCameraAnimationEndListener(null)
        }
    }

    // Automated Monster Tour coroutine
    LaunchedEffect(isTouring, googleMap3D, parsedMonsters) {
        val map = googleMap3D
        if (!isTouring || map == null || parsedMonsters.isEmpty()) return@LaunchedEffect

        var index = 0
        while (isActive && isTouring) {
            popovers = emptyList()

            val monster: Monster = parsedMonsters[index]
            val targetCamera: Camera = monsterCameras[index]
            val anchorKey = "monster_${monster.id}"
            val blurbResId = getMonsterBlurbResId(monster.id)

            // 1. Fly to the monster
            map.awaitCameraAnimation(
                flyToOptions {
                    endCamera = targetCamera
                    durationInMillis = 4.seconds.inWholeMilliseconds
                },
            )
            if (!isActive || !isTouring) break

            // 2. Wait for the 3D mesh building geometry to load (up to 5 seconds)
            withTimeoutOrNull(5.seconds) {
                mapSteadyFlow.first { it }
            }
            if (!isActive || !isTouring) break

            // 3. Orbit around the monster
            map.awaitCameraAnimation(
                flyAroundOptions {
                    center = targetCamera
                    durationInMillis = 5.seconds.inWholeMilliseconds
                    rounds = 1.0
                },
            )
            if (!isActive || !isTouring) break

            // 4. Show monster blurb popover
            if (blurbResId != 0) {
                popovers = listOf(
                    createPopoverConfig(
                        key = "popover_$anchorKey",
                        anchorKey = anchorKey,
                        altitudeMode = monster.altitudeMode,
                        blurbResId = blurbResId,
                    ),
                )
            }

            delay(4.seconds)
            index = (index + 1) % parsedMonsters.size
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = if (isMapSteady) "MapSteady" else "MapLoading" },
    ) {
        // 1. Map fills the entire screen
        GoogleMap3D(
            camera = nycCamera,
            mapMode = Map3DMode.SATELLITE,
            markers = allMarkers,
            popovers = popovers,
            modifier = Modifier.fillMaxSize(),
            onMapReady = { map ->
                googleMap3D = map
            },
            onMapSteady = {
                isMapSteady = true
            },
            onMapSteadyChange = { steady ->
                mapSteadyFlow.value = steady
            },
            onMapClick = {
                popovers = emptyList()
            },
        )

        // 2. Custom Translucent Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75f))
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                text = stringResource(CommonR.string.feature_title_markers),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        // 3. Floating Pill Control Bar (matches activity_common_map.xml + MarkersActivity.kt)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .background(
                        color = Color(0x80FFFFFF),
                        shape = RoundedCornerShape(32.dp),
                    )
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (!isTouring) {
                    // Tour Monsters Button (🗺️)
                    FilledTonalButton(
                        onClick = {
                            if (monsterCameras.isNotEmpty()) {
                                isTouring = true
                            }
                        },
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .alpha(0.85f)
                            .semantics {
                                contentDescription = context.getString(CommonR.string.tour_monsters)
                            },
                    ) {
                        Text(text = "🗺️", fontSize = 20.sp)
                    }
                } else {
                    // Stop Tour Button
                    FilledTonalIconButton(
                        onClick = { stopMonsterTour() },
                        modifier = Modifier.alpha(0.85f),
                    ) {
                        Icon(
                            painter = painterResource(id = CommonR.drawable.stop_24px),
                            contentDescription = stringResource(CommonR.string.stop_camera),
                        )
                    }
                }

                // Fly to Random Monster Button (🎲) with Long-Press Monster Picker Menu
                Box {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .alpha(0.85f)
                            .clip(CircleShape)
                            .combinedClickable(
                                onClick = {
                                    stopMonsterTour()
                                    if (monsterCameras.isNotEmpty()) {
                                        googleMap3D?.flyCameraTo(
                                            flyToOptions {
                                                endCamera = monsterCameras.random()
                                                durationInMillis = 4_000
                                            },
                                        )
                                    }
                                },
                                onLongClick = {
                                    if (parsedMonsters.isNotEmpty()) {
                                        showMonsterMenu = true
                                    }
                                },
                            )
                            .semantics {
                                contentDescription = context.getString(
                                    CommonR.string.content_description_fly_random,
                                )
                            },
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
                        ) {
                            Text(text = "🎲", fontSize = 20.sp)
                        }
                    }

                    DropdownMenu(
                        expanded = showMonsterMenu,
                        onDismissRequest = { showMonsterMenu = false },
                    ) {
                        parsedMonsters.forEachIndexed { index, monster ->
                            DropdownMenuItem(
                                text = { Text(monster.label) },
                                onClick = {
                                    showMonsterMenu = false
                                    stopMonsterTour()
                                    googleMap3D?.flyCameraTo(
                                        flyToOptions {
                                            endCamera = monsterCameras[index]
                                            durationInMillis = 4_000
                                        },
                                    )
                                },
                            )
                        }
                    }
                }

                // Fly to Berlin (4 AltitudeMode Markers)
                FilledTonalIconButton(
                    onClick = {
                        stopMonsterTour()
                        googleMap3D?.flyCameraTo(
                            flyToOptions {
                                endCamera = berlinCamera
                                durationInMillis = 4_000
                            },
                        )
                    },
                    modifier = Modifier.alpha(0.85f),
                ) {
                    Icon(
                        painter = painterResource(id = CommonR.drawable.public_24px),
                        contentDescription = stringResource(
                            CommonR.string.content_description_fly_berlin,
                        ),
                    )
                }

                // Fly to NYC (Empire State Building Ape & Custom Pins)
                FilledTonalIconButton(
                    onClick = {
                        stopMonsterTour()
                        googleMap3D?.flyCameraTo(
                            flyToOptions {
                                endCamera = nycCamera
                                durationInMillis = 4_000
                            },
                        )
                    },
                    modifier = Modifier.alpha(0.85f),
                ) {
                    Icon(
                        painter = painterResource(id = CommonR.drawable.location_city_24px),
                        contentDescription = stringResource(
                            CommonR.string.content_description_fly_nyc,
                        ),
                    )
                }

                // Reset View Button
                FilledTonalIconButton(
                    onClick = {
                        stopMonsterTour()
                        googleMap3D?.flyCameraTo(
                            flyToOptions {
                                endCamera = nycCamera
                                durationInMillis = 2_000
                            },
                        )
                    },
                    modifier = Modifier.alpha(0.85f),
                ) {
                    Icon(
                        painter = painterResource(id = CommonR.drawable.restart_alt_24px),
                        contentDescription = stringResource(CommonR.string.reset_view),
                    )
                }

                // Snapshot Button
                FilledTonalIconButton(
                    onClick = {
                        googleMap3D?.getCamera()?.let { cam ->
                            Log.d(TAG, cam.toValidCamera().toCameraString())
                        }
                    },
                    modifier = Modifier.alpha(0.85f),
                ) {
                    Icon(
                        painter = painterResource(id = CommonR.drawable.photo_camera_24px),
                        contentDescription = stringResource(CommonR.string.snapshot_camera),
                    )
                }
            }
        }
    }
}

private fun createPopoverConfig(
    key: String,
    anchorKey: String,
    altitudeMode: Int,
    blurbResId: Int,
): PopoverConfig = PopoverConfig(
    key = key,
    positionAnchorKey = anchorKey,
    altitudeMode = altitudeMode,
    autoCloseEnabled = true,
    autoPanEnabled = false,
    content = {
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(8.dp),
        ) {
            Text(
                text = stringResource(blurbResId),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = Color.Black,
            )
        }
    },
)

/**
 * Initiates a FlyTo camera animation and suspends until it finishes.
 */
private suspend fun GoogleMap3D.awaitCameraAnimation(options: FlyToOptions) {
    suspendCancellableCoroutine { cont ->
        setCameraAnimationEndListener {
            setCameraAnimationEndListener(null)
            if (cont.isActive) cont.resume(Unit)
        }
        flyCameraTo(options)
    }
}

/**
 * Initiates a FlyAround camera animation and suspends until it finishes.
 */
private suspend fun GoogleMap3D.awaitCameraAnimation(options: FlyAroundOptions) {
    suspendCancellableCoroutine { cont ->
        setCameraAnimationEndListener {
            setCameraAnimationEndListener(null)
            if (cont.isActive) cont.resume(Unit)
        }
        flyCameraAround(options)
    }
}

/**
 * Helper to map monster IDs to their drawable resources without using reflection.
 */
private fun getMonsterDrawableId(drawableName: String): Int = when (drawableName) {
    "alien" -> CommonR.drawable.alien
    "bigfoot" -> CommonR.drawable.bigfoot
    "frank" -> CommonR.drawable.frank
    "godzilla" -> CommonR.drawable.godzilla
    "mothra" -> CommonR.drawable.mothra
    "mummy" -> CommonR.drawable.mummy
    "nessie" -> CommonR.drawable.nessie
    "yeti" -> CommonR.drawable.yeti
    else -> 0
}

/**
 * Helper to map monster IDs to their blurb string resources.
 */
private fun getMonsterBlurbResId(monsterId: String): Int = when (monsterId) {
    "alien" -> CommonR.string.monster_alien_blurb
    "bigfoot" -> CommonR.string.monster_bigfoot_blurb
    "frank" -> CommonR.string.monster_frank_blurb
    "godzilla" -> CommonR.string.monster_godzilla_blurb
    "mothra" -> CommonR.string.monster_mothra_blurb
    "mummy" -> CommonR.string.monster_mummy_blurb
    "nessie" -> CommonR.string.monster_nessie_blurb
    "yeti" -> CommonR.string.monster_yeti_blurb
    else -> 0
}
