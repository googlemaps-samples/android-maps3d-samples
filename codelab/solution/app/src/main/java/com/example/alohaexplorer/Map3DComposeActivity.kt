package com.example.alohaexplorer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.Map3DInitConfig
import com.google.android.gms.maps3d.Map3DView
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.flyAroundOptions
import com.google.android.gms.maps3d.model.flyToOptions
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Configure the initial state of the 3D Map.
 * This global configuration ensures the map starts where we want it (High above Honolulu).
 */
val map3DConfig: Map3DInitConfig = Map3DInitConfig.create(
    centerLat = HONOLULU.latitude,
    centerLng = HONOLULU.longitude,
    centerAlt = 1000.0,
    heading = 0.0,
    range = 10_000_000.0, // Start with a "Global View" from space
    mapMode = Map3DMode.HYBRID,
)

/**
 * A minimalist activity to demonstrate how to integrate [Map3DView] with Jetpack Compose.
 */
class Map3DComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                Map3DContainer(
                    modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
                    config = map3DConfig
                )
            }
        }

        // Hide the system bars (status bar and navigation bar) for a full-screen 3D map
        // experience. Swiping from the screen edge reveals them transiently.
        // Remove these lines if you prefer to keep the system bars visible at all times.
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }
}

/**
 * A Composable wrapper for [Map3DView].
 *
 * This function handles the interop between Compose's declarative UI and the usage of the
 * classic Android View system required by the Maps3D SDK.
 */
@Composable
fun Map3DContainer(
    modifier: Modifier = Modifier,
    config: Map3DInitConfig,
) {
    // We use a state holder to remember the GoogleMap3D object once it's initialized.
    // This allows us to interact with the GoogleMap3D object later in response to UI events.
    // In a more complex app, we would normally hold this state in a ViewModel or similar.
    var googleMap by remember { mutableStateOf<GoogleMap3D?>(null) }

    // CoroutineScope to launch suspending functions.
    val coroutineScope = rememberCoroutineScope()

    var animationJob by remember { mutableStateOf<Job?>(null) }

    Box(modifier = modifier.fillMaxSize()) {

        // AndroidView is the bridge between Compose and Views.
        AndroidView(
            modifier = Modifier.fillMaxSize(),

            // factory: Called ONCE to create the View.
            // We initialize the specific Map3DView here.
            factory = { context ->
                Map3DView(context, config).apply {
                    // We must manually call the lifecycle methods on the View.
                    // Ideally, you would wire this up to the LifecycleOwner, but for
                    // simplicity in this codelab, we call onCreate and onResume here.
                    onCreate(null)
                    onResume()
                }
            },

            // update: Called when the Composable recomposes.
            // We use this to attach listeners or update properties.
            update = { view ->
                view.getMap3DViewAsync(
                    object : OnMap3DViewReadyCallback {
                        override fun onMap3DViewReady(map3D: GoogleMap3D) {
                            // Save the googleMap3D for UI events.
                            googleMap = map3D
                        }

                        override fun onError(e: Exception) {
                            googleMap = null
                            throw e
                        }
                    }
                )
            },

            // onRelease: Called when this Composable is removed from the composition.
            // We clean up the View here to prevent memory leaks and stop the renderer.
            onRelease = { view ->
                googleMap = null
                view.onPause()
                view.onDestroy()
            }
        )

        // A native Compose Button sitting on top of the AndroidView
        Button(
            // We can only click if the map is loaded, and we are not already animating
            enabled = googleMap != null && animationJob == null,
            onClick = {
                animationJob?.cancel()

                googleMap?.let { map ->
                    // Launch a coroutine to run our cinematic sequence
                    animationJob = coroutineScope.launch {
                        // 1. Fly to Honolulu and wait for the fly-to to finish
                        map.awaitCameraAnimation(
                            flyToOptions {
                                endCamera = camera {
                                    center = HONOLULU
                                    tilt = 45.0
                                    range = 20_000.0
                                }
                                durationInMillis = 2000L
                            }
                        )

                        // 2. Orbit and wait for the fly-around to finish
                        map.awaitCameraAnimation(
                            flyAroundOptions {
                                center = camera {
                                    center = HONOLULU
                                    tilt = 45.0
                                    range = 20_000.0
                                }
                                rounds = 1.0
                                durationInMillis = 5000L
                            }
                        )
                        animationJob = null
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            Text(stringResource(R.string.fly_to_honolulu))
        }
    }
}
