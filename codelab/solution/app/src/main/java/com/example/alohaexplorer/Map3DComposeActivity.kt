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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.alohaexplorer.MainActivity.Companion.HONOLULU
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.Map3DOptions
import com.google.android.gms.maps3d.Map3DView
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.flyAroundOptions
import com.google.android.gms.maps3d.model.flyToOptions
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Configure the initial state of the 3D Map.
 * This global configuration ensures the map starts where we want it (High above Honolulu).
 */
val map3DOptions = Map3DOptions(
    defaultUiDisabled = true,
    centerLat = HONOLULU.latitude,
    centerLng = HONOLULU.longitude,
    centerAlt = 1000.0,
    heading = 0.0,
    tilt = 0.0,
    roll = 0.0,
    range = 10_000_000.0, // Start with a "Global View" from space
    minHeading = 0.0,
    maxHeading = 360.0,
    minTilt = 0.0,
    maxTilt = 90.0,
    bounds = null,
    mapMode = Map3DMode.HYBRID,
    mapId = null,
)

/**
 * A simple activity to demonstrate how to integrate [Map3DView] with Jetpack Compose.
 */
class Map3DComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                Map3DContainer(
                    modifier = Modifier.padding(innerPadding),
                    options = map3DOptions
                )
            }
        }
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
    options: Map3DOptions,
) {
    // We use a state holder to remember the GoogleMap3D object once it's initialized.
    // This allows us to interact with the GoogleMap3D object later in response to UI events.
    // In a more complex app, we would normally hold this state in a ViewModel or similar.
    //
    // https://github.com/googlemaps-samples/android-maps3d-samples/tree/main/Maps3DSamples/advanced
    // presents a more complex reference sample.
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
                Map3DView(context, options).apply {
                    // We must manually call the lifecycle methods on the View.
                    // Ideally, you would wire this up to the LifecycleOwner, but for
                    // simplicity in this codelab, we just call onCreate here.
                    onCreate(null)
                }
            },
            
            // update: Called when the Composable recomposes.
            // We use this to attach listeners or update properties.
            update = { view ->
                view.getMap3DViewAsync(
                    object : OnMap3DViewReadyCallback {
                        override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
                            // Save the googleMap3D for UI events.
                            googleMap = googleMap3D
                        }

                        override fun onError(error: Exception) {
                            googleMap = null
                            throw error
                        }
                    }
                )
            },
            
            // onRelease: Called when this Composable is removed from the composition.
            // We clean up the View here to prevent memory leaks and stop the renderer.
            onRelease = { view ->
                googleMap = null
                view.onDestroy()
            }
        )

        // A native Compose Button sitting on top of the AndroidView
        Button(
            // We can only click if the map is loaded, and we are not already animating
            enabled = googleMap != null && animationJob == null,
            onClick = {
                animationJob?.cancel()

                // Launch a coroutine to run our cinematic sequence
                animationJob = coroutineScope.launch {

                    googleMap?.flyCameraTo(flyToOptions {
                        endCamera = camera {
                            center = HONOLULU
                            tilt = 45.0
                            range = 20000.0
                        }
                        durationInMillis = 2000L
                    })

                    // Wait for the fly-to to finish
                    googleMap?.let { map -> awaitCameraAnimation(map) }

                    googleMap?.flyCameraAround(flyAroundOptions {
                        center = camera {
                            center = HONOLULU
                            tilt = 45.0
                            range = 20000.0
                        }
                        rounds = 1.0
                        durationInMillis = 5000L
                    })

                    // Wait for the fly-around to finish
                    googleMap?.let { map -> awaitCameraAnimation(map) }

                    animationJob = null
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            Text("Fly to Honolulu")
        }
    }
}

private suspend fun awaitCameraAnimation(map: GoogleMap3D) = suspendCancellableCoroutine { continuation ->
    map.setCameraAnimationEndListener {
        map.setCameraAnimationEndListener(null) // Cleanup
        if (continuation.isActive) {
            continuation.resume(Unit)
        }
    }

    // Remove listener if coroutine is cancelled
    continuation.invokeOnCancellation {
        map.setCameraAnimationEndListener(null)
    }
}
