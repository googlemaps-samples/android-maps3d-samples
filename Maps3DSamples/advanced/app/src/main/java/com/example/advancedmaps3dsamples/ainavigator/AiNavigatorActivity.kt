package com.example.advancedmaps3dsamples.ainavigator

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults.iconButtonColors
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.advancedmaps3dsamples.scenarios.ThreeDMap
import com.example.advancedmaps3dsamples.ui.theme.AdvancedMaps3DSamplesTheme
import com.google.android.gms.maps3d.Map3DOptions
import com.google.android.gms.maps3d.model.Map3DMode
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@AndroidEntryPoint
class AiNavigatorActivity : ComponentActivity() {
    private val viewModel by viewModels<AiNavigatorViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        )

        // Hide the system bars
        hideSystemUI()

        // Prevent screen from dimming
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val latitude = 40.02196731315463
        val longitude = -105.25453645683653
        val altitude = 1616.0

        val heading = 0.0
        val tilt = 0.0
        val range = 10_000_000.0
        val roll = 0.0

        val options = Map3DOptions(
            defaultUiDisabled = true,
            centerLat = latitude,
            centerLng = longitude,
            centerAlt = altitude,
            heading = heading,
            tilt = tilt,
            roll = roll,
            range = range,
            minHeading = 0.0,
            maxHeading = 360.0,
            minTilt = 0.0,
            maxTilt = 90.0,
            bounds = null,
            mapMode = Map3DMode.SATELLITE,
            mapId = null,
        )

        setContent {
            val scope = rememberCoroutineScope()
            val coroutineScope = rememberCoroutineScope()
            val graphicsLayer = rememberGraphicsLayer()

            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(viewModel.userMessage) {
                scope.launch {
                    viewModel.userMessage.collect { message ->
                        if (message.length > 50) {
                            snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Long)
                        } else {
                            snackbarHostState.showSnackbar(message)
                        }
                    }
                }
            }

            AdvancedMaps3DSamplesTheme {
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            ThreeDMap(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .drawWithContent {  // This bit facilitates taking screenshots
                                        // call record to capture the content in the graphics layer
                                        graphicsLayer.record {
                                            // draw the contents of the composable into the graphics layer
                                            this@drawWithContent.drawContent()
                                        }
                                        // draw the graphics layer on the visible canvas
                                        drawLayer(graphicsLayer)
                                    },
                                options = options,
                                onMap3dViewReady = { viewModel.setGoogleMap3D(it) },
                                onReleaseMap = { viewModel.releaseGoogleMap3D() },
                            )

                            Row(
                                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(
                                    onClick = { viewModel.playAnimation() },
                                    colors = iconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PlayArrow,
                                        contentDescription = "Play"
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.stopAnimation() },
                                    colors = iconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Stop,
                                        contentDescription = "Stop"
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.restartAnimation() },
                                    colors = iconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = "Restart"
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            val bitmap = graphicsLayer.toImageBitmap()
                                            viewModel.whatAmILookingAt(bitmap)
                                        }
                                    },
                                    colors = iconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Info,
                                        contentDescription = "Describe View"
                                    )
                                }

                                var mapModeButtonEnabled by remember { mutableStateOf(true) }
                                IconButton(
                                    onClick = {
                                        viewModel.nextMapMode()
                                        mapModeButtonEnabled = false
                                        scope.launch {
                                            delay(2.seconds)
                                            mapModeButtonEnabled = true
                                        }
                                    },
                                    colors = iconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    enabled = mapModeButtonEnabled
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Public,
                                        contentDescription = "Change Map Type"
                                    )
                                }
                            }
                        }

                        Column(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 16.dp)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            var userInput by rememberSaveable { mutableStateOf("") }
                            val requestIsActive by viewModel.isRequestInflight.collectAsState()

                            // Always reserve space for the progress indicator, but only show it if the requestIsActive
                            Box(modifier = Modifier.fillMaxWidth().padding(top = 0.dp, bottom = 4.dp)) {
                                if (requestIsActive) {
                                    LinearProgressIndicator(
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = userInput,
                                onValueChange = { userInput = it },
                                label = { Text("Where would you like to go today?") },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                singleLine = false,
                                maxLines = 3,
                                trailingIcon = {
                                    if (requestIsActive) {
                                        IconButton(onClick = { viewModel.cancelRequest() }) {
                                            Icon(Icons.Filled.Stop, contentDescription = "Cancel")
                                        }
                                    } else {
                                        IconButton(onClick = { userInput = "" }) {
                                            Icon(Icons.Filled.Clear, contentDescription = "Clear")
                                        }
                                    }
                                }
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Button(
                                    onClick = { userInput = viewModel.getRandomPrompt() },
                                    enabled = !requestIsActive
                                ) {
                                    Text("I'm feeling lucky")
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Generating new prompts...")
                                        }
                                        viewModel.generateNewPrompts()
                                    },
                                    colors = iconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Icon(imageVector = Icons.Filled.Shuffle, contentDescription = "New Prompts")
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                Button(
                                    onClick = {
                                        viewModel.processUserRequest(userInput)
                                    },
                                    enabled = !requestIsActive
                                ) {
                                    Text("Submit")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
