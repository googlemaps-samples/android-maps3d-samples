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

package com.example.maps3djava.routes;

import com.google.android.gms.maps.model.LatLng;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Represents the decoded route and step waypoints payload for the Java Routes API sample.
 */
class RouteData {
    private final String encodedPolyline;
    private final List<LatLng> navPoints;

    public RouteData(String encodedPolyline, List<LatLng> navPoints) {
        this.encodedPolyline = encodedPolyline;
        this.navPoints = navPoints;
    }

    public String getEncodedPolyline() {
        return encodedPolyline;
    }

    public List<LatLng> getNavPoints() {
        return navPoints;
    }
}

/**
 * A data repository responsible for executing background network tasks to compute driving
 * directions using the Google Maps Routes API (v2) in Java.
 */
public class RouteRepository {

    /**
     * Returns a Callable to fetch the route in a background thread pool.
     *
     * @param apiKey The API key to authenticate client requests.
     * @param origin Starting coordinate.
     * @param destination Destination coordinate.
     * @return A Callable producing [RouteData].
     */
    public Callable<RouteData> fetchRouteCallable(String apiKey, LatLng origin, LatLng destination) {
        return () -> {
            URL url = new URL("https://routes.googleapis.com/directions/v2:computeRoutes");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("X-Goog-Api-Key", apiKey);
            connection.setRequestProperty("X-Goog-FieldMask", "routes.polyline.encodedPolyline,routes.legs.steps.startLocation");
            connection.setDoOutput(true);

            // Structure the JSON request body manually using standard JSONObjects
            JSONObject requestBody = new JSONObject();
            
            JSONObject originLatLng = new JSONObject()
                    .put("latitude", origin.latitude)
                    .put("longitude", origin.longitude);
            requestBody.put("origin", new JSONObject()
                    .put("location", new JSONObject().put("latLng", originLatLng)));

            JSONObject destLatLng = new JSONObject()
                    .put("latitude", destination.latitude)
                    .put("longitude", destination.longitude);
            requestBody.put("destination", new JSONObject()
                    .put("location", new JSONObject().put("latLng", destLatLng)));

            requestBody.put("travelMode", "DRIVE");

            // Stream payload to connection output
            try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream())) {
                writer.write(requestBody.toString());
                writer.flush();
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                StringBuilder response = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray routes = jsonResponse.getJSONArray("routes");
                if (routes.length() > 0) {
                    JSONObject route = routes.getJSONObject(0);
                    JSONObject polyline = route.getJSONObject("polyline");
                    String encodedPolyline = polyline.getString("encodedPolyline");

                    List<LatLng> navPoints = new ArrayList<>();
                    JSONArray legs = route.optJSONArray("legs");
                    if (legs != null && legs.length() > 0) {
                        JSONObject leg = legs.getJSONObject(0);
                        JSONArray steps = leg.optJSONArray("steps");
                        if (steps != null) {
                            for (int i = 0; i < steps.length(); i++) {
                                JSONObject step = steps.getJSONObject(i);
                                JSONObject startLocation = step.optJSONObject("startLocation");
                                if (startLocation != null) {
                                    JSONObject latLngObj = startLocation.getJSONObject("latLng");
                                    navPoints.add(new LatLng(
                                            latLngObj.getDouble("latitude"),
                                            latLngObj.getDouble("longitude")
                                    ));
                                }
                            }
                        }
                    }
                    navPoints.add(destination); // Cap route off with final destination
                    return new RouteData(encodedPolyline, navPoints);
                } else {
                    throw new Exception("No route details returned from the server.");
                }
            } else {
                StringBuilder errorResponse = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorResponse.append(line);
                    }
                }
                throw new Exception("HTTP error " + responseCode + ": " + errorResponse);
            }
        };
    }
}
