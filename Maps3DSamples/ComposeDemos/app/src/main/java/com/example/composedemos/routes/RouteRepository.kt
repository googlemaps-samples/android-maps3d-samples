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

package com.example.composedemos.routes

import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class RouteData(
    val encodedPolyline: String,
    val navPoints: List<LatLng>,
)

class RouteRepository {

    suspend fun fetchRoute(
        apiKey: String,
        origin: LatLng,
        dest: LatLng,
    ): RouteData = withContext(Dispatchers.IO) {
        val url = URL("https://routes.googleapis.com/directions/v2:computeRoutes")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("X-Goog-Api-Key", apiKey)
        connection.setRequestProperty("X-Goog-FieldMask", "routes.polyline.encodedPolyline,routes.legs.steps.startLocation")
        connection.doOutput = true

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
                        },
                    ),
                ),
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
                        },
                    ),
                ),
            )
            put("travelMode", "DRIVE")
        }

        OutputStreamWriter(connection.outputStream).use {
            it.write(requestBody.toString())
            it.flush()
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
                                val latLng = startLocation.getJSONObject("latLng")
                                navPoints.add(
                                    LatLng(
                                        latLng.getDouble("latitude"),
                                        latLng.getDouble("longitude"),
                                    ),
                                )
                            }
                        }
                    }
                }
                navPoints.add(dest) // Add destination as last point

                RouteData(encodedPolyline, navPoints)
            } else {
                throw Exception("No route returned from the Maps API.")
            }
        } else {
            val reader = BufferedReader(InputStreamReader(connection.errorStream))
            val response = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
            reader.close()
            throw Exception("API Error (HTTP $responseCode): $response")
        }
    }
}
