// Copyright 2026 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.advancedmaps3dsamples.route

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.advancedmaps3dsamples.BuildConfig
import com.example.advancedmaps3dsamples.R
import com.example.advancedmaps3dsamples.ui.theme.AdvancedMaps3DSamplesTheme
import com.example.advancedmaps3dsamples.utils.calculateHeading
import com.example.advancedmaps3dsamples.utils.haversineDistance
import com.example.advancedmaps3dsamples.utils.toHeading
import com.example.advancedmaps3dsamples.utils.toValidCamera
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.Map3DOptions
import com.google.android.gms.maps3d.Map3DView
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.ImageView
import com.google.android.gms.maps3d.model.Polyline
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.gms.maps3d.model.markerOptions
import com.google.android.gms.maps3d.model.modelOptions
import com.google.android.gms.maps3d.model.orientation
import com.google.android.gms.maps3d.model.polylineOptions
import com.example.advancedmaps3dsamples.utils.GeoMathUtils
import com.google.android.gms.maps3d.model.vector3D
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import androidx.core.content.edit
import kotlin.math.abs
import kotlin.time.Duration.Companion.seconds

sealed interface RouteTracker {
    val name: String

    data object Marker : RouteTracker {
        override val name = "Marker"
    }

    sealed class Model(
        override val name: String,
        val url: String,
        val scale: Double,
        val tilt: Double,
        val hoverAltitude: Double,
        val headingOffset: Double
    ) : RouteTracker

    data object RedCar : Model(
        name = "Red Car",
        url = "https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/red_car.glb",
        scale = 50.0,
        tilt = -90.0,
        hoverAltitude = 25.0,
        headingOffset = 0.0
    )

    data object BananaCar : Model(
        name = "Banana Car",
        url = "https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/banana_car.glb",
        scale = 0.12,
        tilt = -90.0,
        hoverAltitude = 25.0,
        headingOffset = 180.0
    )
}

@AndroidEntryPoint
@OptIn(ExperimentalMaterial3Api::class)
class RouteSampleActivity : ComponentActivity() {
    private val viewModel: RouteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RouteSampleScreen(viewModel)
        }
    }
}

// -------------------------------------------------------------------------------------------------
// MAIN SCREEN COMPOSABLE
// -------------------------------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteSampleScreen(viewModel: RouteViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var map3D by remember { mutableStateOf<GoogleMap3D?>(null) }

    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("route_prefs", Context.MODE_PRIVATE) }
    val warningCount = remember { sharedPrefs.getInt("warning_count", 0) }

    // Display Warnings
    var displayWarning by remember { mutableStateOf(warningCount < 2) }

    // Dynamic tracking state
    var cameraRange by remember { mutableFloatStateOf(1500f) }
    var baseSpeedMps by remember { mutableFloatStateOf(150f) }
    var currentTracker by remember { mutableStateOf<RouteTracker>(RouteTracker.Marker) }
    var currentPolyline by remember { mutableStateOf<Polyline?>(null) }

    // Playback controls state
    var elapsedDistance by remember { mutableFloatStateOf(0f) }
    var totalDistance by remember { mutableFloatStateOf(0f) }
    var isPlaying by remember { mutableStateOf(false) }
    var flyModeActive by remember { mutableStateOf(false) }
    var cameraHeadingOffset by remember { mutableFloatStateOf(0f) }

    AdvancedMaps3DSamplesTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(), topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    scrolledContainerColor = Color.Unspecified,
                    navigationIconContentColor = Color.Unspecified,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    actionIconContentColor = Color.Unspecified
                ), title = { Text("Routes API Integration") }, actions = {
                    IconButton(onClick = {
                        currentTracker = when (currentTracker) {
                            RouteTracker.Marker -> RouteTracker.RedCar
                            RouteTracker.RedCar -> RouteTracker.BananaCar
                            RouteTracker.BananaCar -> RouteTracker.Marker
                        }
                    }) {
                        Icon(
                            imageVector = when (currentTracker) {
                                RouteTracker.Marker -> Icons.Default.Place
                                RouteTracker.RedCar -> Icons.Default.DirectionsCar
                                RouteTracker.BananaCar -> Icons.Default.Star
                            }, contentDescription = "Toggle Tracker Style"
                        )
                    }
                })
            }) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {

                // 1. The Map Viewport Layer
                Map3DViewport(onMapReady = { map3D = it }, onMapCleared = { map3D = null })

                // 2. The Headless Routing Engine
                // This decoupled engine manages the continuous animation frame calculations required for silky smooth route tracking
                RouteFlightEngine(
                    map3D = map3D,
                    uiState = uiState,
                    config = FlightEngineConfig(
                        flyModeActive = flyModeActive,
                        isPlaying = isPlaying,
                        currentTracker = currentTracker,
                        elapsedDistance = elapsedDistance,
                        totalDistance = totalDistance,
                        baseSpeedMps = baseSpeedMps,
                        cameraRange = cameraRange,
                        cameraHeadingOffset = cameraHeadingOffset
                    ),
                    onElapsedDistanceChange = { elapsedDistance = it },
                    onIsPlayingChange = { isPlaying = it })

                // 3. UI Path Drawing (Once Success Reached)
                PolylineDrawer(
                    uiState = uiState,
                    map3D = map3D,
                    currentPolyline = currentPolyline,
                    onPolylineUpdate = { currentPolyline = it })

                // 4. Interactive Overlay UI
                if (!flyModeActive) {
                    StandardControlsOverlay(uiState = uiState, onFetchClicked = {
                        val origin = LatLng(21.307043, -157.858984)
                        val dest = LatLng(21.390177, -157.719454)
                        viewModel.fetchRoute(BuildConfig.MAPS3D_API_KEY, origin, dest)
                    }, onFlyClicked = {
                        val state =
                            uiState as? RouteUiState.Success ?: return@StandardControlsOverlay
                        val rawPath = state.decodedPolyline
                        if (rawPath.size < 2) return@StandardControlsOverlay

                        val cumulativeDistances = DoubleArray(rawPath.size)
                        cumulativeDistances[0] = 0.0
                        for (i in 1 until rawPath.size) {
                            cumulativeDistances[i] = cumulativeDistances[i - 1] + haversineDistance(
                                rawPath[i - 1],
                                rawPath[i]
                            )
                        }
                        totalDistance = cumulativeDistances.last().toFloat()
                        elapsedDistance = 0f

                        flyModeActive = true
                        isPlaying = true
                    })
                } else {
                    PlaybackControlsOverlay(
                        isPlaying = isPlaying,
                        onIsPlayingChange = { isPlaying = it },
                        elapsedDistance = elapsedDistance,
                        onElapsedDistanceChange = { elapsedDistance = it },
                        totalDistance = totalDistance,
                        onExitFlyMode = {
                            flyModeActive = false
                            isPlaying = false
                        })
                }

                // 5. Loading/Error Overlays
                StateStatusOverlay(uiState = uiState)

                // 6. Camera Manipulator Overlays
                if (uiState is RouteUiState.Success) {
                    CameraControlsOverlay(
                        flyModeActive = flyModeActive,
                        cameraRange = cameraRange,
                        onCameraRangeChange = { cameraRange = it },
                        baseSpeedMps = baseSpeedMps,
                        onBaseSpeedChange = { baseSpeedMps = it },
                        cameraHeadingOffset = cameraHeadingOffset,
                        onCameraHeadingChange = { cameraHeadingOffset = it })
                }

                // 7. Dialogs
                if (displayWarning) {
                    SecurityWarningDialog(
                        onDismiss = {
                            displayWarning = false
                            sharedPrefs.edit { putInt("warning_count", warningCount + 1) }
                        })
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// COMPONENT DEFINITIONS
// -------------------------------------------------------------------------------------------------

/**
 * AndroidView wrapper handling the lifecycle bridge to Map3DView.
 */
@Composable
private fun Map3DViewport(
    onMapReady: (GoogleMap3D) -> Unit, onMapCleared: () -> Unit
) {
    val context = LocalContext.current
    AndroidView(modifier = Modifier.fillMaxSize(), factory = {
        val options = Map3DOptions(
            centerLat = 21.350,
            centerLng = -157.800,
            centerAlt = 0.0,
            tilt = 60.0,
            range = 25000.0
        )
        val view = Map3DView(context, options)
        view.onCreate(null)
        view
    }, update = { view ->
        view.getMap3DViewAsync(object : OnMap3DViewReadyCallback {
            override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
                onMapReady(googleMap3D)
            }

            override fun onError(error: Exception) {}
        })
    }, onRelease = { view ->
        onMapCleared()
        view.onDestroy()
    })
}

/**
 * The physical routing logic and frame animation.
 * Abstracted into a strictly headless calculation component. 
 */
data class FlightEngineConfig(
    val flyModeActive: Boolean,
    val isPlaying: Boolean,
    val currentTracker: RouteTracker,
    val elapsedDistance: Float,
    val totalDistance: Float,
    val baseSpeedMps: Float,
    val cameraRange: Float,
    val cameraHeadingOffset: Float
)

@Composable
private fun RouteFlightEngine(
    map3D: GoogleMap3D?,
    uiState: RouteUiState,
    config: FlightEngineConfig,
    onElapsedDistanceChange: (Float) -> Unit,
    onIsPlayingChange: (Boolean) -> Unit
) {
    val updatedConfig by rememberUpdatedState(config)
    val updatedOnElapsedDistanceChange by rememberUpdatedState(onElapsedDistanceChange)
    val updatedOnIsPlayingChange by rememberUpdatedState(onIsPlayingChange)

    LaunchedEffect(config.flyModeActive, uiState, map3D) {
        if (!config.flyModeActive) return@LaunchedEffect

        val state = uiState as? RouteUiState.Success ?: return@LaunchedEffect
        val safeMap = map3D ?: return@LaunchedEffect
        val rawPath = state.decodedPolyline
        if (rawPath.size < 2) return@LaunchedEffect

        val cumulativeDistances = DoubleArray(rawPath.size)
        cumulativeDistances[0] = 0.0
        for (i in 1 until rawPath.size) {
            cumulativeDistances[i] =
                cumulativeDistances[i - 1] + haversineDistance(rawPath[i - 1], rawPath[i])
        }

        val lookaheadSeconds = 8.0
        val lerpFactor = 0.05f

        var currentLat = rawPath[0].latitude
        var currentLng = rawPath[0].longitude
        var currentHeading = calculateHeading(rawPath[0], rawPath[1]).toFloat()

        var lastFrameTime = 0L
        val trackerIds = mutableMapOf<RouteTracker, String>()

        // Hoist internal distance copy to avoid recomposition loops while animating across frames
        var internalDistance = updatedConfig.elapsedDistance

        while (config.flyModeActive) { // Checking the non-updated primitive for effect lifecycle
            withFrameMillis { frameTime ->
                if (lastFrameTime == 0L) {
                    lastFrameTime = frameTime
                    return@withFrameMillis
                }
                val dtMs = frameTime - lastFrameTime
                lastFrameTime = frameTime

                val cfg = updatedConfig

                // Always ingest the external scrubber state to allow the UI to forcefully override position
                if (cfg.elapsedDistance != internalDistance && !cfg.isPlaying) {
                    internalDistance = cfg.elapsedDistance
                }

                if (cfg.isPlaying) {
                    internalDistance += (cfg.baseSpeedMps * (dtMs / 1000.0)).toFloat()
                    if (internalDistance >= cfg.totalDistance) {
                        internalDistance = cfg.totalDistance
                        updatedOnIsPlayingChange(false)
                        updatedOnElapsedDistanceChange(internalDistance)
                    } else if (internalDistance <= 0f) {
                        internalDistance = 0f
                        updatedOnIsPlayingChange(false)
                        updatedOnElapsedDistanceChange(internalDistance)
                    } else {
                        // Push standard updates back up to the UI so scrubber physically advances
                        updatedOnElapsedDistanceChange(internalDistance)
                    }
                }

                val doubleElapsed = internalDistance.toDouble()
                val targetPos = GeoMathUtils.getInterpolatedPoint(doubleElapsed, rawPath, cumulativeDistances)
                val lookaheadSpeed =
                    if (abs(cfg.baseSpeedMps) < 1f) 150.0 else abs(cfg.baseSpeedMps).toDouble()
                val lookaheadDist = doubleElapsed + (lookaheadSpeed * lookaheadSeconds)
                val lookaheadPos = GeoMathUtils.getInterpolatedPoint(lookaheadDist, rawPath, cumulativeDistances)
                val mathHeading = calculateHeading(targetPos, lookaheadPos).toFloat()

                currentLat += (targetPos.latitude - currentLat) * lerpFactor
                currentLng += (targetPos.longitude - currentLng) * lerpFactor
                currentHeading = GeoMathUtils.slerpHeading(currentHeading, mathHeading, lerpFactor)

                val frameCamera = camera {
                    center = latLngAltitude {
                        latitude = currentLat
                        longitude = currentLng
                        altitude = 0.0
                    }
                    heading = (currentHeading.toDouble() + cfg.cameraHeadingOffset).toHeading()
                    tilt = 65.0
                    range = cfg.cameraRange.toDouble()
                }.toValidCamera()
                safeMap.setCamera(frameCamera)

                // UPSERT ALL TRACKERS (Inactive models hide at Null Island)
                val trackers =
                    listOf(RouteTracker.Marker, RouteTracker.RedCar, RouteTracker.BananaCar)
                for (tracker in trackers) {
                    val isActive = tracker == cfg.currentTracker

                    when (tracker) {
                        is RouteTracker.Marker -> {
                            val m = safeMap.addMarker(markerOptions {
                                trackerIds[tracker]?.let { id = it }
                                if (isActive) {
                                    position = latLngAltitude {
                                        latitude = targetPos.latitude; longitude =
                                        targetPos.longitude; altitude = 0.0
                                    }
                                    altitudeMode = AltitudeMode.CLAMP_TO_GROUND
                                } else {
                                    position = latLngAltitude {
                                        latitude = 0.0; longitude = 0.0; altitude = 0.0
                                    }
                                    altitudeMode = AltitudeMode.ABSOLUTE
                                }
                                setStyle(ImageView(R.drawable.car))
                            })
                            if (m != null && !trackerIds.containsKey(tracker)) trackerIds[tracker] =
                                m.id
                        }

                        is RouteTracker.Model -> {
                            val m = safeMap.addModel(modelOptions {
                                trackerIds[tracker]?.let { id = it }
                                if (isActive) {
                                    position = latLngAltitude {
                                        latitude = targetPos.latitude; longitude =
                                        targetPos.longitude; altitude = tracker.hoverAltitude
                                    }
                                    altitudeMode = AltitudeMode.RELATIVE_TO_GROUND
                                } else {
                                    position = latLngAltitude {
                                        latitude = 0.0; longitude = 0.0; altitude = 0.0
                                    }
                                    altitudeMode = AltitudeMode.ABSOLUTE
                                }
                                url = tracker.url
                                scale = if (isActive) vector3D {
                                    x = tracker.scale; y = tracker.scale; z = tracker.scale
                                } else vector3D { x = 0.001; y = 0.001; z = 0.001 }
                                orientation = if (isActive) orientation {
                                    heading =
                                        (currentHeading.toDouble() + tracker.headingOffset).toHeading(); tilt =
                                    tracker.tilt; roll = 0.0
                                } else orientation { heading = 0.0; tilt = 0.0; roll = 0.0 }
                            })
                            if (!trackerIds.containsKey(tracker)) trackerIds[tracker] = m.id
                            // Direct mutation is faster than MapModel object redeclares
                            if (isActive) {
                                m.orientation = orientation {
                                    heading =
                                        (currentHeading.toDouble() + tracker.headingOffset).toHeading(); tilt =
                                    tracker.tilt; roll = 0.0
                                }
                            } else {
                                m.orientation =
                                    orientation { heading = 0.0; tilt = 0.0; roll = 0.0 }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Initial drawing element logic. Overlays polyline visually on Map initial creation.
 */
@Composable
private fun PolylineDrawer(
    uiState: RouteUiState,
    map3D: GoogleMap3D?,
    currentPolyline: Polyline?,
    onPolylineUpdate: (Polyline?) -> Unit
) {
    if (uiState is RouteUiState.Success) {
        LaunchedEffect(uiState.decodedPolyline, map3D) {
            val safeMap = map3D ?: return@LaunchedEffect
            currentPolyline?.remove()

            val polylinePath = uiState.decodedPolyline.map { latLng ->
                latLngAltitude {
                    latitude = latLng.latitude; longitude = latLng.longitude; altitude = 0.0
                }
            }

            val lineOptions = polylineOptions {
                this.path = polylinePath
                strokeColor = android.graphics.Color.BLUE
                strokeWidth = 10.0
            }

            onPolylineUpdate(safeMap.addPolyline(lineOptions))

            val routeCamera = camera {
                center = latLngAltitude { latitude = 21.350; longitude = -157.800; altitude = 0.0 }
                tilt = 45.0
                range = 35000.0
            }
            safeMap.setCamera(routeCamera)
        }
    }
}

@Composable
private fun BoxScope.StandardControlsOverlay(
    uiState: RouteUiState, onFetchClicked: () -> Unit, onFlyClicked: () -> Unit
) {
    Button(
        onClick = onFetchClicked, modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(32.dp)
    ) {
        Text("Fetch & Draw Route")
    }

    Button(
        onClick = onFlyClicked,
        enabled = uiState is RouteUiState.Success,
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(32.dp)
    ) {
        Text("Fly Along")
    }
}

@Composable
private fun BoxScope.PlaybackControlsOverlay(
    isPlaying: Boolean,
    onIsPlayingChange: (Boolean) -> Unit,
    elapsedDistance: Float,
    onElapsedDistanceChange: (Float) -> Unit,
    totalDistance: Float,
    onExitFlyMode: () -> Unit
) {
    Surface(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(16.dp)
            .wrapContentHeight()
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onIsPlayingChange(!isPlaying) }) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause"
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Slider(
                value = elapsedDistance,
                onValueChange = {
                    onElapsedDistanceChange(it)
                    onIsPlayingChange(false)
                },
                valueRange = 0f..kotlin.math.max(1f, totalDistance),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onExitFlyMode) {
                Icon(Icons.Default.Close, contentDescription = "Exit Fly Mode")
            }
        }
    }
}

@Composable
private fun BoxScope.StateStatusOverlay(uiState: RouteUiState) {
    when (uiState) {
        is RouteUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        is RouteUiState.Error -> {
            Box(modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp)) {
                Text(
                    text = uiState.message,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        else -> { /* Do nothing */
        }
    }
}

@Composable
private fun BoxScope.CameraControlsOverlay(
    flyModeActive: Boolean,
    cameraRange: Float,
    onCameraRangeChange: (Float) -> Unit,
    baseSpeedMps: Float,
    onBaseSpeedChange: (Float) -> Unit,
    cameraHeadingOffset: Float,
    onCameraHeadingChange: (Float) -> Unit
) {
    FadingVerticalSlider(
        value = cameraRange,
        onValueChange = onCameraRangeChange,
        valueRange = 200f..10000f,
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .padding(end = 16.dp)
    )

    FadingVerticalSlider(
        value = baseSpeedMps,
        onValueChange = { onBaseSpeedChange(if (abs(it) < 150f) 0f else it) },
        valueRange = -1500f..1500f,
        drawCenterDeadZone = true,
        modifier = Modifier
            .align(Alignment.CenterStart)
            .padding(start = 16.dp)
    )

    if (flyModeActive) {
        FadingThumbWheel(
            value = cameraHeadingOffset,
            onValueChange = onCameraHeadingChange,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp)
                .padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun SecurityWarningDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.Warning, contentDescription = null) },
        title = { Text("Security Warning") },
        text = { Text("This sample makes a direct REST API call from a mobile client to the Google Maps Routes API. In a production application, doing this exposes your API key to malicious extraction.\n\nAlways proxy your Routes API requests through a secure backend server!") },
        confirmButton = { TextButton(onClick = onDismiss) { Text("I Understand") } })
}



@Composable
private fun FadingThumbWheel(
    value: Float, onValueChange: (Float) -> Unit, modifier: Modifier = Modifier
) {
    val currentValue by rememberUpdatedState(value)
    val currentOnValueChange by rememberUpdatedState(onValueChange)

    var sliderInteractionTime by androidx.compose.runtime.remember { mutableLongStateOf(System.currentTimeMillis()) }
    var isSliderActive by androidx.compose.runtime.remember { mutableStateOf(true) }

    LaunchedEffect(sliderInteractionTime) {
        isSliderActive = true
        delay(3.seconds)
        isSliderActive = false
    }

    val sliderAlpha by animateFloatAsState(
        targetValue = if (isSliderActive) 0.9f else 0.3f,
        animationSpec = tween(durationMillis = 500),
        label = "sliderAlpha"
    )

    Box(
        modifier = modifier
            .width(250.dp)
            .height(48.dp)
            .alpha(sliderAlpha)
            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent(PointerEventPass.Initial)
                        sliderInteractionTime = System.currentTimeMillis()
                    }
                }
            }
            .pointerInput(Unit) {
                var localValue = 0f
                detectHorizontalDragGestures(
                    onDragStart = { localValue = currentValue },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        localValue += dragAmount * -0.2f
                        var wrapped = localValue
                        while (wrapped > 180f) wrapped -= 360f
                        while (wrapped <= -180f) wrapped += 360f
                        localValue = wrapped
                        currentOnValueChange(localValue)
                    })
            }) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(11) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(12.dp)
                        .background(Color.White.copy(alpha = 0.4f))
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(4.dp)
                .height(24.dp)
                .background(Color.Red, RoundedCornerShape(2.dp))
        )

        Text(
            text = "${value.toInt()}°",
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 2.dp)
        )
    }
}

@Composable
private fun FadingVerticalSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    drawCenterDeadZone: Boolean = false
) {
    var sliderInteractionTime by androidx.compose.runtime.remember { mutableLongStateOf(System.currentTimeMillis()) }
    var isSliderActive by androidx.compose.runtime.remember { mutableStateOf(true) }

    LaunchedEffect(sliderInteractionTime) {
        isSliderActive = true
        delay(3.seconds)
        isSliderActive = false
    }

    val sliderAlpha by animateFloatAsState(
        targetValue = if (isSliderActive) 0.9f else 0.3f,
        animationSpec = tween(durationMillis = 500),
        label = "sliderAlpha"
    )

    Box(
        modifier = modifier
            .requiredWidth(48.dp)
            .requiredHeight(300.dp)
            .alpha(sliderAlpha)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent(PointerEventPass.Initial)
                        sliderInteractionTime = System.currentTimeMillis()
                    }
                }
            }) {
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier
                .requiredWidth(300.dp)
                .requiredHeight(48.dp)
                .graphicsLayer {
                    rotationZ = 270f
                    transformOrigin = TransformOrigin(0.5f, 0.5f)
                }
                .align(Alignment.Center))

        if (drawCenterDeadZone) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .requiredWidth(16.dp)
                    .requiredHeight(4.dp)
                    .background(Color.White, shape = CircleShape)
            )
        }
    }
}
