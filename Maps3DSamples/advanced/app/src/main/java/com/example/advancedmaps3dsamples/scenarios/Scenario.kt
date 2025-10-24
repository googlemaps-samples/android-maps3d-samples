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
import androidx.compose.ui.res.stringResource
import com.google.android.gms.maps3d.Map3DOptions
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.MarkerOptions
import com.google.android.gms.maps3d.model.ModelOptions // Import ModelOptions
import com.google.android.gms.maps3d.model.PolygonOptions
import com.google.android.gms.maps3d.model.PolylineOptions
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude

data class Scenario(
  val name: String,
  val titleId: Int,
  val mapsOptions: Map3DOptions,
  val animationSteps: List<AnimationStep> = emptyList(),
  val markers: List<MarkerOptions> = emptyList(),
  val models: List<ModelOptions> = emptyList(), // Add models property
  val polylines: List<PolylineOptions> = emptyList(),
  val polygons: List<PolygonOptions> = emptyList(),
) {
  @Composable fun getTitle() = stringResource(titleId)

  fun reset(viewModel: ScenariosViewModel) {
    viewModel.setCamera(mapsOptions.toCamera()) // Set initial camera
    viewModel.setMapMode(mapsOptions.mapMode)

    viewModel.clearObjects()

    markers.forEach { marker ->
      viewModel.addMarker(marker)
    }

    models.forEach { model ->
      viewModel.addModel(model) // Add models to the view model
    }

    polylines.forEach { polyline ->
      viewModel.addPolyline(polyline)
    }

    polygons.forEach { polygon ->
      viewModel.addPolygon(polygon)
    }
  }
}

fun createScenario(
  name: String,
  titleId: Int,
  initialState: String,
  animationString: String = "",
  markers: String = "",
  models: String = "",
  polylines: String = "",
  polygon: String? = null,
): Scenario {
  return Scenario(
    name = name,
    titleId = titleId,
    mapsOptions = initialState.toMaps3DOptions(),
    animationSteps = animationString.toAnimation(),
    markers = markers.toMarkers(),
    models = models.toModels(),
    polylines = polylines.toPolylines(),
    polygons = polygon?.toPolygons() ?: emptyList(),
  )
}

fun Map3DOptions.toCamera(): Camera {
  val options = this
  return camera {
    center = latLngAltitude {
      latitude = options.centerLat
      longitude = options.centerLng
      altitude = options.centerAlt
    }
    range = options.range
    tilt = options.tilt
    roll = options.roll
    heading = options.heading
  }
}
