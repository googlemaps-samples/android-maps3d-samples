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

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Exception thrown when the Routes API returns an error, such as a 403 Forbidden
 * if the API is not enabled for the provided key.
 */
class DirectionsErrorException(message: String) : Exception(message)

/**
 * A simple network service to fetch routes from the Google Maps Routes API.
 * 
 * Note: In a production application, making direct API calls to Google Maps Platform
 * services from a client device requires embedding the API key in the app, which
 * poses a security risk. Best practice is to proxy these requests through a secure
 * backend server. This client implementation is provided for demonstration purposes.
 */
object RoutesApiService {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
            })
        }
    }

    /**
     * Fetches a route between the origin and destination coordinates.
     * 
     * @param apiKey The Google Maps API key (requires Routes API enabled).
     * @param originLat The latitude of the starting point.
     * @param originLng The longitude of the starting point.
     * @param destLat The latitude of the destination point.
     * @param destLng The longitude of the destination point.
     * @return [RoutesResponse] containing the computed route.
     * @throws [DirectionsErrorException] if the API returns a non-success HTTP status.
     */
    suspend fun fetchRoute(
        apiKey: String,
        originLat: Double,
        originLng: Double,
        destLat: Double,
        destLng: Double
    ): RoutesResponse {
        val requestBody = RoutesRequest(
            origin = Waypoint(Location(RequestLatLng(originLat, originLng))),
            destination = Waypoint(Location(RequestLatLng(destLat, destLng)))
        )

        val response: HttpResponse = client.post("https://routes.googleapis.com/directions/v2:computeRoutes") {
            contentType(ContentType.Application.Json)
            header("X-Goog-Api-Key", apiKey)
            // Requesting only the most relevant fields to optimize payload size
            header("X-Goog-FieldMask", "routes.duration,routes.distanceMeters,routes.polyline.encodedPolyline")
            setBody(requestBody)
        }

        if (response.status.isSuccess()) {
            return response.body()
        } else {
            val errorBody = response.bodyAsText()
            Log.e("RoutesApiService", "Failed to fetch route: ${response.status.value}\n$errorBody")
            
            // Provide a localized, user-friendly message based on typical API errors
            val userMsg = if (response.status.value == 403) {
                "API Error (HTTP 403). Ensure the Routes API is enabled in the Google Cloud Console for the provided API key."
            } else {
                "Failed to fetch route (HTTP ${response.status.value})."
            }
            throw DirectionsErrorException(userMsg)
        }
    }
}
