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

import kotlinx.serialization.Serializable

@Serializable
data class RoutesRequest(
    val origin: Waypoint,
    val destination: Waypoint,
    val travelMode: String = "DRIVE",
    val routingPreference: String = "TRAFFIC_AWARE",
    val computeAlternativeRoutes: Boolean = false,
    val routeModifiers: RouteModifiers = RouteModifiers(),
    val languageCode: String = "en-US",
    val units: String = "METRIC"
)

@Serializable
data class Waypoint(
    val location: Location
)

@Serializable
data class Location(
    val latLng: RequestLatLng
)

@Serializable
data class RequestLatLng(
    val latitude: Double,
    val longitude: Double
)

@Serializable
data class RouteModifiers(
    val avoidTolls: Boolean = false,
    val avoidHighways: Boolean = false,
    val avoidFerries: Boolean = false
)

@Serializable
data class RoutesResponse(
    val routes: List<Route> = emptyList()
)

@Serializable
data class Route(
    val distanceMeters: Int? = null,
    val duration: String? = null,
    val polyline: Polyline? = null,
    val legs: List<RouteLeg> = emptyList()
)

@Serializable
data class RouteLeg(
    val steps: List<RouteStep> = emptyList()
)

@Serializable
data class RouteStep(
    val startLocation: Location? = null
)

@Serializable
data class Polyline(
    val encodedPolyline: String
)
