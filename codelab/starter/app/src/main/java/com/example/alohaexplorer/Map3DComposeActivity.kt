package com.example.alohaexplorer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
// import androidx.compose.foundation.layout.Box
// import androidx.compose.foundation.layout.fillMaxSize
// import androidx.compose.foundation.layout.padding
// import androidx.compose.material3.Button
// import androidx.compose.material3.Scaffold
// import androidx.compose.material3.Text
// import androidx.compose.runtime.Composable
// import androidx.compose.runtime.getValue
// import androidx.compose.runtime.mutableStateOf
// import androidx.compose.runtime.remember
// import androidx.compose.runtime.rememberCoroutineScope
// import androidx.compose.runtime.setValue
// import androidx.compose.ui.Alignment
// import androidx.compose.ui.Modifier
// import androidx.compose.ui.unit.dp
// import androidx.compose.ui.viewinterop.AndroidView
// import com.google.android.gms.maps3d.GoogleMap3D
// import com.google.android.gms.maps3d.Map3DMode
// import com.google.android.gms.maps3d.Map3DOptions
// import com.google.android.gms.maps3d.Map3DView
// import com.google.android.gms.maps3d.OnMap3DViewReadyCallback
// import com.google.android.gms.maps3d.model.camera
// import com.google.android.gms.maps3d.model.flyAroundOptions
// import com.google.android.gms.maps3d.model.flyToOptions
// import com.google.android.gms.maps3d.model.latLngAltitude
// import kotlinx.coroutines.Job
// import kotlinx.coroutines.launch
// import kotlinx.coroutines.suspendCancellableCoroutine
// import kotlin.coroutines.resume

// TODO: Step 12 - Jetpack Compose
/*
class Map3DComposeActivity : ComponentActivity() {

    private val map3DOptions = Map3DOptions(
        centerLat = MainActivity.HONOLULU.latitude,
        centerLng = MainActivity.HONOLULU.longitude,
        centerAlt = 1000.0,
        heading = 0.0,
        range = 10_000_000.0, // Start from space
        mapMode = Map3DMode.HYBRID
    )

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

@Composable
fun Map3DContainer(
    modifier: Modifier = Modifier,
    options: Map3DOptions
) {
    // State to hold the map controller
    var googleMap by remember { mutableStateOf<GoogleMap3D?>(null) }
    
    // Animation State
    var animationJob by remember { mutableStateOf<Job?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                 Map3DView(context, options).apply {
                    // Manually call onCreate. In a real app, wire this to LifecycleOwner.
                    onCreate(null)
                 }
            },
            update = { view ->
                view.getMap3DViewAsync(
                    object : OnMap3DViewReadyCallback {
                        override fun onMap3DViewReady(map3D: GoogleMap3D) {
                            googleMap = map3D
                            // No automatic startFromGlobalView here, we wait for button click
                        }
                        override fun onError(e: Exception) {
                            googleMap = null
                            throw e
                        }
                    }
                )
                // Forward Log lifecycle events if possible, or use a LifecycleEventObserver container
                view.onResume()
            },
            onRelease = { view -> 
                googleMap = null
                view.onDestroy() 
            }
        )
        
        Button(
            // Enable only if map is ready and not currently animating
            enabled = googleMap != null && animationJob == null,
            onClick = {
                animationJob?.cancel()
                animationJob = coroutineScope.launch {
                    val map = googleMap ?: return@launch
                    
                    // 1. Fly to Honolulu
                    map.flyCameraTo(flyToOptions {
                        endCamera = camera {
                            center = MainActivity.HONOLULU
                            tilt = 45.0
                            range = 20000.0
                        }
                        durationInMillis = 2000L
                    })
                    awaitCameraAnimation(map)

                    // 2. Orbit
                    map.flyCameraAround(flyAroundOptions {
                        center = camera {
                            center = MainActivity.HONOLULU
                            tilt = 45.0
                            range = 20000.0
                        }
                        rounds = 1.0
                        durationInMillis = 5000L
                    })
                    awaitCameraAnimation(map)
                    
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

// Re-use our robust await function, or copy it here if utils are separate
private suspend fun awaitCameraAnimation(map: GoogleMap3D) = suspendCancellableCoroutine { continuation ->
    map.setCameraAnimationEndListener {
        map.setCameraAnimationEndListener(null) // Cleanup
        if (continuation.isActive) {
            continuation.resume(Unit)
        }
    }
    continuation.invokeOnCancellation {
        map.setCameraAnimationEndListener(null)
    }
}
*/
