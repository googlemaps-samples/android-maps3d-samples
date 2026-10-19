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
// import androidx.compose.ui.res.stringResource
// import androidx.compose.ui.unit.dp
// import androidx.compose.ui.viewinterop.AndroidView
// import com.google.android.gms.maps3d.GoogleMap3D
// import com.google.android.gms.maps3d.Map3DInitConfig
// import com.google.android.gms.maps3d.Map3DView
// import com.google.android.gms.maps3d.OnMap3DViewReadyCallback
// import com.google.android.gms.maps3d.model.Map3DMode
// import com.google.android.gms.maps3d.model.camera
// import com.google.android.gms.maps3d.model.flyAroundOptions
// import com.google.android.gms.maps3d.model.flyToOptions
// import kotlinx.coroutines.Job
// import kotlinx.coroutines.launch

// TODO: Step 12 - Jetpack Compose
/*
val map3DConfig: Map3DInitConfig = Map3DInitConfig.create().copy(
    centerLat = HONOLULU.latitude,
    centerLng = HONOLULU.longitude,
    centerAlt = 1000.0,
    heading = 0.0,
    range = 10_000_000.0, // Start from space
    mapMode = Map3DMode.HYBRID
)

class Map3DComposeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                Map3DContainer(
                    modifier = Modifier.padding(innerPadding),
                    config = map3DConfig
                )
            }
        }
    }
}

@Composable
fun Map3DContainer(
    modifier: Modifier = Modifier,
    config: Map3DInitConfig
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
                 Map3DView(context, config).apply {
                    // Manually call onCreate and onResume. In a real app, wire this to LifecycleOwner.
                    onCreate(null)
                    onResume()
                 }
            },
            update = { view ->
                view.getMap3DViewAsync(
                    object : OnMap3DViewReadyCallback {
                        override fun onMap3DViewReady(map3D: GoogleMap3D) {
                            googleMap = map3D
                        }
                        override fun onError(e: Exception) {
                            googleMap = null
                            throw e
                        }
                    }
                )
            },
            onRelease = { view -> 
                googleMap = null
                view.onPause()
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

                    // 2. Orbit
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
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            Text(stringResource(R.string.fly_to_honolulu))
        }
    }
}
*/
