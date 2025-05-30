package com.example.advancedmaps3dsamples.ainavigator

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.East
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.North
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.NorthWest
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.South
import androidx.compose.material.icons.filled.SouthEast
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.West
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.advancedmaps3dsamples.scenarios.ThreeDMap
import com.example.advancedmaps3dsamples.ui.theme.AdvancedMaps3DSamplesTheme
import com.example.advancedmaps3dsamples.utils.toCameraString
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

        val options = Map3DOptions(
            defaultUiDisabled = true,
            centerLat = 40.02196731315463,
            centerLng = -105.25453645683653,
            centerAlt = 1616.0,
            heading = 0.0,
            tilt = 0.0,
            roll = 0.0,
            range = 10_000_000.0,
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

            val camera by viewModel.currentCamera.collectAsStateWithLifecycle()

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

                            WhiskeyCompass(
                                heading = camera.heading?.toFloat() ?: 0f,
                                modifier = Modifier.fillMaxWidth(),
                                stripHeight = 90.dp,
                                pixelsPerDegree = 7f,
                                degreeLabelInterval = 30 // Less frequent degree labels for clarity
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
                            Box(modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 0.dp, bottom = 4.dp)) {
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
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
                                        viewModel.processUserRequest(userInput, camera.toCameraString())
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

@Composable
private fun Compass(
    heading: Double,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.background(MaterialTheme.colorScheme.primary, CircleShape),
        horizontalAlignment = CenterHorizontally
    ) {
        val (icon, abbreviation) = heading.toCardinalDirection()

        Icon(
            imageVector = icon,
            contentDescription = "Compass",
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(top = 8.dp, start = 8.dp, end = 8.dp, bottom = 0.dp)
        )
        Text(
            text = abbreviation,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
        )
    }
}

private fun Double.toCardinalDirection(): Pair<ImageVector, String> {
    return when (this) {
        in 337.5..360.0, in 0.0..22.5 -> Pair(Icons.Filled.North, "N")
        in 22.5..67.5 -> Pair(Icons.Filled.NorthEast, "NE") // Placeholder, adjust as needed
        in 67.5..112.5 -> Pair(Icons.Filled.East, "E")
        in 112.5..157.5 -> Pair(Icons.Filled.SouthEast, "SE") // Placeholder
        in 157.5..202.5 -> Pair(Icons.Filled.South, "S")
        in 202.5..247.5 -> Pair(Icons.Filled.SouthWest, "SW") // Placeholder
        in 247.5..292.5 -> Pair(Icons.Filled.West, "W")
        else -> Pair(Icons.Filled.NorthWest, "NW") // Placeholder for 292.5..337.5
    }
}
