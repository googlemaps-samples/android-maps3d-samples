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

package com.example.advancedmaps3dsamples.trails

import com.google.android.gms.maps.model.LatLng

data class Trail(
    val id: Int = -1,
    val name: String = "",
    val type: TrailType = TrailType.HIKING,
    val difficulty: DifficultyLevel = DifficultyLevel.EASY,
    val mileage: Double = 0.0,
    val measuredFeet: Int = 0,
    val dogsAllowed: Boolean = false,
    val dogRegulations: DogRegulation = DogRegulation.NO_DOGS_ALLOWED,
    val status: TrailStatus = TrailStatus.OPEN,
    val coordinates: List<LatLng> = emptyList(),
    val dogRegulationDescription: String = "",
    val segmentId: String = "",
    val bicyclesAllowed: Boolean = false,
    val eBikesAllowed: Boolean = false,
)
