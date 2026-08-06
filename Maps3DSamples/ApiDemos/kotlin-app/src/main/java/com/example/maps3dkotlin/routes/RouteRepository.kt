/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.maps3dkotlin.routes

import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Represents the route coordinate and navigation steps payload returned from the Routes API.
 *
 * @property encodedPolyline The Google encoded polyline string containing the full high-resolution route.
 * @property navPoints A list of LatLng waypoint coordinates extracted from the navigation steps.
 */
data class RouteData(
    val encodedPolyline: String,
    val navPoints: List<LatLng>
)

/**
 * A data repository responsible for connecting securely to the Google Maps Routes API (v2)
 * to compute driving directions between two points.
 */
class RouteRepository {

    /**
     * Executes a synchronous network POST request to Routes API v2 to obtain directions.
     *
     * @param apiKey The Google Maps Platform API Key to authenticate the request.
     * @param origin The starting location coordinate.
     * @param dest The ending location coordinate.
     * @return The parsed [RouteData] containing the route coordinates.
     */
    suspend fun fetchRoute(
        apiKey: String,
        origin: LatLng,
        dest: LatLng
    ): RouteData = withContext(Dispatchers.IO) {
        val url = URL("https://routes.googleapis.com/directions/v2:computeRoutes")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("X-Goog-Api-Key", apiKey)
        connection.setRequestProperty("X-Goog-FieldMask", "routes.polyline.encodedPolyline,routes.legs.steps.startLocation")
        connection.doOutput = true

        // Build the standard JSON request body required by the v2 Routes API
        val requestBody = JSONObject().apply {
            put(
                "origin",
                JSONObject().put(
                    "location",
                    JSONObject().put(
                        "latLng",
                        JSONObject().apply {
                            put("latitude", origin.latitude)
                            put("longitude", origin.longitude)
                        }
                    )
                )
            )
            put(
                "destination",
                JSONObject().put(
                    "location",
                    JSONObject().put(
                        "latLng",
                        JSONObject().apply {
                            put("latitude", dest.latitude)
                            put("longitude", dest.longitude)
                        }
                    )
                )
            )
            put("travelMode", "DRIVE")
        }

        // Stream the payload to the server
        OutputStreamWriter(connection.outputStream).use { writer ->
            writer.write(requestBody.toString())
            writer.flush()
        }

        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
            reader.close()

            // Parse the response JSON to extract the encoded polyline and navigation step waypoints
            val jsonResponse = JSONObject(response.toString())
            val routes = jsonResponse.getJSONArray("routes")
            if (routes.length() > 0) {
                val route = routes.getJSONObject(0)
                val polyline = route.getJSONObject("polyline")
                val encodedPolyline = polyline.getString("encodedPolyline")

                val navPoints = mutableListOf<LatLng>()
                val legs = route.optJSONArray("legs")
                if (legs != null && legs.length() > 0) {
                    val leg = legs.getJSONObject(0)
                    val steps = leg.optJSONArray("steps")
                    if (steps != null) {
                        for (i in 0 until steps.length()) {
                            val step = steps.getJSONObject(i)
                            val startLocation = step.optJSONObject("startLocation")
                            if (startLocation != null) {
                                val latLngObj = startLocation.getJSONObject("latLng")
                                navPoints.add(
                                    LatLng(
                                        latLngObj.getDouble("latitude"),
                                        latLngObj.getDouble("longitude")
                                    )
                                )
                            }
                        }
                    }
                }
                navPoints.add(dest) // Cap off the list with the destination coordinate
                RouteData(encodedPolyline, navPoints)
            } else {
                throw Exception("No route was returned from the server.")
            }
        } else {
            val reader = BufferedReader(InputStreamReader(connection.errorStream))
            val response = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
            reader.close()
            throw Exception("HTTP error $responseCode: $response")
        }
    }
}
