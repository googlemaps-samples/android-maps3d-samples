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

package com.example.composedemos.placedetails

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.composedemos.R
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.PlaceDetailsCompactFragment
import com.google.android.libraries.places.widget.PlaceLoadListener
import com.google.android.libraries.places.widget.model.Orientation
import com.google.maps.android.compose3d.GoogleMap3D
import com.google.maps.android.compose3d.utils.toValidCamera
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * A simple ViewModel to hold the selected place ID without using Hilt.
 */
class PlaceDetailsViewModel : ViewModel() {
    companion object {
        val initialCamera: Camera = camera {
            center = latLngAltitude {
                latitude = 39.982129291022446
                longitude = -105.30156359691158
                altitude = 2483.5 // Approx 8148 feet
            }
            heading = 26.0
            tilt = 67.0
            range = 2500.0
        }.toValidCamera()
    }

    val landmarks: List<Landmark> = listOf(
        Landmark(
            id = "ChIJfXOTtWbsa4cRmW07qJRB6_8",
            name = "The Flatirons",
            location = latLngAltitude {
                latitude = 39.9880
                longitude = -105.2930
                altitude = 2100.0
            },
        ),
        Landmark(
            id = "ChIJwd_EEkfsa4cRqy6eShKXFXY",
            name = "Chautauqua Park",
            location = latLngAltitude {
                latitude = 39.9989
                longitude = -105.2828
                altitude = 1750.0
            },
        ),
        Landmark(
            id = "ChIJiTEGLibsa4cRepH7ZMFEcJ8",
            name = "Pearl Street Mall",
            location = latLngAltitude {
                latitude = 40.0177
                longitude = -105.2819
                altitude = 1620.0
            },
        ),
        Landmark(
            id = "ChIJwR6cajTsa4cR2TH0qKTVKAM",
            name = "University of Colorado Boulder",
            location = latLngAltitude {
                latitude = 40.0076
                longitude = -105.2659
                altitude = 1650.0
            },
        ),
        Landmark(
            id = "ChIJAfFnzszva4cR04sAt0lSm1g",
            name = "Boulder Reservoir",
            location = latLngAltitude {
                latitude = 40.0780
                longitude = -105.2220
                altitude = 1580.0
            },
        ),
    )

    private val _placeId = MutableStateFlow<String?>(null)
    val placeId: StateFlow<String?> = _placeId.asStateFlow()

    private val _cameraState = MutableStateFlow<Camera>(initialCamera)
    val cameraState: StateFlow<Camera> = _cameraState.asStateFlow()

    fun setSelectedPlaceId(placeId: String?) {
        _placeId.value = placeId
    }

    fun selectLandmark(landmark: Landmark) {
        setSelectedPlaceId(landmark.id)
        _cameraState.value = camera {
            center = landmark.location
            range = 1000.0
            tilt = 45.0
        }.toValidCamera()
    }
}

class PlaceDetailsActivity : FragmentActivity() {
    companion object {
        private const val TAG = "PlaceDetailsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                    val viewModel: PlaceDetailsViewModel = viewModel()

                    // Handle Intent extra for non-UI state injection (testing)
                    LaunchedEffect(Unit) {
                        intent.getStringExtra("place_name")?.let { name ->
                            delay(2000) // Delay a couple of seconds before showing
                            val landmark = viewModel.landmarks.find { it.name == name }
                            if (landmark != null) {
                                viewModel.selectLandmark(landmark)
                            }
                        }
                    }

                    MainScreen(viewModel)
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainScreen(viewModel: PlaceDetailsViewModel) {
        val landmarks = viewModel.landmarks
        val selectedPlaceId by viewModel.placeId.collectAsState()
        val scope = rememberCoroutineScope()
        val scaffoldState = rememberBottomSheetScaffoldState(
            bottomSheetState = rememberStandardBottomSheetState(
                initialValue = SheetValue.PartiallyExpanded,
            ),
        )
        val sheetPeekHeight = 120.dp
        val cameraState by viewModel.cameraState.collectAsState()

        // Dismiss the place details overlay if the user fully expands the bottom sheet
        LaunchedEffect(scaffoldState.bottomSheetState.currentValue) {
            if (scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
                viewModel.setSelectedPlaceId(null)
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            BottomSheetScaffold(
                scaffoldState = scaffoldState,
                sheetPeekHeight = sheetPeekHeight,
                sheetContent = {
                    LandmarkList(
                        landmarks = landmarks,
                        onLandmarkClick = { landmark ->
                            Log.d(TAG, "LANDMARK_CLICKED: ${landmark.name}")
                            viewModel.selectLandmark(landmark)
                            scope.launch {
                                scaffoldState.bottomSheetState.partialExpand()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp),
                    )
                },
            ) { _ ->
                Box(modifier = Modifier.fillMaxSize()) {
                    GoogleMap3D(
                        camera = cameraState,
                        mapMode = Map3DMode.HYBRID,
                        modifier = Modifier.fillMaxSize(),
                        onPlaceClick = { placeId ->
                            Log.d(TAG, "Place clicked: placeId=$placeId")
                            viewModel.setSelectedPlaceId(placeId)
                        },
                    )
                }
            }

            // Overlay stays on top of the scaffold
            if (!selectedPlaceId.isNullOrEmpty()) {
                PlaceDetailsOverlay(
                    placeId = selectedPlaceId!!,
                    onDismiss = { viewModel.setSelectedPlaceId(null) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = sheetPeekHeight + 16.dp, start = 16.dp, end = 16.dp),
                )
            }
        }
    }

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
                Log.d(TAG, "Updating existing fragment for new placeId: $placeId")
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
                            R.style.BoulderNatureHippieTheme, // Use our custom theme!
                        ).apply {
                            setPlaceLoadListener(object : PlaceLoadListener {
                                override fun onSuccess(place: Place) {
                                    Log.d(TAG, "Place loaded: ${place.id}")
                                }

                                override fun onFailure(e: Exception) {
                                    Log.e(TAG, "Place failed to load for ID: $placeId", e)
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
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                )
            }
        }

        androidx.compose.runtime.DisposableEffect(containerId) {
            onDispose {
                supportFragmentManager.findFragmentById(containerId)?.let {
                    supportFragmentManager.beginTransaction()
                        .remove(it)
                        .commitAllowingStateLoss()
                }
            }
        }
    }
}
