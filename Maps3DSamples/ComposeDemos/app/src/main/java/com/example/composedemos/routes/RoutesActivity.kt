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

package com.example.composedemos.routes

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.composedemos.BuildConfig
import com.example.composedemos.R
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.ImageView
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.maps.android.compose3d.GoogleMap3D
import com.google.maps.android.compose3d.MarkerConfig
import com.google.maps.android.compose3d.ModelConfig
import com.google.maps.android.compose3d.ModelScale
import com.google.maps.android.compose3d.PolylineConfig
import com.google.maps.android.compose3d.PopoverConfig
import com.google.maps.android.compose3d.utils.haversineDistance
import com.google.maps.android.compose3d.utils.toHeading
import com.google.maps.android.compose3d.utils.toValidCamera
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlin.math.abs

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

class RoutesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Hide system tray (immersive mode)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val viewModel: RouteViewModel = viewModel()
                    RouteSampleScreen(viewModel)
                }
            }
        }
    }
}

@OptIn(kotlinx.coroutines.FlowPreview::class)
@Composable
fun RouteSampleScreen(viewModel: RouteViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("route_prefs", Context.MODE_PRIVATE) }
    val warningCount = remember { sharedPrefs.getInt("warning_count", 0) }

    // Display Warnings
    var displayWarning by remember { mutableStateOf(warningCount < 2) }

    // User calibrated camera position
    val initialCamera = remember {
        camera {
            center = latLngAltitude {
                latitude = 21.348567086690373
                longitude = -157.80396135489252
                altitude = 0.0
            }
            heading = 38.685134043475976
            tilt = 44.96296481747822
            range = 29158.68678946048
            roll = 0.0
        }
    }

    // State-driven camera
    var cameraState by remember { mutableStateOf(initialCamera) }
    var polylines by remember { mutableStateOf(emptyList<PolylineConfig>()) }

    // Dynamic tracking state
    var cameraRange by remember { mutableFloatStateOf(1500f) }
    var baseSpeedMps by remember { mutableFloatStateOf(150f) }
    var currentTracker by remember { mutableStateOf<RouteTracker>(RouteTracker.RedCar) }

    // Playback controls state
    var elapsedDistance by remember { mutableFloatStateOf(0f) }
    var totalDistance by remember { mutableFloatStateOf(0f) }
    var isPlaying by remember { mutableStateOf(false) }
    var flyModeActive by remember { mutableStateOf(false) }
    var cameraHeadingOffset by remember { mutableFloatStateOf(0f) }

    val cameraFlow = remember { MutableSharedFlow<Camera>(replay = 0, extraBufferCapacity = 1) }

    LaunchedEffect(cameraFlow) {
        cameraFlow
            .debounce(1000)
            .collect { camera ->
                Log.d("CameraPosition", "Center: ${camera.center.latitude}, ${camera.center.longitude}, Heading: ${camera.heading}, Tilt: ${camera.tilt}, Range: ${camera.range}")
            }
    }

    // 1. State flows for inputs to the engine
    val routeFlow = remember { MutableStateFlow(emptyList<LatLng>()) }
    val progressFlow = remember { MutableStateFlow(0f) }

    // Automatically fetch route on start and update routeFlow
    LaunchedEffect(Unit) {
        val origin = LatLng(21.307043, -157.858984)
        val dest = LatLng(21.390177, -157.719454)
        viewModel.fetchRoute(BuildConfig.MAPS3D_API_KEY, origin, dest)
    }

    LaunchedEffect(uiState) {
        if (uiState is RouteUiState.Success) {
            val state = uiState as RouteUiState.Success
            routeFlow.value = state.decodedPolyline
            
            // Calculate total distance for progress calculation
            val rawPath = state.decodedPolyline
            if (rawPath.size >= 2) {
                val cumulativeDistances = DoubleArray(rawPath.size)
                cumulativeDistances[0] = 0.0
                for (i in 1 until rawPath.size) {
                    cumulativeDistances[i] = cumulativeDistances[i - 1] + haversineDistance(rawPath[i - 1], rawPath[i])
                }
                totalDistance = cumulativeDistances.last().toFloat()
            }

            // Draw polyline
            polylines = listOf(
                PolylineConfig(
                    key = "route_line",
                    points = state.decodedPolyline.map { latLngAltitude { latitude = it.latitude; longitude = it.longitude; altitude = 0.0 } },
                    color = android.graphics.Color.BLUE,
                    width = 10f
                )
            )
        }
    }

    // 2. The Engine Flow
    val trackingFlow = remember(routeFlow) {
        RouteEngine.getRouteTrackingFlow(routeFlow, progressFlow, 1000.0)
    }

    // 3. Collect the output state
    val positionAndHeading by trackingFlow.collectAsState(initial = PositionAndHeading(LatLng(0.0, 0.0), 0f))

    // 4. Update camera reactively during flight
    LaunchedEffect(positionAndHeading, flyModeActive, cameraRange, cameraHeadingOffset) {
        if (flyModeActive && positionAndHeading.position.latitude != 0.0) {
            cameraState = camera {
                center = latLngAltitude {
                    latitude = positionAndHeading.position.latitude
                    longitude = positionAndHeading.position.longitude
                    altitude = 0.0
                }
                heading = (positionAndHeading.heading.toDouble() + cameraHeadingOffset).toHeading()
                tilt = 65.0
                range = cameraRange.toDouble()
            }.toValidCamera()
        }
    }

    // 5. Derive the models list (Combiner)
    val currentModels = remember(positionAndHeading, currentTracker) {
        val redCarConfig = ModelConfig(
            key = "red_car",
            url = RouteTracker.RedCar.url,
            position = if (currentTracker == RouteTracker.RedCar && positionAndHeading.position.latitude != 0.0) {
                latLngAltitude { 
                    latitude = positionAndHeading.position.latitude
                    longitude = positionAndHeading.position.longitude
                    altitude = RouteTracker.RedCar.hoverAltitude 
                }
            } else {
                latLngAltitude { latitude = 0.0; longitude = 0.0; altitude = 0.0 }
            },
            altitudeMode = if (currentTracker == RouteTracker.RedCar) AltitudeMode.RELATIVE_TO_GROUND else AltitudeMode.ABSOLUTE,
            scale = if (currentTracker == RouteTracker.RedCar) ModelScale.Uniform(RouteTracker.RedCar.scale.toFloat()) else ModelScale.Uniform(0.001f),
            heading = if (currentTracker == RouteTracker.RedCar) positionAndHeading.heading.toDouble() else 0.0,
            tilt = if (currentTracker == RouteTracker.RedCar) RouteTracker.RedCar.tilt else 0.0,
            roll = 0.0
        )

        val bananaCarConfig = ModelConfig(
            key = "banana_car",
            url = RouteTracker.BananaCar.url,
            position = if (currentTracker == RouteTracker.BananaCar && positionAndHeading.position.latitude != 0.0) {
                latLngAltitude { 
                    latitude = positionAndHeading.position.latitude
                    longitude = positionAndHeading.position.longitude
                    altitude = RouteTracker.BananaCar.hoverAltitude 
                }
            } else {
                latLngAltitude { latitude = 0.0; longitude = 0.0; altitude = 0.0 }
            },
            altitudeMode = if (currentTracker == RouteTracker.BananaCar) AltitudeMode.RELATIVE_TO_GROUND else AltitudeMode.ABSOLUTE,
            scale = if (currentTracker == RouteTracker.BananaCar) ModelScale.Uniform(RouteTracker.BananaCar.scale.toFloat()) else ModelScale.Uniform(0.001f),
            heading = if (currentTracker == RouteTracker.BananaCar) positionAndHeading.heading.toDouble() else 0.0,
            tilt = if (currentTracker == RouteTracker.BananaCar) RouteTracker.BananaCar.tilt else 0.0,
            roll = 0.0
        )

        listOf(redCarConfig, bananaCarConfig)
    }

    // Derive markers list
    val currentMarkers = remember(positionAndHeading, currentTracker) {
        if (currentTracker == RouteTracker.Marker && positionAndHeading.position.latitude != 0.0) {
            listOf(
                MarkerConfig(
                    key = "route_marker",
                    position = latLngAltitude {
                        latitude = positionAndHeading.position.latitude
                        longitude = positionAndHeading.position.longitude
                        altitude = 0.0
                    },
                    altitudeMode = AltitudeMode.CLAMP_TO_GROUND,
                    styleView = ImageView(R.drawable.car)
                )
            )
        } else {
            emptyList()
        }
    }

    // 6. Drive progress during animation
    LaunchedEffect(isPlaying, totalDistance) {
        if (!isPlaying || totalDistance <= 0f) return@LaunchedEffect
        var lastFrameTime = withFrameMillis { it }
        while (isPlaying) {
            withFrameMillis { frameTime ->
                val dtMs = frameTime - lastFrameTime
                lastFrameTime = frameTime
                val deltaDistance = baseSpeedMps * (dtMs / 1000.0).toFloat()
                elapsedDistance += deltaDistance
                
                if (elapsedDistance >= totalDistance) {
                    elapsedDistance = totalDistance
                    isPlaying = false
                } else if (elapsedDistance <= 0f) {
                    elapsedDistance = 0f
                    isPlaying = false
                }
                progressFlow.value = elapsedDistance / totalDistance
            }
        }
    }

    // Sync progressFlow when elapsedDistance is changed manually via slider
    LaunchedEffect(elapsedDistance, totalDistance) {
        if (totalDistance > 0f) {
            progressFlow.value = elapsedDistance / totalDistance
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // The Map Viewport Layer
        GoogleMap3D(
            camera = cameraState,
            markers = currentMarkers,
            models = currentModels,
            polylines = polylines,
            mapMode = Map3DMode.SATELLITE,
            modifier = Modifier.fillMaxSize(),
            onCameraChanged = { camera ->
                cameraFlow.tryEmit(camera)
            }
        )

        // Custom Translucent Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75f))
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Routes API",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

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
                        },
                        contentDescription = "Toggle Tracker Style",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Interactive Overlay UI
        if (!flyModeActive) {
            StandardControlsOverlay(uiState = uiState, onFlyClicked = {
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
                    cameraState = initialCamera // Reset camera
                    elapsedDistance = 0f
                    progressFlow.value = 0f
                })
        }

        // Loading/Error Overlays
        StateStatusOverlay(uiState = uiState)

        // Camera Manipulator Overlays
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

        // Dialogs
        if (displayWarning) {
            SecurityWarningDialog(
                onDismiss = {
                    displayWarning = false
                    sharedPrefs.edit { putInt("warning_count", warningCount + 1) }
                })
        }
    }
}

@Composable
private fun BoxScope.StandardControlsOverlay(
    uiState: RouteUiState, onFlyClicked: () -> Unit
) {
    Button(
        onClick = onFlyClicked,
        enabled = uiState is RouteUiState.Success,
        modifier = Modifier
            .align(Alignment.BottomCenter)
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
                },
                valueRange = 0f..kotlin.math.max(1f, totalDistance),
                modifier = Modifier.weight(1f).semantics { contentDescription = "Progress Slider" },
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
        else -> { /* Do nothing */ }
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
        delay(3000)
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
        delay(3000)
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
