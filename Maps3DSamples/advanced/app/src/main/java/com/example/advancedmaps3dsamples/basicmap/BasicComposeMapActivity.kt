package com.example.advancedmaps3dsamples.basicmap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.advancedmaps3dsamples.ui.theme.AdvancedMaps3DSamplesTheme
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.Map3DOptions
import com.google.android.gms.maps3d.Map3DView
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback

/**
 * A minimal reference example demonstrating how to integrate the Google Maps 3D SDK
 * into a Jetpack Compose application.
 *
 * This activity serves as a pedagogical example for junior developers to understand
 * the core concepts of using Map3DView with Compose, including state hoisting,
 * lifecycle management, and View interop.
 */
class BasicComposeMapActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdvancedMaps3DSamplesTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val lifecycleOwner = LocalLifecycleOwner.current

                    // 1. STATE HOISTING (The Compose Bridge)
                    // We create a mutable state holder to keep a reference to the GoogleMap3D object.
                    // This allows Compose to react to changes when the map becomes ready and gives us
                    // a handle to interact with the map (e.g., add markers, move camera) later.
                    val map3DState = remember { mutableStateOf<GoogleMap3D?>(null) }

                    // 2. VIEW INITIALIZATION
                    // We use `remember` to ensure the Map3DView is created ONLY ONCE when this
                    // Composable enters the composition. Without `remember`, a new Map3DView would
                    // be created on every recomposition, causing severe performance issues and losing state.
                    val map3DView = remember {
                        // Map3DOptions allows us to set the initial camera position and orientation.
                        val options = Map3DOptions(
                            centerLat = 21.350,
                            centerLng = -157.800,
                            centerAlt = 0.0,
                            tilt = 60.0,
                            range = 25000.0
                        )
                        Map3DView(context, options).apply {
                            // Map3DView loads its resources asynchronously. We must provide a callback
                            // to be notified when the map is fully loaded and ready for interaction.
                            getMap3DViewAsync(object : OnMap3DViewReadyCallback {
                                override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
                                    // Store the reference in our Compose state holder to "bridge" it.
                                    map3DState.value = googleMap3D
                                }
                                override fun onError(error: Exception) {
                                    // In a production app, handle this gracefully (e.g., show an error UI).
                                    throw error
                                }
                            })
                        }
                    }

                    // 3. LIFECYCLE MANAGEMENT
                    // MapView (and Map3DView) are heavy components that manage native resources and rendering loops.
                    // They MUST be notified of the Activity lifecycle events to start/stop rendering,
                    // clean up memory, and respond to app backgrounding correctly.
                    // We use DisposableEffect to attach an observer to the Compose LifecycleOwner.
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
                        
                        // Clean up the observer when this Composable leaves the composition.
                        onDispose {
                            lifecycleOwner.lifecycle.removeObserver(observer)
                        }
                    }

                    // 4. THE BRIDGE
                    // Since Map3DView is a traditional Android View, we use AndroidView to
                    // inflate and display it within our Jetpack Compose layout.
                    AndroidView(
                        modifier = Modifier.fillMaxSize().padding(innerPadding).testTag("map3d_view"),
                        factory = { map3DView }
                    )
                }
            }
        }
    }
}
