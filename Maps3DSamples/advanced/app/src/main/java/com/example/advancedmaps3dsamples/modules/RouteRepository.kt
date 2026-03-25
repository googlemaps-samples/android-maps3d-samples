// Copyright 2026 Google LLC
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

package com.example.advancedmaps3dsamples.modules

import android.util.Log
import com.example.advancedmaps3dsamples.common.Location
import com.example.advancedmaps3dsamples.common.RequestLatLng
import com.example.advancedmaps3dsamples.common.RoutesRequest
import com.example.advancedmaps3dsamples.common.RoutesResponse
import com.example.advancedmaps3dsamples.common.Waypoint
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Exception thrown when the Routes API returns an error, such as a 403 Forbidden
 * if the API is not enabled for the provided key.
 */
class DirectionsErrorException(message: String) : Exception(message)

/**
 * A repository to fetch routes from the Google Maps Routes API.
 * 
 * WHY A REPOSITORY?
 * The Repository Pattern acts as a clean boundary between the data sources (network, database) 
 * and the rest of the application. The UI doesn't need to know *how* to fetch a route 
 * (whether it's from Ktor, Retrofit, or a local cache); it just asks the Repository for it.
 * 
 * Note: In a production application, making direct API calls to Google Maps Platform
 * services from a client device requires embedding the API key in the app, which
 * poses a security risk. Best practice is to proxy these requests through a secure
 * backend server. This client implementation is provided for demonstration purposes.
 */
@Singleton
class RouteRepository @Inject constructor(
    private val client: HttpClient
) {

    /**
     * Fetches a route between the origin and destination coordinates.
     * 
     * WHY KTOR and SUSPEND FUNCTIONS?
     * We use a 'suspend' function because network calls take time. Coroutines allow us to perform
     * this heavy work off the main UI thread, preventing the app from freezing while waiting for the response.
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
            
            // WHY A FIELD MASK?
            // The Routes API can return a massive amount of data (tolls, maneuvers, localized instructions).
            // To optimize payload size and response speed, we use a FieldMask to explicitly tell the API:
            // "Only send us the duration, distance, polyline, and leg steps."
            header("X-Goog-FieldMask", "routes.duration,routes.distanceMeters,routes.polyline.encodedPolyline,routes.legs.steps.startLocation")
            setBody(requestBody)
        }

        if (response.status.isSuccess()) {
            return response.body()
        } else {
            val errorBody = response.bodyAsText()
            Log.e("RouteRepository", "Failed to fetch route: ${response.status.value}\n$errorBody")
            
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
