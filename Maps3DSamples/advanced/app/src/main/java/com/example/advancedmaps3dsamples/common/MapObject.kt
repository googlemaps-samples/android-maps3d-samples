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

import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.MarkerOptions
import com.google.android.gms.maps3d.model.ModelOptions
import com.google.android.gms.maps3d.model.PolygonOptions
import com.google.android.gms.maps3d.model.PolylineOptions

sealed class MapObject {
  internal abstract fun addToMap(controller: GoogleMap3D): ActiveMapObject?
  abstract val id: String

  data class Marker(val options: MarkerOptions) : MapObject() {
    override fun addToMap(controller: GoogleMap3D): ActiveMapObject? {
      return controller.addMarker(options)?.let { marker ->
          ActiveMapObject.ActiveMarker(marker)
      }
    }

    override val id: String
      get() = options.id
  }

  data class Polyline(val options: PolylineOptions) : MapObject() {
    override fun addToMap(controller: GoogleMap3D): ActiveMapObject {
      return ActiveMapObject.ActivePolyline(controller.addPolyline(options))
    }

    override val id: String
      get() = options.id
  }

  data class Polygon(val options: PolygonOptions) : MapObject() {
    override fun addToMap(controller: GoogleMap3D): ActiveMapObject {
      return ActiveMapObject.ActivePolygon(controller.addPolygon(options))
    }

    override val id: String
      get() = options.id
  }

  data class Model(val options: ModelOptions) : MapObject() {
    override fun addToMap(controller: GoogleMap3D): ActiveMapObject {
      return ActiveMapObject.ActiveModel(controller.addModel(options))
    }

    override val id: String
      get() = options.id
  }
}