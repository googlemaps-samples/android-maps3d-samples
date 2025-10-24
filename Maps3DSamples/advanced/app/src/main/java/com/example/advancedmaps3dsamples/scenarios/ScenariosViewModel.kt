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

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.advancedmaps3dsamples.common.Map3dViewModel
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.flyToOptions
import com.google.android.gms.maps3d.model.latLngAltitude
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import com.example.advancedmaps3dsamples.utils.DEFAULT_ROLL
import com.example.advancedmaps3dsamples.utils.toCameraString
import com.example.advancedmaps3dsamples.R
import com.example.advancedmaps3dsamples.utils.copy
import com.google.android.gms.maps3d.model.flyAroundOptions

enum class CameraAttribute(val labelId: Int) {
  HEADING(R.string.heading_label),
  TILT(R.string.tilt_label),
  RANGE(R.string.range_label),
  ROLL(R.string.roll_label),
}

data class ScenarioViewState(
  val scenario: Scenario? = null,
  val countDown: Int = 0,
  val countDownVisible: Boolean = false,
  val showFinished: Boolean = false,
)

private val NEUSCHWANSTEIN_COORDS = latLngAltitude {
  latitude = 47.557714
  longitude = 10.749557
  altitude = 988.6
}

private val NEUSCHWANSTEIN_CAMERA = camera {
  center = NEUSCHWANSTEIN_COORDS
  heading = 0.0
  tilt = 55.0
  range = 723.0
  roll = 0.0
}

@HiltViewModel
class ScenariosViewModel @Inject constructor() : Map3dViewModel() {
  override val TAG = this::class.java.simpleName
  private val _viewState = MutableStateFlow(ScenarioViewState())
  val viewState = _viewState as StateFlow<ScenarioViewState>
  var animationJob: Job? = null

  private val _trackedAttribute = MutableStateFlow(CameraAttribute.HEADING)
  val trackedAttribute: StateFlow<CameraAttribute> = _trackedAttribute

  private val _roll = MutableStateFlow(DEFAULT_ROLL)
  val roll = _roll as StateFlow<Double>

  init {
    viewModelScope.launch {
      viewState
        .map { it.scenario }
        .distinctUntilChanged()
        .filterNotNull()
        .collect { scenario ->
          if (scenario.name == "camera") {
            runCameraScenario()
          } else {
            playAnimation(scenario.animationSteps)
          }
        }
    }

    viewModelScope.launch {
      mapReady.collect { ready ->
        if (ready) {
          _viewState.value =
            viewState.value.copy(
              countDown = 3,
              countDownVisible = true,
              showFinished = false,
            )
          _viewState.value.scenario?.reset(this@ScenariosViewModel)
        }
      }
    }
  }

  /**
   * Logs the current camera state to the console.
   * This is useful for debugging and understanding the camera parameters at a specific moment.
   */
  fun snapshot() {
    Log.w(TAG, "camera: ${currentCamera.value.toCameraString()}")
  }

  /**
   * Sets the current scenario to the provided [scenario].
   *
   * This function updates the view state to reflect the new scenario, resetting the countdown,
   * making the countdown visible, and hiding the finished state. It also calls the
   * [Scenario.reset] method on the new scenario, if it is not null.
   *
   * @param scenario The [Scenario] to set as the current scenario. Can be null to clear the
   *   current scenario.
   */
  fun setScenario(scenario: Scenario?) {
    _viewState.value =
      viewState.value.copy(
        scenario = scenario,
        countDown = 3,
        countDownVisible = true,
        showFinished = false,
      )
    scenario?.reset(this)
  }

  /**
   * Sets the current scenario based on its name.
   *
   * This function looks up the scenario by name in the [scenarios] map. If a scenario with the given
   * name is found, it calls the overloaded `setScenario(Scenario?)` function to set the new scenario.
   * If no scenario is found with the provided name, a warning is logged and no scenario is set.
   *
   * @param scenarioName The name of the scenario to set.
   */
  fun setScenario(scenarioName: String) {
    val newScenario =
      scenarios[scenarioName]
        ?: run {
          Log.w(TAG, "Unrecognized scenario: $scenarioName")
          null
        }
    setScenario(newScenario)
  }

  /**
   * Advances to the next scenario in the list.
   *
   * This function finds the current scenario, determines its index in the ordered list of
   * scenarios, and then calculates the index of the next scenario, wrapping around to the
   * beginning of the list if the current scenario is the last one. The view state is then
   * updated with the next scenario.
   *
   * If no current scenario is found or the current scenario is not in the list, it defaults
   * to setting the first scenario.
   */
  fun nextScenario() {
    val currentScenarioName = _viewState.value.scenario?.name ?: return // Need current scenario
    val scenarioList = scenarios.values.toList()
    val currentIndex = scenarioList.indexOfFirst { it.name == currentScenarioName }

    if (currentIndex == -1) {
      Log.w(TAG, "Current scenario '$currentScenarioName' not found in list.")
      setScenario(scenarioList.firstOrNull()) // Go to first if current is invalid
      return
    }

    val nextIndex = (currentIndex + 1) % scenarioList.size // Calculate next index, wrapping around
    val nextScenario = scenarioList[nextIndex]
    Log.d(TAG, "Advancing from scenario '$currentScenarioName' to '${nextScenario.name}'")
    setScenario(nextScenario) // Set the next scenario
  }

  private fun isMapReady(): Boolean {
    return _viewState.value.scenario != null
  }

  /**
   * Plays a given list of animation steps.
   *
   * The function first cancels any ongoing animation jobs.
   * It then starts a countdown displayed to the user before executing the animation steps.
   * The animation steps are executed sequentially.
   * After the animation is finished, a delay is introduced before the "finished" state is shown.
   *
   * @param animation The list of [AnimationStep] to play.
   */
  private fun playAnimation(animation: List<AnimationStep>) {
    animationJob?.cancel()

    animationJob =
      viewModelScope.launch {
        for (count in 3 downTo 1) {
          _viewState.value = viewState.value.copy(countDown = count, countDownVisible = true)
          delay(1.seconds)
        }

        while (!isMapReady()) {
          delay(100.milliseconds)
        }

        _viewState.value = viewState.value.copy(countDown = 0, countDownVisible = false)
        animation.forEach { step -> step(this@ScenariosViewModel) }
        delay(2.seconds)
        _viewState.value = viewState.value.copy(showFinished = true)
      }
  }

  /**
   * Runs the camera scenario, which includes a countdown, setting the camera to a predefined
   * location, and then sweeping through various camera attributes (heading, tilt, range, and roll)
   * to demonstrate their effects.
   */
  private fun runCameraScenario() {
    animationJob?.cancel()

    animationJob =
      viewModelScope.launch {
        for (count in 3 downTo 1) {
          _viewState.value = viewState.value.copy(countDown = count, countDownVisible = true)
          delay(1.seconds)
        }
        _viewState.value = viewState.value.copy(countDown = 0, countDownVisible = false)
        delay(2.seconds)

        while (!isMapReady()) {
          delay(100.milliseconds)
        }

        setCamera(NEUSCHWANSTEIN_CAMERA)

          awaitMapSteady(10000)

        cameraHeadingSweep(NEUSCHWANSTEIN_CAMERA)
        cameraTiltSweep(NEUSCHWANSTEIN_CAMERA)
        cameraRangeSweep(NEUSCHWANSTEIN_CAMERA)
        cameraRollSweep(NEUSCHWANSTEIN_CAMERA)

        delay(2.seconds)
        _viewState.value = viewState.value.copy(showFinished = true)
      }
  }

  /**
   * Performs a sweep of the camera's heading attribute, animating the camera through various heading
   * values around the current camera's center.
   *
   * @param camera The starting camera position for the sweep.
   */
  private suspend fun cameraHeadingSweep(camera: Camera) {
    _trackedAttribute.value = CameraAttribute.HEADING

    val duration = 3000L

    // There are two ways to achieve this: FlyCameraTo or FlyAround
    awaitFlyTo(
      flyToOptions {
        endCamera = camera.copy(heading = 75.0)
        durationInMillis = duration
      }
    )

    awaitFlyTo(
      flyToOptions {
        endCamera = camera.copy(heading = 0.0)
        durationInMillis = duration
      }
    )

    awaitFlyAround(
      flyAroundOptions {
        center = currentCamera.value
        durationInMillis = duration
        rounds = 1.0
      }
    )

    // Note that the sign of the rounds dictates the direction of the sweep
    awaitFlyAround(
      flyAroundOptions {
        center = currentCamera.value
        durationInMillis = duration
        rounds = -1.0
      }
    )
  }

  /**
   * Performs a sweep of the camera's tilt attribute, animating the camera through various tilt
   * values. It first adjusts the heading slightly and then sweeps the tilt from the initial value
   * to 0 degrees, then to 85 degrees, and finally back to the original camera position.
   *
   * @param cameraIn The initial camera position for the sweep.
   */
  private suspend fun cameraTiltSweep(cameraIn: Camera) {
    val duration = 3000L

    val camera = cameraIn.copy(heading = 163.0)
    _trackedAttribute.value = CameraAttribute.HEADING
    awaitFlyTo(
      flyToOptions {
        endCamera = camera
        durationInMillis = duration
      }
    )
    delay(1500.milliseconds)
    _trackedAttribute.value = CameraAttribute.TILT
    delay(2500.milliseconds)

    awaitFlyTo(
      flyToOptions {
        endCamera = camera.copy(tilt = 0.0)
        durationInMillis = duration
      }
    )

    awaitFlyTo(
      flyToOptions {
        endCamera = camera.copy(tilt = 85.0)
        durationInMillis = duration
      }
    )

    awaitFlyTo(
      flyToOptions {
        endCamera = cameraIn
        durationInMillis = duration
      }
    )
  }

  /**
   * Performs a sweep of the camera's range attribute, animating the camera through various range
   * values around the current camera's center.
   *
   * @param camera The starting camera position for the sweep.
   */
  private suspend fun cameraRangeSweep(camera: Camera) {
    val duration = 3000L
    _trackedAttribute.value = CameraAttribute.RANGE

    delay(duration.milliseconds)

    // Get up close
    awaitFlyTo(
      flyToOptions {
        endCamera = camera.copy(range = 200.0)
        durationInMillis = duration
      }
    )

    // And far, far away
    awaitFlyTo(
      flyToOptions {
        endCamera = camera.copy(range = 10e6)
        durationInMillis = (duration * 1.3).toLong()
      }
    )

    awaitFlyTo(
      flyToOptions {
        endCamera = camera
        durationInMillis = (duration * 1.3).toLong()
      }
    )
  }

  /**
   * Animates a double value smoothly from a start value to an end value.
   *
   * This function is a suspend function and will pause execution until the animation is complete.
   * The animation progresses in steps, with delays between each step, to create a smooth visual effect.
   * The value is updated through the provided callback function at each step.
   *
   * @param start The starting value of the animation.
   * @param end The ending value of the animation.
   * @param periodMillis The time in milliseconds between each animation step. Defaults to 20ms.
   * @param durationMillis The total duration of the animation in milliseconds. Defaults to 1500ms.
   * @param callback A function that will be called with the current animated value at each step.
   *   This function is typically used to update a UI element or another property based on the
   *   animated value.
   */
  private suspend fun animateValue(
    start: Double,
    end: Double,
    periodMillis: Long = 20,
    durationMillis: Long = 1500,
    callback: (Double) -> Unit,
  ) {
    val cycles = durationMillis / periodMillis
    val step = (end - start) / cycles
    var current = start

    if (start < end) {
      while (current < end) {
        callback(current)
        current += step
        delay(periodMillis)
      }
    } else {
      while (end < current) {
        callback(current)
        current += step
        delay(periodMillis)
      }
    }
    callback(end)
  }

  /**
   * Performs a sweep of the camera's roll attribute, animating the camera through various roll
   * values around the current camera's center.
   *
   * @param camera The starting camera position for the sweep.
   */
  private suspend fun cameraRollSweep(camera: Camera) {
    _trackedAttribute.value = CameraAttribute.ROLL
    val duration = 2000L

    delay(duration)

    animateValue(start = 0.0, end = 90.0, durationMillis = duration / 2) {
      setCamera(camera.copy(roll = it))
      _roll.value = it
    }
    delay(duration)
    animateValue(start = 90.0, end = 0.0, durationMillis = duration / 2) {
      setCamera(camera.copy(roll = it))
      _roll.value = it
    }
    delay(duration)
    animateValue(start = 0.0, end = 360.0, durationMillis = duration) {
      setCamera(camera.copy(roll = it))
      _roll.value = it
    }

    // Doesn't change the view, since we were already back to zero
    setCamera(camera)
  }

  fun closeOverlay() {
    _viewState.value = viewState.value.copy(showFinished = false)
  }
}
