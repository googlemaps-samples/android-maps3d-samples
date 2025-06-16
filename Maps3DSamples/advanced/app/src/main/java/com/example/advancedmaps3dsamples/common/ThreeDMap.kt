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

package com.example.advancedmaps3dsamples.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.Map3DOptions
import com.google.android.gms.maps3d.Map3DView
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback

/**
 * Composable function that wraps the AndroidView to display a 3D map.
 *
 * This function handles the creation and lifecycle of a Map3DView.
 * It allows for customization through [Map3DOptions] and provides callbacks
 * for when the map is ready and when it's being released.
 *
 * @param options The [Map3DOptions] to configure the map with.
 * @param onMap3dViewReady A callback that is invoked when the [GoogleMap3D] instance is ready to be used.
 * @param onReleaseMap A callback that is invoked when the map view is being released, allowing for cleanup.
 * @param modifier A [Modifier] to be applied to the underlying [AndroidView].
 */
@Composable
internal fun ThreeDMap(
  options: Map3DOptions,
  onMap3dViewReady: (GoogleMap3D) -> Unit,
  onReleaseMap: () -> Unit,
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
            onMap3dViewReady(googleMap3D)
          }

          override fun onError(error: Exception) {
            throw error
          }
        }
      )
    },
    onRelease = { view ->
      // Clean up resources if needed
      onReleaseMap()
    },
    onReset = { view -> },
  )
}
