package com.example.alohaexplorer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
// TODO: Step 12 - Uncomment imports after adding the Maps 3D SDK dependency
// import androidx.activity.compose.setContent
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

// TODO: Step 12.1 - Configure Map3DInitConfig
/*
val map3DConfig: Map3DInitConfig = Map3DInitConfig.create(
    // TODO: Set centerLat, centerLng, centerAlt, heading, range, and mapMode
)
*/

/**
 * A minimalist activity to demonstrate how to integrate [Map3DView] with Jetpack Compose.
 */
class Map3DComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // TODO: Step 12.3 - Display Map3DContainer inside setContent with a Scaffold
        /*
        setContent {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                Map3DContainer(
                    modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
                    config = map3DConfig
                )
            }
        }
        */

        // Hide the system bars (status bar and navigation bar) for a full-screen 3D map
        // experience. Swiping from the screen edge reveals them transiently.
        // Remove these lines if you prefer to keep the system bars visible at all times.
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }
}

// TODO: Step 12.2 - Implement the Map3DContainer Composable Wrapper
/*
@Composable
fun Map3DContainer(
    modifier: Modifier = Modifier,
    config: Map3DInitConfig,
) {
    // 12.2.1. State to hold the GoogleMap3D controller and active animation Job

    Box(modifier = modifier.fillMaxSize()) {
        // 12.2.1 - 12.2.4. Bridge Map3DView using AndroidView (factory, update, onRelease)

        // 12.2.5. Overlay a Compose Button to trigger the flight and orbit animation
    }
}
*/
