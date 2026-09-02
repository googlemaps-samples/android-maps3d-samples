// Copyright 2026 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.placesuikit3d

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.placesuikit3d.data.model.Camera3DTarget
import com.example.placesuikit3d.data.model.PlaceSearchResult
import com.example.placesuikit3d.ui.animation.Camera3DAnimator
import com.example.placesuikit3d.ui.compose.FloatingCameraControls
import com.example.placesuikit3d.ui.compose.PlaceSearchTopBar
import com.example.placesuikit3d.ui.theme.PlacesUIKit3DTheme
import com.example.placesuikit3d.ui.viewmodel.Camera3DMode
import com.example.placesuikit3d.ui.viewmodel.PlaceSearch3DViewModel
import com.example.placesuikit3d.ui.viewmodel.PlaceSearchUiState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.Map3DInitConfig
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.CollisionBehavior
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.Marker
import com.google.android.gms.maps3d.model.PinConfiguration
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.gms.maps3d.model.markerOptions
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.PlaceDetailsCompactFragment
import com.google.android.libraries.places.widget.PlaceLoadListener
import com.google.android.libraries.places.widget.model.Orientation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Main Activity hosting the Places UI Kit & Google Maps 3D Showcase in Pure Jetpack Compose.
 *
 * ### Architectural Pipeline & Step-by-Step Flow:
 * 1. **Initialization:** Hosts the 3D Map within Jetpack Compose via a FragmentContainerView
 *    and registers the [OnMap3DViewReadyCallback].
 * 2. **Reactive State Observation:** Collects immutable [PlaceSearch3DScreenState] from [PlaceSearch3DViewModel]
 *    using Unidirectional Data Flow (UDF).
 * 3. **Camera Kinematics:** Observes [Camera3DMode] state changes and drives smooth [Camera3DAnimator]
 *    fly-to transitions and continuous 360-degree orbit loops.
 * 4. **3D Marker Pipeline:** Diffs and renders [PlaceSearchResult] markers with custom pin styles,
 *    [AltitudeMode.RELATIVE_TO_MESH], elevation offsets, and [isExtruded] = true for white vertical stems.
 * 5. **Places Autocomplete:** Hosts [PlaceSearchTopBar] powered by Google Places Compose
 *    (`places-compose`) with automatic debouncing and prediction filtering.
 * 6. **Place Details Integration:** Displays Google Places UI Kit [PlaceDetailsCompactFragment]
 *    over the 3D map when a marker or search result is inspected.
 */
@AndroidEntryPoint
class MainActivity :
    AppCompatActivity(),
    OnMap3DViewReadyCallback {
    private val tag = this::class.java.simpleName
    private var googleMap3D: GoogleMap3D? = null
    private val cameraAnimator = Camera3DAnimator()
    private val activeMarkers = mutableMapOf<String, Marker>()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private val viewModel: PlaceSearch3DViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                    fetchLastLocation()
                } else {
                    Toast.makeText(this, getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show()
                    moveToDefaultLocation()
                }
            }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            PlacesUIKit3DTheme {
                MainScreen()
            }
        }
    }

    /**
     * Top-level declarative UI composition hosting the 3D map, bottom sheet, search bar,
     * floating camera controls, and place details overlay.
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainScreen() {
        // Step 1: Collect immutable UI state from the ViewModel
        val screenState by viewModel.screenState.collectAsState()
        val scope = rememberCoroutineScope()
        val keyboardController = LocalSoftwareKeyboardController.current
        val focusManager = LocalFocusManager.current

        val scaffoldState = rememberBottomSheetScaffoldState(
            bottomSheetState = rememberStandardBottomSheetState(
                initialValue = SheetValue.PartiallyExpanded,
            ),
        )
        val sheetPeekHeight = 120.dp

        // Step 2: Reactive Camera Animation - fly to target or start continuous orbit on state change
        LaunchedEffect(screenState.cameraMode) {
            when (val mode = screenState.cameraMode) {
                is Camera3DMode.FlyingTo -> {
                    cameraAnimator.flyToTarget(mode.target)
                }

                is Camera3DMode.Orbiting -> {
                    cameraAnimator.startOrbit(mode.center, range = mode.radius)
                }

                is Camera3DMode.Standard3D -> {
                    cameraAnimator.stopOrbit()
                }
            }
        }

        // Step 3: Reactive 3D Marker Updates - diff and update map markers when places or selection changes
        val effectiveSelectedId = screenState.selectedPlaceId ?: screenState.selectedPlace?.id
        LaunchedEffect(screenState.allMarkers, effectiveSelectedId) {
            updateMapMarkers(screenState.allMarkers, effectiveSelectedId)
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            // Step 4: Bottom Sheet Scaffold hosting the Landmark/Place Explorer List
            BottomSheetScaffold(
                scaffoldState = scaffoldState,
                sheetPeekHeight = sheetPeekHeight,
                sheetContent = {
                    LandmarkList(
                        places = screenState.allMarkers,
                        onPlaceClick = { place ->
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            val target = place.toCameraTarget()
                            cameraAnimator.flyToTarget(target)
                            viewModel.onPlaceSelected(place.id)
                            scope.launch {
                                scaffoldState.bottomSheetState.partialExpand()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 600.dp),
                    )
                },
            ) { _ ->
                Box(modifier = Modifier.fillMaxSize()) {
                    // Step 5: Full-screen 3D Map View Container
                    MapViewContainer(
                        onMapTouched = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        },
                    )

                    // Step 6: Places Autocomplete Top Bar (powered by Google Places Compose)
                    PlaceSearchTopBar(
                        query = screenState.searchQuery,
                        onQueryChange = { viewModel.onQueryChanged(it) },
                        predictions = screenState.autocompleteSuggestions,
                        onPlaceSelected = { place ->
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            viewModel.onSuggestionSelected(place)
                        },
                        isLoading = screenState.searchUiState is PlaceSearchUiState.Loading,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 40.dp),
                    )

                    // Animated Action Feedback Toast Banner
                    val infoMessage = screenState.infoMessage
                    LaunchedEffect(infoMessage) {
                        if (!infoMessage.isNullOrEmpty()) {
                            kotlinx.coroutines.delay(3200)
                            viewModel.clearInfoMessage()
                        }
                    }
                    AnimatedVisibility(
                        visible = !infoMessage.isNullOrEmpty(),
                        enter = fadeIn() + slideInVertically { -it },
                        exit = fadeOut() + slideOutVertically { -it },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 105.dp),
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.92f),
                            shadowElevation = 6.dp,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.inverseOnSurface,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .padding(end = 6.dp),
                                )
                                Text(
                                    text = infoMessage.orEmpty(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.inverseOnSurface,
                                )
                            }
                        }
                    }

                    // Step 7: Floating Camera Controls (Orbit 360, 2D/3D Pitch, Reset Overview, My Location)
                    FloatingCameraControls(
                        isOrbiting = screenState.isOrbiting,
                        is3DView = screenState.is3DView,
                        onToggleOrbit = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            viewModel.toggleOrbit(getCurrentMapCenter())
                        },
                        onToggle2D3D = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            viewModel.toggle2D3DView(getCurrentMapCenter())
                        },
                        onResetOverview = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            viewModel.resetCameraOverview()
                        },
                        onMyLocationClick = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            fetchLastLocation()
                        },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 8.dp),
                    )
                }
            }

            // Step 8: Place Details Compact Overlay (Google Places UI Kit) with smooth slide-up animation
            AnimatedVisibility(
                visible = !screenState.selectedPlaceId.isNullOrEmpty(),
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = sheetPeekHeight + 16.dp, start = 16.dp, end = 16.dp),
            ) {
                val selectedPlaceId = screenState.selectedPlaceId
                if (!selectedPlaceId.isNullOrEmpty()) {
                    PlaceDetailsOverlay(
                        placeId = selectedPlaceId,
                        onDismiss = { viewModel.dismissPlaceDetails() },
                    )
                }
            }
        }
    }

    @Composable
    fun MapViewContainer(onMapTouched: () -> Unit = {}) {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current

        val map3DView = remember {
            val config = Map3DInitConfig.create(
                centerLat = 39.9989,
                centerLng = -105.2828,
                centerAlt = 1750.0,
                heading = 25.0,
                tilt = 50.0,
                roll = 0.0,
                range = 1400.0,
                minAltitude = 0.0,
                maxAltitude = 1000000.0,
                minHeading = 0.0,
                maxHeading = 360.0,
                minTilt = 0.0,
                maxTilt = 90.0,
                bounds = null,
                mapMode = Map3DMode.HYBRID,
                mapId = null,
                language = java.util.Locale.getDefault().language,
                region = java.util.Locale.getDefault().country,
            )
            com.google.android.gms.maps3d.Map3DView(context, config).apply {
                getMap3DViewAsync(this@MainActivity)
            }
        }

        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_CREATE -> map3DView.onCreate(null)
                    Lifecycle.Event.ON_RESUME -> map3DView.onResume()
                    Lifecycle.Event.ON_PAUSE -> map3DView.onPause()
                    Lifecycle.Event.ON_DESTROY -> map3DView.onDestroy()
                    else -> {}
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        AndroidView(
            factory = { map3DView },
            modifier = Modifier
                .fillMaxSize()
                .testTag("map3d_view")
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            onMapTouched()
                        },
                    )
                },
        )
    }

    /**
     * Composable overlay hosting the Places UI Kit [PlaceDetailsCompactFragment].
     */
    @Composable
    fun PlaceDetailsOverlay(
        placeId: String,
        onDismiss: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        val containerId = remember { View.generateViewId() }

        LaunchedEffect(placeId) {
            val fragment = supportFragmentManager.findFragmentById(containerId) as? PlaceDetailsCompactFragment
            if (fragment != null) {
                Log.d(tag, "Updating existing fragment for placeId: $placeId")
                fragment.loadWithPlaceId(placeId)
            }
        }

        Box(
            modifier = modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium),
        ) {
            AndroidView(
                factory = { ctx ->
                    FragmentContainerView(ctx).apply {
                        id = containerId
                        val newFragment = PlaceDetailsCompactFragment.newInstance(
                            PlaceDetailsCompactFragment.ALL_CONTENT,
                            Orientation.VERTICAL,
                            R.style.CustomizedPlaceDetailsTheme,
                        ).apply {
                            setPlaceLoadListener(object : PlaceLoadListener {
                                override fun onSuccess(place: Place) {
                                    Log.d(tag, "Place loaded: ${place.id}")
                                }

                                override fun onFailure(e: Exception) {
                                    Log.e(tag, "Place failed to load for ID: $placeId", e)
                                }
                            })
                        }

                        supportFragmentManager.commit {
                            replace(containerId, newFragment)
                        }

                        post { newFragment.loadWithPlaceId(placeId) }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            FloatingActionButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_close),
                    contentDescription = androidx.compose.ui.res.stringResource(id = R.string.dismiss_button_content_description),
                )
            }
        }

        DisposableEffect(containerId) {
            onDispose {
                supportFragmentManager.findFragmentById(containerId)?.let {
                    supportFragmentManager.commit {
                        remove(it)
                    }
                }
            }
        }
    }

    private var lastSelectedMarkerId: String? = null

    /**
     * Diffs and updates 3D markers on the [GoogleMap3D] instance.
     *
     * ### 3D Marker Configuration:
     * - **Diffing Optimization:** Only recreates markers when they are newly added or when their
     *   selection state changes (e.g. from unselected to selected scale).
     * - **Custom Styling:** Uses [PinConfiguration] to render Google Red pins, scaled up when selected.
     * - **Elevation & Extrusion Pole:** Uses [AltitudeMode.RELATIVE_TO_MESH] with an altitude offset
     *   (12m unselected, 16m selected) and `isExtruded = true` to render the distinctive white
     *   vertical stem anchoring the pin to the rooftop or terrain surface.
     * - **Occlusion Handling:** `isDrawnWhenOccluded = true` guarantees the pin remains visible through structures.
     * - **Interaction:** Tapping a marker animates the 3D camera to focus on it and selects the place.
     *
     * @param places The current list of search result places to render.
     * @param selectedId The ID of the currently selected place, if any.
     */
    private fun updateMapMarkers(places: List<PlaceSearchResult>, selectedId: String?) {
        val map = googleMap3D ?: return

        val currentIds = places.map { it.id }.toSet()
        val iterator = activeMarkers.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.key !in currentIds) {
                entry.value.remove()
                iterator.remove()
            }
        }

        val previousSelectedId = lastSelectedMarkerId
        lastSelectedMarkerId = selectedId

        places.forEach { place ->
            val isSelected = place.id == selectedId || place.isSelected
            val existingMarker = activeMarkers[place.id]

            // Recreate marker only if it doesn't exist, OR if its selection state changed!
            val wasSelected = place.id == previousSelectedId
            val needsUpdate = existingMarker == null || (isSelected != wasSelected)

            if (!needsUpdate) {
                return@forEach
            }

            if (existingMarker != null) {
                existingMarker.remove()
                activeMarkers.remove(place.id)
            }

            val pinConfig = PinConfiguration.builder()
                .setBackgroundColor(Color.parseColor("#EA4335"))
                .setBorderColor(Color.parseColor("#C5221F"))
                .setScale(if (isSelected) 1.35f else 1.0f)
                .build()

            val markerPosition = latLngAltitude {
                latitude = place.location.latitude
                longitude = place.location.longitude
                altitude = if (isSelected) 16.0 else 12.0
            }

            val marker = map.addMarker(
                markerOptions {
                    id = place.id
                    position = markerPosition
                    label = place.name
                    setStyle(pinConfig)
                    altitudeMode = AltitudeMode.RELATIVE_TO_MESH
                    isExtruded = true
                    isDrawnWhenOccluded = true
                    collisionBehavior = CollisionBehavior.REQUIRED
                    isSizePreserved = true
                },
            )
            if (marker != null) {
                marker.setClickListener {
                    runOnUiThread {
                        val target = place.toCameraTarget()
                        cameraAnimator.flyToTarget(target)
                        viewModel.onMarkerClicked(place)
                    }
                }
                activeMarkers[place.id] = marker
            }
        }
    }

    /**
     * Callback invoked when the [GoogleMap3D] instance is initialized and ready for commands.
     *
     * Performs synchronized initialization:
     * 1. Caches the [GoogleMap3D] reference in [cameraAnimator].
     * 2. Sets the display mode to [Map3DMode.HYBRID] for photorealistic 3D satellite imagery with street labels.
     * 3. Renders the initial markers from [viewModel].
     * 4. Flies to the active camera target (or defaults to the Boulder overview).
     * 5. Registers a 3D click listener to inspect buildings and POIs.
     */
    override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
        this.googleMap3D = googleMap3D
        cameraAnimator.setMap(googleMap3D)

        googleMap3D.setMapMode(Map3DMode.HYBRID)

        val currentState = viewModel.screenState.value
        val initialSelectedId = currentState.selectedPlaceId ?: currentState.selectedPlace?.id
        updateMapMarkers(currentState.allMarkers, initialSelectedId)

        val activeMode = currentState.cameraMode
        if (activeMode is Camera3DMode.FlyingTo) {
            cameraAnimator.flyToTarget(activeMode.target)
        } else {
            googleMap3D.setCamera(Camera3DTarget.DEFAULT.toCamera())
        }

        googleMap3D.setMap3DClickListener { _, placeId ->
            if (!placeId.isNullOrEmpty()) {
                runOnUiThread {
                    viewModel.onPlaceSelected(placeId)
                }
            }
        }
    }

    private fun getCurrentMapCenter(): LatLngAltitude? {
        val camera = googleMap3D?.getCamera() ?: return null
        return camera.center
    }

    private fun isLocationPermissionGranted(): Boolean = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun requestLocationPermissions() {
        requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    @SuppressLint("MissingPermission")
    private fun fetchLastLocation() {
        if (isLocationPermissionGranted()) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val userLocation = Camera3DTarget(
                        latitude = it.latitude,
                        longitude = it.longitude,
                        altitude = it.altitude,
                        heading = 25.0,
                        tilt = 50.0,
                        range = 1400.0,
                    )
                    cameraAnimator.flyToTarget(userLocation)
                } ?: run {
                    Toast.makeText(this, getString(R.string.location_services_disabled), Toast.LENGTH_LONG).show()
                    moveToDefaultLocation()
                }
            }.addOnFailureListener {
                Toast.makeText(this, getString(R.string.location_services_disabled), Toast.LENGTH_LONG).show()
                moveToDefaultLocation()
            }
        } else {
            requestLocationPermissions()
        }
    }

    private fun moveToDefaultLocation() {
        cameraAnimator.flyToTarget(Camera3DTarget.DEFAULT)
    }

    override fun onError(error: Exception) {
        Log.e(tag, "Error loading 3D map", error)
        super.onError(error)
    }
}
