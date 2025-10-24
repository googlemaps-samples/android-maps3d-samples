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
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.Map3DOptions
import com.google.android.gms.maps3d.Map3DView
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback

@Composable
internal fun ThreeDMap(
  options: Map3DOptions,
  viewModel: ScenariosViewModel,
  modifier: Modifier = Modifier,
) {
  AndroidView(
    modifier = modifier,
    factory = { context ->
      val map3dView = Map3DView(context = context, options = options)
      map3dView.onCreate(null)
      map3dView
    },
    update = { map3dView ->
      map3dView.getMap3DViewAsync(
        object : OnMap3DViewReadyCallback {
          override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
            viewModel.setGoogleMap3D(googleMap3D)
            googleMap3D.setOnMapSteadyListener { isSceneSteady ->
                viewModel.onMapSteadyChange(isSceneSteady)
            }
          }

          override fun onError(error: Exception) {
            throw error
          }
        }
      )
    },
    onRelease = { _ ->
      // Clean up resources if needed
      viewModel.releaseGoogleMap3D()
    },
    onReset = { _ -> },
  )
}
