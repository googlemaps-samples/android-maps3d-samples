package com.example.advancedmaps3dsamples.animatedpolygon

import com.example.advancedmaps3dsamples.scenarios.ScenariosViewModel
import com.example.advancedmaps3dsamples.scenarios.ThreeDMap
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import android.app.Activity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material.icons.filled.Search
import com.example.advancedmaps3dsamples.R
import com.example.advancedmaps3dsamples.ui.theme.AdvancedMaps3DSamplesTheme
import com.example.advancedmaps3dsamples.utils.toValidCamera
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.android.gms.maps3d.model.polygonOptions
import dagger.hilt.android.AndroidEntryPoint
import com.google.android.gms.maps3d.Map3DOptions
import kotlinx.coroutines.delay

/**
 * Demonstrates how to programmatically control the camera's altitude and render 3D polygons
 * dynamically based on user input and animation states.
 * 
 * This activity showcases integration between Jetpack Compose UI (sliders, dialogs)
 * and the Google Maps 3D SDK, specifically highlighting how to bind Compose state
 * to 3D map properties like object altitude and camera position.
 */
@AndroidEntryPoint
class AnimatedPolygonActivity : ComponentActivity() {

    private val viewModel by viewModels<ScenariosViewModel>()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel.setMapMode(Map3DMode.SATELLITE)
        var showSettingsDialog by mutableStateOf(false)

        setContent {
            val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.let { intent ->
                        // Retrieve the Place selected by the user.
                        val place = Autocomplete.getPlaceFromIntent(intent)
                        place.latLng?.let { latLng ->
                            // When a new location is picked, we persist the coordinates to DataStore
                            // so that the 3D polygon's geometry is recalculated around this new center.
                            viewModel.savePolygonCenter(latLng.latitude, latLng.longitude)
                            
                            // Reset the user's zoom/altitude bounds. Since we don't know the exact elevation of the new place,
                            // expanding the bounds from 0 to 15K feet allows them to easily find the polygon.
                            viewModel.saveMinAltitude(0f)
                            viewModel.saveMaxAltitude(15000f)
                            
                            // Immediately fly the camera to the new location to center it in the view,
                            // overriding whatever camera position the user previously had.
                            val newCam = camera {
                                center = latLngAltitude {
                                    latitude = latLng.latitude
                                    longitude = latLng.longitude
                                    altitude = 10000.0
                                }
                                heading = 0.0
                                tilt = 0.0
                            }
                            viewModel.setCamera(newCam)
                            
                            // Force a save of the new camera right away so that if the user backgrounds
                            // the app before the map becomes "steady", they don't lose this new location.
                            viewModel.saveCameraSettings(newCam)
                        }
                    }
                }
            }

            AdvancedMaps3DSamplesTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            colors = topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.primary,
                            ),
                            title = {
                                Text(stringResource(R.string.map_sample_animated_polygon))
                            },
                            actions = {
                                IconButton(onClick = { 
                                    val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
                                    val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                                        .build(this@AnimatedPolygonActivity)
                                    launcher.launch(intent)
                                }) {
                                    Icon(
                                        imageVector = Icons.Filled.Search,
                                        contentDescription = "Search Location"
                                    )
                                }
                                IconButton(onClick = { showSettingsDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Filled.Settings,
                                        contentDescription = "Settings"
                                    )
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    AnimatedPolygon(
                        viewModel = viewModel,
                        showSettingsDialog = showSettingsDialog,
                        onDismissSettings = { showSettingsDialog = false },
                        modifier = Modifier.padding(innerPadding).fillMaxSize()
                    )
                }
            }
        }
    }
}

/**
 * The main UI content for the Animated Polygon.
 * 
 * This composable orchestrates the interaction between the user controls (slider, play/pause button),
 * the persisted settings (min/max altitude, sweep speed), and the [ThreeDMap] instance.
 * It is responsible for hoisting the state required by the map and the UI, ensuring both
 * remain synchronized without unnecessary recompositions.
 *
 * @param viewModel The [ScenariosViewModel] providing access to persisted DataStore settings and map state.
 * @param showSettingsDialog Controls the visibility of the configuration dialog.
 * @param onDismissSettings Callback invoked when the settings dialog should be closed.
 * @param modifier Standard Compose modifier for layout adjustments.
 */
@Composable
fun AnimatedPolygon(
  viewModel: ScenariosViewModel,
  showSettingsDialog: Boolean,
  onDismissSettings: () -> Unit,
  modifier: Modifier = Modifier,
) {
    val minAltitude by viewModel.minAltitudeFlow.collectAsStateWithLifecycle(initialValue = 5000f)
    val maxAltitude by viewModel.maxAltitudeFlow.collectAsStateWithLifecycle(initialValue = 10000f)
    val sweepSpeed by viewModel.sweepSpeedFlow.collectAsStateWithLifecycle(initialValue = 50f)
    val savedCamera by viewModel.savedCameraFlow.collectAsStateWithLifecycle(initialValue = null)
    
    val centerLat by viewModel.polygonCenterLatFlow.collectAsStateWithLifecycle(initialValue = 40.0)
    val centerLng by viewModel.polygonCenterLngFlow.collectAsStateWithLifecycle(initialValue = -105.5)
    val widthMiles by viewModel.polygonWidthMilesFlow.collectAsStateWithLifecycle(initialValue = 26f)
    val heightMiles by viewModel.polygonHeightMilesFlow.collectAsStateWithLifecycle(initialValue = 27f)
    
    // Ensure altitude is clamped within the min and max bounds
    var altitudeFeet by remember(minAltitude, maxAltitude) { 
        mutableFloatStateOf((minAltitude + maxAltitude) / 2f) 
    }
    
    var isSweeping by remember { mutableStateOf(false) }
    var sweepDirection by remember(sweepSpeed) { mutableFloatStateOf(sweepSpeed) }

    val boulderPolygonPoints = remember(centerLat, centerLng, widthMiles, heightMiles) {
        val center = LatLng(centerLat, centerLng)
        val widthMeters = widthMiles * 1609.34
        val heightMeters = heightMiles * 1609.34
        
        // 0=North, 90=East, 180=South, 270=West
        val north = com.google.maps.android.SphericalUtil.computeOffset(center, heightMeters / 2.0, 0.0)
        val south = com.google.maps.android.SphericalUtil.computeOffset(center, heightMeters / 2.0, 180.0)
        
        // Compute corners by offsetting north/south points east/west
        val nw = com.google.maps.android.SphericalUtil.computeOffset(north, widthMeters / 2.0, 270.0)
        val ne = com.google.maps.android.SphericalUtil.computeOffset(north, widthMeters / 2.0, 90.0)
        val sw = com.google.maps.android.SphericalUtil.computeOffset(south, widthMeters / 2.0, 270.0)
        val se = com.google.maps.android.SphericalUtil.computeOffset(south, widthMeters / 2.0, 90.0)

        listOf(
            latLngAltitude { latitude = nw.latitude; longitude = nw.longitude; altitude = 0.0 }, // NW
            latLngAltitude { latitude = ne.latitude; longitude = ne.longitude; altitude = 0.0 }, // NE
            latLngAltitude { latitude = se.latitude; longitude = se.longitude; altitude = 0.0 }, // SE
            latLngAltitude { latitude = sw.latitude; longitude = sw.longitude; altitude = 0.0 }  // SW
        )
    }

    val mapReady by viewModel.mapReady.collectAsStateWithLifecycle(initialValue = false)
    
    // Observe changes to the current altitude and map readiness state.
    // When the map is ready and the altitude changes (either via user drag or animation sweep),
    // we recalculate the 3D polygon's path to reflect the new height.
    LaunchedEffect(altitudeFeet, mapReady) {
        if (!mapReady) return@LaunchedEffect
        
        // The Maps 3D SDK expects altitude in meters, but our UI operates in feet for user convenience.
        val altitudeMeters = altitudeFeet * 0.3048
        val pointsWithAlt = boulderPolygonPoints.map {
            latLngAltitude {
                latitude = it.latitude
                longitude = it.longitude
                altitude = altitudeMeters
            }
        }

        // To prevent flickering when updating an existing Map Object (like a Polygon),
        // we must NOT remove and re-add it. Instead, we retrieve its ID and build a new
        // Options object with that same ID. The SDK will gracefully update the existing geometry in-place.
        val existingPolygon = viewModel.getPolygon("boulder_polygon")
        if (existingPolygon != null) {
            val polygon = polygonOptions {
                id = existingPolygon.id
                altitudeMode = AltitudeMode.ABSOLUTE
                path = pointsWithAlt
                fillColor = Color.argb(128, 255, 0, 0) // Semi-transparent red
                strokeColor = Color.RED
                strokeWidth = 2.0
            }
            viewModel.addPolygon((polygon))

        } else {
            val polygon = polygonOptions {
                id = "boulder_polygon"
                altitudeMode = AltitudeMode.ABSOLUTE
                path = pointsWithAlt
                fillColor = Color.argb(128, 255, 0, 0) // Semi-transparent red
                strokeColor = Color.RED
                strokeWidth = 2.0
            }
            viewModel.addPolygon(polygon)
        }
    }

    // Manages the continuous "sweep" animation loop.
    // This coroutine runs as long as `isSweeping` is true, incrementing/decrementing
    // the altitude on each frame and reversing direction when bounds are hit.
    LaunchedEffect(isSweeping) {
        while (isSweeping) {
            // Target ~60 FPS. While delay isn't a precise frame-timing mechanism,
            // it's sufficient for this simple demonstration sweep.
            delay(16) // ~60 fps
            altitudeFeet += sweepDirection
            if (altitudeFeet >= maxAltitude) {
                altitudeFeet = maxAltitude
                sweepDirection = -Math.abs(sweepDirection)
            } else if (altitudeFeet <= minAltitude) {
                altitudeFeet = minAltitude
                sweepDirection = Math.abs(sweepDirection)
            }
        }
    }

    Column(modifier = modifier) {
        // The camera requires an initial position when the map is created.
        // We attempt to restore the last known camera position from DataStore.
        // If no saved position exists (e.g., first launch), we fallback to a default view of Boulder, CO.
        val defaultCamera = remember {
            Camera(
                latLngAltitude { latitude = 39.97503; longitude = -105.28518; altitude = 1911.35 },
                355.95,
                61.74,
                0.0,
                6702.39
            ).toValidCamera()
        }
        val initialCamera = savedCamera ?: defaultCamera

        val options = remember(initialCamera) {
            Map3DOptions(
                centerLat = initialCamera.center.latitude,
                centerLng = initialCamera.center.longitude,
                centerAlt = initialCamera.center.altitude,
                heading = initialCamera.heading ?: 0.0,
                tilt = initialCamera.tilt ?: 0.0,
                roll = initialCamera.roll ?: 0.0,
                range = initialCamera.range ?: 15000.0,
            )
        }

        ThreeDMap(
            modifier = Modifier.weight(1f),
            options = options,
            viewModel = viewModel,
        )

        if (showSettingsDialog) {
            var tempMin by remember(minAltitude) { mutableStateOf(minAltitude.toInt().toString()) }
            var tempMax by remember(maxAltitude) { mutableStateOf(maxAltitude.toInt().toString()) }
            var tempSpeed by remember(sweepSpeed) { mutableStateOf(sweepSpeed.toInt().toString()) }
            var tempWidth by remember(widthMiles) { mutableStateOf(widthMiles.toString()) }
            var tempHeight by remember(heightMiles) { mutableStateOf(heightMiles.toString()) }

            AlertDialog(
                onDismissRequest = onDismissSettings,
                title = { Text("Settings") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = tempMin,
                            onValueChange = { tempMin = it },
                            label = { Text("Min Altitude (ft)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = tempMax,
                            onValueChange = { tempMax = it },
                            label = { Text("Max Altitude (ft)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = tempSpeed,
                            onValueChange = { tempSpeed = it },
                            label = { Text("Sweep Speed (ft/frame)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = tempWidth,
                            onValueChange = { tempWidth = it },
                            label = { Text("Width (miles)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = tempHeight,
                            onValueChange = { tempHeight = it },
                            label = { Text("Height (miles)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        tempMin.toFloatOrNull()?.let { viewModel.saveMinAltitude(it) }
                        tempMax.toFloatOrNull()?.let { viewModel.saveMaxAltitude(it) }
                        tempSpeed.toFloatOrNull()?.let {
                            viewModel.saveSweepSpeed(it)
                            // Match the current direction (positive or negative) with the new speed magnitude
                            sweepDirection = if (sweepDirection < 0) -it else it
                        }
                        
                        val w = tempWidth.toFloatOrNull()
                        val h = tempHeight.toFloatOrNull()
                        if (w != null && h != null) {
                            viewModel.savePolygonDimensions(w, h)
                        }
                        
                        onDismissSettings()
                    }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismissSettings) {
                        Text("Cancel")
                    }
                }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { isSweeping = !isSweeping }) {
                Icon(
                    imageVector = if (isSweeping) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isSweeping) "Pause sweep" else "Start sweep"
                )
            }
            Text(
                text = stringResource(id = R.string.altitude_label, altitudeFeet.toInt()),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(end = 16.dp)
            )
            val sliderRange = remember(minAltitude, maxAltitude) { 
                if (minAltitude < maxAltitude) minAltitude..maxAltitude else 0f..10000f 
            }
            Slider(
                value = altitudeFeet.coerceIn(sliderRange),
                onValueChange = { altitudeFeet = it },
                valueRange = sliderRange,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
