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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.Map3DInitConfig
import com.google.android.gms.maps3d.Map3DView
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback

@Composable
internal fun ThreeDMap(
  mapsConfig: Map3DInitConfig,
  viewModel: ScenariosViewModel,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current

  // Use rememberUpdatedState to avoid capturing stale callbacks if they change
  val currentOnMapSteadyChange by rememberUpdatedState { isSteady: Boolean ->
    viewModel.onMapSteadyChange(isSteady)
  }

  val map3dView = remember(mapsConfig) {
    Map3DView(context = context, config = mapsConfig).apply {
      getMap3DViewAsync(
        object : OnMap3DViewReadyCallback {
          override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
            viewModel.setGoogleMap3D(googleMap3D)
            googleMap3D.setOnMapSteadyListener { isSceneSteady ->
              currentOnMapSteadyChange(isSceneSteady)
            }
          }

          override fun onError(error: Exception) {
            throw error
          }
        }
      )
    }
  }

  DisposableEffect(lifecycleOwner, map3dView) {
    val observer = LifecycleEventObserver { _, event ->
      when (event) {
        Lifecycle.Event.ON_CREATE -> map3dView.onCreate(null)
        Lifecycle.Event.ON_RESUME -> map3dView.onResume()
        Lifecycle.Event.ON_PAUSE -> map3dView.onPause()
        Lifecycle.Event.ON_DESTROY -> map3dView.onDestroy()
        else -> {}
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
      viewModel.releaseGoogleMap3D()
    }
  }

  AndroidView(
    modifier = modifier.testTag("map3d_view"),
    factory = { map3dView },
    update = { _ -> },
    onRelease = { _ ->
      viewModel.releaseGoogleMap3D()
    },
    onReset = { _ -> },
  )
}
