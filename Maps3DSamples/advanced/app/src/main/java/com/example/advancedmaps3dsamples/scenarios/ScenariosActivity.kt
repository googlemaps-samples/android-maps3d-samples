// Copyright 2025 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.advancedmaps3dsamples.scenarios

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.advancedmaps3dsamples.R
import com.example.advancedmaps3dsamples.ui.theme.AdvancedMaps3DSamplesTheme
import com.example.advancedmaps3dsamples.utils.DEFAULT_ROLL
import com.example.advancedmaps3dsamples.utils.toHeading
import com.example.advancedmaps3dsamples.utils.toRange
import com.example.advancedmaps3dsamples.utils.toRoll
import com.example.advancedmaps3dsamples.utils.toTilt
import com.google.android.gms.maps3d.model.Camera
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map

@AndroidEntryPoint
@OptIn(ExperimentalMaterial3Api::class)
class ScenariosActivity : ComponentActivity() {
  private val viewModel by viewModels<ScenariosViewModel>()

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    intent.extras?.getString("scenario")?.let { scenarioName: String ->
      viewModel.setScenario(scenarioName)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewState by viewModel.viewState.collectAsStateWithLifecycle()
      val currentCamera by viewModel.currentCamera.collectAsStateWithLifecycle(Camera.DEFAULT_CAMERA)

      val mappedRoll = remember { viewModel.roll.map { it.toRoll().toFloat() } }
      val roll by mappedRoll.collectAsStateWithLifecycle(DEFAULT_ROLL.toFloat())

      val cameraAttribute by viewModel.trackedAttribute.collectAsStateWithLifecycle()

      AdvancedMaps3DSamplesTheme(
        dynamicColor = false
      ) {
        Scaffold(
          modifier = Modifier.fillMaxSize(),
          topBar = {
            CenterAlignedTopAppBar(
              colors = topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary,
              ),
              title = {
                Text(stringResource(viewState.scenario?.titleId ?: R.string.scenarios_none))
              }
            )
          }
        ) { innerPadding ->
          val modifier = Modifier.padding(innerPadding)
          viewState.scenario?.let { scenario ->
            Box(modifier = modifier.fillMaxSize()) {

              if (scenario.name == "camera") {
                CameraControlDemoScreen(
                  modifier = Modifier.fillMaxSize(),
                  scenario = scenario,
                  viewModel = viewModel,
                  attribute = cameraAttribute,
                  heading = currentCamera.heading.toHeading().toFloat(),
                  tilt = currentCamera.tilt.toTilt().toFloat(),
                  range = currentCamera.range.toRange().toFloat(),
                  roll = roll,
                )
              } else {
                ScenarioScreen(
                  modifier = Modifier.fillMaxSize(),
                  scenario = scenario,
                  viewModel = viewModel,
                )
              }

              if (viewState.countDownVisible) {
                CountDownOverlay(scenario, viewState)
              }
              if (viewState.showFinished) {
                FinishedOverlay(
                  onRepeatClick = {
                    viewModel.setScenario(null)
                    viewModel.setScenario(scenario)
                  },
                  onExitClick = { viewModel.setScenario(null) },
                  onNextClick = { viewModel.nextScenario() },
                  onSnapshotClick = { viewModel.snapshot() },
                  onCloseClick = { viewModel.closeOverlay() }
                )
              }
            }
          }
            ?: run {
              ScenarioPicker(modifier = modifier, onScenarioClicked = { viewModel.setScenario(it) })
            }
        }

        BackHandler(enabled = viewState.scenario != null) {
          viewModel.setScenario(null)
        }
      }
    }
  }
}

/**
 * A Composable function that displays a list of available scenarios as buttons.
 *
 * Clicking on a scenario button will trigger the [onScenarioClicked] lambda with the selected
 * [Scenario].
 *
 * @param modifier The Modifier to be applied to the main Column layout.
 * @param onScenarioClicked A lambda function that is invoked when a scenario button is clicked.
 *   It receives the selected [Scenario] as a parameter.
 */
@Composable
fun ScenarioPicker(modifier: Modifier, onScenarioClicked: (Scenario) -> Unit) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    scenarios.forEach { (_, scenario) ->
      Button(modifier = Modifier.fillMaxWidth(), onClick = { onScenarioClicked(scenario) }) {
        Text(scenario.getTitle())
      }
    }
  }
}


/**
 * Displays a countdown overlay on the screen.
 *
 * @param scenario The current [Scenario] being displayed.
 * @param viewState The current [ScenarioViewState] containing countdown information.
 */
@Composable
fun CountDownOverlay(scenario: Scenario, viewState: ScenarioViewState) {
  Box(
    modifier =
      Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.3f)), // Slight black shading
    contentAlignment = Alignment.Center, // Center content within the overlay
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        text = scenario.getTitle(),
        style = MaterialTheme.typography.titleMedium,
        color = Color.White,
        textAlign = TextAlign.Center,
      )
      Spacer(modifier = Modifier.height(8.dp)) // Add some space between title and countdown
      Text(
        text = viewState.countDown.toString(),
        style = MaterialTheme.typography.titleSmall,
        color = Color.White,
        textAlign = TextAlign.Center,
      )
    }
  }
}

/**
 * A composable function that displays an overlay when a scenario is finished, providing options
 * to exit, repeat, take a snapshot, go to the next scenario, or close the overlay.
 *
 * @param onExitClick The lambda to execute when the "Exit" button is clicked.
 * @param onRepeatClick The lambda to execute when the "Repeat" button is clicked.
 * @param onSnapshotClick The lambda to execute when the "Snapshot" button is clicked.
 * @param onNextClick The lambda to execute when the "Next Scenario" button is clicked.
 * @param onCloseClick The lambda to execute when the "Close" button is clicked.
 */
@Composable
fun FinishedOverlay(
  onExitClick: () -> Unit,
  onRepeatClick: () -> Unit,
  onSnapshotClick: () -> Unit,
  onNextClick: () -> Unit,
  onCloseClick: () -> Unit,
) {
  val size = 72.dp
  val modifier = Modifier.size(size)

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.3f)), // Dark overlay
    contentAlignment = Alignment.Center,
  ) {
    // Close Button at the top end corner
    OverlayButton(
      modifier = modifier.align(Alignment.TopEnd).offset(x = (-16).dp, y = 16.dp).size(size),
      onExitClick = onCloseClick,
      imageVector = Icons.Default.Close,
      contentDescription = stringResource(R.string.close)
    )

    Row(
      modifier = Modifier
        .padding(16.dp)
        .fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceEvenly
    ) {
      OverlayButton(
        modifier = modifier,
        onExitClick = onExitClick,
        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
        contentDescription = stringResource(R.string.close)
      )
      OverlayButton(
        modifier = modifier,
        onExitClick = onRepeatClick,
        imageVector = Icons.Filled.Replay,
        contentDescription = stringResource(R.string.repeat)
      )
      OverlayButton(
        modifier = modifier,
        onExitClick = onSnapshotClick,
        imageVector = Icons.Filled.CameraAlt,
        contentDescription = stringResource(R.string.snapshot)
      )
      OverlayButton(
        modifier = modifier,
        onExitClick = onNextClick,
        imageVector = Icons.Filled.SkipNext,
        contentDescription = stringResource(R.string.next_scenario)
      )
    }
  }
}

@Composable
private fun OverlayButton(
  onExitClick: () -> Unit,
  imageVector: ImageVector,
  modifier: Modifier = Modifier,
  contentDescription: String,
) {
  Button(
    onClick = onExitClick,
    shape = CircleShape,
    modifier = modifier,
  ) {
    Icon(
      modifier = Modifier.size(24.dp),
      imageVector = imageVector,
      contentDescription = contentDescription,
    )
  }
}

@Preview
@Composable
fun FinishedOverlayCompose() {
  AdvancedMaps3DSamplesTheme {
    FinishedOverlay(
      onExitClick = {},
      onRepeatClick = {},
      onSnapshotClick = {},
      onNextClick = {},
      onCloseClick = {},
    )
  }
}

@Preview
@Composable
fun OverlayButtonPreview() {
  AdvancedMaps3DSamplesTheme {
    val size = 72.dp
    val modifier = Modifier.size(size)

    OverlayButton(
      onExitClick = {},
      imageVector = Icons.Default.Close,
      modifier = modifier,
      contentDescription = "Close",
    )
  }
}