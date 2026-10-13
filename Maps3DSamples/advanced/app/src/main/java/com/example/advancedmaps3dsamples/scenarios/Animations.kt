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

import com.google.android.gms.maps3d.model.FlyAroundOptions
import com.google.android.gms.maps3d.model.FlyToOptions
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed interface AnimationStep {
  suspend operator fun invoke(viewModel: ScenariosViewModel)
}

data class DelayStep(val durationMillis: Long) : AnimationStep {
  override suspend operator fun invoke(viewModel: ScenariosViewModel) {
    delay(durationMillis)
  }
}

/**
 * An [AnimationStep] that waits until the map is fully loaded and idle before proceeding
 * to the next step in the animation sequence.
 *
 * @param timeoutMillis The maximum time to wait in milliseconds. A value of 0 means no timeout.
 */
data class WaitUntilTheMapIsSteadyStep(val timeoutMillis: Long = 0) : AnimationStep {
  override suspend fun invoke(viewModel: ScenariosViewModel) {
    viewModel.awaitMapSteady(timeoutMillis)
  }
}

data class FlyToStep(val flyToOptions: FlyToOptions) : AnimationStep {
  override suspend operator fun invoke(viewModel: ScenariosViewModel) {
    viewModel.awaitFlyTo(flyToOptions)
  }
}

data class FlyAroundStep(
  val flyAroundOptions: FlyAroundOptions,
  val switchMapMode: Int? = null,
  val switchDelayMillis: Long = 0L,
) : AnimationStep {
  override suspend operator fun invoke(viewModel: ScenariosViewModel) {
    if (switchMapMode != null && switchDelayMillis > 0) {
      coroutineScope {
        val switchJob = launch {
          delay(switchDelayMillis)
          android.util.Log.i("FlyAroundStep", "Switching map mode to $switchMapMode after ${switchDelayMillis}ms")
          viewModel.setMapMode(switchMapMode)
        }
        try {
          viewModel.awaitFlyAround(flyAroundOptions)
        } finally {
          switchJob.cancel()
        }
      }
    } else {
      viewModel.awaitFlyAround(flyAroundOptions)
    }
  }
}

data class SetMapModeStep(val mapMode: Int) : AnimationStep {
  override suspend operator fun invoke(viewModel: ScenariosViewModel) {
    viewModel.setMapMode(mapMode)
  }
}
