package com.example.advancedmaps3dsamples.ainavigator

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.advancedmaps3dsamples.ainavigator.data.examplePrompts
import com.example.advancedmaps3dsamples.scenarios.ThreeDMap
import com.example.advancedmaps3dsamples.ui.theme.AdvancedMaps3DSamplesTheme
import com.google.android.gms.maps3d.Map3DOptions
import com.google.android.gms.maps3d.model.Map3DMode
import android.widget.Toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
@OptIn(ExperimentalMaterial3Api::class)
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
            val context = LocalContext.current
            val scope = rememberCoroutineScope()

            LaunchedEffect(viewModel.userMessage) {
                scope.launch {
                    viewModel.userMessage.collect { message ->
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    }
                }
            }

            AdvancedMaps3DSamplesTheme {
                Column(modifier = Modifier.fillMaxSize()) {
                    ThreeDMap(
                        modifier = Modifier.weight(1f),
                        options = options,
                        onMap3dViewReady = { viewModel.setGoogleMap3D(it) },
                        onReleaseMap = { viewModel.releaseGoogleMap3D() },
                    )

                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        var userInput by rememberSaveable { mutableStateOf("") }
                        val requestIsActive by viewModel.isRequestInflight.collectAsState()

                        OutlinedTextField(
                            value = userInput,
                            onValueChange = { userInput = it },
                            label = { Text("Where would you like to go today?") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = false,
                            maxLines = 3,
                            trailingIcon = {
                                IconButton(onClick = { userInput = "" }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Clear")
                                }
                            }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (requestIsActive) {
                                IconButton(
                                    onClick = {
                                         viewModel.cancelRequest()
                                    },
                                    modifier = Modifier
                                        .padding(8.dp) // Add some padding around the spinner
                                ) {
                                    // Replace with your preferred spinner/loading indicator
                                    Icon(Icons.Filled.Stop, contentDescription = "Cancel")
                                }
                            } else {
                                Button(
                                    onClick = { userInput = examplePrompts.random() }
                                ) {
                                    Text("I'm feeling lucky")
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                Button(
                                    onClick = { viewModel.processUserRequest(userInput) },
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

