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

import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.advancedmaps3dsamples.utils.wrapIn
import com.google.android.gms.maps3d.GoogleMap3D

private val headingSliderRange = -180f..180f
private val tiltSliderRange = 0f..90f
private val rangeSliderRange = 250f..7_500f
private val rollSliderRange = -360f..360f

/**
 * A composable screen that demonstrates camera control over a 3D map.
 *
 * This screen displays a [ThreeDMap] and provides controls to adjust different camera attributes
 * like heading, tilt, roll, and range. The controls are implemented using a [SliderWithLabel]
 * which changes based on the currently selected [attribute].
 *
 * @param scenario The scenario providing options for the map.
 * @param viewModel The view model that manages the map's state.
 * @param modifier The modifier to be applied to the composable.
 * @param heading The current heading value of the camera.
 * @param tilt The current tilt value of the camera.
 * @param roll The current roll value of the camera.
 * @param range The current range value of the camera.
 * @param attribute The currently selected camera attribute to control.
 */
@Composable
fun CameraControlDemoScreen(
  scenario: Scenario,
  viewModel: ScenariosViewModel,
  modifier: Modifier = Modifier,
  heading: Float,
  tilt: Float,
  roll: Float,
  range: Float,
  attribute: CameraAttribute,
) {
  Box(modifier = modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize()) {
      ThreeDMap(
        modifier = Modifier.fillMaxWidth().weight(1f),
        options = scenario.mapsOptions,
        viewModel = viewModel,
      )
      Spacer(modifier = Modifier.height(16.dp))
      Crossfade(
        targetState = attribute,
        label = "cameraAttributeCrossfade",
        animationSpec = tween(durationMillis = 500),
      ) { currentAttribute ->
        when (currentAttribute) {
          CameraAttribute.HEADING -> {
            SliderWithLabel(
              stringId = currentAttribute.labelId,
              // Note we wrap the value to the range -180 to 180
              value = heading.wrapIn(headingSliderRange),
              valueRange = headingSliderRange,
            )
          }

          CameraAttribute.TILT -> {
            SliderWithLabel(
              stringId = currentAttribute.labelId,
              value = tilt,
              valueRange = tiltSliderRange
            )
          }

          CameraAttribute.RANGE -> {
            SliderWithLabel(
              stringId = currentAttribute.labelId,
              value = range,
              valueRange = rangeSliderRange
            )
          }

          CameraAttribute.ROLL -> {
            SliderWithLabel(
              stringId = currentAttribute.labelId,
              value = roll,
              valueRange = rollSliderRange
            )
          }
        }
      }
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

/**
 * A composable function that displays a slider with a label.
 *
 * @param stringId The resource ID of the string to use for the label. This string should be
 *   formatted to accept a float value, e.g., "Value: %.2f".
 * @param value The current value of the slider.
 * @param valueRange The range of values that the slider can represent.
 * @param modifier Modifier to be applied to the Row containing the slider and label.
 * @param onValueChange A lambda function to be called when the slider value changes.
 */
@Composable
fun SliderWithLabel(
  @StringRes stringId: Int,
  value: Float,
  valueRange: ClosedFloatingPointRange<Float>,
  modifier: Modifier = Modifier,
  onValueChange: (Float) -> Unit = {},
) {
  Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        modifier = Modifier,
        text = stringResource(stringId, value),
        style = MaterialTheme.typography.labelMedium,
      )
      Slider(
        modifier = Modifier,
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
      )
    }
  }
}
